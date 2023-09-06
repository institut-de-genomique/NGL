"use strict";

angular.module('home', [
	'commonsServices','ngRoute','ultimateDataTableServices','ui.bootstrap', 'ngl-sq.descriptionsServices.instruments', 
	'ngl-sq.descriptionsServices.imports', 'ngl-sq.descriptionsServices.protocols', 'ngl-sq.descriptionsServices.experiments',
	'ngl-sq.descriptionsServices.mappingprojects', 'ngl-sq.descriptionsServices.processes',
	])
	.config(
	['$routeProvider', '$locationProvider', function($routeProvider, $locationProvider) {
	
    $routeProvider.when('/descriptions/instruments/home', {
		templateUrl : function(params){return jsRoutes.controllers.descriptions.tpl.Descriptions.instruments().url},
		controller : 'InstrumentsCtrl'
	});

	$routeProvider.when('/descriptions/imports/home', {
		templateUrl : function(params){return jsRoutes.controllers.descriptions.tpl.Descriptions.imports().url},
		controller : 'ImportsCtrl'
	});

	$routeProvider.when('/descriptions/protocols/home', {
		templateUrl : function(params){return jsRoutes.controllers.descriptions.tpl.Descriptions.protocols().url},
		controller : 'ProtocolsCtrl'
	});
	$routeProvider.when('/descriptions/protocols/:code', {
		templateUrl : jsRoutes.controllers.descriptions.tpl.Descriptions.detailsProtocol().url,
		controller : 'DetailsCtrl'
	});

	$routeProvider.when('/descriptions/experiments/home', {
		templateUrl : function(params){return jsRoutes.controllers.descriptions.tpl.Descriptions.experiments().url},
		controller : 'ExperimentsCtrl'
	});

	$routeProvider.when('/descriptions/mappingprojects/home', {
		templateUrl : function(params){return jsRoutes.controllers.descriptions.tpl.Descriptions.mappingProjects().url},
		controller : 'MappingProjectsCtrl'
	});

	$routeProvider.when('/descriptions/mappingprojects/new/:typeCode/:tabId', {
		templateUrl : function(params){return jsRoutes.controllers.descriptions.tpl.Descriptions.newMappingProjects().url},
		controller : 'NewMappingProjectsCtrl'
	});

	$routeProvider.when('/descriptions/processes/home', {
		templateUrl : function(params){return jsRoutes.controllers.descriptions.tpl.Descriptions.processes().url},
		controller : 'ProcessesCtrl'
	});

    $routeProvider.otherwise({redirectTo: jsRoutes.controllers.descriptions.tpl.Descriptions.home("instruments").url});

	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
}]);
