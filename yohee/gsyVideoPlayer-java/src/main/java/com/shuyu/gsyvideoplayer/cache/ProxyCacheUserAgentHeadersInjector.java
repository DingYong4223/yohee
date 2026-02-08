package com.shuyu.gsyvideoplayer.cache;

import com.danikula.videocache.headers.HeaderInjector;
import com.fula.CLog;

import java.util.HashMap;
import java.util.Map;

/**
 for android video cache header
 */
public class ProxyCacheUserAgentHeadersInjector implements HeaderInjector {

    public final static Map<String, String> mMapHeadData = new HashMap<>();

    @Override
    public Map<String, String> addHeaders(String url) {
        CLog.i("****** proxy addHeaders ****** " + mMapHeadData.size());
        return mMapHeadData;
    }
}