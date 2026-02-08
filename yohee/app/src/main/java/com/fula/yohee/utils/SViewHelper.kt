package com.fula.yohee.utils

import android.animation.Animator
import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.content.Context
import android.view.animation.BounceInterpolator

class SViewHelper {

    companion object {

        @SuppressLint("ObjectAnimatorBinding")
        private fun getInAnim(context: Context): Animator {
            val trX = PropertyValuesHolder.ofFloat("translationX", DeviceUtils.getScreenWidth(context).toFloat(), 0f)
            return ObjectAnimator.ofPropertyValuesHolder(this, trX)
        }

        @SuppressLint("ObjectAnimatorBinding")
        private fun getOutAnim(): Animator {
            val trX = PropertyValuesHolder.ofFloat("translationX", 0f, -100f)
            val trY2 = PropertyValuesHolder.ofFloat("translationY", 0f, 0f)
            val trAlpha2 = PropertyValuesHolder.ofFloat("alpha", 1f, 1f)
            return ObjectAnimator.ofPropertyValuesHolder(this, trY2, trAlpha2, trX)
        }

        fun getViewInTransition(context: Context, animTime: Long): LayoutTransition = LayoutTransition().apply {
            //setDuration(LayoutTransition.CHANGE_APPEARING, animTime)
            setDuration(LayoutTransition.APPEARING, 2 * animTime)
            setInterpolator(LayoutTransition.APPEARING, BounceInterpolator())
            //setDuration(LayoutTransition.DISAPPEARING, 50)
            //-----------------------设置动画--------------------
            setAnimator(LayoutTransition.APPEARING, getInAnim(context))
            //setAnimator(LayoutTransition.DISAPPEARING, getOutAnim())
            //---------------------------------------------------
            //setStartDelay(LayoutTransition.CHANGE_APPEARING, 0)
            //setStartDelay(LayoutTransition.APPEARING, 0)
        }

        fun webViewTrans(animTime: Long): LayoutTransition = LayoutTransition().apply {
            setDuration(LayoutTransition.APPEARING, animTime)
            setDuration(LayoutTransition.DISAPPEARING, animTime)
            //-----------------------设置动画--------------------
            setAnimator(LayoutTransition.APPEARING, webInAnim())
            setAnimator(LayoutTransition.DISAPPEARING, webOutAnim())
            //---------------------------------------------------
            setStartDelay(LayoutTransition.APPEARING, 0)
            setStartDelay(LayoutTransition.DISAPPEARING, 100)
        }

        @SuppressLint("ObjectAnimatorBinding")
        private fun webInAnim(): Animator {
            val trX = PropertyValuesHolder.ofFloat("translationX", 100f, 0f)
            val trAlpha = PropertyValuesHolder.ofFloat("alpha", 0f, 1f)
            return ObjectAnimator.ofPropertyValuesHolder(this, trAlpha, trX)
        }

        @SuppressLint("ObjectAnimatorBinding")
        private fun webOutAnim(): Animator {
            val trX = PropertyValuesHolder.ofFloat("translationX", 0f, 100f)
            val trAlpha2 = PropertyValuesHolder.ofFloat("alpha", 1f, 0f)
            return ObjectAnimator.ofPropertyValuesHolder(this, trAlpha2, trX)
        }

        @SuppressLint("ObjectAnimatorBinding")
        fun getYTransition(animTime: Long, height: Float): LayoutTransition = LayoutTransition().apply {
            val trY = PropertyValuesHolder.ofFloat("translationY", 0f, height)
            val trAlpha = PropertyValuesHolder.ofFloat("alpha", 1f, 0f)
            val inAnim = ObjectAnimator.ofPropertyValuesHolder(this, trY, trAlpha)
            setDuration(animTime)
            setAnimator(LayoutTransition.CHANGE_APPEARING, inAnim)
            //-----------------------out-------------------
            val trY1 = PropertyValuesHolder.ofFloat("translationY", height, 0f)
            val trAlpha1 = PropertyValuesHolder.ofFloat("alpha", 0f, 1f)
            val outAnim = ObjectAnimator.ofPropertyValuesHolder(this, trY1, trAlpha1)
            setAnimator(LayoutTransition.CHANGE_DISAPPEARING, outAnim)
        }
    }

}