package com.fula.yohee.uiwigit

import android.graphics.Bitmap
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.palette.graphics.Palette
import com.fula.yohee.R
import com.fula.yohee.extensions.inflater
import com.fula.yohee.toolcolor.ToolbarColor
import com.fula.yohee.ui.activity.WebActivity
import kotlinx.android.synthetic.main.layout_debug_window.view.*

/**
 * red point helper to draw point on ui control.
 * @author delanding
 * @date 2018-07-13
 */
class DebugWindow(mContext: WebActivity) : RedPoint(mContext) {

    private val colorHolder: LinearLayout by lazy { popupWindow.contentView.color_holder }

    init {
        popupWindow.apply {
            setBackgroundDrawable(null)
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            contentView = mContext.inflater.inflate(R.layout.layout_debug_window, null).apply {
                debug_layout.visibility = View.GONE
                debug_switch.setOnClickListener {
                    if (debug_layout.visibility == View.VISIBLE) {
                        debug_layout.visibility = View.GONE
                    } else {
                        debug_layout.visibility = View.VISIBLE
                    }
                }
                calc_color.setOnClickListener {
                    if (colorHolder.childCount > 0) {
                        colorHolder.removeAllViews()
                    }
                    calcAndSetToolbarColor(mContext.tabsManager.showingTab.captureImage!!)
                }
                scroll_color.setOnClickListener {
                    if (colorHolder.childCount > 0) {
                        colorHolder.removeAllViews()
                    }
                    calcAndSetToolbarColor(mContext.tabsManager.showingTab.captureScroll!!)
                }
            }
        }
    }

    fun showDebug(anchor: View) {
        popupWindow.showAtLocation(anchor, Gravity.END or Gravity.TOP, 0, 400)
    }

    private fun calcAndSetToolbarColor(capture: Bitmap) {
        val height = (capture.height * ToolbarColor.TITLE_BAR_RATE).toInt()
        val totalNum = capture.width * height * 1.0f
        val palette = Palette.from(capture).setRegion(0, 0, capture.width, height).generate()
        popupWindow.contentView.apply {
            val lp = LinearLayout.LayoutParams(-1, -2)
            TextView(mContext).apply {
                text = "vibrantSwatch:null"
                palette.vibrantSwatch?.let {
                    setBackgroundColor(it.rgb)
                    text = "vibrantSwatch: ${it.population / totalNum}"
                }
                colorHolder.addView(this, lp)
            }

            TextView(mContext).apply {
                text = "lightVibrantSwatch:null"
                palette.lightVibrantSwatch?.let {
                    setBackgroundColor(it.rgb)
                    text = "lightVibrantSwatch: ${it.population / totalNum}"
                }
                colorHolder.addView(this, lp)
            }

            TextView(mContext).apply {
                text = "darkVibrantSwatch:null"
                palette.darkVibrantSwatch?.let {
                    setBackgroundColor(it.rgb)
                    setTextColor(Color.WHITE)
                    text = "darkVibrantSwatch: ${it.population / totalNum}"
                }
                colorHolder.addView(this, lp)
            }

            TextView(mContext).apply {
                text = "mutedSwatch:null"
                palette.mutedSwatch?.let {
                    setBackgroundColor(it.rgb)
                    setTextColor(Color.WHITE)
                    text = "mutedSwatc: ${it.population / totalNum}"
                }
                colorHolder.addView(this, lp)
            }

            TextView(mContext).apply {
                text = "lightMutedSwatch:null"
                palette.lightMutedSwatch?.let {
                    setBackgroundColor(it.rgb)
                    setTextColor(Color.WHITE)
                    text = "lightMutedSwatch: ${it.population / totalNum}"
                }
                colorHolder.addView(this, lp)
            }

            TextView(mContext).apply {
                text = "darkMutedSwatch:null"
                palette.darkMutedSwatch?.let {
                    setBackgroundColor(it.rgb)
                    setTextColor(Color.WHITE)
                    text = "darkMutedSwatch: ${it.population / totalNum}"
                }
                colorHolder.addView(this, lp)
            }
        }
    }


}
