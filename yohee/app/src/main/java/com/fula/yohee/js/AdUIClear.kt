package com.fula.yohee.js

import com.fula.yohee.YoheeApp
import com.fula.fano.file2string
import org.json.JSONArray
import javax.inject.Inject

/**
 * Force the text to reflow.
 */
@file2string("app/src/main/js/AdUIClear.js")
interface AdUIClear {
    fun provideJs(): String
}

class JSAdUIClear (val adHosts: List<String>) : JSlogicface {

    init {
        YoheeApp.injector.inject(this)
    }

    @Inject
    internal lateinit var adUIClear: AdUIClear
    @Inject
    internal lateinit var rtag: RemoveNode
    @Inject
    internal lateinit var mFrtags: FindTagThenExec

    override fun getJs(): String? {
        if (adHosts.isEmpty()) return null
        return String.format(adUIClear.provideJs(), rtag.provideJs(), mFrtags.provideJs(), JSONArray(adHosts).toString())
    }

}

