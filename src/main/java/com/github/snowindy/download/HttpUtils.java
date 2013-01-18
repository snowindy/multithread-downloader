package com.github.snowindy.download;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class HttpUtils {
    public static HttpDetails getHttpResourceDetails(URL url) {
        try {
            URLConnection conn2 = url.openConnection();
            conn2.connect();
            int k = conn2.getContentLength();
            String ar = conn2.getHeaderField("Accept-Ranges");

            HttpDetails det = new HttpDetails();
            det.contentLength = k;
            det.acceptRanges = ar;
            
            InputStream inStream = conn2.getInputStream();
            inStream.close();

            return det;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static class HttpDetails {
        public int contentLength;
        public String acceptRanges;
    }
}
