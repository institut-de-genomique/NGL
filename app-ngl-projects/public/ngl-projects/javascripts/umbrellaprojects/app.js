"use strict";
angular.module('home', ['ngRoute','datatableServices','commonsServices','ui.bootstrap', 'ngl-projects.ProjectsServices'], 
 function($routeProvider, $locationProvider) {
	$routeProvider.when('/umbrellaprojects/search/home', {
		templateUrl : '/tpl/umbrellaprojects/search/default',
		controller : 'SearchCtrl'
	});
	$routeProvider.when('/umbrellaprojects/:code', {
		templateUrl : '/tpl/umbrellaprojects/details/update',
		controller : 'DetailsCtrl'
	});
	$routeProvider.when('/umbrellaprojects/add/home', {
		//templateUrl : '/tpl/umbrellaprojects/add',
		templateUrl : '/tpl/umbrellaprojects/details/add',
		controller : 'AddCtrl'
	});

	$routeProvider.otherwise({redirectTo: '/umbrellaprojects/search/home'});

	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
}
);

