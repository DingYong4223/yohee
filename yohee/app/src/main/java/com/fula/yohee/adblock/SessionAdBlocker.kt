package com.fula.yohee.adblock

import androidx.core.net.toUri
import com.fula.CLog
import com.fula.yohee.database.Ad
import com.fula.yohee.database.AdDatabase
import com.fula.yohee.di.DatabaseScheduler
import com.fula.yohee.eventbus.AdEvent
import io.reactivex.Completable
import io.reactivex.Scheduler
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject
import javax.inject.Singleton

/**
 * An in memory representation of the ad blocking whitelist. Can be queried synchronously.
 */
@Singleton
class SessionAdBlocker @Inject constructor(
        private val adDatabase: AdDatabase,
        @DatabaseScheduler private val dbScheduler: Scheduler): AdBlockListener {

    private lateinit var adSet: HashSet<String>

    init {
        adDatabase
            .getAllAds()
            .map { it.map(Ad::url).toHashSet() }
            .subscribeOn(dbScheduler)
            .subscribe ({ hashSet -> adSet = hashSet }){}
        EventBus.getDefault().register(this)
    }

    override fun isAd(url: String): String? {
        val host = url.toUri().host
        host?.let {
            if (adSet.contains(host)) {
                CLog.i("ad url session = $url")
                return host
            }
        }
        return null
    }

    private fun addUrlToDb(url: String) {
        CLog.i("add ad to db: $url")
        url.toUri().host?.let { host ->
            adDatabase.queryAdForUrl(host)
                .isEmpty
                .flatMapCompletable {
                    if (it) {
                        adDatabase.addAd(Ad(host, System.currentTimeMillis()))
                    } else {
                        Completable.complete()
                    }
                }
                .subscribeOn(dbScheduler)
                .subscribe { CLog.i("ad added to database: $url") }
            adSet.add(host)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: AdEvent) {
        CLog.i("onEvent = ${event.type}, intArg = ${event.stringArg}")
        when (event.type) {
            AdEvent.TYPE_ADD_AD -> {
                event.stringArg?.let {
                    addUrlToDb(it)
                }
            }
            AdEvent.TYPE_ADD_DELETE -> {
                adDatabase.getAllAds()
                        .map { it.map(Ad::url).toHashSet() }
                        .subscribeOn(dbScheduler)
                        .subscribe ({ hashSet -> adSet = hashSet }){}
            }
        }
    }

}
