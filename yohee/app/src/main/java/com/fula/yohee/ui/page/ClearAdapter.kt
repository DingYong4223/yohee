package com.fula.yohee.ui.page

import android.app.Activity
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import com.fula.base.BaseInfoAdapter
import com.fula.base.ListPageAdapter
import com.fula.base.ListPageGroup
import com.fula.base.ListPageGroup.ListPageItem
import com.fula.yohee.R
import java.util.*

/**
 * Setting list page adapter.
 *
 * @author delanding
 */

class ClearAdapter(mContext: Activity, pView: LinearLayout, listener: ListPageAdapter.ItemListener)
    : BaseInfoAdapter(mContext, pView, listener, null) {

    override fun listGroup(vl: View.OnClickListener?): List<ListPageGroup> {
        return ArrayList<ListPageGroup>().apply {

            add(ListPageGroup(null, ArrayList<ListPageItem>().apply {
                add(ListPageItem(INDEX_CLEAR_CACHE_EXIT, R.string.cache))
                add(ListPageItem(INDEX_CLEAR_HISTORY_EXIT, R.string.clear_history_exit))
                add(ListPageItem(INDEX_CLEAR_COOKIE_EXIT, R.string.clear_cookies_exit))
                add(ListPageItem(INDEX_CLEAR_WEBCACHE_EXIT, R.string.clear_web_storage_exit))
            }))

            add(ListPageGroup(null, ArrayList<ListPageItem>().apply {
                add(ListPageItem(INDEX_CLEAR_CACHE, R.string.clear_cache))
                add(ListPageItem(INDEX_CLEAR_HISTORY, R.string.clear_history))
                add(ListPageItem(INDEX_CLEAR_COOKIE, R.string.clear_cookies))
                add(ListPageItem(INDEX_CLEAR_WEBCACHE, R.string.clear_web_storage))
            }))
        }
    }

    override fun getItemLayout(index: Int): Int {
        return R.layout.item_listpage_check
    }

    override fun visitItem(item: ListPageGroup.ListPageItem, itemView: View): Boolean {
        if (item.index == INDEX_CLEAR_CACHE
                || item.index == INDEX_CLEAR_HISTORY
                || item.index == INDEX_CLEAR_COOKIE
                || item.index == INDEX_CLEAR_WEBCACHE) {
            itemView.findViewById<CheckBox>(R.id.item_check)?.visibility = View.GONE
            itemView.setOnClickListener { listener.onItemClick(itemView, item) }
        } else {
            itemView.findViewById<CheckBox>(R.id.item_check)?.run {
                setOnCheckedChangeListener { _, isChecked ->
                    listener.onItemClick(itemView, item, if (isChecked) 1 else 0)
                }
            }
        }
        return true
    }

    companion object {
        const val INDEX_CLEAR_CACHE_EXIT = 1
        const val INDEX_CLEAR_HISTORY_EXIT = 2
        const val INDEX_CLEAR_COOKIE_EXIT = 3
        const val INDEX_CLEAR_WEBCACHE_EXIT = 4
        const val INDEX_CLEAR_CACHE = 5
        const val INDEX_CLEAR_HISTORY = 6
        const val INDEX_CLEAR_COOKIE = 7
        const val INDEX_CLEAR_WEBCACHE = 8
    }

}
