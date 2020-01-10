"use strict";
 
 angular.module('ngl-sq.descriptionsServices.protocols', []).
	factory('descriptionsProtocolsSearchService', ['$http', 'mainService', 'lists', 'datatable', 
		                         function($http,   mainService,   lists,   datatable){

		var getColumns = function(){
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
				"render":"<div list-resize='cellValue' list-resize-min-size='5'vertical>",
			});
			columns.push({
                "header":Messages("descriptions.protocols.table.active"),
                "property":"active",
                "order":true,
                "hide":true,
                "position":4,
                "type":"boolean",
            });
			columns.push({
                "header":Messages("descriptions.protocols.table.properties.extraction.protocol"),
                "property":"properties.extractionProtocol.value",
                "order":true,
                "hide":true,
                "position":5,
                "type":"text",
            });
			columns.push({
                "header":Messages("descriptions.protocols.table.properties.depletion.method"),
                "property":"properties.depletionMethod.value",
                "order":true,
                "hide":true,
                "position":7,
                "type":"text",
			});
			columns.push({
                "header":Messages("descriptions.protocols.table.properties.cdna.synthesis.type"),
                "property":"properties.cDNAsynthesisType.value",
                "order":true,
                "hide":true,
                "position":8,
                "type":"text",
			});
			columns.push({
                "header":Messages("descriptions.protocols.table.properties.strand.orientation"),
                "property":"properties.strandOrientation.value",
                "order":true,
                "hide":true,
                "position":9,
                "type":"text",
			});
			columns.push({
                "header":Messages("descriptions.protocols.table.properties.rnalib.protocol"),
                "property":"properties.rnaLibProtocol.value",
                "order":true,
                "hide":true,
                "position":10,
                "type":"text",
			});
            columns.push({
                "header":Messages("descriptions.protocols.table.properties.library.protocol"),
                "property":"properties.libraryProtocol.value",
                "order":true,
                "hide":true,
                "position":11,
                "type":"text",
			});
		         
			return columns;
		};
		
		var isInit = false;
		
		
		var initListService = function(){
			if(!isInit){
				lists.refresh.experimentTypes();
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
					this.form = {};					
				},

				getExperimentTypes : function(){
					var experimentTypes = this.lists.getExperimentTypes();
					return  experimentTypes ? experimentTypes.filter(x => x.code.indexOf("ext-to") === -1) : [];
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