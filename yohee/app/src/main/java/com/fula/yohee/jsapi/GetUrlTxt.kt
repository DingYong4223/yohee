package com.fula.yohee.jsapi

import android.app.Activity
import android.webkit.WebView
import com.fula.yohee.eventbus.SEvent
import com.fula.CLog
import org.greenrobot.eventbus.EventBus

/**
 * Create by delan
 */
class GetUrlTxt(context: Activity, webView: WebView): JsEventBase(context, webView) {
    override fun onJsEvent(data: String) {
        CLog.i("data = $data")
        EventBus.getDefault().post(SEvent(SEvent.TYPE_BACKOPEN_GETURL_TXT).apply {
            stringArg = data
        })
    }
}