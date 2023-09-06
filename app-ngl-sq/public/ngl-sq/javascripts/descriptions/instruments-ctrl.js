"use strict"
angular.module('home').controller('InstrumentsCtrl', ['$scope','$location','$routeParams','$filter', 'datatable','lists','mainService','tabService', "descriptionsInstrumentsSearchService", 
	                                     function($scope,  $location,  $routeParams,  $filter,   datatable,  lists,  mainService,  tabService, descriptionsInstrumentsSearchService) {

	handleHomePage();
	setSearchService();
	setResetsForm();

	//---

	function handleHomePage(){
		if(isHomePageDefined()) return;
		defineHomePage();
		setTabs();

		//---

		function isHomePageDefined(){
			return angular.isDefined($scope.getHomePage());
		}

		function defineHomePage(){
			mainService.setHomePage('instruments');
		}

		function setTabs(){
			tabService.addTabs(controllerTabs());
			tabService.activeTab(0);

			//---

			function controllerTabs(){
				return {
					label:Messages('descriptions.tabs.instruments'),
					href:defaultTabUrl(),
					remove:true
				};
			}
	
			function defaultTabUrl(){
				return jsRoutes.controllers.descriptions.tpl.Descriptions.home("instruments").url;
			}
		}

	}

	$scope.setIsAdmin = function(isAdmin) {
		$scope.isAdmin=isAdmin;

		setDatatableConfig();
		initSearchService();
	};

	function setDatatableConfig(){
		
		$scope.datatableConfig = {	
			order:{
				by:'category.name',
				reverse :true,
				mode:'local'
			},
			pagination:{
				mode:'local',
				numberRecordsPerPage: 50
			},
			select: {
				active: true,
				showButton: true,
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
			exportCSV:{
				active: true,
				showButton: true
			},
			mergeCells: {
				active: true
			}
		};
	}
	
	function setSearchService(){
		$scope.searchService = descriptionsInstrumentsSearchService;
	}

	function setResetsForm(){
		$scope.resetCategoriesForm = function(){
			$scope.searchService.resetCategoriesForm();
		};

		$scope.resetInstrumentsForm = function(){
			$scope.searchService.resetInstrumentsForm();
		};
	
		$scope.resetPropertiesForm = function(){
			$scope.searchService.resetPropertiesForm();
		};
	}

	function initSearchService(){
		$scope.searchService.init($routeParams, $scope.datatableConfig, $scope.isAdmin);
		$scope.searchService.search();
	}

		
}]);
