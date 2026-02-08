package com.fula.base

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.fula.util.ViewUnit
import com.fula.yohee.R
import com.fula.yohee.extensions.getOptionableString
import com.fula.CLog
import com.fula.yohee.utils.SViewHelper
import com.fula.yohee.utils.ThemeUtils
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


/**
 * list page adapter for list page.
 * @author delanding
 */

class ListPageAdapter(private val mContext: Activity, private val listener: ItemListener?) {

    private val inflater: LayoutInflater = mContext.layoutInflater
    private lateinit var infoAdapter: BaseInfoAdapter
    private val lp = LinearLayout.LayoutParams(-1, -2)
    private val groupBgColor: Int by lazy { ThemeUtils.getPrimaryColorTrans(mContext) }

    interface ItemListener {
        fun onItemClick(v: View, item: ListPageGroup.ListPageItem, arg: Int = 0)
    }

    fun initUI(vp: ViewGroup, infoAdapter: BaseInfoAdapter) {
        this.infoAdapter = infoAdapter
        val listGroup = infoAdapter.group
        for (groupIndex in listGroup.indices) {
            val lgroup = listGroup[groupIndex]
            vp.addView(getGroupView(lgroup, groupIndex), if (groupIndex == 0) {
                LinearLayout.LayoutParams(-1, -2)
            } else {
                lp.apply { topMargin = ViewUnit.dp2px(10f) }
            })
        }
    }

    private fun getGroupView(lgroup: ListPageGroup, indexGroup: Int): LinearLayout {
        val group = LinearLayout(mContext)
        group.apply {
            layoutTransition = SViewHelper.getViewInTransition(mContext, ITEM_ANIM_DURATION)
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(groupBgColor)
        }
        lgroup.group_title?.run {
            CLog.i("inflate group...")
            val groupView = inflater.inflate(R.layout.item_listpage_group_title, group, false)
            ViewHelper.setTextView(groupView, R.id.item_key, mContext.getString(this))
            lgroup.group_desc?.run { ViewHelper.setTextView(groupView, R.id.item_desc, this) }
            groupView.visibility = lgroup.group_visible
            group.addView(groupView)
        }
        Observable.create(ObservableOnSubscribe<Int> { e ->
            for (i in 0 until lgroup.items.size) {
                e.onNext(i)
                Thread.sleep(ITEM_ANIM_DURATION / 2)
            }
            e.onComplete()
        }).subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { itemIndex ->
                    val item = lgroup.getItem(itemIndex)
                    if (null == item.view) {
                        item.view = genItemView(item, group)
                    } else if (item.view !is ViewGroup) {
                        item.view?.run {
                            setOnClickListener { listener?.onItemClick(this, item) }
                        }
                    }
                    item.view!!.setTag(R.id.TAG_KEY_GROUP, indexGroup)
                    item.view!!.setTag(R.id.TAG_KEY_ITEM, itemIndex)
                    item.item_value?.run {
                        item.setUIValue(this, true)
                    }
                    group.addView(item.view, item.lp ?: item.view!!.layoutParams)
                }
        return group
    }

    private fun genItemView(item: ListPageGroup.ListPageItem, parent: ViewGroup): View {
        return inflater.inflate(infoAdapter.getItemLayout(item.index), parent, false).apply {
            if (!foreachItem(item, this)) setOnClickListener { listener?.onItemClick(this, item) }
        }
    }

    /**Itererater process the list item initialize
     * , true will be returned if adapter set the item listner, false otherwise
     * */
    private fun foreachItem(item: ListPageGroup.ListPageItem, itemView: View): Boolean {
        itemView.findViewById<TextView>(R.id.item_key)?.let {
            it.text = mContext.getOptionableString(item.keyRes, item.keyStr)
        }
        return infoAdapter.visitItem(item, itemView)
    }

    companion object {
        const val ITEM_ANIM_DURATION = 100L
    }

}
