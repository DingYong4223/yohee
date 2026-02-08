package com.fula.yohee.view

import android.annotation.TargetApi
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.os.Message
import android.view.LayoutInflater
import android.webkit.*
import android.webkit.WebView
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.fula.CLog
import com.fula.fano.YanoGenerator
import com.fula.yohee.Config
import com.fula.yohee.YoheeApp
import com.fula.yohee.adblock.AdBlockListener
import com.fula.yohee.adblock.NoneAdBlocker
import com.fula.yohee.constant.Setting
import com.fula.yohee.network.SSLState
import com.fula.yohee.preference.UserPreferences
import com.fula.yohee.ui.WebViewController
import com.fula.yohee.ui.activity.WebActivity
import com.fula.yohee.utils.IntentUtils
import com.fula.yohee.utils.UrlUtils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

open class YoheeWebClient(private val activity: WebActivity, private val wController: WebViewController) : WebViewClient() {

    private val intentUtils = IntentUtils(activity)
    private var adAssetBlocker: AdBlockListener = NoneAdBlocker
    private var adSessionBlocker: AdBlockListener = NoneAdBlocker
    //    @Inject
//    internal lateinit var proxyUtils: ProxyUtils
    @Inject
    internal lateinit var userPreferences: UserPreferences
    @Volatile
    private var isRunning = false
    private var zoomScale = 0.0f
    private val textReflowJs = YanoGenerator.TextReflow()
    private var currentUrl: String = ""
    private var adBlockList: MutableList<String> = mutableListOf()

    init {
        YoheeApp.injector.inject(this)
    }

    var sslState: SSLState = SSLState(SSLState.STATE_NONE)
        private set(value) {
            sslStateSubject.onNext(value)
            field = value
        }

    private val sslStateSubject: PublishSubject<SSLState> = PublishSubject.create()

    fun syncPreferSettings(userPrefer: UserPreferences) {
        if (userPrefer.blockAd) {
            adAssetBlocker = YoheeApp.injector.provideAssetsAdBlocker()
            adSessionBlocker = YoheeApp.injector.provideSessionAdBlocker()
        } else {
            adAssetBlocker = NoneAdBlocker
            adSessionBlocker = NoneAdBlocker
        }
    }

    fun sslStateObservable(): Observable<SSLState> = sslStateSubject.hide()
    private fun adResponse(pageUrl: String, requestUrl: String): WebResourceResponse? {
        if (UrlUtils.isGenUrl(pageUrl)) return null

        var host: String?
        if (adAssetBlocker.isAd(requestUrl).apply { host = this } != null) {
            adBlockList.add(host!!)
            return WebResourceResponse(null, null, null)
        }
        return null
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? = interceptRequest(view, "${request.url}")

    @Suppress("OverridingDeprecatedMember")
    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? = interceptRequest(view, url)

    private fun interceptRequest(view: WebView, url: String): WebResourceResponse? {
        CLog.i("should intercept: $url")
        if (URLUtil.isHttpUrl(url) || URLUtil.isHttpsUrl(url)) {
            if (activity.userPrefer.autoMediaDetect) {
                activity.runOnUiThread {
                    activity.detectMediaUrl(url, view.title ?: "untitled")
                }
            }
        }
        val response = adResponse(currentUrl, url)
        if (null != response) return response
        return super.shouldInterceptRequest(view, url)
    }

    override fun onPageFinished(view: WebView, url: String) {
        CLog.i("page finished...")
        wController.onPageFinished(view, url)
        wController.updateWebInfo(url, view.title ?: url)

        if (activity.isTabFront(wController) && wController.showingWebView == view) {
            activity.onPageFinished(view, url, adBlockList)
            activity.updateUrl(url)
        }
    }

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        CLog.i("page started...")
        adBlockList.clear()
        currentUrl = url
        sslState = if (URLUtil.isHttpsUrl(url)) {
            if (activity.invalideSslHost.contains(UrlUtils.getHost(url))) {
                SSLState(SSLState.STATE_INVALIDE)
            } else {
                SSLState(SSLState.STATE_VALIDE)
            }
        } else {
            SSLState(SSLState.STATE_NONE)
        }
        wController.onPageStarted(view, url, favicon)
        if (activity.isTabFront(wController)) {
            activity.onPageStarted(view, url)
            activity.updateUrl(url)
        }
    }

