package com.fula.base.page

import android.view.LayoutInflater
import android.view.ViewGroup
import com.fula.base.iview.BasePage
import com.fula.yohee.R

class PageError : BasePage() {

    override fun initPage(inflater: LayoutInflater, container: ViewGroup) {
        super.initPage(container, R.layout.page_error)
    }

}

