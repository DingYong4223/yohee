package com.fula.yohee.toolcolor

import android.graphics.Bitmap
import android.view.View
import androidx.core.net.toUri
import androidx.palette.graphics.Palette
import com.fula.CLog
import com.fula.view.progress.WebProgressBar
import com.fula.yohee.YoheeApp
import com.fula.yohee.database.WebColor
import com.fula.yohee.database.WebColorDatabase
import com.fula.yohee.di.DatabaseScheduler
import com.fula.yohee.ui.WebViewController
import com.fula.yohee.ui.activity.WebActivity
import com.fula.yohee.utils.ThemeUtils
import com.fula.yohee.utils.UrlUtils
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class ToolbarColor(context: WebActivity, colorViews: List<View>, progressView: WebProgressBar)
    : BaseToolColor(context, colorViews, progressView) {

    init {
        YoheeApp.injector.inject(this)
    }

    @Inject
    internal lateinit var webcolorModel: WebColorDatabase
    @Inject
    @field:DatabaseScheduler
    lateinit var dbScheduler: Scheduler

    /**预测toolbar颜色*/
    override fun predictToolbarColor(url: String) {
        queryThenExec(url, { webColor, _ ->
            CLog.i("query result = $webColor")
            initToolBarColor(webColor.color, COLOR_TRANS_LONG)
        }) { _, _ -> }
    }

    override fun guessToolbarColor(url: String, capture: Bitmap) {
        queryThenExec(url, { webColor, host ->
            CLog.i("query result = $webColor")
            if (WebColor.TYPE_HANDLE != webColor.type) {
                calcAndSetToolbarColor(capture, host)
            }
        }) { _, host ->
            calcAndSetToolbarColor(capture, host)
        }
    }

    override fun tabSwitch(tab: WebViewController) {
        if (UrlUtils.isGenUrl(tab.url)) {
            initToolBarColor(ThemeUtils.getPrimaryColorTrans(context), COLOR_TRANS_LONG)
        } else {
            queryThenExec(tab.url, { webColor, host ->
                CLog.i("query result = $webColor")
                initToolBarColor(webColor.color, COLOR_TRANS_LONG)
                calcAndSetToolbarColor(tab.captureImage, host, COLOR_TRANS_LONG)
            }) { _, host ->
                calcAndSetToolbarColor(tab.captureImage, host, COLOR_TRANS_LONG)
            }
        }
    }

    private var dis: Disposable? = null
    private fun queryThenExec(url: String, exec: (WebColor, String) -> Unit, err: (Any, String) -> Unit) =
            url.toUri().host?.let {
                dis?.dispose()
                dis = webcolorModel.queryItem(it)
                        .subscribeOn(dbScheduler)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ suc -> exec(suc, it) }) { e -> err(e, it) }
            }


    private fun calcAndSetToolbarColor(capture: Bitmap?, host: String?, span: Long = COLOR_TRANS_SHORT) {
        if (null == capture) {
            return initToolBarColor(DEF_TOOL_COLOR)
        }
        val height = (capture.height * TITLE_BAR_RATE).toInt()
        val totalNum = capture.width * height * 1.0f
        val palette = Palette.from(capture).setRegion(0, 0, capture.width, height).generate()
        val vnum = palette.vibrantSwatch.let {
            it?.population ?: 0
        }
        val lvnum = palette.lightVibrantSwatch.let {
            it?.population ?: 0
        }
        val dvrate = palette.darkVibrantSwatch.let {
            it?.population ?: 0
        } / totalNum
        val dmrate = palette.darkMutedSwatch.let {
            it?.population ?: 0
        } / totalNum
        val vrate = vnum / totalNum
        val lvrate = lvnum / totalNum
        val color = when {
            vrate >= VRATE_RATE -> {
                if (lvnum / vnum >= 10) palette.lightVibrantSwatch!!.rgb else palette.vibrantSwatch!!.rgb
            }
            lvrate >= MAIN_COLOR_RATE -> {
                palette.lightVibrantSwatch!!.rgb
            }
            dvrate >= MAIN_COLOR_RATE -> {
                palette.darkVibrantSwatch!!.rgb
            }
            dmrate >= RATE_DARKMUTEDSWATCH -> {
                palette.darkMutedSwatch!!.rgb
            }
            else -> {
                DEF_TOOL_COLOR
            }
        }
        initToolBarColor(color, span)
        if (!host.isNullOrEmpty() && context.getWebScollY() <= 10) {
            CLog.i("save calc color to db...")
            webcolorModel.backinsertOrUpdateItem(WebColor(host, color), dbScheduler)
        }
    }

    companion object {
        const val VRATE_RATE = 0.0075f
        const val TITLE_BAR_RATE = 0.16f
        const val MAIN_COLOR_RATE = 0.1f
        const val RATE_DARKMUTEDSWATCH = 0.005f
    }

}