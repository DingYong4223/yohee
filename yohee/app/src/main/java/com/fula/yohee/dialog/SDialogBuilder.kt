package com.fula.yohee.dialog

import android.content.ClipboardManager
import android.view.View
import com.fula.CLog
import com.fula.yohee.MenuPop
import com.fula.yohee.R
import com.fula.yohee.database.BookmarkDatabase
import com.fula.yohee.database.HistoryDatabase
import com.fula.yohee.di.DatabaseScheduler
import com.fula.yohee.eventbus.DrawerEvent
import com.fula.yohee.eventbus.SEvent
import com.fula.yohee.extensions.copy
import com.fula.yohee.settings.UserSetting
import com.fula.yohee.ui.activity.WebActivity
import com.fula.yohee.uiwigit.floatmenu.FloatMenu
import com.fula.yohee.uiwigit.floatmenu.FloatMenuItem
import com.fula.yohee.utils.ShareUtils
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

/**
 * A builder of various dialogs.
 */
class SDialogBuilder @Inject constructor(
        private val bookmarkManager: BookmarkDatabase,
        private val historyModel: HistoryDatabase,
        private val clipboardManager: ClipboardManager,
        @DatabaseScheduler private val databaseScheduler: Scheduler) {

    fun longPressBookmarkItem(activity: WebActivity, menuPop: MenuPop, url: String) {
//        if (UrlUtils.isBookmarkUrl(url)) {
//            val uri = url.toUri()
//            val filename = requireNotNull(uri.lastPathSegment) { "Last segment should always exist for bookmark file" }
//            val folderTitle = filename.substring(0, filename.length - BookmarkPageFactory.FILENAME.length - 1)
//            showBookmarkFolderLongPressedDialog(activity)
//        } else {
            bookmarkManager.findItemForUrl(url)
                    .subscribeOn(databaseScheduler)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { historyItem ->
                        menuPop.bookmarkItemAction(activity, historyItem)
                    }
//        }
    }

//    /**
//     * Show the appropriated dialog for the long pressed link.
//     * @param activity used to showListDialog the dialog
//     * @param url      the long pressed url
//     */
//    fun longPressDownloadUrl(context: WebActivity, url: String) {
//        val floatMenu = FloatMenu(context)
//        floatMenu.showMenuLayout(R.menu.menu_download, null, object : FloatMenu.OnItemClickListener {
//            override fun onClick(v: View, item: FloatMenuItem, select: Boolean?) {
//                CLog.i("menu clicked...$select")
//                when (item.id) {
//                    R.id.delete_item -> {
//                        downloadsModel.deleteDownload(url)
//                                .subscribeOn(databaseScheduler)
//                                .observeOn(AndroidSchedulers.mainThread())
//                                .subscribe{ it ->
//                                    if(it) context.toast(R.string.handle_success)
//                                }
//                    }
//                    R.id.delete_all_item -> {
//                        downloadsModel.deleteAllDownloads()
//                                .subscribeOn(databaseScheduler)
//                                .observeOn(AndroidSchedulers.mainThread())
//                                .subscribe(context::handleDownloadDeleted)
//                    }
//                }
//            }
//        })
//    }

    private fun showRenameFolderDialog(
            activity: WebActivity
    ) = DialogHelper.showEditDialog(activity,
            R.string.title_rename_folder,
            R.string.hint_title,
            R.string.action_ok) {}

    fun longPressHistoryUrl(activity: WebActivity, url: String) {
        val floatMenu = FloatMenu(activity)
        floatMenu.showMenuLayout(R.menu.menu_history, listOf(), object : FloatMenu.OnItemClickListener {
            override fun onClick(v: View, item: FloatMenuItem, select: Boolean?) {
                when (item.id) {
                    R.id.menu_history_new_tab -> activity.handleNewTab(url, true)
                    R.id.menu_history_new_bgtab -> activity.handleNewTab(url, false)
                    R.id.menu_history_share -> ShareUtils.shareUrl(activity, url)
                    R.id.menu_history_copy_link -> clipboardManager.copy(url)
                    R.id.menu_history_rm -> {
                        historyModel.deleteHistoryEntry(url)
                                .subscribeOn(databaseScheduler)
                                .observeOn(databaseScheduler)
                                .subscribeBy(onComplete = {
                                    CLog.i("event bus send: HistoryEvent")
                                    EventBus.getDefault().post(DrawerEvent(DrawerEvent.HISTORY_DATA_REMOVED, 0))
                                })
                    }
                    R.id.menu_history_clear -> EventBus.getDefault().post(SEvent(SEvent.TYPE_CLEAR_HISTORY))
                    R.id.menu_history_refresh -> EventBus.getDefault().post(DrawerEvent(DrawerEvent.HISTORY_DATA_REMOVED, UserSetting.NO_VALUE))
                }
            }
        })
    }

//    fun showLongPressImageDialog(
//            activity: WebActivity,
//            url: String,
//            userAgent: String
//    ) = DialogHelper.showListDialog(activity, url.replace(HTTP, ""),
//            DialogItem(title = R.string.dialog_open_new_tab) {
//                activity.handleNewTab(NewTab.FOREGROUND, url)
//            },
//            DialogItem(title = R.string.dialog_open_background_tab) {
//                activity.handleNewTab(NewTab.BACKGROUND, url)
//            },
//            DialogItem(
//                    title = R.string.dialog_open_incognito_tab,
//                    isConditionMet = activity is MainActivity
//            ) {
//                activity.handleNewTab(NewTab.INCOGNITO, url)
//            },
//            DialogItem(title = R.string.action_share) {
//                IntentUtils(activity).shareUrl(url, null)
//            },
//            DialogItem(title = R.string.dialog_copy_link) {
//                clipboardManager.copy(url)
//            },
//            DialogItem(title = R.string.dialog_download_image) {
//                downloadHandler.start(activity, userPrefer, url, userAgent, "attachment", "", "")
//            })

//    fun showLongPressLinkDialog(
//            activity: WebActivity,
//            url: String
//    ) = DialogHelper.showListDialog(activity, url,
//            DialogItem(title = R.string.dialog_open_new_tab) {
//                activity.handleNewTab(NewTab.FOREGROUND, url)
//            },
//            DialogItem(title = R.string.dialog_open_background_tab) {
//                activity.handleNewTab(NewTab.BACKGROUND, url)
//            },
//            DialogItem(
//                    title = R.string.dialog_open_incognito_tab,
//                    isConditionMet = activity is MainActivity
//            ) {
//                activity.handleNewTab(NewTab.INCOGNITO, url)
//            },
//            DialogItem(title = R.string.action_share) {
//                IntentUtils(activity).shareUrl(url, null)
//            },
//            DialogItem(title = R.string.dialog_copy_link) {
//                clipboardManager.copy(url)
//            })

}
