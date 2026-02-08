package com.fula.downloader;

import android.content.Context;
import android.text.TextUtils;

import com.fula.downloader.file.Extra;
import com.fula.downloader.m3u8.bean.M3U8;
import com.fula.CLog;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;

public class DownloadTask extends Extra implements Serializable, Cloneable {

    int mId = DownRuntime.getInstance().generateGlobalId();
    public long mTotalsLength;
    public long mDownedLength;
    public long speed;
    public Context mContext;
    public File mFile;
    DownloadListener mDownloadListener;
    public String authority = "";
    public boolean isCustomFile = false;
    public boolean uniquePath = true;
    public int connectTimes = 0;
    public M3U8 m3U8;
    private AtomicInteger status = new AtomicInteger(DownStatus.STATUS_NEW);

    @IntDef({DownStatus.STATUS_NEW, DownStatus.STATUS_PENDDING, DownStatus.STATUS_DOWNLOADING, DownStatus.STATUS_PAUSED, DownStatus.STATUS_COMPLETED})
    @interface DownloadTaskStatus {
    }

    public DownloadTask() {
        super();
    }

    public DownloadTask(String url) {
        super();
        this.mUrl = url;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DownloadTask) {
            DownloadTask m3U8Task = (DownloadTask) obj;
            return mUrl != null && mUrl.equals(m3U8Task.getUrl());
        }
        return false;
    }

    public int getStatus() {
        return status.get();
    }

    public void setStatus(int status) {
        this.status.set(status);
    }

    public int getId() {
        return this.mId;
    }

    public Context getContext() {
        return mContext;
    }

    public DownloadTask setContext(Context context) {
        mContext = context.getApplicationContext();
        return this;
    }

    public DownloadTask setEnableIndicator(boolean enableIndicator) {
        if (enableIndicator && mFile != null && TextUtils.isEmpty(authority)) {
            CLog.i(" Custom file path, you must specify authority, otherwise the notification should not be turned on");
            this.mEnableIndicator = false;
        } else {
            this.mEnableIndicator = enableIndicator;
        }
        return this;
    }

    public DownloadTask setFile(@NonNull File file) {
        mFile = file;
        this.authority = "";
        checkCustomFilePath(file);
        return this;
    }

    private void checkCustomFilePath(File file) {
        if (file == null || file.getAbsolutePath().startsWith(DownRuntime.getInstance().getDefaultDir(this.getContext()).getAbsolutePath())) {
            isCustomFile = false;
        } else if (!TextUtils.isEmpty(this.authority)) {
            setEnableIndicator(true);
            isCustomFile = true;
        } else {
            setEnableIndicator(false);
            isCustomFile = true;
        }
    }

    public DownloadTask setFile(@NonNull File file, @NonNull String authority) {
        this.mFile = file;
        this.authority = authority;
        checkCustomFilePath(file);
        return this;
    }

    public void pause() {
    }

    public void destroy() {
        this.mId = -1;
        this.mUrl = null;
        this.mContext = null;
        this.mFile = null;
        this.mIsParallelDownload = false;
        mIsForceDownload = false;
        mEnableIndicator = true;
        mDownloadIcon = android.R.drawable.stat_sys_download;
        mDownloadDoneIcon = android.R.drawable.stat_sys_download_done;
        mIsParallelDownload = true;
        mIsBreakPointDownload = true;
        mUserAgent = "";
        mContentDisposition = "";
        mMimetype = "";
        mContentLength = -1L;
        if (mHeaders != null) {
            mHeaders.clear();
            mHeaders = null;
        }
        status.set(DownStatus.STATUS_NEW);
    }

    public DownloadListener getDownloadingListener() {
        return mDownloadListener;
    }

    public DownloadListener getDownloadListener() {
        return mDownloadListener;
    }

    public DownloadTask setDownloadListener(DownloadListener downloadListener) {
        mDownloadListener = downloadListener;
        return this;
    }

    public DownloadTask addHeader(String key, String value) {
        if (this.mHeaders == null) {
            this.mHeaders = new ArrayMap<>();
        }
        this.mHeaders.put(key, value);
        return this;
    }

    public DownloadTask setConnectTimeOut(int connectTimeOut) {
        this.connectTimeOut = connectTimeOut;
        return this;
    }

    @Override
    public DownloadTask clone() {
        try {
            DownloadTask downloadTask = (DownloadTask) super.clone();
            downloadTask.mId = DownRuntime.getInstance().generateGlobalId();
            return downloadTask;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return new DownloadTask();
        }
    }

}
