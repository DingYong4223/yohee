package com.fula.downloader.file;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.fula.downloader.DownRuntime;

/**
 * @author cenxiaozhong
 * @date 2018/2/12
 */
public class NotificationCancelReceiver extends BroadcastReceiver {

    public static final String ACTION = "com.download.cancelled";

    public NotificationCancelReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(ACTION)) {
            try {
                String url = intent.getStringExtra("TAG");
                ExecuteTasksMap.getInstance().cancelTask(url);
            } catch (Throwable ignore) {
                if (DownRuntime.getInstance().isDebug()) {
                    ignore.printStackTrace();
                }
            }

        }
    }
}