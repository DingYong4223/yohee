package com.fula.base.tool

import com.fula.CLog
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.net.URLEncoder
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.*
import kotlin.collections.Map.Entry

object HttpHelper {

    private const val defaultCharset = "UTF-8"//"GBK"
    private const val readTimeout = 10000//10s
    private const val connectTimeout = 5000//5s
    private const val maxRedirects = 4//最大重定向次数

    var commonHeaders: MutableMap<String, String> = HashMap()

    private val DO_NOT_VERIFY: HostnameVerifier = HostnameVerifier { _, _ -> true }

    init {
        commonHeaders["User-Agent"] = "Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1"
    }

    fun getHeaderLength(header: Map<String, List<String>>): Long {
        var size: Long = 0
        val key = "Content-Length"
        if (header.containsKey(key) && header.getValue(key).isNotEmpty()) {
            try {
                size = java.lang.Long.parseLong(header.getValue(key)[0])
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
        }
        return size
    }

    private fun trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }

            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                CLog.i("checkClientTrusted")
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                CLog.i("checkServerTrusted")
            }
        })

        // Install the all-trusting trust manager
        try {
            val sc = SSLContext.getInstance("TLS")
            sc.init(null, trustAllCerts, java.security.SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val headRequestResponse = performHeadRequest("https://disp.titan.mgtv.com/vod.do?fmt=4&pno=1121&fid=3BBD5FD649B8DEB99DBDE005F7304103&file=/c1/2017/08/30_0/3BBD5FD649B8DEB99DBDE005F7304103_20170830_1_1_644.mp4")
        println(headRequestResponse.realUrl)
        println(headRequestResponse.headerMap)
    }

    @Throws(IOException::class)
    @JvmOverloads
    fun sendGetRequest(url: String, params: Map<String, String>? = null, headers: Map<String, String>? = commonHeaders): URLConnection {
        var params = params
        val buf = StringBuilder("")
        var urlObject = URL(url)
        buf.append(urlObject.protocol).append("://").append(urlObject.host).append(if (urlObject.port == -1 || urlObject.port != urlObject.defaultPort) "" else ":" + urlObject.port).append(urlObject.path)
        val query = urlObject.query
        if (params == null) {
            params = HashMap()
        }
        var isQueryExist = false
        if (!(query == null || query.isEmpty()) || params.isNotEmpty()) {
            buf.append("?")
            isQueryExist = true
        }
        if (!(query == null || query.isEmpty())) {
            buf.append(query)
            buf.append("&")
        }
        var entrys = params.entries
        for ((key, value) in entrys) {
            buf.append(key).append("=")
                    .append(URLEncoder.encode(value, defaultCharset)).append("&")
        }
        if (isQueryExist) {
            buf.deleteCharAt(buf.length - 1)
        }
        println("before:$url")
        println("after:$buf")
        urlObject = URL(buf.toString())
        var conn: HttpURLConnection? = null
        try {
            if (urlObject.protocol.toUpperCase() == "HTTPS") {
                trustAllHosts()
                val https = urlObject.openConnection() as HttpsURLConnection
                https.hostnameVerifier = DO_NOT_VERIFY
                conn = https
            } else {
                conn = urlObject.openConnection() as HttpURLConnection
            }
            conn.requestMethod = "GET"
            conn.connectTimeout = connectTimeout
            conn.readTimeout = readTimeout
            if (headers != null) {
                entrys = headers.entries
                for ((key, value) in entrys) {
                    conn.setRequestProperty(key, value)
                }
            }
            conn.responseCode
            return conn
        } catch (e: IOException) {
            conn?.disconnect()
            throw e
        }

    }

    @Throws(IOException::class)
    fun sendPostRequest(url: String, params: Map<String, String>?, headers: Map<String, String>?): URLConnection {
        var params = params
        val buf = StringBuilder()
        if (params == null) {
            params = HashMap()
        }
        var entrys = params.entries
        for ((key, value) in entrys) {
            buf.append("&").append(key).append("=")
                    .append(URLEncoder.encode(value, defaultCharset))
        }
        buf.deleteCharAt(0)
        val urlObject = URL(url)
        var conn: HttpURLConnection? = null
        try {
            if (urlObject.protocol.toUpperCase() == "HTTPS") {
                trustAllHosts()
                val https = urlObject.openConnection() as HttpsURLConnection
                https.hostnameVerifier = DO_NOT_VERIFY
                conn = https
            } else {
                conn = urlObject.openConnection() as HttpURLConnection
            }
            conn.requestMethod = "POST"
            conn.connectTimeout = connectTimeout
            conn.readTimeout = readTimeout
            if (headers != null) {
                entrys = headers.entries
                for ((key, value) in entrys) {
                    conn.setRequestProperty(key, value)
                }
            }
            conn.doOutput = true
            val out = conn.outputStream
            //System.out.println("buf.toString():"+buf.toString());
            out.write(buf.toString().toByteArray(charset(defaultCharset)))
            out.flush()
            conn.responseCode // 为了发送成功
            return conn
        } catch (e: IOException) {
            conn?.disconnect()
            throw e
        }

    }

    @Throws(IOException::class)
    fun sendPostRequest(url: String,
                        params: Map<String, String>): URLConnection? {
        try {
            return sendPostRequest(url, params, commonHeaders)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    @Throws(IOException::class)
    fun sendStringPostRequest(url: String, postDataString: String?, headers: Map<String, String>?): URLConnection {
        var postDataString = postDataString
        if (postDataString == null) {
            postDataString = ""
        }
        val entrys: Set<Entry<String, String>>
        val urlObject = URL(url)
        var conn: HttpURLConnection? = null
        try {
            if (urlObject.protocol.toUpperCase() == "HTTPS") {
                trustAllHosts()
                val https = urlObject.openConnection() as HttpsURLConnection
                https.hostnameVerifier = DO_NOT_VERIFY
                conn = https
            } else {
                conn = urlObject.openConnection() as HttpURLConnection
            }
            conn.requestMethod = "POST"
            conn.connectTimeout = connectTimeout
            conn.readTimeout = readTimeout
            if (headers != null) {
                entrys = headers.entries
                for ((key, value) in entrys) {
                    conn.setRequestProperty(key, value)
                }
            }
            conn.doOutput = true
            val out = conn.outputStream
            //System.out.println("buf.toString():"+buf.toString());
            out.write(postDataString.toByteArray(charset(defaultCharset)))
            out.flush()
            conn.responseCode // 为了发送成功
            return conn
        } catch (e: IOException) {
            conn?.disconnect()
            throw e
        }

    }

    @Throws(IOException::class)
    fun sendStringPostRequest(url: String, postDataString: String): URLConnection? {
        try {
            return sendStringPostRequest(url, postDataString, commonHeaders)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    @Throws(IOException::class)
    fun getResponseString(urlConnection: URLConnection): String {

        var inputStream: InputStream? = null
        var inputStreamReader: InputStreamReader? = null
        var reader: BufferedReader? = null
        val resultBuffer = StringBuffer()
        try {
            if ((urlConnection as HttpURLConnection).responseCode >= 300) {
                throw IOException("HTTP Request is not success, Response code is " + urlConnection.responseCode)
            }
            inputStream = urlConnection.getInputStream()
            inputStreamReader = InputStreamReader(inputStream, defaultCharset)
            reader = BufferedReader(inputStreamReader)

            var tempLine: String? = null
            while (reader.readLine().apply { tempLine = this } != null) {
                resultBuffer.append(tempLine + "\n")
            }
            return resultBuffer.toString()
        } catch (e: Throwable) {
            e.printStackTrace()
            return ""
        } finally {
            reader?.close()
            inputStreamReader?.close()
            inputStream?.close()
            (urlConnection as HttpURLConnection).disconnect()
        }
    }


    @Throws(IOException::class)
    fun save2File(urlConnection: URLConnection, saveFilePath: String) {
        var dis: DataInputStream? = null
        var fos: FileOutputStream? = null
        try {
            dis = DataInputStream(urlConnection.getInputStream())
            //建立一个新的文件
            fos = FileOutputStream(File(saveFilePath))
            val buffer = ByteArray(1024)
            var length: Int = 0
            //开始填充数据
            while (dis.read(buffer).apply { length = this } > 0) {
                fos.write(buffer, 0, length)
            }
        } finally {
            dis?.close()
            fos?.close()
            (urlConnection as HttpsURLConnection).disconnect()
        }
    }

    @Throws(IOException::class)
    @JvmOverloads
    fun performHeadRequest(url: String, headers: Map<String, String> = commonHeaders): HeadRequestResponse {
        return performHeadRequestForRedirects(url, headers, 0)
    }

    @Throws(IOException::class)
    private fun performHeadRequestForRedirects(url: String, headers: Map<String, String>?, redirectCount: Int): HeadRequestResponse {
        val urlObject = URL(url)
        var conn: HttpURLConnection? = null
        try {
            if (urlObject.protocol.toUpperCase() == "HTTPS") {
                trustAllHosts()
                val https = urlObject.openConnection() as HttpsURLConnection
                https.hostnameVerifier = DO_NOT_VERIFY
                conn = https
            } else {
                conn = urlObject.openConnection() as HttpURLConnection
            }
            conn.instanceFollowRedirects = false
            conn.requestMethod = "GET"
            conn.connectTimeout = connectTimeout
            conn.readTimeout = readTimeout
            if (headers != null) {
                val entrySet = headers.entries
                for ((key, value) in entrySet) {
                    conn.setRequestProperty(key, value)
                }
            }
            val headerFields = conn.headerFields
            val responseCode = conn.responseCode
            //conn.disconnect()
            return if (responseCode == 302) {
                if (redirectCount >= maxRedirects) {
                    HeadRequestResponse(url, HashMap())
                } else {
                    val location = headerFields["Location"]!![0]
                    performHeadRequestForRedirects(location, headers, redirectCount + 1)
                }
            } else {
                HeadRequestResponse(url, headerFields)
            }
        } finally {
            conn?.disconnect()
        }
    }

    class HeadRequestResponse {
        var realUrl: String? = null
        var headerMap: Map<String, List<String>>? = null

        constructor(realUrl: String, headerMap: Map<String, List<String>>) {
            this.realUrl = realUrl
            this.headerMap = headerMap
        }
    }

}