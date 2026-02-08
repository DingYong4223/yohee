package com.fula.yohee.html.homepage

import com.fula.fano.file2string

/**
 * The store for the homepage HTML.
 */
@file2string("app/src/main/html/homepage.html")
interface HomePageReader {
    fun provideHtml(): String
}