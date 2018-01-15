// Works in combination with the following data-attributes
// data-click-events - this just sets the thing up designed to work with A, INPUT[type~="button radio checkbox"], BUTTON
// OR you can put it on a whole div/form and it will track all the aforementioned elements within it
// data-click-category="Header" - this is the category GA will put it in
// data-click-action="Navigation link clicked" - this is the action GA will label it

'use strict';

var setupTracking = function setupTracking(elements) {
  elements.forEach(function (element) {
    var eventCategory = element.dataset.clickCategory;
    var eventAction = element.dataset.clickAction;

    switch (element.tagName) {
      case 'A':
      case 'BUTTON':
      case 'INPUT':
        var label = element.tagName === 'INPUT' ? element.value : element.innerText;
        addListener(element, eventCategory, eventAction, label);
        break;
      default:
        var childClickables = Array.prototype.slice.call(element.querySelectorAll('a, button, input[type~="button radio checkbox"], summary'));

        if (childClickables.length) {
          childClickables.forEach(function (element) {
            var label = void 0;
            switch (element.tagName) {
              case 'A':
              case 'BUTTON':
              case 'SUMMARY':
                label = element.innerText;
                break;
              default:
                label = element.value;
                break;
            }

            addListener(element, eventCategory, eventAction, label);
          });
        }
        break;
    }
  });
};

var addListener = function addListener(element, category, action, label) {
  if (element.tagName === 'SUMMARY') {
    action = action + ' opened';
  }
  element.addEventListener('click', function () {
    action = toggleAction(element, action);
    ga('send', 'event', category, action, label);
  });
};

var toggleAction = function toggleAction(element, action) {
  var actionWords = action.split(' ');
  var oldState = actionWords[actionWords.length - 1];
  var newState = element.parentElement.hasAttribute('open') ? 'closed' : 'opened';
  return action.replace(oldState, newState);
};

var elementsToTrack = Array.prototype.slice.call(document.querySelectorAll('[data-click-events]'));

if (elementsToTrack) {
  setupTracking(elementsToTrack);
}
