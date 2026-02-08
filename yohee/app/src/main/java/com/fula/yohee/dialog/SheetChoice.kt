package com.fula.yohee.dialog

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fula.base.BaseRecycleAdapter
import com.fula.util.ViewUnit
import com.fula.yohee.R
import com.fula.yohee.ui.activity.WebActivity
import com.fula.yohee.uiwigit.floatmenu.FloatMenu
import com.fula.yohee.uiwigit.floatmenu.FloatMenuItem
import com.fula.yohee.uiwigit.floatmenu.FloatMenuParser
import com.fula.yohee.utils.DeviceUtils
import kotlinx.android.synthetic.main.dlg_sheet_choice.*

class SheetChoice(context: WebActivity
                  , val title: String?
                  , private val menuRes: Int
                  , val selects: List<Int>?
                  , private val listener: FloatMenu.OnItemClickListener) : BaseDialog(context, R.style.SheetDialogStyle) {

    private val list: MutableList<FloatMenuItem> = mutableListOf()
    private val adapter: SheetAdapter by lazy { SheetAdapter(context) }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.dlg_sheet_choice)
        FloatMenuParser(context, list).parseMenu(menuRes)
        initView(title)
    }

    override fun layoutParams(window: Window): WindowManager.LayoutParams {
        var attr = window.attributes
        attr.gravity = Gravity.BOTTOM
        attr.width = DeviceUtils.getScreenWidth(context)
        return attr
    }

    private fun initView(title: String?) {
        if (TextUtils.isEmpty(title)) {
            sheet_title.visibility = View.GONE
        } else {
            sheet_title.text = title
        }
        recycle_view.layoutManager = LinearLayoutManager(context)
        recycle_view.adapter = adapter.apply { updateData(list) }
    }

    inner class SheetAdapter(context: Context) : BaseRecycleAdapter<FloatMenuItem, SheetAdapter.ViewHolder>(context) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(inflater.inflate(R.layout.item_recycle_text_view, parent, false))
        override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bindData(mContext, data[position])

        inner class ViewHolder(cv: View) : RecyclerView.ViewHolder(cv), View.OnClickListener {

            init {
                cv.setOnClickListener(this)
            }

            override fun onClick(v: View?) {
                val index = adapterPosition
                if (index.toLong() != RecyclerView.NO_ID) {
                    listener.onClick(v!!, adapter.itemAt(index), selects?.contains(adapter.itemAt(index).id))
                    dismiss()
                }
            }

            private val item_content: TextView = cv as TextView
            private val selDrawable = ContextCompat.getDrawable(context, R.drawable.ic_right)!!.apply {
                setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            }

            fun bindData(context: Context, info: FloatMenuItem) {
                val drawableLeft = if (info.itemResId != View.NO_ID) ContextCompat.getDrawable(context, info.itemResId)!!.apply { setBounds(0, 0, intrinsicWidth, intrinsicHeight) } else null
                val drawableRight = if (null != selects && selects.contains(info.id)) selDrawable else null

                item_content.compoundDrawablePadding = ViewUnit.dp2px(12f)
                item_content.setCompoundDrawables(drawableLeft, null, drawableRight, null)
                item_content.text = info.item
            }
        }
    }

}
