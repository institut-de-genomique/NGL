"use strict";

angular.module('home', ['commonsServices','ngRoute','ngl-sq.barCodeSearchServices'], 
		function($routeProvider, $locationProvider){
	
	// configure html5 to get links working with bookmarked
	$locationProvider.html5Mode({enabled: true, requireBase: false});
	
});