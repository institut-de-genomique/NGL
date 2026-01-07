"use strict";

angular.module('home').controller('CreateCtrl',[ '$http', '$scope', '$routeParams' , 'mainService', 'lists', 'tabService', 'toolsServices', 'datatable','messages',
                                                 function($http, $scope, $routeParams, mainService, lists, tabService, toolsServices, datatable, messages) { 

	// Initialisation :

	$scope.messages = messages();	// enleve le message mais pas la partie details + efficace d'utiliser $scope.messages.clear();
	$scope.messages.clear();
	$scope.form = {};
	$scope.form2 = {};
	$scope.form.submissionProjectType = "UMBRELLA_PROJECT"; // important de n'autoriser que la creation des projects umbrella
	$scope.lists = lists;
	$scope.treeLoadInProgress = false;
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
		$scope.form.dropBox="Webin-9";          // pour avoir Webin-9 pré-séléctionné																		
		$scope.form2.legendeDropBox="Webin-9";  // pour avoir Webin-9 pré-séléctionné																				
	});
				

	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('create');
		tabService.addTabs({label:Messages('projects.menu.create'),href:jsRoutes.controllers.sra.projects.tpl.Projects.home("create").url,remove:true});
		tabService.activeTab(0); //  active l'onglet en le mettant en bleu
	}
	
	$scope.alerte = "Vous devez vous connecter pour continuer";
	
	
	// methode appelée depuis la vue avec la directive ng-init
	$scope.setUserInScope = function(user) {
		var str = "user='" + $scope.user + "'";
		//console.log("Dans scope.setUserInScope", str);
		if( ! toolsServices.isNotBlank(user)) {
			$scope.messages.setError($scope.alerte);
			return;
		} else {
		   $scope.user = user;		
		}	
	};


	$scope.saveSubmission = function() {
		$scope.messages = messages();	
		$scope.messages.clear();
		$scope.treeLoadInProgress = true;
		//console.log("Dans saveSubmission, scope.form=", $scope.form);
		//console.log("Dans saveSubmission, mainService.getForm=", mainService.getForm());
		$http.put(jsRoutes.controllers.sra.submissions.api.Submissions.createFromUmbrella().url, mainService.getForm())
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
						
				//console.log ("submissionStateCode_1 apres appel de api.Submission.createFromUmbrella = ", data.state.code);
				submissionState.code = "SUB-V";
				$http.put(jsRoutes.controllers.sra.submissions.api.Submissions.updateState(submissionCode).url, submissionState)
				.success(function(data) { 
					//console.log ("submissionStateCode_2 apres appel de api.Submission.updateState(SUB-V) = ", data.state.code);
					$scope.messages.setSuccess("La soumission " + submissionCode + 
						" pour la création à l'EBI du project umbrella " + submission.umbrellaCode + " est validée"); 
					submissionState.code = "SUB-SMD-IW";
					$http.put(jsRoutes.controllers.sra.submissions.api.Submissions.updateState(submissionCode).url, submissionState)
					.success(function(data) { 
						$scope.treeLoadInProgress = false;

						//console.log("data_final!!!!!!!!!!!!!!!!!!!!!!=", data);
						//console.log ("submissionStateCode_3 apres appel de api.Submission.updateState(SUB-SMD-IW) = ", data.state.code);
						$scope.messages.setSuccess("La soumission " + submissionCode + 
						" pour la création à l'EBI du project umbrella " + submission.umbrellaCode + " est en cours de traitement !!!!"); 	
					}).error(function(error) {
						$scope.treeLoadInProgress = false;
						console.log("ZZZZZZZZZZZZZZ dans error,  error= ", error);
						$scope.messages.addDetails(error);
						$scope.messages.setError("Probleme pour le changement de status de la soumission");
					});	
					
				}).error(function(error) {
					$scope.treeLoadInProgress = false;
					console.log("ZZZZZZZZZZZZZZ dans error,  error= ", error);
					$scope.messages.addDetails(error);
					$scope.messages.setError("Probleme pour le changement de status de la soumission");
				});	
			
			}).error(function(error){
		        $scope.treeLoadInProgress = false;
				$scope.messages.setDetails(error);
				$scope.messages.setError("save");
			});
	};
	
	$scope.toJsonUmbrellaModelOrThrow = function(json, errorDetails) {
		//console.log("dans toJsonStudyModelOrThrow, errorDetails=", errorDetails);
		//console.log("dans toJsonStudyModelOrThrow, json=", line);

		toolsServices.toJsonUmbrellaModel(json, errorDetails);
		//line.description = line.studyAbstract;   // mettre la description avec meme valeur que abstract => fait dans java
		//console.log("dans toJsonStudyModelOrThrow, errorDetails=", errorDetails);
		if((Object.keys(errorDetails)).length > 0) {
			//console.log("AAAAAAAAAAAAAAAAAAA               J'ai bien une erreur de  PARSING");
			$scope.messages = messages(); // si on re-sauve sans recharger page.
			$scope.messages.clear();      // si on re-sauve sans recharger page.
			$scope.messages.addDetails(errorDetails);
			$scope.messages.setError("save");
			$scope.treeLoadInProgress = false;
			throw("Erreur de parsing");
		} 
	}; // end $scope.toJsonStudyModelOrThrow
	
	
	$scope.reset = function(){
		$scope.treeLoadInProgress = false;
		$scope.messages = messages();	
		$scope.messages.clear();
		$scope.form = {}; // on initialise à null toutes les variables recuperees dans create-ctrl.js dans code : ng-model="form
		$scope.form2 = {};
		$scope.form.submissionProjectType = "UMBRELLA_PROJECT"; // important de n'autoriser que la creation des projects umbrella
		$scope.form.dropBox="Webin-9";         // pour avoir Webin-9 pré-séléctionné																		
		$scope.form2.legendeDropBox="Webin-9";  // pour avoir Webin-9 pré-séléctionné				
	};
		
	
	$scope.save = function(){
		$scope.messages = messages();
		$scope.messages.clear();
		var str = "user='" + $scope.user + "'";
		//console.log("Dans le save", str);
		if( ! toolsServices.isNotBlank($scope.user)) {
			$scope.messages.setError($scope.alerte);
			return;
		}	
		$scope.errorDetails = {};
		$scope.toJsonUmbrellaModelOrThrow($scope.form, $scope.errorDetails);
		//console.log($scope.form);
		mainService.setForm($scope.form);
		$scope.saveSubmission();
	};

	// NGL-4708
	$scope.isShowConsigne=false;
	$scope.toggleShowConsigne=function(){
		//console.log("TOGGLE search-ctrl.js");
		if ( $scope.isShowConsigne===false) { $scope.isShowConsigne=true}
		else {$scope.isShowConsigne=false}
	};	
	
}]);


