package com.yoho.lib.http;

/**
 * Created by fanchao on 15/10/28.
 */
public enum HttpContext {
    /**
     * 所有api的查询方式,表现在线程池上
     */
    API,

    /**
     * 下载的方式,表现在线程池上
     */
    DOWNLOAD
}
