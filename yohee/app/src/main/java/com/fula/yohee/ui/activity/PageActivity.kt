package com.fula.yohee.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.fula.base.iview.BasePage
import com.fula.base.page.PageError
import com.fula.base.util.StatusBarUtil
import com.fula.yohee.settings.UserSetting


/**
 * @Desc: 用于显示功能简单的界面，主要用于静态页面的显示，不必注册多个activity。
 * @Date: 2018-09-10
 * @author: delanding
 */
class PageActivity : BasePageActivity<BasePage>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.setStatusBarIconMode(this, userPrefer.useTheme == UserSetting.NO_VALUE)
        mPage.onCreate()
    }

    override fun onResume() {
        super.onResume()
        mPage.onResume()
    }


    override fun getViewObj(): BasePage? {
        val intent = intent
        val pageName = intent.getStringExtra(KEY_FIT_PAGE)
        var clazz: Class<out BasePage>?
        try {
            clazz = Class.forName(pageName) as Class<BasePage>
            return clazz.newInstance()
        } catch (e: Exception) {
            clazz = PageError::class.java
            e.printStackTrace()
        }
        return try {
            clazz!!.newInstance()
        } catch (e1: Exception) {
            e1.printStackTrace()
            null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        mPage.onActivityResult(requestCode, resultCode, intent)
    }

    public override fun onDestroy() {
        mPage.onDestroy()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (mPage.onBackPressed()) {
            return
        }
        super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (mPage.onCreateOptionsMenu(menu)) {
            return true
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId ==android.R.id.home) {
            return mPage.onMenuBack()
        }
        if (mPage.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val KEY_FIT_PAGE = "key_fit_page"

        /**不带参数启动 */
        fun genIntent(context: Context, cls: Class<*>): Intent {
            val intent = Intent(context, PageActivity::class.java)
            intent.putExtra(KEY_FIT_PAGE, cls.name)
            return intent
        }
    }

}