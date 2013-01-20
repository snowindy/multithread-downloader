package com.github.snowindy.util;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

import com.github.snowindy.download.HttpUtils;
import com.github.snowindy.download.HttpUtils.HttpDetails;


public class HttpUtilsTest {
    @Test
    public void testGetHttpResourceDetails() throws MalformedURLException {
        HttpDetails details = HttpUtils.getHttpResourceDetails(new URL("http://test1.ru/testRangeDownload.txt"));

        assertEquals("bytes", details.acceptRanges);
        assertEquals(6, details.contentLength);
    }
}
