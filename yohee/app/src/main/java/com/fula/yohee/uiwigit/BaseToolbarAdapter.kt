package com.fula.yohee.uiwigit

import android.content.Context
import android.view.View
import androidx.annotation.ColorInt
import com.fula.util.ViewUnit
import com.fula.view.progress.WebProgressBar
import com.fula.yohee.R
import com.fula.yohee.extensions.dimen
import com.fula.yohee.preference.UserPreferences
import com.fula.yohee.ui.WebViewController

abstract class BaseToolbarAdapter(val context: Context, val userPrefer: UserPreferences, val toolBar: View, val keepBar: View) {

    /**toolbar高度 + 2dp进度条高度*/
    private val heightTool: Float by lazy { context.dimen(R.dimen.toolbar_height).toFloat() }
    var heightStatus: Float = 0f
        private set

    fun updateStatusBarHeight() {
        heightStatus = if (userPrefer.fullScreen) {
            0f//heightProcess
        } else {
            ViewUnit.getStatusBarHeight(context).toFloat()
        }
    }

    fun getToolbarHeight(): Float {
        updateStatusBarHeight()
        return if (userPrefer.fullScreen) {
            heightTool
        } else {
            heightTool + heightStatus// + heightProcess
        }
    }

    abstract fun adapt(showingTab: WebViewController, offY: Float, @ColorInt endColor: Int): Float

}