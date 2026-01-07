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
	$scope.sraVariables = {};
	$scope.treeLoadInProgress = false;
	
	$scope.isClickable=function() {
		if ($scope.treeLoadInProgress){
			return false;
		}
		return true;
	}
	
	$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'legendeDropBox'}})
		.success(function(data) {
		$scope.sraVariables.legendeDropBox = data;	
		$scope.form.dropBox="Webin-9";  // pour avoir Webin-9 pré-séléctionné																		
		$scope.form.legendeDropBox="Webin-9";  // pour avoir Webin-9 pré-séléctionné																		
	});	
		
	// NGL-4235 
	$scope.isShowConsigne=false;
	$scope.toggleShowConsigne=function(){
		//console.log("TOGGLE search-ctrl.js");
		if ( $scope.isShowConsigne===false) { 
			$scope.isShowConsigne=true}
		else {$scope.isShowConsigne=false}
	}
	
	$scope.save = function(){
		$scope.messages = messages();	
		$scope.messages.clear();
		$scope.treeLoadInProgress = true;
		$scope.form._type = "Sample"; // on cree uniquement des Samples via cette page et jamais d'ExternalSample
		
		console.log("scope.form=", $scope.form);
		$scope.form.dropBox = $scope.form.legendeDropBox;
		mainService.setForm($scope.form);
		console.log("mainService.getForm=", mainService.getForm());
		$http.put(jsRoutes.controllers.sra.submissions.api.Submissions.createFromSample().url, mainService.getForm())
		.success(function(data) {
				$scope.messages = messages();
				$scope.messages.clear();
				$scope.messages.clazz="alert alert-success";
				$scope.messages.text=Messages('submissions.msg.save.success') + " : " + data.code;
				$scope.messages.open();
				$scope.codeSubmission = data;
				console.log("ok pour createFromSample avec code=", data.code);
				var submission = data;
				//console.log("data=", data)
				var submissionCode = data.code;
				var submissionState = angular.copy(data.state);		
						
				//console.log ("submissionStateCode_1 apres appel de api.Submission.createFromStudy = ", data.state.code);
				submissionState.code = "SUB-SMD-IW";
				$http.put(jsRoutes.controllers.sra.submissions.api.Submissions.updateState(submissionCode).url, submissionState)
					 .success(function(data) { 
					 $scope.treeLoadInProgress = false;
					console.log("ok pour updateState de la soumission");
					 //console.log("data_final!!!!!!!!!!!!!!!!!!!!!!=", data);
					 //console.log ("submissionStateCode_3 apres appel de api.Submission.updateState(SUB-SMD-IW) = ", data.state.code);
					 var sampleCode = submission.sampleCodes[0];
					 $scope.messages.setSuccess("La soumission " + submissionCode + 
						" pour la création à l'EBI du sample " + sampleCode + " est en cours de traitement !!!!"); 	
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


