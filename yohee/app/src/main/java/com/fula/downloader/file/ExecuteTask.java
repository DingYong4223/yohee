package com.fula.downloader.file;

import com.fula.downloader.DownloadTask;

/**
 * @author cenxiaozhong
 * @date 2018/2/12
 */
public interface ExecuteTask {
    DownloadTask cancelDownload();

    DownloadTask pauseDownload();

    DownloadTask getDownloadTask();
}
