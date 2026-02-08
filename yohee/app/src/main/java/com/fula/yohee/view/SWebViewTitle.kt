package com.fula.yohee.view

import android.content.Context
import android.graphics.Bitmap
import com.fula.yohee.R
import com.fula.yohee.extensions.pad
import com.fula.yohee.utils.DrawableUtils

class SWebViewTitle(private val context: Context) {

    private var favicon: Bitmap? = null
    var title = context.getString(R.string.action_new_tab)

    fun setFavicon(favicon: Bitmap?) {
        this.favicon = favicon?.pad()
    }

    fun getFavicon(): Bitmap = favicon ?: getDefaultIcon(context)

    companion object {
        private var defaultLightIcon: Bitmap? = null
        private fun getDefaultIcon(context: Context): Bitmap {
            var lightIcon = defaultLightIcon
            if (lightIcon == null) {
                lightIcon = DrawableUtils.getThemedBitmap(context, R.drawable.ic_webpage)
                defaultLightIcon = lightIcon
            }
            return lightIcon
        }

    }
}
