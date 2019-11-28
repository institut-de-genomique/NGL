/* FDS NGL-836 19/11/2018 d'apres Samples en cours......*/
"use strict";

angular.module('ngl-sq.indexesServices', []).factory('indexesSearchService', ['$http', 'mainService', 'lists', 'datatable',
	                                                                  function($http, mainService, lists, datatable){
	
	var getColumnsDefault = function(){
		var columns = [];
		
		columns.push({
			"header":Messages("indexes.table.name"),
			"property":"name",
			"order":true,
			"hide":true,
			"position":1,
			"type":"text"
		});
		columns.push({
			"header":Messages("indexes.table.code"),
			"property":"code",
			"order":true,
			"hide":true,
			"position":2,
			"type":"text"
		});
	
		columns.push({
			"header":Messages("indexes.table.sequence"),
			"property":"sequence",
			"order":true,
			"hide":true,
			"position":3,
			"type":"text"
		});
		columns.push({
			"header":Messages("indexes.table.shortName"),
			"property":"shortName",
			"order":true,
			"hide":true,
			"position":4,
			"type":"text"
		});
		columns.push({
			"header":Messages("indexes.table.size"),
			"property":"sequence | tagLength",           // new filter OK !!
			"order":true,
			"hide":true,
			"position":5,
			"type":"text" // float ???
		});
		columns.push({
			"header":Messages("indexes.table.categoryCode"),
			"property":"categoryCode",
			"order":true,
			"hide":true,
			"position":6,
			"type":"text"
		});	
		columns.push({
			"header":Messages("indexes.table.typeCode"),
			"property":"typeCode | messagesPrefix :'techno'",   //new filter OK !!
			"order":true,
			"hide":true,
			"position":7,
			"type":"text"
		});
		columns.push({
			"header":Messages("indexes.table.groupNames"),
			"property":"groupNames",
			"render":"<div list-resize='cellValue' ' list-resize-min-size='3' vertical>",
			"order":true,
			"hide":true,
			"position":8,
			"type":"text",
			"groupMethod":"collect:true"
		});
		columns.push({
			"header":Messages("indexes.table.supplierName"),
			"property":"supplierName | toArrayProps",    // new filter OK
			"order":true,
			"hide":true,
			"position":9,
			"type":"text"
		});
		columns.push({
			"header":Messages("indexes.table.supplierIndexName"),
			"property":"supplierName | unique", // OK
			"order":true,
			"hide":true,
			"position":10,
			"type":"text"
		});
		columns.push({
			"header":Messages("samples.table.creationDate"),
			"property":"traceInformation.creationDate",
			"order":true,
			"hide":true,
			"position":11,			
			"type":"date",
			"groupMethod":"unique"
				});
		columns.push({
			"header":Messages("samples.table.createUser"),
			"property":"traceInformation.createUser",
			"order":true,
			"hide":true,
			"position":12,
			"type":"text",
			"groupMethod":"unique"
		});

		return columns;
	};
	
	var isInit = false;

	var initListService = function(){
		if(!isInit){
			//console.log('refresh lists....')
			lists.refresh.users();
			lists.refresh.tagCategories();
			lists.refresh.tags();
			lists.refresh.tagGroupNames();
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
			//additionalFilters:[],
			//additionalColumns:[],
			//selectedAddColumns:[],
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
				/*  PAS DE COLONNES ADDITIONNELLES...pour l'instant
				for(var i = 0 ; i < this.selectedAddColumns.length ; i++){
					//remove .value if present to manage correctly properties (single, list, etc.)
					if(this.selectedAddColumns[i].queryIncludeKeys && this.selectedAddColumns[i].queryIncludeKeys.length > 0){
						this.form.includes = this.form.includes.concat(this.selectedAddColumns[i].queryIncludeKeys);
					}else{
						this.form.includes.push(this.selectedAddColumns[i].property.replace('.value','').replace(".unit", ''));
					}
					
				}
				*/
				
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
			search : function(){
				this.updateForm();
				mainService.setForm(this.form);
				this.datatable.search(this.convertForm());
				
			},
			
			/* PAS DE COLONNES ADDITIONNELLES...pour l'instant
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
			*/
			
			/* PAS DE FILTRES ADDITIONNELS...pour l'instant
			initAdditionalFilters:function(){
				this.additionalFilters=[];
				
				// ou est lists.get
				if(lists.get("indexes-search-addfilters") && lists.get("indexes-search-addfilters").length === 1){
					var formFilters = [];
					var allFilters = angular.copy(lists.get("indexes-search-addfilters")[0].filters);
					
					/// add static filters here
					//allFilters.push({property:"comments.comment",html:"<textarea class='form-control' ng-model='searchService.form.commentRegex' placeholder='"+Messages("search.placeholder.commentRegex")+"' title='"+Messages("search.placeholder.commentRegex")+"'></textarea>",position:allFilters.length+1});
					allFilters.push({property:"processes.experiments.protocolCode",
						             html:"<div bt-select multiple=true filter=true placeholder='"+Messages("experiments.select.protocols")
						                   +"' class='form-control' ng-model='searchService.form.experimentProtocolCodes' " +
						                   		"     bt-options='protocol.code as protocol.name for protocol in searchService.lists.get(\"all-protocols\")'></div>",
						             position:allFilters.length+1});
					
					
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
			*/
			
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