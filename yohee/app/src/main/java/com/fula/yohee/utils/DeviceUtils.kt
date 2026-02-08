package com.fula.yohee.utils

import android.content.Context
import android.graphics.Point
import android.graphics.PointF
import android.util.DisplayMetrics
import android.view.WindowManager

/**
 * Utils used to access information about the device.
 */
object DeviceUtils {

    @JvmStatic
    fun getScreenRateWidth(context: Context, rata: Float): Int = (getScreenWidth(context) * rata).toInt()

    @JvmStatic
    fun getScreenRateHeight(context: Context, rata: Float): Int = (getScreenHeight(context) * rata).toInt()

    @JvmStatic
    fun getScreenWidth(context: Context): Int = getScreenDim(context).x

    @JvmStatic
    fun getScreenHeight(context: Context): Int = getScreenDim(context).y

    @JvmStatic
    fun getScreenDim(context: Context): Point {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return Point().apply {
            windowManager.defaultDisplay.getSize(this)
        }
    }

    fun getScreenCenter(context: Context): PointF {
        return PointF(getScreenWidth(context) / 2f, getScreenHeight(context) / 2f)
    }

    /**
     * Gets the width of the screen space currently available to the app.
     * @param context the context used to access the [WindowManager].
     */
    @JvmStatic
    fun getAvailableScreenWidth(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        return DisplayMetrics().apply {
            windowManager.defaultDisplay.getRealMetrics(this)
        }.widthPixels
    }

}
