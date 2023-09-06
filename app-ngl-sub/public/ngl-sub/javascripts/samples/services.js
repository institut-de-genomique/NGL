"use strict";
 
 angular.module('ngl-sub.Services', []).
	factory('services', ['$http', 'mainService', 'lists', 'datatable', 'toolsServices', 
		function($http, mainService, lists, datatable, toolsServices) {

		var service = {


	    //methode utilisée pour definir les colonnes 
		getSampleColumns:function() { 		
			var columns = [];
			columns.push({property:"traceInformation.creationDate",
						  header: Messages("traceInformation.creationDate"),
						  type :"date",	
						  hide:true,
						  order:true
						});		
			columns.push({property:"traceInformation.createUser",
						  header: Messages("traceInformation.creationUser"),
						  type :"date",		    	  	
						  order:true
						});
			columns.push({property:"state.code",
						  "filter":"codes:'state'",
						  header: Messages("sample.state"),
						  type :"text",		    	  	
						  order:true,
						  edit:false,
						  order:true
						});	
			columns.push({property : "code",
						  header   : Messages("sample.code"),
						  type     : "text",		    	  	
						  order    : true
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
			columns.push({property : "accession",
						  header   : Messages("sample.accession"),
						  type     : "text",		    	  	
						  order    : true,
						  edit     : false,
						  hide         : true,
						  choiceInList : false  
						  
						});	
			columns.push({property : "externalId",
						  header   : Messages("sample.externalId"),
						  type     : "text",		    	  	
						  order    : true,
						  edit     : false,
						  hide         : true,

						  choiceInList : false  
						});				    	
			columns.push({property : "projectCode",
						  header   : Messages("sample.projectCode"),
						  type     : "text",		    	  	
						  order    : true,
						  edit     : false,
						  hide         : true,

						  choiceInList : false  
						});				    	  			     		     			        			            
			columns.push({property : "taxonId",
						  header   : Messages("sample.taxonId"),
						  type     : "String",
						  hide     : true,
						  edit     : false,
						  order    : true,
						  choiceInList:false
						});	
			columns.push({property : "scientificName",
						  header   : Messages("sample.scientificName"),
						  type     : "String",
						  hide     : true,
						  edit     : false,
						  order    : true,
						  choiceInList : false
						});						
			columns.push({property : "title",
						  header   : Messages("sample.title"),
					      type :"text",		    	  	
					      order:true,
					      edit:true,
					      editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
					      choiceInList:false
						});					 
			columns.push({property : "description",
						  header   : Messages("sample.description"),
					      type :"text",		    	  	
					      order:true,
					      edit:true,
					      editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
					      choiceInList:false
						});	
			columns.push({property : "anonymizedName",
				          header   : Messages("sample.anonymizedName"),
					      type :"text",		    	  	
					      order:true,
					      edit:true,
					      editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
					      choiceInList:false
				});	
//			columns.push({property : "attributes",
//						  header   : Messages("sample.attributes"),
//						  type     : "String",
//						  hide     : false,
//						  edit     : true,
//						  order    : true,
//						  choiceInList : false
//						});	
			columns.push({property:"attributes",
					      header: Messages("sample.attributes"),
					      //"filter":"codes:'state'",
					      type :"text",		    	  	
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
						edit         : false, // pas modifiable en consultation car uniquement dans vue detail
						order        : false,
						choiceInList : false,
						filter       : "collect:'comment'",
						listStyle    : 'bt-select',
						editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
						});		
			
			return columns;
			
		},
						
					   
		
		mergeSampleInfos:function(samples_db, mapUserSamples, editableCreateUser, editableStateCode) {
			//console.log("Dans $scope.mergeSampleInfos");
			var tab_final_samples = [];
			//console.log("editableCreateUser", editableCreateUser);
			//console.log("editableStateCode", editableStateCode);	
		
			//console.log("samples_db = ", samples_db);
			samples_db.forEach(function(sample_db) {
				//console.log("sample_db.code = g",sample_db.code,"g");
				if (mapUserSamples[sample_db.code] &&
				    //editableCreateUser==sample_db.traceInformation.createUser &&  // retrait du controle sur l'utilisateur
					// on veut pouvoir editer des Samples et non des externalSample:
					"Sample"==sample_db._type &&  
				    editableStateCode==sample_db.state.code) {
					//console.log("YYYYYYYYY   La donnee "  + sample_db.code + " est editable et existe bien dans le fichier utilisateur");
					var userSample = mapUserSamples[sample_db.code];
					if (userSample != null) {
						//console.log("userSample : ", userSample);
						if(toolsServices.isNotBlank(userSample.title)) {
							sample_db.title = userSample.title;
						}
						if(toolsServices.isNotBlank(userSample.description)) {
							sample_db.description = userSample.description;
						}
						if(toolsServices.isNotBlank(userSample.anonymizedName)) {
							sample_db.anonymizedName = userSample.anonymizedName;
						}
						if(toolsServices.isNotBlank(userSample.attributes)) {
							sample_db.attributes = userSample.attributes;
						}
					} // end if userSample
				} // end if editable
				tab_final_samples.push(sample_db);
			}) // end forEach
			return tab_final_samples;
		}, // end mergeSampleInfos

		


		// utilisé par consultation avec typeParser=userSample 
		loadUserSampleInfosAndAdd2SamplesDT:function(typeParser, base64UserFileSample, samples_db, mapUserSamples, editableCreateUser, editableStateCode, samplesDT, messages, form) {
			//console.log("Dans loadUserSampleInfosAndAdd2SamplesDT, typeParser=", typeParser);
			//console.log("Dans loadUserSampleInfosAndAdd2SamplesDT, base64UserFileSample: ", base64UserFileSample);
			//console.log("Dans loadUserSampleInfosAndAdd2SamplesDT, samples_db: ", samples_db);
			//console.log("Dans loadUserSampleInfosAndAdd2SamplesDT, mapUserSamples: ", mapUserSamples);
			//console.log("Dans loadUserSampleInfosAndAdd2SamplesDT, editableCreateUser: ", editableCreateUser);
			//console.log("Dans loadUserSampleInfosAndAdd2SamplesDT, editableStateCode: ", editableStateCode);
			//console.log("Dans loadUserSampleInfosAndAdd2SamplesDT, samplesDT: ", samplesDT);
			//console.log("Dans loadUserSampleInfosAndAdd2SamplesDT, messages: ", messages);
			//console.log("Dans loadUserSampleInfosAndAdd2SamplesDT, form: ", form);
			

			var that = this; // this n'est pas connu dans $https. 
			// Il faut sauvegarder this dans une variable(ici that) pour l'utiliser 
			// dans $https
			var fileBase64 = {"base64UserFileSample" : base64UserFileSample};
			//$http.post(jsRoutes.controllers.sra.experiments.api.Experiments.loadUserFileExperiment().url+ args, fileBase64)
			$http.post(jsRoutes.controllers.sra.samples.api.Samples.loadUserFileSample(typeParser).url, fileBase64)

			.success(function(data) {
				mapUserSamples = data;	
				//console.log("YYYYYYYY dans loadUserSampleInfosAndAdd2SamplesDT, data=", data);
				var tab_final_samples = that.mergeSampleInfos(samples_db, mapUserSamples, editableCreateUser, editableStateCode);
				//console.log("Dans loadUserSampleInfosAndAdd2SamplesDT, mapUserSamples: ", mapUserSamples);

				//Init datatable
				//console.log("loadUserSampleInfosAndAdd2SamplesDT::tab_final_samples= ", tab_final_samples);
				//console.log("loadUserSampleInfosAndAdd2SamplesDT::tab_final_samples.length = ", tab_final_samples.length);
				samplesDT.setData(tab_final_samples, tab_final_samples.length);
			}).error(function(error) {
				//console.log("error : ", error);
				messages.addDetails(error);
				messages.setError("PROBLEME dans userFileSample :");
			});
			//base64UserFileSample = "";
			//form = {};
		}, // end loadUserSampleInfosAndAdd2SamplesDT.
				

		
		} // end var service		
		return service;
	}]);

	
	
