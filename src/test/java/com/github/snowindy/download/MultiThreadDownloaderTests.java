package com.github.snowindy.download;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

import com.github.snowindy.util.TestUtils;

public class MultiThreadDownloaderTests {

    @Test
    public void testCountChunks() {
        assertEquals(10, MultiThreadDownloader.countChunks(100, 10));
        assertEquals(11, MultiThreadDownloader.countChunks(109, 10));
        assertEquals(1, MultiThreadDownloader.countChunks(200, 1000));
    }

    @Test
    public void testCalculateChunkSize() {
        assertEquals(20, MultiThreadDownloader.calculateChunkSize(100, 5));
        assertEquals(1, MultiThreadDownloader.calculateChunkSize(4, 5));
        assertEquals(21, MultiThreadDownloader.calculateChunkSize(103, 5));
    }

    @Test
    public void testDownload() throws Exception {
        File destFile = new File("target/testDownload.flv");
        MultiThreadDownloader mtd = new MultiThreadDownloader(new URL("http://test1.ru/big-file.flv"), destFile);
        mtd.setChunkSize(50000);
        mtd.download();

        assertTrue(TestUtils.contentEquals(new File(
                "src/test/java/com/github/snowindy/download/resources/downloadable/big-file.flv"), destFile));
    }

    @Test
    public void testCleanupAfterDownload() throws Exception {
        File destFile = new File("target/testCleanupAfterDownload.txt");
        MultiThreadDownloader mtd = new MultiThreadDownloader(new URL("http://test1.ru/testMultithreadDownload.txt"),
                destFile);
        mtd.setChunkSize(100);

        mtd.download();

        assertFalse(mtd.tempDir.exists());
    }

    @Test
    public void testDownloadWithProgressCheck() throws InterruptedException, MalformedURLException {
        File destFile = new File("target/testDownloadWithProgressCheck.flv");
        final MultiThreadDownloader mtd = new MultiThreadDownloader(new URL("http://test1.ru/big-file.flv"), destFile);
        // A small chunk used to show progress. Don't use this size in
        // production.
        mtd.setChunkSize(10000);

        Thread t = new Thread(new Runnable() {

            public void run() {
                try {

                    mtd.download();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });

        t.start();

        double beginProgress = mtd.getProgress();

        double prevProgress = beginProgress;
        while (t.isAlive()) {
            double currProgress = mtd.getProgress();
            assertTrue(currProgress >= prevProgress);
            prevProgress = currProgress;
            System.out.println("Progress: " + currProgress);
            Thread.sleep(50);
        }

        double endProgress = mtd.getProgress();

        assertTrue(endProgress > beginProgress);
    }
}
