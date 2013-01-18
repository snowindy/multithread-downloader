package com.github.snowindy.util;


import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public class ClasspathUtils {
    public static InputStream getResourceAsStream(String path) {
        return ClasspathUtils.class.getResourceAsStream(path);
    }

    public static String readResourceUTF8(String path) {
        InputStream in = getResourceAsStream(path);
        try {
            return IOUtils.toString(in, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(in);
        }

    }
}