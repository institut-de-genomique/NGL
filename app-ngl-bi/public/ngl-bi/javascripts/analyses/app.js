"use strict";
angular.module('home', ['ngRoute', 'ultimateDataTableServices','valuationServices','commonsServices','biCommonsServices', 'ui.bootstrap','ngl-bi.AnalysesServices'], function($routeProvider, $locationProvider) {
	
	$routeProvider.when('/analyses/search/home', {
		templateUrl : '/tpl/analyses/search/default',
		controller : 'SearchCtrl'
	});
	$routeProvider.when('/analyses/valuation/home', {
		templateUrl : '/tpl/analyses/search/valuation',
		controller : 'SearchValuationCtrl'
	});
	
	$routeProvider.when('/analyses/state/home', {
		templateUrl : '/tpl/analyses/search/state',
		controller : 'SearchStateCtrl'
	});
		
	$routeProvider.when('/analyses/:code', {
		templateUrl : '/tpl/analyses/details',
		controller : 'DetailsCtrl'
	});
	$routeProvider.when('/analyses/:code/:page', {
		templateUrl : '/tpl/analyses/details',
		controller : 'DetailsCtrl'
	});
	
	$routeProvider.otherwise({redirectTo: '/analyses/search/home'});

	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
});