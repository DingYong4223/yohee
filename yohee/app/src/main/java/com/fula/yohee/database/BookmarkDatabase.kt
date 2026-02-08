package com.fula.yohee.database

import android.app.Application
import android.content.ContentValues
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.fula.CLog
import com.fula.yohee.BuildConfig
import com.fula.yohee.R
import com.fula.yohee.extensions.firstOrNullMap
import com.fula.yohee.extensions.useMap
import com.fula.yohee.utils.UrlUtils
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The disk backed bookmark database. See [BookmarkDatabase] for function documentation.
 */
@Singleton
class BookmarkDatabase @Inject constructor(application: Application) : SQLiteOpenHelper(application, DATABASE_NAME, null, DATABASE_VERSION) {

    private val defaultBookmarkTitle: String = application.getString(R.string.untitled)
    private val database: SQLiteDatabase by databaseDelegate()
    var marks: List<Bookmark>? = null
        private set

    // Creating Tables
    override fun onCreate(db: SQLiteDatabase) {
        val bookMarkSql = "CREATE TABLE ${DatabaseUtils.sqlEscapeString(TABLE_BOOKMARK)}(" +
                "${DatabaseUtils.sqlEscapeString(KEY_ID)} INTEGER PRIMARY KEY," +
                "${DatabaseUtils.sqlEscapeString(KEY_URL)} TEXT," +
                "${DatabaseUtils.sqlEscapeString(KEY_TITLE)} TEXT," +
                "${DatabaseUtils.sqlEscapeString(KEY_FOLDER)} TEXT," +
                "${DatabaseUtils.sqlEscapeString(KEY_POSITION)} INTEGER," +
                "${DatabaseUtils.sqlEscapeString(KEY_TYPE)} INTEGER" +
                ')'
        CLog.i("bookMarkSql = $bookMarkSql")
        db.execSQL(bookMarkSql)
    }

