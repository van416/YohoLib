package com.yoho.lib.http;

import com.yoho.lib.core.ThreadPool;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by fanchao on 15/10/28.
 */
public class HttpClientFactory {

    private static Map<HttpContext, ThreadPool> okHttpClientPools = null;

    private HttpClientFactory() {
    }

    private static HttpClient jdkInstance() {
        return new JdkHttpClient();
    }

    private static HttpClient volleyInstance() {
        return new JdkHttpClient();
    }

    public static HttpClient getInstance() {
        return getInstance(HttpContext.API);
    }

    public static HttpClient getInstance(HttpContext context) {
        if (okHttpClientPools == null) {
            okHttpClientPools = buildThreadPools();
        }
        return new OKHTTPClient(okHttpClientPools.get(context));
    }

    private static Map<HttpContext, ThreadPool> buildThreadPools() {
        HashMap<HttpContext, ThreadPool> map = new HashMap<HttpContext, ThreadPool>();
        map.put(HttpContext.API, new ThreadPool("OkHttpClient-api", 2, 10, 60, new LinkedBlockingQueue<Runnable>(), true));
        map.put(HttpContext.DOWNLOAD, new ThreadPool("OkHttpClient-downloads", 1, 10, 5, new LinkedBlockingQueue<Runnable>(), true));
        return map;
    }
}
