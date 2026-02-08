package com.fula.ext

import android.animation.Animator

open class AnimListener : Animator.AnimatorListener{
    override fun onAnimationRepeat(animation: Animator) = Unit

    override fun onAnimationEnd(animation: Animator) = Unit

    override fun onAnimationCancel(animation: Animator) = Unit

    override fun onAnimationStart(animation: Animator) = Unit

}

