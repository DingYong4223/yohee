package com.fula.base

import android.text.Editable
import android.text.TextWatcher

abstract class TextWatcherAdapter : TextWatcher {
    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
    override fun afterTextChanged(e: Editable) = Unit
}