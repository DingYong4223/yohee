package com.fula.yohee.utils

import android.app.Activity
import com.fula.CLog
import com.fula.permission.PermissionsManager
import com.fula.permission.PermissionsResultAction
import com.fula.yohee.R
import com.fula.yohee.YoheeApp
import com.fula.yohee.database.Bookmark
import com.fula.yohee.database.BookmarkDatabase
import com.fula.yohee.di.DatabaseScheduler
import com.fula.yohee.di.DiskScheduler
import com.fula.yohee.eventbus.SEvent
import com.fula.yohee.extensions.shortToast
import com.fula.yohee.extensions.snackbar
import com.fula.yohee.extensions.tryUse
import com.fula.yohee.pb.Info
import com.fula.yohee.preference.UserPreferences
import com.fula.yohee.ui.page.SettingAdapter
import com.fula.yohee.utils.FileUtils.writeExceptionToDisk
import com.google.protobuf.MessageLite
import io.reactivex.Maybe
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject

/**
 * A utility class containing helpful methods
 * pertaining to file storage.
 */
class DiskSave(val context: Activity) {

    init {
        YoheeApp.injector.inject(this)
    }

    @Inject
    internal lateinit var bookmarkModel: BookmarkDatabase

    @Inject
    @field:DatabaseScheduler
    internal lateinit var dbScheduler: Scheduler

    @Inject
    @field:DiskScheduler
    internal lateinit var diskScheduler: Scheduler

    private fun file2Bookmark(file: File): Maybe<Info.BookMark?> = Maybe.fromCallable {
        try {
            FileInputStream(file).use {
                return@fromCallable Info.BookMark.parseFrom(ByteArray(it.available()).apply {
                    it.read(this)
                })
            }
        } catch (e: Exception) {
            writeExceptionToDisk(e)
            e.printStackTrace()
        }
        return@fromCallable null
    }

    private fun file2UserPrefer(file: File): Maybe<Info.UserPrefer?> = Maybe.fromCallable {
        try {
            FileInputStream(file).use {
                return@fromCallable Info.UserPrefer.parseFrom(ByteArray(it.available()).apply {
                    it.read(this)
                })
            }
        } catch (e: Exception) {
            writeExceptionToDisk(e)
            e.printStackTrace()
        }
        return@fromCallable null
    }

    /**保存书签或历史记录至文件中*/
    fun data2Disk(save: MessageLite, fname: String): Single<Unit> = Single.fromCallable {
        PermissionsManager.requestPermissionsIfNecessaryForResult(YoheeApp.mainActivity, YoheePermission.STORAGE_READ_WRITE, object : PermissionsResultAction() {
            override fun onGranted() {
                try {
                    val ff = FileUtils.createYohheFile(FileUtils.SAVEPATH, fname)
                    FileOutputStream(ff).tryUse {
                        it.write(save.toByteArray())
                    }
                } catch (e: Exception) {
                    writeExceptionToDisk(e)
                    e.printStackTrace()
                }
            }
        })
    }

    fun zipAndShareSave(context: Activity, showSnake: Boolean = true) = Single.fromCallable {
        CLog.i("starting zip settings...")
        val saveFolder = FileUtils.getSavePath()
        val zipFile = FileUtils.createYohheFile("", FileUtils.SAVE_SETTINGS)
        return@fromCallable ZipUtils.zipFolder(saveFolder, zipFile.absolutePath)
    }
            .subscribeOn(diskScheduler)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                CLog.i("zip result: $it")
                if (it && showSnake) {
                    context.snackbar(R.string.data_copy_save_tip, R.string.action_share) {
                        val zipFile = FileUtils.createYohheFile("", FileUtils.SAVE_SETTINGS)
                        ShareUtils.shareFile(context, zipFile.path)
                    }
                }
            }

    fun exportSettins(userPrefer: UserPreferences): Disposable = Single.just(userPrefer.exportSetting())
            .observeOn(diskScheduler)
            .doAfterSuccess {
                exportData2Disk(it, FileUtils.SAVE_SETTING)
            }.subscribe()

