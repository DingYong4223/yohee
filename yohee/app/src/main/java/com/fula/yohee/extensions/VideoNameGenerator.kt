package com.fula.yohee.extensions

import android.webkit.URLUtil
import com.fula.downloader.m3u8.NameGenerator

class VideoNameGenerator : NameGenerator {
    // Urls contain mutable parts (parameter 'sessionToken') and stable video's id (parameter 'videoId').
    // e. g. http://example.com?videoId=abcqaz&sessionToken=xyz987
    override fun generate(url: String): String = URLUtil.guessFileName(url, null, null)
}