"use strict";

angular.module('home', ['ngRoute','datatableServices','commonsServices'], function($routeProvider, $locationProvider) {
		
	$routeProvider.when('/barcodes/new/home', {
		templateUrl : '/tpl/barcodes/create',
		controller : 'CreateCtrl'
	});	
	
	$routeProvider.when('/barcodes/search/home', {
		templateUrl : '/tpl/barcodes/search',
		controller : 'SearchCtrl'
	});	
	
	
	$routeProvider.otherwise({redirectTo: '/barcodes/search/home'});

	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
});
