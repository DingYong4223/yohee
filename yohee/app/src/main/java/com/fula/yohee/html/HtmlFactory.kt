package com.fula.yohee.html

import com.fula.yohee.constant.FILE
import io.reactivex.Single
import java.io.File

/**
 * A factory that builds an HTML page.
 */
interface HtmlFactory {

    /**
     * Build the HTML page and emit the URL.
     */
    fun buildPage(): Single<String>
    fun getFile(): File
    fun getUrl(): String = "$FILE${getFile()}"

}
