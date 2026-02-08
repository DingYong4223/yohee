/*
 * Copyright 7/31/2016 Anthony Restaino
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fula.yohee.dialog

import android.app.Activity
import android.app.Dialog
import android.text.InputFilter
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fula.view.LengthFilter
import com.fula.yohee.Config
import com.fula.yohee.R
import com.fula.yohee.constant.Setting
import com.fula.yohee.constant.Setting.Companion.setDialogSize
import com.fula.yohee.extensions.inflater
import com.fula.yohee.list.RecyclerViewStringAdapter
import com.fula.yohee.ui.page.PageWeb


object DialogHelper {

    @JvmStatic
    fun showListDialog(activity: Activity, title: String?, vararg items: DialogItem) {
        val builder = AlertDialog.Builder(activity)
        val layout = activity.inflater.inflate(com.fula.yohee.R.layout.list_dialog, null)
        val titleView = layout.findViewById<TextView>(com.fula.yohee.R.id.dialog_title)
        val recyclerView = layout.findViewById<RecyclerView>(com.fula.yohee.R.id.dialog_list)
        val itemList = items.filter(DialogItem::isConditionMet)
        val adapter = RecyclerViewStringAdapter(itemList, convertToString = { activity.getString(this.title) })
        if (title?.isNotEmpty() == true) {
            titleView.text = title
        }
        recyclerView.apply {
            this.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            this.adapter = adapter
            setHasFixedSize(true)
        }
        builder.setView(layout)
        val dialog = builder.create()
        dialog.apply {
            Setting.applyModeToWindow(activity, window!!)
            setDialogSize(activity, window!!)
        }.show()
        adapter.onItemClickListener = { item ->
            item.onClick()
            dialog.dismiss()
        }
    }

    @JvmStatic
    fun showOkDialog(activity: Activity, @StringRes title: Int, msg: String, positiveButton: DialogItem) {
        val dialog = AlertDialog.Builder(activity).apply {
            setTitle(title)
            setMessage(msg)
            setOnCancelListener { }
            setPositiveButton(positiveButton.title) { _, _ -> positiveButton.onClick() }
        }.create()
        dialog.apply {
            Setting.applyModeToWindow(activity, dialog.window)
            setDialogSize(activity, window)
        }.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(positiveButton.colorTint)
    }

    @JvmStatic
    fun showOkCancelDialog(activity: Activity, @StringRes title: Int, @StringRes message: Int, args: Array<Any>? = null, positiveButton: DialogItem, negativeButton: DialogItem? = null) = showOkCancelDialog(activity, activity.getString(title), if (args != null) {
        activity.getString(message, *args)
    } else {
        activity.getString(message)
    }, positiveButton, negativeButton)

    @JvmStatic
    fun showOkCancelDialog(activity: Activity, @StringRes title: Int, @StringRes message: Int, positiveButton: DialogItem, negativeButton: DialogItem? = null) = showOkCancelDialog(activity, activity.getString(title), message, positiveButton, negativeButton)

    @JvmStatic
    fun showOkCancelDialog(activity: Activity, title: String, @StringRes message: Int, positiveButton: DialogItem, negativeButton: DialogItem? = null) = DialogHelper.showOkCancelDialog(activity, title, activity.getString(message), positiveButton, negativeButton)

    @JvmStatic
    fun showOkCancelDialog(activity: Activity, @StringRes title: Int, message: String, positiveButton: DialogItem, negativeButton: DialogItem? = null) = DialogHelper.showOkCancelDialog(activity, activity.getString(title), message, positiveButton, negativeButton)

    @JvmStatic
    fun showOkCancelDialog(activity: Activity, title: String, message: String, positiveButton: DialogItem, negativeButton: DialogItem? = null): Dialog {
        val cancelItem = negativeButton ?: DialogItem(title = com.fula.yohee.R.string.action_no) {}
        val dialog = AlertDialog.Builder(activity).apply {
            setTitle(title)
            setMessage(message)
            setOnCancelListener { }
            setPositiveButton(positiveButton.title) { _, _ -> positiveButton.onClick() }
            setNegativeButton(cancelItem.title) { _, _ -> cancelItem.onClick() }
        }.create()
        dialog.apply {
            Setting.applyModeToWindow(activity, dialog.window)
            setDialogSize(activity, window)
        }.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(positiveButton.colorTint)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(cancelItem.colorTint)
        return dialog
    }

    /**服务和隐私协议弹框*/
    @JvmStatic
    fun showPrivacyDialog(activity: Activity, positiveButton: DialogItem, negativeButton: DialogItem? = null): Dialog {
        val cancelItem = negativeButton ?: DialogItem(title = R.string.action_no) {}
        val dialog = AlertDialog.Builder(activity).apply {
            setCancelable(false)
            setView(activity.layoutInflater.inflate(R.layout.privacy_layout, null).apply {
                findViewById<TextView>(R.id.goto_service).setOnClickListener {
                    val intent = PageWeb.genIntent(activity, R.string.protol_serve, Config.PROTOL_WEB)
                    activity.startActivity(intent)
                }
                findViewById<TextView>(R.id.goto_secure).setOnClickListener {
                    val intent = PageWeb.genIntent(activity, R.string.privacy_secure, Config.SECURE_WEB)
                    activity.startActivity(intent)
                }
            })
            setTitle(R.string.privacy_title)
            setPositiveButton(positiveButton.title) { _, _ -> positiveButton.onClick() }
            setNegativeButton(cancelItem.title) { _, _ -> cancelItem.onClick() }
        }.create()
        dialog.apply {
            Setting.applyModeToWindow(activity, dialog.window)
            setDialogSize(activity, window)
        }.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(positiveButton.colorTint)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(cancelItem.colorTint)
        return dialog
    }

    @JvmStatic
    fun showEditDialog(
            activity: Activity,
            @StringRes title: Int,
            @StringRes hint: Int,
            @StringRes action: Int,
            maxLen: Int = 30,
            textInputListener: (String) -> Unit) = showEditDialog(activity, title, hint, null, action, maxLen, textInputListener)

    @JvmStatic
    fun showEditDialog(
            activity: Activity,
            @StringRes title: Int,
            @StringRes hint: Int,
            currentText: String?,
            @StringRes action: Int,
            maxLen: Int = 30,
            textInputListener: (String) -> Unit) {
        val dialogView = LayoutInflater.from(activity).inflate(com.fula.yohee.R.layout.dialog_edit_text, null)
        val editText = dialogView.findViewById<EditText>(com.fula.yohee.R.id.dialog_edit_text)
        editText.setHint(hint)
        editText.filters = arrayOf<InputFilter>(LengthFilter(maxLen))
        if (currentText != null) {
            editText.setText(currentText)
        }
        val builder = AlertDialog.Builder(activity, com.fula.yohee.R.style.dialogSoftInput)
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton(action) { _, _ -> textInputListener(editText.text.toString()) }

        val dialog = builder.create()
        dialog.apply {
            Setting.applyModeToWindow(activity, dialog.window!!)
            setDialogSize(activity, window)
        }.show()
    }

}
