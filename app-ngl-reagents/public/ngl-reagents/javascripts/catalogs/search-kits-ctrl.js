"use strict"

angular.module('home').controller('SearchKitsCtrl', ['$scope', 'datatable','lists','$filter','mainService','tabService','kitCatalogsSearchService','$routeParams', function($scope, datatable, lists,$filter,mainService,tabService,kitCatalogsSearchService,$routeParams) {
	$scope.datatableConfig = {
		search:{
			url:jsRoutes.controllers.reagents.api.KitCatalogs.list()
		},
		order:{
			by:'name',
			reverse:true
		},
		edit:{
			active:false
		},
		save:{
			active:false
		},
		show : {
			active:true,
			showButton : true,
			add:function(line){
				console.log(jsRoutes.controllers.reagents.tpl.KitCatalogs.get(line.code));
				tabService.addTabs({label:line.name,href:jsRoutes.controllers.reagents.tpl.KitCatalogs.get(line.code).url,remove:true});
			}
		}
	};

	$scope.search = function(){		
		$scope.searchService.search();
	};
	
	$scope.reset = function(){
		$scope.searchService.resetForm();		
	};
	
	
	//init
	$scope.datatable = datatable($scope.datatableConfig);		
	if(angular.isUndefined($scope.getHomePage())){
		mainService.setHomePage('new');
		tabService.addTabs({label:Messages('kitCatalogs.tabs.search'),href:jsRoutes.controllers.reagents.tpl.KitCatalogs.home("search").url,remove:true});
		tabService.activeTab(0);
	}
	if(angular.isUndefined($scope.getForm())){
		$scope.form = {};
		mainService.setForm($scope.form);
	}
	
	$scope.searchService = kitCatalogsSearchService;
	$scope.searchService.init($routeParams, $scope.datatableConfig)
}]);