"use strict"
angular.module('home').controller('PropertyDefinitonsCtrl', ['$scope','$location','$routeParams','$filter', 'datatable','lists','mainService','tabService', "descriptionsPropertyDefinitionsSearchService", 
	                                     function($scope,  $location,  $routeParams,  $filter,   datatable,  lists,  mainService,  tabService, descriptionsPropertyDefinitionsSearchService) {
	$scope.datatableConfig = {	
			show:{
				active:true
			},
			search:{
				url:jsRoutes.controllers.commons.api.PropertyDefinitions.list()
				
			},
			pagination:{
				mode:'local',
				numberRecordsPerPage: 25,
			},
			select: {
				active: false,
				showButton: false,
			},
			group:{
				active:false,
				showOnlyGroups:false,
				showButton:false
			},
			show: {
				active: false,
				showButton: false,
			},
			hide: {
				active: true,
				showButton: true,
			},
			cancel: {
				active: false,
				showButton: false
			},
			edit:{
				active:false
			},
			order:{
				active:false
			},
			exportCSV:{
				active: true,
				showButton: true,
			},
			mergeCells: {
				active: true
			}
	};

	$scope.search = function(){
		$scope.searchService.search();
	};

	$scope.reset = function(){
		$scope.searchService.resetForm();
	};
	
	//init
	if(angular.isUndefined($scope.getHomePage())){
		mainService.setHomePage('instruments');
		tabService.addTabs({label:Messages('descriptions.tabs.property.definitions'),href:jsRoutes.controllers.descriptions.tpl.Descriptions.home("propertydefinitions").url,remove:true});
		tabService.activeTab(0);
	}
	if(angular.isUndefined($scope.getForm())){
		$scope.form = {objectTypeCode: "Import"};
		mainService.setForm($scope.form);
	}else{
		$scope.form = mainService.getForm();		
	}

	$scope.searchService = descriptionsPropertyDefinitionsSearchService;
	$scope.searchService.init($routeParams, $scope.datatableConfig);

	$scope.searchService.search();
		
}]);
