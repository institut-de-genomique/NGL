"use strict";

angular.module('home', ['commonsServices','ngRoute','ultimateDataTableServices','ui.bootstrap','ngl-sq.containerSupportsServices'], function($routeProvider, $locationProvider) {

	$routeProvider.when('/supports/search/home', {
		templateUrl : jsRoutes.controllers.containers.tpl.ContainerSupports.search().url,
		controller : 'SearchCtrl'
	});
	
	$routeProvider.when('/supports/state/home', {
		templateUrl : jsRoutes.controllers.containers.tpl.ContainerSupports.search().url,
		controller : 'SearchStateCtrl'
	});
	
	
	
	$routeProvider.when('/supports/:code', {
		templateUrl : jsRoutes.controllers.containers.tpl.ContainerSupports.details().url,
		controller : 'DetailsCtrl'
	});
	
	$routeProvider.otherwise({redirectTo: '/supports/search/home'});

	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
});