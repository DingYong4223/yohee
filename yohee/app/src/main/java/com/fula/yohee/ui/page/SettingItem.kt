package com.fula.yohee.ui.page

import androidx.annotation.DrawableRes
import androidx.annotation.Nullable
import androidx.annotation.StringRes
import com.fula.yohee.settings.UserSetting
import java.io.Serializable

class SettingItem(@DrawableRes val resID: Int?, @StringRes @Nullable val key: Int) : Serializable {

    var icon: Int = UserSetting.NO_VALUE
    var value: Int = UserSetting.NO_VALUE

    companion object {
        const val VALUE_GONE = -2
    }

}