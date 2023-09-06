"use strict";

angular.module('home').controller('DetailsCtrl',[ '$http', '$scope', '$routeParams' , '$q', 'mainService', 'lists', 'tabService','messages','datatable',
	function($http, $scope, $routeParams, $q, mainService, lists, tabService, messages, datatable) { 

    $scope.isRemovable = function() {
    	return $scope.submission != null;
    };

    
	var analyzesDTConfig = {
			name:' projectsDT',
			order :{by:'code',mode:'local', reverse:true},
			search:{
				url:jsRoutes.controllers.sra.analyzes.api.Analyzes.list()
			},
			pagination:{
				active:true,
				mode:'local',
                numberRecordsPerPage: 100
			},
			select:{active:false},
			showTotalNumberRecords:true,
			cancel : {
				showButton:true
			},
			hide:{
				active:false,
				showButton:false
			},
			exportCSV:{
				active:true
			},
			save : {
				active:true,
				showButton : false,
				changeClass : false,
				url:function(line){
					return jsRoutes.controllers.sra.analyzes.api.Analyzes.update(line.code).url; // jamais utilisé si mode local
				},
				method:'put',
				value:function(line){
					return line;
				},
				mode:'local',
			},
			edit : {
				active:true,
				showButton : false,
				withoutSelect : true,
				columnMode : true,
				lineMode : function(line){
					if((line.state.code === "NONE")||(line.state.code === "SUB-N"))
						return true;
					else 
						return false;
				}
			}
	};
	
	var getAnalysisColumns = function() {
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
						header   : Messages("analysis.state.code"),
						"filter" : "codes:'state'",
						type     : "text",		    	  	
						order    : true
						});	
			columns.push({
						property   : "code",
		  	        	header     : Messages("analysis.code"),
				       	type       : "text",		    	  	
				       	order      : true
				        });	
			columns.push({
						property     : "projectCodes",
				       	header       : Messages("analysis.projectCodes"),
				       	type         : "text",		    	  	
				       	order        : false,
				       	edit         : false,
				       	choiceInList : false  
				       	}); 			
			columns.push({
						property     : "accession",
					    header       : Messages("analysis.accession"),
				        type         : "text",		    	  	
				        order        : true,
				        edit         : false,
				        choiceInList : false  
				       	});	
			columns.push({
						property     : "studyAccession",
					    header       : Messages("analysis.studyAccession"),
				       	type         : "text",		    	  	
				       	order        : true,
				       	edit         : true,
				       	choiceInList : false  
				       	});				        	    	
		        			        
			columns.push({
						property     : "sampleAccession",
						header       : Messages("analysis.sampleAccession"),
						type         : "String",
						hide         : true,
						edit         : true,
						order        : false,
						choiceInList : false
						});	    
			columns.push({
						property     : "description",
						header       : Messages("analysis.description"),
						type :"text",	
						hide:true,
						order:true,
						edit:true,
						editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
						choiceInList:false
						});	
			columns.push({
						property     : "title",
						header       : Messages("analysis.title"),
						type :"text",	
						hide:true,
						order:true,
						edit:true,
						editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
						choiceInList:false
						});	
			columns.push({
						property     : "comments",
						header       : Messages("comments"),
						type         : "text",
						hide         : true,
						edit         : false, // pas modifiable ici car uniquement dans vue details du study en passant par icone speciale
						order        : false,
						choiceInList : false,
						filter       : "collect:'comment'",
						listStyle    : 'bt-select',
						});	
			return columns;
		};
      
	var getUmbrellaProjectColumns = function() {
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
		  	        	header     : Messages("project.code"),
				       	type       : "text",		    	  	
				       	order      : true
				        });	

			columns.push({
						property     : "childrenProjectAccessions",
				       	header       : Messages("project.childrenProjectAccessions"),
				       	type         : "text",		    	  	
				       	order        : false,
				       	edit         : false,
				       	choiceInList : false  
				       	}); 	
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
			columns.push({
						property     : "comments",
						header       : Messages("comments"),
						type         : "text",
						hide         : true,
						edit         : false, // pas modifiable ici car uniquement dans vue details du study en passant par icone speciale
						order        : false,
						choiceInList : false,
						filter       : "collect:'comment'",
						listStyle    : 'bt-select',
						});	
			return columns;
  
		};

		var umbrellaProjectsDTConfig = {
			name:' projectsDT',
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
				active : false, // permettre edition des champs editables
				showButton : true,// bouton d'edition visible
				withoutSelect : true,
				columnMode : true
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
			edit : {
				active:false,
				showButton : false,
				withoutSelect : true,
				columnMode : true,
			},
			save : {
				active:false,
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
						property     : "releaseDate",
					   	header       : Messages("study.releaseDate"),
					    type         : "date",		    	  	
					    order        : false,
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
						header       : Messages("study.description"),
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
						edit         : false, // study editable uniquement si SUB-N et à ce stade, project.xml n'existe pas et c'est dans ce fichier que ce champs apparait.
						                      // De plus manip a faire si on voulait rendre ce champs editable. Il faudrait transformer ce champs en tableau 
                                              // avant de le reinjecter dans study (voir interface update-ctrl.js)
						order        : false,
						choiceInList : false,
						listStyle    : 'bt-select',
						});
								
			columns.push({
						property     : "idsPubmed",
						header       : Messages("study.idsPubmed"),
						type         : "text",
						hide         : true,
						edit         : false, // study editable uniquement si SUB-N et à ce stade, project.xml n'existe pas et c'est dans ce fichier que ce champs apparait.
						                      // De plus manip a faire si on voulait rendre ce champs editable. Il faudrait transformer ce champs en tableau 
                                              // avant de le reinjecter dans study (voir interface update-ctrl.js)
						order        : false,
						choiceInList : false,
						listStyle    : 'bt-select',
						});
			columns.push({
						property     : "comments",
						header       : Messages("comments"),
						type         : "text",
						hide         : true,
						edit         : false, // pas modifiable ici car uniquement dans vue details du study en passant par icone speciale
						order        : false,
						choiceInList : false,
						filter       : "collect:'comment'",
						listStyle    : 'bt-select',
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
			select:{active:false},
			showTotalNumberRecords:true,
			cancel : {
				showButton:true
			},
			hide:{
				active:false,
				showButton:false
			},
			exportCSV:{
				active:true
			},
			edit : {
				active:true,
				showButton : false,
				withoutSelect : true,
				columnMode : true,
				lineMode : function(line){
					if((line.state.code === "NONE")||(line.state.code === "SUB-N"))
						return true;
					else 
						return false;
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



	var samplesDTConfig = {
			name:'sampleDT',
			//order :{by:'code',mode:'local', reverse:true},
			order :{by:'code',mode:'local', reverse:true},
			search:{active:false},
			select:{active:false},
			pagination:{
				active:false,
				mode:'local'
			},
			showTotalNumberRecords:true,
			edit : {
				active:true,
				showButton : false,
				withoutSelect : true,
				columnMode : true,
				lineMode : function(line){
					if(line.state.code === "SUB-N")
						return true;
					else 
						return false;
				}
			},
			save : {
				active:true,
				// important de mettre en mode local pour rafraichissement de la page, mais sauvegarde globale via bouton
				mode:'local',
				showButton : false,
				changeClass : false,				
				url:function(line){
					return jsRoutes.controllers.sra.samples.api.Samples.update(line.code).url; // jamais utilisé en mode local
				},
				method:'put',
				value:function(line){
					return line;
				},
			},

			/*cancel : {
				showButton:true
			},
			hide:{
				active:true
			},
			 */
			exportCSV:{
				active:true
			},
			

			columns : [
			 	{property:"traceInformation.creationDate",
			       	header: Messages("sample.traceInformation.creationDate"),
			       	type :"date",		    	  	
			       	order:true
			    },
			    {property:"traceInformation.createUser",
			       	header: Messages("traceInformation.creationUser"),
			       	type :"text",		    	  	
			       	order:true
			    },
				{property:"state.code",
					"filter":"codes:'state'", 
					header: Messages("state"),
					type :"text",		    	  	
					order:true,
					edit:false
				},
				{property:"code",
					header: Messages("sample.code"),
					type :"text",		    	  	
					order:true
				},
				{property:"accession",
					header: Messages("sample.accession"),
					type :"text",
					edit :false,
					order:true
				},
				{property:"externalId",
					header: Messages("sample.externalId"),
					type :"text",	
					edit :false,
					order:true
				},				
				{property:"projectCode",
					header: Messages("sample.projectCode"),
					type :"text",		    	  	
					order:true,
					edit :false           
				},
				{property:"anonymizedName",
					header: Messages("sample.anonymizedName"),
					type :"text",	
					hide:true,
					order:true,
					edit:true,
					editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
					choiceInList:false
				},
				{property:"title",
					header: Messages("sample.title"),
					type :"text",	
					hide:true,
					order:true,
					edit:true,
					editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
					choiceInList:false
				},
				{property:"description",
					header: Messages("sample.description"),
					type :"text",	
					hide:true,
					order:true,
					edit:true,
					editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
					choiceInList:false
				},				
				{property:"taxonId",
					header: Messages("sample.taxonId"),
					type :"int",
					hide:true,
					order:true,
					edit:false,
					choiceInList:false
				},
				/*{property:"commonName",
					header: Messages("sample.commonName"),
					type :"text",		    	  	
					hide:true,
					order:true,
					edit:false,
					choiceInList:false
				},*/
				{property:"scientificName",
					header: Messages("sample.scientificName"),
					type :"text",		    	  	
					order:true,
					edit:false,
					choiceInList:false
				},
				{property:"attributes",
					header: Messages("sample.attributes"),
					//"filter":"codes:'state'",
					type :"text",		    	  	
					order:true,
					edit:true,
					editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
					choiceInList:false
				},
			    {property     : "comments",
					header       : Messages("comments"),
					type         : "text",
					hide         : true,
					edit         : false, // pas modifiable en consultation car uniquement dans vue detail
					order        : false,
				    choiceInList : false,
					filter       : "collect:'comment'",
					listStyle    : 'bt-select',
				}
				]				
	};

	var experimentsDTConfig = {
			name:'experimentDT',
			order :{by:'code',mode:'local', reverse:true},
			search:{active:false},
			select:{active:false},
			pagination:{
				active:false,
				mode:'local'
			},
			showTotalNumberRecords:true,
			edit:{
				active : true,
				showButton : false,
				withoutSelect : true,
				columnMode : true,
				lineMode:function(line){
					if(line.state.code === "SUB-N"){
						return true;
					}else {
						return false;
					}
				},
			},
			save : {
				active:true,
				mode:'local',
				showButton : false,
				changeClass : false,
				url:function(lineValue){
					return jsRoutes.controllers.sra.experiments.api.Experiments.update(lineValue.code).url; // jamais utilisé en mode local
				},
				method:'put',
				value:function(line){
					return line;
				},
			},
			/*cancel : {
				showButton:true
			},
			hide:{
				active:true,
				showButton:true
			},*/
			exportCSV:{
				active:true
			},
			columns : [
				{property:"traceInformation.creationDate",
			        header: Messages("experiment.traceInformation.creationDate"),
			       	type :"date",		    	  	
			       	order:true
			    },
			    {property:"traceInformation.createUser",
			       	header: Messages("traceInformation.creationUser"),
			       	type :"text",		    	  	
			       	order:true
			    },
				{property:"state.code",
					"filter":"codes:'state'", 
					header: Messages("state"),
					type :"text",		    	  	
					order:true,
					edit:false
				},
				{property:"code",
					header: Messages("experiment.code"),
					type :"text",		    	  	
					order:true
				},
				{property:"accession",
					header: Messages("experiment.accession"),
					type :"text",		    	  	
					order:true
				},	
				{property:"projectCode",
					header: Messages("experiment.projectCode"),
					type :"text",		    	  	
					order:true,
					edit:false,
					choiceInList:false  
				},
				{property:"title",
					header: Messages("experiment.title"),
					type :"String",		    	  	
					hide:true,
					edit:true,
					editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
					order:true
				},
				{property:"librarySelection",
					header: Messages("experiment.librarySelection"),
					type :"String",
					hide:true,
					edit:true,
					order:true,
					choiceInList:true,
					listStyle:'bt-select',
					possibleValues:'sraVariables.librarySelection',
						
					
				},
				{property:"libraryStrategy",
					header: Messages("experiment.libraryStrategy"),
					type :"String",
					hide:true,
					edit:true,
					order:true,
					choiceInList:true,
					listStyle:'bt-select',
					possibleValues:'sraVariables.libraryStrategy',
				},
				{property:"librarySource",
					header: Messages("experiment.librarySource"),
					type :"String",
					hide:true,
					edit:true,
					order:true,
					choiceInList:true,
					listStyle:'bt-select',
					possibleValues:'sraVariables.librarySource',
				},
				{property:"libraryLayout",
					header: Messages("experiment.libraryLayout"),
					type :"String",
					hide:true,
					edit:false,
					order:true,
					choiceInList:true,
					listStyle:'bt-select',
					possibleValues:'sraVariables.libraryLayout',
				
				},	
				{property:"libraryLayoutNominalLength",
					header: Messages("experiment.libraryLayoutNominalLength"),
					type :"integer",		    	  	
					hide:true,
					edit:true,
					order:true
				},	
				{property:"libraryLayoutOrientation",
					header: Messages("experiment.libraryLayoutOrientation"),
					type :"String",
					hide:true,
//					edit:false,
//					order:true,
//					choiceInList:true,
//					listStyle:'bt-select',
//					possibleValues:'sraVariables.libraryLayoutOrientation'
				},	
				{property:"libraryName",
					header: Messages("experiment.libraryName"),
					type :"String",		    	  	
					hide:true,
					edit:false,
					editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
					order:true
				},
				{property:"libraryConstructionProtocol",
					header: Messages("experiment.libraryConstructionProtocol"),
					type :"String",		    	  	
					hide:true,
					edit:true,
					editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
					order:true
				},

				
				{property:"typePlatform",
					header: Messages("experiment.typePlatform"),
					type :"String",		    	  	
					hide:true,
					edit:false,
					order:true
				},
				{property:"instrumentModel",
					header: Messages("experiment.instrumentModel"),
					type :"String",		    	  	
					hide:true,
					edit:false,
					order:true
				},
				{property:"lastBaseCoord",
					header: Messages("experiment.lastBaseCoord"),
					type :"integer",		    	  	
					hide:true,
					edit:true,
					order:true
				},	
				{property:"spotLength",
					header: Messages("experiment.spotLength"),
					type :"Long",		    	  	
					hide:true,
					edit:true,
					order:true
				},	
				{property:"sampleCode",
					header: Messages("experiment.sampleCode"),
					type :"String",		    	  	
					hide:true,
					edit:false,
					order:true
				},	
				{property:"studyCode",
					header: Messages("experiment.studyCode"),
					type :"String",		    	  	
					hide:true,
					edit:false,
					order:true
				},
				{property     : "comments",
					header       : Messages("comments"),
					type         : "text",
					hide         : true,
					edit         : false, // pas modifiable en consultation car uniquement dans vue detail
					order        : false,
				    choiceInList : false,
					filter       : "collect:'comment'",
					listStyle    : 'bt-select',
				}
				
				]	        
	};

	var runsDTConfig = {
			name:'runDT',
			order :{by:'code',mode:'local', reverse:true},
			search:{active:false},
			select:{active:false},
			pagination:{
				active:false,
				mode:'local'
			},
			showTotalNumberRecords:true,
			
			edit : {
				active:true,
				showButton : false,
				withoutSelect : true,
				columnMode : true
			},
			exportCSV:{
				active:true
			},
			columns : [
				{property:"run.code",
					header: Messages("run.code"),
					type :"text",		    	  	
					order:true
				},
				{property:"run.accession",
					header: Messages("run.accession"),
					type :"text",		    	  	
					order:true
				},
				{property:"code",
					header: Messages("run.experiment.code"),
					type :"text",		    	  	
					order:true
				},
				{property:"run.runDate",
					header: Messages("run.date"),
					type :"date",
					order:true
				}

				]	        
	};
	
	
	var rawDatasDTConfig = {
			name:'rawDataDT',
			order :{by:'code', mode:'local', reverse:true},
			search:{active:false},
			select:{active:false},
			pagination:{
				active:false,
				mode:'local'
			},
			showTotalNumberRecords:true,
			edit : {
				active:true,
				showButton :false,
				withoutSelect : true,
				columnMode : true
			},
			cancel : {
				showButton:true
			},
			hide:{
				active:true
			},
			exportCSV:{
				active:true
			},
			//url:function(lineValue){
			//		return jsRoutes.controllers.sra.experiments.api.Experiments.update(lineValue.code).url; // jamais utilisé en mode local
			//	},
			
//			remove : {
//				active:false,
//				withEdit:false, //to authorize to remove a line in edition mode
//				showButton : false,
//				mode:'local', //or local
//				url:function(lineValue) { 
//					return jsRoutes.controllers.sra.experiments.api.ExperimentsRawDatas.delete(lineValue.experimentCode, lineValue.relatifName).url;
//				},
//				callback : undefined, //used to have a callback after remove all element. the datatable is pass to callback method and number of error
//				start:false,
//				counter:0,
//				number:0, //number of element in progress
//				error:0								
//			},
			
			columns : [
				{property:"relatifName",
					header: Messages("rawData.relatifName"),
					type :"text",		    	  	
					order:true
				},
				{property:"collabFileName",
					header: Messages("rawData.collabFileName"),
					type :"text",		    	  	
					order:true
				}
				]	        
	};	


//----------------------------------------------------------------------------------------
	
	// Definition des methodes :
	//---------------------------
	
	var init = function(){
		$scope.alertUser = "";
		$scope.messages = messages();
		$scope.messages.clear();
		$scope.mainService = mainService;
		$scope.mainService.stopEditMode();
		$scope.sampleCheck=false;
		$scope.experimentCheck=false;
		$scope.analysisCheck=false;
		$scope.studyCheck=false;               // study validé utilisateur
		$scope.runCheck=false;
		$scope.rawDataCheck=false;
		
		$scope.isAnalysisCheckable    = false;  
		$scope.isStudyCheckable       = false;  // le study est-il validable (nouvelle soumission et study à soumettre)
		$scope.isSampleCheckable      = false;
		$scope.isExperimentCheckable  = false;
		$scope.isRunCheckable         = false;
		$scope.isRawDataCheckable     = false;
		$scope.allValidable           = false;

		$scope.isAllValidable = function() {
			//console.log("Dans isALLValidable = ");
			if ( (($scope.isStudyCheckable && $scope.studyCheck)   || !$scope.studyCheckable) &&
				 (($scope.isSampleCheckable && $scope.sampleCheck) || !$scope.sampleCheckable) &&
				 (($scope.isExperimentCheckable && $scope.experimentCheck) || !$scope.experimentCheckable) &&
				 (($scope.isRunCheckable && $scope.runCheck) || !$scope.runCheckable) &&
				 (($scope.isRawDataCheckable && $scope.rawDataCheck) || !$scope.rawDataCheckable) &&
			     (($scope.isAnalysisCheckable && $scope.AnalysisCheck || !$scope.AnalysisCheckable))) {
				$scope.allValidable = true;
			} 
			//console.log("Dans isALLValidable = " + $scope.allValidable);
			return $scope.allValidable;
		}; 
		
		//console.log("!!!! dans init(),  scope.isStudyCheckable=" + $scope.isStudyCheckable);
		// Attention $scope.submission.experimentCodes et $scope.submission.sampleCodes sont affectes par 
		// l'appel à  splice dans l'init d'ou declaration des variables count pour connaitre 
		// nombre de données et non pas utilisation de submission.experimentCodes.lengh corrompu par splice
		$scope.countExperiment = 0;
		$scope.countSample = 0;
		$scope.countAnalysis = 0;
		$scope.countStudy =  0;
		$scope.countRun = 0;
		$scope.countRawData = 0;
		$scope.countUmbrellaProject = 0;

		// Attention appel de get du controller api.sra.submissions qui est herite
		// $routeParams.code contient le code qui apparait dans l'url de consultation donc le code de la submission
		$http.get(jsRoutes.controllers.sra.submissions.api.Submissions.get($routeParams.code).url).success(function(data) {
			$scope.submission = data;	
		
			//console.log("submission=", $scope.submission);
			
			// Ajout des onglets à gauche si rafraichissement page ok
			if(tabService.getTabs().length == 0){			
				//tabService.addTabs({label:Messages('submissions.menu.consultation'),href:jsRoutes.controllers.sra.submissions.tpl.Submissions.home("consultation").url,remove:true});
				tabService.addTabs({label:$scope.submission.code, href:jsRoutes.controllers.sra.submissions.tpl.Submissions.get($scope.submission.code).url,remove:true});
				tabService.activeTab($scope.getTabs(0)); // active l'onglet indiqué, le met en bleu.
			}
			
//			console.log("init::$routeParams.code:"+$routeParams.code);
//			console.log("init::Submission.code :"+$scope.submission.code);
//			console.log("init::Submission.refSampleCodes :"+$scope.submission.refSampleCodes);
//			console.log("init::Submission.sampleCodes :"+$scope.submission.sampleCodes);
//			
//			console.log("init::Submission.experimentCodes :"+$scope.submission.experimentCodes);
//			console.log("init::Submission.runCodes :"+$scope.submission.runCodes);
			if ($scope.submission.umbrellaCode != null && $scope.submission.umbrellaCode.trim().length != 0) {
				$scope.countUmbrellaProject =  1;
				//console.log("$scope.submission.umbrellaCode", $scope.submission.umbrellaCode);
				
			}
			
			// equivalent StringUtils.isNotBlanck($scope.submission.studyCode :
			if ($scope.submission.studyCode != null && $scope.submission.studyCode.trim().length != 0) {
				$scope.countStudy =  1;
			}
			
			if ($scope.submission.analysisCode != null && $scope.submission.analysisCode.trim().length != 0) {
				$scope.countAnalysis =  1;
			}			
			$scope.countExperiment = $scope.submission.experimentCodes.length;
			$scope.countRun = $scope.submission.runCodes.length;
			$scope.countSample = $scope.submission.sampleCodes.length;

//			console.log("init::$scope.countExperiment = " + $scope.submission.experimentCodes.length);
//			console.log("init::$scope.countSample = " + $scope.submission.sampleCodes.length);
//			console.log("init::$scope.countStudy = " + $scope.countStudy);
			$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'existingStudyType'}})
			.success(function(data) {
				// initialisation de la variable sraVariables.existingStudyType utilisee dans experimentsDTConfig
				$scope.sraVariables.existingStudyType = data;
			});	
			$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'librarySelection'}})
			.success(function(data) {
				// initialisation de la variable sraVariables.librarySelection utilisee dans experimentsDTConfig
				$scope.sraVariables.librarySelection = data;
			});	
			$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'libraryStrategy'}})
			.success(function(data) {
				// initialisation de la variable sraVariables.libraryStrategy utilisee dans experimentsDTConfig
				$scope.sraVariables.libraryStrategy = data;
			});
			$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'librarySource'}})
			.success(function(data) {
				// initialisation de la variable sraVariables.librarySource utilisee dans experimentsDTConfig
				$scope.sraVariables.librarySource = data;
			});
			$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'libraryLayout'}})
			.success(function(data) {
				// initialisation de la variable sraVariables.libraryLayout utilisee dans experimentsDTConfig
				$scope.sraVariables.libraryLayout = data;
			});
