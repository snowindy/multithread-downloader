package com.github.snowindy.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class RangeDownloader {
    private URL downloadUrl;

    private long startByteIdx;
    private long endByteIdx;

    public File getDestFile() {
        return destFile;
    }

    public URL getDownloadUrl() {
        return downloadUrl;
    }

    public long getStartByteIdx() {
        return startByteIdx;
    }

    public long getEndByteIdx() {
        return endByteIdx;
    }

    public boolean isStopped() {
        return stopped;
    }

    private File destFile;

    private transient boolean stopped;

    public RangeDownloader(URL downloadUrl, File destFile, long startByteIdx, long endByteIdx) {
        this.downloadUrl = downloadUrl;
        this.startByteIdx = startByteIdx;
        this.endByteIdx = endByteIdx;
        this.destFile = destFile;
    }

    public void stopWhenPossible() {
        stopped = true;
        destFile.deleteOnExit();
    }

    public static final int BUFFER_SIZE = 512;

    public static final long UNBOUNDED = -1;

    private int bytesDownloaded;

    public int getBytesDownloaded() {
        return bytesDownloaded;
    }

    public void download() {
        OutputStream outStream = null;
        URLConnection connection = null;
        InputStream inStream = null;
        try {
            try {
                destFile.delete();
                outStream = new FileOutputStream(destFile);

                connection = downloadUrl.openConnection();

                String endStr = endByteIdx == UNBOUNDED ? "" : String.valueOf(endByteIdx);

                connection.setRequestProperty("Range", "bytes=" + startByteIdx + "-" + endStr);

                connection.setDoInput(true);
                connection.setDoOutput(true);

                connection.connect();

                inStream = connection.getInputStream();

                int bytesRead;
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((bytesRead = inStream.read(buffer, 0, BUFFER_SIZE)) > 0) {
                    if (stopped) {
                        break;
                    }
                    bytesDownloaded += bytesRead;
                    outStream.write(buffer, 0, bytesRead);
                }
            } finally {
                if (outStream != null) {
                    outStream.close();
                }
                if (inStream != null) {
                    inStream.close();
                }
            }
            if (stopped) {
                destFile.delete();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
