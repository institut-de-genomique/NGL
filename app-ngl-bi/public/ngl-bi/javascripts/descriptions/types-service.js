"use strict";
 
 angular.module('ngl-bi.descriptionsServices.types', []).
	factory('descriptionsTypesSearchService', ['$http', '$q', 'mainService', 'lists', 'datatable', 
		                         function($http,  $q,  mainService,   lists,   datatable){

		var getColumns = function(tab){
			if(tab === 'types') return getTypesColumns();
            if(tab === 'properties') return getProperitesColumns();
            if(tab === 'propertiesContent') return getProperitesContentColumns();
            return [];

            //---
    
            function getTypesColumns(){
                var columns = [];	
                columns.push({
                    "header":Messages("descriptions.types.table.type.object"),
                    "property":"objectType",
                    "order":true,
                    "position":1,
                    "type":"text",
                    "hide":true
                });
                columns.push({
                    "header":Messages("descriptions.types.table.type.category.name"),
                    "property":"category.name",
                    "order":true,
                    "position":2,
                    "type":"text",
                    "hide":true
                });
                columns.push({
                    "header":Messages("descriptions.types.table.type.category.code"),
                    "property":"category.code",
                    "order":true,
                    "position":3,
                    "type":"text",
                    "hide":true
                });
                columns.push({
                    "header":Messages("descriptions.types.table.type.name"),
                    "property":"name",
                    "order":true,
                    "position":4,
                    "type":"text",
                    "hide":true
                });
                columns.push({
                    "header":Messages("descriptions.types.table.type.code"),
                    "property":"code",
                    "order":true,
                    "position":5,
                    "type":"text",
                    "hide":true
                });	        
                return columns;
            }

            function getProperitesColumns(){
                return [
                    {
                        "header":Messages("descriptions.types.table.type.object"),
                        "property":"objectType",
                        "order":true,
                        "position":1,
                        "type":"text",
                        "mergeCells":true,
                        "hide":true
                    }, 
                    {
                        "header":Messages("descriptions.types.table.type.category.name"),
                        "property":"category.name",
                        "order":true,
                        "position":2,
                        "type":"text",
                        "mergeCells":true,
                        "hide":true
                    },
                    {
                        "header":Messages("descriptions.types.table.type.category.code"),
                        "property":"category.code",
                        "order":true,
                        "position":3,
                        "type":"text",
                        "mergeCells":true,
                        "hide":true
                    },
                    {
                        "header":Messages("descriptions.types.table.type.name"),
                        "property":"typeName",
                        "order":true,
                        "position":4,
                        "type":"text",
                        "mergeCells":true,
                        "hide":true
                    },
                    {
                        "header":Messages("descriptions.types.table.type.code"),
                        "property":"typeCode",
                        "order":true,
                        "position":5,
                        "type":"text",
                        "mergeCells":true,
                        "hide":true
                    },  
                    {
                        "header":Messages("descriptions.types.table.properties.definitions.name"),
                        "property":"name",
                        "order":true,
                        "position":6,
                        "type":"text",
                        "hide":true  
                    },
                    {
                        "header":Messages("descriptions.types.table.properties.definitions.code"),
                        "property":"code",
                        "order":true,
                        "position":7,
                        "type":"text",
                        "hide":true
                    }, 
                    {
                        "header":Messages("descriptions.types.table.properties.definitions.levels.code"),
                        "property":"levels",
                        "order":true,
                        "position":8,
                        "type":"text",
                        "filter": "getArray:'code'",
                        "render":"<div list-resize='cellValue' list-resize-min-size='6'>",
                        "hide":true
                    },
                    {
                        "header":Messages("descriptions.types.table.properties.definitions.required"),
                        "property":"required",
                        "order":true,
                        "position":9,
                        "type":"boolean",
                        "hide":true   
                    },
                    {
                        "header":Messages("descriptions.types.table.properties.definitions.active"),
                        "property":"active",
                        "order":true,
                        "position":10,
                        "type":"boolean",
                        "hide":true   
                    },
                    {
                        "header":Messages("descriptions.types.table.properties.definitions.value.type"),
                        "property":"valueType",
                        "order":true,
                        "position":11,
                        "type":"text",
                        "hide":true
                    }, 
                    {
                        "header":Messages("descriptions.types.table.properties.definitions.choice.in.list"),
                        "property":"choiceInList",
                        "order":true,
                        "position":12,
                        "type":"boolean",
                        "hide":true   
                    },
                    {
                        "header":Messages("descriptions.types.table.properties.definitions.value.default"),
                        "property":"defaultValue",
                        "order":true,
                        "position":13,
                        "type":"text",
                        "hide":true
                    },   
                    {
                        "header":Messages("descriptions.types.table.properties.definitions.value.possibles"),
                        "property":"possibleValues",
                        "order":true,
                        "hide":false,
                        "position":14,
                        "type":"text",
                        "filter": "getArray:'value'",
                        "render":"<div list-resize='cellValue' list-resize-min-size='5' vertical>",
                        "hide":true
                    }
                ];
            }

            function getProperitesContentColumns(){
                return [
                    {
                        "header":Messages("descriptions.types.table.properties.definitions.name"),
                        "property":"name",
                        "order":true,
                        "position":1,
                        "type":"text",
                        "hide":true  
                    },
                    {
                        "header":Messages("descriptions.types.table.properties.definitions.code"),
                        "property":"code",
                        "order":true,
                        "position":2,
                        "type":"text",
                        "hide":true
                    }, 
                    {
                        "header":Messages("descriptions.types.table.properties.definitions.levels.code"),
                        "property":"levels",
                        "order":true,
                        "position":3,
                        "type":"text",
                        "filter": "getArray:'code'",
                        "render":"<div list-resize='cellValue' list-resize-min-size='6'>",
                        "hide":true
                    },
                    {
                        "header":Messages("descriptions.types.table.properties.definitions.required"),
                        "property":"required",
                        "order":true,
                        "position":4,
                        "type":"boolean",
                        "hide":true   
                    },
                    {
                        "header":Messages("descriptions.types.table.properties.definitions.active"),
                        "property":"active",
                        "order":true,
                        "position":5,
                        "type":"boolean",
                        "hide":true   
                    },
                    {
                        "header":Messages("descriptions.types.table.properties.definitions.value.type"),
                        "property":"valueType",
                        "order":true,
                        "position":6,
                        "type":"text",
                        "hide":true
                    },
                    {
                        "header":Messages("descriptions.types.table.properties.definitions.choice.in.list"),
                        "property":"choiceInList",
                        "order":true,
                        "position":7,
                        "type":"boolean",
                        "hide":true   
                    },
                    {
                        "header":Messages("descriptions.types.table.properties.definitions.value.default"),
                        "property":"defaultValue",
                        "order":true,
                        "position":8,
                        "type":"text",
                        "hide":true
                    },
                    {
                        "header":Messages("descriptions.types.table.properties.definitions.value.possibles"),
                        "property":"possibleValues",
                        "order":true,
                        "hide":false,
                        "position":9,
                        "type":"text",
                        "filter": "getArray:'value'",
                        "render":"<div list-resize='cellValue' list-resize-min-size='5' vertical>",
                        "hide":true
                    }
                ];
            }
        };  
        
        var handleListParams = function(params, isList){
            if(isList) params.list = true;
            return params;
        };
        
        var getRouteParams = function(isList, typeCode){
            var params = {objectTypeCode: typeCode};
            return handleListParams(params);
        };

        var getAnalysisTypesRoute = function(isList) {
            return {
                url: jsRoutes.controllers.commons.api.CommonInfoTypes.list().url,
                params: getRouteParams(isList, "Analysis")
            };
        };

        var getReadSetTypesRoute = function(isList) {
            return {
                url: jsRoutes.controllers.commons.api.CommonInfoTypes.list().url,
                params: getRouteParams(isList, "ReadSet")
            };
        };

        var getRunTypesRoute = function(isList) {
            return {
                url: jsRoutes.controllers.runs.api.RunTypes.list().url,
                params: getRunRouteParams()
            };

            //---

            function getRunRouteParams(){
                return handleListParams({}, isList);
            }
        };

        var getPropertiesDefinitionsRoute = function(){
            return {
                url: jsRoutes.controllers.commons.api.PropertyDefinitions.list().url,
                params: {
                    levelCode: "Content"
                }
            };
        };

        var getTabsConfig = function(){
            return {
                types: getTypesConfig(),
                properties: getPropertiesConfig(),
                propertiesContent : getPropertiesContentConfig()
            };

            //---

            function getTypesConfig(){
                var typesRoutes = [getAnalysisTypesRoute(), getReadSetTypesRoute(), getRunTypesRoute()];
                return {
                    routes: typesRoutes,
                    form: {},
                    objectTypes : getObjectTypes()
                };

                //---

                function getObjectTypes(){
                    return typesRoutes.map(getObjectType);
                }
            }

            function getPropertiesConfig(){
                var propertiesRoutes = [getAnalysisTypesRoute(), getReadSetTypesRoute(), getRunTypesRoute()];
                return {
                    routes: propertiesRoutes,
                    form: {},
                    objectTypes : getObjectTypes()
                };

                //---

                function getObjectTypes(){
                    return propertiesRoutes.map(getObjectType);
                }
            }

            function getPropertiesContentConfig(){
                var propertiesContentRoutes = [getPropertiesDefinitionsRoute()];
                return {
                    routes: propertiesContentRoutes,
                    form: {}
                };
            }

            function getObjectType(route) {
                return {code: findObjectTypeCode()};

                //---

                function findObjectTypeCode() {
                    if(isCommonInfoTypeRoute(route)) return route.params.objectTypeCode;
                    if(isRunTypeRoute(route)) return "Run";
                    return;
                }
            }
        };

        var isCommonInfoTypeRoute = function(route){
            return route.params.objectTypeCode;
        };

        var isRunTypeRoute = function(route){
            return route.url === "/api/run-types";
        };

        var spinnerOn = function(datatable) {
            datatable.setSpinner(true);
        };

        var spinnerOff = function(datatable) {
            datatable.setSpinner(false);
        };

        var setTypes = function(types){
            searchService.tabs.types.datatable.setData(types);
        };

        var setProperties = function(properties){
            searchService.tabs.properties.datatable.setData(properties);
        };

        var setPropertiesContent = function(propertiesContent){
            searchService.tabs.propertiesContent.datatable.setData(propertiesContent);
        };

        var copyRoutes = function(routes){
            return routes.map(identity);

            //--- 

            function identity(route){
                return route;
            }
        };

        var copyForm = function(form){
            return Object.assign({}, form);
        };

        function handleSelectedObjectTypes(routes, form){
            if(!form.objectTypeCodes) return;
            if(form.objectTypeCodes.length === 0) return removeObjectTypeCodes();
            var notSelectedRoutes = routes.filter(isNotSelectedObjectType);
            notSelectedRoutes.forEach(removeNotSelectedRoute);
            removeObjectTypeCodes();

            //---

            function isNotSelectedObjectType(route){
                var routeObjectTypeCode = getRouteObjectTypeCode();
                return !form.objectTypeCodes.includes(routeObjectTypeCode);

                //---

                function getRouteObjectTypeCode(){
                    if(isCommonInfoTypeRoute(route)) return route.params.objectTypeCode;
                    if(isRunTypeRoute(route)) return "Run";
                    return;
                }
            }

            function removeNotSelectedRoute(route){
                var routeIndex = routes.indexOf(route);
                delete routes[routeIndex];
            }
    
            function removeObjectTypeCodes(){
                delete form.objectTypeCodes;
            }
        }

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

        var callRoutes = function(routes, form) {
            var promises = routes.map(function(route){
                return callDescriptionRoute(route, form);
            });
            return $q.all(promises);
        };

        var callTypesRoutes = function(){
            var typesRoutes = copyRoutes(searchService.tabs.types.routes);
            var form = copyForm(searchService.tabs.types.form);
            handleSelectedObjectTypes(typesRoutes, form);
            return callRoutes(typesRoutes, form);
        };

        var callPropertiesRoutes = function(){
            var propertiesRoutes = copyRoutes(searchService.tabs.properties.routes);
            var form = copyForm(searchService.tabs.properties.form);
            handleSelectedObjectTypes(propertiesRoutes, form);
            return callRoutes(propertiesRoutes, form);
        };

        var callPropertiesContentRoutes = function(){
            var propertiesContentRoutes = searchService.tabs.propertiesContent.routes;
            var form = searchService.tabs.properties.form;
            return callRoutes(propertiesContentRoutes, form);
        };

        var toMappedQueriesResult = function(allQueriesResult){
            var mappedResult = new Map();
            allQueriesResult.forEach(fillMappedResult);
            return mappedResult;

            //---

            function fillMappedResult(queryResult){
                var data = queryResult.data;
                if(isCommonInfoTypeQuery()) return handleCommonInfoTypeQuery();
                if(isRunTypeQuery()) return handleRunTypeQuery();

                // ---

                function isCommonInfoTypeQuery(){
                    return isCommonInfoTypeRoute(queryResult.config);
                }

                function isRunTypeQuery(){
                    return isRunTypeRoute(queryResult.config);
                }

                function handleCommonInfoTypeQuery(){
                    var objectTypeCode = queryResult.config.params.objectTypeCode;
                    mappedResult.set(objectTypeCode, data);
                }

                function handleRunTypeQuery(){
                    mappedResult.set("Run", data);
                }
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
                return row;
            }
        };

        var extract = (function(){

            return {

                analysesType : function(rawAnalysisType){
                    return {
                        code: rawAnalysisType.code,
                        name: rawAnalysisType.name,
                        objectType: rawAnalysisType.objectType.code
                    };
                },

                analysesTypeProperties : function(rawAnalysisType){
                    return extractFromSubList(rawAnalysisType.propertiesDefinitions, buildBasicRow, assign.propertyDefinitonValues);
        
                    //---
    
                    function buildBasicRow(){
                        return {
                            typeCode: rawAnalysisType.code,
                            typeName: rawAnalysisType.name,
                            objectType: rawAnalysisType.objectType.code
                        };
                    }
                },
        
                readSetType : function(rawReadsetType){
                    return {
                        code: rawReadsetType.code,
                        name: rawReadsetType.name,
                        objectType: rawReadsetType.objectType.code
                    };
                },
        
                readSetTypeProperties : function(rawReadsetType){
                    return extractFromSubList(rawReadsetType.propertiesDefinitions, buildBasicRow, assign.propertyDefinitonValues)
                    .filter(isNotLevelContent); //A retirer une fois que ces propriétés seront migrées dans importBidon 
        
                    //---
    
                    function buildBasicRow(){
                        return {
                            typeCode: rawReadsetType.code,
                            typeName: rawReadsetType.name,
                            objectType: rawReadsetType.objectType.code
                        };
                    }

                    //A retirer une fois que ces propriétés seront migrées dans importBidon
                    function isNotLevelContent(row){
                        return isLevelsDefined(row) && !row.levels.some(isLevelContent); 
                    }

                    function isLevelsDefined(row){
                        return row.levels;
                    }

                    function isLevelContent(level){
                        return level.code === "Content";
                    }
                },
        
                runType : function(rawRunType){
                    return {
                        code: rawRunType.code,
                        name: rawRunType.name,
                        objectType: rawRunType.objectType.code,
                        category: rawRunType.category
                    };
                },
    
                runTypeProperties : function(rawRunType){
                    return extractFromSubList(rawRunType.propertiesDefinitions, buildBasicRow, assign.propertyDefinitonValues);
        
                    //---
    
                    function buildBasicRow(){
                        return {
                            typeCode: rawRunType.code,
                            typeName: rawRunType.name,
                            objectType: rawRunType.objectType.code,
                            category: rawRunType.category
                        };
                    }
                }
    
            };

            //---

            function extractFromSubList(subList, buildBasicRow, assignFromListElement){
                if(isNullOrEmptyList(subList)) return buildIncompleteRow();
                return subList.map(buildRow);

                //---

                function isNullOrEmptyList(list){
                    return !list || list.length === 0;
                }

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

        var Aggregator = function(){

            this.aggregatedResult = [];
            this.aggregate = function(list, mapper) {
                if(!list) return;
                if(!mapper) return;
                var that = this;
                list.map(mapper).forEach(appendMappedResult);

                //---

                function appendMappedResult(obj){
                    if(isArray(obj)){
                        addResults(obj);
                    } else {
                        addResult(obj);
                    }
                }

                function isArray(obj) {
                    return Array.isArray(obj);
                }

                function addResults(extractedObjects){
                    that.aggregatedResult = that.aggregatedResult.concat(extractedObjects);
                }

                function addResult(extractedObject){
                    that.aggregatedResult.push(extractedObject);
                }
            };
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
                    this.searchPropertiesContent();
                },

                searchTypes : function(){
                    var datatable = searchService.tabs.types.datatable;
                    spinnerOn(datatable);
                    callTypesRoutes()
                        .then(transformData)
                        .then(setTypes)
                        .then(function(){
                            spinnerOff(datatable);
                        });
                    
                    //---

                    function transformData(allQueriesResult) {
                        var allQueriesMappedResult = toMappedQueriesResult(allQueriesResult);
                        var rawAnalysisTypes = getRawAnalysisTypes();
                        var rawReadSetTypes = getRawReadSetTypes();
                        var rawRunTypes = getRawRunTypes();
                        return extractRowsFromRawData();

                        //---

                        function getRawAnalysisTypes(){
                            return allQueriesMappedResult.get("Analysis");
                        }

                        function getRawReadSetTypes(){
                            return allQueriesMappedResult.get("ReadSet");
                        }

                        function getRawRunTypes(){
                            return allQueriesMappedResult.get("Run");
                        }

                        function extractRowsFromRawData(){
                            var types = new Aggregator();  
                            if(rawAnalysisTypes)  types.aggregate(rawAnalysisTypes, extract.analysesType);    
                            if(rawReadSetTypes) types.aggregate(rawReadSetTypes, extract.readSetType);
                            if(rawRunTypes) types.aggregate(rawRunTypes, extract.runType);
                            return types.aggregatedResult;
                        }
                    }
                },

                searchProperties : function(){
                    var datatable = searchService.tabs.properties.datatable;
                    spinnerOn(datatable);
                    callPropertiesRoutes()
                        .then(transformData)
                        .then(setProperties)
                        .then(function(){
                            spinnerOff(datatable);
                        });

                    //---

                    function transformData(allQueriesResult) {

                        var allQueriesMappedResult = toMappedQueriesResult(allQueriesResult);
                        var rawAnalysisTypes = getRawAnalysisTypes();
                        var rawReadSetTypes = getRawReadSetTypes();
                        var rawRunTypes = getRawRunTypes();
                        return extractRowsFromRawData();

                        //---

                        function getRawAnalysisTypes(){
                            return allQueriesMappedResult.get("Analysis");
                        }

                        function getRawReadSetTypes(){
                            return allQueriesMappedResult.get("ReadSet");
                        }

                        function getRawRunTypes(){
                            return allQueriesMappedResult.get("Run");
                        }

                        function extractRowsFromRawData(){
                            var properties = new Aggregator();    
                            if(rawAnalysisTypes) properties.aggregate(rawAnalysisTypes, extract.analysesTypeProperties);    
                            if(rawReadSetTypes) properties.aggregate(rawReadSetTypes, extract.readSetTypeProperties);
                            if(rawRunTypes) properties.aggregate(rawRunTypes, extract.runTypeProperties);
                            return properties.aggregatedResult;
                        }
                    }
                },

                searchPropertiesContent : function(){
                    var datatable = searchService.tabs.propertiesContent.datatable;
                    spinnerOn(datatable);
                    callPropertiesContentRoutes()
                        .then(transformData)
                        .then(setPropertiesContent)
                        .then(function(){
                            spinnerOff(datatable);
                        });

                    //---

                    function transformData(allQueriesResult) {
                        var rawPropertiesContent = getRawPropertiesContent();
                        return extractRowsFromRawData();

                        //---

                        function getRawPropertiesContent(){
                            return allQueriesResult[0].data;
                        }

                        function extractRowsFromRawData(){  
                            if(rawPropertiesContent) return rawPropertiesContent;
                            return [];
                        }
                        
                    }
                },
				
				/**
				 * initialise the service
				 */
				init : function($routeParams, datatableConfig){

                    var createDatatable = datatable;

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
                        registerDatatable('propertiesContent');

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