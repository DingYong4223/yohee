package com.fula.yohee.ui.bilogic

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.fula.base.BaseRecycleAdapter
import com.fula.yohee.R
import com.fula.CLog
import com.fula.yohee.ui.common.EditMenuHelper
import kotlinx.android.synthetic.main.com_list_item.view.*

open class SelectAdapter(mContext: Context,
                         private val longClickListener: ((Int) -> Boolean)? = null,
                         private val clickListener: ((View, Int) -> Unit)? = null,
                         var initVisit: ((View) -> Unit)? = null,
                         var bindVisit: ((View, SelectModel) -> Unit)? = null,
                         var selectListener: ((List<SelectModel>) -> Unit)? = null)
    : BaseRecycleAdapter<SelectModel, SelectAdapter.ViewHolder>(mContext) {

    var editHelper: EditMenuHelper? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(inflater.inflate(R.layout.com_list_item, parent, false))
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bindData(data[position], position)

    inner class ViewHolder(val cv: View) : RecyclerView.ViewHolder(cv), View.OnClickListener, View.OnLongClickListener {

        init {
            initVisit?.invoke(cv)
            cv.item_logo_img.visibility = View.GONE
            cv.setOnLongClickListener(this)
            cv.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val index = adapterPosition
            if (index.toLong() != RecyclerView.NO_ID) {
                if (editHelper?.isEdit == true) cv.item_check.performClick()
                clickListener?.invoke(v, index)
            }
        }

        override fun onLongClick(v: View): Boolean {
            val index = adapterPosition
            return index != RecyclerView.NO_POSITION && (longClickListener?.invoke(index) == true)
        }

        fun bindData(item: SelectModel, position: Int) {
            cv.item_check.apply {
                visibility = if (editHelper?.isEdit == true) View.VISIBLE else View.GONE
                isChecked = item.isCheck
                setOnClickListener {
                    it as CheckBox
                    if (position < data.size) itemAt(position).isCheck = it.isChecked
                }
                setOnCheckedChangeListener { checkView, checked ->
                    CLog.i("checked changed: $checked")
                    if (position < data.size) itemAt(position).isCheck = checkView.isChecked
                    selectListener?.invoke(data)
                }
            }
            itemView.jumpDrawablesToCurrentState()
            cv.item_title.text = item.title
            cv.item_subtitle.text = item.urlTxt
            iconLazyLoad(cv, item)
            bindVisit?.invoke(cv, item)
        }
    }

    open fun iconLazyLoad(cv: View, model: SelectModel) {
        if (model.icon != null || model.iconRes != null) {
            cv.item_logo_img.visibility = View.VISIBLE
            model.iconRes?.let {
                cv.item_logo_img.setImageResource(it)
            }
            model.icon?.let {
                cv.item_logo_img.setImageBitmap(it)
            }
        } else {
            cv.item_logo_img.visibility = View.GONE
        }
        if (model.afterIconRes != null) {
            cv.item_after_img.visibility = View.VISIBLE
            cv.item_after_img.setImageResource(model.afterIconRes!!)
        } else {
            cv.item_after_img.visibility = View.GONE
        }
    }
}

class SelectModel(val obj: Any?, val title: String, var urlTxt: String, var icon: Bitmap? = null, isCheck: Boolean = false, var iconRes: Int? = null, var afterIconRes: Int? = null) : EditMenuHelper.CheckModel(isCheck) {

    var infoArg: Any? = null
    override fun equals(other: Any?): Boolean {
        return if (other is SelectModel) {
            obj == other.obj
        } else {
            super.equals(other)
        }
    }

    override fun hashCode(): Int = obj.hashCode()
    override fun toString(): String = "SelectModel(obj=$obj)"

}