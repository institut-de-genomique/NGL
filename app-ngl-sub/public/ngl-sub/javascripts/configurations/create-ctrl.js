"use strict";

angular.module('home').controller('CreateCtrl',[ '$http', '$scope', '$routeParams' , 'mainService', 'lists', 'tabService','configurationsCreateService','messages',
                                                 function($http, $scope, $routeParams, mainService, lists, tabService, configurationsCreateService, messages) { 
  
	
	$scope.messages = messages();
	
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('create');
		tabService.addTabs({label:Messages('configurations.menu.create'),href:jsRoutes.controllers.sra.configurations.tpl.Configurations.home("create").url,remove:true});
		tabService.activeTab(0); // desactive le lien !
	}
	
	$scope.createService = configurationsCreateService;	
	$scope.createService.init($routeParams);
	
	$scope.save = function(){
		mainService.setForm($scope.createService.form);
			$http.post(jsRoutes.controllers.sra.configurations.api.Configurations.save().url, mainService.getForm()).success(function(data) {
				$scope.messages.clazz="alert alert-success";
				$scope.messages.text=Messages('configurations.msg.save.success')+" : "+data;
				$scope.messages.open();
			}).error(function(data){
				$scope.messages.setDetails(data);
				$scope.messages.setError("save");
			});
	};
	

	
	$scope.reset = function(){
		$scope.createService.resetForm();
		$scope.messages.clear();
	};
		
	
}]);

