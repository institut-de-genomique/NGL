/* global angular, jsRoutes, Messages */

"use strict";

angular.module('home').controller('ConsultationCtrl', ['$http', '$scope', '$routeParams', '$q', 'mainService', 'lists', 'tabService', 'messages', 'toolsServices', 'services', 'datatable',
	function ($http, $scope, $routeParams, $q, mainService, lists, tabService, messages, toolsServices, services, datatable) {
	
	
		var samplesDTConfig = {
			name: 'samplesDT',
			order: {
				by: 'code',
				mode: 'local',
				reverse: true
			},
			search: {
				url: jsRoutes.controllers.sra.samples.api.Samples.list()
			},
			pagination: {
				active: false, // important de mettre à false pour modification en masse ou via fichier
				mode: 'local',
				numberRecordsPerPage: 100
			},
			select: {
				active: false
			},
			showTotalNumberRecords: true,
			edit: {
				active: true, // permettre edition des champs editables
				showButton: true, // bouton d'edition visible
				withoutSelect: true,
				columnMode: true,
				lineMode: function (line) {
					if(line.state.code === "SUB-N" // modification autorisee seulement pour les samples nouvellement crees.
					   && line._type === "Sample"  // On n'edite pas les ExternalSample
					   && line.traceInformation.createUser === $scope.user) // limiter autorisation de modification au proprietaire.
					   return true;
					else 
					   return false;
				}
			},

			cancel: {
				showButton: true
			},
			hide: {
				active: true,
				showButton: true
			},
			exportCSV: {
				active: true
			},
			/*show:{                   // bouton pour epingler si on passe par details-ctrl.js 
				active:true,
				add :function(line){
					tabService.addTabs({label:line.code,href:jsRoutes.controllers.sra.samples.tpl.Samples.get(line.code).url,remove:true});
				}
			},*/

			save: {
				active: true,
				showButton: true,
				changeClass: false,
				url: function (line) {
					return jsRoutes.controllers.sra.samples.api.Samples.update(line.code).url; // jamais utilisé si mode local
				},
				method: 'put',
				mode: 'remote',
				value: function (line) {
					return line;
				},
				withoutEdit:true,
				callback: function (datatable, errors) {}
			}

		};

		
//---------------------------------------------------------------------------------------------------

		// Initialisations :
		//--------------------

		console.log("Dans samples.consultation-ctrl.js");				
			
		$scope.messages = messages();	
		$scope.form = {};   // important, utilisé dans api.samples.list puis api.samples.getQuery
		$scope.form2 = {};  // important, utilise pour determination lignes editable et surcharge données utilisateur

		$scope.saveUserForm = {};
		$scope.lists = lists;		
		$scope.sraVariables = {}; // si on declare dans services => var sraVariables = {};
		$scope.form2.editableStateCode  = "SUB-N"; // utilisé dans api.samples.getQuery pour selection data 
		$scope.form2.editableCreateUser; // utilisé pour surcharge par userSampleFile mais instancié dans search
		$scope.form2.editableType = "Sample";
		if (angular.isUndefined(mainService.getHomePage())) {
			mainService.setHomePage('consultation');
			tabService.addTabs({
				label: Messages('samples.menu.consultation'),
				href: jsRoutes.controllers.sra.samples.tpl.Samples.home("consultation").url,
				remove:true});
			tabService.activeTab(0); //  active l'onglet en le mettant en bleu
		}		
		
//		Initialisation ProjectCodes:
		$scope.lists.refresh.projects();
		
//		Initialisation datatable :
		$scope.samplesDT = datatable(samplesDTConfig);
		$scope.samplesDT.setColumnsConfig(services.getSampleColumns());

		// Initialisation des etats :
		lists.refresh.states({objectTypeCode: "SRASubmission"});
		$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'miniSimplifiedStates'}})
		.success(function(data) {
			$scope.sraVariables.miniSimplifiedStates = data;	
		});	
//		$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'pseudoStateCodeToStateCodes'}})
//		.success(function(data) {
//			$scope.sraVariables.pseudoStateCodeToStateCodes = data;
//		console.log("CCCCCCCCCCCCCCCCC Dans initListService, consultationService.sraVariables.pseudoStateCodeToStateCodes pour pseudo-03-EnCours :" , consultationService.sraVariables.pseudoStateCodeToStateCodes);
//   	});
		$http.get(jsRoutes.controllers.sra.api.Variables.get('pseudoStateCodeToStateCodes').url)
		.success(function(data) {
			$scope.sraVariables.pseudoStateCodeToStateCodes = data;
		});
		
		
