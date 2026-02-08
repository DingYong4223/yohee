package com.fula.yohee.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import androidx.core.content.ContextCompat
import com.fula.yohee.R

/**
 * Create a new transition drawable with the specified list of layers. At least
 * 2 layers are required for this drawable to work properly.
 */
class BackgroundDrawable(
    context: Context
) : TransitionDrawable(
    arrayOf<Drawable>(
        ColorDrawable(Color.TRANSPARENT),
        ColorDrawable(Color.RED)
    )
) {

    private var isSelected: Boolean = false

    override fun startTransition(durationMillis: Int) {
        if (!isSelected) {
            super.startTransition(durationMillis)
        }
        isSelected = true
    }

    override fun reverseTransition(duration: Int) {
        if (isSelected) {
            super.reverseTransition(duration)
        }
        isSelected = false
    }

}
