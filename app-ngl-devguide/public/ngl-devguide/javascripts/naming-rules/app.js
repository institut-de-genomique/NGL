"use strict";
 
angular.module('home', ['ngRoute','directives'], function($routeProvider, $locationProvider) {
	
	$routeProvider.when('/naming-rules/introduction', {
		templateUrl : '/assets/ngl-devguide/html/naming-rules/introduction.html'
	});	
	
	$routeProvider.when('/naming-rules/controllers', {
		templateUrl : '/assets/ngl-devguide/html/naming-rules/controllers.html'
	});	
	
	$routeProvider.when('/naming-rules/views', {
		templateUrl : '/assets/ngl-devguide/html/naming-rules/views.html'
	});
	
	$routeProvider.when('/naming-rules/models', {
		templateUrl : '/assets/ngl-devguide/html/naming-rules/models.html'
	});
	
	$routeProvider.when('/naming-rules/routes', {
		templateUrl : '/assets/ngl-devguide/html/naming-rules/routes.html'
	});
	
	$routeProvider.when('/naming-rules/assets', {
		templateUrl : '/assets/ngl-devguide/html/naming-rules/assets.html'
	});
	
	$routeProvider.when('/naming-rules/messages', {
		templateUrl : '/assets/ngl-devguide/html/naming-rules/messages.html'
	});
	
	$routeProvider.otherwise({redirectTo: '/naming-rules/introduction'});

	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
});
