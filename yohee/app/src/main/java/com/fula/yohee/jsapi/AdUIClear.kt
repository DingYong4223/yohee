package com.fula.yohee.jsapi

import android.app.Activity
import android.webkit.WebView
import com.fula.yohee.eventbus.SEvent
import org.greenrobot.eventbus.EventBus

/**
 * Create by delan
 */
class AdUIClear(context: Activity, webView: WebView): JsEventBase(context, webView) {
    override fun onJsEvent(data: String) = EventBus.getDefault().post(SEvent(SEvent.TYPE_RECAPTRUE_TOOLBAR_COLOR))
}