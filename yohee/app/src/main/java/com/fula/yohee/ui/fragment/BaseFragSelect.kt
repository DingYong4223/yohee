package com.fula.yohee.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.fula.yohee.R
import com.fula.yohee.ui.bilogic.SelectAdapter
import com.fula.yohee.ui.bilogic.SelectModel
import com.fula.yohee.ui.common.EditMenuHelper
import kotlinx.android.synthetic.main.layout_bottom_menu_action.*
import kotlinx.android.synthetic.main.page_recycle_view_layout_notoolbar.*

abstract class BaseFragSelect : BaseFragment() {

    protected lateinit var mAdapter: SelectAdapter
    private lateinit var editMenuHelper: EditMenuHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.page_recycle_view_layout_notoolbar, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        initSelectView()
        recycle_view.apply {
            layoutManager = LinearLayoutManager(mContext)
            this.adapter = mAdapter
        }
        recycle_view.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
                if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    if (isEdit()) {
                        editMenuHelper.swichEdit()
                        mAdapter.notifyDataSetChanged()
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun initSelectView() {
        editMenuHelper = EditMenuHelper(bottom_menu_layout, l, null).apply {
            initMenuItem(R.id.menu_item_1, l, intArrayOf(R.string.action_select_all, R.string.action_cancel), Color.WHITE)
            initMenuItem(R.id.menu_item_2, l, intArrayOf(R.string.action_delete, R.string.action_delete), Color.RED)
            initMenuItem(R.id.menu_item_5, l, intArrayOf(R.string.action_complete, R.string.action_complete), Color.WHITE)
        }

        mAdapter = SelectAdapter(mContext, this::handleItemLongPress, this::handleItemClick, ::initVisit, ::bindVisit).apply {
            this.editHelper = editMenuHelper
        }
    }

    open fun handleItemLongPress(index: Int): Boolean {
        if (!editMenuHelper.isEdit) {
            editMenuHelper.swichEdit()
            mAdapter.data[index].isCheck = true
            mAdapter.notifyDataSetChanged()
        }
        return true
    }

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
            R.id.menu_item_5 -> if (isEdit()) {
                editMenuHelper.swichEdit()
                mAdapter.notifyDataSetChanged()
            }
        }
    }

    protected fun isEdit() = editMenuHelper.isEdit
    open fun initVisit(view: View){}
    open fun bindVisit(view: View, item: SelectModel){}
    abstract fun handleItemClick(v: View, index: Int)
    abstract fun deleteAction()
}
