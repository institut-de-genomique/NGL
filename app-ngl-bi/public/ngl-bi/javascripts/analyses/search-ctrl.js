"use strict";
angular.module('home').controller('SearchCtrl',['$scope', '$routeParams', 'datatable', 'mainService', 'tabService', 'analysisSearchService',  'valuationService',
                                                function($scope, $routeParams, datatable, mainService, tabService, analysisSearchService, valuationService) {
	
	var datatableConfig = {
			name:"Analyses",
			pagination:{mode:'local'},
			order :{mode:'local', by:'code', reverse : true},
			search:{
				url:jsRoutes.controllers.analyses.api.Analyses.list()
			},
			show:{
				active:true,
				add :function(line){
					tabService.addTabs({label:line.code,href:jsRoutes.controllers.analyses.tpl.Analyses.get(line.code).url,remove:true});
				}
			},
			hide:{
				active:true
			},
			exportCSV:{
				active:true
			},
			// NGL-3741 ajout possibilité de group
			group:{
				active:true,
				showOnlyGroups:true,
				enableLineSelection:true,
				showButton:true
			}
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
		tabService.addTabs({label:Messages('analyses.page.tab.search'),href:jsRoutes.controllers.analyses.tpl.Analyses.home("search").url,remove:true});
		tabService.activeTab(0); // active l'onglet, le met en bleu
	}
	
	$scope.searchService = analysisSearchService;	
	$scope.searchService.init($routeParams, datatableConfig);
	
	$scope.valuationService = valuationService();
	
	if($scope.searchService.isRouteParam){
		$scope.search();
	}
}]);

angular.module('home').controller('SearchValuationCtrl', ['$scope', '$routeParams', 'datatable', 'mainService', 'tabService', 'analysisSearchService', 'valuationService',
                                                          function($scope, $routeParams, datatable, mainService, tabService, analysisSearchService, valuationService) {
	
	var datatableConfig = {
			pagination:{mode:'local'},
			order :{mode:'local', by:'code', reverse : true},
			search:{
				url:jsRoutes.controllers.analyses.api.Analyses.list()
			},
			edit : {
				active:Permissions.check("writing")?true:false,
				columnMode:true		    	
			},			
			save : {
				active:Permissions.check("writing")?true:false,
				url: jsRoutes.controllers.analyses.api.Analyses.valuationBatch().url,				
				batch:true,
				method:'put',
				value:function(line){return {code:line.code,valuation:line.valuation};}				
			},
			show:{
				active:true,
				add :function(line){
					tabService.addTabs({label:line.code,href:jsRoutes.controllers.analyses.tpl.Analyses.get(line.code).url,remove:true});
				}
			},
			hide:{
				active:true
			},
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
		mainService.setHomePage('valuation');
		tabService.addTabs({label:Messages('analyses.page.tab.validate'),href:jsRoutes.controllers.analyses.tpl.Analyses.home("valuation").url,remove:true});
		tabService.activeTab(0); // active l'onglet, le met en bleu
	}
	
	$scope.searchService = analysisSearchService;	
	$scope.searchService.init($routeParams, datatableConfig);
	
	$scope.valuationService = valuationService();
	
	$scope.search();
}]);

angular.module('home').controller('SearchStateCtrl', ['$scope', '$routeParams', 'datatable', 'mainService', 'tabService', 'analysisSearchService', 
                                                      function($scope, $routeParams, datatable, mainService, tabService, analysisSearchService) {
	
	var datatableConfig = {
			pagination:{mode:'local'},
			order :{mode:'local', by:'code', reverse : true},
			search:{
				url:jsRoutes.controllers.analyses.api.Analyses.list()
			},
			edit : {
				active:Permissions.check("writing")?true:false,
				columnMode:true		    	
			},			
			save : {
				active:Permissions.check("writing")?true:false,
				url: jsRoutes.controllers.analyses.api.Analyses.updateStateBatch().url,				
				batch:true,
				method:'put',
				value:function(line){return {code:line.code,state:line.state};}				
			},
			show:{
				active:true,
				add :function(line){
					tabService.addTabs({label:line.code,href:jsRoutes.controllers.analyses.tpl.Analyses.get(line.code).url,remove:true});
				}
			},
			hide:{
				active:true
			},
			messages : {active:true}
	};

	$scope.search = function(){
		$scope.searchService.search($scope.datatable);
	};
	
	$scope.reset = function(){
		$scope.searchService.resetForm();
		$scope.searchService.resetTextareas();
	};
	
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('state');
		tabService.addTabs({label:Messages('analyses.page.tab.state'),href:jsRoutes.controllers.analyses.tpl.Analyses.home("state").url,remove:true});
		tabService.activeTab(0); // active l'onglet, le met en bleu
	}

	$scope.searchService = analysisSearchService;	
	$scope.searchService.init($routeParams, datatableConfig);
	
	if($scope.searchService.isRouteParam){
		$scope.search();
	}
}]);





