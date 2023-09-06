"use strict";
 
 angular.module('ngl-sub.Services', []).
	factory('services', ['$http', 'mainService', 'lists', 'datatable', 'toolsServices', 
		function($http, mainService, lists, datatable, toolsServices) {

		var service = {

		// methode utilisée pour definir les colonnes
		getExperimentColumns:function() {				
			var columns = [];
			columns.push({property:"traceInformation.creationDate",
			    		  header: Messages("traceInformation.creationDate"),
			    		  type :"date",	
						  hide :true,	    	  	
			    		  order:true
			    	    });	
			columns.push({property:"traceInformation.createUser",
			    		  header: Messages("traceInformation.creationUser"),
			    		  type :"text",
					      hide :true,			    	  	
			    		  order:true
			    		});	
			columns.push({property:"state.code",
			    		  "filter":"codes:'state'",
			    		  header: Messages("experiment.state"),
			    		  type :"text",		    	  	
					      hide :true,			    	  	
			    		  order:true,
			    		  edit:false,
			    		  choiceInList:false
			    		});		
			columns.push({property:"code",
			    		  header: Messages("experiment.code"),
			    		  type :"text",		    	  	
			    		  order:true
			    		});
			columns.push({property:"firstSubmissionDate",
			    		  header: Messages("firstSubmissionDate"),
			    		  type :"date",	
						  hide :true,	    	  	
			    		  order:true
			    	    });	
			columns.push({property:"accession",
			    		  header: Messages("experiment.accession"),
			    		  type :"text",		    	  	
					      hide :true,			    	  	
			    		  order:true
			    		});
			columns.push({property:"projectCode",
			    		  header: Messages("experiment.projectCode"),
			    		  type :"text",		    	  	
					      hide :true,			    	  	
			    		  order:true,
			    		  edit:false,
			    		  choiceInList:false  
			    		});
			columns.push({property:"title",
			    		  header: Messages("experiment.title"),
						  type :"text",	
					      hide:true,
					      order:true,
					      edit:true,  
					      editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
					      choiceInList:false
			    		});
			columns.push({property:"librarySelection",
			    		  header: Messages("experiment.librarySelection"),
			    		  type :"String",
			    		  hide:true,
			    		  edit:true,
			    		  order:true,
			    		  choiceInList:true,
			    		  listStyle:'bt-select',
			    		  possibleValues:'sraVariables.librarySelection',
			    	 	});
			columns.push({property:"libraryStrategy",
			    		  header: Messages("experiment.libraryStrategy"),
			    		  type :"String",
			    		  hide:true,
			    		  edit:true,
			    		  order:true,
			    		  choiceInList:true,
			    		  listStyle:'bt-select',
			    		  possibleValues:'sraVariables.libraryStrategy',
			    		});
			columns.push({property:"librarySource",
			    		  header: Messages("experiment.librarySource"),
			    		  type :"String",
			    		  hide:true,
			    		  edit:true,
			    		  order:true,
			    		  choiceInList:true,
			    		  listStyle:'bt-select',
			    		  possibleValues:'sraVariables.librarySource',
			    		});
			columns.push({property:"libraryLayout",
			    	      header: Messages("experiment.libraryLayout"),
			    	      type :"String",
			    	      hide:true,
			    	      edit:false,
			    	      order:true,
			    	      choiceInList:true,
			    	      listStyle:'bt-select',
			    	      possibleValues:'sraVariables.libraryLayout',
			    		});	
			columns.push({property:"libraryLayoutNominalLength",
			    		  header: Messages("experiment.libraryLayoutNominalLength"),
			    		  type :"integer",		    	  	
			    		  hide:true,
			    		  edit:true,
			    		  order:true
			    		});	
			columns.push({property:"libraryLayoutOrientation",
			    		  header: Messages("experiment.libraryLayoutOrientation"),
			    		  type :"String",
			    		  hide :true,			  
			    		  order:true,
			    		});	
			columns.push({property:"libraryName",
			    		  header: Messages("experiment.libraryName"),
			    		  type :"String",		    	  	
			    		  hide:true,
			    		  edit:false,
			    		  order:true
			    		});
			columns.push({property:"libraryConstructionProtocol",
			    		  header: Messages("experiment.libraryConstructionProtocol"),
						  type         : "text",
						  hide         : true,
						  edit         : false, // pas modifiable en consultation car uniquement dans vue detail
						  order        : false,
						  choiceInList : false,
						  filter       : "collect:'comment'",
						  listStyle    : 'bt-select',
			    		});
			columns.push({property:"typePlatform",
			    		  header: Messages("experiment.typePlatform"),
			    		  type :"String",		    	  	
			    		  hide:true,
			    		  edit:false,
			    		  order:true
			    		});
			columns.push({property:"instrumentModel",
			    		  header: Messages("experiment.instrumentModel"),
			    		  type :"String",		    	  	
			    		  hide:true,
			    		  edit:false,
			    		  order:true
			    		});
			columns.push({property:"lastBaseCoord",
			    		  header: Messages("experiment.lastBaseCoord"),
			    		  type :"integer",		    	  	
			    		  hide:true,
			    		  edit:true,
			    		  order:true
			    		});	
			columns.push({property:"spotLength",
			    		  header: Messages("experiment.spotLength"),
			    		  type :"Long",		    	  	
			    		  hide:true,
			    		  edit:true,
			    		  order:true
			    		});	
			columns.push({property:"sampleCode",
			    		  header: Messages("experiment.sampleCode"),
			    		  type :"String",		    	  	
			    		  hide:true,
			    		  edit:false,
			    		  order:true
			    		});	
			columns.push({property:"studyCode",
			    		  header: Messages("experiment.studyCode"),
			    		  type :"String",		    	  	
			    		  hide:true,
			    		  edit:false,
			    		  order:true
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
		},
			   
		// methode utilisée pour definir les colonnes
		getExperimentColumnsForUpdate:function() {				
			var columns = [];
			columns.push({property:"traceInformation.creationDate",
			    		  header: Messages("traceInformation.creationDate"),
			    		  type :"date",	
					      hide :true,			    	  	
			    		  order:true
			    	    });	
			columns.push({property:"traceInformation.createUser",
			    		  header: Messages("traceInformation.creationUser"),
			    		  type :"text",		    	  	
					      hide :true,			    	  	
			    		  order:true
			    		});	
			columns.push({property:"state.code",
			    		  "filter":"codes:'state'",
			    		  header: Messages("experiment.state"),
			    		  type :"text",		    	  	
			    		  order:true,
					      hide :true,			    	  	
			    		  edit:false,
			    		  choiceInList:false
			    		});		
			columns.push({property:"code",
			    		  header: Messages("experiment.code"),
			    		  type :"text",		    	  	
					      hide :true,			    	  	
			    		  order:true
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
			columns.push({property:"accession",
			    		  header: Messages("experiment.accession"),
			    		  type :"text",		    	  	
					      hide :true,			    	  	
			    		  order:true
			    		});
			columns.push({property:"projectCode",
			    		  header: Messages("experiment.projectCode"),
			    		  type :"text",		    	  	
					      hide :true,			    	  	
			    		  order:true,
			    		  edit:false,
			    		  choiceInList:false  
			    		});
			columns.push({property:"title",
			    		  header: Messages("experiment.title"),
			    		  type :"String",		    	  	
			    		  hide:true,
			    		  edit:true,
			    		  editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
			    		  order:true
			    		});
			columns.push({property:"librarySelection",
			    		  header: Messages("experiment.librarySelection"),
			    		  type :"String",
			    		  hide:true,
			    		  edit:true,
			    		  order:true,
			    		  choiceInList:true,
			    		  listStyle:'bt-select',
			    		  possibleValues:'sraVariables.librarySelection',
			    	 	});
			columns.push({property:"libraryStrategy",
			    		  header: Messages("experiment.libraryStrategy"),
			    		  type :"String",
			    		  hide:true,
			    		  edit:true,
			    		  order:true,
			    		  choiceInList:true,
			    		  listStyle:'bt-select',
			    		  possibleValues:'sraVariables.libraryStrategy',
			    		});
			columns.push({property:"librarySource",
			    		  header: Messages("experiment.librarySource"),
			    		  type :"String",
			    		  hide:true,
			    		  edit:true,
			    		  order:true,
			    		  choiceInList:true,
			    		  listStyle:'bt-select',
			    		  possibleValues:'sraVariables.librarySource',
			    		});
			columns.push({property:"libraryLayout",
			    	      header: Messages("experiment.libraryLayout"),
			    	      type :"String",
			    	      hide:true,
			    	      edit:false,
			    	      order:true,
			    	      choiceInList:true,
			    	      listStyle:'bt-select',
			    	      possibleValues:'sraVariables.libraryLayout',
			    		});	
			columns.push({property:"libraryLayoutNominalLength",
			    		  header: Messages("experiment.libraryLayoutNominalLength"),
			    		  type :"integer",		    	  	
			    		  hide:true,
			    		  edit:true,
			    		  order:true
			    		});	
			columns.push({property:"libraryLayoutOrientation",
			    		  header: Messages("experiment.libraryLayoutOrientation"),
			    		  type :"String",
			    		  hide :true,			  
			    		  order:true,
			    		});	
			columns.push({property:"libraryName",
			    		  header: Messages("experiment.libraryName"),
			    		  type :"String",		    	  	
			    		  hide:true,
			    		  edit:false,
			    		  order:true
			    		});
			columns.push({property:"libraryConstructionProtocol",
			    		  header: Messages("experiment.libraryConstructionProtocol"),
			    		  type :"String",		    	  	
			    		  hide:true,
			    		  edit:true,
			    		  order:true
			    		});
			columns.push({property:"typePlatform",
			    		  header: Messages("experiment.typePlatform"),
			    		  type :"String",		    	  	
			    		  hide:true,
			    		  edit:false,
			    		  order:true
			    		});
			columns.push({property:"instrumentModel",
			    		  header: Messages("experiment.instrumentModel"),
			    		  type :"String",		    	  	
			    		  hide:true,
			    		  edit:false,
			    		  order:true
			    		});
			columns.push({property:"lastBaseCoord",
			    		  header: Messages("experiment.lastBaseCoord"),
			    		  type :"integer",		    	  	
			    		  hide:true,
			    		  edit:true,
			    		  order:true
			    		});	
			columns.push({property:"spotLength",
			    		  header: Messages("experiment.spotLength"),
			    		  type :"Long",		    	  	
			    		  hide:true,
			    		  edit:true,
			    		  order:true
			    		});	
			columns.push({property:"sampleAccession",
			    		  header: Messages("experiment.sampleAccession"),
			    		  type :"String",		    	  	
			    		  hide:true,
			    		  edit:true,
			    		  order:true
			    		});	
			columns.push({property:"studyAccession",
			    		  header: Messages("experiment.studyAccession"),
			    		  type :"String",		    	  	
			    		  hide:true,
			    		  edit:true,
			    		  order:true
			    		});
				     	        
			 return columns;
		},
			   
		mergeExperimentInfos:function(experiments_db, mapUserExperiments, editableCreateUser, editableStateCode) {
			//console.log("Dans $scope.mergeExperimentInfos, mapUserExperiments=", mapUserExperiments);
			var tab_final_experiments = [];
			//console.log("editableCreateUser", editableCreateUser);
			//console.log("editableStateCode", editableStateCode);			

			experiments_db.forEach(function(experiment_db) {
								
				//console.log ("XXXXXXXXXXXXXX  Dans services.mergeExperimentInfos, experiment_db=", experiment_db);
				//console.log("experiment_db.code = ", experiment_db.code);
				if (mapUserExperiments[experiment_db.code] && (editableStateCode==experiment_db.state.code )) {
						// retrait controle user 
						//(editableCreateUser==experiment_db.traceInformation.createUser && editableStateCode==experiment_db.state.code )) {
			
					//console.log("YYYYYYYYY   La donnee "  + experiment_db.code + " est editable et existe bien dans le fichier utilisateur");
					var userExperiment = mapUserExperiments[experiment_db.code];
					if (userExperiment != null) {
						//console.log("XXXXXXXXXXXXXX  Dans services.mergeExperimentInfos, userExperiment : ", userExperiment);
						if(toolsServices.isNotBlank(userExperiment.lastBaseCoordonnee)) {
							experiment_db.lastBaseCoord = userExperiment.lastBaseCoordonnee;
						}
						if(toolsServices.isNotBlank(userExperiment.nominalLength)) {
							experiment_db.libraryLayoutNominalLength = userExperiment.nominalLength;
						}
						if(toolsServices.isNotBlank(userExperiment.spotLength)) {
							experiment_db.spotLength = userExperiment.spotLength;
						}
						if(toolsServices.isNotBlank(userExperiment.title)) {
							experiment_db.title = userExperiment.title;
						}	
						if(toolsServices.isNotBlank(userExperiment.libraryProtocol)) {
							experiment_db.libraryConstructionProtocol = userExperiment.libraryProtocol;
						}
						if(toolsServices.isNotBlank(userExperiment.librarySelection)) {
							experiment_db.librarySelection = userExperiment.librarySelection;
						}
						if(toolsServices.isNotBlank(userExperiment.libraryStrategy)) {
							experiment_db.libraryStrategy = userExperiment.libraryStrategy;
						}
						if(toolsServices.isNotBlank(userExperiment.librarySource)) {
							experiment_db.librarySource = userExperiment.librarySource;
						}
						// experiment.studyAccession modifiable uniquement si experiment deja soumis et si mode update = etat SUB-F.
						if( toolsServices.isNotBlank(userExperiment.studyAccession)
								&& (experiment_db.state.code === "SUB-F") 
								&& (userExperiment.studyAccession !== experiment_db.studyAccession) ) {
							// si modification du studyAccession alors enlever studyCode pour mettre le bon ensuite recuperer studyCode si existe, sinon le creer  ou bien deporter cela apres 
							experiment_db.studyAccession = userExperiment.studyAccession;
							experiment_db.studyCode = null;
							//console.log("ZZZZZZZZZZZZZ   installation du userStudyAccession pour " , userExperiment.code)
						}
						// experiment.sampleAccession modifiable uniquement si experiment deja soumis et si mode update = etat SUB-F.
						if( toolsServices.isNotBlank(userExperiment.sampleAccession)
								&& (experiment_db.state.code === "SUB-F") 
								&& (userExperiment.sampleAccession !== experiment_db.sampleAccession) ) {
							experiment_db.sampleAccession = userExperiment.sampleAccession;
							experiment_db.sampleCode = null;
							//console.log("ZZZZZZZZZZZZZ   installation du userSampleAccession pour " , userExperiment.code)
						}
					} // end if useExperiment
				} // end if editable
				tab_final_experiments.push(experiment_db);
			}) // end forEach
			return tab_final_experiments;
		}, // end mergeExperimentInfos

		
		// utilisé par consultation avec typeParser=userExperiment => n'autorise pas modification study_ac ou sample_ac via fichier utilisateur 
		// utilisé par modifier à l'EBI avec typeParser=userExperimentExtended => autorise modification study_ac et sample_ac via fichier utilisateur
		loadUserExperimentInfosAndAdd2ExperimentsDT:function(typeParser, base64UserFileExperiment, experiments_db, mapUserExperiments, editableCreateUser, editableStateCode, experimentsDT, messages, form) {
			//console.log("Dans loadUserExperimentInfosAndAdd2ExperimentsDT, typeParser=", typeParser);
			//console.log("Dans loadUserExperimentInfosAndAdd2ExperimentsDT, base64UserFileExperiment: ", base64UserFileExperiment);
			//console.log("Dans loadUserExperimentInfosAndAdd2ExperimentsDT, mapUserExperiments: ", mapUserExperiments);

			var that = this; // this n'est pas connu dans $https. 
			// Il faut sauvegarder this dans une variable(ici that) pour l'utiliser 
			// dans $https
			var fileBase64 = {"base64UserFileExperiment" : base64UserFileExperiment};
			//$http.post(jsRoutes.controllers.sra.experiments.api.Experiments.loadUserFileExperiment().url+ args, fileBase64)
						$http.post(jsRoutes.controllers.sra.experiments.api.Experiments.loadUserFileExperiment(typeParser).url, fileBase64)

			.success(function(data) {
				var mapUserExperiments = data;	
				//console.log("YYYYYYYY dans loadUserExperimentInfosAndAdd2ExperimentsDT, data=", data);
				var tab_final_experiments = that.mergeExperimentInfos(experiments_db, mapUserExperiments, editableCreateUser, editableStateCode);
				//console.log("Dans loadUserExperimentInfosAndAdd2ExperimentsDT, mapUserExperiments: ", mapUserExperiments);

				//Init datatable
				//console.log("loadUserExperimentInfosAndAdd2ExperimentsDT::tab_final_experiments= ", tab_final_experiments);
				//console.log("loadUserExperimentInfosAndAdd2ExperimentsDT::tab_final_experiments.length = ", tab_final_experiments.length);
				experimentsDT.setData(tab_final_experiments, tab_final_experiments.length);
			}).error(function(error) {
				//console.log("error : ", error);
				messages.addDetails(error);
				messages.setError("PROBLEME dans userFileExperiment :");
			});
			base64UserFileExperiment = "";
			form = {};
		}, // end loadUserExperiments.
				

		
		} // end var service		
		return service;
	}]);

	
	
