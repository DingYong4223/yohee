package com.fula.base.iview

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.*
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import androidx.appcompat.app.ActionBar
import com.fula.yohee.R
import com.fula.yohee.preference.UserPreferences
import com.fula.yohee.ui.activity.BasePageActivity
import com.fula.yohee.ui.activity.PageActivity
import org.greenrobot.eventbus.EventBus

/**
 * @Desc: the base view for other view. extend this class, you can specify the header for your view.
 * @Date: 2018-09-10
 * @author: delanding
 */
abstract class BasePage : IMvpView {

    lateinit var mView: View
    lateinit var mContext: BasePageActivity<*>
    protected lateinit var intent: Intent
    private var container: ViewGroup? = null
    val title: String by lazy { intent.getStringExtra(BasePage.KEY_TITLE) }
    protected val actionBar: ActionBar by lazy { requireNotNull(mContext.supportActionBar) }

    protected val layoutInflater: LayoutInflater
        get() = mContext.layoutInflater

    protected fun initToolBar(@StringRes titleRes: Int) {
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_array_left)
        actionBar.title = mContext.getString(titleRes)
    }

    protected fun initToolBar(title: String) {
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_array_left)
        actionBar.title = title
    }

    override fun attachToView(activity: Activity) {
        this.mContext = activity as BasePageActivity<*>
        intent = mContext.intent
    }
    override fun getView(): View = mView
    fun getUserPrefer(): UserPreferences = mContext.userPrefer
    /**
     * no header initialize function
     */
    @CallSuper
    open fun initPage(container: ViewGroup, resId: Int) {
        this.container = container
        mView = LayoutInflater.from(mContext).inflate(resId, container, false)
    }

    protected fun findViewById(resId: Int): View? = mView.findViewById(resId)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) = Unit
    override fun setContentView(layoutResID: Int) {
        mView = LayoutInflater.from(mContext).inflate(layoutResID, container, false)
        mContext.setContentView(mView)
    }

    override fun onCreate() = Unit
    override fun onResume() = Unit
    override fun onDestroy() = unregisterEventBus()
    override fun onBackPressed(): Boolean = false
    override fun onCreateOptionsMenu(menu: Menu?): Boolean = false
    override fun onOptionsItemSelected(item: MenuItem): Boolean = false
    fun finish() = mContext.finish()

    override fun onMenuBack(): Boolean {
        finish()
        return true
    }

    companion object {
        private const val KEY_TITLE = "KEY_TITLE"
        const val KEY_ARGS = "KEY_ARGS"
        fun genTitleIntent(context: Context, cls: Class<*>, title: String): Intent = PageActivity.genIntent(context, cls).apply { putExtra(KEY_TITLE, title) }
        fun genTitleIntent(context: Context, cls: Class<*>, @StringRes titleRes: Int): Intent = PageActivity.genIntent(context, cls).apply { putExtra(KEY_TITLE, context.getString(titleRes)) }
    }

    protected fun registerEventBus() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    private fun unregisterEventBus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    protected fun getString(@StringRes id: Int): String = mContext.getString(id)

}
