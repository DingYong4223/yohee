package com.fula.yohee.database

import android.app.Application
import android.content.ContentValues
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.fula.CLog
import com.fula.yohee.extensions.firstOrNullMap
import com.fula.yohee.utils.UrlUtils
import io.reactivex.Scheduler
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 网页主题颜色监测白名单，处于白名单中的颜色监测是正确的
 */
@Singleton
class WebColorDatabase @Inject constructor(application: Application)
    : SQLiteOpenHelper(application, DATABASE_NAME, null, DATABASE_VERSION) {

    private val database: SQLiteDatabase by databaseDelegate()

    // Creating Tables
    override fun onCreate(db: SQLiteDatabase) {
        val webcolorSql = "CREATE TABLE ${DatabaseUtils.sqlEscapeString(TABLE_WEB_COLOR)}(" +
                "${DatabaseUtils.sqlEscapeString(KEY_ID)} INTEGER PRIMARY KEY," +
                "${DatabaseUtils.sqlEscapeString(KEY_HOST)} TEXT," +
                "${DatabaseUtils.sqlEscapeString(KEY_COLOR)} INTEGER," +
                "${DatabaseUtils.sqlEscapeString(KEY_TYPE)} INTEGER" +
                ')'
        CLog.i("webcolorSql = $webcolorSql")
        db.execSQL(webcolorSql)
    }

    // Upgrading database
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${DatabaseUtils.sqlEscapeString(TABLE_WEB_COLOR)}")
        onCreate(db)
    }

    private fun queryHostSync(host: String): Cursor {
        val alternateUrl = UrlUtils.alternateSlashUrl(host)
        return database.query(
                TABLE_WEB_COLOR,
                null,
                "$KEY_HOST=? OR $KEY_HOST=?",
                arrayOf(host, alternateUrl),
                null,
                null,
                null,
                "1"
        )
    }

    fun insertData(list: List<WebColor>): Single<Unit> = Single.fromCallable {
        for (item in list) {
            val id = database.insert(TABLE_WEB_COLOR, null, item.bindItemToValues())
            CLog.i("insert webcolor: ${item.host}, id = $id")
        }
    }

    fun backinsertOrUpdateItem(entry: WebColor, scheduler: Scheduler) = insertOrUpdateItem(entry).subscribeOn(scheduler).subscribe()

    fun insertOrUpdateItem(entry: WebColor): Single<Boolean> = Single.fromCallable {
        CLog.i("insert entry: $entry")
        queryHostSync(entry.host).use {
            if (!it.moveToFirst()) {
                val id = database.insert(TABLE_WEB_COLOR, null, entry.bindItemToValues())
                CLog.i("insert webcolor: entry")
                return@fromCallable id != -1L
            }
            var updateValue = ContentValues(3).apply {
                put(KEY_HOST, entry.host)
                put(KEY_COLOR, entry.color)
                put(KEY_TYPE, entry.type)
            }
            var updatedRows = database.update(TABLE_WEB_COLOR, updateValue, "$KEY_HOST=?", arrayOf(entry.host))
            CLog.i("update item: $updatedRows")
            return@fromCallable true
        }
    }

    fun queryItem(host: String?): Single<WebColor> = Single.fromCallable {
        CLog.i("namehost = $host")
        if (null == host) {
            return@fromCallable null
        } else {
            return@fromCallable queryHostSync(host).firstOrNullMap { it.bindToItem() }
        }
    }

    private fun WebColor.bindItemToValues() = ContentValues(4).apply {
        put(KEY_HOST, host)
        put(KEY_COLOR, color)
        put(KEY_TYPE, type)
    }

    private fun Cursor.bindToItem() = WebColor (
            getString(getColumnIndex(KEY_HOST)),
            getInt(getColumnIndex(KEY_COLOR)),
            getInt(getColumnIndex(KEY_TYPE))
    )

    fun count(): Long = DatabaseUtils.queryNumEntries(database, TABLE_WEB_COLOR)

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "webcolorModel"
        private const val TABLE_WEB_COLOR = "webcolor"
        private const val KEY_ID = "id"
        private const val KEY_HOST = "host"
        private const val KEY_COLOR = "color" //主题颜色
        private const val KEY_TYPE = "type" //0：系统监测， 1：人工标记
    }

}
