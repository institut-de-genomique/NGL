"use strict";
angular.module('home', ['ngRoute', 'ultimateDataTableServices','commonsServices', 'ui.bootstrap','ngl-reagents.ReagentReceptionsServices'], 
	function($routeProvider, $locationProvider) {
	
	$routeProvider.when('/reagent-receptions/search/home', {
		templateUrl : '/tpl/reagent-receptions/search',
		controller : 'SearchCtrl'
	});

	$routeProvider.when('/reagent-receptions/state/home', {
		templateUrl : '/tpl/reagent-receptions/search',
		controller : 'SearchStateCtrl'
	});
	
	$routeProvider.when('/reagent-receptions/create/home', {
		templateUrl : '/tpl/reagent-receptions/create',
		controller : 'CreationCtrl'
	});
	
	$routeProvider.when('/reagent-receptions/new/home', {
		templateUrl : '/tpl/reagent-receptions/new-from-file',
		controller : 'NewFromFileCtrl'
	});

	$routeProvider.when('/reagent-receptions/:code', {
		templateUrl : '/tpl/reagent-receptions/details',
		controller : 'DetailsCtrl'
	});
	
	$routeProvider.otherwise({redirectTo: '/reagent-receptions/search/home'});

	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
});

