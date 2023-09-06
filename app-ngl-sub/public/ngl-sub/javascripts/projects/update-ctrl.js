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
						property : "state.code",
						header   : Messages("project.state.code"),
						"filter" : "codes:'state'",
						type     : "text",		    	  	
						order    : true
						});	
			columns.push({
						property   : "code",
		  	        	header     : Messages("code"),
				       	type       : "text",		    	  	
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
					    header       : Messages("accession"),
				        type         : "text",		    	  	
				        order        : true,
				        edit         : false,
				        choiceInList : false  
				       	});	     			        			        
			columns.push({
						property     : "title",
						header       : Messages("title"),
						type :"text",	
						hide:true,
						order:true,
						edit:true,  
						editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
						choiceInList:false
						});	    
			columns.push({
						property     : "description",
						header       : Messages("description"),
						type :"text",	
						hide:true,
						order:true,
						edit:true,  
						editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
						choiceInList:false
						});	 
			columns.push({
						property     : "childrenProjectAccessions",
				       	header       : Messages("project.childrenProjectAccessions"),
						type :"text",	
						hide:true,
						order:true,
						edit:true,  
						editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
						choiceInList:false
				       	}); 
			columns.push({
						property     : "idsPubmed",
						header       : Messages("project.idsPubmed"),
						type :"text",	
						hide:true,
						order:true,
						edit:true,  
						editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
						choiceInList:false
						});	
			columns.push({
						property     : "taxonId",
						header       : Messages("project.taxonId"),
						type         : "text",
						hide         : true,
						edit         : false, 
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
				mode:'local',
			},

	};
	
	
