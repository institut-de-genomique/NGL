"use strict"
angular.module('home').controller('ImportsCtrl', ['$http', '$scope','$location','$routeParams','$filter', 'datatable','lists','mainService','tabService', "descriptionsImportsSearchService", 
	                                     function($http, $scope,  $location,  $routeParams,  $filter,   datatable,  lists,  mainService,  tabService, descriptionsImportsSearchService) {
	
	handleHomePage();
	setSearchService();
	setResetsForm();
	setDatatableConfig();
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
			mainService.setHomePage('imports');
		}

		function setTabs(){
			tabService.addTabs(controllerTabs());
			tabService.activeTab(0);

			//---

			function controllerTabs(){
				return {
					label:Messages('descriptions.tabs.imports'),
					href:defaultTabUrl(),
					remove:true
				};
			}
	
			function defaultTabUrl(){
				return jsRoutes.controllers.descriptions.tpl.Descriptions.home("imports").url;
			}
		}

	}

	function setDatatableConfig(){
		
		$scope.datatableConfig = {	
			show:{
				active:true
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
	}
	
	function setSearchService(){
		$scope.searchService = descriptionsImportsSearchService;
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