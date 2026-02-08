package com.fula.yohee.ui.page

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.fula.yohee.R
import com.fula.yohee.constant.FILE
import com.fula.yohee.dialog.DialogHelper
import com.fula.yohee.dialog.DialogItem
import com.fula.yohee.eventbus.SEvent
import com.fula.yohee.ui.bilogic.SelectModel
import com.fula.yohee.utils.YoheePermission
import com.fula.yohee.utils.FileUtils
import com.fula.permission.PermissionsManager
import com.fula.permission.PermissionsResultAction
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.page_recycle_view_layout_notoolbar.view.*
import kotlinx.android.synthetic.main.toolbar.view.*
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * @Desc: offline webpage list showListDialog
 * @Date: 2019-03-01
 * @author: delanding
 */
class PageOfflineWeb : BasePageSelect() {

    private val webPath = File(FileUtils.getDownloadWebPath())

    override fun initPage(inflater: LayoutInflater, container: ViewGroup) {
        super.initPage(container, R.layout.page_recycle_view_layout)
        initView()
        PermissionsManager.requestPermissionsIfNecessaryForResult(mContext, YoheePermission.STORAGE_READ_WRITE, object : PermissionsResultAction() {
            override fun onGranted() {
                initData2UI()
            }
        })
    }

    private fun initView() {
        mContext.setSupportActionBar(mView.toolSetingbar)
        initToolBar(R.string.offpage)

        mView.recycle_view.apply {
            layoutManager = LinearLayoutManager(mContext)
            mView.recycle_view.adapter = mAdapter
        }
    }

    override fun handleItemClick(v: View, index: Int) {
        if (!isEdit()) {
            val file = mAdapter.data[index].obj as File
            EventBus.getDefault().post(SEvent(SEvent.TYPE_OPEN_URL).apply { stringArg = FILE + file.absolutePath })
            finish()
        }
    }

    override fun deleteAction() {
        DialogHelper.showOkCancelDialog(mContext, R.string.alert_warm, R.string.alert_clear_all,
                positiveButton = DialogItem(title = R.string.action_yes) {
                    Single.just(mAdapter.data.filter { it.isCheck }.withIndex())
                            .subscribeOn(Schedulers.single())
                            .doOnSuccess { list ->
                                list.forEach {
                                    (it.value.obj as File).delete()
                                }
                            }
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                initData2UI()
                            }) {}
                }
        )
    }

    private fun initData2UI() = Single.just(webPath.listFiles().asList())
            .subscribeOn(Schedulers.single())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ list ->
                mAdapter.updateData(list.map { SelectModel(it, it.name, SimpleDateFormat("yyyy-MM-dd").format(Date(it.lastModified()))) })
            }) {}

}

