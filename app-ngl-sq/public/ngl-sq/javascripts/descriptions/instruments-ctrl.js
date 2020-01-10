"use strict"
angular.module('home').controller('InstrumentsCtrl', ['$scope','$location','$routeParams','$filter', 'datatable','lists','mainService','tabService', "descriptionsSearchService", 
	                                     function($scope,  $location,  $routeParams,  $filter,   datatable,  lists,  mainService,  tabService, descriptionsSearchService) {
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
				by:'traceInformation.creationDate',
				reverse :true,
				mode:'local'
			},
			group:{
				active:false,
				showOnlyGroups:false,
				showButton:false
			},
			hide:{
		 		 active:true
		 	},
			edit:{
				active:false
			},
			exportCSV:{
				active:false
			},
			mergeCells: {
				active: true
			}
	};

	$scope.changeTab=function(index){
		descriptionsSearchService.changeTab(index);
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

	$scope.searchService = descriptionsSearchService;
	$scope.searchService.init($routeParams, $scope.datatableConfig);

	$scope.searchService.search();
		
}]);
