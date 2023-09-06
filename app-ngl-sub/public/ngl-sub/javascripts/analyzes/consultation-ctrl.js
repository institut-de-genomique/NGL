"use strict";

angular.module('home').controller('ConsultationCtrl',[ '$http', '$scope', '$routeParams' , '$q', 'mainService', 'lists', 'tabService','messages', 'datatable', 'toolsServices',
	                                                  function($http, $scope, $routeParams, $q, mainService, lists, tabService, messages, datatable, toolsServices) { 


	var analyzesDTConfig = {
			pagination:{
				active:true,
				mode:'local',
                numberRecordsPerPage: 100
			},
			select:{active:true},
			showTotalNumberRecords:true,
			order :{mode:'local', by:'code', reverse : true},
			search:{
				url:jsRoutes.controllers.sra.analyzes.api.Analyzes.list()
			},
			show:{
				active:true,
				add :function(line) {
					// ajout onglet avec nom de la soumission permettant d'acceder à la vue details
					tabService.addTabs({label:line.code,
										href:jsRoutes.controllers.sra.analyzes.tpl.Analyzes.get(line.code).url,
										remove:true});
				}
			},
			hide:{
				active:true
			},
			exportCSV:{
				active:true
			}, 
			cancel : {
				showButton:false
			},
			name:"Analyzes"
	};
	var getAnalysisColumns = function(){
		var columns = [];
		columns.push({
			property: "traceInformation.creationDate",
			header: Messages("traceInformation.creationDate"),
			type: "date",
			order: true
		});	
		columns.push({property:"traceInformation.createUser",
			header: Messages("traceInformation.creationUser"),
			type :"date",		    	  	
			order:true
		});
		columns.push({	property:"code",
			    	  	header: Messages("analyzes.code"),
			    	  	type :"text",		    	  	
			    	  	order:true});
		columns.push({	property:"projectCodes",
    	  				header: Messages("analyzes.projectCodes"),
    	  				type :"text",		    	  	
    	  				order:true});		
		columns.push({	property:"title",
						header: Messages("analyzes.title"),
						type :"text",		    	  	
						order:true});
		columns.push({	property:"description",
						header: Messages("analyzes.description"),
						type :"text",		    	  	
						order:true});	
		columns.push({
						property: "firstSubmissionDate",
						header: Messages("firstSubmissionDate"),
						type: "date",
						order: true});		
		columns.push({	property:"accession",
			    	  	header: Messages("analyzes.accession"),
			    	  	type :"text",		    	  	
			    	  	order:true});
		columns.push({	property:"studyAccession",
			    	  	header: Messages("analyzes.studyAccession"),
			    	  	type :"text",		    	  	
			    	  	order:true});
		columns.push({	property:"sampleAccession",
			    	  	header: Messages("analyzes.sampleAccession"),
			    	  	type :"text",		    	  	
			    	  	order:true});		
		columns.push({	property:"state.code",
						"filter":"codes:'state'",
						header: Messages("analyzes.state"),
						type :"text",
						order:true});	
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

	$scope.reset = function() {
		$scope.form = {};
		$scope.analyzesDT = datatable(analyzesDTConfig);
		$scope.analyzesDT.setColumnsConfig(getAnalysisColumns());
		$scope.messages = messages(); 
		$scope.messages.clear();
	};
	
	

	// methode appelee pour remplir le tableau des analyzes
	// Recherche toutes les analyzes pour projCode indiqué :
	$scope.search = function() {
		//console.log("dans consultation-ctrl.search : projCode " + $scope.form.projCode);	
		//console.log("dans consultation-ctrl.search : state !!!!!'" + $scope.form.state+"'");
		$scope.saveUserForm = angular.copy($scope.form);// sauver formulaire utilisateur initial avant modif dans replacePseudoStateCodesToStateCodesInFormulaire
		toolsServices.replacePseudoStateCodesToStateCodesInFormulaire($scope.sraVariables.pseudoStateCodeToStateCodes, $scope.form);
		//console.log("scope.form", $scope.form);
		$scope.analyzesDT.search($scope.form);
		$scope.form = angular.copy($scope.saveUserForm);// remettre formulaire utilisateur initial
		// stoquer form dans main pour pouvoir l'utiliser de n'importe ou : (quand on clique sur l'onglet recherche de la vue details) :
		// attention copie avec passage par valeur, si on modifie $scope.form dans le code, mainService ne contiendra pas les modifications.
		// d'ou importance de le copier dans la methode search et non à l'initialisation.
		mainService.setForm($scope.form);// stoquer form dans main pour pouvoir le reappeler depuis details.ctrl

	};	 

	
//------------------------------------------------------------------------------------------------------------------
	
	
	// initialisations :
	//console.log("Dans analyzes.consultation-ctrl.js");

	$scope.messages = messages();
	$scope.messages.clear();
	$scope.form = {};  // important. 
	$scope.lists = lists; // service lists
	$scope.sraVariables = {};
	
	// Pour avoir les onglets a gauche de la page :
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('consultation');
		tabService.addTabs({label:Messages('analyzes.menu.consultation'),
							href:jsRoutes.controllers.sra.analyzes.tpl.Analyzes.home("consultation").url,
							remove:true}); // remove pour avoir croix pour suppprimer onglet
		tabService.activeTab(0); // active l'onglet en le mettant en bleu
	}


//	Initialisation datatable :

	// datatable dans mainService qui est partagee par plusieurs ctr ou services du coup peut etre recuperé dans details.ctr
	if (analyzesDTConfig && angular.isUndefined(mainService.getDatatable())){
		$scope.analyzesDT = datatable(analyzesDTConfig);
        $scope.analyzesDT.setColumnsConfig(getAnalysisColumns());      
	    // installation de l'adresse du datatable dans mainService  => toutes 
        // les modifications uterieures de $scope.studiesDT seront visibles dans mainService.
		mainService.setDatatable($scope.analyzesDT);
    } else if(angular.isDefined(mainService.getDatatable())){
    	$scope.analyzesDT = mainService.getDatatable();  
    }

	// Recuperer form depuis mainService si existe  :
	// mais ne pas installer $scope.form dans mainService s'il n'existe pas dans mainService, 
	// car methode mainService.setForm($scope.form) qui fait une copie par valeur 
	// => si modifications ulterieures du $scope.form, le form de mainService ne sera pas modifié
	// => installer la copie de $scope.form dans mainService dans methode $scope.search()
	if(angular.isDefined(mainService.getForm())) {
		//console.log("ok recuperation de form dans le main")
        $scope.form = mainService.getForm();
    } 
	
	$scope.lists.refresh.projects();
	$scope.lists.refresh.states({objectTypeCode:"SRASubmission"});
	
	$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'simplifiedStates'}})
	.success(function(data) {
		$scope.sraVariables.simplifiedStates = data;	
		//console.log("Dans initListService, $scope.sraVariables.simplifiedStates :" , $scope.sraVariables.simplifiedStates);
	});	

	// Initialisation des etats :
	lists.refresh.states({objectTypeCode: "SRASubmission"});
	$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'miniSimplifiedStatesWithNone'}})
	.success(function(data) {
		$scope.sraVariables.miniSimplifiedStatesWithNone = data;	
	});	

	$http.get(jsRoutes.controllers.sra.api.Variables.get('pseudoStateCodeToStateCodes').url)
	.success(function(data) {
		$scope.sraVariables.pseudoStateCodeToStateCodes = data;
	});
	

	$http.get(jsRoutes.controllers.sra.api.Variables.get('pseudoStateCodeToStateCodes').url)
	.success(function(data) {
		$scope.sraVariables.pseudoStateCodeToStateCodes = data;
		//console.log("Dans initListService, avec get sans code, $scope.sraVariables.pseudoStateCodeToStateCodes" , $scope.sraVariables.pseudoStateCodeToStateCodes);
	});
	

	

	
	
}]);
