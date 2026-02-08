package com.fula.yohee.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration

class SwipeToDismissListener(private val view: View, private val token: Any?, private val callback: DismissCallback) : View.OnTouchListener {

    private var viewHeight = 1
    private val slop: Int
    private val minFlingVelocity: Int
    private val maxFlingVelocity: Int
    private val animTime: Long

    private var downY: Float = 0.toFloat()
    private var translationY: Float = 0.toFloat()
    private var swiping: Boolean = false
    private var swipingSlop: Int = 0
    private var velocityTracker: VelocityTracker? = null

    interface DismissCallback {
        fun canDismiss(token: Any?): Boolean
        fun onDismiss(view: View, token: Any?)
    }

    init {
        val configuration = ViewConfiguration.get(view.context)
        this.slop = configuration.scaledTouchSlop
        this.minFlingVelocity = configuration.scaledMinimumFlingVelocity
        this.maxFlingVelocity = configuration.scaledMaximumFlingVelocity
        this.animTime = view.context.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        event.offsetLocation(0f, translationY)
        if (viewHeight < 2) {
            viewHeight = this.view.height
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downY = event.rawY
                if (callback.canDismiss(token)) {
                    velocityTracker = VelocityTracker.obtain()
                    velocityTracker!!.addMovement(event)
                }
                return false
            }
            MotionEvent.ACTION_UP -> {
                if (velocityTracker == null) return false

                velocityTracker?.addMovement(event)
                velocityTracker?.computeCurrentVelocity(1000)
                val deltaY = event.rawY - downY
                val velocityY = velocityTracker!!.yVelocity
                val absVelocityY = Math.abs(velocityTracker!!.yVelocity)
                var dismiss = false
                var dismissDown = false
                if (Math.abs(deltaY) > viewHeight / 2 && swiping) {
                    dismiss = true
                    dismissDown = deltaY > 0
                } else if (minFlingVelocity <= absVelocityY && absVelocityY <= maxFlingVelocity && swiping) {
                    dismiss = velocityY < 0 == deltaY < 0
                    dismissDown = velocityTracker!!.yVelocity > 0
                }
                if (dismiss) {
                    this.view.animate()
                            .translationY((if (dismissDown) viewHeight else -viewHeight).toFloat())
                            .alpha(0f)
                            .setDuration(animTime)
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    performDismiss()
                                }
                            })
                } else if (swiping) {
                    this.view.animate()
                            .translationY(0f)
                            .alpha(1f)
                            .setDuration(animTime)
                            .setListener(null)
                }
                downY = 0f
                translationY = 0f
                swiping = false
                velocityTracker?.recycle()
                velocityTracker = null
            }
            MotionEvent.ACTION_CANCEL -> {
                if (velocityTracker == null) return false
                this.view.animate()
                        .translationY(0f)
                        .alpha(1f)
                        .setDuration(animTime)
                        .setListener(null)

                downY = 0f
                translationY = 0f
                swiping = false
                velocityTracker?.recycle()
                velocityTracker = null
            }
            MotionEvent.ACTION_MOVE -> {
                if (velocityTracker == null) return false

                velocityTracker?.addMovement(event)
                val deltaY = event.rawY - downY
                if (Math.abs(deltaY) > slop) {
                    swiping = true
                    swipingSlop = if (deltaY > 0) slop else -slop
                    this.view.parent.requestDisallowInterceptTouchEvent(true)

                    val cancelEvent = MotionEvent.obtainNoHistory(event)
                    cancelEvent.action = MotionEvent.ACTION_CANCEL or (event.actionIndex shl MotionEvent.ACTION_POINTER_INDEX_SHIFT)
                    this.view.onTouchEvent(cancelEvent)
                    cancelEvent.recycle()
                }
                if (swiping) {
                    translationY = deltaY
                    this.view.translationY = deltaY - swipingSlop
                    this.view.alpha = Math.max(0f, Math.min(1f, 1f - 2f * Math.abs(deltaY) / viewHeight))
                    return true
                }
            }
        }

        return false
    }

    private fun performDismiss() {
        val layoutParams = view.layoutParams
        val originalWidth = view.width

        val animator = ValueAnimator.ofInt(originalWidth, 1).setDuration(animTime)
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                callback.onDismiss(view, token)
                view.alpha = 1f
                view.translationY = 0f
                layoutParams.width = originalWidth
                view.layoutParams = layoutParams
            }
        })
        animator.addUpdateListener { animation ->
            layoutParams.width = animation.animatedValue as Int
            view.layoutParams = layoutParams
        }
        animator.start()
    }
}
