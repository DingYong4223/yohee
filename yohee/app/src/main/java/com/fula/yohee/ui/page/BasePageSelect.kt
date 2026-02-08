package com.fula.yohee.ui.page

import android.graphics.Color
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import com.fula.CLog
import com.fula.base.iview.BasePage
import com.fula.yohee.R
import com.fula.yohee.ui.bilogic.SelectAdapter
import com.fula.yohee.ui.bilogic.SelectModel
import com.fula.yohee.ui.common.EditMenuHelper
import kotlinx.android.synthetic.main.layout_bottom_menu_action.view.*

/**
 * @Desc: page for select items in recycle list view.
 * @Date: 2019-03-27
 * @author: delanding
 */
abstract class BasePageSelect : BasePage() {

    protected lateinit var mAdapter: SelectAdapter
    protected lateinit var editMenuHelper: EditMenuHelper
    private var menuItem: MenuItem? = null

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        mContext.menuInflater.inflate(R.menu.menu_android_2edit, menu)
        menuItem = menu!!.getItem(0).apply {
            setTitle(R.string.action_edit)
            isVisible = !mAdapter.data.isEmpty()
        }
        menu.getItem(1).apply {
            isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.toolbar_menu0 -> {
                editMenuHelper.swichEdit()
                mAdapter.notifyDataSetChanged()
            }
        }
        return true
    }

    @CallSuper
    override fun initPage(container: ViewGroup, resId: Int) {
        super.initPage(container, resId)
        initSelectView()
    }

    protected open fun initSelectView() {
        editMenuHelper = EditMenuHelper(mView.bottom_menu_layout, l, this::menuListener).apply {
            initMenuItem(R.id.menu_item_1, l, intArrayOf(R.string.action_select_all, R.string.action_cancel), Color.WHITE)
            initMenuItem(R.id.menu_item_2, l, intArrayOf(R.string.action_delete, R.string.action_delete), Color.RED)
            initMenuItem(R.id.menu_item_5, l, intArrayOf(R.string.action_complete, R.string.action_complete), Color.WHITE)
        }

        mAdapter = provideAdapter().apply {
            this.editHelper = editMenuHelper
            this.dataListener = ::dataChange
            this.initVisit = ::initVisit
            this.bindVisit = ::bindVisit
            this.selectListener = ::selectListener
        }
    }

    private fun dataChange(data: List<EditMenuHelper.CheckModel>) {
        CLog.i("data change...")
        menuItem?.isVisible = !data.isEmpty()
    }


    private fun menuListener(isEdit: Boolean) {
        if (isEdit) {
            menuItem?.setTitle(R.string.action_back)
        } else {
            menuItem?.setTitle(R.string.action_edit)
        }
    }

    open fun provideAdapter(): SelectAdapter {
        return SelectAdapter(mContext, this::handleItemLongPress, this::handleItemClick)
    }

    protected fun handleItemLongPress(index: Int): Boolean {
        if (!editMenuHelper.isEdit) {
            mAdapter.data[index].isCheck = true
            menuItem?.let {
                onOptionsItemSelected(it)
            }
        }
        return true
    }

    override fun onBackPressed(): Boolean {
        if (editMenuHelper.isEdit) {
            editMenuHelper.resetState(mAdapter.data)
            mAdapter.notifyItemRangeChanged(0, mAdapter.data.size)
            return true
        }
        return false
    }

    protected fun resetState(data: List<EditMenuHelper.CheckModel>? = null) = editMenuHelper.resetState(data)

    private val l = View.OnClickListener {
        when (it.id) {
            R.id.menu_item_1 -> {
                editMenuHelper.swichMenuItem(R.id.menu_item_1)

                for (item in mAdapter.data) {
                    item.isCheck = editMenuHelper.isCheck(R.id.menu_item_1)
                }
                mAdapter.notifyDataSetChanged()
            }
            R.id.menu_item_2 -> {
                if (mAdapter.data.count { item -> item.isCheck } <= 0) {
                    return@OnClickListener
                }
                deleteAction()
            }
            R.id.menu_item_5 -> finish()
        }
    }

    protected fun isEdit() = editMenuHelper.isEdit
    open fun initVisit(view: View) {}
    open fun bindVisit(view: View, item: SelectModel) {}
    open fun selectListener(data: List<SelectModel>) {}
    abstract fun handleItemClick(v: View, index: Int)
    abstract fun deleteAction()

}

