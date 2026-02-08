package com.fula.yohee.dialog

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.fula.yohee.R
import com.fula.yohee.utils.DeviceUtils
import kotlinx.android.synthetic.main.dlg_bottom_menu.*
import kotlinx.android.synthetic.main.layout_menu_content.*

class DialogMemuContent(context: Activity
                        , @StringRes val titleRes: Int
                        , @StringRes val menuLeft: MenuAction
                        , @StringRes val menuRight: MenuAction
                        , @DrawableRes val contentView: View
                        , val listener: ((index: Int) -> Unit)?) : BaseDialog(context, R.style.AlertDialogStyle) {

    override fun layoutParams(window: Window): WindowManager.LayoutParams {
        var attr = window.attributes
        attr.gravity = Gravity.CENTER
        attr.width = DeviceUtils.getScreenRateWidth(context, 0.8f)
        attr.height = DeviceUtils.getScreenRateHeight(context, 0.65f)
        return attr
    }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.layout_menu_content)
        initView()
    }

    private fun initView() {
        dlg_title.setText(titleRes)
        content_holder.apply {
            addView(contentView, LinearLayout.LayoutParams(-1, -1))
        }
        txt_back.apply {
            setText(menuLeft.strRes)
            setOnClickListener {
                listener?.invoke(MENU_LEFT)
                if (menuLeft.finishClick) dismiss()
            }
        }
        txt_menu.apply {
            setText(menuRight.strRes)
            setOnClickListener {
                listener?.invoke(MENU_RIGHT)
                if (menuRight.finishClick) dismiss()
            }
        }
    }

    companion object {
        const val MENU_LEFT = 0
        const val MENU_RIGHT = 1
    }

}

class MenuAction(@StringRes val strRes: Int, val finishClick: Boolean)
