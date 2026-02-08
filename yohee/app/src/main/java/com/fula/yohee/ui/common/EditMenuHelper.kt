package com.fula.yohee.ui.common

import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import java.io.Serializable

class EditMenuHelper(val menuLayout: View, val listener: View.OnClickListener, private val editListener: ((Boolean) -> Unit)?) {

    private val menuItems: MutableMap<Int, EditMenuItem> = HashMap()

    init {
        resetState()
    }

    fun initMenuItem(@IdRes idRes: Int, l: View.OnClickListener, sRes: IntArray, @ColorInt color: Int): EditMenuItem  {
        var textView: TextView = menuLayout.findViewById(idRes)
        textView.apply {
            setText(sRes[0])
            setOnClickListener(l)
            setTextColor(color)
            menuItems[idRes] = EditMenuItem(this, sRes)
        }
        return menuItems[idRes]!!
    }

    var isEdit: Boolean = false
        private set

    fun resetState(data: List<CheckModel>? = null) {
        isEdit = false
        menuLayout.visibility = View.GONE
        for (item in menuItems.values) {
            item.textView.setText(item.idRes[0])
        }
        editListener?.invoke(isEdit)
        data?.forEach { it.isCheck = false }
    }

    fun swichEdit() {
        isEdit = !isEdit
        if (isEdit) {
            menuLayout.visibility = View.VISIBLE
        } else {
            menuLayout.visibility = View.GONE
        }
        editListener?.invoke(isEdit)
    }

    fun swichMenuItem(@IdRes id: Int) {
        val item = menuItems[id]
        item?.let {
            it.check = !it.check
            it.textView.setText(if (it.check) it.idRes[1] else it.idRes[0])
        }
    }

    fun isCheck(@IdRes id: Int): Boolean {
        menuItems[id]?.let {
            return it.check
        }
        return false
    }

    fun getItem(@IdRes id: Int): EditMenuItem? = menuItems[id]

    class EditMenuItem(var textView: TextView, val idRes: IntArray, var check: Boolean = false)

    abstract class CheckModel(var isCheck: Boolean = false): Serializable

}