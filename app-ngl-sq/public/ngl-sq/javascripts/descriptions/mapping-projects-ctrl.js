"use strict"
angular.module('home').controller('MappingProjectsCtrl', ['$scope','$location','$routeParams','$filter', 'datatable','lists','mainService','tabService', "descriptionsMappingProjectsSearchService", 
	                                     function($scope,  $location,  $routeParams,  $filter,   datatable,  lists,  mainService,  tabService, descriptionsMappingProjectsSearchService) {
	$scope.datatableConfig = {	
			show:{
				active:true
			},
			order:{
				by:'parentCode',
				reverse :false,
				mode:'local'
			},
			pagination:{
				mode:'local',
				numberRecordsPerPage: 50,
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
			exportCSV:{
				active: true,
				showButton: true,
			}
	};

	$scope.changeTab=function(index){
		$scope.searchService.changeTab(index);
    }

	$scope.getTabs=function(){
		return Object.keys($scope.searchService.sublists);
    }
	
	//init
	if(angular.isUndefined($scope.getHomePage())){
		mainService.setHomePage('instruments');
		tabService.addTabs({label:Messages('descriptions.tabs.mapping.projects'),href:jsRoutes.controllers.descriptions.tpl.Descriptions.home("mappingprojects").url,remove:true});
		tabService.activeTab(0);
	}
	if(angular.isUndefined($scope.getForm())){
		$scope.form = {};
		mainService.setForm($scope.form);
	}else{
		$scope.form = mainService.getForm();		
	}

	$scope.searchService = descriptionsMappingProjectsSearchService;
	$scope.searchService.init($routeParams, $scope.datatableConfig);

	$scope.searchService.search();
		
}]);
