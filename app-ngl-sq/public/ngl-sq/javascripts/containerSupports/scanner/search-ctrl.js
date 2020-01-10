"use strict";

angular.module('home').controller('SearchScan', ['$scope','barCodeSearchService',
                                                   function($scope,barCodeSearchService) {
	
	
	// Initialisation of the Service
	$scope.searchService = barCodeSearchService;
	
	$scope.intercept = function(event){
		if(event.keyCode === 9){ //tab event
			$scope.searchService.search()
			event.preventDefault();
		}
		//scope.setTextareaNgModel()				
	};
	
	angular.element("#scan").focus();
}]);