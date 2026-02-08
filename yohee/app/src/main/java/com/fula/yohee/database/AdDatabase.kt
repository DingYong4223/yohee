package com.fula.yohee.database

import android.app.Application
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.annotation.WorkerThread
import com.fula.CLog
import com.fula.yohee.extensions.firstOrNullMap
import com.fula.yohee.extensions.useMap
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@WorkerThread
class AdDatabase @Inject constructor(
        application: Application) : SQLiteOpenHelper(application, DATABASE_NAME, null, DATABASE_VERSION) {

    private val database: SQLiteDatabase by databaseDelegate()

    // Creating Tables
    override fun onCreate(db: SQLiteDatabase) {
        val createAllowListTable = "CREATE TABLE $TABLE_AD(" +
                " $KEY_ID INTEGER PRIMARY KEY," +
                " $KEY_URL TEXT," +
                " $KEY_CREATED INTEGER" +
                ")"
        db.execSQL(createAllowListTable)
    }

    // Upgrading database
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_AD")
        onCreate(db)
    }

    private fun Cursor.bindToAdItem() = Ad(
            url = getString(1),
            timeCreated = getLong(2)
    )

    fun getAllAds(): Single<List<Ad>> = Single.fromCallable {
        database.query(
                TABLE_AD,
                null,
                null,
                null,
                null,
                null,
                "$KEY_CREATED DESC"
        ).use { cur ->
            cur.useMap { it.bindToAdItem() }
        }
    }

    fun queryAdForUrl(url: String): Maybe<Ad> = Maybe.fromCallable {
        database.query(
                TABLE_AD,
                null,
                "$KEY_URL=?",
                arrayOf(url), null,
                null,
                "$KEY_CREATED DESC",
                "1"
        ).use { cur ->
            cur.firstOrNullMap { it.bindToAdItem() }
        }
    }

    fun addAd(ad: Ad): Completable = Completable.fromAction {
        val values = ContentValues().apply {
            put(KEY_URL, ad.url)
            put(KEY_CREATED, ad.timeCreated)
        }
        database.insert(TABLE_AD, null, values)
    }

    fun deleteAd(ad: Ad): Single<Int> = Single.fromCallable {
        CLog.i("removeAt: ${ad.url}")
        database.delete(TABLE_AD, "$KEY_URL = ?", arrayOf(ad.url))
    }

    fun deleteAd(ads: List<Ad>): Completable = Completable.fromAction {
        for (ad in ads) {
            database.delete(TABLE_AD, "$KEY_URL = ?", arrayOf(ad.url))
        }
    }

    fun clearAd(): Completable = Completable.fromAction {
        database.run {
            delete(TABLE_AD, null, null)
            close()
        }
    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "allowListManager"
        private const val TABLE_AD = "allowList"
        private const val KEY_ID = "id"
        private const val KEY_URL = "url"
        private const val KEY_CREATED = "created"

    }
}
