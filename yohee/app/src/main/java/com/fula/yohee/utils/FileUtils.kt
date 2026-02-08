package com.fula.yohee.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Parcel
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.webkit.MimeTypeMap
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.fula.CLog
import com.fula.permission.PermissionsManager
import com.fula.permission.PermissionsResultAction
import com.fula.yohee.BuildConfig
import com.fula.yohee.YoheeApp
import com.fula.yohee.constant.Setting
import com.fula.yohee.extensions.tryUse
import io.reactivex.Completable
import java.io.*
import java.util.*


/**
 * A utility class containing helpful methods
 * pertaining to file storage.
 */
object FileUtils {

    private val APP_ROOT = "${if (BuildConfig.DEBUG) "" else "."}com.fula.yohee"
    const val DOWNLOAD_WEB = "Download/web"
    const val DOWNLOAD_FILE = "Download/file"
    const val SAVEPATH = "Save"
    const val TEMPPATH = "Temp"
    const val SAVE_BOOKMARK = "BOOKMARK.YH"
    const val SAVE_SETTING = "SETTINGS.YH"
    const val SAVE_SETTING_SUFFIX = ".jar"
    const val SAVE_SETTINGS = "YoheeSettings$SAVE_SETTING_SUFFIX"
    private const val LOGPATH = "Log"
    const val MIMETYPE_TEXT_PLAIN = "text/plain"
    const val MIMETYPE_APPLICATION_OCTET_STREAM = "application/octet-stream"
    private const val BUNDLE_STORAGE = "SAVED_TABS.parcel"

    private val DEFAULT_DOWNLOAD = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
    fun getDownloadWebPath(): String = getExternal(APP_ROOT, DOWNLOAD_WEB).path
    fun getTempFolder(): File = getExternal(APP_ROOT, TEMPPATH)
    fun getSavePath(): String = getSaveFolder().path
    fun getSaveFolder(): File = getExternal(APP_ROOT, SAVEPATH)

    private fun getExternal(root: String, subDir: String? = null): File {
        if (TextUtils.isEmpty(subDir)) {
            return File(DEFAULT_DOWNLOAD + File.separator + root).apply {
                checkAndCreatePath(this)
            }
        } else {
            File(DEFAULT_DOWNLOAD + File.separator + root).apply {
                checkAndCreatePath(this)
                return File(path + File.separator + subDir).apply {
                    checkAndCreatePath(this)
                }
            }
        }
    }

    private fun checkAndCreatePath(file: File) {
        if (!file.isDirectory) {
            file.mkdirs().apply {
                CLog.i("create sub url($this): ${file.path}")
            }
        }
    }

    fun isFile(path: String): Boolean = File(path).isFile

    /**
     * @param path 相对路径，会在app根目录下创建路径
     * @param fileName 文件名称
     * */
    fun createYohheFile(path: String?, fileName: String): File {
        return File(getExternal(APP_ROOT, path), fileName)
    }

    /**
     * @param path 相对路径，会在app根目录下创建路径
     * */
    fun createYoheeFolder(path: String?): File {
        return getExternal(APP_ROOT, path)
    }

    fun checkSdCardStatus(context: Activity): Boolean {
        val status = Environment.getExternalStorageState()
        if (status != Environment.MEDIA_MOUNTED) {
            val title: Int
            val msg: String
            // Check to see if the SDCard is busy, same as the music app
            if (status == Environment.MEDIA_SHARED) {
                msg = context.getString(com.fula.yohee.R.string.download_sdcard_busy_dlg_msg)
                title = com.fula.yohee.R.string.download_sdcard_busy_dlg_title
            } else {
                msg = context.getString(com.fula.yohee.R.string.download_no_sdcard_dlg_msg)
                title = com.fula.yohee.R.string.download_no_sdcard_dlg_title
            }
            val dialog = AlertDialog.Builder(context).setTitle(title)
                    .setIcon(android.R.drawable.ic_dialog_alert).setMessage(msg)
                    .setPositiveButton(com.fula.yohee.R.string.action_ok, null).show()
            Setting.applyModeToWindow(context, dialog.window)
            Setting.setDialogSize(context, dialog.window)
            return false
        }
        return true
    }

    /**
     * Writes a bundle to persistent storage in the files directory
     * using the specified file name. This method is a blocking
     * operation.
     *
     * @param bundle the bundle to store in persistent storage.
     * @param name   the name of the file to store the bundle in.
     */
    fun writeBundleToStorage(bundle: Bundle): Completable {
        CLog.i("write state to disk...")
        return Completable.fromAction {
            val outputFile = File(YoheeApp.app.filesDir, BUNDLE_STORAGE)
            var outputStream: FileOutputStream? = null
            try {
                outputStream = FileOutputStream(outputFile)
                val parcel = Parcel.obtain()
                parcel.writeBundle(bundle)
                outputStream.write(parcel.marshall())
                outputStream.flush()
                parcel.recycle()
            } catch (e: IOException) {
                CLog.i("error, write bundle to storage")
            } finally {
                Utils.close(outputStream)
            }
        }
    }

    /**
     * Use this method to delete the bundle with the specified name.
     * This is a blocking call and should be used within a worker
     * thread unless immediate deletion is necessary.
     *
     * @param name the name of the file.
     */
    fun deleteBundleInStorage() {
        CLog.i("delete state from disk...")
        val outputFile = File(YoheeApp.app.filesDir, BUNDLE_STORAGE)
        if (outputFile.exists()) {
            outputFile.delete()
        }
    }

