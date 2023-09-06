"use strict";
angular.module('home', ['ngRoute', 'commonsServices', 'ultimateDataTableServices','ui.bootstrap', 'ngl-sub.ToolsServices', 'ngl-sub.Services'], 
	function($routeProvider, $locationProvider) {
	
	$routeProvider.when('/sra/samples/consultation/home', {
		// url qui va appeler controler java de type tpl
		templateUrl : '/tpl/sra/samples/consultation',
		controller : 'ConsultationCtrl'
	});	
	$routeProvider.when('/sra/samples/create/home', {
		// url qui va appeler controler java de type tpl
		templateUrl : '/tpl/sra/samples/create',
		controller : 'CreateCtrl'
	});
	$routeProvider.when('/sra/samples/update/home', {
		// url qui va appeler controler java de type tpl
		templateUrl : '/tpl/sra/samples/update',
		controller : 'UpdateCtrl'
	});	
	$routeProvider.when('/sra/samples/:code', {
		// url qui va appeler controler java de type tpl
		templateUrl : '/tpl/sra/samples/details',
		controller : 'DetailsCtrl'
	});
	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
});

