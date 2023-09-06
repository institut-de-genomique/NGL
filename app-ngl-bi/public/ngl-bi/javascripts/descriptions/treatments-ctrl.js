"use strict"
angular.module('home').controller('TreatmentsCtrl', ['$scope','$location','$routeParams','$filter', 'datatable','lists','mainService','tabService', "descriptionsTreatmentsSearchService", 
	                                     function($scope,  $location,  $routeParams,  $filter,   datatable,  lists,  mainService,  tabService, descriptionsTreatmentsSearchService) {
	$scope.datatableConfig = {	
			show:{
				active:true
			},
			pagination:{
				mode:'local',
				numberRecordsPerPage: 50,
			},
			order :{
				mode:'local'
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
		mainService.setHomePage('treatments');
		tabService.addTabs({label:Messages('descriptions.tabs.treatments'),href:jsRoutes.controllers.descriptions.tpl.Descriptions.home("treatments").url,remove:true});
		tabService.activeTab(0);
	}
	if(angular.isUndefined($scope.getForm())){
		$scope.form = {};
		mainService.setForm($scope.form);
	}else{
		$scope.form = mainService.getForm();
	}

	$scope.searchService = descriptionsTreatmentsSearchService;
	$scope.searchService.init($routeParams, $scope.datatableConfig);
		
}]);
