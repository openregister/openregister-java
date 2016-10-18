var elems = document.getElementsByClassName("js-download");
trackEvent(elems, "Data", "download", function(e){ return e.target.text; });