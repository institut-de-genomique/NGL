"use strict";
angular.module('home', ['ngRoute', 'ultimateDataTableServices', 'commonsServices', 'ui.bootstrap', 'ngl-projects.ProjectsServices'], function($routeProvider, $locationProvider) {
	
	$routeProvider.when('/projects/search/home', {
		templateUrl : '/tpl/projects/search/default',
		controller : 'SearchCtrl'
	});
	$routeProvider.when('/projects/:code', {
		templateUrl : '/tpl/projects/details',
		controller : 'DetailsCtrl'
	});
	
	$routeProvider.otherwise({redirectTo: '/projects/search/home'});

	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
});