package com.fula.yohee.ui.page

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.fula.yohee.YoheeApp
import com.fula.yohee.R
import com.fula.yohee.database.Ad
import com.fula.yohee.database.AdDatabase
import com.fula.yohee.di.DatabaseScheduler
import com.fula.yohee.dialog.DialogHelper
import com.fula.yohee.dialog.DialogItem
import com.fula.yohee.eventbus.AdEvent
import com.fula.yohee.ui.bilogic.SelectModel
import com.fula.yohee.utils.SViewHelper
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.page_recycle_view_layout_notoolbar.view.*
import kotlinx.android.synthetic.main.toolbar.view.*
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat
import javax.inject.Inject

/**
 * @Desc: ad block list showListDialog page.
 * @Date: 2019-03-08
 * @author: delanding
 */
class PageAdblock : BasePageSelect() {

    @Inject
    internal lateinit var adDatabase: AdDatabase

    @field:DatabaseScheduler
    @Inject
    internal lateinit var dbScheduler: Scheduler

    override fun initPage(inflater: LayoutInflater, container: ViewGroup) {
        super.initPage(container, R.layout.page_recycle_view_layout)
        YoheeApp.injector.inject(this)
        initView()
        initData2UI()
    }

    private fun initView() {
//        editMenuHelper = EditMenuHelper(mView.bottom_menu_layout, l)
//        editMenuHelper.initMenuItem(R.id.menu_item_1, l, intArrayOf(R.string.action_select_all, R.string.action_cancel), Color.WHITE)
//        editMenuHelper.initMenuItem(R.id.menu_item_2, l, intArrayOf(R.string.action_delete, R.string.action_delete), Color.RED)
        (mView as ViewGroup).layoutTransition = SViewHelper.getYTransition(200, mContext.resources.getDimension(R.dimen.bottom_menu_height))
        mContext.setSupportActionBar(mView.toolSetingbar)
        initToolBar(R.string.block_ads_manage)

//        mAdapter = SelectAdapter(mContext, editMenuHelper, this::handleItemLongPress) {}
        mView.recycle_view.apply {
            layoutManager = LinearLayoutManager(mContext)
            mView.recycle_view.adapter = mAdapter
        }
    }

    override fun handleItemClick(v: View, index: Int) {}

    override fun deleteAction() {
        DialogHelper.showOkCancelDialog(mContext, R.string.alert_warm, R.string.alert_clear_all,
                positiveButton = DialogItem(title = R.string.action_yes) {
                    adDatabase.deleteAd(mAdapter.data.filter { ad -> ad.isCheck }.map { item -> (item.obj as Ad) })
                            .subscribeOn(dbScheduler)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                initData2UI()
                                EventBus.getDefault().post(AdEvent(AdEvent.TYPE_ADD_DELETE, null))
                            }
                }
        )
    }

//    private val l = View.OnClickListener {
//        when (it.id) {
//            R.id.menu_item_1 -> {
//                editMenuHelper.swichMenuItem(R.id.menu_item_1)
//
//                for (item in mAdapter.data) {
//                    item.isCheck = editMenuHelper.isCheck(R.id.menu_item_1)
//                }
//                mAdapter.notifyDataSetChanged()
//            }
//            R.id.menu_item_2 -> {
//                if (mAdapter.data.count { item -> item.isCheck } <= 0) {
//                    return@OnClickListener
//                }
//                DialogHelper.showOkCancelDialog(mContext, R.string.alert_warm, R.string.alert_clear_all,
//                        positiveButton = DialogItem(title = R.string.action_yes) {
//                            adDatabase.deleteAd(mAdapter.data.filter { ad -> ad.isCheck }.map { item -> (item.obj as Ad) })
//                                    .subscribeOn(dbScheduler)
//                                    .observeOn(AndroidSchedulers.mainThread())
//                                    .subscribe {
//                                        initData2UI()
//                                        EventBus.getDefault().post(AdEvent(AdEvent.TYPE_ADD_DELETE, null))
//                                    }
//                        }
//                )
//            }
//        }
//    }

    private fun initData2UI() = adDatabase
            .getAllAds()
            .subscribeOn(dbScheduler)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe ({ data ->
                mAdapter.initData(data.map { SelectModel(it, it.url, timeFormatter.format(it.timeCreated)) })
            }){}

    companion object {
        internal var timeFormatter = SimpleDateFormat("yy-MM-dd HH:mm:ss")
    }

}

