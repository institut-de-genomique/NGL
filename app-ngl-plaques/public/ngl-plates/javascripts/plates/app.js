"use strict";

angular.module('home', ['ngRoute','ultimateDataTableServices','basketServices','commonsServices'], function($routeProvider, $locationProvider) {
		
	$routeProvider.when('/plates/search/home', {
		templateUrl : '/tpl/plates/search',
		controller : 'SearchCtrl'
	});	
	
	$routeProvider.when('/plates/new/home', {
		templateUrl : '/tpl/plates/search-manips',
		controller : 'SearchManipsCtrl'
	});
	
	$routeProvider.when('/plates/new-from-file/home', {
		templateUrl : '/tpl/plates/from-file',
		controller : 'FromFileCtrl'
	});
	
	$routeProvider.when('/plates/:code', {
		templateUrl : '/tpl/plates/details',
		controller : 'DetailsCtrl'
	});
	$routeProvider.otherwise({redirectTo: '/plates/search/home'});

	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
});
