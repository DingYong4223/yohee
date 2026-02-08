package com.fula.yohee.jsapi

import android.app.Activity
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.fula.CLog

interface JsEventInterface {
    fun onJsEvent(data: String)
}

//abstract class JsEventBase(protected var context: Activity, protected var webView: WebView) : JsEventInterface

class JSCore(private val webView: WebView, private val mContext: Activity) {

    private val registerMap = HashMap<String, Class<out JsEventBase>>().apply {
        this["AdUIClear"] = AdUIClear::class.java
        this["GetUrlTxt"] = GetUrlTxt::class.java
    }

    @JavascriptInterface
    fun log(data: String) = CLog.i(data)

    @JavascriptInterface
    fun invoke(method: String, params: String) {
        CLog.i("method = $method, param = $params")
        val jsInterface = getJsObject(mContext, webView, method)

        CLog.i("jsInterface = $jsInterface")
        jsInterface?.let {
            mContext.runOnUiThread { jsInterface.onJsEvent(params) }
        }
    }

    private fun getJsObject(context: Activity, webView: WebView, method: String): JsEventInterface? = try {
        val jsClazz = registerMap[method]
        CLog.i("jsClazz = $jsClazz")
        jsClazz?.let {
            val consClazz = it.getConstructor(Activity::class.java, WebView::class.java)
            return@let consClazz.newInstance(context, webView) as JsEventInterface
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    companion object {
//        private fun getJsObject(context: Activity, webView: WebView, className: String): JsEventInterface? = try {
//            val jsEventBase = JsEventBase::class.java.name
//            val jsPath = jsEventBase.substring(0, jsEventBase.lastIndexOf("."))
//            val clazz = Class.forName(String.format("%s.%s", jsPath, className))
//            val c = clazz.getConstructor(Activity::class.java, WebView::class.java)
//            c.newInstance(context, webView) as JsEventInterface
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }

    }
}
