"use strict";

angular.module('ngl-sq.processesServices', [])
.factory('processesSearchService', [ '$http', '$parse', '$filter', 'mainService', 'lists', 'datatable', 'propertyDefHelpers',
                                          function($http, $parse, $filter, mainService, lists, datatable, propertyDefHelpers) {
	var isInit = false;

	var initListService = function() {
		if (!isInit) {
			lists.refresh.processCategories();
			lists.refresh.projects();
			lists.refresh.users();
			lists.refresh.states({
				objectTypeCode : "Process"
			});
			lists.refresh.reportConfigs({
				pageCodes : [ "processes-addcolumns" ]
			}, "processes-addcolumns");
			lists.refresh.filterConfigs({
				pageCodes : [ "processes-search-addfilters" ]
			}, "processes-search-addfilters");
			
			lists.refresh.resolutions({"objectTypeCode":"Process"}, "processResolutions");
			
			isInit = true;
		}
	};

	var getDefaultColumns = function() {
		var columns = [];
		columns.push({
			"header" : Messages("processes.table.inputContainerSupportCode"),
			"property" : "inputContainerSupportCode",
			"order" : true,
			"hide" : false,
			"group" : true,
			"position" : 0.5,
			"type" : "text"
		});
		columns.push({
			"header" : Messages("processes.table.inputContainerCode"),
			"property" : "inputContainerCode",
			"order" : true,
			"hide" : false,
			"group" : true,
			"position" : 1,
			"type" : "text",
			"groupMethod":"count:true"
		});
		columns.push({
			"header" : Messages("processes.table.projectCode"),
			"property" : "projectCodes",
			"order" : true,
			"hide" : true,
			"group" : true,
			"groupMethod" : "collect:true",
			"filter":"orderBy",
        	"render" : "<div list-resize='cellValue' list-resize-min-size='3'>",
			"position" : 2,
			"type" : "text"
		});
		columns.push({
			"header" : Messages("processes.table.sampleCode"),
			"property" : "sampleCodes",
			"order" : true,
			"hide" : false,
			"group" : true,
			"groupMethod" : "collect:true",
			"filter":"orderBy",
        	"render" : "<div list-resize='cellValue' list-resize-min-size='3'>",
			"position" : 3,
			"type" : "text"
		});
		
		columns.push({
			"header" : Messages("containers.table.tags"),
			"property" : "sampleOnInputContainer.properties.tag.value",
			"type" : "string",
			"order" : true,
			"hide" : true,
			"groupMethod" : "collect:true",
			"render" : "<div list-resize='cellValue' list-resize-min-size='3'>",
			"position" : 4
		});
		columns.push({
			"header" : Messages("processes.table.typeCode"),
			"property" : "typeCode",
			"filter" : "codes:'type'",
			"order" : true,
			"hide" : false,
			"groupMethod" : "unique",
			"position" : 5,
			"type" : "text"
		});
		if (mainService.getHomePage() == 'state') {
			columns.push({
				"header" : Messages("processes.table.stateCode"),
				"property" : "state.code",
				"order" : true,
				"hide" : false,
				"position" : 6,
				"type" : "text",
				"filter" : "codes:'state'",
				"edit" : true,
				"choiceInList" : true,
				"listStyle":"bt-select",
				"possibleValues" : "searchService.lists.getStates()",
			});
		} else {

			columns.push({
				"header" : Messages("processes.table.stateCode"),
				"property" : "state.code",
				"position" : 6,
				"order" : true,
				"hide" : false,
				"edit" : false,
				"type" : "text",
				"filter" : "codes:'state'",
				"groupMethod" : "collect:true",
				"render" : "<div list-resize='cellValue' list-resize-min-size='3'>",
			});
		}
		columns.push({
			"header" : Messages("processes.table.resolutionCodes"),
			"property" : "state.resolutionCodes",
			"filter":"codes:'resolution'",
			"position" : 7,
			"order" : true,
			"edit" : true,
			"hide" : true,
			"choiceInList":true,
		    "listStyle":"bt-select-multiple",
		    "groupMethod" : "collect:true",
		    "possibleValues":"searchService.lists.get('processResolutions')",
			"type" : "text"
		});
		columns.push({
			"header" : Messages("processes.table.currentExperimentTypeCode"),
			"property" : "currentExperimentTypeCode",
			"filter" : "codes:'type'",
			"order" : true,
			"hide" : false,
			"groupMethod" : "collect:true",
			"position" : 8,
			"type" : "text"
		});
		
		columns.push({
			"header" : Messages("processes.table.outputContainerSupportCodes"),
			"property" : "outputContainerSupportCodes",
			"order" : false,
			"hide" : false,
			"position" : 8.5,
			"filter":"unique",
			"groupMethod" : "collect:true",
			"filter":"orderBy",
			"render" : "<div list-resize='cellValue' list-resize-min-size='2' vertical>",
			"type" : "text"
		});
		
		columns.push({
			"header" : Messages("processes.table.outputContainerCodes"),
			"property" : "outputContainerCodes",
			"order" : false,
			"hide" : false,
			"position" : 9,
			"filter":"unique",
			"groupMethod" : "collect:true",
			"filter":"orderBy",
			"render" : "<div list-resize='cellValue' list-resize-min-size='2' vertical>",
			"type" : "text"
		});
		
		columns.push({
			"header" : Messages("processes.table.experimentCodes"),
			"property" : "experimentCodes",
			"order" : false,
			"hide" : false,
			"position" : 10,
			"groupMethod" : "collect:true",
			"filter":"orderBy:searchService.extractDate",
			"render" : "<div list-resize='cellValue' list-resize-min-size='2' vertical>",
			"type" : "text"
		});
		columns.push({
			"header" : Messages("processes.table.code"),
			"property" : "code",
			"order" : true,
			"hide" : true,
			"groupMethod" : "collect:true",
			"render" : "<div list-resize='cellValue' list-resize-min-size='3'>",
			"position" : 11,
			"type" : "text"
		});
		columns.push({
			"header" : Messages("processes.table.creationDate"),
			"property" : "traceInformation.creationDate",
			"position" : 12,
			"order" : true,
			"hide" : true,
			"groupMethod" : "unique",
			"format" : Messages("datetime-hour.format"),
			"type" : "date"
		});
		columns.push({
			"header" : Messages("processes.table.assignDate"),
			"property" : "state.historical",
			"position" : 13,
			"order" : true,
			"hide" : true,
			"filter" : "filters:{code:'N'}:true|get:'date'",
			"format" : Messages("datetime-hour.format"),
			"groupMethod" : "unique",
			"type" : "date"
		});
		columns.push({
			"header" : Messages("processes.table.endDate"),
			"property" : "state.historical",
			"position" : 14,
			"order" : true,
			"hide" : true,
			"filter" : "filters:{code:'F'}:true|get:'date'",
			"format" : Messages("datetime-hour.format"),
			"groupMethod" : "unique",
			"type" : "date"
		});
		columns.push({
			"header" : Messages("processes.table.createUser"),
			"property" : "traceInformation.createUser",
			"position" : 15,
			"order" : true,
			"hide" : true,
			"groupMethod" : "unique",
			"type" : "text"
		});
		columns.push({
			"header" : Messages("processes.table.comments"),
			"property" : "comments[0].comment",
			"editTemplate":"<textarea class='form-control' #ng-model rows='3'></textarea>",
			"position" : 500,
			"order" : false,
			"edit" : true,
			"hide" : true,
			"groupMethod" : "unique",
			"type" : "text"
		});
		return columns;
	};

	
	
	var searchService = {

		datatable : undefined,
		isRouteParam : false,
		lists : lists,
		getDefaultColumns : getDefaultColumns,
		additionalFilters : [],
		additionalProcessFilters : [],
		isProcessFiltered : false,
		additionalColumns : [],
		selectedAddColumns : [],
		processTypesByCategory : {},
		processTypesForCategories : [],
		setRouteParams : function($routeParams) {
			var count = 0;
			for ( var p in $routeParams) {
				count++;
				break;
			}
			if (count > 0) {
				this.isRouteParam = true;
				this.form = angular.copy($routeParams);
				
				if(angular.isString(this.form.typeCodes)){
					this.form.typeCodes = [this.form.typeCodes];
				}
				if(angular.isString(this.form.categoryCodes)){
					this.form.categoryCodes = [this.form.categoryCodes];
				}
				
				
			}
		},
		extractDate: function(value){
			return value.split(/(\d+_\d+)/)[1];
		},
		
		initCommonPropertiesDefinitions : function(typeCodes){
			if(angular.isArray(typeCodes)){
				var processTypes = typeCodes.map(function(typeCode){
					return this.processTypesForCategories.find(function(pTypes){return (pTypes.code ===  typeCode);});
				},this);
				
				var propertyDefCounter = {};
				
				processTypes.forEach(function(processType){
					processType.propertiesDefinitions.forEach(function(pdef){
						if( propertyDefCounter[pdef.code] === undefined){
							pdef._number = 0;
							propertyDefCounter[pdef.code] = pdef;
						}
						 propertyDefCounter[pdef.code]._number++;
					});
				});
				var pdefs = [];
				for(var key in propertyDefCounter){
					if(propertyDefCounter[key]._number === processTypes.length){
						propertyDefCounter[key]._number = undefined;
						pdefs.push(propertyDefCounter[key])
					}
				}
				this.commonPropertiesDefinitions = pdefs;
			}else{
				this.commonPropertiesDefinitions = [];
			}
			
		},
		setColumns : function() {
			
			var datatable = this.datatable;
			var columnsDefault = this.columnsDefault;

			if (this.selectedAddColumns != undefined && this.selectedAddColumns != null) {
				columnsDefault = this.getDefaultColumns().concat(this.selectedAddColumns);

			}
			if(angular.isArray(this.commonPropertiesDefinitions) && this.commonPropertiesDefinitions.length > 0){
				var columns = this.commonPropertiesDefinitions.map(function(propertyDefinition) {
					return propertyDefHelpers.getProcessUDTColumn(propertyDefinition);					
				});
				
				columns = columnsDefault.concat(columns);
				datatable.setColumnsConfig(columns);
					
			}else{
				datatable.setColumnsConfig(columnsDefault);
			}
		},

		updateForm : function() {
			this.form.includes = [];
			if (this.reportingConfiguration) {
				for (var i = 0; i < this.reportingConfiguration.columns.length; i++) {
					if (this.reportingConfiguration.columns[i].queryIncludeKeys && this.reportingConfiguration.columns[i].queryIncludeKeys.length > 0) {
						this.form.includes = this.form.includes.concat(this.reportingConfiguration.columns[i].queryIncludeKeys);
					} else {
						this.form.includes.push(this.reportingConfiguration.columns[i].property.replace('.value', '').replace(".unit", ''));
					}
				}
			} else {
				this.form.includes = [ "default" ];
			}

			//this.form.includes = ["default"];
			for (var i = 0; i < this.selectedAddColumns.length; i++) {
				//remove .value if present to manage correctly properties (single, list, etc.)
				if (this.selectedAddColumns[i].queryIncludeKeys && this.selectedAddColumns[i].queryIncludeKeys.length > 0) {
					this.form.includes = this.form.includes.concat(this.selectedAddColumns[i].queryIncludeKeys);
				} else {
					this.form.includes.push(this.selectedAddColumns[i].property.replace('.value', '').replace(".unit", ''));
				}

			}
		},
		convertForm : function() {
			var _form = angular.copy(this.form);
			if (_form.fromDate)
				_form.fromDate = moment(_form.fromDate, Messages("date.format").toUpperCase()).valueOf();
			if (_form.toDate)
				_form.toDate = moment(_form.toDate, Messages("date.format").toUpperCase()).valueOf();
			return _form;
		},

		resetForm : function() {
			this.form = {};
			this.changeProcessCategories();				
		},

		resetTextareas : function(){
			Array.from(document.getElementsByTagName('textarea')).forEach(function (element) {
				var elementScope = angular.element(element).scope();
				if(elementScope.textareaValue){
					elementScope.textareaValue = null;
				}
			}); 
		},

		resetSampleCodes : function() {
			this.form.sampleCodes = [];
		},

		initAdditionalFilters : function() {
			this.additionalFilters = [];
			var formFilters = [];
			var allFilters = undefined;
			var nbElementByColumn = undefined;

			if (lists.get("processes-search-addfilters") && lists.get("processes-search-addfilters").length === 1) {
				allFilters = angular.copy(lists.get("processes-search-addfilters")[0].filters);
			}
			if (angular.isDefined(allFilters)) {
				nbElementByColumn = Math.ceil(allFilters.length / 5); //5 columns
				for (var i = 0; i < 5 && allFilters.length > 0; i++) {
					formFilters.push(allFilters.splice(0, nbElementByColumn));
				}
				//complete to 5 five element to have a great design 
				while (formFilters.length < 5) {
					formFilters.push([]);
				}
			}

			this.additionalFilters = formFilters;
		},

		getAddFiltersToForm : function() {
			if (this.additionalFilters !== undefined && this.additionalFilters.length === 0) {
				this.initAdditionalFilters();
			}
			return this.additionalFilters;

		},

		

		search : function() {
			this.updateForm();
			mainService.setForm(this.form);
			this.datatable.search(this.convertForm());

		},

		refreshSamples : function() {
			if (this.form.projectCodes && this.form.projectCodes.length > 0) {
				this.lists.refresh.samples({
					projectCodes : this.form.projectCodes
				});
			}
		},

		initProcessTypes:function(){
			//load all process cat
			return $http.get(jsRoutes.controllers.processes.api.ProcessTypes.list().url, {processTypesByCategory:this.processTypesByCategory})
				.success(function(data, status, headers, config) {
				data.forEach(function(processType){
					if(!config.processTypesByCategory[processType.category.code]){
						config.processTypesByCategory[processType.category.code] = [];
					}
					config.processTypesByCategory[processType.category.code].push(processType);
				})
				
				
			});
		},		
		changeProcessCategories : function() {
			this.additionalProcessFilters = [];
			this.commonPropertiesDefinitions = [];
			this.form.typeCodes = undefined;
			this.processTypesForCategories=[];
			this.setColumns();
			if (this.form.categoryCodes && this.form.categoryCodes.length > 0) {
				this.form.categoryCodes.forEach(function(code){
					this.processTypesForCategories = this.processTypesForCategories.concat(this.processTypesByCategory[code])
				},this)
				this.processTypesForCategories = $filter('orderBy')(this.processTypesForCategories, 'displayOrder');
			}else {
				this.form.categoryCodes = undefined;
			}
		},

		changeProcessTypeCode : function() {
			if (!angular.isDefined(this.form.categoryCodes)) {	
				this.form.typeCodes = undefined;
			}
			this.initCommonPropertiesDefinitions(this.form.typeCodes);
			this.setColumns();			
			this.initAdditionalProcessFilters();
		},
		//new version based on type properties
		initAdditionalProcessFilters : function() {
			this.additionalProcessFilters = [];
			var formFilters = [];
			var allFilters = undefined;
			var nbElementByColumn = undefined;
			this.isProcessFiltered = false;	
			
			if(angular.isArray(this.commonPropertiesDefinitions) && this.commonPropertiesDefinitions.length > 0){
				allFilters = this.commonPropertiesDefinitions.map(function(pDef){
					return propertyDefHelpers.getHtmlFilter(pDef, 'searchService');
				});				
			} 
			if (angular.isDefined(allFilters)) {
				nbElementByColumn = Math.ceil(allFilters.length / 5); //5 columns
				for (var i = 0; i < 5 && allFilters.length > 0; i++) {
					formFilters.push(allFilters.splice(0, nbElementByColumn));
				}
				//complete to 5 five element to have a great design 
				while (formFilters.length < 5) {
					formFilters.push([]);
				}
				this.isProcessFiltered = true;					
			}
			this.additionalProcessFilters = formFilters;
			
		},	
		getAddProcessFiltersToForm : function() {
			return this.additionalProcessFilters;
		},
		
		initAdditionalColumns : function() {
			this.additionalColumns = [];
			this.selectedAddColumns = [];

			if (lists.get("processes-addcolumns") && lists.get("processes-addcolumns").length === 1) {
				var formColumns = [];
				var allColumns = angular.copy(lists.get("processes-addcolumns")[0].columns);
				var nbElementByColumn = Math.ceil(allColumns.length / 5); //5 columns
				for (var i = 0; i < 5 && allColumns.length > 0; i++) {
					formColumns.push(allColumns.splice(0, nbElementByColumn));
				}
				//complete to 5 five element to have a great design 
				while (formColumns.length < 5) {
					formColumns.push([]);
				}
				this.additionalColumns = formColumns;
			}
		},
		getAddColumnsToForm : function() {
			if (this.additionalColumns.length === 0) {
				this.initAdditionalColumns();
			}
			return this.additionalColumns;
		},
		addColumnsToDatatable : function() {

			this.selectedAddColumns = [];
			for (var i = 0; i < this.additionalColumns.length; i++) {
				for (var j = 0; j < this.additionalColumns[i].length; j++) {
					if (this.additionalColumns[i][j].select) {
						this.selectedAddColumns.push(this.additionalColumns[i][j]);
					}
				}
			}
			this.setColumns();
			this.search();
		},
		resetDatatableColumns : function() {
			this.initAdditionalColumns();
			this.datatable.setColumnsConfig(this.getDefaultColumns);
			this.search();
		},
		/**
		 * Update column when change reportingConfiguration
		 */
		updateColumn : function() {
			this.initAdditionalColumns();
			if (this.reportingConfigurationCode) {
				$http.get(jsRoutes.controllers.reporting.api.ReportingConfigurations.get(this.reportingConfigurationCode).url, {
					searchService : this,
					datatable : this.datatable
				}).success(function(data, status, headers, config) {
					config.searchService.reportingConfiguration = data;
					config.searchService.search();
					config.datatable.setColumnsConfig(data.columns);
				});
			} else {
				this.reportingConfiguration = undefined;
				this.datatable.setColumnsConfig(this.getDefaultColumns());
				this.search();
			}

		},
		/**
		 * initialise the service
		 */
		init : function($routeParams, datatableConfig) {
			initListService();
			
			datatableConfig.messages = {
					transformKey: function(key, args) {
                        return Messages(key, args);
                    }
			};
			
			//to avoid to lost the previous search
			if (datatableConfig && angular.isUndefined(mainService.getDatatable())) {
				searchService.datatable = datatable(datatableConfig);
				searchService.datatable.setColumnsConfig(getDefaultColumns());
				mainService.setDatatable(searchService.datatable);
			} else if (angular.isDefined(mainService.getDatatable())) {
				searchService.datatable = mainService.getDatatable();
			}

			if (angular.isDefined(mainService.getForm())) {
				searchService.form = mainService.getForm();
			} else {
				searchService.resetForm();
			}
						
			searchService.initProcessTypes().then(function(){
				searchService.setRouteParams($routeParams);
				if(searchService.isRouteParam){
					searchService.changeProcessCategories();
					searchService.setRouteParams($routeParams); //twice because changeProcessCategories remove typeCodes
					searchService.changeProcessTypeCode();
					searchService.search();			
				}
			});

			
		}
	};

	return searchService;
}]).factory('processesNewService', [ '$q', '$http', '$parse', '$filter', 'mainService', 'datatable', 'messages', 'propertyDefHelpers',
    function($q, $http, $parse, $filter, mainService, datatable,messages,propertyDefHelpers) {

	var	getEditDatatableConfig = function(newService){
		var editDatatableConfig = {
				 columns: [],
		         pagination:{
		        	 active:false
		         },		
		         search:{
		        	 active:false
		         },
		         order:{
		        	 mode:'local',
		        	 active:true
		         },
		         edit:{  		
		        	 active:true,
		        	 columnMode:true,
		        	 byDefault : true,
		        	 showButton:false
		         },
		         save:{
		        	 active: true,
		        	 withoutEdit:true,
		        	 showButton : true,
		        	 mode:"local",
		        	 changeClass : false,
		        	 callback : function(datatable){
		        		 newService.udtSaveCallback(datatable);
		        	 }
		         },
		         remove:{
		        	 active:true,
		        	 mode:'local',
		        	 withEdit:true,
		        	 callback : function(datatable){
		        		 newService.udtRemoveCallback(datatable);
		        	 }
		         },
		         lines:{
		        	trClass:function(data,line){
		        		return newService.udtTrClass(data,line);
		        	}
		         },
		         messages:{
		        	 active:false,
		        	 transformKey: function(key, args) {
			             return Messages(key, args);
		        	 }
		         },
		         cancel:{
		        	 active:true
		         },
		         select:{
		        	    "active":true,//Active or not
		        	    "showButton":true,//Show the select all button in the toolbar,
		         },
		         //Bouton pour switcher de containers à support ou de support a containers
		         otherButtons :newService.udtOtherButtons()
		};
		return editDatatableConfig;
	};
	
	var	getEditDatatableConfigWithScope = function(newService,$scope){
		var editDatatableConfig = {
				 columns: [],
		         pagination:{
		        	 active:false
		         },		
		         search:{
		        	 active:false
		         },
		         order:{
		        	 mode:'local',
		        	 active:true
		         },
		         edit:{  		
		        	 active:true,
		        	 columnMode:true,
		        	 byDefault : true,
		        	 showButton:false
		         },
		         save:{
		        	 active: true,
		        	 withoutEdit:true,
		        	 showButton : true,
		        	 mode:"local",
		        	 changeClass : false,
		        	 callback : function(datatable){
		        		 newService.udtSaveCallback(datatable,$scope);
		        	 }
		         },
		         remove:{
		        	 active:true,
		        	 mode:'local',
		        	 withEdit:true,
		        	 callback : function(datatable){
		        		 newService.udtRemoveCallback(datatable);
		        	 }
		         },
		         lines:{
		        	trClass:function(data,line){
		        		return newService.udtTrClass(data,line);
		        	}
		         },
		         messages:{
		        	 active:false,
		        	 transformKey: function(key, args) {
			             return Messages(key, args);
		        	 }
		         },
		         cancel:{
		        	 active:true
		         },
		         select:{
		        	    active:true,//Active or not
		        	    showButton:true,//Show the select all button in the toolbar,
		         },
		         //Bouton pour switcher de containers à support ou de support a containers
		         otherButtons :newService.udtOtherButtons()
		};
		return editDatatableConfig;
	};

	var getProcessColumnOk = function(){
		var columns = [
		         {
		        	 "header":Messages("processes.table.inputContainerCode"),
		        	 "property":"inputContainerCode",
		        	 "order":true,
		        	 "hide":true,
		        	 "position":1,
		        	 "type":"text"
		         },
		         {
		 			"header":Messages("containers.table.contents.length"),
		 			"property":"contents.length",
		 			"url":"'/api/containers/'+inputContainerCode",
		 			"order":true,
		 			"hide":true,
		 			"position":2,
		 			"type":"number"
			 	},
		         {
		        	 "header":Messages("processes.table.sampleCode"),
		        	 "property":"sampleCodes",
		        	 "order":true,
		        	 "hide":true,
		        	 "position":2.01,
		        	 "type":"text"
		         },			        
		         {
		        	"header" : Messages("containers.table.tags"),
		 			"property" : "sampleOnInputContainer.properties.tag.value",
		 			"type" : "text",
		 			"order" : true,
		 			"hide" : true,
		 			"position":4,
		 			"groupMethod" : "collect",
		 			"render" : "<div list-resize='cellValue | unique' list-resize-min-size='3'>",		        	
		         },		         
		         {
		        	 "header" : Messages("processes.table.typeCode"),
		 			"property" : "typeCode",
		 			"filter" : "codes:'type'",
		 			"order" : true,
		 			"hide" : true,
		 			"position" : 9,
		 			"type" : "text"		        	
		         },
		         {
		        	 "header":Messages("processes.table.stateCode"),
		        	 "property":"state.code",
		        	 "order":true,
		        	 "hide":true,
		        	 "filter": "codes:'state'",
		        	 "position":30,
		        	 "type":"text"
		         },
		         {
		        	 "header":Messages("processes.table.code"),
		        	 "property":"code",
		        	 "order":true,
		        	 "hide":true,
		        	 "position":33,
		        	 "type":"text"
		         },
		         {
		        	 "header":Messages("processes.table.creationDate"),
		        	 "property":"traceInformation.creationDate",
		        	 "order":true,
		        	 "hide":true,
		        	 "position":34,
		        	 "type":"date"
		         },
		         {
		        	 "header":Messages("processes.table.projectCode"),
		        	 "property":"projectCodes",
		        	 "order":true,
		        	 "hide":true,
		        	 "position":37,
		        	 "type":"text"
		         },
		         {
		         "header" : Messages("processes.table.comments"),
					"property" : "comments[0].comment",
					"position" : 500,
					"order" : false,
					"edit" : true,
					"hide" : true,
					"type" : "text"
			        }
		 ];
		
		
		return columns;
	};
	
	var	datatableConfigProcessOK = {
			 columns: [],
	         pagination:{
	        	 active:true,
	        	 mode:'local',
	        	 numberRecordsPerPage:50
	         },		
	         search:{
	        	 active:false
	         },
	         order:{
	        	 mode:'local',
	        	 active:true
	         },
	         lines:{
	        	trClass:function(data, line){
	        		return 'success';	        			        	
	        	} 
	         }
	};
	
	var newService = {
				datatable : undefined,
				messages:messages(),
				processesDoneWithSuccess : [],
				getEditDatatableConfig : getEditDatatableConfig,
				getEditDatatableConfigWithScope : getEditDatatableConfigWithScope,
				getProcessColumnOk : getProcessColumnOk,
				datatableConfigProcessOK:datatableConfigProcessOK,
				processType : undefined,
				processPropertyColumns :[],
				computeProcessColumns : function(properties){
					this.processPropertyColumns = [];
					if(properties){										
						this.processPropertyColumns = properties.map(function(propertyDefinition){
							return propertyDefHelpers.getProcessUDTColumn(propertyDefinition);
							});					
					}
					
				},
				setDatatableProcessOk : function(){
					var columns = this.getProcessColumnOk().concat(this.processPropertyColumns);
					this.datatable = datatable(this.datatableConfigProcessOK);
					this.datatable.setColumnsConfig(columns);
					this.datatable.setData(this.processesDoneWithSuccess);				
				},
				initProcessType : function(processTypeCode) {
					this.processesDoneWithSuccess = [];
					var promise = $q.when(this);
					if(processTypeCode){
						promise = $http.get(jsRoutes.controllers.processes.api.ProcessTypes.get(processTypeCode).url,{newService:this})
							.then(function(result){
								var newService = result.config.newService;
								newService.processType = result.data;
								newService.computeProcessColumns(newService.processType.propertiesDefinitions);
								return newService;
						});				
					}		
					return promise;
				}
			};		
		return newService;
	
}]).factory('processesNewFromSamplesService', ['$q', '$http', '$parse', '$filter', 'mainService', 'lists', 'datatable', 'processesNewService','messages',
    function($q, $http, $parse, $filter, mainService, lists, datatable, processesNewService,messages) {
	
	var getDefaultColumns = function() {
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
			"hide":true,
			"position":6,
			"type":"text",			
			"groupMethod":"collect:true"
		});	
		columns.push({
			"header" : Messages("processes.table.comments"),
			"property" : "comments[0].comment",
			"position" : 500,
			"order" : false,
			"edit" : true,
			"editTemplate":"<textarea class='form-control' #ng-model rows='3'></textarea>",
			"hide" : true,
			"type" : "text"
		});
		return columns;
	};
	
	
	var sampleNewService = {
		getDefaultColumns : getDefaultColumns,
		sampleViewData : undefined,
		computeData : function(samples, isFirst){
			this.sampleViewData = {};
			samples.forEach(function(sample){
				if(isFirst){
					sample.properties = null;
					sample.comments = [];
				}
				this.sampleViewData[sample.code]={sample:sample};
			},this);
			return samples;
		},
		swithToSampleErrorView : function(){
			var samples = [];
			this.messages.setError("save");
			for(var key in this.sampleViewData){
				if(this.sampleViewData[key].onError){
					samples.push(this.sampleViewData[key].sample);
					this.messages.addDetails(this.sampleViewData[key].errors);						
				}
			}
			samples = $filter('orderBy')(samples, ['sampleCodes']);
			
			this.datatable = datatable(this.getEditDatatableConfig(this));
			this.datatable.setColumnsConfig(this.getDefaultColumns().concat(this.processPropertyColumns))
			this.datatable.setData(samples);
		},
		swithToGlobalErrorView : function(){
			var samples = [];
			this.messages.setError("save");
			for(var key in this.sampleViewData){
				samples.push(this.sampleViewData[key].sample);				
			}
			samples = $filter('orderBy')(samples, ['sampleCodes']);
			
			this.datatable = datatable(this.getEditDatatableConfig(this));
			this.datatable.setColumnsConfig(this.getDefaultColumns().concat(this.processPropertyColumns))
			this.datatable.setData(samples);
		},
		udtSaveCallback : function(datatable){
			this.messages.clear();
			var data = this.computeData(datatable.getData());
			var allProcesses = [];
			data.forEach(function(value, index){
				var process = {};
				process.typeCode = this.processType.code;
				process.categoryCode = this.processType.category.code;
				process.properties = value.properties;
				process.comments = value.comments;	
				process.sampleCodes = [value.code];
				allProcesses.push({data:process, index:index});
			},this);
			
			var nbElementByBatch = Math.ceil(allProcesses.length / 6);
			var queries = [];
	        for (var i = 0; i < 6 && allProcesses.length > 0; i++) {
	        	var subsetOfProcesses = allProcesses.splice(0, nbElementByBatch);
	        	queries.push($http.post(jsRoutes.controllers.processes.api.Processes.saveBatch("from-sample").url, subsetOfProcesses,{subsetOfProcesses:subsetOfProcesses}));
	        }
			var $that = this;
			$q.all(queries).then(function(results) {
				var atLeastOneError = false;
				
				results.forEach(function(result){
					if (result.status !== 200) {
						console.log("Batch in error");					
		            } else {
		            	result.data.forEach(function(data){
		            		
		            		if (data.status === 200) {
		            			$that.sampleViewData[data.data[0].sampleCodes[0]].trClass = "success";
		            			$that.processesDoneWithSuccess = $that.processesDoneWithSuccess.concat(data.data);
		            		}else{
		            			var process = $filter('filter')(result.config.subsetOfProcesses,{index:data.index}, true)[0];
		            			$that.sampleViewData[process.data.sampleCodes[0]].trClass = "danger";
		            			$that.sampleViewData[process.data.sampleCodes[0]].onError = true;
		            			$that.sampleViewData[process.data.sampleCodes[0]].errors = {};
		            			$that.sampleViewData[process.data.sampleCodes[0]].errors[process.data.sampleCodes[0]] = data.data;
		            			atLeastOneError = true;		            					            			
		            		}	            		
		            	});	            	
		            }
				});
			
				if(atLeastOneError){
					$that.swithToSampleErrorView();
	    		}else{
	    			$that.setDatatableProcessOk();		    					    		
	    		}
				
			},function(result){
				$that.swithToGlobalErrorView();						
			});
		},
		udtRemoveCallback : function(datatable){
			mainService.getBasket().reset();
			datatable.getData().forEach(function(elt){
				mainService.getBasket().add(elt);
			});				 
			this.computeData(mainService.getBasket().get());	 
		},
		udtTrClass : function(data, line){
    		if(this.sampleViewData[data.code]){
    			return this.sampleViewData[data.code].trClass
    		}else{
    			return '';
    		}    		
    	},
    	udtOtherButtons : function(){return undefined;},
    	/**
		 * initialise the service
		 */
		init : function(processTypeCode) {
			this.initProcessType(processTypeCode).then(function(newService){
				if(newService.processType){
					var data = newService.computeData(mainService.getBasket().get(),true);
					newService.datatable = datatable(newService.getEditDatatableConfig(newService));
					newService.datatable.setColumnsConfig(newService.getDefaultColumns().concat(newService.processPropertyColumns))
					newService.datatable.setData(data);
				}
			});
		}
	};
	
	
	
	return function(){	
		var newService = $.extend(true,{},sampleNewService,processesNewService);
		newService.messages = messages(); //need new messages manager for each instance
		return newService;
	};
	
}]).factory('processesNewFromContainersService', [ '$q', '$http', '$parse', '$filter', 'mainService', 'lists', 'datatable', 'processesNewService', 
    function($q, $http, $parse, $filter, mainService, lists, datatable, processesNewService) {
	
	var getDefaultColumns = function(view) {
		var columns = [];

		if("container" === view){

			columns.push({
				"header":Messages("processes.table.supportCode"),
				"property":"support.code",
				"order":true,
				"hide":true,
				"position":1,
				"type":"text"
			});

			columns.push({
				"header":Messages("processes.table.line"),
				"property":"support.line",
				"order":true,
				"hide":true,
				"position":2,
				"type":"text"
			});
			columns.push({
				"header":Messages("processes.table.columns"),
				"property":"support.column*1",
				"order":true,
				"hide":true,
				"position":3,
				"type":"number"
			});			

		}else{
			columns.push({
				"header":Messages("processes.table.supportCode"),
				"property":"support.code",
				"order":true,
				"hide":true,
				"position":1,
				"type":"text"
			});

		}

		columns.push({
			"header":Messages("processes.table.projectCode"),
			"property":"projectCodes",
			"order":true,
			"hide":true,
			"position":4,
			"render":"<div list-resize='value.data.projectCodes | unique' list-resize-min-size='3'>",
			"type":"text"
		});
		columns.push({
			"header":Messages("processes.table.sampleCode"),
			"property":"sampleCodes",
			"order":true,
			"hide":true,
			"position":5,
			"render":"<div list-resize='value.data.sampleCodes | unique' list-resize-min-size='3'>",
			"type":"text"
		});

		columns.push({
			"header":Messages("containers.table.sampleTypes"),
			"property":"contents",
			"order":false,
			"hide":false,
			"position":5.01,
			"type":"text",
			"filter":"getArray:'sampleTypeCode' | unique | codes:\"type\"",
			"groupMethod":"collect"
		});

		columns.push({
			"header":Messages("containers.table.contents.length"),
			"property":"contents.length",
			"order":true,
			"hide":true,
			"position":5.05,
			"type":"number"
		});
		columns.push({
			"header":Messages("containers.table.stateCode"),
			"property":"state.code",
			"order":true,
			"hide":true,
			"position":6,
			"filter": "codes:'state'",
			"type":"text"
		});
		columns.push({
			"header" : Messages("processes.table.comments"),
			"property" : "comments[0].comment",
			"position" : 500,
			"order" : false,
			"edit" : true,
			"editTemplate":"<textarea class='form-control' #ng-model rows='3'></textarea>",
			"hide" : true,
			"type" : "text"
		});
		return columns;
	};

	
	var containerNewService = {
			containerViewData : undefined,
			supportViewData : undefined,
			containerErroView : false,
			getDefaultColumns : getDefaultColumns,
			computeData : function(){
				var containerViewData = this.containerViewData = {};
				var supportViewData = this.supportViewData = {};

				var containerCodes = [];
				containerCodes = containerCodes.concat(mainService.getBasket().get());

				if(containerCodes.length > 0){
					var nbElementByBatch = Math.ceil(containerCodes.length / 6); //6 because 6 request max in parrallel with firefox and chrome
					var queries = [];
					for (var i = 0; i < 6 && containerCodes.length > 0; i++) {
						var subContainerCodes = containerCodes.splice(0, nbElementByBatch);
						queries.push( $http.get(jsRoutes.controllers.containers.api.Containers.list().url,{params:{codes:subContainerCodes}}) );
					}

					return $q.all(queries).then(function(results) {
						var allData = [];
						results.forEach(function(result){
							allData = allData.concat(result.data);
						});

						allData.forEach(function(data){
							data.properties = null;
							data.comments = [];
							containerViewData[data.code]=data;
							containerViewData[data.code].code = [data.code];
							if(supportViewData[data.support.code]){
								supportViewData[data.support.code].code = supportViewData[data.support.code].code.concat(data.code);
								supportViewData[data.support.code].projectCodes = supportViewData[data.support.code].projectCodes.concat(data.projectCodes);
								supportViewData[data.support.code].sampleCodes = supportViewData[data.support.code].sampleCodes.concat(data.sampleCodes);
								supportViewData[data.support.code].contents = supportViewData[data.support.code].contents.concat(data.contents);
							}else{
								supportViewData[data.support.code] = $.extend(true,{},data);						
							}	
						});


					});		
				}
			},
			swithView : function(){		
				if(this.supportView){
					this.swithToContainerView();
				}else{
					this.swithToSupportView()
				}
			},
			swithToContainerErrorView : function(){
				var containers = [];
				this.messages.setError("save");
				for(var key in this.containerViewData){
					if(this.containerViewData[key].onError){
						containers.push(this.containerViewData[key]);
						this.messages.addDetails(this.containerViewData[key].errors);
					}
				}
				containers = $filter('orderBy')(containers, ['support.code', 'support.column*1', 'support.line']);
				this.datatable.setColumnsConfig(this.getDefaultColumns("container").concat(this.processPropertyColumns));
				this.datatable.setData(containers);
				this.containerErroView = true;
			},
			swithToGlobalErrorView : function(){
				var containers = [];
				this.messages.setError("save");
				for(var key in this.containerViewData){
					containers.push(this.containerViewData[key]);					
				}
				containers = $filter('orderBy')(containers, ['support.code', 'support.column*1', 'support.line']);
				this.datatable.setColumnsConfig(this.getDefaultColumns("container").concat(this.processPropertyColumns));
				this.datatable.setData(containers);
				this.containerErroView = true;
			},
			swithToContainerView : function(){
				var containers = [];
				for(var key in this.containerViewData){
					containers.push(this.containerViewData[key]);
				}
				containers = $filter('orderBy')(containers, ['support.code', 'support.column*1', 'support.line']);
				this.datatable.setColumnsConfig(this.getDefaultColumns("container").concat(this.processPropertyColumns));
				this.datatable.setData(containers);	
				this.supportView = false;

			},
			swithToSupportView : function(){
				var supports = [];
				for(var key in this.supportViewData){
					supports.push(this.supportViewData[key]);
				}
				supports = $filter('orderBy')(supports, 'support.code');
				this.datatable.setColumnsConfig(this.getDefaultColumns("support").concat(this.processPropertyColumns));
				this.datatable.setData(supports);			
				this.supportView = true;
			},
			udtSaveCallback : function(datatable,$scope){
				$scope.spinner=true;
				this.messages.clear();
				var data = datatable.getData();
				var allProcesses = [];
				data.forEach(function(value, index){
					var process = {};
					process.typeCode = this.processType.code;
					process.categoryCode = this.processType.category.code;
					process.properties = value.properties;
					process.inputContainerSupportCode = value.support.code;
					process.comments = value.comments;
					value.code.forEach(function(containerCode){
						var processContainer =  $.extend(true,{},process);
						processContainer.inputContainerCode = containerCode;
						allProcesses.push({data:processContainer, index:index});
					})			
				},this);
				
				var nbElementByBatch = Math.ceil(allProcesses.length / 6);
				var queries = [];
		        for (var i = 0; i < 6 && allProcesses.length > 0; i++) {
		        	var subsetOfProcesses = allProcesses.splice(0, nbElementByBatch);
		        	queries.push($http.post(jsRoutes.controllers.processes.api.Processes.saveBatch("from-container").url, subsetOfProcesses,{subsetOfProcesses:subsetOfProcesses}));
		        }
				var $that = this;
				datatable.setSpinner(true);
				$q.all(queries).then(function(results) {
					datatable.setSpinner(true);
					$that.containerErroView = false;
					var atLeastOneError = false;
					
					results.forEach(function(result){
						if (result.status !== 200) {
							console.log("Batch in error");					
			            } else {
			            	result.data.forEach(function(data){
			            		
			            		if (data.status === 200) {
			            			$that.containerViewData[data.data[0].inputContainerCode].trClass = "success";
			            			if($that.supportViewData[data.data[0].inputContainerSupportCode].trClass !== "danger"){
			            				$that.supportViewData[data.data[0].inputContainerSupportCode].trClass = "success";
			            			}
			            			$that.processesDoneWithSuccess = $that.processesDoneWithSuccess.concat(data.data);
			            		}else{
			            			var process = $filter('filter')(result.config.subsetOfProcesses,{index:data.index}, true)[0];
			            			$that.containerViewData[process.data.inputContainerCode].trClass = "danger";
			            			$that.containerViewData[process.data.inputContainerCode].onError = true;
			            			$that.containerViewData[process.data.inputContainerCode].errors = {};
			            			$that.containerViewData[process.data.inputContainerCode].errors[process.data.inputContainerCode] = data.data;
			            			$that.supportViewData[process.data.inputContainerSupportCode].trClass = "danger";
			            			atLeastOneError = true;
			            		}	            		
			            	});	            	
			            }
					});
					if(atLeastOneError){
						$that.swithToContainerErrorView();
		    		}else{
		    			$that.setDatatableProcessOk();		    					    		
		    		}
					$scope.spinner=false;
				},function(result){
					$that.swithToGlobalErrorView();						
				});
			},
			udtRemoveCallback : function(datatable){
				mainService.getBasket().reset();
				datatable.getData().forEach(function(elt){
					mainService.getBasket().add(elt.code);
				});
				this.computeData();	        		 
			},
			udtTrClass:function(data, line){
        		if(this.supportView && this.supportViewData[data.support.code]){
        			return this.supportViewData[data.support.code].trClass
        		}else if(this.containerViewData[data.code[0]]){	        			
        			return this.containerViewData[data.code[0]].trClass	        			
        		}else{
        			return '';
        		}	        		
        	}, 
        	udtOtherButtons : function(){
        		return {
        			active:true,
        			template:''
        				+' <button ng-click="newService.swithView()" ng-disabled="newService.loadView"  class="btn btn-info" ng-switch="newService.supportView" ng-if="!newService.containerErroView">'+Messages("baskets.switchView")+
        				' '+'<b ng-switch-when="true" class="switchLabel">'+
        				Messages("baskets.switchView.containers")+'</b>'+
        				'<b ng-switch-when="false" class="switchLabel">'+Messages("baskets.switchView.supports")+'</b></button></button>'
        		};
        	},

			/**
			 * initialise the service
			 */
			init : function(processTypeCode,$scope) {
				this.initProcessType(processTypeCode).then(function(newService){
					if(newService.processType){
						newService.supportView= false;
						newService.computeData().then(function(){
							console.log($scope);
							newService.datatable = datatable(newService.getEditDatatableConfigWithScope(newService,$scope));
							if(!newService.supportView){
								newService.swithToContainerView();
							}else{
								newService.swithToSupportView()
							}
							$scope.spinner=false;
						});
						
					}
				});
				
			}
	}
	return function(){	
		var newService = $.extend(true,{},containerNewService,processesNewService);
		return newService;
	};	

}]).factory('assignToContainerService', [ '$q', '$http', '$parse', '$filter', 'mainService', 'messages', 'lists', 'datatable', 'propertyDefHelpers',
    function($q, $http, $parse, $filter, mainService, messages, lists, datatable, propertyDefHelpers) {
	
	var getDefaultColumns = function(view) {
		var columns = [];

		columns.push({
			"header":Messages("processes.table.supportCode"),
			"property":"support.code",
			"order":true,
			"hide":true,
			"position":1,
			"type":"text"
		});

		columns.push({
			"header":Messages("processes.table.line"),
			"property":"support.line",
			"order":true,
			"hide":true,
			"position":2,
			"type":"text"
		});
		columns.push({
			"header":Messages("processes.table.columns"),
			"property":"support.column*1",
			"order":true,
			"hide":true,
			"position":3,
			"type":"number"
		});			

		columns.push({
			"header":Messages("processes.table.projectCode"),
			"property":"projectCodes",
			"order":true,
			"hide":true,
			"position":4,
			"render":"<div list-resize='value.data.projectCodes | unique' list-resize-min-size='3'>",
			"type":"text"
		});
		columns.push({
			"header":Messages("processes.table.sampleCode"),
			"property":"sampleCodes",
			"order":true,
			"hide":true,
			"position":5,
			"render":"<div list-resize='value.data.sampleCodes | unique' list-resize-min-size='3'>",
			"type":"text"
		});

		columns.push({
			"header":Messages("containers.table.sampleTypes"),
			"property":"contents",
			"order":false,
			"hide":false,
			"position":5.001,
			"type":"text",
			"filter":"getArray:'sampleTypeCode' | unique | codes:\"type\"",
			"groupMethod":"collect"
		});
		
		columns.push({
			"header":Messages("processes.table.availableProcesses"),
			"property":"processCode",
			"order":true,
			"edit":true,
			"required":true,
			"editTemplate":"<div class='form-control' bt-select  bt-options='opt.code as opt.code for opt in assignService.getProcesses(value.data)' #ng-model udt-change='assignService.selectProcess(value.data)' auto-select></div>",
			"position":5.002,
			"type":"text"
		});
		columns.push({
			"header" : Messages("processes.table.typeCode"),
			"property" : "processTypeCode",
			"filter" : "codes:'type'",
			"order" : true,
			"hide" : false,
			"watch":true,
			"groupMethod" : "unique",
			"position" : 5.003,
			"type" : "text"
		});
		/*
		columns.push({
			"header":Messages("containers.table.contents.length"),
			"property":"contents.length",
			"order":true,
			"hide":true,
			"position":5.05,
			"type":"number"
		});
		*/
		columns.push({
			"header" : Messages("processes.table.process.comments"),
			"property" : "processComments[0].comment",
			"position" : 9,
			"order" : false,
			"edit" : false,
			"hide" : true,
			"watch":true,
			"type" : "text"
		});
		/*
		columns.push({
			"header":Messages("processes.table.container.state"),
			"property":"state.code",
			"order":true,
			"hide":true,
			"position":10,
			"filter": "codes:'state'",
			"type":"text"
		});
		*/
		columns.push({
			"header" : Messages("processes.table.container.comments"),
			"property" : "comments[0].comment",
			"position" : 500,
			"order" : false,
			"edit" : false,
			"hide" : true,
			"type" : "text"
		});
		return columns;
	};

	var	getDatatableConfig = function(service){
		var datatableConfig = {
				 columns: [],
		         pagination:{
		        	 active:false
		         },		
		         search:{
		        	 active:false
		         },
		         order:{
		        	 mode:'local',
		        	 active:true
		         },
		         edit:{  		
		        	 active:true,
		        	 columnMode:false,
		        	 byDefault : true,
		        	 showButton:false
		         },
		         save:{
		        	 active: true,
		        	 withoutEdit:true,
		        	 showButton : true,
		        	 mode:"local",
		        	 changeClass : false,
		        	 callback : function(datatable){
		        		 service.udtSaveCallback(datatable);
		        	 }
		         },
		         remove:{
		        	 active:true,
		        	 mode:'local',
		        	 withEdit:true,
		        	 callback : function(datatable){
		        		 service.udtRemoveCallback(datatable);
		        	 }
		         },
		         lines:{
		        	trClass:function(data,line){
		        		return service.udtTrClass(data,line);
		        	}
		         },		        
		         cancel:{
		        	 active:false
		         }
		};
		return datatableConfig;
	}
	
	
	var assignService = {
			getDatatableConfig:getDatatableConfig,
			getDefaultColumns:getDefaultColumns,
			form :{},
			lists:lists,
			messages:messages(),
			additionalProcessFilters : undefined,
			additionalProcessColumns : undefined,
			processType : undefined,
			isProcessFiltered : false,
			selectedSampleCodes : [],
			containerData : {},
			
			//containerSampleCodes : {},
			//containerProcesses : {},
			
			udtSaveCallback : function(datatable){
				this.messages.clear();
				var data = datatable.getData();
				var allProcesses = [];
				var nbContainerByProcess = {};
				data.forEach(function(container, index){
					var selectedProcess = $filter('filter')(this.containerData[container.code].processes, {code:container.processCode},true)[0];
					selectedProcess.inputContainerCode = container.code;
					selectedProcess.inputContainerSupportCode = container.support.code;
					allProcesses.push({data:selectedProcess, index:index});
					
					if(!nbContainerByProcess[selectedProcess.code]){
						nbContainerByProcess[selectedProcess.code] = 0;
					}
					nbContainerByProcess[selectedProcess.code]++;
					
				},this);
				var errors = false;
				for(var key in nbContainerByProcess){
					if(nbContainerByProcess[key] > 1){
						var error = {};
						error[key]=[Messages("processes.msg.assign.error.severalcontainer")];
						this.messages.addDetails(error);
						errors = true;
					}
				}
				
				if(!errors){
				
					var nbElementByBatch = Math.ceil(allProcesses.length / 6);
					var queries = [];
			        for (var i = 0; i < 6 && allProcesses.length > 0; i++) {
			        	var subsetOfProcesses = allProcesses.splice(0, nbElementByBatch);
			        	queries.push($http.put(jsRoutes.controllers.processes.api.Processes.updateBatch().url, subsetOfProcesses,{subsetOfProcesses:subsetOfProcesses}));
			        }
					var $that = this;
					$q.all(queries).then(function(results) {
						
						results.forEach(function(result){
							if (result.status !== 200) {
								console.log("Batch in error");					
				            } else {
				            	result.data.forEach(function(data){
				            		
				            		if (data.status === 200) {
				            			$that.containerData[data.data.inputContainerCode].trClass = "success";			            			
				            		}else{
				            			var process = $filter('filter')(result.config.subsetOfProcesses,{index:data.index}, true)[0];
				            			$that.containerData[process.data.inputContainerCode].trClass = "danger";
				            			$that.containerData[process.data.inputContainerCode].onError = true;
				            			$that.containerData[process.data.inputContainerCode].errors = {};
				            			$that.containerData[process.data.inputContainerCode].errors[process.data.inputContainerCode] = data.data;
				            			atLeastOneError = true;
				            		}	 
				            		           		
				            	});	            	
				            }
						});
						
					},function(result){
						$that.messages.setError("save");
						$that.messages.showDetails = true;
						$that.datatable.setColumnsConfig($that.getDefaultColumns().concat($that.additionalProcessColumns));		
						
					});
				}else{
					this.messages.setError("save");
					this.messages.showDetails = true;
					this.datatable.setColumnsConfig(this.getDefaultColumns().concat(this.additionalProcessColumns));		
					
				}
			},
			udtRemoveCallback : function(datatable){
				mainService.getBasket().reset();
				datatable.getData().forEach(function(elt){
					mainService.getBasket().add(elt.code);
				});
				this.computeData();	        		 
			},
			udtTrClass:function(data, line){
        		if(this.containerData[data.code]){	        			
        			return this.containerData[data.code].trClass	        			
        		}else{
        			return '';
        		}	        		
        	}, 
			initProcessType : function(processTypeCode) {
				var promise = $q.when(this);
				if(processTypeCode){
					promise = $http.get(jsRoutes.controllers.processes.api.ProcessTypes.get(processTypeCode).url,{service:this})
						.then(function(result){
							var service = result.config.service;
							service.processType = result.data;
							service.initProcessAttributes(service.processType.propertiesDefinitions);
							return service;
					});				
				}		
				return promise;
			},
			
			initProcessAttributes : function(propertiesDefinitions) {
				this.additionalProcessFilters = [];
				this.additionalProcessColumns = [];
				var formFilters = [];
				var allFilters = undefined;
				var nbElementByColumn = undefined;
				this.isProcessFiltered = false;	
				//filters
				if(angular.isArray(propertiesDefinitions) && propertiesDefinitions.length > 0){
					allFilters = propertiesDefinitions.map(function(pDef){
						return propertyDefHelpers.getHtmlFilter(pDef,"assignService");
					});				
				 
					if (angular.isDefined(allFilters)) {
						nbElementByColumn = Math.ceil(allFilters.length / 5); //5 columns
						for (var i = 0; i < 5 && allFilters.length > 0; i++) {
							formFilters.push(allFilters.splice(0, nbElementByColumn));
						}
						//complete to 5 five element to have a great design 
						while (formFilters.length < 5) {
							formFilters.push([]);
						}
						this.isProcessFiltered = true;					
					}
					this.additionalProcessFilters = formFilters;
					
					
					this.additionalProcessColumns =  propertiesDefinitions.map(function(pDef){
						var column = propertyDefHelpers.getProcessUDTColumn(pDef);
						column.edit = false;
						return column;
					});
						
				}
				
			},	
			getAddProcessFiltersToForm : function() {
				return this.additionalProcessFilters;
			},
			computeData : function(){
				this.containerData = {};
				
				var containerCodes = [];
				containerCodes = containerCodes.concat(mainService.getBasket().get());

				if(containerCodes.length > 0){
					var nbElementByBatch = Math.ceil(containerCodes.length / 6); //6 because 6 request max in parrallel with firefox and chrome
					var queries = [];
					for (var i = 0; i < 6 && containerCodes.length > 0; i++) {
						var subContainerCodes = containerCodes.splice(0, nbElementByBatch);
						queries.push( $http.get(jsRoutes.controllers.containers.api.Containers.list().url,{params:{codes:subContainerCodes}}) );
					}
					var insideService = this;
					return $q.all(queries).then(function(results) {
						var allData = [];
						results.forEach(function(result){
							allData = allData.concat(result.data);
						});
						
						insideService.setSelectedSampleCodes(allData);
						insideService.udtData = allData;
						return allData;
					});		
				}
			},
			reset : function(){
				this.form = {};
			},
			/**
			 * Search processes and assign it to each container.
			 * The assignation is based on the sampleCode of the processes
			 */
			searchProcesses : function(){
				this.form.stateCode = 'IW-C';
				this.form.sampleCodes = this.selectedSampleCodes;
				this.form.typeCode = this.processType.code;
				//clean old process search
				for(var key in this.containerData){
					this.containerData[key].processes = [];
				}
				$http.get(jsRoutes.controllers.processes.api.Processes.list().url,{params:this.form,service:this})
					.then(function(result){
						var service = result.config.service;
						result.data.forEach(function(process){
							process.sampleCodes.forEach(function(sampleCode){
								Object.keys(this.containerData)
									.forEach(function(containerCode){
										if(this.containerData[containerCode].sampleCodes.indexOf(sampleCode) > -1){
											this.containerData[containerCode].processes = this.containerData[containerCode].processes.concat(process); 
											//not used push because auto-select does not work. angular need new array ref to run watch.
										}
								}, this);								
							},this);							
						}, service);
						//reset all data
						service.datatable.setData(service.udtData);
					});
			},
			setSelectedSampleCodes : function(selectedContainers){
				this.selectedSampleCodes = [];
				var queries = [];
				
				selectedContainers.forEach(function(container){
					
					queries.push($http.get(jsRoutes.controllers.samples.api.Samples.list().url,{params:{codes:container.sampleCodes},service:this, container:container})
					.then(function(result){
						var service = result.config.service;
						var container = result.config.container;
						var containerSampleCodes = container.sampleCodes;
							
						result.data.forEach(function(sample){
							if(sample.life) containerSampleCodes = containerSampleCodes.concat(sample.life.path.split(','));							
						});
						
						if(!service.containerData[container.code])service.containerData[container.code] = {};
						service.containerData[container.code].sampleCodes = containerSampleCodes;
						service.selectedSampleCodes = Array.from(new Set(service.selectedSampleCodes.concat(containerSampleCodes)));
												 
					}));
					
				},this);
				var that = this;
				$q.all(queries)
					.then(function(results){									
						that.searchProcesses();						
					})				
			},
			getProcesses : function(container){
				if(container && this.containerData[container.code] && this.containerData[container.code].processes)return this.containerData[container.code].processes;
				return null;
			},
			selectProcess : function(container){
				var selectedProcess = undefined;
				if(container.processCode !== undefined && container.processCode !== null){
					selectedProcess = $filter('filter')(this.containerData[container.code].processes, {code:container.processCode},true)[0];
				}
				if(selectedProcess){
					container.properties = selectedProcess.properties;
					container.processTypeCode = selectedProcess.typeCode;
					container.processComments = selectedProcess.comments;
				} else {
					container.properties = null;
					container.processTypeCode = null;
					container.processComments = null;
				}
				
			},
			/**
			 * initialise the service
			 */
			init : function(processTypeCode) {
				this.reset();
				this.initProcessType(processTypeCode).then(function(service){
					if(service.processType){
						service.computeData().then(function(selectedContainers){
							service.datatable = datatable(service.getDatatableConfig(service));
							service.datatable.setColumnsConfig(service.getDefaultColumns().concat(service.additionalProcessColumns));		
							service.datatable.setData(selectedContainers);
						});
					}
				});
			}
	}

	
	return assignService;

}]).factory('processGraphService', [ '$q', '$http', '$parse', '$filter',  'messages', 'lists', 
    function($q, $http, $parse, $filter, messages, lists) {
	
	var newNode = function(experimentNode){
		return  {experimentType:experimentNode.experimentType, parentNodes:(experimentNode.previousExperimentTypes)?experimentNode.previousExperimentTypes:[], childNodes:[]};
	};
	
	var graphNodes = {};
	var service = {
			experimentTypes : undefined,
			document : undefined,
			
			changeProcessType : function(processTypeCode){
				
				$http.get(jsRoutes.controllers.processes.api.ProcessTypes.get(processTypeCode).url,{service:this}).then(function(result){
					var nodeQueries = [];	
					var processExperimentTypes = new Map();
					var $this = result.config.service;
					result.data.experimentTypes.forEach(function(experimentType){
						if(this.get(experimentType.experimentTypeCode) === undefined){
							this.set(experimentType.experimentTypeCode, [experimentType.positionInProcess]);
						}else{
							this.get(experimentType.experimentTypeCode).push(experimentType.positionInProcess);
						}
						nodeQueries.push($http.get(jsRoutes.controllers.experiments.api.ExperimentTypeNodes.get(experimentType.experimentTypeCode).url)
								.then(function(result){
									if(!graphNodes[result.data.experimentType.code])
										graphNodes[result.data.experimentType.code] = newNode(result.data);
									return result.data;
								})								
						);
							
						
						
					},processExperimentTypes);
					$q.all(nodeQueries).then(function(expNodes){
						expNodes.forEach(function(experimentNode){
							graphNodes[experimentNode.experimentType.code].parentNodes.forEach(function(parent){
								if(graphNodes[parent.code] && $filter('filter')(graphNodes[parent.code].childNodes,{code:this.code}).length === 0)
									graphNodes[parent.code].childNodes.push(this);
							}, experimentNode.experimentType) 
						});
						var graphElements  = $this.computeGraphElements(processExperimentTypes);
						$this.initCytoscape(graphElements);
					});
					
					
					
				})
			
			},
			computeGraphElements : function(processExperimentTypes){
				//nodes
				var graphElements = [];
				
				var getFaveColor = function(processExperimentTypes, key){
					if(processExperimentTypes.get(key)[0] > -1 )return '#6FB1FC';
					else return '#BDBDBD';
				}
				
				var getFaveWith = function(processExperimentTypes, key){
					if(processExperimentTypes.get(key)[0] > -1 )return '150';
					else return '100';
				}
				
				var getFaveFontSize = function(processExperimentTypes, key){
					if(processExperimentTypes.get(key)[0] > -1 )return '11';
					else return '9';
				}
				
				for(var i = 0; i < Array.from(processExperimentTypes.entries()).length; i++) {
					var key = Array.from(processExperimentTypes.entries())[i][0];
					var currentNode = graphNodes[key];
					var currentExperimentType = graphNodes[key].experimentType;
					if(currentExperimentType.category.code === 'transformation'){
						currentExperimentType.id = currentExperimentType.code;
						currentExperimentType.label = currentExperimentType.name;
						currentExperimentType.faveColor = getFaveColor(processExperimentTypes, key);
						currentExperimentType.faveShape="ellipse";
						currentExperimentType.faveWith = getFaveWith(processExperimentTypes, key);
						currentExperimentType.faveFontSize = getFaveFontSize(processExperimentTypes, key);
						graphElements.push({"data":currentExperimentType,"group":"nodes"});
					}else if(currentExperimentType.category.code === 'voidprocess'){
						currentExperimentType.id = currentExperimentType.code;
						currentExperimentType.label = 'None';
						currentExperimentType.faveColor = getFaveColor(processExperimentTypes, key);
						currentExperimentType.faveShape="ellipse";
						currentExperimentType.faveWith = getFaveWith(processExperimentTypes, key);
						currentExperimentType.faveFontSize = getFaveFontSize(processExperimentTypes, key);
						graphElements.push({"data":currentExperimentType,"group":"nodes"});
					}
					
				} 
				//edges
				
				var isDiffPositionIsOne = function(processExperimentTypes, keyParent, keyChild){
					return processExperimentTypes.get(keyParent).some(function(parentPos){
						return processExperimentTypes.get(keyChild).some(function(childPos){
								return (childPos - parentPos === 1);
						});
					});
					//return (processExperimentTypes.get(keyChild) - processExperimentTypes.get(keyParent) === 1);			
				}
				
				for(var i = 0; i < Array.from(processExperimentTypes.entries()).length; i++) {
					var key = Array.from(processExperimentTypes.entries())[i][0];
					var currentNode = graphNodes[key];
					var currentExperimentType = graphNodes[key].experimentType;
					if(currentExperimentType.category.code === 'transformation'
						|| currentExperimentType.category.code === 'voidprocess'){
						angular.forEach(currentNode.childNodes, function(childNode){
							var childExperimentType = childNode;
							if(childExperimentType.category.code === 'transformation' 
								&& (processExperimentTypes.get(childExperimentType.code) !== undefined && isDiffPositionIsOne(processExperimentTypes, key, childExperimentType.code))){
								var currentExperimentType = this;
								var edge = {
										"id":currentExperimentType.code+"-"+childExperimentType.code,
										"source":currentExperimentType.code,
										"target":childExperimentType.code
										
								}
								var faveColor = getFaveColor(processExperimentTypes, key);
								edge.faveColor=faveColor;
								graphElements.push({"data":edge,"group":"edges"})	
							}
						},currentExperimentType)
					}
				}
				
				return graphElements;
			},			
			initCytoscape : function(graphElements){
				
				var asynchGraph = function(document) {
					 return $q(function(resolve, reject) {
						 setTimeout(function() {
						 	 var cy = 
								cytoscape({
							          container: document.getElementById('graph'),
							          boxSelectionEnabled: false,
							          autounselectify: true,
							          wheelSensitivity: 0.1, //NGL-4082 ajout
							          layout: {
							            name: 'breadthfirst',
							            directed:true,
							            padding:5,
							            spacingFactor:0.6,
							          },
							          style: cytoscape.stylesheet()
								          .selector('node')
								            .css({
								              'shape': 'data(faveShape)',
								              'width': 'data(faveWith)',
								              'label': 'data(label)',
								              'text-valign': 'center',
								              //'text-outline-width': 2,
								              //'text-outline-color': 'data(faveColor)',
								              'background-color': 'data(faveColor)',
								              'color': '#fff',
								              'font-size':'data(faveFontSize)'  
								            })
								          .selector(':selected')
								            .css({
								              'border-width': 3,
								              'border-color': '#333'
								            })
								          .selector('edge')
								            .css({
								              'curve-style': "bezier", 
								              'opacity': 0.666,
								              'width': '3',
								              'label': 'data(label)',
								              'color': '#000',
								              'font-size':11,
								              'font-weight': 'bold',
								              'target-arrow-shape': 'triangle',
								              'source-arrow-shape': 'circle',
								              'line-color': 'data(faveColor)',
								              'source-arrow-color': 'data(faveColor)',
								              'target-arrow-color': 'data(faveColor)'
								            })
								            /*
								          .selector('edge.questionable')
								            .css({
								              'line-style': 'dotted',
								              'target-arrow-shape': 'diamond'
								            })
								            */
								          .selector('.faded')
								            .css({
								              'opacity': 0.25,
								              'text-opacity': 0
								            })
								      ,
							          elements : graphElements
							        });
						});
					 }, 1);
				};
				asynchGraph(this.document);
			},
			init : function(document){
				this.document = document;
				$http.get(jsRoutes.controllers.experiments.api.ExperimentTypes.list().url,{service:this}).then(function(result){
					var experimentTypes = {};
					result.data.forEach(function(expType){
						experimentTypes[expType.code] = expType;
					})
					result.config.service.experimentTypes = experimentTypes;
				})				
			}
			
	};
	
	return service;
		
}]);