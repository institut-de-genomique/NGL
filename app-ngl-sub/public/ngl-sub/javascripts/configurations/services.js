"use strict";
 
 angular.module('ngl-sub.ConfigurationsServices', []).
	factory('configurationsCreateService', ['$http', 'mainService', 'lists', 'datatable', function($http, mainService, lists, datatable){
		
		var isInit = false;
		
		var initListService = function(){
			if(!isInit){
				createService.lists.refresh.projects();
				
				$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'strategySample'}})
				.success(function(data) {
					// initialisation de la variable createService.sraVariables.strategySample utilisée dans create.scala.html
					createService.sraVariables.strategySample = data;																					
				});
				$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'strategyStudy'}})
				.success(function(data) {
					// initialisation de la variable createService.sraVariables.strategyStudy utilisé dans create.scala.html
					createService.sraVariables.strategyStudy = data;																					
				});
				$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'librarySelection'}})
				.success(function(data) {
					createService.sraVariables.librarySelection = data;																				
				});
				$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'libraryStrategy'}})
				.success(function(data) {
					createService.sraVariables.libraryStrategy = data;																					
				});
				$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'librarySource'}})
				.success(function(data) {
					createService.sraVariables.librarySource = data;																					
				});
				
				isInit=true;
			}
		};
		
		
		var createService = {
				isRouteParam : false,
				lists : lists,
				form : undefined,
				sraVariables : {},
				setRouteParams:function($routeParams){
					var count = 0;
					for(var p in $routeParams){
						count++;
						break;
					}
					if(count > 0){
						this.isRouteParam = true;
						this.form = $routeParams;
					}
				},
				
				
				resetForm : function(){
					this.form = {};	
				},
				
				
				
				
				/**
				 * initialization of the service
				 */
				init : function($routeParams){
					initListService();
					
					//to avoid to lost the previous search
					if(angular.isDefined(mainService.getForm())){
						createService.form = mainService.getForm();
					}else{
						createService.resetForm();						
					}
					
					if(angular.isDefined($routeParams)){
						this.setRouteParams($routeParams);
					}
				}
		};
		
		
		return createService;

	}	
]).factory('configurationsConsultationService', ['$http', 'mainService', 'lists', 'datatable', function($http, mainService, lists, datatable){


// methode utilisée pour definir les colonnes du datatable 

var getColumns = function(){
		var columns = [];
		columns.push({property:"traceInformation.creationDate",
			        	header: Messages("traceInformation.creationDate"),
			        	type :"date",		    	  	
			        	order:true
			        });	
		columns.push({property:"traceInformation.createUser",
        				header: Messages("traceInformation.creationUser"),
        				type :"date",		    	  	
        				order:true
					});
		columns.push({property:"code",
			        	header: Messages("configuration.code"),
			        	type :"text",		    	  	
			        	order:true
			        });	
		columns.push({property:"projectCodes",
			        	header: Messages("configuration.projectCodes"),
			        	type :"text",		    	  	
			        	order:false,
			        	edit:false,
			        	choiceInList:false  
			        });	
		columns.push({property:"strategySample",
						header: Messages("configuration.strategySample"),
						type :"String",
						hide:true,
						edit:true,
						order:false,
						choiceInList:true,
						//listStyle:'bt-select',
						possibleValues:'consultationService.sraVariables.strategySample',
					});	
		columns.push({property:"strategyStudy",
						header: Messages("configuration.strategyStudy"),
						type :"String",
						hide:true,
						edit:true,
						order:false,
						choiceInList:true,
						//listStyle:'bt-select',
						possibleValues:'consultationService.sraVariables.strategyStudy',
					});	
		columns.push({property:"librarySelection",
						header: Messages("configuration.librarySelection"),
						type :"String",
			        	hide:true,
			        	edit:true,
						order:false,
				    	choiceInList:true,
				    	listStyle:'bt-select',
				    	possibleValues:'consultationService.sraVariables.librarySelection',
				    });	
		columns.push({property:"libraryStrategy",
						header: Messages("configuration.libraryStrategy"),
						type :"String",
						hide:true,
						edit:true,
						order:false,
						choiceInList:true,
						//listStyle:'bt-select',
						possibleValues:'consultationService.sraVariables.libraryStrategy',
				    });	
		columns.push({property:"librarySource",
						header: Messages("configuration.librarySource"),
						type :"String",
						hide:true,
						edit:true,
						order:false,
						choiceInList:true,
						//listStyle:'bt-select',
						possibleValues:'consultationService.sraVariables.librarySource',
					});	
		columns.push({property:"libraryConstructionProtocol",
						 header: Messages("configuration.libraryConstructionProtocol"),
						 type :"String",		    	  	
						 hide:true,
						 edit:true,
					});	
		columns.push({property:"state.code",
			        	  header: Messages("configuration.state.code"),
			        	  "filter":"codes:'state'",
			        	  type :"text",		    	  	
			        	  order:false,
			        	  edit:false,
			        	  choiceInList:false
			        });	
			return columns;
	};
	
		

	var isInit = false;
	
	var initListService = function(){
		if(!isInit){
			consultationService.lists.refresh.projects();
			//lists.refresh.states({objectTypeCode:"SRASubmission"});
			
/*			$http.get(jsRoutes.controllers.sra.api.Variables.get('strategySample').url)
				.success(function(data) {
					// initialisation de la variable sraVariables.strategySample utilisée dans consultation.scala.html
					consultationService.sraVariables.strategySample = data;																					
			});
			$http.get(jsRoutes.controllers.sra.api.Variables.get('strategyStudy').url)
				.success(function(data) {
					// initialisation de la variable sraVariables.strategyStudy utilisée dans consultation.scala.html
					consultationService.sraVariables.strategyStudy = data;																					
			});
			$http.get(jsRoutes.controllers.sra.api.Variables.get('librarySelection').url)
			.success(function(data) {
				consultationService.sraVariables.librarySelection = data;																					
			});
			$http.get(jsRoutes.controllers.sra.api.Variables.get('libraryStrategy').url)
			.success(function(data) {
				consultationService.sraVariables.libraryStrategy = data;																					
			});
			$http.get(jsRoutes.controllers.sra.api.Variables.get('librarySource').url)
			.success(function(data) {
				consultationService.sraVariables.librarySource = data;																					
			});
*/			
			isInit=true;
		}
	};
	
	
	var consultationService = {
			isRouteParam : false,
			lists : lists,
			form : undefined,
			datatable : undefined,
			sraVariables : {},
			
			//console.log("sraVariables :" + sraVariables); 
			// methode appelee pour remplir le tableau des configurations 
			// Recherche toutes les configurations pour projCode indiqué :
			search : function(){
				// produit erreur => $scope.messages = messages();	
				//this.datatable.search({projCodes:this.form.projCodes, codes:this.form.codes, codeRegex:this.form.codeRegex});				
				this.datatable.search(this.form);
				//console.log("consultationService: " + this.form);
			},
			
			cancel : function(){
				this.datatable.setData([],0);
			},
			
			
			resetForm : function(){
				this.form = {};	
			},
			
			// important pour avoir le menu permettant d'epingler : 
			setRouteParams:function($routeParams){
					var count = 0;
					for(var p in $routeParams){
						count++;
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
			init : function($routeParams, configurationDTConfig){
				initListService();
				
			
				//to avoid to lost the previous search
				if(configurationDTConfig && angular.isUndefined(mainService.getDatatable())){
					consultationService.datatable = datatable(configurationDTConfig);
					mainService.setDatatable(consultationService.datatable);
					// On definit la config du tableau configurationDT dans consultation-ctrl.js et les colonnes à afficher dans
					// consultation-ctrl.js ou bien dans services.js (dernier cas qui permet de reutiliser la definition des colonnes => factorisation du code)
					// Dans notre cas definition des colonnes dans consultation-ctrl.js d'ou ligne suivante en commentaire.
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
 