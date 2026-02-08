package com.fula.yohee.view

import android.app.Activity
import android.os.Bundle
import android.os.Message
import android.webkit.WebView
import com.fula.CLog
import com.fula.yohee.R
import com.fula.yohee.di.DiskScheduler
import com.fula.yohee.dialog.DialogHelper
import com.fula.yohee.dialog.DialogItem
import com.fula.yohee.eventbus.SEvent
import com.fula.yohee.html.HtmlFactory
import com.fula.yohee.html.history.HistoryFactory
import com.fula.yohee.html.homepage.HomeFactory
import com.fula.yohee.preference.UserPreferences
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

/**
 * An initializer that is run on a [SWebView] after it is created.
 */
interface TabInitializer {
    /**
     * Initialize the [WebView] INSTANCE held by the [SWebView]. If a url is loaded, the
     * provided [headers] should be used to load the url.
     */
    fun initialize(webView: WebView, headers: Map<String, String>)

    fun backInitialize() = Unit
}

/**
 * An initializer that loads a [url].
 */
class UrlInitializer(private val url: String) : TabInitializer {
    override fun initialize(webView: WebView, headers: Map<String, String>) {
        webView.loadUrl(url, headers)
    }
}

class HistoryInitializer @Inject constructor(factory: HistoryFactory, @DiskScheduler diskScheduler: Scheduler) : HtmlInitializer(factory, diskScheduler)

/**
 * An initializer that loads the url built by the [HtmlFactory].
 */
abstract class HtmlInitializer(
        protected val factory: HtmlFactory,
        @DiskScheduler protected val diskScheduler: Scheduler) : TabInitializer {

    protected var dis: Disposable? = null

    override fun initialize(webView: WebView, headers: Map<String, String>) = pageInitialize(webView, headers)
    override fun backInitialize() = Unit


    private fun pageInitialize(webView: WebView?, headers: Map<String, String>?) {
        dis?.dispose()
        dis = factory
                .buildPage()
                .subscribeOn(diskScheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    CLog.i("onSuccess webview = $webView, url = $it")
                    webView?.loadUrl(it, headers ?: emptyMap())
                }) {}
    }
}

/**
 * An initializer that displays the start page.
 */
class HomeInitializer @Inject constructor(
        homePageFactory: HomeFactory,
        private val userPrefer: UserPreferences,
        @DiskScheduler diskScheduler: Scheduler)
    : HtmlInitializer(homePageFactory, diskScheduler) {

    override fun backInitialize() {
        CLog.i("build and load home page...")
        dis = factory
                .buildPage()
                .subscribeOn(diskScheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy {
                    CLog.i("back onSuccess = $it")
                    EventBus.getDefault().post(SEvent(SEvent.TYPE_HOMEPAGE_RELOAD))
                }
    }

    override fun initialize(webView: WebView, headers: Map<String, String>) {
        if (userPrefer.appInitialized != 0) {
            CLog.i("load home page...")
            webView.loadUrl(factory.getUrl(), headers)
        } else {
            CLog.i("build and load home page...")
            super.initialize(webView, headers)
        }
    }
}

/**
 * An initializer that sets the [WebView] as the target of the [resultMessage]. Used for
 * `target="_blank"` links.
 */
class ResultMessageInitializer(private val resultMessage: Message) : TabInitializer {

    override fun initialize(webView: WebView, headers: Map<String, String>) {
        resultMessage.apply {
            (obj as WebView.WebViewTransport).webView = webView
        }.sendToTarget()
    }

}

/**
 * An initializer that restores the [WebView] state using the [bundle].
 */
class BundleInitializer(private val bundle: Bundle) : TabInitializer {

    override fun initialize(webView: WebView, headers: Map<String, String>) {
        webView.restoreState(bundle)
    }

}

/**
 * An initializer that does not load anything into the [WebView].
 */
class NoOpInitializer : TabInitializer {
    override fun initialize(webView: WebView, headers: Map<String, String>) = Unit
}

/**
 * Ask the user's permission before loading the [url] and load the homepage instead if they deny
 * permission. Useful for scenarios where another app may attempt to open a malicious URL in the
 * browser via an intent.
 */
class PermissionInitializer(
        private val url: String,
        private val activity: Activity,
        private val homeInitializer: HomeInitializer) : TabInitializer {

    override fun initialize(webView: WebView, headers: Map<String, String>) {
        DialogHelper.showOkCancelDialog(activity, R.string.alert_warm, R.string.message_blocked_local,
                positiveButton = DialogItem(title = R.string.action_open) {
                    UrlInitializer(url).initialize(webView, headers)
                },
                negativeButton = DialogItem(title = R.string.action_cancel) {}
        ).setOnDismissListener {
            homeInitializer.initialize(webView, headers)
        }
    }

}
