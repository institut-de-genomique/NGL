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
				active: true
			},
			showTotalNumberRecords: true,
			edit: {
				active: true, // permettre edition des champs editables
				showButton: true, // bouton d'edition visible
				withoutSelect: true,
				columnMode: true,
				lineMode: function (line) {
					if(line.state.code === "SUB-N" // modification autorisee seulement pour les samples nouvellement crees.
					   && line._type === "Sample")  // On n'edite pas les ExternalSample
					   //&& line.traceInformation.createUser === $scope.user) // ne pas limiter autorisation de modification au proprietaire.
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
			show:{                   // bouton pour epingler si on passe par details-ctrl.js 
				active:true,
				add :function(line){
					// ajout onglet avec nom du sample permettant d'acceder à la vue details
					tabService.addTabs({label:line.code,
										href:jsRoutes.controllers.sra.samples.tpl.Samples.get(line.code).url,
										remove:true // permet fermer onglet
										});
					// inutile d'installer le datatable renseigné dans mainService car fait a l'initialisation et passage par adresse. 
					// modifications de $scope.samplesDT prises en compte dans mainService
					// mainService.setDatatable($scope.samplesDT);
				}
			},
			save: {
				active: true,
				showButton: true,
				changeClass: true, // save en echec et save en succes auront couleurs differentes
				url: function (line) {
					return jsRoutes.controllers.sra.samples.api.Samples.update(line.code).url; // utilise uniquement en mode remote
				},
				method: 'put',
				mode: 'remote',
				value: function (line) {
					return line;
				},
				withoutEdit:true,
				callback: function (datatable, errors) {
				}
			}

		};

		
//---------------------------------------------------------------------------------------------------

		// Initialisations :
		//--------------------

		//console.log("Dans samples.consultation-ctrl.js");				
			
		$scope.messages = messages();
		$scope.messages.clear();	
			
		$scope.form = {};   // important, utilisé dans api.samples.list puis api.samples.getQuery
		$scope.form2 = {};   // important, utilisé pour recuperer userFileSample
		//$scope.treeLoadInProgress = false;

		// Recuperer form depuis mainService si existe  :
		// mais ne pas installer $scope.form dans mainService s'il n'existe pas dans mainService, 
		// car methode mainService.setForm($scope.form) qui fait une copie par valeur 
		// => si modifications ulterieures du $scope.form, le form de mainService ne sera pas modifié
		// => installer la copie de $scope.form dans mainService dans methode $scope.search()
		
		if(angular.isDefined(mainService.getForm())) {
        	$scope.form = mainService.getForm();
    	} 
	
		$scope.lists = lists;		
		$scope.sraVariables = {}; // si on declare dans services => var sraVariables = {};
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
		$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'miniSimplifiedStatesWithNone'}})
		.success(function(data) {
			$scope.sraVariables.miniSimplifiedStatesWithNone = data;	
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
		// initialisation typeParser :
		$scope.typeParser = "userSample";
		
//---------------------------------------------------------------------------------------------------

	    // Definitions methodes :
		//------------------------
		
		$scope.search = function(user){
			$scope.messages = messages();
			$scope.messages.clear();
			//$scope.treeLoadInProgress = true;
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
			
		mainService.setForm($scope.form);
		$scope.user = user;
		// plus prudent de recuperer editableCreateUser ici car pas forcement instanciable 
		// à l'initialisation
		// retrait controle utilisateur :
		//$scope.form2.editableCreateUser = user; // utilisé pour surcharge utilisateur
		
		$scope.base64UserFileSample=""; 
		//console.log("userFileSample=", $scope.form2.userFileSample);
	
		$scope.editableStateCode  = "SUB-N"; // utilisé dans api.samples.getQuery pour selection data 
		//$scope.editableCreateUser; // utilisé pour surcharge par userSampleFile mais instancié dans search
		
		if (toolsServices.isNotBlank($scope.form2.userFileSample) &&
			toolsServices.isNotBlank($scope.form2.userFileSample.value)) {
				//console.log("Recuperation de $scope.base64UserFileSample");
				$scope.base64UserFileSample = $scope.form2.userFileSample.value;
				//delete $scope.form.userFileSample; // pas besoin car userFileSample est dans form2
		} 
		
		// Remplacement des pseudoStateCodes par codes stateCodes dans le formulaire et destruction
		// de form.pseudoStateCodes pour pouvoir envoyer directement form a la methode search du datatable:
		$scope.saveUserForm = angular.copy($scope.form);  // sauver formulaire utilisateur initial avant modif dans replacePseudoStateCodesToStateCodesInFormulaire
		toolsServices.replacePseudoStateCodesToStateCodesInFormulaire($scope.sraVariables.pseudoStateCodeToStateCodes, $scope.form);
		$scope.samplesDT.setSpinner(true);
		$http.get(jsRoutes.controllers.sra.samples.api.Samples.list().url,{params: $scope.form})
			.success(function(data) {
				$scope.samples_db = data;
				//console.log("scope.samples_db", $scope.samples_db);
				// ajout données utilisateurs si besoin :
				$scope.mapUserSamples = null;
				$scope.typeParser = "userSample";
				//$scope.tab_final_samples = null;
				if(toolsServices.isNotBlank($scope.base64UserFileSample)) {
					services.loadUserSampleInfosAndAdd2SamplesDT($scope.typeParser, $scope.base64UserFileSample, $scope.samples_db, 
																 $scope.mapUserSamples, $scope.user, 
																 $scope.editableStateCode, $scope.samplesDT, $scope.messages);
					//console.log("$scope.mapUserSamples", $scope.mapUserSamples);
				} else {
					$scope.samplesDT.setData($scope.samples_db, $scope.samples_db.length);
				}
				// remettre le formulaire en etat si surchargé par l'utilisateur pour nouveau search
				$scope.form = angular.copy($scope.saveUserForm);
				$scope.samplesDT.setSpinner(false);

			}).error(function(data) {
				//$scope.treeLoadInProgress = false;
				
				$scope.samplesDT.setSpinner(false);
				console.log("error HHHHHHHHHHHHHHHHHHHHH   : ", data);
				//$scope.messages.setError("save");
				$scope.messages.addDetails(data);
				//$scope.messages.setError("PROBLEME lors de la recuperation des samples de la base de données :");
				$scope.messages.setError("save");
				$scope.form = angular.copy($scope.saveUserForm);

			});
			//$scope.treeLoadInProgress = false;

		};
			
			
		$scope.reset = function() {
			//$scope.treeLoadInProgress = false;
			$scope.form = {};
			$scope.tab_samples = []; 
			$scope.samplesDT = datatable(samplesDTConfig);
			$scope.samplesDT.setColumnsConfig(services.getSampleColumns());
			$scope.messages = messages();
			$scope.messages.clear();
			$scope.editableStateCode = "SUB-N";
			$scope.typeParser = "userSample";
			if(toolsServices.isNotBlank($scope.form2.userFileSample)){
				$scope.form2.userFileSample = null;
			} 
			$scope.form2 = {};
		};
		
		$scope.errorCallback = function(error) {
			console.log("error XXXXX: ", error);
			$scope.messages.addDetails(error.data);
			$scope.messages.setError("PROBLEME dans userFileSample :");
			//$scope.treeLoadInProgress = false;
		};
		
		// NGL-4235 
		$scope.isShowConsigne=false;
		$scope.toggleShowConsigne=function(){
			//console.log("TOGGLE search-ctrl.js");
		if ( $scope.isShowConsigne===false) { $scope.isShowConsigne=true}
		else {$scope.isShowConsigne=false}
		};
		
	}

]);