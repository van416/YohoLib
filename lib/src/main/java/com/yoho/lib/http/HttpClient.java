package com.yoho.lib.http;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by fanchao on 15/10/27.
 */
public interface HttpClient {

    void setListener(HttpClientListener listener);

    HttpClientListener getListener();

    void onCancel();

    void onData(byte[] b, int i, int n);

    void onError(Exception e);

    void onComplete();

    int head(String url, int connectTimeoutInMillis, Map<String, List<String>> outputHeaders) throws IOException;

    String get(String url) throws IOException;

    String get(String url, int timeout) throws IOException;

    String get(String url, int timeout, String userAgent) throws IOException;

    String get(String url, int timeout, String userAgent, String referrer, String cookie) throws IOException;

    String get(String url, int timeout, String userAgent, String referrer, String cookie, Map<String, String> customHeaders) throws IOException;

    byte[] getBytes(String url);

    byte[] getBytes(String url, int timeout);

    byte[] getBytes(String url, int timeout, String referrer);

    byte[] getBytes(String url, int timeout, String userAgent, String referrer);

    byte[] getBytes(String url, int timeout, String userAgent, String referrer, String cookies);

    void save(String url, File file) throws IOException;

    void save(String url, File file, boolean resume) throws IOException;

    void save(String url, File file, boolean resume, int timeout, String userAgent) throws IOException;

    void save(String url, File file, boolean resume, int timeout, String userAgent, String referrer) throws IOException;

    String post(String url, int timeout, String userAgent, Map<String, String> formData) throws IOException;

    String post(String url, int timeout, String userAgent, String content, boolean gzip) throws IOException;

    String post(String url, int timeout, String userAgent, String content, String postContentType, boolean gzip) throws IOException;

    void cancel();

    boolean isCanceled();

    interface HttpClientListener {

        void onError(HttpClient client, Throwable e);

        void onData(HttpClient client, byte[] buffer, int offset, int length);

        void onComplete(HttpClient client);

        void onCancel(HttpClient client);

        void onHeaders(HttpClient client, Map<String, List<String>> headerFields);
    }

    abstract class HttpClientListenerAdapter implements HttpClientListener {

        public void onError(HttpClient client, Throwable e) {
        }

        public void onData(HttpClient client, byte[] buffer, int offset, int length) {
        }

        public void onComplete(HttpClient client) {
        }

        public void onCancel(HttpClient client) {
        }

        public void onHeaders(HttpClient client, Map<String, List<String>> headerFields) {
        }
    }

    class HttpRangeException extends IOException {

        HttpRangeException(String message) {
            super(message);
        }
    }

    final class RangeNotSupportedException extends HttpRangeException {

        RangeNotSupportedException(String message) {
            super(message);
        }
    }

    final class HttpRangeOutOfBoundsException extends HttpRangeException {

        HttpRangeOutOfBoundsException(int rangeStart, long expectedFileSize) {
            super("HttpRange Out of Bounds error: start=" + rangeStart + " expected file size=" + expectedFileSize);
        }
    }

    final class ResponseCodeNotSupportedException extends IOException {
        private final int responseCode;

        ResponseCodeNotSupportedException(int code) {
            responseCode = code;
        }

        int getResponseCode() {
            return responseCode;
        }
    }
}
