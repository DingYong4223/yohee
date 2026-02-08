package com.fula.downloader.m3u8;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.fula.downloader.DownStatus;
import com.fula.downloader.DownloadListener;
import com.fula.downloader.DownloadTask;
import com.fula.downloader.m3u8.bean.M3U8;
import com.fula.downloader.m3u8.utils.MUtils;
import com.fula.CLog;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.Nullable;

public class M3U8Downloader {

    private long currentTime;
    private DownloadTask currentM3U8Task;
    private DownloadQueue downLoadQueue;
    private M3U8DownloadTask downTask;
    private List<DownloadListener> ls = new LinkedList<>();
    private M3u8Info mM3u8Info = new M3u8Info();
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private M3U8Downloader() {
        downLoadQueue = new DownloadQueue();
        downTask = new M3U8DownloadTask(mM3u8Info);
    }

    private static class SingletonHolder {
        static M3U8Downloader instance = new M3U8Downloader();
    }

    public static M3U8Downloader Builder() {
        return SingletonHolder.instance;
    }

    public M3U8Downloader fileNameGenerator(NameGenerator generator) {
        mM3u8Info.setNameGenerator(generator);
        return this;
    }

    public M3U8Downloader cacheRoot(String cacheRoot) {
        mM3u8Info.setCacheRoot(cacheRoot);
        return this;
    }

    public static M3U8Downloader getInstance() {
        return SingletonHolder.instance;
    }


    /**
     * 防止快速点击引起ThreadPoolExecutor频繁创建销毁引起crash
     *
     * @return
     */
    private boolean isQuicklyClick() {
        boolean result = false;
        if (System.currentTimeMillis() - currentTime <= 100) {
            result = true;
            CLog.i("is too quickly click!");
        }
        currentTime = System.currentTimeMillis();
        return result;
    }


    /**
     * 下载下一个任务，直到任务全部完成
     */
    private void downloadNextTask() {
        startDownloadTask(downLoadQueue.poll());
    }

    private void pendingTask(DownloadTask task) {
        task.setStatus(DownStatus.STATUS_PENDDING);
        for (DownloadListener item : ls) {
            item.onResult(task.getStatus(), task.getUrl(), task);
        }
    }

    public void download(String url) {
        if (TextUtils.isEmpty(url) || isQuicklyClick()) return;
        DownloadTask task = new DownloadTask(url);
        if (downLoadQueue.contains(task)) {
            task = downLoadQueue.getTask(url);
            if (task.getStatus() != DownStatus.STATUS_DOWNLOADING) {
                startDownloadTask(task);
            } else {
                pause(url);
            }
        } else {
            downLoadQueue.offer(task);
            startDownloadTask(task);
        }
    }

    /**
     * 暂停，如果此任务正在下载则暂停，否则无反应
     * 只支持单一任务暂停，多任务暂停请使用{@link #pause(List)}
     */
    public void pause(String url) {
        if (TextUtils.isEmpty(url)) return;
        DownloadTask task = downLoadQueue.getTask(url);
        if (task != null) {
            task.setStatus(DownStatus.STATUS_PAUSED);
            for (DownloadListener item : ls) {
                item.onResult(DownStatus.STATUS_PAUSED, url, task);
            }
            if (url.equals(currentM3U8Task.getUrl())) {
                downTask.stop();
                downloadNextTask();
            } else {
                downLoadQueue.remove(task);
            }
        }
    }

    /**
     * 批量暂停
     */
    public void pause(List<String> urls) {
        if (urls == null || urls.size() == 0) return;
        boolean isCurrentTaskPause = false;
        for (String url : urls) {
            if (downLoadQueue.contains(new DownloadTask(url))) {
                DownloadTask task = downLoadQueue.getTask(url);
                if (task != null) {
                    task.setStatus(DownStatus.STATUS_PAUSED);
                    for (DownloadListener item : ls) {
                        item.onResult(DownStatus.STATUS_PAUSED, url, task);
                    }
                    if (task.equals(currentM3U8Task)) {
                        downTask.stop();
                        isCurrentTaskPause = true;
                    }
                    downLoadQueue.remove(task);
                }
            }
        }
        if (isCurrentTaskPause) startDownloadTask(downLoadQueue.peek());
    }

    /**
     * 检查m3u8文件是否存在
     *
     * @param url
     * @return
     */
    public boolean checkExist(String url) {
        try {
            return downTask.getM3u8File(url).exists();
        } catch (Exception e) {
            CLog.i(e.getMessage());
        }
        return false;
    }

    /**
     * 得到m3u8文件路径
     *
     * @param url
     * @return
     */
    public String getM3U8Path(String url) {
        return downTask.getM3u8File(url).getPath();
    }

    public boolean isRunning() {
        return downTask.isRunning();
    }


    /**
     * if task is the current task , it will return true
     *
     * @param url
     * @return
     */
    public boolean isCurrentTask(String url) {
        return !TextUtils.isEmpty(url)
                && downLoadQueue.peek() != null
                && downLoadQueue.peek().getUrl().equals(url);
    }

    public void registerListener(DownloadListener listener) {
        if (ls.contains(listener)) return;
        ls.add(listener);
    }

    public boolean unregisterListener(DownloadListener listener) {
        if (ls.contains(listener)) {
            return ls.remove(listener);
        }
        return false;
    }

