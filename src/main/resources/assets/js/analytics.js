var GOVUK = GOVUK || {};
GOVUK.registers = GOVUK.registers || {};

GOVUK.registers.analytics = (function () {
  var setupGA, setupEvents, sendPageview, sendEvent;

  var trackerId;
  var isTrackerPresent = function() {
    return (typeof trackerId !== 'undefined') && (trackerId !== '');
  };

  var checkTrackerAndExecute = function(fnToExecute, errorMsg) {
    if (isTrackerPresent()) {
      fnToExecute();
    } else {
      console.error('GA tracker not present. ' + errorMsg || '');
    }
  };

  sendEvent = function(category, action, targetLabel, fnHitCallback) {
    checkTrackerAndExecute(function() {
      callbackExecuted = false;
      var executeHitCallback = function () {
        if (!callbackExecuted) {
          callbackExecuted = true;
          fnHitCallback();
        }
      };
      setTimeout(executeHitCallback, 1000);

      ga('send', 'event', category, action, targetLabel, { hitCallback: executeHitCallback });
    }, 'Cannot send event.')
  };

  sendPageview = function() {
    checkTrackerAndExecute(function() {
      ga('send', 'pageview');
    }, 'Cannot send pageview.');
  };

  setupGA = function(trackingId) {
    trackerId = trackingId;
    checkTrackerAndExecute(function() {
      (function(i,s,o,g,r,a,m) { i['GoogleAnalyticsObject']=r;i[r]=i[r]||function() {
        (i[r].q=i[r].q||[]).push(arguments)}, i[r].l=1*new Date();a=s.createElement(o),
        m=s.getElementsByTagName(o)[0]; a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
      })(window, document, 'script', '//www.google-analytics.com/analytics.js','ga');

      ga('create', trackerId, 'auto');
      ga('set', 'anonymizeIp', true);
      ga('set', 'displayFeaturesTask', null);
      ga('set', 'transport', 'beacon');
    }, 'Cannot initialise GA.');
  };

  setupEvent = function(elems, category, action, fnLabel) {
    checkTrackerAndExecute(function() {
      for (var i=0; i < elems.length; i++) {
        elems[i].addEventListener('click', function(e) {
          e.preventDefault();
          var targetLabel = fnLabel(e);
          sendEvent(category, action, targetLabel, function() {
            if (e.target.href) {
              document.location = e.target.href;
            } else {
              console.log('GA event sent for target without href');
              console.log(e);
            }
          });
        }, false);
      };
    }, 'Cannot setup event');
  };

  return {
    setupGA: setupGA,
    setupEvent: setupEvent,
    sendPageview: sendPageview,
    sendEvent: sendEvent
  };
}());

GOVUK.registers.analytics.setupGA(gaTrackingId);
GOVUK.registers.analytics.sendPageview();
