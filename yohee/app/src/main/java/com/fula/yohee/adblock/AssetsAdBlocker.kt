package com.fula.yohee.adblock

import android.app.Application
import androidx.core.net.toUri
import com.fula.yohee.di.DiskScheduler
import com.fula.yohee.extensions.*
import com.fula.CLog
import io.reactivex.Completable
import io.reactivex.Scheduler
import java.io.InputStreamReader
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * An implementation of the ad blocker that checks the URLs against the hosts stored in assets.
 * Checking whether a URL is an ad is an `O(log n)` operation.
 */
@Singleton
class AssetsAdBlocker @Inject internal constructor(private val application: Application, @DiskScheduler diskScheduler: Scheduler): AdBlockListener {

    private val adSet = HashSet<String>()

    init {
        loadHostsFile().subscribeOn(diskScheduler).subscribe()
    }

    override fun isAd(url: String): String? {
        val host = url.toUri().host
        host?.let {
            if (adSet.contains(host)) {
                CLog.i("ad url = $url")
                return host
            }
        }
        return null
    }

    private fun loadHostsFile() = Completable.fromAction {
        val asset = application.assets
        val reader = InputStreamReader(asset.open(AD_FILE_NAME))
        val lineBuilder = StringBuilder()
        val time = System.currentTimeMillis()
        val domains = ArrayList<String>(1)
        reader.safeUse { assertReader ->
            assertReader.forEachLine {
                lineBuilder.append(it)
                parseString(lineBuilder, domains)
                lineBuilder.setLength(0)
            }
        }
        adSet.addAll(domains)
        CLog.i("Loaded ad list in: ${(System.currentTimeMillis() - time)} ms")
    }

    companion object {
        private const val AD_FILE_NAME = "hosts.txt"
        private const val LOCAL_IP_V4 = "127.0.0.1"
        private const val LOCAL_IP_V4_ALT = "0.0.0.0"
        private const val LOCAL_IP_V6 = "::1"
        private const val LOCALHOST = "localhost"
        private const val COMMENT = "#"
        private const val TAB = "\t"
        private const val SPACE = " "
        private const val EMPTY = ""

        @JvmStatic
        internal fun parseString(lineBuilder: StringBuilder, parsedList: MutableList<String>) {
            if (lineBuilder.isEmpty() || lineBuilder.startsWith(COMMENT)) {
                return
            }
            lineBuilder.inlineReplace(LOCAL_IP_V4, EMPTY)
            lineBuilder.inlineReplace(LOCAL_IP_V4_ALT, EMPTY)
            lineBuilder.inlineReplace(LOCAL_IP_V6, EMPTY)
            lineBuilder.inlineReplace(TAB, EMPTY)
            val comment = lineBuilder.indexOf(COMMENT)
            if (comment >= 0) {
                lineBuilder.replace(comment, lineBuilder.length, EMPTY)
            }
            lineBuilder.inlineTrim()
            if (lineBuilder.isEmpty() || lineBuilder.stringEquals(LOCALHOST)) {
                return
            }
            while (lineBuilder.contains(SPACE)) {
                val space = lineBuilder.indexOf(SPACE)
                val partial = lineBuilder.substringToBuilder(0, space)
                partial.inlineTrim()

                val partialLine = partial.toString()

                // Add string to list
                parsedList.add(partialLine)
                lineBuilder.inlineReplace(partialLine, EMPTY)
                lineBuilder.inlineTrim()
            }
            if (lineBuilder.isNotEmpty()) {
                parsedList.add(lineBuilder.toString())
            }
        }
    }

}
