function(tag, url) {
   if(tag != 'a' && tag != "img") return;
   var ls=document.getElementsByTagName(tag);
   for(var i=0;i<ls.length;i++){
       var turl = unescape(tag == 'a' ? ls[i].href : ls[i].src);
       if(turl.indexOf(url) != -1) {
            return ls[i];
       }
   };
   return null;
}