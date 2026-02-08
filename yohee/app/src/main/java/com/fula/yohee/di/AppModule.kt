package com.fula.yohee.di

import android.app.Application
import android.app.DownloadManager
import android.app.NotificationManager
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ShortcutManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import com.fula.downloader.m3u8.M3U8Downloader
import com.fula.fano.YanoGenerator
import com.fula.yohee.YoheeApp
import com.fula.yohee.html.ListPageReader
import com.fula.yohee.html.homepage.HomePageReader
import com.fula.yohee.js.*
import com.fula.yohee.search.suggestions.RequestFactory
import com.fula.yohee.utils.FileUtils
import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import okhttp3.*
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
class AppModule(private val yoheeApp: YoheeApp) {

    @Provides
    @MainHandler
    fun provideMainHandler() = Handler(Looper.getMainLooper())

    @Provides
    fun provideApplication(): Application = yoheeApp

    @Provides
    fun provideContext(): Context = yoheeApp.applicationContext

//    @Provides
//    DevPrefs
//    fun provideDebugPreferences(): SharedPreferences = yoheeApp.getSharedPreferences("settings", 0)

    @Provides
    @UserPrefs
    fun provideUserPreferences(): SharedPreferences = yoheeApp.getSharedPreferences("yosettings", 0)

    @Provides
    fun providesClipboardManager() = yoheeApp.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    @Provides
    fun providesInputMethodManager() = yoheeApp.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    @Provides
    fun providesDownloadManager() = yoheeApp.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    @Provides
    fun providesConnectivityManager() = yoheeApp.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @Provides
    fun providesNotificationManager() = yoheeApp.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @Provides
    fun providesWindowManager() = yoheeApp.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    @Provides
    fun providesShortcutManager() = yoheeApp.getSystemService(Context.SHORTCUT_SERVICE) as ShortcutManager

    @Provides
    @DatabaseScheduler
    @Singleton
    fun providesIoThread(): Scheduler = Schedulers.from(Executors.newSingleThreadExecutor())

    @Provides
    @DiskScheduler
    @Singleton
    fun providesDiskThread(): Scheduler = Schedulers.from(Executors.newSingleThreadExecutor())

    @Provides
    @NetworkScheduler
    @Singleton
    fun providesNetworkThread(): Scheduler = Schedulers.from(ThreadPoolExecutor(0, 4, 60, TimeUnit.SECONDS, LinkedBlockingDeque()))

    @Singleton
    @Provides
    fun providesSuggestionsCacheControl(): CacheControl = CacheControl.Builder().maxStale(1, TimeUnit.DAYS).build()

    @Singleton
    @Provides
    fun providesSuggestionsRequestFactory(cacheControl: CacheControl): RequestFactory = object : RequestFactory {

        override fun createSuggestionsRequest(httpUrl: HttpUrl, encoding: String): Request {
            return Request.Builder().url(httpUrl)
                .addHeader("Accept-Charset", encoding)
                .cacheControl(cacheControl)
                .build()
        }
    }

    @Singleton
    @Provides
    fun providesM3u3Downloader(): M3U8Downloader = M3U8Downloader
                .Builder()
//                .fileNameGenerator(VideoNameGenerator())
//                .cacheRoot(FileUtils.yoheeCacheDir(YoheeApp.app).path)
                .cacheRoot(FileUtils.createYoheeFolder(FileUtils.DOWNLOAD_FILE).path)


    @Singleton
    @Provides
    fun providesSuggestionsHttpClient(): OkHttpClient {
        val intervalDay = TimeUnit.DAYS.toSeconds(1)

        val rewriteCacheControlInterceptor = Interceptor { chain ->
            val originalResponse = chain.proceed(chain.request())
            originalResponse.newBuilder()
                .header("cache-control", "max-age=$intervalDay, max-stale=$intervalDay")
                .build()
        }

        val suggestionsCache = File(yoheeApp.cacheDir, "suggestion_responses")

        return OkHttpClient.Builder()
            .cache(Cache(suggestionsCache, FileUtils.megabytesToBytes(1)))
            .addNetworkInterceptor(rewriteCacheControlInterceptor)
            .build()
    }

    @Provides
    fun providesListPageReader(): ListPageReader = YanoGenerator.ListPageReader()

    @Provides
    fun providesHomePageReader(): HomePageReader = YanoGenerator.HomePageReader()

    @Provides
    fun provideOpensug(): OpenSug = YanoGenerator.OpenSug()

    @Provides
    fun provideAdblock(): AdBlock = YanoGenerator.AdBlock()

    @Provides
    fun provideAdUIClear(): AdUIClear = YanoGenerator.AdUIClear()

    @Provides
    fun provideAppendMarks(): AppendMarks = YanoGenerator.AppendMarks()

    @Provides
    fun provideRemoveNode(): RemoveNode = YanoGenerator.RemoveNode()

    @Provides
    fun provideFindTagThenExec(): FindTagThenExec = YanoGenerator.FindTagThenExec()
    @Provides
    fun provideFindTag(): FindTag = YanoGenerator.FindTag()
    @Provides
    fun provideGetUrlTxt(): GetUrlTxt = YanoGenerator.GetUrlTxt()
}

@Qualifier
@Retention(AnnotationRetention.SOURCE)
annotation class MainHandler

@Qualifier
@Retention(AnnotationRetention.SOURCE)
annotation class UserPrefs

@Qualifier
@Retention(AnnotationRetention.SOURCE)
annotation class DevPrefs

@Qualifier
@Retention(AnnotationRetention.SOURCE)
annotation class DiskScheduler

@Qualifier
@Retention(AnnotationRetention.SOURCE)
annotation class NetworkScheduler

@Qualifier
@Retention(AnnotationRetention.SOURCE)
annotation class DatabaseScheduler
