"use strict";

angular.module('home').controller('UpdateCtrl',
		  [ '$http', '$scope', '$routeParams' , '$q', 'mainService', 'lists', 'tabService', 'messages', 'toolsServices', 'datatable', 'services',
	function($http,   $scope,   $routeParams,    $q,   mainService,   lists,   tabService,  messages, toolsServices, datatable, services) { 


	
	var experimentsDTConfig = {
			name:'experimentsDT',
			order :{by:'code',mode:'local', reverse:true},
			search:{
				url:jsRoutes.controllers.sra.experiments.api.Experiments.list()
			},

			pagination:{
				active:false,
				mode:'local'
			},
			select:{active: true, showButton: true},
			showTotalNumberRecords:true,
			edit : {
				active:true, // permettre edition des champs editables
				showButton : true,// bouton d'edition visible sur chaque
									// colonne
				withoutSelect : true,
				columnMode : true,
				lineMode : function(line) {
					// modification autorisee seulement pour les experiments avec AC 
                    // et non engagés dans une soumission en cours et appartenant à l'utilisateur logué :
					if(line.state.code === "SUB-F") 
					  // && line.traceInformation.createUser === $scope.user) // suppression controle user
							return true;
						 else 
							return false; 
					}
			},	
			cancel : {
				showButton:true
			},
			hide:{
				active:true,
				showButton:true
			},
			exportCSV:{
				active:false,
				showButton : false
			},
// show:{ // bouton pour epingler si on passe par details-ctrl.js
// active:true,
// add :function(line){
// tabService.addTabs({label:line.code,href:jsRoutes.controllers.sra.experiments.tpl.Experiments.get(line.code).url,remove:true});
// }
// },
			save : {
				active:true,
				showButton : false,
				changeClass : false,
				url:function(line){
					// jamais utilisé si mode local :
					return jsRoutes.controllers.sra.experiments.api.Experiments.update(line.code).url; 
				},
				method:'put',
				mode:'local',
				value:function(line){
					return line;
				},
				callback : function(datatable, errors) {
				}
			},
//			bouton remove remplacé par other bouton 
//			remove : {
//				active :true,
//				withEdit : true, // Allow to remove a line in edition mode
//				showButton : true,// Show the remove button in the toolbar
//				mode : 'local', // Remove mode
//				callback : function(datatable, errorsNumber) {
//				}, // Callback after remove all element.
//			},
			// on ne peut pas mettre de mode local sur otherButtons
			otherButtons :{
				  active:true,
				  template:'<button class="btn btn-primary" ng-click="toolsServices.poubelleBleue(experimentsDT)"  ng-disabled="datatable.isEmpty()" title="'+Messages("Retirer les lignes selectionnées du tableau")+'"><i class="fa fa-trash" ></i></button>'
			},
		};
	



// ---------------------------------------------------------------------------------------------------
	
	// Initialisations :
	// -------------------
	
	//console.log("Dans update-ctrl.js");
	// mettre toolsServices dans $scope pour pouvoir y acceder depuis la config du datatable 
	$scope.toolsServices = toolsServices;
	
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('update');
		tabService.addTabs({label:Messages('experiments.menu.update'),href:jsRoutes.controllers.sra.experiments.tpl.Experiments.home("update").url,remove:true});
		tabService.activeTab(0); //  active l'onglet en le mettant en bleu
	}
	$scope.messages = messages();	// enleve le message mais pas la partie details + efficace d'utiliser $scope.messages.clear();
	$scope.messages.clear();	
	$scope.form = {};   // important, utilisé dans api.experiments.list puis api.experiments.getQuery
	$scope.form2 = {};  // important, utilise pour determination lignes editable et surcharge données utilisateur
	$scope.lists = lists;
	$scope.form.stateCode ="SUB-F";  
	$scope.sraVariables = {};
	//$scope.form.createUser = $scope.user;        
	//$scope.form2.editableCreateUser = $scope.user; 
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('update');
		tabService.addTabs({label:Messages('experiments.menu.update'),href:jsRoutes.controllers.sra.experiments.tpl.Experiments.home("update").url,remove:true});
		tabService.activeTab(0); //  active l'onglet en le mettant en bleu
	}

// Initialisation ProjectCodes:
	$scope.lists.refresh.projects();	
	lists.refresh.states({objectTypeCode:"SRASubmission"});

// Initialisation variables sra :

	$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'librarySelection'}})
	.success(function(data) {
	// initialisation de la variable
	// consultationService.sraVariables.librarySelection utilisée dans datatable
		$scope.sraVariables.librarySelection = data;																									
	});			
	$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'libraryStrategy'}})
		.success(function(data) {
		// initialisation de la variable
		// consultationService.sraVariables.libraryStrategy utilisée dans
		// datatable
			$scope.sraVariables.libraryStrategy = data;																									
	});					
	$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'librarySource'}})
		.success(function(data) {
		// initialisation de la variable
		// consultationService.sraVariables.librarySource utilisée dans
		// datatable
			$scope.sraVariables.librarySource = data;																									
		});						

	
