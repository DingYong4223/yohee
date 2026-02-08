package com.fula.yohee.js

import android.app.Activity
import android.webkit.WebView
import com.fula.yohee.jsapi.JsEventBase
import com.fula.CLog

/**
 * Create by delan
 */
class Test(context: Activity, webView: WebView) : JsEventBase(context, webView) {

    override fun onJsEvent(data: String) {
        CLog.i("data: $data")
    }

}
