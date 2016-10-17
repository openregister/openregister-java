var elems = document.getElementsByClassName('js-download');
for(var i=0; i< elems.length; i++) {
    elems[i].onclick = function(e){
        ga('send', 'event', 'Data', 'download', e.target.text);
        console.log(e.target.text);
    };
}