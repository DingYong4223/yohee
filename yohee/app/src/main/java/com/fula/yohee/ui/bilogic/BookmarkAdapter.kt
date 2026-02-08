package com.fula.yohee.ui.bilogic

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import com.fula.yohee.R
import com.fula.yohee.favicon.FaviconModel
import com.fula.yohee.utils.DrawableUtils
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.com_list_item.view.*
import java.util.concurrent.ConcurrentHashMap

class BookmarkAdapter(mContext: Context,
                      private val faviconModel: FaviconModel,
                      private val networkScheduler: Scheduler,
                      onItemLongClickListener: (Int) -> Boolean,
                      onItemClickListener: (View, Int) -> Unit) : SelectAdapter(mContext, onItemLongClickListener, onItemClickListener) {

    private val faviconFetchers = ConcurrentHashMap<String, Disposable>()
    private val webPageBitmap: Bitmap = DrawableUtils.getThemedBitmap(mContext, R.drawable.ic_webpage)

    fun cleanupSubscriptions() {
        for (subscription in faviconFetchers.values) {
            subscription.dispose()
        }
        faviconFetchers.clear()
    }

    override fun iconLazyLoad(cv: View, model: SelectModel) {
        cv.item_logo_img.visibility = View.VISIBLE
        val bitmap = model.icon ?: webPageBitmap.also {
            cv.item_logo_img.tag = model.urlTxt.hashCode()
            val url = model.urlTxt
            faviconFetchers[url]?.dispose()
            faviconFetchers[url] = faviconModel.faviconForUrl(url, model.title)
                    .subscribeOn(networkScheduler)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                            onSuccess = { bitmap ->
                                model.icon = bitmap
                                if (cv.item_logo_img.tag == url.hashCode()) {
                                    cv.item_logo_img.setImageBitmap(bitmap)
                                }
                            }
                    )
        }
        cv.item_logo_img.setImageBitmap(bitmap)
    }
}