package com.fula.yohee.ui.page

import android.app.Activity
import android.content.Intent
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
import com.fula.yohee.YoheeApp
import com.fula.yohee.FlurryConst
import com.fula.yohee.R
import com.fula.yohee.database.HistoryDatabase
import com.fula.yohee.di.DatabaseScheduler
import com.fula.yohee.dialog.DialogHelper
import com.fula.yohee.dialog.DialogItem
import com.fula.yohee.dialog.SimpleDialog
import com.fula.yohee.extensions.snackbar
import com.fula.yohee.extensions.toInt
import com.fula.yohee.utils.WebUtils
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.*
import javax.inject.Inject

/**
 * @Desc: clear user information setting page
 * @Date: 2018-09-11
 * @author: delanding
 */
class PageWebSetting : BasePage(), ListPageAdapter.ItemListener {

    private lateinit var adapter: ClearAdapter
    @Inject
    internal lateinit var historyModule: HistoryDatabase
    @Inject
    @field:DatabaseScheduler
    internal lateinit var dbScheduler: Scheduler

    override fun initPage(inflater: LayoutInflater, container: ViewGroup) {
        super.initPage(container, R.layout.page_items)
        YoheeApp.injector.inject(this)
        initView()
    }

    private fun initView() {
        mContext.setSupportActionBar(findViewById(R.id.toolSetingbar) as Toolbar)
        initToolBar()

        val listPageContent = findViewById(R.id.list_page_content) as LinearLayout
        adapter = ClearAdapter(mContext, listPageContent, this)
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
                INDEX_CLEAR_CACHE_EXIT -> {
                    FlurryAgent.logEvent(FlurryConst.SETTING_WEB_CLEARCACHE_EXIT_ + isTrue.toInt())
                    it.clearCacheExit = isTrue
                }
                INDEX_CLEAR_HISTORY_EXIT -> {
                    FlurryAgent.logEvent(FlurryConst.SETTING_WEB_CLEARHISTORY_EXIT_ + isTrue.toInt())
                    it.clearHistoryExit = isTrue
                }
                INDEX_CLEAR_COOKIE_EXIT -> {
                    FlurryAgent.logEvent(FlurryConst.SETTING_WEB_CLEARCOOKIE_EXIT_ + isTrue.toInt())
                    it.clearCookiesExit = isTrue
                }
                INDEX_CLEAR_WEBCACHE_EXIT -> {
                    FlurryAgent.logEvent(FlurryConst.SETTING_WEB_CLEARWEBCACHE_EXIT_ + isTrue.toInt())
                    it.clearWebCacheExit = isTrue
                }

                INDEX_CLEAR_CACHE -> {
                    FlurryAgent.logEvent(FlurryConst.SETTING_WEB_CLEARCACHE_CLICK)
                    WebUtils.clearCache(mContext)
                    mContext.snackbar(R.string.message_cache_cleared)
                }
                INDEX_CLEAR_HISTORY -> {
                    FlurryAgent.logEvent(FlurryConst.SETTING_WEB_CLEARHISTORY_CLICK)
                    SimpleDialog.clearHistoryDialog(mContext, historyModule, dbScheduler)
                }
                INDEX_CLEAR_COOKIE -> {
                    FlurryAgent.logEvent(FlurryConst.SETTING_WEB_CLEARCOOKIE_CLICK)
                    clearCookiesDialog()
                }
                INDEX_CLEAR_WEBCACHE -> {
                    FlurryAgent.logEvent(FlurryConst.SETTING_WEB_CLEARWEBCACHE_CLICK)
                    WebUtils.clearWebStorage()
                    mContext.snackbar(R.string.message_web_storage_cleared)
                }
            }
        }
    }

    private fun clearCookiesDialog() {
        DialogHelper.showOkCancelDialog(
                activity = mContext,
                title = R.string.title_clear_cookies,
                message = R.string.dialog_cookies,
                positiveButton = DialogItem(title = R.string.action_yes) {
                    Completable.fromAction { WebUtils.clearCookies(mContext) }
                            .subscribeOn(dbScheduler)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                mContext.snackbar(R.string.message_cookies_cleared)
                            }
                }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        CLog.i("requestCode = $requestCode resultCode = $resultCode")
        if (resultCode != Activity.RESULT_OK) {
            return
        }
    }

    inner class ClearAdapter(mContext: Activity, pView: LinearLayout, listener: ListPageAdapter.ItemListener)
        : BaseInfoAdapter(mContext, pView, listener, null) {

        private val checkItems = mutableListOf(INDEX_CLEAR_CACHE_EXIT, INDEX_CLEAR_HISTORY_EXIT, INDEX_CLEAR_COOKIE_EXIT, INDEX_CLEAR_WEBCACHE_EXIT)

        override fun listGroup(vl: View.OnClickListener?): List<ListPageGroup> {
            return ArrayList<ListPageGroup>().apply {

                add(ListPageGroup(null, ArrayList<ListPageGroup.ListPageItem>().apply {
                    add(ListPageGroup.ListPageItem(INDEX_CLEAR_CACHE_EXIT, R.string.cache))
                    add(ListPageGroup.ListPageItem(INDEX_CLEAR_HISTORY_EXIT, R.string.clear_history_exit))
                    add(ListPageGroup.ListPageItem(INDEX_CLEAR_COOKIE_EXIT, R.string.clear_cookies_exit))
                    add(ListPageGroup.ListPageItem(INDEX_CLEAR_WEBCACHE_EXIT, R.string.clear_web_storage_exit))
                }))

                add(ListPageGroup(null, ArrayList<ListPageGroup.ListPageItem>().apply {
                    add(ListPageGroup.ListPageItem(INDEX_CLEAR_CACHE, R.string.clear_cache, null))
                    add(ListPageGroup.ListPageItem(INDEX_CLEAR_HISTORY, R.string.clear_history, null))
                    add(ListPageGroup.ListPageItem(INDEX_CLEAR_COOKIE, R.string.clear_cookies, null))
                    add(ListPageGroup.ListPageItem(INDEX_CLEAR_WEBCACHE, R.string.clear_web_storage, null))
                }))
            }
        }

        override fun getItemLayout(index: Int): Int {
            return when (index) {
                in checkItems -> R.layout.item_listpage_check
                else -> super.getItemLayout(index)
            }
        }

        override fun visitItem(item: ListPageGroup.ListPageItem, itemView: View): Boolean = when (item.index) {
            in checkItems -> {
                itemView.findViewById<CheckBox>(R.id.item_check)?.run {
                    when (item.index) {
                        INDEX_CLEAR_CACHE_EXIT -> this.isChecked = getUserPrefer().clearCacheExit
                        INDEX_CLEAR_HISTORY_EXIT -> this.isChecked = getUserPrefer().clearHistoryExit
                        INDEX_CLEAR_COOKIE_EXIT -> this.isChecked = getUserPrefer().clearCookiesExit
                        INDEX_CLEAR_WEBCACHE_EXIT -> this.isChecked = getUserPrefer().clearWebCacheExit
                    }
                    setOnCheckedChangeListener { _, isChecked ->
                        listener.onItemClick(itemView, item, if (isChecked) 1 else 0)
                    }
                }
                true
            }
            else -> false
        }
    }

    companion object {
        const val INDEX_CLEAR_CACHE_EXIT = 1
        const val INDEX_CLEAR_HISTORY_EXIT = 2
        const val INDEX_CLEAR_COOKIE_EXIT = 3
        const val INDEX_CLEAR_WEBCACHE_EXIT = 4
        const val INDEX_CLEAR_CACHE = 5
        const val INDEX_CLEAR_HISTORY = 6
        const val INDEX_CLEAR_COOKIE = 7
        const val INDEX_CLEAR_WEBCACHE = 8
    }


}

