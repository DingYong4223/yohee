package com.fula.yohee.adblock

object NoneAdBlocker : AdBlockListener {
    override fun isAd(url: String): String? = null
}
