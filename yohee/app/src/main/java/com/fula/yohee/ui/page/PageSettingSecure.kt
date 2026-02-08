package com.fula.yohee.ui.page

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import com.flurry.android.FlurryAgent
import com.fula.CLog
import com.fula.base.BaseInfoAdapter
import com.fula.base.ListPageAdapter
import com.fula.base.ListPageGroup
import com.fula.base.iview.BasePage
import com.fula.yohee.FlurryConst
import com.fula.yohee.R
import com.fula.yohee.extensions.toInt
import java.util.*
import javax.inject.Inject

/**
 * @Desc: user infomation setting page
 * @Date: 2018-09-11
 * @author: delanding
 */
class PageSettingSecure : BasePage(), ListPageAdapter.ItemListener {

    private lateinit var adapter: SecureAdapter
    @Inject

    override fun initPage(inflater: LayoutInflater, container: ViewGroup) {
        super.initPage(container, R.layout.page_items)
        initView()
    }

    private fun initView() {
        mContext.setSupportActionBar(findViewById(R.id.toolSetingbar) as Toolbar)
        initToolBar()

        val listPageContent = findViewById(R.id.list_page_content) as LinearLayout
        adapter = SecureAdapter(mContext, listPageContent, this)
        adapter.initAdapter()
    }

    private fun initToolBar() {
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_array_left)
        actionBar.title = title
    }

    override fun onItemClick(v: View, item: ListPageGroup.ListPageItem, arg: Int) {
        CLog.i("onitem click...${item.index}, arg = $arg")
        getUserPrefer().let {
            val isTrue = arg == 1
            when (item.index) {
                INDEX_LOCATION -> {
                    FlurryAgent.logEvent(FlurryConst.SETTING_SECRATE_LOCATION_ + isTrue.toInt())
                    it.locationEnabled = isTrue
                }
                INDEX_PWD_KEEP -> {
                    FlurryAgent.logEvent(FlurryConst.SETTING_SECRATE_SAVEPASS_ + isTrue.toInt())
                    it.savePwd = isTrue
                }
            }
        }
    }

    inner class SecureAdapter(mContext: Activity, pView: LinearLayout, listener: ListPageAdapter.ItemListener)
        : BaseInfoAdapter(mContext, pView, listener, null) {

        override fun listGroup(vl: View.OnClickListener?): List<ListPageGroup> {
            return ArrayList<ListPageGroup>().apply {
                add(ListPageGroup(null, ArrayList<ListPageGroup.ListPageItem>().apply {
                    add(ListPageGroup.ListPageItem(INDEX_LOCATION, R.string.location))
                    add(ListPageGroup.ListPageItem(INDEX_PWD_KEEP, R.string.password))
                }))
            }
        }

        override fun getItemLayout(index: Int): Int {
            return R.layout.item_listpage_check
        }

        override fun visitItem(item: ListPageGroup.ListPageItem, itemView: View): Boolean {
            itemView.findViewById<CheckBox>(R.id.item_check)?.run {
                setOnCheckedChangeListener { _, isChecked ->
                    listener.onItemClick(itemView, item, if (isChecked) 1 else 0)
                }
                when (item.index) {
                    INDEX_LOCATION -> this.isChecked = getUserPrefer().locationEnabled
                    INDEX_PWD_KEEP -> this.isChecked = getUserPrefer().savePwd
                }
            }
            return true
        }

    }

    companion object {
        private const val INDEX_LOCATION = 0
        private const val INDEX_PWD_KEEP = 1
    }

}

