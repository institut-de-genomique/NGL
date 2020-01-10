"use strict";

angular.module('home', ['commonsServices','ngRoute','ultimateDataTableServices','ui.bootstrap', 'ngl-sq.descriptionsServices'], function($routeProvider, $locationProvider) {
	
    $routeProvider.when('/descriptions/instruments/home', {
		templateUrl : function(params){return jsRoutes.controllers.descriptions.tpl.Descriptions.instruments().url},
		controller : 'InstrumentsCtrl'
	});
    
    $routeProvider.otherwise({redirectTo: jsRoutes.controllers.descriptions.tpl.Descriptions.home("instruments").url});

	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
});