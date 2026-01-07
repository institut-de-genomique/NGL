var id = 0;
angular.module('commonsServices').directive('accordion', function($compile, $sce) {
    return {
        restrict: 'E',
        scope: {
            panels: '=?'
        },
        compile: function(tElement, tAttrs) {
            return function postLink(scope, element, attrs) {
                console.log("Post-link function execution");

                if (!scope.panels) {
                    scope.panels = [];
                    var panels = element[0].querySelectorAll('panel');

                    angular.forEach(panels, function(panelElem) {
                        var panelScope = {};
                        var panel = angular.element(panelElem);

                        panelScope.title = panel.children('title').text().trim();
                        panelScope.content = panel.children('content').html().trim();

                        var openAttr = panelElem.getAttribute('open');
                        panelScope.open = (openAttr !== null && openAttr.trim().toLowerCase() === 'true');

                        scope.panels.push(panelScope);

                    });
                }

                var accordionId = ++id;

                var template = [
                    '<div class="panel-group" id="group' + accordionId + '" role="tablist" aria-multiselectable="false">',
                        '<div class="panel panel-default" ng-repeat="panel in panels">',
                            '<div class="panel-heading" role="tab" id="heading' + accordionId + '{{ $index + 1 }}">',
                                '<h5 class="panel-title">',
                                    '<a role="button" data-toggle="collapse" class="accordion-plus-toggle"',
                                       'ng-class="{collapsed: !panel.open}"',
                                       'data-parent="#group' + accordionId + '"',
                                       'href="#' + accordionId + '_{{ $index + 1 }}_{{ concat(panel.title) }}" aria-expanded="{{ panel.open }}" aria-controls="' + accordionId + '_{{ $index + 1 }}_{{ concat(panel.title) }}">',
                                        '{{ capitalizeFirsts(panel.title) }}',
                                    '</a>',
                                '</h5>',
                            '</div>',
                            '<div id="' + accordionId + '_{{ $index + 1 }}_{{ concat(panel.title) }}" class="panel-collapse collapse" ng-class="{in: panel.open}"',
                                 'role="tabpanel"',
                                 'aria-labelledby="heading' + accordionId + '{{ $index + 1 }}">',
                                '<div class="panel-body" ng-bind-html="getContent(panel)">',
                                '</div>',
                            '</div>',
                        '</div>',
                    '</div>'
                ].join('');

                element.html(template);
                $compile(element.contents())(scope);

                scope.concat = function(title) {
                    return title.replace(/\s+/g, ' ').trim().split(' ').join('_').toLowerCase();
                };

                scope.capitalizeFirsts = function(title) {
                    return title.replace(/\s+/g, ' ').trim()
                        .split(' ')
                        .map(function(word) {
                            return word.charAt(0).toUpperCase() + word.slice(1).toLowerCase();
                        })
                        .join(' ');
                };

                scope.getContent = function(panel) {
                    return $sce.trustAsHtml(panel.content);
                };

                // 
                var hash = window.location.hash;
                if (hash) {
                    var target = angular.element(hash);
                    angular.forEach(scope.panels, function(panel, index) {
                        var panelId = '#' + accordionId + '_' + (index + 1) + '_' + scope.concat(panel.title);
                        var panelElement = angular.element(panelId);
                        if (panelId === hash) {
                            panel.open = true;
                            panelElement.collapse('show');
                        } else {
                            panel.open = false;
                            panelElement.collapse('hide');
                        }
                    });
                } else {
                    angular.forEach(scope.panels, function(panel, index) {
                        var panelId = '#' + accordionId + '_' + (index + 1) + '_' + scope.concat(panel.title);
                        var panelElement = angular.element(panelId);
                        if (panel.open) {
                            panelElement.collapse('show');
                        } else {
                            panelElement.collapse('hide');
                        }
                    });
                }

                // 
                function openPanelFromHash() {
                    var hash = window.location.hash;
                    if (hash) {
                        var target = angular.element(hash);
                        if (target.length) {
                            element.find('.panel-collapse').not(target).collapse('hide');
                            target.collapse('show');
                        }
                    }
                }

                // 
                element.on('click', 'a[data-toggle="collapse"]', function(event) {
                    event.preventDefault();
                    var target = angular.element(this).attr('href');
                    var targetElement = angular.element(target);
                    var isCollapsed = targetElement.hasClass('in');

                    element.find('.panel-collapse').not(target).collapse('hide');

                    if (!isCollapsed) {
                        targetElement.collapse('show');
                    } else {
                        targetElement.collapse('hide');
                    }
	
                    //var newUrl = window.location.protocol + "//" + window.location.host + window.location.pathname + target;
                    //history.pushState(null, null, newUrl);
                });

                window.addEventListener('popstate', openPanelFromHash);
            };
        }
    };
});
