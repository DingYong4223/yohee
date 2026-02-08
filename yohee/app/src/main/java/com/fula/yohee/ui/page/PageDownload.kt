package com.fula.yohee.ui.page

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.*
import android.widget.LinearLayout
import androidx.annotation.MainThread
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.fula.CLog
import com.fula.base.ToolUtils
import com.fula.base.ui.video.GSYVideoActivity
import com.fula.downloader.DownStatus
import com.fula.downloader.DownloadListener
import com.fula.downloader.DownloadTask
import com.fula.downloader.file.Extra
import com.fula.downloader.m3u8.M3U8Downloader
import com.fula.downloader.m3u8.M3u8Info
import com.fula.permission.PermissionsManager
import com.fula.permission.PermissionsResultAction
import com.fula.util.NetUtils
import com.fula.util.ViewUnit
import com.fula.yohee.R
import com.fula.yohee.YoheeApp
import com.fula.yohee.database.DownloadEntry
import com.fula.yohee.database.DownloadsDatabase
import com.fula.yohee.di.DatabaseScheduler
import com.fula.yohee.di.DiskScheduler
import com.fula.yohee.dialog.DialogHelper
import com.fula.yohee.dialog.DialogItem
import com.fula.yohee.dialog.InfomationDlg
import com.fula.yohee.download.DownloadHandler
import com.fula.yohee.extensions.color
import com.fula.yohee.extensions.copy
import com.fula.yohee.extensions.shortToast
import com.fula.yohee.ui.bilogic.SelectModel
import com.fula.yohee.utils.FileUtils
import com.fula.yohee.utils.UrlUtils
import com.fula.yohee.utils.Utils
import com.fula.yohee.utils.YoheePermission
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.com_list_item.view.*
import kotlinx.android.synthetic.main.page_recycle_view_layout_notoolbar.view.*
import kotlinx.android.synthetic.main.toolbar.view.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap


/**
 * @Date: 2018-09-11
 * @author: delanding
 */
class PageDownload : BasePageSelect() {

    @Inject
    internal lateinit var downloadsDB: DownloadsDatabase
    @Inject
    internal lateinit var downloadhandler: DownloadHandler
    @Inject
    @field:DatabaseScheduler
    internal lateinit var dbScheduler: Scheduler
    private var subscriber: Disposable? = null
    private val viewMap by lazy { HashMap<String, View>() }
    @Inject
    @field:DiskScheduler
    internal lateinit var diskScheduler: Scheduler
    @Inject
    lateinit var m3u8Downloader: M3U8Downloader
    private val KEY_TITLE by lazy { getString(R.string.hint_title) }
    private val KEY_URL by lazy { getString(R.string.hint_url) }

