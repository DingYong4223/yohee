package com.fula.downloader.file;

import com.fula.downloader.DownloadTask;

/**
 * @author cenxiaozhong
 * @date 2019/2/8
 * @since 1.0.0
 */
public interface IDownloader<T extends DownloadTask> {
    boolean download(T t);

    T cancel();

    int status();
}