//			$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'libraryLayoutOrientation'}})
//			.success(function(data) {
//				// initialisation de la variable sraVariables.libraryLayoutOrientation utilisee dans experimentsDTConfig
//				$scope.sraVariables.libraryLayoutOrientation = data;
//			});	
			if($scope.submission.umbrellaCode != null && $scope.submission.umbrellaCode.trim().length != 0){
				$http.get(jsRoutes.controllers.sra.projects.api.Projects.get($scope.submission.umbrellaCode).url).success(function(data)
				{
					$scope.umbrellaProjects = [];
					$scope.umbrellaProjects.push(data);
					//console.log("$scope.umbrellaProjects :" , $scope.umbrellaProjects);					
					//Init datatable
					$scope.umbrellaProjectDT = datatable(umbrellaProjectsDTConfig);
					$scope.umbrellaProjectDT.setColumnsConfig(getUmbrellaProjectColumns());
					$scope.umbrellaProjectDT.setData($scope.umbrellaProjects, $scope.umbrellaProjects.length);
				});
			}			
			if($scope.submission.studyCode != null && $scope.submission.studyCode.trim().length != 0){
				$http.get(jsRoutes.controllers.sra.studies.api.Studies.get($scope.submission.studyCode).url).success(function(data)
				{
					$scope.studies = [];
					$scope.studies.push(data);
//					console.log("$scope.submission.study: " + $scope.submission.studyCode);
//					console.log("$scope.studies :" + $scope.studies);
					
					//Init datatable
//					$scope.studyDT = datatable(studiesDTConfig);
//					$scope.studyDT.setData($scope.studies, $scope.studies.length);
					$scope.studyDT = datatable(studiesDTConfig);
					$scope.studyDT.setColumnsConfig(getStudyColumns());
					$scope.studyDT.setData($scope.studies, $scope.studies.length);
					//if ( $scope.submission.state.code =='SUB-N' && $scope.submission.traceInformation.createUser === $scope.user) {
					if ( $scope.submission.state.code =='SUB-N') {
						$scope.isAllValidable();
						$scope.isStudyCheckable = true;
						//console.log("ok cond 1");
						//console.log("!!!!  scope.isStudyCheckable= " + $scope.isStudyCheckable);
						//console.log("!!!!! scope.isAllValidable = " + $scope.isAllValidable());
					}
				});
			} else {
				$scope.studyCheck = true;
			}
			if($scope.submission.analysisCode != null && $scope.submission.analysisCode.trim().length != 0){
				$http.get(jsRoutes.controllers.sra.analyzes.api.Analyzes.get($scope.submission.analysisCode).url).success(function(data)
				{
					$scope.analyzes = [];
					$scope.analyzes.push(data);
				
					//Init datatable

					$scope.analysisDT = datatable(analyzesDTConfig);
					$scope.analysisDT.setColumnsConfig(getAnalysisColumns());
					$scope.analysisDT.setData($scope.analyzes, $scope.analyzes.length);
					// Get RawDatas : construction de la liste des rawData puis injection dans datatable :
					var maListRawDatas = [];
					for (var i=0; i<$scope.analyzes.length; i++) {
						var analysis = $scope.analyzes[i];
						for (var j=0; j<analysis.listRawData.length; j++) {
							var rawdata = analysis.listRawData[j];
							maListRawDatas.push(rawdata);
						}
					}
					$scope.rawDataDT = datatable(rawDatasDTConfig);
					$scope.rawDataDT.setData(maListRawDatas, maListRawDatas.length);
					$scope.countRawData = maListRawDatas.length; 
					//if ( $scope.submission.state.code =='SUB-N' && $scope.submission.traceInformation.createUser === $scope.user) {
					if ( $scope.submission.state.code =='SUB-N') {
						$scope.isAnalysisCheckable = true;
						$scope.isRawDataCheckable = true;
						$scope.isAllValidable();
					}
				});
			} else {
				$scope.analysisCheck = true;
				if($scope.submission.experimentCodes== null|| $scope.submission.experimentCodes.length==0){
					$scope.rawDataCheck = true;
				}
			}			


			if($scope.submission.sampleCodes.length>0){
				var nbElementByBatch = Math.ceil($scope.submission.sampleCodes.length / 6); //6 because 6 request max in parrallel with firefox and chrome
				var queries = [];
				for (var i = 0; i < 6 && $scope.submission.sampleCodes.length > 0; i++) {
					var subSampleCodes = $scope.submission.sampleCodes.splice(0, nbElementByBatch);
					//queries.push( $http.get(jsRoutes.controllers.sra.samples.api.Samples.list().url, {params: {listSampleCodes:subSampleCodes}}) );
					queries.push( $http.get(jsRoutes.controllers.sra.samples.api.Samples.list().url, {params: {codes:subSampleCodes}}) );
				}
				$q.all(queries).then(function(results) {
					var allData = [];
					results.forEach(function(result){
						allData = allData.concat(result.data);
					});

					$scope.samples = allData;

//					console.log("scope.submission.sampleCodes: " + $scope.submission.sampleCodes);
//					console.log("!!!!!!! scope.samples :" + $scope.samples);

					//Init datatable
					$scope.sampleDT = datatable(samplesDTConfig);
					$scope.sampleDT.setData($scope.samples, $scope.samples.length);
					//if ($scope.submission.state.code == 'SUB-N' && $scope.submission.traceInformation.createUser === $scope.user) {
					if ($scope.submission.state.code == 'SUB-N') {
						//console.log("ok cond 2");
						$scope.isSampleCheckable = true;
					}
				});
			} else {
				$scope.sampleCheck = true;
			}


			if($scope.submission.experimentCodes.length>0){
				var nbElementByBatch = Math.ceil($scope.submission.experimentCodes.length / 6); //6 because 6 request max in parrallel with firefox and chrome
				var queries = [];
				for (var i = 0; i < 6 && $scope.submission.experimentCodes.length > 0; i++) {
					var subExperimentCodes = $scope.submission.experimentCodes.splice(0, nbElementByBatch);
					//queries.push( $http.get(jsRoutes.controllers.sra.experiments.api.Experiments.list().url, { params : { listExperimentCodes : subExperimentCodes } }) );
					queries.push( $http.get(jsRoutes.controllers.sra.experiments.api.Experiments.list().url, { params : { codes : subExperimentCodes } }) );
				}
				$q.all(queries).then(function(results) {
					var allData = [];
					results.forEach(function(result){
						allData = allData.concat(result.data);
					});
                    //console.log("XXXXXXXXXXXXXXXXXXXXXx");
                    
					$scope.experiments = allData;
					//Init datatable
					$scope.experimentDT = datatable(experimentsDTConfig);
					$scope.experimentDT.setData($scope.experiments, $scope.experiments.length);
					if($scope.submission.runCodes.length > 0 ) {
						//Get Runs
						$scope.runDT = datatable(runsDTConfig);
						// Comme on a un seul run par experiment, on n'a pas besoin de boucler pour recuperer les données :
						$scope.runDT.setData($scope.experiments, $scope.experiments.length);


						// Get RawDatas : construction de la liste des rawData puis injection dans datatable :
						var maListRawDatas = [];
						for (var i=0; i<$scope.experiments.length; i++) {
							var experiment = $scope.experiments[i];
							var run = experiment.run;
							for (var j=0; j<run.listRawData.length; j++) {
								var rawdataPlus = run.listRawData[j];
								//console.log("rawDataPlus = " + rawdataPlus.relatifName);
								rawdataPlus.experimentCode = experiment.code;
								//console.log("experimentCode = " +rawdataPlus.experimentCode);
								//maListRawDatas.push(run.listRawData[j]);
								maListRawDatas.push(rawdataPlus);
							}
						}
						// Partie de la configuration (rawDatasDTConfig) à definir seulement
						// lorsque $scope.submission est definit.
						$scope.rawDataDT = datatable(rawDatasDTConfig);
						//console.log("sortie de rawData " + $scope.submission.state.code);
						$scope.rawDataDT.setData(maListRawDatas, maListRawDatas.length);
						$scope.countRawData = maListRawDatas.length;
						//if ($scope.submission.state.code == 'SUB-N' && $scope.submission.traceInformation.createUser === $scope.user) {
						if ($scope.submission.state.code == 'SUB-N') {
							//console.log("ok cond 3");
							$scope.isExperimentCheckable = true;
							$scope.isRunCheckable        = true;
							$scope.isRawDataCheckable    = true;
						}
						//console.log("scope.isExperimentCheckable= "   + $scope.isExperimentCheckable );
					} 
				});	
			} else {
				$scope.experimentCheck = true;
				$scope.runCheck = true;
				if ($scope.submission.analysisCode == null || $scope.submission.analysisCode.trim().length == 0){
					$scope.rawDataCheck = true;
				}
			}			
		});

	};

	
	function closeSubmission(tableau_analyzes, tableau_studies, tableau_samples, tableau_experiments){
		// $scope.submission.state.code = "SUB-V";		
		//$http.put(jsRoutes.controllers.sra.submissions.api.Submissions.update($scope.submission.code).url, $scope.submission)
		var state = angular.copy($scope.submission.state);
		state.code = "SUB-V";
		$http.put(jsRoutes.controllers.sra.submissions.api.Submissions.updateState($scope.submission.code).url, state)
		.success(function(data) {
			//Set success message
			$scope.messages.clazz="alert alert-success";
			$scope.messages.text=Messages('submissions.msg.validate.success');
			$scope.messages.open();
	        $scope.treeLoadInProgress = false;
	        $scope.submission.state.code="SUB-V";
	        // rafraichissement des tableaux de la vue avec bons statuts: 
	        if ((tableau_analyzes!=='undefined') && (tableau_analyzes.length > 0)) {
	        	for (var i=0; i<tableau_analyzes.length; i++) {
	        		tableau_analyzes[i].state.code=$scope.submission.state.code;
	        	}
	        	$scope.analysisDT.setData(tableau_analyzes, tableau_analyzes.length);
	        	$scope.analysisDT.save(); // fait le save cote client mais n'utilise pas url et ne fait pas save dans database.
	        }		
	        if ((tableau_studies!=='undefined') && (tableau_studies.length > 0)) {
	        	for (var i=0; i<tableau_studies.length; i++) {
	        		tableau_studies[i].state.code=$scope.submission.state.code;
	        	}
	        	$scope.studyDT.setData(tableau_studies, tableau_studies.length);
	        	$scope.studyDT.save(); // fait le save cote client mais n'utilise pas url et ne fait pas save dans database.
	        }
	        if ((tableau_samples!=='undefined') && (tableau_samples.length > 0)) {
	        	for (var i=0; i<tableau_samples.length; i++) {
	        		tableau_samples[i].state.code=$scope.submission.state.code;
	        	}
	        	$scope.sampleDT.setData(tableau_samples, tableau_samples.length);
	        	$scope.sampleDT.save(); // fait le save cote client mais n'utilise pas url et ne fait pas save dans database.
	        }
	        if ((tableau_experiments!=='undefined') && (tableau_experiments.length > 0)) {
	        	// initialisation inutile $scope.experimentDT = datatable(experimentsDTConfig);
	        	for (var i=0; i<tableau_experiments.length; i++) {
	        		tableau_experiments[i].state.code=$scope.submission.state.code;
	        	}
	        	$scope.experimentDT.setData(tableau_experiments, tableau_experiments.length);
	        	// sauvegarde cote client des experiments avec bon statut :
	        	$scope.experimentDT.save(); // fait le save cote client mais n'utilise pas url et ne fait pas save dans database.
	        }

		}).error(function(data){
			$scope.messages.addDetails(data);
			$scope.messages.setError("save");
	        $scope.treeLoadInProgress = false;
	        
		});
	};

	//en Javascript, ce n'est pas vous qui choisissez le mode de passage ; 
	//ça fonctionne un peu comme en Java[1] : 
	//si c'est un type natif (Number, String, etc.) c'est passé par valeur, 
	//tandis que si c'est un objet d'une classe à vous, c'est passé par référence.
	// => d'ou l'importance du return ici
	function processInSubmission(decompte, action, error, tableau_analyzes, tableau_studies, tableau_samples, tableau_experiments) { // pas d'indication de retour dans la signature.
//		console.log("processInSubmission ::decompte "+ decompte);
//		console.log("processInSubmission ::error "+ error);
		
		decompte = decompte - 1;
		if (decompte === 0) {
			if (error){
				// afficher message d'erreur sans sauver la soumission.
				$scope.messages.setError("save");
				//console.log("TUTU       Erreur avant sauvegarde de la soumission");
				$scope.treeLoadInProgress = false;
				//throw("TUTU       Erreur avant sauvegarde de la soumission");
			} else {
				if ( action === "VALIDATE") {
					// sauver la soumission dans base et afficher resultat de la requete 
					closeSubmission(tableau_analyzes, tableau_studies, tableau_samples, tableau_experiments);
				} else {
					$scope.messages.clazz="alert alert-success";
					$scope.messages.text=Messages('submissions.msg.save.success');
					$scope.messages.open();
			        $scope.treeLoadInProgress = false;
				}
			}
		}
		return decompte;
	};

	
	/* buttons section */
	


	$scope.userValidate = function() {
		$scope.userRegisterValidate("VALIDATE");
	};

	$scope.userRegister = function() {
		$scope.userRegisterValidate("NO_VALIDATE");
	};
	
	
	$scope.userRegisterValidate = function(action){
		$scope.messages.clear();
//		console.log("Dans userValidate");
//		
//		console.log("userValidate::Submission.code :"+$scope.submission.code);
//		console.log("userValidate::Submission.refSampleCodes :"+$scope.submission.refSampleCodes);
//		console.log("userValidate::Submission.sampleCodes :"+$scope.submission.sampleCodes);
//		console.log("userValidate::Submission.experimentCodes :"+ $scope.submission.experimentCodes);
//		console.log("userValidate::Submission.runCodes :"+$scope.submission.runCodes);
//		console.log("userValidate::Nombre de samples: " + $scope.countSample);
//		console.log("userValidate::Nombre d'experiment: " + $scope.countExperiment);
//		console.log("userValidate::Nombre de study " + $scope.countStudy);
		
        $scope.treeLoadInProgress = true;

		
		var error = false;
		
		if( $scope.countAnalysis != 0){
			$scope.analysisDT.save();	// sauvegarde dans client des analyses avec valeurs editees (valeurs utilisateurs)	
			tableau_analyzes = $scope.analysisDT.getData();
		} else {
			//console.log("userValidate:: $scope.countAnalysis == 0");
		}
		// Recuperation des studies :
		if( $scope.countStudy != 0) {
			//console.log("userValidate:: $scope.countStudy != 0");
			$scope.studyDT.save();	// sauvegarde dans client des studies avec valeurs editees (valeurs utilisateurs)	
			tableau_studies = $scope.studyDT.getData();
		} else {
			//console.log("userValidate:: $scope.countStudy == 0");
		}

		// Recuperation des samples :
		if($scope.countSample != 0) {
			//console.log("samples à sauver");
			$scope.sampleDT.save();	// sauvegarde dans client des samples avec valeurs editees (valeurs utilisateurs)	
			tableau_samples = $scope.sampleDT.getData();
		} else {
			//console.log("Aucun sample à sauver");
		}
		// Recuperation des experiments :	
		if($scope.countExperiment) {
			//console.log("experiments à sauver");
			$scope.experimentDT.save();		// recuperation saisie utilisateur et sauvegarde dans client.
			tableau_experiments = $scope.experimentDT.getData();
		} else {
			//console.log("Aucun experiment à sauver "+ $scope.submission.experimentCodes.length);
		}
		var decompte = tableau_samples.length +  tableau_experiments.length + tableau_studies.length + tableau_analyzes.length;
		//console.log("decompte = " + decompte);
		
		// Mise à jour du status  des analyses :
		for(var i = 0; i < tableau_analyzes.length ; i++){
			// sauvegarde dans database asynchrone
			$http.put(jsRoutes.controllers.sra.analyzes.api.Analyzes.update(tableau_analyzes[i].code).url, tableau_analyzes[i])
			.success(function(data){
				decompte = processInSubmission(decompte, action, error, tableau_analyzes, tableau_studies, tableau_samples, tableau_experiments);
				
			}).error(function(data){
				$scope.messages.addDetails(data);
				//$scope.messages.setError("save");
				error = true;
				decompte = processInSubmission(decompte, action, error, tableau_analyzes, tableau_studies, tableau_samples, tableau_experiments);
		        $scope.treeLoadInProgress = false;
			});			
			//console.log("studyTitle = " + tableau_studies[i].title + " state = "+ tableau_studies[i].state.code);
		}
		if($scope.submission.analysisCode !=null){
			$scope.analysisDT.setData(tableau_analyzes, tableau_analyzes.length);
			// sauvegarde cote client des analyses avec bon statut
			$scope.analysisDT.save(); // fait le save cote client mais n'utilise pas url et ne fait pas save dans database.
		}		
		 
		//Mise à jour du status  des studies :
		for(var i = 0; i < tableau_studies.length ; i++){
			//console.log("studyCode = " + tableau_studies[i].code + " state = "+ tableau_studies[i].state.code);
			//tableau_studies[i].state.code = "SUB-V";
			//console.log("studyTitle = " + tableau_studies[i].title + " state = "+ tableau_studies[i].state.code);
			//console.log("studyCode = " + tableau_studies[i].code + " state = "+ tableau_studies[i].state.code);
			// sauvegarde dans database asynchrone
			$http.put(jsRoutes.controllers.sra.studies.api.Studies.update(tableau_studies[i].code).url, tableau_studies[i])
			.success(function(data){
				//Set success message
				//$scope.messages.clazz="alert alert-success";
				//$scope.messages.text=Messages('submissions.msg.validate.success');
				//$scope.messages.open();
				decompte = processInSubmission(decompte, action, error, tableau_analyzes, tableau_studies, tableau_samples, tableau_experiments);
				
			}).error(function(data){
				$scope.messages.addDetails(data);
				//$scope.messages.setError("save");
				error = true;
				decompte = processInSubmission(decompte, action, error, tableau_analyzes, tableau_studies, tableau_samples, tableau_experiments);
		        $scope.treeLoadInProgress = false;
			});			
			//console.log("studyTitle = " + tableau_studies[i].title + " state = "+ tableau_studies[i].state.code);
		}
		if($scope.submission.studyCode !=null){
			$scope.studyDT.setData(tableau_studies, tableau_studies.length);
			// sauvegarde cote client des studies avec bon statut
			$scope.studyDT.save(); // fait le save cote client mais n'utilise pas url et ne fait pas save dans database.
		}

	

		// Mise à jour du status  des samples :
		for(var i = 0; i < tableau_samples.length ; i++){
			//console.log("sampleCode = " + tableau_samples[i].code + " state = "+ tableau_samples[i].state.code);
			//tableau_samples[i].state.code = "SUB-V";
			//console.log("sampleTitle = " + tableau_samples[i].title + " state = "+ tableau_samples[i].state.code);
			//console.log("sampleCode = " + tableau_samples[i].code + " state = "+ tableau_samples[i].state.code);
			// sauvegarde dans database asynchrone
			$http.put(jsRoutes.controllers.sra.samples.api.Samples.update(tableau_samples[i].code).url, tableau_samples[i])
			.success(function(data){
				//Set success message
				//$scope.messages.clazz="alert alert-success";
				//$scope.messages.text=Messages('submissions.msg.validate.success');
				//$scope.messages.open();
				decompte = processInSubmission(decompte, action, error, tableau_analyzes, tableau_studies, tableau_samples, tableau_experiments);
			}).error(function(data){
				$scope.messages.addDetails(data);
				//$scope.messages.setError("save");
				error = true;
				decompte = processInSubmission(decompte, action, error, tableau_analyzes, tableau_studies, tableau_samples, tableau_experiments);
		        $scope.treeLoadInProgress = false;
			});			
			//console.log("sampleTitle = " + tableau_samples[i].title + " state = "+ tableau_samples[i].state.code);
		}
		
		if($scope.countSample != 0) {
			$scope.sampleDT.setData(tableau_samples, tableau_samples.length);
			// sauvegarde cote client des samples avec bon statut
			$scope.sampleDT.save(); // fait le save cote client mais n'utilise pas url et ne fait pas save dans database.
		}
		// Mise à jour du statut des experiments :
		for(var i = 0; i < tableau_experiments.length ; i++){
			//console.log("a l'ind " + i + " experimentCode = " + tableau_experiments[i].code + " state = "+ tableau_experiments[i].state.code);
			//tableau_experiments[i].state.code = "SUB-V";
			//console.log("experimentCode = " + tableau_experiments[i].code + " state = "+ tableau_experiments[i].state.code);
			// sauvegarde dans database :
			//console.log("appel processInSubmission pour les experiments");

			$http.put(jsRoutes.controllers.sra.experiments.api.Experiments.update(tableau_experiments[i].code).url, tableau_experiments[i]).success(function(data){
				//console.log("LILI  success");
				decompte = processInSubmission(decompte, action, error, tableau_analyzes, tableau_studies, tableau_samples, tableau_experiments);
			}).error(function(data){
				$scope.messages.addDetails(data);
				//var maMap = new Map(); bad solution
				//maMap.set("toto","titi");
				//$scope.messages.addDetails(maMap);
				// bad ecriture => $scope.messages.addDetails('keyTOTO', "valErroTOTO");

//				var monError = {};
//				monError['keyTiti']=["valErrorTiti"];
//				$scope.messages.addDetails(monError);
//				
//				$scope.messages.addDetails({"keyToto":["valToto_1", "valToto_2"]});
				
				error = true;
				//console.log("LILI echec");
				decompte = processInSubmission(decompte, action, error, tableau_analyzes, tableau_studies, tableau_samples, tableau_experiments);
				//$scope.messages.setError("save");
		        $scope.treeLoadInProgress = false;
			});			
		}
		if($scope.countExperiment != 0) {
			// initialisation inutile $scope.experimentDT = datatable(experimentsDTConfig);
			$scope.experimentDT.setData(tableau_experiments, tableau_experiments.length);
			// sauvegarde cote client des experiments avec bon statut :
			$scope.experimentDT.save(); // fait le save cote client mais n'utilise pas url et ne fait pas save dans database.
		}
		//mise a jour l'etat de submission a SUB-V realise dans closeSubmission appelé par processInSubmission

	};

	
	$scope.cancel = function(){
		//console.log("call cancel");
		$scope.messages = messages();
		$scope.messages.clear();
		if ($scope.studyDT) {
			$scope.studyDT.cancel();
		}
		if ($scope.sampleDT) {
			$scope.sampleDT.cancel();
		}
		if ($scope.experimentDT) {
			$scope.experimentDT.cancel();
		}
		if ($scope.runDT) {
			$scope.runDT.cancel();
		}	
		if ($scope.rawDataDT) {
			$scope.rawDataDT.cancel();
		}
		$scope.mainService.stopEditMode();	
        $scope.treeLoadInProgress = false;
	};

	
	$scope.activeEditMode = function(){
		$scope.messages = messages();
		$scope.messages.clear();
		$scope.mainService.startEditMode();
		//console.log("studyDT = "+$scope.studyDT);
		//console.log("expDT = "+$scope.experimentDT);
		if ($scope.analysisDT) {
			$scope.analysisDT.setEdit();
		}
		if ($scope.studyDT) {
			$scope.studyDT.setEdit();
		}
		if ($scope.sampleDT) {
			$scope.sampleDT.setEdit();
		}
		if ($scope.experimentDT) {
			$scope.experimentDT.setEdit();
		}
		
	};

	// Les donnees sont editables uniquement si submission de type creation et SUB-N. Suppression du controle utilisateur
	// En mode update, en passant par interface, la soumission est mise dans l'etat SUBU-SMD-IW donc non editable.
	// En mode release, en passant par interface, la soumission est mise dans l'etat SUBR-SMD-IW donc non editable.
	$scope.isEditable = function() {  
		var value = false;
		if ($scope.submission != null && $scope.user != null) {
			//if ($scope.submission.state.code=='SUB-N' && $scope.user === $scope.submission.traceInformation.createUser) {
			if ($scope.submission.state.code=='SUB-N') {
				value = true;
			}
		}
		return value;
	};
	

	$scope.isActiveUmbrellaProject = function() {  
		var value = false;
		if ($scope.countUmbrellaProject > 0) {
			value = true;
		}
		//console.log("isActiveUmbrellaProject=", Svalue);
		return value;
	};	
	
	$scope.isActiveStudy = function() {  
		var value = false;
		if ($scope.countStudy > 0) {
			value = true;
		}
		//console.log("isActiveStudy=", value);

		return value;
	};
	
	$scope.isDataNotActiveStudy = function() {  
		var value = false;
//		if ($scope.countStudy > 0 
//				&& $scope.countProject > 0 ) {
		if ($scope.countStudy > 0) {
			value = true;
		}
		//console.log("isDataNotActiveStudy = ", value);
		return value;
	};
	
	$scope.isActiveSample = function() {  
		var value = false;
		if ($scope.countSample > 0 
//				&& ! $scope.countProject > 0
				&& ! $scope.countStudy > 0) {
			value = true;
		}
		//console.log("isActiveSample=", value);
		return value;
	};
	
	$scope.isDataNotActiveSample = function() {  
		var value = false;
		if ($scope.countSample > 0 
//			&& ($scope.countProject > 0 || $scope.countStudy > 0) ) {
			&& $scope.countStudy > 0 ) {

			value = true;
		}
		//console.log("isDataNotActiveSample = ", value);
		return value;
	};
	
	$scope.isActiveExperiment = function() {  
		var value = false;
		if ($scope.countExperiment > 0 
//			    && ! $scope.countProject > 0
				&& ! $scope.countStudy > 0 
				&& ! $scope.countSample > 0) {
			value = true;
		}
		//console.log("isActiveExperiment=", value);
		return value;
	};
	
	$scope.isActiveAnalysis = function() {  
		var value = false;
		if ($scope.countAnalysis > 0){
			value = true;
		}
		//console.log("isActiveAnalysis=", value);
		return value;
	};	
	
	$scope.isDataNotActiveExperiment = function() {  
		var value = false;
		if ($scope.countExperiment > 0 &&
//				($scope.countProject > 0 || $scope.countStudy > 0 || $scope.countSample > 0)) {
				($scope.countStudy > 0 || $scope.countSample > 0)) {

			value = true;
		}
		//console.log("isDataNotActiveExperiment=", value);
		return value;
	};
	
