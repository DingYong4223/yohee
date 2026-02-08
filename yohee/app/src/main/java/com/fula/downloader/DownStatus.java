package com.fula.downloader;

public class DownStatus {

    public static final int STATUS_NEW = 1000;
    public static final int STATUS_PENDDING = 1001;
    public static final int STATUS_DOWNLOADING = 0x399;
    public static final int STATUS_PAUSED = 0x404;
    public static final int STOPPING = 0x398;
    public static final int ERROR_NETWORK_CONNECTION = 0x400;
    public static final int ERROR_RESPONSE_STATUS = 0x401;
    public static final int ERROR_STORAGE = 0x402;
    public static final int ERROR_TIME_OUT = 0x403;
    public static final int ERROR_USER_CANCEL = 0x406;
    public static final int ERROR_SHUTDOWN = 0x407;
    public static final int ERROR_TOO_MANY_REDIRECTS = 0x408;
    public static final int ERROR_LOAD = 0x409;
    public static final int ERROR_RESOURCE_NOT_FOUND = 0x410;
    public static final int ERROR_SERVICE = 0x503;
    public static final int STATUS_COMPLETED = 0x200;
    public static final int HTTP_RANGE_NOT_SATISFIABLE = 416;

}
