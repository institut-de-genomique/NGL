"use strict";
angular.module('home', ['ngRoute','ultimateDataTableServices','valuationServices','commonsServices','biCommonsServices', 'biWorkflowChartServices', 'ui.bootstrap','ngl-bi.RunsServices'], function($routeProvider, $locationProvider) {
	
	$routeProvider.when('/runs/search/home', {
		templateUrl : '/tpl/runs/search/default',
		controller : 'SearchCtrl'
	});
	$routeProvider.when('/runs/valuation/home', {
		templateUrl : '/tpl/runs/search/valuation',
		controller : 'SearchValuationCtrl'
	});
	$routeProvider.when('/runs/state/home', {
		templateUrl : '/tpl/runs/search/state',
		controller : 'SearchStateCtrl'
	});
	$routeProvider.when('/runs/:code', {
		templateUrl : '/tpl/runs/details',
		controller : 'DetailsCtrl'
	});
	$routeProvider.when('/runs/:code/:page', {
		templateUrl : '/tpl/runs/details',
		controller : 'DetailsCtrl'
	});

	$routeProvider.otherwise({redirectTo: '/runs/search/home'});

	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
});

