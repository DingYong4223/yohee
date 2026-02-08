package com.fula.yohee.search.engine

import com.fula.yohee.R

/**
 * The Google search engine.
 *
 * See https://www.google.com/images/srpr/logo11w.png for the icon.
 */
class GoogleSearch : BaseSearchEngine(
        R.mipmap.icon_google,
    "file:///android_asset/google.png",
    "https://www.google.com/search?client=yohee&ie=UTF-8&oe=UTF-8&q=",
    R.string.search_engine_google,
        ENGINE_GOOGLE
)
