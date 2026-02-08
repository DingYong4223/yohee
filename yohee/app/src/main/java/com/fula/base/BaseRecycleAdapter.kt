package com.fula.base

import android.content.Context
import android.view.LayoutInflater
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

/**
 * base adapter for recycleView
 * Created by delanding on 2018-12-05
 */
abstract class BaseRecycleAdapter<T, VH : RecyclerView.ViewHolder>(protected var mContext: Context) : RecyclerView.Adapter<VH>() {

    var dataListener: (data: List<T>) -> Unit = {}

    val inflater: LayoutInflater = LayoutInflater.from(mContext)

    var data: MutableList<T> = ArrayList()

    fun itemAt(position: Int): T = data[position]

    fun updateData(initList: List<T>) {
        val oldList = data
        data = initList.toMutableList()
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = oldList.size
            override fun getNewListSize() = data.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                    oldList[oldItemPosition] == data[newItemPosition]

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                    oldList[oldItemPosition] == data[newItemPosition]
        })
        diffResult.dispatchUpdatesTo(this)
        dataListener(initList)
    }

    fun initData(initList: List<T>) {
        data.clear()
        data.addAll(initList)
        notifyDataSetChanged()
        dataListener(initList)
    }

    override fun getItemCount(): Int = data.size

}
