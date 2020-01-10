"use strict";

angular.module('home').controller('StatsSearchLanesCtrl',['$scope', '$routeParams', '$location','$modal', 'mainService', '$http','tabService','runSearchService','chartsLanesService',
                                                             function($scope, $routeParams, $location, $modal, mainService, $http, tabService, runSearchService, chartsLanesService) { 

	
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('search');
		tabService.addTabs({label:Messages('stats.page.tab.lanes.select'),href:jsRoutes.controllers.stats.tpl.Stats.home("lanes").url});
		tabService.addTabs({label:Messages('stats.page.tab.show'),href:jsRoutes.controllers.stats.tpl.Stats.home("lanes-show").url});		
		
		tabService.activeTab(0); // desactive le lien !
	}
	
	var datatableConfig = {
			group:{active:true},
			order :{mode:'local', by:'sequencingStartDate', reverse:true},
			search:{
				url:jsRoutes.controllers.runs.api.Runs.list()
			},
			pagination:{
				mode:'local'
			},
			hide:{active:true},
			exportCSV:{
				active:true
			},
			 callbackEndDisplayResult : function(){
				 chartsLanesService.loadData(); 
			 },
			name:"Runs"
	};
	
	$scope.search = function(){
		$scope.searchService.search();
	};
	
	$scope.reset = function(){
		$scope.searchService.resetForm();
	};
	
	
	$scope.searchService = runSearchService;	
	$scope.searchService.init($routeParams, datatableConfig)
	
}]);


angular.module('home').controller('StatsShowLanesCtrl',['$scope', '$routeParams', 'mainService', 'tabService', 'chartsLanesService',
                                                              function($scope, $routeParams, mainService, tabService, chartsLanesService) { 
	
	
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('search');
			tabService.addTabs({label:Messages('stats.page.tab.lanes.select'),href:jsRoutes.controllers.stats.tpl.Stats.home("lanes").url});
			//tabService.addTabs({label:Messages('stats.page.tab.lanes.config'),href:jsRoutes.controllers.stats.tpl.Stats.home("lanes-config").url});		
			tabService.addTabs({label:Messages('stats.page.tab.show'),href:jsRoutes.controllers.stats.tpl.Stats.home("lanes-show").url});			
			tabService.activeTab(0); // desactive le lien !
		}
	
	
	$scope.chartsLanesService = chartsLanesService;
	$scope.chartsLanesService.init();
	
}]);
