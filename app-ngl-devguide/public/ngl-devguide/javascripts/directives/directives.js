'use strict';
(function () {


var module = angular.module('directives', []);


/*
* <pre> elements : adds a 'prettyprint' attribute for syntax highlighting with google-code-prettify, and triggers its
* lauching if it isn't already triggered.
*/
module.directive('pre', ['$rootScope', '$timeout', function($rootScope, $timeout) {
    var prettyPrintTriggered = false;
    return {
        restrict: 'E',
        terminal: true, // Prevent AngularJS compiling code blocks
        compile: function(element, attrs) {
            if (!attrs['class']) {
                attrs.$set('class', 'prettyprint');
            } else if (attrs['class'] && attrs['class'].split(' ').indexOf('prettyprint') == -1) {
                attrs.$set('class', attrs['class'] + ' prettyprint');
            }
            return function(scope, element, attrs) {
                if (!prettyPrintTriggered) {
                    prettyPrintTriggered = true;
                    $timeout(function () {
                        prettyPrintTriggered = false;
                        prettyPrint();
                    });
                }
            };
        }

    };
}]);

})();