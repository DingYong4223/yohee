package com.fula.yohee.js

import com.fula.fano.file2string

/**
 * Force the text to reflow.
 */
@file2string("app/src/main/js/RemoveNode.js")
interface RemoveNode {
    fun provideJs(): String
}

@file2string("app/src/main/js/FindTagThenExec.js")
interface FindTagThenExec {
    fun provideJs(): String
}

@file2string("app/src/main/js/FindTag.js")
interface FindTag {
    fun provideJs(): String
}