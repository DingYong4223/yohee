package com.fula.base

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

/**
 * base adapter for recycleView
 * Created by delanding on 2018-12-05
 */
class BaseTouchHelper<T, VH : RecyclerView.ViewHolder>(val mAdapter: BaseRecycleAdapter<T, VH>) : ItemTouchHelper.Callback() {

    override fun isLongPressDragEnabled(): Boolean = true

    //线性布局和网格布局都可以使用
    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        var dragFrlg = 0
        if (recyclerView.layoutManager is LinearLayoutManager) {
            dragFrlg = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        }
        return makeMovementFlags(dragFrlg, 0)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        val from = viewHolder.adapterPosition
        //拿到当前拖拽到的item的viewHolder
        val to = target.adapterPosition
        if (from < to) {
            for (i in from until to) {
                Collections.swap(mAdapter.data, i, i + 1)
            }
        } else {
            for (i in from downTo to + 1) {
                Collections.swap(mAdapter.data, i, i - 1)
            }
        }
        mAdapter.notifyItemMoved(from, to)
        //mAdapter.notifyDataSetChanged()
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

//        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder, actionState: Int) {
//            super.onSelectedChanged(viewHolder, actionState)
//            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
//                viewHolder.itemView.setBackgroundColor(Color.RED)
//                //获取系统震动服务//震动70毫秒
////                val vib = getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
////                vib.vibrate(70)
//            }
//        }

    /**
     * 手指松开的时候还原高亮
     * @param recyclerView
     * @param viewHolder
     */
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        viewHolder.itemView.setBackgroundColor(0)
        mAdapter.notifyDataSetChanged()
    }
}
