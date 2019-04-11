"use strict";


angular.module('ngl-sub.ExperimentsServices', []).
	factory('experimentsConsultationService', ['$http', 'mainService', 'lists', 'datatable', 
		function($http, mainService, lists, datatable) {

   //methode utilisée pour definir les colonnes 
   var getColumns = function() {				
		var columns = [];
		columns.push({property:"traceInformation.creationDate",
					  header: Messages("experiment.traceInformation.creationDate"),
					  type :"date",		    	  	
					  order:false
				     });		
		columns.push({property:"code",
					  header: Messages("experiment.code"),
					  type :"text",		    	  	
					  order:false
				     });
		columns.push({property:"accession",
					  header: Messages("experiment.accession"),
					  type :"text",		    	  	
					  order:false
				     });
		columns.push({property:"projectCode",
					  header: Messages("experiment.projectCode"),
					  type :"text",		    	  	
					  order:false,
					  edit:false,
					  choiceInList:false  
				     });
		columns.push({property:"title",
					  header: Messages("experiment.title"),
					  type :"String",		    	  	
					  hide:true,
					  edit:true,
					  editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
					  order:false
				     });
		columns.push({property:"librarySelection",
					  header: Messages("experiment.librarySelection"),
					  type :"String",
					  hide:true,
					  edit:true,
					  order:false,
					  choiceInList:true,
					  listStyle:'bt-select-multiple',
					  possibleValues:'sraVariables.librarySelection',
				     });
		columns.push({property:"libraryStrategy",
					  header: Messages("experiment.libraryStrategy"),
					  type :"String",
					  hide:true,
					  edit:true,
					  order:false,
					  choiceInList:true,
					  listStyle:'bt-select-multiple',
					  possibleValues:'sraVariables.libraryStrategy',
				     });
		columns.push({property:"librarySource",
		 			  header: Messages("experiment.librarySource"),
					  type :"String",
					  hide:true,
					  edit:true,
					  order:false,
					  choiceInList:true,
					  listStyle:'bt-select-multiple',
					  possibleValues:'sraVariables.librarySource',
				     });
		columns.push({property:"libraryLayout",
					  header: Messages("experiment.libraryLayout"),
					  type :"String",
					  hide:true,
					  edit:false,
					  order:false,
					  choiceInList:true,
					  listStyle:'bt-select-multiple',
					  possibleValues:'sraVariables.libraryLayout',
				     });	
		columns.push({property:"libraryLayoutNominalLength",
					  header: Messages("experiment.libraryLayoutNominalLength"),
					  type :"integer",		    	  	
					  hide:true,
					  edit:true,
					  order:false
				     });	
		columns.push({property:"libraryLayoutOrientation",
					  header: Messages("experiment.libraryLayoutOrientation"),
					  type :"String",
					  hide:true,
					  edit:true,
					  order:false,
					  choiceInList:true,
					  listStyle:'bt-select-multiple',
					  possibleValues:'sraVariables.libraryLayoutOrientation',
				     });	
		columns.push({property:"libraryName",
					  header: Messages("experiment.libraryName"),
					  type :"String",		    	  	
					  hide:true,
					  edit:true,
					  order:false
				     });
		columns.push({property:"libraryConstructionProtocol",
					  header: Messages("experiment.libraryConstructionProtocol"),
					  type :"String",		    	  	
					  hide:true,
					  edit:true,
					  order:false
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
					  edit:true,
					  order:true
				     });
		columns.push({property:"lastBaseCoord",
					  header: Messages("experiment.lastBaseCoord"),
					  type :"integer",		    	  	
				  	  hide:true,
					  edit:true,
					  order:false
				     });	
		columns.push({property:"spotLength",
					  header: Messages("experiment.spotLength"),
					  type :"Long",		    	  	
					  hide:true,
				  	  edit:true,
					  order:false
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
		columns.push({property:"state.code",
					  "filter":"codes:'state'",
					  header: Messages("experiment.state"),
					  type :"text",		    	  	
					  order:true,
					  edit:false,
					  choiceInList:false
				     });			     	        
		return columns;
	};
	
	
	var isInit = false;
	
	var initListService = function() {
		if(!isInit) {
			console.log("dans experiments.services.initListService.js");
			consultationService.lists.refresh.projects();
			lists.refresh.states({objectTypeCode:"SRASubmission"});
			isInit=true;
		}
	};
	
	var consultationService = {
			//console.log("dans experiments.services.consultationService.js");
			isRouteParam : false,
			lists : lists,
			form : undefined,
			datatable : undefined,
			sraVariables : {},
			//console.log("dans experiments.services.js");

			//console.log("sraVariables :" + sraVariables); 
			// Recherche l'ensemble de experiments pour un projCode :
			
			search : function() {
				//this.form.accessions = [];
				//this.form.accessions.push("ERP005930");
				//this.datatable.search({projCodes:this.form.projCodes, accessions:this.form.accessions, codes:this.form.codes, accessionRegex:this.form.accessionRegex, codeRegex:this.form.codeRegex});				
				//console.log("dans experiments.services.js, form.accessionRegex=" + form.accessionRegex);
				//console.log("dans experiments.services.js, form.projCodes=" + form.projCodes);
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
			init : function($routeParams, experimentsDTConfig){
				initListService();
				
				//to avoid to lost the previous search
				if(experimentsDTConfig && angular.isUndefined(mainService.getDatatable())){
					consultationService.datatable = datatable(experimentsDTConfig);
					mainService.setDatatable(consultationService.datatable);
					// On definit la config du tableau experimentsDTConfig dans consultation-ctrl.js et les colonnes à afficher dans
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
