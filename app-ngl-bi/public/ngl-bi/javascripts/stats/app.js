"use strict";
angular.module('home', ['ngRoute', 'ultimateDataTableServices','commonsServices','valuationServices','biCommonsServices', 'ui.bootstrap', 
                        'ngl-bi.ReadSetsStatsServices','ngl-bi.LanesStatsServices','ngl-bi.ReadSetsServices', 'ngl-bi.RunsServices', 'basketServices'], function($routeProvider, $locationProvider) {
	
	$routeProvider.when('/stats/readsets/home', {
		templateUrl : '/tpl/stats/readsets/choice',
		controller : 'StatsChoiceCtrl'
	});
	
	$routeProvider.when('/stats/lanes/home', {
		templateUrl : '/tpl/runs/search/default',
		controller : 'StatsSearchLanesCtrl'
	});
	
	$routeProvider.when('/stats/readsets-search/home', {
		templateUrl : '/tpl/readsets/search',
		controller : 'StatsSearchReadSetsCtrl'
	});
	
	$routeProvider.when('/stats/readsets-config/home', {
		templateUrl : '/tpl/stats/readsets/config',
		controller : 'StatsConfigReadSetsCtrl'
	});
	
	$routeProvider.when('/stats/lanes-config/home', {
		templateUrl : '/tpl/stats/lanes/config',
		controller : 'StatsConfigLanesCtrl'
	});
	
	$routeProvider.when('/stats/readsets-show/home', {
		templateUrl : '/tpl/stats/readsets/show',
		controller : 'StatsShowReadSetsCtrl'
	});
	
	$routeProvider.when('/stats/lanes-show/home', {
		templateUrl : '/tpl/stats/lanes/show',
		controller : 'StatsShowLanesCtrl'
	});
	
	$routeProvider.otherwise({redirectTo: '/stats/readsets/home'});

	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
});

