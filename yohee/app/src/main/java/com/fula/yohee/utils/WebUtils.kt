package com.fula.yohee.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.webkit.*
import com.fula.permission.PermissionsManager
import com.fula.permission.PermissionsResultAction
import com.fula.yohee.R
import com.fula.yohee.extensions.longSnackbar
import com.fula.yohee.extensions.snackbar
import com.fula.CLog
import com.fula.yohee.database.HistoryDatabase
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.File

object WebUtils {

    private const val FILE_NAME_MAX_LENGTH = 128

    fun clearCache(context: Context) {
        WebView(requireNotNull(context)).apply {
            clearCache(true)
            destroy()
        }
    }

    fun clearCookies(context: Context) {
        val c = CookieManager.getInstance()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            c.removeAllCookies(null)
        } else {
            CookieSyncManager.createInstance(context)
            c.removeAllCookie()
        }
    }

    fun clearWebStorage() {
        WebStorage.getInstance().deleteAllData()
    }

    fun clearHistory(context: Context,
                     historyRepository: HistoryDatabase,
                     databaseScheduler: Scheduler) {
        historyRepository.deleteHistory()
                .subscribeOn(databaseScheduler)
                .subscribe()
        val webViewDatabase = WebViewDatabase.getInstance(context)
        webViewDatabase.clearFormData()
        webViewDatabase.clearHttpAuthUsernamePassword()
        Utils.trimCache(context)
    }

    fun clearCache(view: WebView?) {
        if (view == null) return
        view.clearCache(true)
    }

    var dis: Disposable? = null
    fun saveWeb(context: Activity, webView: WebView) {
        if (UrlUtils.isGenUrl(webView.url)) {
            context.snackbar(R.string.error_tip_download)
            return
        }
        var title = webView.title
        dis?.dispose()
        dis = Single.fromCallable {
            title?.let {
                if (it.length > FILE_NAME_MAX_LENGTH) title = it.substring(it.length - FILE_NAME_MAX_LENGTH)
                if (it.contains(File.separator)) {
                    title = it.substring(it.lastIndexOf(File.separator))
                }
            }
            var stringBuilder = StringBuilder()
            stringBuilder.append(title).append(".mht")
            var file = FileUtils.createYohheFile(FileUtils.DOWNLOAD_WEB, stringBuilder.toString())
            var i = 0
            while (file.exists()) {
                i++
                stringBuilder = StringBuilder()
                stringBuilder.append(title)
                stringBuilder.append("_")
                stringBuilder.append(i)
                stringBuilder.append(".mht")
                file = FileUtils.createYohheFile(FileUtils.DOWNLOAD_WEB, stringBuilder.toString())
            }
            return@fromCallable file
        }.subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({ file ->
                    CLog.i("file = $file")
                    PermissionsManager.requestPermissionsIfNecessaryForResult(context, YoheePermission.STORAGE_READ_WRITE, object : PermissionsResultAction() {
                        override fun onGranted() {
                            try {
                                webView.saveWebArchive(file.absolutePath)
                                val tip = context.getString(R.string.save_page) + context.getString(R.string.tip_success)
                                val append = context.getString(R.string.offpage_where_tip)
                                context.longSnackbar("$tip, $append")
                            } catch (e: Exception) {
                                context.snackbar(context.getString(R.string.save_page) + context.getString(R.string.tip_fail))
                            }
                        }
                    })
                }){}
    }

}
