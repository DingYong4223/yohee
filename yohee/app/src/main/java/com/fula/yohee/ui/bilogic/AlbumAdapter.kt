package com.fula.yohee.ui.bilogic

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fula.yohee.R
import com.fula.yohee.eventbus.SEvent
import com.fula.yohee.extensions.inflater
import com.fula.CLog
import com.fula.yohee.ui.TabsManager
import com.fula.yohee.ui.WebViewController
import com.fula.yohee.view.SwipeToDismissListener
import kotlinx.android.synthetic.main.album.view.*
import org.greenrobot.eventbus.EventBus

class AlbumAdapter(val mContext: Context, private val tabsManager: TabsManager)
    : RecyclerView.Adapter<AlbumAdapter.ViewHolder>() {

    override fun getItemCount(): Int = tabsManager.tabList.size


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumAdapter.ViewHolder = ViewHolder(mContext.inflater.inflate(R.layout.album, parent, false))
    override fun onBindViewHolder(holder: AlbumAdapter.ViewHolder, index: Int) = holder.onBind(tabsManager.tabList[index], index)

    inner class ViewHolder(val cv: View) : RecyclerView.ViewHolder(cv) {
        init {
            cv.setOnTouchListener(SwipeToDismissListener(cv, null,
                    object : SwipeToDismissListener.DismissCallback {
                        override fun canDismiss(token: Any?): Boolean {
                            return true
                        }

                        override fun onDismiss(view: View, token: Any?) {
                            CLog.i("view = $view token = $token")
                            EventBus.getDefault().post(SEvent(SEvent.TYPE_ITEM_PAGEREMOVE).apply {
                                obj = tabsManager.tabList[adapterPosition]
                            })
                        }
                    }
            ))
            cv.setOnClickListener {
                EventBus.getDefault().post(SEvent(SEvent.TYPE_ITEM_SHORTCLICK).apply {
                    obj = tabsManager.tabList[adapterPosition]
                })
            }

            cv.setOnLongClickListener {
                CLog.i("long click...")
                EventBus.getDefault().post(SEvent(SEvent.TYPE_ITEM_LONGCLICK).apply {
                    obj = tabsManager.tabList[adapterPosition]
                })
                return@setOnLongClickListener true
            }
        }

        fun onBind(item: WebViewController, index: Int) {
            CLog.i("index = $index, bitmap = ${item.captureImage}")
            cv.album_title.text = item.title
            if (item.captureImage?.isRecycled == false) {
                cv.album_cover.setImageBitmap(item.captureImage)
            } else {
                cv.album_cover.setImageBitmap(null)
            }
//            item.captureImage?.apply {
//                if (!isRecycled) cv.album_cover.setImageBitmap(this)
//            }
            if (tabsManager.showingTab == item) {
                cv.setBackgroundResource(R.drawable.album_shape_blue)
            } else {
                cv.setBackgroundColor(R.drawable.album_shape_dark)
            }
        }

    }

}
