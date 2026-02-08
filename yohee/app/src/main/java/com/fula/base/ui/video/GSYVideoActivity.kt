package com.fula.base.ui.video

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fula.CLog
import com.fula.base.ToolUtils
import com.fula.yohee.YoheeApp
import com.fula.yohee.database.VProgressDatabase
import com.fula.yohee.di.DatabaseScheduler
import com.fula.yohee.ui.bilogic.SelectModel
import com.fula.yohee.utils.UrlUtils
import com.fula.downloader.m3u8.M3U8Downloader
import com.fula.downloader.m3u8.M3U8HttpServer
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import com.shuyu.gsyvideoplayer.player.SystemPlayerManager
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class GSYVideoActivity : AppCompatActivity() {

    @Inject
    lateinit var prosDatabase: VProgressDatabase
    @Inject
    @field:DatabaseScheduler
    internal lateinit var dbScheduler: Scheduler
    @Inject
    lateinit var m3U8Downloader: M3U8Downloader
    private val videoPlayer: StandardGSYVideoPlayer by lazy { StandardGSYVideoPlayer(this) }
    private var m3u8Server: M3U8HttpServer? = null
    private val vInfo: SelectModel by lazy { intent.getSerializableExtra(VEDIO_INFO) as SelectModel }
    private var dis: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(videoPlayer)
        YoheeApp.injector.inject(this)
        if (intent.data != null) {
            intent.data!!.toString().let {
                startPlay(it, UrlUtils.getNameFromUrl(it))
            }
        } else {
            startPlay(vInfo.urlTxt, vInfo.title)
        }
    }

    override fun onResume() {
        videoPlayer.currentPlayer.onVideoResume(false)
        super.onResume()
    }

    private fun startPlay(url: String, title: String) {
        if (m3U8Downloader.checkExist(url)) {
            CLog.i("start local server play...")
            m3u8Server = M3U8HttpServer().apply {
                execute()
                videoPlayer.currentPlayer.setUp(createLocalHttpUrl(m3U8Downloader.getM3U8Path(url)), false, title)
            }
        } else {
            CLog.i("start remote server play...")
            videoPlayer.currentPlayer.setUp(url, true, title)
        }
        videoPlayer.startWindowFullscreen(this, false, false)
        videoPlayer.setBackFromFullScreenListener { finish() }
        videoPlayer.currentPlayer.startPlayLogic()
        dis = prosDatabase.query(ToolUtils.md5(videoPlayer.currentPlayer.originUrl))
                .subscribeOn(dbScheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    CLog.i("last progress: ${it.progress}")
                    videoPlayer.currentPlayer.seekOnStart = it.progress
                }
    }

    override fun onPause() {
        CLog.i("save progress = ${videoPlayer.currentPlayer.currentPositionWhenPlaying}")
        prosDatabase.updateOrAdd(ToolUtils.md5(videoPlayer.currentPlayer.originUrl), videoPlayer.currentPlayer.currentPositionWhenPlaying.toLong())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe()
        videoPlayer.currentPlayer.onVideoPause()
        super.onPause()
    }

    override fun onDestroy() {
        dis?.dispose()
        m3u8Server?.finish()
        videoPlayer.destroy()
        super.onDestroy()
    }

    companion object {
        private const val VEDIO_INFO = "VEDIO_INFO"
        fun genIntent(context: Context, title: String, url: String): Intent {
            PlayerFactory.setPlayManager(SystemPlayerManager::class.java)

            val vInfo = SelectModel(null, title, url)
            return Intent(context, GSYVideoActivity::class.java).apply {
                putExtra(VEDIO_INFO, vInfo)
            }
        }
    }

}
