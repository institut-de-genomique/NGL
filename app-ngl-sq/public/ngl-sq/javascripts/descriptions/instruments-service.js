"use strict";
 
 angular.module('ngl-sq.descriptionsServices.instruments', []).
	factory('descriptionsInstrumentsSearchService', ['$http', 'mainService', 'lists', 'datatable', 
		                         function($http,   mainService,   lists,   datatable){

		var getColumns = function(index){
			var columns = [];

            if(!index || index === "categories"){
                columns.push({
                    "header":Messages("descriptions.instruments.table.category.name"),
                    "property":"category.name",
                    "order":true,
                    "hide":false,
                    "position":1,
                    "type":"text",
                    "mergeCells":true,
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.category.code"),
                    "property":"category.code",
                    "order":true,
                    "hide":false,
                    "position":2,
                    "type":"text",
                    "mergeCells":true,
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.type.name"),
                    "property":"name",
                    "order":true,
                    "hide":false,
                    "position":3,
                    "type":"text"    
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.type.code"),
                    "property":"code",
                    "order":true,
                    "hide":false,
                    "position":4,
                    "type":"text",
                });
            } else if(index === "instruments"){
                columns.push({
                    "header":Messages("descriptions.instruments.table.category.name"),
                    "property":"categoryName",
                    "order":true,
                    "hide":false,
                    "position":1,
                    "type":"text",
                    "mergeCells":true,
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.category.code"),
                    "property":"categoryCode",
                    "order":true,
                    "hide":false,
                    "position":2,
                    "type":"text",
                    "mergeCells":true,
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.type.name"),
                    "property":"typeName",
                    "order":true,
                    "hide":false,
                    "position":3,
                    "type":"text",
                    "mergeCells":true,    
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.type.code"),
                    "property":"typeCode",
                    "order":true,
                    "hide":false,
                    "position":4,
                    "type":"text",
                    "mergeCells":true,
                });  
                columns.push({
                    "header":Messages("descriptions.instruments.table.instruments.name"),
                    "property":"name",
                    "order":true,
                    "hide":false,
                    "position":5,
                    "type":"text",
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.instruments.code"),
                    "property":"code",
                    "order":true,
                    "hide":false,
                    "position":6,
                });   
                columns.push({
                    "header":Messages("descriptions.instruments.table.instruments.path"),
                    "property":"path",
                    "order":true,
                    "hide":false,
                    "position":7,
                    "type":"text",
                });  
                columns.push({
                    "header":Messages("descriptions.instruments.table.active"),
                    "property":"active",
                    "order":true,
                    "hide":false,
                    "position":8,
                    "type":"boolean",
                });                     
            } else if(index === "properties"){
                columns.push({
                    "header":Messages("descriptions.instruments.table.type.name"),
                    "property":"typeName",
                    "order":true,
                    "hide":false,
                    "position":1,
                    "type":"text" ,
                    "mergeCells":true,  
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.type.code"),
                    "property":"typeCode",
                    "order":true,
                    "hide":false,
                    "position":2,
                    "type":"text",
                    "mergeCells":true,
                }); 
                columns.push({
                    "header":Messages("descriptions.instruments.table.properties.definitions.name"),
                    "property":"name",
                    "order":true,
                    "hide":false,
                    "position":3,
                    "type":"text"    
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.properties.definitions.code"),
                    "property":"code",
                    "order":true,
                    "hide":false,
                    "position":4,
                    "type":"text",
                }); 
                columns.push({
                    "header":Messages("descriptions.instruments.table.properties.definitions.required"),
                    "property":"required",
                    "order":true,
                    "hide":false,
                    "position":5,
                    "type":"boolean"    
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.properties.definitions.levels.code"),
                    "property":"levels",
                    "order":true,
                    "hide":false,
                    "position":6,
                    "type":"text",
                    "render":"<div list-resize='cellValue | getArray:\"code\"' list-resize-min-size='3'>",
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.properties.definitions.value.type"),
                    "property":"valueType",
                    "order":true,
                    "hide":false,
                    "position":7,
                    "type":"text",
                }); 
                columns.push({
                    "header":Messages("descriptions.instruments.table.properties.definitions.value.default"),
                    "property":"defaultValue",
                    "order":true,
                    "hide":false,
                    "position":8,
                    "type":"text",
                }); 
                columns.push({
                    "header":Messages("descriptions.instruments.table.properties.definitions.value.possibles"),
                    "property":"possibleValues",
                    "order":true,
                    "hide":false,
                    "position":9,
                    "type":"text",
                    "render":"<div list-resize='cellValue | getArray:\"value\"' list-resize-min-size='5'>",
                });   
            }
		         
			return columns;
        };
		
		var searchService = {
				getColumns:getColumns,
                getDefaultColumns:getColumns,
				datatable:undefined,
				isRouteParam:false,
                lists : lists,
                sublists: {},
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
                    this.clearSublists();
                },

                clearSublists : function() {
                    this.sublists.categories = null;
                    this.sublists.instruments = null;
                    this.sublists.properties = null;
                },

                generateSubLists : function() {
                    if(!(this.sublists.categories && this.sublists.instruments && this.sublists.properties)){
                        var categories = [];
                        var instruments = [];
                        var properties = [];
                        this.datatable.getData().forEach(function(instrUsed) {
                            categories.push({
                                "category": {
                                    "name": instrUsed.category.name, 
                                    "code": instrUsed.category.code,
                                }, 
                                "name": instrUsed.name, 
                                "code": instrUsed.code,
                            });
                            instrUsed.instruments.forEach(function(instr){
                                instruments.push({
                                    "categoryName": instrUsed.category.name, 
                                    "categoryCode": instrUsed.category.code, 
                                    "typeName": instrUsed.name, 
                                    "typeCode": instrUsed.code, 
                                    "name": instr.name, 
                                    "code": instr.code, 
                                    "path": instr.path, 
                                    "active": instrUsed.active
                                });
                            });
                            instrUsed.propertiesDefinitions.forEach(function(propdef){
                                properties.push({
                                    "typeName": instrUsed.name, 
                                    "typeCode": instrUsed.code, 
                                    "name": propdef.name, 
                                    "code": propdef.code, 
                                    "required": propdef.required, 
                                    "levels": propdef.levels,
                                    "valueType": propdef.valueType,
                                    "defaultValue": propdef.defaultValue,
                                    "possibleValues": propdef.possibleValues
                                });
                            });
                        });
                        this.sublists.categories = categories;
                        this.sublists.instruments = instruments;
                        this.sublists.properties = properties;
                    }
                },
                
                changeTab : function(index){

                    this.generateSubLists();

                    if(index === "categories"){
                        this.datatable.setData(this.sublists.categories, this.sublists.categories.length);
                    } if(index === "instruments"){
                        this.datatable.setData(this.sublists.instruments, this.sublists.instruments.length);
                    } else if(index === "properties"){
                        this.datatable.setData(this.sublists.properties, this.sublists.properties.length);
                    }
                    this.datatable.setColumnsConfig(getColumns(index));
                },
				
				/**
				 * initialise the service
				 */
				init : function($routeParams, datatableConfig){
					
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