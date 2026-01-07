// NGL-3741 ajout group:true + groupMethods
"use strict";
 
 angular.module('ngl-bi.AnalysesServices', []).
	factory('analysisSearchService', ['$http', '$filter', '$parse', 'mainService', 'lists', 'datatable', 'treatments', function($http, $filter, $parse, mainService, lists, datatable, treatments){
		const CONSTS = treatments.CONSTS;
		var getColumns = function(){
			var columns = [];
			columns.push({	"property":"code",
							"header":Messages("analyses.code"),
							"type":"text",
							"order":true,
							"position":1,
							"groupMethod":"countDistinct"
			});
			columns.push({	"property":"typeCode",
							"filter":"codes:'type'",
							"header":Messages("analyses.typeCode"),
							"type":"text",
							"order":true,
							"position":2,
							"group":true,
							"groupMethod":"collect:true"
			});
			columns.push({	"property":"masterReadSetCodes",
							"header":Messages("analyses.masterReadSetCodes"),
							"type":"text",
							"render":"<div list-resize='cellValue' list-resize-min-size='3'vertical>",
							"position":3,
							"groupMethod":"collect:true"
			});					
			columns.push({	"property":"projectCodes",
							"header":Messages("analyses.projectCodes"),
							"type":"text",
							"render":"<div list-resize='cellValue' list-resize-min-size='3'vertical>",
							"position":4,
							"group":true,
							"groupMethod":"collect:true" //AJOUT
			});
			columns.push({	"property":"sampleCodes",
							"header":Messages("analyses.sampleCodes"),
							"type":"text",
							"render":"<div list-resize='cellValue' list-resize-min-size='3'vertical>",
							"position":5,
							"groupMethod":"collect:true"
			});
			if(!mainService.isHomePage('state')){
				columns.push({	"property":"state.code",
								"filter":"codes:'state'",
								"header":Messages("analyses.state.code"),
								"type":"text",
								"order":true,
								"position":6
				});
			}else{
				columns.push({	"property":"state.code",
								"filter":"codes:'state'",
								"header":Messages("analyses.state.code"),
								"type":"text",
								"edit":true,
								"order":true,
						    	"choiceInList":true,
						    	"listStyle":'bt-select',
								"possibleValues":'searchService.lists.getStates()',
								"position":6
				});
			}
			if(!mainService.isHomePage('valuation')){
				columns.push({	"property":"valuation.valid",
								"filter":"codes:'valuation'",
								"header":Messages("analyses.valuation.valid"),
								"type":"text",
								"order":true,
								"position":7
				});
				columns.push({	"property":"valuation.resolutionCodes",
								"header":Messages("analyses.valuation.resolutions"),
								"render":'<div bt-select ng-model="value.data.valuation.resolutionCodes" bt-options="valid.code as valid.name group by valid.category.name for valid in searchService.lists.getResolutions()" ng-edit="false"></div>',
								"type":"text",
								"hide":true,
								"position":8
				});
				columns.push({	"property":"traceInformation.creationDate",
								"header":Messages("analyses.valuation.creationDate"),
								"type":"date",
								"order":true,
								"hide":true,
								"position":9
				});
			}else{
				columns.push({	"property":"valuation.valid",
								"filter":"codes:'valuation'",
								"header":Messages("analyses.valuation.valid"),
								"type":"text",
								"order":true,
								"edit":true,
								"choiceInList":true,
								"listStyle":'bt-select',
								"possibleValues":'searchService.lists.getValuations()',
								"position":7
				});
				columns.push({	"property":"valuation.criteriaCode",
								"filter":"codes:'valuation_criteria'",
								"header":Messages("analyses.valuation.criteria"),
								"type":"text",
								"edit":true,
								"choiceInList":true,
								"listStyle":'bt-select',
								"possibleValues":'searchService.lists.getValuationCriterias()',
								"position":8
				});
				columns.push({	"property":"valuation.resolutionCodes",
								"header":Messages("analyses.valuation.resolutions"),
								"render":'<div bt-select ng-model="value.data.valuation.resolutionCodes" bt-options="valid.code as valid.name group by valid.category.name for valid in searchService.lists.getResolutions()" ng-edit="false"></div>',
								"type":"text",
								"edit":true,
								"choiceInList":true,
						    	"listStyle":'bt-select-multiple',
						    	"possibleValues":'searchService.lists.getResolutions()',
								"groupBy":'category.name',
								"position":9
				});
			}					
			return columns;
		};
		
		
		var isInit = false;
		
		var initListService = function(){
			if(!isInit){
				searchService.lists.refresh.projects();
				searchService.lists.refresh.states({objectTypeCode:"Analysis", display:true},'statetrue');
				searchService.lists.refresh.states({objectTypeCode:"Analysis"});
				searchService.lists.refresh.types({objectTypeCode:"Analysis"});
				searchService.lists.refresh.resolutions({objectTypeCode:"Analysis"});
				searchService.lists.refresh.valuationCriterias({objectTypeCode:"Analysis"});
				searchService.lists.refresh.reportConfigs({pageCodes:["analysis"+"-"+mainService.getHomePage()], active:true});
				searchService.lists.refresh.reportConfigs({pageCodes:["analysis-addcolumns"]}, "analysis-addcolumns");
				searchService.lists.refresh.filterConfigs({pageCodes:["analysis-addfilters"]}, "analysis-addfilters");
				searchService.lists.refresh.users();  // FDS pas de liste users ici pas necessaire  !!!
				searchService.lists.refresh.umbrellaProjects();  // FDS pas de liste UmbrellaProjects ici pas necessaire !!
				// NGL-5241
				searchService.lists.refresh.context({categoryCode:"analysis", typeCode:'context-description'});
				isInit=true;
			}
		};
		
		var searchService = {
				getColumns:getColumns,
				datatable:undefined,
				isRouteParam : false,
				lists : lists,
				form : undefined,
				reportingConfigurationCode:undefined,
				reportingConfiguration:undefined,
				additionalFilters:[],
				additionalColumns:[],
				additionalColumnsContext:[],
				contextValue:undefined,
				mapAdditionnalColumn : new Map(),
				selectedAddColumns:[],
			
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
				
				updateForm : function(){
					
					if (mainService.isHomePage('valuation')) {
						if(!this.isRouteParam && (this.form.stateCodes === undefined || this.form.stateCodes.length === 0)) {
							//No stateCodes selected, the filter by default (on the only two possible states for the valuation) is applied
							this.form.stateCodes = ["IW-V"];
						}		
					}

					if (this.reportingConfiguration){
						for (var i = 0 ; i < this.reportingConfiguration.columns.length ; i++){
							if (this.reportingConfiguration.columns[i].queryIncludeKeys && this.reportingConfiguration.columns[i].queryIncludeKeys.length > 0){
								this.form.includes = this.form.includes.concat(this.reportingConfiguration.columns[i].queryIncludeKeys);
						  	} else {
								// NGL-4785
								if(this.form.includes == null){
									this.form.includes = [];
								}
					  			this.form.includes.push(this.reportingConfiguration.columns[i].property.replace('.value',''));	
						  	}
						}
						this.form.excludes = [];
					} else{
						this.form.includes = ["default"];
					}

					// NGL-4771 FDS ajouté a l'identique de readsets....
					for(var i = 0 ; i < this.selectedAddColumns.length ; i++){
						//remove .value if present to manage correctly properties (single, list, etc.)
						if(this.selectedAddColumns[i].queryIncludeKeys && this.selectedAddColumns[i].queryIncludeKeys.length > 0){
							this.form.includes = this.form.includes.concat(this.selectedAddColumns[i].queryIncludeKeys);
						}else{
							this.form.includes.push(this.selectedAddColumns[i].property.replace('.value',''));	
						}
					}

					// Ancien code :
					/* 
						if(this.reportingConfiguration && this.reportingConfiguration.queryConfiguration){
							var queryParams = this.reportingConfiguration.queryConfiguration;
							if(queryParams && queryParams.includeKeys && queryParams.includeKeys.length > 0){
								this.form.includes = queryParams.includeKeys;
							}else if(queryParams && queryParams.excludeKeys && queryParams.excludeKeys.length > 0) {
								this.form.excludes = queryParams.excludeKeys;
							}
						}
					*/
				},
				
				resetForm : function(){
					this.form = {};									
				},

				resetTextareas : function(){
					Array.from(document.getElementsByTagName('textarea')).forEach(function(element) {
						var elementScope = angular.element(element).scope();
						if(elementScope.textareaValue){
							elementScope.textareaValue = null;
						}
					}); 
				},
				
				resetSampleCodes : function(){
					this.form.sampleCodes = [];									
				},
				
				search : function(){
					this.updateForm();
					mainService.setForm(this.form);
					this.datatable.search(this.form);
				},
				
				refreshSamples : function(){
					if(this.form.projectCodes && this.form.projectCodes.length > 0){
						this.lists.refresh.samples({projectCodes:this.form.projectCodes});
					}
				},
				
				valuationStates : [{code:"IW-V",name:Codes("state.IW-V")}],
				states : function(){
					if (mainService.isHomePage('valuation')) {
						return this.valuationStates;
					}else{
						return this.lists.get('statetrue');
					}
				},
				
				/**
				 * Update column when change reportingConfiguration
				 */
				updateColumn : function(){
					if(this.reportingConfigurationCode){
						$http.get(jsRoutes.controllers.reporting.api.ReportingConfigurations.get(this.reportingConfigurationCode).url,{searchService:this, datatable:this.datatable})
								.success(function(data, status, headers, config) {
									config.searchService.reportingConfiguration = data;
									// NGL-4785 : on souhaite recherche uniquement si données dans datatable
									if(config.searchService.lists.get('reportConfigs').length > 1 && config.datatable.isData()){
										config.searchService.search();
									}
									config.datatable.setColumnsConfig(data.columns);																								
						});
					}else{
						this.reportingConfiguration = undefined;
						this.datatable.setColumnsConfig(this.getColumns());
						if(this.datatable.isData()){
							this.search();
						}
						//this.search();
					}
					
				},
				
				initAdditionalColumns:function(){
					this.additionalColumns=[];
					this.selectedAddColumns=[];
					this.additionalColumnsContext=[];
					this.contextValue=undefined;
					this.mapAdditionnalColumn=new Map();
					
					if(lists.get("analysis-addcolumns") && lists.get("analysis-addcolumns").length === 1){
						var formColumns = [];
						//NGL-5241
						//var allColumns = angular.copy(lists.get("analysis-addcolumns")[0].columns);
						var allColumns = this.computeMapAdditionnalColumns();

						var nbElementByColumn = Math.ceil(allColumns.length / 5); //5 columns
						for(var i = 0; i  < 5 && allColumns.length > 0 ; i++){
							formColumns.push(allColumns.splice(0, nbElementByColumn));	    								
						}
						//complete to 5 five element to have a great design 
						while(formColumns.length < 5){
							formColumns.push([]);
						}
						this.additionalColumns = formColumns;
					}
				},

				// NGL-5241 ajout
				computeMapAdditionnalColumns:function(){
					var allColumns = angular.copy(lists.get("analysis-addcolumns")[0].columns);
					var allColumnsFiltered = [];
					for(var i=0; i<allColumns.length; i++){
						if(allColumns[i].groupHeader==undefined){
							allColumnsFiltered.push(allColumns[i]);
						}else{
							if(this.mapAdditionnalColumn.get(allColumns[i].groupHeader)==undefined){
								//if(this.contextValue==undefined || (this.contextValue!=undefined && allColumns[i].context!=null && allColumns[i].context.includes(this.contextValue))){
									allColumnsFiltered.push(allColumns[i]);
									var tabColumn=[];
									tabColumn.push(allColumns[i]);
									this.mapAdditionnalColumn.set(allColumns[i].groupHeader,tabColumn);
									//}
							}else{
								this.mapAdditionnalColumn.get(allColumns[i].groupHeader).push(allColumns[i]);
							}
						}
					}
					return allColumnsFiltered;
				},

				// NGL-5241 ajout
				updateAdditionnalColumnContext: function(){
					if(this.contextValue!=undefined){
						var filteredColumn = [];
						for(var i = 0 ; i < this.additionalColumns.length ; i++){
							for(var j = 0; j < this.additionalColumns[i].length; j++){
								if(this.additionalColumns[i][j].context!=null && this.additionalColumns[i][j].context.includes(this.contextValue)){
									filteredColumn.push(this.additionalColumns[i][j]);
								}
							}
						}
						var formColumns = [];
						var nbElementByColumn = Math.ceil(filteredColumn.length / 5); //5 columns
						for(var i = 0; i  < 5 && filteredColumn.length > 0 ; i++){
							formColumns.push(filteredColumn.splice(0, nbElementByColumn));	    								
						}
						//complete to 5 five element to have a great design 
						while(formColumns.length < 5){
							formColumns.push([]);
						}
						this.additionalColumnsContext=formColumns;
					}
				},
				getAddColumnsToForm : function(){
					// NGL-5241
					if(this.contextValue!=undefined && this.additionalColumnsContext.length>0){
						return this.additionalColumnsContext;
					}else{
						if(this.additionalColumns.length === 0){
							this.initAdditionalColumns();
						}
						return this.additionalColumns;
					}								
				},
				
				addColumnsToDatatable:function(){
					this.selectedAddColumns = [];

					for(var i = 0 ; i < this.additionalColumns.length ; i++){
						for(var j = 0; j < this.additionalColumns[i].length; j++){
							if(this.additionalColumns[i][j].select){
								// NGL-5254
								//this.selectedAddColumns.push(this.additionalColumns[i][j]);
								if(this.additionalColumns[i][j].groupHeader!=undefined){
									for(var c=0; c<this.mapAdditionnalColumn.get(this.additionalColumns[i][j].groupHeader).length; c++){
										this.selectedAddColumns.push(this.mapAdditionnalColumn.get(this.additionalColumns[i][j].groupHeader)[c]);
									}
								}else{
									this.selectedAddColumns.push(this.additionalColumns[i][j]);
								}
							}
						}
					}
					if(this.reportingConfigurationCode){
						this.datatable.setColumnsConfig(this.reportingConfiguration.columns.concat(this.selectedAddColumns));
					}else{
						this.datatable.setColumnsConfig(this.getColumns().concat(this.selectedAddColumns));						
					}
					this.search();
				},
				resetDatatableColumns:function(){
					this.initAdditionalColumns();
					this.datatable.setColumnsConfig(this.getColumns());
					this.search();
				},
				
				initAdditionalFilters:function(){
					this.additionalFilters=[];
					
					if(lists.get("analysis-addfilters") && lists.get("analysis-addfilters").length === 1){
						var formFilters = [];
						var allFilters = angular.copy(lists.get("analysis-addfilters")[0].filters);
						
						var nbElementByColumn = Math.ceil(allFilters.length / 5); //5 columns
						for(var i = 0; i  < 5 && allFilters.length > 0 ; i++){
							formFilters.push(allFilters.splice(0, nbElementByColumn));	    								
						}
						//complete to 5 five element to have a great design 
						while(formFilters.length < 5){
							formFilters.push([]);
						}
							
						this.additionalFilters = formFilters;
					}
				},
				
				getAddFiltersToForm : function(){
					if(this.additionalFilters.length === 0){
						this.initAdditionalFilters();
					}
					return this.additionalFilters;									
				},	
				
				/**
				 * initialization of the service
				 */
				init : function($routeParams, datatableConfig){
					initListService();
					
					datatableConfig.messages = {
							transformKey: function(key, args) {
		                        return Messages(key, args);
		                    }
					};

					$http.get(jsRoutes.controllers.commons.api.Values.list().url, { params: { propertyDefinitionCode: "state" } }).success(function(data) { 
						searchService.stateProperties = CONSTS.WORKFLOWS.GENOME_DENOVO_ASSEMBLY.map(function(code) {return { code: code };});
					});
					
					//to avoid to lost the previous search
					if(datatableConfig && angular.isUndefined(mainService.getDatatable())){
						searchService.datatable = datatable(datatableConfig);
						mainService.setDatatable(searchService.datatable);
						searchService.datatable.setColumnsConfig(getColumns());		
					}else if(angular.isDefined(mainService.getDatatable())){
						searchService.datatable = mainService.getDatatable();			
					}	
					
					if(angular.isDefined(mainService.getForm())){
						searchService.form = mainService.getForm();
					}else{
						searchService.resetForm();						
					}
					
					if(angular.isDefined($routeParams)){
						this.setRouteParams($routeParams);
					}
				}
		};
		
		return searchService;
	}
]);
 