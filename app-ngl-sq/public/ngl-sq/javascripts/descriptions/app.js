"use strict";

angular.module('home', ['commonsServices','ngRoute','ultimateDataTableServices','ui.bootstrap', 'ngl-sq.descriptionsServices.instruments', 
	'ngl-sq.descriptionsServices.propertydefinitions', 'ngl-sq.descriptionsServices.protocols', 'ngl-sq.descriptionsServices.experiments']).config(
	['$routeProvider', '$locationProvider', function($routeProvider, $locationProvider) {
	
    $routeProvider.when('/descriptions/instruments/home', {
		templateUrl : function(params){return jsRoutes.controllers.descriptions.tpl.Descriptions.instruments().url},
		controller : 'InstrumentsCtrl'
	});

	$routeProvider.when('/descriptions/propertydefinitions/home', {
		templateUrl : function(params){return jsRoutes.controllers.descriptions.tpl.Descriptions.propertyDefinitions().url},
		controller : 'PropertyDefinitonsCtrl'
	});

	$routeProvider.when('/descriptions/protocols/home', {
		templateUrl : function(params){return jsRoutes.controllers.descriptions.tpl.Descriptions.protocols().url},
		controller : 'ProtocolsCtrl'
	});

	$routeProvider.when('/descriptions/experiments/home', {
		templateUrl : function(params){return jsRoutes.controllers.descriptions.tpl.Descriptions.experiments().url},
		controller : 'ExperimentsCtrl'
	});

	$routeProvider.when('/descriptions/mappingprojects/home', {
		templateUrl : function(params){return jsRoutes.controllers.descriptions.tpl.Descriptions.mappingProjects().url},
		controller : 'MappingProjectsCtrl'
	});
    
    $routeProvider.otherwise({redirectTo: jsRoutes.controllers.descriptions.tpl.Descriptions.home("instruments").url});

	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
}]);
