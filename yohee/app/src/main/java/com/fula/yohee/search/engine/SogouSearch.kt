package com.fula.yohee.search.engine

import com.fula.yohee.R

class SogouSearch : BaseSearchEngine(
        R.mipmap.icon_sogou,
        "file:///android_asset/sogou.png",
        "https://m.sogou.com/web/searchList.jsp?keyword=",
        R.string.search_engine_sogou,
        ENGINE_SOGOU
)
