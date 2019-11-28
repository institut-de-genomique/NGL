"use strict"
angular.module('home').controller('InstrumentsCtrl', ['$scope','$location','$routeParams','$filter', 'datatable','lists','mainService','tabService', "descriptionsInstrumentsSearchService", 
	                                     function($scope,  $location,  $routeParams,  $filter,   datatable,  lists,  mainService,  tabService, descriptionsInstrumentsSearchService) {
	$scope.datatableConfig = {	
			show:{
				active:true
			},
			search:{
				url:jsRoutes.controllers.instruments.api.InstrumentUsedTypes.list()
				
			},
			pagination:{
				mode:'local',
				numberRecordsPerPage: 50,
			},
			order:{
				by:'code',
				reverse :true,
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

	$scope.changeTab=function(index){
		$scope.searchService.changeTab(index);
    }
	
	//init
	if(angular.isUndefined($scope.getHomePage())){
		mainService.setHomePage('instruments');
		tabService.addTabs({label:Messages('descriptions.tabs.instruments'),href:jsRoutes.controllers.descriptions.tpl.Descriptions.home("instruments").url,remove:true});
		tabService.activeTab(0);
	}
	if(angular.isUndefined($scope.getForm())){
		$scope.form = {};
		mainService.setForm($scope.form);
	}else{
		$scope.form = mainService.getForm();		
	}

	$scope.searchService = descriptionsInstrumentsSearchService;
	$scope.searchService.init($routeParams, $scope.datatableConfig);

	$scope.searchService.search();
		
}]);
