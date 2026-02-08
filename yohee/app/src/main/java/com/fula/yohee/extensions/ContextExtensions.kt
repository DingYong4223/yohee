@file:Suppress("NOTHING_TO_INLINE")

package com.fula.yohee.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.*
import androidx.core.content.ContextCompat

/**
 * Returns the dimension in pixels.
 *
 * @param dimenRes the dimension resource to fetch.
 */
inline fun Context.dimen(@DimenRes dimenRes: Int): Int = resources.getDimensionPixelSize(dimenRes)

/**
 * Returns the [ColorRes] as a [ColorInt]
 */
@ColorInt
inline fun Context.color(@ColorRes colorRes: Int): Int = ContextCompat.getColor(this, colorRes)

/**
 * Shows a toast with the provided [StringRes].
 */
inline fun Context.toast(@StringRes stringRes: Int) = Toast.makeText(applicationContext, stringRes, Toast.LENGTH_SHORT).show()

/**
 * The [LayoutInflater] available on the [Context].
 */
inline val Context.inflater: LayoutInflater
    get() = LayoutInflater.from(this)

/**
 * Gets a drawable from the context.
 */
inline fun Context.drawable(@DrawableRes drawableRes: Int): Drawable = ContextCompat.getDrawable(this, drawableRes)!!

//bindView's extension
@Suppress("UNCHECKED_CAST")
fun <V : View> Activity.bindView(id: Int): Lazy<V> = lazy {
    viewFinder(id) as V
}

private val viewFinder: Activity.(Int) -> View?
    get() = { findViewById(it) }

inline fun Context.getNullableString(@StringRes stringRes: Int): String = if (View.NO_ID != stringRes) {
    getString(stringRes)
} else {
    ""
}

inline fun Context.getOptionableString(@StringRes stringRes: Int, def: String): String = if (View.NO_ID != stringRes) {
    getString(stringRes)
} else {
    def
}

inline fun Context.getAppColor(@ColorRes id: Int): Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    this.getColor(id)
} else {
    this.resources.getColor(id)
}

fun Context.getBitmap(@DrawableRes res: Int): Bitmap = BitmapFactory.decodeResource(resources, res)

fun Activity.tryShare(goUrl: String, share: () -> Unit) {
    try{
        share()
    } catch (e: Exception) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(goUrl)))
    }
}
