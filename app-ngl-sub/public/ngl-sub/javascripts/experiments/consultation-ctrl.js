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
					if (line.state.code === "SUB-N" )
						//&& line.traceInformation.createUser === $scope.user) // retrait du controle utilisateur.
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
				changeClass : true, // pour mettre les lignes bien sauvegardees en verts et celles en echec en rouge
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
	//console.log("Dans experiments.consultation-ctrl.js");

	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('consultation');
		tabService.addTabs({label:Messages('experiments.menu.consultation'),
							href:jsRoutes.controllers.sra.experiments.tpl.Experiments.home("consultation").url,
							remove:true});
		tabService.activeTab(0); //  active l'onglet en le mettant en bleu
	}	

	$scope.messages = messages();	// enleve le message mais pas la partie details + efficace d'utiliser $scope.messages.clear();
	$scope.messages.clear();	
	$scope.form = {};   // important, utilisé dans api.experiments.list puis api.experiments.getQuery
	////$scope.treeLoadInProgress = false;
	// Attention on n'arrive pas à conserver le userFileExperiment quand on recupere le form de mainService.getForm()
	if(angular.isDefined(mainService.getForm())) {
        $scope.form = mainService.getForm();
    } 
	

	$scope.lists = lists;		
	$scope.sraVariables = {}; // si on declare dans services => var sraVariables = {};

	
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
	////$scope.treeLoadInProgress = false;
// ---------------------------------------------------------------------------------------------------
	
	// Definition des methodes :
	//--------------------------

	$scope.search = function(user){
		$scope.messages = messages();	// enleve le message mais pas la partie details + efficace d'utiliser $scope.messages.clear();
		$scope.messages.clear();
		////$scope.treeLoadInProgress = true;
		//console.log("$scope.messages = ", $scope.messages);
		//console.log("Dans consultation-ctr.search, avec user", user);
		if (! $scope.form.projCodes        &&
			! $scope.form.codes            &&
			! $scope.form.codeRegex        &&
			! $scope.form.accessionRegex   && 
			! $scope.form.accessions       && 
			! $scope.form.studyCode        &&
			! $scope.form.studyIdentifier  &&
			! $scope.form.sampleCode       &&
			! $scope.form.sampleIdentifier &&
			! $scope.form.pseudoStateCodes ) {
				//console.log("Aucun parametre => envoyer message erreur");
				$scope.messages.setError(Messages("AbsenceSearchParameter"));
				//console.log("$scope.messages = ", $scope.messages);
				////$scope.treeLoadInProgress = false;
				throw("Aucun parametre pour la recherche des experiments");   
			} 
		

		mainService.setForm($scope.form);
		$scope.user = user;
		// plus prudent de recuperer editableCreateUser ici car pas forcement instanciable 
		// à l'initialisation
		// retrait controle utilisateur :
		//$scope.form2.editableCreateUser = user; // utilisé pour surcharge utilisateur
		
		$scope.base64UserFileExperiment=""; 
		//console.log("userFileExperiment=", $scope.form2.userFileExperiment);

		$scope.editableStateCode  = "SUB-N"; // utilisé dans api.samples.getQuery pour selection data 
		//$scope.editableCreateUser; // utilisé pour surcharge par userSampleFile mais instancié dans search
		
		if (toolsServices.isNotBlank($scope.form.userFileExperiment) &&
			toolsServices.isNotBlank($scope.form.userFileExperiment.value)) {
				//console.log("Recuperation de $scope.base64UserFileExperiment");
				$scope.base64UserFileExperiment = $scope.form.userFileExperiment.value;
				delete $scope.form.userFileExperiment;
		} 
		
		
		// Remplacement des pseudoStateCodes par codes stateCodes dans le formulaire et destruction
		// de form.pseudoStateCodes pour pouvoir envoyer directement form a la methode search du datatable:
		$scope.saveUserForm = angular.copy($scope.form);// sauver formulaire utilisateur initial avant modif dans replacePseudoStateCodesToStateCodesInFormulaire
		toolsServices.replacePseudoStateCodesToStateCodesInFormulaire($scope.sraVariables.pseudoStateCodeToStateCodes, $scope.form);
		console.log("scope.form", $scope.form);

		$scope.experimentsDT.setSpinner(true);
		// chargement des experiments de la base :
		//console.log("scope.form", $scope.form);
		$http.get(jsRoutes.controllers.sra.experiments.api.Experiments.list().url,{params: $scope.form})
		.success(function(data) {
			$scope.experiments_db = data;
			//console.log("scope.experiments_db", $scope.experiments_db);
			// ajout données utilisateurs si besoin :
			$scope.mapUserExperiments = null;
			//$scope.tab_final_experiments = null;
			if(toolsServices.isNotBlank($scope.base64UserFileExperiment)) {
				services.loadUserExperimentInfosAndAdd2ExperimentsDT($scope.typeParser, $scope.base64UserFileExperiment, $scope.experiments_db, 
																   	 $scope.mapUserExperiments, $scope.user, 
																     $scope.editableStateCode, $scope.experimentsDT, $scope.messages);
			} else {
				$scope.experimentsDT.setData($scope.experiments_db, $scope.experiments_db.length);
			}
			$scope.experimentsDT.setSpinner(false);
			$scope.form = angular.copy($scope.saveUserForm);// remettre formulaire utilisateur initial

		}).error(function(error) {
			////$scope.treeLoadInProgress = false;
			console.log("error : ", error);
			$scope.messages.addDetails(error);
			$scope.messages.setError("PROBLEME lors de la recuperation des experiments de la base de données :");
			$scope.experimentsDT.setSpinner(false);
			$scope.form = angular.copy($scope.saveUserForm);// remettre formulaire utilisateur initial
		});
		////$scope.treeLoadInProgress = false;
	};
		
	$scope.reset = function() {
		$scope.form = {};
		$scope.tab_experiments = []; 
		////$scope.treeLoadInProgress = false;
		// $scope.experimentsDT = null; // ne marche pas datatable
		$scope.experimentsDT = datatable(experimentsDTConfig);
		$scope.experimentsDT.setColumnsConfig(services.getExperimentColumns());
		$scope.messages = messages();	// enleve le message mais pas la partie details + efficace d'utiliser $scope.messages.clear();
		$scope.messages.clear();
		$scope.editableStateCode ="SUB-N";
		if(toolsServices.isNotBlank($scope.form.userFileExperiment)){
			$scope.userFileExperiment = null;
		} 
	};
	
	$scope.errorCallback = function(error) {
		console.log("error : ", error);
		$scope.messages.addDetails(error.data);
		$scope.messages.setError("PROBLEME dans userFileExperiment :");
	};
	
}

]);