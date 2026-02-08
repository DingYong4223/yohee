package com.fula.yohee.ui

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.URLUtil
import androidx.annotation.DrawableRes
import com.flurry.android.FlurryAgent
import com.fula.CLog
import com.fula.base.ViewHelper
import com.fula.permission.PermissionsManager
import com.fula.permission.PermissionsResultAction
import com.fula.yohee.FlurryConst
import com.fula.yohee.R
import com.fula.yohee.constant.INTENT_ORIGIN
import com.fula.yohee.database.Bookmark
import com.fula.yohee.database.BookmarkDatabase
import com.fula.yohee.dialog.DialogHelper
import com.fula.yohee.dialog.DialogItem
import com.fula.yohee.dialog.SheetChoice
import com.fula.yohee.eventbus.DrawerEvent
import com.fula.yohee.eventbus.SEvent
import com.fula.yohee.extensions.copy
import com.fula.yohee.extensions.shortToast
import com.fula.yohee.extensions.snackbar
import com.fula.yohee.extensions.toast
import com.fula.yohee.preference.UserPreferences
import com.fula.yohee.search.engine.BaseSearchEngine
import com.fula.yohee.ui.activity.WebActivity
import com.fula.yohee.ui.page.PageItemEdit
import com.fula.yohee.uiwigit.floatmenu.FloatMenu
import com.fula.yohee.uiwigit.floatmenu.FloatMenuItem
import com.fula.yohee.utils.*
import com.fula.yohee.view.TabInitializer
import com.fula.yohee.view.UrlInitializer
import com.fula.yohee.view.find.FindResults
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import org.greenrobot.eventbus.EventBus

/**
 * Presenter in charge of keeping track of the current tab and setting the current tab of the
 * browser.
 */
