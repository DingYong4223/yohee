package com.fula.yohee.extensions

import android.text.TextUtils
import com.fula.yohee.BuildConfig
import org.json.JSONObject
import java.io.Closeable

/**
 * @author: delanding
 */
inline fun <T : Closeable?, R> T.tryUse(block: (T) -> R): R? {
    return try {
        use(block)
    } catch (e: Throwable) {
        e.printStackTrace()
        null
    }
}

inline fun String.tryJson(block: (JSONObject) -> Any) {
    if (!TextUtils.isEmpty(this)) {
        try {
            block(JSONObject(this))
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}

inline fun tryCatch(block: () -> Unit) {
    try {
        block()
    } catch (t: Throwable) {
        if (BuildConfig.DEBUG) t.printStackTrace()
    }
}

fun Boolean.toInt() = if (this) 1 else 0
fun Int.toBoolean() = this >= 1
fun Int.toBoolean(num: Int) = this >= num
