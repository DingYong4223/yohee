package com.fula.yohee.download

import android.app.Activity
import android.webkit.MimeTypeMap
import androidx.annotation.MainThread
import com.fula.CLog
import com.fula.base.ToolUtils
import com.fula.base.iview.BasePage
import com.fula.base.tool.HttpHelper
import com.fula.base.util.VideoFormatUtil
import com.fula.downloader.DownStatus
import com.fula.downloader.DownloadListener
import com.fula.downloader.DownloadTask
import com.fula.downloader.file.DownloadImpl
import com.fula.downloader.file.Extra
import com.fula.downloader.m3u8.M3U8Downloader
import com.fula.downloader.m3u8.M3u8Info
import com.fula.permission.PermissionsManager
import com.fula.permission.PermissionsResultAction
import com.fula.util.NetUtils
import com.fula.yohee.R
import com.fula.yohee.YoheeApp
import com.fula.yohee.bean.VideoInfo
import com.fula.yohee.database.DownloadEntry
import com.fula.yohee.database.DownloadsDatabase
import com.fula.yohee.di.DatabaseScheduler
import com.fula.yohee.di.NetworkScheduler
import com.fula.yohee.dialog.DialogHelper
import com.fula.yohee.dialog.DialogItem
import com.fula.yohee.extensions.shortToast
import com.fula.yohee.preference.UserPreferences
import com.fula.yohee.ui.page.PageDownload
import com.fula.yohee.utils.FileUtils
import com.fula.yohee.utils.UrlUtils
import com.fula.yohee.utils.Utils
import com.fula.yohee.utils.YoheePermission
import io.reactivex.Maybe
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Handle download requests
 */
@Singleton
class DownloadHandler @Inject constructor() : DownloadListener {

    @Inject
    lateinit var userPrefer: UserPreferences
    @Inject
    lateinit var downloadsDB: DownloadsDatabase
    @Inject
    @field:DatabaseScheduler
    lateinit var dbScheduler: Scheduler
    @Inject
    @field:NetworkScheduler
    lateinit var netScheduler: Scheduler
    @Inject
    lateinit var m3u8Downloader: M3U8Downloader
    private val listeners by lazy { mutableListOf<DownloadListener>() }

    init {
        YoheeApp.injector.inject(this)
        m3u8Downloader.registerListener(this)

        val dis = downloadsDB.getAllDownloads()
                .subscribeOn(dbScheduler)
                .subscribe { list ->
                    if (list.isEmpty()) return@subscribe
                    list.filter {
                        it.status == DownStatus.STATUS_DOWNLOADING
                    }.forEach {
                        downloadsDB.updateItemStatus(it.url, DownStatus.STATUS_PAUSED)
                                .subscribeOn(dbScheduler)
                                .subscribe()
                    }
                }
    }

    fun addListener(l: DownloadListener) {
        listeners += l
    }

    fun removeListener(l: DownloadListener) {
        if (listeners.contains(l)) listeners.remove(l)
    }

    fun destroy() {
        listeners.clear()
        DownloadImpl.getInstance().pauseAll()
    }

    /**下载文件*/
    fun start(context: Activity, url: String) {
        if (!FileUtils.checkSdCardStatus(context)) return

        if (userPrefer.wifiDownload) {
            val ntype = NetUtils.getNetworkState(context)
            if (NetUtils.NETWORK_WIFI != ntype && NetUtils.NETWORK_NONE != ntype) {
                DialogHelper.showOkCancelDialog(context, com.fula.yohee.R.string.alert_warm, com.fula.yohee.R.string.data_net_tip,
                        positiveButton = DialogItem(title = com.fula.yohee.R.string.action_yes) {
                            fileDownloadAfterInfoget(context, url)
                        }
                )
                return
            }
        }
        fileDownloadAfterInfoget(context, url)
    }

