"use strict";

angular.module('home').controller('SearchCtrl',['$scope', '$routeParams','datatable', 'mainService', 'tabService', 'projectsSearchService',
                                                function($scope, $routeParams, datatable,mainService,tabService,projectsSearchService) {
	
	var datatableConfig = {
			order :{mode:'local', by:'traceInformation.creationDate', reverse:true},
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
			hide:{
				active:true
			},
			edit:{
				active:Permissions.check("writing")?true:false,
				columnMode:true
			},
			save:{
				active:Permissions.check("writing")?true:false,
				url:function(line){return jsRoutes.controllers.projects.api.Projects.update(line.code).url;},
				mode:'remote',
				method:'put',
			},
			group:{
				active:true
			},
			name:"Projects"
	};

	$scope.search = function(){
		console.log("appel search");
		$scope.searchService.search();
	};
	
	$scope.reset = function(){
		$scope.searchService.resetForm();
		$scope.searchService.resetTextareas();
	};
	
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('search');
		tabService.addTabs({label:Messages('projects.menu.search'),href:jsRoutes.controllers.projects.tpl.Projects.home("search").url,remove:true});
		tabService.activeTab(0); // active l'onglet, le met en bleu
	}
	
	$scope.searchService = projectsSearchService;	
	$scope.searchService.init($routeParams, datatableConfig);
	
	if($scope.searchService.isRouteParam){
		$scope.search();
	}
}]);

