"use strict";
 
angular.module('home', ['commonsServices','ngRoute','datatableServices','ngl-reagent.kitDeclarationsService'], function($routeProvider, $locationProvider) {
	$routeProvider.when('/kits/create/home', {
		templateUrl : jsRoutes.controllers.reagents.tpl.Kits.createOrEdit().url,
		controller : 'CreationKitsCtrl'
	});
	
	$routeProvider.when('/orders/create/home', {
		templateUrl : jsRoutes.controllers.reagents.tpl.Orders.createOrEdit().url,
		controller : 'CreationOrdersCtrl'
	});
	
	$routeProvider.when('/orders/:code', {
		templateUrl : jsRoutes.controllers.reagents.tpl.Orders.createOrEdit().url,
		controller : 'CreationOrdersCtrl'
	});
	
	$routeProvider.when('/kits/search/home', {
		templateUrl : jsRoutes.controllers.reagents.tpl.Kits.search().url,
		controller : 'SearchKitsCtrl'
	});
	
	$routeProvider.when('/kits/:kitCode', {
		templateUrl : jsRoutes.controllers.reagents.tpl.Kits.createOrEdit().url,
		controller : 'CreationKitsCtrl'
	});
	
	$routeProvider.when('/boxes/search/home', {
		templateUrl : jsRoutes.controllers.reagents.tpl.Boxes.search().url,
		controller : 'SearchBoxesCtrl'
	});
	
	$routeProvider.otherwise({redirectTo: '/kits/create/home'});

	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
});