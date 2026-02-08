package com.fula.yohee.eventbus

import com.flurry.android.FlurryAgent
import com.fula.yohee.FlurryConst
import com.fula.yohee.ui.page.SettingAdapter
import com.fula.yohee.ui.activity.BaseActivity

class SEvent constructor(val type: Int) {

    var intArg: Int = -1
    var stringArg: String? = null
    var obj: Any? = null

    companion object {
        /**album event*/
        const val TYPE_NAVI_AND_ALBUM_CHANGE = 10
        const val TYPE_ITEM_LONGCLICK = 11  //long click album to switch a new page
        const val TYPE_ITEM_SHORTCLICK = 12  //click album to switch a new page
        const val TYPE_ITEM_PAGEREMOVE = 13 //remove the item

        /**setting event*/
        const val TYPE_HOMEPAGE_INIT_AND_RELOAD = 15 //re initialize the home page and load it after appInitialized.
        const val TYPE_USERSETTING_CHANGE = 16
        const val TYPE_CLEAR_HISTORY = 17
        const val TYPE_OPEN_URL = 18
        const val TYPE_HOMEPAGE_RELOAD = 19 //only load web page, not initialize.
        const val TYPE_LOSTED_TAB_NOTIFY = 20
        const val TYPE_BOOK_MARK_CHANGED = 21
        const val TYPE_RECAPTRUE_TOOLBAR_COLOR = 22
        const val TYPE_BACKOPEN_GETURL_TXT = 23
        const val TYPE_MEDIA_DETECTED = 24

        /**
         * processing common business logic.
         * @param context the context which will process this event.
         * @param event the event of the context has received.
         * */
        fun comEventPros(context: BaseActivity, event: SEvent): Boolean = when (event.type) {
            SettingAdapter.INDEX_SCREEN_ROTATE -> {
                FlurryAgent.logEvent(FlurryConst.SETTING_SCREEN_ROTATE_CHOICE_ + event.intArg)
                if (event.intArg != context.userPrefer.screenRotate) {
                    context.userPrefer.screenRotate = event.intArg
                }
                context.requestedOrientation = event.intArg
                true
            }
            SettingAdapter.INDEX_THEME_CHOICE -> {
                FlurryAgent.logEvent(FlurryConst.SETTING_THEME_CHOICE_ + event.intArg)
                if (event.intArg != context.userPrefer.useTheme) {
                    context.userPrefer.useTheme = event.intArg
                }
                context.syncThemeBgUI()
                true
            }
            else -> false
        }


    }

}
