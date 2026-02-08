package com.fula.yohee.html

import com.fula.fano.file2string

/**
 * The store for the list view HTML.
 */
@file2string("app/src/main/html/list.html")
interface ListPageReader {
    fun provideHtml(): String
}
