"use strict";

angular.module('home').controller('UpdateCtrl',
		  [ '$http', '$scope', '$routeParams' , '$q', 'mainService', 'lists', 'tabService', 'messages', 'toolsServices', 'services', 'datatable',
	function($http,   $scope,   $routeParams,    $q,   mainService,   lists,   tabService,  messages, toolsServices, services, datatable) { 


	var samplesDTConfig = {
			name:'samplesDT',
			order :{by:'code',mode:'local', reverse:true},
			search:{
				url:jsRoutes.controllers.sra.samples.api.Samples.list()
			},
			pagination:{
				active:false,
				mode:'local'
			},
			select:{active: true,
					withEdit : false,
					showButton: true
					},
			showTotalNumberRecords:true,
			edit : {
				active:true, // permettre edition des champs editables
				showButton : true,// bouton d'edition visible sur chaque colonne
				withoutSelect : true,
				columnMode : true,
				lineMode : function(line) {
					if(line.state.code === "SUB-F" // modification autorisee seulement pour les samples avec AC et non engagés dans une soumission en cours
					   && line._type === "Sample"  // On n'edite pas les ExternalSample
					   && line.traceInformation.createUser === $scope.user) // limiter autorisation de modification au proprietaire.
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
				showButton : false,
			},
			/*show:{                   // bouton pour epingler si on passe par details-ctrl.js 
				active:true,
				add :function(line){
					tabService.addTabs({label:line.code,href:jsRoutes.controllers.sra.samples.tpl.Samples.get(line.code).url,remove:true});
				}
			},*/
			save : {
				active:true,
				showButton : false,
				changeClass : false,
				url:function(line){
					return jsRoutes.controllers.sra.samples.api.Samples.update(line.code).url; // jamais utilisé si mode local
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
//				withEdit : true, //Allow to remove a line in edition mode
//				showButton : true,//Show the remove button in the toolbar
//				mode : 'local', //Remove mode
//				callback : function(datatable, errorsNumber) {
//				}, //Callback after remove all element. 
//			},
			// on ne peut pas mettre de mode local sur otherButtons
			otherButtons :{
				  active:true,
				  template:'<button class="btn btn-primary" ng-click="toolsServices.poubelleBleue(samplesDT)"  ng-disabled="datatable.isEmpty()" title="'+Messages("Retirer les lignes selectionnées du tableau")+'"><i class="fa fa-trash" ></i></button>'
			},
		};
	
//---------------------------------------------------------------------------------------------------


	// Initialisation :
	//-------------------
	
	console.log("Dans samples.update-ctrl.js");
	// mettre toolsServices dans $scope pour pouvoir y acceder depuis la config du datatable 
	$scope.toolsServices = toolsServices;
	$scope.messages = messages();	
	$scope.form = {};  // important. 
	$scope.lists = lists;
	
	//sgas ajout
	$scope.messages = messages();	
	$scope.form = {};   // important, utilisé dans api.samples.list puis api.samples.getQuery
	$scope.form2 = {};  // important, utilise pour determination lignes editable et surcharge données utilisateur
	$scope.lists = lists;
	$scope.form.stateCode ="SUB-F";  
	$scope.sraVariables = {};
	$scope.form.createUser = $scope.user;    
	// Operation update possible uniquement sur Sample (pas sur ExternalSample) 
	// dans etat SUB-F pour utilisateur proprietaire:
	$scope.form2.editableCreateUser = $scope.user; // plus prudent d'initialiser dans search.
	$scope.form2.editableStateCode = "SUB-F"; 
	$scope.form2.editableType = "Sample"; // important de ne pas proposer ExternalSample pour update 
	//end sgas ajout

	$scope.form.stateCode  = "SUB-F";
	$scope.form.type       = "Sample"; // important de ne pas proposer ExternalSample pour update 
	
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('update');
		tabService.addTabs({label:Messages('samples.menu.update'),
							href:jsRoutes.controllers.sra.samples.tpl.Samples.home("update").url,
							remove:true});
		tabService.activeTab(0); // active l'onglet en le mettant en bleu
	}

	
//	Initialistation ProjectCodes:
	$scope.lists.refresh.projects();

//	Initialisation datatable :
	$scope.samplesDT = datatable(samplesDTConfig);
	$scope.samplesDT.setColumnsConfig(services.getSampleColumns());
 

//---------------------------------------------------------------------------------------------------

	// Definition methodes:
	//-----------------------
	
	
	// methode appelée depuis la vue avec la directive ng-init
	$scope.setUserInScope = function(user) {
		$scope.user = user;
		//console.log("Dans setUserInScope, user= ", user);
	};
	
	$scope.search = function(user) {
		//console.log("Dans update-ctrl.search");
		$scope.messages = messages();
		//console.log("$scope.messages = ", $scope.messages);
		
		if (! $scope.form.externalIdRegex && 
			! $scope.form.externalIds     &&
		    ! $scope.form.accessionRegex  && 
			! $scope.form.accessions      && 
			! $scope.form.codeRegex       &&
			! $scope.form.codes           &&
			! $scope.form.projCodes  ) {
			//console.log("Aucun parametre => envoyer message erreur");
			$scope.reset(); // a mettre avant setError car annule messages
			$scope.messages.setError(Messages("AbsenceSearchParameter"));
			//console.log("update-ctrl.$scope.messages = ", $scope.messages);
			// annuler spinner si besoin
			
			throw("Aucun parametre pour la recherche des samples à updater");   
		} 
		$scope.form.createUser = $scope.user;
		$scope.form2.editableCreateUser = $scope.user;
		$scope.form2.editableStateCode = "SUB-F";
		$scope.form2.editableCreateUser = $scope.user;// pour rendre surchageable par userSampleFile

		//console.log("update-ctrl.search : ok j'ai des parametres");
		//console.log("update-ctrl.$scope.user = ", $scope.user); // recuperation de la variable user depuis formulaire et setUserInScope
		//console.log("$scope.form = ", $scope.form);
		

		$scope.base64UserFileSample=""; 
		//console.log("userFileSample=", $scope.form2.userFileSample);

		//console.log("dans search, $scope.form2", $scope.form2);
		if (toolsServices.isNotBlank($scope.form2.userFileSample) &&
			toolsServices.isNotBlank($scope.form2.userFileSample.value)) {
				console.log("Recuperation de $scope.base64UserFileSample");
				$scope.base64UserFileSample = $scope.form2.userFileSample.value;
		}
		// Remplacement des pesudoStateCodes par codes stateCodes dans le formulaire et destruction
		// de form.pseudoStateCodes pour pouvoir envoyer directement form a la methode search ou save du datatable:
		$scope.saveUserForm = angular.copy($scope.form);// sauver formulaire utilisateur initial avant modif dans replacePseudoStateCodesToStateCodesInFormulaire
		toolsServices.replacePseudoStateCodesToStateCodesInFormulaire($scope.sraVariables.pseudoStateCodeToStateCodes, $scope.form);
		$scope.formSearch = angular.copy($scope.form);
		$scope.formUpdate = angular.copy($scope.form);
		
		$scope.form = {};
		console.log("$scope.form" ,$scope.form);
		console.log("$scope.formSearch" ,$scope.formSearch);

		// Charger le bon form pour le datatable :
		mainService.setForm($scope.formSearch);
		
		// chargement des samples de la base :
		$http.get(jsRoutes.controllers.sra.samples.api.Samples.list().url,{params: $scope.formSearch})
		.success(function(data) {
			$scope.samples_db = data;
				
			// ajout données utilisateurs si besoin :
			$scope.mapUserSamples = null;
			//$scope.tab_final_samples = null;
			if(toolsServices.isNotBlank($scope.base64UserFileSample)) {
				services.loadUserSampleInfosAndAdd2SamplesDT($scope.base64UserFileSample, $scope.samples_db, 
															 $scope.mapUserSamples, $scope.form2.editableCreateUser, 
															 $scope.form2.editableStateCode, $scope.form2.editableType, 
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
		//$scope.samplesDT = null;    // ne marche pas pour effacer datatable
		$scope.samplesDT = datatable(samplesDTConfig);
		$scope.samplesDT.setColumnsConfig(services.getSampleColumns());
		$scope.samplesDT.setData([], 0);
		$scope.messages = messages();
		$scope.messages.clear();
		$scope.form.stateCode ="SUB-F";
		$scope.form.type="Sample"; // important de ne pas proposer ExternalSample pour update 
		$scope.form2 = {};
		$scope.form2.editableStateCode = "SUB-F";
		$scope.form2.editableCreateUser = $scope.user;// pour rendre surchageable par userSampleFile
		$scope.form2.editableType = "Sample";
	};
	
	
	$scope.submit = function() {
		$scope.messages = messages();
		console.log("Dans update-ctrl.submit");
		mainService.setForm($scope.formUpdate);
		// sauvegarde des données en local (mode local le save de getColumnSample) 
		// (si mode remote, les donnees sont bien sauvées dans base avec valeurs editées mais 
		// le datatable utilisé ici ne contient pas les modifs des utilisateurs 
		// => en mode remote, dans le datatable, le modele n'est pas synchronisé avec la vue.
		// En mode local, dans le datatable, le modele est synchronisé avec la vue et contient bien 
		// les modifications des utilisateurs, en revanche pas de sauvegarde dans la base !
		$scope.samplesDT.save(); 
		var tab_samples = $scope.samplesDT.getData();
		if(! tab_samples || tab_samples.length < 1) {
			$scope.reset();
			$scope.messages.setError(Messages("AbsenceDonneePourUpdate"));
			console.log("$scope.messages = ", $scope.messages);
			throw(Messages("AbsenceDonneePourUpdate"));
		}
		//console.log("taille de tab_samples: ", tab_samples.length);
		
		// declencher creation d'un objet submission pour update :
		
		var args = {
			project     : null,
			study       : null,
			samples     : tab_samples,
			experiments : []
		};
		
		// declencher creation d'un objet submission pour update :
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
				$scope.samplesDT = datatable(samplesDTConfig);
				$scope.samplesDT.setColumnsConfig(services.getSampleColumns());
				for(var i= 0; i < tab_samples.length; i++){
					tab_codes.push(tab_samples[i].code);
				}
				$scope.samplesDT.search({codes:tab_codes});		
			}).error(function(error) {
				// testé avec sample deja dans soumission => ok  
				 console.log("ZZZZZZZZZZZZZZ dans error,  error= ", error);
				 $scope.messages.addDetails(error);
				 $scope.messages.setError("Probleme pour le changement de status de la soumission");
				 // var submissionCode = error.data.code;
				 //$scope.messages.setError("Soumission " + submissionCode + " qui n'a pas pu
				 // etre mise dans le bon etat, mais sample sauve dans base avec ses
				 // modifications");
			});
		}).error(function(error) {
			console.log("YYYYYYYYYYYYYYYYYYYYYYYYY dans error ");
				console.log("error = ", error);
				$scope.messages.addDetails(error); 
				$scope.messages.setError("Erreur creation de la soumission"); 
		});
		
	}; // end $scope.submit
	
}]);
