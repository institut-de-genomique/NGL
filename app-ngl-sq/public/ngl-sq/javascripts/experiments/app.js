"use strict";
 
angular.module('home', ['ngRoute','ultimateDataTableServices','basketServices', 'commonsServices','valuationServices','ui.bootstrap','atomicTransfereServices','dragndropServices','ngl-sq.experimentsServices','tools','toolsHelpers']).config(['$routeProvider', '$locationProvider', '$compileProvider', function ($routeProvider, $locationProvider, $compileProvider) {
	$routeProvider.when('/experiments/new/home', {
		templateUrl : jsRoutes.controllers.experiments.tpl.Experiments.searchContainers().url,
		controller : 'SearchContainersCtrl'
	});
	
	$routeProvider.when('/experiments/search/home', {
		templateUrl : function(params){return jsRoutes.controllers.experiments.tpl.Experiments.search("home").url},
		controller : 'SearchCtrl'
	});
	
	$routeProvider.when('/experiments/reagents/home', {
		templateUrl : function(params){return jsRoutes.controllers.experiments.tpl.Experiments.search("reagents").url},
		controller : 'SearchReagentsCtrl'
	});
	
	$routeProvider.when('/experiments/graph/home', {
		templateUrl : function(params){return jsRoutes.controllers.experiments.tpl.Experiments.graph().url},
		controller : 'SearchGraphCtrl'
	});
	
	$routeProvider.when('/experiments/:code', {
		templateUrl : function(params){return jsRoutes.controllers.experiments.tpl.Experiments.details().url},
		controller : 'DetailsCtrl'
	});
	
	$routeProvider.when('/experiments/:code/:typeCode', {
		templateUrl : function(params){return jsRoutes.controllers.experiments.tpl.Experiments.details().url},
		controller : 'DetailsCtrl'
	});
	
	//surement à enlever les 2 car ne voit pas l'usage ????
	$routeProvider.when('/experiments/search/:experimentTypeCode', {
		templateUrl : function(params){return jsRoutes.controllers.experiments.tpl.Experiments.search(params.experimentTypeCode).url},
		controller : 'SearchCtrl'
	});
	
	
	$routeProvider.when('/experiments/:newExperiment/home', {
		templateUrl : function(params){return jsRoutes.controllers.experiments.tpl.Experiments.searchSupports(params.newExperiment).url},
		controller : 'SearchContainerCtrl'
	});
	
	$routeProvider.otherwise({redirectTo: '/experiments/search/home'});

	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({
		  enabled: true,
		  requireBase: false
		});

	$compileProvider.aHrefSanitizationWhitelist(/^\s*(https?|mailto|data):/);

}]);