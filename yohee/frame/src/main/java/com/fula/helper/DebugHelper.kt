package com.fula.helper

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import com.fula.CLog
import com.fula.frame.BuildConfig
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

object DebugHelper {

    fun saveBitmapImage(bm: Bitmap) {
        if (BuildConfig.DEBUG) {
            CLog.i("保存图片")
            val f = File(Environment.getExternalStorageDirectory(), "$bm.png")
            if (f.exists()) {
                f.delete()
            }
            try {
                val out = FileOutputStream(f)
                bm.compress(Bitmap.CompressFormat.PNG, 90, out)
                out.flush()
                out.close()
                CLog.i("已经保存")
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun notifyFileChanged(context: Context, path: String) {
        if (BuildConfig.DEBUG) {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val contentUri = Uri.parse (path)
            mediaScanIntent.data = contentUri
            context.sendBroadcast(mediaScanIntent)
        }
    }

}
