"use strict";

angular.module('home').controller('ValidationCtrl',[ '$http', '$scope', '$routeParams' , '$q', 'mainService', 'lists', 'tabService','messages','datatable',
	                                                  function($http, $scope, $routeParams, $q, mainService, lists, tabService, messages, datatable) { 


	var submissionsDTConfig = {
			pagination:{
				active:true,
				mode:'local',
                numberRecordsPerPage: 100
			},
			select:{active:true},
			showTotalNumberRecords:true,			
			order :{mode:'local', by:'code', reverse : true},
			search:{
				url:jsRoutes.controllers.sra.submissions.api.Submissions.list()
			},
			show:{
				active:true,
				add :function(line){
					tabService.addTabs({label:line.code,href:jsRoutes.controllers.sra.submissions.tpl.Submissions.get(line.code).url,remove:true});
				}
			},
			hide:{
				active:true
			},
			exportCSV:{
				active:false
			}, 
			name:"Submissions"
	};
	
	
	var getSubmissionColumns = function(){
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
			    	  	header: Messages("submissions.code"),
			    	  	type :"text",		    	  	
			    	  	order:true});
		columns.push({	property:"projectCodes",
    	  				header: Messages("submissions.projectCodes"),
    	  				type :"text",		    	  	
    	  				order:true});		
		columns.push({	property:"type",
						header: Messages("submissions.type"),
						type :"text",		    	  	
						order:true});	
		columns.push({
						property     : "firstSubmissionDate",
						header       : Messages("firstSubmissionDate"),
						type         : "date",	
						hide         : true,	    	  	
						order        : true,
						edit         : false,
						choiceInList : false  
						});		
		columns.push({	property:"accession",
			    	  	header: Messages("submissions.accession"),
			    	  	type :"text",		    	  	
			    	  	order:true});		
		columns.push({	property:"state.code",
						"filter":"codes:'state'",
						header: Messages("submissions.state"),
						type :"text",
						order:true});		
		return columns;
	};

	//--------------------------------------------------------------------------------------

	
	// initialisations :
	console.log("Dans submissions.validation-ctrl.js");

	$scope.messages = messages();
	$scope.messages.clear();
	$scope.form = {};  // important. 
	$scope.lists = lists; // service lists
	$scope.sraVariables = {};
	
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('validation');
		tabService.addTabs({label:Messages('submissions.menu.validation'),href:jsRoutes.controllers.sra.submissions.tpl.Submissions.home("validation").url,remove:true});
		tabService.activeTab(0); // active l'onglet en le mettant en bleu
	}

//	Initialisation datatable :
	$scope.submissionsDT = datatable(submissionsDTConfig);
	$scope.submissionsDT.setColumnsConfig(getSubmissionColumns());

	$scope.lists.refresh.projects();
	$scope.lists.refresh.states({objectTypeCode:"SRASubmission"});
	
	
	$scope.isValidation = true;
	
	$scope.form.stateCode = 'SUB-N';
	
	console.log("validation-ctrl:stateCode " + $scope.form.stateCode);

//------------------------------------------------------------------------------------------
	
	// Definitions methodes :
	//-----------------------
	
	// methode appelée depuis la vue avec la directive ng-init
	$scope.setUserInScope = function(user) {
		$scope.user = user;
		console.log("Dans setUserInScope, user= ", user);
	};

	$scope.reset = function() {
		$scope.form = {};
		$scope.submissionsDT = datatable(submissionsDTConfig);
		$scope.submissionsDT.setColumnsConfig(getSubmissionColumns());
		$scope.messages = messages(); 
		$scope.messages.clear();
	};
	

	// methode appelee pour remplir le tableau des submissions
	// Recherche toutes les submissions pour projCode indiqué :
	$scope.search = function() {
		console.log("dans consultation-ctrl.search : projCode " + $scope.form.projCode);	
		console.log("dans consultation-ctrl.search : state !!!!!'" + $scope.form.state+"'");
		$scope.form.stateCode="SUB-N";
		//$scope.form.createUser = $scope.user;
		$scope.submissionsDT.search($scope.form);
	};	

}]);
