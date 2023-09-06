"use strict";
angular.module('home', ['ngRoute', 'commonsServices', 'ultimateDataTableServices','ui.bootstrap', 'ngl-sub.ToolsServices'], 
	function($routeProvider, $locationProvider) {

	$routeProvider.when('/sra/projects/consultation/home', {
		// url qui va appeler controler java de type tpl
		templateUrl : '/tpl/sra/projects/consultation',
		controller : 'ConsultationCtrl'
	});	
	

		
	$routeProvider.when('/sra/projects/update/home', {
		// url qui va appeler controler java de type tpl
		templateUrl : '/tpl/sra/projects/update',
		controller : 'UpdateCtrl'
	});	

		
	$routeProvider.when('/sra/projects/create/home', {
		// url qui va appeler controler java de type tpl
		templateUrl : '/tpl/sra/projects/create',
		controller : 'CreateCtrl'
	});	

	$routeProvider.when('/sra/projects/:code', {
		// url qui va appeler controler java de type tpl
		templateUrl : '/tpl/sra/projects/details',
		controller : 'DetailsCtrl'
	});
	
	$routeProvider.otherwise({redirectTo: '/sra/projects/create/home'});

	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
});

