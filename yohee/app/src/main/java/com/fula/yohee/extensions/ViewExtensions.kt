package com.fula.yohee.extensions

import android.app.Dialog
import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.Rect
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.view.animation.Interpolator
import android.view.animation.Transformation
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import com.fula.CLog

/**
 * Removes a view from its parent if it has one.
 */
fun View?.removeFromParent() = this?.let {
    val parent = it.parent
    (parent as? ViewGroup)?.removeView(it)
}

/**
 * Performs an action when the view is laid out.
 *
 * @param runnable the runnable to run when the view is laid out.
 */
inline fun View?.doOnLayout(crossinline runnable: () -> Unit) = this?.let {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            viewTreeObserver.removeOnGlobalLayoutListener(this)
            runnable()
        }
    })
}

fun Dialog.getString(@StringRes resId: Int) = this.let {
    context.getString(resId)
}

fun View.setTextView(@IdRes resId: Int, tt: String) = this.apply {
    findViewById<TextView>(resId)?.text = tt
}

fun View.setImageView(@IdRes resId: Int, @DrawableRes img: Int, colorFilter: Int? = null) = this.apply {
    findViewById<ImageView>(resId)?.let {
        it.setImageResource(img)
        if (null != colorFilter) {
            it.setColorFilter(colorFilter, PorterDuff.Mode.SRC_IN)
        }
    }
}

fun View.startAnim(duration: Long, inter: Interpolator, action: ((View, Float, Transformation) -> Unit)) = this.apply {
    val anim = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            action(this@startAnim, interpolatedTime, t)
        }
    }
    anim.duration = duration
    anim.interpolator = inter
    startAnimation(anim)
}

fun ViewGroup.addVisibleView(child: View, index: Int, params: ViewGroup.LayoutParams) {
    addView(child, index, params)
    if (child.visibility != View.VISIBLE) {
        child.visibility = View.VISIBLE
    }
}

fun ViewGroup.removeAllWebViews() {
    for (i in childCount - 1 downTo 0) {
        var child = getChildAt(i)
        if (child is WebView) {
            child.removeFromParent()
        }
    }
}

fun View.contains(rawX: Int, rawY: Int): Boolean {
    val location = IntArray(2)
    getLocationOnScreen(location)
    return Rect(location[0], location[1], left + width, top + height).contains(rawX, rawY)
}

fun View.center(): PointF {
    val location = IntArray(2)
    getLocationOnScreen(location)
    return PointF(location[0].toFloat() + width / 2, location[1].toFloat() + height / 2)
}

fun View.getIntTag(defTag: Int): Int {
    try {
        val tag = this.tag
        if (tag is Int) return tag.toInt()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return defTag
}

fun WebView.loadJavascript(js: String?) {
    CLog.i("js = $js")
    if (!TextUtils.isEmpty(js)) this.loadUrl("javascript:$js") else {
    }
}

