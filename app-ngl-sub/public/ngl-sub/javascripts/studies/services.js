"use strict";
 //ajouter $scope dans un service genere des problemes => $scope à utiliser dans ctrl et non services.
 angular.module('ngl-sub.StudiesServices', []).
	factory('studiesCreateService', ['$http', 'mainService', 'lists', 'datatable',  
		function($http, mainService, lists, datatable) {
		
		var isInit = false;
		
		var initListService = function(){
			if(!isInit){
				createService.lists.refresh.projects();
				$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'existingStudyType'}})
				.success(function(data) {
				// initialisation de la variable createService.sraVariables.exitingStudyType utilisée dans create.scala.html
				createService.sraVariables.existingStudyType = data;																					
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
	}])

	
	

	.factory('studiesReleaseService', ['$http', 'mainService', 'lists', 'datatable', function($http, mainService, lists, datatable){

		//methode utilisée pour definir les colonnes 
		var getColumns = function() {
		var columns = [];
			columns.push({property:"code",
			        	header: Messages("study.code"),
			        	type :"text",		    	  	
			        	order:true
			        	});	
			        	// voir comment affichicher liste des codesProjets.	
			 /*columns.push({property:"projectCodes",
			        	header: Messages("study.projectCodes"),
			        	type :"text",		    	  	
			        	order:false,
			        	edit:false,
			        	choiceInList:false  
			        	});*/ 	
			columns.push({property:"accession",
			        	header: Messages("study.accession"),
			        	type :"text",		    	  	
			        	order:false,
			        	edit:false,
			        	choiceInList:false  
			        	});	
			columns.push({property:"externalId",
			        	header: Messages("study.externalId"),
			        	type :"text",		    	  	
			        	order:false,
			        	edit:false,
			        	choiceInList:false  
			        	});				        	
			/*columns.push({property:"releaseDate",
			        	header: "study.releaseDate",
			        	type :Date,		    	  	
			        	order:false,
			        	edit:false,
			        	choiceInList:false  
			        	});	*/
			 columns.push({property:"state.code",
			        	header: Messages("study.state.code"),
			        	"filter":"codes:'state'",
			        	type :"text",		    	  	
			        	order:true
			        	});	
			 columns.push({property:"centerName",
			        	header: Messages("study.centerName"),
			        	type :"text",		    	  	
			        	order:false,
			        	edit:false,
			        	choiceInList:false  
			        	});	
			  columns.push({property:"centerProjectName",
			        	header: Messages("study.centerProjectName"),
			        	type :"text",		    	  	
			        	order:false,
			        	edit:false,
			        	choiceInList:false  
			        	});			        			        			        
			  columns.push({property:"title",
						header: Messages("study.title"),
						type :"text",	
						hide:true,
						order:true,
						edit:true,  
						editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
						choiceInList:false
				    	});	
				    
			  columns.push({property:"studyAbstract",
						header: Messages("study.studyAbstract"),
						type :"text",	
						hide:true,
						order:true,
						edit:true,  
						editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
						choiceInList:false
				    	});	
			   columns.push({property:"description",
						header: Messages("study.description"),
						type :"text",	
						hide:true,
						order:true,
						edit:true,  
						editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
						choiceInList:false
				    	});	
			      
				columns.push({property:"existingStudyType",
						header: Messages("study.existingStudyType"),
						type :"String",
						hide:true,
						edit:true,
						order:false,
						choiceInList:true,
						listStyle:'bt-select',
						possibleValues:'releaseService.sraVariables.existingStudyType',
				    	});
		return columns;
	};
	
	
	var isInit = false;
	var release = true;
	
	var initListService = function(){
		if(!isInit){
			releaseService.lists.refresh.projects();
			$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'existingStudyType'}})
				.success(function(data) {
					// initialisation de la variable sraVariables.existingStudyType 
					releaseService.sraVariables.existingStudyType = data;																					
			});
			isInit=true;
		}
	};
	
	var releaseService = {
			isRouteParam : false,
			lists : lists,
			form : undefined,
			datatable : undefined,
			sraVariables : {},
			
			
			//console.log("sraVariables :" + sraVariables); 
			// Recherche l'ensemble de studies pour un projCode :
			search : function(){
				// produit erreur => $scope.messages = messages();	
				//this.datatable.search({projCodes:this.form.projCodes, stateCode:'F-SUB', confidential:'true'});
				this.datatable.search(this.form);
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
			init : function($routeParams, studiesDTConfig){
				initListService();
				mainService.put("release",true);
				//to avoid to lost the previous search
				if(studiesDTConfig && angular.isUndefined(mainService.getDatatable())){
					releaseService.datatable = datatable(studiesDTConfig);
					mainService.setDatatable(releaseService.datatable);
					// On definit la config du tableau studiesDTConfig dans release-ctrl.js et les colonnes à afficher dans
					// release-ctrl.js ou bien dans services.js (dernier cas qui permet de reutiliser la definition des colonnes => factorisation du code)
					// Dans notre cas definition des colonnes dans releaseService.js d'ou ligne suivante 
					releaseService.datatable.setColumnsConfig(getColumns());	
						
				}else if(angular.isDefined(mainService.getDatatable())){
					releaseService.datatable = mainService.getDatatable();			
				}			
				//to avoid to lost the previous search
				if(angular.isDefined(mainService.getForm())){
					releaseService.form = mainService.getForm();	
				}else{
					releaseService.resetForm();						
				}
				
				if(angular.isDefined($routeParams)){
					this.setRouteParams($routeParams);
				}
			}
	};
	return releaseService;
	
	}]);
 		
