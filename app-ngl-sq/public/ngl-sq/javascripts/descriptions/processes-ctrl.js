"use strict"
angular.module('home').controller('ProcessesCtrl', ['$scope','$http', '$location','$routeParams','$filter', 'datatable','lists','mainService','tabService', "descriptionsProcessesSearchService", 
	                                     function($scope, $http, $location,  $routeParams,  $filter,   datatable,  lists,  mainService,  tabService, descriptionsProcessesSearchService) {

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
			mainService.setHomePage('processes');
		}

		function setTabs(){
			tabService.addTabs(controllerTabs());
			tabService.activeTab(0);

			//---

			function controllerTabs(){
				return {
					label:Messages('descriptions.tabs.processes'),
					href: jsRoutes.controllers.descriptions.tpl.Descriptions.home("processes").url,
					remove:true
				};
			}
		}

	}

	function setDattableConfig(){
		$scope.datatableConfig = {	
			show:{
				active:true
			},
			pagination:{
				mode:'local',
				numberRecordsPerPage: 50,
			},
			order:{
				by:'category.name',
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
	}

	function setSearchService(){
		$scope.searchService = descriptionsProcessesSearchService;
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
	}

		
}]);