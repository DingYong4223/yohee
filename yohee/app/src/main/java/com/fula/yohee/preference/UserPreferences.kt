package com.fula.yohee.preference

import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import com.fula.yohee.constant.DEF_MEDIA_CACHE_DAY
import com.fula.yohee.constant.DEF_PROXY_PORT
import com.fula.yohee.constant.NO_PROXY
import com.fula.yohee.di.UserPrefs
import com.fula.yohee.extensions.toBoolean
import com.fula.yohee.extensions.toInt
import com.fula.yohee.pb.Info
import com.fula.yohee.preference.delegates.booleanPreference
import com.fula.yohee.preference.delegates.intPreference
import com.fula.yohee.preference.delegates.stringPreference
import com.fula.yohee.search.engine.BaseSearchEngine
import com.fula.yohee.settings.UserSetting
import com.fula.yohee.settings.UserSetting.Companion.ANIM_MARK_TRANS
import com.fula.yohee.settings.UserSetting.Companion.BLOCK_NONE
import javax.inject.Inject
import javax.inject.Singleton


/**
 * The user's preferences.
 */
@Singleton
class UserPreferences @Inject constructor(@UserPrefs preferences: SharedPreferences) {
    var appInitialized by preferences.intPreference(APPINITIALIZED, 0) //不能备份
    var blockAd by preferences.booleanPreference(BLOCKADS, true)
    var webRtcEnabled by preferences.booleanPreference(WEBRTCENABLED, false)
    var blockImage by preferences.intPreference(BLOCKIMAGE, BLOCK_NONE)
    var clearCacheExit by preferences.booleanPreference(CLEARCACHEEXIT, false)
    var cookiesEnabled by preferences.booleanPreference(COOKIESENABLED, true)
    var fullScreen by preferences.booleanPreference(FULLSCREEN, false)
    var locationEnabled by preferences.booleanPreference(LOCATIONENABLED, false)
    var restoreLostTabs by preferences.booleanPreference(RESTORELOSTTABS, false)
    var savePwd by preferences.booleanPreference(SAVEPWD, true)
    var engine by preferences.intPreference(ENGINE, BaseSearchEngine.ENGINE_GOOGLE)
    var textReflowEnabled by preferences.booleanPreference(TEXTREFLOWENABLED, false)
    var textSize by preferences.intPreference(TEXTSIZE, 3)
    var screenRotate by preferences.intPreference(SCREENROTATE, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
    var userAgent by preferences.intPreference(USERAGENT, 1)
    var clearHistoryExit by preferences.booleanPreference(CLEARHISTORYEXIT, false)
    var clearCookiesExit by preferences.booleanPreference(CLEARCOOKIESEXIT, false)
    var colorModeEnabled by preferences.booleanPreference(COLORMODEENABLED, true)
    var useTheme by preferences.intPreference(USETHEME, -1)
    var dynamicBg by preferences.intPreference(DYNAMICBG, -1)
    var clearWebCacheExit by preferences.booleanPreference(CLEARWEBSTORAGEEXIT, false)
    var rightHandUsage by preferences.booleanPreference(RIGHTHANDUSAGE, false)
    var proxyChoice by preferences.intPreference(PROXYCHOICE, NO_PROXY)
    var proxyHost by preferences.stringPreference(PROXYHOST, "localhost")
    var proxyPort by preferences.intPreference(PROXYPORT, DEF_PROXY_PORT)
    var nightMode by preferences.booleanPreference(NIGHTMODE, false)
    var mediaCacheDay by preferences.intPreference(MEDIACACHEDAY, DEF_MEDIA_CACHE_DAY)
    var autoMediaDetect by preferences.booleanPreference(AUTOMEDIADETECT, false)
    var animHomeMarks by preferences.intPreference(ANIMHOMEMARKS, UserSetting.ANIM_MARK_FADE)
    var sslErrorShake by preferences.booleanPreference(SSLERRORSHAKE, false)
    var wifiDownload by preferences.booleanPreference(WIFIDOWNLOAD, true)
    var fixToolbar by preferences.booleanPreference(FIXTOOLBAR, false)

    var checkedForTor by preferences.booleanPreference(CHECKEDFORTOR, false)
    var checkedForI2P by preferences.booleanPreference(CHECKEDFORI2P, false)
    var toolColorFollow by preferences.booleanPreference(COLOEFOLOWW, true)
    var floatMenu by preferences.intPreference(FLOATMENU, UserSetting.DOT_SHOW_ALWAYS)

    /****************应用备份*******************/
    fun exportSetting(): Info.UserPrefer {
        val settingBuilder = Info.UserPrefer.newBuilder()
        settingBuilder.apply {
            putSettings(WEBRTCENABLED, webRtcEnabled.toInt())
            putSettings(BLOCKADS, blockAd.toInt())
            putSettings(BLOCKIMAGE, blockImage)
            putSettings(CLEARCACHEEXIT, clearCacheExit.toInt())
            putSettings(COOKIESENABLED, cookiesEnabled.toInt())
            putSettings(FULLSCREEN, fullScreen.toInt())
            putSettings(LOCATIONENABLED, locationEnabled.toInt())
            putSettings(RESTORELOSTTABS, restoreLostTabs.toInt())
            putSettings(SAVEPWD, savePwd.toInt())
            putSettings(ENGINE, engine)
            putSettings(TEXTREFLOWENABLED, textReflowEnabled.toInt())
            putSettings(TEXTSIZE, textSize)
            putSettings(SCREENROTATE, screenRotate)
            putSettings(USERAGENT, userAgent)
            putSettings(CLEARHISTORYEXIT, clearHistoryExit.toInt())
            putSettings(CLEARCOOKIESEXIT, clearCookiesExit.toInt())
            putSettings(COLORMODEENABLED, colorModeEnabled.toInt())
            putSettings(USETHEME, useTheme)
            putSettings(DYNAMICBG, dynamicBg)
            putSettings(CLEARWEBSTORAGEEXIT, clearWebCacheExit.toInt())
            putSettings(RIGHTHANDUSAGE, rightHandUsage.toInt())
            putSettings(PROXYCHOICE, proxyChoice)
            putSettings(PROXYPORT, proxyPort)
            putSettings(NIGHTMODE, nightMode.toInt())
            putSettings(MEDIACACHEDAY, mediaCacheDay)
            putSettings(AUTOMEDIADETECT, autoMediaDetect.toInt())
            putSettings(CHECKEDFORTOR, checkedForTor.toInt())
            putSettings(FIXTOOLBAR, fixToolbar.toInt())
            putSettings(CHECKEDFORI2P, checkedForI2P.toInt())
            putSettings(COLOEFOLOWW, toolColorFollow.toInt())
            putSettings(ANIMHOMEMARKS, animHomeMarks)
            putSettings(SSLERRORSHAKE, sslErrorShake.toInt())
            putSettings(WIFIDOWNLOAD, wifiDownload.toInt())
            putSettings(FLOATMENU, floatMenu)
        }
        settingBuilder.host = proxyHost
        return settingBuilder.build()
    }

    fun importSetting(up: Info.UserPrefer) {
        webRtcEnabled = up.getSettingsOrDefault(WEBRTCENABLED, -1).toBoolean()
        blockAd = up.getSettingsOrDefault(BLOCKADS, 1).toBoolean()
        blockImage = up.getSettingsOrDefault(BLOCKIMAGE, BLOCK_NONE)
        clearCacheExit = up.getSettingsOrDefault(CLEARCACHEEXIT, -1).toBoolean()
        cookiesEnabled = up.getSettingsOrDefault(COOKIESENABLED, -1).toBoolean()
        fullScreen = up.getSettingsOrDefault(FULLSCREEN, -1).toBoolean()
        locationEnabled = up.getSettingsOrDefault(LOCATIONENABLED, -1).toBoolean()
        restoreLostTabs = up.getSettingsOrDefault(RESTORELOSTTABS, -1).toBoolean()
        savePwd = up.getSettingsOrDefault(SAVEPWD, -1).toBoolean()
        engine = up.getSettingsOrDefault(ENGINE, BaseSearchEngine.ENGINE_GOOGLE)
        textReflowEnabled = up.getSettingsOrDefault(TEXTREFLOWENABLED, -1).toBoolean()
        textSize = up.getSettingsOrDefault(TEXTSIZE, 100)
        screenRotate = up.getSettingsOrDefault(SCREENROTATE, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
        userAgent = up.getSettingsOrDefault(USERAGENT, 1)
        clearHistoryExit = up.getSettingsOrDefault(CLEARHISTORYEXIT, -1).toBoolean()
        clearCookiesExit = up.getSettingsOrDefault(CLEARCOOKIESEXIT, -1).toBoolean()
        colorModeEnabled = up.getSettingsOrDefault(COLORMODEENABLED, 1).toBoolean()
        useTheme = up.getSettingsOrDefault(USETHEME, -1)
        dynamicBg = up.getSettingsOrDefault(DYNAMICBG, -1)
        clearWebCacheExit = up.getSettingsOrDefault(CLEARWEBSTORAGEEXIT, -1).toBoolean()
        rightHandUsage = up.getSettingsOrDefault(RIGHTHANDUSAGE, -1).toBoolean()
        proxyChoice = up.getSettingsOrDefault(PROXYCHOICE, NO_PROXY)
        proxyPort = up.getSettingsOrDefault(PROXYPORT, DEF_PROXY_PORT)
        nightMode = up.getSettingsOrDefault(NIGHTMODE, -1).toBoolean()
        mediaCacheDay = up.getSettingsOrDefault(MEDIACACHEDAY, DEF_MEDIA_CACHE_DAY)
        autoMediaDetect = up.getSettingsOrDefault(AUTOMEDIADETECT, -1).toBoolean()
        fixToolbar = up.getSettingsOrDefault(FIXTOOLBAR, -1).toBoolean()
        checkedForTor = up.getSettingsOrDefault(CHECKEDFORTOR, -1).toBoolean()
        checkedForI2P = up.getSettingsOrDefault(CHECKEDFORI2P, -1).toBoolean()
        toolColorFollow = up.getSettingsOrDefault(COLOEFOLOWW, -1).toBoolean()
        animHomeMarks = up.getSettingsOrDefault(ANIMHOMEMARKS, ANIM_MARK_TRANS)
        sslErrorShake = up.getSettingsOrDefault(SSLERRORSHAKE, -1).toBoolean()
        wifiDownload = up.getSettingsOrDefault(WIFIDOWNLOAD, -1).toBoolean()
        proxyHost = up.host
        floatMenu = up.getSettingsOrDefault(FLOATMENU, UserSetting.DOT_SHOW_ALWAYS)
    }

}

private const val WEBRTCENABLED = "webRtc"
private const val APPINITIALIZED = "appinitialized"
private const val BLOCKADS = "AdBlock"
private const val BLOCKIMAGE = "blockimages"
private const val CLEARCACHEEXIT = "cache"
private const val COOKIESENABLED = "cookies"
private const val FULLSCREEN = "fullscreen"
private const val LOCATIONENABLED = "location"
private const val RESTORELOSTTABS = "restoreclosed"
private const val SAVEPWD = "passwords"
private const val ENGINE = "engine"
private const val TEXTREFLOWENABLED = "textreflow"
private const val TEXTSIZE = "textsize"
private const val SCREENROTATE = "screen_rotate" //1：锁定竖屏，2：锁定横屏，0:跟随系统
private const val USERAGENT = "agentchoose"
private const val CLEARHISTORYEXIT = "clearHistoryExit"
private const val CLEARCOOKIESEXIT = "clearCookiesExit"
private const val COLORMODEENABLED = "colorMode"
private const val USETHEME = "Theme"
private const val DYNAMICBG = "dynamicbg"
private const val CLEARWEBSTORAGEEXIT = "clearWebCacheExit"
private const val RIGHTHANDUSAGE = "swapBookmarksAndTabs"
private const val PROXYCHOICE = "proxyChoice"
private const val PROXYHOST = "useProxyHost"
private const val PROXYPORT = "useProxyPort"
private const val NIGHTMODE = "nightMode"
private const val MEDIACACHEDAY = "media_cache_day" //媒体缓存时间
private const val AUTOMEDIADETECT = "autoMediadetect" //自动媒体监测
private const val CHECKEDFORTOR = "checkForTor"
private const val FIXTOOLBAR = "fixtoolbar"          //是否固定导航栏
private const val CHECKEDFORI2P = "checkForI2P"
private const val COLOEFOLOWW = "certforcetip"       //证书强制弹框提醒
private const val ANIMHOMEMARKS = "animhomemarks"    //首页标签进场动画
private const val SSLERRORSHAKE = "sslerrorshake"    //https证书异常动画提醒
private const val WIFIDOWNLOAD = "wifidownload"      //https证书异常动画提醒
private const val FLOATMENU = "dotshow"                //是否显示小红点
