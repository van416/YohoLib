package com.yoho.lib.http;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by fanchao on 15/10/28.
 */
public class VolleyHttpClient extends AbstractHttpClient {

    @Override
    public int head(String url, int connectTimeoutInMillis, Map<String, List<String>> outputHeaders) throws IOException {
        return 0;
    }

    @Override
    public String get(String url, int timeout, String userAgent, String referrer, String cookie, Map<String, String> customHeaders) throws IOException {
        return null;
    }

    @Override
    public byte[] getBytes(String url, int timeout, String userAgent, String referrer, String cookie) {
        return new byte[0];
    }

    @Override
    public void save(String url, File file, boolean resume, int timeout, String userAgent, String referrer) throws IOException {

    }

    @Override
    public String post(String url, int timeout, String userAgent, Map<String, String> formData) throws IOException {
        return null;
    }

    @Override
    public String post(String url, int timeout, String userAgent, String content, String postContentType, boolean gzip) throws IOException {
        return null;
    }
}
