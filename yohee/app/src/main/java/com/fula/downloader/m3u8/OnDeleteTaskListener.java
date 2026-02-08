package com.fula.downloader.m3u8;

public interface OnDeleteTaskListener extends BaseListener {
    /**
     * 开始的时候回调
     */
    void onStart();

    /**
     * 非UI线程
     */
    void onSuccess();

    /**
     * 非UI线程
     */
    void onFail();
}
