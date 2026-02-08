package com.fula.yohee.ui.page

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import com.fula.base.iview.BasePage
import com.fula.yohee.R
import kotlinx.android.synthetic.main.page_web.view.*


/**
 * @Desc: user infomation download setting page
 * @Date: 2018-09-11
 * @author: delanding
 */
class PageWeb : BasePage() {

    private val url by lazy { intent.getStringExtra(KEY_URL) }

    override fun initPage(inflater: LayoutInflater, container: ViewGroup) {
        super.initPage(container, R.layout.page_web)
        mContext.setSupportActionBar(findViewById(R.id.toolSetingbar) as Toolbar)
        initToolBar()
        initView()
    }

    private fun initToolBar() {
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_array_left)
        actionBar.title = title
    }

    private fun initView() {
        mView.web_view.loadUrl(url)
    }

    companion object {

        private const val KEY_URL = "key_url"
        fun genIntent(context: Context, @StringRes titleRes: Int, url: String): Intent {
            val intent = BasePage.genTitleIntent(context, PageWeb::class.java, titleRes)
            intent.putExtra(KEY_URL, url)
            return intent
        }

    }

}

