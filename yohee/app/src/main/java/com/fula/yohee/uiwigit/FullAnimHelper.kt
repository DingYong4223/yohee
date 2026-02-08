package com.fula.yohee.uiwigit

import android.graphics.Bitmap
import android.graphics.PointF
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.graphics.minus
import com.fula.CLog
import com.fula.yohee.R
import com.fula.yohee.extensions.center
import com.fula.yohee.extensions.removeFromParent
import com.fula.yohee.ui.activity.BaseActivity
import com.fula.yohee.utils.DeviceUtils
import com.fula.yohee.view.AnimConfig
import kotlinx.android.synthetic.main.layout_full_anim.view.*

class FullAnimHelper(val mContext: BaseActivity) {

    private val decorView: FrameLayout by lazy { mContext.window.decorView as FrameLayout }
    private val fullLayout: View by lazy { LayoutInflater.from(mContext).inflate(R.layout.layout_full_anim, decorView, true) }

    /**背后打开url新页面*/
    fun animBackUrlOpen(bitmap: Bitmap, from: PointF, to: PointF, span: Long) {
        fullLayout.apply {
            val halfBitmap = PointF(bitmap.width.toFloat() / 2, bitmap.height.toFloat() / 2)
            full_anim_view.transAnim(bitmap, from - halfBitmap, to - halfBitmap, PointF(1f, 1f), span, AnticipateOvershootInterpolator()/*BounceInterpolator()*/) {
                val config = AnimConfig(alpha = 125, centerPoint = to - halfBitmap)
                full_anim_view.scaleAnim(bitmap, config, PointF(1f, 0f), span / 2, AccelerateInterpolator()) {
                    bitmap.recycle()
                    full_anim_view.removeFromParent()
                }
            }
        }
    }

//    fun animBackUrlOpen(bitmap: Bitmap, fromV: View, toV: View, span: Long) {
//        fullLayout.apply {
//            animBackUrlOpen(bitmap, fromV.center(), toV.center(), span)
//        }
//    }

    fun animBackUrlOpen(bitmap: Bitmap, from: PointF, toV: View, span: Long) {
        fullLayout.apply {
            animBackUrlOpen(bitmap, from, toV.center(), span)
        }
    }

    fun animSSLWarning(bitmap: Bitmap, toPointFun: (PointF) -> PointF, offPoint: PointF, span: Long, recycle: Boolean = true, endListener: (() -> Unit)? = null) {
        fullLayout.apply {
            val center = DeviceUtils.getScreenCenter(mContext) - PointF(bitmap.width.toFloat() / 2, bitmap.height.toFloat() / 2)
            val config = AnimConfig(alpha = 125, centerPoint = center)
            full_anim_view.shakeAnim(bitmap, config, 3f, 2f, span / 2, DecelerateInterpolator()) {
                CLog.i("shake complete...")
                val half = PointF(bitmap.width.toFloat() / 2, bitmap.height.toFloat() / 2)
                full_anim_view.transAnim(bitmap, center, toPointFun(offPoint) - half, PointF(3f, 1f), span, DecelerateInterpolator()) {
                    CLog.i("trans complete...")
                    full_anim_view.removeFromParent()
                    if (recycle) {
                        bitmap.recycle()
                        //full_anim_view.recycleImg()
                    }
                    endListener?.invoke()
                }
            }
        }
    }

}