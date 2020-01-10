"use strict";
 
angular.module('home', ['ngRoute','ultimateDataTableServices', 'commonsServices'], function($routeProvider, $locationProvider) {
	
	$routeProvider.when('/printing/tags/home', {
		templateUrl : jsRoutes.controllers.printing.tpl.Tags.display().url,
		controller : 'TagsCtrl'
	});
	
	
	$routeProvider.otherwise({redirectTo: '/printing/tags/home'});

	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({
		  enabled: true,
		  requireBase: false
		});
});