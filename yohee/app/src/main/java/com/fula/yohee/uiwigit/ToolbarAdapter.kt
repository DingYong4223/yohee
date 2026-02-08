package com.fula.yohee.uiwigit

import android.content.Context
import android.graphics.fonts.FontVariationAxis
import android.view.View
import androidx.annotation.ColorInt
import com.fula.CLog
import com.fula.view.progress.WebProgressBar
import com.fula.yohee.eventbus.SEvent
import com.fula.yohee.preference.UserPreferences
import com.fula.yohee.ui.WebViewController
import org.greenrobot.eventbus.EventBus

open class ToolbarAdapter(context: Context, userPrefer: UserPreferences, toolBar: View, keepBar: View)
    : BaseToolbarAdapter(context, userPrefer, toolBar, keepBar) {

    lateinit var listener: (Float) -> Unit

    override fun adapt(showingTab: WebViewController, offY: Float, @ColorInt endColor: Int): Float {
        val rht = getToolbarHeight()
        //CLog.i("set toolbar tooltransY = ${toolBar.translationY}, offy = $offY")
        var webOffY = 0f
        when {
            (offY >= -rht && offY <= 0f) -> {
                //CLog.i("range in...")
                toolBar.translationY = offY

                if (offY >= heightStatus - rht) {
                    if (keepBar.height != 0) {
                        keepBar.layoutParams.height = 0
                        keepBar.requestLayout()
                    }
                    webOffY = rht + offY - showingTab.showingWebView.translationY
                    showingTab.showingWebView.translationY += webOffY
                } else {
                    if (keepBar.height != heightStatus.toInt()) {
                        keepBar.layoutParams.height = heightStatus.toInt()
                        keepBar.setBackgroundColor(endColor)
                        keepBar.requestLayout()
                    }
                    showingTab.showingWebView.translationY = heightStatus
                }
            }
            offY < -rht -> {
                CLog.i("lower range in...")
                if (toolBar.translationY != -rht) {
                    toolBar.translationY = -rht
                    showingTab.showingWebView.translationY = heightStatus
                }
            }
            else -> {
                CLog.i("upper range in...")
                if (toolBar.translationY != 0f || showingTab.showingWebView.translationY != rht) {
                    toolBar.translationY = 0f
                    showingTab.showingWebView.translationY = rht
                }
            }
        }
        showingTab.showingWebView.translationY.let {
            listener(it)
        }
        return webOffY
    }

}