    /**
     * Reads a bundle from the file with the specified
     * name in the persistent storage files directory.
     * This method is a blocking operation.
     *
     * @param name the name of the file to read from.
     * @return a valid Bundle loaded using the system class loader
     * or null if the method was unable to read the Bundle from storage.
     */
    fun readBundleFromStorage(): Bundle? {
        CLog.i("read state from disk...")
        val inputFile = File(YoheeApp.app.filesDir, BUNDLE_STORAGE)
        var inputStream: FileInputStream? = null
        try {
            inputStream = FileInputStream(inputFile)
            val parcel = Parcel.obtain()
            val data = ByteArray(inputStream.channel.size().toInt())
            inputStream.read(data, 0, data.size)
            parcel.unmarshall(data, 0, data.size)
            parcel.setDataPosition(0)
            val out = parcel.readBundle(ClassLoader.getSystemClassLoader())
            out!!.putAll(out)
            parcel.recycle()
            return out
        } catch (e: FileNotFoundException) {
            CLog.i("error, read bundle from storage: FileNotFoundException")
        } catch (e: IOException) {
            CLog.i("error, read bundle from storage: IOException")
        } finally {
            inputFile.delete()
            Utils.close(inputStream)
        }
        return null
    }

    /**
     * Writes a stacktrace to the downloads folder with
     * the following filename: [EXCEPTION]_[TIME OF CRASH IN MILLIS].txt
     * @param throwable the Throwable to log to external storage
     */
    fun writeCrashToStorage(throwable: Throwable) = writeExceptionToDisk(throwable)

    fun writeExceptionToDisk(throwable: Throwable, fn: String? = null) {
        if (BuildConfig.DEBUG) {
            CLog.i("+++write exception to file+++")
            throwable.printStackTrace()
        }
        PermissionsManager.requestPermissionsIfNecessaryForResult(YoheeApp.mainActivity, YoheePermission.STORAGE_READ_WRITE, object : PermissionsResultAction() {
            override fun onGranted() {
                val fileName = fn
                        ?: "${throwable.javaClass.simpleName}_${Utils.stamp2Date(System.currentTimeMillis(), "yy-MM-dd_HHmmss")}.txt"
                val outputFile = createYohheFile(LOGPATH, fileName)
                FileOutputStream(outputFile).tryUse {
                    throwable.printStackTrace(PrintStream(it))
                    it.flush()
                }
            }
        })
    }

    /**
     * Converts megabytes to bytes.
     *
     * @param megaBytes the number of megabytes.
     * @return the converted bytes.
     */
    fun megabytesToBytes(megaBytes: Long): Long {
        return megaBytes * 1024 * 1024
    }

    fun getPath(context: Context, uri: Uri): String? {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else if (isDownloadsDocument(uri)) {
                //                Log.i(TAG,"isDownloadsDocument***"+uri.toString());
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))

                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                //                Log.i(TAG,"isMediaDocument***"+uri.toString());
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                var contentUri: Uri? = null
                when (type) {
                    "image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    "video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    "audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }// MediaProvider
        } else if ("content".equals(uri.scheme, ignoreCase = true)) { // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.lastPathSegment

            if (isQQMediaDocument(uri)) {
                val path = uri.path
                val fileDir = Environment.getExternalStorageDirectory()
                val file = File(fileDir, path.substring("/QQBrowser".length))
                return if (file.exists()) file.toString() else null
            }

            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }// File
        return null
    }

    /**
     * 使用第三方qq文件管理器打开
     *
     * @param uri
     *
     * @return
     */
    private fun isQQMediaDocument(uri: Uri): Boolean {
        return "com.tencent.mtt.fileprovider" == uri.authority
    }

    /**
     * @param uri
     * The Uri to check.
     *
     * @return Whether the Uri authority is Google Photos.
     */
    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private fun getDataColumn(context: Context, uri: Uri?, selection: String?,
                              selectionArgs: Array<String>?): String? {

        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(columnIndex)
            }
        } finally {
            cursor?.close()
        }
        return null
    }


    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * 打开文件
     * @param file
     */
    fun openFile(context: Context, file: File) {
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.action = Intent.ACTION_VIEW
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(Utils.getFileExtension(file.name))
        val data: Uri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//判断版本大于等于7.0
            // "sven.com.fileprovider.fileprovider"即是在清单文件中配置的authorities，通过FileProvider创建一个content类型的Uri
            data = FileProvider.getUriForFile(context,"${context.packageName}.fileprovider", file)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            data = Uri.fromFile(file)
        }
        intent.setDataAndType(data, mimeType)
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    fun byte2FitMemorySize(byteNum: Long): String {
        val b = 1024
        val kb = 1024 * 1024
        val gb = 1024 * 1024 * 1024
        return when {
            byteNum < 0 -> ""
            byteNum < b -> String.format(Locale.getDefault(), "%.2fB", byteNum.toDouble())
            byteNum < kb -> String.format(Locale.getDefault(), "%.2fKB", byteNum.toDouble() / b)
            byteNum < gb -> String.format(Locale.getDefault(), "%.2fMB", byteNum.toDouble() / kb)
            else -> String.format(Locale.getDefault(), "%.2fGB", byteNum.toDouble() / (gb))
        }
    }

    fun getSuffix(fileName: String): String {
        if (!fileName.contains(".")) return ""
        return fileName.substring(fileName.lastIndexOf(".") + 1)
    }

    fun getNoSuffixName(fileName: String): String {
        if (!fileName.contains(".")) return fileName
        return fileName.substring(0, fileName.lastIndexOf("."))
    }

    fun getNameWithNoSuffix(fileName: String): String {
        if (!fileName.contains(".")) return fileName
        return fileName.substring(0, fileName.lastIndexOf("."))
    }

}
