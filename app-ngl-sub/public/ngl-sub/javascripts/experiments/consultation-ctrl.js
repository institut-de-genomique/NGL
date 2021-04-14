"use strict";

angular.module('home').controller('ConsultationCtrl',[ '$http', '$scope', '$routeParams' , '$q', 'mainService', 'lists', 'tabService','messages','toolsServices', 'services', 'datatable',
	                                                  function($http, $scope, $routeParams, $q, mainService, lists, tabService, messages, toolsServices, services, datatable) { 



			
	var experimentsDTConfig = {
			name:'experimentsDT',
			order :{by:'code',mode:'local', reverse:true},
			search:{
				url:jsRoutes.controllers.sra.experiments.api.Experiments.list()
			},
			pagination:{
				active:false,// important de mettre à false pour modification en masse ou via fichier
				mode:'local',
                numberRecordsPerPage: 100
			},
			select:{active:true},
			showTotalNumberRecords:true,
			edit : {
				active:true, // permettre edition des champs editables
				showButton : true,// bouton d'edition visible
				withoutSelect : true,
				columnMode : true,
				lineMode : function(line){
					if (line.state.code === "SUB-N" && 
						line.traceInformation.createUser === $scope.user)
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
				active:true
			},
			show:{                   // bouton pour epingler si on passe par details-ctrl.js 
				active:true,
				add :function(line){
					tabService.addTabs({label:line.code,href:jsRoutes.controllers.sra.experiments.tpl.Experiments.get(line.code).url,remove:true});
				}
			},
			save : {
				active:true,
				showButton : true,
				changeClass : false,
				url:function(line){
					return jsRoutes.controllers.sra.experiments.api.Experiments.update(line.code).url; // jamais utilisé si mode local
				},
				method:'put',
				mode:'remote',
				value:function(line){
					return line;
				},
				withoutEdit:true,
				callback : function(datatable, errors) {
				}
			}

	};

	
	//---------------------------------------------------------------------------------------------------
	// Initialisation :
	//-----------------
	console.log("Dans experiments.consultation-ctrl.js");

	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('consultation');
		tabService.addTabs({label:Messages('experiments.menu.consultation'),
							href:jsRoutes.controllers.sra.experiments.tpl.Experiments.home("consultation").url,
							remove:true});
		tabService.activeTab(0); //  active l'onglet en le mettant en bleu
	}	

	
	$scope.messages = messages();	
	$scope.form = {};   // important, utilisé dans api.experiments.list puis api.experiments.getQuery
	$scope.form2 = {};  // important, utilise pour determination lignes editable et surcharge données utilisateur

	$scope.saveUserForm = {};
	$scope.lists = lists;		
	$scope.sraVariables = {}; // si on declare dans services => var sraVariables = {};
	$scope.form2.editableStateCode  = "SUB-N"; // utilisé dans api.experiments.getQuery pour selection data 
	$scope.form2.editableCreateUser; // utilisé pour surcharge par userExperimentFile mais instancié dans search

	
//	Initialisation ProjectCodes:
	$scope.lists.refresh.projects();
	
//	Initialisation datatable :
	$scope.experimentsDT = datatable(experimentsDTConfig);
	$scope.experimentsDT.setColumnsConfig(services.getExperimentColumns());

	// Initialisation des etats :
	lists.refresh.states({objectTypeCode: "SRASubmission"});
	$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'miniSimplifiedStates'}})
	.success(function(data) {
		$scope.sraVariables.miniSimplifiedStates = data;	
	});	
//	$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'pseudoStateCodeToStateCodes'}})
//	.success(function(data) {
//		$scope.sraVariables.pseudoStateCodeToStateCodes = data;
//	console.log("CCCCCCCCCCCCCCCCC Dans initListService, sraVariables.pseudoStateCodeToStateCodes pour pseudo-03-EnCours :" , sraVariables.pseudoStateCodeToStateCodes);
//	});
	$http.get(jsRoutes.controllers.sra.api.Variables.get('pseudoStateCodeToStateCodes').url)
	.success(function(data) {
		$scope.sraVariables.pseudoStateCodeToStateCodes = data;
	});
	
	$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'librarySelection'}})
	.success(function(data) {
	// initialisation de la variable sraVariables.librarySelection utilisée dans datatable
		$scope.sraVariables.librarySelection = data;
		//console.log("XXXXXXX   $scope.sraVariables.librarySelection",$scope.sraVariables.librarySelection);
	});	
	// initialisation des liste pour remplir champs library du datatable :
	$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'libraryStrategy'}})
	.success(function(data) {
	// initialisation de la variable sraVariables.libraryStrategy utilisée dans datatable
		$scope.sraVariables.libraryStrategy = data;																									
	});					
	$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'librarySource'}})
	.success(function(data) {
	// initialisation de la variable sraVariables.librarySource utilisée dans datatable
		$scope.sraVariables.librarySource = data;																									
	});	
	
