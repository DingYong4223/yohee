package com.fula.yohee.database

import android.graphics.Color
import com.fula.downloader.DownStatus
import java.io.Serializable

/**
 * A data type that represents a page that can be loaded.
 */
sealed class WebPage(open val url: String, open val title: String) : Serializable

/**
 * A data type that represents a page that was visited by the user.
 */
data class HistoryEntry(override val url: String, override val title: String, val lastTimeVisited: Long = System.currentTimeMillis()) : WebPage(url, title)

/**
 * A data type that represents an entity that has been bookmarked by the user or contains a page
 * that has been bookmarked by the user.
 */
data class Bookmark(
        override val url: String,
        override val title: String,
        val folder: String,
        val position: Int,
        val type: Int) : WebPage(url, title), Serializable {

    companion object {
        const val TYPE_BOOK = 1
        const val TYPE_MARK = 1 shl 1
        const val TYPE_BOOKMARK = TYPE_BOOK or TYPE_MARK

        const val FROM_BOOK = 0
        const val FROM_MARK = 1
    }
}

/**
 * A data type that represents a suggestion for a search query.
 */
data class SearchSuggestion(override val url: String, override val title: String) : WebPage(url, title)

data class WebColor(val host: String, val color: Int = Color.WHITE, val type: Int = TYPE_AUTO) {
    companion object {
        const val TYPE_AUTO = 0
        const val TYPE_HANDLE = 1
    }
}

data class Ad(
        val url: String,
        val timeCreated: Long
)

data class VideoProgress(
        val url: String,
        val progress: Long
)

data class DownloadEntry(
        var url: String,
        var title: String,
        var length: Long,/**下载内容大小*/
        var downed: Long = 0,/**已下载字节数*/
        var status: Int = DownStatus.STATUS_DOWNLOADING,/**下载状态：枚举值*/
        var type: Int = TYPE_NOMAL_FILE): Serializable { /**文件类型：枚举值*/
    companion object {
        const val TYPE_NOMAL_FILE = 0
        const val TYPE_VIDEO_FILE = 1
    }
}

