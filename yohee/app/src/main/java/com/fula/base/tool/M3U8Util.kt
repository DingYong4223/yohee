package com.fula.base.tool

import com.fula.CLog
import java.io.IOException
import java.net.URL

/**
 * Created by xm on 17/8/17.
 */
object M3U8Util {

    @Throws(IOException::class)
    fun figureM3U8Duration(url: String): Double {
        val m3U8Content = HttpHelper.getResponseString(HttpHelper.sendGetRequest(url))
        var isSubFileFound = false
        var totalDuration = 0.0
        for (line in m3U8Content.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            val lineString = line.trim { it <= ' ' }
            if (isSubFileFound) {
                return if (lineString.startsWith("#")) {
                    //格式错误 直接返回时长0
                    CLog.i("格式错误1")
                    0.0
                } else {
                    val subFileUrl = URL(URL(url), lineString).toString()
                    figureM3U8Duration(subFileUrl)
                }
            }
            if (lineString.startsWith("#")) {
                if (lineString.startsWith("#EXT-X-STREAM-INF")) {
                    isSubFileFound = true
                    continue
                }
                if (lineString.startsWith("#EXTINF:")) {
                    var sepPosition = lineString.indexOf(",")
                    if (sepPosition <= "#EXTINF:".length) {
                        sepPosition = lineString.length
                    }
                    var duration = 0.0
                    try {
                        duration = java.lang.Double.parseDouble(lineString.substring("#EXTINF:".length, sepPosition).trim { it <= ' ' })
                    } catch (e: NumberFormatException) {
                        e.printStackTrace()
                        //格式错误 直接返回时长0
                        CLog.i("格式错误3")
                        return 0.0
                    }
                    totalDuration += duration
                }
            }
        }
        return totalDuration
    }

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        println("start")
        println(figureM3U8Duration("http://pl-ali.youku.com/playlist/m3u8?ids=%7B%22a1%22%3A%22746535159_mp4%22%2C%22v%22%3A%22XMzAwNzM0NzQ0OA%3D%3D_mp4%22%7D&&ups_client_netip=114.222.108.2&ups_ts=1504594399&utid=OyY1EjMURAwCAXLebAJBRcEs&ccode=0501&psid=18a1a49d4af3dc3b0d07580a085ac3fa&duration=70&expire=18000&ups_key=dd10f71f035b9b0b6471afbb3e8f2248"))
        println("stop")
    }

}
