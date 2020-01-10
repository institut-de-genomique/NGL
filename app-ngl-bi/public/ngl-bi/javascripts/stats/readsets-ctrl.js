"use strict";

angular.module('home').controller('StatsChoiceCtrl',['$scope', '$location','$modal', 'mainService', '$http',
                                                             function($scope, $location, $modal, mainService, $http) { 

	
	$http.get(jsRoutes.controllers.stats.api.StatsConfigurations.list().url,{params:{count:true}}).success(function(data, status, headers, config) {
		var nb = data.result;
		if(nb > 0){
			var modalInstance = $modal.open({
			      templateUrl: 'myChoice.html',
			      backdrop : 'static',
			      controller : 'ModalChoiceCtrl',
			    });
			
			modalInstance.result.then(function (value) {
			     	mainService.put('choice', value);
			     	if(value === 'manual'){
			     		$location.url(jsRoutes.controllers.stats.tpl.Stats.home("readsets-search").url);
			     	}else{
			     		$location.url(jsRoutes.controllers.stats.tpl.Stats.home("readsets-show").url);
			     	}
			    });
		}else{
			mainService.put('choice', 'manual');
			$location.url(jsRoutes.controllers.stats.tpl.Stats.home("readsets-search").url);
		}
	});
	
	
	
	//we used jquery to open the modal
}]);

angular.module('home').controller('ModalChoiceCtrl', function ($scope, $modalInstance) {
	$scope.choice = function(value){
		$modalInstance.close(value);
	};	 
});
	
angular.module('home').controller('StatsSearchReadSetsCtrl',['$scope', '$routeParams', 'mainService', 'tabService','readSetsSearchService', 'valuationService','queriesConfigReadSetsService',
                                                              function($scope, $routeParams, mainService, tabService, readSetsSearchService, valuationService, queriesConfigReadSetsService) { 

	var datatableConfig = {
			group: {
				active : true
			},
			pagination:{mode:'local'},
			order :{mode:'local',by:'runSequencingStartDate', reverse : true},
			search:{
				url:jsRoutes.controllers.readsets.api.ReadSets.list()
			},
			hide:{
				active:true
			},
			otherButtons:{
				active:true,
				template:'<button class="btn btn-default" ng-click="addToBasket()" data-toggle="tooltip" title="'+Messages("button.query.addbasket")+'"><i class="fa fa-shopping-cart"></i> (<span ng-bind="queriesConfigService.queries.length"/>)</button>'
			}
	};

	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('search');
		tabService.addTabs({label:Messages('stats.page.tab.readsets.select'),href:jsRoutes.controllers.stats.tpl.Stats.home("readsets-search").url});
		tabService.addTabs({label:Messages('stats.page.tab.readsets.config'),href:jsRoutes.controllers.stats.tpl.Stats.home("readsets-config").url});		
		tabService.addTabs({label:Messages('stats.page.tab.show'),href:jsRoutes.controllers.stats.tpl.Stats.home("readsets-show").url});		
		
		tabService.activeTab(0); // desactive le lien !
	}
	
	$scope.search = function(){
		$scope.searchService.search();
	};
	
	$scope.reset = function(){
		$scope.searchService.resetForm();
	};

		
	$scope.addToBasket = function(){
		var query = {form : angular.copy($scope.searchService.convertForm())};
		query.form.includes = undefined;
		query.form.excludes = undefined;
		$scope.queriesConfigService.addQuery(query);			
	};
	
	$scope.searchService = readSetsSearchService;	
	$scope.searchService.init($routeParams, datatableConfig);
	$scope.valuationService = valuationService();
		
	$scope.queriesConfigService = queriesConfigReadSetsService;
	
}]);

angular.module('home').controller('StatsConfigReadSetsCtrl',['$scope', 'mainService', 'tabService', 'basket', 'statsConfigReadSetsService','queriesConfigReadSetsService',
                                                              function($scope, mainService, tabService, basket, statsConfigReadSetsService, queriesConfigReadSetsService) { 
	
	if(angular.isUndefined(mainService.getBasket())){
		mainService.setBasket(basket());
	}
	
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('search');
		tabService.addTabs({label:Messages('stats.page.tab.readsets.select'),href:jsRoutes.controllers.stats.tpl.Stats.home("readsets-search").url});
		tabService.addTabs({label:Messages('stats.page.tab.readsets.config'),href:jsRoutes.controllers.stats.tpl.Stats.home("readsets-config").url});		
		tabService.addTabs({label:Messages('stats.page.tab.show'),href:jsRoutes.controllers.stats.tpl.Stats.home("readsets-show").url});		
		tabService.activeTab(1); // desactive le lien !
	}
	$scope.statsConfigService = statsConfigReadSetsService;
	$scope.queriesConfigService = queriesConfigReadSetsService;
		
	$scope.queriesConfigService.loadDatatable();	
}]);

angular.module('home').controller('StatsShowReadSetsCtrl',['$scope', '$routeParams', 'mainService', 'tabService', 'chartsReadSetsService',
                                                              function($scope, $routeParams, mainService, tabService, chartsReadSetsService) { 
	
	$scope.isManual = function(){
		return (mainService.get('choice') === 'manual')
	};
	
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('search');
		if($scope.isManual()){
			tabService.addTabs({label:Messages('stats.page.tab.readsets.select'),href:jsRoutes.controllers.stats.tpl.Stats.home("readsets-search").url});
			tabService.addTabs({label:Messages('stats.page.tab.readsets.config'),href:jsRoutes.controllers.stats.tpl.Stats.home("readsets-config").url});		
			tabService.addTabs({label:Messages('stats.page.tab.show'),href:jsRoutes.controllers.stats.tpl.Stats.home("readsets-show").url});		
			tabService.activeTab(2); // desactive le lien !
		}else{
			tabService.addTabs({label:Messages('stats.page.tab.show'),href:jsRoutes.controllers.stats.tpl.Stats.home("readsets-show").url});		
			tabService.activeTab(0);
		}
	}
	
	
	
	$scope.chartsReadSetsService = chartsReadSetsService;
	$scope.chartsReadSetsService.init();
	
}]);
