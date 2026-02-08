package com.fula.yohee.uiwigit.floatmenu

import android.content.Context
import android.content.res.XmlResourceParser
import android.util.AttributeSet
import android.util.Xml
import android.view.InflateException
import android.view.View
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class FloatMenuParser(private val context: Context
                      , private val menuItemList: MutableList<FloatMenuItem>
                      , private val deleteList: MutableList<Int>? = null) {

    fun parseMenu(menuRes: Int): MutableList<FloatMenuItem> {
        var parser: XmlResourceParser? = null
        try {
            parser = context.resources.getLayout(menuRes)
            val attrs = Xml.asAttributeSet(parser)
            parseMenu(parser!!, attrs)
        } catch (e: Exception) {
            throw InflateException("Error inflating menu XML", e)
        } finally {
            parser?.close()
        }
        return menuItemList
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun parseMenu(parser: XmlPullParser, attrs: AttributeSet) {
        var eventType = parser.eventType
        var tagName: String
        var lookingForEndOfUnknownTag = false
        var unknownTagName: String? = null
        do {
            if (eventType == XmlPullParser.START_TAG) {
                tagName = parser.name
                if (tagName == XML_MENU) {
                    eventType = parser.next()
                    break
                }
                throw RuntimeException("Expecting menu, got $tagName")
            }
            eventType = parser.next()
        } while (eventType != XmlPullParser.END_DOCUMENT)
        var reachedEndOfMenu = false
        while (!reachedEndOfMenu) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
//                    if (lookingForEndOfUnknownTag) {
//                        break
//                    }
                    tagName = parser.name
                    if (tagName == XML_GROUP) {
                        parser.next()
                    } else if (tagName == XML_ITEM) {
                        readItem(attrs)
                    } else if (tagName == XML_MENU) {
                        parser.next()
                    } else {
                        lookingForEndOfUnknownTag = true
                        unknownTagName = tagName
                    }
                }

                XmlPullParser.END_TAG -> {
                    tagName = parser.name
                    if (lookingForEndOfUnknownTag && tagName == unknownTagName) {
                        lookingForEndOfUnknownTag = false
                        unknownTagName = null
                    } else if (tagName == XML_GROUP) {

                    } else if (tagName == XML_ITEM) {

                    } else if (tagName == XML_MENU) {
                        reachedEndOfMenu = true
                    }
                }
                XmlPullParser.END_DOCUMENT -> throw RuntimeException("Unexpected end of document")
            }
            eventType = parser.next()
        }
    }

    private fun readItem(attrs: AttributeSet) {
        val id = attrs.getAttributeResourceValue(XMLNS, "id", View.NO_ID)
        if (null != deleteList && deleteList.contains(id)) {
            return
        }

        val title = attrs.getAttributeResourceValue(XMLNS, "title", View.NO_ID)
        val icon = attrs.getAttributeResourceValue(XMLNS, "icon", View.NO_ID)
        val menu = FloatMenuItem()
        menu.id = id
        if (View.NO_ID != title) menu.item = context.resources.getString(title)
        if (icon != View.NO_ID) menu.itemResId = icon
        menuItemList.add(menu)
    }

    companion object {
        private const val XMLNS = "http://schemas.android.com/apk/res/android"
        /**
         * Menu tag name in XML.
         */
        private const val XML_MENU = "menu"
        /**
         * Group tag name in XML.
         */
        private const val XML_GROUP = "group"
        /**
         * Item tag name in XML.
         */
        private const val XML_ITEM = "item"
    }

}
