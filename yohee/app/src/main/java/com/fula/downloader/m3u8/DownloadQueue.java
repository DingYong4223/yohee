package com.fula.downloader.m3u8;

import com.fula.downloader.DownloadTask;

import java.util.ArrayList;
import java.util.List;

class DownloadQueue {

    private List<DownloadTask> queue;

    public DownloadQueue() {
        queue = new ArrayList<>();
    }

    /**
     * 入队
     *
     * @param task
     */
    public void offer(DownloadTask task) {
        queue.add(task);
    }

    /**
     * 队头元素出队，并返回队头元素
     *
     * @return
     */
    public DownloadTask poll() {
        try {
            if (queue.size() >= 2) {
                queue.remove(0);
                return queue.get(0);
            } else if (queue.size() == 1) {
                queue.remove(0);
            }
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 返回队头元素
     *
     * @return
     */
    public DownloadTask peek() {
        try {
            if (queue.size() >= 1) {
                return queue.get(0);
            }
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 移除元素
     *
     * @param task
     * @return 是否成功移除
     */
    public boolean remove(DownloadTask task) {
        if (contains(task)) {
            return queue.remove(task);
        }
        return false;
    }

    /**
     * 判断队列中是否含有此元素
     *
     * @param task
     * @return
     */
    public boolean contains(DownloadTask task) {
        return queue.contains(task);
    }

    /**
     * 通过url 返回队列中任务元素
     *
     * @param url
     * @return
     */
    public DownloadTask getTask(String url) {
        try {
            for (int i = 0; i < queue.size(); i++) {
                if (queue.get(i).getUrl().equals(url)) {
                    return queue.get(i);
                }
            }
        } catch (Exception e) {
        }

        return null;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int size() {
        return queue.size();
    }

    public boolean isHead(String url) {
        return isHead(new DownloadTask(url));
    }

    public boolean isHead(DownloadTask task) {
        return task.equals(peek());
    }
}
