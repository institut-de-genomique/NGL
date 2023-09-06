"use strict";
angular.module('home', ['ngRoute', 'commonsServices', 'ultimateDataTableServices','ui.bootstrap', 'ngl-sub.ToolsServices'], 
	function($routeProvider, $locationProvider) {
	
	$routeProvider.when('/sra/analyzes/create/home', {
		// url qui va appeler controler java de type tpl
		templateUrl : '/tpl/sra/analyzes/create',
		controller : 'CreateCtrl'
	});

	$routeProvider.when('/sra/analyzes/consultation/home', {
		// url qui va appeler controler java de type tpl
		templateUrl : '/tpl/sra/analyzes/consultation',
		controller : 'ConsultationCtrl'
	});	
			
	$routeProvider.when('/sra/analyzes/:code', {
		// url qui va appeler controler java de type tpl
		templateUrl : '/tpl/sra/analyzes/details',
		controller : 'DetailsCtrl'
	});
	
	$routeProvider.otherwise({redirectTo: '/sra/analyzes/create/home'});

	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
});
