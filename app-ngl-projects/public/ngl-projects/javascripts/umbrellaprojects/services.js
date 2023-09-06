 "use strict";
 
 angular.module('ngl-projects.ProjectsServices', []).
	factory('searchService', ['$http', 'mainService', 'lists', 'datatable', function($http, mainService, lists, datatable){

		var getDefaultColumns = function () {
			var columns = [];

			columns.push({  	
				property:"name",
				header: "projects.name",
				type :"text",
				order:true,
				position: 2,
				edit:false
			});

			columns.push({
				property: "traceInformation.creationDate",
				header: "projects.traceInformation.creationDate",
				type: "date",
				order: true,
				position: 3,
				edit: false,
				hide: true
			});

			columns.push({
				property:"code",
				header: "projects.code",
				type :"text",
				position: 4,
				order:true,
				hide:true,
				edit:false
			});

			return columns;
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
   
				if (lists.get("umbrellaprojects-addcolumns") && lists.get("umbrellaprojects-addcolumns").length === 1) {
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

			computeMapAdditionnalColumns: function () {
				var allColumns = angular.copy(lists.get("umbrellaprojects-addcolumns")[0].columns);
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

			setRouteParams: function ($routeParams) {
				var count = 0;
				for (var p in $routeParams) {
					count++;
					break;
				}
				if (count > 0) {
					this.isRouteParam = true;
					this.form = $routeParams;
				}
			},

			updateForm: function() {

			},
				
			convertForm : function(){
				var _form = angular.copy(this.form);
				if(_form.fromDate)_form.fromDate = moment(_form.fromDate, Messages("date.format").toUpperCase()).valueOf();
				if(_form.toDate)_form.toDate = moment(_form.toDate, Messages("date.format").toUpperCase()).valueOf();		
				return _form
			},

			search : function(){
				this.updateForm();
				mainService.setForm(this.form);
				this.datatable.search(this.convertForm());
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

			init: function ($routeParams, datatableConfig) {
				searchService.lists.refresh.umbrellaProjects();
				searchService.lists.refresh.reportConfigs({
					pageCodes: ["umbrellaprojects-addcolumns"]
				}, "umbrellaprojects-addcolumns");

				datatableConfig.messages = {
					transformKey: function (key, args) {
						return Messages(key, args);
					}
				};
	   
				if (datatableConfig && angular.isUndefined(mainService.getDatatable())) {
					searchService.datatable = datatable(datatableConfig);
					mainService.setDatatable(searchService.datatable);
					searchService.datatable.setColumnsConfig(getDefaultColumns());
					this.datatable.search();
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
	}
]);