package com.fula.yohee.utils

import android.Manifest

class YoheePermission{

    companion object {
        val STORAGE_READ_WRITE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

}