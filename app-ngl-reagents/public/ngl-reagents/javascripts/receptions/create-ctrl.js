"use strict";

angular.module('home').controller('CreationCtrl',[ '$scope', '$routeParams', 'mainService', 'tabService', 'reagentReceptionsCreationService',
                                                 function($scope, $routeParams, mainService, tabService, reagentReceptionsCreationService) { 
	
	$scope.reset = function(){
		$scope.createService.resetForm();
		$scope.createService.resetTextareas();
	};
	
	$scope.save = function(){
		$scope.createService.save();					
	};
	
	$scope.cancel = function(){
		$scope.createService.cancel();			
	};
	
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('search');
		tabService.addTabs({label:Messages('reagent.receptions.page.tab.create'),href:jsRoutes.controllers.reagents.tpl.Receptions.home("search").url,remove:true});
		tabService.activeTab(0);
	}
	
	$scope.createService = reagentReceptionsCreationService;
	$scope.createService.init($routeParams)	
	
}]);
