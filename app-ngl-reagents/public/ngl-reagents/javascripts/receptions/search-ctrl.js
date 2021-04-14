"use strict";

angular.module('home').controller('SearchCtrl',[ '$scope', '$routeParams', 'mainService', 'tabService', 'reagentReceptionsSearchService',
                                                 function($scope, $routeParams, mainService, tabService, reagentReceptionsSearchService) { 
    
	var datatableConfig = {
			order :{
				mode:'local'
			},
			pagination:{
				mode:'local'
			},
			search:{
				url:jsRoutes.controllers.reagents.api.Receptions.list()
			},
			hide:{
				active:true
			},
			edit: {
				active: true,
				columnMode: true
			},
			save: {
				active: true,
				url: function(value) {
					return jsRoutes.controllers.reagents.api.Receptions.update(value.code).url;
				},
				method: 'put',
				mode: 'remote',
				callback: function(datatable, error) {
					datatable.search(datatable.lastSearchParams);
				}
			},
			exportCSV:{
				active:true
			}
	};

	$scope.search = function(){
		$scope.searchService.search();
	};
	
	$scope.reset = function(){
		$scope.searchService.resetForm();
		$scope.searchService.resetTextareas();
	};
	
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('search');
		tabService.addTabs({label:Messages('reagent.receptions.page.tab.search'),href:jsRoutes.controllers.reagents.tpl.Receptions.home("search").url,remove:true});
		tabService.activeTab(0);
	}
	
	$scope.searchService = reagentReceptionsSearchService;
	$scope.searchService.init($routeParams, datatableConfig)
	
	if($scope.searchService.isRouteParam){
		$scope.search();
	}	
	
}]);
