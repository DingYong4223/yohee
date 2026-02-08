package com.fula.base

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import com.fula.yohee.extensions.getBitmap
import com.fula.yohee.extensions.tryCatch
import com.fula.CLog
import com.fula.yohee.uiwigit.glide.FastBlur
import io.reactivex.Maybe
import io.reactivex.Scheduler
import io.reactivex.Single
import java.io.File
import java.io.FileOutputStream

class BitmapStrategy {

    companion object {

        @JvmStatic
        fun  getBlurCacheBitmap(context: Context, diskScheduler: Scheduler, @DrawableRes res: Int, sampling: Int, redus: Int): Maybe<Bitmap> = Maybe.fromCallable {
            CLog.i("get blur bitmap...")
            val md5Name = ToolUtils.md5("blur_${res}_${sampling}_$redus")
            val cache = File(context.cacheDir.path + File.separator + md5Name)
            synchronized(BitmapStrategy::class.java) {
                if (cache.exists()) {
                    tryCatch {
                        CLog.i("get bitmap from disk cache...")
                        return@fromCallable BitmapFactory.decodeFile(cache.absolutePath)
                    }
                }
            }
            CLog.i("get bitmap and cache to disk...")
            val bitmap = context.getBitmap(res)
            val blurBitmap = FastBlur.transform(bitmap, sampling, redus)
            bitmap.recycle()
            Single.fromCallable {
                synchronized(BitmapStrategy::class.java) {
                    CLog.i("write to disk begin...")
                    tryCatch {
                        val out = FileOutputStream(cache)
                        blurBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        out.flush()
                        out.close()
                        CLog.i("save bitmap suc...")
                    }
                }
            }.subscribeOn(diskScheduler)
                    .subscribe()
            return@fromCallable blurBitmap
        }
    }

}