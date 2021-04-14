/* FDS NGL-836 19/11/2018 d'apres Samples */
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
			"property":"sequence | tagLength",
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
			"property":"typeCode | messagesPrefix :'techno'",
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
			"property":"supplierName",
			"order":true,
			"hide":true,
			"position":9,
			"type":"text"
		});
		columns.push({
			"header":Messages("indexes.table.supplierIndexName"),
			"property":"supplierIndexName",
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
			lists.refresh.users();
			lists.refresh.tagCategories();
			lists.refresh.reportConfigs({pageCodes:["indexes"+"-"+mainService.getHomePage()]});
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
			updateColumn : function(){
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