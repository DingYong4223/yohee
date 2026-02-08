package com.fula.yohee.ui.page

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fula.CLog
import com.fula.base.iview.BasePage
import com.fula.yohee.YoheeApp
import com.fula.yohee.R
import com.fula.yohee.database.Bookmark
import com.fula.yohee.database.BookmarkDatabase
import com.fula.yohee.di.DatabaseScheduler
import com.fula.yohee.di.NetworkScheduler
import com.fula.yohee.dialog.DialogHelper
import com.fula.yohee.dialog.DialogItem
import com.fula.yohee.eventbus.SEvent
import com.fula.yohee.extensions.toast
import com.fula.yohee.favicon.FaviconModel
import com.fula.yohee.ui.bilogic.BookmarkAdapter
import com.fula.yohee.ui.bilogic.SelectAdapter
import com.fula.yohee.ui.bilogic.SelectModel
import com.fula.swipe.touch.OnItemMoveListener
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.page_recycle_view_layout_notoolbar.view.*
import kotlinx.android.synthetic.main.toolbar.view.*
import org.greenrobot.eventbus.EventBus
import java.util.*
import javax.inject.Inject


/**
 * @Desc: edit the mark in start page.
 * @Date: 2018-09-11
 * @author: delanding
 */
class PageMarkBatchEdit : BasePageSelect() {

    companion object {

        const val KEY_FROM = "KEY_FROM"
        fun genIntent(context: Context, title: String, from: Int): Intent {
            val intent = BasePage.genTitleIntent(context, PageMarkBatchEdit::class.java, title)
            intent.putExtra(KEY_FROM, from)
            return intent
        }
    }

    @Inject
    internal lateinit var bookmarkModel: BookmarkDatabase
    @Inject
    @field:DatabaseScheduler
    internal lateinit var dbScheduler: Scheduler
    @Inject
    internal lateinit var faviconModel: FaviconModel
    @Inject
    @field:NetworkScheduler
    internal lateinit var networkScheduler: Scheduler
    private val from: Int by lazy { intent.getIntExtra(KEY_FROM, Bookmark.FROM_BOOK) }
    private var subscriber: Disposable? = null
    private var editPosition = 0
    private lateinit var key_title: String
    private lateinit var key_url: String

    override fun initPage(inflater: LayoutInflater, container: ViewGroup) {
        YoheeApp.injector.inject(this)
        super.initPage(container, R.layout.page_recycle_view_layout)
        initView()
        initData2UI()
    }

    override fun provideAdapter(): SelectAdapter {
        return BookmarkAdapter(mContext, faviconModel, networkScheduler, this::handleItemLongPress, this::handleItemClick)
    }

    private fun initView() {
        mContext.setSupportActionBar(mView.toolSetingbar as Toolbar)
        initToolBar(title)
        key_title = mContext.getString(R.string.hint_title)
        key_url = mContext.getString(R.string.hint_url)
        mView.recycle_view.apply {
            layoutManager = LinearLayoutManager(mContext)
            isLongPressDragEnabled = true
            setOnItemMoveListener(itemMoveListener)
            adapter = mAdapter
            mContext.toast(R.string.edit_tip)
        }
    }

    override fun handleItemClick(v: View, index: Int) {
        if (!isEdit()) {
            editPosition = index
            val item = mAdapter.itemAt(index)
            val intent = PageItemEdit.genIntent(mContext, HashMap<String, String>().apply {
                this[key_title] = item.title
                this[key_url] = item.urlTxt
            })
            mContext.startActivityForResult(intent, PageItemEdit.ACTIVITY_RESULT_EDITBOOKMARK)
        }
    }

    override fun deleteAction() {
        DialogHelper.showOkCancelDialog(mContext, R.string.alert_warm, R.string.alert_clear_all,
                positiveButton = DialogItem(title = R.string.action_yes) {
                    bookmarkModel.deleteItems(mAdapter.data.filter { it.isCheck }.map { item -> item.obj as Bookmark })
                            .subscribeOn(dbScheduler)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe { _ ->
                                initData2UI()
                                EventBus.getDefault().post(SEvent(SEvent.TYPE_HOMEPAGE_INIT_AND_RELOAD))
                            }
                }
        )
    }

    override fun onBackPressed(): Boolean {
        if (isEdit()) {
            resetState(mAdapter.data)
            mAdapter.notifyItemRangeChanged(0, mAdapter.data.size)
            return true
        }
        return false
    }

    private val itemMoveListener = object : OnItemMoveListener {

        override fun onItemMoveEnd(srcHolder: RecyclerView.ViewHolder?) {
            CLog.i("onItemMoveEnd...")
            bookmarkModel.sort(mAdapter.data.map { it.obj as Bookmark })
                    .subscribeOn(dbScheduler)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        EventBus.getDefault().post(SEvent(SEvent.TYPE_HOMEPAGE_INIT_AND_RELOAD))
                        mContext.toast(R.string.resort_success)
                    }
        }

        override fun onItemMove(srcHolder: RecyclerView.ViewHolder, targetHolder: RecyclerView.ViewHolder): Boolean {
            if (srcHolder.itemViewType != targetHolder.itemViewType) return false

            val fromPosition = srcHolder.adapterPosition
            val toPosition = targetHolder.adapterPosition

            CLog.i("onItemMove fromPosition = $fromPosition toPosition = $toPosition")
            Collections.swap(mAdapter.data, fromPosition, toPosition)
            mAdapter.notifyItemMoved(fromPosition, toPosition)
            return true
        }

        override fun onItemDismiss(srcHolder: RecyclerView.ViewHolder) {
            CLog.i("onItemDismiss...")
        }
    }

    private fun initData2UI() {
        subscriber?.dispose()
        subscriber = bookmarkModel.getSortItemsByType(if (from == Bookmark.FROM_BOOK) Bookmark.TYPE_BOOK else Bookmark.TYPE_MARK)
                .subscribeOn(dbScheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { lists ->
                    mAdapter.initData(lists.map { SelectModel(it, it.title, it.url) })
                }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == Activity.RESULT_OK && requestCode == PageItemEdit.ACTIVITY_RESULT_EDITBOOKMARK) {
            intent?.let {
                val item = mAdapter.data[editPosition]
                val map = intent.getSerializableExtra(PageItemEdit.KEY_MAP) as HashMap<String, String>
                val bookmark = item.obj as Bookmark
                val newItem = bookmark.copy(url = map[key_url]!!, title = map[key_title]!!, folder = bookmark.folder, position = bookmark.position, type = bookmark.type)

                bookmarkModel.editItem(bookmark, newItem)
                        .subscribeOn(dbScheduler)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            EventBus.getDefault().post(SEvent(SEvent.TYPE_HOMEPAGE_INIT_AND_RELOAD))
                            mContext.toast(R.string.setting_success)

                            mAdapter.data[editPosition] = SelectModel(newItem, newItem.title, newItem.url, item.icon)
                            mAdapter.notifyItemChanged(editPosition)
                        }
            }
        }
    }

}