    // Upgrading database
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop older table if it exists
        db.execSQL("DROP TABLE IF EXISTS ${DatabaseUtils.sqlEscapeString(TABLE_BOOKMARK)}")
        // Create tables again
        onCreate(db)
    }

    fun updateMarks(): List<Bookmark> = database.query(
            TABLE_BOOKMARK,
            null,
            "$KEY_TYPE=? or $KEY_TYPE=?",
            arrayOf("${Bookmark.TYPE_MARK}", "${Bookmark.TYPE_BOOKMARK}"),
            null,
            null,
            "$KEY_POSITION ASC, $KEY_ID DESC").use { cur ->
        cur.useMap { it.bindEntry() }
    }.apply { marks = this }

    private fun queryCursor(url: String): Cursor {
        val alternateUrl = UrlUtils.alternateSlashUrl(url)
        return database.query(
                TABLE_BOOKMARK,
                null,
                "$KEY_URL=? OR $KEY_URL=?",
                arrayOf(url, alternateUrl),
                null,
                null,
                null,
                "1"
        )
    }

    /**
     * Deletes a bookmark from the database with the provided URL. If it
     * cannot find any bookmark with the given URL, it will try to delete
     * a bookmark with the [.alternateSlashUrl] as its URL.
     *
     * @param url the URL to delete.
     * @return the number of deleted rows.
     */
    private fun deleteSync(url: String): Int {
        return database.delete(
                TABLE_BOOKMARK,
                "$KEY_URL=? OR $KEY_URL=?",
                arrayOf(url, UrlUtils.alternateSlashUrl(url))
        )
    }

    private fun updateSync(url: String, contentValues: ContentValues): Int {
        var updatedRows = database.update(
                TABLE_BOOKMARK,
                contentValues,
                "$KEY_URL=?",
                arrayOf(url)
        )
        if (updatedRows == 0) {
            val alternateUrl = UrlUtils.alternateSlashUrl(url)
            updatedRows = database.update(
                    TABLE_BOOKMARK,
                    contentValues,
                    "$KEY_URL=?",
                    arrayOf(alternateUrl)
            )
        }
        return updatedRows
    }

    fun findItemForUrl(url: String): Maybe<Bookmark> = Maybe.fromCallable {
        return@fromCallable queryCursor(url).firstOrNullMap { it.bindEntry() }
    }

    fun isBookmark(url: String): Single<Int> = Single.fromCallable {
        queryCursor(url).use {
            if (!it.moveToFirst()) {
                return@fromCallable -1
            }
            return@fromCallable it.getInt(it.getColumnIndex(KEY_TYPE))
        }
    }

    fun insertItemIfNotExist(entry: Bookmark): Single<Boolean> = Single.fromCallable {
        queryCursor(entry.url).use {
            if (!it.moveToFirst()) {
                val id = database.insert(TABLE_BOOKMARK, null, entry.bindValues())
                return@fromCallable id != -1L
            }
            val type = it.getInt(it.getColumnIndex(KEY_TYPE))
            if (type == entry.type) {
                CLog.i("type bookmark exist: $type")
                return@fromCallable false
            }
            var updateValue = ContentValues(3)
            updateValue.put(KEY_TYPE, Bookmark.TYPE_BOOKMARK)
            updateValue.put(KEY_TITLE, entry.title)
            updateValue.put(KEY_FOLDER, entry.folder)
            var updatedRows = database.update(
                    TABLE_BOOKMARK,
                    updateValue,
                    "$KEY_URL=?",
                    arrayOf(entry.url)
            )
            CLog.i("update bookmark: $updatedRows")
            return@fromCallable true
        }
    }

    fun insertItems(bookmarkItems: List<Bookmark>): Completable = Completable.fromAction {
        database.apply {
            beginTransaction()
            for (item in bookmarkItems) {
                insertItemIfNotExist(item).subscribe()
            }
            setTransactionSuccessful()
            endTransaction()
        }
    }

    fun deleteItems(list: List<Bookmark>): Single<Unit> = Single.fromCallable {
        for (item in list) {
            deleteSync(item)
        }
    }

    fun deleteItem(entry: Bookmark): Single<Boolean> = Single.fromCallable {
        return@fromCallable deleteSync(entry)
    }

    private fun deleteSync(entry: Bookmark): Boolean = queryCursor(entry.url).use {
        if (!it.moveToFirst()) {
            return false
        }
        val type = it.getInt(it.getColumnIndex(KEY_TYPE))
        if (type == entry.type) {
            CLog.i("delete type bookmark exist: $type")
            return deleteSync(entry.url) > 0
        }
        if (type == Bookmark.TYPE_BOOKMARK) {
            var updateValue = ContentValues(1)
            updateValue.put(KEY_TYPE, if (entry.type == Bookmark.TYPE_BOOK) Bookmark.TYPE_MARK else Bookmark.TYPE_BOOK)
            var updateResult = database.update(
                    TABLE_BOOKMARK,
                    updateValue,
                    "$KEY_URL=?",
                    arrayOf(entry.url)
            )
            CLog.i("update bookmark: $updateResult")
            return updateResult > 0
        }
        return true
    }

    fun clearItems(): Completable = Completable.fromAction {
        database.run {
            delete(TABLE_BOOKMARK, null, null)
            close()
        }
    }

    fun editItem(oldBookmark: Bookmark, newBookmark: Bookmark): Completable = Completable.fromAction {
        val contentValues = newBookmark.bindValues()
        updateSync(oldBookmark.url, contentValues)
    }

    fun sort(array: List<Bookmark>): Completable = Completable.fromAction {
        for (index in array.indices) {
            val old = array[index]
            val new = old.copy(url = old.url, title = old.title, folder = old.folder, position = index, type = old.type)

            val contentValues = new.bindValues()
            updateSync(old.url, contentValues)
        }
    }

    fun getSortItems(): Single<List<Bookmark>> = Single.fromCallable {
        return@fromCallable database.query(
                TABLE_BOOKMARK,
                null,
                null,
                null,
                null,
                null,
                "$KEY_POSITION ASC, $KEY_ID DESC"
        ).use { cur ->
            cur.useMap { it.bindEntry() }
        }
    }

    fun getSortItemsByType(type: Int = Bookmark.TYPE_BOOK): Single<List<Bookmark>> = Single.fromCallable {
        return@fromCallable database.query(
                TABLE_BOOKMARK,
                null,
                "$KEY_TYPE=? or $KEY_TYPE=?",
                arrayOf("$type", "${Bookmark.TYPE_BOOKMARK}"),
                null,
                null,
                "$KEY_POSITION ASC, $KEY_ID DESC"
        ).use { cur ->
            cur.useMap { it.bindEntry() }
        }
    }

    fun count(): Long = DatabaseUtils.queryNumEntries(database, TABLE_BOOKMARK)

    /**
     * Binds a [Bookmark] to [ContentValues].
     * @return a valid values object that can be inserted into the database.
     */
    private fun Bookmark.bindValues() = ContentValues(6).apply {
        put(KEY_TITLE, title.takeIf(String::isNotBlank) ?: defaultBookmarkTitle)
        put(KEY_FOLDER, folder)
        put(KEY_URL, url)
        put(KEY_POSITION, position)
        put(KEY_TYPE, type)
    }

    private fun Cursor.bindEntry() = Bookmark(
            url = getString(getColumnIndex(KEY_URL)),
            title = getString(getColumnIndex(KEY_TITLE)),
            folder = getString(getColumnIndex(KEY_FOLDER)),
            position = getInt(getColumnIndex(KEY_POSITION)),
            type = getInt(getColumnIndex(KEY_TYPE))
    )

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "bookmarkModel"
        private const val TABLE_BOOKMARK = "bookmark"
        const val KEY_ID = "id"
        const val KEY_URL = "url"
        const val KEY_TITLE = "title"
        const val KEY_FOLDER = "folder" //folder name belong to.
        const val KEY_POSITION = "position"
        const val KEY_TYPE = "type"

        fun getProvideMarks() = mutableListOf<Bookmark>().apply {
            add(Bookmark("https://sina.cn/", "手机新浪网", "", 1, Bookmark.TYPE_MARK))
            add(Bookmark("https://m.sohu.com/", "手机搜狐网", "", 2, Bookmark.TYPE_MARK))
            add(Bookmark("https://nba.sina.cn/?from=wap", "新浪NBA", "", 3, Bookmark.TYPE_MARK))
            add(Bookmark("https://m.facebook.com/", "facebook", "", 4, Bookmark.TYPE_MARK))
            add(Bookmark("https://m.youtube.com/", "youtube", "", 5, Bookmark.TYPE_MARK))
            add(Bookmark("https://mobile.twitter.com/", "twitter", "", 6, Bookmark.TYPE_MARK))
            add(Bookmark("https://m.china.nba.com/", "NBA", "", 7, Bookmark.TYPE_MARK))
            if (BuildConfig.DEBUG) {
                add(Bookmark("http://android.myapp.com/", "应用宝", "", 8, Bookmark.TYPE_MARK))
            }
        }

    }

}
