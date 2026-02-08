package com.fula.yohee.ui.page

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ResolveInfo
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.flurry.android.FlurryAgent
import com.fula.base.iview.BasePage
import com.fula.yohee.FlurryConst
import com.fula.yohee.R
import kotlinx.android.synthetic.main.page_setting_default.view.*


/**
 * @Desc: user default browser setting.
 * @Date: 2018-09-11
 * @author: delanding
 */
class PageSettingDefault : BasePage() {

    private val engineUrl: String by lazy { intent.getStringExtra(KEY_ARGS) }

    override fun initPage(inflater: LayoutInflater, container: ViewGroup) {
        super.initPage(container, R.layout.page_setting_default)
        mContext.setSupportActionBar(findViewById(R.id.toolSetingbar) as Toolbar)
        initToolBar()
    }

    private fun initToolBar() {
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_array_left)
        actionBar.title = title
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun updateUI() {
        val blist = getDefaultBrowserList(mContext)
        val defPkgInfo = if (blist.isNotEmpty()) blist[0] else null

        if (null != defPkgInfo) {
            val selfName = mContext.packageName
            if (selfName == defPkgInfo.activityInfo.packageName) {
                mView.bind_image.visibility = View.GONE
                mView.start_setting.visibility = View.GONE
                mView.default_setting_tip.let {
                    it.visibility = View.VISIBLE
                    it.setText(R.string.setting_success)
                }
                return
            }
            mView.bind_image.visibility = View.VISIBLE
            mView.start_setting.let {
                it.visibility = View.VISIBLE
                it.setText(R.string.goto_clear)
                it.setOnClickListener {
                    FlurryAgent.logEvent(FlurryConst.SETTING_DEFBROWSE_SET_NOW_CLICK)
                    toSetting(defPkgInfo.activityInfo.packageName)
                }
            }
            mView.default_setting_tip.let {
                it.visibility = View.VISIBLE
                it.setText(R.string.default_clear_tip)
            }
        } else { //未设置默认
            mView.bind_image.visibility = View.VISIBLE
            mView.default_setting_tip.visibility = View.GONE
            mView.start_setting.let {
                it.start_setting.setText(R.string.goto_setting)
                it.start_setting.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(engineUrl)
                    mContext.startActivity(intent)
                }
            }
        }
    }

    private fun toSetting(pkgName: String) {
        try {
            val mIntent = Intent()
            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            mIntent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
            mIntent.data = Uri.fromParts("package", pkgName, null)

            mContext.startActivity(mIntent)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun getDefaultBrowserList(paramContext: Context): List<ResolveInfo> {
        val defBrowseList = mutableListOf<ResolveInfo>()
        val localPackageManager = paramContext.packageManager
        val localIntent = Intent("android.intent.action.VIEW")
        localIntent.addCategory(BROWSE_CATEGRY)
        localIntent.data = Uri.parse("http://bbs.liebao.cn")
        try {
            val defList = localPackageManager.queryIntentActivities(localIntent, 0)
            if (defList != null) {
                val tempList = mutableListOf<ComponentName>()
                val tmpList = mutableListOf<IntentFilter>()
                val iter = defList.iterator()
                while (iter.hasNext()) {
                    val localResolveInfo = iter.next() as ResolveInfo
                    localPackageManager.getPreferredActivities(tmpList, tempList, localResolveInfo.activityInfo.packageName)
                    val iter1 = tmpList.iterator()
                    while (iter1.hasNext()) {
                        val localIntentFilter = iter1.next()
                        if (!localIntentFilter.hasCategory(BROWSE_CATEGRY) && !localIntentFilter.hasCategory("android.intent.category.DEFAULT")/* || !localIntentFilter.hasDataScheme("http")*/)
                            continue
                        defBrowseList.add(localResolveInfo)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return defBrowseList
    }

    companion object {
        const val BROWSE_CATEGRY = "android.intent.category.BROWSABLE"
    }

}

