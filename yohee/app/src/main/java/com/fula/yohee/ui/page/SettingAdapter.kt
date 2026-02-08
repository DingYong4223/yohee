package com.fula.yohee.ui.page

import android.app.Activity
import android.view.View
import android.widget.LinearLayout
import com.fula.base.BaseInfoAdapter
import com.fula.base.ListPageAdapter
import com.fula.base.ListPageGroup
import com.fula.yohee.R
import java.util.*

/**
 * Setting list page adapter.
 *
 * @author delanding
 */

class SettingAdapter : BaseInfoAdapter {

    constructor(mContext: Activity, pView: LinearLayout, listener: ListPageAdapter.ItemListener) : super(mContext, pView, listener, null)

    override fun listGroup(vl: View.OnClickListener?): List<ListPageGroup> {
        return ArrayList<ListPageGroup>().apply {
            //系统设置
            add(ListPageGroup(null, ArrayList<ListPageGroup.ListPageItem>().apply {
                add(ListPageGroup.ListPageItem(INDEX_ENGINE, R.string.title_search_engine))
                add(ListPageGroup.ListPageItem(INDEX_THEME_CHOICE, R.string.theme))
                add(ListPageGroup.ListPageItem(INDEX_SCREEN_ROTATE, R.string.setting_screen_rotate))
                add(ListPageGroup.ListPageItem(INDEX_FONT, R.string.title_text_size))
            }))
            //浏览器设置
            add(ListPageGroup(null, ArrayList<ListPageGroup.ListPageItem>().apply {
                add(ListPageGroup.ListPageItem(INDEX_FAVER, R.string.settings_general))
                add(ListPageGroup.ListPageItem(INDEX_SECURATY, R.string.settings_privacy))
                add(ListPageGroup.ListPageItem(INDEX_WEB_SETTING, R.string.setting_browser))
                add(ListPageGroup.ListPageItem(INDEX_AD_BLOCK, R.string.block_ads))
            }))
            //个人设置
            add(ListPageGroup(null, ArrayList<ListPageGroup.ListPageItem>().apply {
                //add(ListPageGroup.ListPageItem(INDEX_HABIT, R.string.setting_hoby))
                //add(ListPageGroup.ListPageItem(INDEX_DYNC_BG, R.string.dynamic_bg))
                add(ListPageGroup.ListPageItem(INDEX_APP_DATA_KEEP, R.string.app_data_keep))
            }))
            //帮助设置
            add(ListPageGroup(null, ArrayList<ListPageGroup.ListPageItem>().apply {
                add(ListPageGroup.ListPageItem(INDEX_DEFBROWSE_SET, R.string.setting_detfault))
                add(ListPageGroup.ListPageItem(INDEX_HELP, R.string.setting_help))
                add(ListPageGroup.ListPageItem(INDEX_SCORE, R.string.setting_score))
                add(ListPageGroup.ListPageItem(INDEX_ABOUT, R.string.settings_about))
            }))
        }
    }

    override fun visitItem(item: ListPageGroup.ListPageItem, itemView: View): Boolean {
        return false
    }

    companion object {
        const val INDEX_ENGINE = 0
        const val INDEX_FONT = 1
        const val INDEX_SCREEN_ROTATE = 2
        const val INDEX_THEME_CHOICE = 3
        const val INDEX_FAVER = 4
        const val INDEX_SECURATY = 6
        const val INDEX_DYNC_BG = 8
        const val INDEX_AD_BLOCK = 9
        const val INDEX_WEB_SETTING = 11
        const val INDEX_APP_DATA_KEEP = 12
        const val INDEX_DEFBROWSE_SET = 13
        const val INDEX_HELP = 14
        const val INDEX_SCORE = 15
        const val INDEX_ABOUT = 16
    }


}
