package com.fula.yohee.dialog

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import com.fula.base.ViewHelper
import com.fula.yohee.Config
import com.fula.yohee.R
import com.fula.yohee.ui.activity.WebActivity
import com.fula.yohee.uiwigit.floatmenu.FloatMenuItem
import com.fula.yohee.uiwigit.floatmenu.FloatMenuParser
import com.fula.yohee.utils.DeviceUtils
import kotlinx.android.synthetic.main.dlg_main_menu.*

abstract class BaseSheetMenu(context: WebActivity
                             , protected val listener: MenuListener
                             , protected val resMap: Map<Int, Drawable>? = null)
    : BaseDialog(context, R.style.SheetDialogStyle) {

    protected lateinit var adapter: SimpleAdapter
    protected lateinit var items: List<Map<String, FloatMenuItem>>
    protected val disables: MutableList<Int> = mutableListOf()
    protected val hightLight: MutableList<Int> = mutableListOf()

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.dlg_main_menu)
        items = initData()
        initUIState()
        initView()
        updateUIState()
    }

    override fun layoutParams(window: Window): WindowManager.LayoutParams {
        var attr = window.attributes
        attr.gravity = Gravity.BOTTOM
        attr.width = DeviceUtils.getScreenWidth(context)
        return attr
    }

    private fun initData(): List<Map<String, FloatMenuItem>> {
        val menuItemList: MutableList<FloatMenuItem> = mutableListOf()
        FloatMenuParser(context, menuItemList, null).parseMenu(initMenuResId())
        return menuItemList.map {
            HashMap<String, FloatMenuItem>().apply { put(BaseSheetMenu.SHEET_ITEM_KEY, it) }
        }
    }

    abstract fun initMenuResId() : Int
    abstract fun initUIState()
    open fun updateUIState(){}

    private fun initView() {
        val from = arrayOf(SHEET_ITEM_KEY)
        val to = intArrayOf(R.id.item_text)
        adapter = GridAdapter(context, items, R.layout.dlg_menu_item, from, to)
        adapter.setViewBinder { view, data, _ ->
            if (view is TextView) {
                view.text = (data as FloatMenuItem).item
                ViewHelper.setDrawble(view, data.itemResId, ViewHelper.DRAWABLE_TOP)

                var drawable = resMap?.get(data.id)
                if (null != drawable) {
                    ViewHelper.setDrawble(view, drawable, ViewHelper.DRAWABLE_TOP)
                }
            }
            true
        }
        menu_list.adapter = adapter
        menu_list.setOnItemClickListener { _, _, position, _ ->
            val item = items[position][SHEET_ITEM_KEY]
            item?.let {
                listener.onCLick(it.id, hightLight.contains(item.id))
            }
            dismiss()
        }
        findViewById<View>(R.id.button_hiddon).apply {
            setOnClickListener { dismiss() }
        }
        findViewById<View>(R.id.button_appfinish).apply {
            setOnClickListener {
                listener.onCLick(R.id.button_appfinish, false)
                dismiss()
            }
        }
    }

    inner class GridAdapter(val context: Context, data: List<Map<String, *>>,
                            @LayoutRes resource: Int, from: Array<String>,
                            @IdRes to: IntArray) : SimpleAdapter(context, data, resource, from, to) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var v = super.getView(position, convertView, parent) as ViewGroup
            var colorValue = -1
            val item = items[position][SHEET_ITEM_KEY]
            item?.let {
                if (disables.contains(item.id)) {
                    colorValue = Config.COLOR_ITEM_UNSELECT
                    v.isClickable = true
                } else if (hightLight.contains(item.id)) {
                    colorValue = Config.COLOR_ITEM_SELECT
                }
            }
            if (-1 != colorValue) {
                ViewHelper.setTVDrawbleColor(v.getChildAt(0) as TextView, colorValue)
            }
            return v
        }
    }

    companion object {
        const val SHEET_ITEM_KEY = "itemText"
    }

}