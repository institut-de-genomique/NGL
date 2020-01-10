"use strict";
 
angular.module('home', ['ngRoute', 'ultimateDataTableServices','commonsServices', 'ui.bootstrap', 
                        'ngl-bi.BalanceSheetsService'], function($routeProvider, $locationProvider) {
	
	$routeProvider.when('/balance-sheets/:typeCode/general/home',{
		templateUrl : '/tpl/balance-sheets/general',
		controller : 'BalanceSheetsGeneralCtrl'
	});
	
	$routeProvider.when('/balance-sheets/:typeCode/:year/home',{
		templateUrl : '/tpl/balance-sheets/year',
		controller : 'BalanceSheetsYearCtrl'
	});
	
	
	
	$routeProvider.otherwise({redirectTo: '/balance-sheets/general/home'});

	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
});
