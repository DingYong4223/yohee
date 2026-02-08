package com.fula.base

import android.app.Activity
import android.view.View
import android.widget.LinearLayout
import com.fula.yohee.R
import com.fula.CLog

/**
 * Class used for user info showListDialog or user info edit.
 *
 * @author delanding
 */

abstract class BaseInfoAdapter(protected val mContext: Activity, protected val pView: LinearLayout, protected val listener: ListPageAdapter.ItemListener, private val vl: View.OnClickListener?) {

    val group: List<ListPageGroup> by lazy { listGroup(vl) }
    private val listPageAdapter: ListPageAdapter by lazy { ListPageAdapter(mContext, listener) }

    protected abstract fun listGroup(vl: View.OnClickListener?): List<ListPageGroup>
    open fun initAdapter() {
        listPageAdapter.initUI(pView, this)
    }

    /**
     * set item value in group.
     * @param key the item's key about the list showListDialog in group.
     * @param value the value which will be set to the controller about the item。
     */
    fun setItemValue(key: Int, value: String) {
        CLog.i("setItemValue $key -> $value")
        for (group in group) {
            val gitems = group.items
            for (item in gitems) {
                if (key == item.keyRes) {
                    setItemUIValue(item, value)
                    return
                }
            }
        }
    }

    private fun setItemUIValue(item: ListPageGroup.ListPageItem, value: String) {
        item.setUIValue(value, false)
    }

    open fun getItemLayout(index: Int): Int {
        return R.layout.item_listpage_more
    }

    abstract fun visitItem(item: ListPageGroup.ListPageItem, itemView: View): Boolean

}
