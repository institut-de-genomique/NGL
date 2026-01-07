"use strict"
angular.module('home').controller('SearchCtrl', ['$scope','$location','$routeParams','$filter', 'datatable','lists','mainService','tabService','experimentsSearchService', 'projectSearchFromUmbrella', 
	                                     function($scope,  $location,  $routeParams,  $filter,   datatable,  lists,  mainService,  tabService,  experimentsSearchService, projectSearchFromUmbrella) {
	$scope.datatableConfig = {	
			show:{
				active:true,
				add :function(line){
					tabService.addTabs({label:line.code,href:jsRoutes.controllers.experiments.tpl.Experiments.get(line.code).url,remove:true});
				}
			},
			search:{
				url:jsRoutes.controllers.experiments.api.Experiments.list()
				
			},
			pagination:{
				mode:'local'
			},
			order:{
				by:'traceInformation.creationDate',
				reverse :true,
				mode:'local'
			},
			group:{
				active:true,
				showOnlyGroups:true,
				showButton:true
			},
			hide:{
		 		 active:true
		 	},
			edit:{
				active:false
			},
			exportCSV:{
				active:true
			}
	};

	$scope.selectProjetsFromUmbrellas = function() {
		var projCodes = projectSearchFromUmbrella.search($scope.searchService.form.umbrellaCodes);
		$scope.searchService.form.projectCodes = projCodes;
	}
	
	$scope.search = function(){		
		$scope.searchService.search();
	};
	
	$scope.reset = function(){
		$scope.searchService.resetForm();		
		$scope.searchService.resetTextareas();
	};
	
	
	$scope.changeContainerSupportCode = function(val){
		console.log(val);
		return $scope.searchService.changeContainerSupportCode(val);		 
	};

	$scope.changeExperimentType = function(){
		$scope.searchService.changeExperimentType();
	};
	
	$scope.changeProcessType = function(){
		$scope.searchService.changeProcessType();
	};
	
	$scope.changeProcessCategory = function(){
		$scope.searchService.changeProcessCategory();
	};
	
	//init
	if(angular.isUndefined($scope.getHomePage())){
		mainService.setHomePage('search');
		tabService.addTabs({label:Messages('experiments.tabs.search'),href:jsRoutes.controllers.experiments.tpl.Experiments.home("search").url,remove:true});
		tabService.activeTab(0);
	}
	if(angular.isUndefined($scope.getForm())){
		$scope.form = {};
		mainService.setForm($scope.form);
	}else{
		$scope.form = mainService.getForm();		
	}
	
	$scope.searchService = experimentsSearchService;
	$scope.searchService.init($routeParams, $scope.datatableConfig);	
	
	
	if($scope.searchService.isRouteParam){
		$scope.search();
	}	
}]);



"use strict"
angular.module('home').controller('SearchReagentsCtrl', ['$scope', '$http', '$q', '$routeParams', 'datatable', 'experimentsSearchService', 'mainService', 'tabService', 'projectSearchFromUmbrella', 
                                                 function($scope,   $http,   $q,   $routeParams,   datatable,   experimentsSearchService,   mainService,   tabService, projectSearchFromUmbrella){
	$scope.datatableConfig = {
			name:"experimentReagents",
			search:{
				active:true,
				url:jsRoutes.controllers.experiments.api.ExperimentReagents.list()
			},
			order:{
				active:true,
				mode:'local'
			},
	        pagination:{
	        	mode:'local'
	        },	
			show:{
				active:true,
				add :function(line){
					tabService.addTabs({label:line.code,href:jsRoutes.controllers.experiments.tpl.Experiments.get(line.code).url,remove:true});
				}
			},
			edit:{
				active:false
			},
			hide:{
				active:false
			},
			exportCSV:{
				active:true,
				showButton:true,
				delimiter:";"
			},
			compact:true
	};

	$scope.selectProjetsFromUmbrellas = function() {
		var projCodes = projectSearchFromUmbrella.search($scope.searchService.form.umbrellaCodes);
		$scope.searchService.form.projectCodes = projCodes;
	}

	
	$scope.search = function(){
		$scope.searchService.search();
	};
	
	$scope.reset = function(){
		$scope.searchService.resetForm();
		$scope.searchService.resetTextareas();
	}
	
	
	//init	
	if(angular.isUndefined($scope.getHomePage())){
		mainService.setHomePage('reagents');
		tabService.addTabs({label:Messages('experiments.tabs.reagents'),href:jsRoutes.controllers.experiments.tpl.Experiments.home("reagents").url,remove:true});
		tabService.activeTab(0);
	}
	
	if(angular.isUndefined($scope.getForm())){
		$scope.form = {};
		mainService.setForm($scope.form);
	}else{
		$scope.form = mainService.getForm();		
	}
	
	$scope.searchService = experimentsSearchService;
	$scope.searchService.init($routeParams, $scope.datatableConfig);

}]);