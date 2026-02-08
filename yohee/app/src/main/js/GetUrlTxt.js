(function() {
var getTxt = function(el) {
     if(!el) return;
     yohee.invoke('GetUrlTxt', el.text);
};
var frtags = %s;
frtags('a', '%s', getTxt);
}());