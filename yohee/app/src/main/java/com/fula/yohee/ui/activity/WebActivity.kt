package com.fula.yohee.ui.activity

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.PowerManager
import android.os.Process.killProcess
import android.os.Process.myPid
import android.text.TextUtils
import android.view.*
import android.view.View.OnClickListener
import android.view.ViewGroup.LayoutParams
import android.view.animation.Transformation
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import android.webkit.WebChromeClient.CustomViewCallback
import android.webkit.WebView
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.ActionBar
import androidx.core.net.toUri
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.flurry.android.FlurryAgent
import com.fula.CLog
import com.fula.base.ViewHelper
import com.fula.base.iview.BasePage
import com.fula.base.ui.video.GSYVideoActivity
import com.fula.base.util.FileUtil
import com.fula.base.util.StatusBarUtil
import com.fula.permission.PermissionsManager
import com.fula.permission.PermissionsResultAction
import com.fula.util.NetUtils
import com.fula.util.ViewUnit
import com.fula.util.YoUtils
import com.fula.view.BottomDrawer
import com.fula.yohee.*
import com.fula.yohee.animation.AnimationUtils
import com.fula.yohee.bean.DetectUrl
import com.fula.yohee.bean.VideoInfo
import com.fula.yohee.constant.Setting
import com.fula.yohee.database.Bookmark
import com.fula.yohee.database.BookmarkDatabase
import com.fula.yohee.database.DownloadsDatabase
import com.fula.yohee.database.HistoryDatabase
import com.fula.yohee.di.MainHandler
import com.fula.yohee.dialog.*
import com.fula.yohee.download.DownloadHandler
import com.fula.yohee.eventbus.DrawerEvent
import com.fula.yohee.eventbus.SEvent
import com.fula.yohee.extensions.*
import com.fula.yohee.interpolator.BezierDecelerateInterpolator
import com.fula.yohee.js.JSAdUIClear
import com.fula.yohee.js.JsGetUrlTxt
import com.fula.yohee.network.SSLState
import com.fula.yohee.search.SearchEngineProvider
import com.fula.yohee.search.SuggestionsAdapter
import com.fula.yohee.search.engine.BaseSearchEngine
import com.fula.yohee.settings.UserSetting
import com.fula.yohee.toolcolor.BaseToolColor
import com.fula.yohee.toolcolor.ToolbarColor
import com.fula.yohee.toolcolor.ToolbarWiteColor
import com.fula.yohee.ui.TabsManager
import com.fula.yohee.ui.WebViewController
import com.fula.yohee.ui.YoheePresenter
import com.fula.yohee.ui.bilogic.AlbumAdapter
import com.fula.yohee.ui.bilogic.SearchListener
import com.fula.yohee.ui.bilogic.SelectModel
import com.fula.yohee.ui.fragment.FragBookmark
import com.fula.yohee.ui.fragment.FragHistory
import com.fula.yohee.ui.page.*
import com.fula.yohee.uiwigit.*
import com.fula.yohee.uiwigit.floatmenu.FloatMenu
import com.fula.yohee.uiwigit.floatmenu.FloatMenuItem
import com.fula.yohee.utils.*
import com.fula.yohee.view.*
import com.fula.yohee.view.SearchView
import com.fula.yohee.widget.DangerBlocker
import com.fula.yohee.widget.VideoSniffer
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.main_bottom_drawer.*
import kotlinx.android.synthetic.main.main_bottombar.*
import kotlinx.android.synthetic.main.main_toolbar.*
import kotlinx.android.synthetic.main.main_toolbar.view.*
import kotlinx.android.synthetic.main.tabview_layout.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.layout_logic.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.concurrent.LinkedBlockingQueue
import javax.inject.Inject

@SuppressLint("InvalidWakeLockTag", "StringFormatInvalid")
abstract class WebActivity : BaseActivity(), OnClickListener {

    companion object {
        const val INTENT_PANIC_TRIGGER = "info.guardianproject.panic.action.TRIGGER"
        private const val WEBVIEW_TRANSIN_TIME = 200L
        private const val FLOAT_TXT_TRANSIN_TIME = 1800L
        private val MATCH_PARENT = FrameLayout.LayoutParams(-1, -1)
    }

    private val videoSniffer: VideoSniffer by lazy { VideoSniffer(detectedTaskUrlQueue, mediaDetectList, 3, 3).apply { startSniffer() } }
    private val detectedTaskUrlQueue by lazy { LinkedBlockingQueue<DetectUrl>() }
    private var pageFinishCount = 0
    private val mediaDetectList: MutableList<VideoInfo> = mutableListOf()
    private val dangerBlocker: DangerBlocker by lazy { DangerBlocker() }
    val invalideSslHost = mutableSetOf<String>()
    lateinit var toolbarAdapter: BaseToolbarAdapter
    private lateinit var toolColor: BaseToolColor
    private val netStateReceiver: NetWorkStateReceiver by lazy { NetWorkStateReceiver() }
    private val mediaPoint: RedPoint by lazy { RedPoint(this) }
    private val debugWindow: DebugWindow by lazy { DebugWindow(this) }
    private val floatWidget: FloatMenuWidget by lazy { FloatMenuWidget(this, id_layer_logic)}
    private val fullWidget: FullscreenWidget by lazy { FullscreenWidget(this, floatWidget, ::fullScreenTrigger) }
    protected val searchView: SearchView by lazy { actionBar.customView.search_view }
    private var videoView: VideoView? = null
    private var customView: View? = null
    private var customViewCallback: CustomViewCallback? = null
    private val yellowTipBar: YellowTipBar by lazy { YellowTipBar(this, content_frame) }
    @Inject
    lateinit var downloadsDB: DownloadsDatabase
    @Inject
    internal lateinit var menuPop: MenuPop
    @Inject
    lateinit var bookmarkModel: BookmarkDatabase
    @Inject
    lateinit var historyModel: HistoryDatabase
    @Inject
    lateinit var engineProvider: SearchEngineProvider
    @Inject
    lateinit var inputMethodManager: InputMethodManager
    lateinit var tabsManager: TabsManager
    @Inject
    lateinit var mHomeInitializer: HomeInitializer
    @Inject
    lateinit var downloadhandler: DownloadHandler
    @Inject
    @field:MainHandler
    lateinit var mainHandler: Handler
    private var sslDrawable: Drawable? = null
    lateinit var mPresenter: YoheePresenter
    private val searchCancelIcon: Drawable by lazy { ThemeUtils.getThemedBoundsDrawable(this, R.drawable.ic_wrong, 24.0f) }
    private val searchNoBookIcon: Drawable by lazy { ThemeUtils.getThemedBoundsDrawable(this, R.mipmap.ic_bookmark, 24.0f) }
    private val searchBookIcon: Drawable by lazy { ThemeUtils.getThemedBoundsDrawable(this, R.drawable.ic_book, 24.0f) }
    private val searchMarkIcon: Drawable by lazy {
        ThemeUtils.getThemedBoundsDrawable(this, R.drawable.ic_mark, 24.0f).apply {
            setColorFilter(Config.COLOR_ITEM_SELECT, PorterDuff.Mode.SRC_IN)
        }
    }
    private val searchBookMarkIcon: Drawable by lazy {
        ThemeUtils.getThemedBoundsDrawable(this, R.mipmap.ic_bookmark, 24.0f).apply {
            setColorFilter(Config.COLOR_ITEM_SELECT, PorterDuff.Mode.SRC_IN)
        }
    }

