package com.fula.yohee

import android.graphics.Color
import androidx.annotation.ColorInt

class Config {

    companion object {
        const val COLOR_ITEM_UNSELECT = Color.GRAY
        @ColorInt
        val COLOR_ITEM_SELECT: Int = Color.parseColor("#FF64965A")

        const val SINA_APP_ID = "1843783351"
        const val TWITTER_ID = "2240609264"
        const val MY_EMAIL = "dingyong4223@163.com"
        const val MY_TWITTER = "https://twitter.com/dingyong4223"
        const val QQ_GROUP_UIN = "245755406"
        const val FLURRY_API_KEY = "FT2YXC679FVF5ZGJNW43"
        const val PROTOL_WEB = "file:///android_asset/protol.html"
        const val SECURE_WEB = "file:///android_asset/secure.html"

        val INVALIDE_SCHEMA = listOf("sinaapp", "baidubox")
    }
}