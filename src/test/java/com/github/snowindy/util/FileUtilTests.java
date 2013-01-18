package com.github.snowindy.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.github.snowindy.util.FileUtil;

import static org.junit.Assert.*;

public class FileUtilTests {

    @Test
    public void testFileCombination() throws IOException {
        File dest = new File("target/test-combination.txt");
        File dest1 = new File("target/test-combination1.txt");

        dest.delete();
        dest1.delete();

        List<File> parts = new ArrayList<File>();
        parts.add(new File("src/test/java/com/github/snowindy/util/file/combine/1.txt"));
        parts.add(new File("src/test/java/com/github/snowindy/util/file/combine/2.txt"));
        parts.add(new File("src/test/java/com/github/snowindy/util/file/combine/3.txt"));
        parts.add(new File("src/test/java/com/github/snowindy/util/file/combine/4.txt"));

        FileUtil.combineFileParts(dest, parts);

        String cnt = FileUtils.readFileToString(dest, "UTF-8");
        assertEquals("1234", cnt);

        List<File> parts1 = new ArrayList<File>();
        parts1.add(dest);

        FileUtil.combineFilePartsAndDelete(dest1, parts1);

        assertTrue("1234".equals(FileUtils.readFileToString(dest1, "UTF-8")));
        assertTrue(!dest.exists());

        dest1.delete();
    }

}
