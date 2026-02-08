package com.fula.yohee.ui.page

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import com.flurry.android.FlurryAgent
import com.fula.CLog
import com.fula.base.BaseInfoAdapter
import com.fula.base.ListPageAdapter
import com.fula.base.ListPageGroup
import com.fula.base.iview.BasePage
import com.fula.yohee.FlurryConst
import com.fula.yohee.R
import com.fula.yohee.eventbus.SEvent
import com.fula.yohee.extensions.toInt
import com.fula.yohee.settings.SettingProvider
import com.fula.yohee.settings.UserSetting
import org.greenrobot.eventbus.EventBus
import java.util.*

/**
 * @Desc: 个人偏好设置
 * @Date: 2018-09-11
 * @author: delanding
 */
class PageSettingFaver : BasePage(), ListPageAdapter.ItemListener {

    private lateinit var adapter: WebAdapter

    override fun initPage(inflater: LayoutInflater, container: ViewGroup) {
        super.initPage(container, R.layout.page_items)
        initView()
    }

    private fun initView() {
        mContext.setSupportActionBar(findViewById(R.id.toolSetingbar) as Toolbar)
        initToolBar()

        val listPageContent = findViewById(R.id.list_page_content) as LinearLayout
        adapter = WebAdapter(mContext, listPageContent, this)
        adapter.initAdapter()
    }

