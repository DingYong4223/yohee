function(el) {
     if(!el) return;
     var el2=el;
     while(true){
         if(!el2 || el2.id||el2.className){break}else{
             el2=el2.parentNode;
         }
     };
     if(el2 && el2.tagName.toLowerCase()!='body'){
         el=el2;
         while(true){
             el2=el2.parentNode;
             if(!el2 || el2.childElementCount>1 || el2.tagName.toLowerCase()=='body'){break};
             if(el2.id || el2.className){el=el2}
         };
         el.parentNode.removeChild(el);
     }
}