package com.fula.yohee.uiwigit

import android.content.pm.ActivityInfo
import android.view.View
import com.fula.yohee.R
import com.fula.yohee.eventbus.SEvent
import com.fula.yohee.settings.UserSetting
import com.fula.yohee.ui.activity.WebActivity
import com.fula.yohee.ui.page.PageItemEdit
import com.fula.yohee.ui.page.SettingAdapter
import com.fula.yohee.utils.ShareUtils
import com.fula.yohee.utils.UrlUtils
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import org.greenrobot.eventbus.EventBus
import java.util.HashMap

/**
 * float menu logic process.
 * @author delanding
 * @date 2020-08-27
 */
class FloatMenuWidget(val webActivity: WebActivity, val view: View) {

    private val actionMenu : FloatingActionMenu by lazy { view.findViewById<FloatingActionMenu>(R.id.float_menu_up) }
    private val mAction1 : FloatingActionButton by lazy { view.findViewById<FloatingActionButton>(R.id.float_action_1) }
    private val mAction2 : FloatingActionButton by lazy { view.findViewById<FloatingActionButton>(R.id.float_action_2) }
    private val mAction3 : FloatingActionButton by lazy { view.findViewById<FloatingActionButton>(R.id.float_action_3) }

    init {
        mAction1.setOnClickListener {
            EventBus.getDefault().post(SEvent(SettingAdapter.INDEX_SCREEN_ROTATE).apply {
                intArg = if (webActivity.userPrefer.screenRotate != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            })
        }
        mAction2.setOnClickListener {
            webActivity.mPresenter.findInPage()
        }
        mAction3.setOnClickListener {
            webActivity.tabsManager.showingTab.let {
                if (UrlUtils.isLocalUrl(it.url)) {
                    val intent = PageItemEdit.genIntent(webActivity, HashMap<String, String>().apply {
                        this[webActivity.getString(R.string.share_tip)] = ""
                    }, R.string.app_share)
                    webActivity.startActivityForResult(intent, PageItemEdit.ACTIVITY_RESULT_EDITBOOKMARK)
                } else {
                    ShareUtils.shareUrl(webActivity, it.url, it.title)
                }
            }
        }
    }

    fun collapseTrigger(open: Boolean) {
        if (open) {
            actionMenu.open(true)
        } else {
            actionMenu.close(true)
        }
    }

    fun visiable(visiable: Int) {
        actionMenu.visibility = visiable
    }

    fun fullScreenVisiable(visiable: Int) {
        if (webActivity.userPrefer.floatMenu == UserSetting.DOT_SHOW_ONLY_FSCREEN) {
            actionMenu.visibility = visiable
        }
    }

}
