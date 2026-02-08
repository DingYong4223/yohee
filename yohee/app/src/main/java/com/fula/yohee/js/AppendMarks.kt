package com.fula.yohee.js

import com.fula.yohee.YoheeApp
import com.fula.yohee.database.Bookmark
import com.fula.fano.file2string
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import javax.inject.Inject

/**
 * Force the text to reflow.
 */
@file2string("app/src/main/js/AppendMarks.js")
interface AppendMarks {
    fun provideJs(): String
}

class JSAppendMarks (private val marks: List<Bookmark>?) : JSlogicface {

    init {
        YoheeApp.injector.inject(this)
    }

    @Inject
    internal lateinit var appendMarks: AppendMarks

    override fun getJs(): String? {
        if (marks.isNullOrEmpty()) return null
        return String.format(appendMarks.provideJs(), JSONArray(marks.map {
            JSONObject("{title:'${URLEncoder.encode(it.title)}',url:'${URLEncoder.encode(it.url)}'}")
        }).toString())
    }

}

/*
(function() {
var holder = document.getElementById('flex-holder');
var alist = [{"title":"手机新浪网","url":"https:\/\/sina.cn"},{"title":"手机搜狐网","url":"https:\/\/m.sohu.com"},{"title":"新浪NBA","url":"https:\/\/nba.sina.cn\/?from=wap"},{"title":"facebook","url":"https:\/\/www.facebook.com"},{"title":"youtube","url":"https:\/\/www.youtube.com"},{"title":"twitter","url":"https:\/\/twitter.com"},{"title":"NBA","url":"https:\/\/china.nba.com"}];
var index = 0;
var timer = setInterval(function() {
if(index >= alist.length - 1) {
clearInterval(timer);
}
var a = document.createElement('a');
a.setAttribute('href',alist[index]);
a.innerText=alist[index].title;
holder.appendChild(a);
index++;
}, 1000);
}());
 */