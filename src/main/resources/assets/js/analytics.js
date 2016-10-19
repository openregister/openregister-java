(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
    (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
        m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
})(window,document,'script','//www.google-analytics.com/analytics.js','ga');

ga('create', 'UA-85775854-1', 'auto', {
    'allowLinker': false,
    'name':'oldTracker'
});

ga('create', 'UA-85775854-2', 'auto', {
    'allowLinker': false
});

ga('send', 'pageview');
ga('oldTracker.send', 'pageview');


var setupEventAnalytics = function(elems, category, action, fnLabel){
    for(var i=0; i < elems.length; i++) {
        elems[i].addEventListener("click", function(e){
            var targetLabel = fnLabel(e);
            console.log("sending event - category: "+ category + " action: "+ action + " label: "+ targetLabel +" target: "+ e.target);
            ga('send', 'event', category, action, targetLabel);
            ga('oldTracker.send', 'event', category, action, targetLabel);
        }, false);
    };
};

var setupVirtualPageviewAnalytics = function(elems, fnPath){
    for(var i=0; i < elems.length; i++) {
        elems[i].addEventListener("click", function(e){
            var targetPath = fnPath(e);
            console.log("sending virtual pageview - path: "+targetPath+" target: "+ e.target);
            ga('send', 'pageview', targetPath);
            ga('oldTracker.send', 'pageview', targetPath);
        }, false);
    };
};
