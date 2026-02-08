/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fula.yohee.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.MailTo
import android.net.Uri
import android.util.Patterns
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import androidx.annotation.Nullable
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.fula.CLog
// import com.fula.yohee.BuildConfig
import com.fula.yohee.constant.FILE
import com.fula.yohee.database.DownloadEntry
import com.fula.yohee.extensions.snackbar
import com.fula.yohee.html.history.HistoryFactory
import com.fula.yohee.html.homepage.HomeFactory
import java.io.File
import java.net.URISyntaxException
import java.util.*
import java.util.regex.Pattern

/**
 * Utility methods for URL manipulation.
 */
object UrlUtils {

    private val ACCEPTED_URI_SCHEMA = Pattern.compile("(?i)((?:http|https|file)://|(?:inline|data|about|javascript):|(?:.*:.*@))(.*)")
    const val QUERY_PLACE_HOLDER = "%s"
    private const val URL_ENCODED_SPACE = "%20"

    const val URL_TYPE_HOME = 0
    const val URL_TYPE_HISTORY = 1
    const val URL_TYPE_OFFLINE = 2
    const val URL_TYPE_WEB = 3

    /**
     * Attempts to determine whether user input is a URL or search terms.  Anything with a space is
     * passed to search if [canBeSearch] is true.
     *
     * Converts to lowercase any mistakenly upper-cased scheme (i.e., "Http://" converts to
     * "http://")
     *
     * @param canBeSearch if true, will return a search url if it isn't a valid  URL. If false,
     * invalid URLs will return null.
     * @return original or modified URL.
     */
    @JvmStatic
    fun smartUrlFilter(url: String, canBeSearch: Boolean, searchUrl: String): String {
        var inUrl = url.trim()
        val hasSpace = inUrl.contains(' ')
        val matcher = ACCEPTED_URI_SCHEMA.matcher(inUrl)
        if (matcher.matches()) {
            // force scheme to lowercase
            val scheme = matcher.group(1)
            val lcScheme = scheme.toLowerCase()
            if (lcScheme != scheme) {
                inUrl = lcScheme + matcher.group(2)
            }
            if (hasSpace && Patterns.WEB_URL.matcher(inUrl).matches()) {
                inUrl = inUrl.replace(" ", URL_ENCODED_SPACE)
            }
            return inUrl
        }
        if (!hasSpace) {
            if (Patterns.WEB_URL.matcher(inUrl).matches()) {
                return URLUtil.guessUrl(inUrl)
            }
        }

        return if (canBeSearch) {
            URLUtil.composeSearchUrl(inUrl,
                    searchUrl, QUERY_PLACE_HOLDER)
        } else {
            ""
        }
    }

//    /**检验无效url*/
//    fun isInValidUrl(url: String): Boolean {
//        Config.INVALIDE_SCHEMA.forEach {
//            if (url.startsWith(it)) {
//                return true
//            }
//        }
//        return false
//    }

    fun getHost(url: String): String {
        return url.toUri().host ?: ""
    }

    @JvmStatic
    fun getNameFromUrl(url: String): String {
        val suffixes = "avi|mpeg|3gp|mp3|mp4|wav|jpeg|gif|jpg|png|apk|exe|pdf|rar|zip|docx|doc|m3u8|html"
        val pat = Pattern.compile("[\\w]+[\\.]($suffixes)")
        val mc = pat.matcher(url)
        if (mc.find()) {
            return mc.group()
        }
        return ""
    }

    @JvmStatic
    fun isVideo(fileName: String): Boolean {
        val reg = "\\.(mp4|flv|avi|rm|rmvb|wmv|m3u8)$"
        return Pattern.compile(reg).matcher(fileName).find()
    }

    @JvmStatic
    fun isImage(fileName: String): Boolean {
        val reg = "\\.(jpg|gif|png|jpeg|bmp)$"
        return Pattern.compile(reg).matcher(fileName).find()
    }

