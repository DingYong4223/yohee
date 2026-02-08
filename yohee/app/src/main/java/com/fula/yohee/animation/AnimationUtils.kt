package com.fula.yohee.animation

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.animation.addListener
import com.fula.yohee.preference.UserPreferences
import com.fula.yohee.settings.UserSetting

/**
 * Animation specific helper code.
 */
object AnimationUtils {

    /**
     * Creates an animation that rotates an [ImageView] around the Y axis by 180 degrees and changes
     * the image resource shown when the view is rotated 90 degrees to the user.
     *
     * @param imageView   the view to rotate.
     * @param drawableRes the drawable to set when the view is rotated by 90 degrees.
     * @return an animation that will change the image shown by the view.
     */
    @JvmStatic
    fun createRotationTransitionAnimation(
            imageView: ImageView,
            @DrawableRes drawableRes: Int
    ): Animation = object : Animation() {

        private var setFinalDrawable: Boolean = false

        override fun applyTransformation(interpolatedTime: Float, t: Transformation) =
                if (interpolatedTime < 0.5f) {
                    imageView.rotationY = 90f * interpolatedTime * 2f
                } else {
                    if (!setFinalDrawable) {
                        setFinalDrawable = true
                        imageView.setImageResource(drawableRes)
                    }
                    imageView.rotationY = -90 + 90f * (interpolatedTime - 0.5f) * 2f
                }

    }.apply {
        duration = 300
        interpolator = AccelerateDecelerateInterpolator()
    }

    @JvmStatic
    fun genMarkAnim(userPrefer: UserPreferences): String = when (userPrefer.animHomeMarks) {
        UserSetting.ANIM_MARK_TRANS -> {
            """16%  { transform: translate3d(-5%, -5%, 0); }
            50%  { transform: translate3d(10%, 10%, 0) scale(1); color: #0F0;}
            100% { transform: translate3d(0, 0, 0) scale(1); color: #00F; }"""
        }
        UserSetting.ANIM_MARK_FADE -> {
            """0% {opacity: 0;color: red;}
            50% {opacity: 0.8;color: green;}
            100% {opacity: 1;color: blue;}"""
        }
        else -> ""
    }

    @JvmStatic
    fun startAnim(from: Float, to: Float, span: Long, listener: (Float) -> Unit, inter: TimeInterpolator = AccelerateDecelerateInterpolator(),  comlete: (() -> Unit)? = null) {
        val animator = ValueAnimator.ofFloat(from, to).apply {
            duration = span
            interpolator = inter
            addUpdateListener { va ->
                listener(va.animatedValue as Float)
            }
            addListener(onEnd = {
                comlete?.invoke()
            })
        }
        animator.start()
    }

}