//    fun exportSettins(userPrefer: UserPreferences) = Single.fromCallable<Info.UserPrefer> {
//        return@fromCallable userPrefer.exportSetting()
//    }.observeOn(diskScheduler)
//            .doAfterSuccess {
//                exportData2Disk(it, FileUtils.SAVE_SETTING)
//            }.subscribe()

    fun exportBookmark(): Disposable = bookmarkModel.getSortItems()
            .observeOn(dbScheduler)
            .doAfterSuccess {
                val bookMarkBuilder = Info.BookMark.newBuilder()
                bookMarkBuilder.addAllData(it.map {
                    Info.BookMark.Item.newBuilder().apply {
                        this.title = it.title
                        this.url = it.url
                        this.folder = it.folder
                        this.position = it.position
                        this.type = it.type
                    }.build()
                })
                CLog.i("bookmark size = ${bookMarkBuilder.dataCount}")
                exportData2Disk(bookMarkBuilder.build(), FileUtils.SAVE_BOOKMARK)
            }.subscribe()

    private fun exportData2Disk(save: MessageLite, fname: String) = data2Disk(save, fname)
            .observeOn(diskScheduler)
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                CLog.i("cacheSet: $it, fileName = $fname")
            }

    fun importSave2App(zipfile: File, userPrefer: UserPreferences) {
        PermissionsManager.requestPermissionsIfNecessaryForResult(YoheeApp.mainActivity, YoheePermission.STORAGE_READ_WRITE, object : PermissionsResultAction() {
            override fun onGranted() {
                Single.fromCallable {
                    val tempFilder = FileUtils.getTempFolder()
                    if (tempFilder.exists() && tempFilder.isDirectory
                            && tempFilder.list().isNotEmpty()) tempFilder.deleteRecursively().apply {
                        CLog.i("delete temp: $this")
                    }
                    ZipUtils.unZipFolder(zipfile.path, FileUtils.getTempFolder().path)
                }.subscribeOn(diskScheduler)
                        .observeOn(dbScheduler)
                        .subscribe({
                            CLog.i("unzip finished...")
                            importData2Db(userPrefer)
                        }){}
            }
        })
    }

    private fun importData2Db(userPrefer: UserPreferences) {
        val unzip = FileUtils.createYoheeFolder(FileUtils.TEMPPATH + File.separator + FileUtils.SAVEPATH)
        unzip.listFiles().asList().forEach {
            CLog.i("file name = ${it.path}")
            when (it.name) {
                FileUtils.SAVE_BOOKMARK -> importBookmarkNoReload(it)
                FileUtils.SAVE_SETTING -> importSettingsAndReload(it, userPrefer)
            }
        }
    }

    /**重载书签到应用中*/
    private fun importSettingsAndReload(file: File, userPrefer: UserPreferences) = file2UserPrefer(file)
            .subscribeOn(diskScheduler)
            .doOnSuccess {
                it?.apply {
                    userPrefer.importSetting(this)
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { up ->
                up?.let {
                    context.shortToast(R.string.setting_import)
                    EventBus.getDefault().post(SEvent(SEvent.TYPE_HOMEPAGE_INIT_AND_RELOAD))
                    EventBus.getDefault().post(SEvent(SettingAdapter.INDEX_THEME_CHOICE).apply { intArg = userPrefer.useTheme })
                }
            }


    private fun importBookmarkNoReload(file: File) = file2Bookmark(file)
            .subscribeOn(diskScheduler)
            .subscribe { bookmarks ->
                CLog.i("bookmarks = $bookmarks")
                bookmarks?.apply {
                    bookmarkModel.insertItems(dataList.map { Bookmark(it.url, it.title, it.folder, it.position, it.type) })
                            .subscribeOn(dbScheduler)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeBy {
                                CLog.i("import success...")
                                context.shortToast(R.string.message_import)
                            }
                }
            }

}
