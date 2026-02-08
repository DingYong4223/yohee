package com.fula.yohee.di

import com.fula.base.ui.video.GSYVideoActivity
import com.fula.yohee.YoheeApp
import com.fula.yohee.MenuPop
import com.fula.yohee.adblock.AssetsAdBlocker
import com.fula.yohee.adblock.SessionAdBlocker
import com.fula.yohee.dialog.SDialogBuilder
import com.fula.yohee.download.DownloadHandler
import com.fula.yohee.download.SDownloadListener
import com.fula.yohee.js.*
import com.fula.yohee.search.SuggestionsAdapter
import com.fula.yohee.toolcolor.ToolbarColor
import com.fula.yohee.ui.WebViewController
import com.fula.yohee.ui.activity.BaseActivity
import com.fula.yohee.ui.activity.WebActivity
import com.fula.yohee.ui.fragment.FragBookmark
import com.fula.yohee.ui.fragment.FragHistory
import com.fula.yohee.ui.page.*
import com.fula.yohee.utils.DiskSave
import com.fula.yohee.view.YoheeChromeClient
import com.fula.yohee.view.YoheeWebClient
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [(AppModule::class), (AppBindsModule::class)])
interface AppComponent {
    fun inject(activity: WebActivity)
    fun inject(fragment: FragBookmark)
    fun inject(builder: SDialogBuilder)
    fun inject(popMenu: MenuPop)
    fun inject(webViewController: WebViewController)
    fun inject(app: YoheeApp)
    fun inject(webClient: YoheeWebClient)
    fun inject(listener: SDownloadListener)
    fun inject(suggestionsAdapter: SuggestionsAdapter)
    fun inject(chromeClient: YoheeChromeClient)
    fun inject(downloadHandler: DownloadHandler)
    fun inject(frag: FragHistory)
    fun inject(activity: BaseActivity)
    fun inject(pageSetting: PageSetting)
    fun inject(pageSetting: PageMarkBatchEdit)
    fun inject(pagesWebSetting: PageWebSetting)
    fun inject(pageAdblock: PageAdblock)
    fun inject(pageDownloadCache: PageDownload)
    fun inject(pageAppdataCopy: PageDataSave)
    fun inject(jsAdBlock: JSAdBlock)
    fun inject(jsOpenSug: JSOpenSug)
    fun inject(jsAdUIClear: JSAdUIClear)
    fun inject(diskSave: DiskSave)
    fun inject(toolbarClore: ToolbarColor)
    fun inject(player: GSYVideoActivity)
    fun inject(jsAppendMarks: JSAppendMarks)
    fun inject(jsGetUrlTxt: JsGetUrlTxt)

    fun provideAssetsAdBlocker(): AssetsAdBlocker
    fun provideSessionAdBlocker(): SessionAdBlocker
}
