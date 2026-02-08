package com.fula.yohee.dialog

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.fula.yohee.R
import com.fula.yohee.ui.activity.WebActivity
import com.fula.yohee.ui.bilogic.SelectAdapter
import com.fula.yohee.ui.bilogic.SelectModel
import com.fula.yohee.utils.DeviceUtils
import kotlinx.android.synthetic.main.com_list_item.view.*
import kotlinx.android.synthetic.main.dlg_bottom_menu.*
import kotlinx.android.synthetic.main.layout_recycle_title.*

class SheetList(context: WebActivity
                , val title: Int?
                , val menuDeleteRes: Int?
                , val menuRes: Int
                , private val models: List<SelectModel>
                , val clickL: (View, SelectModel?) -> Unit) : BaseDialog(context, R.style.SheetDialogStyle) {

    private val adapter: SelectAdapter by lazy { SelectAdapter(context, clickListener = ::itemClick, bindVisit = ::bindVisit) }

    override fun layoutParams(window: Window): WindowManager.LayoutParams {
        var attr = window.attributes
        attr.gravity = Gravity.BOTTOM
        attr.width = DeviceUtils.getScreenWidth(context)
        attr.height = DeviceUtils.getScreenHeight(context) * 2 / 3
        return attr
    }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.layout_menu_recycle)
        initView(title)
    }

    private fun initView(title: Int?) {
        if (null == title) {
            dlg_title.visibility = View.GONE
        } else {
            dlg_title.setText(title)
        }
        dlg_recycle.let {
            it.layoutManager = LinearLayoutManager(context)
            it.isLongClickable = true
            it.adapter = adapter.apply { initData(models) }
        }
        txt_back.setOnClickListener(::onClick)
        menuDeleteRes?.let {
            txt_delete.apply {
                visibility = View.VISIBLE
                setText(it)
                setOnClickListener(::onClick)
            }
        }
        txt_menu.apply {
            setText(menuRes)
            setOnClickListener(::onClick)
        }
    }

    private fun itemClick(v: View, index: Int) {
        clickL(v, adapter.itemAt(index))
        dismiss()
    }

    private fun onClick(v: View) {
        clickL(v, null)
        dismiss()
    }

    private fun bindVisit(cv: View, model: SelectModel) {
        cv.item_after_img.setOnClickListener {
            clickL(it, model)
            dismiss()
        }
    }

}
