package com.fula.yohee.view

import android.content.Context
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.core.graphics.plus
import com.fula.yohee.extensions.execIf


class SearchView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = android.R.attr.autoCompleteTextViewStyle)
    : AppCompatAutoCompleteTextView(context, attrs, defStyleAttr) {

    interface FocusListener {
        fun onPreFocus()
    }

    var focusListener: FocusListener? = null
    var drawableListener: ((SearchView, Drawable, Int) -> Unit)? = null
    private var isBeingClicked: Boolean = false
    private var timePressed: Long = 0

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                timePressed = System.currentTimeMillis()
                isBeingClicked = true
            }
            MotionEvent.ACTION_CANCEL -> isBeingClicked = false
            MotionEvent.ACTION_UP -> {
                compoundDrawables[2]?.execIf({ event.x >= (width - paddingRight - it.intrinsicWidth) }) {
                    drawableListener?.invoke(this@SearchView, it, DRAWABLE_RIGHT)
                    return true
                }
                compoundDrawables[0]?.execIf({ event.x <= (paddingLeft + it.intrinsicWidth) }) {
                    drawableListener?.invoke(this@SearchView, it, DRAWABLE_LEFT)
                    return true
                }
                if (isBeingClicked && !isLongPress(timePressed)) {
                    focusListener?.onPreFocus()
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private val leftDrawablePoint = PointF(0f, 0f)
    private val rightDrawablePoint = PointF(0f, 0f)
    private val location = IntArray(2)

    fun leftDrawableScreenCenter(offPoint: PointF): PointF  = drawableScreenCenter(compoundDrawables[0], leftDrawablePoint, offPoint, 0, 0)
    fun rightDrawableScreenCenter(offPoint: PointF): PointF = drawableScreenCenter(compoundDrawables[2], rightDrawablePoint, offPoint, width, 0)

    private fun drawableScreenCenter(drawable: Drawable?, keepPoint: PointF, offPoint: PointF, offX: Int, offY: Int): PointF {
        drawable?.let {
            getLocationOnScreen(location)
            val left = location[0].toFloat() + paddingLeft
            val top = location[1].toFloat() + paddingTop
            return keepPoint.apply {
                x = left + offX + it.intrinsicWidth / 2
                y = top + offY + it.intrinsicHeight / 2
            } + offPoint
        }
        return keepPoint.apply {
            x = offX * 1.0f
            y = offY * 1.0f
        } + offPoint
    }

    private fun isLongPress(actionDownTime: Long): Boolean = System.currentTimeMillis() - actionDownTime >= ViewConfiguration.getLongPressTimeout()

    companion object {
        const val DRAWABLE_LEFT = 0
        const val DRAWABLE_RIGHT = 1
    }

}
