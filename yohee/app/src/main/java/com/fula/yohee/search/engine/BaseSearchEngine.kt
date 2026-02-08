package com.fula.yohee.search.engine

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * A class representative of a search engine.
 *
 * Contains three key pieces of information:
 *  - The icon shown for the search engine, should point to a local assets URL.
 *  - The query URL for the search engine, the query will be appended to the end.
 *  - The title string resource for the search engine.
 */
open class BaseSearchEngine(
        @DrawableRes val menuIcon: Int,
        val iconUrl: String,
        val queryUrl: String,
        @StringRes val titleRes: Int,
        val intArg: Int) {

    operator fun component0() = menuIcon

    operator fun component1() = iconUrl

    operator fun component2() = queryUrl

    operator fun component3() = titleRes

    operator fun component4() = intArg

    companion object {
        const val ENGINE_GOOGLE = 0
        const val ENGINE_BAIDU = 1
        const val ENGINE_YAHOO = 2
        const val ENGINE_SOGOU = 3
    }

}
