"use strict";
 
 angular.module('ngl-sub.SubmissionsServices', []).
	factory('submissionsCreateService', ['$http', 'mainService', 'lists', 'datatable', 
		function($http, mainService, lists, datatable) {

	// si utilisation scope, le passer en argument des fonctions. 
		// le scope est defini au niveau du controller qui pourra le fournir à une methode du service.
		var getColumns = function(){
			var columns = [];
			columns.push({	property:"traceInformation.creationDate",
			            	header: Messages("traceInformation.creationDate"),
			        		type :"date",		    	  	
			        		order:true});
				   			columns.push({	property:"code",
				    	  	header: Messages("submissions.code"),
				    	  	type :"text",		    	  	
				    	  	order:true});
			columns.push({	property:"state.code",
							"filter":"codes:'state'",
							header: Messages("submissions.state"),
							type :"text",
							order:true});	
			return columns;
		};
		
		var isInit = false;
		
		var initListService = function(){
			if(!isInit){
				createService.lists.refresh.projects();
				isInit=true;
			}
		};
		
		
		var createService = {
				isRouteParam : false,
				lists : lists,
				form : undefined,
				datatable : undefined,
				internalStudies : true,
				externalStudies : false,
				readSets:undefined,
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
					// produit erreur si pas de passage en parametre de la methode :$scope.messages = messages();
				},
				
				refreshSubmissions : function(){
					if(this.form.projCodes && this.form.projCodes.length > 0){
						//this.datatable.search(this.form);
						this.datatable.search({projCodes:this.form.projCodes, stateCode:'SUB-N'});
						//MAJ liste des readSets
						$http.get(jsRoutes.controllers.readsets.api.ReadSets.list().url,{params:{projectCodes:this.form.projCodes, submissionStateCode:"NONE", stateCode:"A", includes:"code"}}).success(function(data) {
							createService.readSets=data;
						});
					}
				},
				
				refreshSraStudies : function(){
					if(this.form.projCodes && this.form.projCodes.length > 0){
						// appel de refresh.sraStudies dans lists de common.js
						this.lists.refresh.sraStudies({projCodes:this.form.projCodes, stateCodes:["NONE", "SUB-F"]});
					}
				},
				
				refreshSraConfigurations : function(){
					if(this.form.projCodes && this.form.projCodes.length > 0){
						// appel de refresh.sraConfigurations dans lists de common.js
						this.lists.refresh.sraConfigurations({projCodes:this.form.projCodes});
					}
				},
				
				refreshReadSets : function(){
					if(this.form.projCodes && this.form.projCodes.length > 0){
						// appel de refresh.ReadSets dans lists de common.js
						// Dans ReadSetsSearchForm.java champs projectCodes et non projCodes !!!
						// includes:"code" pour ne rapatrier que les codes des readsets et non les objets entiers
						this.lists.refresh.readSets({projectCodes:this.form.projCodes, submissionStateCode:"NONE", stateCode:"A", includes:"code"});
					}
				},
				
				// fonction qui recupere objet configuration dont le code est saisi par utilisateur et qui en fonction
				// de config.strategy_internal_study determine si la variable internal_studies est à true ou false.
				displayStudies : function(){
					//if(this.form.configurationCode !== null && this.form.configurationCode !== undefined){
					if(this.form.projCodes && this.form.projCodes.length > 0){
						//get configuration
						$http.get(jsRoutes.controllers.sra.configurations.api.Configurations.get(this.form.configurationCode).url).success(function(data) {
							if(data.strategyStudy === 'STRATEGY_CODE_STUDY'){
								createService.internalStudies=true;
								createService.externalStudies=false;
							}else{
								createService.internalStudies=false;
								createService.externalStudies=true;	
							}
						});
					}
				},
				

				// methode appelee pour remplir le tableau des soumissions 
				search : function(){
					//$scope.messages = messages();
					this.datatable.search({projCodes:this.form.projCodes, stateCode:'SUB-N'});
				},
				/**
				 * initialization of the service
				 */
				init : function($routeParams, submissionDTConfig){
					initListService();
					
					//to avoid to lost the previous search
					if(submissionDTConfig && angular.isUndefined(mainService.getDatatable())){
						createService.datatable = datatable(submissionDTConfig);
						mainService.setDatatable(createService.datatable);
						createService.datatable.setColumnsConfig(getColumns());		
					}else if(angular.isDefined(mainService.getDatatable())){
						createService.datatable = mainService.getDatatable();	
						if(this.form.projCodes && this.form.projCodes.length > 0){
							this.datatable.search({projCodes:this.form.projCodes, stateCode:'SUB-N'});
						}
					}	
					
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
	
	
]).factory('submissionsActivateService', ['$http', 'mainService', 'lists', 'datatable', 
	function($http, mainService, lists, datatable) {
	
	var getColumns = function(){
		var columns = [];
		columns.push({  property:"traceInformation.creationDate",
			            header: Messages("traceInformation.creationDate"),
			        	type :"date",		    	  	
			        	order:true});		
		columns.push({	property:"code",
			    	  	header: Messages("submissions.code"),
			    	  	type :"text",		    	  	
			    	  	order:true});
		columns.push({	property:"state.code",
						"filter":"codes:'state'",
						header: Messages("submissions.state"),
						type :"text",
						order:true});	
		return columns;
	};
		
	var isInit = false;
	
	var initListService = function(){
		if(!isInit){
			activateService.lists.refresh.projects();
			isInit=true;
		}
	};
	
	var activateService = {
			isRouteParam : false,
			lists : lists,
			form : undefined,
			datatable : undefined,
			
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
			

			// methode appelee pour remplir le tableau des soumissions 
			search : function(){
				// $scope.messages = messages(); geré par la methode appelante
				console.log("activateService.search:projCodes " + this.form.projCodes);	
				this.datatable.search({projCodes:this.form.projCodes, stateCode:'SUB-V'});
			},
			cancel : function(){
				this.datatable.setData([],0);
			},
			//
			// initialization of the service
			 //
			init : function($routeParams, submissionDTConfig){
				initListService();
				
				//to avoid to lost the previous search
				if(submissionDTConfig && angular.isUndefined(mainService.getDatatable())){
					activateService.datatable = datatable(submissionDTConfig);
					mainService.setDatatable(activateService.datatable);
					activateService.datatable.setColumnsConfig(getColumns());		
				}else if(angular.isDefined(mainService.getDatatable())){
					activateService.datatable = mainService.getDatatable();			
				}	
				
				//to avoid to lost the previous search
				if(angular.isDefined(mainService.getForm())){
					activateService.form = mainService.getForm();
				}else{
					activateService.resetForm();						
				}
				
				if(angular.isDefined($routeParams)){
					this.setRouteParams($routeParams);
				}
			}
	};
	return activateService;
	}


//]).factory('submissionsConsultationService', ['$http', 'mainService', 'lists', 'datatable', 'toolsService',
//	function($http, mainService, lists, datatable, toolsService) {
//	
//	var getSubmissionColumns = function(){
//		var columns = [];
//		columns.push({  property:"traceInformation.creationDate",
//			        	header: Messages("traceInformation.creationDate"),
//			        	type :"date",		    	  	
//			        	order:true});
//		columns.push({  property:"creationUser",
//						header: Messages("submissions.creationUser"),
//						type :"text",		    	  	
//						order:true});
//		columns.push({	property:"code",
//			    	  	header: Messages("submissions.code"),
//			    	  	type :"text",		    	  	
//			    	  	order:true});
//		columns.push({	property:"projectCodes",
//    	  				header: Messages("submissions.projectCodes"),
//    	  				type :"text",		    	  	
//    	  				order:true});		
//		columns.push({	property:"type",
//						header: Messages("submissions.type"),
//						type :"text",		    	  	
//						order:true});		
//		columns.push({	property:"accession",
//			    	  	header: Messages("submissions.accession"),
//			    	  	type :"text",		    	  	
//			    	  	order:true});		
//		columns.push({	property:"state.code",
//						"filter":"codes:'state'",
//						header: Messages("submissions.state"),
//						type :"text",
//						order:true});	
////		columns.push({	property:"creationDate",
////						header: Messages("submissions.creationDate"),
////						type :"Date",
////						order:true});	
//										
//		return columns;
//	};
//
//	var isInit = false;
//	
//	
//	var initListService = function(){	
//		if(!isInit){
//			lists.refresh.projects();
//			/* initialisation de la variable consultationService.sraVariables.state utilisée dans consultation.scala.html
//			
//			$http.get(jsRoutes.controllers.sra.api.Variables.get('status').url)
//			.success(function(data) {
//			//initialisation de la variable consultationService.sraVariables.state utilisée dans consultation.scala.html
//			consultationService.sraVariables.state = data;	
//			console.log("state " + data);																					
//			});	*/	
//			
//			lists.refresh.states({objectTypeCode:"SRASubmission"});
//			$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'simplifiedStates'}})
//			.success(function(data) {
//				consultationService.sraVariables.simplifiedStates = data;	
//				console.log("Dans initListService, consultationService.sraVariables.simplifiedStates :" , consultationService.sraVariables.simplifiedStates);
//			});	
//
//			$http.get(jsRoutes.controllers.sra.api.Variables.get('pseudoStateCodeToStateCodes').url)
//			.success(function(data) {
//				consultationService.sraVariables.pseudoStateCodeToStateCodes = data;
//				console.log("Dans initListService, avec get sans code, consultationService.sraVariables.pseudoStateCodeToStateCodes" , consultationService.sraVariables.pseudoStateCodeToStateCodes);
//			});
//			//lists.refresh.states({objectTypeCode:"SRASubmission"},"SRASubmissionState");
//			isInit=true;
//		}
//	};
//	
//	
//			
//	var consultationService = {
//			isRouteParam : false,
//			lists : lists,
//			sraVariables : {},
//			form : undefined,
//			datatable : undefined,
//			isValidation : false,
//			
//			// methode appelee pour remplir le tableau des submissions
//			// Recherche toutes les submissions pour projCode indiqué :
//			search : function(){
//				//$scope.messages = messages();
//				console.log("consultationService.search :projCode " + this.form.projCode);	
//				console.log("consultationService.search :state !!!!!'" + this.form.state+"'");
//				//console.log("consultationService:state " + sraVariables.state);	
//									
//				console.log("consultationService:state " + this.form.state);
//				
//				/*if (this.form.state!==null && this.form.state !== undefined){
//					this.datatable.search({projCode:this.form.projCode, state : this.form.state});
//									//this.datatable.search({projCodes:this.form.projCodes});
//				} else {
//					this.datatable.search({projCode:this.form.projCode});
//				}*/
//				//this.datatable.search({projCodes:this.form.projCodes, accessions:this.form.accessions, codes:this.form.codes, accessionRegex:this.form.accessionRegex, codeRegex:this.form.codeRegex});
//				toolsService.replacePseudoStateCodesToStateCodesInFormulaire(consultationService.sraVariables.pseudoStateCodeToStateCodes, this.form);
//				this.datatable.search(this.form);
//			},	
//			cancel : function(){
//				this.datatable.setData([],0);
//			},
//			
//			resetForm : function(){
//				this.form = {};	
//				//$scope.messages = messages();
//			},
//			
//			// important pour avoir le menu permettant d'epingler : 
//			setRouteParams:function($routeParams){
//					var count = 0;
//					for(var p in $routeParams){
//						count++;
//						break;
//					}
//					if(count > 0){
//						this.isRouteParam = true;
//						this.form = $routeParams;
//					}
//				},
//				
//			//
//			// initialization of the service
//			 //
//			init : function($routeParams, submissionDTConfig){
//				initListService();
//				
//				//to avoid to lost the previous search
//				if(submissionDTConfig && angular.isUndefined(mainService.getDatatable())){
//					consultationService.datatable = datatable(submissionDTConfig);
//					mainService.setDatatable(consultationService.datatable);
//					consultationService.datatable.setColumnsConfig(getSubmissionColumns());		
//				}else if(angular.isDefined(mainService.getDatatable())){
//					consultationService.datatable = mainService.getDatatable();			
//				}	
//				
//				//to avoid to lost the previous search
//				if(angular.isDefined(mainService.getForm())){
//					consultationService.form = mainService.getForm();
//					
//				}else{
//					consultationService.resetForm();						
//				}
//				
//				if(angular.isDefined($routeParams)){
//					this.setRouteParams($routeParams);
//				}
//			}
//	};
//	return consultationService;
//}
]);

 