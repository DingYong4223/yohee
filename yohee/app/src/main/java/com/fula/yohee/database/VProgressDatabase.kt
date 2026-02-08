package com.fula.yohee.database

import android.app.Application
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.annotation.WorkerThread
import com.fula.CLog
import com.fula.base.ToolUtils
import com.fula.yohee.extensions.firstOrNullMap
import io.reactivex.Completable
import io.reactivex.Maybe
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@WorkerThread
class VProgressDatabase @Inject constructor(
        application: Application) : SQLiteOpenHelper(application, DATABASE_NAME, null, DATABASE_VERSION) {

    private val database: SQLiteDatabase by databaseDelegate()

    // Creating Tables
    override fun onCreate(db: SQLiteDatabase) {
        val createAllowListTable = "CREATE TABLE $TABLE_PROGRESS(" +
                " $KEY_ID INTEGER PRIMARY KEY," +
                " $KEY_URL TEXT," +
                " $KEY_PROGRESS BIGINT" +
                ")"
        db.execSQL(createAllowListTable)
    }

    // Upgrading database
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROGRESS")
        onCreate(db)
    }

    private fun Cursor.bindToItem() = VideoProgress(
            url = getString(1),
            progress = getLong(2)
    )

    private fun queryUrlSync(url: String): Cursor {
        return database.query(
                TABLE_PROGRESS,
                null,
                "${DownloadsDatabase.KEY_URL}=?",
                arrayOf(url),
                null,
                null,
                null,
                "1"
        )
    }

    fun updateOrAdd(url: String, progress: Long): Completable = Completable.fromAction {
        val md5Url = ToolUtils.md5(url)
        queryUrlSync(md5Url).use {
            if (!it.moveToFirst()) {
                val values = ContentValues().apply {
                    put(KEY_URL, md5Url)
                    put(KEY_PROGRESS, progress)
                }
                return@use database.insert(TABLE_PROGRESS, null, values)
            }
            var updateValue = ContentValues(1).apply {
                this.put(KEY_PROGRESS, progress)
            }
            var updatedRows = database.update(TABLE_PROGRESS, updateValue, "$KEY_URL=?", arrayOf(md5Url))
            CLog.i("update item: $updatedRows")
        }
    }

    fun update(url: String, progress: Long): Maybe<Boolean> = Maybe.fromCallable {
        CLog.i("update entry progress: $progress")
        val md5Url = ToolUtils.md5(url)
        queryUrlSync(md5Url).use {
            if (!it.moveToFirst()) {
                return@fromCallable false
            }
            var updateValue = ContentValues(1).apply {
                this.put(KEY_PROGRESS, progress)
            }
            var updatedRows = database.update(TABLE_PROGRESS, updateValue, "$KEY_URL=?", arrayOf(md5Url))
            CLog.i("update item: $updatedRows")
            return@fromCallable true
        }
    }

    fun query(url: String): Maybe<VideoProgress> = Maybe.fromCallable {
        val md5Url = ToolUtils.md5(url)
        database.query(
                TABLE_PROGRESS,
                null,
                "$KEY_URL=?",
                arrayOf(md5Url),
                null,
                null,
                "1"
        ).use { cur ->
            cur.firstOrNullMap { it.bindToItem() }
        }
    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "videoProgress"
        private const val TABLE_PROGRESS = "VideoProgress"
        private const val KEY_ID = "id"
        private const val KEY_URL = "url"
        private const val KEY_PROGRESS = "progress" //当前播放的进度
    }
}
