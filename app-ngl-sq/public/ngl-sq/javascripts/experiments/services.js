 "use strict";
 
 angular.module('ngl-sq.experimentsServices', []).
	factory('experimentsSearchService', ['$http', 'mainService', 'lists', 'datatable', 
		                         function($http,   mainService,   lists,   datatable){
		var getColumns = function(){
			var columns = [];
			
			columns.push({
			            	 "header":Messages("experiments.table.typeCode"),
			            	 "property":"typeCode",
			            	 "filter":"codes:'type'",
			            	 "order":true,
			            	 "hide":true,
			            	 "position":1,
			            	 "type":"text"
			            	 
			});
			columns.push({
							"header":Messages("experiments.table.code"),
							"property":"code",
							"order":true,
							"hide":false,
							"position":2,
							"type":"text",
							"groupMethod":"count:true"
			});
			columns.push({
							"header":Messages("experiments.intrument"),
							"property":"instrument.code",
							"order":true,
							"hide":true,
							"position":3,
							"type":"text",
							"filter":"codes:'instrument'",
							"groupMethod":"collect:true"
			});
			columns.push({
							"header":Messages("experiments.table.projectCodes"),
							"property":"projectCodes",
							"order":false,
							"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
							"hide":true,
							"position":7,
							"type":"text",
							"filter":"unique",
							"groupMethod":"collect:true"
			});
			columns.push({
							"header":Messages("containers.table.sampleCodes"),
							"property":"sampleCodes",
							"order":false,
							"hide":true,
							"position":9,
							"type":"text",
							"render":"<div list-resize='value.data.sampleCodes | unique' list-resize-min-size='3'>"					
			});
			columns.push({
							"header":Messages("experiments.table.creationDate"),
							"property":"traceInformation.creationDate",
							"order":true,
							"hide":true,
							"position":11,
							"type":"date"
			});
			columns.push({
							"header":Messages("experiments.table.createUser"),
							"property":"traceInformation.createUser",
							"order":true,
							"hide":true,
							"position":12,
							"type":"text"
			});
			
			if(mainService.getHomePage() === 'reagents'){
				
				columns.push({
		        	 "header":Messages("reagents.table.kitname"),
		        	 "property":"reagents[0].kitCatalogCode",
		        	 "filter":"codes:'reagentKit'",
		        	 "order":true,
		        	 "type":"text",
		        	 "position":13

		        });
				columns.push({
		        	 "header":Messages("reagents.table.boxname"),
		        	 "property":"reagents[0].boxCatalogCode",
		        	 "filter":"codes:'reagentBox'",
		        	 
		        	 "order":false,
		        	 "type":"text",
		        	 "position":14
				});
				columns.push({
		        	 "header":Messages("reagents.table.boxcode"),
		        	 "property":"reagents[0].boxCode",
		        	 "order":true,
		        	 "type":"text",
		        	 "position":15			
				});
				columns.push({
		        	 "header":Messages("reagents.table.reagentname"),
		        	 "property":"reagents[0].reagentCatalogCode",
		        	 "filter":"codes:'reagentReagent'",
		        	 "order":false,
		        	 "type":"text",
		        	 "position":16	
				});
				columns.push({
		        	 "header":Messages("reagents.table.reagentcode"),
		        	 "property":"reagents[0].code",
		        	 "order":true,
		        	 "type":"text",
		        	 "position":17			
				});
				columns.push({
		        	 "header":Messages("reagents.table.description"),
		        	 "property":"reagents[0].description",
		        	 "order":true,
		        	 "type":"text",
		        	 "position":18			
				});				
				
			}else{	// getHomePage() = "new" || "search"				
				
				columns.push({
					"header":Messages("experiments.table.categoryCode"),
					"property":"categoryCode",
					"order":true,
					"hide":true,
					"position":4,
					"type":"text",
					"filter":"codes:'experiment_cat'",
					"groupMethod":"unique"
				});
				columns.push({
					"header":Messages("experiments.table.state.code"),
					"property":"state.code",
					"order":true,
					"type":"text",
					"position":5,
					"hide":true,
					"filter":"codes:'state'",
					"groupMethod":"collect:true"
				});
				/*columns.push({
					"header":Messages("experiments.table.status"),
					"property":"status.valid",
					"render":"<div bt-select ng-model='value.data.status.valid' bt-options='valid.code as valid.name for valid in searchService.lists.get(\"status\")'  ng-edit='false'></div>",
					"order":false,
					"hide":true,
					"position":5.5,
					"type":"text",
					"groupMethod":"collect"
				});*/
				columns.push({
					"header":Messages("experiments.table.status"),
					"property":"status.valid",
					"filter":"codes:'status'",
					"order":false,
					"hide":true,
					"edit":true,
					"position":5.5,
					"type":"text",
					"choiceInList":true,
				    "listStyle":"bt-select-multiple",
					 "possibleValues":"searchService.lists.get(\"status\")",
					"groupMethod":"collect:true"
				});
				/*columns.push({
					"header":Messages("experiments.table.resolutionCodes"),
					"property":"state.resolutionCodes",
					"render":"<div bt-select ng-model='value.data.state.resolutionCodes' bt-options='valid.code as valid.name for valid in searchService.lists.getResolutions()'  ng-edit=\"false\"></div>",
					"order":false,
					"hide":true,
					"position":6,
					"type":"text",
					"groupMethod":"collect:true"
				});*/
				columns.push({
					"header":Messages("experiments.table.resolutionCodes"),
					"property":"state.resolutionCodes",
					"filter":"codes:'resolution'",
					"order":false,
					"hide":true,
					"edit" : true,
					"position":6,
					"type":"text",
					"choiceInList":true,
				    "listStyle":"bt-select-multiple",
					 "possibleValues":"searchService.lists.getResolutions()",
					"groupMethod":"collect:true"
				});
				columns.push({
					"header":Messages("containers.table.sampleCodes.length"),
					"property":"sampleCodes.length",
					"order":true,
					"hide":true,
					"position":8,
					"type":"text"					
				});
				columns.push({
					"header":Messages("containers.table.tags"),
					"property":"atomicTransfertMethods",
					"order":true,
					"hide":true,
					"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
					"filter":"flatArray:\"inputContainerUseds\" | flatArray:\"contents\" | getArray:\"properties.tag.value\" | unique",
					"position":10,
					"type":"text"				
				});
				
			}
		         
			return columns;
		};
		
		
		var isInit = false;
		
		
		var initListService = function(){
			if(!isInit){
				lists.refresh.types({objectTypeCode:"Process"}, true);
				lists.refresh.processCategories();
				lists.refresh.experimentCategories();
				lists.refresh.projects();
				lists.refresh.users();
				lists.refresh.containerSupports();
				lists.refresh.states({objectTypeCode:"Experiment"});
				lists.refresh.experimentTypes({categoryCode:"purification"}, "purifications");
				lists.refresh.experimentTypes({categoryCode:"qualitycontrol"}, "qualitycontrols");
				lists.refresh.experimentTypes({categoryCode:"transfert"}, "transferts");
				lists.refresh.experimentTypes({categoryCode:"transformation"}, "transformations");
				lists.refresh.experimentTypes({categoryCode:"transformation"}, "fromTransformations");
				lists.refresh.reportConfigs({pageCodes:["experiments-addcolumns"]}, "experiments-addcolumns");
				lists.refresh.filterConfigs({pageCodes:["experiments-search-addfilters"]}, "experiments-search-addfilters");
				lists.refresh.protocols({}, 'all-protocols');
				//lists.refresh.instruments();
				lists.refresh.resolutions({objectTypeCode:"Experiment",distinct:true});
				isInit=true;
			}
		};
		
		var searchService = {
				getColumns:getColumns,
				getDefaultColumns:getColumns,
				datatable:undefined,
				isRouteParam:false,
				lists : lists,
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
					this.form.includes = [];
					if(this.reportingConfiguration){
						for(var i = 0 ; i < this.reportingConfiguration.columns.length ; i++){
							if(this.reportingConfiguration.columns[i].queryIncludeKeys && this.reportingConfiguration.columns[i].queryIncludeKeys.length > 0){
								this.form.includes = this.form.includes.concat(this.reportingConfiguration.columns[i].queryIncludeKeys);
							}else{
								this.form.includes.push(this.reportingConfiguration.columns[i].property.replace('.value','').replace(".unit", ''));
							}
						}
					}else{
						this.form.includes = ["default"];
					}
					
					
					//this.form.includes = ["default"];
					for(var i = 0 ; i < this.selectedAddColumns.length ; i++){
						//remove .value if present to manage correctly properties (single, list, etc.)
						if(this.selectedAddColumns[i].queryIncludeKeys && this.selectedAddColumns[i].queryIncludeKeys.length > 0){
							this.form.includes = this.form.includes.concat(this.selectedAddColumns[i].queryIncludeKeys);
						}else{
							this.form.includes.push(this.selectedAddColumns[i].property.replace('.value','').replace(".unit", ''));
						}
						
					}
				},
				convertForm : function(){
					var _form = angular.copy(this.form);
					if(_form.fromDate)_form.fromDate = moment(_form.fromDate, Messages("date.format").toUpperCase()).valueOf();
					if(_form.toDate)_form.toDate = moment(_form.toDate, Messages("date.format").toUpperCase()).valueOf();		
					return _form
				},
				
				resetForm : function(){					
					this.form = {};					
				},
				
				resetSampleCodes : function(){
					this.form.sampleCodes = [];									
				},
				
				resetSampleCodes : function(){
					this.form.sampleCodes = [];									
				},
				
				search : function(){
					this.updateForm();
					mainService.setForm(this.form);				
					this.datatable.search(this.convertForm());
					
				},
				initAdditionalColumns : function(){
					this.additionalColumns=[];
					this.selectedAddColumns=[];
					
					if(lists.get("experiments-addcolumns") && lists.get("experiments-addcolumns").length === 1){
						var formColumns = [];
						var allColumns = angular.copy(lists.get("experiments-addcolumns")[0].columns);
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
				refreshSamples : function(){
					if(this.form.projectCodes && this.form.projectCodes.length > 0){
						this.lists.refresh.samples({projectCodes:this.form.projectCodes});
					}
				},
				changeTypeCode : function(){
					//this.search();					
				},
				useMoment: function(date, format){
					//ex: 2014-10-02
					var patt = /[0-9]{4}-[0-9]{2}-[0-9]{2}/;
					
					//chrome browser always return input with type=date value as AAAA-MM-DD
					if(date.search(patt) != -1){
						return moment(date).valueOf();
					}
					//fifox browser return the specified format
					return moment(date, Messages("date.format").toUpperCase()).valueOf();
				},
				changeExperimentType : function(){											
					lists.refresh.instruments({"experimentTypes":this.form.typeCode}, "instruments-search-list");
					this.form.instruments = undefined;
				},
				
				changeProcessCategory : function(){
					//this.form.experimentType = undefined;
					//this.form.experimentCategory = undefined;
					this.form.processTypeCode = undefined;
					if(this.form.processCategory){
						lists.refresh.processTypes({categoryCode:this.form.processCategory});
					}
					
				},
				
				changeProcessType : function(){
					this.form.experimentType = undefined;
					this.form.experimentCategory = undefined;
				},
				
				changeExperimentCategory : function(){
					this.form.experimentType = undefined;
					if(this.form.processType && this.form.experimentCategory){
						lists.refresh.experimentTypes({categoryCode:this.form.experimentCategory, processTypeCode:this.form.processType});
					}else if(this.form.experimentCategory){
						lists.refresh.experimentTypes({categoryCode:this.form.experimentCategory});
					}
				},
				changeContainerSupportCode: function(val){
					
					console.log(val);
					return $http.get(jsRoutes.controllers.containers.api.ContainerSupports.list().url,{params:{"codeRegex":val}}).success(function(data, status, headers, config) {
						console.log(data);
						
						return [data];				
	    			});
					
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
						this.datatable.setColumnsConfig(this.getDefaultColumns().concat(this.selectedAddColumns));						
					}
					this.search();
				},	
				resetDatatableColumns:function(){
					this.initAdditionalColumns();
					this.datatable.setColumnsConfig(this.getDefaultColumns());
					this.search();
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
									config.searchService.search();
									config.datatable.setColumnsConfig(data.columns);																								
						});
					}else{
						this.reportingConfiguration = undefined;
						this.datatable.setColumnsConfig(this.getDefaultColumns());
						this.search();
					}
					
				},
				initAdditionalFilters:function(){
					this.additionalFilters=[];
					
					if(lists.get("experiments-search-addfilters") && lists.get("experiments-search-addfilters").length === 1){
						var formFilters = [];
						var allFilters = angular.copy(lists.get("experiments-search-addfilters")[0].filters);
						
						/* add static filters here*/
						allFilters.push({property:"protocolCode",html:"<div bt-select multiple=true filter=true placeholder='"+Messages("experiments.select.protocols")+"' class='form-control' ng-model='searchService.form.protocolCodes' bt-options='protocol.code as protocol.name for protocol in searchService.lists.get(\"all-protocols\")'></div>",position:allFilters.length+1});
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