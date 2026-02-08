(function() {
var rtag = %s;
var frtags = %s;
var adList = %s;
for(var i=0;i<adList.length;i++) {
    frtags('a', adList[i], rtag);
    frtags('img', adList[i], rtag);
}
yohee.invoke('AdUIClear', '');
}());