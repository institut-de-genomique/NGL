"use strict";

angular.module('home', ['commonsServices','ngRoute','ultimateDataTableServices','ui.bootstrap','ngl-sq.samplesServices'], function($routeProvider, $locationProvider) {
	
	$routeProvider.when('/samples/search/home', {
		templateUrl : jsRoutes.controllers.samples.tpl.Samples.search().url,
		controller : 'SearchCtrl'
	});
	
	
	$routeProvider.when('/samples/:code', {
		templateUrl : jsRoutes.controllers.samples.tpl.Samples.details().url,
		controller : 'DetailsCtrl'
	});
	
	$routeProvider.otherwise({redirectTo: jsRoutes.controllers.samples.tpl.Samples.home("search").url});

	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
});