    override fun onReceivedHttpAuthRequest(view: WebView, handler: HttpAuthHandler,
                                           host: String, realm: String) =
            AlertDialog.Builder(activity).apply {
                val dialogView = LayoutInflater.from(activity).inflate(com.fula.yohee.R.layout.dialog_auth_request, null)

                val realmLabel = dialogView.findViewById<TextView>(com.fula.yohee.R.id.auth_request_realm_textview)
                val name = dialogView.findViewById<EditText>(com.fula.yohee.R.id.auth_request_username_edittext)
                val password = dialogView.findViewById<EditText>(com.fula.yohee.R.id.auth_request_password_edittext)
                realmLabel.text = activity.getString(com.fula.yohee.R.string.label_realm, realm)
                setView(dialogView)
                setTitle(com.fula.yohee.R.string.title_sign_in)
                setCancelable(true)
                setPositiveButton(com.fula.yohee.R.string.title_sign_in) { _, _ ->
                    val user = name.text.toString()
                    val pass = password.text.toString()
                    handler.proceed(user.trim(), pass.trim())
                    CLog.i("Attempting HTTP Authentication")
                }
                setNegativeButton(com.fula.yohee.R.string.action_cancel) { _, _ ->
                    handler.cancel()
                }
            }.create().apply {
                Setting.applyModeToWindow(activity, window!!)
                Setting.setDialogSize(activity, window)
            }.show()

    override fun onScaleChanged(view: WebView, oldScale: Float, newScale: Float) {
        CLog.i("scale changed...")
        if (view.isShown && wController.userPreferences.textReflowEnabled) {
            if (isRunning)
                return
            val changeInPercent = Math.abs(100 - 100 / zoomScale * newScale)
            if (changeInPercent > 2.5f && !isRunning) {
                isRunning = view.postDelayed({
                    zoomScale = newScale
                    view.evaluateJavascript(textReflowJs.provideJs()) { isRunning = false }
                }, 100)
            }
        }
    }

    override fun onReceivedError(view: WebView, errorCode: Int,
                                 description: String, failingUrl: String) {
        super.onReceivedError(view, errorCode, description, failingUrl)
        CLog.i("url = ${view.url}" + "failingUrl = $failingUrl, description = $description")
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
        super.onReceivedError(view, request, error)
        CLog.i("${error.errorCode} + ${error.description}, url = ${view.url}" + "request = ${request.url} ")
    }


    override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse) {
        super.onReceivedHttpError(view, request, errorResponse)
        CLog.i("url = ${view.url}" + "request = ${request.url} " + errorResponse.statusCode + errorResponse.reasonPhrase)
    }

    override fun onReceivedSslError(webView: WebView, handler: SslErrorHandler, error: SslError) {
        CLog.i("receive ssl error: ${webView.url}")
        webView.url?.let { activity.invalideSslHost.add(UrlUtils.getHost(it)) }
        sslState = SSLState(SSLState.STATE_INVALIDE, webView.url, error)
        handler.proceed()
        if (activity.isTabFront(wController)) {
            activity.onReceivedSslError()
        }
    }

    override fun onFormResubmission(view: WebView, dontResend: Message, resend: Message) = resend.sendToTarget()
    @RequiresApi(Build.VERSION_CODES.N)
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        CLog.i("request redirect: ${request.isRedirect}")
        return shouldOverrideLoading(view, request.url.toString(), request.isRedirect) || super.shouldOverrideUrlLoading(view, request)
    }

    @Suppress("OverridingDeprecatedMember", "DEPRECATION")
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        CLog.i("test redirect hitTestResult: ${view.hitTestResult?.type}")
        var redirect = view.hitTestResult?.type == WebView.HitTestResult.UNKNOWN_TYPE
        return shouldOverrideLoading(view, url, redirect) || super.shouldOverrideUrlLoading(view, url)
    }

    private fun shouldOverrideLoading(view: WebView, url: String, redirect: Boolean): Boolean {
        CLog.i("should override, redirect: $redirect, url: $url")
        //if (!proxyUtils.isProxyReady(activity)) return true
        //val headers = wController.requestHeaders
        if (URLUtil.isValidUrl(url)) {
            if (redirect) {
                return false
            }
            if (URLUtil.isHttpUrl(url) || URLUtil.isHttpsUrl(url)) {
                return wController.newWebLoading(url)
            }
//            Config.INVALIDE_SCHEMA.forEach {
//                if (url.startsWith(it)) {
//                    return true
//                }
//            }
        }
        //无论schema启动是否成功，都拦截
        intentUtils.startActivityForUrl(view, url)
        return true
    }

}
