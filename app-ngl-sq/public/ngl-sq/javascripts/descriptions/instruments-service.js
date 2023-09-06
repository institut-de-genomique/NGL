"use strict";
 
 angular.module('ngl-sq.descriptionsServices.instruments', []).
	factory('descriptionsInstrumentsSearchService', ['$http', 'mainService', 'lists', 'datatable', 
		                         function($http,   mainService,   lists,   datatable){

		var getColumns = function(tab){
            if(tab === 'categories') return getCategoriesColumns();
            if(tab === 'instruments') return getInstrumentsColumns();
            if(tab === 'properties') return getPropertiesColumns();
            if(tab === 'supportCategories') return getSupportCategoriesColumns();
            return [];

            //---

            function getCategoriesColumns(){
                var columns = [];
                columns.push({
                    "header":Messages("descriptions.instruments.table.category.name"),
                    "property":"category.name",
                    "order":true,
                    "hide":false,
                    "position":1,
                    "type":"text",
                    "mergeCells":true,
                    "hide":true
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.category.code"),
                    "property":"category.code",
                    "order":true,
                    "hide":false,
                    "position":2,
                    "type":"text",
                    "mergeCells":true,
                    "hide":true
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.type.name"),
                    "property":"name",
                    "order":true,
                    "hide":false,
                    "position":3,
                    "type":"text",
                    "hide":true
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.type.code"),
                    "property":"code",
                    "order":true,
                    "hide":false,
                    "position":4,
                    "type":"text",
                    "hide":true
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.active"),
                    "property":"active",
                    "order":true,
                    "hide":false,
                    "position":5,
                    "type":"boolean",
                    "hide":true,
                    "edit":true
                });
                return columns;
            }

            function getInstrumentsColumns(){
                var columns = [];
                columns.push({
                    "header":Messages("descriptions.instruments.table.category.name"),
                    "property":"categoryCode",
                    "order":true,
                    "position":1,
                    "type":"text",
                    "filter": "codes:'instrument_cat'",
                    "mergeCells":true,
                    "hide":true
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.category.code"),
                    "property":"categoryCode",
                    "order":true,
                    "position":2,
                    "type":"text",
                    "mergeCells":true,
                    "hide":true
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.type.name"),
                    "property":"typeCode",
                    "order":true,
                    "position":3,
                    "type":"text",
                    "filter": "codes:'type'",
                    "mergeCells":true,
                    "hide":true
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.type.code"),
                    "property":"typeCode",
                    "order":true,
                    "position":4,
                    "type":"text",
                    "mergeCells":true,
                    "hide":true
                });  
                columns.push({
                    "header":Messages("descriptions.instruments.table.instruments.name"),
                    "property":"name",
                    "order":true,
                    "position":5,
                    "type":"text",
                    "hide":true
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.instruments.code"),
                    "property":"code",
                    "order":true,
                    "position":6,
                    "type":"text",
                    "hide":true
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.active"),
                    "property":"active",
                    "order":true,
                    "position":7,
                    "type":"boolean",
                    "hide":true,
                    "edit":true

                }); 
                columns.push({
                    "header":Messages("descriptions.instruments.table.instruments.short.name"),
                    "property":"shortName",
                    "order":true,
                    "position":8,
                    "type":"text",
                    "hide":true
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.instruments.path"),
                    "property":"path",
                    "order":true,
                    "position":9,
                    "type":"text",
                    "hide":true
                    
                });
                return columns;
            }

            function getPropertiesColumns(){
                var columns = [];
                columns.push({
                    "header":Messages("descriptions.instruments.table.type.name"),
                    "property":"type.name",
                    "order":true,
                    "hide":false,
                    "position":1,
                    "type":"text" ,
                    "mergeCells":true,
                    "hide":true
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.type.code"),
                    "property":"type.code",
                    "order":true,
                    "hide":false,
                    "position":2,
                    "type":"text",
                    "mergeCells":true,
                    "hide":true
                }); 
                columns.push({
                    "header":Messages("descriptions.instruments.table.properties.definitions.name"),
                    "property":"name",
                    "order":true,
                    "hide":false,
                    "position":3,
                    "type":"text",
                    "hide":true  
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.properties.definitions.code"),
                    "property":"code",
                    "order":true,
                    "hide":false,
                    "position":4,
                    "type":"text",
                    "hide":true
                }); 
                columns.push({
                    "header":Messages("descriptions.instruments.table.properties.definitions.required"),
                    "property":"required",
                    "order":true,
                    "hide":false,
                    "position":5,
                    "type":"boolean",
                    "hide":true   
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.properties.definitions.levels.code"),
                    "property":"levels",
                    "order":true,
                    "hide":false,
                    "position":6,
                    "type":"text",
                    "filter": "getArray:'code'",
                    "render":"<div list-resize='cellValue' list-resize-min-size='3'>",
                    "hide":true
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.properties.definitions.value.type"),
                    "property":"valueType",
                    "order":true,
                    "hide":false,
                    "position":7,
                    "type":"text",
                    "hide":true
                }); 
                columns.push({
                    "header":Messages("descriptions.instruments.table.properties.definitions.value.default"),
                    "property":"defaultValue",
                    "order":true,
                    "hide":false,
                    "position":8,
                    "type":"text",
                    "hide":true
                }); 
                columns.push({
                    "header":Messages("descriptions.instruments.table.properties.definitions.value.unit"),
                    "property":"displayMeasureValue.value",
                    "order":true,
                    "hide":false,
                    "position":9,
                    "type":"text",
                    "hide":true
                }); 
                columns.push({
                    "header":Messages("descriptions.instruments.table.properties.definitions.value.possibles"),
                    "property":"possibleValues",
                    "order":true,
                    "hide":false,
                    "position":10,
                    "type":"text",
                    "filter": "getArray:'value'",
                    "render":"<div list-resize='cellValue' list-resize-min-size='5'>",
                    "hide":true
                });
                return columns;
            }

            function getSupportCategoriesColumns(){
                var columns = [];
                columns.push({
                    "header":Messages("descriptions.instruments.table.type.name"),
                    "property":"type.name",
                    "order":true,
                    "hide":false,
                    "position":1,
                    "type":"text" ,
                    "mergeCells":true,  
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.type.code"),
                    "property":"type.code",
                    "order":true,
                    "hide":false,
                    "position":2,
                    "type":"text" ,
                    "mergeCells":true,  
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.in.support.name"),
                    "property":"inSupportName",
                    "order":true,
                    "hide":true,
                    "position":3,
                    "type":"text" ,
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.in.support.code"),
                    "property":"inSupportCode",
                    "order":true,
                    "hide":true,
                    "position":4,
                    "type":"text" ,
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.in.category.name"),
                    "property":"inCategoryName",
                    "order":true,
                    "hide":true,
                    "position":5,
                    "type":"text" ,
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.in.category.code"),
                    "property":"inCategoryCode",
                    "order":true,
                    "hide":true,
                    "position":6,
                    "type":"text" ,
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.in.nb.column"),
                    "property":"inNbCol",
                    "order":true,
                    "hide":true,
                    "position":7,
                    "type":"text" ,
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.in.nb.line"),
                    "property":"inNbLine",
                    "order":true,
                    "hide":true,
                    "position":8,
                    "type":"text" ,
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.in.nb.container"),
                    "property":"inNbContainer",
                    "order":true,
                    "hide":true,
                    "position":9,
                    "type":"text" ,
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.out.support.name"),
                    "property":"outSupportName",
                    "order":true,
                    "hide":true,
                    "position":10,
                    "type":"text" ,
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.out.support.code"),
                    "property":"outSupportCode",
                    "order":true,
                    "hide":true,
                    "position":11,
                    "type":"text" ,
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.out.category.name"),
                    "property":"outCategoryName",
                    "order":true,
                    "hide":true,
                    "position":12,
                    "type":"text" ,
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.out.category.code"),
                    "property":"outCategoryCode",
                    "order":true,
                    "hide":true,
                    "position":13,
                    "type":"text" ,
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.out.nb.column"),
                    "property":"outNbCol",
                    "order":true,
                    "hide":true,
                    "position":14,
                    "type":"text" ,
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.out.nb.line"),
                    "property":"outNbLine",
                    "order":true,
                    "hide":true,
                    "position":15,
                    "type":"text" ,
                });
                columns.push({
                    "header":Messages("descriptions.instruments.table.out.nb.container"),
                    "property":"outNbContainer",
                    "order":true,
                    "hide":true,
                    "position":16,
                    "type":"text" ,
                });
                return columns;
            }
        };

        var getRouteParams = function(isList){
            var params = {};
            if(isList) params.list = true;
            return params;
        };

        var getInstrumentsRoute = function(isList) {
            return {
                url: jsRoutes.controllers.instruments.api.Instruments.list().url,
                params: getRouteParams(isList)
            };
        };
    
        var getInstrumentUsedTypesRoute = function(isList) {
            return {
                url: jsRoutes.controllers.instruments.api.InstrumentUsedTypes.list().url,
                params: getRouteParams(isList)
            };
        };
    
        var getTabsConfig = function(){
            return {
                categories: getCategoriesConfig(),
                instruments: getInstrumentsConfig(),
                properties: getPropertiesConfig(),
                supportCategories: getSupportCategoriesConfig()
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

            function getCategoriesConfig(){
                return {
                    route: getInstrumentUsedTypesRoute(),
                    form: {},
                    lists: {
                        categories: lists.getInstrumentCategories,
                        types: getListForTab("instrumentUsedTypes(tabCategories)", lists.getInstrumentUsedTypes, function(){
                            return { categoryCodes: searchService.tabs.categories.form.categoryCodes };
                        }),
                    }
                };
            }
    
            function getInstrumentsConfig(){
                return {
                    route: getInstrumentsRoute(),
                    form: {},
                    lists: {
                        categories: lists.getInstrumentCategories,
                        types: getListForTab("instrumentUsedTypes(tabInstruments)", lists.getInstrumentUsedTypes, function(){
                            return { categoryCodes: searchService.tabs.instruments.form.categoryCodes };
                        }),
                        instruments: getListForTab("instruments(tabInstruments)", lists.getInstruments, function(){
                            return { 
                                categoryCodes: searchService.tabs.instruments.form.categoryCodes,
                                typeCodes: searchService.tabs.instruments.form.typeCodes 
                            };
                        })
                    }
                };
            }
    
            function getPropertiesConfig(){
                return {
                    route: getInstrumentUsedTypesRoute(),
                    form: {},
                    lists: {
                        types: lists.getInstrumentUsedTypes
                    }
                };
            }

            function getSupportCategoriesConfig(){
                return {
                    route: getInstrumentUsedTypesRoute(),
                    form: {},
                    lists: {
                        types: lists.getInstrumentUsedTypes
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
    
        var setCategories = function(categories){
            searchService.tabs.categories.datatable.setData(categories);
        };

        var setInstruments = function(instruments){
            searchService.tabs.instruments.datatable.setData(instruments);
        };
    
        var setProperties = function(properties){
            searchService.tabs.properties.datatable.setData(properties);
        };

        var setSupportCategories = function(supportCategories){
            searchService.tabs.supportCategories.datatable.setData(supportCategories);
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
    
        var callCategoriesRoute = function(){
            var categoriesRoute = searchService.tabs.categories.route;
            var form = searchService.tabs.categories.form;
            return callDescriptionRoute(categoriesRoute, form);
        };

        var callInstrumentsRoute = function(){
            var instrumentsRoute = searchService.tabs.instruments.route;
            var form = searchService.tabs.instruments.form;
            return callDescriptionRoute(instrumentsRoute, form);
        };
    
        var callPropertiesRoute = function(){
            var propertiesRoute = searchService.tabs.properties.route;
            var form = searchService.tabs.properties.form;
            return callDescriptionRoute(propertiesRoute, form);
        };

        var callSupportCategoriesRoute = function(){
            var supportCategoriesRoute = searchService.tabs.supportCategories.route;
            var form = searchService.tabs.supportCategories.form;
            return callDescriptionRoute(supportCategoriesRoute, form);
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
                row.displayMeasureValue=propDef.displayMeasureValue;
                return row;
            },
            inContainerSupportCategoryValues : function(row, inContainerSupportCategory) {
                row.inSupportName=inContainerSupportCategory.name;
                row.inSupportCode=inContainerSupportCategory.code;
                row.inCategoryName=inContainerSupportCategory.containerCategory.name;
                row.inCategoryCode=inContainerSupportCategory.containerCategory.code;
                row.inNbCol=inContainerSupportCategory.nbColumn;
                row.inNbLine=inContainerSupportCategory.nbLine;
                row.inNbContainer=inContainerSupportCategory.nbUsableContainer;
                return row;
            },
            outContainerSupportCategoryValues : function(row, outContainerSupportCategory) {
                row.outSupportName=outContainerSupportCategory.name;
                row.outSupportCode=outContainerSupportCategory.code;
                row.outCategoryName=outContainerSupportCategory.containerCategory.name;
                row.outCategoryCode=outContainerSupportCategory.containerCategory.code;
                row.outNbCol=outContainerSupportCategory.nbColumn;
                row.outNbLine=outContainerSupportCategory.nbLine;
                row.outNbContainer=outContainerSupportCategory.nbUsableContainer;
                return row;
            }
        };
    
        var extract = (function(){
            return {        
                properties : function(rawInstrumentUsedType){
                    return extractFromSubList(rawInstrumentUsedType.propertiesDefinitions, buildBasicRow, assign.propertyDefinitonValues);
        
                    //---
        
                    function buildBasicRow(){
                        return {
                            type: {
                                code: rawInstrumentUsedType.code,
                                name: rawInstrumentUsedType.name
                            }
                        };
                    }
                },
                supportCategories : function(rawInstrumentUsedType){
                    var inContainerSupportCategories = rawInstrumentUsedType.inContainerSupportCategories;
                    var outContainerSupportCategories = rawInstrumentUsedType.outContainerSupportCategories;
                    if(doesntHaveContainerSupportCategories()) return buildIncompleteRow();
                    return range().map(handleContainerSupportCategories);
        
                    //---

                    function doesntHaveContainerSupportCategories(){
                        return isNullOrEmptyList(inContainerSupportCategories) && isNullOrEmptyList(outContainerSupportCategories);
                    }

                    function buildIncompleteRow(){
                        return [buildBasicRow()];
                    }

                    function range(){
                        var biggestLength = getBiggestLength();
                        var iterator = rangeIterator(biggestLength);
                        return Array.from(iterator);

                        //---

                        function getBiggestLength(){
                            return Math.max(inContainerSupportCategories.length, outContainerSupportCategories.length);
                        }

                        function rangeIterator(max){
                            return Array(max).keys();
                        }
                    }

                    function handleContainerSupportCategories(index){
                        var row = buildBasicRow();
                        if(inValidIndex()) assign.inContainerSupportCategoryValues(row, inContainerSupportCategories[index]);
                        if(outValidIndex()) assign.outContainerSupportCategoryValues(row, outContainerSupportCategories[index]);
                        return row;

                        //---

                        function inValidIndex(){
                            return inContainerSupportCategories.length > index;
                        }

                        function outValidIndex(){
                            return outContainerSupportCategories.length > index;
                        }
                    }
        
                    function buildBasicRow(){
                        return {
                            type: {
                                code: rawInstrumentUsedType.code,
                                name: rawInstrumentUsedType.name
                            }
                        };
                    }
                }
            };

            //---

            function isNullOrEmptyList(list){
                return !list || list.length === 0;
            }

            function extractFromSubList(subList, buildBasicRow, assignFromListElement){
                if(isNullOrEmptyList(subList)) return buildIncompleteRow();
                return subList.map(buildRow);

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
            lists.refresh.instrumentCategories();
			lists.refresh.instrumentUsedTypes();
            lists.refresh.instruments();
			isInit=true;
		};
		
		var searchService = {
            getColumns:getColumns,
            isRouteParam:false,
            lists : lists,
            tabs : getTabsConfig(),

            resetCategoriesForm : function(){
                searchService.tabs.categories.form = {};
            },

            resetInstrumentsForm : function(){
                searchService.tabs.instruments.form = {};
            },

            resetPropertiesForm : function(){
                searchService.tabs.properties.form = {};
            },

            resetSupportCategoriesForm : function(){
                searchService.tabs.supportCategories.form = {};
            },
				
			search : function(){
                this.searchCategories();
                this.searchInstruments();
                this.searchProperties();
                this.searchSupportCategories();
            },

            searchCategories : function(){
                var datatable = searchService.tabs.categories.datatable;
                spinnerOn(datatable);
                callCategoriesRoute()
                    .then(transformData)
                    .then(setCategories)
                    .then(function(){
                        spinnerOff(datatable);
                    });
                
                //---

                function transformData(queryResult) {

                    var rawInstrumentUsedTypes = getRawInstrumentUsedTypes();
                    return extractRowsFromRawData();

                    //---

                    function getRawInstrumentUsedTypes(){
                        return queryResult.data;
                    }

                    function extractRowsFromRawData(){
                        if(!rawInstrumentUsedTypes) return [];
                        return rawInstrumentUsedTypes;
                    }
                }
            },

            searchInstruments : function(){
                var datatable = searchService.tabs.instruments.datatable;
                spinnerOn(datatable);
                callInstrumentsRoute()
                    .then(transformData)
                    .then(setInstruments)
                    .then(function(){
                        spinnerOff(datatable);
                    });
                
                //---

                function transformData(queryResult) {

                    var rawInstruments = getRawInstruments();
                    return extractRowsFromRawData();

                    //---

                    function getRawInstruments(){
                        return queryResult.data;
                    }

                    function extractRowsFromRawData(){
                        if(!rawInstruments) return [];
                        return rawInstruments.map(treatNullValuesAsEmptyStrings);
                    }

                    // NGL-3612 : correctif sur l'ordre des tris des valeurs null
                    // à enlever après MEP du NGL-2532
                    function treatNullValuesAsEmptyStrings(instrument) {
                        if(!instrument.shortName) instrument.shortName = "";
                        if(!instrument.path) instrument.path = "";
                        return instrument;
                    }
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

                function transformData(queryResult) {

                    var rawInstrumentUsedTypes = getRawInstrumentUsedTypes();
                    return extractRowsFromRawData();

                    //---

                    function getRawInstrumentUsedTypes(){
                        return queryResult.data;
                    }

                    function extractRowsFromRawData(){
                        if(!rawInstrumentUsedTypes) return [];
                        return rawInstrumentUsedTypes.flatMap(extract.properties);
                    }
                }
            },

            searchSupportCategories : function(){
                var datatable = searchService.tabs.supportCategories.datatable;
                spinnerOn(datatable);
                callSupportCategoriesRoute()
                    .then(transformData)
                    .then(setSupportCategories)
                    .then(function(){
                        spinnerOff(datatable);
                    });
                
                //---

                function transformData(queryResult) {

                    var rawInstrumentUsedTypes = getRawInstrumentUsedTypes();
                    return extractRowsFromRawData();

                    //---

                    function getRawInstrumentUsedTypes(){
                        return queryResult.data;
                    }

                    function extractRowsFromRawData(){
                        if(!rawInstrumentUsedTypes) return [];
                        return rawInstrumentUsedTypes.flatMap(extract.supportCategories);
                    }
                }
            },
				
            /**
             * initialise the service
             */
            init : function($routeParams, datatableConfig, isAdmin){
                
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

                function getConfig(tabName) {
                    switch(tabName) {
                        case 'instruments': return withParams({
                            order: {
                                by:'categoryCode',
                                reverse :false,
                                mode:'local'
                            }
                        }, isAdmin);
                        default: return datatableConfig;
                    }

                    //---

                    function withParams(params, isAdmin){
                        var config = Object.assign({}, datatableConfig);

                        if (isAdmin === true) {
                            config.edit = {
                                active: true,
				                showButton: true,
				                
                            };

                            config.save = {
                                active: true,
                                method: 'put',
                                url: function(value) {
                                    return jsRoutes.controllers.instruments.api.Instruments.update(value.code).url;
                                },
                            }
                        }

                        return  Object.assign(config, params);
                    }
                }

                function registerDatatables() {
                    if(!datatableConfig) return;
                    registerDatatable('categories');
                    registerDatatable('instruments');
                    registerDatatable('properties');
                    registerDatatable('supportCategories');

                    //---

                    function registerDatatable(tab) {
                        var config = getConfig(tab)
                        var datatable = createDatatable(config);
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