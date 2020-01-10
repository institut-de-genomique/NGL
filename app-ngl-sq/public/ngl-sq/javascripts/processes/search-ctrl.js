"use strict"
angular.module('home').controller('SearchCtrl', ['$scope','$location','$routeParams', 'datatable','lists','$filter','$http','mainService','tabService','processesSearchService', function($scope,$location,$routeParams, datatable, lists,$filter,$http,mainService,tabService,processesSearchService) {
	$scope.datatableConfig = {
			search:{
				url:jsRoutes.controllers.processes.api.Processes.list()
				
			},
			pagination:{
				mode:'local'
			},
			order:{
				by:'traceInformation.creationDate',
				reverse : true,
				mode:'local'
			},
			hide:{
				active:true
			},
			edit:{
				active:Permissions.check("writing")?true:false,
				columnMode:true
			},
			group:{
				active:true,
				showOnlyGroups:true
			},
			save:{
				active:Permissions.check("writing")?true:false,
				url:function(line){return jsRoutes.controllers.processes.api.Processes.update(line.code).url;},
				mode:'remote',
				method:'put',
			},
			exportCSV:{
				active:true,
			},
			objectsMustBeAddInGetFinalValue:{
				"searchService":processesSearchService
			}
	};

	$scope.reset = function(){
		$scope.searchService.resetForm();
	};
	
	$scope.search = function(){	
		$scope.searchService.search();
	};
	
	//init
	if(angular.isUndefined($scope.getHomePage())){
		mainService.setHomePage('new');
		tabService.addTabs({label:Messages('processes.tabs.search'),href:jsRoutes.controllers.processes.tpl.Processes.home("new").url,remove:true});
		tabService.activeTab(0);
	}
	
	if(angular.isUndefined($scope.getForm())){
		$scope.form = {};
		mainService.setForm($scope.form);
	}else{
		$scope.form = mainService.getForm();			
	}
	
	$scope.searchService = processesSearchService;
	$scope.searchService.init($routeParams, $scope.datatableConfig)
	
	if($scope.form.project || $scope.form.type || $scope.searchService.form.stateCodes){
		$scope.search();
	}
	
}]);

angular.module('home').controller('SearchStateCtrl', ['$scope','$location','$routeParams', 'datatable','lists','$filter','$http','mainService','tabService','processesSearchService', function($scope,$location,$routeParams, datatable, lists,$filter,$http,mainService,tabService,processesSearchService) {
	$scope.datatableConfig = {
			search:{
				url:jsRoutes.controllers.processes.api.Processes.list()
				
			},
			pagination:{
				mode:'local'
			},
			order:{
				by:'traceInformation.creationDate',
				reverse : true,
				mode:'local'
			},
			edit:{
				active:Permissions.check("writing")?true:false,
				columnMode:true
			},
			save:{
				active:Permissions.check("writing")?true:false,
				url:function(line){return jsRoutes.controllers.processes.api.Processes.updateState(line.code).url;},
				mode:'remote',
				method:'put',
				value:function(process){
					return process.state;
				}
			}
	};

	$scope.reset = function(){
		$scope.searchService.resetForm();
	};
	
	$scope.search = function(){	
		$scope.searchService.search();
	};
	
	//init
	if(angular.isUndefined($scope.getHomePage())){
		mainService.setHomePage('state');
		tabService.addTabs({label:Messages('processes.tabs.state'),href:jsRoutes.controllers.processes.tpl.Processes.home("new").url,remove:true});
		tabService.activeTab(0);
	}
	
	if(angular.isUndefined($scope.getForm())){
		$scope.form = {};
		mainService.setForm($scope.form);
	}else{
		$scope.form = mainService.getForm();			
	}
	
	$scope.searchService = processesSearchService;
	$scope.searchService.init($routeParams, $scope.datatableConfig)
	
	if($scope.form.project || $scope.form.type){
		$scope.search();
	}
	
	
}]);


angular.module('home').controller('SearchRemoveCtrl', ['$scope','$location','$routeParams', 'datatable','lists','$filter','$http','mainService','tabService','processesSearchService', function($scope,$location,$routeParams, datatable, lists,$filter,$http,mainService,tabService,processesSearchService) {
	$scope.datatableConfig = {
			search:{
				url:jsRoutes.controllers.processes.api.Processes.list()
				
			},
			pagination:{
				mode:'local'
			},
			order:{
				by:'traceInformation.creationDate',
				reverse : true,
				mode:'local'
			},
			edit:{
				active:false,
				columnMode:false
			},
			save:{
				active:false
			},
			remove:{
				active:Permissions.check("writing")?true:false,
				mode:"remote",
				url:function(line){
					return jsRoutes.controllers.processes.api.Processes.delete(line.code).url;
					}
			},
			exportCSV:{
				active:true,
			}
	};

	$scope.reset = function(){
		$scope.searchService.resetForm();
	};
	
	$scope.search = function(){	
		$scope.searchService.search();
	};
	
	//init
	if(angular.isUndefined($scope.getHomePage())){
		mainService.setHomePage('state');
		tabService.addTabs({label:Messages('processes.tabs.remove'),href:jsRoutes.controllers.processes.tpl.Processes.home("new").url,remove:false});
		tabService.activeTab(0);
	}
	
	if(angular.isUndefined($scope.getForm())){
		$scope.form = {};
		mainService.setForm($scope.form);
	}else{
		$scope.form = mainService.getForm();			
	}
	
	$scope.searchService = processesSearchService;
	$scope.searchService.init($routeParams, $scope.datatableConfig)
	
	if($scope.form.project || $scope.form.type){
		$scope.search();
	}
}]);