/*
 * Copyright 2014 A.C.R. Development
 */
package com.fula.yohee.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import android.webkit.URLUtil
import com.fula.CLog
import com.fula.yohee.R
import com.fula.yohee.database.HistoryEntry
import com.fula.yohee.extensions.snackbar
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.text.SimpleDateFormat
import java.util.*

object Utils {

    private var lastClick = 0L
    fun fastClick(timeSpan: Long = 500): Boolean {
        val detaClick = System.currentTimeMillis() - lastClick
        return if (detaClick < timeSpan) {
            true
        } else {
            lastClick = System.currentTimeMillis()
            false
        }
    }

    fun stamp2Date(miniSeconds: Long, fmt: String? = null): String {
        var format = fmt
        if (TextUtils.isEmpty(format)) {
            format = "yy-MM-dd HH:mm:ss"
        }
        val sdf = SimpleDateFormat(format)
        return sdf.format(Date(miniSeconds))
    }

    fun obj2Int(intObj: Any?, def: Int): Int {
        if (null == intObj) return def
        try {
            return intObj as? Int ?: Integer.parseInt(intObj.toString() + "")
        } catch (e: Exception) {
        }
        return def
    }

    /**
     * Creates a new intent that can launch the email
     * app with a subject, address, body, and cc. It
     * is used to handle mail:to links.
     *
     * @param address the address to send the email to.
     * @param subject the subject of the email.
     * @param body    the body of the email.
     * @param cc      extra addresses to CC.
     * @return a valid intent.
     */
    fun newEmailIntent(address: String, subject: String,
                       body: String, cc: String): Intent {
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
        intent.putExtra(Intent.EXTRA_TEXT, body)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_CC, cc)
        intent.type = "message/rfc822"
        return intent
    }

    /**
     * Extracts the domain name from a URL.
     * NOTE: Should be used for display only.
     *
     * @param url the URL to extract the domain from.
     * @return the domain name, or the URL if the domain
     * could not be extracted. The domain name may include
     * HTTPS if the URL is an SSL supported URL.
     */
    fun getDomainName(u: String?): String {
        var url = u
        if (url == null || url.isEmpty()) return ""

        val ssl = URLUtil.isHttpsUrl(url)
        val index = url.indexOf('/', 8)
        if (index != -1) {
            url = url.substring(0, index)
        }
        val uri: URI
        var domain: String?
        try {
            uri = URI(url)
            domain = uri.host
        } catch (e: URISyntaxException) {
            CLog.i("Unable to parse URI")
            domain = null
        }
        if (domain == null || domain.isEmpty()) {
            return url
        }
        return if (ssl)
            "https://$domain"
        else
            if (domain.startsWith("www.")) domain.substring(4) else domain
    }

    fun trimCache(context: Context) {
        try {
            val dir = context.cacheDir

            if (dir != null && dir.isDirectory) {
                deleteDir(dir)
            }
        } catch (ignored: Exception) {

        }

    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()
            for (aChildren in children) {
                val success = deleteDir(File(dir, aChildren))
                if (!success) {
                    return false
                }
            }
        }
        return dir != null && dir.delete()
    }

    fun isColorTooDark(color: Int): Boolean {
        val RED_CHANNEL = 16
        val GREEN_CHANNEL = 8
        //final byte BLUE_CHANNEL = 0;

        val r = ((color shr RED_CHANNEL and 0xff).toFloat() * 0.3f).toInt() and 0xff
        val g = ((color shr GREEN_CHANNEL and 0xff).toFloat() * 0.59).toInt() and 0xff
        val b = ((color /* >> BLUE_CHANNEL */ and 0xff).toFloat() * 0.11).toInt() and 0xff
        val gr = r + g + b and 0xff
        val gray = gr /* << BLUE_CHANNEL */ + (gr shl GREEN_CHANNEL) + (gr shl RED_CHANNEL)

        return gray < 0x727272
    }

    fun mixTwoColors(color1: Int, color2: Int, amount: Float): Int {
        val ALPHA_CHANNEL = 24
        val RED_CHANNEL = 16
        val GREEN_CHANNEL = 8
        //final byte BLUE_CHANNEL = 0;
        val inverseAmount = 1.0f - amount
        val r = ((color1 shr RED_CHANNEL and 0xff).toFloat() * amount + (color2 shr RED_CHANNEL and 0xff).toFloat() * inverseAmount).toInt() and 0xff
        val g = ((color1 shr GREEN_CHANNEL and 0xff).toFloat() * amount + (color2 shr GREEN_CHANNEL and 0xff).toFloat() * inverseAmount).toInt() and 0xff
        val b = ((color1 and 0xff).toFloat() * amount + (color2 and 0xff).toFloat() * inverseAmount).toInt() and 0xff
        return 0xff shl ALPHA_CHANNEL or (r shl RED_CHANNEL) or (g shl GREEN_CHANNEL) or b
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + '_'.toString()
        val storageDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        )
    }

    /**
     * Checks if flash player is installed
     *
     * @param context the context needed to obtain the PackageManager
     * @return true if flash is installed, false otherwise
     */
    fun isFlashInstalled(context: Context): Boolean {
        try {
            val pm = context.packageManager
            val ai = pm.getApplicationInfo("com.adobe.flashplayer", 0)
            if (ai != null) {
                return true
            }
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }

        return false
    }

    /**
     * Quietly closes a closeable object like an InputStream or OutputStream without
     * throwing any errors or requiring you do do any checks.
     *
     * @param closeable the object to close
     */
    fun close(closeable: Closeable?) {
        closeable?.let {
            try {
                it.close()
            } catch (e: IOException) {
                CLog.i("Unable to close closeable")
            }
        }
    }

    /**
     * Creates a shortcut on the homescreen using the
     * [HistoryEntry] information that opens the
     * browser. The icon, URL, and title are used in
     * the creation of the shortcut.
     *
     * @param activity the mActivity needed to create
     * the intent and showListDialog a snackbar message
     * @param historyEntry     the HistoryEntity to create the shortcut from
     */
    fun createShortcut(activity: Activity,
                       historyEntry: HistoryEntry,
                       favicon: Bitmap) {
        val shortcutIntent = Intent(Intent.ACTION_VIEW)
        shortcutIntent.data = Uri.parse(historyEntry.url)

        val title = if (TextUtils.isEmpty(historyEntry.title)) activity.getString(R.string.untitled) else historyEntry.title

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            val addIntent = Intent()
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title)
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, favicon)
            addIntent.action = "com.android.launcher.action.INSTALL_SHORTCUT"
            activity.sendBroadcast(addIntent)
            activity.snackbar(R.string.message_added_to_homescreen)
        } else {
            val shortcutManager = activity.getSystemService(ShortcutManager::class.java)
            if (shortcutManager.isRequestPinShortcutSupported) {
                val pinShortcutInfo = ShortcutInfo.Builder(activity, "browser-shortcut-" + historyEntry.url.hashCode())
                        .setIntent(shortcutIntent)
                        .setIcon(Icon.createWithBitmap(favicon))
                        .setShortLabel(title)
                        .build()

                shortcutManager.requestPinShortcut(pinShortcutInfo, null)
                activity.snackbar(R.string.message_added_to_homescreen)
            } else {
                activity.snackbar(R.string.shortcut_message_failed_to_add)
            }
        }
    }

    fun calculateInSampleSize(options: BitmapFactory.Options,
                              reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    fun getFileExtension(filename: String): String? {
        val lastIndex = filename.lastIndexOf('.') + 1
        return if (lastIndex > 0 && filename.length > lastIndex) {
            filename.substring(lastIndex, filename.length)
        } else null
    }

    /**get name in namehost, eg:www.sina.com -> sina*/
    fun getNameHost(host: String?): String? {
        if (host.isNullOrEmpty()) return null
        val hts = host.split('.')
        return when {
            (hts.size <= 1) -> null
            (hts.size == 2) -> hts[0]
            else -> hts[1]
        }
    }

    fun <T> getFromList(list: List<T>, block: (T) -> Boolean): IndexedValue<T>? {
        list.withIndex().forEach {
            if (block(it.value)) return it
        }
        return null
    }

    fun crash2string(t: Throwable, filter: (StackTraceElement) -> Boolean): String {
        val infos = StringBuilder(t.javaClass.simpleName)
        for (info in t.stackTrace) {
            if (filter(info)) {
                val elFormat = String.format("%s;%s;%s", info.fileName, info.methodName, info.lineNumber)
                infos.append("|").append(elFormat)
            }
        }
        return infos.toString()
    }

}
