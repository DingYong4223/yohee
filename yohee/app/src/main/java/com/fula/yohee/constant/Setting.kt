package com.fula.yohee.constant

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.FrameLayout
import com.fula.yohee.R
import com.fula.CLog
import com.fula.yohee.preference.UserPreferences
import com.fula.yohee.settings.UserSetting
import com.fula.yohee.ui.BlUtils
import com.fula.yohee.ui.activity.BaseActivity
import com.fula.yohee.utils.DeviceUtils
import com.fula.yohee.view.YoheeWebClient


class Setting {

    companion object {

        val flp = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        @JvmStatic
        fun getAppColor(userPrefer: UserPreferences): Int {
            if (-1 == userPrefer.useTheme && !userPrefer.nightMode) return -1

            val alpha = if (userPrefer.nightMode) 0x7F else 0x1F
            val nightRGB = if (userPrefer.nightMode) 0x2F else 0
            val rgbValue = 0x5F
            return when (userPrefer.useTheme) {
                UserSetting.THEME_SPRING -> Color.argb(alpha, (0x00 + nightRGB) / 2, (rgbValue + nightRGB) / 2, (0x00 + nightRGB) / 2)
                UserSetting.THEME_SUMMER -> Color.argb(alpha, (0x00 + nightRGB) / 2, (0x00 + nightRGB) / 2, (rgbValue + nightRGB) / 2)
                UserSetting.THEME_AUTUMUN -> Color.argb(alpha, (rgbValue + nightRGB) / 2, (rgbValue + nightRGB) / 2, (0x00 + nightRGB) / 2)
                else -> Color.argb(alpha, 0x2F, 0x2F, 0x2F)
            }
        }

        /**setting theme or night mode*/
        @JvmStatic
        fun applyModeToWindow(activity: Activity, window: Window) = applyModeToView(activity, window.decorView)

        /**setting theme or night mode*/
        @JvmStatic
        fun applyModeToView(activity: Context, decorView: View) {
            if (activity is BaseActivity) {
                var appColor = getAppColor(activity.userPrefer)
                val decor = decorView as FrameLayout
                removeNightIdHas(decor)
                if (-1 != appColor) {
                    decor.addView(FrameLayout(activity).apply {
                        background = ColorDrawable(appColor)
                        id = R.id.night_id
                    }, flp)
                }
            }
        }

        @JvmStatic
        private fun removeNightIdHas(frame: FrameLayout) {
            for (i in frame.childCount - 1 downTo 0) {
                if (frame.getChildAt(i).id == R.id.night_id) {
                    frame.removeViewAt(i)
                }
            }
        }

        @JvmStatic
        fun setDialogSize(context: Context, window: Window?, align: Int = Gravity.CENTER) {
            window?.let {
                //it.setLayout((DeviceUtils.getScreenWidth(context) * 0.9f).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
                it.attributes.apply {
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                    width = (DeviceUtils.getScreenWidth(context) * 0.9f).toInt()
                    gravity = align
                    it.attributes = this
                }
            }
        }

        /**
         * Initialize the settings of the WebView that are intrinsic to Yohee and cannot be altered
         * by the user. Distinguish between Incognito and Regular tabs here.
         */
        @JvmStatic
        @SuppressLint("NewApi")
        fun initWebviewSettings(context: Context, settings: WebSettings) {
            settings.apply {
                mediaPlaybackRequiresUserGesture = true

                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                domStorageEnabled = true
                setAppCacheEnabled(true)
                cacheMode = WebSettings.LOAD_DEFAULT
                setEnableSmoothTransition(true)
                setAppCachePath(context.cacheDir.toString())
                saveFormData = true
                databaseEnabled = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                allowContentAccess = true
                allowFileAccess = true
                allowFileAccessFromFileURLs = false
                allowUniversalAccessFromFileURLs = false

                setSupportMultipleWindows(false)
                defaultTextEncodingName = UTF8
                loadWithOverviewMode = true
                useWideViewPort = true
                javaScriptEnabled = true
                layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL //此处设置会影响textZoom效果
                javaScriptCanOpenWindowsAutomatically = true
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    setGeolocationDatabasePath(context.filesDir.toString())
                }
                if (Build.VERSION.SDK_INT >= 21) {
                    mixedContentMode = 0
                }
            }
        }

        @JvmStatic
        @SuppressLint("NewApi", "SetJavaScriptEnabled")
        fun syncPreferSettings(webList: List<WebView>, mWebClient: YoheeWebClient, userPrefer: UserPreferences) {
            CLog.i("sync websetting preferences...")
            mWebClient.syncPreferSettings(userPrefer)
            for (web in webList) {
                web.apply {
                    //            if (userPrefer.doNotTrackEnabled) {
//                requestHeaders[HEADER_DNT] = "1"
//            } else {
//                requestHeaders.remove(HEADER_DNT)
//            }
//            if (userPrefer.removeIdentifyingHeadersEnabled) {
//                requestHeaders[HEADER_REQUESTED_WITH] = ""
//                requestHeaders[HEADER_WAP_PROFILE] = ""
//            } else {
//                requestHeaders.remove(HEADER_REQUESTED_WITH)
//                requestHeaders.remove(HEADER_WAP_PROFILE)
//            }
                    settings.setGeolocationEnabled(userPrefer.locationEnabled)
                    settings.savePassword = userPrefer.savePwd
                    setUserAgent(context, this, userPrefer.userAgent)
                    settings.blockNetworkImage = BlUtils.getBlockImage(context, userPrefer)
                    settings.textZoom = getTextZoom(userPrefer)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                            CookieManager.getInstance().flush()
                        } else {
                            CookieSyncManager.getInstance().sync()
                            CookieSyncManager.createInstance(context)
                        }
                    }
                }
            }
        }

        private fun getTextZoom(userPrefer: UserPreferences): Int = when (userPrefer.textSize) {
            0 -> 175
            1 -> 150
            2 -> 125
            4 -> 75
            else -> 100
        }

        /**
         * This method sets the user agent of the current tab. There are four options, 1, 2, 3, 4.
         *
         * 1. use the default user agent
         * 2. use the desktop user agent
         * 3. use the mobile user agent
         * 4. use a custom user agent, or the default user agent if none was set.
         *
         * @param choice  the choice of user agent to use, see above comments.
         */
        @JvmStatic
        @SuppressLint("NewApi")
        fun setUserAgent(context: Context, webview: WebView, choice: Int) {
            webview.settings.let {
                when (choice) {
                    1 -> it.userAgentString = WebSettings.getDefaultUserAgent(context)
                    2 -> it.userAgentString = DESKTOP_USER_AGENT
                    3 -> it.userAgentString = MOBILE_USER_AGENT
                    else -> it.userAgentString = MOBILE_USER_AGENT
                }
            }
        }

    }

}
