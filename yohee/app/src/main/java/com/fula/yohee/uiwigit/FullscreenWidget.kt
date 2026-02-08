package com.fula.yohee.uiwigit

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.fula.CLog
import com.fula.yohee.R
import com.fula.yohee.constant.Setting
import com.fula.yohee.extensions.inflater
import com.fula.yohee.extensions.removeFromParent
import com.fula.yohee.utils.DeviceUtils
import kotlinx.android.synthetic.main.layout_fullscreen_pop.view.*

/**
 * red point helper to draw point on ui control.
 * @author delanding
 * @date 2018-07-13
 */
class FullscreenWidget(mContext: Context, val floatMenu: FloatMenuWidget, private val trigger: ((Boolean) -> Unit)? = null) : RedPoint(mContext) {

    private var anchorView: View? = null

    init {
        popupWindow.apply {
            setBackgroundDrawable(null)
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            contentView = mContext.inflater.inflate(R.layout.layout_fullscreen_pop, null)
        }
    }

    /**判断是否折叠收起状态*/
    fun getIsCollaps(): Boolean {
        popupWindow.apply {
            return contentView.bottom_view_holder.visibility == View.GONE
        }
    }

    fun showFSPoint(holderView: View, anchor: View) {
        floatMenu.fullScreenVisiable(View.VISIBLE)
        popupWindow.apply {
            contentView.apply {
                val holderW = DeviceUtils.getScreenWidth(mContext) - mContext.resources.getDimension(R.dimen.toolbar_height)
                CLog.i("fuw = ${fulls_switch.width}")
                bottom_view_holder.addView(holderView.apply {
                    removeFromParent()
                    layoutParams.apply {
                        width = holderW.toInt()
                    }
                })
                fulls_switch.setOnClickListener {
                    switchCollapse()
                }
                initViewState(bottom_view_holder, false)
            }
            showAtLocation(anchor, Gravity.END or Gravity.BOTTOM, 0, 0)
            Setting.applyModeToView(mContext, contentView.parent as View)
            anchorView = anchor
        }
    }

    fun switchCollapse() {
        popupWindow.contentView.apply {
            initViewState(bottom_view_holder, bottom_view_holder.visibility != View.VISIBLE)
        }
    }

    private fun initViewState(menuHolder: View, open: Boolean) {
        if (open) {
            floatMenu.collapseTrigger(true)
            menuHolder.visibility = View.VISIBLE
        } else {
            floatMenu.collapseTrigger(false)
            menuHolder.visibility = View.GONE
        }
        trigger?.invoke(getIsCollaps())
    }

    fun preShow() {
        floatMenu.fullScreenVisiable(View.VISIBLE)
        anchorView?.let {
            popupWindow.showAtLocation(anchorView, Gravity.END or Gravity.BOTTOM, 0, 0)
        }
    }

    override fun dismiss() {
        floatMenu.fullScreenVisiable(View.GONE)
        floatMenu.collapseTrigger(false)
        super.dismiss()
    }

}
