package com.fula.yohee.settings

class UserSetting {
    companion object {
        const val NO_VALUE = -1

        /**theme*/
        const val THEME_SPRING = 0
        const val THEME_SUMMER = 1
        const val THEME_AUTUMUN = 2
        const val THEME_WINTER = 3
        const val THEME_AUTOCHANGE = 4

        /**dynamic backgroud*/
        const val DYNAMICBG_STORM = 0

        /**user habit*/
        const val USER_HABIT_HAND_LEFT = 0
        const val USER_HABIT_HAND_RIGHT = 1

        const val BLOCK_NONE = 0
        const val BLOCK_WAP = 1
        const val BLOCK_ALL = 2

        const val ANIM_MARK_NONE = 0
        const val ANIM_MARK_TRANS = 1
        const val ANIM_MARK_FADE = 2

        //float dot show rule
        const val DOT_SHOW_ALWAYS = 0  //一直展示
        const val DOT_SHOW_ONLY_FSCREEN = 2 //全屏模式展示
    }

}