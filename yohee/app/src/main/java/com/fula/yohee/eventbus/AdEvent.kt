package com.fula.yohee.eventbus

class AdEvent constructor(val type: Int, val stringArg: String?) {

    companion object {
        const val TYPE_ADD_AD = 0
        const val TYPE_ADD_DELETE = 1
    }

}
