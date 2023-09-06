"use strict";

angular.module('home').controller('ConsultationCtrl',[ '$http', '$scope', '$routeParams' , '$q', 'mainService', 'lists', 'tabService', 'messages', 'toolsServices', 'datatable', '$window',
	                                                  function($http, $scope, $routeParams, $q, mainService, lists, tabService, messages, toolsServices, datatable, $window) { 

	var getProjectColumns = function() {
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
// Pas de ERP dans le cas d'un project umbrella et sequencingProject pas stoqué dans collection project mais fusionné avec study
//			columns.push({
//						property     : "externalId",
//					    header       : Messages("project.externalId"),
//				       	type         : "text",		    	  	
//				       	order        : true,
//				       	edit         : false,
//				       	choiceInList : false  
//				       	});	
// Dans nouveau model, uniquement des projects de type umbrella stoqués dans collection project			        	    	
//			columns.push({
//						property     : "submissionProjectType",
//					    header       : Messages("project.submissionProjectType"),
//				       	type         : "text",		    	  	
//						hide         : true,	    	  	
//				       	order        : true,
//				       	edit         : false,
//				       	choiceInList : false  
//				       	});		
		        			        			        
			columns.push({
						property     : "title",
						header       : Messages("project.title"),
						type :"text",	
						hide:true,
						order:true,
						edit:true,  
						editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
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
// Pas de locus_tag dans le cas d'un project umbrella et sequencingProject pas stoqué dans collection project mais fusionné avec study
//			columns.push({
//						property     : "locusTagPrefixs",
//				       	header       : Messages("project.locusTagPrefixs"),
//				       	type         : "text",		    	  	
//						hide         : true,	    	  	
//				       	order        : false,
//				       	edit         : false,
//				       	choiceInList : false  
//				       	}); 
			columns.push({
						property     : "childrenProjectAccessions",
				       	header       : Messages("project.childrenProjectAccessions"),
				       	type         : "text",		    	  	
						hide         : true,	    	  	
				       	order        : false,
				       	edit         : false,
				       	choiceInList : false  
				       	}); 	
			columns.push({
						property     : "idsPubmed",
						header       : Messages("project.idsPubmed"),
						type         : "text",
						hide         : true,
						edit         : false, 
						order        : false,
						choiceInList : false,
						listStyle    : 'bt-select',
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

	
	var projectsDTConfig = {
			name:'projectsDT',
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
				active:false, // permettre edition des champs editables
				showButton : false,// bouton d'edition 
				withoutSelect : true,
				columnMode : true
			},
			
			cancel : {
				showButton:false
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
					// ajout onglet avec nom du project permettant d'acceder à la vue details
					tabService.addTabs({label:line.code,
										href:jsRoutes.controllers.sra.projects.tpl.Projects.get(line.code).url,
										remove:true // permet fermer onglet
										});
					// inutile d'installer le datatable renseigné dans mainService car fait a l'initialisation et passage par adresse. 
					// modifications de $scope.studiesDT prises en compte dans mainService
					// mainService.setDatatable($scope.projectsDT);
				}
			},
//			save : {
//				active:true,
//				showButton : true,
//				changeClass : false,
//				url:function(line){
//					return jsRoutes.controllers.sra.projects.api.Projects.update(line.code).url; // jamais utilisé si mode local
//				},
//				method:'put',
//				value:function(line){
//					return line;
//				},
//			},

	};
	
	

	

//---------------------------------------------------------------------------------------------------
	
	// Definitions des methodes :
	//---------------------------
	
	$scope.reset = function() {
		$scope.form = {};
		$scope.form.submissionProjectType = "UMBRELLA_PROJECT";
		//$scope.form.submissionProjectType = $scope.submissionProjectType;
		$scope.projectsDT = datatable(projectsDTConfig);
		$scope.projectsDT.setColumnsConfig(getProjectColumns());
		$scope.messages = messages();	// enleve le message mais pas la partie details + efficace d'utiliser $scope.messages.clear();
		$scope.messages.clear();
		//console.log("99999999999999999999999999999999999999999999999999999999999999  passage dans reset");
	};


	// methode appelée depuis la vue avec la directive ng-init
	$scope.setUserInScope = function(user) {
		$scope.user = user;
		//console.log("Dans setUserInScope, user= ", user);
	};


//	// methode appelée depuis la vue avec la directive ng-init
//	$scope.setSubmissionProjectType = function(submissionProjectType) {
//		$scope.submissionProjectType = submissionProjectType;
//		console.log("Dans setSubmissionProjectType, submissionProjectType = ", submissionProjectType);
//	};	
	
	$scope.search = function(user){
		$scope.messages = messages();	// enleve le message mais pas la partie details + efficace d'utiliser $scope.messages.clear();
		$scope.messages.clear();
		//console.log("$scope.messages = ", $scope.messages);
		//console.log("Dans consultation-ctr.search, avec user", user);
		//$scope.form.submissionProjectType = "SEQUENCING_PROJECT";
		//$scope.form.submissionProjectType = $scope.submissionProjectType;

//		if (! $scope.form.externalIdRegex      && 
//				! $scope.form.externalIds      &&
//				! $scope.form.accessionRegex   && 
//				! $scope.form.accessions       && 
//				! $scope.form.codeRegex        &&
//				! $scope.form.codes            &&
//				! $scope.form.pseudoStateCodes &&
//				! $scope.form.projCodes ) {
//				console.log("Aucun parametre => envoyer message erreur");
//				$scope.messages.setError(Messages("AbsenceSearchParameter"));
//				console.log("$scope.messages = ", $scope.messages);
//				throw("Aucun parametre pour la recherche des Project");   
//			} 
		$scope.user = user;
		// Remplacement des pesudoStateCodes par codes stateCodes dans le formulaire et destruction
		// de form.pseudoStateCodes pour pouvoir envoyer directement form a la methode search du datatable:
		$scope.saveUserForm = angular.copy($scope.form);// sauver formulaire utilisateur initial avant modif dans replacePseudoStateCodesToStateCodesInFormulaire
		toolsServices.replacePseudoStateCodesToStateCodesInFormulaire($scope.sraVariables.pseudoStateCodeToStateCodes, $scope.form);
		$scope.projectsDT.search($scope.form);
		$scope.form = angular.copy($scope.saveUserForm);// remettre formulaire utilisateur initial
		
		// stoquer form dans main pour pouvoir l'utiliser de n'importe ou : (quand on clique sur l'onglet recherche de la vue details) :
		// attention copie avec passage par valeur, si on modifie $scope.form dans le code, mainService ne contiendra pas les modifications.
		// d'ou importance de le copier dans la methode search et non à l'initialisation.
		mainService.setForm($scope.form);

		// inutile de sauver le datatable renseigné dans mainService car fait a l'initialisation et passage par adresse. 
		// modifications de $scope.projectsDT prises en compte dans mainService
		// mainService.setDatatable($scope.projectsDT);
		// mainService.setDatatable($scope.projectsDT);  
	};
	
	
	
//------------------------------------------------------------------------------------------------------------------
	
	
	// Initialisations :
	//-------------------
	//console.log("Dans projects.consultation-ctrl.js");
	$scope.messages = messages();	// enleve le message mais pas la partie details + efficace d'utiliser $scope.messages.clear();
	$scope.messages.clear();
	$scope.form = {};  // important. 
	$scope.form.submissionProjectType = "UMBRELLA_PROJECT";
	
	$scope.form.submissionProjectType = $scope.submissionProjectType;

	$scope.lists = lists;		
	$scope.sraVariables = {}; // si on declare dans services => var sraVariables = {};
	
	// Pour avoir les onglets a gauche de la page :
	if (angular.isUndefined(mainService.getHomePage())) {
		mainService.setHomePage('consultation');
		tabService.addTabs({
			label: Messages('projects.menu.consultation'),
			href: jsRoutes.controllers.sra.projects.tpl.Projects.home("consultation").url,
			remove:true});
		tabService.activeTab(0); // active l'onglet, le met en bleu
	}		
	



//	Initialisation ProjectCodes:
	$scope.lists.refresh.projects();
	
//	Initialisation datatable : le stoquer dans main pour pouvoir le recuperer depuis une autre vue
//	$scope.projectsDT = datatable(projectsDTConfig);
//	$scope.projectsDT.setColumnsConfig(getProjectColumns());
	
	if ($scope.projectsDT == null) {
		//console.log("projectsDT null                  nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn");
	}
	if ($scope.projectsDT == undefined) {
		//console.log("projectsDT undefined                 nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn");
	}
	
	// datatable dans mainService qui est partagee par plusieurs ctr ou services du coup peut etre recuperé dans details.ctr
	if (projectsDTConfig && angular.isUndefined(mainService.getDatatable())) {
		$scope.projectsDT = datatable(projectsDTConfig);
        $scope.projectsDT.setColumnsConfig(getProjectColumns());
        // installation de l'adresse du datatable dans mainService  => toutes 
        // les modifications uterieures de $scope.projectsDT seront visibles dans mainService mais
        // on pourra acceder à scope.projectsDT meme quand on revient sur la page recherche
        // car on ne relance pas la recherche ?????
        mainService.setDatatable($scope.projectsDT); 
    	//console.log("AAAAAAAAAAAA         installation du datatable dans le mainService");
    } else if (angular.isDefined(mainService.getDatatable())) {
    	$scope.projectsDT = mainService.getDatatable();  
    	//console.log("AAAAAAAAAAAA         recuperation du datatable depuis le mainService", $scope.projectsDT);
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
	$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'simplifiedStates'}})
	.success(function(data) {
		$scope.sraVariables.simplifiedStates = data;	
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
	
	

}]);
