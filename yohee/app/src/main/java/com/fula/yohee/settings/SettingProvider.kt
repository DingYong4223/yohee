package com.fula.yohee.settings

import android.content.pm.ActivityInfo
import com.fula.yohee.R
import com.fula.yohee.constant.SCHEME_BOOKMARKS
import com.fula.yohee.constant.SCHEME_HOMEPAGE
import com.fula.yohee.constant.SCHEME_URL
import com.fula.yohee.ui.page.SettingItem
import com.fula.yohee.settings.UserSetting.Companion.ANIM_MARK_TRANS
import com.fula.yohee.settings.UserSetting.Companion.THEME_AUTOCHANGE
import com.fula.yohee.settings.UserSetting.Companion.THEME_AUTUMUN
import com.fula.yohee.settings.UserSetting.Companion.THEME_SPRING
import com.fula.yohee.settings.UserSetting.Companion.THEME_SUMMER
import com.fula.yohee.settings.UserSetting.Companion.THEME_WINTER

class SettingProvider {

    companion object {

        @JvmStatic
        fun provideFloatMenu(): Array<SettingItem> = listOf(
                SettingItem(SettingItem.VALUE_GONE, R.string.always).apply { value = UserSetting.DOT_SHOW_ALWAYS },
                SettingItem(SettingItem.VALUE_GONE, R.string.only_full_screen).apply { value = UserSetting.DOT_SHOW_ONLY_FSCREEN }
        ).toTypedArray()

        @JvmStatic
        fun provideAnimMark(): Array<SettingItem> = listOf(
                SettingItem(R.drawable.ic_apha_change, R.string.anim_mark_fade).apply { value = UserSetting.ANIM_MARK_FADE },
                SettingItem(R.drawable.ic_move, R.string.anim_mark_trans).apply { value = ANIM_MARK_TRANS },
                SettingItem(R.drawable.ic_no_choice, R.string.setting_no).apply { value = UserSetting.NO_VALUE }
        ).toTypedArray()

        @JvmStatic
        fun provideThemeChoice(): Array<SettingItem> = listOf(
                SettingItem(R.mipmap.icon_spring, R.string.setting_theme_spring).apply { value = THEME_SPRING },
                SettingItem(R.mipmap.icon_summer, R.string.setting_theme_summer).apply { value = THEME_SUMMER },
                SettingItem(R.mipmap.icon_autumn, R.string.setting_theme_autunm).apply { value = THEME_AUTUMUN },
                SettingItem(R.mipmap.icon_winter, R.string.setting_theme_winter).apply { value = THEME_WINTER },
                SettingItem(R.mipmap.icon_theme_defailt, R.string.setting_theme_switch).apply { value = THEME_AUTOCHANGE },
                SettingItem(R.drawable.ic_no_choice, R.string.setting_no).apply { value = UserSetting.NO_VALUE }
        ).toTypedArray()

        @JvmStatic
        fun provideFontChoice(): Array<SettingItem> = listOf(
                SettingItem(R.drawable.ic_txt, R.string.setting_txt_0).apply { icon = 7; value = 0 },
                SettingItem(R.drawable.ic_txt, R.string.setting_txt_1).apply { icon = 9; value = 1 },
                SettingItem(R.drawable.ic_txt, R.string.setting_txt_2).apply { icon = 12; value = 2 },
                SettingItem(R.drawable.ic_txt, R.string.setting_txt_3).apply { icon = 15; value = 3 },
                SettingItem(R.drawable.ic_txt, R.string.setting_txt_4).apply { icon = 18; value = 4 }
        ).toTypedArray()

        @JvmStatic
        fun provideRotate(): Array<SettingItem> = listOf(
                SettingItem(SettingItem.VALUE_GONE, R.string.setting_rotate_0).apply { value = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT },
                SettingItem(SettingItem.VALUE_GONE, R.string.setting_rotate_1).apply { value = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE },
                SettingItem(SettingItem.VALUE_GONE, R.string.setting_rotate_2).apply { value = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
        ).toTypedArray()

        @JvmStatic
        fun provideHelp(): Array<SettingItem> = listOf(
                SettingItem(R.drawable.ic_weibo, R.string.help_sina).apply { value = HELP_WEIBO },
                SettingItem(R.drawable.ic_twitter, R.string.help_twitter).apply { value = HELP_TWITTER },
                SettingItem(R.drawable.ic_email, R.string.help_email).apply { value = HELP_EMAIL },
                SettingItem(R.drawable.ic_qq, R.string.help_qq).apply { value = HELP_QQ }
        ).toTypedArray()

        @JvmStatic
        fun provideHabit(): Array<SettingItem> = listOf(
                SettingItem(R.drawable.ic_hand_left, R.string.setting_hand_left).apply { value = UserSetting.USER_HABIT_HAND_LEFT },
                SettingItem(R.drawable.ic_hand_right, R.string.setting_hand_right).apply { value = UserSetting.USER_HABIT_HAND_RIGHT }
        ).toTypedArray()

        @JvmStatic
        fun provideDynamicBG(): Array<SettingItem> = listOf(
                SettingItem(R.drawable.ic_back_white, R.string.dynamic_bg).apply { value = R.mipmap.icon_summer },
                SettingItem(R.drawable.ic_hand_right, R.string.setting_no).apply { value = UserSetting.NO_VALUE }
        ).toTypedArray()

        @JvmStatic
        fun provideOpenClose(): Array<SettingItem> = listOf(
                SettingItem(R.drawable.ic_eye_open, R.string.action_open).apply { value = 1 },
                SettingItem(R.drawable.ic_eye_close, R.string.action_close).apply { value = 0 }
        ).toTypedArray()

        @JvmStatic
        fun getHomePageIndex(homepage: String): Int {
            return when (homepage) {
                SCHEME_HOMEPAGE -> 0
                SCHEME_BOOKMARKS -> 1
                else -> 2
            }
        }

        @JvmStatic
        fun getHomePage(index: Int): String {
            return when (index) {
                0 -> SCHEME_HOMEPAGE
                1 -> SCHEME_BOOKMARKS
                else -> SCHEME_URL
            }
        }

        const val HELP_WEIBO = 0
        const val HELP_TWITTER = 1
        const val HELP_EMAIL = 2
        const val HELP_QQ = 3

    }

}