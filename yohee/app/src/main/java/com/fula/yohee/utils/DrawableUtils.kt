package com.fula.yohee.utils

import android.app.Application
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.fula.util.ViewUnit
import com.fula.CLog


object DrawableUtils {

    /**generate round number image view.
     * @param width the width in dp mode.
     * @param height the height in dp mode.
     * */
    fun getRectedNumberImage(number: Int, w: Float, h: Float, color: Int, thickness: Int): Bitmap {
        val text = if (number > 99) "\u221E" else number.toString()
        val image = Bitmap.createBitmap(ViewUnit.dp2px(w), ViewUnit.dp2px(h), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)
        val paint = Paint()
        paint.strokeWidth = thickness * 1.0f
        paint.color = color
        val boldText = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.typeface = boldText
        paint.textSize = ViewUnit.dp2px(8f).toFloat()
        paint.isAntiAlias = true
        paint.textAlign = Paint.Align.CENTER
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
        paint.style = Paint.Style.STROKE
        val radius = ViewUnit.dp2px(2f)
        val padding = ViewUnit.dp2px(2f)
        val outer = RectF(padding.toFloat(), padding.toFloat(), (canvas.width - padding).toFloat(), (canvas.height - padding).toFloat())
        canvas.drawRoundRect(outer, radius.toFloat(), radius.toFloat(), paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
        val xPos = canvas.width / 2
        val yPos = (canvas.height / 2 - (paint.descent() + paint.ascent()) / 2).toInt()
        canvas.drawText(text, xPos.toFloat(), yPos.toFloat(), paint)
        return image
    }

    fun getRectedLetterImage(character: Char, w: Int, h: Int, color: Int): Bitmap {
        val image = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)
        val paint = Paint()
        paint.color = color
        val boldText = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.typeface = boldText
        paint.textSize = ViewUnit.dp2px(14f).toFloat()
        paint.isAntiAlias = true
        paint.textAlign = Paint.Align.CENTER
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)

        val radius = ViewUnit.dp2px(2f)

        val outer = RectF(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat())
        canvas.drawRoundRect(outer, radius.toFloat(), radius.toFloat(), paint)

        val xPos = canvas.width / 2
        val yPos = (canvas.height / 2 - (paint.descent() + paint.ascent()) / 2).toInt()

        paint.color = Color.WHITE
        canvas.drawText(character.toString(), xPos.toFloat(), yPos.toFloat(), paint)

