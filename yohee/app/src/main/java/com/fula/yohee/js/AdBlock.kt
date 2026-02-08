package com.fula.yohee.js

import android.content.Context
import com.fula.yohee.YoheeApp
import com.fula.fano.file2string
import javax.inject.Inject

/**
 * Force the text to reflow.
 */
@file2string("app/src/main/js/AdBlock.js")
interface AdBlock {
    fun provideJs(): String
}

class JSAdBlock(val context: Context, val url: String): JSlogicface {
    init {
        YoheeApp.injector.inject(this)
    }
    @Inject
    internal lateinit var adblock: AdBlock
    @Inject
    internal lateinit var findtag: FindTag
    @Inject
    internal lateinit var removeNode: RemoveNode

    override fun getJs(): String {
        val url = if (url.length <= 100) url else url.substring(0, 100)
        return String.format(adblock.provideJs(), url, findtag.provideJs(), removeNode.provideJs())
    }

}