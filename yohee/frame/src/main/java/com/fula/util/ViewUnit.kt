package com.fula.util

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import java.io.File

object ViewUnit {

    /**
     * get captrue of the view
     * @param pxw the bitmap's width in dp mode
     * @param pxh the bitmap's height in dp mode
     */
    @JvmOverloads
    fun capture(view: View, map: Bitmap?, width: Float, height: Float, config: Bitmap.Config = Bitmap.Config.RGB_565): Bitmap {
        if (!view.isDrawingCacheEnabled) {
            view.isDrawingCacheEnabled = true
        }
        val bitmap = map ?: Bitmap.createBitmap(width.toInt(), height.toInt(), config)
        bitmap.eraseColor(Color.WHITE)
        val canvas = Canvas(bitmap)
        val left = view.scrollX
        val top = view.scrollY
//        val status = canvas.save()
        canvas.translate((-left).toFloat(), (-top).toFloat())
        val scale = width / view.width
        canvas.scale(scale, scale, left.toFloat(), top.toFloat())
        view.draw(canvas)
//        canvas.restoreToCount(status)
        //DebugHelper.saveBitmapImage(bitmap)
        return bitmap
    }

    @JvmOverloads
    fun captureShare(view: View, width: Float, height: Float, waterTxt: String, config: Bitmap.Config = Bitmap.Config.RGB_565): File {
        if (!view.isDrawingCacheEnabled) {
            view.isDrawingCacheEnabled = true
        }
        val bitmap = Bitmap.createBitmap(width.toInt(), height.toInt(), config)
        bitmap.eraseColor(Color.WHITE)
        val canvas = Canvas(bitmap)
        val left = view.scrollX
        val top = view.scrollY
        canvas.translate((-left).toFloat(), (-top).toFloat())
        val scale = width / view.width
        canvas.scale(scale, scale, left.toFloat(), top.toFloat())
        view.draw(canvas)
        val rect = Rect()
        val paint = getTextPaint(0x77FF0000, 6, 48f)
        paint.getTextBounds(waterTxt, 0, waterTxt.length, rect)
        canvas.let {
            it.rotate(-45f/*, it.width / 2f, it.height / 2f*/)
            it.drawText(waterTxt, (it.width - rect.width()) / 2f, (it.height - rect.height()) / 2f, paint)
        }
        return YoUtils.saveBitmap2File(bitmap, Environment.getExternalStorageDirectory().path, "shareTemp.png")
    }

    private fun getTextPaint(color: Int, thickness: Int, dpSize: Float): Paint {
        val paint = Paint()
        paint.strokeWidth = thickness * 1.0f
        paint.color = color
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textSize = dp2px(dpSize).toFloat()
        paint.isAntiAlias = true
        paint.textAlign = Paint.Align.CENTER
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
        paint.style = Paint.Style.FILL
        return paint
    }

    /**
     * Converts Density Pixels (DP) to Pixels (PX).
     *
     * @param dp the number of density pixels to convert.
     * @return the number of pixels that the conversion generates.
     */
    fun dp2px(dp: Float): Int {
        val metrics = Resources.getSystem().displayMetrics
        return (dp * metrics.density + 0.5f).toInt()
    }

    fun getDrawable(context: Context, id: Int): Drawable {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            context.resources.getDrawable(id, null)
        } else context.resources.getDrawable(id)
    }

    fun getStatusBarHeight(context: Context): Int {
        val resources = context.resources
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else 0

    }

    fun getWindowHeight(context: Context): Int {
        var wh = context.resources.displayMetrics.heightPixels
        if (Settings.Global.getInt(context.contentResolver, "force_fsg_nav_bar", 0) != 0) {
            //小米全面屏手机window高度不包括导航栏，需要自己计算并加上
            return wh + getNavigationBarHeight(context)
        }
        return wh
    }

    fun getNavigationBarHeight(context: Context): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    fun groupColorFilter(group: View, color: Int, recursive: Boolean = true) {
        if (group is ViewGroup) {
            for (i in 0 until group.childCount) {
                val item = group.getChildAt(i)
                when (item) {
                    is ImageView -> {
                        if (item.isEnabled) {
                            item.setColorFilter(color, PorterDuff.Mode.SRC_IN)
                        }
                    }
                    is ViewGroup -> {
                        if (recursive) groupColorFilter(item, color)
                    }
                    else -> {
                    }
                }

            }
        }
    }

}