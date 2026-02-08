package com.fula.yohee.uiwigit.floatmenu

import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import com.fula.frame.FulaBaseActivity
import com.fula.frame.R
import com.fula.util.ViewUnit
import com.fula.yohee.constant.Setting
import com.fula.yohee.utils.DeviceUtils

class FloatMenu(private val context: FulaBaseActivity, private val view: View) : PopupWindow(context) {

    private val DEFAULT_MENU_WIDTH: Int
    private val VERTICAL_OFFSET: Int
    private val menuItemList: MutableList<FloatMenuItem> = mutableListOf()
    private val deletList: MutableList<Int> = mutableListOf()
    private var selects: List<Int>? = null
    private val screenPoint: Point
    private var clickX: Int = 0
    private var clickY: Int = 0
    private var menuWidth: Int = 0
    private var menuHeight: Int = 0
    private var menuLayout: LinearLayout? = null
    private var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onClick(v: View, item: FloatMenuItem, select: Boolean?)
    }

    constructor(activity: FulaBaseActivity) : this(activity, activity.findViewById<View>(android.R.id.content))

    init {
        isOutsideTouchable = true
        isFocusable = true
        setBackgroundDrawable(BitmapDrawable())
        view.setOnTouchListener(MenuTouchListener())
        VERTICAL_OFFSET = ViewUnit.dp2px(10f)
        DEFAULT_MENU_WIDTH = ViewUnit.dp2px(180f)
        screenPoint = DeviceUtils.getScreenDim(context)
    }

    fun addDelete(@IdRes delete: Int) {
        deletList.add(delete)
    }

    fun showMenuLayout(menuRes: Int, selects: List<Int>?, l: OnItemClickListener) {
        this.selects = selects
        inflate(menuRes, DEFAULT_MENU_WIDTH)
        setOnItemClickListener(l)
        show()
    }

    fun showMenuLayout(v: View, menuRes: Int, selects: List<Int>?, l: OnItemClickListener) {
        this.selects = selects
        inflate(menuRes, DEFAULT_MENU_WIDTH)
        setOnItemClickListener(l)
        show(v)
    }

    private fun inflate(menuRes: Int, itemWidth: Int) {
        FloatMenuParser(context, menuItemList, deletList).parseMenu(menuRes)
        generateLayout(itemWidth)
    }

    fun showItems(items: Array<String>, l: OnItemClickListener) {
        menuItemList.clear()
        for (i in items.indices) {
            val menuModel = FloatMenuItem()
            menuModel.item = items[i]
            menuItemList.add(menuModel)
        }
        generateLayout(DEFAULT_MENU_WIDTH)
        setOnItemClickListener(l)
        show()
    }

    fun <T : FloatMenuItem> items(itemList: List<T>) {
        menuItemList.clear()
        menuItemList.addAll(itemList)
        generateLayout(DEFAULT_MENU_WIDTH)
    }

    fun <T : FloatMenuItem> items(itemList: List<T>, itemWidth: Int) {
        menuItemList.clear()
        menuItemList.addAll(itemList)
        generateLayout(itemWidth)
    }

    private fun generateLayout(itemWidth: Int) {
        menuLayout = LinearLayout(context)
//        menuLayout!!.background = ContextCompat.getDrawable(context, R.drawable.bg_shadow)
        menuLayout!!.orientation = LinearLayout.VERTICAL
        val padding = ViewUnit.dp2px(12f)
        val selDrawable = ContextCompat.getDrawable(context, R.drawable.ic_right)!!.apply {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        }

        for (i in menuItemList.indices) {
            val textView = TextView(context)
            textView.isClickable = true
            textView.background = ContextCompat.getDrawable(context, R.drawable.selector_item)
            textView.setPadding(padding, padding, padding, padding)
            textView.width = itemWidth
            textView.gravity = Gravity.CENTER_VERTICAL or Gravity.START
            textView.textSize = 15f
            textView.setTextColor(Color.BLACK)
            val menuItem = menuItemList[i]
            val drawableLeft = if (menuItem.itemResId != View.NO_ID) ContextCompat.getDrawable(context, menuItem.itemResId)!!.apply { setBounds(0, 0, intrinsicWidth, intrinsicHeight) } else null
            val drawableRight = if (null != selects && selects!!.contains(menuItem.id)) selDrawable else null

            textView.compoundDrawablePadding = ViewUnit.dp2px(12f)
            textView.setCompoundDrawables(drawableLeft, null, drawableRight, null)
            textView.text = menuItem.item

            if (onItemClickListener != null) {
                textView.setOnClickListener(ItemOnClickListener(i))
            }
            menuLayout!!.addView(textView)
        }
        val width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        menuLayout!!.measure(width, height)
        menuWidth = menuLayout!!.measuredWidth
        menuHeight = menuLayout!!.measuredHeight
        contentView = menuLayout
        setWidth(menuWidth)
        setHeight(menuHeight)
    }

    private fun readGroup(attrs: AttributeSet) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.MenuItem)
        a.recycle()
    }

    private fun show(v: View? = null) {
        if (isShowing) return
        clickX = context.touchPoint.x
        clickY = context.touchPoint.y
        if (clickX <= screenPoint.x / 2) {
            if (clickY + menuHeight < screenPoint.y) {
                animationStyle = R.style.Animation_top_left
                if (null != v) {
                    val location = IntArray(2)
                    v.getLocationOnScreen(location)
                    showAtLocation(view, ANCHORED_GRAVITY, location[0] + v.width / 2, location[1] + v.height / 2)
                } else {
                    showAtLocation(view, ANCHORED_GRAVITY, clickX, clickY + VERTICAL_OFFSET)
                }
            } else {
                animationStyle = R.style.Animation_bottom_left
                showAtLocation(view, ANCHORED_GRAVITY, clickX, clickY - menuHeight - VERTICAL_OFFSET)
            }
        } else {
            if (clickY + menuHeight < screenPoint.y) {
                animationStyle = R.style.Animation_top_right
                if (null != v) {
                    val location = IntArray(2)
                    v.getLocationOnScreen(location)
                    showAtLocation(view, ANCHORED_GRAVITY, location[0] + v.width / 2 - menuWidth, location[1] + v.height / 2)
                } else {
                    showAtLocation(view, ANCHORED_GRAVITY, clickX - menuWidth, clickY + VERTICAL_OFFSET)
                }
            } else {
                animationStyle = R.style.Animation_bottom_right
                showAtLocation(view, ANCHORED_GRAVITY, clickX - menuWidth, clickY - menuHeight - VERTICAL_OFFSET)
            }
        }
        Setting.applyModeToView(context, contentView.parent as View)
    }

    private fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
        if (onItemClickListener != null) {
            for (i in 0 until menuLayout!!.childCount) {
                val view = menuLayout!!.getChildAt(i)
                view.setOnClickListener(ItemOnClickListener(i))
            }
        }
    }

    internal inner class MenuTouchListener : View.OnTouchListener {

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_DOWN) {
                clickX = event.rawX.toInt()
                clickY = event.rawY.toInt()
            }
            return false
        }
    }

    internal inner class ItemOnClickListener(var position: Int) : View.OnClickListener {

        override fun onClick(v: View) {
            dismiss()
            if (onItemClickListener != null) {
                val item = menuItemList[position]
                onItemClickListener!!.onClick(v, item, selects?.contains(item.id))
            }
        }
    }

    companion object {
        private const val ANCHORED_GRAVITY = Gravity.TOP or Gravity.START
    }
}
