package com.fula.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.TextUtils
import java.util.*

object GoToScoreUtils {

    fun goToMarket(context: Context, packageName: String) {
        try {
            val uri = Uri.parse("market://details?id=$packageName")
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    /**
     * 第三种方法
     * 首先先获取手机上已经安装的应用市场
     * 获取已安装应用商店的包名列表
     * 获取有在AndroidManifest 里面注册<category android:name="android.intent.category.APP_MARKET"></category>的app
     * @param context
     * @return
     */
    fun getInstallAppMarkets(context: Context?): ArrayList<String> {
        //默认的应用市场列表，有些应用市场没有设置APP_MARKET通过隐式搜索不到
        val pkgList = ArrayList<String>()
        //将我们上传的应用市场都传上去
        pkgList.add("com.xiaomi.market")                       //小米应用商店
        pkgList.add("com.lenovo.leos.appstore")                //联想应用商店
        pkgList.add("com.oppo.market")                         //OPPO应用商店
        pkgList.add("com.tencent.android.qqdownloader")        //腾讯应用宝
        pkgList.add("com.qihoo.appstore")                      //360手机助手
        pkgList.add("com.baidu.appsearch")                     //百度手机助手
        pkgList.add("com.huawei.appmarket")                    //华为应用商店
        pkgList.add("com.wandoujia.phoenix2")                  //豌豆荚
        pkgList.add("com.hiapk.marketpho")                     //安智应用商店
        val pkgs = ArrayList<String>()
        if (context == null)
            return pkgs
        val intent = Intent()
        intent.action = "android.intent.action.MAIN"
        intent.addCategory("android.intent.category.APP_MARKET")
        val pm = context.packageManager
        val info = pm.queryIntentActivities(intent, 0)
        if (info == null || info.size == 0)
            return pkgs
        val size = info.size
        for (i in 0 until size) {
            var pkgName = ""
            try {
                val activityInfo = info[i].activityInfo
                pkgName = activityInfo.packageName
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (!TextUtils.isEmpty(pkgName))
                pkgs.add(pkgName)
        }
        //取两个list并集,去除重复
        pkgList.removeAll(pkgs)
        pkgs.addAll(pkgList)
        return pkgs
    }

    /**
     * 过滤出已经安装的包名集合
     * @param context
     * @param pkgs  待过滤包名集合
     * @return      已安装的包名集合
     */
    fun getFilterInstallMarkets(context: Context?, pkgs: ArrayList<String>?): ArrayList<String> {
        val mAppInfo = ArrayList<AppInfo>()
        mAppInfo.clear()
        val appList = ArrayList<String>()
        if (context == null || pkgs == null || pkgs.size == 0)
            return appList
        val pm = context.packageManager
        val installedPkgs = pm.getInstalledPackages(0)
        val li = installedPkgs.size
        val lj = pkgs.size
        for (j in 0 until lj) {
            for (i in 0 until li) {
                var installPkg = ""
                val checkPkg = pkgs[j]
                val packageInfo = installedPkgs[i]
                try {
                    installPkg = packageInfo.packageName

                } catch (e: Exception) {
                    e.printStackTrace()
                }

                if (TextUtils.isEmpty(installPkg))
                    continue
                if (installPkg == checkPkg) {
                    // 如果非系统应用，则添加至appList,这个会过滤掉系统的应用商店，如果不需要过滤就不用这个判断
                    if (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                        //将应用相关信息缓存起来，用于自定义弹出应用列表信息相关用
                        val appInfo = AppInfo(packageInfo.applicationInfo.loadLabel(context.packageManager).toString(),
                                packageInfo.applicationInfo.loadIcon(context.packageManager),
                                packageInfo)
                        mAppInfo.add(appInfo)
                        appList.add(installPkg)
                    }
                    break
                }
            }
        }
        return appList
    }


    /**
     * 获取已安装应用商店的包名列表
     * @param context       context
     * @return
     */
    fun queryInstalledMarketPkgs(context: Context?): ArrayList<String> {
        val pkgs = ArrayList<String>()
        if (context == null)
            return pkgs
        val intent = Intent()
        intent.action = "android.intent.action.MAIN"
        intent.addCategory("android.intent.category.APP_MARKET")
        val pm = context.packageManager
        val infos = pm.queryIntentActivities(intent, 0)
        if (infos == null || infos.size == 0)
            return pkgs
        val size = infos.size
        for (i in 0 until size) {
            var pkgName = ""
            try {
                val activityInfo = infos[i].activityInfo
                pkgName = activityInfo.packageName
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (!TextUtils.isEmpty(pkgName))
                pkgs.add(pkgName)
        }
        return pkgs
    }

    /**
     * 过滤出已经安装的包名集合
     * @param context
     * @param pkgs 待过滤包名集合
     * @return 已安装的包名集合
     */
    fun filterInstalledPkgs(context: Context?, pkgs: ArrayList<String>?): ArrayList<String> {
        val empty = ArrayList<String>()
        if (context == null || pkgs == null || pkgs.size == 0)
            return empty
        val pm = context.packageManager
        val installedPkgs = pm.getInstalledPackages(0)
        val li = installedPkgs.size
        val lj = pkgs.size
        for (j in 0 until lj) {
            for (i in 0 until li) {
                var installPkg = ""
                val checkPkg = pkgs[j]
                try {
                    installPkg = installedPkgs[i].applicationInfo.packageName
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                if (TextUtils.isEmpty(installPkg))
                    continue
                if (installPkg == checkPkg) {
                    empty.add(installPkg)
                    break
                }
            }
        }
        return empty
    }


    /**
     * 跳转到应用市场app详情界面
     * @param appPkg    App的包名
     * @param marketPkg 应用市场包名
     */
    fun launchAppDetail(context: Context, appPkg: String, marketPkg: String) {
        try {
            if (TextUtils.isEmpty(appPkg))
                return
            val uri = Uri.parse("market://details?id=$appPkg")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            if (!TextUtils.isEmpty(marketPkg)) {
                intent.setPackage(marketPkg)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}

class AppInfo(val name: String, val icon: Drawable, val packageInfo: PackageInfo)