    private val albumAdapter: AlbumAdapter by lazy {
        AlbumAdapter(this, tabsManager).apply {
            tab_holder.adapter = this
            tab_holder.layoutManager = LinearLayoutManager(this@WebActivity, RecyclerView.HORIZONTAL, false)
            LinearSnapHelper().attachToRecyclerView(tab_holder)
        }
    }

    /**
     * An observable which asynchronously updates the user's cookie preferences.
     */
    protected abstract fun updateCookiePreference(): Completable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        registerEventBus()
        YoheeApp.injector.inject(this)
        initView()
        initData(savedInstanceState)
        initUserSettings()
        registerReceiver(netStateReceiver, IntentFilter().apply {
            addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        })
    }

    private fun initView() {
        initToolBar()
        engineUISync()
        resetToolColor()
        content_frame.layoutTransition = SViewHelper.webViewTrans(WEBVIEW_TRANSIN_TIME)
        button_forward.setOnClickListener(this)
        button_back.setOnClickListener(this)
        button_menu.setOnClickListener(this)
        button_home.setOnClickListener(this)
        button_page.setOnClickListener(this)
        switcher_add.setOnClickListener(this)
        side_drawer.setLayerType(View.LAYER_TYPE_NONE, null)
        drawer_layout.addDrawerListener(DrawerLocker())
        drawer_layout.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN
                    && 1 == drawer_layout.getIntTag(0)) {
                drawer_layout.closeDrawers()
            }
            return@setOnTouchListener false
        }
        initDrawerPager()
        bottomDrawer.apply {
            omnibox = bottom_tool_bar
            addStatusListener {
                when (it) {
                    BottomDrawer.STATE_DRAWER_OPEN -> {
                        EventBus.getDefault().post(SEvent(SEvent.TYPE_NAVI_AND_ALBUM_CHANGE))
                        if (userPrefer.fullScreen) {
                            fullWidget.dismiss()
                        }
                    }
                    BottomDrawer.STATE_DRAWER_CLOSE -> {
                        if (userPrefer.fullScreen) {
                            fullWidget.preShow()
                        }
                    }
                }
            }
        }
    }

    private fun initData(savedInstanceState: Bundle?) {
        tabsManager = TabsManager(engineProvider, dbScheduler, diskScheduler, mHomeInitializer)
        tabsManager.addTabChangeListener(::tabChangeListener)
        tabsManager.addTabNumListener {
            button_page.setImageBitmap(DrawableUtils.getRectedNumberImage(it, 16f,
                    16f, Color.BLACK, ViewUnit.dp2px(0.5f)))
            albumAdapter.notifyDataSetChanged()
        }
        toolbarAdapter = if (userPrefer.fixToolbar) {
            FixToolbarAdapter(this, userPrefer, toolbar_layout, status_keep)
        } else {
            ToolbarAdapter(this, userPrefer, toolbar_layout, status_keep).apply {
                listener = {
                    if (progress_view.translationY != it) {
                        progress_view.translationY = it
                    }
                    yellowTipBar.updateMarginTop(it)
                }
            }
        }
        mPresenter = YoheePresenter(this, userPrefer, tabsManager, bookmarkModel, dbScheduler).apply {
            val url = getIntentUrl(savedInstanceState)
            if (!url.isNullOrEmpty()) {
                CLog.i("url = $url")
                newTab(UrlInitializer(url), true)
                intent = null
            } else {
                CLog.i("init home...")
                newTab(mHomeInitializer, true)
                checkLostTabs()
                notifyTabViewInitialized()
            }
        }
    }

    private fun getIntentUrl(savedInstanceState: Bundle?): String? {
        var intent: Intent? = if (savedInstanceState == null) {
            intent
        } else {
            null
        }
        return if (intent?.action == Intent.ACTION_WEB_SEARCH) {
            tabsManager.extractSearchFromIntent(intent)
        } else {
            intent?.dataString
        }
    }

    fun detectMediaUrl(url: String, pageTitle: String) {
        CLog.i("detect quene add: $url")
        detectedTaskUrlQueue.put(DetectUrl(url, pageTitle))
        videoSniffer.trigger()
    }

    fun onReceivedTitle(view: WebView?, title: String) {
        view?.url?.let {
            CLog.i("receiveTitle: ${view.url}, title: $title")
            updateHistory(title, view.url)
        }
    }

    private fun importProvideMarksAndReload(tip: Boolean) = bookmarkModel.insertItems(BookmarkDatabase.getProvideMarks())
            .subscribeOn(dbScheduler)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                CLog.i("import success...")
                EventBus.getDefault().post(SEvent(SEvent.TYPE_HOMEPAGE_INIT_AND_RELOAD))
                if (tip) shortToast(R.string.first_import_tip)
            }

    public override fun onWindowVisibleToUserAfterResume() {
        super.onWindowVisibleToUserAfterResume()
        CLog.i("onWindowVisibleToUserAfterResume")
        /*showingTab?.let {
            adaptToolBarStatet(fullWidget.getIsCollaps(), UrlUtils.getType(it.url))
        }*/
    }

    override fun onWindowAttributesChanged(params: WindowManager.LayoutParams?) {
        super.onWindowAttributesChanged(params)
        CLog.i("onWindowAttributesChanged befor")
        tryCatch {
            CLog.i("onWindowAttributesChanged befor")
            //CLog.i("onWindowAttributesChanged....${toolbar_layout.height}")
        }
    }

    private val fragments = arrayOfNulls<Fragment>(2)
    private fun initDrawerPager() {
        mainHandler.postDelayed({
            if (userPrefer.rightHandUsage) { //right hand habit
                val lp = side_drawer.layoutParams as DrawerLayout.LayoutParams
                lp.gravity = Gravity.END
                side_drawer.layoutParams = lp
            }
            fragments[0] = FragBookmark()
            fragments[1] = FragHistory()
            view_pager.adapter = object : FragmentPagerAdapter(supportFragmentManager) {
                override fun getItem(i: Int): Fragment = fragments[i]!!
                override fun getCount(): Int = fragments.size
                override fun getPageTitle(position: Int): CharSequence {
                    return if (position == 0) {
                        getString(R.string.bookmark)
                    } else {
                        getString(R.string.action_history)
                    }
                }
            }
            tab_layout.setupWithViewPager(view_pager)
            view_pager.currentItem = 0
        }, 300)
    }

    private val actionBar: ActionBar by lazy { requireNotNull(supportActionBar) }
    private fun initToolBar() {
        setSupportActionBar(toolSetingbar)
        actionBar.setDisplayShowTitleEnabled(false)
        actionBar.setDisplayShowHomeEnabled(false)
        actionBar.setDisplayShowCustomEnabled(true)
        actionBar.setCustomView(R.layout.main_toolbar)
        val customView = actionBar.customView
        customView.layoutParams = customView.layoutParams.apply {
            width = LayoutParams.MATCH_PARENT
            height = LayoutParams.MATCH_PARENT
        }
        toolbar_layout.let {
            it.removeFromParent()
            content_frame.addView(it, -1)
        }
        searchView.apply {
            compoundDrawablePadding = ViewUnit.dp2px(3f)
            setCompoundDrawablesWithIntrinsicBounds(sslDrawable, null, null, null)
            SearchListener(this@WebActivity, this).let {
                onFocusChangeListener = it
                focusListener = it
                setOnKeyListener(it)
                setOnEditorActionListener(it)
                addTextChangedListener(it)
            }
            initializeSearchSuggestions(this)
            drawableListener = ::searchViewDrawbleClick
        }
        engine_choice.setOnClickListener(this@WebActivity)
    }

    private fun searchViewDrawbleClick(searchView: SearchView, rightDrawable: Drawable, index: Int) {
        CLog.i("search right click...")
        if (searchView.hasFocus()) {
            return searchView.setText("")
        }
        when (index) {
            SearchView.DRAWABLE_LEFT -> {
                if (rightDrawable == sslDrawable) {
                    showTabSslError()
                }
            }
            SearchView.DRAWABLE_RIGHT -> {
                tabsManager.showingTab.let {
                    if (rightDrawable == searchCancelIcon) {
                        it.stopLoading()
                    } else {
                        mPresenter.showAddBookMark(it.url)
                    }
                }
            }
        }
    }

    private fun showTabSslError() {
        val sslState = showingTab().sslState()
        if (sslState.state == SSLState.STATE_INVALIDE) {
            val errorArray = sslState.getAllSslErrorMessageCodes()
            val stringBuilder = StringBuilder()
            for (messageCode in errorArray) {
                stringBuilder.append(" - ").append(getString(messageCode)).append("\n")
            }
            val alertMessage = getString(R.string.message_insecure_connection, stringBuilder.toString())
            DialogHelper.showOkDialog(this, R.string.alert_warm, alertMessage, DialogItem(title = R.string.confirm) {})
        }
    }

    /**
     * Choose what to do when the browser visits a website.
     *
     * @param title the title of the site visited.
     * @param url the url of the site visited.
     */
    private fun updateHistory(title: String?, url: String) {
        if (UrlUtils.isGenUrl(url)) return
        historyModel.visitHistoryEntry(url, title)
                .subscribeOn(dbScheduler)
                .subscribe()
    }

    protected fun panicClean() {
        CLog.i("Closing browser")
        tabsManager.newTab(this, NoOpInitializer())
        //tabsManager.switchToTab(0)
        tabsManager.clearSavedState()
        powerOffAndExit()
    }

    fun searchAction(searchView: SearchView) {
        searchView.let {
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
            searchTheWeb(it.text.toString())
        }
        tabsManager.showingTab.requestFocus()
    }

    fun onSearchViewFocusChange(v: View, hasFocus: Boolean) {
        invalidateOptionsMenu()
        val currentView = tabsManager.showingTab
        if (!hasFocus) {
            freshSearchIcon(currentView.progress < 100)
            updateUrl(currentView.url)
        } else if (hasFocus) {
            (v as SearchView).selectAll()
            searchView.setCompoundDrawablesWithIntrinsicBounds(null, null, searchCancelIcon, null)
        }
        if (!hasFocus) {
            searchView.let {
                inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
            }
        }
    }

    private fun resetToolColor() {
        val colorViews = listOf(toolbar_layout, bottom_tool_bar, status_keep)
        toolColor = if (userPrefer.toolColorFollow) ToolbarColor(this, colorViews, progress_view) else ToolbarWiteColor(this, colorViews, progress_view)
    }

    private fun initUserSettings() {
        resetToolColor()
        invalidateFullscreenUI()
        updateCookiePreference().subscribeOn(Schedulers.computation()).subscribe()
        /*if (UserSetting.NO_VALUE == userPrefer.dynamicBg) {
            dynamicBg.setImageDrawable(null)
            dynamicBg.visibility = View.GONE
        } else {
            Glide.with(this@WebActivity).load(userPrefer.dynamicBg).into(dynamicBg)
        }*/
        /**drawer*/
        val lp = side_drawer.layoutParams as DrawerLayout.LayoutParams
        lp.gravity = if (userPrefer.rightHandUsage) Gravity.END else Gravity.START
        side_drawer.layoutParams = lp

        when {
            userPrefer.floatMenu == UserSetting.DOT_SHOW_ALWAYS -> floatWidget.visiable(View.VISIBLE)
            userPrefer.floatMenu == UserSetting.DOT_SHOW_ONLY_FSCREEN
                    && userPrefer.screenRotate == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> floatWidget.visiable(View.VISIBLE)
            else -> floatWidget.visiable(View.GONE)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        CLog.i("item = $item")
        when (item.itemId) {
            R.id.url_search -> {
                FlurryAgent.logEvent(FlurryConst.TOP_MENU_FIND_CLICK)
                CLog.i("url search...")
                searchAction(searchView)
            }
            R.id.url_action_more -> {
                FlurryAgent.logEvent(FlurryConst.TOP_MENU_MORE_CLICK)
                CLog.i("url action...")
                val floatMenu = FloatMenu(this)
                floatMenu.showMenuLayout(R.menu.menu_web_more, null, object : FloatMenu.OnItemClickListener {
                    override fun onClick(v: View, item: FloatMenuItem, select: Boolean?) {
                        CLog.i("menu clicked...$select")
                        mPresenter.urlMenuMoreClick(item.id, select)
                    }
                })
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun engineUISync() {
        when (userPrefer.engine) {
            BaseSearchEngine.ENGINE_GOOGLE -> engine_choice.setImageResource(R.mipmap.icon_google)
            BaseSearchEngine.ENGINE_BAIDU -> engine_choice.setImageResource(R.mipmap.icon_baidu)
            BaseSearchEngine.ENGINE_YAHOO -> engine_choice.setImageResource(R.mipmap.icon_yahoo)
            BaseSearchEngine.ENGINE_SOGOU -> engine_choice.setImageResource(R.mipmap.icon_sogou)
            else -> engine_choice.setImageResource(R.mipmap.icon_google)
        }
    }

    private fun notifyTabViewInitialized() {
        CLog.i("Notify Tabs Initialized")
        if (0 == userPrefer.appInitialized) {
            DialogHelper.showPrivacyDialog(this,
                    positiveButton = DialogItem(title = R.string.privacy_confirm, colorTint = Color.BLUE) {
                        PermissionsManager.requestPermissionsIfNecessaryForResult(YoheeApp.mainActivity, YoheePermission.STORAGE_READ_WRITE, object : PermissionsResultAction() {
                            override fun onGranted() {
                                val jar = FileUtils.createYohheFile("", FileUtils.SAVE_SETTINGS)
                                if (jar.isFile && jar.exists()) {
                                    DialogHelper.showOkCancelDialog(this@WebActivity, R.string.alert_warm, R.string.setting_import_tip,
                                            positiveButton = DialogItem(title = R.string.action_yes) {
                                                FlurryAgent.logEvent(FlurryConst.IMPORT_SETTING_FROMSAVE_CLICK)
                                                DiskSave(this@WebActivity).importSave2App(jar, userPrefer)
                                            },
                                            negativeButton = DialogItem(title = R.string.action_no) {
                                                importProvideMarksAndReload(false)
                                            }
                                    )
                                }
                            }
                        })
                        if (YoUtils.isZh(this)) userPrefer.engine = BaseSearchEngine.ENGINE_BAIDU
                        importProvideMarksAndReload(true)
                        userPrefer.appInitialized = 1
                    },
                    negativeButton = DialogItem(title = R.string.privacy_reject) {finish()}
            )
        }
    }

    fun updateSslState(sslState: SSLState) {
        sslDrawable = when (sslState.state) {
            SSLState.STATE_VALIDE -> getDrawable(R.drawable.ic_ssl_check)
            SSLState.STATE_INVALIDE -> getDrawable(R.drawable.ic_ssl_error)
            else -> null
        }
        searchView.setCompoundDrawablesWithIntrinsicBounds(sslDrawable, null, searchView.compoundDrawables[2], null)
    }

    private fun tabChangeListener(switchTab: WebViewController?) {
        CLog.i("switchTab = $switchTab")
        if (switchTab == null) { //delete over
            return powerOffAndExit()
        }
        drawer_layout.closeDrawers()
        EventBus.getDefault().post(SEvent(SEvent.TYPE_NAVI_AND_ALBUM_CHANGE))

        toolColor.tabSwitch(switchTab)
        tabWebChangeListener(switchTab.showingWebView)
    }

    /**与当前tab相关的web变动监听*/
    fun tabWebChangeListener(webView: NSWebView) {
        webView.let {
            CLog.i("Setting the tab view")
            it.removeFromParent()
            it.requestFocus()
            content_frame.apply {
                removeAllWebViews()
                addView(it.apply {
                    translationY = toolbar_layout.translationY + toolbarAdapter.getToolbarHeight()
                }, MATCH_PARENT)

                bringChildToFront(progress_view)
                progress_view.translationY = it.translationY
            }
            adaptToolBarStatet(UrlUtils.getType(it.url))
            invalidateOptionsMenu()
        }
        updateUrl(webView.url)
        if (webView.progress < 100) {
            updateProgress(webView.progress.toFloat())
        } else {
            AnimationUtils.startAnim(0f, 100f, 10L, ::updateProgress)
        }
        EventBus.getDefault().post(SEvent(SEvent.TYPE_NAVI_AND_ALBUM_CHANGE))
    }

    fun bookmarkItemClicked(entry: Bookmark) {
        loadUrl(entry.url)
        mainHandler.postDelayed({ drawer_layout.closeDrawers() }, 150)
    }

    protected fun handleNewIntent(intent: Intent) {
        mPresenter.onNewIntent(intent)
    }

    private fun performExitCleanUp() {
        if (userPrefer.clearCacheExit) {
            WebUtils.clearCache(this)
            CLog.i("Cache Cleared")
        }
        if (userPrefer.clearHistoryExit) {
            WebUtils.clearHistory(this, historyModel, dbScheduler)
            CLog.i("History Cleared")
        }
        if (userPrefer.clearCookiesExit) {
            WebUtils.clearCookies(this)
            CLog.i("Cookies Cleared")
        }
        if (userPrefer.clearWebCacheExit) {
            WebUtils.clearWebStorage()
            CLog.i("WebStorage Cleared")
        }
    }

    /**横竖屏切换时回调*/
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        CLog.i("configer changed...")
        invalidateOptionsMenu()
        invalidateFullscreenUI()
        if (bottomDrawer.status == BottomDrawer.STATE_DRAWER_OPEN) {
            bottomDrawer.closeDrawer()
        }
        bottomDrawer.invalidateParams(newConfig)
    }

    override fun onBackPressed() {
        CLog.i("back key pressed...")
        if (searchView.hasFocus()) {
            tabsManager.showingTab.requestFocus()
            return
        }
        if (bottomDrawer.status == BottomDrawer.STATE_DRAWER_OPEN) {
            bottomDrawer.closeDrawer()
            return
        }
        if (drawer_layout.isDrawerOpen(side_drawer)) {
            drawer_layout.closeDrawer(side_drawer)
            return
        }
        if (tabsManager.onBackPress()) {
            if (Utils.fastClick(1000L)) {
                return powerOffAndExit()
            }
            shortToast(R.string.will_exit_tip)
        }
    }

    override fun onPause() {
        super.onPause()
        CLog.i("onPause")
        tabsManager.pauseAll()
        if (!isFinishing) {
            tabsManager.saveWebState()
        } else {
            overridePendingTransition(R.anim.fade_in_scale, R.anim.slide_down_out)
        }
    }

    override fun onDestroy() {
        CLog.i("onDestroy")
        videoSniffer.stopSniffer()
        mediaPoint.dismiss()
        fullWidget.dismiss()
        mainHandler.removeCallbacksAndMessages(null)
        mPresenter.shutdown()
        yellowTipBar.destroy()
        downloadhandler.destroy()
        unregisterReceiver(netStateReceiver)
        super.onDestroy()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        tabsManager.clearTabs()
    }

    override fun onResume() {
        super.onResume()
        CLog.i("onResume...")
        StatusBarUtil.setStatusBarIconMode(this, toolColor.blackStatus)
        tabsManager.resumeAll()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        CLog.i("new intent...")
        val url = intent.dataString
        if (!url.isNullOrEmpty()) {
            CLog.i("url = $url")
            tabsManager.showingTab.loadUrl(url)
        }
    }

    /**
     * searches the web for the query fixing any and all problems with the input
     * checks if it is a search, url, etc.
     */
    private val intentUtils = IntentUtils(this)

    private fun searchTheWeb(query: String) {
        if (query.isEmpty()) return

        showingTab().stopLoading()
        val searchUrl = "${engineProvider.provideSearchEngine().queryUrl}${UrlUtils.QUERY_PLACE_HOLDER}"
        if (URLUtil.isValidUrl(query)) {
            CLog.i("request url directly...")
            if (!intentUtils.startActivityForUrl(showingTab().showingWebView, query)) {
                loadUrl(UrlUtils.smartUrlFilter(query.trim(), true, searchUrl))
            }
        } else {
            loadUrl(UrlUtils.smartUrlFilter(query.trim(), true, searchUrl))
        }
    }

    fun updateUrl(url: String?) {
        CLog.i("new url visited...")
        url?.let {
            val currentTab = tabsManager.showingTab
            searchView.setText(UrlUtils.getTitle(this, it, currentTab.title))
            updateUrlPageUI(url)
        }
    }

    private fun updateNaviBntState() {
        tabsManager.showingTab.let {
            val isHome = UrlUtils.isGenUrl(it.url)
            ViewHelper.setImgViewEnable(button_back, !isHome && it.canGoBack())
            CLog.i("goback = ${it.canGoBack()}, goforward ${it.canGoForward()}")
            ViewHelper.setImgViewEnable(button_forward, it.canGoForward())
        }
    }

    /**update the UI about the start view*/
    private fun updateUrlPageUI(url: String?): Boolean {
        val isHome = UrlUtils.isGenUrl(url)
        engine_choice.visibility = if (isHome) View.VISIBLE else View.GONE
        return isHome
    }

    fun updateProgress(pros: Float) {
        CLog.i("pros = $pros")
        freshSearchIcon(pros < 100f)
        progress_view.progress = pros.toInt()
    }

    /**
     * method to generate search suggestions for the AutoCompleteTextView from
     * previously searched URLs
     */
    private fun initializeSearchSuggestions(autoCTV: AutoCompleteTextView) {
        autoCTV.threshold = 1
        autoCTV.dropDownWidth = -1
        autoCTV.dropDownAnchor = R.id.toolSetingbar
        autoCTV.onItemClickListener = OnItemClickListener { _, view, _, _ ->
            val urlTxt = (view.findViewById<View>(R.id.url) as TextView).text
            var url: String = urlTxt.toString()
            if (TextUtils.isEmpty(url) || url.startsWith(getString(R.string.suggestion))) {
                val urlTitle = (view.findViewById<View>(R.id.title) as TextView).text
                url = urlTitle.toString()
            }
            if (TextUtils.isEmpty(url)) {
                return@OnItemClickListener
            }
            autoCTV.setText(url)
            searchTheWeb(url)
            inputMethodManager.hideSoftInputFromWindow(autoCTV.windowToken, 0)
            mPresenter.onAutoCompleteItemPressed()
        }
        autoCTV.setSelectAllOnFocus(true)
        autoCTV.setAdapter(SuggestionsAdapter(this))
    }

    /**
     * helper function that opens the bookmark drawer
     */
    private fun openDrawer() {
        if (drawer_layout.isDrawerOpen(side_drawer)) {
            drawer_layout.closeDrawers()
        }
        drawer_layout.openDrawer(side_drawer)
    }

    /**
     * used to allow uploading into the browser
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (null == intent) {
            return
        }
        when (requestCode) {
//            PageItemEdit.ACTIVITY_RESULT -> {
//                val map = intent.getSerializableExtra(PageItemEdit.KEY_MAP) as java.util.HashMap<String, String>
//                val txt = map[KEY_SHARE]
//                txt?.let {
//                    ShareUtils.shareText(this, it)
//                }
//            }
            PageItemEdit.ACTIVITY_RESULT_EDITBOOKMARK -> {
                val old = intent.getSerializableExtra(PageItemEdit.KEY_OBJ) as Bookmark

                val keyTitle = getString(R.string.hint_title)
                val keyUrl = getString(R.string.hint_url)
                val map = intent.getSerializableExtra(PageItemEdit.KEY_MAP) as HashMap<String, String>
                val newItem = old.copy(url = map[keyUrl]!!, title = map[keyTitle]!!, folder = old.folder, position = old.position, type = old.type)
                mPresenter.updateBookmark2Db(this, old, newItem)
            }
        }
    }

    private fun hideToolBar() {
        CLog.i("hide tool bar...")
        val to = -toolbarAdapter.getToolbarHeight()
        val from = toolbar_layout.translationY
//        if (from == to) {
//            return CLog.i("hide aready...")
//        }
        content_frame.startAnim(250, BezierDecelerateInterpolator()) { _: View, inter: Float, _: Transformation ->
            val trans = inter * (to - from)
            toolbarAdapter.adapt(showingTab(), from + trans, toolColor.endColor)
            //progress_view.translationY = showingWeb().translationY
        }
    }

    private fun showToolBar() {
        CLog.i("show tool bar...")
        val to = 0f
        val from = toolbar_layout.translationY
//        if (from == to) {//全屏模式transy=0时，切换到非全屏，statusbar高度变了，但webView没有下移
//            return CLog.i("tool bar show aready...")
//        }
        content_frame.startAnim(250, BezierDecelerateInterpolator()) { _: View, inter: Float, _: Transformation ->
            val trans = inter * (to - from)
            toolbarAdapter.adapt(showingTab(), from + trans, toolColor.endColor)
            //progress_view.translationY = showingWeb().translationY
        }
    }

    private var actionDown = false;
    fun webViewTouchDown() {
        actionDown = true
        if (userPrefer.fullScreen) {
            if (UrlUtils.URL_TYPE_WEB == UrlUtils.getType(showingTab().url) && !fullWidget.getIsCollaps()) {
                fullWidget.switchCollapse()
            }
        }
    }

    fun webViewTouchMove(deltaY: Float): Float {
        return if (userPrefer.fullScreen) {
            0f
        } else {
            val offY = toolbar_layout.translationY + deltaY
            toolbarAdapter.adapt(showingTab(), offY, toolColor.endColor)
            //progress_view.translationY = showingWeb().translationY
        }
    }

    fun webViewTouchUp() {
        actionDown = false
        if (!userPrefer.fullScreen) {
            if (toolbar_layout.translationY < -toolbarAdapter.getToolbarHeight() / 2) {
                hideToolBar()
            } else {
                showToolBar()
            }
        }
    }

    private fun setVideoViewListener(view: VideoView) {
        view.setOnErrorListener(VideoCompletionListener())
        view.setOnCompletionListener(VideoCompletionListener())
    }

    private val wakeLock: PowerManager.WakeLock by lazy {
        (getSystemService(POWER_SERVICE) as PowerManager).newWakeLock(PowerManager.FULL_WAKE_LOCK, "WakeLock")
    }

    fun onShowCustomView(view: View, callback: CustomViewCallback) = onShowCustomView(view, callback, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
    fun onShowCustomView(view: View, callback: CustomViewCallback, orientation: Int) {
        if (customView != null) return tryCatch { callback.onCustomViewHidden() }
        customViewCallback = callback
        requestedOrientation = orientation
        val decorView = window.decorView as FrameLayout
        view.setBackgroundColor(Color.BLACK)
        if (view is FrameLayout) {
            val child = view.focusedChild
            if (child is VideoView) {
                videoView = child.apply { setVideoViewListener(this) }
            }
        } else if (view is VideoView) {
            videoView = view.apply { setVideoViewListener(this) }
        }
        decorView.addView(view, MATCH_PARENT)
        decorView.requestLayout()
        customView = view
        wakeLock.acquire()
    }

    fun onHideCustomView() {
        CLog.i("hide...")
        if (customView == null) return tryCatch {
            customViewCallback?.onCustomViewHidden()
            customViewCallback = null
        }
        CLog.i("onHideCustomView")
        tabsManager.showingTab.setVisibility(View.VISIBLE)
        tryCatch { customView?.keepScreenOn = false }
        customView?.removeFromParent()
        customView = null

        videoView?.let {
            it.stopPlayback()
            it.setOnErrorListener(null)
            it.setOnCompletionListener(null)
            videoView = null
        }
        EventBus.getDefault().post(SEvent(SettingAdapter.INDEX_SCREEN_ROTATE).apply {
            intArg = userPrefer.screenRotate
        })
        tryCatch {
            customViewCallback?.onCustomViewHidden()
            customViewCallback = null
        }
        wakeLock.release()
    }

    private inner class VideoCompletionListener : MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
        override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean = false
        override fun onCompletion(mp: MediaPlayer) = onHideCustomView()
    }

    private fun onCurwebGoforward(): Boolean {
        val curTab = tabsManager.showingTab
        if (curTab.canGoForward()) {
            curTab.goForward()
            drawer_layout.closeDrawers()
            return true
        }
        return false
    }

    @SuppressLint("RestrictedApi")
    private fun invalidateFullscreenUI() {
        bottom_tool_bar.removeFromParent()
        if (userPrefer.fullScreen) {
            /*if (immersive) {
                decor.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            } else {
                decor.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            }*/
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN)
            content_frame.post {
                fullWidget.showFSPoint(bottom_tool_bar, content_frame)
            }
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

            val llp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            llp.gravity = Gravity.BOTTOM
            ui_layout.addView(bottom_tool_bar.apply {
                layoutParams.width = -1
            }, -1)
            fullWidget.dismiss()
        }
        adaptToolBarStatet(UrlUtils.getType(showingTab().url))
        side_drawer.apply {
            setPadding(paddingLeft, toolbarAdapter.heightStatus.toInt(), paddingRight, paddingBottom)
        }
        invalidateWebPaddingBottom()
    }

    private fun invalidateWebPaddingBottom() {
        if (userPrefer.fullScreen) {
            content_frame.setPadding(0, 0, 0, 0)
        } else {
            val padding = if (userPrefer.fixToolbar) {
                toolbarAdapter.getToolbarHeight()
            } else {
                toolbarAdapter.heightStatus
            }
            CLog.i("web padding bottom: $padding")
            content_frame.setPadding(0, 0, 0, padding.toInt())
        }
    }

    /**
     * This method handles the JavaScript callback to create a new tab.
     * Basically this handles the event that JavaScript needs to create
     * a popup.
     * @param resultMsg the transport message used to send the URL to
     * the newly created WebView.
     */
    fun onCreateWindow(resultMsg: Message) {
        mPresenter.newTab(ResultMessageInitializer(resultMsg), true)
    }

    /**
     * Closes the specified [WebViewController]. This implements
     * the JavaScript callback that asks the tab to close itself and
     * is especially helpful when a page creates a redirect and does
     * not need the tab to stay open any longer.
     *
     * @param tab the WebViewController to close, delete it.
     */
    fun onCloseWindow(tab: WebViewController) = tabsManager.deleteTab(tab)

    /**open a url*/
    fun loadUrl(url: String) {
        drawer_layout.closeDrawers()
        tabsManager.showingTab.loadUrl(url)
    }

    fun handleNewTab(url: String, show: Boolean) {
        if (mPresenter.newTab(UrlInitializer(url), show)) {
            if (!show) { //open background
                if (UrlUtils.isDownwebUrl(tabsManager.showingTab.url)) {
                    EventBus.getDefault().post(SEvent(SEvent.TYPE_BACKOPEN_GETURL_TXT).apply {
                        stringArg = url.toUri().host
                    })
                } else {
                    showingTab().showingWebView.loadJavascript(JsGetUrlTxt(url).getJs())
                }
            }
        }
    }

    /**
     * This method lets the search bar know that the page is currently loading
     * and that it should display the stop icon to indicate to the user that
     * pressing it stops the page from loading
     */
    private var freshIconDis: Disposable? = null

    private fun freshSearchIcon(isLoading: Boolean) {
        if (!searchView.hasFocus()) {
            if (isLoading) {
                searchView.setCompoundDrawablesWithIntrinsicBounds(sslDrawable, null, searchCancelIcon, null)
            } else {
                searchView.setCompoundDrawablesWithIntrinsicBounds(sslDrawable, null, searchNoBookIcon.apply {
                    setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN)
                }, null)
                val url = tabsManager.showingTab.url
                if (!UrlUtils.isGenUrl(url)) {
                    freshIconDis?.dispose()
                    freshIconDis = bookmarkModel.findItemForUrl(url)
                            .subscribeOn(dbScheduler)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe { item ->
                                item?.apply {
                                    val icon2 = when (type) {
                                        Bookmark.TYPE_MARK -> searchMarkIcon
                                        Bookmark.TYPE_BOOKMARK -> searchBookMarkIcon
                                        else -> searchBookIcon.apply {
                                            setColorFilter(Config.COLOR_ITEM_SELECT, PorterDuff.Mode.SRC_IN)
                                        }
                                    }
                                    searchView.setCompoundDrawablesWithIntrinsicBounds(sslDrawable, null, icon2, null)
                                }
                            }
                }
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_forward -> {
                FlurryAgent.logEvent(FlurryConst.BOTTOM_MENU_FORWARD_CLICK)
                onCurwebGoforward()
            }
            R.id.button_back -> {
                FlurryAgent.logEvent(FlurryConst.BOTTOM_MENU_BACK_CLICK)
                tabsManager.showingTab.goBack()
            }
            R.id.button_menu -> {
                FlurryAgent.logEvent(FlurryConst.BOTTOM_MENU_MAIN_CLICK)
                if (Utils.fastClick()) return
                showMenuMain()
            }
            R.id.button_home -> {
                FlurryAgent.logEvent(FlurryConst.BOTTOM_MENU_HOME_CLICK)
                showingTab().reinitialize(mHomeInitializer)
            }
            R.id.button_page -> {
                FlurryAgent.logEvent(FlurryConst.BOTTOM_MENU_PAGE_CLICK)
                bottomDrawer.openDrawer()
            }
            R.id.switcher_add -> {
                FlurryAgent.logEvent(FlurryConst.BOTTOM_MENU_NEW_PAGE)
                bottomDrawer.closeDrawer()
                mPresenter.newTab(mHomeInitializer, true)
            }
            R.id.engine_choice -> {
                FlurryAgent.logEvent(FlurryConst.TOP_MENU_ENGINE_CLICK)
                mPresenter.showEngineChoiceMenu(engine_choice)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        CLog.i("ontouch: ${event.action}")
        val url = tabsManager.showingTab.url
        val isStartPg = UrlUtils.isHomePage(url)
        if (isStartPg) return false
        return super.onTouchEvent(event)
    }

    private fun showMenuMain() {
        val idmap = HashMap<Int, Drawable>()
        idmap.put(R.id.mm_addbookmark, searchView.compoundDrawables[2])
        MenuSheetMain(this, idmap, showingTab().showingWebView, userPrefer, object : BaseDialog.MenuListener {
            override fun onCLick(arg0: Int, arg1: Boolean) {
                val controller = tabsManager.showingTab
                when (arg0) {
                    R.id.mm_bookmark -> {
                        FlurryAgent.logEvent(FlurryConst.SHEET_BOOKMARK_CLICK)
                        openDrawer()
                    }
                    R.id.mm_downlaod -> {
                        FlurryAgent.logEvent(FlurryConst.SHEET_DOWNLOAD_CLICK)
                        val intent = BasePage.genTitleIntent(this@WebActivity, PageDownload::class.java, R.string.action_downloads)
                        startActivity(intent)
                        //mPresenter.newTab(tabsManager.downloadPageInitializer, true)
                    }
                    R.id.mm_imageblock -> {
                        FlurryAgent.logEvent(FlurryConst.SHEET_BLOCKIMG_CLICK)
                        showBlockImg()
                    }
                    R.id.mm_night -> {
                        FlurryAgent.logEvent(FlurryConst.SHEET_NIGHT_CLICK)
                        userPrefer.nightMode = !userPrefer.nightMode
                        Setting.applyModeToWindow(this@WebActivity, window)
                    }
                    R.id.mm_fresh -> {
                        FlurryAgent.logEvent(FlurryConst.SHEET_FRESH_CLICK)
                        tabsManager.showingTab.reload()
                    }
                    R.id.mm_addbookmark -> {
                        FlurryAgent.logEvent(FlurryConst.SHEET_FRESH_CLICK)
                        mPresenter.showAddBookMark(controller.url)
                    }
                    R.id.mm_share -> {
                        FlurryAgent.logEvent(FlurryConst.SHEET_SHARE_CLICK)
                        ShareUtils.shareUrl(this@WebActivity, controller.url, controller.title)
                    }
                    R.id.mm_full_screen -> {
                        FlurryAgent.logEvent(FlurryConst.SHEET_FULLSCREEN_CLICK)
                        userPrefer.fullScreen = !userPrefer.fullScreen
                        toolbarAdapter.updateStatusBarHeight()
                        invalidateFullscreenUI()
                    }
                    R.id.mm_toolbox -> {
                        FlurryAgent.logEvent(FlurryConst.SHEET_TOOLBOX_CLICK)
                        showMenuToolBox()
                    }
                    R.id.mm_settings -> {
                        FlurryAgent.logEvent(FlurryConst.SHEET_SETTING_CLICK)
                        val intent = PageActivity.genIntent(this@WebActivity, PageSetting::class.java)
                        startActivity(intent)
                    }
                    R.id.button_appfinish -> {
                        FlurryAgent.logEvent(FlurryConst.SHEET_POWEROFF_CLICK)
                        powerOffAndExit()
                    }
                }
            }
        }).show()
    }

    private fun powerOffAndExit() {
        FileUtils.deleteBundleInStorage()
        performExitCleanUp()
        tabsManager.clearTabs()
        finish()
    }

    private fun showMenuToolBox() {
        MenuSheetToolBox(this, showingTab().showingWebView, userPrefer, object : BaseDialog.MenuListener {
            override fun onCLick(arg0: Int, arg1: Boolean) {
                when (arg0) {
                    R.id.tb_action_find -> mPresenter.findInPage()
                    R.id.tb_action_media -> {
                        if (!userPrefer.autoMediaDetect) {
                            return shortToast(R.string.goto_media_tip)
                        }
                        showMediaDetectList()
                    }
                    R.id.tb_action_adblock_list -> {
                        val intent = PageActivity.genIntent(this@WebActivity, PageAdblock::class.java)
                        startActivity(intent)
                    }
                    R.id.tb_action_offline -> {
                        FlurryAgent.logEvent(FlurryConst.SHEET_OFFWEB_CLICK)
                        val intent = PageActivity.genIntent(this@WebActivity, PageOfflineWeb::class.java)
                        startActivity(intent)
                    }
                }
            }
        }).show()
    }

    private fun showBlockImg() {
        val select = mutableListOf<Int>().apply {
            add(when (userPrefer.blockImage) {
                UserSetting.BLOCK_ALL -> R.id.menu_block_image_all
                UserSetting.BLOCK_WAP -> R.id.menu_block_image_wap
                else -> R.id.menu_block_image_cancel
            })
        }

        SheetChoice(this, null, R.menu.menu_block_image, select, object : FloatMenu.OnItemClickListener {
            override fun onClick(v: View, item: FloatMenuItem, select: Boolean?) {
                CLog.i("menu clicked...$select")
                when (item.id) {
                    R.id.menu_block_image_all -> userPrefer.blockImage = UserSetting.BLOCK_ALL
                    R.id.menu_block_image_wap -> userPrefer.blockImage = UserSetting.BLOCK_WAP
                    R.id.menu_block_image_cancel -> userPrefer.blockImage = UserSetting.BLOCK_NONE
                }
                tabsManager.resumeAll()
            }
        }).show()
    }

    fun onPageStarted(view: WebView, url: String) {
        CLog.i("page start...")
        val urlType = UrlUtils.getType(url)
        when (urlType) {
            UrlUtils.URL_TYPE_HOME -> {
                view.setBackgroundColor(Color.TRANSPARENT)
                toolColor.initToolBarColor(ThemeUtils.getPrimaryColorTrans(this), BaseToolColor.COLOR_TRANS_LONG)
            }
            UrlUtils.URL_TYPE_WEB -> {
                view.setBackgroundColor(getAppColor(R.color.defcolorPrimaryTrns))
                toolColor.predictToolbarColor(url)

                if (userPrefer.engine == BaseSearchEngine.ENGINE_BAIDU
                        && url.startsWith(engineProvider.provideSearchEngine().queryUrl)) {
                    val baiduTip = getString(R.string.baidu_tip)
                    if (pageFinishCount++ % 20 == 0) {
                        yellowTipBar.showYellowTip(baiduTip, dismissTime = 5000L)
                    }
                }
            }
        }
    }

    private fun fullScreenTrigger(collapsed: Boolean) {
        showingTab().let {
            adaptToolBarStatet(UrlUtils.getType(it.url))
        }
    }

    private fun adaptToolBarStatet(urlType: Int) {
        when (urlType) {
            UrlUtils.URL_TYPE_HOME -> {
                showToolBar()
            }
            else -> {
                if (fullWidget.getIsCollaps()) {
                    hideToolBar()
                } else if (!actionDown) {
                    showToolBar()
                }
            }
        }
    }

    private var mediaDispose: Disposable? = null
    private fun updateVideoDetectPoint() {
        mediaPoint.dismiss()
        if (userPrefer.autoMediaDetect && mediaDetectList.isNotEmpty()) {
            mediaDispose?.dispose()
            mediaDispose = downloadsDB.filter(mediaDetectList.map { it.url }) {
                !it.moveToFirst()
            }.subscribeOn(dbScheduler)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { list ->
                        mediaDetectList.forEach {
                            if (!list.contains(it.url)) {
                                CLog.i("remove video: ${it.url}")
                                mediaDetectList.remove(it)
                            }
                        }
                        if (list.isEmpty()) return@subscribe

//                        FullAnimHelper(this@WebActivity).animSSLWarning(bitmap, searchView::rightDrawableScreenCenter, PointF(ViewUnit.dp2px(16f).toFloat(), -5f), FLOAT_TXT_TRANSIN_TIME, false) {
//                            mediaPoint.setInfo(bitmap, 18)
//                                    .show(searchView, 10f, 10f) {
//                                        showMediaDetectList()
//                                    }
//                        }

                        val bitmap = DrawableUtils.getRoundNumberImage(list.size, 20f, Color.RED, 20f)
                        mediaPoint.setInfo(bitmap, 18)
                                .show(searchView, 10f, 10f) {
                                    showMediaDetectList()
                                }
                    }
        }
    }

    private fun showMediaDetectList() {
        SheetList(this@WebActivity, R.string.media_list_title, R.string.action_delete, R.string.download_list, mediaDetectList.map {
            SelectModel(it, it.name
                    ?: FileUtil.getName(it.url), it.url, iconRes = R.drawable.ic_leida, afterIconRes = R.drawable.ic_download)
        }) { cv: View, model: SelectModel? ->
            when (cv.id) {
                R.id.txt_delete -> {
                    DialogHelper.showOkCancelDialog(this, R.string.alert_warm, R.string.alert_clear_all,
                            positiveButton = DialogItem(title = R.string.action_yes) {
                                FlurryAgent.logEvent(FlurryConst.MEDIA_LIST_DELETE_CLICK)
                                mediaDetectList.clear()
                                mediaPoint.dismiss()
                            }
                    )
                }
                R.id.txt_menu -> {
                    FlurryAgent.logEvent(FlurryConst.MEDIA_LIST_TODOWN_CLICK)
                    val intent = BasePage.genTitleIntent(this@WebActivity, PageDownload::class.java, R.string.action_downloads)
                    startActivity(intent)
                }
                R.id.item_after_img -> {
                    FlurryAgent.logEvent(FlurryConst.MEDIA_LIST_STARTDOWN_CLICK)
                    model?.let {
                        downloadhandler.start(this, it.obj as VideoInfo, it.title)
                    }
                }
                else -> {
                    FlurryAgent.logEvent(FlurryConst.MEDIA_LIST_ITEM_CLICK)
                    model?.let {
                        val intent = GSYVideoActivity.genIntent(this@WebActivity, it.title, it.urlTxt)
                        startActivity(intent)
                    }
                }
            }
        }.show()
    }

    fun showingTab() = tabsManager.showingTab

    fun getWebScollY() = showingTab().showingWebView.scaleY
    fun isTabFront(tab: WebViewController): Boolean = tab == showingTab()

    fun actionToolBarColor(url: String) {
        if (UrlUtils.isGenUrl(url)) {
            toolColor.initToolBarColor(ThemeUtils.getPrimaryColorTrans(this))
        } else {
            showingTab().captureImage?.let {
                toolColor.guessToolbarColor(url, it)
            }
        }
    }

    fun onPageFinished(webView: WebView, url: String, adHosts: List<String>) {
        if (BuildConfig.DEBUG) {
            debugWindow.showDebug(searchView)
        }
        if (!webView.hasFocus()) webView.requestFocus()
        invalidateOptionsMenu()
        EventBus.getDefault().post(SEvent(SEvent.TYPE_NAVI_AND_ALBUM_CHANGE).apply { obj = tabsManager.showingTab })
        val urlType = UrlUtils.getType(url)
        if (webView.scrollY < 100) {
            adaptToolBarStatet(urlType)
        }
        when (urlType) {
            UrlUtils.URL_TYPE_HOME -> {
//                getMarkAndLoadJs {
//                    webView.loadJavascript(JSAppendMarks(it).getJs())
//                }
//                webView.loadJavascript(JSOpenSug(url).getJs())
                toolColor.initToolBarColor(ThemeUtils.getPrimaryColorTrans(this))
            }
            UrlUtils.URL_TYPE_WEB -> {
                webView.loadJavascript(JSAdUIClear(adHosts).getJs())
                tabsManager.showingTab.let {
                    it.captureImage?.apply {
                        toolColor.guessToolbarColor(url, this)
                    }
                }
                if (userPrefer.engine == BaseSearchEngine.ENGINE_BAIDU) {
                    dangerBlocker.detectDanger(url) {
                        val baiduTip = getString(R.string.baidu_tip)
                        val dangerTip = getString(R.string.danger_tip, it, baiduTip)
                        yellowTipBar.showYellowTip(dangerTip, dismissTime = 5000L)
                    }
                }
            }
        }
    }

    fun onReceivedSslError() {
        if (userPrefer.sslErrorShake) {
            val bitmap = ThemeUtils.getBitmapFromVectorDrawable(this, R.drawable.ic_ssl_error)
            FullAnimHelper(this@WebActivity).animSSLWarning(bitmap, searchView::leftDrawableScreenCenter, PointF(0f, 0f), FLOAT_TXT_TRANSIN_TIME)
        }
    }

    fun getMarkAndLoadJs(loadJs: (List<Bookmark>?) -> Unit) {
        if (null != bookmarkModel.marks) {
            loadJs(bookmarkModel.marks)
        } else {
            val dis = Single.just(bookmarkModel.updateMarks())
                    .subscribeOn(dbScheduler)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        loadJs(it)
                    }) {}
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: SEvent) {
        CLog.i("onEvent = ${event.type}, intArg = ${event.intArg}")
        if (SEvent.comEventPros(this, event)) return
        CLog.i("sub transe: ${event.type}")
        when (event.type) {
            SettingAdapter.INDEX_SCREEN_ROTATE -> {
                invalidateFullscreenUI()
            }
            SEvent.TYPE_NAVI_AND_ALBUM_CHANGE -> {
                updateNaviBntState()
                if (bottomDrawer.status == BottomDrawer.STATE_DRAWER_OPEN) {
                    CLog.i("update bottom view item: ${event.obj}")
                    if (event.obj is WebViewController) {
                        val index = tabsManager.indexOf(event.obj as WebViewController)
                        albumAdapter.notifyItemChanged(index)
                    } else {
                        albumAdapter.notifyDataSetChanged()
                        val manager = tab_holder.layoutManager as LinearLayoutManager
                        val fitem = manager.findFirstVisibleItemPosition()
                        val litem = manager.findLastVisibleItemPosition()
                        val index = tabsManager.indexOf(tabsManager.showingTab)
                        CLog.i("fitem: $fitem, litem: $litem, index: $index")
                        if (index < fitem || index > litem) {
                            tab_holder.smoothScrollToPosition(tabsManager.indexOf(tabsManager.showingTab))
                        }
                    }
                }
            }
            SEvent.TYPE_ITEM_SHORTCLICK -> {
                bottomDrawer.closeDrawer()
                if (event.obj is WebViewController) {
                    tabsManager.switchToTab(event.obj as WebViewController)
                }
            }
            SEvent.TYPE_ITEM_LONGCLICK -> {
                if (event.obj is WebViewController) {
                    tabsManager.switchToTab(event.obj as WebViewController)
                }
            }
            SEvent.TYPE_ITEM_PAGEREMOVE -> if (event.obj is WebViewController) {
                tabsManager.deleteTab(event.obj as WebViewController)
                if (tabsManager.size() <= 0) powerOffAndExit()
            }

            SEvent.TYPE_HOMEPAGE_INIT_AND_RELOAD -> {
                mHomeInitializer.backInitialize()
                toolbarAdapter = if (userPrefer.fixToolbar) {
                    FixToolbarAdapter(this, userPrefer, toolbar_layout, status_keep)
                } else {
                    ToolbarAdapter(this, userPrefer, toolbar_layout, status_keep).apply {
                        listener = {
                            if (progress_view.translationY != it) {
                                progress_view.translationY = it
                            }
                            yellowTipBar.updateMarginTop(it)
                        }
                    }
                }
                //重制web padding
                invalidateWebPaddingBottom()
            }
            SEvent.TYPE_HOMEPAGE_RELOAD -> {
                engineUISync()
                //tabsManager.homeReinitialized(homePageFactory)
                for (tab in tabsManager.tabList) {
                    if (UrlUtils.isHomePage(tab.url)) {
                        tab.reinitialize(mHomeInitializer)
                        //tabsManager.homeReinitialized(homePageFactory)
//                        tabsManager.showingTab.reinitialize(mHomeInitializer)
                    }
                }
            }
            SEvent.TYPE_USERSETTING_CHANGE -> initUserSettings()
            SEvent.TYPE_CLEAR_HISTORY -> SimpleDialog.clearHistoryDialog(this, historyModel, dbScheduler)
            SEvent.TYPE_OPEN_URL -> {
                event.stringArg?.let {
                    loadUrl(it)
                }
            }
            SEvent.TYPE_LOSTED_TAB_NOTIFY -> {
                val restoreLostTabs = fun() {
                    tabsManager.restoreLostTabs(this@WebActivity, event.obj!! as MutableList<Bundle>)
                            .subscribeBy(
                                    onSuccess = {
                                        CLog.i("restore it = $it")
                                        tabsManager.switchToTab(it)
                                    }
                            )
                }
                if (userPrefer.restoreLostTabs) {
                    CLog.i("restore lost page by user setting...")
                    restoreLostTabs()
                } else {
                    yellowTipBar.showYellowTip(getString(R.string.web_restore_tip), getString(R.string.web_restore), 10000L) {
                        CLog.i("restore lost page by click...")
                        restoreLostTabs()
                    }
                }
            }
            SEvent.TYPE_BOOK_MARK_CHANGED -> {
                freshSearchIcon(false)
                if (event.intArg == Bookmark.TYPE_MARK) {
                    EventBus.getDefault().post(SEvent(SEvent.TYPE_HOMEPAGE_INIT_AND_RELOAD))
                }
            }
            SEvent.TYPE_RECAPTRUE_TOOLBAR_COLOR -> toolColor.tabSwitch(tabsManager.showingTab)
            SEvent.TYPE_BACKOPEN_GETURL_TXT -> {
                val data = event.stringArg
                if (!data.isNullOrEmpty()) {
                    val bitmap = DrawableUtils.txt2Bitmap(data, Color.BLACK)
                    FullAnimHelper(this).animBackUrlOpen(bitmap, PointF(touchPoint.x.toFloat(), touchPoint.y.toFloat()), button_page, FLOAT_TXT_TRANSIN_TIME)
                }
            }
            SEvent.TYPE_MEDIA_DETECTED -> {
                updateVideoDetectPoint()
            }
        }
    }

    private inner class DrawerLocker : DrawerLayout.DrawerListener {

        override fun onDrawerClosed(v: View) {
            CLog.i("onDrawerClosed...")
            drawer_layout.tag = 0
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            EventBus.getDefault().post(DrawerEvent(DrawerEvent.DRAWER_CLOSED, 0))
        }

        override fun onDrawerOpened(v: View) {
            CLog.i("onDrawerOpened...")
            EventBus.getDefault().post(DrawerEvent(DrawerEvent.DRAWER_OPENED, 0))
        }

        override fun onDrawerSlide(v: View, arg: Float) = Unit
        override fun onDrawerStateChanged(newState: Int) = Unit
    }

    fun onDrawerOpenedLock() {
        CLog.i("onDrawerOpenedLock...")
        drawer_layout.tag = 1
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN)
    }

    fun showingWeb() = showingTab().showingWebView

    inner class NetWorkStateReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            CLog.i("network changed...")
            downloadhandler.netWorkChange(this@WebActivity)
            if (NetUtils.isNetConnected(context)) {
                tabsManager.resetAllPreferences()
            }
        }
    }

}
