package com.fula.downloader.file;

import android.content.Context;

import com.fula.downloader.DownRuntime;
import com.fula.downloader.DownloadListener;
import com.fula.downloader.DownloadTask;

import java.io.File;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;

/**
 * @author cenxiaozhong
 * @date 2019/2/9
 * @since 1.0.0
 */
public class ResourceRequest<T extends DownloadTask> {
	private DownloadTask mDownloadTask;

	static ResourceRequest with(Context context) {
		ResourceRequest resourceRequest = new ResourceRequest();
		resourceRequest.mDownloadTask = DownRuntime.getInstance().getDefaultDownloadTask();
		resourceRequest.mDownloadTask.setContext(context);
		return resourceRequest;
	}

	public ResourceRequest url(@NonNull String url) {
		mDownloadTask.mUrl = url;
		return this;
	}

	public ResourceRequest target(@Nullable File target) {
		mDownloadTask.setFile(target);
		return this;
	}

	public ResourceRequest setUniquePath(boolean uniquePath) {
		mDownloadTask.uniquePath = uniquePath;
		return this;
	}

	public ResourceRequest setRetryTime(int retryTime) {
		mDownloadTask.retryTimeIfFail = retryTime;
		return this;
	}

	public ResourceRequest target(@NonNull File target, @NonNull String authority) {
		mDownloadTask.setFile(target, authority);
		return this;
	}

	protected ResourceRequest setContentLength(long contentLength) {
		mDownloadTask.mContentLength = contentLength;
		return this;
	}


	public ResourceRequest setDownloadTimeOut(long downloadTimeOut) {
		mDownloadTask.downloadTimeOut = downloadTimeOut;
		return this;
	}

	public ResourceRequest setConnectTimeOut(int connectTimeOut) {
		mDownloadTask.connectTimeOut = connectTimeOut;
		return this;
	}

	public ResourceRequest setOpenBreakPointDownload(boolean openBreakPointDownload) {
		mDownloadTask.mIsBreakPointDownload = openBreakPointDownload;
		return this;
	}

	public ResourceRequest setForceDownload(boolean force) {
		mDownloadTask.mIsForceDownload = force;
		return this;
	}

	public ResourceRequest setEnableIndicator(boolean enableIndicator) {
		mDownloadTask.mEnableIndicator = enableIndicator;
		return this;
	}


	public ResourceRequest setIcon(@DrawableRes int icon) {
		mDownloadTask.mDownloadIcon = icon;
		return this;
	}

	public ResourceRequest setParallelDownload(boolean parallelDownload) {
		mDownloadTask.mIsParallelDownload = parallelDownload;
		return this;
	}

	public ResourceRequest addHeader(String key, String value) {
		if (mDownloadTask.mHeaders == null) {
			mDownloadTask.mHeaders = new ArrayMap<>();
		}
		mDownloadTask.mHeaders.put(key, value);
		return this;
	}

	public ResourceRequest setAutoOpen(boolean autoOpen) {
		mDownloadTask.mAutoOpen = autoOpen;
		return this;
	}

	public File get() {
		return DownloadImpl.getInstance().call(mDownloadTask);
	}

	public void enqueue(DownloadListener downloadListener) {
		mDownloadTask.setDownloadListener(downloadListener);
		DownloadImpl.getInstance().enqueue(mDownloadTask);
	}

}
