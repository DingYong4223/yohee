package com.fula.yohee.toolcolor

import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.ColorInt
import com.fula.CLog
import com.fula.base.util.StatusBarUtil
import com.fula.util.ViewUnit
import com.fula.view.progress.WebProgressBar
import com.fula.yohee.ui.WebViewController
import com.fula.yohee.ui.activity.WebActivity
import com.fula.yohee.utils.DrawableUtils
import com.fula.yohee.utils.ThemeUtils

abstract class BaseToolColor(val context: WebActivity, private val colorViews: List<View>,
                             private val progressView: WebProgressBar) {
    var endColor: Int = ThemeUtils.getPrimaryColorTrans(context)
        private set
    var blackStatus: Boolean = false
        private set
    abstract fun guessToolbarColor(url: String, capture: Bitmap)
    abstract fun predictToolbarColor(url: String)
    abstract fun tabSwitch(tab: WebViewController)

    fun initToolBarColor(@ColorInt toColor: Int, span: Long = COLOR_TRANS_SHORT) {
//        if (UrlUtils.isGenUrl(context.showingWeb().url)) {
//            toColor = ThemeUtils.getPrimaryColorTrans(context)
//        }
        blackStatus = !DrawableUtils.isThickColor(toColor)
        StatusBarUtil.setStatusBarIconMode(context, blackStatus)
        if (endColor == toColor) {
            CLog.i("same color...")
            return
        }
        CLog.i("endColor = ${DrawableUtils.strColor(endColor)}, to = ${DrawableUtils.strColor(toColor)}")
        val startColor = endColor
        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = span
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {va->
                val interpolate = va.animatedValue as Float
                val mixColor = DrawableUtils.mixColor(interpolate, startColor, endColor)
                CLog.i("set toolbar color: ${DrawableUtils.strColor(mixColor)}, toColor: ${DrawableUtils.strColor(endColor)}")
                colorViews.forEach {
                    it.setBackgroundColor(mixColor)
                }
                progressView.setmTempProgressColor(DrawableUtils.invertColor(mixColor))
            }
        }
        endColor = toColor
        animator.start()
        initViewImageItemColor(if (blackStatus) Color.BLACK else Color.WHITE)
    }

    private fun initViewImageItemColor(color: Int) {
        colorViews.forEach {
            if (it is ViewGroup) {
                ViewUnit.groupColorFilter(it, color, false)
            }
        }
    }

    companion object {
        const val DEF_TOOL_COLOR = Color.WHITE

        const val COLOR_TRANS_LONG = 1000L
        const val COLOR_TRANS_SHORT = 300L
    }

}