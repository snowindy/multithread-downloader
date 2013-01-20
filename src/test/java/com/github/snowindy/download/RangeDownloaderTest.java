package com.github.snowindy.download;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.github.snowindy.download.RangeDownloader;
import com.github.snowindy.util.TestUtils;

public class RangeDownloaderTest {

    @Test
    public void testRangeDownloadFromLocalMachine() throws Exception {
        testRangeDownloadFromLocalMachine("target/testRangeDownloadFromLocalMachine.txt",
                "http://test1.ru/testRangeDownload.txt", 0, 2, "123");
        testRangeDownloadFromLocalMachine("target/testRangeDownloadFromLocalMachine.txt",
                "http://test1.ru/testRangeDownload.txt", 3, 5, "456");
    }

    public void testRangeDownloadFromLocalMachine(String targetFile, String fromUrl, int rangeBegin, int rangeEnd,
                                                  String expected) throws Exception {
        File destFile = new File(targetFile);
        RangeDownloader rd = new RangeDownloader(new URL(fromUrl), destFile, rangeBegin, rangeEnd);

        rd.download();

        assertTrue(destFile.exists());

        String partContent = FileUtils.readFileToString(destFile, "UTF-8");

        assertEquals(expected, partContent);

        destFile.delete();
    }

    @Test
    public void testRangeDownloadBigFile() throws Exception {
        File destFile = new File("target/testRangeDownloadBigFile.flv");
        RangeDownloader rd = new RangeDownloader(new URL("http://test1.ru/big-file.flv"), destFile, 0,
                RangeDownloader.UNBOUNDED);

        rd.download();

        FileInputStream fisMain = null;
        FileInputStream fisDownloaded = null;
        try {
            fisMain = new FileInputStream(new File(
                    "src/test/java/com/github/snowindy/download/resources/downloadable/big-file.flv"));
            fisDownloaded = new FileInputStream(destFile);

            assertTrue(TestUtils.contentEquals(fisMain, fisDownloaded));
        } finally {
            fisMain.close();
            fisDownloaded.close();
        }
    }

}
