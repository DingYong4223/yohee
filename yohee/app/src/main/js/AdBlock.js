(function() {
    var url=unescape('%s');
    var ftag = %s;
    var el = ftag('a', url);
    if(!el) el = ftag('img', url);
    if(el) {
        var rtag = %s;
        rtag(el);
    }
}());