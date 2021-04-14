"use strict";
 
 angular.module('ngl-sq.descriptionsServices.propertydefinitions', []).
	factory('descriptionsPropertyDefinitionsSearchService', ['$http', 'mainService', 'lists', 'datatable', 
		                         function($http,   mainService,   lists,   datatable){

		var getColumns = function(){
			var columns = [];

            columns.push({
                "header":Messages("descriptions.property.definitions.table.name"),
                "property":"name",
                "order":true,
                "hide":true,
                "position":1,
                "type":"text",
                "mergeCells":true,
            });
            columns.push({
                "header":Messages("descriptions.property.definitions.table.code"),
                "property":"code",
                "order":true,
                "hide":true,
                "position":2,
                "type":"text",
                "mergeCells":true,
            });
            columns.push({
                "header":Messages("descriptions.property.definitions.table.common.type.code"),
                "property":"commonInfoType.code",
                "order":true,
                "hide":true,
                "position":3,
                "type":"text",
                "mergeCells":false,
			});
			columns.push({
                "header":Messages("descriptions.property.definitions.table.common.type.name"),
                "property":"commonInfoType.name",
                "order":true,
                "hide":true,
                "position":4,
                "type":"text",
                "mergeCells":false,
            });
            columns.push({
                "header":Messages("descriptions.property.definitions.table.value.possibles"),
                "property":"possibleValues",
                "order":true,
                "hide":true,
                "position":5,
                "type":"text",
				"mergeCells":true,
				"filter": "getArray:'value'",
                "render":"<div list-resize='cellValue' list-resize-min-size='5'vertical>",
            });
		         
			return columns;
		};
		
		var isInit = false;
		
		
		var initListService = function(){
			if(!isInit){
				lists.refresh.propertyDefinitions({objectTypeCode: "Import"});
				lists.refresh.importTypes();
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
				
				search : function(){
					mainService.setForm(this.form);				
					this.datatable.search(this.form);
                },
				
				resetForm : function(){
					this.form = {objectTypeCode: "Import"};								
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