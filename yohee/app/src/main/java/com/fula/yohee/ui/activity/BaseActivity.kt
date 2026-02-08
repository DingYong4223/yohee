package com.fula.yohee.ui.activity

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.webkit.ValueCallback
import androidx.core.net.toUri
import com.fula.CLog
import com.fula.base.BitmapStrategy
import com.fula.frame.FulaBaseActivity
import com.fula.permission.PermissionsManager
import com.fula.yohee.R
import com.fula.yohee.YoheeApp
import com.fula.yohee.constant.Setting
import com.fula.yohee.di.DatabaseScheduler
import com.fula.yohee.di.DiskScheduler
import com.fula.yohee.preference.UserPreferences
import com.fula.yohee.settings.UserSetting
import com.fula.yohee.utils.ThemeUtils
import com.fula.yohee.utils.Utils
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import org.greenrobot.eventbus.EventBus
import java.io.IOException
import javax.inject.Inject


abstract class BaseActivity : FulaBaseActivity() {

    companion object {
        private const val FILE_CHOOSER_REQUEST_CODE = 1111
    }

    @Inject
    lateinit var userPrefer: UserPreferences
    @Inject
    @field:DiskScheduler
    lateinit var diskScheduler: Scheduler
    @Inject
    @field:DatabaseScheduler
    lateinit var dbScheduler: Scheduler

    private var isResumed = false
    private var bgBlurBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        CLog.i("onCreate...")
        YoheeApp.injector.inject(this)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        //window.setWindowAnimations(R.style.ActionScaleAnimation)
        requestedOrientation = userPrefer.screenRotate
        super.onCreate(savedInstanceState)
        syncThemeBgUI()
        Setting.applyModeToWindow(this, window)
    }

    override fun onResume() {
        super.onResume()
        CLog.i("onResume...")
        isResumed = true
    }

    protected open fun onWindowVisibleToUserAfterResume() = Unit

    private var cameraPhotoPath: String? = null
    fun showFileChooser(filePathCallback: ValueCallback<Array<Uri>>) {
        CLog.i("showListDialog file chooser...")
        this.h5fileListener?.onReceiveValue(null)
        this.h5fileListener = filePathCallback
        val intentArray: Array<Intent> = try {
            arrayOf(Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra("PhotoPath", cameraPhotoPath)
                putExtra(
                        MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(Utils.createImageFile().also { file ->
                            cameraPhotoPath = "file:${file.absolutePath}"
                        })
                )
            })
        } catch (ex: IOException) {
            CLog.i("error, Unable to create Image File")
            emptyArray()
        }
        startActivityForResult(Intent(Intent.ACTION_CHOOSER).apply {
            putExtra(Intent.EXTRA_INTENT, Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            })
            putExtra(Intent.EXTRA_TITLE, "Image Chooser")
            putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
        }, FILE_CHOOSER_REQUEST_CODE)
    }

    /**
     * opens a file chooser
     * param ValueCallback is the message from the WebView indicating a file chooser
     * should be opened
     */
    fun openFileChooser(uploadMsg: ValueCallback<Uri>) {
        CLog.i("open file...")
        uploadMsgListener = uploadMsg
        startActivityForResult(Intent.createChooser(Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }, getString(R.string.title_file_chooser)), FILE_CHOOSER_REQUEST_CODE)
    }

    private var uploadMsgListener: ValueCallback<Uri>? = null
    private var h5fileListener: ValueCallback<Array<Uri>>? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        when (requestCode) {
            FILE_CHOOSER_REQUEST_CODE -> {
                val results: Array<Uri>? = if (resultCode == Activity.RESULT_OK) {
                    intent?.dataString?.let { arrayOf(it.toUri()) }
                } else {
                    null
                }
                if (results?.isNotEmpty() == true) {
                    uploadMsgListener?.onReceiveValue(results[0])
                } else {
                    uploadMsgListener?.onReceiveValue(null)
                }
                h5fileListener?.onReceiveValue(results)
                uploadMsgListener = null
                h5fileListener = null
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        CLog.i("permission changed...")
        PermissionsManager.notifyPermissionsChange(permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private var bimapDis: Disposable? = null
    fun syncThemeBgUI() {
        //window.statusBarColor = Color.RED //ThemeUtils.getStatusBarColor(this)
        val res = when (userPrefer.useTheme) {
            UserSetting.NO_VALUE -> {
                return window.setBackgroundDrawableResource(R.mipmap.img_white)
            }
            UserSetting.THEME_SPRING -> R.mipmap.bg_spring
            UserSetting.THEME_SUMMER -> R.mipmap.bg_summer
            UserSetting.THEME_AUTUMUN -> R.mipmap.bg_autumn
            UserSetting.THEME_WINTER -> R.mipmap.bg_winter
            else -> ThemeUtils.getThemeBg()
        }
        bimapDis?.dispose()
        bimapDis = BitmapStrategy.getBlurCacheBitmap(this, dbScheduler, res, 5, 10)
                .subscribeOn(diskScheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    window.setBackgroundDrawable(BitmapDrawable(resources, it))
                    bgBlurBitmap?.recycle()
                    bgBlurBitmap = it
                }
        /*val option = RequestOptions()
                .override(DeviceUtils.getScreenWidth(this) / 3, DeviceUtils.getScreenHeight(this) / 3)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontAnimate()
                .skipMemoryCache(true)
                .optionalTransform(BlurTransformation(5, 3))
        Glide.with(this).load(res).apply(option)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        shortToast("resource = ${(resource as BitmapDrawable).bitmap}")
                        window.setBackgroundDrawable(resource)
                        return false
                    }
                })
                .preload()*/
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && isResumed) {
            isResumed = false
            onWindowVisibleToUserAfterResume()
        }
    }

    override fun onDestroy() {
        bimapDis?.dispose()
        window.setBackgroundDrawable(null)
        bgBlurBitmap?.recycle()
        unregisterEventBus()
        super.onDestroy()
    }

    protected val isTablet: Boolean
        get() = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_XLARGE

    protected fun registerEventBus() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    private fun unregisterEventBus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    protected fun restart() {
        finish()
        startActivity(Intent(this, javaClass))
    }

}
