package com.fula.downloader.file;

import android.os.Handler;
import android.os.Looper;

import com.fula.downloader.DownloadTask;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author cenxiaozhong
 * @date 2019/2/9
 * @since 1.0.0
 */
public class SyncDownloader extends Downloader implements Callable<File> {

	private static final Handler HANDLER = new Handler(Looper.getMainLooper());
	private volatile boolean mEnqueue;
	private ReentrantLock mLock = new ReentrantLock();
	private Condition mCondition = mLock.newCondition();

	SyncDownloader(DownloadTask downloadTask) {
		super();
		mDownloadTask = downloadTask;
	}

	@Override
	protected void onPreExecute() {
		try {
			super.onPreExecute();
		} catch (Throwable throwable) {
			this.mThrowable = throwable;
			throw throwable;
		}
	}

	@Override
	protected void onPostExecute(Integer integer) {
		try {
			super.onPostExecute(integer);
		} finally {
			mLock.lock();
			try {
				mCondition.signal();
			} finally {
				mLock.unlock();
			}
		}
	}

	@Override
	protected void destroyTask() {
	}

	@Override
	public DownloadTask cancelDownload() {
		super.cancelDownload();
		return null;
	}

	@Override
	public File call() throws Exception {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			throw new UnsupportedOperationException("Sync download must call it in the non main-Thread  ");
		}
		mLock.lock();
		try {
			final CountDownLatch syncLatch = new CountDownLatch(1);
			HANDLER.post(new Runnable() {
				@Override
				public void run() {
					mEnqueue = download(mDownloadTask);
					syncLatch.countDown();
				}
			});
			syncLatch.await();
			if (!mEnqueue) {
				throw new RuntimeException("download task already exist!");
			}
			mCondition.await();
		} finally {
			mLock.unlock();
		}
		if (null != mThrowable) {
			throw (RuntimeException) mThrowable;
		}
		return mDownloadTask.mFile;
	}
}
