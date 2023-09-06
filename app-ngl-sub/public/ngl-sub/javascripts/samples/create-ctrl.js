"use strict";

angular.module('home').controller('CreateCtrl',[ '$http', '$scope', '$routeParams' , 'mainService', 'lists', 'tabService','datatable','messages',
                                                 function($http, $scope, $routeParams, mainService, lists, tabService, datatable, messages) { 

	// Initialisation :

	$scope.messages = messages();
	$scope.messages.clear();
	$scope.form = {};
	$scope.lists = lists;
	$scope.treeLoadInProgress = false;
	$scope.form._type = "Sample"; // on cree uniquement des Samples via cette page et jamais d'ExternalSample
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('create');
		tabService.addTabs({label:Messages('samples.menuEnBref.create'),href:jsRoutes.controllers.sra.samples.tpl.Samples.home("create").url,remove:true});
		tabService.activeTab(0); //  active l'onglet en le mettant en bleu
	}
	
	//	Initialisation ProjectCodes:
	$scope.lists.refresh.projects();
	
	// NGL-4235 
	$scope.isShowConsigne=false;
	$scope.toggleShowConsigne=function(){
		//console.log("TOGGLE search-ctrl.js");
		if ( $scope.isShowConsigne===false) { $scope.isShowConsigne=true}
		else {$scope.isShowConsigne=false}
	}
	
	$scope.save = function(){
		$scope.messages = messages();	
		$scope.messages.clear();
		$scope.treeLoadInProgress = true;
		$scope.form._type = "Sample"; // on cree uniquement des Samples via cette page et jamais d'ExternalSample
		
		console.log("scope.form=", $scope.form);
		mainService.setForm($scope.form);
		console.log("mainService.getForm=", mainService.getForm());
			$http.post(jsRoutes.controllers.sra.samples.api.Samples.save().url, mainService.getForm()).success(function(data) {
		        $scope.treeLoadInProgress = false;
				$scope.messages = messages();
				$scope.messages.clear();
				$scope.messages.clazz="alert alert-success";
				$scope.messages.text="Sauvegarde réussie du sample : '" + data.code + "'";
				$scope.messages.open();
				$scope.codeSample = data.code;
		        $scope.resetUserData();
			}).error(function(data){
		        $scope.treeLoadInProgress = false;
				//$scope.messages.setDetails({"error":["code":"value","code2":"value2"]});
		        //console.log("data=", data);
				$scope.messages.setDetails(data);
				$scope.messages.setError("save");

			});
	};
	
	$scope.resetUserData = function(){
		$scope.treeLoadInProgress = false;
		$scope.form = {}; // on initialise à null toutes les variables recuperees dans create-ctrl.js dans code : ng-model="form
		$scope.form._type="Sample";
	};		
	
	$scope.reset = function(){
		$scope.treeLoadInProgress = false;
		$scope.messages = messages();	
		$scope.messages.clear();
		$scope.resetUserData();
	};	
	
}]);


