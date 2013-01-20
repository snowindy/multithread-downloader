package com.github.snowindy.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TestUtils {
    public static boolean contentEquals(InputStream input1, InputStream input2) throws IOException {
        if (!(input1 instanceof BufferedInputStream)) {
            input1 = new BufferedInputStream(input1);
        }
        if (!(input2 instanceof BufferedInputStream)) {
            input2 = new BufferedInputStream(input2);
        }

        int ch = input1.read();
        while (-1 != ch) {
            int ch2 = input2.read();
            if (ch != ch2) {
                return false;
            }
            ch = input1.read();
        }

        int ch2 = input2.read();
        return (ch2 == -1);
    }

    public static boolean contentEquals(File f1, File f2) {
        try {
            FileInputStream fisMain = null;
            FileInputStream fisDownloaded = null;
            try {
                fisMain = new FileInputStream(f1);
                fisDownloaded = new FileInputStream(f2);

                return TestUtils.contentEquals(fisMain, fisDownloaded);
            } finally {
                fisMain.close();
                fisDownloaded.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
