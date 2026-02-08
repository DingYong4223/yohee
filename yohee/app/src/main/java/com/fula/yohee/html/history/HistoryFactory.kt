package com.fula.yohee.html.history

import android.app.Application
import com.fula.yohee.R
import com.fula.yohee.constant.FILE
import com.fula.yohee.database.HistoryDatabase
import com.fula.yohee.html.HtmlFactory
import com.fula.yohee.html.ListPageReader
import com.fula.yohee.html.jsoup.*
import io.reactivex.Completable
import io.reactivex.Single
import java.io.File
import java.io.FileWriter
import javax.inject.Inject

/**
 * Factory for the history page.
 */
class HistoryFactory @Inject constructor(
    private val listPageReader: ListPageReader,
    private val application: Application,
    private val historyModel: HistoryDatabase
) : HtmlFactory {

    private val title = application.getString(R.string.action_history)

    override fun buildPage(): Single<String> = historyModel
        .lastHundredVisitedHistoryEntries()
        .map { list ->
            parse(listPageReader.provideHtml()) andBuild {
                title { title }
                body {
                    val repeatedElement = id("repeated").removeElement()
                    id("content") {
                        list.forEach {
                            appendChild(repeatedElement.clone {
                                tag("a") { attr("href", it.url) }
                                id("title") { text(it.title) }
                                id("url") { text(it.url) }
                            })
                        }
                    }
                }
            }
        }
        .map { content -> Pair(getFile(), content) }
        .doOnSuccess { (page, content) ->
            FileWriter(page, false).use { it.write(content) }
        }
        .map { (page, _) -> "$FILE$page" }

    /**
     * Use this observable to immediately delete the history page. This will clear the cached
     * history page that was stored on file.
     *
     * @return a completable that deletes the history page when subscribed to.
     */
    fun deleteHistoryPage(): Completable = Completable.fromAction {
        with(getFile()) {
            if (exists()) {
                delete()
            }
        }
    }

    override fun getFile() = File(application.filesDir, FILENAME)

    companion object {
        const val FILENAME = "history.html"
    }

}
