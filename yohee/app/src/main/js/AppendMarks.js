(function() {
    var holder = document.getElementById('flex-holder');
    if(holder.hasChildNodes()) return;
    var alist = %s;

    var index = 0;
    var timer = setInterval(function() {
        if(index >= alist.length - 1) clearInterval(timer);

        var a = document.createElement('a');
        a.setAttribute('href',alist[index].url);
        a.setAttribute('class','animMarkIn');
        a.innerText=alist[index++].title;
        holder.appendChild(a);
    }, 30);
}());