"use strict";
angular.module('home', ['ngRoute', 'commonsServices', 'ultimateDataTableServices','ui.bootstrap', 'ngl-sub.ExperimentsServices'], 
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
    		
	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
});