    public void setEncryptKey(String encryptKey) {
        downTask.setEncryptKey(encryptKey);
    }

    public String getEncryptKey() {
        return downTask.getEncryptKey();
    }

    private void startDownloadTask(DownloadTask task) {
        if (task == null) return;
        pendingTask(task);
        if (!downLoadQueue.isHead(task)) {
            CLog.i("start download task, but task is running: " + task.getUrl());
            return;
        }

        if (task.getStatus() == DownStatus.STATUS_PAUSED) {
            CLog.i("start download task, but task has pause: " + task.getUrl());
            return;
        }
        try {
            currentM3U8Task = task;
            CLog.i("====== start downloading ===== " + task.getUrl());
            downTask.download(task.getUrl(), onTaskDownloadListener);
        } catch (Exception e) {
            CLog.i("startDownloadTask Error:" + e.getMessage());
        }
    }

    /**
     * 取消任务
     *
     * @param url
     */
    public void cancel(String url) {
        pause(url);
    }

    /**
     * 批量取消任务
     *
     * @param urls
     */
    public void cancel(List<String> urls) {
        pause(urls);
    }

    /**
     * 取消任务,删除缓存
     *
     * @param url
     */
    public void cancelAndDelete(final String url, @Nullable final OnDeleteTaskListener listener) {
        pause(url);
        if (listener != null) {
            listener.onStart();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isDelete = MUtils.clearDir(new File(mM3u8Info.genFilePath(url)));
                if (listener != null) {
                    if (isDelete) {
                        listener.onSuccess();
                    } else {
                        listener.onFail();
                    }
                }
            }
        }).start();
    }

    /**
     * 批量取消任务,删除缓存
     */
    public void cancelAndDelete(final List<String> urls, @Nullable final OnDeleteTaskListener listener) {
        pause(urls);
        if (listener != null) {
            listener.onStart();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isDelete = true;
                for (String url : urls) {
                    isDelete = isDelete && MUtils.clearDir(new File(mM3u8Info.genFilePath(url)));
                }
                if (listener != null) {
                    if (isDelete) {
                        listener.onSuccess();
                    } else {
                        listener.onFail();
                    }
                }
            }
        }).start();
    }

    private OnTaskDownloadListener onTaskDownloadListener = new OnTaskDownloadListener() {
        private long fileLength;
        private long lastLength;

        @Override
        public void onStartDownload(int totalTs, int curTs) {
            CLog.i("onStartDownload: " + totalTs + "|" + curTs);
            currentM3U8Task.setStatus(DownStatus.STATUS_DOWNLOADING);
        }

        @Override
        public void onDownloading(long totalFileSize, final long itemFileSize, final int totalTs, final int curTs) {
            if (!downTask.isRunning()) return;
            CLog.i("onDownloading: " + totalFileSize + "|" + itemFileSize + "|" + totalTs + "|" + curTs);
            fileLength = totalFileSize;
            currentM3U8Task.mTotalsLength = totalFileSize;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (DownloadListener item : ls) {
                        item.onProgress(currentM3U8Task.getUrl(), currentM3U8Task.mDownedLength, currentM3U8Task.mTotalsLength, currentM3U8Task.speed);
                    }
                }
            });
        }

        @Override
        public void onSuccess(M3U8 m3U8) {
            downTask.stop();
            currentM3U8Task.m3U8 = m3U8;
            currentM3U8Task.setStatus(DownStatus.STATUS_COMPLETED);
            CLog.i("m3u8 Downloader onSuccess: " + m3U8);
            downloadNextTask();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (DownloadListener item : ls) {
                        item.onResult(DownStatus.STATUS_COMPLETED, currentM3U8Task.getUrl(), currentM3U8Task);
                    }
                }
            });
        }

        @Override
        public void onProgress(long curLength) {
            if (curLength - lastLength > 0) {
                currentM3U8Task.mTotalsLength = fileLength;
                currentM3U8Task.mDownedLength = curLength;
                currentM3U8Task.speed = curLength - lastLength;
                lastLength = curLength;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (DownloadListener item : ls) {
                            item.onProgress(currentM3U8Task.getUrl(), currentM3U8Task.mDownedLength, fileLength, currentM3U8Task.speed);
                        }
                    }
                });
            }
        }

        @Override
        public void onStart() {
            currentM3U8Task.setStatus(DownStatus.STATUS_PENDDING);
            CLog.i("onDownloadPrepare: " + currentM3U8Task.getUrl());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (DownloadListener item : ls) {
                        item.onStart(currentM3U8Task);
                    }
                }
            });
        }

        @Override
        public void onError(final Throwable errorMsg) {
            if (errorMsg.getMessage() != null && errorMsg.getMessage().contains("ENOSPC")) {
                currentM3U8Task.setStatus(DownStatus.ERROR_LOAD);
            } else {
                currentM3U8Task.setStatus(DownStatus.ERROR_NETWORK_CONNECTION);
            }
            CLog.i("onError: " + errorMsg.getMessage());
            downloadNextTask();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (DownloadListener item : ls) {
                        item.onResult(currentM3U8Task.getStatus(), currentM3U8Task.getUrl(), currentM3U8Task);
                    }
                }
            });
        }

    };

}