// initialisation typeParser :
	$scope.typeParser = "userExperiment";
// ---------------------------------------------------------------------------------------------------
	
	// Definition des methodes :
	//--------------------------

	$scope.search = function(user){
		//$scope.messages.clear();
		$scope.messages = messages();
		//console.log("$scope.messages = ", $scope.messages);
		//console.log("Dans consultation-ctr.search, avec user", user);
		if (! $scope.form.projCodes        &&
			! $scope.form.codes            &&
			! $scope.form.codeRegex        &&
			! $scope.form.accessionRegex   && 
			! $scope.form.accessions       && 
			! $scope.form.studyAccession   &&
			! $scope.form.sampleAccession  &&
			! $scope.form.pseudoStateCodes ) {
				//console.log("Aucun parametre => envoyer message erreur");
				$scope.messages.setError(Messages("AbsenceSearchParameter"));
				//console.log("$scope.messages = ", $scope.messages);
				throw("Aucun parametre pour la recherche des samples");   
			} 
		$scope.user = user;
		// plus prudent de recuperer editableCreateUser ici car pas forcement instanciable 
		// à l'initialisation
		$scope.form2.editableCreateUser = user; // utilisé pour surcharge utilisateur

		$scope.base64UserFileExperiment=""; 
		//console.log("userFileExperiment=", $scope.form2.userFileExperiment);

		//console.log("userFileExperiment.value=", $scope.userFileExperiment.value);

		if (toolsServices.isNotBlank($scope.form2.userFileExperiment) &&
			toolsServices.isNotBlank($scope.form2.userFileExperiment.value)) {
				console.log("Recuperation de $scope.base64UserFileExperiment");
				$scope.base64UserFileExperiment = $scope.form2.userFileExperiment.value;
		} 
		
		// Remplacement des pesudoStateCodes par codes stateCodes dans le formulaire et destruction
		// de form.pseudoStateCodes pour pouvoir envoyer directement form a la methode search du datatable:
		$scope.saveUserForm = angular.copy($scope.form);// sauver formulaire utilisateur initial avant modif dans replacePseudoStateCodesToStateCodesInFormulaire
		toolsServices.replacePseudoStateCodesToStateCodesInFormulaire($scope.sraVariables.pseudoStateCodeToStateCodes, $scope.form);
		$scope.experimentsDT.search($scope.form, $scope.errorCallback);
		$scope.form = angular.copy($scope.saveUserForm);// remettre formulaire utilisateur initial
		// chargement des experiments de la base :
		$http.get(jsRoutes.controllers.sra.experiments.api.Experiments.list().url,{params: $scope.form})
		.success(function(data) {
			$scope.experiments_db = data;
			
			// ajout données utilisateurs si besoin :
			$scope.mapUserExperiments = null;
			//$scope.tab_final_experiments = null;
			if(toolsServices.isNotBlank($scope.base64UserFileExperiment)) {
				services.loadUserExperimentInfosAndAdd2ExperimentsDT($scope.typeParser, $scope.base64UserFileExperiment, $scope.experiments_db, 
																   $scope.mapUserExperiments, $scope.form2.editableCreateUser, 
																   $scope.form2.editableStateCode, $scope.experimentsDT, $scope.messages)
			} else {
				$scope.experimentsDT.setData($scope.experiments_db, $scope.experiments_db.length);
			}
		}).error(function(error) {
			console.log("error : ", error);
			$scope.messages.addDetails(error);
			$scope.messages.setError("PROBLEME lors de la recuperation des experiments de la base de données :");
		});
		
	};
		
	$scope.reset = function() {
		$scope.form = {};
		$scope.tab_experiments = []; 
		// $scope.experimentsDT = null; // ne marche pas datatable
		$scope.experimentsDT = datatable(experimentsDTConfig);
		$scope.experimentsDT.setColumnsConfig(services.getExperimentColumns());
		$scope.messages = messages();
		$scope.messages.clear();
		$scope.form2.editableStateCode ="SUB-N";
		if(toolsServices.isNotBlank($scope.form2.userFileExperiment)){
			$scope.form2.userFileExperiment = null;
		} 
	};
	
}

]);