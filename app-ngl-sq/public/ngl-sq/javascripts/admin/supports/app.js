"use strict";

angular.module('home', ['commonsServices','ngRoute','ultimateDataTableServices','ui.bootstrap'], function($routeProvider, $locationProvider) {
	
	$routeProvider.when('/admin/supports/switch-index/home', {
		templateUrl : jsRoutes.controllers.admin.supports.tpl.Supports.search('switch-index').url,
		controller : 'SwitchIndexSearchCtrl'
	});
	
	$routeProvider.when('/admin/supports/content-update/home', {
		templateUrl : jsRoutes.controllers.admin.supports.tpl.Supports.search('content-update').url,
		controller : 'ContentUpdateCtrl'
	});
	
	$routeProvider.otherwise({redirectTo: jsRoutes.controllers.admin.supports.tpl.Supports.home('switch-index').url});

	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
});