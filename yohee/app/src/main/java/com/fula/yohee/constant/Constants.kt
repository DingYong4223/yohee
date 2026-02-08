/*
 * Copyright 2014 A.C.R. Development
 */
@file:JvmName("Constants")

package com.fula.yohee.constant

// Hardcoded user agents
const val DESKTOP_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36"
const val MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; U; Android 4.4; en-us; Nexus 4 Build/JOP24G) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30"

// URL Schemes
const val HTTP = "http://"
const val HTTPS = "https://"
const val FILE = "file://"
const val ABOUT = "about:"
const val FOLDER = "folder://"

// Custom local page schemes
const val SCHEME_HOMEPAGE = "${ABOUT}home"
const val SCHEME_BLANK = "${ABOUT}blank"
const val SCHEME_BOOKMARKS = "${ABOUT}bookmarks"
const val SCHEME_URL = "${ABOUT}url"

const val LOAD_READING_URL = "ReadingUrl"

const val NO_PROXY = 0
const val PROXY_ORBOT = 1
const val PROXY_I2P = 2
const val PROXY_MANUAL = 3
const val DEF_PROXY_PORT = 8118
const val DEF_MEDIA_CACHE_DAY = 7

const val UTF8 = "UTF-8"

// Allowable text encodings for the WebView
@JvmField
val TEXT_ENCODINGS = arrayOf("ISO-8859-1", UTF8, "GBK", "Big5", "ISO-2022-JP", "SHIFT_JS", "EUC-JP", "EUC-KR")

const val INTENT_ORIGIN = "URL_INTENT_ORIGIN"