        return image
    }

    /**
     * Creates a rounded square of a certain color with
     * a character imprinted in white on it.
     *
     * @param character the character to write on the image.
     * @param w     the width of the final image in dp mode.
     * @param h    the height of the final image in dp mode.
     * @param color     the background color of the rounded square.
     * @return a valid bitmap of a rounded square with a character on it.
     */
    fun getRoundLetterImage(character: Char, w: Float, h: Float, color: Int): Bitmap =
            getRectedLetterImage(character, ViewUnit.dp2px(w), ViewUnit.dp2px(h), color)

    fun getRoundNumberImage(number: Int, redis: Float, color: Int, textSize: Float): Bitmap {
        val text = if (number > 99) "\u221E" else number.toString()
        val image = Bitmap.createBitmap(ViewUnit.dp2px(redis * 2), ViewUnit.dp2px(redis * 2), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)
        val paint = Paint()
        paint.color = color
        val boldText = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.typeface = boldText
        paint.isAntiAlias = true
        paint.textAlign = Paint.Align.CENTER
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
        paint.style = Paint.Style.FILL
        val r = canvas.width / 2.0f
        canvas.drawCircle(r, r, r, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)

        paint.textSize = ViewUnit.dp2px(textSize).toFloat()
        paint.color = Color.WHITE
        val xPos = canvas.width / 2
        val yPos = (canvas.height / 2 - (paint.descent() + paint.ascent()) / 2).toInt()
        canvas.drawText(text, xPos.toFloat(), yPos.toFloat(), paint)
        return image
    }

    /**
     * Hashes a character to one of four colors:
     * blue, green, red, or orange.
     *
     * @param character the character to hash.
     * @param app       the application needed to get the color.
     * @return one of the above colors, or black something goes wrong.
     */
    @ColorInt
    fun characterToColorHash(character: Char, app: Application): Int {
        val smallHash = Character.getNumericValue(character) % 4
        return when (Math.abs(smallHash)) {
            0 -> ContextCompat.getColor(app, com.fula.yohee.R.color.default_blue)
            1 -> ContextCompat.getColor(app, com.fula.yohee.R.color.default_green)
            2 -> ContextCompat.getColor(app, com.fula.yohee.R.color.default_red)
            3 -> ContextCompat.getColor(app, com.fula.yohee.R.color.default_orange)
            else -> Color.BLACK
        }
    }

    fun strColor(color: Int): String {
        val startA = color shr 24 and 0xff
        val startR = color shr 16 and 0xff
        val startG = color shr 8 and 0xff
        val startB = color and 0xff
        return "0x${Integer.toHexString(startA)}${Integer.toHexString(startR)}${Integer.toHexString(startG)}${Integer.toHexString(startB)}"
    }

    /**判断是否厚重色彩*/
    fun isThickColor(color: Int): Boolean {
        val thread = 0.45f
        val threadUp = 0.55f
        val r = (color shr 16 and 0xff) / 255f
        val g = (color shr 8 and 0xff) / 255f
        val b = (color and 0xff) / 255f
        CLog.i("r = $r, g = $g, b = $b")
        if (r + g + b < 3 * thread) return true
        if (r < thread && (g > threadUp || b > threadUp)) return true
        if (g < thread && (r > threadUp || b > threadUp)) return true
        if (b < thread && (r > threadUp || g > threadUp)) return true
        return false
    }

    /**反转色彩*/
    fun invertColor(color: Int): Int {
        val r = 255 - (color shr 16 and 0xff)
        val g = 255 - (color shr 8 and 0xff)
        val b = 255 - (color and 0xff)
        return Color.rgb(r, g, b)
    }

    fun mixColor(fraction: Float, startValue: Int, endValue: Int): Int {
        val startA = startValue shr 24 and 0xff
        val startR = startValue shr 16 and 0xff
        val startG = startValue shr 8 and 0xff
        val startB = startValue and 0xff

        val endA = endValue shr 24 and 0xff
        val endR = endValue shr 16 and 0xff
        val endG = endValue shr 8 and 0xff
        val endB = endValue and 0xff

        return startA + (fraction * (endA - startA)).toInt() shl 24 or (
                startR + (fraction * (endR - startR)).toInt() shl 16) or (
                startG + (fraction * (endG - startG)).toInt() shl 8) or
                startB + (fraction * (endB - startB)).toInt()
    }

    fun drawable2Bitmap(drawable: Drawable): Bitmap = when (drawable) {
        is BitmapDrawable -> drawable.bitmap
        else -> {
            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight,
                    if (drawable.opacity != PixelFormat.OPAQUE)
                        Bitmap.Config.ARGB_8888
                    else
                        Bitmap.Config.RGB_565)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, drawable.intrinsicWidth,
                    drawable.intrinsicHeight)
            drawable.draw(canvas)
            bitmap
        }
    }

    fun txt2Bitmap(txt: String, @ColorInt c: Int): Bitmap {
        val p = Paint().apply {
            color = c
            typeface = Typeface.create("normal", Typeface.ITALIC)
            textSize = ViewUnit.dp2px(20f).toFloat()
            textAlign = Paint.Align.LEFT
        }

        val rect = Rect()
        p.getTextBounds(txt, 0, txt.length, rect)
        val bmp = Bitmap.createBitmap((rect.width() * 1.1f).toInt(), (rect.height() * 1.2f).toInt(), Bitmap.Config.ARGB_8888)
        Canvas(bmp).apply {
            drawColor(Color.TRANSPARENT)
            val paddingBottom = (bmp.height - rect.height()) * 2 / 3f
            drawText(txt, 0f, bmp.height - paddingBottom, p)
        }
        return bmp
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
    fun getThemedBitmap(context: Context, @DrawableRes res: Int): Bitmap {
        val sourceBitmap = ThemeUtils.getBitmapFromVectorDrawable(context, res)
        val resultBitmap = Bitmap.createBitmap(sourceBitmap.width, sourceBitmap.height,
                Bitmap.Config.ARGB_8888)
        val p = Paint()
//        val filter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
//        p.colorFilter = filter
        val canvas = Canvas(resultBitmap)
        canvas.drawBitmap(sourceBitmap, 0f, 0f, p)
        sourceBitmap.recycle()
        return resultBitmap
    }


}
