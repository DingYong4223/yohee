package com.fula.yohee.bean

import com.fula.base.bean.VideoFormat

/**
 * Created by xm on 17-8-17.
 */
class VideoInfo {
    lateinit var url: String
    lateinit var videoFormat: VideoFormat
    var size: Long = 0//单位byte m3u8不显示
    var duration: Double = 0.toDouble()//单位s m3u8专用
    var name: String? = null//原网页url
}

class DetectUrl(var url: String, var name: String) {
    override fun toString(): String {
        return "url: $url\npageUrl: $name"
    }
}