//---------------------------------------------------------------------------------------------------

	    // Definitions methodes :
		//------------------------
		
		$scope.search = function(user){
			//$scope.messages.clear();
			$scope.messages = messages();
			//console.log("$scope.messages = ", $scope.messages);
			//console.log("Dans consultation-ctr.search, avec user", user);
			if (! $scope.form.externalIdRegex  && 
				! $scope.form.externalIds      &&
				! $scope.form.accessionRegex   && 
				! $scope.form.accessions       && 
				! $scope.form.codeRegex        &&
				! $scope.form.codes            &&
				! $scope.form.pseudoStateCodes &&
				! $scope.form.projCodes ) {
					//console.log("Aucun parametre => envoyer message erreur");
					$scope.messages.setError(Messages("AbsenceSearchParameter"));
					//console.log("$scope.messages = ", $scope.messages);
					throw("Aucun parametre pour la recherche des samples");   
				} 
			$scope.user = user;
			$scope.form2.editableCreateUser = user; // utilisé pour surcharge utilisateur
			$scope.base64UserFileSample=""; 
			//console.log("userFileSample=", $scope.form2.userFileSample);

//			// Remplacement des pseudoStateCodes par codes stateCodes dans le formulaire et destruction
//			// de form.pseudoStateCodes pour pouvoir envoyer directement form a la methode search du datatable:
//			$scope.saveUserForm = angular.copy($scope.form);// sauver formulaire utilisateur initial avant modif dans replacePseudoStateCodesToStateCodesInFormulaire
//			toolsServices.replacePseudoStateCodesToStateCodesInFormulaire($scope.sraVariables.pseudoStateCodeToStateCodes, $scope.form);
//			$scope.samplesDT.search($scope.form);
//			$scope.form = angular.copy($scope.saveUserForm);// remettre formulaire utilisateur initial
			// chargement des samples de la base :
			$http.get(jsRoutes.controllers.sra.samples.api.Samples.list().url,{params: $scope.form})
			.success(function(data) {
				$scope.samples_db = data;
				
				// ajout données utilisateurs si besoin :
				$scope.mapUserSamples = null;
				if (toolsServices.isNotBlank($scope.form2.userFileSample) &&
					toolsServices.isNotBlank($scope.form2.userFileSample.value)) {
						//console.log("Recuperation de $scope.base64UserFileSample");
						$scope.base64UserFileSample = $scope.form2.userFileSample.value;
				} 
					
				//console.log("Alors $scope.base64UserFileSample", $scope.base64UserFileSample);
				//console.log("JJJJJJJJJJJJ $scope.form2.editableType : ", $scope.form2.editableType);
				if(toolsServices.isNotBlank($scope.base64UserFileSample)) {
					//console.log("Appel de loadUserSampleInfosAndAdd2SamplesDT");
					services.loadUserSampleInfosAndAdd2SamplesDT($scope.base64UserFileSample, $scope.samples_db, $scope.mapUserSamples,
																 $scope.form2.editableCreateUser, $scope.form2.editableStateCode, $scope.form2.editableType,
																 $scope.samplesDT, $scope.messages)
				} else {
					$scope.samplesDT.setData($scope.samples_db, $scope.samples_db.length);
				}
			}).error(function(error) {
				console.log("error : ", error);
				$scope.messages.addDetails(error);
				$scope.messages.setError("PROBLEME lors de la recuperation des samples de la base de données :");
			});
			
		};
			
		
		$scope.reset = function() {
			$scope.form = {};
			$scope.tab_samples = []; 
			$scope.samplesDT = datatable(samplesDTConfig);
			$scope.samplesDT.setColumnsConfig(services.getSampleColumns());
			$scope.messages = messages();
			$scope.messages.clear();
			$scope.form2.editableStateCode ="SUB-N";
			if(toolsServices.isNotBlank($scope.form2.userFileSample)){
				$scope.form2.userFileSample = null;
			} 
		};
		
	}

]);