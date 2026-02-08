package com.fula.base.iview

import android.app.Activity
import android.content.Intent
import android.view.*

interface IMvpView {
    fun onMenuBack(): Boolean
    fun initPage(inflater: LayoutInflater, container: ViewGroup)
    fun getView(): View
    fun attachToView(activity: Activity)
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    fun onCreate()
    fun onResume()
    fun onDestroy()
    fun onBackPressed(): Boolean
    fun setContentView(layoutResID: Int)
    fun onCreateOptionsMenu(menu: Menu?): Boolean
    fun onOptionsItemSelected(item: MenuItem): Boolean
}
