package com.fula.yohee.widget

import com.fula.CLog
import java.net.URLDecoder
import javax.inject.Singleton

/**
 * 危险词汇
 */
@Singleton
class DangerBlocker {

    private val dangerSet: HashSet<String> = HashSet()

    init {
        dangerSet.add("医院")
        dangerSet.add("酒店")
        dangerSet.add("饭店")
        dangerSet.add("学校")
        dangerSet.add("股票")
        dangerSet.add("基金")
    }

    fun detectDanger(url: String, listener: (String) -> Unit) {
        val decodeUrl = URLDecoder.decode(url)
        CLog.i("decodeUrl = $decodeUrl")
        dangerSet.forEach {
            if (decodeUrl.contains(it)) {
                return listener(it)
            }
        }
    }

}
