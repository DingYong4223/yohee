package com.fula.yohee.search.engine

import com.fula.yohee.R

class YahooSearch : BaseSearchEngine(
        R.mipmap.icon_yahoo,
        "file:///android_asset/yahoo.png",
        "https://search.yahoo.com/search?p=",
        R.string.search_engine_yahoo,
        ENGINE_YAHOO
)
