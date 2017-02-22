// Check if domain is gov.uk so that we can display the gov.uk branding i.e. font and crest
document.body.className = (window.location.href.indexOf("openregister.org") >= 0) ? "openregister-org" : "gov-uk";
