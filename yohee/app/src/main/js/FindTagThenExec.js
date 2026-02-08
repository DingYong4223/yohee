function(tag, url, exec) {
    if(tag != 'a' && tag != 'img') return;
    var ls=document.getElementsByTagName(tag);
    var el=null;
    for(var i=0;i<ls.length;i++) {
        var turl = tag == 'a' ? ls[i].href : ls[i].src;
        if(turl && turl.indexOf(url) != -1) {
             exec(ls[i]);
        }
    };
}