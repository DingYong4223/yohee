package com.fula.downloader;

import com.fula.downloader.file.Extra;

import androidx.annotation.MainThread;

/**
 * @author cenxiaozhong
 * @date 2018/6/21
 * @update 4.0.0
 * @since 1.0.0
 */
public interface DownloadListener {

	@MainThread
	void onStart(DownloadTask task);

	@MainThread
	void onProgress(String url, long downloaded, long length, long speed);
	/**
	 * @param status 回调状态
	 * @param url       下载的地址
	 * @return true     处理了下载完成后续的事件 ，false 默认交给Downloader 处理
	 */
	@MainThread
	boolean onResult(int status, String url, Extra extra);
}
