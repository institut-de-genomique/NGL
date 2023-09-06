// NGL-3741 ajout group:true + groupMethods
"use strict";
 
 angular.module('ngl-bi.AnalysesServices', []).
	factory('analysisSearchService', ['$http', '$filter', '$parse', 'mainService', 'lists', 'datatable', function($http, $filter, $parse, mainService, lists, datatable){
		
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
				
				lists.refresh.valuationCriterias({objectTypeCode:"Analysis"});
				
				searchService.lists.refresh.reportConfigs({pageCodes:["analysis"+"-"+mainService.getHomePage()]});
				searchService.lists.refresh.filterConfigs({pageCodes:["analysis-addfilters"]}, "analysis-addfilters");
				searchService.lists.refresh.reportConfigs({pageCodes:["analysis-addcolumns"]}, "analysis-addcolumns");
				searchService.lists.refresh.users();
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
					  			this.form.includes.push(this.reportingConfiguration.columns[i].property.replace('.value',''));	
						  	}
						}
					} else{
						this.form.includes = ["default"];
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
									config.searchService.search();
									config.datatable.setColumnsConfig(data.columns);																								
						});
					}else{
						this.reportingConfiguration = undefined;
						this.datatable.setColumnsConfig(this.getColumns());
						this.search();
					}
					
				},
				
				initAdditionalColumns:function(){
					this.additionalColumns=[];
					this.selectedAddColumns=[];
					
					if(lists.get("analysis-addcolumns") && lists.get("analysis-addcolumns").length === 1){
						var formColumns = [];
						var allColumns = angular.copy(lists.get("analysis-addcolumns")[0].columns);
						
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
				
				getAddColumnsToForm : function(){
					if(this.additionalColumns.length === 0){
						this.initAdditionalColumns();
					}
					return this.additionalColumns;									
				},
				
				addColumnsToDatatable:function(){
					//this.reportingConfiguration = undefined;
					//this.reportingConfigurationCode = undefined;
					
					this.selectedAddColumns = [];
					for(var i = 0 ; i < this.additionalColumns.length ; i++){
						for(var j = 0; j < this.additionalColumns[i].length; j++){
							if(this.additionalColumns[i][j].select){
								this.selectedAddColumns.push(this.additionalColumns[i][j]);
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
 