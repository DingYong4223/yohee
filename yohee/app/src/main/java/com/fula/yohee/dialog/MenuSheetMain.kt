package com.fula.yohee.dialog

import android.graphics.drawable.Drawable
import android.webkit.WebView
import android.widget.RelativeLayout
import com.fula.yohee.R
import com.fula.yohee.preference.UserPreferences
import com.fula.yohee.settings.UserSetting
import com.fula.yohee.ui.activity.WebActivity
import com.fula.yohee.utils.UrlUtils
import kotlinx.android.synthetic.main.dlg_main_menu.*

class MenuSheetMain(context: WebActivity
                    , resMap: Map<Int, Drawable>
                    , private val webview: WebView
                    , private val userPrefer: UserPreferences
                    , listener: MenuListener)
    : BaseSheetMenu(context, listener, resMap) {

    override fun initMenuResId(): Int {
        return R.menu.menu_sheet_main
    }

    override fun initUIState() {
        if (userPrefer.rightHandUsage) {
            val params = button_appfinish.layoutParams as RelativeLayout.LayoutParams
            params.apply {
                removeRule(RelativeLayout.ALIGN_PARENT_START)
                addRule(RelativeLayout.ALIGN_PARENT_END)
                button_appfinish.layoutParams = this
            }
        }
        if (UrlUtils.isGenUrl(webview.url)) {
            disables.add(R.id.mm_fresh)
            disables.add(R.id.mm_share)
            disables.add(R.id.mm_addbookmark)
        }

        if (userPrefer.nightMode) hightLight.add(R.id.mm_night)
        if (UserSetting.BLOCK_NONE != userPrefer.blockImage) hightLight.add(R.id.mm_imageblock)
        if (userPrefer.fullScreen) hightLight.add(R.id.mm_full_screen)

        setOnDismissListener {
            listener.onCLick(-1, false)
        }
    }

}
