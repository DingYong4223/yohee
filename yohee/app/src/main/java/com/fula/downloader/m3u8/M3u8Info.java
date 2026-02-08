package com.fula.downloader.m3u8;

import com.fula.base.ToolUtils;

import java.io.File;

public class M3u8Info {
    public static final String M3U8SURFFIX = "m3u8";
    public static final int THREAD_COUNT = 3;
    public static final int TIME_OUT = 10 * 1000;
    public static final int READ_TIME_OUT = 30 * 60 * 1000;
    public static final boolean DEBUGABLE = true;
    private String cacheRoot = null;
    private NameGenerator generator = null;

    public void setCacheRoot(String root) {
        this.cacheRoot = root;
    }

    public String getCacheRoot() {
        return cacheRoot;
    }

    public void setNameGenerator(NameGenerator ng) {
        this.generator = ng;
    }

    public NameGenerator setNameGenerator() {
        return generator;
    }

    public String genFilePath(String url) {
        if (null == generator) {
            return cacheRoot + File.separator + ToolUtils.md5(url);
        }
        return cacheRoot + File.separator + generator.generate(url);
    }

}
