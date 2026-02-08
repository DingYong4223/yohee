package com.fula.yohee.js

import com.fula.fano.file2string

/**
 * Reads the theme color from the DOM.
 */
@file2string("app/src/main/js/ThemeColor.js")
interface ThemeColor {

    fun provideJs(): String

}