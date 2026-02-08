package com.fula.yohee.uiwigit

import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import com.fula.CLog
import com.fula.view.BottomDrawer
import com.fula.yohee.R
import com.fula.yohee.database.Bookmark
import com.fula.yohee.dialog.SimpleDialog
import com.fula.yohee.eventbus.SEvent
import com.fula.yohee.extensions.inflater
import com.fula.yohee.extensions.removeFromParent
import com.fula.yohee.ui.WebViewController
import com.fula.yohee.ui.activity.WebActivity
import com.fula.yohee.ui.page.SettingAdapter
import com.fula.yohee.utils.DrawableUtils
import com.fula.yohee.utils.UrlUtils
import java.lang.ref.WeakReference
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class YellowTipBar(val context: WebActivity, val rootView: FrameLayout) {

    private var fp: FrameLayout.LayoutParams? = null

    private val handler = YellowHandler(this)

    private val yellowBar: View by lazy { context.inflater.inflate(R.layout.layout_yellow_tip, rootView, false) }
    fun showYellowTip(content: String, action: String? = null, dismissTime: Long = -1L, actListener: (() -> Unit)? = null) {
        CLog.i("showListDialog yellow tips: $rootView, $yellowBar")
        fp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT).apply {
            topMargin = context.toolbarAdapter.getToolbarHeight().toInt()
        }
        val yellowContent = yellowBar.findViewById<TextView>(R.id.yellow_content)
        val yellowAction = yellowBar.findViewById<TextView>(R.id.yellow_action)
        val ltClose = yellowBar.findViewById<ImageView>(R.id.lt_close)
        rootView.addView(yellowBar.apply {
            removeFromParent()
            yellowContent.text = content
            if (action.isNullOrEmpty()) {
                yellowAction.visibility = View.GONE
                yellowAction.text = ""
            } else {
                yellowAction.visibility = View.VISIBLE
                yellowAction.text = action
            }
            yellowAction.setOnClickListener {
                CLog.i("lt_open clicked...")
                handler.removeMessages(MSG_DISMISS_YELLOWBAR)
                yellowBar.removeFromParent()
                actListener?.invoke()
            }
            ltClose.setOnClickListener {
                handler.removeMessages(MSG_DISMISS_YELLOWBAR)
                yellowBar.removeFromParent()
            }
        }, fp)
        if (-1L != dismissTime) {
            handler.sendEmptyMessageDelayed(MSG_DISMISS_YELLOWBAR, dismissTime)
        }
    }

    fun destroy() {
        handler.removeCallbacksAndMessages(null)
    }

    private inner class YellowHandler(tipbar: YellowTipBar) : Handler() {

        private val mReference: WeakReference<YellowTipBar> = WeakReference(tipbar)
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            CLog.i("msg what = ${msg.what}")
            when (msg.what) {
                MSG_DISMISS_YELLOWBAR -> {
                    mReference.get()?.yellowBar.removeFromParent()
                }
            }
        }
    }

    fun updateMarginTop(marginTop: Float) {
        if (yellowBar.parent != null) {
            fp?.let {
                it.topMargin = marginTop.toInt()
            }
            yellowBar.requestLayout()
        }
    }

    companion object {
        const val MSG_DISMISS_YELLOWBAR = 0
    }

}
