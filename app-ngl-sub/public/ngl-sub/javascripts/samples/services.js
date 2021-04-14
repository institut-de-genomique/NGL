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
						  type     : "String",
						  hide     : false,
						  edit     : true,
						  order    : true,
						  choiceInList : false
						});					 
			columns.push({property : "description",
						  header   : Messages("sample.description"),
						  type     : "String",
						  hide     : false,
						  edit     : true,
						  order    : true,
						  choiceInList : false
						});	
			columns.push({property : "anonymizedName",
				  header   : Messages("sample.anonymizedName"),
				  type     : "String",
				  hide     : false,
				  edit     : true,
				  order    : true,
				  choiceInList : false
				});	
			columns.push({property : "attributes",
						  header   : Messages("sample.attributes"),
						  type     : "String",
						  hide     : false,
						  edit     : true,
						  order    : true,
						  choiceInList : false
						});	
			
			return columns;
			
		},
						
					   
		
		mergeSampleInfos:function(samples_db, mapUserSamples, editableCreateUser, editableStateCode, editableType) {
			//console.log("Dans $scope.mergeSampleInfos");
			var tab_final_samples = [];
			//console.log("editableCreateUser", editableCreateUser);
			//console.log("editableStateCode", editableStateCode);			
			
			samples_db.forEach(function(sample_db) {
								
				//console.log("sample_db.code = ", sample_db.code);
				if (mapUserSamples[sample_db.code] &&
				    editableCreateUser==sample_db.traceInformation.createUser && 
					editableType==sample_db._type &&
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

		
		loadUserSampleInfosAndAdd2SamplesDT:function(base64UserFileSample, samples_db, mapUserSamples, editableCreateUser, editableStateCode, editableType, samplesDT, messages, form) {
			//console.log("Dans loadUserSampleInfosAndAdd2SamplesDT, base64UserFileSample: ", base64UserFileSample);
			//console.log("KKKKKKKKKKK , editableStateCode: ", editableStateCode);
			//console.log("KKKKKKKKKKK , editableType: ", editableType);
			var that = this; // this n'est pas connu dans $https. 
			// Il faut sauvegarder this dans une variable(ici that) pour l'utiliser 
			// dans $https
			var fileBase64 = {"base64UserFileSample" : base64UserFileSample};
			$http.post(jsRoutes.controllers.sra.samples.api.Samples.loadUserFileSample().url, fileBase64)
			.success(function(data) {
				var mapUserSamples = data;	
				var tab_final_samples = that.mergeSampleInfos(samples_db, mapUserSamples, editableCreateUser, editableStateCode, editableType);
				//Init datatable
				//console.log("loadUserSampleInfosAndAdd2SamplesDT::tab_final_samples= ", tab_final_samples);
				//console.log("loadUserSampleInfosAndAdd2SamplesDT::tab_final_samples.length = ", tab_final_samples.length);
				//console.log("loadUserSampleInfosAndAdd2SamplesDT::editableType = ", editableType);
				//console.log("loadUserSampleInfosAndAdd2SamplesDT::samplesDT = ", samplesDT);

				samplesDT.setData(tab_final_samples, tab_final_samples.length);
			}).error(function(error) {
				//console.log("error : ", error);
				messages.addDetails(error);
				messages.setError("PROBLEME dans userFileSample :");
			});
			base64UserFileSample = "";
			form = {};
		} // end loadUserSamples.
				
		
		
		} // end var service		
		return service;
	}]);

	
	
