package com.fula.yohee.ui.page

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import com.fula.base.iview.BasePage
import com.fula.yohee.R
import com.fula.util.ViewUnit
import kotlinx.android.synthetic.main.toolbar.view.*
import java.io.Serializable


/**
 * @Desc: edit the mark in start page.
 * @Date: 2018-09-11
 * @author: delanding
 */
class PageItemEdit : BasePage() {

    companion object {

        private const val ACTIVITY_RESULT_BASE = 1000
        const val ACTIVITY_RESULT_EDITBOOKMARK    = ACTIVITY_RESULT_BASE + 1
        const val ACTIVITY_RESULT_DOWNLOAD        = ACTIVITY_RESULT_BASE + 2
        const val ACTIVITY_RESULT_DOWNLOAD_RENAME = ACTIVITY_RESULT_BASE + 3
        const val ACTIVITY_RESULT_SHARE           = ACTIVITY_RESULT_BASE + 4

        const val KEY_MAP = "KEY_MAP"
        const val KEY_OBJ = "KEY_OBJ"
        fun genIntent(context: Context, map: HashMap<String, String>, @StringRes titleRes: Int = R.string.action_edit): Intent {
            val intent = BasePage.genTitleIntent(context, PageItemEdit::class.java, titleRes)
            intent.putExtra(KEY_MAP, map)
            return intent
        }

        fun genIntent(context: Context, map: HashMap<String, String>, obj: Serializable): Intent {
            return genIntent(context, map).apply {
                putExtra(KEY_OBJ, obj)
            }
        }

    }

    private val map: HashMap<String, String> by lazy { intent.getSerializableExtra(KEY_MAP) as HashMap<String, String> }
    private val mapView: MutableMap<String, EditText> = mutableMapOf()

    override fun initPage(inflater: LayoutInflater, container: ViewGroup) {
        super.initPage(container, R.layout.page_item_edit)
        initView()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        mContext.menuInflater.inflate(R.menu.menu_android_edit, menu)
        val item = menu!!.getItem(0)
        item?.let {
            item.setTitle(R.string.action_complete)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.toolbar_menu0 -> {
                complete()
            }
        }
        return true
    }

    private fun initView() {
        mContext.setSupportActionBar(mView.toolSetingbar as Toolbar)
        initToolBar(title)

        val rootContainer = mView as LinearLayout
        for (item in map.keys) {
            val edit = EditText(mContext).apply {
                hint = item
                setText(map[item])
                setSingleLine(true)
                maxLines = 1
            }
            mapView[item] = edit
            rootContainer.addView(edit, LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = ViewUnit.dp2px(5f)
            })
        }
    }

    private fun complete() {
        var edit = false
        for (item in map.keys) {
            val editText = mapView[item]?.text.toString()
            if (editText != map[item]) {
                edit = true
                map[item] = editText
            }
        }
        if (edit) mContext.setResult(Activity.RESULT_OK, intent.apply {
                putExtra(KEY_MAP, map)
            })
        finish()
    }

}

