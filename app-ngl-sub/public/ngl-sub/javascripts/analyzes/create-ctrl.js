"use strict";

angular.module('home').controller('CreateCtrl',[ '$http', '$scope', '$routeParams' , 'mainService', 'lists', 'tabService','datatable','messages',
                                                 function($http, $scope, $routeParams, mainService, lists, tabService, datatable, messages) { 

	// Initialisation :

	$scope.messages = messages();
	$scope.messages.clear();
	$scope.form = {};
	$scope.lists = lists;
	$scope.treeLoadInProgress = false;

	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('create');
		tabService.addTabs({label:Messages('analyzes.menu.create'),href:jsRoutes.controllers.sra.analyzes.tpl.Analyzes.home("create").url,remove:true});
		tabService.activeTab(0); //  active l'onglet en le mettant en bleu
	}
	
	//	Initialisation ProjectCodes:
	$scope.lists.refresh.projects();
	

	$scope.save = function(){
		$scope.messages = messages();	
		$scope.messages.clear();
		$scope.treeLoadInProgress = true;
		// important si le fichier utilisateur ne peut pas ou ne doit pas etre chargé que form.base64File soit
		// mis à chaine vide et non à null pour l'appel de l'url sra/api/submissions
		//console.log("$scope.userRefFileCollabToAc : '" + $scope.userRefFileCollabToAc + "'");
//		console.log("typeof $scope.userRefFileCollabToAc : '" + typeof $scope.userRefFileCollabToAc + "'");
//		console.log("typeof undefined : '" + typeof undefined + "'");
		


		console.log("scope.form=", $scope.form);
		mainService.setForm($scope.form);
		console.log("mainService.getForm=", mainService.getForm());
		//$scope.search();
			$http.post(jsRoutes.controllers.sra.analyzes.api.Analyzes.save().url, mainService.getForm()).success(function(data) {
		        $scope.treeLoadInProgress = false;
				$scope.messages = messages();
				$scope.messages.clear();
				$scope.messages.clazz="alert alert-success";
				$scope.messages.text="Sauvegarde réussie de l'analyse : '" + data.code + "'";
				$scope.messages.open();
				$scope.codeAnalysis = data.code;
		        $scope.resetUserData();
			}).error(function(data){
		        $scope.treeLoadInProgress = false;
				//$scope.messages.setDetails({"error":["code":"value","code2":"value2"]});
		        //console.log("data=", data);
				$scope.messages.setDetails(data);
				$scope.messages.setError("save");
				//$scope.resetUserData();
				//$scope.messages.clear();
				//angular.element('#idUserFileReadSet')[0].value = null;
				//angular.element('#idUserFileRefCollabToAc')[0].value = null;
			});
	};
	
	$scope.resetUserData = function(){
		$scope.treeLoadInProgress = false;
		$scope.form = {}; // on initialise à null toutes les variables recuperees dans create-ctrl.js dans code : ng-model="form
		$scope.acStudy=null;
		$scope.acSample=null;
	};		
	
	$scope.reset = function(){
		$scope.treeLoadInProgress = false;
		$scope.messages = messages();	
		$scope.messages.clear();
		$scope.resetUserData();
	};	
	
}]);


