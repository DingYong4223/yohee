package com.fula;

import android.util.Log;
/**
 * 请勿将此文件转换成kotlin，否则在混淆时不能移除调用.
 * */
public class CLog {

    private static final String format = "(%s:%s)";
    private static final String CFT_TAG = "CFT-sdk";

    public static void i(String msg) {
        try {
            String logmsg = getFinalLog(msg);
            Log.i(CFT_TAG, logmsg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void track(String msg) {
        new Exception(msg).printStackTrace();
    }

    private static String getFinalLog(String log) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement stacks = stackTrace[4];
        if (null == stacks) return log;
        String elFormat = String.format(format, stacks.getFileName(), stacks.getLineNumber());
        return String.format("{%d}%s[%s]%s", Thread.currentThread().getId(), elFormat, stacks.getMethodName(), log);
    }

}