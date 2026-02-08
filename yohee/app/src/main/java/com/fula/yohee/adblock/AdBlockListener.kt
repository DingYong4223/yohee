package com.fula.yohee.adblock

interface AdBlockListener {
    fun isAd(url: String): String?
}