package com.github.snowindy.download;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import com.github.snowindy.download.HttpUtils.HttpDetails;
import com.github.snowindy.util.FileUtil;

public class MultiThreadDownloader {
    private int chunkSize = 1000 * 1000;
    private int executorPoolSize = 5;

    private URL downloadUrl;

    private File destFile;

    private ExecutorService downloadExecutor;

    public int getChunkSize() {
        return chunkSize;
    }

    public MultiThreadDownloader(URL downloadUrl, File destFile) {
        this.downloadUrl = downloadUrl;
        this.destFile = destFile;
    }

    private boolean chunkSizeSetExplicitly;

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
        chunkSizeSetExplicitly = true;
    }

    public int getExecutorPoolSize() {
        return executorPoolSize;
    }

    public void setExecutorPoolSize(int executorPoolSize) {
        this.executorPoolSize = executorPoolSize;
    }

    public static int countChunks(int contentLength, int chunkSize) {
        int chunks = contentLength / chunkSize;
        if (chunks == 0) {
            chunks = 1;
        } else {
            if (contentLength % chunkSize != 0) {
                chunks++;
            }
        }
        return chunks;
    }

    public static int calculateChunkSize(int contentLength, int executorPoolSize) {
        if (executorPoolSize >= contentLength) {
            return 1;
        }

        int chunkSize = contentLength / executorPoolSize;

        if (contentLength % executorPoolSize != 0) {
            chunkSize++;
        }

        return chunkSize;
    }

    static String className = MultiThreadDownloader.class.getName();
    static Logger log = Logger.getLogger(className);

    private List<RangeDownloader> downloaders;

    public void stop() {
        downloadExecutor.shutdown();
        for (RangeDownloader dl : downloaders) {
            dl.stopWhenPossible();
        }
        tempDir.deleteOnExit();
    }

    File tempDir;

    int maxTimeToDownloadSeconds = 60 * 60 * 24 * 30;

    public int getMaxTimeToDownloadSeconds() {
        return maxTimeToDownloadSeconds;
    }

    public void setMaxTimeToDownloadSeconds(int maxTimeToDownloadSeconds) {
        this.maxTimeToDownloadSeconds = maxTimeToDownloadSeconds;
    }

    private int contentLength;

    private void downloadInner() throws ExecutionException, InterruptedException, IOException {
        downloadExecutor = Executors.newFixedThreadPool(executorPoolSize);

        HttpDetails details = HttpUtils.getHttpResourceDetails(downloadUrl);
        contentLength = details.contentLength;

        if (!chunkSizeSetExplicitly) {
            chunkSize = calculateChunkSize(details.contentLength, executorPoolSize);
        }

        int totalChunks = countChunks(details.contentLength, chunkSize);
        if (!"bytes".equals(details.acceptRanges)) {
            log.info(String.format(
                    "Server does not support byte ranges: '%s'. Falling back to a single-thread download process.",
                    details.acceptRanges));
            totalChunks = 1;
        }

        tempDir = new File(System.getProperty("java.io.tmpdir"), "mt-download-" + Math.round(Math.random() * 1000000));
        FileUtils.deleteDirectory(tempDir);
        tempDir.mkdirs();

        Collection<Future<?>> futures = new LinkedList<Future<?>>();
        downloaders = new ArrayList<RangeDownloader>();
        for (int i = 0; i < totalChunks; i++) {
            RangeDownloader rd = prepareRangeDownloader(tempDir, i, totalChunks, details);
            downloaders.add(rd);
            futures.add(downloadExecutor.submit(new RunnableTask(rd)));
        }

        try {
            long start = System.currentTimeMillis();
            long timeout = maxTimeToDownloadSeconds * 1000L;
            for (Future<?> future : futures) {
                long curr = System.currentTimeMillis();
                long diff = curr - start;
                long timeoutForThisFuture = timeout - diff;
                future.get(timeoutForThisFuture, TimeUnit.MILLISECONDS);
            }

        } catch (TimeoutException e) {
            throw new RuntimeException("Timed out waiting for download tasks.", e);
        }

    }

    public void download() {
        try {
            downloadInner();
            combineDownloadParts();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void combineDownloadParts() throws IOException {
        List<File> parts = new ArrayList<File>();
        for (RangeDownloader dl : downloaders) {
            parts.add(dl.getDestFile());
        }
        FileUtil.combineFilePartsAndDelete(destFile, parts);
        FileUtils.deleteDirectory(tempDir);
    }

    class RunnableTask implements Runnable {
        RangeDownloader rd;
        RangeDownloadCompleteListener listener;

        public RunnableTask(RangeDownloader rd) {
            this.rd = rd;
        }

        public void run() {
            log.fine(String.format("Starting download for range [%s, %s], url: %s.", rd.getStartByteIdx(),
                    rd.getEndByteIdx(), rd.getDownloadUrl()));
            try {
                rd.download();
                if (listener != null) {
                    listener.onComplete(rd);
                }
                log.fine(String.format("Completed download for range [%s, %s], url: %s. \n Dest file: %s.", rd
                        .getStartByteIdx(), rd.getEndByteIdx(), rd.getDownloadUrl(), rd.getDestFile().getAbsolutePath()));
            } catch (Exception e) {
                log.logp(Level.WARNING, className, "run", String.format(
                        "Could not complete download for range [%s, %s], url: %s.", rd.getStartByteIdx(),
                        rd.getEndByteIdx(), rd.getDownloadUrl()), e);
            }
        }
    };

    private RangeDownloader prepareRangeDownloader(File tempDir, int i, int totalChunks, HttpDetails details) {
        File tmpFile = new File(tempDir, i + ".part");
        long startIdx = i * chunkSize;
        long endIdx;
        if (i == totalChunks - 1) {
            endIdx = details.contentLength;
        } else {
            endIdx = (i + 1) * chunkSize;
        }
        endIdx--;
        RangeDownloader rd = new RangeDownloader(downloadUrl, tmpFile, startIdx, endIdx);
        return rd;

    }

    public double getProgress() {
        if (contentLength == 0) {
            return 0;
        }
        if (downloaders == null) {
            return 0;
        }
        int sum = 0;
        for (RangeDownloader dl : downloaders) {
            sum += dl.getBytesDownloaded();
        }

        return ((double) sum) / contentLength;

    }

}
