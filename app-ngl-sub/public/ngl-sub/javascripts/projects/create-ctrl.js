"use strict";

angular.module('home').controller('CreateCtrl',[ '$http', '$scope', '$routeParams' , 'mainService', 'lists', 'tabService', 'toolsServices', 'datatable','messages',
                                                 function($http, $scope, $routeParams, mainService, lists, tabService, toolsServices, datatable, messages) { 

	// Initialisation :

	$scope.messages = messages();	// enleve le message mais pas la partie details + efficace d'utiliser $scope.messages.clear();
	$scope.messages.clear();
	$scope.form = {};
	$scope.form.submissionProjectType = "UMBRELLA_PROJECT"; // important de n'autoriser que la creation des projects umbrella
	$scope.lists = lists;
	$scope.treeLoadInProgress = false;

	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('create');
		tabService.addTabs({label:Messages('projects.menu.create'),href:jsRoutes.controllers.sra.projects.tpl.Projects.home("create").url,remove:true});
		tabService.activeTab(0); //  active l'onglet en le mettant en bleu
	}
	
	$scope.alerte = "Vous devez vous connecter pour continuer";
	
	// methode appelée depuis la vue avec la directive ng-init
	$scope.setUserInScope = function(user) {
		var str = "user='" + $scope.user + "'";
		console.log("Dans scope.setUserInScope", str);
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
		console.log("Dans saveSubmission, scope.form=", $scope.form);
		mainService.setForm($scope.form);
		console.log("Dans saveSubmission, mainService.getForm=", mainService.getForm());
		//$scope.search();
			$http.put(jsRoutes.controllers.sra.submissions.api.Submissions.createFromUmbrella().url, mainService.getForm())
			.success(function(data) {
		        $scope.treeLoadInProgress = false;
				$scope.messages = messages();
				$scope.messages.clear();
				$scope.messages.clazz="alert alert-success";
				$scope.messages.text=Messages('submissions.msg.save.success') + " : " + data.code;
				$scope.messages.open();
				$scope.codeSubmission = data;
				
				var submission = data;
				console.log("data=", data)
				var submissionCode = data.code;
				var submissionState = angular.copy(data.state);		
						
				console.log ("submissionStateCode_1 apres appel de api.Submission.createFromUmbrella = ", data.state.code);
				submissionState.code = "SUB-V";
		     
				$http.put(jsRoutes.controllers.sra.submissions.api.Submissions.updateState(submissionCode).url, submissionState)
				.success(function(data) { 
					console.log ("submissionStateCode_2 apres appel de api.Submission.updateState(SUB-V) = ", data.state.code);
					$scope.messages.setSuccess("La soumission " + submissionCode + 
						" pour la création à l'EBI du project umbrella " + submission.umbrellaCode + " est validée"); 
					submissionState.code = "SUB-SMD-IW";
							$http.put(jsRoutes.controllers.sra.submissions.api.Submissions.updateState(submissionCode).url, submissionState)
							.success(function(data) { 
							console.log("data_final!!!!!!!!!!!!!!!!!!!!!!=", data);
							console.log ("submissionStateCode_3 apres appel de api.Submission.updateState(SUB-SMD-IW) = ", data.state.code);
							$scope.messages.setSuccess("La soumission " + submissionCode + 
								" pour la création à l'EBI du project umbrella " + submission.umbrellaCode + " est en cours de traitement !!!!"); 
								
								
							}).error(function(error) {
							console.log("ZZZZZZZZZZZZZZ dans error,  error= ", error);
							$scope.messages.addDetails(error);
							$scope.messages.setError("Probleme pour le changement de status de la soumission");
							});	
					
				}).error(function(error) {
			 
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
	

	$scope.reset = function(){
		$scope.treeLoadInProgress = false;
		$scope.messages = messages();	
		$scope.messages.clear();
		$scope.form = {}; // on initialise à null toutes les variables recuperees dans create-ctrl.js dans code : ng-model="form
		$scope.form.submissionProjectType = "UMBRELLA_PROJECT"; // important de n'autoriser que la creation des projects umbrella
	};	
	
	$scope.save = function(){
		$scope.messages = messages();
		$scope.messages.clear();
		var str = "user='" + $scope.user + "'";
		console.log("Dans le save", str);
		if( ! toolsServices.isNotBlank($scope.user)) {
			$scope.messages.setError($scope.alerte);
			return;
		}	
		$scope.form.childrenProjectAccessions = new Array();
		
		if( ! toolsServices.isNullOrEmpty($scope.form.stringChildProjects)){
			if ($scope.form.stringChildProjects.indexOf(",") > 0) {
				var tab = $scope.form.stringChildProjects.split(",");
				for(var i= 0; i < tab.length; i++) {
    	 			var child = tab[i].trim();
					if( ! toolsServices.isNullOrEmpty(child)){
						$scope.form.childrenProjectAccessions.push(child);
					}
				}
			} else {
				if( ! toolsServices.isNullOrEmpty($scope.form.stringChildProjects.trim())){
					$scope.form.childrenProjectAccessions.push($scope.form.stringChildProjects.trim());
				}
			}
		}
		$scope.form.idsPubmed = new Array();				
		if( ! toolsServices.isNullOrEmpty($scope.form.strIdsPubmed)){
			if ($scope.form.strIdsPubmed.indexOf(",") > 0) {
				var tab = $scope.form.strIdsPubmed.split(",");
				for(var i= 0; i < tab.length; i++) {
    	 			var id = tab[i].trim();
					console.log("yyyyyyyyyyyyyyyyyn nn   id=", id);
					if( ! toolsServices.isNullOrEmpty(id)){
						$scope.form.idsPubmed.push(id);
					}
				}
			} else {
				if( ! toolsServices.isNullOrEmpty($scope.form.strIdsPubmed.trim())){
					$scope.form.idsPubmed.push($scope.form.strIdsPubmed.trim());
				}
			}
			console.log("$scope.form.idsPubmed", $scope.form.idsPubmed);
		}
		console.log("form.childrenProjectAccessions = ", $scope.form.childrenProjectAccessions);
		//delete $scope.form.stringChildProjects;	
		mainService.setForm($scope.form);
		console.log($scope.form);
		$scope.saveSubmission();
	};
	
	
}]);


