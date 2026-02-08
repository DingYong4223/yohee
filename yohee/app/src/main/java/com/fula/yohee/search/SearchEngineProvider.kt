package com.fula.yohee.search

import android.app.Application
import com.fula.yohee.R
import com.fula.yohee.preference.UserPreferences
import com.fula.yohee.search.engine.*
import com.fula.yohee.search.engine.BaseSearchEngine.Companion.ENGINE_BAIDU
import com.fula.yohee.search.engine.BaseSearchEngine.Companion.ENGINE_GOOGLE
import com.fula.yohee.search.engine.BaseSearchEngine.Companion.ENGINE_SOGOU
import com.fula.yohee.search.engine.BaseSearchEngine.Companion.ENGINE_YAHOO
import com.fula.yohee.search.suggestions.BaiduSuggestionsModel
import com.fula.yohee.search.suggestions.GoogleSuggestionsModel
import com.fula.yohee.search.suggestions.RequestFactory
import com.fula.yohee.search.suggestions.SuggestionsRepository
import okhttp3.OkHttpClient
import javax.inject.Inject

/**
 * The model that provides the search engine based
 * on the user's preference.
 */
class SearchEngineProvider @Inject constructor(
    private val userPreferences: UserPreferences,
    private val httpClient: OkHttpClient,
    private val requestFactory: RequestFactory,
    private val application: Application) {

    /**
     * Provide the [SuggestionsRepository] that maps to the user's current preference.
     */
    fun provideSearchSuggestions(): SuggestionsRepository =
        when (userPreferences.engine) {
            ENGINE_BAIDU,ENGINE_SOGOU -> BaiduSuggestionsModel(httpClient, requestFactory, application)
            else -> GoogleSuggestionsModel(httpClient, requestFactory, application)
        }

    /**
     * Provide the [BaseSearchEngine] that maps to the user's current preference.
     */
    fun provideSearchEngine(): BaseSearchEngine =
        when (userPreferences.engine) {
            ENGINE_GOOGLE -> GoogleSearch()
            ENGINE_BAIDU -> BaiduSearch()
            ENGINE_YAHOO -> YahooSearch()
            ENGINE_SOGOU -> SogouSearch()
            else -> GoogleSearch()
        }

    fun getEngineName(): String = when (userPreferences.engine) {
        ENGINE_GOOGLE -> application.getString(R.string.search_engine_google)
        ENGINE_BAIDU -> application.getString(R.string.search_engine_baidu)
        ENGINE_YAHOO -> application.getString(R.string.search_engine_yahoo)
        ENGINE_SOGOU -> application.getString(R.string.search_engine_sogou)
        else -> application.getString(R.string.search_engine_google)
    }

    /**
     * Provide a list of all supported search menu_engines.
     */
    fun provideEngines(): List<BaseSearchEngine> = listOf(
        GoogleSearch(),
        BaiduSearch(),
        YahooSearch(),
        SogouSearch()
    )

}
