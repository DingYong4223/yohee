package com.fula.yohee.ui.bilogic

import android.text.Editable
import android.text.style.CharacterStyle
import android.text.style.ParagraphStyle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.fula.base.TextWatcherAdapter
import com.fula.yohee.ui.activity.WebActivity
import com.fula.yohee.utils.UrlUtils
import com.fula.yohee.view.SearchView

class SearchListener(private val context: WebActivity, private val searchView: SearchView) : TextWatcherAdapter(), View.OnKeyListener,
        TextView.OnEditorActionListener,
        View.OnFocusChangeListener,
        SearchView.FocusListener {

    override fun afterTextChanged(e: Editable) {
        e.getSpans(0, e.length, CharacterStyle::class.java).forEach(e::removeSpan)
        e.getSpans(0, e.length, ParagraphStyle::class.java).forEach(e::removeSpan)
    }

    override fun onKey(view: View, keyCode: Int, keyEvent: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_ENTER -> {
                context.searchAction(searchView)
                return true
            }
            else -> {
            }
        }
        return false
    }

    override fun onEditorAction(arg0: TextView, actionId: Int, arg2: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_GO
                || actionId == EditorInfo.IME_ACTION_DONE
                || actionId == EditorInfo.IME_ACTION_NEXT
                || actionId == EditorInfo.IME_ACTION_SEND
                || actionId == EditorInfo.IME_ACTION_SEARCH
                || arg2?.action == KeyEvent.KEYCODE_ENTER) {
            context.searchAction(searchView)
            return true
        }
        return false
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) = context.onSearchViewFocusChange(v, hasFocus)

    override fun onPreFocus() {
        val url = context.tabsManager.showingTab.url
        if (!UrlUtils.isGenUrl(url)) {
            if (!searchView.hasFocus()) {
                searchView.setText(url)
            }
        }
    }
}