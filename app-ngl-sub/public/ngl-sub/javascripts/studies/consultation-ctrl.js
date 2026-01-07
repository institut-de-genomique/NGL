"use strict";

angular.module('home').controller('ConsultationCtrl',[ '$http', '$scope', '$routeParams' , '$q', 'mainService', 'lists', 'tabService', 'messages', 'toolsServices', 'datatable', '$window',
	                                                  function($http, $scope, $routeParams, $q, mainService, lists, tabService, messages, toolsServices, datatable, $window) { 

	var getStudyColumns = function() {
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
			    		"filter"     :"codes:'state'",
						header       : Messages("study.state.code"),
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
			columns.push({property:"description",            // mettre description et pas abstract car abstract sera synchronisé  sur description.
						header: Messages("study.description"),
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
						type         : "text",
						hide         : true,
						edit         : true, 
						order        : false,
						choiceInList : false,
						listStyle    : 'bt-select',
						});
			columns.push({
						property     : "idsPubmed",
						header       : Messages("study.idsPubmed"),
						type         : "text",
						hide         : false,
						edit         : true, // study editable uniquement si SUB-N et à ce stade
						order        : false,
						choiceInList : false,
						listStyle    : 'bt-select',
						});
			columns.push({
						property     : "parentProjectAccessions",
						header       : Messages("study.parentProjectAccessions"),
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
						hide         : false,
						edit         : true, 
						order        : true,
						choiceInList : false,
						});		
			columns.push({
						property     : "scientificName",
						header       : Messages("project.scientificName"),
						type         : "text",
						hide         : false,
						edit         : false, 
						order        : true,
						choiceInList : false,
						});	
						
			columns.push({property    : "dropBox",
						header        : Messages("project.dropBox"),
						type          : "String",
						hide          : true,
						edit          : false,
						order         : false,
						choiceInList  : true,
						listStyle     : 'bt-select',
						possibleValues: 'sraVariables.dropBox',
				    	});		
//			columns.push({property    : "attributes",
//					    header        : Messages("study.attributes"),
//					    //"filter":"codes:'state'",
//					    type          : "text",		    	  	
//					    order         : false, 
//					    edit          : true,  
//					   	hide          : true,
//						choiceInList  : false,
//						editTemplate  :"<textarea class='form-control' #ng-model rows='3'></textarea>",
//						render        : function(data, edit) {
//							              return toolsServices.getStringAttributes(data);
//						              },
//				        });	
			columns.push({property    : "attributes",
					    header        : Messages("study.attributes"),
					    //"filter":"codes:'state'",
					    type          : "text",		    	  	
					    order         : false, 
					    edit          : true,  
					   	hide          : true,
						choiceInList  : false,
						editTemplate  :"<textarea class='form-control' #ng-model rows='3'></textarea>",
				        });	
			columns.push({
						property      : "comments",
						header        : Messages("comments"),
						type          : "text",
						hide          : true,
						edit          : false, // pas modifiable en consultation car uniquement dans vue detail
						order         : false,
						choiceInList  : false,
						filter        : "collect:'comment'",
						listStyle     : 'bt-select',
						});	
	
			return columns;
		};
      
	
	var studiesDTConfig = {
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
				lineMode: function (line) {
					//console.log("line", line);
					if((line.state.code === "SUB-N" || line.state.code === "NONE") // modification autorisee seulement pour les samples nouvellement crees.
						&&line._type === "Study") { // On n'edite pas les ExternalStudy
						// suppression controle user
						//&& line.traceInformation.createUser === $scope.user) // limiter autorisation de modification au proprietaire.
						
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
				active      :true,
				showButton  : true,
				mode        :'remote', 
				changeClass : true,   // pour mettre les lignes bien sauvegardees en verts et celles en echec en rouge
				url:function(line){
					// pour regler problème de mapping entre representation du study dans datatable et dans model java :
					//console.log("line=", line);
					//console.log("$scope.studiesDT  du datatable avant save", $scope.studiesDT); 
					$scope.toJsonStudyModelOrThrow(line, $scope.errorDetails);
					//console.log("line dans bon model =", line);
					return jsRoutes.controllers.sra.studies.api.Studies.update(line.code).url; // jamais utilisé si mode local 
					// et si mode remote alors problème com.fasterxml.jackson.databind.JsonMappingException pour les champs locus_tag ...
					// d'ou appel préalable de toJsonStudyModel;
				},
				method:'put',
				//withoutEdit:true, //??

				value:function(line){
					return line;
				},
				
				callback : function(datatable, errors) {
					//console.log("XXXXXXXXXXXXXXXXX           datatable", datatable);
					//console.log("XXXXXXXXXXXXXXXXX           errors", errors);
					$scope.updateCallback(datatable, errors);
				}
			},
				
	};
	
	

	

//---------------------------------------------------------------------------------------------------
	// Definitions des methodes :
	//---------------------------
	$scope.toJsonStudyModelOrThrow = function(line, errorDetails) {
		toolsServices.toJsonStudyModel(line, errorDetails);
		//line.description = line.studyAbstract;   // mettre la description avec meme valeur que abstract => fait dans java
		//console.log("dans save, scope.errorDetails=", $scope.errorDetails);
		if((Object.keys(errorDetails)).length > 0) {
			//console.log("AAAAAAAAAAAAAAAAAAA               J'ai bien une erreur de  PARSING");
			$scope.messages = messages(); // si on re-sauve sans recharger page.
			$scope.messages.clear();      // si on re-sauve sans recharger page.
			$scope.messages.addDetails($scope.errorDetails);
			$scope.messages.setError("save");
			$scope.studiesDT.setSpinner(false);
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
		} else {
			$scope.messages.setSuccess("save");			
		}
	}; // end updateCallback
	

					
	$scope.reset = function() {
		$scope.form = {};
		$scope.studiesDT = datatable(studiesDTConfig);
		$scope.studiesDT.setColumnsConfig(getStudyColumns());
		$scope.messages = messages();
		$scope.messages.clear();
		$scope.errorDetails = {};
		//console.log("99999999999999999999999999999999999999999999999999999999999999  passage dans reset");
	};
	
	
	$scope.search = function(user){
		$scope.messages = messages();
		$scope.messages.clear();
		var str = "user='" + $scope.user + "'";
		//console.log("Dans le search", str);
		if( ! toolsServices.isNotBlank(user)) {
			$scope.messages.setError(toolsServices.getNoLoginAlert());
			return;
		}	
		//console.log("$scope.messages = ", $scope.messages);
		//console.log("Dans consultation-ctr.search, avec user", user);
		if (	! $scope.form.identifierRegex  && 
				! $scope.form.identifiers      &&
				! $scope.form.codeRegex        &&
				! $scope.form.codes            &&
				! $scope.form.pseudoStateCodes &&
				! $scope.form.projCodes ) {
				//console.log("Aucun parametre => envoyer message erreur");
				$scope.messages.setError(Messages("AbsenceSearchParameter"));
				//console.log("$scope.messages = ", $scope.messages);
				throw("Aucun parametre pour la recherche des study");   
			} 
			//console.log("form = ", $scope.form);
		$scope.user = user;
		// Remplacement des pesudoStateCodes par codes stateCodes dans le formulaire et destruction
		// de form.pseudoStateCodes pour pouvoir envoyer directement form a la methode search du datatable:
		$scope.saveUserForm = angular.copy($scope.form);// sauver formulaire utilisateur initial avant modif dans replacePseudoStateCodesToStateCodesInFormulaire
		toolsServices.replacePseudoStateCodesToStateCodesInFormulaire($scope.sraVariables.pseudoStateCodeToStateCodes, $scope.form);
		$scope.studiesDT.search($scope.form);
		$scope.form = angular.copy($scope.saveUserForm);// remettre formulaire utilisateur initial
		
		// stoquer form dans main pour pouvoir l'utiliser de n'importe ou : (quand on clique sur l'onglet recherche de la vue details) :
		// attention copie avec passage par valeur, si on modifie $scope.form dans le code, mainService ne contiendra pas les modifications.
		// d'ou importance de le copier dans la methode search et non à l'initialisation.
		mainService.setForm($scope.form);

		// inutile de sauver le datatable renseigné dans mainService car fait a l'initialisation et passage par adresse. 
		// modifications de $scope.studiesDT prises en compte dans mainService
		// mainService.setDatatable($scope.studiesDT);
		// mainService.setDatatable($scope.studiesDT);  
	};
	
	
	
//------------------------------------------------------------------------------------------------------------------
	
	
	// Initialisations :
	//-------------------
	//console.log("Dans studies.consultation-ctrl.js");
	$scope.messages = messages();	
	$scope.messages.clear();
	$scope.form = {};  // important. 
	$scope.lists = lists;		
	$scope.sraVariables = {}; // si on declare dans services => var sraVariables = {};
	$scope.errorDetails = {};
	// Pour avoir les onglets a gauche de la page :
	if (angular.isUndefined(mainService.getHomePage())) {
		mainService.setHomePage('consultation');
		tabService.addTabs({
			label: Messages('studies.menu.consultation'),
			href: jsRoutes.controllers.sra.studies.tpl.Studies.home("consultation").url,
			remove:true});
		tabService.activeTab(0); // active l'onglet, le met en bleu
	}		
	



//	Initialisation ProjectCodes:
	$scope.lists.refresh.projects();
	
//	Initialisation datatable : le stoquer dans main pour pouvoir le recuperer depuis une autre vue
//	$scope.studiesDT = datatable(studiesDTConfig);
//	$scope.studiesDT.setColumnsConfig(getStudyColumns());
	
//	if ($scope.studiesDT == null) {
//		console.log("studiesDT null                  nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn");
//	}
//	if ($scope.studiesDT == undefined) {
//		console.log("studiesDT undefined                 nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn");
//	}
	
	// datatable dans mainService qui est partagee par plusieurs ctr ou services du coup peut etre recuperé dans details.ctr
	if (studiesDTConfig && angular.isUndefined(mainService.getDatatable())) {
		$scope.studiesDT = datatable(studiesDTConfig);
        $scope.studiesDT.setColumnsConfig(getStudyColumns());
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
	

	
	// Recuperer form depuis mainService si existe  :
	// mais ne pas installer $scope.form dans mainService s'il n'existe pas dans mainService, 
	// car methode mainService.setForm($scope.form) qui fait une copie par valeur 
	// => si modifications ulterieures du $scope.form, le form de mainService ne sera pas modifié
	// => installer la copie de $scope.form dans mainService dans methode $scope.search()
	if(angular.isDefined(mainService.getForm())) {
        $scope.form = mainService.getForm();
    } // else
	
//	$window.onbeforeunload = function(){
//		 console.log("BBBBBBBBBBBBBBBB          Etes vous sure de vouloir changer de page?");
//	};
		
	// Initialisation des etats :
	lists.refresh.states({objectTypeCode: "SRASubmission"});
	$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'simplifiedStatesWithNone'}})
	.success(function(data) {
		$scope.sraVariables.simplifiedStatesWithNone = data;	
	});	
//	$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'pseudoStateCodeToStateCodes'}})
//	.success(function(data) {
//		$scope.sraVariables.pseudoStateCodeToStateCodes = data;
//	console.log("CCCCCCCCCCCCCCCCC Dans initListService, consultationService.sraVariables.pseudoStateCodeToStateCodes pour pseudo-03-EnCours :" , consultationService.sraVariables.pseudoStateCodeToStateCodes);
//	});
	$http.get(jsRoutes.controllers.sra.api.Variables.get('pseudoStateCodeToStateCodes').url)
	.success(function(data) {
		$scope.sraVariables.pseudoStateCodeToStateCodes = data;
	});
	
//	Initialistation ExistingStudyType:
	$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'existingStudyType'}})
	.success(function(data) {
	// initialisation de la variable createService.sraVariables.exitingStudyType utilisée dans create.scala.html
	$scope.sraVariables.existingStudyType = data;																					
	});
	
//	Initialistation ExistingStudyType:
	$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'dropBox'}})
	.success(function(data) {
	// initialisation de la variable createService.sraVariables.dropBox utilisée dans create.scala.html
	$scope.sraVariables.dropBox = data;																					
	});	
			
	// NGL-4708
	$scope.isShowConsigne=false;
	$scope.toggleShowConsigne=function(){
		//console.log("TOGGLE search-ctrl.js");
		if ( $scope.isShowConsigne===false) { $scope.isShowConsigne=true}
		else {$scope.isShowConsigne=false}
	};	

}]);
