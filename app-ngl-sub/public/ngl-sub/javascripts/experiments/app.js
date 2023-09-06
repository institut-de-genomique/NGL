"use strict";
angular.module('home', ['ngRoute', 'commonsServices', 'ultimateDataTableServices','ui.bootstrap', 'ngl-sub.ToolsServices', 'ngl-sub.Services'], 
	function($routeProvider, $locationProvider) {
	
	$routeProvider.when('/sra/experiments/consultation/home', {
		// url qui va appeler controler java de type tpl
		templateUrl : '/tpl/sra/experiments/consultation',
		controller : 'ConsultationCtrl'
	});	

	$routeProvider.when('/sra/experiments/:code', {
		// url qui va appeler controler java de type tpl
		templateUrl : '/tpl/sra/experiments/details',
		controller : 'DetailsCtrl'
	});
	$routeProvider.when('/sra/experiments/update/home', {
		// url qui va appeler controler java de type tpl
		templateUrl : '/tpl/sra/experiments/update',
		controller : 'UpdateCtrl'
	});			
	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
});

