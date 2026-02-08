package com.fula.yohee.ui.page

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.flurry.android.FlurryAgent
import com.fula.CLog
import com.fula.base.ListPageAdapter
import com.fula.base.ListPageGroup
import com.fula.base.iview.BasePage
import com.fula.util.GoToScoreUtils
import com.fula.util.YoUtils
import com.fula.yohee.Config
import com.fula.yohee.FlurryConst
import com.fula.yohee.R
import com.fula.yohee.YoheeApp
import com.fula.yohee.di.DatabaseScheduler
import com.fula.yohee.dialog.DialogMemuContent
import com.fula.yohee.dialog.MenuAction
import com.fula.yohee.eventbus.SEvent
import com.fula.yohee.extensions.*
import com.fula.yohee.search.SearchEngineProvider
import com.fula.yohee.settings.SettingProvider
import com.fula.yohee.settings.UserSetting
import com.fula.yohee.utils.DiskSave
import com.fula.yohee.utils.FileUtils
import com.fula.yohee.utils.IntentUtils
import io.reactivex.Scheduler
import kotlinx.android.synthetic.main.page_items.view.*
import kotlinx.android.synthetic.main.toolbar.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject


/**
 * @Desc: user infomation setting page
 * @Date: 2018-09-11
 * @author: delanding
 */
class PageSetting : BasePage(), ListPageAdapter.ItemListener {

    private lateinit var adapter: SettingAdapter
    @Inject
    lateinit var searchEngineProvider: SearchEngineProvider
    @Inject
    @field:DatabaseScheduler
    internal lateinit var dbScheduler: Scheduler
    private val diskSave: DiskSave by lazy { DiskSave(mContext) }
    private val handler = Handler()

    override fun initPage(inflater: LayoutInflater, container: ViewGroup) {
        super.initPage(container, R.layout.page_items)
        registerEventBus()
        YoheeApp.injector.inject(this)
        initView()
        autoSaveSetting()
    }

    private fun initView() {
        mContext.setSupportActionBar(mView.toolSetingbar)
        initToolBar()

        adapter = SettingAdapter(mContext, mView.list_page_content, this)
        adapter.initAdapter()
    }

    private fun initToolBar() {
//        actionBar.setNavigationIcon(R.mipmap.ic_launcher)
        //toolSetingbar.setLogo(R.mipmap.ic_launcher)
        //toolSetingbar.title = "HelloToolbar"
        //toolSetingbar.subtitle = "SubTitle"
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(com.fula.yohee.R.drawable.ic_array_left)
        actionBar.title = mContext.getString(com.fula.yohee.R.string.settings)
    }

