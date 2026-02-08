package com.fula.yohee.database

import android.app.Application
import android.content.ContentValues
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.fula.CLog
import com.fula.yohee.extensions.firstOrNullMap
import com.fula.yohee.extensions.useMap
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The disk backed download database. See for function documentation.
 */
@Singleton
class DownloadsDatabase @Inject constructor(application: Application)
    : SQLiteOpenHelper(application, DATABASE_NAME, null, DATABASE_VERSION) {

    private val database: SQLiteDatabase by databaseDelegate()

    // Creating Tables
    override fun onCreate(db: SQLiteDatabase) {
        val createsql = "CREATE TABLE ${DatabaseUtils.sqlEscapeString(TABLE_DOWNLOADS)}(" +
                "${DatabaseUtils.sqlEscapeString(KEY_ID)} INTEGER PRIMARY KEY," +
                "${DatabaseUtils.sqlEscapeString(KEY_URL)} TEXT," +
                "${DatabaseUtils.sqlEscapeString(KEY_TITLE)} TEXT," +
                "${DatabaseUtils.sqlEscapeString(KEY_SIZE)} BIGINT," +
                "${DatabaseUtils.sqlEscapeString(KEY_DOWNED)} BIGINT," +
                "${DatabaseUtils.sqlEscapeString(KEY_STATUS)} INTEGER," +
                "${DatabaseUtils.sqlEscapeString(KEY_TYPE)} INTEGER" +
                ')'
        CLog.i("create sql = $createsql")
        db.execSQL(createsql)
    }

    // Upgrading database
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop older table if it exists
        db.execSQL("DROP TABLE IF EXISTS ${DatabaseUtils.sqlEscapeString(TABLE_DOWNLOADS)}")
        // Create tables again
        onCreate(db)
    }

    fun query(url: String): Maybe<DownloadEntry> = Maybe.fromCallable {
        database.query(
                TABLE_DOWNLOADS,
                null,
                "$KEY_URL=?",
                arrayOf(url),
                null,
                null,
                "1"
        ).use { cur ->
            cur.firstOrNullMap { it.bindToDownloadItem() }
        }
    }

    fun filter(urls: List<String>, block: (Cursor) -> Boolean): Maybe<List<String>> = Maybe.fromCallable {
        val filter = mutableListOf<String>()
        urls.forEach {
            database.query(
                    TABLE_DOWNLOADS,
                    null,
                    "$KEY_URL=?",
                    arrayOf(it),
                    null,
                    null,
                    "1"
            ).use { cur ->
                if (block(cur)) filter.add(it)
            }
        }
        return@fromCallable filter
    }

    fun isDownload(url: String): Single<Boolean> = Single.fromCallable {
        database.query(
                TABLE_DOWNLOADS,
                null,
                "$KEY_URL=?",
                arrayOf(url),
                null,
                null,
                null,
                "1"
        ).use {
            return@fromCallable it.moveToFirst()
        }
    }

    private fun queryUrlSync(url: String): Cursor {
        return database.query(
                TABLE_DOWNLOADS,
                null,
                "$KEY_URL=?",
                arrayOf(url),
                null,
                null,
                null,
                "1"
        )
    }

    fun insertItem(entry: DownloadEntry): Single<Boolean> = Single.fromCallable {
        CLog.i("insert entry: $entry")
        database.query(
                TABLE_DOWNLOADS,
                null,
                "$KEY_URL=?",
                arrayOf(entry.url),
                null,
                null,
                "1"
        ).use {
            if (it.moveToFirst()) {
                return@fromCallable false
            }
        }
        val id = database.insert(TABLE_DOWNLOADS, null, entry.toContentValues())
        return@fromCallable id != -1L
    }

    fun updateItemSize(url: String, size: String): Single<Boolean> = Single.fromCallable {
        CLog.i("update entry size: $size")
        queryUrlSync(url).use {
            if (!it.moveToFirst()) {
                return@fromCallable false
            }
            var updateValue = ContentValues(1).apply {
                put(KEY_SIZE, size)
            }
            var updatedRows = database.update(TABLE_DOWNLOADS, updateValue, "$KEY_URL=?", arrayOf(url))
            CLog.i("update item: $updatedRows")
            return@fromCallable true
        }
    }

    fun updateItemProgress(url: String, downed: Long, length: Long): Single<Boolean> = Single.fromCallable {
        CLog.i("update entry downed: $downed")
        queryUrlSync(url).use {
            if (!it.moveToFirst()) {
                return@fromCallable false
            }
            var updateValue = ContentValues(1).apply {
                put(KEY_DOWNED, downed)
                put(KEY_SIZE, length)
            }
            var updatedRows = database.update(TABLE_DOWNLOADS, updateValue, "$KEY_URL=?", arrayOf(url))
            CLog.i("update item: $updatedRows")
            return@fromCallable true
        }
    }

    fun updateItemStatus(url: String, status: Int): Single<Boolean> = Single.fromCallable {
        CLog.i("update entry status: $status")
        queryUrlSync(url).use {
            if (!it.moveToFirst()) {
                return@fromCallable false
            }
            var updateValue = ContentValues(1).apply {
                put(KEY_STATUS, status)
            }
            var updatedRows = database.update(TABLE_DOWNLOADS, updateValue, "$KEY_URL=?", arrayOf(url))
            CLog.i("update item: $updatedRows")
            return@fromCallable true
        }
    }

    fun update(url: String, map: Map<String, String>): Maybe<Boolean> = Maybe.fromCallable {
        CLog.i("update entry map: $map")
        queryUrlSync(url).use {
            if (!it.moveToFirst()) {
                return@fromCallable false
            }
            var updateValue = ContentValues(1).apply {
                map.keys.forEach { key ->
                    this.put(key, map[key])
                }
            }
            var updatedRows = database.update(TABLE_DOWNLOADS, updateValue, "$KEY_URL=?", arrayOf(url))
            CLog.i("update item: $updatedRows")
            return@fromCallable true
        }
    }

    fun addDownloadsList(downloadEntries: List<DownloadEntry>): Completable = Completable.fromAction {
        database.apply {
            beginTransaction()
            setTransactionSuccessful()

            for (item in downloadEntries) {
                insertItem(item).subscribe()
            }
            endTransaction()
        }
    }

    fun deleteDownload(lists: List<DownloadEntry>): Single<Unit> = Single.fromCallable {
        lists.forEach {
            database.delete(TABLE_DOWNLOADS, "$KEY_URL=?", arrayOf(it.url))
        }
    }

    fun clearDownload(): Completable = Completable.fromAction {
        database.run {
            delete(TABLE_DOWNLOADS, null, null)
            close()
        }
    }

    fun getDownloads(type: Int = DownloadEntry.TYPE_NOMAL_FILE): Single<List<DownloadEntry>> = Single.fromCallable {
        return@fromCallable database.query(
                TABLE_DOWNLOADS,
                null,
                "$KEY_TYPE=?",
                arrayOf("$type"),
                null,
                null,
                "$KEY_ID DESC"
        ).use { cur ->
            cur.useMap { it.bindToDownloadItem() }
        }
    }

    fun getAllDownloads(): Single<List<DownloadEntry>> = Single.fromCallable {
        return@fromCallable database.query(
                TABLE_DOWNLOADS,
                null,
                null,
                null,
                null,
                null,
                "$KEY_ID DESC"
        ).use { cur ->
            cur.useMap { it.bindToDownloadItem() }
        }
    }

    fun count(): Long = DatabaseUtils.queryNumEntries(database, TABLE_DOWNLOADS)

    /**
     * Maps the fields of [DownloadEntry] to [ContentValues].
     */
    private fun DownloadEntry.toContentValues() = ContentValues(6).apply {
        put(KEY_TITLE, title)
        put(KEY_URL, url)
        put(KEY_SIZE, length)
        put(KEY_DOWNED, downed)
        put(KEY_STATUS, status)
        put(KEY_TYPE, type)
    }

    /**
     * Binds a [Cursor] to a single [DownloadEntry].
     */
    private fun Cursor.bindToDownloadItem() = DownloadEntry(
            url = getString(getColumnIndex(KEY_URL)),
            title = getString(getColumnIndex(KEY_TITLE)),
            length = getLong(getColumnIndex(KEY_SIZE)),
            downed = getLong(getColumnIndex(KEY_DOWNED)),
            status = getInt(getColumnIndex(KEY_STATUS)),
            type = getInt(getColumnIndex(KEY_TYPE))
    )

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "yoheeDownload"
        private const val TABLE_DOWNLOADS = "download"

        private const val KEY_ID = "id"
        const val KEY_URL = "url"
        const val KEY_TITLE = "title"
        const val KEY_DOWNED = "downed"
        const val KEY_STATUS = "status"
        const val KEY_SIZE = "size"
        const val KEY_TYPE = "type" //类型：0普通下载文件，1视频下载文件

    }

}
