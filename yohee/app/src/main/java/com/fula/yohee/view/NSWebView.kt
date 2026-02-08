package com.fula.yohee.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.webkit.WebView
import androidx.core.view.NestedScrollingChild
import androidx.core.view.NestedScrollingChildHelper
import com.fula.CLog

class NSWebView : WebView, NestedScrollingChild {

    private val childHelper: NestedScrollingChildHelper = NestedScrollingChildHelper(this)
    var disListener: ((View, MotionEvent?, Boolean) -> Float)? = null
    var actionSrolly: ((Int, Long) -> Unit)? = null

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
            : super(context, attrs, defStyleAttr) {
//        setBackgroundColor(context.getAppColor(R.color.defthemeDarkColor))
        setBackgroundColor(Color.TRANSPARENT)
        isNestedScrollingEnabled = true
    }

    private var webScolled: Boolean = false
    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        webScolled = true
        actionSrolly?.invoke(scrollY, 0)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        CLog.i("event ${event.action}")
        val webOffY = disListener?.invoke(this, event, webScolled)?: 0f
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                webScolled = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (webOffY != 0f) {
                    CLog.i("move compensate...")
                    return super.onTouchEvent(MotionEvent.obtain(event).apply {
                        offsetLocation(0f, -webOffY)
                        recycle()
                    })
                }
            }
        }
        return super.onTouchEvent(event)
    }

    /*override fun onTouchEvent(event: MotionEvent): Boolean {
        var result = true
        val y = Math.round(event.y)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastMotionY = y
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
                result = super.onTouchEvent(event)
            }
            MotionEvent.ACTION_MOVE -> {
                CLog.i("onmove..." + canScrollHorizontally(1) + " " + canScrollHorizontally(-1))
                val deltaY = lastMotionY - y
                val cansrollY = canScrollVertically(deltaY)
                if (cansrollY) {
                    actionWithParent(event, y, deltaY)?.let {
                        result = super.onTouchEvent(it)
                        if (it != event) it.recycle()
                    }
                } else {
                    result = super.onTouchEvent(event)
                }
            }
            MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                CLog.i("ontouchEvent up...")
                stopNestedScroll()
                result = super.onTouchEvent(event)
            }
        }
        return result
    }*/

    /**@return null if parent actioned the scroll or a new event will be returned.*/
    /*private fun actionWithParent(event: MotionEvent, y: Float, dY: Int): MotionEvent? {
        var deltaY = dY
        if (dispatchNestedPreScroll(0, deltaY, consumed, scrollOffset)) {
            CLog.i("deltaY = $deltaY, consumed = ${consumed[1]}, scrollOffset = ${scrollOffset[1]}")
            deltaY -= consumed[1]
            if (deltaY == 0) return null

            return MotionEvent.obtain(event).apply {
                offsetLocation(0f, -deltaY.toFloat())
            }
        }
        downY = y - scrollOffset[1]
        return event
    }*/

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        childHelper.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return childHelper.isNestedScrollingEnabled
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return childHelper.startNestedScroll(axes)
    }

    override fun stopNestedScroll() {
        childHelper.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean {
        return childHelper.hasNestedScrollingParent()
    }

    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, offsetInWindow: IntArray?): Boolean {
        return childHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow)
    }

    override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?): Boolean {
        return childHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)
    }

    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return childHelper.dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return childHelper.dispatchNestedPreFling(velocityX, velocityY)
    }

    companion object {
        const val LONG_PRESS_EVENT = 1
        const val LONG_PRESS_ACTION = 2
    }

}