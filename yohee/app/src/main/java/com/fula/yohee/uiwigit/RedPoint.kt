package com.fula.yohee.uiwigit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import com.fula.util.ViewUnit
import com.fula.yohee.constant.Setting

/**
 * red point helper to draw point on ui control.
 * @author delanding
 * @date 2018-07-13
 */
open class RedPoint(var mContext: Context) {

    protected val popupWindow: PopupWindow = PopupWindow(mContext).apply {
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        isOutsideTouchable = false
        isTouchable = true
        isFocusable = false
    }

    /**
     * @desc create a red point.
     * @param drawable the image drawable witch will showListDialog as red point.
     * @param redis the read point's width and heitgh.
     */
    fun setInfo(bit: Bitmap, redis: Int): RedPoint {
        popupWindow.apply {
            width = ViewUnit.dp2px(redis.toFloat())
            height = ViewUnit.dp2px(redis.toFloat())
            contentView = ImageView(mContext).apply {
                scaleType = ImageView.ScaleType.FIT_XY
                setImageBitmap(bit)
            }
        }
        return this
    }

    /**
     * showListDialog read point on the anchor ui conteol
     * @param anchor the ui which will append the red point.
     * @param mode the showListDialog mode, must be one of MODE_ALIGN_ANCHOR, MODE_ALIGN_ANCHOR_RIGHT_TOP, MODE_ALIGN_ANCHOR_CENTER
     * @param offx offset the last position x in dp.
     * @param offy offset the last position y in dp
     */
    fun show(anchor: View, offx: Float, offy: Float, l: ((View) -> Unit)? = null) {
        popupWindow.apply {
            l?.let {
                contentView.setOnClickListener {
                    l(it)
                }
            }
            showAtLocation(anchor, Gravity.TOP or Gravity.END, ViewUnit.dp2px(offx), ViewUnit.dp2px(offy))
            Setting.applyModeToView(mContext, contentView.parent as View)
        }
    }
    open fun dismiss() = popupWindow.dismiss()
}
