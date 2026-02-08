package com.fula.yohee.ui.page

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import com.fula.CLog
import com.fula.base.ListPageAdapter
import com.fula.base.ListPageGroup
import com.fula.base.iview.BasePage
import com.fula.yohee.R
import com.fula.yohee.eventbus.SEvent
import com.fula.yohee.extensions.getNullableString
import kotlinx.android.synthetic.main.page_items.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * @Desc: user infomation setting page
 * @Date: 2018-09-11
 * @author: delanding
 */
class PageItemSelect : BasePage(), ListPageAdapter.ItemListener {

    private lateinit var adapter: ItemSelectAdapter
    private val settingIndex: Int by lazy { intent.getIntExtra(KEY_SETTING_INDEX, -1) }
    private val descId: Int by lazy { intent.getIntExtra(KEY_DESC, View.NO_ID) }
    private val select: Int by lazy { intent.getIntExtra(KEY_SELECT_INDEX, -1) }
    private val postSelect: Boolean by lazy { intent.getBooleanExtra(KEY_POST_SELECT, false) }
    private val items: Array<SettingItem> by lazy { intent.getSerializableExtra(KEY_ITEMS) as Array<SettingItem> }
    private val pageType: Int by lazy { intent.getIntExtra(KEY_PAGE_TYPE, PAGE_TYPE_SELECT) }

    override fun initPage(inflater: LayoutInflater, container: ViewGroup) {
        super.initPage(container, R.layout.page_items)
        registerEventBus()
        initView()
    }

    private fun initView() {
        mContext.setSupportActionBar(findViewById(R.id.toolSetingbar) as Toolbar)
        initToolBar()

        if (descId != View.NO_ID) {
            mView.page_items_tip.apply {
                visibility = View.VISIBLE
                setText(descId)
            }
        }
        val listPageContent = findViewById(R.id.list_page_content) as LinearLayout
        adapter = ItemSelectAdapter(mContext, listPageContent, this, items)
        adapter.initAdapter(select)
    }

    private fun initToolBar() {
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_array_left)
        actionBar.title = title
    }

    private fun prosBack() {
        CLog.i("back key pressed...")
        adapter.curItem?.run {
            CLog.i("set new select: $index")
            val intent = Intent()
            intent.putExtra(KEY_SETTING_INDEX, settingIndex)
            intent.putExtra(KEY_SELECT_INDEX, index)
            intent.putExtra(KEY_SELECT_VALUE, items[index].value)
            mContext.setResult(Activity.RESULT_OK, intent)
        }
    }

    override fun onMenuBack(): Boolean {
        prosBack()
        finish()
        return true
    }

    override fun onBackPressed(): Boolean {
        prosBack()
        return super.onBackPressed()
    }

    override fun onItemClick(v: View, item: ListPageGroup.ListPageItem, arg: Int) {
        CLog.i("onitem click...${item.index}, arg = $arg")
        if (pageType == PAGE_TYPE_SELECT_FINISH) {
            val intent = Intent()
            intent.putExtra(KEY_SETTING_INDEX, settingIndex)
            intent.putExtra(KEY_SELECT_INDEX, item.index)
            intent.putExtra(KEY_SELECT_VALUE, items[item.index].value)
            mContext.setResult(Activity.RESULT_OK, intent)
            finish()
        } else {
            adapter.setItemSelect(item)
            if (postSelect) EventBus.getDefault().post(SEvent(settingIndex).apply {
                intArg = items[item.index].value
            })
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: SEvent) {
        SEvent.comEventPros(mContext, event)
    }

    companion object {

        const val KEY_ITEMS = "KEY_ITEMS"
        const val KEY_SELECT_INDEX = "KEY_SELECT_INDEX"
        const val KEY_SELECT_VALUE = "KEY_SELECT_VALUE"
        const val KEY_SETTING_INDEX = "KEY_SETTING_INDEX"
        const val KEY_POST_SELECT = "KEY_POST_SELECT"
        const val KEY_DESC = "KEY_DESC"
        const val KEY_PAGE_TYPE = "KEY_PAGE_TYPE"

        const val PAGE_TYPE_SELECT = 0  //仅仅选择，不finish
        const val PAGE_TYPE_SELECT_FINISH = 1  //选择后finish掉自己并返回结果

        fun genIntent(context: Context, @StringRes titleRes: Int, settingIndex: Int, items: Array<SettingItem>, select: Int = -1, postSelect: Boolean = false, descId: Int = View.NO_ID, pageType: Int = PAGE_TYPE_SELECT): Intent {
            if (-1 == settingIndex) {
                throw RuntimeException("error, setting intArg = $settingIndex")
            }
            val intent = BasePage.genTitleIntent(context, PageItemSelect::class.java, context.getNullableString(titleRes))
            intent.putExtra(KEY_SETTING_INDEX, settingIndex)
            intent.putExtra(KEY_ITEMS, items)
            intent.putExtra(KEY_SELECT_INDEX, select)
            intent.putExtra(KEY_POST_SELECT, postSelect)
            intent.putExtra(KEY_DESC, descId)
            intent.putExtra(KEY_PAGE_TYPE, pageType)
            return intent
        }

    }

}

