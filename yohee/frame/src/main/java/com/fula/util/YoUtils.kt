package com.fula.util

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream

object YoUtils {

    fun saveBitmap2File(bm: Bitmap, path: String, fileName: String): File {
        val f = File(path, fileName)
        if (f.exists()) f.delete()
        FileOutputStream(f).use {
            bm.compress(Bitmap.CompressFormat.PNG, 90, it)
            it.flush()
        }
        return f
    }

    /**
     * 获取应用包名
     */
    @JvmStatic
    fun getPackageName(context: Context): String {
        try {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(
                    context.packageName, 0)
            return packageInfo.packageName
        } catch (e: Exception) {
            e.printStackTrace()
            return "com.fula.yohee"
        }
    }

    fun isZh(context: Context): Boolean {
        val locale = context.resources.configuration.locale
        return locale.language.endsWith("zh")
    }

}