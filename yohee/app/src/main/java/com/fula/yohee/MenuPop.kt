package com.fula.yohee

import android.content.ClipboardManager
import android.view.View
import android.webkit.WebView
import com.fula.CLog
import com.fula.yohee.database.Bookmark
import com.fula.yohee.database.BookmarkDatabase
import com.fula.yohee.di.DatabaseScheduler
import com.fula.yohee.download.DownloadHandler
import com.fula.yohee.eventbus.AdEvent
import com.fula.yohee.eventbus.DrawerEvent
import com.fula.yohee.eventbus.SEvent
import com.fula.yohee.extensions.copy
import com.fula.yohee.extensions.loadJavascript
import com.fula.yohee.js.JSAdBlock
import com.fula.yohee.ui.activity.WebActivity
import com.fula.yohee.ui.page.PageItemEdit
import com.fula.yohee.ui.page.PageMarkBatchEdit
import com.fula.yohee.uiwigit.floatmenu.FloatMenu
import com.fula.yohee.uiwigit.floatmenu.FloatMenuItem
import com.fula.yohee.utils.ShareUtils
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuPop @Inject constructor(
        private val bookmarkManager: BookmarkDatabase,
        private val downloadHandler: DownloadHandler,
        private val clipboardManager: ClipboardManager,
        @DatabaseScheduler private val databaseScheduler: Scheduler) {

    fun longPressOnlineUrl(activity: WebActivity, url: String, userAgent: String, type: Int) {
        CLog.i("url = $url, type = $type")
        if (type == WebView.HitTestResult.UNKNOWN_TYPE) return

        val floatMenu = FloatMenu(activity).apply {
            if (type != WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE
                    && type != WebView.HitTestResult.IMAGE_TYPE) addDelete(R.id.menu_item_link_download)
        }
        floatMenu.showMenuLayout(R.menu.menu_long_press, null, object : FloatMenu.OnItemClickListener {
            override fun onClick(v: View, item: FloatMenuItem, select: Boolean?) {
                when (item.id) {
                    R.id.menu_item_link_newtab -> activity.handleNewTab(url, true)
                    R.id.menu_item_link_backtab -> activity.handleNewTab(url, false)
                    R.id.menu_item_add_share -> ShareUtils.shareUrl(activity, url)
                    R.id.menu_item_link_copy -> clipboardManager.copy(url)
                    R.id.menu_item_link_download -> downloadHandler.start(activity, url)
                    R.id.menu_item_link_adblock -> {
                        activity.showingWeb().loadJavascript(JSAdBlock(activity, url).getJs())
                        EventBus.getDefault().post(AdEvent(AdEvent.TYPE_ADD_AD, url))
                    }
                }
            }
        })
    }

    fun bookmarkItemAction(activity: WebActivity, entry: Bookmark, from: Int = Bookmark.FROM_MARK) {
        val floatMenu = FloatMenu(activity)
        floatMenu.showMenuLayout(R.menu.menu_bookmark_press, listOf(), object : FloatMenu.OnItemClickListener {
            override fun onClick(v: View, item: FloatMenuItem, select: Boolean?) {
                when (item.id) {
                    R.id.menu_book_new_tab -> activity.handleNewTab(entry.url, true)
                    R.id.menu_book_new_bgtab -> activity.handleNewTab(entry.url, false)
                    R.id.menu_book_share -> ShareUtils.shareUrl(activity, entry.url, entry.title)
                    R.id.menu_book_copy_link -> clipboardManager.copy(entry.url)
                    R.id.menu_book_rm -> {
                        bookmarkManager.deleteItem(entry)
                                .subscribeOn(databaseScheduler)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe { success ->
                                    if (success) {
                                        EventBus.getDefault().post(DrawerEvent(DrawerEvent.BOOKMARK_DATA_CHANGED, 0))
                                        if (from == Bookmark.FROM_MARK) {
                                            EventBus.getDefault().post(SEvent(SEvent.TYPE_HOMEPAGE_INIT_AND_RELOAD))
                                        }
                                    }
                                }
                    }
                    R.id.menu_edit_item -> {
                        val intent = PageItemEdit.genIntent(activity, HashMap<String, String>().apply {
                            this[activity.getString(R.string.hint_title)] = entry.title
                            this[activity.getString(R.string.hint_url)] = entry.url
                        }, entry)
                        activity.startActivityForResult(intent, PageItemEdit.ACTIVITY_RESULT_EDITBOOKMARK)
                    }
                    R.id.menu_edit_all -> {
                        if (from == Bookmark.FROM_MARK) {
                            val intent = PageMarkBatchEdit.genIntent(activity, activity.getString(R.string.action_edit), from)
                            activity.startActivity(intent)
                        } else {
                            EventBus.getDefault().post(DrawerEvent(DrawerEvent.BOOKMARK_BACH_EDIT, 0))
                        }
                    }
                }
            }
        })
    }

}