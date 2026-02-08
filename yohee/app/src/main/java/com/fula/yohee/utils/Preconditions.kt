package com.fula.yohee.utils

object Preconditions {

    /**
     * Ensure that an object is not null
     * and throw a RuntimeException if it
     * is null.
     *
     * @param object check nullness on this object.
     */
    fun checkNonNull(`object`: Any?) {
        if (`object` == null) {
            throw RuntimeException("Object must not be null")
        }
    }
}
