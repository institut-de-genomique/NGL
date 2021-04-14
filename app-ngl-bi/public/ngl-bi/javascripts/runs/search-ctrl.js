"use strict";

angular.module('home').controller('SearchCtrl', ['$scope', '$routeParams', 'mainService', 'tabService', 'runSearchService', 'valuationService', 
                                                 function($scope, $routeParams, mainService, tabService, runSearchService, valuationService) {
	var datatableConfig = {
			group:{active:true},
			order :{mode:'local', by:'sequencingStartDate', reverse:true},
			search:{
				url:jsRoutes.controllers.runs.api.Runs.list()
			},
			pagination:{
				mode:'local'
			},
			show:{
				active:true,
				add :function(line){
					tabService.addTabs({label:line.code,href:jsRoutes.controllers.runs.tpl.Runs.get(line.code).url,remove:true});
				}
			},
			hide:{active:true},
			exportCSV:{
				active:true
			},
			name:"Runs"
	};
	
	$scope.search = function(){
		$scope.searchService.search();
	};
	
	$scope.reset = function(){
		$scope.searchService.resetForm();
		$scope.searchService.resetTextareas();
	};
	
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('search');
		tabService.addTabs({label:Messages('runs.page.tab.search'),href:jsRoutes.controllers.runs.tpl.Runs.home("search").url,remove:true});
		tabService.activeTab(0); // active l'onglet, le met en bleu
	}
	
	$scope.searchService = runSearchService;	
	$scope.searchService.init($routeParams, datatableConfig)
	$scope.valuationService = valuationService();
	
	if($scope.searchService.isRouteParam){
		$scope.search();
	}
}]);


angular.module('home').controller('SearchValuationCtrl', ['$scope', '$routeParams', 'mainService', 'tabService', 'runSearchService',
                                                          function($scope, $routeParams, mainService, tabService, runSearchService) {
	var datatableConfig = {
			order :{mode:'local', by:'sequencingStartDate', reverse:true},
			search:{
				url:jsRoutes.controllers.runs.api.Runs.list()
			},
			pagination:{
				mode:'local'
			},
			show:{
				active:true,
				add :function(line){
					tabService.addTabs({label:line.code,href:jsRoutes.controllers.runs.tpl.Runs.valuation(line.code).url,remove:true});
				}
			},
			hide:{active:true}
	};
	
	$scope.search = function(){
		$scope.searchService.search();
	};
	
	$scope.reset = function(){
		$scope.searchService.resetForm();
		$scope.searchService.resetTextareas();
	};
	
	
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('valuation');
		tabService.addTabs({label:Messages('runs.page.tab.validate'),href:jsRoutes.controllers.runs.tpl.Runs.home("valuation").url,remove:true});
		tabService.activeTab(0); // active l'onglet, le met en bleu
	}
	
	$scope.searchService = runSearchService;	
	$scope.searchService.init($routeParams, datatableConfig)
	
	$scope.search();
	
}]);

angular.module('home').controller('SearchStateCtrl', ['$scope', '$routeParams', 'mainService', 'tabService', 'runSearchService',
                                                      function($scope, $routeParams, mainService, tabService, runSearchService) {

	var datatableConfig = {
			order :{mode:'local', by:'sequencingStartDate', reverse:true},
			search:{
				url:jsRoutes.controllers.runs.api.Runs.list()
			},
			pagination:{
				mode:'local'
			},
			edit : {
				active:Permissions.check("writing")?true:false,
				columnMode:true		    	
			},
			save : {
				active:Permissions.check("writing")?true:false,
				url: jsRoutes.controllers.runs.api.States.updateStateBatch().url,				
				batch:true,
				method:'put',
				value:function(line){return {code:line.code,state:line.state};}				
			},
			
			show:{
				active:true,
				add :function(line){
					tabService.addTabs({label:line.code,href:jsRoutes.controllers.runs.tpl.Runs.get(line.code).url,remove:true});
				}
			},
			hide:{active:true},
			messages : {active:true}
	};
	
	$scope.search = function(){
		$scope.searchService.search();
	};
	
	$scope.reset = function(){
		$scope.searchService.resetForm();
		$scope.searchService.resetTextareas();
	};
	
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('state');
		tabService.addTabs({label:Messages('runs.page.tab.state'),href:jsRoutes.controllers.runs.tpl.Runs.home("state").url,remove:true});
		tabService.activeTab(0); // active l'onglet, le met en bleu
	}
	
	$scope.searchService = runSearchService;	
	$scope.searchService.init($routeParams, datatableConfig)
	
		
}]);