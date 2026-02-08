(function() {
   var holder = document.getElementById('flex-holder');
   var alist = ["pv.sohu.com","www.qchannel03.cn","s.go.sohu.com","s.go.sohu.com"];
   for(var i=0;i<alist.length;i++){
       var a = document.createElement('a');
       a.setAttribute('href',alist[i]);
	   a.innerText='测试搜索';
       holder.appendChild(a);
   };
}());