"use strict"
angular.module('home').controller('ProtocolsCtrl', ['$scope','$http','$window','$location','$routeParams','$filter', 'datatable','lists','mainService','tabService', "descriptionsProtocolsSearchService", 
	                                     function($scope,$http,$window, $location,  $routeParams,  $filter,   datatable,  lists,  mainService,  tabService, descriptionsProtocolsSearchService) {

			$scope.onInit = function(isAdmin){
				setAdmin(isAdmin);
				initSearchService();

				function setAdmin (isAdmin) {
					$scope.isAdmin=isAdmin;
				};
			}
			
				
											
	$scope.datatableConfig = {	
		

			search:{
				url:jsRoutes.controllers.protocols.api.Protocols.list()
				
			},
			pagination:{
				mode:'local',
				numberRecordsPerPage: 25,
			},
			
			group:{
				active:false,
				showOnlyGroups:false,
				showButton:false
			},
			
			
			cancel: {
				active: true,
				showButton: true
			},
	
			hide: {
				active: true,
				showButton: true,
			},

			order:{
				by:'code',
				reverse :false,
				mode:'local'
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

	
	if(angular.isUndefined($scope.getHomePage())){
		mainService.setHomePage('instruments');
		tabService.addTabs({label:Messages('descriptions.tabs.protocols'),href:jsRoutes.controllers.descriptions.tpl.Descriptions.home("protocols").url,remove:true});
		tabService.activeTab(0);
	}
	if(angular.isUndefined($scope.getForm())){
		mainService.setForm($scope.form);
	}else{
		$scope.form = mainService.getForm();		
	}

	$scope.searchService = descriptionsProtocolsSearchService;

	function initSearchService(){
		$scope.searchService.init($routeParams, $scope.datatableConfig, $scope.isAdmin);
		$scope.searchService.search();
	}
	$scope.createProtocol = function() {
		tabService.addTabs({label:"Création Protocole",href:("/descriptions/protocols/new"), remove:true});
		tabService.activeTab(0);
	}

}]);
