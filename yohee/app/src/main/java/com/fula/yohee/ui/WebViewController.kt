package com.fula.yohee.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.webkit.WebView
import androidx.collection.ArrayMap
import com.fula.CLog
import com.fula.base.ViewHelper
import com.fula.yohee.MenuPop
import com.fula.yohee.YoheeApp
import com.fula.yohee.constant.DESKTOP_USER_AGENT
import com.fula.yohee.constant.Setting
import com.fula.yohee.di.DatabaseScheduler
import com.fula.yohee.dialog.SDialogBuilder
import com.fula.yohee.download.SDownloadListener
import com.fula.yohee.eventbus.SEvent
import com.fula.yohee.extensions.loadJavascript
import com.fula.yohee.extensions.removeFromParent
import com.fula.yohee.js.JSAppendMarks
import com.fula.yohee.js.JSOpenSug
import com.fula.yohee.jsapi.JSCore
import com.fula.yohee.network.SSLState
import com.fula.yohee.preference.UserPreferences
import com.fula.yohee.ui.activity.WebActivity
import com.fula.yohee.utils.UrlUtils
import com.fula.yohee.view.*
import com.fula.yohee.view.NSWebView.Companion.LONG_PRESS_EVENT
import com.fula.yohee.view.find.FindResults
import io.reactivex.Observable
import io.reactivex.Scheduler
import org.greenrobot.eventbus.EventBus
import java.lang.ref.WeakReference
import java.util.Collections.singletonList
import javax.inject.Inject

class WebViewController(private val activity: WebActivity, tabInitializer: TabInitializer) {

    private val webHandler: WebHandler by lazy { WebHandler(this) }
    val webList: ArrayList<NSWebView> = ArrayList()
    val webInfo: MutableMap<String, SWebViewTitle> = mutableMapOf()
    var showingWebView: NSWebView
    var captureImage: Bitmap? = null
    var captureScroll: Bitmap? = null
    private val requestHeaders = ArrayMap<String, String>()
    private val maxFling: Float
    @Inject
    internal lateinit var userPreferences: UserPreferences
    @Inject
    internal lateinit var dialogBuilder: SDialogBuilder
    @Inject
    internal lateinit var menuPop: MenuPop
    @Inject
    @field:DatabaseScheduler
    internal lateinit var databaseScheduler: Scheduler
    private val longPressSquare = ViewConfiguration.getTouchSlop() * ViewConfiguration.getTouchSlop()
    val mWebClient: YoheeWebClient
    private val mYoheeChrome: YoheeChromeClient
    val progress: Int
        get() = showingWebView.progress
    private val userAgent: String
        get() = showingWebView.settings?.userAgentString ?: ""
    val favicon: Bitmap?
        get() = webInfo[url]?.getFavicon()
    val title: String
        get() = webInfo[url]?.title ?: ""
    val url: String
        get() = showingWebView.url ?: ""

    init {
        YoheeApp.injector.inject(this)
        maxFling = ViewConfiguration.get(activity).scaledMaximumFlingVelocity.toFloat()
        mWebClient = YoheeWebClient(activity, this)
        mYoheeChrome = YoheeChromeClient(activity, this@WebViewController)
        webList.add(genWebView().apply { showingWebView = this })
        tabInitializer.initialize(showingWebView, requestHeaders as Map<String, String>)
    }

