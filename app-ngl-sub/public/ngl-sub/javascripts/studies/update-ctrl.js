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
						property : "state.code",
						header   : Messages("study.state.code"),
						"filter" : "codes:'state'",
						type     : "text",		    	  	
						order    : true
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
							type :"text",	
						hide:true,
						order:true,
						edit:true,
						editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
						choiceInList:false
						});	    
			columns.push({
						property     : "studyAbstract",
						header       : Messages("study.AbstractDescription"),
						type :"text",	
						hide:true,
						order:true,
						edit:true,
						editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
						choiceInList:false
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
						type :"text",	
						hide:true,
						order:true,
						edit:true,  // modifiable en update
						editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
					choiceInList:false
						});
			columns.push({
						property     : "idsPubmed",
						header       : Messages("study.idsPubmed"),
						type :"text",	
						hide:true,
						order:true,
						edit:true,  // modifiable en update
						editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
						choiceInList:false
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
						type :"text",	
						hide:true,
						order:true,
						edit:true,  // modifiable en update
						editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
						choiceInList:false
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
				active:true, // permettre edition des champs editables
				showButton : true,// bouton d'edition visible
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
					return jsRoutes.controllers.sra.studies.api.Studies.update(line.code).url; // jamais utilisé si mode local
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
		if (! $scope.form.accession && ! $scope.form.externalId ) {
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

		$scope.studiesDT.save(); 
		$scope.tab_studies = $scope.studiesDT.getData();
		if(!$scope.tab_studies || $scope.tab_studies.length < 1) {
			$scope.reset();
			$scope.messages.setError(Messages("AbsenceDonneePourUpdate"));
			throw(Messages("AbsenceDonneePourUpdate"));
		}
		var tabRequestCreateForUpdate = [];
		
		//console.log("YYYYYYYYYY Dans update-ctrl.submit: nb_study=" + $scope.tab_bilans.length);
		// declencher creation d'un objet submission pour update :
		var study = $scope.tab_studies[0];  // requete avec ERP ou PRJEB qui renvoie un seul resultat
        console.log("xxxxxxxx userStudy.releaseDate=", study.releaseDate);
		console.log("releaseDate="+study.releaseDate);
		console.log("releaseDate="+ moment(study.releaseDate).calendar());
		console.log("date.now="+ Date.now());
		console.log("releaseDate.endOf="+ moment(study.releaseDate).endOf('day'));
		console.log("releaseDate.fromNow="+ moment(study.releaseDate).fromNow());
		console.log("releaseDate.endOf.fromNow="+ moment(study.releaseDate).endOf('day').fromNow());
		
		console.log(moment (study.releaseDate, "YYYY-MM-DD", true) .isValid ());
		console.log(moment (study.releaseDate, "DD/MM/YYYY", true) .isValid ());
		
		console.log (moment (study.releaseDate). format ('DD/MM/YYYY'));
		
		

		if(toolsServices.isNotBlank(study.locusTagPrefixs)) {
			console.log("typeOf study.locusTagPrefixs", typeof study.locusTagPrefixs);
			console.log(study.locusTagPrefixs);
			// si on edite la valeur de locusTagPrefixs dans datatable alors string, et sinon array d'ou teste sur le type :
			if(typeof study.locusTagPrefixs === 'string' || study.locusTagPrefixs instanceof String) {
				var strLocusTagPrefixs = study.locusTagPrefixs.replace(/,\s*$/, "");
				strLocusTagPrefixs = strLocusTagPrefixs.replace(/,\s*$/, "");
				delete study.locusTagPrefixs;
				study.locusTagPrefixs = strLocusTagPrefixs.split(','); 
		    	// Preferer boucle for au forEach ici car passage par valeur du locusTag d'ou modification dans le forEach qu'il faut sauvegarder
            	// hors de la boucle forEach. De plus, boucle for plus efficace.
				for(var i= 0; i < study.locusTagPrefixs.length; i++) {
     				study.locusTagPrefixs[i] = toolsServices.clean(study.locusTagPrefixs[i]);
				}
			}
		} else {
			delete study.locusTagPrefixs;
		}
		
	
		if(toolsServices.isNotBlank(study.idsPubmed)) {
			console.log("typeOf study.idsPubmed", typeof study.idsPubmed);
			console.log(study.idsPubmed);
			// si on edite la valeur de idsPubmed dans datatable alors string, et sinon array d'ou teste sur le type :
			if(typeof study.idsPubmed === 'string' || study.idsPubmed instanceof String) {
				var strIdsPubmed = study.idsPubmed.replace(/,\s*$/, "");
				strIdsPubmed = strIdsPubmed.replace(/,\s*$/, "");
				delete study.idsPubmed;
				study.idsPubmed = strIdsPubmed.split(','); 
		    	// Preferer boucle for au forEach ici car passage par valeur du idPubmed d'ou modification dans le forEach qu'il faut sauvegarder
            	// hors de la boucle forEach. De plus, boucle for plus efficace.
				for(var i= 0; i < study.idsPubmed.length; i++) {
     				study.idsPubmed[i] = toolsServices.clean(study.idsPubmed[i]);
				}
			}
		} else {
			delete study.idsPubmed;
		}
			
		
		study.description = study.studyAbstract;	// important de remettre la description avec meme valeur que abstract.	
		// mise à jour du study et project dans base qui se fait dans createSubmission() appelé par createForUpdate

		var args = {
			study       : study,
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
//				$scope.tab_studies.forEach(function(study) {
//					study.state.code="SUBU-SMD-IW";	
//				});
//				$scope.studiesDT.setData($scope.tab_studies, $scope.tab_studies.length);	

				// mieux de passer par valeur en base car ecrasement de userReleaseDate si < dbReleaseDate
				var tab_codes = [];
				$scope.studiesDT = datatable($scope.studiesDTConfig);
				$scope.studiesDT.setColumnsConfig($scope.getStudyColumns());
				for(var i= 0; i < $scope.tab_studies.length; i++) {
					tab_codes.push($scope.tab_studies[i].code);
				}
				$scope.studiesDT.search({codes:tab_codes});	
								
		}).catch(error => { 
			$scope.messages.addDetails(error.data); 
			$scope.messages.setError("Erreur de sauvegarde");   // important pour avoir le message.
			// Pas bonne idée car si message d'erreur bien que l'utilisateur voit sa saisie erronnée dans le tableau
			// affichage données originales dans base si donnée modifiées par user non validees
			//$scope.studiesDT.setData($scope.tab_studies_ori, $scope.tab_studies_ori.length);				
		});
	
	};// end $scope.submit


}]);



