package com.fula.yohee.ui.fragment

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.LinearLayout
import com.fula.yohee.R
import com.fula.yohee.YoheeApp
import com.fula.yohee.di.DatabaseScheduler
import com.fula.yohee.di.NetworkScheduler
import com.fula.yohee.eventbus.DrawerEvent
import com.fula.yohee.html.history.HistoryFactory
import com.fula.CLog
import com.fula.yohee.preference.UserPreferences
import com.fula.yohee.ui.WebViewController
import com.fula.yohee.ui.activity.WebActivity
import com.fula.yohee.view.HistoryInitializer
import com.fula.yohee.view.YoheeWebClient
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.history_drawer.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

class FragHistory : BaseFragment() {

    @Inject lateinit var mHistoryInitializer: HistoryInitializer
    @Inject lateinit var mHistoryFactory: HistoryFactory
    @Inject internal lateinit var userPreferences: UserPreferences
    @Inject @field:DatabaseScheduler
    internal lateinit var databaseScheduler: Scheduler
    @Inject @field:NetworkScheduler
    internal lateinit var networkScheduler: Scheduler
    val mActivity: WebActivity by lazy { requireNotNull(context as WebActivity) { "Context should never be null in onCreate" } }
    var webInited: Boolean = false
    val webViewController: WebViewController by lazy { WebViewController(mActivity, mHistoryInitializer) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerEventBus()
        YoheeApp.injector.inject(this)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        CLog.i("isVisibleToUser = $isVisibleToUser")
        if (isVisibleToUser) {
            mActivity.onDrawerOpenedLock()
            if (!webInited) {
                webInited = true
                var lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT).apply { weight = 1.0f }
                holder_web.addView(webViewController.showingWebView, 0,  lp)
                webViewController.showingWebView.webViewClient = object : YoheeWebClient(mActivity, webViewController) {

                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        CLog.i("url = $url")
                        mActivity.loadUrl(url)
                        return true
                    }
                    override fun onPageFinished(view: WebView, url: String) {
                        view.requestLayout()
                    }
                    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) = Unit
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.history_drawer, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CLog.i("view created...")
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onEvent(event: DrawerEvent) {
        CLog.i("onEvent = ${event.type}")
        when (event.type) {
            DrawerEvent.DRAWER_OPENED, DrawerEvent.HISTORY_DATA_REMOVED -> {
                if (webInited) {
                    mHistoryFactory
                            .buildPage()
                            .subscribeOn(databaseScheduler)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ webViewController.reload() }){}
                }
            }
        }
    }

}
