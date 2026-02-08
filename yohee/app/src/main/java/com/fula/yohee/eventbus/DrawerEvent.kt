package com.fula.yohee.eventbus

class DrawerEvent constructor(val type: Int, val index: Int) {

    companion object {
        const val DRAWER_OPENED = 1 //open the drawer
        const val DRAWER_CLOSED = 2 //close the drawer

        const val HISTORY_DATA_REMOVED = 3 //remove one item from database
        const val BOOKMARK_DATA_CHANGED = 4 //data changed

        const val BOOKMARK_BACH_EDIT = 5
    }
}
