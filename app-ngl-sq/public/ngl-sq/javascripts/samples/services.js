"use strict";

angular.module('ngl-sq.samplesServices', []).
factory('samplesSearchService', ['$http', 'mainService', 'lists', 'datatable', function($http, mainService, lists, datatable){
	//var tags = [];
	var getColumnsDefault = function(){
		var columns = [];
		
		columns.push({
			"header":Messages("samples.table.projectCodes"),
			"property":"projectCodes",
			"order":true,
			"hide":true,
			"group":true,
			"position":1,					
			"render":"<div list-resize='cellValue | unique' ' list-resize-min-size='2'>",
			"type":"text",
			"groupMethod":"collect"
		});	
		
		columns.push({
			"header":Messages("samples.table.code"),
			"property":"code",
			"render":"<div list-resize='cellValue | unique' ' list-resize-min-size='3' vertical>",
			"order":true,
			"hide":true,
			"position":2,
			"type":"text",
			"group":true,
			"groupMethod":"collect"
		});
		columns.push({
			"header":Messages("samples.table.typeCode"),
			"property":"typeCode",
			"filter":"codes:'type'",
			"order":true,
			"group": true,
			"hide":true,
			"position":3,
			"type":"text",			
			"groupMethod":"collect:true"
		});
		columns.push({
			"header":Messages("samples.table.referenceCollab"),
			"property":"referenceCollab",
			"order":true,
			"hide":true,
			"position":4,
			"type":"text",			
			"groupMethod":"count:true"
		});	
		columns.push({
			"header":Messages("samples.table.taxonCode"),
			"property":"taxonCode",
			"render":"<div list-resize='cellValue' ' list-resize-min-size='3' vertical>",
			"order":true,
			"group": true,
			"hide":true,
			"position":5,
			"type":"text",			
			"groupMethod":"collect:true"
		});	
		columns.push({
			"header":Messages("samples.table.ncbiScientifiName"),
			"property":"ncbiScientificName",
			"render":"<div list-resize='cellValue' ' list-resize-min-size='3' vertical>",
			"order":true,
			"group": true,
			"hide":true,
			"position":6,
			"type":"text",			
			"groupMethod":"collect:true"
		});	
		
		columns.push({
			"header":Messages("samples.table.creationDate"),
			"property":"traceInformation.creationDate",
			"order":true,
			"hide":true,
			"position":14,			
			"type":"date",
			"groupMethod":"unique"
				});
		columns.push({
			"header":Messages("samples.table.createUser"),
			"property":"traceInformation.createUser",
			"order":true,
			"hide":true,
			"position":15,
			"type":"text",
			"groupMethod":"unique"
		});
		columns.push({
			"header":Messages("samples.table.modifyDate"),
			"property":"traceInformation.modifyDate",
			"order":true,
			"hide":true,
			"position":15,			
			"type":"date",
			"groupMethod":"unique"
				});
		columns.push({
			"header":Messages("samples.table.modifyUser"),
			"property":"traceInformation.modifyUser",
			"order":true,
			"hide":true,
			"position":16,
			"type":"text",
			"groupMethod":"unique"
		});
		
		/*
		columns.push({
			"header":"Check condition",
			"property":"processes",
			"filter":"isConditions:[{criteria:{experiments:{typeCode:'cdna-synthesis'}},expected:true},{criteria:{experiments:{typeCode:'dna-illumina-indexed-library'}},expected:true}]:[{criteria:{experiments:{typeCode:'rna-illumina-indexed-library'}},expected:true}]",
			//"filter":"isConditions:[{criteria:{typeCode:'tag-pcr'},expected:false}, {criteria:{typeCode:'cdna-synthesis'},expected:false},{criteria:{typeCode:'dna-illumina-indexed-library'},expected:true}]",
			//"filter":"isConditions:[{criteria:{typeCode:'tag-pcr',properties:{targetedRegion:{value:'18S_V9'}}},expected:true}, {criteria:{typeCode:'dna-illumina-indexed-library'},expected:true}]",
			"order":false,
			"hide":true,
			"watch":true,
			"position":16,
			"type":"text",
			"watch":true,
			"render":"<div present-sample-processes='cellValue'/>"
						
		});
		
		
		
		columns.push({
			"header":"ReadSets",
			"property":"processes",
			"filter":"getArray:'readsets'|flatArray",
			"order":false,
			"hide":true,
			"position":22,
			"type":"text",
			"render":"<table ng-if=\"cellValue.length > 0\" class='table table-condensed'>\n<tr><th>Code</th><th>Etat</th><th>Valid QC</th><th>Valid BioInfo</th><th>CR QC</th><th>Nb. séquences valide</th></tr>\n<tr ng-repeat=\"r in ::cellValue|orderBy:'runSequencingStartDate' track by $index\">\n<td> <a href=\"#\" ng-click=\"goToReadSet(r.code, $event)\" ng-bind=\"::r.code\"></a></td>\n<td ng-bind=\"::r.state.code|codes:'state'\"></td>\n<td ng-bind=\"::r.productionValuation.valid|codes:'valuation'\"></td>\n<td ng-bind=\"::r.bioinformaticValuation.valid|codes:'valuation'\"></td>\n<td><span ng-repeat=\"r in ::r.productionValuation.resolutionCodes\" ng-bind=\"r|codes:'resolution'\"></span></td>\n<td ng-bind=\"r.treatments.ngsrg.default.nbCluster.value|number\"></td></tr></table>"
						
		});
		
		
		columns.push({
			"header":"Processus Type Present",
			"headerTpl":"<div bt-select placeholder='Select Processus Type' class='form-control' ng-model='column.headerForm.processTypeCode' bt-options='processType.code as processType.name for processType in searchService.lists.getProcessTypes()' style='display:inline-block'></div></div>",
			"property":"processes",
			"filter":"presentSampleProcesses:col.headerForm.processTypeCode:'process':false",
			"order":false,
			"hide":true,
			"watch":true,
			"position":16,
			"type":"text",
			"watch":true,
			"render":"<div present-sample-processes='cellValue'/>"
						
		});
		columns.push({
			"header":"Exp. Type Present",
			"headerTpl":"<div bt-select placeholder='Select Processus Type' class='form-control' ng-model='column.headerForm.experimentTypeCode' bt-options='type.code as type.name for type in searchService.lists.getExperimentTypes()' style='display:inline-block'></div></div>",
			"property":"processes",
			"filter":"presentSampleProcesses:col.headerForm.experimentTypeCode:'experiment':true",
			"order":false,
			"hide":true,
			"watch":true,
			"position":16,
			"type":"text",
			"watch":true,
			"render":"<div present-sample-processes='cellValue' inverse=true/>"
						
		});
		
		columns.push({
			"header":"Processus Categories",
			"headerTpl":"<div bt-select placeholder='Select Processus Category' multiple=true class='form-control' ng-model='column.headerForm.processCategoryCode' bt-options='processCategory.code as processCategory.name for processCategory in searchService.lists.getProcessCategories()' style='display:inline-block'></div></div>"
				+" <div class='checkbox'><label><input type='checkbox' ng-model='column.headerForm.showRS'> ReadSets</label></div>"
				+" <div class='checkbox'><label><input type='checkbox' ng-model='column.headerForm.showPercent'> %</label></div>"
				+" <legend-sample-processes/>"
				,
			"property":"processes",
			"order":false,
			"hide":true,
			"position":16,
			"type":"text",
			"watch":true,
			"render": "<display-sample-processes dsp-processes='cellValue' dsp-process-category-codes='col.headerForm.processCategoryCode' dsp-show-rs='col.headerForm.showRS' dsp-show-percent='col.headerForm.showPercent'/>"			
		});
		*/
		return columns;
	};
	
	var isInit = false;

	var initListService = function(){
		if(!isInit){
			lists.refresh.projects();
			lists.refresh.processCategories();
			lists.refresh.processTypes();
			lists.refresh.experimentTypes({categoryCodes:["transformation"], withoutOneToVoid:false},"transformation");
			lists.refresh.experimentTypes({categoryCodes:["purification"], withoutOneToVoid:false},"purification");
			lists.refresh.experimentTypes({categoryCodes:["qualitycontrol"], withoutOneToVoid:false},"qualitycontrol");
			lists.refresh.experimentTypes({categoryCodes:["transfert"], withoutOneToVoid:false},"transfert");
			lists.refresh.states({objectTypeCode:"Sample"});
			lists.refresh.users();
			lists.refresh.reportConfigs({pageCodes:["samples"+"-"+mainService.getHomePage()]});
			lists.refresh.reportConfigs({pageCodes:["samples-addcolumns"]}, "samples-addcolumns");
			lists.refresh.filterConfigs({pageCodes:["samples-search-addfilters"]}, "samples-search-addfilters");
			lists.refresh.resolutions({"objectTypeCode":"Sample"}, "sampleResolutions");
			lists.refresh.protocols({}, 'all-protocols');
			isInit=true;
		}
	};

	var searchService = {
			getColumns:getColumnsDefault,
			getDefaultColumns:getColumnsDefault,
			datatable:undefined,
			isRouteParam:false,
			lists : lists,
			form:undefined,
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
			getStates : function(value){
				/*
				this.initAuthorizedStates();
				if(value && value.data){
					return this.authorizedStates[value.data.state.code];
				}else{
					return this.lists.getStates();
				}
				*/
				return this.lists.getStates();
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
				
				if(this.form.reportingQuery){
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
				if(_form.fromDate)_form.fromDate = moment(_form.fromDate, Messages("date.format").toUpperCase()).valueOf();
				if(_form.toDate)_form.toDate = moment(_form.toDate, Messages("date.format").toUpperCase()).valueOf();		
				return _form

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
				lists.clear('samples');								
			},

			
			search : function(){
				this.updateForm();
				mainService.setForm(this.form);				
				this.datatable.search(this.convertForm());
				this.datatable.setMessagesActive(true);
			},

			refreshSamples : function(){
				if(this.form.projectCodes && this.form.projectCodes.length>0){
					lists.refresh.samples({projectCodes:this.form.projectCodes});
				}
			},

			changeExistingProcessType : function(){
				this.form.existingTransformationTypeCode = undefined;
				if(this.form.existingProcessTypeCode){
					lists.refresh.experimentTypes({categoryCode:"transformation", withoutOneToVoid:false,processTypeCode:this.form.existingProcessTypeCode},"transformation");
				}else{
					lists.refresh.experimentTypes({categoryCodes:["transformation"], withoutOneToVoid:false},"transformation");
				}
			},
			initAdditionalColumns : function(){
				this.additionalColumns=[];
				this.selectedAddColumns=[];
				this.mapAdditionnalColumn=new Map();
				
				if(lists.get("samples-addcolumns") && lists.get("samples-addcolumns").length === 1){
					var formColumns = [];
					//we used the order in the document to order column in display and not the position value !!!!
					//var allColumns = angular.copy(lists.get("samples-addcolumns")[0].columns);
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
				var allColumns = angular.copy(lists.get("samples-addcolumns")[0].columns);
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
								if(config.searchService.lists.get('reportConfigs').length > 1){
									config.searchService.search();
								}
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
				
				if(lists.get("samples-search-addfilters") && lists.get("samples-search-addfilters").length === 1){
					var formFilters = [];
					var allFilters = angular.copy(lists.get("samples-search-addfilters")[0].filters);
					
					/* add static filters here*/
					//allFilters.push({property:"comments.comment",html:"<textarea class='form-control' ng-model='searchService.form.commentRegex' placeholder='"+Messages("search.placeholder.commentRegex")+"' title='"+Messages("search.placeholder.commentRegex")+"'></textarea>",position:allFilters.length+1});
					allFilters.push({property:"processes.experiments.protocolCode",html:"<div bt-select multiple=true filter=true placeholder='"+Messages("experiments.select.protocols")+"' class='form-control' ng-model='searchService.form.experimentProtocolCodes' bt-options='protocol.code as protocol.name for protocol in searchService.lists.get(\"all-protocols\")'></div>",position:allFilters.length+1});
					
					
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
				
				// to avoid to lost the previous search
				if(datatableConfig && angular.isUndefined(mainService.getDatatable())){
					searchService.datatable = datatable(datatableConfig);
					mainService.setDatatable(searchService.datatable);
					searchService.datatable.setColumnsConfig(getColumnsDefault());		
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
}]);