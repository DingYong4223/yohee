package com.fula.yohee.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.fula.CLog
import com.fula.yohee.YoheeApp
import com.fula.yohee.MenuPop
import com.fula.yohee.R
import com.fula.yohee.database.Bookmark
import com.fula.yohee.database.BookmarkDatabase
import com.fula.yohee.di.DatabaseScheduler
import com.fula.yohee.di.NetworkScheduler
import com.fula.yohee.dialog.DialogHelper
import com.fula.yohee.dialog.DialogItem
import com.fula.yohee.dialog.SDialogBuilder
import com.fula.yohee.eventbus.DrawerEvent
import com.fula.yohee.favicon.FaviconModel
import com.fula.yohee.preference.UserPreferences
import com.fula.yohee.ui.activity.WebActivity
import com.fula.yohee.ui.bilogic.BookmarkAdapter
import com.fula.yohee.ui.bilogic.SelectModel
import com.fula.yohee.ui.common.EditMenuHelper
import com.fula.yohee.utils.SViewHelper
import com.fula.helper.HorizontalDividerItemDecoration
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.layout_bottom_menu_action.view.*
import kotlinx.android.synthetic.main.page_recycle_view_layout_notoolbar.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject


class FragBookmark : BaseFragment() {

    @Inject
    internal lateinit var bookmarkModel: BookmarkDatabase
    @Inject
    internal lateinit var dialogBuilder: SDialogBuilder
    @Inject
    internal lateinit var userPreferences: UserPreferences
    @Inject
    internal lateinit var faviconModel: FaviconModel
    @Inject
    internal lateinit var menuPop: MenuPop
    @Inject
    @field:DatabaseScheduler
    internal lateinit var dbScheduler: Scheduler
    @Inject
    @field:NetworkScheduler
    internal lateinit var networkScheduler: Scheduler
    private lateinit var mAdapter: BookmarkAdapter
    private var isIncognito: Boolean = false
    private var darkTheme: Boolean = false
    private var subscriber: Disposable? = null
    private var updateDisposable: Disposable? = null
    val mActivity: WebActivity by lazy { requireNotNull(context as WebActivity) { "Context should never be null in onCreate" } }
    private lateinit var editMenuHelper: EditMenuHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerEventBus()
        YoheeApp.injector.inject(this)
        isIncognito = arguments?.getBoolean(INCOGNITO_MODE, false) == true
        darkTheme = userPreferences.useTheme != 0 || isIncognito
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.page_recycle_view_layout_notoolbar, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        editMenuHelper = EditMenuHelper(view!!.bottom_menu_layout, l){}
        editMenuHelper.initMenuItem(R.id.menu_item_1, l, intArrayOf(R.string.action_select_all, R.string.action_cancel), Color.WHITE)
        editMenuHelper.initMenuItem(R.id.menu_item_2, l, intArrayOf(R.string.action_delete, R.string.action_delete), Color.RED)
        editMenuHelper.initMenuItem(R.id.menu_item_5, l, intArrayOf(R.string.action_complete, R.string.action_complete), Color.WHITE)
        (view as ViewGroup).layoutTransition = SViewHelper.getYTransition(100, mContext.resources.getDimension(R.dimen.bottom_menu_height))

        mAdapter = BookmarkAdapter(mContext, faviconModel, networkScheduler, this::handleItemLongPress, this::handleItemClick).apply {
            this.editHelper = editMenuHelper
        }
        recycle_view.let {
            it.layoutManager = LinearLayoutManager(context)
            it.adapter = mAdapter
            it.addItemDecoration(HorizontalDividerItemDecoration.Builder(context).size(2)
                    .color(Color.GRAY)
                    .build())
        }
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
                DialogHelper.showOkCancelDialog(mActivity, R.string.alert_warm, R.string.alert_clear_all,
                        positiveButton = DialogItem(title = R.string.action_yes) {
                            bookmarkModel.deleteItems(mAdapter.data.filter { it.isCheck }.map { item -> item.obj as Bookmark })
                                    .subscribeOn(dbScheduler)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe { _ ->
                                        initDataToUI()
                                    }
                        }
                )
            }
            R.id.menu_item_5 -> {
                editMenuHelper.swichEdit()
                mAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        subscriber?.dispose()
        updateDisposable?.dispose()
        mAdapter.cleanupSubscriptions()
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onEvent(event: DrawerEvent) {
        CLog.i("onEvent = ${event.type}")
        if (!visiable) {
            CLog.i("fragment not visiable...")
            return
        }
        when (event.type) {
            DrawerEvent.DRAWER_OPENED, DrawerEvent.BOOKMARK_DATA_CHANGED -> {
                initDataToUI()
            }
            DrawerEvent.BOOKMARK_BACH_EDIT -> {
                editMenuHelper.swichEdit()
                mAdapter.notifyDataSetChanged()
            }
            DrawerEvent.DRAWER_CLOSED -> {
                if (editMenuHelper.isEdit) {
                    editMenuHelper.swichEdit()
                    mAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun initDataToUI() {
        CLog.i("start update db data to UI...")
        editMenuHelper.resetState(mAdapter.data)
        subscriber?.dispose()
        subscriber = bookmarkModel.getSortItemsByType()
                .subscribeOn(dbScheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { lists ->
                    mAdapter.updateData(lists.map { SelectModel(it, it.title, it.url) })
                }
    }

    private fun handleItemLongPress(index: Int): Boolean {
        (context as WebActivity?)?.let {
            val item = mAdapter.itemAt(index).obj as Bookmark
            menuPop.bookmarkItemAction(it, item, Bookmark.FROM_BOOK)
        }
        return true
    }

    private fun handleItemClick(v: View, index: Int) {
        if (!editMenuHelper.isEdit) {
            mActivity.bookmarkItemClicked(mAdapter.itemAt(index).obj as Bookmark)
        }
    }

    companion object {
        private const val TAG = "FragBookmark"
        private const val INCOGNITO_MODE = "$TAG.INCOGNITO_MODE"
    }

}
