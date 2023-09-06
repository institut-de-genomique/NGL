 "use strict";

 angular.module('ngl-projects.ProjectsServices', []).
 factory('projectsSearchService', ['$http', 'mainService', 'lists', 'datatable', function ($http, mainService, lists, datatable) {

 	var getDefaultColumns = function () {
 		var columns = [];

 		columns.push({
 			property: "code",
			header: "projects.code",
			groupMethod: "count:true",
 			type: "String",
 			order: true,
 			edit: false,
 			position: 1
 		});

 		columns.push({
 			property: "name",
 			header: "projects.name",
 			type: "String",
 			order: false,
 			edit: false,
 			position: 2
 		});

 		columns.push({
 			property: "bioinformaticParameters.fgGroup",
 			header: "projects.bioinformaticParameters.fgGroup",
 			type: "String",
 			order: true,
			 edit: false,
			 position: 3,
 			choiceInList: false
 		});

 		columns.push({
 			property: "bioinformaticParameters.fgPriority",
 			header: "projects.bioinformaticParameters.fgPriority",
 			type: "String",
 			order: true,
			 edit: false,
			 position: 4,
 			choiceInList: false
 		});

 		columns.push({
 			property: "bioinformaticParameters.biologicalAnalysis",
 			header: "projects.bioinformaticParameters.biologicalAnalysis",
 			type: "String",
 			filter: "codes:'boolean'",
			 order: true,
			 position: 5,
 			edit: false,
 			choiceInList: false
 		});

 		columns.push({
 			property: "state.code",
 			filter: "codes:'state'",
 			header: "projects.stateCode",
 			type: "String",
 			order: false,
			 edit: false,
			 position: 6,
 			choiceInList: false,
 			listStyle: 'bt-select',
 			possibleValues: 'listsTable.getStates()'
 		});

 		columns.push({
 			property: "traceInformation.creationDate",
 			header: "projects.traceInformation.creationDate",
 			type: "date",
			 order: true,
			 position: 7,
 			edit: false,
 			hide: true
 		});

 		return columns;
 	};

 	var isInit = false;

 	var initListService = function () {
 		if (!isInit) {
 			searchService.lists.refresh.bioinformaticParameters();
 			searchService.lists.refresh.states({
 				objectTypeCode: "Project",
 				display: true
 			}, 'statetrue');
 			searchService.lists.refresh.states({
 				objectTypeCode: "Project"
 			});
 			searchService.lists.refresh.types({
 				objectTypeCode: "Project"
 			});
 			/*TODO EJACOBY AD*/
			
 			searchService.lists.refresh.reportConfigs({
 				pageCodes: ["projects-addcolumns"]
 			}, "projects-addcolumns");
 			searchService.lists.refresh.filterConfigs({pageCodes:["projects-addfilters"]}, "projects-addfilters");
 			isInit = true;
 		}
 	};

 	var searchService = {
 		getDefaultColumns: getDefaultColumns,
 		datatable: undefined,
 		isRouteParam: false,
 		lists: lists,
 		form: undefined,
 		additionalColumns: [],
 		additionalFilters: [],
 		selectedAddColumns: [],

 		getAddColumnsToForm: function () {
 			if (this.additionalColumns.length === 0) {
 				this.initAdditionalColumns();
 			}
 			return this.additionalColumns;
 		},

 		initAdditionalColumns: function () {
 			this.additionalColumns = [];
 			this.selectedAddColumns = [];
 			this.mapAdditionnalColumn = new Map();

 			if (lists.get("projects-addcolumns") && lists.get("projects-addcolumns").length === 1) {
 				var formColumns = [];
 				var allColumns = this.computeMapAdditionnalColumns();
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

 		computeMapAdditionnalColumns: function () {
 			var allColumns = angular.copy(lists.get("projects-addcolumns")[0].columns);
 			var allColumnsFiltered = [];
 			for (var i = 0; i < allColumns.length; i++) {
 				if (allColumns[i].groupHeader == undefined) {
 					allColumnsFiltered.push(allColumns[i]);
 				} else {
 					if (this.mapAdditionnalColumn.get(allColumns[i].groupHeader) == undefined) {
 						allColumnsFiltered.push(allColumns[i]);
 						var tabColumn = [];
 						tabColumn.push(allColumns[i]);
 						this.mapAdditionnalColumn.set(allColumns[i].groupHeader, tabColumn);
 					} else {
 						this.mapAdditionnalColumn.get(allColumns[i].groupHeader).push(allColumns[i]);
 					}
 				}
 			}
 			return allColumnsFiltered;
 		},

 		initAdditionalFilters: function () {
 			this.additionalFilters = [];

 			if (lists.get("projects-addfilters") && lists.get("projects-addfilters").length === 1) {
 				var formFilters = [];
 				var allFilters = angular.copy(lists.get("projects-addfilters")[0].filters);

 				var nbElementByColumn = Math.ceil(allFilters.length / 5); //5 columns
 				for (var i = 0; i < 5 && allFilters.length > 0; i++) {
 					formFilters.push(allFilters.splice(0, nbElementByColumn));
 				}
 				//complete to 5 five element to have a great design 
 				while (formFilters.length < 5) {
 					formFilters.push([]);
				 }

 				this.additionalFilters = formFilters;
 			}
 		},

 		getAddFiltersToForm: function () {
 			if (this.additionalFilters.length === 0) {
 				this.initAdditionalFilters();
 			}
 			return this.additionalFilters;
 		},

 		addColumnsToDatatable: function () {
 			this.selectedAddColumns = [];
 			for (var i = 0; i < this.additionalColumns.length; i++) {
 				for (var j = 0; j < this.additionalColumns[i].length; j++) {
 					if (this.additionalColumns[i][j].select) {
 						if (this.additionalColumns[i][j].groupHeader != undefined) {
 							for (var c = 0; c < this.mapAdditionnalColumn.get(this.additionalColumns[i][j].groupHeader).length; c++) {
 								this.selectedAddColumns.push(this.mapAdditionnalColumn.get(this.additionalColumns[i][j].groupHeader)[c]);
 							}
 						} else {
 							this.selectedAddColumns.push(this.additionalColumns[i][j]);
 						}
 					}
 				}
 			}
 			if (this.reportingConfigurationCode) {
 				this.datatable.setColumnsConfig(this.reportingConfiguration.columns.concat(this.selectedAddColumns));
 			} else {
 				this.datatable.setColumnsConfig(this.getDefaultColumns().concat(this.selectedAddColumns));
 			}
 			this.search();
 		},

 		resetDatatableColumns: function () {
 			this.initAdditionalColumns();
 			this.datatable.setColumnsConfig(this.getDefaultColumns());
 			this.search();
 		},

		 updateForm: function() {
			
		 },

		 convertForm : function(){
			var _form = angular.copy(this.form);
			
			return _form
		},

 		setRouteParams: function ($routeParams) {
 			var count = 0;
 			for (var p in $routeParams) {
				console.log(p)
 				count++;
 				break;
 			}
 			if (count > 0) {
 				this.isRouteParam = true;
 				this.form = $routeParams;
				console.log(this.form);
 			}
 		},

 		search: function () {
			this.updateForm();
			mainService.setForm(this.form);
			var form = this.convertForm();
			console.log(form);
			this.datatable.search(form);
			this.datatable.setMessagesActive(true);
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

 		states: function () {
 			return this.lists.get('statetrue');
 		},

 		init: function ($routeParams, datatableConfig) {
 			initListService();

 			datatableConfig.messages = {
 				transformKey: function (key, args) {
 					return Messages(key, args);
 				}
 			};

 			if (datatableConfig && angular.isUndefined(mainService.getDatatable())) {
 				searchService.datatable = datatable(datatableConfig);
 				mainService.setDatatable(searchService.datatable);
 				searchService.datatable.setColumnsConfig(getDefaultColumns());
 				// this.datatable.search(this.convertForm());
 			} else if (angular.isDefined(mainService.getDatatable())) {
 				searchService.datatable = mainService.getDatatable();
 			}

 			if (angular.isDefined(mainService.getForm())) {
 				searchService.form = mainService.getForm();
 			} else {
 				searchService.resetForm();
 			}

 			if (angular.isDefined($routeParams)) {
 				this.setRouteParams($routeParams);
 			}
 		}
 	};

 	return searchService;
 }]);