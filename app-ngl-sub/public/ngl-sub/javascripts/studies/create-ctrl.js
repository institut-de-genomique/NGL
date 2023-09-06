"use strict";

angular.module('home').controller('CreateCtrl',[ '$http', '$scope', '$routeParams' , 'mainService', 'lists', 'tabService','studiesCreateService','messages',
                                                 function($http, $scope, $routeParams, mainService, lists, tabService, studiesCreateService, messages) { 
  
	
	$scope.messages = messages();
	$scope.messages.clear();
	
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('create');
		tabService.addTabs({label:Messages('studies.menu.create'),href:jsRoutes.controllers.sra.studies.tpl.Studies.home("create").url,remove:true});
		tabService.activeTab(0); //  active l'onglet en le mettant en bleu
	}
	
	$scope.createService = studiesCreateService;	
	$scope.createService.init($routeParams);
	
	$scope.save = function(){
		$scope.messages = messages();
		$scope.messages.clear();
		// Pas de saisie utilisateur de la description qui est mise à la valeur de l'abstract:
		$scope.createService.form.description = $scope.createService.form.studyAbstract;
		mainService.setForm($scope.createService.form);
		$http.post(jsRoutes.controllers.sra.studies.api.Studies.save().url, mainService.getForm())
		.success(function(data) {
				$scope.messages.clazz="alert alert-success";
				$scope.messages.text=Messages('studies.msg.save.success')+" : "+data.code;
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


