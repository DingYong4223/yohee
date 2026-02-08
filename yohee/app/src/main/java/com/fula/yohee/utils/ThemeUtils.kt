package com.fula.yohee.utils

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.fula.util.ViewUnit
import com.fula.yohee.R
import java.util.*

object ThemeUtils {

    private val sTypedValue = TypedValue()

    @ColorInt
    fun getPrimaryColor(context: Context): Int {
        return getColor(context, R.attr.colorPrimary)
    }

    @ColorInt
    fun getPrimaryColorTrans(context: Context): Int {
        return getColor(context, R.attr.colorPrimaryTrans)
    }

    @ColorInt
    fun getPrimaryColorDark(context: Context): Int {
        return getColor(context, R.attr.colorPrimaryDark)
    }

    /**
     * Gets the accent color of the current theme.
     *
     * @param context the context to get the theme from.
     * @return the accent color of the current theme.
     */
    @ColorInt
    fun getAccentColor(context: Context): Int {
        return getColor(context, R.attr.colorAccent)
    }

    /**
     * Gets the color of the status bar as set in styles
     * for the current theme.
     *
     * @param context the context to get the theme from.
     * @return the status bar color of the current theme.
     */
    @ColorInt
    @TargetApi(21)
    fun getStatusBarColor(context: Context): Int {
        return getColor(context, android.R.attr.statusBarColor)
    }

    /**
     * Gets the color attribute from the current theme.
     *
     * @param context  the context to get the theme from.
     * @param resource the color attribute resource.
     * @return the color for the given attribute.
     */
    @ColorInt
    fun getColor(context: Context, @AttrRes resource: Int): Int {
        val a = context.obtainStyledAttributes(sTypedValue.data, intArrayOf(resource))
        val color = a.getColor(0, 0)
        a.recycle()
        return color
    }

    private fun getVectorDrawable(context: Context, drawableId: Int): Drawable {
        var drawable = ContextCompat.getDrawable(context, drawableId)

        Preconditions.checkNonNull(drawable)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = DrawableCompat.wrap(drawable!!).mutate()
        }
        return drawable!!
    }

    // http://stackoverflow.com/a/38244327/1499541
    fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
        val drawable = getVectorDrawable(context, drawableId)

        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }

    /**
     * Gets the icon with an applied color filter
     * for the correct theme.
     *
     * @param context the context to use.
     * @param res     the drawable resource to use.
     * @param dark    true for icon suitable for use with a dark theme,
     * false for icon suitable for use with a light theme.
     * @return a themed icon.
     */
    fun getThemedDrawable(context: Context, @DrawableRes res: Int): Drawable {
        val drawable = getVectorDrawable(context, res)
        drawable.mutate()
//        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        return drawable
    }

    /**设置带宽度和高度的drawable*/
    fun getThemedBoundsDrawable(context: Context, @DrawableRes res: Int, wh: Float): Drawable = getThemedDrawable(context, res)
            .apply { setBounds(0, 0, ViewUnit.dp2px(wh), ViewUnit.dp2px(wh)) }


    /**
     * Gets the edit text text color for the current theme.
     *
     * @param context the context to use.
     * @return a text color.
     */
    @ColorInt
    fun getTextColor(context: Context): Int {
        return getColor(context, android.R.attr.editTextColor)
    }

    @JvmStatic
    fun getThemeBg(): Int {
        return when(Calendar.getInstance().get(Calendar.MONTH) + 1) {
            in 1..3 -> R.mipmap.bg_spring
            in 4..6 -> R.mipmap.bg_summer
            in 7..9 -> R.mipmap.bg_autumn
            in 10..12 -> R.mipmap.bg_winter
            else -> R.mipmap.bg_spring
        }
    }

}