class YoheePresenter(
        private val activity: WebActivity,
        private val userPrefer: UserPreferences,
        private val tabsManager: TabsManager,
        private val bookmarkModel: BookmarkDatabase,
        private val dbScheduler: Scheduler) {

    init {
        tabsManager.addTabChangeListener(::tabChangeListener)
    }

    private var shouldClose: Boolean = false
    private var sslStateSubscription: Disposable? = null

//    /**
//     * Initializes the tab manager with the new intent that is handed in by the WebActivity.
//     *
//     * @param intent the intent to handle, may be null.
//     */
//    fun setupTabs(intent: Intent?) = tabsManager.initializeTabs(activity as Activity, intent, userPrefer.restoreLostTabs).subscribeBy(
//            onSuccess = {
//                CLog.i("set up tabs finish...")
//                activity.notifyTabViewInitialized()
//                tabsManager.switchToTab(it)
//            }
//    )

    private fun tabChangeListener(nowTab: WebViewController?) {
        CLog.i("On tab changed: $nowTab")
        nowTab?.let {
            activity.updateSslState(it.sslState())
            sslStateSubscription?.dispose()
            sslStateSubscription = it
                    .sslStateObservable()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(activity::updateSslState)
            it.resumeTimers()
            it.onResume()
//            activity.updateProgress(it.progress)
//            activity.updateUrl(it.url)
        }
    }

    fun checkLostTabs() = Observable
            .fromCallable {
                val bundle = FileUtils.readBundleFromStorage()
                val keys = bundle?.keySet()?.filter { it.startsWith(TabsManager.BUNDLE_KEY) }
                val bundles = mutableListOf<Bundle>()
                if (keys?.isNotEmpty() == true) {
                    for (key in keys) bundle.getBundle(key)?.let {
                        bundles.add(it)
                    }
                }
                return@fromCallable bundles
            }.subscribeOn(dbScheduler)
            .subscribe {
                CLog.i("count = ${it.size}")
                if (it.isNotEmpty()) EventBus.getDefault().post(SEvent(SEvent.TYPE_LOSTED_TAB_NOTIFY).apply {
                    this.obj = it
                })
            }

    /**
     * Handle a new intent from the the main WebActivity.
     *
     * @param intent the intent to handle, may be null.
     */
    fun onNewIntent(intent: Intent?) = tabsManager.doAfterInitialization {
        val url = if (intent?.action == Intent.ACTION_WEB_SEARCH) {
            tabsManager.extractSearchFromIntent(intent)
        } else {
            intent?.dataString
        }

        val tabHashCode = intent?.extras?.getInt(INTENT_ORIGIN, 0) ?: 0
        if (tabHashCode != 0 && url != null) {
            tabsManager.getTabForHashCode(tabHashCode)?.loadUrl(url)
        } else if (url != null) {
            if (URLUtil.isFileUrl(url)) {
                DialogHelper.showOkCancelDialog(activity, R.string.alert_warm, R.string.message_blocked_local, DialogItem(title = R.string.action_yes) {
                    newTab(UrlInitializer(url), true)
                    shouldClose = true
                }, DialogItem(title = R.string.action_cancel) {})
            } else {
                newTab(UrlInitializer(url), true)
                shouldClose = true
            }
        }
    }

    /**
     * Notifies the mPresenter that it should shut down. This should be called when the
     * WebActivity is destroyed so that we don't leak any memory.
     */
    fun shutdown() {
        tabsManager.cancelPendingWork()
        sslStateSubscription?.dispose()
    }

    /**
     * Open a new tab with the specified URL. You can choose to showListDialog the tab or load it in the
     * background.
     *
     * @param tabInitializer the tab initializer to run after the tab as been created.
     * @param show whether or not to switch to this tab after opening it.
     * @return true if we successfully created the tab, false if we have hit max tabs.
     */
    fun newTab(tabInitializer: TabInitializer, show: Boolean): Boolean {
        if (tabsManager.size() >= 10) {
            activity.shortToast(R.string.max_tabs)
            return false
        }
        CLog.i("New tab, showListDialog: $show")
        val newTab = tabsManager.newTab(activity, tabInitializer)
        if (tabsManager.size() == 1) {
            newTab.resumeTimers()
        }
        if (show) tabsManager.switchToTab(newTab)
        return true
    }

    fun onAutoCompleteItemPressed() {
        tabsManager.showingTab.requestFocus()
    }

    private var bookmarkDis: Disposable? = null
    fun showAddBookMark(url: String) {
        if (UrlUtils.isGenUrl(url)) return
        bookmarkDis?.dispose()
        bookmarkDis = bookmarkModel.isBookmark(url)
                .subscribeOn(dbScheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { it ->
                    val select = mutableListOf<Int>()
                    if (null != it && -1 != it) {
                        CLog.i("it = $it")
                        if (it and Bookmark.TYPE_BOOK > 0) select.add(R.id.menu_item_add_book)
                        if (it and Bookmark.TYPE_MARK > 0) select.add(R.id.menu_item_add_mark)
                    }
                    SheetChoice(activity, null, R.menu.menu_bookmark, select, object : FloatMenu.OnItemClickListener {
                        override fun onClick(v: View, item: FloatMenuItem, select: Boolean?) {
                            CLog.i("menu clicked...$select, ${item.id}, contains = $select")
                            tabsManager.showingTab.let {
                                if (null != select && select) {
                                    deleteBookmark(it.url, if (item.id == R.id.menu_item_add_book) Bookmark.TYPE_BOOK else Bookmark.TYPE_MARK)
                                } else {
                                    addBookMark(it.url, it.showingWebView.title, "", if (item.id == R.id.menu_item_add_book) Bookmark.TYPE_BOOK else Bookmark.TYPE_MARK)
                                }
                            }
                        }
                    }).show()
                }
    }

    fun showEngineChoiceMenu(v: View) {
        val floatMenu = FloatMenu(activity)
        val selects = listOf<Int>(when (userPrefer.engine) {
            BaseSearchEngine.ENGINE_GOOGLE -> R.id.menu_google
            BaseSearchEngine.ENGINE_BAIDU -> R.id.menu_baidu
            BaseSearchEngine.ENGINE_YAHOO -> R.id.menu_yahoo
            BaseSearchEngine.ENGINE_SOGOU -> R.id.menu_sogou
            else -> R.id.menu_google
        })
        floatMenu.showMenuLayout(v, R.menu.menu_engines, selects, object : FloatMenu.OnItemClickListener {
            override fun onClick(v: View, item: FloatMenuItem, select: Boolean?) {
                CLog.i("menu clicked...$select, ${R.id.menu_google}")
                when (item.id) {
                    R.id.menu_google -> {
                        FlurryAgent.logEvent(FlurryConst.TOP_MENU_ENGINE_GOOGLE)
                        CLog.i("change engine: google")
                        userPrefer.engine = BaseSearchEngine.ENGINE_GOOGLE
                        EventBus.getDefault().post(SEvent(SEvent.TYPE_HOMEPAGE_INIT_AND_RELOAD))
                    }
                    R.id.menu_baidu -> {
                        FlurryAgent.logEvent(FlurryConst.TOP_MENU_ENGINE_BAIDU)
                        CLog.i("change engine: baidu")
                        userPrefer.engine = BaseSearchEngine.ENGINE_BAIDU
                        EventBus.getDefault().post(SEvent(SEvent.TYPE_HOMEPAGE_INIT_AND_RELOAD))
                    }
                    R.id.menu_yahoo -> {
                        FlurryAgent.logEvent(FlurryConst.TOP_MENU_ENGINE_YAHOO)
                        CLog.i("change engine: yahoo")
                        userPrefer.engine = BaseSearchEngine.ENGINE_YAHOO
                        EventBus.getDefault().post(SEvent(SEvent.TYPE_HOMEPAGE_INIT_AND_RELOAD))
                    }
                    R.id.menu_sogou -> {
                        FlurryAgent.logEvent(FlurryConst.TOP_MENU_ENGINE_SOGOU)
                        CLog.i("change engine: sogou")
                        userPrefer.engine = BaseSearchEngine.ENGINE_SOGOU
                        EventBus.getDefault().post(SEvent(SEvent.TYPE_HOMEPAGE_INIT_AND_RELOAD))
                    }
                }
            }
        })
    }

    fun urlMenuMoreClick(@DrawableRes menuId: Int, select: Boolean?) {
        CLog.i("menu clicked...$menuId, contains = $select")
        tabsManager.showingTab.let {
            when (menuId) {
                R.id.menu_action_copy -> {
                    FlurryAgent.logEvent(FlurryConst.TOP_MENU_MORE_COPY)
                    val clipManager = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipManager.copy(it.url)
                }
                R.id.menu_action_share -> {
                    FlurryAgent.logEvent(FlurryConst.TOP_MENU_MORE_SHARE)
                    ShareUtils.shareUrl(activity, it.url, it.title)
                }
                R.id.menu_action_share_pic -> {
                    FlurryAgent.logEvent(FlurryConst.TOP_MENU_MORE_SHARE_PIC)
                    activity.showingTab().let {
                        PermissionsManager.requestPermissionsIfNecessaryForResult(activity, YoheePermission.STORAGE_READ_WRITE, object : PermissionsResultAction() {
                            override fun onGranted() {
                                val file = ViewHelper.captrueShareWebView(it.showingWebView, activity.toolbarAdapter.getToolbarHeight(), activity.getString(R.string.share_from))
                                ShareUtils.shareImage(activity, file.absolutePath)
                            }
                        })
                    }
                }
                R.id.menu_action_find -> {
                    FlurryAgent.logEvent(FlurryConst.TOP_MENU_MORE_FIND)
                    findInPage()
                }
                R.id.menu_action_offpage -> {
                    FlurryAgent.logEvent(FlurryConst.TOP_MENU_MORE_OFFWEB)
                    WebUtils.saveWeb(activity, it.showingWebView)
                }
                else -> {
                }
            }
        }
    }

    /**
     * method that shows a dialog asking what string the user wishes to search
     * for. It highlights the text entered.
     */
    private var findResult: FindResults? = null

    fun findInPage() = DialogHelper.showEditDialog(activity, R.string.action_find, R.string.search_hint, R.string.search_hint) { text ->
        if (text.isNotEmpty()) {
            findResult = tabsManager.showingTab.find(text)
        }
    }

    private var bkDisposable: Disposable? = null
    private fun addBookMark(url: String, title: String, folder: String = "", type: Int = Bookmark.TYPE_BOOK) {
        if (UrlUtils.isGenUrl(url)) {
            CLog.i("error, wrong url = $url")
            return
        }
        CLog.i("insert data to db, url = $url")
        val bookmark = Bookmark(url, if (title.length > 20) title.substring(0, 20) else title, folder, 0, type)
        bkDisposable?.dispose()
        bkDisposable = bookmarkModel.insertItemIfNotExist(bookmark)
                .subscribeOn(dbScheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { boolean ->
                    if (boolean) {
                        EventBus.getDefault().post(SEvent(SEvent.TYPE_BOOK_MARK_CHANGED).apply { intArg = type })
                        activity.snackbar(R.string.message_bookmark_added, R.string.action_edit) {
                            val intent = PageItemEdit.genIntent(activity, HashMap<String, String>().apply {
                                this[activity.getString(R.string.hint_title)] = bookmark.title
                                this[activity.getString(R.string.hint_url)] = bookmark.url
                            }, bookmark)
                            activity.startActivityForResult(intent, PageItemEdit.ACTIVITY_RESULT_EDITBOOKMARK)
                        }
                    }
                }
    }

    fun deleteBookmark(url: String, type: Int = Bookmark.TYPE_BOOKMARK): Disposable = bookmarkModel
            .deleteItem(Bookmark(url, "", "", 0, type))
            .subscribeOn(dbScheduler)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { boolean ->
                if (boolean) {
                    EventBus.getDefault().post(SEvent(SEvent.TYPE_BOOK_MARK_CHANGED).apply { intArg = type })
                }
            }


    fun updateBookmark2Db(context: Context, old: Bookmark, nbookmarl: Bookmark): Disposable = bookmarkModel
            .editItem(old, nbookmarl)
            .subscribeOn(dbScheduler)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                EventBus.getDefault().post(DrawerEvent(DrawerEvent.BOOKMARK_DATA_CHANGED, 0))
                EventBus.getDefault().post(SEvent(SEvent.TYPE_BOOK_MARK_CHANGED).apply { intArg = nbookmarl.type })
                context.toast(R.string.setting_success)
            }

}
