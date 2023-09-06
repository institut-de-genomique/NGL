"use strict";
 
angular.module('home', ['directives','datatableServices','ngRoute'], function($routeProvider, $locationProvider) {
	
	$routeProvider.when('/datatable/demo', {
		templateUrl : '/assets/ngl-devguide/html/datatable/demo.html',
		controller : 'DemoCtrl'
	});	
	
	$routeProvider.when('/datatable', {
		templateUrl : '/assets/ngl-devguide/html/datatable/documentation.html',
		controller : 'DocCtrl'
	});	
	
	$routeProvider.otherwise({redirectTo: '/'});

	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
});
