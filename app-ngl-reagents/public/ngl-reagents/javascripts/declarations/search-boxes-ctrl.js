"use strict"

angular.module('home').controller('SearchBoxesCtrl', ['$scope', 'datatable','lists','$filter','mainService','tabService','boxesSearchService','$routeParams', function($scope, datatable, lists,$filter,mainService,tabService,boxesSearchService,$routeParams) {
	$scope.datatableConfig = {
		search:{
			url:jsRoutes.controllers.reagents.api.Boxes.list()
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
				tabService.addTabs({label:line.barCode,href:jsRoutes.controllers.reagents.tpl.Kits.get(line.kitCode).url,remove:true});
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
		mainService.setHomePage('boxes.new');
		tabService.addTabs({label:Messages('boxes.tabs.search'),href:jsRoutes.controllers.reagents.tpl.Boxes.home("search").url,remove:true});
		tabService.activeTab(0);
	}
	
	if(angular.isUndefined($scope.getForm())){
		$scope.form = {};
		mainService.setForm($scope.form);
	}
	
	$scope.searchService = boxesSearchService;
	$scope.searchService.init($routeParams, $scope.datatableConfig)
}]);