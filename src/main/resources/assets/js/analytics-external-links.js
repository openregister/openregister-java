(function(){
  var elems = document.getElementsByTagName('a');
  var filtered = new Array();

  for(var j = 0; j < elems.length; j++) {
    var href = elems[j].getAttribute("href");
    if (! (href.startsWith("/") || href.startsWith("#") || href.startsWith("mailto:")) ) {
      filtered.push(elems[j]);
    }
  }

  GOVUK.registers.analytics.setupEvent(filtered, "Link", "external", function(e){ 
    return e.target.href; 
  });
})();