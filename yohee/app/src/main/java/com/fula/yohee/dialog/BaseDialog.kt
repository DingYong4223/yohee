package com.fula.yohee.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.annotation.LayoutRes
import com.fula.CLog
import com.fula.yohee.R
import com.fula.yohee.constant.Setting

abstract class BaseDialog(protected val context : Activity, theme: Int = R.style.YoheeDialogStyle) : Dialog(context, theme) {

    interface MenuListener {
        /**@param arg0 index position or item id declared in memu.*/
        fun onCLick(arg0: Int, arg1: Boolean)
    }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        CLog.i("base dialog init...")
        setCancelable(true)
        setCanceledOnTouchOutside(true)
    }

    override fun setContentView(@LayoutRes layoutResID: Int) {
        super.setContentView(layoutResID)
        window?.let {
            Setting.applyModeToWindow(context, it)
            it.attributes = layoutParams(it)
            it.setBackgroundDrawableResource(R.color.defcolorPrimaryDarkTrns)
        }
    }

    abstract fun layoutParams(window: Window): WindowManager.LayoutParams

}