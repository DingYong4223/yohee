package com.fula.yohee

import android.content.Intent
import android.os.Build
import android.view.Menu
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import com.fula.CLog
import com.fula.yohee.ui.activity.WebActivity
import com.fula.yohee.utils.UrlUtils
import io.reactivex.Completable

class MainActivity : WebActivity() {

    @Suppress("DEPRECATION")
    public override fun updateCookiePreference(): Completable = Completable.fromAction {
        val cookieManager = CookieManager.getInstance()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(this@MainActivity)
        }
        cookieManager.setAcceptCookie(userPrefer.cookiesEnabled)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_android_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) : Boolean {
        CLog.i("onPrepareOptionsMenu...")
        if (searchView.hasFocus() || UrlUtils.getType(showingTab().url) == UrlUtils.URL_TYPE_HOME) {
            menu.findItem(R.id.url_search).isVisible = true
            menu.findItem(R.id.url_action_more).isVisible = false
        } else {
            menu.findItem(R.id.url_search).isVisible = false
            menu.findItem(R.id.url_action_more).isVisible = true
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onNewIntent(intent: Intent) =
        if (intent.action == INTENT_PANIC_TRIGGER) {
            panicClean()
        } else {
            handleNewIntent(intent)
            super.onNewIntent(intent)
        }

//    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
//        if (event.action == KeyEvent.ACTION_DOWN && event.isCtrlPressed) {
//            when (event.keyCode) {
//                KeyEvent.KEYCODE_P ->
//                    // Open a new private window
//                    if (event.isShiftPressed) {
//                        startActivity(IncognitoActivity.createIntent(this))
//                        overridePendingTransition(R.anim.slide_up_in, R.anim.fade_out_scale)
//                        return true
//                    }
//            }
//        }
//        return super.dispatchKeyEvent(event)
//    }

}
