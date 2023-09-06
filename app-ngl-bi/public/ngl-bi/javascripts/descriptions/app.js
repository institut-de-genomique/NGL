"use strict";

angular.module('home', ['commonsServices','ngRoute','ultimateDataTableServices','ui.bootstrap', 'ngl-sq.descriptionsServices.treatments', 'ngl-bi.descriptionsServices.types'], function($routeProvider, $locationProvider) {
	
    $routeProvider.when('/descriptions/treatments/home', {
		templateUrl : function(params){return jsRoutes.controllers.descriptions.tpl.Descriptions.treatments().url},
		controller : 'TreatmentsCtrl'
	});

	$routeProvider.when('/descriptions/types/home', {
		templateUrl : function(params){return jsRoutes.controllers.descriptions.tpl.Descriptions.types().url},
		controller : 'TypesCtrl'
	});

	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
});