    fun getFileType(url: String, fileName: String? = null): Int {
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url))
        val fname = fileName ?: URLUtil.guessFileName(url, "", mimeType)
        if (mimeType?.contains("video") == true
                || UrlUtils.isVideo(fname)) {
            CLog.i("media add mimetype = $mimeType, url = $url, fname = $fname")
            return DownloadEntry.TYPE_VIDEO_FILE
        }
        return DownloadEntry.TYPE_NOMAL_FILE
    }

    @JvmStatic
    fun getType(url: String?): Int = when {
        isHomePage(url) -> URL_TYPE_HOME
        isHistoryUrl(url) -> URL_TYPE_HISTORY
        isLocalUrl(url) -> URL_TYPE_OFFLINE
        else -> URL_TYPE_WEB
    }

    @JvmStatic
    fun isLocalUrl(url: String?): Boolean = url != null && url.startsWith(FILE)

    @JvmStatic
    fun isDownwebUrl(url: String?): Boolean = isLocalUrl(url) && url?.contains(FileUtils.DOWNLOAD_WEB) == true

    /**
     * Returns whether the given url is the bookmarks/history page or a normal website
     */
    @JvmStatic
    fun isGenUrl(url: String?): Boolean = isHistoryUrl(url) || isHomePage(url)

    /**
     * Determines if the url is a url for the history page.
     *
     * @param url the url to check, may be null.
     * @return true if the url is a history url, false otherwise.
     */
    @JvmStatic
    fun isHistoryUrl(url: String?): Boolean =
            url != null && url.startsWith(FILE) && url.endsWith(HistoryFactory.FILENAME)

    /**
     * Determines if the url is a url for the start page.
     *
     * @param url the url to check, may be null.
     * @return true if the url is a start page url, false otherwise.
     */
    @JvmStatic
    fun isHomePage(url: String?): Boolean =
            url != null && url.startsWith(FILE) && url.endsWith(HomeFactory.FILENAME)

    @JvmStatic
    fun getTitle(context: Context, url: String, title: String?): String {
        if (isGenUrl(url)) return ""
        if (isGenUrl(title)) return url
        return if (title?.isEmpty() == false) { title } else { url }
    }

    @JvmStatic
    fun mailTo(context: Activity, url: String) {
        CLog.i("is mail or intent...")
        if (url.startsWith("mailto:")) {
            val mailTo = MailTo.parse(url)
            val i = Utils.newEmailIntent(mailTo.to, mailTo.subject,
                    mailTo.body, mailTo.cc)
            context.startActivity(i)
        }
    }

    @JvmStatic
    fun intentTo(context: Activity, url: String) {
        CLog.i("is mail or intent...")
        if (url.startsWith("intent://")) {
            val intent = try {
                Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
            } catch (ignored: URISyntaxException) {
                null
            }
            if (intent != null) {
                intent.addCategory(Intent.CATEGORY_BROWSABLE)
                intent.component = null
                intent.selector = null
                try {
                    context.startActivity(intent)
                } catch (e: Throwable) {
                    CLog.i("ActivityNotFoundException")
                }
            }
        }
    }

    @JvmStatic
    fun fileIntent(context: Activity, url: String) {
        CLog.i("url = $url")
        if (!URLUtil.isFileUrl(url) || UrlUtils.isGenUrl(url)) {
            return
        }
        val file = File(url.replace(FILE, ""))
        if (!file.exists()) {
            context.snackbar(com.fula.yohee.R.string.message_open_download_fail)
            return
        }

        val newMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(Utils.getFileExtension(file.toString()))
        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        val contentUri = FileProvider.getUriForFile(context, "com.fula.yohee.fileprovider", file)
        intent.setDataAndType(contentUri, newMimeType)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            CLog.i("YoheeWebClient: cannot open downloaded file")
        }
    }

    @JvmStatic
    fun alternateSlashUrl(url: String): String = if (url.endsWith("/")) {
        url.substring(0, url.length - 1)
    } else {
        "$url/"
    }

    @JvmStatic
    fun removeParams(url: String): String {
        val index = url.indexOf("?")
        if (index > 0) {
            return url.substring(0, index)
        }
        return url
    }

    /**
     * Guesses canonical filename that a download would have, using
     * the URL and contentDisposition. File extension, if not defined,
     * is added based on the mimetype
     * @param url Url to the content
     * @param contentDisposition Content-Disposition HTTP header or `null`
     * @param mimeType Mime-type of the content or `null`
     *
     * @return suggested filename
     */
    @JvmStatic
    fun guessFileName(url: String, @Nullable contentDisposition: String?, @Nullable mimeType: String?): String {
        var filename: String? = null
        var extension: String? = null

        // If we couldn't do anything with the hint, move toward the content disposition
        if (filename == null && contentDisposition != null) {
            filename = parseContentDisposition(contentDisposition)
            if (filename != null) {
                val index = filename!!.lastIndexOf('/') + 1
                if (index > 0) {
                    filename = filename.substring(index)
                }
            }
        }
        // If all the other http-related approaches failed, use the plain uri
        if (filename == null) {
            var decodedUrl: String? = Uri.decode(url)
            if (decodedUrl != null) {
                val queryIndex = decodedUrl.indexOf('?')
                // If there is a query string strip it, same as desktop browsers
                if (queryIndex > 0) {
                    decodedUrl = decodedUrl.substring(0, queryIndex)
                }
                if (!decodedUrl.endsWith("/")) {
                    val index = decodedUrl.lastIndexOf('/') + 1
                    if (index > 0) {
                        filename = decodedUrl.substring(index)
                    }
                }
            }
        }
        // Finally, if couldn't get filename from URI, get a generic filename
        if (filename == null) {
            filename = "downloadfile"
        }
        // Split filename between base and extension
        // Add an extension if filename does not have one
        val dotIndex = filename.indexOf('.')
        if (dotIndex < 0) {
            if (mimeType != null) {
                extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
                if (extension != null) {
                    extension = ".$extension"
                }
            }
            if (extension == null) {
                if (mimeType != null && mimeType.toLowerCase(Locale.ROOT).startsWith("text/")) {
                    if (mimeType.equals("text/html", ignoreCase = true)) {
                        extension = ".html"
                    } else {
                        extension = ".txt"
                    }
                } else {
                    extension = ".bin"
                }
            }
        } else {
            if (mimeType != null) {
                // Compare the last segment of the extension against the mime type.
                // If there's a mismatch, discard the entire extension.
                val lastDotIndex = filename.lastIndexOf('.')
                val typeFromExt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                        filename.substring(lastDotIndex + 1))
                if (typeFromExt != null && !typeFromExt.equals(mimeType, ignoreCase = true)) {
                    extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
                    if (extension != null) {
                        extension = ".$extension"
                    }
                }
            }
            if (extension == null) {
                extension = filename.substring(dotIndex)
            }
            filename = filename.substring(0, dotIndex)
        }
        return filename + extension
    }

    /** Regex used to parse content-disposition headers  */
    private val CONTENT_DISPOSITION_PATTERN = Pattern.compile("attachment;\\s*filename\\s*=\\s*(\"?)([^\"]*)\\1\\s*]$",
            Pattern.CASE_INSENSITIVE)


    private fun parseContentDisposition(contentDisposition: String): String? {
        try {
            val m = CONTENT_DISPOSITION_PATTERN.matcher(contentDisposition)
            if (m.find()) {
                return m.group(2)
            }
        } catch (ex: IllegalStateException) {
            // This function is defined as returning null when it can't parse the header
        }
        return null
    }

}
