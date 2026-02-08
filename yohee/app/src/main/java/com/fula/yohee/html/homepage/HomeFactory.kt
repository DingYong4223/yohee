package com.fula.yohee.html.homepage

import android.app.Application
import com.fula.CLog
import com.fula.yohee.R
import com.fula.yohee.animation.AnimationUtils
import com.fula.yohee.constant.UTF8
import com.fula.yohee.database.BookmarkDatabase
import com.fula.yohee.html.HtmlFactory
import com.fula.yohee.html.jsoup.*
import com.fula.yohee.preference.UserPreferences
import com.fula.yohee.search.SearchEngineProvider
import io.reactivex.Single
import java.io.File
import java.io.FileWriter
import javax.inject.Inject

/**
 * A factory for the home page.
 */
class HomeFactory @Inject constructor(
        private val app: Application,
        private val userPrefer: UserPreferences,
        private val searchEngineProvider: SearchEngineProvider,
        private val bookmarkModel: BookmarkDatabase,
        private val homePageReader: HomePageReader) : HtmlFactory {

    override fun buildPage(): Single<String> = Single.fromCallable {
        bookmarkModel.updateMarks()
        searchEngineProvider.provideSearchEngine().let {
            CLog.i("start to build engine page...")
            val content = parse(homePageReader.provideHtml()) andBuild {
                title { app.getString(R.string.page_home) }
                charset { UTF8 }
                head {
                    tag("style") { html(html().replace("\${MARK_ANIM}", AnimationUtils.genMarkAnim(userPrefer)))}
                }
                body {
                    id("image_url") { attr("src", it.iconUrl) }
                    id("search_submit") { attr("value", app.resources.getString(R.string.search_hint)) }
                    id("mark_tip") { text(app.resources.getString(R.string.common_mark)) }
                    tag("script") { html(html().replace("\${BASE_URL}", it.queryUrl).replace("&", "\\u0026")) }
                }
            }
            var page = getFile()
            FileWriter(page, false).use { it.write(content) }
            getUrl()
        }

    }

//    override fun buildPage(): Single<String> = bookmarkModel.getSortItemsByType(Bookmark.TYPE_MARK)
//            .subscribeOn(dbScheduler)
//            .observeOn(diskScheduler)
//            .map {
//                buildEnginePage(it)
//            }
//
//    private fun buildEnginePage(list: List<Bookmark>): String =
//            searchEngineProvider.provideSearchEngine().let {
//                CLog.i("start to build engine page...")
//                val content = parse(homePageReader.provideHtml()) andBuild {
//                    title { app.getString(R.string.page_home) }
//                    charset { UTF8 }
//                    body {
//                        CLog.i("body build...")
//                        id("image_url") { attr("src", it.iconUrl) }
//                        id("search_submit") { attr("value", app.resources.getString(R.string.search_hint)) }
//                        id("mark_tip") { text(app.resources.getString(R.string.common_mark)) }
//                        id("flex-holder") {
//                            for (item in list) {
//                                appendChild(Element("a").text(item.title).attr("href", item.url))
//                            }
//                        }
//                        tag("script") { html(html().replace("\${BASE_URL}", it.queryUrl).replace("&", "\\u0026")) }
//                    }
//                }
//                var page = getFile()
//                FileWriter(page, false).use { it.write(content) }
//                getUrl()
//            }


    /**
     * Create the home page file.
     */
    override fun getFile() = File(app.filesDir, FILENAME)

    companion object {
        const val FILENAME = "homepage.html"
    }

}
