package com.fula.downloader.file;

import android.os.AsyncTask;

import java.util.ArrayDeque;
import java.util.concurrent.Executor;

/**
 * @author cenxiaozhong
 * @date 2019/2/8
 * @since 1.0.0
 */
public class SerialExecutor implements Executor {
	static final Executor THREAD_POOL_EXECUTOR = AsyncTask.THREAD_POOL_EXECUTOR;
	final ArrayDeque<Runnable> mTasks = new ArrayDeque<Runnable>();
	Runnable mActive;

	@Override
	public synchronized void execute(final Runnable r) {
		mTasks.offer(new Runnable() {
			@Override
			public void run() {
				try {
					r.run();
				} finally {
					scheduleNext();
				}
			}
		});
		if (mActive == null) {
			scheduleNext();
		}
	}

	protected synchronized void scheduleNext() {
		if ((mActive = mTasks.poll()) != null) {
			THREAD_POOL_EXECUTOR.execute(mActive);
		}
	}
}