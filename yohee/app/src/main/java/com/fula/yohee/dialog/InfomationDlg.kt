package com.fula.yohee.dialog

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.view.setPadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fula.base.BaseRecycleAdapter
import com.fula.util.ViewUnit
import com.fula.yohee.R
import com.fula.yohee.extensions.color
import com.fula.yohee.ui.bilogic.SelectModel
import com.fula.yohee.utils.DeviceUtils
import kotlinx.android.synthetic.main.info_list_item.view.*
import kotlinx.android.synthetic.main.layout_recycle_title.*

class InfomationDlg(context: Activity
                    , @StringRes val titleRes: Int
                    , val fileName: String? = null
                    , private val models: List<SelectModel>
                    , private val item1: DialogItem
                    , private val item2: DialogItem? = null
                    , private val item3: DialogItem? = null) : BaseDialog(context, R.style.AlertDialogStyle) {

    private val adapter: InfomAdapter by lazy { InfomAdapter(context) }

    override fun layoutParams(window: Window): WindowManager.LayoutParams {
        var attr = window.attributes
        attr.gravity = Gravity.CENTER
        attr.width = DeviceUtils.getScreenRateWidth(context, 0.8f)
        attr.height = DeviceUtils.getScreenRateHeight(context, 0.65f)
        return attr
    }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.layout_recycle_title)
        window?.let {
            it.setBackgroundDrawableResource(R.color.defcolorPrimaryTrns)
        }
        initView()
    }

    private fun initView() {
        dlg_title.setText(titleRes)
        fileName?.let {
            file_name.visibility = View.VISIBLE
            file_name.text = it
        }
        dlg_recycle.let {
            it.layoutManager = LinearLayoutManager(context)
            it.isLongClickable = false
            it.adapter = adapter.apply { initData(models) }
        }

        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(context.color(R.color.defcolorPrimaryTrns))
            addActionItem(this, item1)
            addActionItem(this, item2)
            addActionItem(this, item3)
        }
        layout_root.addView(content, LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(0, ViewUnit.dp2px(15f), 0, 0)
        })
    }

    private fun addActionItem(layout: LinearLayout, item: DialogItem?) {
        val lp = LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(0, ViewUnit.dp2px(5f), 0, 0)
        }
        item?.let {
            layout.addView(TextView(context).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                setText(it.title)
                setPadding(ViewUnit.dp2px(8f))
                setBackgroundResource(R.drawable.trans_list_item_style)
                setOnClickListener {
                    dismiss()
                    item.onClick()
                }
            }, lp)
        }
    }

    inner class InfomAdapter(mContext: Context)
        : BaseRecycleAdapter<SelectModel, InfomAdapter.InfoViewHolder>(mContext) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoViewHolder = InfoViewHolder(inflater.inflate(R.layout.info_list_item, parent, false))
        override fun onBindViewHolder(holder: InfoViewHolder, position: Int) = holder.bindData(data[position], position)

        inner class InfoViewHolder(val cv: View) : RecyclerView.ViewHolder(cv) {

            fun bindData(item: SelectModel, position: Int) {
                cv.item_title.text = "${item.title}:"
                cv.item_content.text = item.urlTxt
            }
        }

    }

}
