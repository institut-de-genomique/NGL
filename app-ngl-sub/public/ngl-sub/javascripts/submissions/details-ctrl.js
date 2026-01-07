"use strict";

angular.module('home').controller('DetailsCtrl',[ '$http', '$scope', '$routeParams' , '$q', 'mainService', 'lists', 'tabService','messages', 'toolsServices', 'datatable',
	function($http, $scope, $routeParams, $q, mainService, lists, tabService, messages, toolsServices, datatable) { 

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
				mode:'remote',
                numberRecordsPerPage: 100
			},
			select:{active:false},
			showTotalNumberRecords:true,
			cancel : {
				showButton:true
			},
			/*hide:{
				active:false,
				showButton:false
			},*/
			exportCSV:{
				active:true
			},
			save : {
				active:true,
				showButton : true,
				changeClass : false,
				url:function(line){
					return jsRoutes.controllers.sra.analyzes.api.Analyzes.update(line.code).url; // jamais utilisé si mode local
				},
				callback: function (datatable, countError) {
					$scope.updateCallback(datatable, countError);
				},
				method:'put',
				value:function(line){
					return line;
				},
				mode:'remote',
			},
			edit : {
				active:true,
				showButton : true,
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
						property     : "studyIdentifier",
					    header       : Messages("analysis.studyIdentifier"),
				       	type         : "text",		    	  	
				       	order        : true,
				       	edit         : false,
				       	choiceInList : false  
				       	});				        	    	
		        			        
			columns.push({
						property     : "sampleIdentifier",
						header       : Messages("analysis.sampleIdentifier"),
						type         : "String",
						hide         : true,
						edit         : false,
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
						property     : "dropBox",
						header       : Messages("analysis.dropBox"),
						type :"text",	
						hide:true,
						order:true,
						edit:false,
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
		
		
		var umbrellaProjectsDTConfig = { // on ne doit pas pouvoir editer car on construit umbrella et on envoie à l'EBI sans edition et validation utilisateur
			name:' projectsDT',          // c'est juste pour la visualisation.
			order :{by:'code',mode:'local', reverse:true},
			search:{
				url:jsRoutes.controllers.sra.projects.api.Projects.list()
			},
			pagination:{
				active: false,
				mode:'local',
                numberRecordsPerPage: 100
			},
			select:{active:true},
			showTotalNumberRecords:true,
			edit : {
				active : false,     // permettre edition des champs editables
				showButton : false,  // bouton d'edition visible
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
				active      : false, // car l'umbrella est directement declaré à l'EBI. si modification alors passer par modifier à l'EBI.
				showButton  : false,
				mode        :'remote', 
				changeClass : true, // pour mettre les lignes bien sauvegardees en verts et celles en echec en rouge
				url:function(line){
					// pour regler problème de mapping entre representation de umbrella dans datatable et dans model java :
					$scope.toJsonUmbrellaModelOrThrow(line, $scope.errorDetails);
					console.log("line dans bon model verifier si tous les champs =", line);
					return jsRoutes.controllers.sra.projects.api.Projects.update(line.code).url; // jamais utilisé si mode local
					// et si mode remote alors problème com.fasterxml.jackson.databind.JsonMappingException pour les champs locus_tag ...
					// d'ou appel préalable de toJsonStudyModel;
				},
				method       :'put',
				//withoutEdit:true, //??

				value        :function(line){
					return line;
				},				
				callback : function(datatable, errors) {
					//console.log("XXXXXXXXXXXXXXXXX           datatable", datatable);
					//console.log("XXXXXXXXXXXXXXXXX           errors", errors);
					$scope.updateCallback(datatable, errors, $scope.messages);
				}
			},

	};
	

	var getUmbrellaProjectColumns = function() {
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
			columns.push({
						property     : "parentProjectAccessions",
				       	header       : Messages("project.parentProjectAccessions"),
				       	type         : "text",		    	  	
						hide         : true,	    	  	
				       	order        : false,
				       	edit         : false,
				       	choiceInList : false  
				       	}); 	
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
			columns.push({property   : "attributes",
					    header       : Messages("project.attributes"),
					    //"filter":"codes:'state'",
					    type         : "text",		    	  	
					    order        : false, 
					    edit         : false,  
					   	hide         : true,
						choiceInList : false,
						editTemplate :"<textarea class='form-control' #ng-model rows='3'></textarea>",
//						render       : function(data, edit) {
//							              return toolsServices.getStringAttributes(data);
//						             }
				        });							
			columns.push({property:"dropBox",
						header: Messages("study.dropBox"),
						type :"String",
						hide:true,
						edit:false,
						order:false,
						choiceInList:true,
						listStyle:'bt-select',
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


	var studiesDTConfig = {
			name:'studiesDT',
			order :{by:'code',mode:'local', reverse:true},
			search:{
				url:jsRoutes.controllers.sra.studies.api.Studies.list()
			},
			pagination:{
				active:true,
				mode:'local',
                numberRecordsPerPage: 10
			},
			select:{active:false},
			showTotalNumberRecords:true,
			cancel : {
				showButton:true
			},
			/*hide:{
				active:false,
				showButton:false
			},*/
			exportCSV:{
				active:true
			},
			edit : {
				active:true,
				showButton : true,
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
				showButton : true,
				changeClass : false,
				url:function(line){
					// pour regler problème de mapping entre representation du study dans datatable et dans model java :
					console.log("line avant sauvegarde = ", line);
					$scope.toJsonStudyModelOrThrow(line, $scope.errorDetails);
					console.log("line dans bon model =", line);
					
					return jsRoutes.controllers.sra.studies.api.Studies.update(line.code).url; // jamais utilisé si mode local 
					// et si mode remote alors problème com.fasterxml.jackson.databind.JsonMappingException pour les champs locus_tag ...
					// d'ou appel préalable de toJsonStudyModel;
				},
				method:'put',
				value:function(line){
					return line;
				},
				mode:'remote',
				callback : function(datatable, errors) {
					//console.log("XXXXXXXXXXXXXXXXX           datatable", datatable);
					//console.log("XXXXXXXXXXXXXXXXX           errors", errors);
					$scope.updateCallback(datatable, errors);
				}
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
						hide         : false,
						edit         : true, // study editable uniquement si SUB-N et à ce stade
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
			columns.push({property    : "attributes",
					    header        : Messages("study.attributes"),
					    //"filter":"codes:'state'",
					    type          : "text",		    	  	
					    order         : false, 
					    edit          : true,  
					   	hide          : true,
						choiceInList  : false,
						editTemplate  :"<textarea class='form-control' #ng-model rows='3'></textarea>",
//						render        : function(data, edit) {
//							              return toolsServices.getStringAttributes(data);
//						              },
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
				showButton : true,
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
				mode:'remote',
				batch: true,
				showButton : true,
				changeClass : false,
				url:function(lineValue){
					return jsRoutes.controllers.sra.samples.api.Samples.batchUpdate().url; 
				},
				callback: function (datatable, countError) {
					$scope.updateCallback(datatable, countError);
				},
				method:'put'
				},
			cancel : {
				showButton:true
			},
			/*
			hide:{
				active:true
			},
			 */
			exportCSV:{
				active:true
			},
			
			pagination: {
				active:true,
				mode:'local',
                numberRecordsPerPage: 250
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
				{property     : "dropBox",
					header       : Messages("sample.dropBox"),
					type :"text",	
					hide:true,
					order:true,
					edit:false,
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
				showButton : true,
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
				mode:'remote',
				batch: true,
				showButton : true,
				changeClass : false,
				url:function(lineValue){
					return jsRoutes.controllers.sra.experiments.api.Experiments.batchUpdate().url; // jamais utilisé en mode local
				},
				callback: function (datatable, countError) {
					$scope.updateCallback(datatable, countError);
				},
				method:'put'
			},
			cancel : {
				showButton:true
			},
			/*
			hide:{
				active:true,
				showButton:true
			},*/
			exportCSV:{
				active:true
			},
			pagination: {
				active:true,
				mode:'local',
                numberRecordsPerPage: 250
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
				{property     : "dropBox",
					header       : Messages("experiment.dropBox"),
					type :"text",	
					hide:true,
					order:true,
					edit:false,
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

	var runsDTConfig = {   // uniquement pour la consultation, pas d'edition ou de sauvegarde.
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
				active:false,
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
	
	
	var rawDatasDTConfig = {  // uniquement pour la consultation. Pas d'edition ou de save
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
	
	var laboReadsetsDTConfig = {  // uniquement pour la consultation. Pas d'edition ou de save
			name:'laboReadsetDT',
			order :{by:'code', mode:'local', reverse:true},
			search:{active:false},
			select:{active:false},
			pagination:{
				active:false,
				mode:'local'
			},
			showTotalNumberRecords:true,
			edit : {
				active:false,
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
			columns : [
				{property:"code",
					header: Messages("readset.code"),
					type :"text",		    	  	
					order:true
				},
				{property:"sampleOnContainer.sampleCode",
					header: Messages("nglbiSampleCode"),
					type :"text",		    	  	
					order:true
				},
				{property:"sampleOnContainer.taxonCode",
					header: Messages("nglbiTaxonCode"),
					type :"text",		    	  	
					order:true
				},
				{property:"sampleOnContainer.ncbiScientificName",
					header: Messages("nglbiNcbiScientificName"),
					type :"text",		    	  	
					order:true
				},
				{property:"sampleOnContainer.properties.devProdContext.value",
					header: Messages("nglbiDevProdContext"),
					type :"text",		    	  	
					order:true
				},	
				{property:"sampleOnContainer.referenceCollab",
					header: Messages("nglbiReferenceCollab"),
					type :"text",		    	  	
					order:true
				},
				{property:"properties.refCollabSub.value",
					header: Messages("nglbiRefCollabSub"),
					type :"text",		    	  	
					order:true
				}					
				]	        
	};

//----------------------------------------------------------------------------------------
	
	// Definition des methodes :
	//---------------------------
	
	var init = function(){
		// variables qui doivent etre definie dans le callback :
		$scope.errorDetails = {};
		$scope.userRegisterValidateError = false;
		$scope.action = "NO_VALIDATE";
		$scope.tableau_analyses = [];
		$scope.tableau_studies = [];
		$scope.tableau_samples = [];
		$scope.tableau_experiments = [];
		$scope.decompte = 1;
		$scope.tableau_readsets = [];
		$scope.h_nglbi_readset = {};
		
		$scope.dbSamples = {};
		
		$scope.messages = messages();
		$scope.messages.clear();
		$scope.decompte = 0;
		
		$scope.alertUser = "";

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
						$scope.countReadset = $scope.submission.experimentCodes.length;
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
			$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'dropBox'}})
			.success(function(data) {
				// initialisation de la variable sraVariables.dropBox 
				$scope.sraVariables.dropBox = data;
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

			$scope.initUmbrella();
			$scope.initStudy();	
			$scope.initAnalysis();	
			$scope.initSamples();
			$scope.initExperiments();
			$scope.initNglBiReadsets();

		}) // end http
	};

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
			$scope.studyDT.setSpinner(false);
			$scope.treeLoadInProgress = false;
			throw("Erreur de parsing");
		} 
	}; // end $scope.toJsonStudyModelOrThrow
		
	$scope.updateCallback=function(datatable, countError) {
		$scope.messages = messages(); // si on re-sauve sans recharger page.
		$scope.messages.clear();      // si on re-sauve sans recharger page.
		//console.log(datatable);
		//console.log(countError);
		//console.log("dans callback, $scope.tableau_samples.length = ", $scope.tableau_samples.length);
		//console.log("YYYYYYYY   TOTOTO   YYYYYYYYYYY    dans callback, displayResult : ", datatable.displayResult);
		//console.log("xxxxxxxxxx    dans callback, displayResult.line : ", datatable.displayResult.line);
		//console.log("eeeeeeeeeeeeee  dans callback scope.dbSamples = ", $scope.dbSamples);
		for (var i = 0; i < datatable.displayResult.length; i++) {
			var keysError =  Object.keys(datatable.displayResult[i].line.errors);
			//console.log("eeeeeeeee keysError", keysError);
			//console.log("eeeeeeeee keysError.length", keysError.length);
			if ( keysError.length >= 1 ) {
				// Recuperation message erreur :
				var monObj = datatable.displayResult[i].line.errors;
				//console.log("dans callback, monObj", monObj);
				//console.log("dans callback, monObj.typeOf", monObj.typeOf);
				//console.log("dans callback, monObj.hasOwnProperty()", monObj.hasOwnProperty());
				for (var [key, values] of Object.entries(monObj)) {
					var cle = `${key}`;
					var values = `${values}`; 
  					//console.log("xxxxxxxxxxx    cle = ", cle);
					//console.log("xxxxxxxxxxx    values = ", values);
					//console.log("monObjValues = ", monObj[cle]);
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
			$scope.studyDT.setSpinner(false);
			$scope.sampleDT.setSpinner(false);			
			$scope.experimentDT.setSpinner(false);
						$scope.readsetDT.setSpinner(false);					
		} else {
			$scope.messages.setSuccess("save");			
		}
	};


	$scope.initUmbrella=function() {
		if($scope.submission.umbrellaCode != null && $scope.submission.umbrellaCode.trim().length != 0){
			$http.get(jsRoutes.controllers.sra.projects.api.Projects.get($scope.submission.umbrellaCode).url)
			.success(function(data) {
				$scope.umbrellaProjects = [];
				$scope.umbrellaProjects.push(data);
				//console.log("$scope.umbrellaProjects :" , $scope.umbrellaProjects);					
				//Init datatable
				$scope.umbrellaProjectDT = datatable(umbrellaProjectsDTConfig);
				$scope.umbrellaProjectDT.setColumnsConfig(getUmbrellaProjectColumns());
				$scope.umbrellaProjectDT.setData($scope.umbrellaProjects, $scope.umbrellaProjects.length);
			});// end http
		}			
	};
	
    $scope.initStudy=function() {
		if($scope.submission.studyCode != null && $scope.submission.studyCode.trim().length != 0){
			$http.get(jsRoutes.controllers.sra.studies.api.Studies.get($scope.submission.studyCode).url)
			.success(function(data) {
				$scope.studies = [];
				$scope.studies.push(data);
//				console.log("$scope.submission.study: " + $scope.submission.studyCode);
//				console.log("$scope.studies :" + $scope.studies);
					
				//Init datatable
//				$scope.studyDT = datatable(studiesDTConfig);
//				$scope.studyDT.setData($scope.studies, $scope.studies.length);
				$scope.studyDT = datatable(studiesDTConfig);
				$scope.studyDT.setColumnsConfig(getStudyColumns());
				$scope.studyDT.setData($scope.studies, $scope.studies.length);
				//if ( $scope.submission.state.code =='SUB-N' && $scope.submission.traceInformation.createUser === $scope.user) 
				if ( $scope.submission.state.code =='SUB-N') {
					$scope.isAllValidable();
					$scope.isStudyCheckable = true;
					//console.log("ok cond 1");
					//console.log("!!!!  scope.isStudyCheckable= " + $scope.isStudyCheckable);
					//console.log("!!!!! scope.isAllValidable = " + $scope.isAllValidable());
				}
			}); // end success
		} else { // sinon, aucun studyCode
			$scope.studyCheck = true;
		}
	};
	
	
	$scope.initAnalysis=function(){
		if($scope.submission.analysisCode != null && $scope.submission.analysisCode.trim().length != 0) {
			$http.get(jsRoutes.controllers.sra.analyzes.api.Analyzes.get($scope.submission.analysisCode).url)
			.success(function(data) {
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
					if ( $scope.submission.state.code =='SUB-N') {
						$scope.isAnalysisCheckable = true;
						$scope.isRawDataCheckable = true;
						$scope.isAllValidable();
					}
			}); // end http
		} else { // sinon, aucune analyse
			$scope.analysisCheck = true;
			if($scope.submission.experimentCodes== null|| $scope.submission.experimentCodes.length==0) {
				$scope.rawDataCheck = true;
			}
		} 	
	};	


	$scope.initExperiments=function() {
		if($scope.submission.experimentCodes.length>0) {
			
			var nbElementByBatch = Math.ceil($scope.submission.experimentCodes.length / 15); //6 because 6 request max in parrallel with firefox and chrome
			var queries = [];
			var tabExperiments = [];
			tabExperiments = angular.copy($scope.submission.experimentCodes);

			for (var i = 0; i < 15 && tabExperiments.length > 0; i++) {
				var subExperimentCodes = tabExperiments.splice(0, nbElementByBatch);
				//queries.push( $http.get(jsRoutes.controllers.sra.experiments.api.Experiments.list().url, { params : { listExperimentCodes : subExperimentCodes } }) );
				queries.push( $http.get(jsRoutes.controllers.sra.experiments.api.Experiments.list().url, { params : { codes : subExperimentCodes } }) );
			}
			$q.all(queries)
			.then(function(results) {
				var allData = [];
				results.forEach(function(result) {
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
					if ($scope.submission.state.code == 'SUB-N') {
						//console.log("ok cond 3");
						$scope.isExperimentCheckable = true;
						$scope.isRunCheckable        = true;
						$scope.isRawDataCheckable    = true;
					}
					//console.log("scope.isExperimentCheckable= "   + $scope.isExperimentCheckable );
				} 
			});	// end then
		} else { //sinon, si aucun experiment
			$scope.experimentCheck = true;
			$scope.runCheck = true;
			if ($scope.submission.analysisCode == null || $scope.submission.analysisCode.trim().length == 0){
				$scope.rawDataCheck = true;
			}
		}			
	};
		

	$scope.buildRequeteNglBiReadset = function(tab_all_readsetCodes, allRequestReadset, max) {
		var tab_readsetCodes = []; // tableau provisoire pour construire requete

		for (var i=0; i < tab_all_readsetCodes.length; i++) {
			tab_readsetCodes.push(tab_all_readsetCodes[i]);
			if(tab_readsetCodes.length > max) {
				// construire requetes readsets
				// On n'utilise pas la requete get qui nous ferait une requete par experiment.
				//var request = $http.get(jsRoutes.controllers.readsets.api.ReadSets.get(experiment.readSetCode).url)
				//var requestReadset = $http.get(jsRoutes.controllers.readsets.api.ReadSets.list().url,{params:{codes:tab_readsetCodes}})
				var requestReadset = $http.get(jsRoutes.controllers.readsets.api.ReadSets.list().url,
					{params:{codes:tab_readsetCodes, includes:["code", "sampleOnContainer", "properties" ]}})
				.catch (error => {
					console.log(error);
					console.log(error.data);
					$scope.messages.addDetails(error.data); 
					return $q.reject(error);                // propage erreur, comme throw 
				});
				allRequestReadset.push(requestReadset);
				// Reinitialiser
				tab_readsetCodes = [];
			}
		}	
		// correction effet de bord pour req readset:
		if(tab_readsetCodes.length > 0) {
			//var requestReadset = $http.get(jsRoutes.controllers.readsets.api.ReadSets.list().url,
			//		{params:{codes:tab_readsetCodes}})
			var requestReadset = $http.get(jsRoutes.controllers.readsets.api.ReadSets.list().url,
					{params:{codes:tab_readsetCodes, includes:["code", "sampleOnContainer", "properties" ]}})

			.catch (error => {
				console.log(error);
				console.log(error.data);
				$scope.messages.addDetails(error.data); 
				$scope.treeLoadInProgress = false;
				return $q.reject(error);                // propage erreur, comme throw 
			});
			allRequestReadset.push(requestReadset);
	     } // end correction effet de bord	
	}// end buildRequeteReadset 	
	
	
	$scope.buildRequeteNglSubReadset = function(tab_all_readsetCodes, allRequestReadset, max) {
		var tab_readsetCodes = []; // tableau provisoire pour construire requete

		for (var i=0; i < tab_all_readsetCodes.length; i++) {
			tab_readsetCodes.push(tab_all_readsetCodes[i]);
			if(tab_readsetCodes.length > max) {
				// construire requetes readsets
				// On n'utilise pas la requete get qui nous ferait une requete par experiment.
				//var request = $http.get(jsRoutes.controllers.readsets.api.ReadSets.get(experiment.readSetCode).url)
				//var requestReadset = $http.get(jsRoutes.controllers.readsets.api.ReadSets.list().url,{params:{codes:tab_readsetCodes}})
				var requestReadset = $http.get$http.get(jsRoutes.controllers.sra.readsets.api.Readsets.list().url,
				{params:{codes:tab_readsetCodes, includes:["code", "nglbiSampleCode", "nglbiTaxonCode", "nglbiScientificName", "nglbiReferenceCollab", "nglbiRefCollabSub" ]}})
				.catch (error => {
					console.log(error);
					console.log(error.data);
					$scope.messages.addDetails(error.data); 
					return $q.reject(error);                // propage erreur, comme throw 
				});
				allRequestReadset.push(requestReadset);
				// Reinitialiser
				tab_readsetCodes = [];
			}
		}	
		// correction effet de bord pour req readset:
		if(tab_readsetCodes.length > 0) {
			//var requestReadset = $http.get(jsRoutes.controllers.readsets.api.ReadSets.list().url,
			//		{params:{codes:tab_readsetCodes}})
			var requestReadset = $http.get(jsRoutes.controllers.sra.readsets.api.Readsets.list().url,
			{params:{codes:tab_readsetCodes, includes:["code", "nglbiSampleCode", "nglbiTaxonCode", "nglbiScientificName", "nglbiReferenceCollab", "nglbiRefCollabSub" ]}})

			.catch (error => {
				console.log(error);
				console.log(error.data);
				$scope.messages.addDetails(error.data); 
				$scope.treeLoadInProgress = false;
				return $q.reject(error);                // propage erreur, comme throw 
			});
			allRequestReadset.push(requestReadset);
	     } // end correction effet de bord	
	}// end buildRequeteReadset 
	
			
//	// si solution affichage mixte 
//	$scope.initNglBiReadsetsMixteAndInitNglSubReadsetsMixte=function() {
//		console.log("Dans initNglBiReadsetsMixteAndInitNglSubReadsetsMixte");
//		if($scope.submission.experimentCodes.length>0) {
//			var nglbi_queries = [];
//			var nglsub_queries = [];
//			$scope.tabReadsets = [];
//			var tabReadsetCodes = [];
//			var nglbi_hReadsets = {};
//			tabReadsetCodes = angular.copy($scope.submission.experimentCodes);
//			//console.log("dans initReadsets avec tabReadsets=", tabReadsets);
//		    for(var i = 0; i < tabReadsetCodes.length; i++){
//				tabReadsetCodes[i] = tabReadsetCodes[i].replace("exp_","");
//			}
//			//console.log("dans initReadsets avec tabReadsetCodes final=", tabReadsetCodes);
//			$scope.buildRequeteNglBiReadset(tabReadsetCodes, nglbi_queries, 100);
//			$scope.buildRequeteNglSubReadset(tabReadsetCodes, nglsub_queries, 100);
//			$q.all(nglbi_queries)
//			.then(nglbi_result => { 
//				for (var j = 0; j < nglbi_result.length; j++) {
//					for (var k = 0; k < nglbi_result[j].data.length; k++) {
//						var readsetCode = nglbi_result[j].data[k].code;
//						var readset = nglbi_result[j].data[k];
//						//console.log("readset Code avant integration dans hash : " , readset.code);
//						//Object.assign(readsets, {readsetCode: readset}); // ne marche pas prendre ecriture suivante :	
//						nglbi_hReadsets[readsetCode] = readset;
//					}
//				}
//				$q.all(nglsub_queries)
//				.then(nglsub_result => { 
//					for (var j = 0; j < nglsub_result.length; j++) {
//						for (var k = 0; k < nglsub_result[j].data.length; k++) {
//							var readsetCode = nglsub_result[j].data[k].code;
//							var nglsub_readset = nglsub_result[j].data[k];
//							console.log("nglsub_readset", nglsub_readset);
//						    if(nglbi_hReadsets.hasOwnProperty(readsetCode)) {
//							   	var labo_readset = nglbi_hReadsets[readsetCode];
//							   	console.log("labo_readset", labo_readset);
//							   	if (labo_readset.sampleOnContainer != null && 
//						    	  	labo_readset.sampleOnContainer.properties != null &&
//								  	labo_readset.sampleOnContainer.properties.devProdContext != null && 
//								  	labo_readset.sampleOnContainer.properties.devProdContext.value != null ) {
//								  		nglsub_readset.nglbiDevProdContext = labo_readset.sampleOnContainer.properties.devProdContext.value;		
//							      	}
//						    }
//							//console.log("readset", readset);
//							$scope.tabReadsets.push(readset);
//							//Init datatable
//							$scope.readsetDT = datatable(readsetsDTConfig);
//							$scope.readsetDT.setData($scope.tabReadsets, $scope.tabReadsets.length);
//						 }	
//					}
//					//console.log("$scope.tabReadsets=", $scope.tabReadsets);
//					//console.log("$scope.readsetDT = ", $scope.readsetDT);
//				}); // end then nglsub_results
//			});	// end then nglbi_results
//			
//			//console.log("XXXXXXXXXXXXXXXXXXXXXx");
//		}//end if($scope.submission.experimentCodes.length>0)
//	};// end initNglBiReadsetsMixteAndInitNglSubReadsetsMixte
	
	
	// solution affichage dynamique nglbiReadsets
	$scope.initNglBiReadsets=function() {
		console.log("Dans initNglBiReadsets");
		if($scope.submission.experimentCodes.length>0) {
			var nglbi_queries = [];
			var nglsub_queries = [];
			$scope.tabReadsets = [];
			var tabReadsetCodes = [];
			var nglbi_hReadsets = {};
			tabReadsetCodes = angular.copy($scope.submission.experimentCodes);
			//console.log("dans initReadsets avec tabReadsets=", tabReadsets);
		    for(var i = 0; i < tabReadsetCodes.length; i++){
				tabReadsetCodes[i] = tabReadsetCodes[i].replace("exp_","");
			}
			//console.log("dans initReadsets avec tabReadsetCodes final=", tabReadsetCodes);
			$scope.buildRequeteNglBiReadset(tabReadsetCodes, nglbi_queries, 100);
			$q.all(nglbi_queries)
			.then(nglbi_result => { 
				for (var j = 0; j < nglbi_result.length; j++) {
					for (var k = 0; k < nglbi_result[j].data.length; k++) {
						var readsetCode = nglbi_result[j].data[k].code;
						var readset = nglbi_result[j].data[k];
						//console.log("readset Code avant integration dans hash : " , readset.code);
						//Object.assign(readsets, {readsetCode: readset}); // ne marche pas prendre ecriture suivante :	
						nglbi_hReadsets[readsetCode] = readset;
						$scope.tabReadsets.push(readset);
						//console.log("readset=", readset);
						//Init datatable
						$scope.laboReadsetDT = datatable(laboReadsetsDTConfig);
						$scope.laboReadsetDT.setData($scope.tabReadsets, $scope.tabReadsets.length);
					}
				}
				//console.log("$scope.tabReadsets=", $scope.tabReadsets);
				//console.log("$scope.laboReadsetDT = ", $scope.laboReadsetDT);
			});	// end then nglbi_results			
			//console.log("XXXXXXXXXXXXXXXXXXXXXx");
		}//end if($scope.submission.experimentCodes.length>0)
	};// end initNglBiReadsetsMixteAndInitNglSubReadsetsMixte
	

		
	$scope.initSamples=function() {
		if($scope.submission.sampleCodes.length>0){
			var nbElementByBatch = Math.ceil($scope.submission.sampleCodes.length / 6); //6 because 6 request max in parrallel with firefox and chrome
			var queries = [];
			var tabSamples = [];
			tabSamples = angular.copy($scope.submission.sampleCodes);
			//console.log("tabSamples", tabSamples);
			for (var i = 0; i < 6 && tabSamples.length > 0; i++) {
				var subSampleCodes = tabSamples.splice(0, nbElementByBatch);
				//queries.push( $http.get(jsRoutes.controllers.sra.samples.api.Samples.list().url, {params: {listSampleCodes:subSampleCodes}}) );
				queries.push( $http.get(jsRoutes.controllers.sra.samples.api.Samples.list().url, {params: {codes:subSampleCodes}}) );
			}
			//console.log("tabSamples apres ", tabSamples);
			//console.log("sscope.submission.sampleCodes apres ", $scope.submission.sampleCodes);

			$q.all(queries).
			then(function(results) {
				var allData = [];
				results.forEach(function(result) {
					allData = allData.concat(result.data);
				});
				
				$scope.samples = allData;

//				console.log("scope.submission.sampleCodes: " + $scope.submission.sampleCodes);
//				console.log("!!!!!!! scope.samples :" + $scope.samples);

				//Init datatable
				$scope.sampleDT = datatable(samplesDTConfig);
				$scope.sampleDT.setData($scope.samples, $scope.samples.length);
				//if ($scope.submission.state.code == 'SUB-N' && $scope.submission.traceInformation.createUser === $scope.user) 
				if ($scope.submission.state.code == 'SUB-N') {
					//console.log("ok cond 2");
					$scope.isSampleCheckable = true;
				}
			}); // end then
		} else {// si aucun samples
			$scope.sampleCheck = true;
		}
	};
	

			
	$scope.validate=function() {
		//console.log("Dans validate");
 		//var state = angular.copy($scope.submission.state);
		$scope.treeLoadInProgress = true;
		$http.put(jsRoutes.controllers.sra.submissions.api.Submissions.validateAndUpdateState($scope.submission.code).url)
		.success(function(data) {
			// Recharger données avec bon status :
			$scope.initUmbrella();
			$scope.initStudy();	
			$scope.initAnalysis();	
			$scope.initSamples();
			$scope.initExperiments();
			//Set success message
			$scope.messages = messages(); // si on re-sauve sans recharger page.
			$scope.messages.clear();      // si on re-sauve sans recharger page.
			$scope.messages.setSuccess("Succes validation de la soumission");
			$scope.treeLoadInProgress = false;
		}).error(function(data){
			$scope.messages = messages(); // si on re-sauve sans recharger page.
			$scope.messages.clear();      // si on re-sauve sans recharger page.
			$scope.messages.addDetails(data);
			$scope.messages.setError("Echec validation de la soumission");
	        $scope.treeLoadInProgress = false;
		});
	};
	
	


	$scope.userValidate = function() {
		$scope.action="VALIDATE";
		$scope.userRegisterValidate($scope.action);
	};

	$scope.userRegister = function() {
		$scope.action="NO_VALIDATE";
		$scope.userRegisterValidate($scope.action);
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
		if ($scope.readsetDT) {
			$scope.readsetDT.cancel();
		}
		$scope.mainService.stopEditMode();	
        $scope.treeLoadInProgress = false;
        $scope.errorDetails = {};
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
	
	
	$scope.isAfficheReadset = function() {  
		var value = false;
		//console.log("$scope.countReadset=", $scope.countReadset);
		//console.log("$scope.submission.state.code=", $scope.submission.state.code);
		if ($scope.countReadset > 0 
				&& $scope.submission.state.code == "SUB-N") {
			value = true;
		}
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
	$scope.errorDetails = {};
	$scope.sraVariables = {};
//	$scope.form = {};
	$scope.subList={};
	//$scope.checkSample=false;
	$scope.treeLoadInProgress = false;
	//$scope.form.createUser = $scope.user;

	//$scope.createUser = "sgas";  // instantié depuis la vue avec la directive ng-init et appel de setUserInScope

	init();  // il faut mettre definition des fonctions avant appel, sinon reconnait pas la fonction

	//console.log($scope.submission);
		$scope.userRegisterValidateError = false;

	// NGL-4235 
	$scope.isShowConsigne=false;
	$scope.toggleShowConsigne=function(){
		//console.log("TOGGLE details-ctrl.js");
		if ( $scope.isShowConsigne===true) { 
			$scope.isShowConsigne=false;
		} else {
			$scope.isShowConsigne=true;
		}
	};
	
	$scope.userRegisterValidateError = false;
	
	// NGL-5110
	$scope.isShowNglbiInformations=false;
	$scope.toggleShowNglbiInformations=function(){
		//console.log("TOGGLE search-ctrl.js");
		if ( $scope.isShowNglbiInformations===false) { $scope.isShowNglbiInformations=true}
		else {$scope.isShowNglbiInformations=false}
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
				
	// NGL-4708
	$scope.isShowConsigne=false;
	$scope.toggleShowConsigne=function(){
		//console.log("TOGGLE search-ctrl.js");
		if ( $scope.isShowConsigne===false) { $scope.isShowConsigne=true}
		else {$scope.isShowConsigne=false}
	};	
	
}]);




