package com.fula.yohee.js

import com.fula.fano.file2string

/**
 * Force the text to reflow.
 */
@file2string("app/src/main/js/TextReflow.js")
interface TextReflow {

    fun provideJs(): String

}