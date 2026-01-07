"use strict";

angular.module('home').controller('UpdateCtrl',
		[ '$http', '$scope', '$routeParams' , '$q', 'mainService', 'lists', 'tabService', 'messages', 'toolsServices', 'datatable',
  function($http,   $scope,   $routeParams,    $q,   mainService,   lists,   tabService,  messages, toolsServices, datatable) { 


	$scope.getStudyColumns = function() {
		var columns = [];
			columns.push({
						property     : "traceInformation.creationDate",
						header       : Messages("traceInformation.creationDate"),
						type         : "date",		    	  	
						order        : true,
						edit         : false,
						choiceInList : false  
						});
			columns.push({
						property     : "traceInformation.createUser",
						header       : Messages("traceInformation.creationUser"),
						type         : "date",		    	  	
						order        : true,
						edit         : false,
						choiceInList : false  
						});
			columns.push({
						property     : "state.code",
						header       : Messages("study.state.code"),
						"filter"     : "codes:'state'",
						type         : "text",		    	  	
						order        : true
						});	
			columns.push({
						property   : "code",
		  	        	header     : Messages("study.code"),
				       	type       : "text",		    	  	
				       	order      : true
				        });	
			columns.push({
						property     : "projectCodes",
				       	header       : Messages("study.projectCodes"),
				       	type         : "text",		    	  	
				       	order        : false,
				       	edit         : false,
				       	choiceInList : false  
				       	}); 	
			columns.push({
						property     : "firstSubmissionDate",
						header       : Messages("firstSubmissionDate"),
						type         : "date",		    	  	
						order        : true,
						edit         : false,
						choiceInList : false  
						});		        	    	
			columns.push({
						property     : "releaseDate",
					   	header       : Messages("study.releaseDate"),
					    type         : "date",		    	  	
					    order        : true,
					    edit         : true,
					    choiceInList : false  
						});	
			columns.push({
						property     : "accession",
					    header       : Messages("study.accession"),
				        type         : "text",		    	  	
				        order        : true,
				        edit         : false,
				        choiceInList : false  
				       	});	
			columns.push({
						property     : "externalId",
					    header       : Messages("study.externalId"),
				       	type         : "text",		    	  	
				       	order        : true,
				       	edit         : false,
				       	choiceInList : false  
				       	});	
			columns.push({
						property     : "centerProjectName",
						header       : Messages("study.centerProjectName"),
						type         : "text",		    	  	
						order        : false,
						edit         : true,
						choiceInList : false  
						});			        			        			        
			columns.push({
						property     : "title",
						header       : Messages("study.title"),
						type         : "text",	
						hide         : true,
						order        : true,
						edit         : true,
						editTemplate : "<textarea class='form-control' #ng-model rows='1'></textarea>",
						choiceInList : false
						});	    
			columns.push({
						property     : "description",
						header       : Messages("study.AbstractDescription"),
						type         : "text",	
						hide         : true,
						order        : true,
						edit         : true,
						editTemplate :"<textarea class='form-control' #ng-model rows='1'></textarea>",
						choiceInList : false
						});	
				
					      
			columns.push({
						property     : "existingStudyType",
						header       : Messages("study.existingStudyType"),
						type         : "String",
						hide         : true,
						edit         : true,
						order        : false,
						choiceInList : true,
						listStyle    : 'bt-select',
						possibleValues : 'sraVariables.existingStudyType',
						});
		
			columns.push({
						property     : "locusTagPrefixs",
						header       : Messages("study.locusTagPrefixs"),
						type         : "text",	
						hide         : true,
						order        : true,
						edit         : true,  // modifiable en update
						editTemplate : "<textarea class='form-control' #ng-model rows='1'></textarea>",
					    choiceInList : false
						});
			columns.push({
						property     : "parentProjectAccessions",
						header       : Messages("study.parentProjectAccessions"),
						type         : "text",	
						hide         : true,
						order        : true,
						edit         : true,  // modifiable en update
						editTemplate : "<textarea class='form-control' #ng-model rows='1'></textarea>",
					    choiceInList : false
						});
			columns.push({
						property     : "idsPubmed",
						header       : Messages("study.idsPubmed"),
						type         : "text",	
						hide         : true,
						order        : true,
						edit         : true,  // modifiable en update
						editTemplate : "<textarea class='form-control' #ng-model rows='1'></textarea>",
						choiceInList : false
						});
			columns.push({
						property     : "taxonId",
						header       : Messages("project.taxonId"),
						type         : "text",
						hide         : true,
						edit         : true, 
						order        : true,
						choiceInList : false,
						});		
			columns.push({
						property     : "scientificName",
						header       : Messages("project.scientificName"),
						type         : "text",	
						hide         : true,
						order        : true,
						edit         : false,  // synchronisé avec taxonId
						editTemplate  :"<textarea class='form-control' #ng-model rows='1'></textarea>",
                        });
			columns.push({
						property     : "attributes",
						header       : Messages("study.attributes"),
						type         : "text",	
						hide         : true,
						order        : true,
						edit         : true,  // modifiable en update
						editTemplate : "<textarea class='form-control' #ng-model rows='3'></textarea>",
						//render       : function(data, edit) {
						//	              return toolsServices.getStringAttributes(data);
                        //               },
						choiceInList : false,
						});				
			return columns;
		};
      
	
	$scope.studiesDTConfig = {
			name:'studiesDT',
			order :{by:'code',mode:'local', reverse:true},
			search:{
				url:jsRoutes.controllers.sra.studies.api.Studies.list()
			},
			pagination:{
				active:true,
				mode:'local',
                numberRecordsPerPage: 100
			},
			select:{active:true},
			showTotalNumberRecords:true,
			edit : {
				active        : true,  // permettre edition des champs editables
				showButton    : true,  // bouton d'edition visible
				withoutSelect : true,
				columnMode : true,
				lineMode : function(line) {
					if(line.state.code === "SUB-F"  // modification autorisee seulement pour les study avec AC et non engagés dans une soumission en cours
					&& line._type      === "Study" ) { // On n'edite pas les ExternalStudy
					//&& line.traceInformation.createUser === $scope.user) { // limiter autorisation de modification au proprietaire.
						return true;
					} else {
						return false;  
					} 
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
					// ajout onglet avec nom du study permettant d'acceder à la vue details
					tabService.addTabs({label:line.code,
										href:jsRoutes.controllers.sra.studies.tpl.Studies.get(line.code).url,
										remove:true // permet fermer onglet
										});
					// inutile d'installer le datatable renseigné dans mainService car fait a l'initialisation et passage par adresse. 
					// modifications de $scope.studiesDT prises en compte dans mainService
					// mainService.setDatatable($scope.studiesDT);
				}
			},
			save : {
				active:true,
				showButton : false,
				changeClass : false,
				url:function(line){
					
					console.log("++++++++++++++++++++++                 JE PASSE PAR LE url:function(line) DU DATATABLE  !!!! ");
					// pour regler problème de mapping entre representation du study dans datatable et dans model java :
					//$scope.toJsonStudyModelOrThrow(line, $scope.errorDetails);
					//return jsRoutes.controllers.sra.studies.api.Studies.update(line.code).url; // jamais utilisé si mode local
				},
				method:'put',
				value:function(line){
					console.log("++++++++++++++++++++++                 JE PASSE PAR LE value:function(line) DU DATATABLE  !!!! ");
					return line;
				},
				callback : function(datatable, errors) {
					console.log("++++++++++++++++++++++                 JE PASSE PAR LE callback DU DATATABLE  !!!! ");
					$scope.updateCallback(datatable, errors);
				},
				mode:'local',
			},

	};
	
	
//---------------------------------------------------------------------------------------------------
	
	// Initialisations :
	//------------------
	//console.log("Dans update-ctrl.js");
	$scope.errorDetails = {};
	$scope.messages = messages();	
	$scope.messages.clear();
	$scope.form = {};  // important. 
	//$scope.form.createUser = $scope.user; // suppression du controle utilisateur
	$scope.treeLoadInProgress = false;
	//console.log("Dans consultation-ctrl.js, scope.treeLoadInProgress = "   , $scope.treeLoadInProgress);
	
	// Oeration update possible uniquement sur Study (pas sur ExternalStudy) dans etat SUB-F :
	$scope.form.stateCode ="SUB-F";
	$scope.form.type="Study"; // important de ne pas proposer ExternalStudy pour update.
	$scope.sraVariables = {};
	// Pour avoir les onglets a gauche de la page :
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('update');
		tabService.addTabs({label:Messages('studies.menu.update'),
			href:jsRoutes.controllers.sra.studies.tpl.Studies.home("update").url,
			remove:true});
		tabService.activeTab(0); //  active l'onglet en le mettant en bleu
	}
	// si on declare dans services => var sraVariables = {};
	// si on declare dans le controlleur : $scope.sraVariables = {};

//	Initialisation ExistingStudyType:
	$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'existingStudyType'}})
	.success(function(data) {
	// initialisation de la variable createService.sraVariables.exitingStudyType utilisée dans create.scala.html
	$scope.sraVariables.existingStudyType = data;																					
	});	
	
	
	//	Initialisation datatable :
