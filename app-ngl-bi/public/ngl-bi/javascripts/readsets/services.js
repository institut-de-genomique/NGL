 "use strict";
 
 angular.module('ngl-bi.ReadSetsServices', []).
	factory('readSetsSearchService', ['$http', 'mainService', 'lists', 'datatable', function($http, mainService, lists, datatable){
		
		var getDefaultColumns = function(){
			var columns = [];
			
			columns.push({	property:"code",
							"header":Messages("readsets.code"),
				    	  	type :"text",		    	  	
				    	  	order:true,
				    	  	groupMethod:"count",
				    	  	position:1});
			columns.push({	property:"runCode",
							header: Messages("readsets.runCode"),
							type :"text",
							group:true,
							groupMethod:"collect:true",
							order:true,
							render:"<div list-resize='cellValue' list-resize-min-size='3' vertical>",
				    	  	position:2});
			columns.push({	property:"laneNumber",
							header: Messages("readsets.laneNumber"),
							type :"text",
							order:true,
				    	  	position:3});
			columns.push({	property:"projectCode",
							header: Messages("readsets.projectCode"),
							type :"text",
							order:true,
							group:true,
							groupMethod:"countDistinct",
				    	  	position:4});			
			columns.push({	property:"sampleCode",
							header: Messages("readsets.sampleCode"),
							type :"text",
							group:true,
							groupMethod:"countDistinct",
							order:true,
				    	  	position:5});
			columns.push({	property:"runSequencingStartDate",
							header: Messages("runs.sequencingStartDate"),
							type :"date",
							order:true,
							position:6,
							groupMethod:"unique",
			});
			if(mainService.getHomePage() == 'search'){
					columns.push({	"property":"state.code",
									"filter":"codes:'state'",
									"header":Messages("readsets.stateCode"),
									"type":"text",
									"order":true,
									"position":7
					});
					
					columns.push({	"property":"productionValuation.valid",
									"filter":"codes:'valuation'",
									"header":Messages("readsets.productionValuation.valid"),
									"type":"text",
									"order":true,
									"position":70
					});
					
					columns.push({	"property":"productionValuation.resolutionCodes",
									"header":Messages("readsets.productionValuation.resolutions"),
									"filter":"codes:'resolution'",
									"groupMethod":"collect:true",
									"render":'<div bt-select ng-model="cellValue" bt-options="valid.name as valid.name group by valid.category.name for valid in searchService.lists.getResolutions()" ng-edit="false"></div>',
									"type":"text",
									"hide":true,
									"position":72
					});
					
					columns.push({	"property":"bioinformaticValuation.valid",
									"filter":"codes:'valuation'",
									"header":Messages("readsets.bioinformaticValuation.valid"),
									"type":"text",
									"order":true,
									"position":80
					});
					
					columns.push({	"property":"bioinformaticValuation.resolutionCodes",
									"header":Messages("readsets.bioinformaticValuation.resolutions"),
									"filter":"codes:'resolution'",
									"groupMethod":"collect:true",
									"render":'<div bt-select ng-model="cellValue" bt-options="valid.name as valid.name group by valid.category.name for valid in searchService.lists.getResolutions()" ng-edit="false"></div>',
									"type":"text",
									"hide":true,
									"position":82
					});
			}else if(mainService.getHomePage() == 'valuation'){
					columns.push({	"property":"state.code",
									"filter":"codes:'state'",
									"header":Messages("readsets.stateCode"),
									"type":"text",
									"order":true,
									"position":7
					});
					
					columns.push({	"property":"productionValuation.valid",
									"filter":"codes:'valuation'",
									"header":Messages("readsets.productionValuation.valid"),
									"type":"text",
									"order":true,
									"edit":true,
									"choiceInList":true,
									"listStyle":'bt-select',
							    	"possibleValues":'searchService.lists.getValuations()',
							    	"position":70
					});
					
					columns.push({	"property":"productionValuation.criteriaCode",
									"filter":"codes:'valuation_criteria'",
									"header":Messages("readsets.productionValuation.criteria"),
									"type":"text",
									"edit":true,
									"choiceInList":true,
									"listStyle":'bt-select',
							    	"possibleValues":'searchService.lists.getValuationCriterias()',
							    	"position":71
				    });
					
					columns.push({	"property":"productionValuation.resolutionCodes",
									"header":Messages("readsets.productionValuation.resolutions"),
									"render":'<div bt-select ng-model="value.data.productionValuation.resolutionCodes" bt-options="valid.code as valid.name group by valid.category.name for valid in searchService.lists.getResolutions()" ng-edit="false"></div>',
									"type":"text",
									"edit":true,
									"choiceInList":true,
									"listStyle":'bt-select-multiple',
							    	"possibleValues":'searchService.lists.getResolutions()',
							    	"groupBy":'category.name',
							    	"position":72
							    		
					});
					
					columns.push({	"property":"productionValuation.comment",
						"header":Messages("readsets.productionValuation.comment"),
						"type":"text",
						"edit":true,
						"position":73,
						"editTemplate" : "<textarea class='form-control' #ng-model rows='3'></textarea>"
					});
					
					columns.push({	"property":"bioinformaticValuation.valid",
									"filter":"codes:'valuation'",
									"header":Messages("readsets.bioinformaticValuation.valid"),
									"type":"text",
									"order":true,
									"edit":true,
									"choiceInList":true,
									"listStyle":'bt-select',
							    	"possibleValues":'searchService.lists.getValuations()',
							    	"position":80
					});	
					
					columns.push({	"property":"bioinformaticValuation.resolutionCodes",
									"header":Messages("readsets.bioinformaticValuation.resolutions"),
									"render":'<div bt-select ng-model="value.data.bioinformaticValuation.resolutionCodes" bt-options="valid.code as valid.name group by valid.category.name for valid in searchService.lists.getResolutions()" ng-edit="false"></div>',
									"type":"text",
									"edit":true,
									"choiceInList":true,
									"listStyle":'bt-select-multiple',
							    	"possibleValues":'searchService.lists.getResolutions()',
							    	"groupBy":'category.name',
							    	"position":82
					});
					
					
					
			}else if(mainService.getHomePage() == 'state'){
					columns.push({	"property":"state.code",
									"filter":"codes:'state'",
									"header":Messages("readsets.stateCode"),
									"type":"text",
									"edit":true,
									"order":true,
									"choiceInList":true,
									"listStyle":'bt-select',
							    	"possibleValues":'searchService.lists.getStates()',
							    	"position":7
					});
					
					columns.push({	"property":"productionValuation.valid",
									"filter":"codes:'valuation'",
									"header":Messages("readsets.productionValuation.valid"),
									"type":"text",
									"order":true,
									"position":70    	
					});
					
					columns.push({	"property":"bioinformaticValuation.valid",
									"filter":"codes:'valuation'",
									"header":Messages("readsets.bioinformaticValuation.valid"),
									"type":"text",
									"order":true,
									"position":80
					});
					
			}else if(mainService.getHomePage() == 'batch'){
					columns.push({	"property":"state.code",
									"filter":"codes:'state'",
									"header":Messages("readsets.stateCode"),
									"type":"text",
									"order":true,
							    	"choiceInList":true,
							    	"listStyle":'bt-select',
							    	"possibleValues":'searchService.lists.getStates()',
							    	"position":7
					});
					columns.push({	"property":"productionValuation.valid",
									"filter":"codes:'valuation'",
									"header":Messages("readsets.productionValuation.valid"),
									"type":"text",
									"order":true,
									"position":70    	
					});
					columns.push({	"property":"bioinformaticValuation.valid",
									"filter":"codes:'valuation'",
									"header":Messages("readsets.bioinformaticValuation.valid"),
									"type":"text",
									"order":true,
									"position":80
				    });
					columns.push({	"property":"location",
									"header":Messages("readsets.files.location"),
									"type":"text",
									"order":true,
									"position":90
					});
					columns.push({	"property":"properties.isSentCollaborator.value",
									"header":Messages("readsets.properties.isSentCollaborator"),
									"type":"boolean",
									"edit":true,
									"position":91
					});
			}
			
			return columns;
		};
		
		var isInit = false;
		
		var initListService = function(){
			if(!isInit){
				lists.refresh.projects();
				lists.refresh.states({objectTypeCode:"ReadSet", display:true},'statetrue');
				lists.refresh.states({objectTypeCode:"ReadSet"});			
				lists.refresh.valuationCriterias({objectTypeCode:"ReadSet", orderBy:'name'});
				lists.refresh.types({objectTypeCode:"Run"},"runTypes");
				lists.refresh.types({objectTypeCode:"ReadSet"},"readSetTypes");
				lists.refresh.runs();
				//NGL-3392 ajout mgi-sequencer
				lists.refresh.instruments({categoryCodes:["illumina-sequencer","extseq","nanopore-sequencer", "opt-map-bionano", "mgi-sequencer"]});
				//TODO Warn if pass to one application page
				lists.refresh.reportConfigs({pageCodes:["readsets"+"-"+mainService.getHomePage()]});
				lists.refresh.reportConfigs({pageCodes:["readsets-addcolumns"]}, "readsets-addcolumns");
				lists.refresh.filterConfigs({pageCodes:["readsets-addfilters"]}, "readsets-addfilters");
				
				lists.refresh.resolutions({objectTypeCode:"ReadSet"});
				lists.refresh.context({categoryCode:"readset", typeCode:'context-description'});
				
				lists.refresh.users();
				isInit=true;
			}
		};
		
		var searchService = {
				getDefaultColumns:getDefaultColumns,
				datatable:undefined,
				isRouteParam:false,
				lists : lists,
				form : undefined,
				reportingConfigurationCode:undefined,
				reportingConfiguration:undefined,
				additionalColumns:[],
				additionalColumnsContext:[],
				mapAdditionnalColumn : new Map(),
				mainFilters:[],
				additionalFilters:[],
				selectedAddColumns:[],
				contextValue:undefined,
				
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
					/*if (mainService.isHomePage('valuation')) {
						if(!this.isRouteParam && (this.form.stateCodes === undefined || this.form.stateCodes.length === 0)) {
							//No stateCodes selected, the filter by default (on the only two possible states for the valuation) is applied
							this.form.stateCodes = ["IW-VQC", "IP-VQC", "IW-VBA"];
						}		
					}*/
					this.form.includes = [];
					if(this.reportingConfiguration){
						for(var i = 0 ; i < this.reportingConfiguration.columns.length ; i++){
							if(this.reportingConfiguration.columns[i].queryIncludeKeys && this.reportingConfiguration.columns[i].queryIncludeKeys.length > 0){
								this.form.includes = this.form.includes.concat(this.reportingConfiguration.columns[i].queryIncludeKeys);
							}else{
								this.form.includes.push(this.reportingConfiguration.columns[i].property.replace('.value',''));	
							}
						}
					}else{
						this.form.includes = ["default"];
					}
					
					for(var i = 0 ; i < this.selectedAddColumns.length ; i++){
						//remove .value if present to manage correctly properties (single, list, etc.)
						if(this.selectedAddColumns[i].queryIncludeKeys && this.selectedAddColumns[i].queryIncludeKeys.length > 0){
							this.form.includes = this.form.includes.concat(this.selectedAddColumns[i].queryIncludeKeys);
						}else{
							this.form.includes.push(this.selectedAddColumns[i].property.replace('.value',''));	
						}
						
					}
					
					if(this.reportingConfiguration && this.reportingConfiguration.queryConfiguration 
							&& this.reportingConfiguration.queryConfiguration.query){
						this.form.reportingQuery = this.reportingConfiguration.queryConfiguration.query;
						this.form.reporting=true;
						this.form.aggregate=false;
						if(this.reportingConfiguration.queryConfiguration.type === 'aggregate'){
							this.form.aggregate=true;
						}
						
						for(var key in this.aggregateForm){
							//replace "#someThing" by "somethingValue" but in case of number does not work because number cannot be inside a double quote
							this.form.reportingQuery = this.form.reportingQuery.replace(new RegExp("#"+key,"g"),this.aggregateForm[key]);
						}						
					}else if(this.form.reportingQuery){
						this.form.reportingQuery.trim();
						if(this.form.reportingQuery.length > 0){
							this.form.reporting=true;
						}else{
							this.form.reporting=false;
						}
					}else{
						this.form.reporting=false;
					}
				},
				
				convertForm : function(){
					var _form = angular.copy(this.form);
					return _form
				},
				
				resetForm : function(){
					this.form = {};		
					if (mainService.isHomePage('valuation')) {
						if(!this.isRouteParam && (this.form.stateCodes === undefined || this.form.stateCodes.length === 0)) {
							//No stateCodes selected, the filter by default (on the only two possible states for the valuation) is applied
							this.form.stateCodes = ["IW-VQC", "IP-VQC", "IW-VBA"];
						}		
					}
				},

				resetTextareas : function(){
					Array.from(document.getElementsByTagName('textarea')).forEach(function (element) {
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
					this.datatable.search(this.convertForm());
					this.datatable.setMessagesActive(true);
				},
				
				refreshSamples : function(){
					if(this.form.projectCodes && this.form.projectCodes.length > 0){
						this.lists.refresh.samples({projectCodes:this.form.projectCodes});
					}
				},
				valuationStates : [{code:"IW-VQC",name:Codes("state.IW-VQC")},{code:"IP-VQC",name:Codes("state.IP-VQC")},{code:"IW-VBA",name:Codes("state.IW-VBA")}],
				states : function(){
					//if (mainService.isHomePage('valuation')) {
					//	return this.valuationStates;
					//}else{
						return this.lists.get('statetrue');
					//}
				},
				
				
				initAdditionalColumns:function(){
					this.additionalColumns=[];
					this.selectedAddColumns=[];
					this.additionalColumnsContext=[];
					this.contextValue=undefined;
					this.mapAdditionnalColumn=new Map();
					
					if(lists.get("readsets-addcolumns") && lists.get("readsets-addcolumns").length === 1){
						var formColumns = [];
						//var allColumns = angular.copy(lists.get("readsets-addcolumns")[0].columns);
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
				
				computeMapAdditionnalColumns:function(){
					var allColumns = angular.copy(lists.get("readsets-addcolumns")[0].columns);
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
						this.datatable.setColumnsConfig(this.getDefaultColumns().concat(this.selectedAddColumns));						
					}
					if(this.datatable.isData()){
						this.search();
					}
				},	
				
				resetDatatableColumns:function(){
					this.updateColumn();
					if(this.datatable.isData()){
						this.search();
					}
				},
				
				/**
				 * Update column when change reportingConfiguration
				 */
				updateColumn : function(){
					this.initAdditionalColumns();
					if(this.reportingConfigurationCode){
						$http.get(jsRoutes.controllers.reporting.api.ReportingConfigurations.get(this.reportingConfigurationCode).url,{searchService:this, datatable:this.datatable})
								.success(function(data, status, headers, config) {
									
									config.searchService.reportingConfiguration = data;
									if(config.searchService.lists.get('reportConfigs').length > 1 && config.datatable.isData()){
										config.searchService.search();
									}
									config.datatable.setColumnsConfig(data.columns);
									config.searchService.mainFilters = config.searchService.organizeFilters(data.filters);								
						});
					}else{
						
						this.reportingConfiguration = undefined;
						this.initAdditionalFilters();
						this.mainFilters = [];							
						this.datatable.setColumnsConfig(this.getDefaultColumns());
						if(this.datatable.isData()){
							this.search();
						}
					}
					
				},
				initAdditionalFilters:function(){
					this.additionalFilters=[];
					
					if(lists.get("readsets-addfilters") && lists.get("readsets-addfilters").length === 1){
						var allFilters = angular.copy(lists.get("readsets-addfilters")[0].filters);
						
						/* add static filters*/
						allFilters.push({property:"fromEvalDate",html:"<input type='text' class='form-control' ng-model='searchService.form.fromEvalDate' placeholder='"+Messages("search.placeholder.fromEvalDate")+"' title='"+Messages("search.placeholder.fromEvalDate")+"' date-timestamp>",position:allFilters.length+1});
						allFilters.push({property:"toEvalDate",html:'<input type="text" class="form-control" ng-model="searchService.form.toEvalDate" placeholder="'+Messages("search.placeholder.toEvalDate")+'" title="'+Messages("search.placeholder.toEvalDate")+'" date-timestamp>',position:allFilters.length+1});
						
						this.additionalFilters = this.organizeFilters(allFilters);					
					}
				},
				
				
				organizeFilters : function(allFilters){
					if(allFilters !== undefined && allFilters !== null && allFilters.length > 0){
					
						var formFilters = [];
						var nbElementByColumn = Math.ceil(allFilters.length / 5); //5 columns
						for(var i = 0; i  < 5 && allFilters.length > 0 ; i++){
							formFilters.push(allFilters.splice(0, nbElementByColumn));	    								
						}
						//complete to 5 five element to have a great design 
						while(formFilters.length < 5){
							formFilters.push([]);
						}
							
						return formFilters;
					}else{
						return [];
					}
				},
				getAddFiltersToForm : function(){
					if(this.additionalFilters.length === 0){
						this.initAdditionalFilters();
					}
					return this.additionalFilters;									
				},
				
				getMainFiltersToForm:function(){
					return this.mainFilters;	
				},
				/**
				 * initialise the service
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
						searchService.datatable.setColumnsConfig(getDefaultColumns());		
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
 