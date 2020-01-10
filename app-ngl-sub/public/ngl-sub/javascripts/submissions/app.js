"use strict";
angular.module('home', ['ngRoute', 'commonsServices', 'ultimateDataTableServices','ui.bootstrap', 'ngl-sub.SubmissionsServices'], 
	function($routeProvider, $locationProvider) {
	
	$routeProvider.when('/sra/submissions/create/home', {
		// url qui va appeler controler java de type tpl
		templateUrl : '/tpl/sra/submissions/create',
		controller : 'CreateCtrl'
	});
	
	$routeProvider.when('/sra/submissions/:code', {
		// url qui va appeler controler java de type tpl
		templateUrl : '/tpl/sra/submissions/details',
		controller : 'DetailsCtrl'
	});
	
	$routeProvider.when('/sra/submissions/activate/home', {
		// url qui va appeler controler java de type tpl
		templateUrl : '/tpl/sra/submissions/activate',
		controller : 'ActivateCtrl'
	});
	
	$routeProvider.when('/sra/submissions/consultation/home', {
		// url qui va appeler controler java de type tpl
		templateUrl : '/tpl/sra/submissions/consultation',
		controller : 'ConsultationCtrl'
	});	
	
	
	$routeProvider.when('/sra/submissions/validation/home', {
		// url qui va appeler controler java de type tpl
		templateUrl : '/tpl/sra/submissions/validation',
		controller : 'ValidationCtrl'
	});		
	
	$routeProvider.otherwise({redirectTo: '/sra/submissions/create/home'});

	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
});

