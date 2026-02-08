package com.fula.yohee.view

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.animation.addListener
import androidx.core.graphics.minus
import androidx.core.graphics.plus
import com.fula.yohee.extensions.times


/**
 * Created by Carson_Ho on 17/4/18.
 */
class FullAnim @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {

    private var type = ANIM_TYPE_TRANSLATE
    private var imgRotation = 0f
    private var scale: Float = 1f
    private val bitmapMatrx: Matrix = Matrix()
    private var img: Bitmap? = null
    private var config: AnimConfig = AnimConfig()

    private val mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLUE
    }

    private fun transAnim(bitmap: Bitmap, from: PointF, toPointFun: () -> PointF, scaleFT: PointF = PointF(1f, 1f), span: Long, inter: TimeInterpolator, comlete: (() -> Unit)?) {
        this.img = bitmap
        type = ANIM_TYPE_TRANSLATE
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = span
            interpolator = inter
            addUpdateListener { animation ->
                val fraction = animation.animatedValue as Float
                config.centerPoint = from + (toPointFun() - from) * fraction
                scale = scaleFT.x + (scaleFT.y - scaleFT.x) * fraction
                postInvalidate()
            }
            addListener(onEnd = {
                comlete?.invoke()
            })
        }.start()
    }

    fun transAnim(bitmap: Bitmap, from: PointF, to: PointF, scaleFT: PointF = PointF(1f, 1f), span: Long, inter: TimeInterpolator, comlete: (() -> Unit)?) {
        this.transAnim(bitmap, from, {to}, scaleFT, span, inter, comlete)
    }

    fun scaleAnim(bitmap: Bitmap, config: AnimConfig, scaleFT: PointF = PointF(1f, 0f), span: Long, inter: TimeInterpolator, comlete: (() -> Unit)?) {
        this.img = bitmap
        type = ANIM_TYPE_SCALE
        this.config = config
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = span
            interpolator = inter
            addUpdateListener { va ->
                val fraction = va.animatedValue as Float
                scale = scaleFT.x + (scaleFT.y - scaleFT.x) * fraction
                postInvalidate()
            }
            addListener(onEnd = {
                comlete?.invoke()
            })
        }.start()
    }

    private var startTime: Long = 0L
    /**抖动效果，dstRect参数left表示bitmap展示的x坐标，top为y坐标
     * @param bitmap 要展示的bitmap图
     * @param centerPoint 围绕着shake的中心店
     * @param initScale bitmap的缩放系数
     * @param shakeScale bitmap在shake过程中的缩放系数
     * @param span 动画持续时间*/
    fun shakeAnim(bitmap: Bitmap, config: AnimConfig, initScale: Float, shakeScale: Float = 0f, span: Long, inter: TimeInterpolator, comlete: (() -> Unit)?) {
        this.img = bitmap
        type = ANIM_TYPE_SHAKE
        this.scale = initScale
        val offLeft = 3f * SHAKEFACTOR
        this.config = config
        startTime = System.currentTimeMillis()
        ValueAnimator.ofFloat(0f, offLeft, -offLeft, offLeft, -offLeft, offLeft, -offLeft, offLeft, -offLeft, offLeft, 0f).apply {
            duration = span
            interpolator = inter
            addUpdateListener { va ->
                imgRotation = va.animatedValue as Float
                val timeBy = Math.abs(((System.currentTimeMillis() - startTime).toFloat() / span) - 0.5f)
                scale = initScale * (1 + shakeScale * timeBy)

                postInvalidate()
            }
            addListener(onEnd = {
                comlete?.invoke()
            })
        }.start()
    }

    override fun onDraw(canvas: Canvas) {
        img?.let {
            val offsetX = it.width / 2
            val offsetY = it.height / 2
            bitmapMatrx.apply {
                reset()
                postTranslate(-offsetX.toFloat(), -offsetY.toFloat())
                postRotate(imgRotation)
                postScale(scale, scale)
                postTranslate(config.centerPoint.x + offsetX, config.centerPoint.y + offsetY)
                canvas.drawBitmap(it, this, mPaint.apply { alpha = config.alpha })
            }
        }
    }

    companion object {
        private const val ANIM_TYPE_TRANSLATE = 0
        private const val ANIM_TYPE_SCALE = 1
        private const val ANIM_TYPE_SHAKE = 2

        private const val SHAKEFACTOR = 5f
    }
}

/**apha:透明度（0 ~ 255）*/
class AnimConfig(val alpha: Int = 255, val scale: Float = 1f, var centerPoint: PointF = PointF(0f, 0f))
