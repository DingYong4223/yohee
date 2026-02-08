package com.fula.downloader.file;

import java.io.Serializable;
import java.util.Map;

import androidx.annotation.DrawableRes;

/**
 * @author cenxiaozhong
 * @date 2019/2/8
 * @since 1.0.0
 */
public class Extra implements Serializable, Cloneable {

	private static final int DEF_RETRY_TIME = 3;
	/**
	 * 强制下载
	 */
	public boolean mIsForceDownload = false;
	/**默认重试次数*/
	protected int retryTimeIfFail = DEF_RETRY_TIME;
	/**
	 * 显示系统通知
	 */
	protected boolean mEnableIndicator = true;
	/**
	 * 通知icon
	 */
	@DrawableRes
	public int mDownloadIcon = android.R.drawable.stat_sys_download;
	@DrawableRes
	public int mDownloadDoneIcon = android.R.drawable.stat_sys_download_done;
	/**
	 * 并行下载
	 */
	public boolean mIsParallelDownload = true;
	/**
	 * 断点续传，分块传输该字段无效
	 */
	public boolean mIsBreakPointDownload = true;
	/**
	 * 当前下载链接
	 */
	protected String mUrl;
	/**
	 * ContentDisposition ，提取文件名 ，如果ContentDisposition不指定文件名，则从url中提取文件名
	 */
	public String mContentDisposition;
	/**
	 * 文件大小
	 */
	public long mContentLength;
	/**
	 * 文件类型
	 */
	public String mMimetype;
	/**
	 * UA
	 */
	public String mUserAgent;
	/**
	 * Header
	 */
	protected Map<String, String> mHeaders;
	/**
	 * 下载文件完成，是否自动打开该文件
	 */
	public boolean mAutoOpen = false;
	/**
	 * 超时时长默认为两小时
	 */
	public long downloadTimeOut = Long.MAX_VALUE;
	/**
	 * 连接超时， 默认10s
	 */
	protected int connectTimeOut = 10 * 1000;
	/**
	 * 以8KB位单位，默认60s ，如果60s内无法从网络流中读满8KB数据，则抛出异常 。
	 */
	public int blockMaxTime = 10 * 60 * 1000;

	public Map<String, String> getHeaders() {
		return mHeaders;
	}

	protected Extra() {}

	public int getBlockMaxTime() {
		return blockMaxTime;
	}

	public String getUrl() {
		return mUrl;
	}

	public String getUserAgent() {
		return mUserAgent;
	}

	public String getContentDisposition() {
		return mContentDisposition;
	}

	public String getMimetype() {
		return mMimetype;
	}

	public long getContentLength() {
		return mContentLength;
	}

	public boolean isForceDownload() {
		return mIsForceDownload;
	}

	public boolean isEnableIndicator() {
		return mEnableIndicator;
	}

	public long getDownloadTimeOut() {
		return downloadTimeOut;
	}

	public int getConnectTimeOut() {
		return connectTimeOut;
	}

	public int getDownloadIcon() {
		return mDownloadIcon;
	}

	public boolean isParallelDownload() {
		return mIsParallelDownload;
	}

	public boolean isBreakPointDownload() {
		return mIsBreakPointDownload;
	}

	public boolean isAutoOpen() {
		return mAutoOpen;
	}

	public int getDownloadDoneIcon() {
		return mDownloadDoneIcon;
	}

	@Override
	protected Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return new Extra();
	}
}