//	
//	$scope.isDataRun = function() {  
//		var value = false;
//		if ($scope.countRun > 0) {
//			value = true;
//		}
//		return value;
//	};
	

	
	// methode appelée depuis la vue avec la directive ng-init
	$scope.setUserInScope = function(user) {
		$scope.user = user;
		//console.log("Dans setUserInScope, user= ", user);
	};


	// methode utilisée dans le controller CommentCtrl
	$scope.isCreationMode=function() {
		//console.log("Dans isCreationMode, scope.submission = ", $scope.submission);
		return false; // Dans la vue details, on n'est jamais en mode creation d'un project, le project existe bien en base.
	};
	

	//-----------------------------------------------------------------------------------------

	// Initialisations :
	//------------------

	// si on declare dans services => var sraVariables = {};
	// si on declare dans le controlleur :
	$scope.sraVariables = {};
//	$scope.form = {};
	$scope.subList={};
	//$scope.checkSample=false;
	$scope.treeLoadInProgress = false;
	//$scope.form.createUser = $scope.user;

	//$scope.createUser = "sgas";  // instantié depuis la vue avec la directive ng-init et appel de setUserInScope
	var tableau_analyzes = [];
	var tableau_studies = [];
	var tableau_samples = [];
	var tableau_experiments = [];
	init();  // il faut mettre definition des fonctions avant appel, sinon reconnait pas la fonction

	//console.log($scope.submission);
	
	// NGL-4235 
	$scope.isShowConsigne=true;
	$scope.toggleShowConsigne=function(){
		console.log("TOGGLE details-ctrl.js");
		if ( $scope.isShowConsigne===false) { $scope.isShowConsigne=true}
		else {$scope.isShowConsigne=false}
	};
	
	
}]).controller('CommentsCtrl',['$scope','$sce', '$http','lists','$parse','$filter','datatable', 
                               function($scope,$sce,$http,lists,$parse,$filter,datatable) {

	$scope.currentComment = {comment:undefined};
	//console.log("Dans CommentsCtrl, scope.submission=", $scope.submission);
	$scope.analyseText = function(e){
		
		if(e.keyCode === 9){
			e.preventDefault();
		}
	};
	
	$scope.convertToBr = function(text){
		return $sce.trustAsHtml(text.replace(/\n/g, "<br>"));
	};
	
	$scope.cancel = function(){	
		$scope.currentComment = {comment:undefined};
		$scope.index = undefined;
	};
	
	$scope.save = function(){	
		//console.log("Dans le save de CommentsCtrl, scope.submission = ", $scope.submission );
		//console.log("Dans le save de CommentsCtrl, scope.currentComment = ", $scope.currentComment );

		if($scope.isCreationMode()){
			$scope.submission.comments.push($scope.currentComment);
			$scope.currentComment = {comment:undefined};
		}else{
			$scope.messages.clear();
			$http.post(jsRoutes.controllers.sra.submissions.api.SubmissionComments.save($scope.submission.code).url, $scope.currentComment)
			.success(function(data, status, headers, config) {
				if(data!=null){
					$scope.messages.setSuccess("save");
					$scope.submission.comments.push(data);
					$scope.currentComment = {comment:undefined};
				}
			})
			.error(function(data, status, headers, config) {
				$scope.messages.setError("save");
				$scope.messages.setDetails(data);
			});		
		}		
	};
	
	$scope.isUpdate = function(){
		return ($scope.index != undefined);		
	};
	
	$scope.setUpdate = function(comment, index){
		$scope.currentComment = angular.copy(comment);
		$scope.index = index;
	};
	
	$scope.update = function(){		
		if($scope.isCreationMode()){
			$scope.submission.comments[$scope.index] = $scope.currentComment;
			$scope.currentComment = {comment:undefined};
			$scope.index = undefined;			
		}else{	
			$scope.messages.clear();
			$http.put(jsRoutes.controllers.sra.submissions.api.SubmissionComments.update($scope.submission.code, $scope.currentComment.code).url, $scope.currentComment)
			.success(function(data, status, headers, config) {
				if(data!=null){
					$scope.messages.setSuccess("save");
					$scope.submission.comments[$scope.index] = $scope.currentComment;
					$scope.currentComment = {comment:undefined};
					$scope.index = undefined;
				}
			})
			.error(function(data, status, headers, config) {
				$scope.messages.setError("save");
				$scope.messages.setDetails(data);
			});
		}
	};
	
	$scope.remove = function(comment, index){
		if($scope.isCreationMode()){
			$scope.currentComment = {comment:undefined};
			$scope.submission.comments.splice(index, 1);
		}else if (confirm(Messages("comments.remove.confirm"))) {
			$scope.messages.clear();
			$http.delete(jsRoutes.controllers.sra.submissions.api.SubmissionComments.delete($scope.submission.code, comment.code).url)
			.success(function(data, status, headers, config) {
				if(data!=null){
					$scope.messages.setSuccess("save");
					$scope.currentComment = {comment:undefined};
					$scope.submission.comments.splice(index, 1);
				}
			})
			.error(function(data, status, headers, config) {
				$scope.messages.setError("remove");
				$scope.messages.setDetails(data);				
			});
		}
	};

}]);




