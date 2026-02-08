/*
 * Copyright 2014 A.C.R. Development
 */
package com.fula.yohee.download


import android.util.Patterns.GOOD_IRI_CHAR
import java.util.*
import java.util.regex.Pattern

/**
 * Web Address Parser
 *
 *
 * This is called WebAddress, rather than URL or URI, because it attempts to
 * parse the stuff that a user will actually type into a browser address widget.
 *
 *
 * Unlike java.net.uri, this parser will not choke on URIs missing schemes. It
 * will only throw a ParseException if the input is really hosed.
 *
 *
 * If given an https scheme but no port, fills in port
 */
internal class WebAddress
/**
 * Parses given URI-like string.
 */
@Throws(IllegalArgumentException::class)
constructor(address: String?) {

    var scheme: String? = null
    var host: String? = null
    var port: Int = 0
    var path: String? = null
    var authInfo: String? = null

    init {

        if (address == null) {
            throw IllegalArgumentException("address can't be null")
        }
        scheme = ""
        host = ""
        port = -1
        path = "/"
        authInfo = ""

        val m = sAddressPattern.matcher(address)
        var t: String?
        if (!m.matches()) {
            throw IllegalArgumentException("Parsing of address '$address' failed")
        }

        t = m.group(MATCH_GROUP_SCHEME)
        if (t != null) {
            scheme = t.toLowerCase(Locale.ROOT)
        }
        t = m.group(MATCH_GROUP_AUTHORITY)
        if (t != null) {
            authInfo = t
        }
        t = m.group(MATCH_GROUP_HOST)
        if (t != null) {
            host = t
        }
        t = m.group(MATCH_GROUP_PORT)
        if (t != null && !t.isEmpty()) {
            // The ':' character is not returned by the regex.
            try {
                port = Integer.parseInt(t)
            } catch (ex: NumberFormatException) {
                throw RuntimeException("Parsing of port number failed", ex)
            }

        }
        t = m.group(MATCH_GROUP_PATH)
        if (t != null && !t.isEmpty()) {
            /*
             * handle busted myspace frontpage redirect with missing initial "/"
             */
            path = if (t[0] == '/') {
                t
            } else {
                "/$t"
            }
        }

        /*
         * Get port from scheme or scheme from port, if necessary and possible
         */
        if (port == 443 && scheme != null && scheme!!.isEmpty()) {
            scheme = "https"
        } else if (port == -1) {
            port = if ("https" == scheme) {
                443
            } else {
                80 // default
            }
        }
        if (scheme != null && scheme!!.isEmpty()) {
            scheme = "http"
        }
    }

    override fun toString(): String {

        var port = ""
        if (this.port != 443 && "https" == scheme || this.port != 80 && "http" == scheme) {
            port = ':' + Integer.toString(this.port)
        }
        var authInfo = ""
        if (!this.authInfo!!.isEmpty()) {
            authInfo = this.authInfo!! + '@'
        }

        return "$scheme://$authInfo$host$port$path"
    }

    companion object {
        private const val MATCH_GROUP_SCHEME = 1
        private const val MATCH_GROUP_AUTHORITY = 2
        private const val MATCH_GROUP_HOST = 3
        private const val MATCH_GROUP_PORT = 4
        private const val MATCH_GROUP_PATH = 5
        private val sAddressPattern = Pattern.compile(
                /* scheme */"(?:(http|https|file)://)?" +
                /* authority */"(?:([-A-Za-z0-9$\\_.+!*'(),;?&=]+(?::[-A-Za-z0-9$\\_.+!*'(),;?&=]+)?)@)?" +
                /* host */"([" + GOOD_IRI_CHAR + "%_-][" + GOOD_IRI_CHAR + "%_\\.-]*|\\[[0-9a-fA-F:\\.]+\\])?" +
                /* port */"(?::([0-9]*))?" +
                /* url */"(/?[^#]*)?" +
                /* anchor */".*", Pattern.CASE_INSENSITIVE)
    }
}
