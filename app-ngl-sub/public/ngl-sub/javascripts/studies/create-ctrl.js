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
	$scope.treeLoadInProgress = false;
	
	$scope.isClickable=function() {
		if ($scope.treeLoadInProgress){
			return false;
		}
		return true;
	}
	
	$scope.save = function(){
		$scope.messages = messages();
		$scope.messages.clear();
		
		// Pas de saisie utilisateur de la description qui est mise à la valeur de l'abstract
		$scope.createService.form.description = $scope.createService.form.studyAbstract;
		$scope.createService.form.dropBox = $scope.createService.form.legendeDropBox;
		$scope.treeLoadInProgress = true;
		mainService.setForm($scope.createService.form);
		console.log("scope.createService.form", $scope.createService.form);
		
		//$http.post(jsRoutes.controllers.sra.studies.api.Studies.save().url, mainService.getForm())
		
		//$http.put(jsRoutes.controllers.sra.studies.api.Studies.createFromStudy().url, mainService.getForm())
		$http.put(jsRoutes.controllers.sra.submissions.api.Submissions.createFromStudy().url, mainService.getForm())
		.success(function(data) {
				$scope.messages = messages();
				$scope.messages.clear();
				$scope.messages.clazz="alert alert-success";
				$scope.messages.text=Messages('submissions.msg.save.success') + " : " + data.code;
				$scope.messages.open();
				$scope.codeSubmission = data;
				
				var submission = data;
				//console.log("data=", data)
				var submissionCode = data.code;
				var submissionState = angular.copy(data.state);		
						
				//console.log ("submissionStateCode_1 apres appel de api.Submission.createFromStudy = ", data.state.code);
				submissionState.code = "SUB-SMD-IW";
				$http.put(jsRoutes.controllers.sra.submissions.api.Submissions.updateState(submissionCode).url, submissionState)
					 .success(function(data) { 
					 $scope.treeLoadInProgress = false;
					 //console.log("data_final!!!!!!!!!!!!!!!!!!!!!!=", data);
					 //console.log ("submissionStateCode_3 apres appel de api.Submission.updateState(SUB-SMD-IW) = ", data.state.code);
					 $scope.messages.setSuccess("La soumission " + submissionCode + 
						" pour la création à l'EBI du study " + submission.studyCode + " est en cours de traitement !!!!"); 	
				}).error(function(error) {
					 $scope.treeLoadInProgress = false;
					 console.log("ZZZZZZZZZZZZZZ dans error,  error= ", error);
					 $scope.messages.addDetails(error);
					 $scope.messages.setError("Probleme pour le changement de status de la soumission");
				});	
			}).error(function(error){
				console.log("ZZZZZZZZZZZZZZ dans error,  error= ", error);
		        $scope.treeLoadInProgress = false;
				$scope.messages.setDetails(error);
				$scope.messages.setError("save");
			});
	};

	
	$scope.reset = function(){
		$scope.createService.resetForm();
		$scope.messages = messages();
		$scope.messages.clear();
		$scope.treeLoadInProgress = false;
	};
		
	// NGL-4708
	$scope.isShowConsigne=false;
	$scope.toggleShowConsigne=function(){
		//console.log("TOGGLE search-ctrl.js");
		if ( $scope.isShowConsigne===false) { $scope.isShowConsigne=true}
		else {$scope.isShowConsigne=false}
	};	
}]);


