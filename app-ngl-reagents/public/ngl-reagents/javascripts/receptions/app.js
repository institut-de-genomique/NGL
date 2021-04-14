"use strict";
angular.module('home', ['ngRoute', 'ultimateDataTableServices','commonsServices', 'ui.bootstrap','ngl-reagents.ReagentReceptionsServices'], 
	function($routeProvider, $locationProvider) {
	
	$routeProvider.when('/receptions/search/home', {
		templateUrl : '/tpl/receptions/search',
		controller : 'SearchCtrl'
	});
	
	$routeProvider.when('/receptions/create/home', {
		templateUrl : '/tpl/receptions/create',
		controller : 'CreationCtrl'
	});
	
	$routeProvider.when('/receptions/new/home', {
		templateUrl : '/tpl/receptions/new-from-file',
		controller : 'NewFromFileCtrl'
	});
	
	$routeProvider.otherwise({redirectTo: '/receptions/search/home'});

	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
});