    private val listener = object : DownloadListener {

        override fun onStart(task: DownloadTask) {
            initData2UI()
        }

        @MainThread
        override fun onProgress(url: String, downloaded: Long, length: Long, usedTime: Long) {
            mView.post {
                updateViewHolderValue(url, downloaded, length)
            }
            if ((downloaded * 1f / length * 100).toInt() % 5 == 0) {
                downloadsDB.updateItemProgress(url, downloaded, length)
                        .subscribeOn(dbScheduler)
                        .subscribe()
            }
            Utils.getFromList(mAdapter.data) {
                (it.obj as DownloadEntry).url == url
            }?.let {
                (it.value.obj as DownloadEntry).apply {
                    downed = downloaded
                    this.length = length
                }
            }
        }

        override fun onResult(status: Int, url: String, extra: Extra): Boolean {
            initData2UI()
            return true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val cmenu = super.onCreateOptionsMenu(menu)
        menu!!.getItem(1).apply {
            setTitle(R.string.action_add_download)
            isVisible = true
        }
        return cmenu
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val omenu = super.onOptionsItemSelected(item)
        when (item.itemId) {
            R.id.toolbar_menu1 -> {
                val intent = PageItemEdit.genIntent(mContext, java.util.HashMap<String, String>().apply {
                    this[KEY_URL] = ""
                }, R.string.action_add_download)
                mContext.startActivityForResult(intent, PageItemEdit.ACTIVITY_RESULT_EDITBOOKMARK)
            }
        }
        return omenu
    }

    override fun initPage(inflater: LayoutInflater, container: ViewGroup) {
        super.initPage(container, R.layout.page_recycle_view_layout)
        YoheeApp.injector.inject(this)
        mContext.setSupportActionBar(mView.toolSetingbar as Toolbar)
        initToolBar(title)
        downloadhandler.addListener(listener)
        m3u8Downloader.registerListener(listener)
        mView.recycle_view.apply {
            layoutManager = LinearLayoutManager(mContext)
            isLongPressDragEnabled = true
            adapter = mAdapter
        }
        initData2UI()
    }

    override fun initSelectView() {
        super.initSelectView()
        editMenuHelper.apply {
            initMenuItem(R.id.menu_item_3, l, intArrayOf(R.string.detail_info, R.string.detail_info), Color.BLUE).apply {
                textView.isEnabled = false
            }
        }
    }

    private val l = View.OnClickListener {
        when (it.id) {
            R.id.menu_item_3 -> {
                val list = mAdapter.data.filter { it.isCheck }
                if (list.size == 1) {
                    val item = list[0].obj as DownloadEntry
                    val file = FileUtils.createYohheFile(FileUtils.DOWNLOAD_FILE, getDownFileName(list[0].urlTxt ,list[0].title))
                    val infos = mutableListOf<SelectModel>().apply {
                        add(SelectModel(null, mContext.getString(R.string.type), getUrlType(mContext, item.type, item.title)))
                        add(SelectModel(null, mContext.getString(R.string.size), getFileSizeTxt(if (item.length >= 0) item.length else item.downed)))
                        add(SelectModel(null, mContext.getString(R.string.date), SimpleDateFormat("yyyy-MM-dd").format(Date(file.lastModified()))))
                        add(SelectModel(null, mContext.getString(R.string.link_url), list[0].urlTxt))
                    }

                    InfomationDlg(mContext, R.string.detail_info, item.title, infos, DialogItem(title = R.string.rename) {
                        if (isEdit()) {
                            editMenuHelper.swichEdit()
                            mAdapter.notifyDataSetChanged()
                        }
                        val intent = PageItemEdit.genIntent(mContext, HashMap<String, String>().apply {
                            this[KEY_TITLE] = FileUtils.getNameWithNoSuffix(item.title)
                        }, list[0])
                        mContext.startActivityForResult(intent, PageItemEdit.ACTIVITY_RESULT_DOWNLOAD)
                    }, DialogItem(title = R.string.dialog_copy_link) {
                        val clipManager = mContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipManager.copy(item.url)
                        mContext.shortToast(mContext.getString(R.string.copyed_to_clip, item.url))
                    }).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (intent == null) {
            return CLog.i("no intent back...")
        }
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PageItemEdit.ACTIVITY_RESULT_DOWNLOAD -> { //新建下载
                    val map = intent.getSerializableExtra(PageItemEdit.KEY_MAP) as java.util.HashMap<String, String>
                    val url = map[KEY_URL]
                    if (url.isNullOrEmpty()) {
                        return mContext.shortToast(R.string.url_invalide)
                    }
                    downloadhandler.start(mContext, url)
                }
                PageItemEdit.ACTIVITY_RESULT_DOWNLOAD_RENAME -> { //重命名
                    val old = intent.getSerializableExtra(PageItemEdit.KEY_OBJ) as SelectModel
                    val item = old.obj as DownloadEntry

                    val map = intent.getSerializableExtra(PageItemEdit.KEY_MAP) as HashMap<String, String>
                    val newName = map[KEY_TITLE]
                    CLog.i("newName = $newName")
                    newName?.let {
                        val map = HashMap<String, String>().apply {
                            this[DownloadsDatabase.KEY_TITLE] = newName + "." + FileUtils.getSuffix(item.title)
                        }
                        downloadsDB.update(item.url, map)
                                .subscribeOn(dbScheduler)
                                .subscribeOn(AndroidSchedulers.mainThread())
                                .subscribe {
                                    CLog.i("update status = $it")
                                    initData2UI()
                                }
                    }
                }
            }
        }
    }

    private fun getFileSizeTxt(length: Long): String = if (length <= 0) mContext.getString(R.string.unkown) else FileUtils.byte2FitMemorySize(length)

    private fun updateViewHolderValue(url: String, downed: Long, length: Long) {
        viewMap[url]?.let {
            it.item_subtitle.text = mContext.getString(R.string.downloading_info, FileUtils.byte2FitMemorySize(downed) + "/" + getFileSizeTxt(length))
            updateProgress(it, if (downed > 0 && length > 0) {
                (downed * 1.0f / length * 100).toInt()
            } else {
                0
            })
        }
    }

    private fun updateProgress(cv: View, progress: Int) {
        cv.top_progress.visibility = View.VISIBLE
        cv.top_progress.progress = progress
    }

    private val margin by lazy { ViewUnit.dp2px(5f) }
    override fun initVisit(cv: View) {
        super.initVisit(cv)
        cv.item_logo_img.apply {
            (layoutParams as LinearLayout.LayoutParams).apply {
                leftMargin = margin
                rightMargin = margin
            }
            requestLayout()
        }
    }

    override fun bindVisit(cv: View, item: SelectModel) {
        super.bindVisit(cv, item)
        val model = item.obj
        if (model is DownloadEntry) {
            viewMap[model.url] = cv
            when (model.status) {
                DownStatus.STATUS_PENDDING -> {
                    cv.item_subtitle.setText(R.string.download_inqueue)
                }
                DownStatus.STATUS_DOWNLOADING -> {
                    cv.item_subtitle.text = mContext.getString(R.string.downloading_info, FileUtils.byte2FitMemorySize(model.downed) + "/" + getFileSizeTxt(model.length))
                }
                DownStatus.STOPPING -> cv.item_subtitle.setText(R.string.is_stopping)
                DownStatus.STATUS_COMPLETED -> {
                    cv.item_subtitle.text = FileUtils.byte2FitMemorySize(if (model.length <= 0) model.downed else model.length)
                }
                DownStatus.STATUS_PAUSED -> {
                    cv.item_subtitle.text = getString(R.string.paused)
                }
                else -> {
                    cv.item_subtitle.text = getString(R.string.download_fail)
                }
            }
            updateProgress(cv, if (DownStatus.STATUS_COMPLETED == model.status) {
                100
            } else {
                if (model.length > 0 && model.downed > 0) {
                    (model.downed * 1.0f / model.length * 100).toInt()
                } else {
                    0
                }
            })
        }
    }

    override fun handleItemClick(v: View, index: Int) {
        if (!isEdit()) {
            val item = mAdapter.itemAt(index).obj as DownloadEntry
            CLog.i("item type = ${item.type}")
            if (item.type == DownloadEntry.TYPE_VIDEO_FILE) {
                handleVideoClick(item, index)
            } else {
                handleFileClick(item, index)
            }
        }
    }

    override fun selectListener(data: List<SelectModel>) {
        data.filter { it.isCheck }.apply {
            CLog.i("size = ${this.size}")
            editMenuHelper.getItem(R.id.menu_item_3)?.textView?.let {
                val enable = this.size == 1
                it.isEnabled = enable
                it.setTextColor(mContext.color(if (enable) R.color.default_blue else android.R.color.darker_gray))
            }
        }
    }

    private fun handleFileClick(item: DownloadEntry, index: Int) {
        when (item.status) {
            DownStatus.STATUS_DOWNLOADING -> {
                downloadhandler.pauseUrl(item)
            }
            DownStatus.STATUS_COMPLETED -> {
                PermissionsManager.requestPermissionsIfNecessaryForResult(YoheeApp.mainActivity, YoheePermission.STORAGE_READ_WRITE, object : PermissionsResultAction() {
                    override fun onGranted() {
                        val file = FileUtils.createYohheFile(FileUtils.DOWNLOAD_FILE, getDownFileName(item.url, item.title))
                        FileUtils.openFile(mContext, file)
                    }
                })
            }
            else -> {
                resumeDownload(listOf(item))
            }
        }
    }

    private fun handleVideoClick(item: DownloadEntry, index: Int) {
        if (item.title.endsWith(M3u8Info.M3U8SURFFIX) || item.url.endsWith(M3u8Info.M3U8SURFFIX)) {
            if (m3u8Downloader.checkExist(item.url)) {
                val intent = GSYVideoActivity.genIntent(mContext, item.title, item.url)
                mContext.startActivity(intent)
            } else {
                m3u8Downloader.download(item.url)
            }
        } else {
            when (item.status) {
                DownStatus.STOPPING -> {
                    mContext.shortToast(R.string.is_stopping)
                }
                DownStatus.STATUS_DOWNLOADING -> {
                    downloadhandler.pauseUrl(item)
                    item.status = DownStatus.STOPPING
                    mAdapter.notifyItemChanged(index)
                }
                DownStatus.STATUS_COMPLETED -> {
                    val item = mAdapter.itemAt(index)
                    val file = FileUtils.createYohheFile(FileUtils.DOWNLOAD_FILE, getDownFileName(item.urlTxt, item.title))
                    if (!file.exists()) {
                        mContext.shortToast(R.string.message_open_download_fail)
                        return
                    }
                    mContext.startActivity(GSYVideoActivity.genIntent(mContext, item.title, Uri.fromFile(file).toString()))
                }
                else -> {
                    resumeDownload(listOf(item))
                }
            }
        }
    }

    private fun resumeDownload(list: List<DownloadEntry>) {
        if (getUserPrefer().wifiDownload) {
            val ntype = NetUtils.getNetworkState(mContext)
            if (NetUtils.NETWORK_WIFI != ntype && NetUtils.NETWORK_NONE != ntype) {
                DialogHelper.showOkCancelDialog(mContext, R.string.alert_warm, R.string.data_net_tip,
                        positiveButton = DialogItem(title = R.string.action_yes) {
                            downloadhandler.resumeUrl(list)
                        }
                )
                return
            }
        }
        downloadhandler.resumeUrl(list)
    }

    override fun deleteAction() {
        PermissionsManager.requestPermissionsIfNecessaryForResult(mContext as Activity, YoheePermission.STORAGE_READ_WRITE, object : PermissionsResultAction() {
            override fun onGranted() {
                DialogHelper.showOkCancelDialog(mContext as Activity, R.string.alert_warm, R.string.alert_clear_all,
                        positiveButton = DialogItem(title = R.string.action_yes) {
                            downloadsDB.deleteDownload(mAdapter.data.filter { it.isCheck }.map {
                                it.obj as DownloadEntry
                            }).doOnSuccess {
                                mAdapter.data.filter { it.isCheck }.forEach {
                                    if (it.urlTxt.endsWith(M3u8Info.M3U8SURFFIX)) {
                                        m3u8Downloader.cancelAndDelete(mAdapter.data.filter { it.isCheck }.map {
                                            it.urlTxt
                                        }, null)
                                    } else {
                                        FileUtils.createYohheFile(FileUtils.DOWNLOAD_FILE, getDownFileName(it.urlTxt, it.title)).delete()
                                    }
                                }
                            }
                                    .subscribeOn(diskScheduler)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({ initData2UI() }) {}
                        }
                )
            }
        })
    }

    private fun initData2UI() {
        subscriber?.dispose()
        subscriber = downloadsDB.getAllDownloads()
                .subscribeOn(dbScheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { lists ->
                    mAdapter.initData(lists.map {
                        SelectModel(it, it.title, it.url, iconRes = getUrlRes(it.type, it.title))
                    })
                }
    }

    override fun onDestroy() {
        m3u8Downloader.unregisterListener(listener)

        downloadhandler.removeListener(listener)
        val downing = mAdapter.data.filter {
            (it.obj as DownloadEntry).status == DownStatus.STATUS_DOWNLOADING
        }
        if (downing.isNotEmpty()) {
            mContext.shortToast(R.string.downloading_back)
        }
        super.onDestroy()
    }

    companion object {
        fun getUrlRes(type: Int, title: String): Int {
            return when {
                type == DownloadEntry.TYPE_VIDEO_FILE -> R.drawable.ic_video
                UrlUtils.isImage(title) -> R.drawable.ic_image
                title.endsWith("pdf") -> R.drawable.ic_pdf
                title.endsWith("apk") -> R.drawable.ic_apk
                else -> R.drawable.ic_file
            }
        }

        fun getUrlType(contxt: Context, type: Int, title: String): String {
            return contxt.getString(when {
                type == DownloadEntry.TYPE_VIDEO_FILE -> R.string.video
                UrlUtils.isImage(title) -> R.string.image
                title.endsWith("pdf") -> R.string.file_pdf
                title.endsWith("apk") -> R.string.file_apk
                else -> R.string.file
            })
        }

        fun getDownFileName(url: String, title: String) =
            ToolUtils.md5(url) + "." + FileUtils.getSuffix(title)


    }
}