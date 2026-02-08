package com.fula.base.util

import android.text.TextUtils
import com.fula.base.bean.VideoFormat
import java.net.MalformedURLException
import java.util.*

/**
 * Created by xm on 17-8-16.
 */
object VideoFormatUtil {

    private val videoExtensionList = Arrays.asList(
            "m3u8", "mp4", "flv", "mpeg"
    )

    private val videoFormatList = Arrays.asList(
            VideoFormat("m3u8", Arrays.asList("application/octet-stream", "application/vnd.apple.mpegurl", "application/mpegurl", "application/x-mpegurl", "audio/mpegurl", "audio/x-mpegurl")),
            VideoFormat("mp4", Arrays.asList("video/mp4", "application/mp4", "video/h264")),
            VideoFormat("flv", Arrays.asList("video/x-flv")),
            VideoFormat("f4v", Arrays.asList("video/x-f4v")),
            VideoFormat("mpeg", Arrays.asList("video/vnd.mpegurl"))
    )


    fun containsVideoExtension(url: String): Boolean {
        for (videoExtension in videoExtensionList) {
            if (!TextUtils.isEmpty(url)) {
                if (url.contains(videoExtension)) {
                    return true
                }
            }
        }
        return false
    }

    fun isLikeVideo(fullUrl: String): Boolean {
        try {
            val extension = FileUtil.getExtension(fullUrl)
            if (TextUtils.isEmpty(extension)) {
                return true
            }
            return videoExtensionList.contains(extension.toLowerCase())
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            return false
        }

    }

    fun detectVideoFormat(url: String, mime: String): VideoFormat? {
        var mime = mime
        try {
            val extension = FileUtil.getExtension(url)
            if ("mp4" == extension) {
                mime = "video/mp4"
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            return null
        }

        mime = mime.toLowerCase()
        for (videoFormat in videoFormatList) {
            if (!TextUtils.isEmpty(mime)) {
                for (mimePattern in videoFormat.mimeList!!) {
                    if (mime.contains(mimePattern)) {
                        return videoFormat
                    }
                }
            }
        }
        return null
    }
}
