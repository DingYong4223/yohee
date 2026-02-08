package com.fula.yohee.download

import android.text.format.Formatter
import android.webkit.URLUtil
import com.fula.permission.PermissionsManager
import com.fula.permission.PermissionsResultAction
import com.fula.yohee.R
import com.fula.yohee.YoheeApp
import com.fula.yohee.dialog.DialogHelper
import com.fula.yohee.dialog.DialogItem
import com.fula.yohee.preference.UserPreferences
import com.fula.yohee.ui.activity.WebActivity
import com.fula.yohee.utils.YoheePermission
import javax.inject.Inject

class SDownloadListener(private val activity: WebActivity) : android.webkit.DownloadListener {

    @Inject
    lateinit var userPrefer: UserPreferences
    @Inject
    lateinit var downloadHandler: DownloadHandler

    init {
        YoheeApp.injector.inject(this)
    }

    override fun onDownloadStart(url: String, userAgent: String,
                                 disc: String, mimetype: String, contentLength: Long) {
        PermissionsManager.requestPermissionsIfNecessaryForResult(activity, YoheePermission.STORAGE_READ_WRITE,
                object : PermissionsResultAction() {
                    override fun onGranted() {
                        val fileName = URLUtil.guessFileName(url, disc, mimetype)
                        val message = activity.getString(R.string.dialog_download, if (contentLength > 0) {
                            Formatter.formatFileSize(activity, contentLength)
                        } else {
                            activity.getString(R.string.unknown_size)
                        })
                        DialogHelper.showOkCancelDialog(activity, fileName, message, DialogItem(title = R.string.action_yes) {
                            downloadHandler.start(activity, url)
                        }, DialogItem(title = R.string.action_no) {})
                    }
                })
    }
}
