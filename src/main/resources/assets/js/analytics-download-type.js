(function(){
    var elems = document.getElementsByClassName("js-download");

    setupEventAnalytics(elems, "Data", "download", function(e){
        return e.target.getAttribute("data-download-type") || "unrecognised";
    });

    setupEventAnalytics(elems, "Data", "download", function(e){
        return "data-download";
    });
})();