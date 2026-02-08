package com.fula.yohee.search

/**
 * The suggestion choices.
 *
 * Created by anthonycr on 2/19/18.
 */
enum class Suggestions(val index: Int) {
    GOOGLE(1),
    DUCK(2),
    BAIDU(3),
    NAVER(4);

    companion object {
        fun from(value: Int): Suggestions {
            return when (value) {
                1 -> GOOGLE
                2 -> DUCK
                3 -> BAIDU
                4 -> NAVER
                else -> GOOGLE
            }
        }
    }
}