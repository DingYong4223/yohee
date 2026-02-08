package com.fula.yohee.extensions

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import com.fula.yohee.R

/**
 * Copies the [text] to the clipboard with the label `URL`.
 */
fun ClipboardManager.copy(text: String) {
    primaryClip = ClipData.newPlainText("URL", text)
}

fun ClipboardManager.copyAndTip(context: Activity, text: String) {
    primaryClip = ClipData.newPlainText("URL", text)
    context.shortToast(context.getString(R.string.copyed_to_clip, text))
}
