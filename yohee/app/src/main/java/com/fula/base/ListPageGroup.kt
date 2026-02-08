package com.fula.base

import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import com.fula.yohee.R
import com.fula.CLog

/**
 * the group showListDialog in list page
 *
 * @author delanding
 */
class ListPageGroup(@StringRes val group_title: Int?, val items: List<ListPageItem>) {

    var group_desc: String? = null
    var group_visible = View.VISIBLE

    class ListPageItem(val index: Int, @StringRes var keyRes: Int = View.NO_ID, var item_value: String? = "") {

        constructor(index: Int, keyStr: String = "", item_value: String? = ""): this(index, View.NO_ID, item_value) {
            this.keyStr = keyStr
        }

        var keyStr: String = ""
        var view: View? = null
        var resID: Int? = null
        var intValue: Int? = -1
        var lp: LinearLayout.LayoutParams? = null

        /**
         * 设置Item_value的值
         */
        fun setUIValue(value: String?, hint: Boolean) {
            val txtValue = view?.findViewById<TextView>(R.id.item_value)?.apply {
                visibility = if (null == value) View.GONE else View.VISIBLE
            }
            if (TextUtils.isEmpty(value)) {
                CLog.i("no value set...")
                return
            }
            item_value = value
            if (value == DEF_ITEM_VALUE && txtValue is TextView) {
                setTvText(txtValue, value, hint)
                return
            }
        }

        private fun setTvText(tv: TextView, text: String, hint: Boolean) {
            if (hint) {
                tv.hint = text
            } else {
                tv.text = text
            }
        }
    }

    fun getItem(index: Int): ListPageItem {
        return items[index]
    }

    companion object {
        const val DEF_ITEM_VALUE = "未填写"
    }

}
