package com.fula.view

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import com.fula.CLog
import com.fula.util.ViewUnit
import com.fula.yohee.extensions.contains

class BottomDrawer @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RelativeLayout(context, attrs, defStyleAttr) {
    private lateinit var switcherView: View
    private lateinit var mainView: View
    var omnibox: View? = null
    /* slideRange: px */
    private var slideRange = 0f
    private var slideOffset = 1f
    private var interceptX = 0f
    private var interceptY = 0f
    /* coverHeight: px */
    private var coverHeight = 0f
    private var parallaxOffset = PARALLAX_OFFSET_DEFAULT_TOP

    var status = STATUS_DEFAULT
        private set(value) {
            field = value
            statusListener.forEach { it(value) }
        }

    private var statusListener = emptySet<(Int) -> Unit>()
    fun addStatusListener(l: (Int) -> Unit){
        statusListener = statusListener + l
    }

    private val dragHelper: ViewDragHelper?

    private fun setFlingVelocity(flingVelocity: Int) {
        if (dragHelper != null) {
            dragHelper.minVelocity = ViewUnit.dp2px(flingVelocity.toFloat()).toFloat()
        }
    }

    private inner class DragHelperCallback : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return child === mainView
        }

        override fun getViewVerticalDragRange(child: View): Int {
            return slideRange.toInt()
        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            fling(top)
            invalidate()
        }

        override fun onViewDragStateChanged(state: Int) {
            if (dragHelper!!.viewDragState == ViewDragHelper.STATE_IDLE) {
                slideOffset = computeSlideOffset(mainView.top)
                applyParallaxForCurrentSlideOffset()

                if (slideOffset == 1f && status != STATE_DRAWER_CLOSE) {
                    status = STATE_DRAWER_CLOSE
                    switcherView.isEnabled = false
                } else if (slideOffset == 0f && status != STATE_DRAWER_OPEN) {
                    status = STATE_DRAWER_OPEN
                }
            }
        }
    }

    class LayoutParams : ViewGroup.MarginLayoutParams {

        constructor() : super(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        constructor(source: ViewGroup.LayoutParams) : super(source)

        constructor(source: ViewGroup.MarginLayoutParams) : super(source)

        constructor(c: Context, attrs: AttributeSet) : super(c, attrs) {
            val typedArray = c.obtainStyledAttributes(attrs, ATTRS)
            typedArray.recycle()
        }

        companion object {
            private val ATTRS = intArrayOf(android.R.attr.layout_weight)
        }
    }

    private val dimen38dp = ViewUnit.dp2px(38f)
    init {
        parallaxOffset = PARALLAX_OFFSET_DEFAULT_BOTTOM
        dragHelper = ViewDragHelper.create(this, 0.5f, DragHelperCallback())
        setFlingVelocity(FLING_VELOCITY_DEFAULT)
        setWillNotDraw(false)
        invalidateParams(resources.configuration)
    }

    fun invalidateParams(config: Configuration) {
        val albnumH = if (config.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            ViewUnit.dp2px(ALBEN_HEIGHT_PORTRIT)
        } else {
            ViewUnit.dp2px(ALBEN_HEIGHT_LAND)
        }
        val windowHeight = ViewUnit.getWindowHeight(context)
        val statusBarHeight = ViewUnit.getStatusBarHeight(context)
        coverHeight = (windowHeight - /*2 * */statusBarHeight - albnumH - dimen38dp).toFloat()
        CLog.i("windowHeight = $windowHeight, statusBarHeight = $statusBarHeight, coverHeight = $coverHeight")
    }

    override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams {
        return LayoutParams()
    }

    override fun generateLayoutParams(layoutParams: ViewGroup.LayoutParams): ViewGroup.LayoutParams {
        return if (layoutParams is ViewGroup.MarginLayoutParams) LayoutParams(layoutParams) else LayoutParams(layoutParams)
    }

    override fun generateLayoutParams(attrs: AttributeSet): RelativeLayout.LayoutParams {
        return RelativeLayout.LayoutParams(context, attrs)
    }

    override fun checkLayoutParams(layoutParams: ViewGroup.LayoutParams): Boolean {
        return layoutParams is LayoutParams && super.checkLayoutParams(layoutParams)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        when {
            View.MeasureSpec.getMode(widthMeasureSpec) != View.MeasureSpec.EXACTLY -> throw IllegalStateException("Width must have an exact value or MATCH_PARENT.")
            View.MeasureSpec.getMode(heightMeasureSpec) != View.MeasureSpec.EXACTLY -> throw IllegalStateException("Height must have an exact value or MATCH_PARENT.")
            childCount != 2 -> throw IllegalStateException("SwitcherPanel layout must have exactly 2 children!")
            else -> {
                switcherView = getChildAt(0)
                mainView = getChildAt(1)
                val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
                val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)
                val layoutWidth = widthSize - paddingLeft - paddingRight
                val layoutHeight = heightSize - paddingTop - paddingBottom

                for (i in 0 until childCount) {
                    val child = getChildAt(i)
                    val layoutParams = child.layoutParams as LayoutParams

                    var width = layoutWidth
                    var height = layoutHeight
                    if (child === switcherView) {
                        height = (height - coverHeight).toInt()
                        width = width - layoutParams.leftMargin - layoutParams.rightMargin
                    } else if (child === mainView) {
                        height -= layoutParams.topMargin
                    }
                    val childWidthSpec = when {
                        layoutParams.width == ViewGroup.LayoutParams.WRAP_CONTENT -> View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.AT_MOST)
                        layoutParams.width == ViewGroup.LayoutParams.MATCH_PARENT -> View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
                        else -> View.MeasureSpec.makeMeasureSpec(layoutParams.width, View.MeasureSpec.EXACTLY)
                    }
                    val childHeightSpec= when {
                        layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT -> View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.AT_MOST)
                        layoutParams.height == ViewGroup.LayoutParams.MATCH_PARENT -> View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
                        else -> View.MeasureSpec.makeMeasureSpec(layoutParams.height, View.MeasureSpec.EXACTLY)
                    }
                    child.measure(childWidthSpec, childHeightSpec)
                    if (child === mainView) {
                        slideRange = mainView.measuredHeight - coverHeight
                    }
                }
                setMeasuredDimension(widthSize, heightSize)
            }
        }

    }

    override fun onLayout(change: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val paddingLeft = paddingLeft
        val paddingTop = paddingTop

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val layoutParams = child.layoutParams as LayoutParams

            var top = paddingTop
            if (child === mainView) {
                top = computeTopPosition(slideOffset)
            }
            if (child === switcherView) {
                top = computeTopPosition(slideOffset) + mainView.measuredHeight
            }
            val height = child.measuredHeight
            val bottom = top + height
            val left = paddingLeft + layoutParams.leftMargin
            val right = left + child.measuredWidth
            child.layout(left, top, right, bottom)
        }
        applyParallaxForCurrentSlideOffset()
    }

    private fun computeTopPosition(slideOffset: Float): Int {
        val slidePixelOffset = (slideOffset * slideRange).toInt()
        return ((paddingTop - mainView.measuredHeight).toFloat() + coverHeight + slidePixelOffset.toFloat()).toInt()
    }

    private fun computeSlideOffset(topPosition: Int): Float {
        return (topPosition - computeTopPosition(0f)) / slideRange
    }

    override fun computeScroll() {
        if (dragHelper != null && dragHelper.continueSettling(true)) {
            if (!isEnabled) {
                dragHelper.abort()
                return
            }
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        if (!isEnabled || action == MotionEvent.ACTION_CANCEL) {
            return super.onInterceptTouchEvent(event)
        }
        if (action == MotionEvent.ACTION_DOWN) {
            interceptX = event.rawX
            interceptY = event.rawY
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (omnibox?.contains(interceptX.toInt(), interceptY.toInt()) == true) {
                if (interceptY - event.rawY >= ViewUnit.dp2px(42f)) {
                    openDrawer()
                    return true
                }
            }
        }
        if (status == STATE_DRAWER_OPEN
                && mainView.contains(event.rawX.toInt(), event.rawY.toInt())) {
            closeDrawer()
            return true
        }
        return super.onInterceptTouchEvent(event)
    }

    fun closeDrawer() {
        smoothSlideTo(1f)
//        status = STATE_DRAWER_CLOSE
    }

    fun openDrawer() {
        switcherView.isEnabled = true
        smoothSlideTo(0f)
//        status = STATE_DRAWER_OPEN
    }

    private fun fling(top: Int) {
        status = STATE_DRAWER_FLING
        slideOffset = computeSlideOffset(top)
        applyParallaxForCurrentSlideOffset()
        val layoutParams = switcherView.layoutParams as LayoutParams
        val defaultHeight = (height.toFloat() - paddingBottom.toFloat() - paddingTop.toFloat() - coverHeight).toInt()

        if (slideOffset <= 0) {
            layoutParams.height = height - paddingBottom - mainView.measuredHeight - top
        } else if (layoutParams.height != defaultHeight) {
            layoutParams.height = defaultHeight
        }
        switcherView.requestLayout()
    }

    private fun smoothSlideTo(slideOffset: Float): Boolean {
        if (!isEnabled) {
            return false
        }
        val top = computeTopPosition(slideOffset)
        if (dragHelper!!.smoothSlideViewTo(mainView, mainView.left, top)) {
            ViewCompat.postInvalidateOnAnimation(this)
            return true
        }
        return false
    }

    private fun applyParallaxForCurrentSlideOffset() {
        if (parallaxOffset > 0) {
            val offset = ViewUnit.dp2px(parallaxOffset.toFloat()).toFloat()
            switcherView.translationY = +(offset * Math.max(slideOffset, 0f))
        }
    }

    companion object {
        /* parallaxOffset: dp */
        const val ALBEN_HEIGHT_PORTRIT = 50f
        const val ALBEN_HEIGHT_LAND = 90f
        const val PARALLAX_OFFSET_DEFAULT_TOP = 64
        const val PARALLAX_OFFSET_DEFAULT_BOTTOM = 16
        /* flingVelocity: dp/s */
        const val FLING_VELOCITY_DEFAULT = 256
        const val STATE_DRAWER_CLOSE = 0
        const val STATE_DRAWER_OPEN = 1
        const val STATE_DRAWER_FLING = 2
        private const val STATUS_DEFAULT = STATE_DRAWER_CLOSE
    }
}