// Initialisation datatable :
	$scope.experimentsDT = datatable(experimentsDTConfig);
	$scope.experimentsDT.setColumnsConfig(services.getExperimentColumnsForUpdate());


// initialisation typeParser
	$scope.typeParser = "userExperimentExtended";
	//$scope.treeLoadInProgress = false;

// ---------------------------------------------------------------------------------------------------

	// Definitions des methodes :
	// ---------------------------
	
	
	// methode appelée depuis la vue avec la directive ng-init
	$scope.setUserInScope = function(user) {
		$scope.user = user;
		//console.log("Dans setUserInScope, user= ", user);
	};
	
	$scope.errorCallback = function(error) {
		console.log("error : ", error);
		$scope.messages.addDetails(error.data);
		$scope.messages.setError("PROBLEME dans userFileExperiment :");
	};
	

	$scope.search = function(user) {
		//console.log("Dans update-ctrl.search");
		$scope.messages = messages();
		$scope.messages.clear();
		//$scope.treeLoadInProgress = true;

		if (! $scope.form.accessionRegex   && 
			! $scope.form.projCodes        &&
			! $scope.form.accessions       && 
			! $scope.form.studyIdentifier  &&
			! $scope.form.sampleIdentifier &&
			! $scope.form.codeRegex        &&
			! $scope.form.codes) {			
			//console.log("Aucun parametre => envoyer message erreur");
			$scope.reset(); // a mettre avant setError car annule messages
			$scope.messages.setError(Messages("AbsenceSearchParameter"));
			//console.log("update-ctrl.$scope.messages = ", $scope.messages);
			// annuler spinner si besoin
			//$scope.treeLoadInProgress = false;
			throw("Aucun parametre pour la recherche des experiments à updater"); 
		} 
		$scope.form.stateCode="SUB-F";
		// suppression controle user 
		//$scope.form.createUser = $scope.user; // pour selectionner uniquement
												// donnees utilisateur
		$scope.form2.editableStateCode = "SUB-F";
		// suppression controle user
		//$scope.form2.editableCreateUser = $scope.user;// pour rendre surchageable par userExperimentFile
														

		$scope.base64UserFileExperiment=""; 
		//console.log("userFileExperiment=", $scope.form2.userFileExperiment);

		//console.log("dans search, $scope.form2", $scope.form2);
		
		if (toolsServices.isNotBlank($scope.form2.userFileExperiment) &&
			toolsServices.isNotBlank($scope.form2.userFileExperiment.value)) {
				//console.log("Recuperation de $scope.base64UserFileExperiment");
				$scope.base64UserFileExperiment = $scope.form2.userFileExperiment.value;

		} 
		//console.log("update-ctrl.search : ok j'ai des parametres");
		// recuperation de la variable user depuis formulaire et setUserInScope
		//console.log("update-ctrl.$scope.user = ", $scope.user); 
		//console.log("$scope.form = ", $scope.form);
		
		// Remplacement des pesudoStateCodes par codes stateCodes dans le formulaire et destruction
		// de form.pseudoStateCodes pour pouvoir envoyer directement form a la methode search ou save du datatable:
		$scope.saveUserForm = angular.copy($scope.form);// sauver formulaire utilisateur initial avant modif dans replacePseudoStateCodesToStateCodesInFormulaire
		toolsServices.replacePseudoStateCodesToStateCodesInFormulaire($scope.sraVariables.pseudoStateCodeToStateCodes, $scope.form);
		$scope.formSearch = angular.copy($scope.form);
		$scope.formUpdate = angular.copy($scope.form);
		
		//$scope.form = {};
		//console.log("$scope.form" ,$scope.form);
		//console.log("$scope.formSearch" ,$scope.formSearch);

		// Charger le bon form pour le datatable :
		mainService.setForm($scope.formSearch);
		
		// chargement des experiments de la base :
		$http.get(jsRoutes.controllers.sra.experiments.api.Experiments.list().url,{params: $scope.formSearch})
		.success(function(data) {
			$scope.experiments_db = data;
			$scope.experiments_db_ori = angular.copy($scope.experiments_db);
			// ajout données utilisateurs si besoin :
			$scope.mapUserExperiments = null;
			//$scope.tab_final_experiments = null;
			if(toolsServices.isNotBlank($scope.base64UserFileExperiment)) {
				//console.log("IIIIIIIIIII             Dans search, $scope.mapUserExperiments=", $scope.mapUserExperiments);
				services.loadUserExperimentInfosAndAdd2ExperimentsDT($scope.typeParser, $scope.base64UserFileExperiment, $scope.experiments_db, 
																   			 $scope.mapUserExperiments, $scope.user, 
																   			 $scope.form2.editableStateCode, $scope.experimentsDT, $scope.messages);
			} else {
				$scope.experimentsDT.setData($scope.experiments_db, $scope.experiments_db.length);
			}
		}).error(function(error) {
			console.log("error : ", error);
			$scope.messages.addDetails(error);
			$scope.messages.setError("PROBLEME lors de la recuperation des experiments de la base de données :");
			//$scope.treeLoadInProgress = false;
		});
		//$scope.treeLoadInProgress = false;
	}; // end search
		
	$scope.reset = function() {
		$scope.form = {};
		$scope.tab_experiments = []; 
		//$scope.treeLoadInProgress = false;
		// $scope.experimentsDT = null; // ne marche pas datatable
		$scope.experimentsDT = datatable(experimentsDTConfig);
		$scope.experimentsDT.setColumnsConfig(services.getExperimentColumnsForUpdate());
		$scope.messages = messages();	// enleve le message mais pas la partie details + efficace d'utiliser $scope.messages.clear();
		$scope.messages.clear();
		$scope.form.stateCode = "SUB-F";
		$scope.form2 = {};
		$scope.form2.editableStateCode = "SUB-F";
		//$scope.form2.editableCreateUser = $scope.user;// pour rendre surchageable par userExperimentFile
	}; // end reset
	


	

	
	// declencher creation d'un objet submission pour update :
	$scope.build_and_exec_submission = function(tab_experiments) {
		var args = {
			project     : null,
			study       : null,
			samples     : [],
			experiments : tab_experiments
		};
	
		// appeler url pour creation d'un objet submission pour update :
		$http.put(jsRoutes.controllers.sra.submissions.api.Submissions.createForUpdate().url, args)
		.success(function(data) { 
		
			console.log("yyyyyyyy dans success, data = ", data); 	
			var submission = data;
			var submissionCode = data.code;
			var submissionState = angular.copy(data.state);				
			submissionState.code = "SUBU-SMD-IW";
			// $scope.messages.setSuccess("La soumission " + submissionCode
			// + " pour la mise a jour à l'EBI des données du tableau est en
			// cours de traitement");
			$http.put(jsRoutes.controllers.sra.submissions.api.Submissions.updateState(submissionCode).url, submissionState)
			.success(function(data) { 
				// Pas de recuperation d'erreur si requete ci-dessous : 
				//$http.put(jsRoutes.controllers.sra.submissions.api.Submissions.updateState(submissionCode).url, "toto")
				//.success(function(data) { 
				console.log("zzzzzzzzzzzz dans success, data=", data);
				var submissionCode = data.code;
				$scope.messages.setSuccess("La soumission " + submissionCode + " pour la mise a jour à l'EBI des données du tableau est en cours de traitement"); 
				// rafraichissement tableau avec nouveaux status et nouveau studyCode et sampleCode : 
				var tab_codes = [];
				$scope.experimentsDT = datatable(experimentsDTConfig);
				$scope.experimentsDT.setColumnsConfig(services.getExperimentColumnsForUpdate());
				for(var i= 0; i < tab_experiments.length; i++){
					tab_codes.push(tab_experiments[i].code);
				}
				$scope.experimentsDT.search({codes:tab_codes});		
			}).error(function(error) {
				// testé avec experiment deja dans soumission => ok  
				console.log("ZZZZZZZZZZZZZZ dans error,  error= ", error);
				$scope.messages.addDetails(error);
				$scope.messages.setError("Probleme pour le changement de status de la soumission");
				// var submissionCode = error.data.code;
				//$scope.messages.setError("Soumission " + submissionCode + " qui n'a pas pu
				// etre mise dans le bon etat, mais experiment sauve dans base avec ses
				// modifications");
				//$scope.treeLoadInProgress = false;
			});
		}).error(function(error) {
			console.log("YYYYYYYYYYYYYYYYYYYYYYYYY dans error ");
			console.log("error = ", error);
			$scope.messages.addDetails(error); 
			$scope.messages.setError("Erreur creation de la soumission"); 
		});
	
	}; // end build_and_exec_submission

	
	$scope.submit = function() {
		//console.log("Dans update-ctrl.submit");
		$scope.messages = messages();	// enleve le message mais pas la partie details + efficace d'utiliser $scope.messages.clear();
		$scope.messages.clear();
		//$scope.treeLoadInProgress = true;
		mainService.setForm($scope.formUpdate);
		// sauvegarde des données en local (mode local le save de getExperimentColumnForUpdate)
		// (si mode remote, les donnees sont bien sauvées dans base avec valeurs
		// editées mais le datatable utilisé ici ne contient pas les modifs des utilisateurs
		// => En mode remote, dans le datatable, le modele n'est pas synchronisé
		// avec la vue.
		// => En mode local, dans le datatable, le modele est synchronisé avec la
		// vue et contient bien les modifications des utilisateurs, 
		// en revanche, pas de sauvegarde dans la base !
		$scope.experimentsDT.save(); 
		
		var tab_experiments = $scope.experimentsDT.getData();

		if(! tab_experiments || tab_experiments.length < 1) {
			$scope.reset();
			$scope.messages.setError(Messages("AbsenceDonneePourUpdate"));
			throw(Messages("AbsenceDonneePourUpdate"));
		}
		// declencher creation d'un objet submission pour update et rafraichir tableau des experiments:
		$scope.build_and_exec_submission(tab_experiments);
		//$scope.treeLoadInProgress = false;

	}; // end $scope.submit
	
	
}]);
