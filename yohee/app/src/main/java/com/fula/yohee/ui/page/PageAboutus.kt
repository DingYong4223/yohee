package com.fula.yohee.ui.page

import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.fula.CLog
import com.fula.base.iview.BasePage
import com.fula.util.GoToScoreUtils
import com.fula.util.YoUtils
import com.fula.yohee.BuildConfig
import com.fula.yohee.Config
import com.fula.yohee.R
import com.fula.yohee.utils.ShareUtils
import kotlinx.android.synthetic.main.page_about_us.view.*
import java.util.*


/**
 * @Desc: user infomation download setting page
 * @Date: 2018-09-11
 * @author: delanding
 */
class PageAboutus : BasePage() {

    private val KEY_SHARE by lazy { mContext.getString(R.string.share_tip) }

    override fun initPage(inflater: LayoutInflater, container: ViewGroup) {
        super.initPage(container, R.layout.page_about_us)
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
        mView.protol_serve.paint.flags = Paint.UNDERLINE_TEXT_FLAG
        mView.protol_secure.paint.flags = Paint.UNDERLINE_TEXT_FLAG

        mView.txt_version.text = "${mContext.getString(R.string.app_name)}-${BuildConfig.BUILD_TYPE}：${BuildConfig.VERSION_NAME}"
        mView.update_check.setOnClickListener {
            GoToScoreUtils.goToMarket(mContext, YoUtils.getPackageName(mContext))
        }
        mView.protol_serve.setOnClickListener {
            val intent = PageWeb.genIntent(mContext, R.string.protol_serve, Config.PROTOL_WEB)
            mContext.startActivity(intent)
        }
        mView.protol_secure.setOnClickListener {
            val intent = PageWeb.genIntent(mContext, R.string.privacy_secure, Config.SECURE_WEB)
            mContext.startActivity(intent)
        }
        mView.app_share.setOnClickListener {
            val intent = PageItemEdit.genIntent(mContext, HashMap<String, String>().apply {
                this[KEY_SHARE] = ""
            }, R.string.app_share)
            mContext.startActivityForResult(intent, PageItemEdit.ACTIVITY_RESULT_SHARE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (intent == null) {
            return CLog.i("no intent back...")
        }
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PageItemEdit.ACTIVITY_RESULT_SHARE) {
                val map = intent.getSerializableExtra(PageItemEdit.KEY_MAP) as HashMap<String, String>
                val txt = map[KEY_SHARE]
                txt?.let {
                    ShareUtils.shareText(mContext, it)
                }
            }
        }
    }

}