    /**下载视频*/
    fun start(context: Activity, videoInfo: VideoInfo, nameNoSuffix: String) {
        if (!FileUtils.checkSdCardStatus(context)) return

        CLog.i("start nameNoSuffix: $nameNoSuffix")
        if (videoInfo.videoFormat.name.endsWith(M3u8Info.M3U8SURFFIX)) { //m3u8视频
            m3u8fileDownload(context, videoInfo.url, "$nameNoSuffix.m3u8")
        } else {
            CLog.i("start download directly...")
            val fileName = "$nameNoSuffix.${videoInfo.videoFormat.name}"
            val downItem = DownloadEntry(videoInfo.url, fileName, videoInfo.size, type = DownloadEntry.TYPE_VIDEO_FILE)
            dis?.dispose()
            dis = downloadsDB.insertItem(downItem)
                    .subscribeOn(dbScheduler)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { isadd ->
                        if (isadd) {
                            enqueueDownload(videoInfo.url, videoInfo.videoFormat.name)

                            context.shortToast(context.getString(R.string.download_start) + fileName)
                            val intent = BasePage.genTitleIntent(context, PageDownload::class.java, R.string.action_downloads)
                            context.startActivity(intent)
                        } else {
                            context.shortToast(R.string.download_aready_exist)
                        }
                    }
        }
    }

