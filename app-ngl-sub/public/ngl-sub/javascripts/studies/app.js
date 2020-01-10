"use strict";
angular.module('home', ['ngRoute', 'commonsServices', 'ultimateDataTableServices','ui.bootstrap', 'ngl-sub.StudiesServices'], 
	function($routeProvider, $locationProvider) {
	
	$routeProvider.when('/sra/studies/create/home', {
		// url qui va appeler controler java de type tpl
		templateUrl : '/tpl/sra/studies/create',
		controller : 'CreateCtrl'
	});
	$routeProvider.when('/sra/studies/consultation/home', {
		// url qui va appeler controler java de type tpl
		templateUrl : '/tpl/sra/studies/consultation',
		controller : 'ConsultationCtrl'
	});	
	$routeProvider.when('/sra/studies/release/home', {
		// url qui va appeler controler java de type tpl
		templateUrl : '/tpl/sra/studies/release',
		controller : 'ReleaseCtrl'
	});	
	
	$routeProvider.when('/sra/studies/:code', {
		// url qui va appeler controler java de type tpl
		templateUrl : '/tpl/sra/studies/details',
		controller : 'DetailsCtrl'
	});
			
	$routeProvider.otherwise({redirectTo: '/sra/studies/create/home'});

	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
});

