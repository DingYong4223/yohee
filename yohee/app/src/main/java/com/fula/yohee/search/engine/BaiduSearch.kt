package com.fula.yohee.search.engine

import com.fula.yohee.R

class BaiduSearch : BaseSearchEngine(
        R.mipmap.icon_baidu,
        "file:///android_asset/baidu.png",
        "https://www.baidu.com/s?wd=",
        R.string.search_engine_baidu,
        ENGINE_BAIDU
)
