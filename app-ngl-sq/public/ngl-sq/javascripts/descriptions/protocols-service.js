"use strict";
 
 angular.module('ngl-sq.descriptionsServices.protocols', []).
	factory('descriptionsProtocolsSearchService', ['$http', 'mainService', 'lists', 'datatable', 
		                         function($http,   mainService,   lists,   datatable){

		var getColumns = function(data){
			var columns = [];

            columns.push({
                "header":Messages("descriptions.protocols.table.name"),
                "property":"name",
                "order":true,
                "hide":true,
                "position":1,
                "type":"text",
                "mergeCells":true,
            });
            columns.push({
                "header":Messages("descriptions.protocols.table.code"),
                "property":"code",
                "order":true,
                "hide":true,
                "position":2,
                "type":"text",
                "mergeCells":true,
            });
            columns.push({
                "header":Messages("descriptions.protocols.table.experiment.types.code"),
                "property":"experimentTypeCodes",
                "order":true,
                "hide":true,
                "position":3,
                "type":"text",
				"render":"<div list-resize='cellValue' list-resize-min-size='2'vertical>",
			});
			columns.push({
                "header":Messages("descriptions.protocols.table.active"),
                "property":"active",
                "order":true,
                "hide":true,
                "position":4,
                "type":"boolean",
			});
			if(data){
				getPropertyColumns(data, columns);
			}		         
			return columns;
		};

		var getPropertyColumns = function(data, columns){
			var propertySet = new Set();
			data
			.filter(function(protocol){return protocol && protocol.properties})
			.flatMap(function(protocol){return Object.keys(protocol.properties)})
			.forEach(function(protocol){propertySet.add(protocol)});
			var index = 0;
			var properties = Array.from(propertySet);
			properties.sort(function(pA, pB) {
				return pA ? pA.localeCompare(pB) : 1;
			});
			properties.forEach(function(property){
				columns.push({
					"header": "Propriété (" + property + ")",
					"property":"properties." + property + ".value",
					"order":true,
					"hide":true,
					"position":5 + index,
					"type":"text",
				});
				index++;
			});
		};
		
		var isInit = false;
		
		
		var initListService = function(){
			if(!isInit){
				lists.refresh.experimentTypes();
				lists.refresh.protocols();
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
				dataCallBackHash: undefined,
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

				search : function() {
					mainService.setForm(this.form);				
					this.datatable.search(this.form);
				},
				
				resetForm : function(){					
					this.form = {};					
				},

				getExperimentTypes : function() {
					var experimentTypes = this.lists.getExperimentTypes();
					return experimentTypes ? experimentTypes.filter(function (x) { return x.code.indexOf("ext-to") === -1; }).sort(function(x, y) { return x.name < y.name ? -1 : 1; }) : [];
				},

				callBack: function() {
					var data = searchService.datatable.getData();
					var dataHash = searchService.callBackHash(data);
					if(searchService.datatable && (this.dataCallBackHash !== dataHash)){
						this.dataCallBackHash = dataHash;
						searchService.datatable.setColumnsConfig(getColumns(data));
						searchService.datatable.setSpinner(false);
					} 
				},

				callBackHash: function(data) {
					return data.flatMap(function(protocol) { return protocol.code.split(''); }).reduce(function(a,b) {
						a=((a<<5)-a)+b.charCodeAt(0);
						return a&a;
					}, 0);
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
					datatableConfig.callbackEndDisplayResult = searchService.callBack;
					
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