//---------------------------------------------------------------------------------------------------
	
	// Initialisations :
	//------------------
	//console.log("Dans update-ctrl.js");
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
		console.log("scope.form", $scope.form);
		if ($scope.form.accession) {
			$scope.messages.setError(Messages("AbsenceSearchParameter"));
			console.log("update-ctrl.$scope.messages = ", $scope.messages);
			// annuler spinner si besoin
			throw("Aucun parametre pour la recherche du study à updater");  
		} 
		$scope.form.stateCode="SUB-F";
		//$scope.form.createUser = $scope.user;
		$scope.projectsDT.search($scope.form);
	}; // end search

	$scope.reset = function(){
		//console.log("Dans update-ctrl.resetForm");
        $scope.form = {};
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
		//console.log("YYYYYYYYYY Dans update-ctrl.submit");
		mainService.setForm($scope.form);
		// sauvegarde des données en local (mode local le save de getColumnStudy) 
		// (si mode remote, les donnees sont bien sauvées dans base avec valeurs editées mais 
		// le datatable utilisé ici ne contient pas les modifs des utilisateurs 
		// => en mode remote, dans le datatable, le modele n'est pas synchronisé avec la vue.
		// En mode local, dans le datatable, le modele est synchronisé avec la vue et contient bien 
		// les modifications des utilisateurs, en revanche pas de sauvegarde dans la base !
		// ici la sauvegarde du study se fait au moment de la creation de la soumission dans la partie java.

		$scope.projectsDT.save(); 
		$scope.tab_projects = $scope.projectsDT.getData();
		if(!$scope.tab_projects || $scope.tab_projects.length < 1) {
			$scope.reset();
			$scope.messages.setError(Messages("AbsenceDonneePourUpdate"));
			throw(Messages("AbsenceDonneePourUpdate"));
		}
		var tabRequestCreateForUpdate = [];
		
		//console.log("YYYYYYYYYY Dans update-ctrl.submit: nb_study=" + $scope.tab_bilans.length);
		// declencher creation d'un objet submission pour update :
		var umbrella = $scope.tab_projects[0];  // requete avec ERP ou PRJEB qui renvoie un seul resultat
        //console.log("xxxxxxxx study=", study);

		if(toolsServices.isNotBlank(umbrella.childrenProjectAccessions)) {
			console.log("typeOf childrenProjectAccessions", typeof umbrella.childrenProjectAccessions);
			console.log(umbrella.childrenProjectAccessions);
			// si on edite la valeur de locusTagPrefixs dans datatable alors string, et sinon array d'ou teste sur le type :
			if(typeof umbrella.childrenProjectAccessions === 'string' || umbrella.childrenProjectAccessions instanceof String) {
				var strChildrenProjectAccessions = umbrella.childrenProjectAccessions.replace(/,\s*$/, "");
				strChildrenProjectAccessions = strChildrenProjectAccessions.replace(/,\s*$/, "");
				delete umbrella.childrenProjectAccessions;
				umbrella.childrenProjectAccessions = strChildrenProjectAccessions.split(','); 
		    	// Preferer boucle for au forEach ici car passage par valeur du locusTag d'ou modification dans le forEach qu'il faut sauvegarder
            	// hors de la boucle forEach. De plus, boucle for plus efficace.
				for(var i= 0; i < umbrella.childrenProjectAccessions.length; i++) {
     				umbrella.childrenProjectAccessions[i] = toolsServices.clean(umbrella.childrenProjectAccessions[i]);
				}
			}
		} else {
			delete umbrella.childrenProjectAccessions;
		}
		if(toolsServices.isNotBlank(umbrella.idsPubmed)) {
			console.log("typeOf umbrella.idsPubmed", typeof umbrella.idsPubmed);
			console.log(umbrella.idsPubmed);
			// si on edite la valeur de idsPubmed dans datatable alors string, et sinon array d'ou teste sur le type :
			if(typeof umbrella.idsPubmed === 'string' || umbrella.idsPubmed instanceof String) {
				var strIdsPubmed = umbrella.idsPubmed.replace(/,\s*$/, "");
				strIdsPubmed = strIdsPubmed.replace(/,\s*$/, "");
				delete umbrella.idsPubmed;
				umbrella.idsPubmed = strIdsPubmed.split(','); 
		    	// Preferer boucle for au forEach ici car passage par valeur du idPubmed d'ou modification dans le forEach qu'il faut sauvegarder
            	// hors de la boucle forEach. De plus, boucle for plus efficace.
				for(var i= 0; i < umbrella.idsPubmed.length; i++) {
     				umbrella.idsPubmed[i] = toolsServices.clean(umbrella.idsPubmed[i]);
				}
			}
		} else {
			delete umbrella.idsPubmed;
		}
		
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
				console.log("result_2 = " + result); 	
				//console.log("result_2 = " + result[0].data); 	
				//console.log("result_2 = " + result[0].data.code); 	
				var submission = result[0].data;
				var submissionCode = result[0].data.code;
				var submissionState = angular.copy(result[0].data.state);
				submissionState.code = "SUBU-SMD-IW";
				$scope.messages.setSuccess("La soumission " + submissionCode + " pour la mise a jour à l'EBI des données du tableau est en cours de traitement"); 
				return $http.put(jsRoutes.controllers.sra.submissions.api.Submissions.updateState(submissionCode).url, submissionState);					
		}).then(result => { // result de l'operation precedente cad la mise à jour de l'etat de la soumission
				console.log("result_3 = " + result); 	
				//console.log("result_3 = " + result.data); 
				var submissionCode = result.data.code;
				$scope.messages.setSuccess("La soumission " + submissionCode + " pour la mise a jour à l'EBI des données du tableau est en cours de traitement"); 
				// rafraichissement tableau avec nouveaux status: 
				$scope.tab_projects.forEach(function(umbrella) {
					umbrella.state.code="SUBU-SMD-IW";	
				});
				$scope.projectsDT.setData($scope.tab_projects, $scope.tab_projects.length);				
		}).catch(error => { 
			$scope.messages.addDetails(error.data); 
			$scope.messages.setError("Erreur de sauvegarde");   // important pour avoir le message.
			// Pas bonne idée car si message d'erreur bien que l'utilisateur voit sa saisie erronnée dans le tableau
			// affichage données originales dans base si donnée modifiées par user non validees
			//$scope.projectsDT.setData($scope.tab_projects_ori, $scope.tab_projects_ori.length);				
		});
		
	};// end $scope.submit

}]);



