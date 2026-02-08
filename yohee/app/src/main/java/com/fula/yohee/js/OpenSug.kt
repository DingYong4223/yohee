package com.fula.yohee.js

import com.fula.yohee.YoheeApp
import com.fula.fano.file2string
import javax.inject.Inject

/**
 * Force the text to reflow.
 */
@file2string("app/src/main/js/opensug.js")
interface OpenSug {
    fun provideJs(): String
}

class JSOpenSug(val url: String) : JSlogicface {

    init {
        YoheeApp.injector.inject(this)
    }

    @Inject
    internal lateinit var openSug: OpenSug

    override fun getJs(): String? = openSug.provideJs()

}