//	$scope.studiesDT = datatable($scope.studiesDTConfig);
//	$scope.studiesDT.setColumnsConfig($scope.getStudyColumns());
		
	// datatable dans mainService qui est partagee par plusieurs ctr ou services du coup peut etre recuperé dans details.ctr
	if ($scope.studiesDTConfig && angular.isUndefined(mainService.getDatatable())) {
		$scope.studiesDT = datatable($scope.studiesDTConfig);
        $scope.studiesDT.setColumnsConfig($scope.getStudyColumns());
        // installation de l'adresse du datatable dans mainService  => toutes 
        // les modifications uterieures de $scope.studiesDT seront visibles dans mainService mais
        // on pourra acceder à scope.studiesDT meme quand on revient sur la page recherche
        // car on ne relance pas la recherche ?????
        mainService.setDatatable($scope.studiesDT); 
    	//console.log("AAAAAAAAAAAA         installation du datatable dans le mainService");
    } else if (angular.isDefined(mainService.getDatatable())) {
    	$scope.studiesDT = mainService.getDatatable();  
    	//console.log("AAAAAAAAAAAA         recuperation du datatable depuis le mainService", $scope.studiesDT);
    }
	

//------------------------------------------------------------------------------------------
	
	// Definitions methodes :
	//-----------------------
	$scope.toJsonStudyModelOrThrow = function(line, errorDetails) {
		//console.log("dans toJsonStudyModelOrThrow, errorDetails=", errorDetails);
		//console.log("dans toJsonStudyModelOrThrow, json=", line);

		toolsServices.toJsonStudyModel(line, errorDetails);
		//line.description = line.studyAbstract;   // mettre la description avec meme valeur que abstract => fait dans java
		//console.log("dans toJsonStudyModelOrThrow, errorDetails=", errorDetails);
		if((Object.keys(errorDetails)).length > 0) {
			//console.log("AAAAAAAAAAAAAAAAAAA               J'ai bien une erreur de  PARSING");
			$scope.messages = messages(); // si on re-sauve sans recharger page.
			$scope.messages.clear();      // si on re-sauve sans recharger page.
			$scope.messages.addDetails(errorDetails);
			$scope.messages.setError("save");
			$scope.studiesDT.setSpinner(false);
			$scope.treeLoadInProgress = false;
			throw("Erreur de parsing");
		} 
	}; // end $scope.toJsonStudyModelOrThrow
		
	$scope.updateCallback = function(datatable, countError) {
		$scope.messages = messages(); // si on re-sauve sans recharger page.
		$scope.messages.clear();      // si on re-sauve sans recharger page.
		//console.log(datatable);
//		console.log(countError);
//		console.log("YYYYYYYY   TOTOTO   YYYYYYYYYYY    dans callback, displayResult : ", datatable.displayResult);
//		console.log("xxxxxxxxxx    dans callback, displayResult.line : ", datatable.displayResult.line);
		//console.log("eeeeeeeeeeeeee  dans callback scope.dbSamples = ", $scope.dbSamples);
		for (var i = 0; i < datatable.displayResult.length; i++) {
			var keysError =  Object.keys(datatable.displayResult[i].line.errors);
//			console.log("eeeeeeeee keysError", keysError);
//			console.log("eeeeeeeee keysError.length", keysError.length);
			if ( keysError.length >= 1 ) {
				// Recuperation message erreur :
				var monObj = datatable.displayResult[i].line.errors;
//				console.log("dans callback, monObj", monObj);
//				console.log("dans callback, monObj.typeOf", monObj.typeOf);
//				console.log("dans callback, monObj.hasOwnProperty()", monObj.hasOwnProperty());
				for (var [key, values] of Object.entries(monObj)) {
					var cle = `${key}`;
					var values = `${values}`; 
//  				console.log("xxxxxxxxxxx    cle = ", cle);
//					console.log("xxxxxxxxxxx    values = ", values);
//					console.log("monObjValues = ", monObj[cle]);
					var monError = {};
					monError[cle]=[values];
					$scope.messages.addDetails(monError);
				} // end for
			} // end if						
		}// end for
		// Affichage message succes ou erreur :
		// Dans le cas d'une exception, countError n'est pas definie :
		if ( countError == undefined || countError == null || countError > 0) {
			$scope.messages.setError("save");
			$scope.treeLoadInProgress = false;
			$scope.studiesDT.setSpinner(false);
		} else {
			$scope.messages.setSuccess("save");			
		}
	}; // end updateCallback
	
		
	// methode appelée depuis la vue avec la directive ng-init
	$scope.setUserInScope = function(user) {
		$scope.user = user;
		//console.log("Dans setUserInScope, user= ", user);
	};

	$scope.search = function(user) {
		$scope.messages = messages();
		$scope.messages.clear();
		//console.log("Dans update-ctrl.search");
		//console.log("user", $scope.user)
		if (! $scope.form.identifier) {
			$scope.messages.setError(Messages("AbsenceSearchParameter"));
			console.log("update-ctrl.$scope.messages = ", $scope.messages);
			// annuler spinner si besoin
			throw("Aucun parametre pour la recherche du study à updater");  
		} 
		$scope.form.stateCode="SUB-F";
		$scope.form.type="Study";
		//$scope.form.createUser = $scope.user;
		$scope.studiesDT.search($scope.form);
	}; // end search

	$scope.reset = function(){
		//console.log("Dans update-ctrl.resetForm");
		$scope.form = {};
		//enlever controle user
		//$scope.form.createUser = $scope.user;
		$scope.tab_studies = []; 
		//$scope.studiesDT = null;    // ne marche pas datatable
		$scope.studiesDT = datatable($scope.studiesDTConfig);
		$scope.studiesDT.setColumnsConfig($scope.getStudyColumns());
		$scope.messages = messages();
		$scope.messages.clear();
		$scope.form.stateCode ="SUB-F";
		$scope.form.type="Study"; // important de ne pas proposer ExternalStudy pour update 
		$scope.treeLoadInProgress = false;
		$scope.errorDetails = {};
	};


	$scope.submit = function() {
		$scope.messages = messages();
		$scope.messages.clear();
		$scope.errorDetails = {}; // important de le mettre ici si après une erreur on retente un submit sans recharger page

		//console.log("YYYYYYYYYY Dans update-ctrl.submit");
		mainService.setForm($scope.form);
		//$scope.treeLoadInProgress = true;
		$scope.studiesDT.setSpinner(true);
		// sauvegarde des données en local (mode local le save de getColumnStudy) 
		// (si mode remote, les donnees sont bien sauvées dans base avec valeurs editées mais 
		// le datatable utilisé ici ne contient pas les modifs des utilisateurs 
		// => en mode remote, dans le datatable, le modele n'est pas synchronisé avec la vue.
		// En mode local, dans le datatable, le modele est synchronisé avec la vue et contient bien 
		// les modifications des utilisateurs, en revanche pas de sauvegarde dans la base !
		// ici la sauvegarde du study en base se fait au moment de la creation de la soumission dans la partie java.
		
		// Attention, lorsqu'on sauve un study en mode local, AllResult contient tous les champs alors que displayResult ne contient pas les
		// champs de type list ou objet s'ils n'ont pas été modifiés par les utilisateurs. Apres un save local dans le datatable, prendre
		// les données dans AllResult (et non dans displayResult) et transformer dans bon type si besoin avant de sauvegarder en base.

		console.log("$scope.studiesDT avant save du datatable ", $scope.studiesDT);
		$scope.studiesDT.save(); 
		$scope.tab_studies = $scope.studiesDT.getData();
		if(!$scope.tab_studies || $scope.tab_studies.length < 1) {
			$scope.reset();
			$scope.messages.setError(Messages("AbsenceDonneePourUpdate"));
			throw(Messages("AbsenceDonneePourUpdate"));
		}
		console.log("$scope.studiesDT apres save du datatable ", $scope.studiesDT);
		for(var i= 0; i < $scope.tab_studies.length; i++) {
			var study = $scope.tab_studies[i];
			//pour regler problème de mapping entre representation du study dans datatable et dans model java :
			$scope.toJsonStudyModelOrThrow(study, $scope.errorDetails);
		}
	
	
		var study = $scope.tab_studies[0];
		//console.log("study pour sauvegarde en base = ", umbrella);
		var tabRequestCreateForUpdate = [];

//		console.log("HHHHHHHHHHHHHHH                         YYYYYYYYYYYYYYYYYY           study= ", study)
//      console.log("xxxxxxxx userStudy.releaseDate=", study.releaseDate);
//		console.log("releaseDate="+study.releaseDate);
//		console.log("releaseDate="+ moment(study.releaseDate).calendar());
//		console.log("date.now="+ Date.now());
//		console.log("releaseDate.endOf="+ moment(study.releaseDate).endOf('day'));
//		console.log("releaseDate.fromNow="+ moment(study.releaseDate).fromNow());
//		console.log("releaseDate.endOf.fromNow="+ moment(study.releaseDate).endOf('day').fromNow());		
//		console.log(moment (study.releaseDate, "YYYY-MM-DD", true) .isValid ());
//		console.log(moment (study.releaseDate, "DD/MM/YYYY", true) .isValid ());
//		console.log (moment (study.releaseDate). format ('DD/MM/YYYY'));

	
		var args = {
			study       : study,
			samples     : [],
			experiments : []
		};		
		
		// declencher creation d'un objet submission pour update et sauvegarde du study passés dans args
		var request = $http.put(jsRoutes.controllers.sra.submissions.api.Submissions.createForUpdate().url, args);
		tabRequestCreateForUpdate.push(request);
		$q.all(tabRequestCreateForUpdate)
		.then(result => { // result de l'operation precedente, cad la creation de la soumission
				//console.log("result_2 = " + result); 	
				//console.log("result_2 = " + result[0].data); 	
				//console.log("result_2 = " + result[0].data.code); 	
				var submission = result[0].data;
				//var submissionCode = result[0].data.code;				
				$scope.messages.setSuccess("Le study " + study.code + " a bien été mis à jour à l'EBI via la soumission "  + submission.code + " voir ebiReport " + submission.submissionDirectory + "/" + submission.ebiResult); 
				//$scope.treeLoadInProgress = false;	
				$scope.studiesDT.setSpinner(false);
//
//				return $http.put(jsRoutes.controllers.sra.submissions.api.Submissions.updateState(submissionCode).url, submissionState);					
//		}).then(result => { // result de l'operation precedente cad la mise à jour de l'etat de la soumission
//				//console.log("result_3 = " + result); 	
//				//console.log("result_3 = " + result.data); 
//				var submissionCode = result.data.code;
//				$scope.messages.setSuccess("La soumission " + submissionCode + " pour la mise a jour à l'EBI des données du tableau est en cours de traitement"); 
//				// mieux de passer par valeur en base car ecrasement de userReleaseDate si < dbReleaseDate
//				var tab_codes = [];
//				$scope.studiesDT = datatable($scope.studiesDTConfig);
//				$scope.studiesDT.setColumnsConfig($scope.getStudyColumns());
//				for(var i= 0; i < $scope.tab_studies.length; i++) {
//					tab_codes.push($scope.tab_studies[i].code);
//				}
//				$scope.studiesDT.search({codes:tab_codes});	
//				$scope.treeLoadInProgress = false;					
		}).catch(error => { 
			$scope.messages.addDetails(error.data); 
			$scope.messages.setError("Erreur "); 
			// Pas bonne idée car si message d'erreur bien que l'utilisateur voit sa saisie erronnée dans le tableau
			// affichage données originales dans base si donnée modifiées par user non validees
			//$scope.studiesDT.setData($scope.tab_studies_ori, $scope.tab_studies_ori.length);	
			//$scope.treeLoadInProgress = false;
			$scope.studiesDT.setSpinner(false);

			
		});
	
	};// end $scope.submit


	// NGL-4708
	$scope.isShowConsigne=false;
	$scope.toggleShowConsigne=function(){
		//console.log("TOGGLE search-ctrl.js");
		if ( $scope.isShowConsigne===false) { $scope.isShowConsigne=true}
		else {$scope.isShowConsigne=false}
	};

}]);



