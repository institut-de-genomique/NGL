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
	
	console.log("Dans update-ctrl.js");
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

// Initialisation map
	$scope.mapOriExpCodeToStudyAc     = {}; // correspondance entre expCode et studyAC dans base de données avant modif utilisateur
	$scope.mapOriExpCodeToSampleAc    = {}; // correspondance entre expCode et sampleAC dans base de données avant modif utilisateur
	$scope.mapStudyAcToStudyCode   = {}; // correspondance entre studyAc et studyCode
	$scope.mapSampleAcToSampleCode = {}; // correspondance entre sampleAc et sampleCode
	$scope.mapExpCodeToStudyAc     = {}; // correspondance entre expCode et studyAC dans base de données apres modif utilisateur
	$scope.mapExpCodeToSampleAc    = {}; // correspondance entre expCode et sampleAC dans base de données apres modif utilisateur
// initialisation typeParser
	$scope.typeParser = "userExperimentExtended";
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
		if (! $scope.form.accessionRegex  && 
			! $scope.form.projCodes       &&
			! $scope.form.accessions      && 
			! $scope.form.studyAccession  &&
			! $scope.form.sampleAccession  &&
			! $scope.form.codeRegex       &&
			! $scope.form.codes) {			
			//console.log("Aucun parametre => envoyer message erreur");
			$scope.reset(); // a mettre avant setError car annule messages
			$scope.messages.setError(Messages("AbsenceSearchParameter"));
			//console.log("update-ctrl.$scope.messages = ", $scope.messages);
			// annuler spinner si besoin
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
				console.log("Recuperation de $scope.base64UserFileExperiment");
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
		console.log("$scope.form" ,$scope.form);
		console.log("$scope.formSearch" ,$scope.formSearch);

		// Charger le bon form pour le datatable :
		mainService.setForm($scope.formSearch);
		
		// chargement des experiments de la base :
		$http.get(jsRoutes.controllers.sra.experiments.api.Experiments.list().url,{params: $scope.formSearch})
		.success(function(data) {
			$scope.experiments_db = data;
			$scope.experiments_db_ori = angular.copy($scope.experiments_db);
			$scope.experiments_db.forEach(function(experiment_db) {
				//console.log("experiment_db.code = ", experiment_db.code);
				//console.log("experiment_db.studyAccession = ", experiment_db.studyAccession);
				$scope.mapOriExpCodeToStudyAc[experiment_db.code] = experiment_db.studyAccession;
				$scope.mapOriExpCodeToSampleAc[experiment_db.code] = experiment_db.sampleAccession;
				$scope.mapStudyAcToStudyCode[experiment_db.studyAccession] = experiment_db.studyCode;
				$scope.mapSampleAcToSampleCode[experiment_db.sampleAccession] = experiment_db.sampleCode;

			}); // end forEach
			
//			console.log("scope.mapOriExpToStudyAc= ", $scope.mapOriExpCodeToStudyAc);
//			console.log("scope.mapOriExpToSampleAc= ", $scope.mapOriExpCodeToSampleAc);
//			console.log("scope.mapStudyAcToStudyCode= ", $scope.mapStudyAcToStudyCode);
//			console.log("scope.mapSampleAcToSampleCode= ", $scope.mapSampleAcToSampleCode);
			// ajout données utilisateurs si besoin :
			$scope.mapUserExperiments = null;
			//$scope.tab_final_experiments = null;
			if(toolsServices.isNotBlank($scope.base64UserFileExperiment)) {
				console.log("IIIIIIIIIII             Dans search, $scope.mapUserExperiments=", $scope.mapUserExperiments);
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
		});
		
	}; // end search
		
	$scope.reset = function() {
		$scope.form = {};
		$scope.tab_experiments = []; 
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
	
	// Modifier dans le tableau des experiments pour le studyCode et studySample:
	$scope.update_tab_experiments = function(tab_experiments, mapStudyAcToStudyCode, mapSampleAcToSampleCode) {
		tab_experiments.forEach(function(experiment) {
			//console.log("experiment.studyAccession= ", experiment.studyAccession);
			//console.log("experiment.studyCode", mapStudyAcToStudyCode[experiment.studyAccession]);
			//console.log("experiment.sampleCode", mapSampleAcToSampleCode[experiment.sampleAccession]);

			experiment.studyCode = mapStudyAcToStudyCode[experiment.studyAccession]; 
			experiment.sampleCode = mapSampleAcToSampleCode[experiment.sampleAccession]; 
		}); // end foreach
	}; // end update_tab_experiments
	
	// construction des map $scope.mapExpCodeToStudyAc et $scope.mapExpCodeToSampleAc :
	$scope.build_mapExpCodeToStudyAndSample = function(tab_experiments, mapExpCodeToStudyAc, mapExpCodeToSampleAc) {
		tab_experiments.forEach(function(experiment) {
			mapExpCodeToStudyAc[experiment.code] = experiment.studyAccession;
			mapExpCodeToSampleAc[experiment.code] = experiment.sampleAccession;
		}); // end forEach
	}; // end build_mapExpCodeToStudyAndSample
	
	// construction des requetes pour recuperer study et sample pour les studyAc et sampleAc modifiés par user :
	$scope.build_RequestStudyAndSample = function(tab_experiments, allRequestGetStudy, allRequestGetSample) {
		tab_experiments.forEach(function(experiment) {
			// construire requete getStudy si besoin :
			if(experiment.studyAccession !== $scope.mapOriExpCodeToStudyAc[experiment.code]) {
				// ajout requete pour recuperer studyCode				
				var request = $http.get(jsRoutes.controllers.sra.studies.api.Studies.list().url,{
					params:{accession:experiment.studyAccession}})
				.catch (error => {
					console.log(error);
					console.log(error.data);
					$scope.messages.addDetails(error.data); 
					//$scope.treeLoadInProgress = false;
					return $q.reject(error);                // propage erreur, comme throw 
				});
				//console.log("req =" + request);
				allRequestGetStudy.push(request);
			}

			// construire requete getSample si besoin :
			if(experiment.sampleAccession !== $scope.mapOriExpCodeToSampleAc[experiment.code]) {
				// ajout requete pour recuperer sampleCode
				var request = $http.get(jsRoutes.controllers.sra.samples.api.Samples.list().url,{
					params:{accessions:experiment.sampleAccession}})
				.catch (error => {
					console.log(error);
					console.log(error.data);
					$scope.messages.addDetails(error.data); 
					//$scope.treeLoadInProgress = false;
					return $q.reject(error);                // propage erreur, comme throw 
				});
				//console.log("req =" + request);
				allRequestGetSample.push(request);
			}
		}); // end forEach
	}; // end build_RequestStudyAndSample
	
	
	
	
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
				// rafraichissement tableau avec nouveaux status:
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
			});
		}).error(function(error) {
			console.log("YYYYYYYYYYYYYYYYYYYYYYYYY dans error ");
			console.log("error = ", error);
			$scope.messages.addDetails(error); 
			$scope.messages.setError("Erreur creation de la soumission"); 
		});
	
	}; // end build_and_exec_submission
	
	// Creation des requetes externalStudy et externalSample :
	$scope.build_RequestExternalStudyAndExternalSample = function(tab_experiments, 
																  mapStudyAcToStudyCode, 
																  mapSampleAcToSampleCode, 
																  allRequestPostExternalStudy, 
																  allRequestPostExternalSample) {
		tab_experiments.forEach(function(experiment) {
			
			if (! mapStudyAcToStudyCode.hasOwnProperty(experiment.studyAccession)) {
				var request = $http.post(jsRoutes.controllers.sra.studies.api.Studies.save().url,{"accession":experiment.studyAccession,"_type":"ExternalStudy"})
									//params:{accession:experiment.studyAccession, type:"ExternalStudy"}})
									.catch (error => {
										console.log(error);
										console.log(error.data);
										$scope.messages.addDetails(error.data); 
										return $q.reject(error);                // propage erreur, comme throw 
									});
			allRequestPostExternalStudy.push(request);
			}
			if (! mapSampleAcToSampleCode.hasOwnProperty(experiment.sampleAccession)) {
				var request = $http.post(jsRoutes.controllers.sra.samples.api.Samples.save().url,{"accession":experiment.sampleAccession,"_type":"ExternalSample"})
									.catch (error => {
										console.log(error);
										console.log(error.data);
										$scope.messages.addDetails(error.data); 
										return $q.reject(error);                // propage erreur, comme throw 
									});
			allRequestPostExternalSample.push(request);
			}			
		}); // end foreach
	}; // end build_RequestExternalStudyAndExternalSample
	
	
	$scope.submit = function() {
		console.log("Dans update-ctrl.submit");
		$scope.messages = messages();	// enleve le message mais pas la partie details + efficace d'utiliser $scope.messages.clear();
		$scope.messages.clear();
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
		
		
		var allRequestGetStudy = [];
		var allRequestGetSample = [];
		var allRequestPostExternalStudy = [];
		var allRequestPostExternalSample = [];

		// construction des map $scope.mapExpCodeToStudyAc et $scope.mapExpCodeToSampleAc :
		$scope.build_mapExpCodeToStudyAndSample(tab_experiments, $scope.mapExpCodeToStudyAc, $scope.mapExpCodeToSampleAc);

		// construction des requetes pour recuperer les study et samples pour les studyAccession et sampleAccession modifiés par le user: 
		$scope.build_RequestStudyAndSample(tab_experiments, allRequestGetStudy, allRequestGetSample);

		// Execution des requetes pour les study et samples et update de $scope.mapStudyAcToStudyCode pour les studyAc modifiés par utilisateurs :
		$q.all(allRequestGetStudy)
		.then(result => { // resultat de l'ensemble des requetes, avec result[0]=resultat de la requete 1 
			              // avec tableau de data si la requete retourne une liste (url de la forme list) 
			              // et data si la requete retourne un seul resultat(url de la forme get)
			for (var j = 0; j < result.length; j++) {
				for (var k = 0; k < result[j].data.length; k++) {
					var study = result[j].data[k];
					if(study != null) {
						// Mise à jour de $scope.mapStudyAcToStudyCode :
//						console.log("Mise a jour de $scope.mapStudyAcToStudyCode :");
//						console.log("studyCode=", study.code);
//						console.log("studyAccession=", study.accession);
						$scope.mapStudyAcToStudyCode[study.accession] = study.code;	
					}
				}
			}	

			$q.all(allRequestGetSample)
			.then(result => { // resultat de l'ensemble des requetes, avec result[0]=resultat de la requete 1 
				              // avec tableau de data si la requete retourne une liste (url de la forme list) 
				              // et data si la requete retourne un seul resultat(url de la forme get)
				console.log("result.length = ", result.length);
				for (var j = 0; j < result.length; j++) {
					console.log("result[j].data.length = ", result[j].data.length);

					for (var k = 0; k < result[j].data.length; k++) {
						var sample = result[j].data[k];
						if(sample != null) {
							console.log("sample recupere avec !!!!!!!!!!!!!  j=", j);
							console.log("!!!!!!!!!!!!!!!! k=", k);
							
							// Mise à jour de $scope.mapSampleAcToSampleCode :
//							console.log("Mise a jour de $scope.mapSampleAcToSampleCode :");
//							console.log("sampleCode=", sample.code);
//							console.log("sampleAccession=", sample.accession);
							$scope.mapSampleAcToSampleCode[sample.accession] = sample.code;						
						}
					}
				}	

				// Creation des requetes externalStudy et externalSample :
				$scope.build_RequestExternalStudyAndExternalSample(tab_experiments, 
															$scope.mapStudyAcToStudyCode, 
															$scope.mapSampleAcToSampleCode, 
															allRequestPostExternalStudy, 
															allRequestPostExternalSample);
				//console.log("$scope.mapStudyAcToStudyCode = ", $scope.mapStudyAcToStudyCode);
				//console.log("$scope.mapSampleAcToSampleCode = ", $scope.mapSampleAcToSampleCode);
				
				console.log("allRequestPostExternalStudy = ",allRequestPostExternalStudy);
				console.log("allRequestPostExternalSample = ",allRequestPostExternalSample);

				// Lancer creation des externalStudy avec mise à jour de $scope.mapStudyAcToStudyCode
				$q.all(allRequestPostExternalStudy)
				.then(result => { // resultat de l'ensemble des requetes, avec result[0]=resultat de la requete 1 
					              // avec tableau de data si la requete retourne une liste (url de la forme list) 
					              // et data si la requete retourne un seul resultat(url de la forme get)
					for (var j = 0; j < result.length; j++) {
						var study = result[j].data;
						$scope.mapStudyAcToStudyCode[study.accession] = study.code;
						console.log("Apres POST, study.code = ", study.code);
					}	

					// Lancer creation des externalSample avec mise à jour de $scope.mapSampleAcToSampleCode
					$q.all(allRequestPostExternalSample)
					.then(result => { // resultat de l'ensemble des requetes, avec result[0]=resultat de la requete 1 
						              // avec tableau de data si la requete retourne une liste (url de la forme list) 
						              // et data si la requete retourne un seul resultat(url de la forme get)
						for (var j = 0; j < result.length; j++) {
							var sample = result[j].data;
							$scope.mapSampleAcToSampleCode[sample.accession] = sample.code;
							console.log("Apres POST, sample.code = ", sample.code);
						}	

						// Mettre à jour le tableau des experiments pour le studyCode et studySample:
						$scope.update_tab_experiments(tab_experiments, $scope.mapStudyAcToStudyCode, $scope.mapSampleAcToSampleCode);
						console.log("tab_experiments après mise à jour : ", tab_experiments);
						// declencher creation d'un objet submission pour update : // sgas a decommenter en fin de tests
						$scope.build_and_exec_submission(tab_experiments);
						
					}); // fin requete externalSample
				}); // fin requete externalStudy
			}); // fin requete sample		
		}); // fin requete study
		



	}; // end $scope.submit
	
	
}]);