    private fun genWebView() = NSWebView(activity).apply {
        disListener = ::toolbarTouchConsumeAction
        actionSrolly = ::actionSrolly
        id = View.generateViewId()
        isFocusableInTouchMode = true
        isFocusable = true
        isDrawingCacheEnabled = false
        setWillNotCacheDrawing(true)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            isAnimationCacheEnabled = false
            isAlwaysDrawnWithCacheEnabled = false
        }
        isScrollbarFadingEnabled = true
        isSaveEnabled = true
        setNetworkAvailable(true)
        webChromeClient = mYoheeChrome
        webViewClient = mWebClient
        setDownloadListener(SDownloadListener(activity))
        Setting.initWebviewSettings(activity, settings)
        //setLayerType(View.LAYER_TYPE_HARDWARE, paint)
        addJavascriptInterface(JSCore(this, activity), "yohee")
        Setting.syncPreferSettings(singletonList(this), mWebClient, userPreferences)
    }

    fun updateWebInfo(url: String, t: String) {
        var info = webInfo[url]
        if (info == null) {
            showingWebView.url?.let {
                webInfo[it] = SWebViewTitle(activity).apply {
                    info = this
                }
            }
        }
        info?.title = t
    }

    fun updateWebInfo(url: String, icon: Bitmap) {
        var info = webInfo[url]
        if (info == null) {
            webInfo[url] = SWebViewTitle(activity).apply {
                info = this
            }
        }
        info!!.setFavicon(icon)
    }

    fun newWebLoading(url: String): Boolean {
        CLog.i("new loading: $url")
        if (webList.size < MAX_IDENTITY_PAGE) {
            CLog.i("new view page...")
            var showIndex = webList.indexOf(showingWebView)
            if (showIndex >= 0) {
                for (i in webList.size - 1 downTo showIndex + 1) {
                    CLog.i("remove: $i, showIndex: $showIndex, size: ${webList.size}")
                    destroyWebView(webList.removeAt(i))
                }
            }
            webList.add(genWebView().apply {
                showingWebView = this
            })
            showingWebView.loadUrl(url, requestHeaders)
            activity.tabWebChangeListener(showingWebView)
            return true
        }
        return false
    }

    private var downX: Float = 0f
    private var downY: Float = 0f
    private var lastActionY: Float = 0f
    private var ylocked = false
    private fun toolbarTouchConsumeAction(view: View, event: MotionEvent?, scroll: Boolean): Float {
        if (!view.hasFocus()) view.requestFocus()
        var webOffY = 0f
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                ylocked = false
                lastActionY = event.y
                downX = event.x
                downY = event.y
                webHandler.apply {
                    removeMessages(LONG_PRESS_EVENT)
                    sendEmptyMessageAtTime(LONG_PRESS_EVENT,
                            event.downTime + ViewConfiguration.getLongPressTimeout())
                }
                activity.webViewTouchDown()
            }
            MotionEvent.ACTION_MOVE -> {
                if (!UrlUtils.isGenUrl(url) && scroll) {
                    val cx = if (downX - event.x >= 0) 1 else -1
                    if (view.canScrollHorizontally(cx)
                            || (event.y - lastActionY < 0 && !view.canScrollVertically(1))) {
                        CLog.i("scoll cut...")
                        ylocked = true
                    }
                    if (!ylocked) {
                        webOffY = activity.webViewTouchMove(event.y - lastActionY)
                    }
                }

                val deltaX = (event.x - downX).toInt()
                val deltaY = (event.y - downY).toInt()
                val distance = deltaX * deltaX + deltaY * deltaY
                if (distance > longPressSquare || webOffY != 0f) {
                    webHandler.removeMessages(LONG_PRESS_EVENT)
                }
                lastActionY = event.y - webOffY
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                webHandler.removeMessages(LONG_PRESS_EVENT)
                if (!UrlUtils.isGenUrl(url)) {
                    captureScoroll(view)
                    activity.webViewTouchUp()
                }
            }
        }
        CLog.i("consumed = $webOffY")
        return webOffY
    }

    fun sslState(): SSLState = mWebClient.sslState

    fun sslStateObservable(): Observable<SSLState> = mWebClient.sslStateObservable()

    fun reinitialize(initializer: TabInitializer) {
        webList.remove(showingWebView.apply {
            clearHistory()
            initializer.initialize(this, requestHeaders)
        })
        webList.apply {
            forEach {
                webInfo.remove(it.url)
                destroyWebView(it)
            }
            clear()
            add(showingWebView)
        }
        //activity.tabWebChangeListener(showingWebView)
    }

    private fun captureAlbum(view: WebView) {
        ViewHelper.captrueWebView(view, captureImage)?.let {
            //captureImage?.recycle()
            captureImage = it
            EventBus.getDefault().post(SEvent(SEvent.TYPE_NAVI_AND_ALBUM_CHANGE).apply {
                obj = this@WebViewController
            })
        }
    }

    private fun captureScoroll(view: View) {
        ViewHelper.captrueScroll(view, captureScroll)?.let {
            captureScroll = it
        }
    }

    fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        CLog.i("page start, capture...")
    }

    fun onPageFinished(view: WebView, url: String) {
        CLog.i("page finished, capture...")
        actionSrolly(view.scrollY, 300L)
        when (UrlUtils.getType(url)) {
            UrlUtils.URL_TYPE_HOME -> {
                activity.getMarkAndLoadJs {
                    view.loadJavascript(JSAppendMarks(it).getJs())
                }
                view.loadJavascript(JSOpenSug(url).getJs())
            }
            UrlUtils.URL_TYPE_WEB -> {
            }
        }
    }

    private fun actionSrolly(scrollY: Int, delayCapture: Long) {
        if (scrollY <= 1) {
            CLog.i("action scroll: $scrollY")
            webHandler.postDelayed({
                captureAlbum(showingWebView)
                webHandler.postDelayed({
                    actionToolBarColor()
                }, 200)
            }, delayCapture)
        }
    }

    private fun actionToolBarColor() {
        if (activity.isTabFront(this)) {
            activity.actionToolBarColor(url)
        }
    }

    fun onProgressChanged(view: WebView, newProgress: Int) {
        if (0 == newProgress % 20 || newProgress >= 100) {
            captureAlbum(view)
        }
        if (newProgress % 20 == 0 && view.scrollY <= 80) {
            actionToolBarColor()
        }
    }

    private fun captrueThenAdaptToolbarColor() {
        captureAlbum(showingWebView)
        actionToolBarColor()
    }

    fun onReceivedTitle(view: WebView?, title: String) {
        view?.let {
            updateWebInfo(it.url, title)
        }
        EventBus.getDefault().post(SEvent(SEvent.TYPE_NAVI_AND_ALBUM_CHANGE).apply {
            obj = this@WebViewController
        })
    }

    /**
     * This method is used to toggle the user agent between desktop and the current preference of
     * the user.
     */
    fun toggleDesktopUA() {
        showingWebView.settings?.let {
            if (it.userAgentString == DESKTOP_USER_AGENT) {
                Setting.setUserAgent(activity, showingWebView, userPreferences.userAgent)
            } else {
                showingWebView.settings?.userAgentString = DESKTOP_USER_AGENT
            }
        }
    }

    /**
     * Save the state of the tab and return it as a [Bundle].
     */
    fun saveState(): Bundle = Bundle(ClassLoader.getSystemClassLoader()).also {
        showingWebView.saveState(it)
    }

    fun onPause() {
        for (v in webList) {
            v.onPause()
        }
    }

    fun onResume() {
        for (v in webList) {
            v.onResume()
        }
    }

    /**
     * Notify the WebView to stop the current load.
     */
    fun stopLoading() = showingWebView.stopLoading()

    /**
     * Pauses the JavaScript timers of the
     * WebView INSTANCE, which will trigger a
     * pause for all WebViews in the app.
     */
    fun pauseTimers() {
        for (v in webList) {
            v.pauseTimers()
        }
    }

    /**
     * Resumes the JavaScript timers of the
     * WebView INSTANCE, which will trigger a
     * resume for all WebViews in the app.
     */
    fun resumeTimers() {
        for (v in webList) {
            v.resumeTimers()
        }
    }

    /**
     * Requests focus down on the WebView INSTANCE
     * if the view does not already have focus.
     */
    fun requestFocus() {
        if (!showingWebView.hasFocus()) {
            showingWebView.requestFocus()
        }
    }

    /**
     * Sets the visibility of the WebView to either
     * View.GONE, View.VISIBLE, or View.INVISIBLE.
     * other values passed in will have no effect.
     * @param visible the visibility to set on the WebView.
     */
    fun setVisibility(visible: Int) {
        showingWebView.visibility = visible
    }

    /**
     * Tells the WebView to reload the current page.
     * If the proxy settings are not ready then the
     * this method will not have an affect as the
     * proxy must start before the load occurs.
     */
    fun reload() {
        showingWebView.reload()
    }

    /**
     * Finds all the instances of the text passed to this
     * method and highlights the instances of that text
     * in the WebView.
     * @param text the text to search for.
     */
    @SuppressLint("NewApi")
    fun find(text: String): FindResults {
        showingWebView.findAllAsync(text)

        return object : FindResults {
            override fun nextResult() {
                showingWebView.findNext(true)
            }

            override fun previousResult() {
                showingWebView.findNext(false)
            }

            override fun clearResults() {
                showingWebView.clearMatches()
            }
        }
    }

    fun onDestroy() {
        webHandler.removeCallbacksAndMessages(null)
        captureImage?.recycle()
        captureImage = null
        for (v in webList) {
            destroyWebView(v)
        }
    }

    private fun destroyWebView(v: WebView) {
        webInfo.remove(v.url)
        v.removeFromParent()
        v.stopLoading()
        v.onPause()
        v.clearHistory()
        v.removeAllViews()
        v.destroyDrawingCache()
        v.destroy()
    }

    /**
     * Tell the WebView to navigate backwards
     * in its history to the previous page.
     */
    fun goBack() {
        if (showingWebView.canGoBack()) {
            CLog.i("webview go back")
            return showingWebView.goBack()
        }
        val index = webList.indexOf(showingWebView)
        if (index > 0) {
            CLog.i("list go back")
            showingWebView = webList[index - 1]
            activity.tabWebChangeListener(showingWebView)
            captrueThenAdaptToolbarColor()
        }
    }

    fun goForward() {
        if (showingWebView.canGoForward()) {
            CLog.i("webview go forward")
            return showingWebView.goForward()
        }
        val index = webList.indexOf(showingWebView)
        if (index < webList.size - 1) {
            CLog.i("list go forward")
            showingWebView = webList[index + 1]
            activity.tabWebChangeListener(showingWebView)
            captrueThenAdaptToolbarColor()
        }
    }

    private fun longPressPage(url: String?) {
        val result = showingWebView.hitTestResult
        val finalUrl = url ?: result?.extra
        CLog.i("finalUrl = $finalUrl, type = ${result.type}")
        finalUrl?.apply {
            if (UrlUtils.isGenUrl(showingWebView.url)) {
                when {
                    UrlUtils.isHomePage(showingWebView.url) -> dialogBuilder.longPressBookmarkItem(activity, menuPop, this)
                    UrlUtils.isHistoryUrl(showingWebView.url) -> dialogBuilder.longPressHistoryUrl(activity, this)
                    //UrlUtils.isBookmarkUrl(webView.url) -> dialogBuilder.longPressBookmarkItem(activity, menuPop, this)
                    //UrlUtils.isDownloadsUrl(webView.url) -> dialogBuilder.longPressDownloadUrl(activity, this)
                }
            } else {
                menuPop.longPressOnlineUrl(activity, this, userAgent, result.type)
            }
        }
    }

    /**
     * Determines whether or not the WebView can go
     * backward or if it as the end of its history.
     * @return true if the WebView can go back, false otherwise.
     */
    fun canGoBack(): Boolean {
        val index = webList.indexOf(showingWebView)
        return showingWebView.canGoBack() || index > 0
    }

    /**
     * Determine whether or not the WebView can go
     * forward or if it is at the front of its history.
     * @return true if it can go forward, false otherwise.
     */
    fun canGoForward(): Boolean {
        val index = webList.indexOf(showingWebView)
        return showingWebView.canGoForward() || index < webList.size - 1
    }

    /**
     * Loads the URL in the WebView. If the proxy settings
     * are still initializing, then the URL will not load
     * as it is necessary to have the settings appInitialized
     * before a load occurs.
     * @param url the non-null URL to attempt to load in
     * the WebView.
     */
    fun loadUrl(url: String) {
        CLog.i("load url = $url")
        showingWebView.loadUrl(url, requestHeaders)
    }

    private inner class WebHandler(viewController: WebViewController) : Handler() {

        private val mReference: WeakReference<WebViewController> = WeakReference(viewController)
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            CLog.i("msg what = ${msg.what}")
            when (msg.what) {
                NSWebView.LONG_PRESS_EVENT -> {
                    val msg = obtainMessage(NSWebView.LONG_PRESS_ACTION)
                    msg.target = webHandler
                    showingWebView.requestFocusNodeHref(msg)
                }
                NSWebView.LONG_PRESS_ACTION -> {
                    val url = msg.data.getString("url")
                    mReference.get()?.longPressPage(url)
                }
            }
        }
    }

    companion object {
        const val MAX_IDENTITY_PAGE = 1
    }

}
