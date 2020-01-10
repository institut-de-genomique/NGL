"use strict";


angular.module('ngl-sub.SamplesServices', []).
	factory('samplesConsultationService', ['$http', 'mainService', 'lists', 'datatable', 
		function($http, mainService, lists, datatable) {

    //methode utilisée pour definir les colonnes 
    var getColumns = function() {
		var columns = [];
		columns.push({property:"traceInformation.creationDate",
		        	header: Messages("traceInformation.creationDate"),
		        	type :"date",		    	  	
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
			          choiceInList : false  
			    	});	
		columns.push({property : "externalId",
			      	  header   : Messages("sample.externalId"),
			          type     : "text",		    	  	
			          order    : true,
			          edit     : false,
			          choiceInList : false  
			    	});				    	
		columns.push({property : "projectCode",
			      	  header   : Messages("sample.projectCode"),
			          type     : "text",		    	  	
			          order    : true,
			          edit     : false,
			          choiceInList : false  
			    	});				    	
		columns.push({property : "state.code",
			          header   : Messages("sample.state.code"),
			          "filter" : "codes:'state'",
			          type     : "text",		    	  	
			          order    : true
			     	});	    
		columns.push({property : "clone",
			          header   : Messages("sample.clone"),
			          edit     : true,
			          hide     : true,
			          type     : "text",		    	  	
			          order    : true
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
/*		columns.push({property : "commonName",
				      header   : Messages("sample.commonName"),
				      type     : "String",
			          hide     : true,
			          edit     : false,
				      order    : true,
				      choiceInList : false
					});		
*/
		columns.push({property : "title",
				      header   : Messages("sample.title"),
				      type     : "String",
			          hide     : true,
			          edit     : true,
				      order    : true,
				      choiceInList : false
					});					 
		columns.push({property : "description",
				      header   : Messages("sample.description"),
				      type     : "String",
			          hide     : true,
			          edit     : true,
				      order    : true,
				      choiceInList : false
					});			
		columns.push({property : "attributes",
				      header   : Messages("sample.attributes"),
				      type     : "String",
			          hide     : true,
			          edit     : true,
				      order    : true,
				      choiceInList : false
					});									
		return columns;
	};
	
	
	var isInit = false;
	
	var initListService = function() {
		if(!isInit) {
			console.log("dans samples.services.initListService.js");
			consultationService.lists.refresh.projects();
			lists.refresh.states({objectTypeCode:"SRASubmission"});
			isInit=true;
		}
	};
	
	var consultationService = {
			//console.log("dans samples.services.consultationService.js");
			isRouteParam : false,
			lists : lists,
			form : undefined,
			datatable : undefined,
			sraVariables : {},
			//console.log("dans samples.services.js");

			//console.log("sraVariables :" + sraVariables); 
			// Recherche l'ensemble de samples pour un projCode :
			
			search : function() {
				//this.form.accessions = [];
				//this.form.accessions.push("ERP005930");
				//this.datatable.search({projCodes:this.form.projCodes, accessions:this.form.accessions, codes:this.form.codes, accessionRegex:this.form.accessionRegex, codeRegex:this.form.codeRegex});				
				//console.log("dans samples.services.js, form.accessionRegex=" + form.accessionRegex);
				//console.log("dans samples.services.js, form.projCodes=" + form.projCodes);
				this.datatable.search(this.form);
			},
		
			cancel : function() {
				this.datatable.setData([],0);
			},
			
			resetForm : function(){
				this.form = {};	
			},
			
			// important pour avoir le menu permettant d'epingler : 
			setRouteParams:function($routeParams){
					var count = 0;
					for(var p in $routeParams){
						count++;getColumns
						break;
					}
					if(count > 0){
						this.isRouteParam = true;
						this.form = $routeParams;
					}
				},
				
			//
			// initialization of the service
			 //
			init : function($routeParams, samplesDTConfig){
				initListService();
				
				//to avoid to lost the previous search
				if(samplesDTConfig && angular.isUndefined(mainService.getDatatable())){
					consultationService.datatable = datatable(samplesDTConfig);
					mainService.setDatatable(consultationService.datatable);
					// On definit la config du tableau samplesDTConfig dans consultation-ctrl.js et les colonnes à afficher dans
					// consultation-ctrl.js ou bien dans services.js (dernier cas qui permet de reutiliser la definition des colonnes => factorisation du code)
					// Dans notre cas definition des colonnes dans consultationService.js d'ou ligne suivante 
					consultationService.datatable.setColumnsConfig(getColumns());	
						
				}else if(angular.isDefined(mainService.getDatatable())){
					consultationService.datatable = mainService.getDatatable();			
				}			
				//to avoid to lost the previous search
				if(angular.isDefined(mainService.getForm())){
					consultationService.form = mainService.getForm();	
				}else{
					consultationService.resetForm();						
				}
				
				if(angular.isDefined($routeParams)){
					this.setRouteParams($routeParams);
				}
			}
	};
	return consultationService;
	}
	
]);
