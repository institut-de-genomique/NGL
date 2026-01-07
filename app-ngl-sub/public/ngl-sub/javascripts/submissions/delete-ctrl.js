"use strict";

angular.module('home').controller('DeleteCtrl',[ '$http', '$scope', '$routeParams' , '$q', 'mainService', 'lists', 'tabService', 'toolsServices', 'messages', 'datatable',
	                                                  function($http, $scope, $routeParams, $q, mainService, lists, tabService, toolsServices, messages, datatable) { 
	

	// initialisations :
	console.log("Dans submissions.delete-ctrl.js");

	$scope.messages = messages();
	$scope.messages.clear();
	$scope.form = {};  // important. 
		$scope.lists = lists; // service lists
	$scope.sraVariables = {};

	$scope.messError = "Error pour la suppression de la soumission ";
	$scope.messSuccess = "Succes pour la suppression de la soumission ";
	
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('delete');
		tabService.addTabs({label:Messages('submissions.menu.delete'),href:jsRoutes.controllers.sra.submissions.tpl.Submissions.home("delete").url,remove:true});
		tabService.activeTab(0); //  active l'onglet en le mettant en bleu
	}

	
	

//----------------------------------------------------------------------------------------

	// Definitions methodes :
	//-----------------------
	
	
	// methode appelée depuis la vue avec la directive ng-init
	$scope.setUserInScope = function(user) {
		$scope.user = user;
		console.log("Dans setUserInScope, user= ", $scope.user);
	};
	
	
	$scope.reset = function() {
		$scope.form = {};
		$scope.formFinal = {};
		$scope.formFinal.codes = [];
		$scope.submissionsDT = datatable(submissionsDTConfig);
		$scope.submissionsDT.setColumnsConfig(getSubmissionColumns());
		$scope.messages = messages(); 
		$scope.messages.clear();
		$scope.messError = "Error pour la suppression de la soumission ";
		$scope.messSuccess = "Succes pour la suppression de la soumission ";
		//$scope.form.stateCode="SUB-V";
		$scope.form.type="CREATION";
		// retrait du controle utilisateur
		//$scope.form.createUser = $scope.user;
        $scope.treeLoadInProgress = false;
	};
	


	
	$scope.delete = function() {
		mainService.setForm($scope.form);
		console.log("$scope.submissionCode=", $scope.submissionCode);
		$http.delete(jsRoutes.controllers.sra.submissions.api.Submissions.delete($scope.submissionCode).url).success(function(data) {
			$scope.treeLoadInProgress = false;
			$scope.messages = messages();
			$scope.messages.clear();
			$scope.messages.clazz="alert alert-success";
			$scope.messages.text=Messages('submissions.msg.delete.success') + " : " + $scope.submissionCode;
			$scope.messages.open();
			$scope.codeSubmission = data;
		}).error(function(data){
		    $scope.treeLoadInProgress = false;
			$scope.messages.setDetails(data);
			$scope.messages.setError("Suppression de la soumission en erreur");
		});
	}; // end delete()

		
}]);
