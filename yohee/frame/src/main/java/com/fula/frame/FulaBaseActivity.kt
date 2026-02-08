package com.fula.frame

import android.graphics.Point
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity



abstract class FulaBaseActivity : AppCompatActivity() {

    val touchPoint = Point()

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            touchPoint.x = ev.rawX.toInt()
            touchPoint.y = ev.rawY.toInt()
        }
        return super.dispatchTouchEvent(ev)
    }

}
