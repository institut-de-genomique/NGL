/* FDS NGL-836 19/11/2018 d'apres Samples*/
"use strict";

angular.module('home', ['commonsServices','ngRoute','ultimateDataTableServices','ui.bootstrap','ngl-sq.indexesServices']).config(['$routeProvider', '$locationProvider', function($routeProvider, $locationProvider) {
	$routeProvider.when('/indexes/search/home', {
		templateUrl : jsRoutes.controllers.indexes.tpl.Indexes.search().url,
		controller : 'SearchCtrl'
	});
	
	// si aucune de ces routes n'est demandee=> fallback search
	$routeProvider.otherwise({redirectTo: jsRoutes.controllers.indexes.tpl.Indexes.home("search").url});

	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
}]);