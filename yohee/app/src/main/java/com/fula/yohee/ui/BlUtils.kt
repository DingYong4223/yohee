package com.fula.yohee.ui

import android.content.Context
import com.fula.util.NetUtils
import com.fula.CLog
import com.fula.yohee.preference.UserPreferences
import com.fula.yohee.settings.UserSetting

class BlUtils {

    companion object {

        fun getBlockImage(context: Context, userPrefs: UserPreferences): Boolean {
            when (userPrefs.blockImage) {
                UserSetting.BLOCK_NONE -> return false
                UserSetting.BLOCK_ALL -> return true
                else -> {
                    val ntype = NetUtils.getNetworkState(context)
                    CLog.i("ntype = $ntype")
                    if (NetUtils.NETWORK_NONE != ntype && NetUtils.NETWORK_WIFI != ntype) return true
                    return false
                }
            }
        }
    }

}