    override fun onItemClick(v: View, item: ListPageGroup.ListPageItem, arg: Int) {
        CLog.i("onitem click...${item.index}, arg = $arg")
        when (item.index) {
            SettingAdapter.INDEX_ENGINE -> {
                FlurryAgent.logEvent(FlurryConst.SETTING_ENGINE_CLICK)
                val engineList = searchEngineProvider.provideEngines()
                val list = engineList.map {
                    SettingItem(it.menuIcon, it.titleRes).apply { value = it.intArg }
                }.toTypedArray()
                val intent = PageItemSelect.genIntent(mContext, item.keyRes, item.index, list, getUserPrefer().engine)
                mContext.startActivityForResult(intent, REQUEST_TYPE_SETTING)
            }
            SettingAdapter.INDEX_FONT -> {
                FlurryAgent.logEvent(FlurryConst.SETTING_TXT_SIZE_CLICK)
                val intent = PageItemSelect.genIntent(mContext, item.keyRes, item.index, SettingProvider.provideFontChoice(), getUserPrefer().textSize)
                mContext.startActivityForResult(intent, REQUEST_TYPE_SETTING)
            }
            SettingAdapter.INDEX_SCREEN_ROTATE -> {
                FlurryAgent.logEvent(FlurryConst.SETTING_SCREEN_ROTATE_CLICK)
                val intent = PageItemSelect.genIntent(mContext, item.keyRes, item.index, SettingProvider.provideRotate(), getUserPrefer().screenRotate, true)
                mContext.startActivityForResult(intent, REQUEST_TYPE_SETTING)
            }
            SettingAdapter.INDEX_THEME_CHOICE -> {
                FlurryAgent.logEvent(FlurryConst.SETTING_THEME_CLICK)
                val intent = PageItemSelect.genIntent(mContext, item.keyRes, item.index, SettingProvider.provideThemeChoice(), getUserPrefer().useTheme, true)
                mContext.startActivityForResult(intent, REQUEST_TYPE_SETTING)
            }
            SettingAdapter.INDEX_FAVER -> {
                FlurryAgent.logEvent(FlurryConst.SETTING_FAVER_CLICK)
                val intent = genTitleIntent(mContext, PageSettingFaver::class.java, item.keyRes)
                mContext.startActivity(intent)
            }
            SettingAdapter.INDEX_SECURATY -> {
                FlurryAgent.logEvent(FlurryConst.SETTING_SECRATE_CLICK)
                val intent = genTitleIntent(mContext, PageSettingSecure::class.java, item.keyRes)
                mContext.startActivity(intent)
            }
            SettingAdapter.INDEX_DYNC_BG -> {
                val intent = PageItemSelect.genIntent(mContext, item.keyRes, item.index, SettingProvider.provideDynamicBG(), getUserPrefer().dynamicBg)
                mContext.startActivityForResult(intent, REQUEST_TYPE_SETTING)
            }
            SettingAdapter.INDEX_AD_BLOCK -> {
                FlurryAgent.logEvent(FlurryConst.SETTING_AD_BLOCK_CLICK)
                val intent = PageItemSelect.genIntent(mContext, item.keyRes, item.index, SettingProvider.provideOpenClose(), getUserPrefer().blockAd.toInt(), descId = com.fula.yohee.R.string.ad_block_desc)
                mContext.startActivityForResult(intent, REQUEST_TYPE_SETTING)
            }
            SettingAdapter.INDEX_WEB_SETTING -> {
                FlurryAgent.logEvent(FlurryConst.SETTING_WEB_CLICK)
                val intent = genTitleIntent(mContext, PageWebSetting::class.java, item.keyRes)
                mContext.startActivity(intent)
            }
            SettingAdapter.INDEX_APP_DATA_KEEP -> {
                FlurryAgent.logEvent(FlurryConst.SETTING_DATA_SAVE)
                val intent = genTitleIntent(mContext, PageDataSave::class.java, item.keyRes)
                mContext.startActivity(intent)
            }
            SettingAdapter.INDEX_DEFBROWSE_SET -> {
                FlurryAgent.logEvent(FlurryConst.SETTING_DEFBROWSE_SET_CLICK)
                val intent = genTitleIntent(mContext, PageSettingDefault::class.java, item.keyRes)
                intent.putExtra(KEY_ARGS, searchEngineProvider.provideSearchEngine().queryUrl)
                mContext.startActivity(intent)
            }
            SettingAdapter.INDEX_SCORE -> {
                FlurryAgent.logEvent(FlurryConst.SETTING_SCORE_CLICK)
                GoToScoreUtils.goToMarket(mContext, YoUtils.getPackageName(mContext))
            }
            SettingAdapter.INDEX_HELP -> {
                FlurryAgent.logEvent(FlurryConst.SETTING_HELP_CLICK)
                val helps = SettingProvider.provideHelp()
                val intent = PageItemSelect.genIntent(mContext, item.keyRes, item.index, helps, pageType = PageItemSelect.PAGE_TYPE_SELECT_FINISH)
                mContext.startActivityForResult(intent, REQUEST_TYPE_SETTING)
            }
            SettingAdapter.INDEX_ABOUT -> {
                FlurryAgent.logEvent(FlurryConst.SETTING_ABOUT_CLICK)
                val intent = genTitleIntent(mContext, PageAboutus::class.java, item.keyRes)
                mContext.startActivity(intent)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        CLog.i("requestCode = $requestCode resultCode = $resultCode")
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        val settingIndex = data!!.getIntExtra(PageItemSelect.KEY_SETTING_INDEX, UserSetting.NO_VALUE)
        val selectIndex = data.getIntExtra(PageItemSelect.KEY_SELECT_INDEX, UserSetting.NO_VALUE)
        val selectValue = data.getIntExtra(PageItemSelect.KEY_SELECT_VALUE, UserSetting.NO_VALUE)
        if (-1 == selectIndex || -1 == settingIndex) {
            CLog.i("error, new select = $selectIndex, setting intArg = $settingIndex")
        }
        CLog.i("set intArg = $settingIndex, newSelect = $selectIndex")
        when (settingIndex) {
            SettingAdapter.INDEX_ENGINE -> {
                FlurryAgent.logEvent(FlurryConst.SETTING_ENGINE_CHOICE_ + selectValue)
                getUserPrefer().engine = selectValue
                EventBus.getDefault().post(SEvent(SEvent.TYPE_HOMEPAGE_INIT_AND_RELOAD))
            }
            SettingAdapter.INDEX_FONT -> {
                FlurryAgent.logEvent(FlurryConst.SETTING_TXT_SIZE_CHOICE_ + selectValue)
                getUserPrefer().textSize = selectValue
            }
            SettingAdapter.INDEX_DYNC_BG -> {
                EventBus.getDefault().post(SEvent(SEvent.TYPE_USERSETTING_CHANGE).apply {
                    getUserPrefer().dynamicBg = selectValue
                    intArg = selectValue
                })
            }
            SettingAdapter.INDEX_AD_BLOCK -> {
                FlurryAgent.logEvent(FlurryConst.SETTING_AD_BLOCK_CHOICE_ + selectValue)
                getUserPrefer().blockAd = selectValue.toBoolean()
            }
            SettingAdapter.INDEX_HELP -> {
                FlurryAgent.logEvent(FlurryConst.SETTING_HELP_CHOICE_ + selectValue)
                gotoHelp(selectIndex)
            }
        }
    }

    private fun gotoHelp(index: Int) {
        when (index) {
            SettingProvider.HELP_WEIBO -> {
                mContext.tryShare("https://weibo.com/u/${Config.SINA_APP_ID}") {
                    mContext.startActivity(Intent().apply {
                        action = Intent.ACTION_VIEW
                        addCategory(Intent.CATEGORY_DEFAULT)
                        data = Uri.parse("sinaweibo://userinfo?uid=${Config.SINA_APP_ID}")
                    })
                }
            }
            SettingProvider.HELP_TWITTER -> {
                mContext.tryShare(Config.MY_TWITTER) {
                    mContext.startActivity(Intent().apply {
                        action = Intent.ACTION_VIEW
                        addCategory(Intent.CATEGORY_DEFAULT)
                        data = Uri.parse("twitter://user?user_id=${Config.TWITTER_ID}")
                    })
                }
            }
            SettingProvider.HELP_EMAIL -> {
                /*val mailTo = "mailto:${Config.MY_EMAIL}?cc=${Config.MY_EMAIL}&subject=${mContext.getString(R.string.setting_help)}&body=${mContext.getString(R.string.email_body)}"
                UrlUtils.mailTo(mContext, mailTo)*/
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("mailto:${Config.MY_EMAIL}")
                mContext.startActivity(intent)
            }
            SettingProvider.HELP_QQ -> {
                val image = ImageView(mContext).apply {
                    setImageResource(com.fula.yohee.R.mipmap.qq_share)
                }
                DialogMemuContent(mContext, com.fula.yohee.R.string.setting_help, MenuAction(com.fula.yohee.R.string.help_qq_copy, false), MenuAction(com.fula.yohee.R.string.help_qq_jump, true), image) {
                    if (it == DialogMemuContent.MENU_LEFT) {
                        val clipManager = mContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipManager.copy(Config.QQ_GROUP_UIN)
                        mContext.shortToast(mContext.getString(R.string.copyed_to_clip, Config.QQ_GROUP_UIN))
                    } else if (it == DialogMemuContent.MENU_RIGHT) {
                        tryCatch {
                            val url = "mqqwpa://im/chat?chat_type=wpa&uin=2174625097"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            if (IntentUtils.isValidIntent(mContext, intent)) {
                                mContext.startActivity(intent)
                            } else {
                                mContext.shortToast(R.string.open_app_fail)
                            }
                        }
                    }
                }.show()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: SEvent) {
        SEvent.comEventPros(mContext, event)
    }

    /**自动保存个人设置*/
    private fun autoSaveSetting() {
        val jar = FileUtils.createYohheFile("", FileUtils.SAVE_SETTINGS)
        if (jar.exists()) {
            if (jar.lastModified() - System.currentTimeMillis() > OVERTIM_15_DAYS) {
                saveSetting()
            }
        } else {
            saveSetting()
        }
    }

    private fun saveSetting() {
        diskSave.exportBookmark()
        diskSave.exportSettins(getUserPrefer())
        handler.postDelayed({
            diskSave.zipAndShareSave(mContext, false)
        }, 1000)
    }

    companion object {
        private const val REQUEST_TYPE_SETTING = 101
        private const val OVERTIM_15_DAYS = 15 * 24 * 60 * 60 * 1000L
    }

}

