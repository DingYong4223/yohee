package com.fula.yohee.jsapi

import android.app.Activity
import android.webkit.WebView

abstract class JsEventBase(protected var context: Activity, protected var webView: WebView) : JsEventInterface