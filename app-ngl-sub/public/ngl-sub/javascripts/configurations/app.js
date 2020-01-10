"use strict";
angular.module('home', ['ngRoute', 'commonsServices', 'ultimateDataTableServices','ui.bootstrap', 'ngl-sub.ConfigurationsServices'], 
	function($routeProvider, $locationProvider) {
	
	$routeProvider.when('/sra/configurations/create/home', {
		// url qui va appeler controler java de type tpl
		templateUrl : '/tpl/sra/configurations/create',
		controller : 'CreateCtrl'
	});
	
/*	$routeProvider.when('/sra/configurations/:code', {
		// url qui va appeler controler java de type tpl
		templateUrl : '/tpl/sra/configurations/details',
		controller : 'DetailsCtrl'
	});
*/	
	$routeProvider.when('/sra/configurations/consultation/home', {
		// url qui va appeler controler java de type tpl
		templateUrl : '/tpl/sra/configurations/consultation',
		controller : 'ConsultationCtrl'
	});	
			

	$routeProvider.otherwise({redirectTo: '/sra/configurations/create/home'});

	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
});
