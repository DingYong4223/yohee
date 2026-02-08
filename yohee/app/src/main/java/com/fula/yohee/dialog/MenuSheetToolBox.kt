package com.fula.yohee.dialog

import android.view.View
import com.fula.yohee.R
import com.fula.yohee.preference.UserPreferences
import com.fula.yohee.ui.activity.WebActivity
import com.fula.yohee.utils.UrlUtils
import com.fula.yohee.view.NSWebView
import kotlinx.android.synthetic.main.dlg_main_menu.*

class MenuSheetToolBox(context: WebActivity, val webView: NSWebView?, val userPre: UserPreferences,
                       listener: MenuListener) : BaseSheetMenu(context, listener) {

    override fun initMenuResId(): Int {
        return R.menu.menu_sheet_toolbox
    }

    override fun initUIState() {
        button_appfinish.visibility = View.GONE
        if (UrlUtils.isGenUrl(webView?.url)) {
            disables.add(R.id.tb_action_find)
        }
    }

}
