"use strict";
 
angular.module('home', ['ngRoute','directives'], function($routeProvider, $locationProvider) {
	
	$routeProvider.when('/services/atomicTransfereServices', {
		templateUrl : '/assets/ngl-devguide/html/services/atomicTransfereServices/documentation.html'
	});
	
	$routeProvider.otherwise({redirectTo: '/'});

	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
});
