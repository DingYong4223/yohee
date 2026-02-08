package com.fula.yohee.widget

import com.fula.CLog
import com.fula.base.tool.HttpHelper
import com.fula.base.tool.M3U8Util
import com.fula.base.util.VideoFormatUtil
import com.fula.yohee.bean.DetectUrl
import com.fula.yohee.bean.VideoInfo
import com.fula.yohee.eventbus.SEvent
import com.fula.yohee.utils.UrlUtils
import org.greenrobot.eventbus.EventBus
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue

class VideoSniffer(private val detectedTaskUrlQueue: LinkedBlockingQueue<DetectUrl>, private val mediaDetectList: MutableList<VideoInfo>, private val threadPoolSize: Int, private val retryCountOnFail: Int) {
    private var threadList: MutableList<Thread> = ArrayList()

    fun trigger() = Unit //此函数仅作为触发laze条件，请勿删除调用
    private val detected: ConcurrentHashMap<String, Int> = ConcurrentHashMap()

    fun startSniffer() {
        CLog.i("start video sniffer...")
        stopSniffer()
        threadList = ArrayList()
        for (i in 0 until threadPoolSize) {
            val workerThread = WorkerThread(detectedTaskUrlQueue, mediaDetectList, retryCountOnFail)
            threadList.add(workerThread)
        }
        for (thread in threadList) {
            try {
                thread.start()
            } catch (e: IllegalThreadStateException) {
                CLog.i("thread start, Pass")
            }
        }
    }

    fun stopSniffer() {
        for (thread in threadList) {
            try {
                thread.interrupt()
            } catch (e: Exception) {
                CLog.i("线程已中止, Pass")
            }
        }
    }

    private inner class WorkerThread internal constructor(private val detectedTaskUrlQueue: LinkedBlockingQueue<DetectUrl>, private val mediaDetectList: MutableList<VideoInfo>, private val retryCountOnFail: Int) : Thread() {

        override fun run() {
            super.run()
            CLog.i("thread (" + Thread.currentThread().id + ") :start")
            while (!Thread.currentThread().isInterrupted) {
                try {
                    val detectUrl = detectedTaskUrlQueue.take()
                    if (null == detected[UrlUtils.removeParams(detectUrl.url)]) {
                        CLog.i("start taskUrl=$detectUrl")
                        var failCount = 0
                        while (!detectUrl(detectUrl)) { //如果检测失败
                            failCount++
                            if (failCount >= retryCountOnFail) {
                                break
                            }
                        }
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                    CLog.i("thread (" + Thread.currentThread().id + ") :Interrupted")
                    return
                }
            }
            CLog.i("thread (" + Thread.currentThread().id + ") :exited")
        }

        private fun detectUrl(detectUrl: DetectUrl): Boolean {
            val url = detectUrl.url
            try {
                val videoInfo = VideoInfo()
                val headReq = HttpHelper.performHeadRequest(url)
                videoInfo.url = headReq.realUrl ?: detectUrl.url
                val headerMap = headReq.headerMap
                CLog.i("headerMap = " + headerMap!!)
                if (!headerMap.containsKey("Content-Type")) { //检测失败，未找到Content-Type
                    return false
                }
                CLog.i("Content-Type:" + headerMap["Content-Type"] + " taskUrl=" + url)
                val videoFormat = VideoFormatUtil.detectVideoFormat(url, headerMap["Content-Type"]!!.toString())
                if (videoFormat == null) { //检测成功，不是视频
                    CLog.i("fail not video taskUrl=$url")
                    return true
                }
                if ("m3u8" == videoFormat.name) {
                    val duration = M3U8Util.figureM3U8Duration(url)
                    if (duration <= 0) { //检测成功，不是m3u8的视频
                        CLog.i("fail not m3u8 taskUrl=$url")
                        return true
                    }
                    videoInfo.duration = duration
                } else {
                    /*var size: Long = 0
                    if (headerMap.containsKey("Content-Length") && headerMap.getValue("Content-Length").isNotEmpty()) {
                        try {
                            size = java.lang.Long.parseLong(headerMap["Content-Length"]!![0])
                        } catch (e: NumberFormatException) {
                            e.printStackTrace()
                        }
                    }*/
                    videoInfo.size = HttpHelper.getHeaderLength(headerMap)
                }
                synchronized(mediaDetectList) {
                    mediaDetectList.forEach {
                        if (it.url == url) {
                            CLog.i("video araedy added: $url")
                            return@detectUrl true
                        }
                    }
                }
                videoInfo.url = url
                videoInfo.videoFormat = videoFormat
                videoInfo.name = detectUrl.name
                mediaDetectList.add(videoInfo)
                UrlUtils.removeParams(url).let {
                    if (null == detected[it]) detected[it] = 1
                }
                EventBus.getDefault().post(SEvent(SEvent.TYPE_MEDIA_DETECTED))
                CLog.i("found video taskUrl = $url")
                return true
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }

        }
    }

    companion object {

        fun genUUID(): String {
            return UUID.randomUUID().toString().replace("-".toRegex(), "")
        }

    }
}
