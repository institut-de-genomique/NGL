"use strict"
angular.module('home').controller('ExperimentsCtrl', ['$scope','$location','$routeParams','$filter', 'datatable','lists','mainService','tabService', "descriptionsExperimentsSearchService", 
	                                     function($scope,  $location,  $routeParams,  $filter,   datatable,  lists,  mainService,  tabService, descriptionsExperimentsSearchService) {
	
		handleHomePage();
		setDatatableConfig();
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
				mainService.setHomePage('experiments');
			}
	
			function setTabs(){
				tabService.addTabs(controllerTabs());
				tabService.activeTab(0);
	
				//---
	
				function controllerTabs(){
					return {
						label:Messages('descriptions.tabs.experiments'),
						href: jsRoutes.controllers.descriptions.tpl.Descriptions.home("experiments").url,
						remove:true
					};
				}
			}
	
		}
	
		function setDatatableConfig(){
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
					extraHeaders: true,
				},
				mergeCells: {
					active: true
				},
				extraHeaders:{
					number:1,
					dynamic:true,
				}
			};
		}
	
		function setSearchService(){
			$scope.searchService = descriptionsExperimentsSearchService;
		}
	
		function setResetsForm(){
			$scope.resetTypesForm = function(){
				$scope.searchService.resetTypesForm();
			};
		
			$scope.resetPropertiesForm = function(){
				$scope.searchService.resetPropertiesForm();
			};
	
			$scope.resetInstrumentsForm = function(){
				$scope.searchService.resetInstrumentsForm();
			};

			$scope.resetAtmsForm = function(){
				$scope.searchService.resetAtmsForm();
			};

			$scope.resetPreviousForm = function(){
				$scope.searchService.resetPreviousForm();
			};

			$scope.resetCrossTableForm = function(){
				$scope.searchService.resetCrossTableForm();
			};
		}
	
		function initSearchService(){
			$scope.searchService.init($routeParams, $scope.datatableConfig);
		}
		
}]);
