angular.module('commonsServices')
    .directive('toggleButtons', ['$compile', function($compile) {
      return {
        restrict: 'E',
        scope: {
          buttons: '='
        },
        compile: function(tElement, tAttrs) {
          return function postLink(scope, element, attrs) {
            
            var template = [
              '<ul class="list-inline">' +
                '<li>' +
                  '<div ng-repeat="button in buttons" class="btn-group btn-toggle">' + 
                    '<button ng-class="{\'btn-toggle-primary active\': $index === 0, \'btn-toggle-default\': $index !== 0}" class="btn btn-xs no-radius no-outline" ng-click="toggle($index);button.action()" ng-bind-html="button.name"></button>' +
                  '</div>' +
                '</li>' +
              '</ul>'
            ].join('');
            
            element.html(template);
            $compile(element.contents())(scope);
             
            // 
            scope.toggle = function(index) {
              angular.forEach(element.find('.btn'), function(btn, btnIndex) {
                var $btn = angular.element(btn);
                if (btnIndex === index) {
                  $btn.addClass('btn-toggle-primary active').removeClass('btn-toggle-default');
                } else {
                  $btn.addClass('btn-toggle-default').removeClass('btn-toggle-primary active');
                }
              });
            };
          };
        }
      };
    }]);
