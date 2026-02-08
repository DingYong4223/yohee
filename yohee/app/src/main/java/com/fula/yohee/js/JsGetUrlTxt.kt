package com.fula.yohee.js

import com.fula.yohee.YoheeApp
import com.fula.fano.file2string
import javax.inject.Inject

/**
 * Force the text to reflow.
 */
@file2string("app/src/main/js/GetUrlTxt.js")
interface GetUrlTxt {
    fun provideJs(): String
}

class JsGetUrlTxt (val url: String) : JSlogicface {

    init {
        YoheeApp.injector.inject(this)
    }

    @Inject
    internal lateinit var getUrlTxt: GetUrlTxt
    @Inject
    internal lateinit var mFrtags: FindTagThenExec

    override fun getJs(): String? {
        if (url.isEmpty()) return null
        return String.format(getUrlTxt.provideJs(), mFrtags.provideJs(), url)
    }

}

