package com.yoho.lib.http;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.squareup.okhttp.Dispatcher;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.yoho.lib.core.ThreadPool;
import com.yoho.lib.util.StringUtil;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okio.Buffer;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;

/**
 * Created by fanchao on 15/10/27.
 */
public class OKHTTPClient extends AbstractHttpClient {

    private static final String TAG = "OKHTTPClient";
    private final ThreadPool pool;

    public OKHTTPClient(ThreadPool pool) {
        this.pool = pool;
    }

    @Override
    public int head(String url, int connectTimeoutInMillis, Map<String, List<String>> outputHeaders) throws IOException {
        OkHttpClient okHttpClient = newOkHttpClient();
        okHttpClient.setConnectTimeout(connectTimeoutInMillis, TimeUnit.MILLISECONDS);
        okHttpClient.setFollowRedirects(false);
        Request req = new Request.Builder().
                url(url).
                header("User-Agent", DEFAULT_USER_AGENT).
                head().
                build();
        Response resp = okHttpClient.newCall(req).execute();
        copyMultiMap(resp.headers().toMultimap(), outputHeaders);
        return resp.code();
    }

    @Override
    public byte[] getBytes(String url, int timeout, String userAgent, String referrer, String cookie) {
        byte[] result = null;
        OkHttpClient okHttpClient = newOkHttpClient();
        Request.Builder builder = prepareRequestBuilder(okHttpClient, url, timeout, userAgent, referrer, cookie);
        try {
            result = getSyncResponse(okHttpClient, builder).body().bytes();
        } catch (IOException e) {
            Log.e(TAG, "Error getting bytes from http body response: " + e.getMessage(), e);
        }

        return result;
    }

