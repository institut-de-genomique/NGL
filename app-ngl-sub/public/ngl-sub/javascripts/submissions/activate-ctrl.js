"use strict";

angular.module('home').controller('ActivateCtrl',[ '$http', '$scope', '$routeParams' , '$q', 'mainService', 'lists', 'tabService', 'toolsServices', 'messages', 'datatable',
	                                                  function($http, $scope, $routeParams, $q, mainService, lists, tabService, toolsServices, messages, datatable) { 
	

	var submissionsDTConfig = {
			select:{active:true, showButton:true},
			showTotalNumberRecords:true,
			pagination:{
				active:true,
				mode:'local',
                numberRecordsPerPage: 100
			},

			order :{mode:'local', by:'code', reverse : true},
			search:{
				url:jsRoutes.controllers.sra.submissions.api.Submissions.list()
			},
//			show:{
//				active:false,
//				showButton:false,
//				add :function(line){
//					tabService.addTabs({label:line.code,href:jsRoutes.controllers.sra.submissions.tpl.Submissions.get(line.code).url,remove:true});
//				}
//			},
			hide:{
				active:true
			},
			exportCSV:{
				active:false
			}, 
			cancel : {
				showButton:false
			},
//			otherButtons:{
//				active:true,
//				template:'<button class="btn" ng-click="activate()" data-toggle="tooltip" title="'+Messages("button.activate")+'">'
//				+'<i class="fa fa-play fa-lg"></i></button>'
//			},
			name:"Submissions"
	};
	
	var getSubmissionColumns = function(){
		var columns = [];
		columns.push({  property:"traceInformation.creationDate",
			        	header: Messages("traceInformation.creationDate"),
			        	type :"date",		    	  	
			        	order:true});
		columns.push({  property:"traceInformation.createUser",
						header: Messages("submissions.creationUser"),
						type :"text",		    	  	
						order:true});
		columns.push({	property:"code",
			    	  	header: Messages("submissions.code"),
			    	  	type :"text",		    	  	
			    	  	order:true});
		columns.push({	property:"projectCodes",
    	  				header: Messages("submissions.projectCodes"),
    	  				type :"text",		    	  	
    	  				order:true});		
		columns.push({	property:"type",
						header: Messages("submissions.type"),
						type :"text",		    	  	
						order:true});	
		columns.push({
						property     : "firstSubmissionDate",
						header       : Messages("firstSubmissionDate"),
						type         : "date",	
						hide         : true,	    	  	
						order        : true,
						edit         : false,
						choiceInList : false  
						});		
		columns.push({	property:"accession",
			    	  	header: Messages("submissions.accession"),
			    	  	type :"text",		    	  	
			    	  	order:true});		
		columns.push({	property:"state.code",
						"filter":"codes:'state'",
						header: Messages("submissions.state"),
						type :"text",
						order:true});		
		return columns;
	};

//----------------------------------------------------------------------------------------
	
	// initialisations :
	//console.log("Dans submissions.activate-ctrl.js");

	$scope.messages = messages();
	$scope.messages.clear();
	$scope.form = {};  // important. 
	$scope.lists = lists; // service lists
	$scope.sraVariables = {};
	$scope.messError = "Error pour le demarrage des soumissions ";
	$scope.messSuccess = "Succes pour le demarrage des soumissions ";
	$scope.activate = {};

	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('activate');
		tabService.addTabs({label:Messages('submissions.menu.activate'),href:jsRoutes.controllers.sra.submissions.tpl.Submissions.home("activate").url,remove:true});
		tabService.activeTab(0); //  active l'onglet en le mettant en bleu
	}

//	Initialisation datatable :
	$scope.submissionsDT = datatable(submissionsDTConfig);
	$scope.submissionsDT.setColumnsConfig(getSubmissionColumns());
	$scope.lists.refresh.projects();
	
	

//----------------------------------------------------------------------------------------

	// Definitions methodes :
	//-----------------------
	
	
	// methode appelée depuis la vue avec la directive ng-init
	$scope.setUserInScope = function(user) {
		$scope.user = user;
		//console.log("Dans setUserInScope, user= ", user);
	};
	
	
	$scope.reset = function() {
		$scope.form = {};
		$scope.formFinal = {};
		$scope.formFinal.codes = [];
		$scope.submissionsDT = datatable(submissionsDTConfig);
		$scope.submissionsDT.setColumnsConfig(getSubmissionColumns());
		$scope.messages = messages(); 
		$scope.messages.clear();
		$scope.messError = "Erreur pour le demarrage des soumissions ";
		$scope.messSuccess = "Succes pour le demarrage des soumissions ";
		$scope.form.stateCode="SUB-V";
		$scope.form.type="CREATION";
		// retrait du controle utilisateur
		//$scope.form.createUser = $scope.user;
        $scope.treeLoadInProgress = false;
	};
	

	// methode appelee pour remplir le tableau des submissions
	// Recherche toutes les submissions de type CREATION validées et créés par utilisateur courant 
	$scope.search = function() {
		//console.log("dans activate-ctrl.search : projCode " + $scope.form.projCode);	
		//console.log("dans activate-ctrl.search : state !!!!!'" + $scope.form.state+"'");
		$scope.form.stateCode="SUB-V";
		$scope.form.type="CREATION";
		// retrait du controle utilisateur
		//$scope.form.createUser = $scope.user;
		$scope.submissionsDT.search($scope.form);
		$scope.formFinal = {};
		$scope.formFinal.codes = [];
	};	
	
	
	
	// message erreur correct avec 2 submission.code si rep_soumission existent deja
	$scope.build_request = function(submissionCode, state) {
		var request =$http.put(jsRoutes.controllers.sra.submissions.api.Submissions.updateState(submissionCode).url, state)
		.success(function(data) {
			//console.log("SUCCESS MY MY data = ", data);
			//console.log("SUCCESS MY MY submissionCode = ", submissionCode);
			$scope.messSuccess = $scope.messSuccess + submissionCode +", ";
			$scope.formFinal.codes.push(submissionCode);
		})
		.error(function(data){
			//console.log("ERROR MY MY data = ", data);
			//console.log("ERROR MYMY pour ", submissionCode);
			// ok on recupere bien les erreurs, 
			//facilement verifiable sur localhost avec absence de fichiers sur disques
			//console.log("ERROR MY MY data = ", data);
			//console.log("ERROR MY MY submissionCode = ", submissionCode);
			$scope.messages.addDetails(data); 
			$scope.messError = $scope.messError + submissionCode +", ";
			$scope.formFinal.codes.push(submissionCode);
			//console.log("$scope.messError=",$scope.messError);
		});	
		return request;
	}
	

	$scope.activate = function() {
		$scope.messages = messages();
		$scope.messages.clear(); // imperatif en debut de fonction si utilisation de addDetails
		//console.log("activate ");
		$scope.treeLoadInProgress = true;
		var queries = [];
		var error = false;
		//Get data du datable
		//console.log("Get data ");
		var message_error = "";
		
		// test important car si undef alors $scope.submissionsDT.getSelection(true); declenche erreur
		if ( ! $scope.submissionsDT.getData()) {
			message_error = "Aucune soumission selectionnée pour etre demarrée";
			$scope.messages.setError(message_error);
			$scope.treeLoadInProgress = false;
			throw(message_error);
			message_error = "";
		}
		
		var tab_submissions = $scope.submissionsDT.getSelection(true);
		if(!tab_submissions || tab_submissions.length < 1) {
			message_error = "Aucune soumission selectionnée pour etre demarrée";
			$scope.treeLoadInProgress = false;
			$scope.messages.setError(message_error);
			throw(message_error);
			message_error = "";
		}
		var decompte = tab_submissions.length;
		var submissionCode = "";

		for(var i = 0; i < tab_submissions.length ; i++){
			submissionCode = tab_submissions[i].code;
			//console.log("wwwwwwwwwwwww       submissionCode = ", submissionCode);
			var state = angular.copy(tab_submissions[i].state);
			//console.log("stateCode =" + state.code);
			// si soumission avec données brutes :
			if(tab_submissions[i].experimentCodes.length>0 || toolsServices.isNotBlank(tab_submissions[i].analysisCode)){
				state.code = "SUB-SRD-IW";
			} else {
				state.code = "SUB-SMD-IW";
			}
			//console.log("stateCode =" + state.code);
			var allRequest = [];
			
			// met à jour dans la base les objets qui doivent etres soumis à l'EBI avec status SUB-SRD-IW
			
			var request = $scope.build_request(submissionCode, state);
					
			//console.log("req =" + request);
			allRequest.push(request);
		}	
		
		$q.all(allRequest)
		.then(result => { 
			// resultat de l'ensemble des requetes, avec result[0]=resultat de la requete 1 
            // avec tableau de data si la requete retourne une liste (url de la forme list) 
            // et data si la requete retourne un seul resultat(url de la forme get)
			//console.log("result = " + result); 	
			//console.log("result[0] = " + result[0]); 	
			//console.log("result[0].data = " + result[0].data); 
			//console.log("result[0].data[0] = " + result[0].data[0]); 
		}) // fin then
		.catch (error => {
			// Faire le setError dans catch mais le addDetails dans request.error 
			// car on ne passe qu'une seule fois dans le catch et contient premiere ou derniere erreur.
			//console.log("Je passe bien dans le catch ");
			//console.log("error", error);
			//console.log("error.data", error.data);
			//$scope.messages.addDetails(error.data); 
			$scope.treeLoadInProgress = false;
			//return $q.reject(error);
			
		})// fin catch
		.finally(data => { 
//			console.log("********* Dans finally : $scope.messError = ", $scope.messError);
//			console.log("********* Dans finally : $scope.messSuccess = ", $scope.messSuccess);
//			console.log("********* Dans finally : $scope.formFinal = ", $scope.formFinal);
			// Rafraichir tableau avec nouveaux status :
			$scope.submissionsDT = datatable(submissionsDTConfig);
			$scope.submissionsDT.setColumnsConfig(getSubmissionColumns());
			$scope.submissionsDT.search($scope.formFinal);
			//$scope.messages.setError($scope.messError); 

			if($scope.messError.includes(",")) {
				//console.log("********* Dans finally : Il existe bien une erreur");
				$scope.messages.setError($scope.messError); 
			} else {
				//console.log("********* Dans finally : Il n'y a aucune erreur");
				$scope.messages.setSuccess($scope.messSuccess); 
			}
	        $scope.treeLoadInProgress = false;
			//throw "ERREUR";
		}); // end finally
	}; // fin activate
	
	
}]);
