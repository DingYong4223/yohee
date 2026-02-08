package com.fula.yohee.uiwigit

import android.content.Context
import android.view.View
import androidx.annotation.ColorInt
import com.fula.CLog
import com.fula.view.progress.WebProgressBar
import com.fula.yohee.preference.UserPreferences
import com.fula.yohee.ui.WebViewController

class FixToolbarAdapter(context: Context, userPrefer: UserPreferences, toolBar: View, keepBar: View)
    : ToolbarAdapter(context, userPrefer, toolBar, keepBar) {

    override fun adapt(showingTab: WebViewController, offY: Float, @ColorInt endColor: Int): Float {
        val rht = getToolbarHeight()
        return if (userPrefer.fullScreen) {
            super.adapt(showingTab, offY, endColor)
        } else {
            if (toolBar.translationY != 0f || showingTab.showingWebView.translationY != rht) {
                CLog.i("set toolbar tooltransY = ${toolBar.translationY}, offy = $offY")
                showingTab.showingWebView.translationY = rht
                toolBar.translationY = 0f
            }
            0f
        }
    }

}