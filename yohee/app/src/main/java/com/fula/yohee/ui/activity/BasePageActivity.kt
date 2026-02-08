package com.fula.yohee.ui.activity

import android.os.Bundle
import android.view.ViewGroup
import com.fula.base.iview.IMvpView


abstract class BasePageActivity<V : IMvpView> : BaseActivity() {

    val mPage: V by lazy { getViewObj()!! }

    protected abstract fun getViewObj(): V?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPage.attachToView(this)
        mPage.initPage(layoutInflater, window.decorView as ViewGroup)
        super.setContentView(mPage.getView())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("SavedInstance", 1)
    }

}
