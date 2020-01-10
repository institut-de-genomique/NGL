"use strict";

angular.module('home').controller('DetailsCtrl', ['$scope', '$http', '$routeParams', '$filter', 'messages', 'lists', 'mainService', 'tabService', 
                                                                                                    
  function($scope, $http, $routeParams, $filter, messages, lists, mainService, tabService) {
		
	$scope.form = {				
	}
	
	
	/* buttons section */
	$scope.update = function(){
		var objProj = angular.copy($scope.umbrellaProject);
		
		$http.put(jsRoutes.controllers.projects.api.UmbrellaProjects.update($routeParams.code).url, objProj).success(function(data) {
			$scope.messages.setSuccess("save");
			mainService.stopEditMode();
		}).error(function(data, status, headers, config){
			$scope.messages.setError("save");
		});
	};
	
	$scope.cancel = function(){
		$scope.messages.clear();
		updateData(true);				
	};
	
	var updateData = function(isCancel){
		$http.get(jsRoutes.controllers.projects.api.UmbrellaProjects.get($routeParams.code).url).success(function(data) {
			$scope.umbrellaProject = data;	
			$scope.stopEditMode();
		});
	};
	
	/* main section  */
	var init = function() {
		$scope.messages = messages();	
		$scope.mainService = mainService;
		$scope.mainService.stopEditMode();

		$http.get(jsRoutes.controllers.projects.api.UmbrellaProjects.get($routeParams.code).url).success(function(data) {
			$scope.umbrellaProject = data;		
		
			if(tabService.getTabs().length == 0){
				tabService.addTabs({label:Messages('projects.menu.search'), href:jsRoutes.controllers.projects.tpl.UmbrellaProjects.home("search").url, remove:true});
				tabService.addTabs({label:$scope.umbrellaProject.code, href:jsRoutes.controllers.projects.tpl.UmbrellaProjects.get($scope.umbrellaProject.code).url, remove:true});
				tabService.activeTab(tabService.getTabs(1));
			}
			
		});
		
	};
	
	init();	
}]);



angular.module('home').controller('AddCtrl', ['$scope', '$http', '$routeParams', 'messages', 'lists', 'mainService', 
  function($scope, $http, $routeParams, messages, lists, mainService) {
	
	$scope.form = {		
	}
	
	$scope.save = function(){
		var objUmbrellaProj = angular.copy($scope.form);
		
		$http.post(jsRoutes.controllers.projects.api.UmbrellaProjects.save().url, objUmbrellaProj).success(function(data) {
			$scope.messages.setSuccess("save");
		});
	};	
		
	var init = function(){
		$scope.messages = messages();
		
		if(angular.isUndefined(mainService.getHomePage())){
			mainService.setHomePage('add');
		}
	};
	
	init();
}]);