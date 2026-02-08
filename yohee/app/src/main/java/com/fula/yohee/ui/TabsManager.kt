package com.fula.yohee.ui

import android.app.Activity
import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.webkit.URLUtil
import com.fula.CLog
import com.fula.yohee.constant.Setting
import com.fula.yohee.di.DatabaseScheduler
import com.fula.yohee.di.DiskScheduler
import com.fula.yohee.extensions.tryCatch
import com.fula.yohee.search.SearchEngineProvider
import com.fula.yohee.ui.activity.WebActivity
import com.fula.yohee.utils.FileUtils
import com.fula.yohee.utils.Option
import com.fula.yohee.utils.UrlUtils
import com.fula.yohee.utils.value
import com.fula.yohee.view.*
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class TabsManager @Inject constructor(private val searchEngineProvider: SearchEngineProvider,
                                      @DatabaseScheduler private val databaseScheduler: Scheduler,
                                      @DiskScheduler private val diskScheduler: Scheduler,
                                      private val homeInitializer: HomeInitializer) {

    val tabList = arrayListOf<WebViewController>()
    lateinit var showingTab: WebViewController
        private set

    private var tabChangeListener = emptySet<(WebViewController?) -> Unit>()
    private var tabNumListener = emptySet<(Int) -> Unit>()

    private var isInitialized = false
    private var initializeList = emptyList<() -> Unit>()

    fun addTabChangeListener(listener: ((WebViewController?) -> Unit)) {
        tabChangeListener = tabChangeListener + listener
    }

    fun addTabNumListener(listener: ((Int) -> Unit)) {
        tabNumListener = tabNumListener + listener
    }

    fun cancelPendingWork() {
        initializeList = emptyList()
    }

    fun doAfterInitialization(runnable: () -> Unit) {
        if (isInitialized) {
            runnable()
        } else {
            initializeList = initializeList + runnable
        }
    }

    private fun finishInitialization() {
        isInitialized = true
        for (runnable in initializeList) {
            runnable()
        }
    }

    fun findWebViewInfo(pageUrl: String?): SWebViewTitle? {
        tabList.forEach { tab ->
            tab.webList.forEach {
                if (it.url == pageUrl) return tab.webInfo[it.url]
            }
        }
        return null
    }

//    /**
//     * Initialize the state of the [TabsManager] based on previous state of the browser and with the
//     * new provided [intent] and emit the last tab that should be displayed. By default operates on
//     * a background scheduler and emits on the foreground scheduler.
//     */
//    fun initializeTabs(activity: Activity, intent: Intent?, restoreLostTabs: Boolean): Single<WebViewController> =
//            Single.just(Option.fromNullable(
//                    if (intent?.action == Intent.ACTION_WEB_SEARCH) {
//                        extractSearchFromIntent(intent)
//                    } else {
//                        intent?.dataString
//                    }, intent?.action == Intent.ACTION_WEB_SEARCH))
//                    /*.doOnSuccess { clearTabs() }
//                    .subscribeOn(AndroidSchedulers.mainThread())*/
//                    .flatMapObservable {
//                        return@flatMapObservable when {
//                            (it is Option.None && !restoreLostTabs) -> {
//                                Observable.fromCallable { homeInitializer }
//                            }
//                            it is Option.Search -> Observable.fromCallable { UrlInitializer(it.value()!!) }
//                            else -> this.initializeRegularMode(it.value(), activity)
//                        }
//                    }
//                    .subscribeOn(diskScheduler)
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .map { if (it is HomeInitializer) showingTab else newTab(activity, it) }
//                    .lastOrError()
//                    .doAfterSuccess { finishInitialization() }

    /**
     * Creates an [Observable] that emits the [Bundle] state stored for each previously opened tab
     * on disk. After the list of bundle [Bundle] is read off disk, the old state will be deleted.
     * Can potentially be empty.
     */
    private fun readSavedStateFromDisk(): Observable<Bundle> = Maybe
            .fromCallable { FileUtils.readBundleFromStorage() }
            .flattenAsObservable { bundle ->
                bundle.keySet()
                        .filter { it.startsWith(BUNDLE_KEY) }
                        .map(bundle::getBundle)
                        .apply {
                            CLog.i("filter count = ${count()}")
                        }
            }
            .doOnNext { CLog.i("Restoring previous WebView state now") }

    fun restoreLostTabs(activity: Activity, bundles: MutableList<Bundle>): Single<WebViewController> = Maybe
            .fromCallable { bundles.map { BundleInitializer(it) } }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { clearTabs() }
            .flattenAsObservable {
                it.apply { CLog.i("filter count = ${count()}") }
            }.subscribeOn(diskScheduler)
            .observeOn(AndroidSchedulers.mainThread())
            .map { newTab(activity, it) }
            .lastOrError()

    /**
     * Returns an [Observable] that emits the [TabInitializer] for normal operation mode.
     */
    private fun initializeRegularMode(initialUrl: String?, activity: Activity)
            : Observable<TabInitializer> {
        return restorePreviousTabs().concatWith(Maybe.fromCallable<TabInitializer> {
            CLog.i("init regular mode...")
            return@fromCallable initialUrl?.let {
                if (URLUtil.isFileUrl(it)) {
                    PermissionInitializer(it, activity, homeInitializer)
                } else {
                    UrlInitializer(it)
                }
            }
        }).defaultIfEmpty(homeInitializer)
    }

    /**
     * Returns the URL for a search [Intent]. If the query is empty, then a null URL will be
     * returned.
     */
    fun extractSearchFromIntent(intent: Intent): String? {
        val query = intent.getStringExtra(SearchManager.QUERY)
        val searchUrl = "${searchEngineProvider.provideSearchEngine().queryUrl}${UrlUtils.QUERY_PLACE_HOLDER}"

        return if (query?.isNotBlank() == true) {
            UrlUtils.smartUrlFilter(query, true, searchUrl)
        } else {
            null
        }
    }

    /**
     * Returns an observable that emits the [TabInitializer] for each previously opened tab as
     * saved on disk. Can potentially be empty.
     */
    private fun restorePreviousTabs(): Observable<TabInitializer> = readSavedStateFromDisk()
            .map { bundle ->
                CLog.i("bundle = $bundle")
                return@map BundleInitializer(bundle)
            }


    /**
     * Method used to resume all the tabs in the browser. This is necessary because we cannot pause
     * the WebView when the application is open currently due to a bug in the WebView, where calling
     * onResume doesn't consistently resume it.
     */
    fun resumeAll() {
        CLog.i("resume all...")
        showingTab.resumeTimers()
        for (tab in tabList) {
            tab.apply {
                onResume()
                Setting.syncPreferSettings(webList, mWebClient, userPreferences)
            }
        }
    }

    fun resetAllPreferences() {
        CLog.i("reset prefers...")
        for (tab in tabList) {
            tab.apply {
                Setting.syncPreferSettings(webList, mWebClient, userPreferences)
            }
        }
    }

    /**
     * Method used to pause all the tabs in the browser. This is necessary because we cannot pause
     * the WebView when the application is open currently due to a bug in the WebView, where calling
     * onResume doesn't consistently resume it.
     */
    fun pauseAll() {
        showingTab.pauseTimers()
        tabList.forEach(WebViewController::onPause)
    }

    /**
     * Shutdown the manager. This destroys all tabs and clears the references to those tabs. Current
     * tab is also released for garbage collection.
     */
    fun clearTabs() {
        CLog.i("shut down...")
        repeat(tabList.size) { deleteTab(tabList[0]) }
        isInitialized = false
    }

    /**
     * The current number of tabs in the manager.
     *
     * @return the number of tabs in the list.
     */
    fun size(): Int = tabList.size

    /**
     * The last tab in the tab manager.
     *
     * @return the last tab, or null if there are no tabs.
     */
    fun lastTab(): WebViewController? = tabList.lastOrNull()

    /**
     * Create and return a new tab. The tab is automatically added to the tabs list.
     *
     * @param activity the mActivity needed to create the tab.
     * @param tabInitializer the initializer to run on the tab after it's been created.
     * @param isIncognito whether the tab is an menu_android_incognito tab or not.
     * @return a valid appInitialized tab.
     */
    fun newTab(activity: Activity, tabInitializer: TabInitializer): WebViewController {
        CLog.i("New tab: $tabInitializer")
        val tab = WebViewController(activity as WebActivity, tabInitializer)
        tabList.add(tab)
        tabNumListener.forEach { it(size()) }
        return tab
    }

    /**back pressed
     * return true if there is only one tab.*/
    fun onBackPress(): Boolean {
        if (showingTab.canGoBack()) {
            showingTab.goBack()
            return false
        }
        if (size() <= 1) return true
        deleteTab(showingTab)
        return false
    }

    /**
     * Deletes a tab from the manager. If the tab being deleted is the current tab, this method will
     * switch the current tab to a new valid tab.
     *
     * @return returns true if ti should finish, false otherwise.
     */
    fun deleteTab(rtab: WebViewController) {
        CLog.i("Delete tab: $rtab")
        tabList.remove(rtab)
        if (rtab == showingTab && size() >= 1) switchToTab(tabList[size() - 1])
        rtab.onDestroy()
        tabNumListener.forEach { it(size()) }
    }

    /**
     * Save the im normally closed page state.
     */
    fun saveWebState() {
        val outState = Bundle(ClassLoader.getSystemClassLoader())
        CLog.i("Saving tab state")
        var saved = false
        tabList.filter { UrlUtils.URL_TYPE_WEB == UrlUtils.getType(it.url) }
                .withIndex()
                .forEach { (index, tab) ->
                    CLog.i("save index = $index")
                    saved = true
                    outState.putBundle(BUNDLE_KEY + index, tab.saveState())
                }
        CLog.i("state save: $saved")
        if (saved) {
            FileUtils.writeBundleToStorage(outState)
                    .subscribeOn(diskScheduler)
                    .subscribe()
        } else {
            FileUtils.deleteBundleInStorage()
        }
    }

    /**
     * Use this method to clear the saved state if you do not wish it to be restored when the
     * browser next starts.
     */
    fun clearSavedState() = FileUtils.deleteBundleInStorage()

    /**
     * Returns the [WebViewController] with the provided hash, or null if there is no tab with the hash.
     *
     * @param hashCode the hashcode.
     * @return the tab with an identical hash, or null.
     */
    fun getTabForHashCode(hashCode: Int): WebViewController? =
            tabList.firstOrNull { webViewController -> webViewController.showingWebView.let { it.hashCode() == hashCode } }

    /**
     * Switch the current tab to the one at the given position. It returns the selected tab that has
     * been switched to.
     *
     * @return the selected tab or null if position is out of tabs range.
     */
    fun switchToTab(tab: WebViewController) {
        CLog.i("switch to tab: $tab")
        tryCatch { if (showingTab == tab) return }
        showingTab = tab
        tabChangeListener.forEach { it(showingTab) }
    }

    fun indexOf(tab: WebViewController) = tabList.indexOf(tab)

    companion object {
        const val BUNDLE_KEY = "WEBVIEW_"
    }

}
