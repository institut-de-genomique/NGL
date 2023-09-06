"use strict";
 
 angular.module('ngl-sq.descriptionsServices.processes', []).
	factory('descriptionsProcessesSearchService', ['$http', 'mainService', 'lists', 'datatable', 
		                         function($http,   mainService,   lists,   datatable){

		var getColumns = function(tab){
            if(tab === 'types') return getTypesColumns();
            if(tab === 'properties') return getPropertiesColumns();
            return [];

            //---

            function getTypesColumns(){
                return [
                    {
                        "header":Messages("descriptions.processes.table.category.name"),
                        "property":"category.name",
                        "order":true,
                        "position":1,
                        "type":"text",
                        "mergeCells":true,
                        "hide":true
                    },
                    {
                        "header":Messages("descriptions.processes.table.category.code"),
                        "property":"category.code",
                        "order":true,
                        "position":2,
                        "type":"text",
                        "mergeCells":true,
                        "hide":true
                    },
                    {
                        "header":Messages("descriptions.processes.table.type.name"),
                        "property":"name",
                        "order":true,
                        "position":3,
                        "type":"text",
                        "mergeCells":true,
                        "hide":true
                    },
                    {
                        "header":Messages("descriptions.processes.table.type.code"),
                        "property":"code",
                        "order":true,
                        "position":4,
                        "type":"text",
                        "mergeCells":true,
                        "hide":true
                    },  
                    {
                        "header":Messages("descriptions.processes.table.void.experiment.name"),
                        "property":"voidExperimentType.name",
                        "order":true,
                        "position":5,
                        "type":"text",
                        "mergeCells":true,
                        "hide":true
                    },
                    {
                        "header":Messages("descriptions.processes.table.void.experiment.code"),
                        "property":"voidExperimentType.code",
                        "order":true,
                        "position":6,
                        "type":"text",
                        "mergeCells":true,
                        "hide":true
                    },
                    {
                        "header":Messages("descriptions.processes.table.first.experiment.name"),
                        "property":"firstExperimentType.name",
                        "order":true,
                        "position":7,
                        "type":"text",
                        "mergeCells":true,
                        "hide":true
                    }, 
                    {
                        "header":Messages("descriptions.processes.table.first.experiment.code"),
                        "property":"firstExperimentType.code",
                        "order":true,
                        "position":8,
                        "type":"text",
                        "mergeCells":true,
                        "hide":true
                    },
                    {
                        "header":Messages("descriptions.processes.table.last.experiment.name"),
                        "property":"lastExperimentType.name",
                        "order":true,
                        "position":9,
                        "type":"text",
                        "mergeCells":true,
                        "hide":true
                    },
                    {
                        "header":Messages("descriptions.processes.table.last.experiment.code"),
                        "property":"lastExperimentType.code",
                        "order":true,
                        "position":10,
                        "type":"text",
                        "mergeCells":true,
                        "hide":true
                    },
                    {
                        "header":Messages("descriptions.processes.table.experiment.type.position.in.process"),
                        "property":"positionInProcess",
                        "position":11,
                        "type":"number",
                        "hide":true
                    },
                    {
                        "header":Messages("descriptions.processes.table.experiment.type.name"),
                        "property":"experimentTypeCode",
                        "filter":"codes:'type'",
                        "position":12,
                        "type":"text",
                        "hide":true
                    },
                    {
                        "header":Messages("descriptions.processes.table.experiment.type.code"),
                        "property":"experimentTypeCode",
                        "position":13,
                        "type":"text",
                        "hide":true
                    }
                ];
            }

            function getPropertiesColumns(){
                return [
                    {
                        "header":Messages("descriptions.processes.table.category.name"),
                        "property":"category.name",
                        "order":true,
                        "position":1,
                        "type":"text",
                        "mergeCells":true,
                        "hide":true
                    },
                    {
                        "header":Messages("descriptions.processes.table.category.code"),
                        "property":"category.code",
                        "order":true,
                        "position":2,
                        "type":"text",
                        "mergeCells":true,
                        "hide":true
                    },
                    {
                        "header":Messages("descriptions.processes.table.type.name"),
                        "property":"type.name",
                        "order":true,
                        "position":3,
                        "type":"text" ,
                        "mergeCells":true,
                        "hide":true
                    },
                    {
                        "header":Messages("descriptions.processes.table.type.code"),
                        "property":"type.code",
                        "order":true,
                        "position":4,
                        "type":"text",
                        "mergeCells":true,
                        "hide":true
                    }, 
                    {
                        "header":Messages("descriptions.processes.table.properties.definitions.name"),
                        "property":"name",
                        "order":true,
                        "position":5,
                        "type":"text",
                        "hide":true  
                    },
                    {
                        "header":Messages("descriptions.processes.table.properties.definitions.code"),
                        "property":"code",
                        "order":true,
                        "position":6,
                        "type":"text",
                        "hide":true
                    }, 
                    {
                        "header":Messages("descriptions.processes.table.properties.definitions.required"),
                        "property":"required",
                        "order":true,
                        "position":7,
                        "type":"boolean",
                        "hide":true   
                    },
                    {
                        "header":Messages("descriptions.processes.table.properties.definitions.levels.code"),
                        "property":"levels",
                        "order":true,
                        "position":8,
                        "type":"text",
                        "filter": "getArray:'code'",
                        "render":"<div list-resize='cellValue' list-resize-min-size='3'>",
                        "hide":true
                    },
                    {
                        "header":Messages("descriptions.processes.table.properties.definitions.value.type"),
                        "property":"valueType",
                        "order":true,
                        "position":9,
                        "type":"text",
                        "hide":true
                    }, 
                    {
                        "header":Messages("descriptions.processes.table.properties.definitions.value.default"),
                        "property":"defaultValue",
                        "order":true,
                        "position":10,
                        "type":"text",
                        "hide":true
                    }, 
                    {
                        "header":Messages("descriptions.processes.table.properties.definitions.value.possibles"),
                        "property":"possibleValues",
                        "order":true,
                        "position":11,
                        "type":"text",
                        "filter": "getArray:'value'",
                        "render":"<div list-resize='cellValue' list-resize-min-size='5'>",
                        "hide":true
                    },
                ];
            }
        };

        var getRouteParams = function(isList){
            var params = {};
            if(isList) params.list = true;
            return params;
        };

        var getTypesRoute = function(isList, light) {
            var typesParams = getRouteParams(isList);
            typesParams.light = light ? true : false;
            return {
                url: jsRoutes.controllers.processes.api.ProcessTypes.list().url,
                params: typesParams
            };
        };
    
        var getTabsConfig = function(){
            return {
                types: getTypesConfig(),
                properties: getPropertiesConfig()
            };
    
            //---

            function getListForTab(label, getter, getParams) {
                return function(){
                    if(!getter) {return [];}
                    var params = getParams ? getParams() : {};
                    var key = label + JSON.stringify(params);
                    params.list = true;
                    return getter(params, key);
                };
            }

            function getTypesConfig(){
                return {
                    route: getTypesRoute(),
                    form: {},
                    lists: {
                        categories: lists.getProcessCategories,
                        types: getListForTab("process(tabTypes)", lists.getProcessTypes, function(){
                            return { categoryCodes: searchService.tabs.types.form.categoryCodes };
                        }),
                    }
                };
            }
    
            function getPropertiesConfig(){
                return {
                    route: getTypesRoute(false, true),
                    form: {},
                    lists: {
                        categories: lists.getProcessCategories,
                        types: getListForTab("process(tabProperties)", lists.getProcessTypes, function(){
                            return { categoryCodes: searchService.tabs.properties.form.categoryCodes };
                        }),
                        properties: lists.getPropertyDefinitions
                    }
                };
            }
        };

        var spinnerOn = function(datatable) {
            datatable.setSpinner(true);
        };
    
        var spinnerOff = function(datatable) {
            datatable.setSpinner(false);
        };
    
        var callDescriptionRoute = function(route, form) {
            var url = route.url;
            var config = {params: mergeForms()};
            return $http.get(url, config);

            //---

            function mergeForms(){
                var mergedForm = {};
                Object.assign(mergedForm, route.params);
                Object.assign(mergedForm, form);
                return mergedForm;
            }
        };
    
        var assign = {
            propertyDefinitonValues : function(row, propDef) {
                row.name=propDef.name;
                row.code=propDef.code;
                row.levels=propDef.levels;
                row.required=propDef.required;
                row.active=propDef.active;
                row.choiceInList=propDef.choiceInList;
                row.valueType=propDef.valueType;
                row.defaultValue=propDef.defaultValue;
                row.possibleValues=propDef.possibleValues;
                return row;
            },
            experimentTypes : function(row, processExpType) {
                row.positionInProcess=processExpType.positionInProcess;
                row.experimentTypeCode=processExpType.experimentTypeCode;
            }
        };
    
        var extract = (function(){
            return {        
                properties : function(rawProcessType){
                    return extractFromSubList(rawProcessType.propertiesDefinitions, buildBasicPropertiesRow, assign.propertyDefinitonValues);
        
                    //---
        
                    function buildBasicPropertiesRow(){
                        return {
                            category: rawProcessType.category,
                            type: {
                                code: rawProcessType.code,
                                name: rawProcessType.name
                            }
                        };
                    }
                },
                experimentTypes : function(rawProcessType){
                    return extractFromSubList(rawProcessType.experimentTypes, buildBasicExperimentTypesRow, assign.experimentTypes);
        
                    //---
        
                    function buildBasicExperimentTypesRow(){
                        return {
                            code: rawProcessType.code,
                            name: rawProcessType.name,
                            category: rawProcessType.category,
                            voidExperimentType : rawProcessType.voidExperimentType,
                            firstExperimentType : rawProcessType.firstExperimentType,
                            lastExperimentType : rawProcessType.lastExperimentType
                        };
                    }
                }
            };

            //---

            function isNullOrEmptyList(list){
                return !list || list.length === 0;
            }

            function extractFromSubList(subList, buildBasicRow, assignFromListElement){
                return isNullOrEmptyList(subList) ? buildIncompleteRow() : subList.map(buildRow);

                //---

                function buildIncompleteRow() {
                    return [buildBasicRow()];
                }

                function buildRow(listElement){
                    var row = buildBasicRow();
                    assignFromListElement(row, listElement);
                    return row;
                }
            }

        })();

        var isInit = false;

        var initListService = function(){
            if(isInit) return;
            lists.refresh.processCategories();
			lists.refresh.processTypes();
			isInit=true;
		};
		
		var searchService = {
            getColumns:getColumns,
            isRouteParam:false,
            lists : lists,
            tabs : getTabsConfig(),

            resetTypesForm : function(){
                searchService.tabs.types.form = {};
            },

            resetPropertiesForm : function(){
                searchService.tabs.properties.form = {};
            },
				
			search : function(){
                this.searchTypes();
                this.searchProperties();
            },

            searchTypes : function(){
                var datatable = searchService.tabs.types.datatable;
                spinnerOn(datatable);
                callTypesRoute()
                    .then(transformData)
                    .then(setTypes)
                    .then(function(){
                        spinnerOff(datatable);
                    });
                
                //---

                function callTypesRoute(){
                    var typesRoute = searchService.tabs.types.route;
                    var form = searchService.tabs.types.form;
                    return callDescriptionRoute(typesRoute, form);
                }

                function transformData(queryResult) {
                    var rawProcessTypes = queryResult.data;
                    if(!rawProcessTypes) return [];
                    return rawProcessTypes.flatMap(extract.experimentTypes);
                }

                function setTypes(types){
                    datatable.setData(types);
                }
            },

            searchProperties : function(){
                var datatable = searchService.tabs.properties.datatable;
                spinnerOn(datatable);
                callPropertiesRoute()
                    .then(transformData)
                    .then(setProperties)
                    .then(function(){
                        spinnerOff(datatable);
                    });
                
                //---

                function callPropertiesRoute(){
                    var propertiesRoute = searchService.tabs.properties.route;
                    var form = searchService.tabs.properties.form;
                    return callDescriptionRoute(propertiesRoute, form);
                }

                function transformData(queryResult) {
                    var rawProcessTypes = queryResult.data;
                    if(!rawProcessTypes) return [];
                    return rawProcessTypes.flatMap(extract.properties);
                }

                function setProperties(properties){
                    datatable.setData(properties);
                }
            },
				
            /**
             * initialise the service
             */
            init : function($routeParams, datatableConfig){
                
                var createDatatable = datatable;

                initListService();
                addMessagesToDatatableConfig();
                registerDatatables();
                handleRouteParams();

                //---

                function addMessagesToDatatableConfig() {
                    datatableConfig.messages = {
                        transformKey: function(key, args) {
                            return Messages(key, args);
                        }
                    };
                }

                function registerDatatables() {
                    if(!datatableConfig) return;
                    registerDatatable('types');
                    registerDatatable('properties');

                    //---

                    function registerDatatable(tab) {
                        var datatable = createDatatable(datatableConfig);
                        searchService.tabs[tab].datatable = datatable;
                        mainService.setDatatable(datatable);
                        var columns = getColumns(tab);
                        datatable.setColumnsConfig(columns);
                    }
                }

                function handleRouteParams(){
                    if(!angular.isDefined($routeParams)) return;
                    if(isRouteParamsNotEmpty()) setRouteParams();

                    //---

                    function isRouteParamsNotEmpty() {
                        var count = 0;
                        for(var p in $routeParams){
                            count++;
                            break;
                        }
                        return count > 0;
                    }

                    function setRouteParams(){
                        searchService.isRouteParam = true;
                        searchService.form = $routeParams;
                    }
                }					
					
					
            }
		};
		
		return searchService;				
	}

]);