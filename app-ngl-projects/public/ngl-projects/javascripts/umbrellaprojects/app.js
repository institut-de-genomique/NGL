"use strict";
angular.module('home', ['ngRoute','ultimateDataTableServices','commonsServices','ui.bootstrap', 'ngl-projects.ProjectsServices', 'ngSanitize']).config(['$routeProvider', '$locationProvider', '$compileProvider', function($routeProvider, $locationProvider, $compileProvider) {
	
	$routeProvider.when('/umbrellaprojects/search/home', {
		templateUrl : '/tpl/umbrellaprojects/search/default',
		controller : 'SearchCtrl'
	});

	$routeProvider.when('/umbrellaprojects/:code', {
		templateUrl : '/tpl/umbrellaprojects/details/update',
		controller : 'DetailsCtrl'
	});

	$routeProvider.otherwise({redirectTo: '/umbrellaprojects/search/home'});

	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});

	$compileProvider.aHrefSanitizationWhitelist(/^\s*(https?|mailto|data):/);
}]);

