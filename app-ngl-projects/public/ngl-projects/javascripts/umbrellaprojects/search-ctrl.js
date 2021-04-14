angular.module('home').controller('SearchCtrl', ['$scope', '$routeParams', 'datatable', 'mainService', 'tabService', 'searchService', 
  function($scope, $routeParams, datatable, mainService, tabService, searchService) {
	
	var datatableConfig = {
			order :{mode:'local', by:'code', reverse:false},
			search:{
				url:jsRoutes.controllers.projects.api.UmbrellaProjects.list()
			},
			show:{
				active:true,
				add :function(line){
					tabService.addTabs({label:line.code,href:jsRoutes.controllers.projects.tpl.UmbrellaProjects.get(line.code).url, remove:true});
				}
			}
	};
	
	$scope.search = function(){
		$scope.searchService.search($scope.datatable);
	};
	
	$scope.reset = function(){
		$scope.searchService.reset();
	};

	
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('search');
		tabService.addTabs({label:Messages('umbrellaProjects.menu.search'), href:jsRoutes.controllers.projects.tpl.UmbrellaProjects.home("search").url, remove:true});
		tabService.activeTab(0); // active l'onglet, le met en bleu
	}
	
	$scope.searchService = searchService();	
	$scope.searchService.setRouteParams($routeParams);
	
	//to avoid to lost the previous search
	if(angular.isUndefined(mainService.getDatatable())){
		$scope.datatable = datatable($scope, datatableConfig);			
		mainService.setDatatable($scope.datatable);
		$scope.datatable.setColumnsConfig($scope.searchService.getColumns());
	}else{
		$scope.datatable = mainService.getDatatable();
	}
	$scope.search();
	
	
}]);

