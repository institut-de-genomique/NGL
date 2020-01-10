 "use strict";
 
 angular.module('ngl-bi.RunsServices', []).
	factory('runSearchService', ['$http', 'mainService', 'lists','datatable', function($http, mainService, lists, datatable){
		
		var getDefaultColumns = function(){
				var columns = [
							    {  	"property":"code",
							    	"header": Messages("runs.code"),
							    	"type" :"text",
							    	"order":true,
							    	"position":1,
							    	"groupMethod":"countDistinct"
								},
								{	"property":"typeCode",
									"header": Messages("runs.typeCode"),
									"filter":"codes:'type'",
									"type" :"text",
							    	"order":true,
							    	"group":true,
							    	"position":2
								},
								{	"property":"sequencingStartDate",
									"header": Messages("runs.sequencingStartDate"),
									"type" :"date",
							    	"order":true,
							    	"position":3
								},
								{	"property":"state.historical|filter:'F-RG'|get:'date'",
									"header": Messages("runs.endOfRG"),
									"type" :"date",
							    	"order":true,
							    	"position":4
								},
								{	"property":"state.code",
									"filter":"codes:'state'",					
									"header": Messages("runs.stateCode"),
									"type" :"text",
									"edit":true,
									"order":true,
									"choiceInList":true,
							    	"listStyle":'bt-select',
							    	"possibleValues":'searchService.lists.getStates()',
							    	"position":5	
								},
								{	"property":"valuation.valid",
									"filter":"codes:'valuation'",					
									"header": Messages("runs.valuation.valid"),
									"type" :"text",
							    	"order":true,
							    	"position":100
								},
								{	"property":"valuation.resolutionCodes",
									"header": Messages("runs.valuation.resolutions"),
									"render":'<div bt-select ng-model="value.data.valuation.resolutionCodes" bt-options="valid.code as valid.name group by valid.category.name for valid in searchService.lists.getResolutions()" ng-edit="false"></div>',
									"type" :"text",
									"hide":true,
									"position":101
								} 
							];						
				return columns;
			
		}
		
		
		var isInit = false;
		
		var initListService = function(){
			if(!isInit){
				lists.refresh.projects();
				lists.refresh.states({objectTypeCode:"Run", display:true},'statetrue');				
				lists.refresh.states({objectTypeCode:"Run"});							
				lists.refresh.types({objectTypeCode:"Run"});
				lists.refresh.resolutions({objectTypeCode:"Run"});
				lists.refresh.runs();
				lists.refresh.runCategories();
				lists.refresh.instruments({categoryCodes:["illumina-sequencer","extseq","nanopore-sequencer"]});
				lists.refresh.users();
				lists.refresh.filterConfigs({pageCodes:["runs-addfilters"]}, "runs-addfilters");
				lists.refresh.reportConfigs({pageCodes:["runs-addcolumns"]}, "runs-addcolumns");
				
				searchService.lists.refresh.valuationCriterias({objectTypeCode:"Run",orderBy:'name'});
				
				
				isInit=true;
			}
		};
		
		
		var searchService = {
				getColumns:getDefaultColumns,
				datatable:undefined,
				isRouteParam:false,
				lists : lists,
				form : undefined,
				additionalColumns:[],
				additionalFilters:[],
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
							this.form.stateCodes = ["IW-V","IP-V"];
						}		
					}
					this.form.includes = ["default"];
					//this.form.excludes =  ["lanes"];	
					
					for(var i = 0 ; i < this.selectedAddColumns.length ; i++){
						//remove .value if present to manage correctly properties (single, list, etc.)
						if(this.selectedAddColumns[i].queryIncludeKeys && this.selectedAddColumns[i].queryIncludeKeys.length > 0){
							this.form.includes = this.form.includes.concat(this.selectedAddColumns[i].queryIncludeKeys);
						}else{
							this.form.includes.push(this.selectedAddColumns[i].property.replace('.value',''));	
						}
						
					}
				},
				convertForm : function(){
					var _form = angular.copy(this.form);
					if(_form.fromDate)_form.fromDate = moment(_form.fromDate, Messages("date.format").toUpperCase()).valueOf();
					if(_form.toDate)_form.toDate = moment(_form.toDate, Messages("date.format").toUpperCase()).valueOf();		
					if(_form.fromEndRGDate)_form.fromEndRGDate = moment(_form.fromEndRGDate, Messages("date.format").toUpperCase()).valueOf();
					if(_form.toEndRGDate)_form.toEndRGDate = moment(_form.toEndRGDate, Messages("date.format").toUpperCase()).valueOf();		
					
					
					return _form
				},
				
				resetForm : function(){
					this.form = {};
				},
				
				resetSampleCodes : function(){
					this.form.sampleCodes = [];									
				},
				
				search : function(){
					this.updateForm();
					mainService.setForm(this.form);
					this.datatable.search(this.convertForm());
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
				initAdditionalColumns:function(){
					this.additionalColumns=[];
					this.selectedAddColumns=[];
					
					if(lists.get("runs-addcolumns") && lists.get("runs-addcolumns").length === 1){
						var formColumns = [];
						var allColumns = angular.copy(lists.get("runs-addcolumns")[0].columns);
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
					if(lists.get("runs-addfilters") && lists.get("runs-addfilters").length === 1){
						var formFilters = [];
						var allFilters = angular.copy(lists.get("runs-addfilters")[0].filters);
						/* add static filters*/
						allFilters.push({property:"fromEndRGDate",html:"<input type='text' class='form-control' ng-model='searchService.form.fromEndRGDate' placeholder='"+Messages("search.placeholder.fromEndRGDate")+"' title='"+Messages("search.placeholder.fromEndRGDate")+"'>",position:allFilters.length+1});
						allFilters.push({property:"toEndRGDate",html:'<input type="text" class="form-control" ng-model="searchService.form.toEndRGDate" placeholder="'+Messages("search.placeholder.toEndRGDate")+'" title="'+Messages("search.placeholder.toEndRGDate")+'">',position:allFilters.length+1});
						
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
 