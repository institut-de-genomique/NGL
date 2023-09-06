"use strict"
angular.module('home').controller('TypesCtrl', ['$scope','$location','$routeParams','$filter', 'datatable','lists','mainService','tabService', "descriptionsTypesSearchService", 
	                                     function($scope,  $location,  $routeParams,  $filter,   datatable,  lists,  mainService,  tabService, descriptionsTypesSearchService) {

	handleHomePage();
	setDattableConfig();
	setSearchService();
	setResetsForm();
	initSearchService();

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
			mainService.setHomePage('types');
		}

		function setTabs(){
			tabService.addTabs(controllerTabs());
			tabService.activeTab(0);

			//---

			function controllerTabs(){
				return {
					label:Messages('descriptions.tabs.types'),
					href:defaultTabUrl(),
					remove:true
				};
			}
	
			function defaultTabUrl(){
				return jsRoutes.controllers.descriptions.tpl.Descriptions.home("types").url;
			}
		}

	}

	function setDattableConfig(){
		$scope.datatableConfig = {	
			pagination:{
				mode:'local',
				numberRecordsPerPage: 50,
			},
			order :{
				mode:'local', 
				by:'objectType', 
				reverse : true
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
	}

	function setSearchService(){
		$scope.searchService = descriptionsTypesSearchService;
	}

	function setResetsForm(){
		$scope.resetTypesForm = function(){
			$scope.searchService.resetTypesForm();
		};
	
		$scope.resetPropertiesForm = function(){
			$scope.searchService.resetPropertiesForm();
		};
	}

	function initSearchService(){
		$scope.searchService.init($routeParams, $scope.datatableConfig);
		$scope.searchService.search();
	}

}]);
