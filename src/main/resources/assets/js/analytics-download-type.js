(function(){
  var elems = document.getElementsByClassName("js-download");

  GOVUK.registers.analytics.setupEvent(elems, "Data", "download", function(e){
    return e.target.getAttribute("data-download-type") || "unrecognised";
  });

  GOVUK.registers.analytics.setupEvent(elems, "Data", "download", function(e){
    return "data-download";
  });
})();