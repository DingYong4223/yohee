package com.fula.yohee.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.webkit.WebView
import com.fula.yohee.constant.INTENT_ORIGIN
import com.fula.CLog
import java.net.URISyntaxException
import java.util.regex.Pattern

class IntentUtils(private val mActivity: Activity) {

    fun startActivityForUrl(tab: WebView?, url: String): Boolean {
        var intent: Intent
        try {
            intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
        } catch (ex: URISyntaxException) {
            CLog.i("Bad URI " + url + ": " + ex.message)
            return false
        }
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        intent.component = null
        intent.selector = null
        if (mActivity.packageManager.resolveActivity(intent, 0) == null) {
            val packagename = intent.getPackage()
            return if (packagename != null) {
                intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:$packagename"))
                intent.addCategory(Intent.CATEGORY_BROWSABLE)
                mActivity.startActivity(intent)
                true
            } else {
                false
            }
        }
        if (tab != null) {
            intent.putExtra(INTENT_ORIGIN, tab.hashCode())
        }
        val m = ACCEPTED_URI_SCHEMA.matcher(url)
        if (m.matches() && !isSpecializedHandlerAvailable(intent)) {
            return false
        }
        try {
            if (mActivity.startActivityIfNeeded(intent, -1)) {
                return true
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return false
    }

    /**
     * Search for intent handlers that are specific to this URL aka, specialized
     * apps like google maps or youtube
     */
    private fun isSpecializedHandlerAvailable(intent: Intent): Boolean {
        val pm = mActivity.packageManager
        val handlers = pm.queryIntentActivities(intent,
                PackageManager.GET_RESOLVED_FILTER)
        if (handlers == null || handlers.isEmpty()) {
            return false
        }
        for (resolveInfo in handlers) {
            val filter = resolveInfo.filter
                    ?: // No intent filter matches this intent?
                    // Error on the side of staying in the browser, ignore
                    continue
// NOTICE: Use of && instead of || will cause the browser
            // to launch a new intent for every URL, using OR only
            // launches a new one if there is a non-browser app that
            // can handle it.
            // Previously we checked the number of data paths, but it is unnecessary
            // filter.countDataAuthorities() == 0 || filter.countDataPaths() == 0
            if (filter.countDataAuthorities() == 0) {
                // Generic handler, skip
                continue
            }
            return true
        }
        return false
    }

    companion object {

        fun isValidIntent(context: Context, intent: Intent): Boolean {
            val packageManager = context.packageManager
            val activities = packageManager.queryIntentActivities(intent, 0)
            return !activities.isEmpty()
        }

        private val ACCEPTED_URI_SCHEMA = Pattern.compile("(?i)"
                + // switch on case insensitive matching

                '('.toString()
                + // begin group for schema

                "(?:http|https|file)://" + "|(?:inline|data|about|javascript):" + "|(?:.*:.*@)"
                + ')'.toString() + "(.*)")
    }
}