    private fun m3u8fileDownload(context: Activity, url: String, fileName: String) {
        CLog.i("m3u8 file downbload...")
        downloadsDB.insertItem(DownloadEntry(url, fileName, 0, 0, type = DownloadEntry.TYPE_VIDEO_FILE))
                .subscribeOn(dbScheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { it ->
                    if (it) {
                        context.apply {
                            m3u8Downloader.download(url)

                            shortToast(getString(R.string.download_start) + fileName)
                            val intent = BasePage.genTitleIntent(this, PageDownload::class.java, R.string.action_downloads)
                            startActivity(intent)
                        }
                    } else {
                        context.shortToast(R.string.exist_download)
                    }
                }
    }

    private fun fileDownloadAfterInfoget(context: Activity, url: String) {
        CLog.i("start download from http request mimetype...")
        dis?.dispose()
        dis = Maybe.fromCallable {
            val headRequest = HttpHelper.performHeadRequest(url)
            CLog.i("header = ${headRequest.headerMap}")
            return@fromCallable headRequest.headerMap
        }.subscribeOn(netScheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { header ->
                    CLog.i("header = $header")
                    header?.let {
                        if (!header.containsKey("Content-Type")) {
                            return@subscribe CLog.i("request fail...")
                        }
                        var mimeType = it.getValue("Content-Type")[0]
                        CLog.i("get header mimetype = $mimeType")
                        val contentDisposition = it["Content-Disposition"]?.toString()
                        if (mimeType.equals(FileUtils.MIMETYPE_TEXT_PLAIN, ignoreCase = true) || mimeType.equals(FileUtils.MIMETYPE_APPLICATION_OCTET_STREAM, ignoreCase = true)) {
                            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(Utils.getFileExtension(url))
                        }
                        val filename = UrlUtils.guessFileName(url, contentDisposition, mimeType)
                        CLog.i("filename = $filename")
                        val videoFormat = VideoFormatUtil.detectVideoFormat(url, header.getValue("Content-Type").toString())
                        if (videoFormat != null) {
                            CLog.i("viddo detect: $filename")
                            val videoInfo = VideoInfo()
                            videoInfo.url = url
                            videoInfo.videoFormat = videoFormat
                            return@subscribe start(context, videoInfo, FileUtils.getNoSuffixName(filename))
                        }
                        val downItem = DownloadEntry(url, filename, HttpHelper.getHeaderLength(header))
                        downloadsDB.insertItem(downItem)
                                .subscribeOn(dbScheduler)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe { isadd ->
                                    if (isadd) {
                                        enqueueDownload(url, FileUtils.getSuffix(filename))

                                        context.shortToast(context.getString(R.string.download_start) + filename)
                                        val intent = BasePage.genTitleIntent(context, PageDownload::class.java, R.string.action_downloads)
                                        context.startActivity(intent)
                                    } else {
                                        context.shortToast(R.string.download_aready_exist)
                                    }
                                }
                    }
                }
    }

    private fun enqueueDownload(url: String, suffix: String) {
        PermissionsManager.requestPermissionsIfNecessaryForResult(YoheeApp.mainActivity, YoheePermission.STORAGE_READ_WRITE, object : PermissionsResultAction() {
            override fun onGranted() {
                DownloadImpl.getInstance()
                        .with(YoheeApp.app)
                        .target(FileUtils.createYohheFile(FileUtils.DOWNLOAD_FILE, "${ToolUtils.md5(url)}.$suffix"))
                        .setUniquePath(false)
                        .setForceDownload(true)
                        .url(url)
                        .enqueue(this@DownloadHandler)
            }
        })
    }

    private var dis: Disposable? = null

    override fun onStart(task: DownloadTask) {
        CLog.i("url = ${task.url}, allLength = ${task.mTotalsLength}")
        downloadsDB.updateItemStatus(task.url, task.status)
                .subscribeOn(dbScheduler)
                .subscribe()
        listeners.forEach {
            it.onStart(task)
        }
    }

    private val writeTime: MutableMap<String, Long> = mutableMapOf()
    @MainThread
    override fun onProgress(url: String, downloaded: Long, length: Long, speed: Long) {
        val progress = if (length > 0L) {
            (downloaded / length.toFloat() * 100).toInt()
        } else 0
        CLog.i("onProgress:$progress, speed:$speed, url:$url")
        val lastTime = writeTime[url] ?: 0
        val nowTime = System.currentTimeMillis()
        if (nowTime - lastTime > WRITE_DB_TIMESPAN || progress >= 100) {
            writeTime[url] = nowTime
            downloadsDB.updateItemProgress(url, downloaded, length)
                    .subscribeOn(dbScheduler)
                    .subscribe()
        }
        listeners.forEach {
            it.onProgress(url, downloaded, length, speed)
        }
    }

    override fun onResult(status: Int, url: String, extra: Extra): Boolean {
        CLog.i("onResult status:$status url:$url")
        var lastStatus = status
        val fileName = UrlUtils.getNameFromUrl(url)
        if (fileName.endsWith(M3u8Info.M3U8SURFFIX)) { //m3u8视频
            if (m3u8Downloader.checkExist(url)) lastStatus = DownStatus.STATUS_COMPLETED
        }

        downloadsDB.updateItemStatus(url, lastStatus)
                .subscribeOn(dbScheduler)
                .subscribe()
        listeners.forEach {
            it.onResult(lastStatus, url, extra)
        }
        return true
    }

    fun pauseUrl(entry: DownloadEntry) {
        if (DownloadImpl.getInstance().exist(entry.url)) {
            DownloadImpl.getInstance().pause(entry.url)
        } else {
            enqueueDownload(entry.url, FileUtils.getSuffix(entry.title))
        }
    }

    fun resumeUrl(list: List<DownloadEntry>) {
        list.forEach { entry ->
            if (DownloadImpl.getInstance().exist(entry.url)) {
                DownloadImpl.getInstance().resume(entry.url)
            } else {
                enqueueDownload(entry.url, entry.title)
            }
        }
    }

    private var pausedByNetChange = false
    fun netWorkChange(context: Activity) {
        val ntype = NetUtils.getNetworkState(context)
        CLog.i("ntype = $ntype")
        if (NetUtils.NETWORK_NONE == ntype) {
            return
        }
        when (ntype) {
            NetUtils.NETWORK_WIFI -> {
                if (pausedByNetChange && DownloadImpl.getInstance().resumeAll()) {
                    context.shortToast(com.fula.yohee.R.string.download_onlywifi_resume)
                }
            }
            else -> {
                if (userPrefer.wifiDownload) {
                    if (DownloadImpl.getInstance().pauseAll().isNotEmpty()) {
                        pausedByNetChange = true
                        context.shortToast(com.fula.yohee.R.string.download_onlywifi_pause)
                    }
                }
            }
        }
    }

    companion object {
        const val WRITE_DB_TIMESPAN = 5000
    }

}
