angular.module('home').controller('SearchCtrl', ['$scope', '$routeParams', 'mainService', 'tabService', 'searchService', 
  function($scope, $routeParams, mainService, tabService, searchService) {
	
	var datatableConfig = {
		order :{mode:'local', by:'traceInformation.creationDate', reverse:true},
			search:{
				url:jsRoutes.controllers.projects.api.UmbrellaProjects.list()
			},
			show:{
				active:true,
				add :function(line){
					tabService.addTabs({label:line.name,href:jsRoutes.controllers.projects.tpl.UmbrellaProjects.get(line.code).url, remove:true});
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
			save:{
				active:Permissions.check("writing")?true:false,
				url:function(line){return jsRoutes.controllers.projects.api.UmbrellaProjects.update(line.code).url;},
				mode:'remote',
				method:'put',
			},
			group:{
				active:true
			},
			name:"UmbrellaProjects"
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
		tabService.addTabs({label:Messages('umbrellaProjects.menu.search'), href:jsRoutes.controllers.projects.tpl.UmbrellaProjects.home("search").url, remove:true});
		tabService.activeTab(0); // active l'onglet, le met en bleu
	}

	$scope.searchService = searchService;	
	$scope.searchService.init($routeParams, datatableConfig);
	
	if($scope.searchService.isRouteParam){
		$scope.search();
	}
}]);

