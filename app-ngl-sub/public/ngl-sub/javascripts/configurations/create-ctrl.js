"use strict";

angular.module('home').controller('CreateCtrl',[ '$http', '$scope', '$routeParams' , 'mainService', 'lists', 'tabService','configurationsCreateService','messages',
                                                 function($http, $scope, $routeParams, mainService, lists, tabService, configurationsCreateService, messages) { 
  
	$scope.messages = messages();	// enleve le message mais pas la partie details + efficace d'utiliser $scope.messages.clear();
	$scope.messages.clear();
	
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('create');
		tabService.addTabs({label:Messages('configurations.menu.create'),href:jsRoutes.controllers.sra.configurations.tpl.Configurations.home("create").url,remove:true});
		tabService.activeTab(0); //  active l'onglet en le mettant en bleu
	}
	
	$scope.createService = configurationsCreateService;	
	$scope.createService.init($routeParams);
	
	$scope.save = function(){
		$scope.messages = messages();	// enleve le message mais pas la partie details + efficace d'utiliser $scope.messages.clear();
        $scope.messages.clear();
		mainService.setForm($scope.createService.form);
			$http.post(jsRoutes.controllers.sra.configurations.api.Configurations.save().url, mainService.getForm()).success(function(data) {
				$scope.messages.clazz="alert alert-success";
				//console.log(data);
				$scope.messages.text=Messages('configurations.msg.save.success')+" : "+data.code;
				$scope.messages.open();
				$scope.createService.resetForm();
			}).error(function(data){
				$scope.messages.setDetails(data);
				$scope.messages.setError("save");
				$scope.createService.resetForm();
			});
	};
	

	
	$scope.reset = function(){
		$scope.createService.resetForm();
		$scope.messages = messages();
		$scope.messages.clear();
	};
		
	
}]);

