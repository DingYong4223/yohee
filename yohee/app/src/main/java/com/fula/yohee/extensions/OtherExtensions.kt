@file:JvmName("ActivityExtensions")

package com.fula.yohee.extensions

import android.app.Activity
import android.graphics.PointF
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import com.fula.CLog
import com.google.android.material.snackbar.Snackbar
import java.io.Closeable

fun Activity.snackbar(@StringRes resource: Int) {
    val view = findViewById<View>(android.R.id.content)
    Snackbar.make(view, resource, Snackbar.LENGTH_SHORT).show()
}

fun Activity.snackbar(@StringRes tostId: Int, @StringRes actionId: Int, actionCall: (View) -> Unit) {
    val view = findViewById<View>(android.R.id.content)
    Snackbar.make(view, tostId, Snackbar.LENGTH_LONG).setAction(actionId, actionCall).show()
}

fun Activity.shortToast(@StringRes res: Int) = shortToast(getString(res))
fun Activity.shortToast(tip: String) {
    Toast.makeText(applicationContext, tip, Toast.LENGTH_SHORT).show()
}

fun Activity.longToast(@StringRes res: Int) = longToast(getString(res))
fun Activity.longToast(res: String) {
    Toast.makeText(applicationContext, res, Toast.LENGTH_LONG).show()
}

fun Activity.snackbar(message: String) {
    val view = findViewById<View>(android.R.id.content)
    Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
}

fun Activity.longSnackbar(message: String) {
    val view = findViewById<View>(android.R.id.content)
    Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
}

inline fun <T : Closeable> T.safeUse(block: (T) -> Unit) {
    try {
        this.use(block)
    } catch (throwable: Throwable) {
        CLog.i("error, Unable to parse results")
    }
}

inline fun <T> T.execIf(predicate: (T) -> Boolean, exec: (T) -> Unit) = takeIf(predicate)?.let { exec(it) }

inline operator fun PointF.times(fraction: Float): PointF {
    return PointF(x, y).apply {
        x *= fraction
        y *= fraction
    }
}

inline operator fun PointF.compareTo(pointB: PointF): Int =
        if (this.x >= pointB.x && this.y >= pointB.y) 1 else -1

