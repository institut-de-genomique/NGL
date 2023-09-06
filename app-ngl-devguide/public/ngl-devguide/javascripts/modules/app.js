"use strict";
 
angular.module('home', ['ngRoute','directives'], function($routeProvider, $locationProvider) {
	
	$routeProvider.when('/modules/twitter-bootstrap', {
		templateUrl : '/assets/ngl-devguide/html/modules/twitter-bootstrap/documentation.html'
	});	
	
	$routeProvider.when('/modules/gestion-user-permissions', {
		templateUrl : '/assets/ngl-devguide/html/modules/gestion-user-permissions/documentation.html'
	});	
	
	$routeProvider.when('/modules/gestion-user-history', {
		templateUrl : '/assets/ngl-devguide/html/modules/gestion-user-history/documentation.html'
	});	
	
	$routeProvider.when('/modules/gestion-assets', {
		templateUrl : '/assets/ngl-devguide/html/modules/gestion-assets/documentation.html'
	});	
	
	$routeProvider.when('/modules/cas', {
		templateUrl : '/assets/ngl-devguide/html/modules/cas/documentation.html'
	});	
	
	$routeProvider.otherwise({redirectTo: '/'});

	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
});