    @Override
    public String get(String url, int timeout, String userAgent, String referrer, String cookie, Map<String, String> customHeaders) throws IOException {
        String result = null;
        OkHttpClient okHttpClient = newOkHttpClient();
        Request.Builder builder = prepareRequestBuilder(okHttpClient, url, timeout, userAgent, referrer, cookie);
        addCustomHeaders(customHeaders, builder);
        try {
            result = getSyncResponse(okHttpClient, builder).body().string();
        } catch (IOException ioe) {
            throw ioe;
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return result;
    }


    @Override
    public void save(String url, File file, boolean resume, int timeout, String userAgent, String referrer) throws IOException {
        FileOutputStream fos;
        long rangeStart;
        canceled = false;
        if (resume && file.exists()) {
            fos = new FileOutputStream(file, true);
            rangeStart = file.length();
        } else {
            fos = new FileOutputStream(file, false);
            rangeStart = -1;
        }

        OkHttpClient okHttpClient = newOkHttpClient();
        Request.Builder builder = prepareRequestBuilder(okHttpClient, url, timeout, userAgent, referrer, null);
        addRangeHeader(rangeStart, -1, builder);
        Response response = getSyncResponse(okHttpClient, builder);
        Headers headers = response.headers();
        onHeaders(headers);
        InputStream in = response.body().byteStream();

        byte[] b = new byte[4096];
        int n;
        while (!canceled && (n = in.read(b, 0, b.length)) != -1) {
            if (!canceled) {
                fos.write(b, 0, n);
                onData(b, 0, n);
            }
        }
        closeQuietly(fos);
        if (canceled) {
            onCancel();
        } else {
            onComplete();
        }
    }

    private void onHeaders(Headers headers) {
        if (getListener() != null) {
            try {
                getListener().onHeaders(this, headers.toMultimap());
            } catch (Exception e) {
                Log.w(TAG, e.getMessage(), e);
            }
        }
    }

    @Override
    public String post(String url, int timeout, String userAgent, Map<String, String> formData) throws IOException {
        return post(url, timeout, userAgent, "application/x-www-form-urlencoded; charset=utf-8", getFormDataBytes(formData), false);
    }

    @Override
    public String post(String url, int timeout, String userAgent, String content, String postContentType, boolean gzip) throws IOException {
        return post(url, timeout, userAgent, postContentType, content.getBytes("UTF-8"), gzip);
    }

    private String post(String url, int timeout, String userAgent, String postContentType, byte[] postData, boolean gzip) throws IOException {
        canceled = false;
        OkHttpClient okHttpClient = newOkHttpClient();
        Request.Builder builder = prepareRequestBuilder(okHttpClient, url, timeout, userAgent, null, null);
        RequestBody requestBody = RequestBody.create(MediaType.parse(postContentType), postData);
        prepareOkHttpClientForPost(okHttpClient, gzip);
        builder.post(requestBody);
        return getPostSyncResponse(builder);
    }

    private String getPostSyncResponse(Request.Builder builder) throws IOException{
        String result = null;
        OkHttpClient okHttpClient = newOkHttpClient();
        Response response = this.getSyncResponse(okHttpClient, builder);
        int httpResponseCode = response.code();

        if ((httpResponseCode != HttpURLConnection.HTTP_OK) && (httpResponseCode != HttpURLConnection.HTTP_PARTIAL)) {
            throw new ResponseCodeNotSupportedException(httpResponseCode);
        }

        if (canceled) {
            onCancel();
        } else {
            result = response.body().string();
            onComplete();
        }

        return result;
    }

    private void prepareOkHttpClientForPost(OkHttpClient okHttpClient, boolean gzip) {
        okHttpClient.setFollowRedirects(false);
        okHttpClient.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        okHttpClient.setSslSocketFactory(CUSTOM_SSL_SOCKET_FACTORY);
        if (gzip) {
            if (okHttpClient.interceptors().size() > 0) {
                okHttpClient.interceptors().remove(0);
                okHttpClient.interceptors().add(0, new GzipRequestInterceptor());
            }
        }
    }

    private void addRangeHeader(long rangeStart, long rangeEnd, Request.Builder builderRef) {
        if (rangeStart < 0) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("bytes=");
        sb.append(String.valueOf(rangeStart));
        sb.append("-");
        if (rangeEnd > 0 && rangeEnd > rangeStart) {
            sb.append(String.valueOf(rangeEnd));
        }
        builderRef.addHeader("Range", sb.toString());
    }

    private Request.Builder prepareRequestBuilder(OkHttpClient okHttpClient, String url, int timeout, String userAgent, String referrer, String cookie) {
        okHttpClient.setConnectTimeout(timeout, TimeUnit.MILLISECONDS);
        okHttpClient.setReadTimeout(timeout, TimeUnit.MILLISECONDS);
        okHttpClient.setWriteTimeout(timeout, TimeUnit.MILLISECONDS);
        okHttpClient.interceptors().clear();
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        if (!StringUtil.isNullOrEmpty(userAgent)) {
            builder.header("User-Agent", userAgent);
        }
        if (!StringUtil.isNullOrEmpty(referrer)) {
            builder.header("Referer", referrer);
        }
        if (!StringUtil.isNullOrEmpty(cookie)) {
            builder.header("Cookie", cookie);
        }

        return builder;
    }

    private void addCustomHeaders(Map<String, String> customHeaders, Request.Builder builder) {
        if (customHeaders != null && customHeaders.size() > 0) {
            try {
                for (Map.Entry<String, String> header : customHeaders.entrySet()) {
                    builder.header(header.getKey(), header.getValue());
                }
            } catch (Throwable e) {
                Log.w(TAG, e.getMessage(), e);
            }
        }
    }

    private Response getSyncResponse(OkHttpClient okHttpClient, Request.Builder builder) throws IOException {
        Request request = builder.build();
        return okHttpClient.newCall(request).execute();
    }

    private OkHttpClient newOkHttpClient() {
        return newOkHttpClient(pool);
    }

    public static OkHttpClient newOkHttpClient(ThreadPool pool) {
        OkHttpClient client = new OkHttpClient();
        client.setDispatcher(new Dispatcher(pool));
        client.setFollowRedirects(true);
        client.setFollowSslRedirects(true);
        client.setConnectTimeout(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);

        return client;
    }

    class GzipRequestInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            if (originalRequest.body() == null || originalRequest.header("Content-Encoding") != null) {
                return chain.proceed(originalRequest);
            }

            Request compressedRequest = originalRequest.newBuilder()
                    .header("Content-Encoding", "gzip")
                    .method(originalRequest.method(), forceContentLength(gzip(originalRequest.body())))
                    .build();
            return chain.proceed(compressedRequest);
        }

        /** https://github.com/square/okhttp/issues/350 */
        private RequestBody forceContentLength(final RequestBody requestBody) throws IOException {
            final Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);
            return new RequestBody() {
                @Override
                public MediaType contentType() {
                    return requestBody.contentType();
                }

                @Override
                public long contentLength() {
                    return buffer.size();
                }

                @Override
                public void writeTo(BufferedSink sink) throws IOException {
                    sink.write(buffer.snapshot());
                }
            };
        }

        private RequestBody gzip(final RequestBody body) {
            return new RequestBody() {
                @Override
                public MediaType contentType() {
                    return body.contentType();
                }

                @Override
                public long contentLength() {
                    return -1; // We don't know the compressed length in advance!
                }

                @Override
                public void writeTo(BufferedSink sink) throws IOException {
                    BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));
                    body.writeTo(gzipSink);
                    gzipSink.close();
                }
            };
        }
    }
}