    private fun initToolBar() {
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_array_left)
        actionBar.title = title
    }

    override fun onItemClick(v: View, item: ListPageGroup.ListPageItem, arg: Int) {
        CLog.i("onitem click...${item.index}, arg = $arg")
        getUserPrefer().let {
            val isTrue = arg == 1
            when (item.index) {
                INDEX_HABIT -> {
                    FlurryAgent.logEvent(FlurryConst.SETTING_FAVER_HABIT)
                    val intent = PageItemSelect.genIntent(mContext, item.keyRes, item.index, SettingProvider.provideHabit(), if (getUserPrefer().rightHandUsage) UserSetting.USER_HABIT_HAND_RIGHT else UserSetting.USER_HABIT_HAND_LEFT)
                    mContext.startActivityForResult(intent, REQUEST_TYPE_SETTING)
                }
                AUTO_RESTORE_WEBS -> {
                    FlurryAgent.logEvent(FlurryConst.SETTING_FAVER_RESTORE_LOSTWEB_ + isTrue.toInt())
                    it.restoreLostTabs = isTrue
                }
                WEB_SINK_TOOLBAR -> {
                    FlurryAgent.logEvent(FlurryConst.SETTING_FAVER_SINK_TOOLBAR_ + isTrue.toInt())
                    it.toolColorFollow = isTrue
                    EventBus.getDefault().post(SEvent(SEvent.TYPE_USERSETTING_CHANGE))
                }
                WEB_FIX_TOOLBAR -> {
                    FlurryAgent.logEvent(FlurryConst.SETTING_FAVER_FIX_TOOLBAR_ + isTrue.toInt())
                    getUserPrefer().fixToolbar = isTrue
                    EventBus.getDefault().post(SEvent(SEvent.TYPE_HOMEPAGE_INIT_AND_RELOAD))
                }
                WEB_MEDIA_DETECT -> {
                    FlurryAgent.logEvent(FlurryConst.SETTING_FAVER_DECTECT_MEDIA_ + isTrue.toInt())
                    it.autoMediaDetect = isTrue
                }
                ANIM_HOME_MARKS -> {
                    FlurryAgent.logEvent(FlurryConst.SETTING_FAVER_MARKANIM)
                    val intent = PageItemSelect.genIntent(mContext, item.keyRes, item.index, SettingProvider.provideAnimMark(), getUserPrefer().animHomeMarks)
                    mContext.startActivityForResult(intent, REQUEST_TYPE_SETTING)
                }
                FLOAT_MENU -> {
                    FlurryAgent.logEvent(FlurryConst.SETTING_FLOAT_MENU)
                    val intent = PageItemSelect.genIntent(mContext, item.keyRes, item.index, SettingProvider.provideFloatMenu(), getUserPrefer().floatMenu)
                    mContext.startActivityForResult(intent, REQUEST_TYPE_SETTING)
                }
                SSL_ERROR_SHAKE -> {
                    FlurryAgent.logEvent(FlurryConst.SETTING_FAVER_SSL_SHAKE_ + isTrue.toInt())
                    it.sslErrorShake = isTrue
                }
                DOWNLOAD_ONLY_WIFI -> {
                    FlurryAgent.logEvent(FlurryConst.SETTING_FAVER_WIFI_DOWNLOAD_ + isTrue.toInt())
                    it.wifiDownload = isTrue
                }
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
            INDEX_HABIT -> {
                FlurryAgent.logEvent(FlurryConst.SETTING_FAVER_HABIT_CHOICE_ + selectValue)
                getUserPrefer().rightHandUsage = selectValue == UserSetting.USER_HABIT_HAND_RIGHT
                EventBus.getDefault().post(SEvent(SEvent.TYPE_USERSETTING_CHANGE))
            }
            ANIM_HOME_MARKS -> {
                FlurryAgent.logEvent(FlurryConst.SETTING_FAVER_MARKANIM_CHOICE_ + selectValue)
                getUserPrefer().animHomeMarks = selectValue
                EventBus.getDefault().post(SEvent(SEvent.TYPE_HOMEPAGE_INIT_AND_RELOAD))
            }
            FLOAT_MENU -> {
                FlurryAgent.logEvent(FlurryConst.SETTING__FLOAT_MENU_CHOICE_ + selectValue)
                getUserPrefer().floatMenu = selectValue
                EventBus.getDefault().post(SEvent(SEvent.TYPE_USERSETTING_CHANGE))
            }
        }
    }

    inner class WebAdapter(mContext: Activity, pView: LinearLayout, listener: ListPageAdapter.ItemListener)
        : BaseInfoAdapter(mContext, pView, listener, null) {

        private val checkItems = mutableListOf(AUTO_RESTORE_WEBS, WEB_SINK_TOOLBAR, WEB_FIX_TOOLBAR, WEB_MEDIA_DETECT, SSL_ERROR_SHAKE, DOWNLOAD_ONLY_WIFI)

        override fun getItemLayout(index: Int): Int {
            return when (index) {
                in checkItems -> R.layout.item_listpage_check
                else -> super.getItemLayout(index)
            }
        }

        override fun listGroup(vl: View.OnClickListener?): List<ListPageGroup> {
            return ArrayList<ListPageGroup>().apply {
                add(ListPageGroup(null, ArrayList<ListPageGroup.ListPageItem>().apply {
                    add(ListPageGroup.ListPageItem(INDEX_HABIT, R.string.setting_hoby))
                    add(ListPageGroup.ListPageItem(ANIM_HOME_MARKS, R.string.anim_home_marks))
                    add(ListPageGroup.ListPageItem(FLOAT_MENU, R.string.float_menu))
                    add(ListPageGroup.ListPageItem(AUTO_RESTORE_WEBS, R.string.auto_restore_webs))
                    add(ListPageGroup.ListPageItem(WEB_SINK_TOOLBAR, R.string.web_color_follow))
                    add(ListPageGroup.ListPageItem(WEB_FIX_TOOLBAR, R.string.web_fix_toolbar))
                    add(ListPageGroup.ListPageItem(WEB_MEDIA_DETECT, R.string.auto_pop_video_detect))
                    add(ListPageGroup.ListPageItem(SSL_ERROR_SHAKE, R.string.ssl_error_shake))
                    add(ListPageGroup.ListPageItem(DOWNLOAD_ONLY_WIFI, R.string.download_only_wifi))
                }))
            }
        }

        override fun visitItem(item: ListPageGroup.ListPageItem, itemView: View): Boolean {
            return when (item.index) {
                in checkItems -> {
                    itemView.findViewById<CheckBox>(R.id.item_check)?.run {
                        when (item.index) {
                            AUTO_RESTORE_WEBS -> this.isChecked = getUserPrefer().restoreLostTabs
                            WEB_SINK_TOOLBAR -> this.isChecked = getUserPrefer().toolColorFollow
                            WEB_FIX_TOOLBAR -> this.isChecked = getUserPrefer().fixToolbar
                            WEB_MEDIA_DETECT -> this.isChecked = getUserPrefer().autoMediaDetect
                            SSL_ERROR_SHAKE -> this.isChecked = getUserPrefer().sslErrorShake
                        }
                        setOnCheckedChangeListener { _, isChecked ->
                            listener.onItemClick(itemView, item, if (isChecked) 1 else 0)
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }

    companion object {
        private const val REQUEST_TYPE_SETTING = 100

        private const val INDEX_HABIT = 0
        private const val AUTO_RESTORE_WEBS = 1
        private const val WEB_SINK_TOOLBAR = 2
        private const val WEB_FIX_TOOLBAR = 3
        private const val WEB_MEDIA_DETECT = 4
        private const val SSL_ERROR_SHAKE = 5
        private const val ANIM_HOME_MARKS = 6
        private const val FLOAT_MENU = 7
        private const val DOWNLOAD_ONLY_WIFI = 8
    }

}

