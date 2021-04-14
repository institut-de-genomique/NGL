"use strict"
angular.module('home').controller('ExperimentsCtrl', ['$scope','$location','$routeParams','$filter', 'datatable','lists','mainService','tabService', "descriptionsExperimentsSearchService", 
	                                     function($scope,  $location,  $routeParams,  $filter,   datatable,  lists,  mainService,  tabService, descriptionsExperimentsSearchService) {
	$scope.datatableConfig = {	
			show:{
				active:true
			},
			search:{
				url:jsRoutes.controllers.experiments.api.ExperimentTypes.list()
				
			},
			pagination:{
				mode:'local',
				numberRecordsPerPage: 50,
			},
			order:{
				by:'atomicTransfertMethod',
				reverse :false,
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
			},
			extraHeaders:{
				number:1,
				dynamic:true,
			}
	};

	$scope.changeTab=function(index){
		$scope.searchService.changeTab(index);
    }
	
	//init
	if(angular.isUndefined($scope.getHomePage())){
		mainService.setHomePage('experiments');
		tabService.addTabs({label:Messages('descriptions.tabs.experiments'),href:jsRoutes.controllers.descriptions.tpl.Descriptions.home("experiments").url,remove:true});
		tabService.activeTab(0);
	}
	if(angular.isUndefined($scope.getForm())){
		$scope.form = {};
		mainService.setForm($scope.form);
	}else{
		$scope.form = mainService.getForm();		
	}

	$scope.searchService = descriptionsExperimentsSearchService;
	$scope.searchService.init($routeParams, $scope.datatableConfig);

	$scope.searchService.search();
		
}]);
