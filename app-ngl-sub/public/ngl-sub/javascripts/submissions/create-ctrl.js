"use strict";

angular.module('home').controller('CreateCtrl',[ '$http', '$scope', '$routeParams' , 'mainService', 'lists', 'tabService','datatable','messages',
                                                 function($http, $scope, $routeParams, mainService, lists, tabService, datatable, messages) { 

	// Initialisation :

	$scope.messages = messages();
	$scope.form = {};
	$scope.lists = lists;
	$scope.treeLoadInProgress = false;

	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('create');
		tabService.addTabs({label:Messages('submissions.menu.create'),href:jsRoutes.controllers.sra.submissions.tpl.Submissions.home("create").url,remove:true});
		tabService.activeTab(0); //  active l'onglet en le mettant en bleu
	}
	
//	Initialisation ProjectCodes:
	$scope.lists.refresh.projects();
	
	$scope.refreshSraStudies = function() {
		if($scope.form.projCodes && $scope.form.projCodes.length > 0){
			// appel de refresh.sraStudies dans lists de common.js
			$scope.lists.refresh.sraStudies({projCodes:this.form.projCodes, stateCodes:["NONE", "SUB-F"]});
		}
	};
	
	$scope.refreshSraConfigurations = function() {
		//console.log("Je suis dans refreshSraConfigurations avec ", this.form.projCodes);
		if($scope.form.projCodes && $scope.form.projCodes.length > 0){
			// appel de refresh.sraConfigurations dans lists de common.js
			$scope.lists.refresh.sraConfigurations({projCodes:this.form.projCodes});
		}
	};
	
	$scope.refreshReadSets = function() {
		//console.log("Je suis dans refreshReadSets");
		if($scope.form.projCodes && $scope.form.projCodes.length > 0){
			// Dans ReadSetsSearchForm.java champs projectCodes et non projCodes !!!
			// includes:"code" pour ne rapatrier que les codes des readsets et non les objets entiers
			$http.get(jsRoutes.controllers.readsets.api.ReadSets.list().url,{params:{projectCodes:this.form.projCodes, submissionStateCode:"NONE", stateCode:"A", includes:"code"}}).success(function(data) {
				$scope.readSets=data;
			});
			//console.log("liste des readsets =", $scope.readSets);
		}
	};

	// fonction qui recupere objet configuration dont le code est saisi par utilisateur et qui en fonction
	// de config.strategy_internal_study determine si la variable internal_studies est à true ou false.
	$scope.displayStudies = function() {
		//console.log("dans displayStudies");
		if($scope.form.projCodes && $scope.form.projCodes.length > 0){
			//get configuration
			$http.get(jsRoutes.controllers.sra.configurations.api.Configurations.get($scope.form.configurationCode).url).success(function(data) {
				//console.log("data.strategyStudy", data.strategyStudy);
				if(data.strategyStudy === 'STRATEGY_CODE_STUDY') {
					//console.log("cas STRATEGY_CODE_STUDY");
					$scope.STRATEGY_CODE_STUDY=true;
					$scope.STRATEGY_AC_STUDY=false;
				} else {
					//console.log("cas STRATEGY_AC_STUDY");
					$scope.STRATEGY_CODE_STUDY=false;
					$scope.STRATEGY_AC_STUDY=true;	
				}
			});
		}
	};
	
//	Initialisation variables sra :

	$scope.save = function(){
		$scope.messages.clear();
		$scope.messages = messages();	
		$scope.treeLoadInProgress = true;
		// important si le fichier utilisateur ne peut pas ou ne doit pas etre chargé que form.base64File soit
		// mis à chaine vide et non à null pour l'appel de l'url sra/api/submissions
		//console.log("$scope.userRefFileCollabToAc : '" + $scope.userRefFileCollabToAc + "'");
//		console.log("typeof $scope.userRefFileCollabToAc : '" + typeof $scope.userRefFileCollabToAc + "'");
//		console.log("typeof undefined : '" + typeof undefined + "'");
		
		$scope.form.base64UserFileRefCollabToAc=""; 
		if ($scope.userFileRefCollabToAc != null &&	$scope.userFileRefCollabToAc != undefined) {
			if ($scope.userFileRefCollabToAc.value != null && $scope.userFileRefCollabToAc.value != undefined) {
				$scope.form.base64UserFileRefCollabToAc=$scope.userFileRefCollabToAc.value;
			}
		} 
		$scope.form.base64UserFileReadSet="";
		if ($scope.userFileReadSet != null && $scope.userFileReadSet != undefined) {
			if ($scope.userFileReadSet.value != null && $scope.userFileReadSet.value != undefined) {
				$scope.form.base64UserFileReadSet=$scope.userFileReadSet.value;
			}
		} 	
		
//		$scope.form.base64UserFileExperiments = ""; 
//		if ($scope.userFileExperiments != null && $scope.userFileExperiments != undefined) {
//			if ($scope.userFileExperiments.value != null && $scope.userFileExperiments.value != undefined) {
//				$scope.form.base64UserFileExperiments=$scope.userFileExperiments.value;
//			}
//		} 		
//		$scope.form.base64UserFileSamples=""; 
//		if ($scope.userFileSamples != null && $scope.userFileSamples != undefined) {
//			if ($scope.userFileSamples.value != null && $scope.userFileSamples.value != undefined) {
//				$scope.form.base64UserFileSamples=$scope.userFileSamples.value;
//			}
//		} 

		console.log("scope.form=", $scope.form);
		mainService.setForm($scope.form);
		console.log("mainService.getForm=", mainService.getForm());
		//$scope.search();
			$http.post(jsRoutes.controllers.sra.submissions.api.Submissions.save().url, mainService.getForm()).success(function(data) {
		        $scope.treeLoadInProgress = false;
				$scope.messages.clear();
				$scope.messages.clazz="alert alert-success";
				$scope.messages.text=Messages('submissions.msg.save.success') + " : " + data;
				$scope.messages.open();
				$scope.codeSubmission = data;
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
		$scope.userFileExperiments=null;
		$scope.userFileSamples=null;
		$scope.userFileRefCollabToAc=null;
		$scope.userFileReadSet=null;
		$scope.acStudy=null;
		$scope.acSample=null;
		if (angular.element('#idUserFileReadSet') != null && angular.element('#idUserFileReadSet')[0] != null) {
			angular.element('#idUserFileReadSet')[0].value = null;
		}
		if (angular.element('#idUserFileRefCollabToAc') != null && angular.element('#idUserFileRefCollabToAc')[0] != null) {
			angular.element('#idUserFileRefCollabToAc')[0].value = null;	
		}
	};		
	
	$scope.reset = function(){
		$scope.treeLoadInProgress = false;
		$scope.messages = messages();	
		$scope.messages.clear();
		$scope.resetUserData();
	};	
	
}]);


