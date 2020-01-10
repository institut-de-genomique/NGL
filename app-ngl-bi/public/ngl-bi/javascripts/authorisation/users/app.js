"use strict";
angular.module('home', ['ngRoute','ultimateDataTableServices','commonsServices','biCommonsServices', 'ui.bootstrap','ngl-bi.UsersServices'], function($routeProvider, $locationProvider) {
	
	$routeProvider.when('/users/search/home', {
		templateUrl : '/tpl/users/search',
		controller : 'SearchUsersCtrl'
	});

	$routeProvider.otherwise({redirectTo: '/users/search/home'});

	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
});