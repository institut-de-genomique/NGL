"use strict";

angular.module('home').controller('UpdateCtrl',
		[ '$http', '$scope', '$routeParams' , '$q', 'mainService', 'lists', 'tabService', 'messages', 'toolsServices', 'datatable',
  function($http,   $scope,   $routeParams,    $q,   mainService,   lists,   tabService,  messages, toolsServices, datatable) { 


	$scope.getProjectColumns = function() {
		var columns = [];
			columns.push({
						property     : "traceInformation.creationDate",
						header       : Messages("traceInformation.creationDate"),
						type         : "date",	
						hide         : true,	    	  	
						order        : true,
						edit         : false,
						choiceInList : false  
						});
			columns.push({
						property     : "traceInformation.createUser",
						header       : Messages("traceInformation.creationUser"),
						type         : "date",		    	  	
						hide         : true,	    	  	
						order        : true,
						edit         : false,
						choiceInList : false  
						});
			columns.push({
						header       : Messages("project.state.code"),
						property     :"state.code",
						"filter"     :"codes:'state'",
						type         : "text",		    	  	
						hide         : true,	    	  	
						order        : true
						});	
			columns.push({
						property   : "code",
		  	        	header     : Messages("project.code"),
				       	type       : "text",		    	  	
						hide       : true,	    	  	
				       	order      : true
				        });		
			columns.push({
						property     : "firstSubmissionDate",
						header       : Messages("firstSubmissionDate"),
						type         : "date",	
						hide         : true,	    	  	
						order        : true,
						edit         : false,
						choiceInList : false  
						});	
			columns.push({
						property     : "accession",
					    header       : Messages("project.accession"),
				        type         : "text",		    	  	
						hide         : true,	    	  	
				        order        : true,
				        edit         : false,
				        choiceInList : false  
				       	});	   			        
			columns.push({
						property     : "title",
						header       : Messages("project.title"),
						type         :"text",	
						hide         :true,
						order        :true,
						edit         :true,  
						editTemplate :"<textarea class='form-control' #ng-model rows='1'></textarea>",
						choiceInList:false
						});	    
			columns.push({
						property     : "description",
						header       : Messages("project.description"),
						type :"text",	
						hide:true,
						order:true,
						edit:true,  
						editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
						choiceInList:false
						});	
			columns.push({
						property     : "parentProjectAccessions",
						header       : Messages("parentProjectAccessions"),
						type         : "text",	
						hide         : true,
						order        : true,
						edit         : true,  // modifiable en update
					    choiceInList : false
						}); 
			columns.push({
						property     : "childrenProjectAccessions",
				       	header       : Messages("project.childrenProjectAccessions"),
				       	type         : "text",		    	  	
						hide         : true,	    	  	
				       	order        : false,
				       	edit         : true,
				       	choiceInList : false  
				       	}); 	
			columns.push({
						property     : "idsPubmed",
						header       : Messages("project.idsPubmed"),
						type         : "text",
						hide         : true,
						edit         : true, 
						order        : false,
						choiceInList : false,
						listStyle    : 'bt-select',
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
						edit         : false, 
						order        : true,
						choiceInList : false,
						});	
			columns.push({property   : "attributes",
					    header       : Messages("project.attributes"),
					    //"filter":"codes:'state'",
					    type         : "text",		    	  	
					    order        : false, 
					    edit         : true,  
					   	hide         : true,
						choiceInList : false,
						editTemplate :"<textarea class='form-control' #ng-model rows='3'></textarea>",
//						render       : function(data, edit) {
//							              return toolsServices.getStringAttributes(data);
//						             }
				        });							
			columns.push({property   : "dropBox",
						header       : Messages("study.dropBox"),
						type         : "String",
						hide         : true,
						edit         : false,
						order        : false,
						choiceInList : true,
						listStyle    : 'bt-select',
						possibleValues:'sraVariables.dropBox',
				    	});						
			columns.push({
						property     : "comments",
						header       : Messages("comments"),
						type         : "text",
						hide         : true,
						edit         : false, // pas modifiable en consultation car uniquement dans vue detail
						order        : false,
						choiceInList : false,
						filter       : "collect:'comment'",
						listStyle    : 'bt-select',
						});						
			return columns;
		};

	
	
	$scope.projectsDTConfig = {
			name:'umbrellasDT',
			order :{by:'code',mode:'local', reverse:true},
			search:{
				url:jsRoutes.controllers.sra.projects.api.Projects.list()
			},
			pagination:{
				active:true,
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
				lineMode : function(line) {
					if(line.state.code === "SUB-F") { // modification autorisee seulement pour les study avec AC et non engagés dans une soumission en cours
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
										href:jsRoutes.controllers.sra.projects.tpl.Projects.get(line.code).url,
										remove:true // permet fermer onglet
										});
					// inutile d'installer le datatable renseigné dans mainService car fait a l'initialisation et passage par adresse. 

				}
			},
			save : {
				active:true,
				showButton : false,
				changeClass : false,
				url:function(line){
					return jsRoutes.controllers.sra.projects.api.Projects.update(line.code).url; // jamais utilisé si mode local
				},
				method:'put',
				value:function(line){
					return line;
				},
				callback : function(datatable, errors) {
					console.log("++++++++++++++++++++++                 JE PASSE PAR LE callback DU DATATABLE  !!!! ");
					$scope.messages.clear();
					$scope.updateCallback(datatable, errors);
					$scope.projectsDT.setSpinner(false);
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

	// Oeration update possible uniquement sur Study (pas sur ExternalStudy) dans etat SUB-F :
	$scope.form.stateCode ="SUB-F";
	$scope.sraVariables = {};
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('update');
		tabService.addTabs({label:Messages('projects.menu.update'),href:jsRoutes.controllers.sra.projects.tpl.Projects.home("update").url,remove:true});
		tabService.activeTab(0); //  active l'onglet en le mettant en bleu
	}
	// si on declare dans services => var sraVariables = {};
	// si on declare dans le controlleur : $scope.sraVariables = {};


//	Initialisation datatable :
	$scope.projectsDT = datatable($scope.projectsDTConfig);
	$scope.projectsDT.setColumnsConfig($scope.getProjectColumns());

//------------------------------------------------------------------------------------------
	
	// Definitions methodes :
	//-----------------------
	$scope.toJsonUmbrellaModelOrThrow = function(line, errorDetails) {
		//console.log("dans toJsonUmbrellaModelOrThrow, errorDetails=", errorDetails);
		//console.log("En entree de toJsonUmbrellaModelOrThrow, json=", line);

		toolsServices.toJsonUmbrellaModel(line, errorDetails);
		if((Object.keys(errorDetails)).length > 0) {
			//console.log("AAAAAAAAAAAAAAAAAAA               J'ai bien une erreur de  PARSING");
			$scope.messages = messages(); // si on re-sauve sans recharger page.
			$scope.messages.clear();      // si on re-sauve sans recharger page.
			$scope.messages.addDetails(errorDetails);
			$scope.messages.setError("save");
			$scope.projectsDT.setSpinner(false);
			$scope.treeLoadInProgress = false;
			throw("Erreur de parsing");
		} 
		//console.log("En sortie de toJsonUmbrellaModelOrThrow, json=", line);
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
		console.log("Dans update-ctrl.search");
		console.log("user", $scope.user);
		//console.log("scope.form", $scope.form);
		if(toolsServices.isBlank($scope.form.accession)) {
			$scope.messages.setError(Messages("AbsenceSearchParameter"));
			//console.log("update-ctrl.$scope.messages = ", $scope.messages);
			// annuler spinner si besoin
			throw("Aucun parametre pour la recherche du study à updater");  
		} 
		$scope.form.stateCode="SUB-F";
		//$scope.form.createUser = $scope.user;
		$scope.projectsDT.search($scope.form);
	}; // end search

	$scope.reset = function(){
		//console.log("Dans update-ctrl.resetForm");
		$scope.errorDetails = {};
		$scope.tab_projects = []; 
		$scope.projectsDT = datatable($scope.projectsDTConfig);
		$scope.projectsDT.setColumnsConfig($scope.getProjectColumns());
		$scope.messages = messages();
		$scope.messages.clear();
		$scope.form.stateCode ="SUB-F";
	};



	$scope.submit = function() {
		$scope.messages = messages();
		$scope.messages.clear();
		$scope.errorDetails = {}; // important de le mettre ici si après une erreur on retente un submit sans recharger page
		$scope.projectsDT.setSpinner(true);	

		//console.log("YYYYYYYYYY Dans update-ctrl.submit");
		mainService.setForm($scope.form);
		// sauvegarde des données en local (mode local le save de getColumnStudy) 
		// (si mode remote, les donnees sont bien sauvées dans base avec valeurs editées mais 
		// le datatable utilisé ici ne contient pas les modifs des utilisateurs 
		// => en mode remote, dans le datatable, le modele n'est pas synchronisé avec la vue.
		// En mode local, dans le datatable, le modele est synchronisé avec la vue et contient bien 
		// les modifications des utilisateurs, en revanche pas de sauvegarde dans la base !
		// ici la sauvegarde du study se fait au moment de la creation de la soumission dans la partie java.

		console.log("$scope.projectsDT AVANT SAVE du datatable ", $scope.projectsDT);
		$scope.projectsDT.save();
		console.log("$scope.projectsDT APRES SAVE du datatable ", $scope.projectsDT);
		$scope.tab_projects = $scope.projectsDT.getData();
		if(!$scope.tab_projects || $scope.tab_projects.length < 1) {
			$scope.reset();
			$scope.messages.setError(Messages("AbsenceDonneePourUpdate"));
			throw(Messages("AbsenceDonneePourUpdate"));
		}
	
		
		//console.log("$scope.projectsDT APRES SAVE du datatable ", $scope.projectsDT);
		for(var i= 0; i < $scope.tab_projects.length; i++) {
			var umbrella = $scope.tab_projects[i];
			//console.log("UMBRELLA avant modification modele = ", umbrella);
			$scope.toJsonUmbrellaModelOrThrow(umbrella, $scope.errorDetails);
			//console.log("UMBRELLA apres modification modele = ", umbrella);
		}


		var umbrella = $scope.tab_projects[0];
		console.log("UMBRELLA pour sauvegarde en base = ", umbrella);
		//$scope.treeLoadInProgress = false;

        //throw("Arret premature");
		var tabRequestCreateForUpdate = [];
		
		// mise à jour du project umbrella dans base qui se fait dans createSubmission() appelé par createForUpdate

		var args = {
			umbrella    : umbrella,
			study       : null,
			samples     : [],
			experiments : []
		};			

		// declencher creation d'un objet submission pour update et sauvegarde du study passés dans args
		var request = $http.put(jsRoutes.controllers.sra.submissions.api.Submissions.createForUpdate().url, args);
//		.catch (error => {
//			console.log(error);
//			$scope.messages.addDetails(error.data); 
//			return $q.reject(error);                // propage erreur, comme throw 
//		});
		tabRequestCreateForUpdate.push(request);
		$q.all(tabRequestCreateForUpdate)
		.then(result => { // result de l'operation precedente, cad la creation de la soumission
			$scope.projectsDT.setSpinner(false);	
			console.log("result_2 = " + result); 	
			//console.log("result_2 = " + result[0].data); 	
			//console.log("result_2 = " + result[0].data.code); 	
			var submission = result[0].data;
			//var submissionState = angular.copy(result[0].data.state);
			var submission = result[0].data;
			$scope.messages.setSuccess("L'umbrella " + umbrella.code + " a bien été mis à jour à l'EBI via la soumission "  + submission.code + " voir ebiReport " + submission.submissionDirectory + "/" + submission.ebiResult);
		}).catch(error => { 
			$scope.projectsDT.setSpinner(false);	
			$scope.messages.addDetails(error.data); 
			$scope.messages.setError("Erreur "); 
			// Pas bonne idée car si message d'erreur bien que l'utilisateur voit sa saisie erronnée dans le tableau
			// affichage données originales dans base si donnée modifiées par user non validees
			//$scope.projectsDT.setData($scope.tab_projects_ori, $scope.tab_projects_ori.length);				
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



