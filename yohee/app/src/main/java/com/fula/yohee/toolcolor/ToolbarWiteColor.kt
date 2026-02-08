package com.fula.yohee.toolcolor

import android.graphics.Bitmap
import android.graphics.Color
import android.view.View
import com.fula.view.progress.WebProgressBar
import com.fula.yohee.ui.WebViewController
import com.fula.yohee.ui.activity.WebActivity

class ToolbarWiteColor(context: WebActivity, colorViews: List<View>, progressView: WebProgressBar)
    : BaseToolColor(context, colorViews, progressView) {

    override fun predictToolbarColor(url: String) = initToolBarColor(Color.WHITE, COLOR_TRANS_LONG)
    override fun guessToolbarColor(url: String, capture: Bitmap) = initToolBarColor(Color.WHITE)
    override fun tabSwitch(tab: WebViewController) = initToolBarColor(Color.WHITE)

}