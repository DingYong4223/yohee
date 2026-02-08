package com.fula.yohee.ui.page

import android.app.Activity
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.setPadding
import com.fula.base.BaseInfoAdapter
import com.fula.base.ListPageAdapter
import com.fula.base.ListPageAdapter.Companion.ITEM_ANIM_DURATION
import com.fula.base.ListPageGroup
import com.fula.base.ListPageGroup.ListPageItem
import com.fula.yohee.R
import com.fula.yohee.settings.UserSetting.Companion.NO_VALUE
import com.fula.util.ViewUnit
import kotlinx.android.synthetic.main.item_listpage_select.view.*
import java.util.*

/**
 * Setting list page adapter.
 * @author delanding
 */
class ItemSelectAdapter(mContext: Activity, pView: LinearLayout, listener: ListPageAdapter.ItemListener, private val settingItems: Array<SettingItem>)
    : BaseInfoAdapter(mContext, pView, listener, null) {

    var curItem: ListPageItem? = null

    fun initAdapter(select: Int) {
        super.initAdapter()
        pView.postDelayed({ initSelect(select) }, ITEM_ANIM_DURATION * settingItems.size)
    }

    private fun initSelect(select: Int) {
        val group0 = group[0]
        group0.items.forEach {
            it.view?.item_radio?.run {
                isChecked = (select == it.intValue)
                if (isChecked) {
                    curItem = it
                }
            }
        }
    }

    override fun listGroup(vl: View.OnClickListener?): List<ListPageGroup> {
        val glist = ArrayList<ListPageGroup>()
        val lp0 = ArrayList<ListPageItem>()
        for (i in settingItems.indices) {
            val item = settingItems[i]
            val litem = ListPageItem(i, item.key, "").apply {
                resID = item.resID
                intValue = item.value
            }
            lp0.add(litem)
        }
        val gp0 = ListPageGroup(null, lp0)
        glist.add(gp0)
        return glist
    }

    override fun getItemLayout(index: Int): Int {
        return R.layout.item_listpage_select
    }

    override fun visitItem(item: ListPageGroup.ListPageItem, itemView: View): Boolean {
        if (NO_VALUE != item.resID) {
            val settingItem = settingItems[item.index]
            when {
                item.resID == SettingItem.VALUE_GONE -> itemView.logo_img.visibility = View.GONE
                else -> item.resID?.let {
                    itemView.logo_img.apply {
                        setImageResource(it)
                        if (settingItem.icon >= 0) {
                            this.setPadding(ViewUnit.dp2px(settingItem.icon.toFloat()))
                        }
                    }
                }
            }
        }
        return false
    }

    fun setItemSelect(item: ListPageGroup.ListPageItem) {
        val group0 = group[0]
        curItem = item
        group0.items.forEach {
            it.view?.item_radio?.run {
                isChecked = (item == it)
            }
        }
    }

}
