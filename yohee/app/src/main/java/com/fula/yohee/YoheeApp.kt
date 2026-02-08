package com.fula.yohee

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.webkit.WebView
import androidx.appcompat.app.AppCompatDelegate
import com.flurry.android.FlurryAgent
import com.fula.CLog
import com.fula.yohee.di.AppComponent
import com.fula.yohee.di.AppModule
import com.fula.yohee.di.DaggerAppComponent
import com.fula.yohee.utils.FileUtils
import com.fula.yohee.utils.MemoryLeakUtils
import com.fula.yohee.utils.Utils
import io.reactivex.plugins.RxJavaPlugins


class YoheeApp : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            appDebugConfig()
        }

        FlurryAgent.Builder()
                .withLogEnabled(true)
                .withCaptureUncaughtExceptions(true)
                .withContinueSessionMillis(10000)
                .withLogLevel(2)
                .withListener { FlurryAgent.logEvent(FlurryConst.APP_INITED) }
                .build(this, Config.FLURRY_API_KEY)
        Thread.getDefaultUncaughtExceptionHandler().let {
            Thread.setDefaultUncaughtExceptionHandler { thread, ex ->
                FileUtils.writeCrashToStorage(ex)
                it.uncaughtException(thread, ex)

                FlurryAgent.logEvent(FlurryConst.APP_CRASH, HashMap<String, String>().apply {
                    this["detail"] = Utils.crash2string(ex) {
                        return@crash2string it.className.contains("fula")
                    }
                })
            }
        }
        RxJavaPlugins.setErrorHandler { throwable: Throwable? ->
            throwable?.apply {
                FileUtils.writeCrashToStorage(this)

                FlurryAgent.logEvent(FlurryConst.APP_CRASH, HashMap<String, String>().apply {
                    this["detail"] = Utils.crash2string(throwable) {
                        return@crash2string it.className.contains("fula")
                    }
                })
            }
        }
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
        registerActivityLifecycleCallbacks(object : MemoryLeakUtils.LifecycleAdapter() {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                CLog.i("activity create: $activity")
                if (activity is MainActivity) {
                    CLog.i("main activity create...")
                    mainActivity = activity
                }
            }
            override fun onActivityDestroyed(activity: Activity) {
                CLog.i("Cleaning up after the Android framework")
                MemoryLeakUtils.clearNextServedView(activity, this@YoheeApp)
                if (activity is MainActivity) {
                    CLog.i("main activity create...")
                    mainActivity = null
                }
            }
        })
    }

    private fun appDebugConfig() {
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build())
        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build())
        //LeakCanary.install(this)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        app = this
        injector = DaggerAppComponent.builder().appModule(AppModule(app)).build()
    }

    companion object {

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT)
        }

        @JvmStatic
        lateinit var injector: AppComponent
        lateinit var app: YoheeApp
        var mainActivity: Activity? = null
    }

}
