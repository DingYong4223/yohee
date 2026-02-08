package com.fula.yohee.view

import android.Manifest
import android.annotation.TargetApi
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.webkit.*
import com.fula.CLog
import com.fula.permission.PermissionsManager
import com.fula.permission.PermissionsResultAction
import com.fula.yohee.R
import com.fula.yohee.YoheeApp
import com.fula.yohee.di.DiskScheduler
import com.fula.yohee.dialog.DialogHelper
import com.fula.yohee.dialog.DialogItem
import com.fula.yohee.favicon.FaviconModel
import com.fula.yohee.preference.UserPreferences
import com.fula.yohee.ui.WebViewController
import com.fula.yohee.ui.activity.WebActivity
import com.fula.yohee.view.webrtc.WebRtcPermissionsModel
import com.fula.yohee.view.webrtc.WebRtcPermissionsView
import io.reactivex.Scheduler
import javax.inject.Inject

class YoheeChromeClient(
        private val activity: WebActivity,
        private val webViewController: WebViewController) : WebChromeClient(), WebRtcPermissionsView {

    private val geoLocationPermissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    @Inject internal lateinit var faviconModel: FaviconModel
    @Inject internal lateinit var userPreferences: UserPreferences
    @Inject internal lateinit var webRtcPermissionsModel: WebRtcPermissionsModel
    @Inject @field:DiskScheduler internal lateinit var diskScheduler: Scheduler

    init {
        YoheeApp.injector.inject(this)
    }

    override fun onProgressChanged(view: WebView, newProgress: Int) {
        webViewController.onProgressChanged(view, newProgress)
        if (activity.isTabFront(webViewController)) {
            activity.updateProgress(newProgress.toFloat())
        }
    }

    override fun onReceivedIcon(view: WebView, icon: Bitmap) {
        CLog.i("receive icon...")
        webViewController.updateWebInfo(view.url, icon)
        cacheFavicon(view.url, icon)
    }

    /**
     * Naive caching of the favicon according to the domain name of the URL
     * @param icon the icon to cache
     */
    private fun cacheFavicon(url: String?, icon: Bitmap?) {
        if (icon == null || url == null) {
            return
        }
        CLog.i("cache favicon...")
        faviconModel.cacheFaviconForUrl(icon, url)
            .subscribeOn(diskScheduler)
            .subscribe()
    }


    override fun onReceivedTitle(view: WebView?, title: String?) {
        CLog.i("title = $title")
        val lastTitle = if (title != null && !title.isEmpty()) {
            title
        } else {
            activity.getString(R.string.untitled)
        }
        webViewController.onReceivedTitle(view, lastTitle)
        activity.onReceivedTitle(view, lastTitle)
    }

    override fun requestPermissions(permissions: Set<String>, onGrant: (Boolean) -> Unit) {
        val missingPermissions = permissions
            .filter { PermissionsManager.hasPermission(activity, it) }

        if (missingPermissions.isEmpty()) {
            onGrant(true)
        } else {
            PermissionsManager.requestPermissionsIfNecessaryForResult(
                activity,
                missingPermissions.toTypedArray(),
                object : PermissionsResultAction() {
                    override fun onGranted() = onGrant(true)
                    override fun onDenied(permission: String) = onGrant(false)
                }
            )
        }
    }

    override fun requestResources(source: String, resources: Array<String>, onGrant: (Boolean) -> Unit) {
        activity.runOnUiThread {
            val resString = resources.joinToString(separator = "\n")
            DialogHelper.showOkCancelDialog(
                activity = activity,
                title = R.string.title_permission_request,
                message = R.string.message_permission_request,
                args = arrayOf(source, resString),
                positiveButton = DialogItem(title = R.string.action_allow) { onGrant(true) },
                negativeButton = DialogItem(title = R.string.action_dont_allow) { onGrant(false) }//,
            )
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onPermissionRequest(request: PermissionRequest) {
        if (userPreferences.webRtcEnabled) {
            webRtcPermissionsModel.requestPermission(request, this)
        } else {
            request.deny()
        }
    }

    override fun onGeolocationPermissionsShowPrompt(origin: String,
                                                    callback: GeolocationPermissions.Callback) =
        PermissionsManager.requestPermissionsIfNecessaryForResult(activity, geoLocationPermissions, object : PermissionsResultAction() {
            override fun onGranted() {
                val org = if (origin.length > 50) {
                    "${origin.subSequence(0, 50)}..."
                } else {
                    origin
                }
                DialogHelper.showOkCancelDialog(activity, R.string.location, org + activity.getString(R.string.message_location),
                        positiveButton = DialogItem(title = R.string.action_yes) {
                            callback.invoke(origin, true, true)
                        },
                        negativeButton = DialogItem(title = R.string.action_dont_allow) {
                            callback.invoke(origin, false, true)
                        }
                )
            }
        })

    override fun onCreateWindow(view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message): Boolean {
        CLog.i("url = ${view.url}, isDialog = $isDialog, isUserGesture = $isUserGesture, resultMsg = $resultMsg")
        activity.onCreateWindow(resultMsg)
        return true
    }

    override fun onCloseWindow(window: WebView) = activity.onCloseWindow(webViewController)

    @Suppress("unused", "UNUSED_PARAMETER")
    fun openFileChooser(uploadMsg: ValueCallback<Uri>) = activity.openFileChooser(uploadMsg)

    @Suppress("unused", "UNUSED_PARAMETER")
    fun openFileChooser(uploadMsg: ValueCallback<Uri>, acceptType: String) = activity.openFileChooser(uploadMsg)

    @Suppress("unused", "UNUSED_PARAMETER")
    fun openFileChooser(uploadMsg: ValueCallback<Uri>, acceptType: String, capture: String) = activity.openFileChooser(uploadMsg)

    override fun onShowFileChooser(webView: WebView, filePathCallback: ValueCallback<Array<Uri>>,
                                   fileChooserParams: FileChooserParams): Boolean {
        activity.showFileChooser(filePathCallback)
        return true
    }

    /**
     * Obtain an image that is displayed as a placeholder on a video until the video has appInitialized
     * and can begin loading.
     * @return a Bitmap that can be used as a place holder for videos.
     */
    override fun getDefaultVideoPoster(): Bitmap? {
        return BitmapFactory.decodeResource(activity.resources, android.R.drawable.spinner_background)
    }

    /**
     * Inflate a view to send to a WebViewController when it needs to display a video and has to
     * showListDialog a loading dialog. Inflates a downed view and returns it.
     * @return A view that should be used to display the state
     * of a video's loading downed.
     */
    override fun getVideoLoadingProgressView(): View =
        LayoutInflater.from(activity).inflate(R.layout.video_loading_progress, null)

    override fun onHideCustomView() = activity.onHideCustomView()

    override fun onShowCustomView(view: View, callback: CustomViewCallback) =
        activity.onShowCustomView(view, callback)

    override fun onShowCustomView(view: View, reqOrientation: Int, callback: CustomViewCallback) =
        activity.onShowCustomView(view, callback, reqOrientation)

}
