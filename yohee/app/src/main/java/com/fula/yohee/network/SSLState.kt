package com.fula.yohee.network

import android.net.http.SslError
import com.fula.yohee.R
import java.util.*

/**
 * Representing the SSL state of the browser.
 */
class SSLState(val state: Int, val url: String? = null, val sslError: SslError? = null) {

    fun getAllSslErrorMessageCodes(): List<Int> {
        val errorCodeMessageCodes = ArrayList<Int>(1)
        sslError?.let {
            if (sslError.hasError(SslError.SSL_DATE_INVALID)) {
                errorCodeMessageCodes.add(R.string.message_certificate_date_invalid)
            }
            if (sslError.hasError(SslError.SSL_EXPIRED)) {
                errorCodeMessageCodes.add(R.string.message_certificate_expired)
            }
            if (sslError.hasError(SslError.SSL_IDMISMATCH)) {
                errorCodeMessageCodes.add(R.string.message_certificate_domain_mismatch)
            }
            if (sslError.hasError(SslError.SSL_NOTYETVALID)) {
                errorCodeMessageCodes.add(R.string.message_certificate_not_yet_valid)
            }
            if (sslError.hasError(SslError.SSL_UNTRUSTED)) {
                errorCodeMessageCodes.add(R.string.message_certificate_untrusted)
            }
            if (sslError.hasError(SslError.SSL_INVALID)) {
                errorCodeMessageCodes.add(R.string.message_certificate_invalid)
            }
        }
        return errorCodeMessageCodes
    }

    companion object {
        const val STATE_NONE = -1
        const val STATE_INVALIDE = 1
        const val STATE_VALIDE = 0
    }

}