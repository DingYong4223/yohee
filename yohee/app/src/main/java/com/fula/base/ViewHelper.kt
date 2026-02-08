package com.fula.base

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.text.Spanned
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import com.fula.CLog
import com.fula.util.ViewUnit
import com.fula.yohee.R
import com.fula.yohee.utils.DeviceUtils
import java.io.File

/**
 * @Date: 2017-06-09
 * @author: delanding
 */
object ViewHelper {

    private const val DRAWABLE_LEFT = 0
    private const val DRAWABLE_RIGHT = 1
    private const val DRAWABLE_BOTTOM = 3
    const val DRAWABLE_TOP = 4

    private var ischeked = false
    private var isdevelop = false

    /**
     * get if in the develop mode for developer use
     */
    val isDevelop: Boolean
        get() {
            if (ischeked) {
                return isdevelop
            }
            ischeked = true
            isdevelop = File("/sdcard/debug_local").exists()
            return isdevelop
        }

    /**获取TextView对应的值
     * @param pview TextView对应的父控件
     * @param resId 控件ID
     * @param deftxt 失败时返回的默认值
     */
    fun getViewText(pview: View, resId: Int, deftxt: String): String {
        try {
            val item = pview.findViewById<View>(resId) ?: return deftxt
            if (item is EditText) {
                return item.text.toString()
            } else if (item is TextView) {
                return item.text.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return deftxt
    }

    fun setTextView(pview: View, resId: Int, txt: Spanned): TextView? {
        try {
            val tmp = pview.findViewById<View>(resId) as TextView
            tmp.text = txt
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun setTextView(pview: View, resId: Int, txt: String, visible: Int): TextView? {
        val tmp = setTextView(pview, resId, txt)
        if (null != tmp) {
            tmp.visibility = visible
        }
        return tmp
    }

    fun setTextHint(pview: View, resId: Int, txt: String): TextView? {
        try {
            val tmp = pview.findViewById<View>(resId) as TextView
            if (!TextUtils.isEmpty(txt)) {
                tmp.hint = txt
                return tmp
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    fun setTextView(pview: View, resId: Int, txt: String): TextView? {
        try {
            val tmp = pview.findViewById<View>(resId) as TextView
            if (!TextUtils.isEmpty(txt)) {
                tmp.text = txt
                return tmp
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    fun setImageView(pview: View, resId: Int, imgId: Int): ImageView? {
        try {
            val tmp = pview.findViewById<View>(resId) as ImageView
            tmp.setImageResource(imgId)
            return tmp
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    fun clearChilds(pview: View, resId: Int): ViewGroup? {
        try {
            val tmp = pview.findViewById<View>(resId) as ViewGroup
            tmp.removeAllViews()
            return tmp
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    fun setViewListener(pview: View, resId: Int, l: View.OnClickListener): View? {
        try {
            val tmp = pview.findViewById<View>(resId)
            tmp?.setOnClickListener(l)
            return tmp
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun setViewVisible(pview: View?, resId: Int, visiable: Int): View? {
        try {
            val tmp = pview?.findViewById<View>(resId)
            if (null != tmp) {
                tmp.visibility = visiable
            }
            return tmp
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    fun createColorStateList(normal: Int, pressed: Int, focused: Int, unable: Int): ColorStateList {
        val colors = intArrayOf(pressed, focused, normal, focused, unable, normal)
        val states = arrayOfNulls<IntArray>(6)
        states[0] = intArrayOf(android.R.attr.state_pressed, android.R.attr.state_enabled)
        states[1] = intArrayOf(android.R.attr.state_enabled, android.R.attr.state_focused)
        states[2] = intArrayOf(android.R.attr.state_enabled)
        states[3] = intArrayOf(android.R.attr.state_focused)
        states[4] = intArrayOf(android.R.attr.state_window_focused)
        states[5] = intArrayOf()
        return ColorStateList(states, colors)
    }

    fun setDrawble(tv: TextView, resId: Int, type: Int) {
        if (-1 == resId) {
            tv.setCompoundDrawables(null, null, null, null)
            return
        }
        val drawable = tv.context.resources.getDrawable(resId)
        setDrawble(tv, drawable, type)
//        drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
//        when {
//            DRAWABLE_LEFT == type -> tv.setCompoundDrawables(drawable, null, null, null)
//            DRAWABLE_RIGHT == type -> tv.setCompoundDrawables(null, null, drawable, null)
//            DRAWABLE_BOTTOM == type -> tv.setCompoundDrawables(null, null, null, drawable)
//            else -> tv.setCompoundDrawables(null, drawable, null, null)
//        }
    }

    fun setDrawble(tv: TextView, drawable: Drawable, type: Int) {
        drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
        when {
            DRAWABLE_LEFT == type -> tv.setCompoundDrawables(drawable, null, null, null)
            DRAWABLE_RIGHT == type -> tv.setCompoundDrawables(null, null, drawable, null)
            DRAWABLE_BOTTOM == type -> tv.setCompoundDrawables(null, null, null, drawable)
            else -> tv.setCompoundDrawables(null, drawable, null, null)
        }
    }

    fun setTVDrawbleColor(tv: TextView, @ColorInt color: Int) {
        //tv.setTextColor(color)
        val drawables = tv.compoundDrawables
        for (i in drawables.indices) {
            val d = drawables[i]
            d?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            //d?.setTint(color)
        }
    }

    fun setImgViewEnable(image: ImageView, enable: Boolean, @ColorInt disableColor: Int = Color.GRAY) {
        image.isEnabled = enable
        image.drawable?.let {
            if (enable) {
                it.clearColorFilter()
            } else {
                it.setColorFilter(disableColor, PorterDuff.Mode.SRC_IN)
            }
        }
    }

    fun captrueWebView(view: WebView, map: Bitmap?): Bitmap? {
        view.context.resources.apply {
            if (null == view.parent && (view.width == 0 || view.height == 0)) {
                val sw = DeviceUtils.getScreenWidth(view.context)
                val sh = DeviceUtils.getScreenHeight(view.context)
                val measuredWidth = View.MeasureSpec.makeMeasureSpec(sw, View.MeasureSpec.EXACTLY)
                val measuredHeight = View.MeasureSpec.makeMeasureSpec(sh, View.MeasureSpec.EXACTLY)
                view.measure(measuredWidth, measuredHeight)
                view.layout(0, 0, sw, sh)
                CLog.i("viewW = ${view.width}, viewH = ${view.height}")
            }
            return ViewUnit.capture(view, map, getDimension(R.dimen.captrue_width) * 0.7f, getDimension(R.dimen.captrue_height) * 0.7f)
        }
    }

    fun captrueShareWebView(view: WebView, toolHeight: Float, waterTxt: String): File {
        val sw = DeviceUtils.getScreenWidth(view.context)
        val sh = DeviceUtils.getScreenHeight(view.context) - toolHeight
        return ViewUnit.captureShare(view, sw.toFloat(), sh, waterTxt)
    }

    fun captrueScroll(view: View, map: Bitmap?): Bitmap? {
        view.context.resources.apply {
            return ViewUnit.capture(view, map, getDimension(R.dimen.captrue_width) * 0.7f, ViewUnit.dp2px(2f).toFloat())
        }
    }

//    fun showEngineMenu(context: Context, archer: View, click: (item: MenuItem) -> Boolean): PopupMenu {
//        return PopupMenu(context, archer).apply {
//            menuInflater.inflate(R.menu.menu_engines, menu)
//            setOnMenuItemClickListener {
//                click(it)
//            }
//            showListDialog()
//            setMenuIconEnable(context, this)
//        }
//    }

}