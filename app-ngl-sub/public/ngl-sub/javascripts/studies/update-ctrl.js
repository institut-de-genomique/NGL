"use strict";

angular.module('home').controller('UpdateCtrl',
		[ '$http', '$scope', '$routeParams' , '$q', 'mainService', 'lists', 'tabService', 'messages', 'toolsServices', 'datatable',
  function($http,   $scope,   $routeParams,    $q,   mainService,   lists,   tabService,  messages, toolsServices, datatable) { 


	
	var studiesDTConfig = {
			name:'studiesDT',
			order :{by:'code',mode:'local', reverse:true},
			search:{
				url:jsRoutes.controllers.sra.studies.api.Studies.list()
			},
			pagination:{
				active:true,
				mode:'local'
			},
			select:{active: false, showButton: false},
			showTotalNumberRecords:true,
			edit : {
				active:true, // permettre edition des champs editables
				showButton : true,// bouton d'edition visible sur chaque colonne
				withoutSelect : true,
				columnMode : true,
				lineMode : function(line) {
					if(line.state.code === "SUB-F" // modification autorisee seulement pour les study avec AC et non engagés dans une soumission en cours
						&& line._type === "Study"  // On n'edite pas les ExternalStudy
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
			show:{                   // bouton pour epingler si on passe par details-ctrl.js 
				active:false,
				showButton : false,
				add :function(line){
					tabService.addTabs({label:line.code,href:jsRoutes.controllers.sra.studies.tpl.Studies.get(line.code).url,remove:true});
				}
			},
			save : {
				active:true,
				showButton : false,
				changeClass : false,
				withoutEdit : false,  // si false ne sauve que si modif
				url:function(line){
//					line.state.code = "SUBU-N";
					return jsRoutes.controllers.sra.studies.api.Studies.update(line.code).url;
				},
				method:'put',
				value:function(line){
					return line;
				},
				mode:'local'
			},

	};
	
	
	var getStudyColumns = function(){
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
					property     : "_type",
	    			header       : Messages("type"),
       				type         : "text",		    	  	
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
						edit     : false,
						order    : true
						});	
				columns.push({property:"releaseDate",
						header: Messages("study.releaseDate"),
						type :"date",		    	  	
						order:false,
						edit:false,
						choiceInList:false  
						});
				columns.push({property:"code",
			        	header: Messages("study.code"),
			        	type :"text",		    	  	
			        	order:false,
			        	edit :false
			        	});	
			     // voir comment afficher liste des codesProjets.	
			 	columns.push({property:"projectCodes",
			        	header: Messages("study.projectCodes"),
			        	type :"text",		    	  	
			        	order:false,
			        	edit:false,
			        	choiceInList:false  
			        	});					
			   	columns.push({property:"accession",
			        	header: Messages("study.accession"),
			        	type :"text",		    	  	
			        	order:false,
			        	edit:false,
			        	choiceInList:false  
			        	});	
			   	columns.push({property:"externalId",
			        	header: Messages("study.externalId"),
			        	type :"text",		    	  	
			        	order:false,
			        	edit:false,
			        	choiceInList:false  
			        	});				        	
				columns.push({
						property     : "centerProjectName",
						header       : Messages("study.centerProjectName"),
						type         : "text",		    	  	
						order        : true,
						edit         : true,
						choiceInList : false  
						});	
			  	columns.push({property:"title",
						header: Messages("study.title"),
						type :"String",
						hide:false,
			        	edit:true,
						order:false,
				    	choiceInList:false
				    	});	    
			  	columns.push({property:"studyAbstract",
						header: Messages("study.abstract"),
						type :"String",
			        	hide:false,
			        	edit:true,
						order:false,
				    	choiceInList:false
				    	});	
			   	columns.push({property:"description",
						header: Messages("study.description"),
						type :"String",
			        	hide:false,
			        	edit:true,
						order:false,
				    	choiceInList:false
				    	});	
			      
				columns.push({property:"existingStudyType",
						header: Messages("study.existingStudyType"),
						type :"String",
						hide:false,
						edit:true,
						order:false,
						choiceInList:true,
						listStyle:'bt-select',
						possibleValues:'sraVariables.existingStudyType',
				    	});
		return columns;
	};
	

//---------------------------------------------------------------------------------------------------
	
	// Initialisations :
	//------------------
	console.log("Dans update-ctrl.js");
	$scope.messages = messages();	
	$scope.form = {};  // important. 
	$scope.form.createUser = $scope.user;

	// Oeration update possible uniquement sur Study (pas sur ExternalStudy) dans etat SUB-F :
	$scope.form.stateCode ="SUB-F";
	$scope.form.type="Study"; // important de ne pas proposer ExternalStudy pour update.
	$scope.sraVariables = {};
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('update');
		tabService.addTabs({label:Messages('studies.menu.update'),href:jsRoutes.controllers.sra.studies.tpl.Studies.home("update").url,remove:true});
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
	$scope.studiesDT = datatable(studiesDTConfig);
	$scope.studiesDT.setColumnsConfig(getStudyColumns());

//------------------------------------------------------------------------------------------
	
	// Definitions methodes :
	//-----------------------
	
	// methode appelée depuis la vue avec la directive ng-init
	$scope.setUserInScope = function(user) {
		$scope.user = user;
		console.log("Dans setUserInScope, user= ", user);
	};

	$scope.search = function(user) {
		$scope.messages = messages();
		console.log("Dans update-ctrl.search");
		console.log("user", $scope.user)
		if (! $scope.form.accession && ! $scope.form.externalId ) {
			$scope.messages.setError(Messages("AbsenceSearchParameter"));
			console.log("update-ctrl.$scope.messages = ", $scope.messages);
			// annuler spinner si besoin
			throw("Aucun parametre pour la recherche du study à updater");  
		} 
		$scope.form.stateCode="SUB-F";
		$scope.form.type="Study";
		$scope.form.createUser = $scope.user;
		$scope.studiesDT.search($scope.form);
	};

	$scope.reset = function(){
		console.log("Dans update-ctrl.resetForm");
		$scope.form = {};
		$scope.form.createUser = $scope.user;
		$scope.tab_studies = []; 
		//$scope.studiesDT = null;    // ne marche pas datatable
		$scope.studiesDT = datatable(studiesDTConfig);
		$scope.studiesDT.setColumnsConfig(getStudyColumns());
		$scope.messages = messages();
		$scope.form.stateCode ="SUB-F";
		$scope.form.type="Study"; // important de ne pas proposer ExternalStudy pour update 
	};


	$scope.submit = function() {
		$scope.messages = messages();
		console.log("YYYYYYYYYY Dans update-ctrl.submit");
		mainService.setForm($scope.form);
		// sauvegarde des données en local (mode local le save de getColumnStudy) 
		// (si mode remote, les donnees sont bien sauvées dans base avec valeurs editées mais 
		// le datatable utilisé ici ne contient pas les modifs des utilisateurs 
		// => en mode remote, dans le datatable, le modele n'est pas synchronisé avec la vue.
		// En mode local, dans le datatable, le modele est synchronisé avec la vue et contient bien 
		// les modifications des utilisateurs, en revanche pas de sauvegarde dans la base !

		$scope.studiesDT.save(); 
		var tab_studies = $scope.studiesDT.getData();
		if(!tab_studies || tab_studies.length < 1) {
			$scope.reset();
			$scope.messages.setError(Messages("AbsenceDonneePourUpdate"));
			throw(Messages("AbsenceDonneePourUpdate"));
		}
		var allRequest = [];
		
		console.log("YYYYYYYYYY Dans update-ctrl.submit: nb_study=" + tab_studies.length);
		// declencher creation d'un objet submission pour update :
		
		var args = {
			project     : null,
			study       : tab_studies[0],
			samples     : [],
			experiments : []
		};
		
		// declencher creation d'un objet submission pour update :
		var request = $http.put(jsRoutes.controllers.sra.submissions.api.Submissions.createForUpdate().url, args);
//		.catch (error => {
//			console.log(error);
//			$scope.messages.addDetails(error.data); 
//			return $q.reject(error);                // propage erreur, comme throw 
//		});
		allRequest.push(request);
		$q.all(allRequest)
		.then(result => { // result de l'operation precedente, cad la creation de la soumission
				console.log("result_2 = " + result); 	
				console.log("result_2 = " + result[0].data); 	
				console.log("result_2 = " + result[0].data.code); 	
				var submission = result[0].data;
				var submissionCode = result[0].data.code;
				var submissionState = angular.copy(result[0].data.state);
				submissionState.code = "SUBU-SMD-IW";
				//$scope.messages.setSuccess("La soumission " + submissionCode + " pour la mise a jour à l'EBI des données du tableau est en cours de traitement"); 
				return $http.put(jsRoutes.controllers.sra.submissions.api.Submissions.updateState(submissionCode).url, submissionState);					
		}).then(result => { // result de l'operation precedente cad la mise à jour de l'etat de la soumission
				console.log("result_3 = " + result); 	
				console.log("result_3 = " + result.data); 
				var submissionCode = result.data.code;
				$scope.messages.setSuccess("La soumission " + submissionCode + " pour la mise a jour à l'EBI des données du tableau est en cours de traitement"); 
				// rafraichissement tableau avec nouveaux status: 
				var tab_codes = [];
				for(var i= 0; i < tab_studies.length; i++){
					tab_codes.push(tab_studies[i].code);
				}
				$scope.studiesDT = datatable(studiesDTConfig);
				$scope.studiesDT.setColumnsConfig(getStudyColumns());
				$scope.studiesDT.search({codes:tab_codes});					
		}).catch(error => { 
			$scope.messages.addDetails(error.data); 
			$scope.messages.setError("Erreur de sauvegarde");   // important pour avoir le message.
			// sgas pour affichage données dans base si donnée modifiées par user non validees
			var tab_codes = [];
			for(var i= 0; i < tab_studies.length; i++){
				tab_codes.push(tab_studies[i].code);
			}
			$scope.studiesDT = datatable(studiesDTConfig);
			$scope.studiesDT.setColumnsConfig(getStudyColumns());
			$scope.studiesDT.search({codes:tab_codes});
		});
	};// end $scope.submit

}]);



