"use strict";

angular.module('home').controller('ActivateCtrl',[ '$http', '$scope', '$routeParams' , '$q', 'mainService', 'lists', 'tabService','messages','submissionsActivateService',
	                                                  function($http, $scope, $routeParams, $q, mainService, lists, tabService, messages, submissionsActivateService) { 

	var submissionDTConfig = {
			pagination:{mode:'local'},			
			order :{mode:'local', by:'code', reverse : true},
			search:{
				//url:jsRoutes.controllers.sra.submissions.api.Submissions.list().url+'?state=uservalidate'
				url:jsRoutes.controllers.sra.submissions.api.Submissions.list()
			},
			otherButtons:{
				active:true,
				template:'<button class="btn" ng-click="activate()" data-toggle="tooltip" title="'+Messages("button.activate")+'">'
				+'<i class="fa fa-play fa-lg"></i></button>'
			},
			name:"Submissions"
	};	
	
	

	function processInSubmission(decompte, error) { // pas d'indication de retour dans la signature.
		decompte = decompte - 1;
 		if (decompte === 0) {
 			if (error){
 				// afficher message d'erreur sans sauver la soumission.
 				$scope.messages.setError("activate");
 			} else {
 				$scope.messages.setSuccess("activate");
				//$scope.activateService.search();
 			}
 		}
 		return decompte;
	}
	
	var configMessage = {
		errorClass:'alert alert-danger',
		successClass: 'alert alert-success',							
		errorKey:{save:'msg.error.save',remove:'msg.error.remove',activate:'msg.error.activate'},
		successKey:{save:'msg.success.save',remove:'msg.success.remove',activate:'msg.success.activate'}
	};
	
	$scope.messages = messages();	
	$scope.messages.clear();
	$scope.messages.setConfig(configMessage);
	
	
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('activate');
		tabService.addTabs({label:Messages('submissions.menu.activate'),href:jsRoutes.controllers.sra.submissions.tpl.Submissions.home("activate").url,remove:true});
		tabService.activeTab(0); //  active l'onglet en le mettant en bleu
	}
	// si on declare dans services => var sraVariables = {};
	// si on declare dans le controlleur :

	$scope.activateService = submissionsActivateService;	
	$scope.activateService.init($routeParams, submissionDTConfig);

	// methode appelee pour remplir le tableau des soumissions 
	$scope.search = function(){
		$scope.messages = messages();	
		$scope.messages.clear();
		if($scope.activateService.form.projCodes && $scope.activateService.form.projCodes.length > 0){
			console.log("activateService.search:projCodes " + this.form.projCodes);	
			this.datatable.search({projCodes:this.form.projCodes, stateCode:'SUB-V'});
		} else {
			console.log("Cancel datatable");
			$scope.activateService.cancel();
		}	
	};

	
	$scope.activate = function() {
		$scope.messages = messages();
		$scope.messages.clear(); // imperatif en debut de fonction si utilisation de addDetails
		console.log("activate ");
		
		var queries = [];
		var error = false;
		//Get data du datable
		console.log("Get data ");
		//var tab_submissions = $scope.activateService.datatable.getData();
		var tab_submissions = $scope.datatable.getSelection(true);
		var decompte = tab_submissions.length ;
		
		//boucle data
		//
		
		for(var i = 0; i < tab_submissions.length ; i++){
			console.log("submissionCode = " + tab_submissions[i].code + " state = "+ tab_submissions[i].state.code);
			// met à jour dans la base les objets qui doivent etres soumis à l'EBI avec status "inwaiting" ancienne version:
			/*$http.put(jsRoutes.controllers.sra.submissions.api.Submissions.activate(tab_submissions[i].code).url, tab_submissions[i])
			.success(function(data){
		   		decompte = processInSubmission(decompte, error);
			})
			*/
			var state = angular.copy(tab_submissions[i].state);
			console.log("stateCode =" + state.code);
			
			state.code = "SUB-SRD-IW";
			console.log("stateCode =" + state.code);
			$http.put(jsRoutes.controllers.sra.submissions.api.Submissions.updateState(tab_submissions[i].code).url, state)
			.success(function(data) {
		   		decompte = processInSubmission(decompte, error);
		   		// ajout pour rafraichissement du tableau
		   		$scope.activateService.search();
			})
			
			.error(function(data){
				//$scope.messages.addDetails(data);
				$scope.messages.addDetails(data);
				error = true;
				decompte = processInSubmission(decompte, error);

			});	
		}		
		
	};
	
}]);
