package com.fula.yohee.utils

import com.fula.yohee.dialog.DialogItem
import org.junit.Test
import java.util.regex.Pattern

/**
 * Unit tests for [DialogItem].
 */
class JavaTest {

    @Test
    fun `my test for kotlin`() {
        assert(getNameHost("m.sohu.com.cn?x=5") == "sohu")
        assert(getNameHost("sina.com?x=5") == "sina")
        assert(getNameHost("com.sina.com.cn?x=5") == "sina")
    }

    @Test
    fun `test isVideoUrl function`() {
        assert(UrlUtils.isVideo("www.test.com?xxx=sdf.mp4"))
        assert(!UrlUtils.isVideo("www.test.com?xxx=sdf.mp3"))
        assert(!UrlUtils.isVideo("www.test.com?xxx=sdf.mp4xxx"))
        assert(!UrlUtils.isVideo("www.test.com?xxx=sdf.mp4.mp"))
        assert(UrlUtils.isVideo("www.test.com?xxx=sdf.rmvb"))
        assert(!UrlUtils.isVideo("www.test.com?xxx=sdf.rmvb."))
        assert(!UrlUtils.isVideo("www.test.com?xxx=sdf.avi."))
    }

    @Test
    fun `test suffix`() {
        val filename = "sdf.mp4"
        val suffx = filename.substring(filename.indexOf("."))
        assert(suffx == ".mp4")
    }

    @Test
    fun `test m3u8 segment`() {
        var line = "#EXTINF:10, no desc"
        line = line.substring(8)
        val lineArray = line.split(",")
        val duration = lineArray[0].trim()
        System.out.println(duration)
    }

    @Test
    fun `test exception stack info`() {
        System.out.println(getStackInfo(Exception("--------------")))
    }

    @Test
    fun `test down count`() {
        for (i in 5 downTo 0) {
            System.out.println("--------------$i")
        }
    }

    @Test
    fun `get file suffix`() {
        val fileName = "xscfasd.xxx"
        val suffix = fileName.substring(fileName.lastIndexOf(".") + 1)
        System.out.println("--------------$suffix")
    }

    @Test
    fun `concurrent hash map`() {
        val url = "https://ykugc.cp31.ott.cibntv.net/657359989343D718C534F3642/03000801005ACCDE306C0DA408E6DE08C1A2A3-6113-459E-B061-FC1798C357E4.mp4?ccode=0590&duration=128&expire=18000&psid=638de11927241e81add5823ecf783837&ups_client_netip=3b257d22&ups_ts=1558415003&ups_userid=&utid=RnRqFUVB%2FwMCATslfSJfo%2BL7&vid=XMzUyOTQzOTQ5Ng&vkey=Ac3b152ec5736e71c334bc08fcf72e376&sp=&bc=2"
        val index = url.indexOf("?")
        var realUrl: String = url
        if (index > 0) {
            realUrl = url.substring(0, index)
        }
        System.out.println(realUrl)
    }

    private fun `func return func`(): (Int, Long) -> Long {
        val index = 5
        return fun(i: Int, l: Long): Long {
            return (index + i) * l
        }
    }
    @Test
    fun `func return func test`() {
        val mytest = `func return func`()
        System.out.println("------" + mytest(5, 10L))
    }

//    @Test
//    fun `test for filename get`() {
//        var filename = UrlUtils.parseContentDisposition("[attachment; filename=cn.com.hsbc.hsbcchina_2.24.0_22400.apk]")
//        System.out.println(filename)
//    }

    @Test
    fun `test for partten`() {
        val p = Pattern.compile("\\d+")
        val m = p.matcher("22bb23")
        if (m.find()) {
            System.out.println(m.group(0))
        }
//        m.pattern()//返回p 也就是返回该Matcher对象是由哪个Pattern对象的创建的
    }

    companion object {
        fun getHost(url: String): String {
            var url = url
            if (!(url.startsWith("http://") || url
                            .startsWith("https://"))) {
                url = "http://$url"
            }
            var returnVal = ""
            try {
                val p = Pattern.compile("(?<=//|)((\\w)+\\.)+\\w+")
                val m = p.matcher(url)
                if (m.find()) {
                    returnVal = m.group()
                }
            } catch (e: Exception) {
            }
            if (returnVal.endsWith(".html") || returnVal
                            .endsWith(".htm")) {
                returnVal = ""
            }
            return returnVal
        }

        fun getNameHost(host: String): String? {
            val hts = host.split('.')
            return when {
                (hts.size <= 1) -> null
                (hts.size == 2) -> hts[0]
                else -> hts[1]
            }
        }

        fun getStackInfo(e: Exception): String {
            val sb = StringBuffer()
            val stacks = e.stackTrace
            for (i in stacks.indices) {
                sb.append(stacks[i]).append("\n")
            }
            return sb.toString()
        }

    }
}
