"use strict";

angular.module('home').controller('SearchCtrl',[ '$scope', '$routeParams', 'mainService', 'tabService', 'readSetsSearchService', 'valuationService',
                                                 function($scope, $routeParams, mainService, tabService, readSetsSearchService, valuationService) { 
    
	var datatableConfig = {
			group:{active:true},
			pagination:{mode:'local'},
			order :{mode:'local', by:'runSequencingStartDate', reverse : true},
			search:{
				url:jsRoutes.controllers.readsets.api.ReadSets.list()
			},
			show:{
				active:true,
				add :function(line){
					tabService.addTabs({label:line.code,href:jsRoutes.controllers.readsets.tpl.ReadSets.get(line.code).url,remove:true});
				}
			},
			hide:{
				active:true
			},
			exportCSV:{
				active:true
			},
			name:"Readsets"
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
		tabService.addTabs({label:Messages('readsets.page.tab.search'),href:jsRoutes.controllers.readsets.tpl.ReadSets.home("search").url,remove:true});
		tabService.activeTab(0); // active l'onglet, le met en bleu
	}
	
	$scope.searchService = readSetsSearchService;
	$scope.searchService.init($routeParams, datatableConfig)
	$scope.valuationService = valuationService();
	
	if($scope.searchService.isRouteParam){
		$scope.search();
	}	
	
}]);




angular.module('home').controller('SearchValuationCtrl', ['$scope', '$routeParams', '$parse', 'mainService', 'tabService', 'readSetsSearchService', 'valuationService', 
                                                          function($scope, $routeParams, $parse, mainService, tabService, readSetsSearchService, valuationService) {
	var datatableConfig = {
			pagination:{mode:'local'},
			order:{mode:'local', by:'runSequencingStartDate', reverse : true},
			search:{
				url:jsRoutes.controllers.readsets.api.ReadSets.list()
			},
			edit:{
				active:Permissions.check("writing")?true:false,
				columnMode:true		    	
			},			
			save:{
				active:Permissions.check("writing")?true:false,
				url: jsRoutes.controllers.readsets.api.ReadSets.valuationBatch().url,				
				batch:true,
				method:'put',
				value:function(line){return {code:line.code,productionValuation:line.productionValuation,bioinformaticValuation:line.bioinformaticValuation};}				
			},
			show:{
				active:true,
				add :function(line){
					tabService.addTabs({label:line.code,href:jsRoutes.controllers.readsets.tpl.ReadSets.valuation(line.code).url,remove:true});
				}
			},
			hide:{
				active:true
			},
			exportCSV:{
				active:true
			},
			messages:{active:true}
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
		tabService.addTabs({label:Messages('readsets.page.tab.validate'),href:jsRoutes.controllers.readsets.tpl.ReadSets.home("valuation").url,remove:true});
		tabService.activeTab(0); // active l'onglet, le met en bleu
	}
	
	$scope.searchService = readSetsSearchService;
	$scope.searchService.init($routeParams, datatableConfig)
	
	$scope.valuationService = valuationService();
	
	$scope.search();
	
}]);


angular.module('home').controller('SearchStateCtrl', ['$scope', '$routeParams', 'mainService', 'tabService', 'readSetsSearchService', 
                                                      function($scope, $routeParams, mainService, tabService, readSetsSearchService) {

	var datatableConfig = {
			pagination:{mode:'local'},
			order :{mode:'local', by:'runSequencingStartDate', reverse : true},
			search:{
				url:jsRoutes.controllers.readsets.api.ReadSets.list()
			},
			edit : {
				active:Permissions.check("writing")?true:false,
				columnMode:true		    	
			},
			save : {
				active:Permissions.check("writing")?true:false,
				url: jsRoutes.controllers.readsets.api.ReadSets.updateStateBatch().url,				
				batch:true,
				method:'put',
				value:function(line){return {code:line.code,state:line.state};}				
			},
			show:{
				active:true,
				add :function(line){
					tabService.addTabs({label:line.code,href:jsRoutes.controllers.readsets.tpl.ReadSets.get(line.code).url,remove:true});
				}
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
		mainService.setHomePage('state');
		tabService.addTabs({label:Messages('readsets.page.tab.state'),href:jsRoutes.controllers.readsets.tpl.ReadSets.home("state").url,remove:true});
		tabService.activeTab(0); // active l'onglet, le met en bleu
	}
	
	$scope.searchService = readSetsSearchService;
	$scope.searchService.init($routeParams, datatableConfig)
			
}]);


angular.module('home').controller('SearchBatchCtrl', ['$scope', '$routeParams', 'mainService', 'tabService', 'readSetsSearchService', 
                                                      function($scope, $routeParams, mainService, tabService, readSetsSearchService) {

	var datatableConfig = {
			pagination:{mode:'local'},
			order :{mode:'local', by:'runSequencingStartDate', reverse : true},
			search:{
				url:jsRoutes.controllers.readsets.api.ReadSets.list()
			},
			exportCSV:{
				active:true
			},
			group:{
				active:true
			},
			edit : {
				active:Permissions.check("writing")?true:false,
				columnMode:true		    	
			},
			save : {
				active:Permissions.check("writing")?true:false,
				url: jsRoutes.controllers.readsets.api.ReadSets.propertiesBatch().url,				
				batch:true,
				method:'put',
				value:function(line){return {code:line.code, properties : line.properties};}				
			},	
			show:{
				active:true,
				add :function(line){
					$scope.addTabs({label:line.code,href:jsRoutes.controllers.readsets.tpl.ReadSets.get(line.code).url,remove:true});
				}
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
		mainService.setHomePage('batch');
		tabService.addTabs({label:Messages('readsets.page.tab.batch'),href:jsRoutes.controllers.readsets.tpl.ReadSets.home("batch").url,remove:true});
		tabService.activeTab(0); // active l'onglet, le met en bleu
	}
	
	$scope.searchService = readSetsSearchService;
	$scope.searchService.init($routeParams, datatableConfig)
			
	
}]);
