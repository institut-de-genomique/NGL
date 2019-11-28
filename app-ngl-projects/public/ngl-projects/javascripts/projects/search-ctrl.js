"use strict";

angular.module('home').controller('SearchCtrl',['$scope', '$routeParams','datatable', 'mainService', 'tabService', 'projectsSearchService',
                                                function($scope, $routeParams, datatable,mainService,tabService,projectsSearchService) {
	
	var datatableConfig = {
			order :{mode:'local', by:'code', reverse:false},
			search:{
				url:jsRoutes.controllers.projects.api.Projects.list()
			},
			show:{
				active:true,
				add :function(line){
					tabService.addTabs({label:line.code,href:jsRoutes.controllers.projects.tpl.Projects.get(line.code).url, remove:true});
				}
			},
			exportCSV:{
				active:true
			},
			pagination:{
				mode:'local'
			},
			name:"Projects"
	};

	$scope.search = function(){
		$scope.searchService.search();
	};
	
	$scope.reset = function(){
		$scope.searchService.reset();
	};
	
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('search');
		tabService.addTabs({label:Messages('projects.menu.search'),href:jsRoutes.controllers.projects.tpl.Projects.home("search").url,remove:true});
		tabService.activeTab(0); // desactive le lien !
	}
	
	$scope.searchService = projectsSearchService;	
	$scope.searchService.init($routeParams, datatableConfig);
	
	if($scope.searchService.isRouteParam){
		$scope.search();
	}

	
}]);

