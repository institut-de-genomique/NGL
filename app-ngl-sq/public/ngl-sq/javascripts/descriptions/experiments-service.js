"use strict";
 
 angular.module('ngl-sq.descriptionsServices.experiments', []).
    factory('descriptionsExperimentsSearchService', ['$http', '$filter', 'mainService', 'lists', 'datatable', 
        function($http, $filter,  mainService,   lists,   datatable){

            var getColumns = function(index, objects){

                if(!index || index === "types") return getTypesColumns();
                if(index === "properties") return getPropertiesColumns();
                if(index === "instruments") return getInstrumentColumns();
                if(index === "atms") return getAtmColumns();
                if(index === "previous") return getPreviousColumns();
                if(index === "crossTable" && objects) return getCrossTableColumns(objects);
                return [];
    
                //---

                function getTypesColumns(){
                    return [
                        {
                            "header":Messages("descriptions.experiments.table.type.name"),
                            "property":"name",
                            "order":true,
                            "hide":false,
                            "position":1,
                            "type":"text" 
                        },
                        {
                            "header":Messages("descriptions.experiments.table.type.code"),
                            "property":"code",
                            "order":true,
                            "hide":false,
                            "position":2,
                            "type":"text"
                        },
                        {
                            "header":Messages("descriptions.experiments.table.type.active"),
                            "property":"active",
                            "order":true,
                            "hide":true,
                            "position":3,
                            "type":"boolean"    
                        }
                    ];    
                } 

                function getPropertiesColumns(){
                    return [
                        {
                            "header":Messages("descriptions.experiments.table.type.name"),
                            "property":"typeName",
                            "order":true,
                            "hide":false,
                            "position":1,
                            "type":"text" ,
                            "mergeCells":true,  
                        },
                        {
                            "header":Messages("descriptions.experiments.table.type.code"),
                            "property":"typeCode",
                            "order":true,
                            "hide":false,
                            "position":2,
                            "type":"text",
                            "mergeCells":true,
                        },
                        {
                            "header":Messages("descriptions.experiments.table.properties.definitions.name"),
                            "property":"name",
                            "order":false,
                            "hide":true,
                            "position":3,
                            "type":"text"    
                        },
                        {
                            "header":Messages("descriptions.experiments.table.properties.definitions.code"),
                            "property":"code",
                            "order":false,
                            "hide":true,
                            "position":4,
                            "type":"text",
                        }, 
                        {
                            "header":Messages("descriptions.experiments.table.properties.definitions.levels.code"),
                            "property":"levels",
                            "order":false,
                            "hide":true,
                            "position":5,
                            "type":"text",
                            "filter": "getArray:'code'",
                            "render":"<div list-resize='cellValue' list-resize-min-size='3'>",
                        },
                        {
                            "header":Messages("descriptions.experiments.table.properties.definitions.required"),
                            "property":"required",
                            "order":false,
                            "hide":true,
                            "position":6,
                            "type":"boolean"    
                        },
                        {
                            "header":Messages("descriptions.experiments.table.properties.definitions.value.type"),
                            "property":"valueType",
                            "order":false,
                            "hide":true,
                            "position":7,
                            "type":"text",
                        },
                        {
                            "header":Messages("descriptions.experiments.table.properties.definitions.display.format"),
                            "property":"displayFormat",
                            "order":false,
                            "hide":true,
                            "position":8,
                            "type":"text",
                        },
                        {
                            "header":Messages("descriptions.experiments.table.properties.definitions.display.order"),
                            "property":"displayOrder",
                            "order":false,
                            "hide":true,
                            "position":9,
                            "type":"text",
                        },
                        {
                            "header":Messages("descriptions.experiments.table.properties.definitions.value.default"),
                            "property":"defaultValue",
                            "order":false,
                            "hide":true,
                            "position":10,
                            "type":"text",
                        },
                        {
                            "header":Messages("descriptions.experiments.table.properties.definitions.choice"),
                            "property":"choiceInList",
                            "order":false,
                            "hide":true,
                            "position":11,
                            "type":"boolean",
                        }, 
                        {
                            "header":Messages("descriptions.experiments.table.properties.definitions.value.possibles"),
                            "property":"possibleValues",
                            "order":true,
                            "hide":false,
                            "position":12,
                            "type":"text",
                            "filter": "getArray:'value'",
                            "render":"<div list-resize='cellValue' list-resize-min-size='5' vertical>",
                            "hide":true
                        }
                    ];          
                }
                
                function getInstrumentColumns(){
                    return [
                        {
                            "header":Messages("descriptions.experiments.table.type.name"),
                            "property":"name",
                            "order":true,
                            "hide":false,
                            "position":1,
                            "type":"text" ,
                            "mergeCells":true, 
                        },
                        {
                            "header":Messages("descriptions.experiments.table.instrument.type.name"),
                            "property":"typeName",
                            "order":true,
                            "hide":false,
                            "position":2,
                            "type":"text" , 
                        },
                        {
                            "header":Messages("descriptions.experiments.table.instrument.type.code"),
                            "property":"typeCode",
                            "order":true,
                            "hide":false,
                            "position":3,
                            "type":"text" , 
                        }
                    ];
                } 
                
                function getAtmColumns(){
                    return [
                        {
                            "header":Messages("descriptions.experiments.table.atomic.transfert.method"),
                            "property":"experimentType.atomicTransfertMethod",
                            "order":true,
                            "hide":true,
                            "position":1,
                            "type":"text",
                            "mergeCells":true,
                        },
                        {
                            "header":Messages("descriptions.experiments.table.category.code"),
                            "property":"experimentType.category.code",
                            "order":true,
                            "hide":true,
                            "position":2,
                            "type":"text",
                            "mergeCells":true,
                        },
                        {
                            "header":Messages("descriptions.experiments.table.type.name"),
                            "property":"experimentType.name",
                            "order":true,
                            "hide":true,
                            "position":3,
                            "type":"text"    
                        },
                        {
                            "header":Messages("descriptions.experiments.table.type.code"),
                            "property":"experimentType.code",
                            "order":true,
                            "hide":true,
                            "position":4,
                            "type":"text",
                        },
                        {
                            "header":Messages("descriptions.experiments.table.do.transfert"),
                            "property":"doTransfert",
                            "order":true,
                            "hide":true,
                            "position":5,
                            "type":"boolean",
                        },
                        {
                            "header":Messages("descriptions.experiments.table.mandatory.transfert"),
                            "property":"mandatoryTransfert",
                            "order":true,
                            "hide":true,
                            "position":6,
                            "type":"boolean",
                        },
                        {
                            "header":Messages("descriptions.experiments.table.do.quality.control"),
                            "property":"doQualityControl",
                            "order":true,
                            "hide":true,
                            "position":7,
                            "type":"boolean",
                        },
                        {
                            "header":Messages("descriptions.experiments.table.mandatory.quality.control"),
                            "property":"mandatoryQualityControl",
                            "order":true,
                            "hide":true,
                            "position":8,
                            "type":"boolean",
                        },
                        {
                            "header":Messages("descriptions.experiments.table.do.purification"),
                            "property":"doPurification",
                            "order":true,
                            "hide":true,
                            "position":9,
                            "type":"boolean",
                        },
                        {
                            "header":Messages("descriptions.experiments.table.mandatory.purification"),
                            "property":"mandatoryPurification",
                            "order":true,
                            "hide":true,
                            "position":10,
                            "type":"boolean",
                        }
                    ];
                } 
                
                function getPreviousColumns(){
                    return [
                        {
                            "header":Messages("descriptions.experiments.table.type.name"),
                            "property":"experimentTypeName",
                            "order":true,
                            "hide":false,
                            "position":1,
                            "type":"text",
                            "mergeCells":true,  
                        },
                        {
                            "header":Messages("descriptions.experiments.table.previous.exp.type.name"),
                            "property":"previousName",
                            "order":false,
                            "hide":false,
                            "position":2,
                            "type":"text" ,
                        },
                        {
                            "header":Messages("descriptions.experiments.table.previous.exp.type.code"),
                            "property":"previousCode",
                            "order":false,
                            "hide":false,
                            "position":3,
                            "type":"text" ,
                        }
                    ];
                } 

                function getCrossTableColumns(instrumentUsedTypes) {
                    return instrumentUsedTypes.reduce(instrumentUsedTypesReducer, firstColumns());

                    //---

                    function firstColumns() {
                        return [
                            {
                                "header":Messages("descriptions.experiments.table.type.name"),
                                "property":"name",
                                "order":true,
                                "hide":false,
                                "position":1,
                                "type":"text" ,
                            },
                            {
                                "header":Messages("descriptions.experiments.table.type.code"),
                                "property":"code",
                                "order":true,
                                "hide":false,
                                "position":2,
                                "type":"text" , 
                            },
                        ];
                    }

                    function instrumentUsedTypesReducer(columns, instrumentUsedType) {
                        if(columns.length > 2 && columns.some(isSameInstrumentUsedType)) return columns;
                        columns.push(createColumn());
                        return columns.sort(alphabeticalOrder);

                        //---

                        function isSameInstrumentUsedType(column) {
                            if(isInvalidComparison(column)) return false;
                            return column.header === instrumentUsedType.code;
                        }

                        function isInvalidComparison(column) {
                            return !(instrumentUsedType && instrumentUsedType.code && column && column.header);
                        }

                        function createColumn() {
                            return {
                                "header":instrumentUsedType.code,
                                "property":instrumentUsedType.code.replaceAll("-", ""),
                                "order":true,
                                "hide":true,
                                "position":3,
                                "type":"boolean" , 
                                "extraHeaders":{0:instrumentUsedType.name},
                            };
                        }

                        function alphabeticalOrder(columnA, columnB) {
                            if(!columnA.extraHeaders) return -1;
                            if(!columnB.extraHeaders) return 1;
                            return columnA.extraHeaders[0].localeCompare(columnB.extraHeaders[0]);
                        }
                    }

                }
            };

            var isInit = false;

            var initListService = function(){
                if(isInit) return;
                lists.refresh.instrumentUsedTypes();
                lists.refresh.experimentTypes({withoutExtTo: true});
                lists.refresh.propertyDefinitions();
                isInit=true;
            };

            var getRouteParams = function(isList){
                var params = {};
                if(isList) params.list = true;
                return params;
            };
    
            var getTypesRoute = function() {
                var typesParams = getRouteParams(false);
                return {
                    url: jsRoutes.controllers.experiments.api.ExperimentTypes.list().url,
                    params: typesParams
                };
            };

            var getNodesRoute = function() {
                var nodesParams = getRouteParams(false);
                return {
                    url: jsRoutes.controllers.experiments.api.ExperimentTypeNodes.list().url,
                    params: nodesParams
                };
            };

            var getWithoutExtToForm = function(){
                return {withoutExtTo: true};
            };
        
            var getTabsConfig = function(){
                return {
                    types: getTypesConfig(),
                    atms: getAtmsConfig(),
                    properties: getPropertiesConfig(),
                    instruments: getInstrumentsConfig(),
                    previous: getPreviousConfig(),
                    crossTable: getCrossTableConfig()
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
                        form: getWithoutExtToForm(),
                        lists : {
                            types : lists.getExperimentTypes
                        }
                    };
                }

                function getAtmsConfig(){
                    return {
                        route: getNodesRoute(),
                        form: {},
                        lists : {
                            types : lists.getExperimentTypes
                        }
                    };
                }
        
                function getPropertiesConfig(){
                    return {
                        route: getTypesRoute(),
                        form: getWithoutExtToForm(),
                        lists : {
                            types : lists.getExperimentTypes,
                            propertyDefinitions: getListForTab("propertyDefinitions(tabProperties)", lists.getPropertyDefinitions, function(){
                                var form = { objectTypeCode :  'Experiment' };
                                if(isExperimentTypeCodesSelected()) addExperimentTypeNamesToForm();
                                return form;

                                //---

                                function isExperimentTypeCodesSelected(){
                                    return searchService.tabs.properties.form.codes;
                                } 

                                function addExperimentTypeNamesToForm() {
                                    form.typeNames = searchService.tabs.properties.form.codes.map(toNames);
                                }

                                function toNames(propertyCode){
                                    return $filter('codes')(propertyCode,'type');
                                }
                            })
                        }
                    };
                }

                function getInstrumentsConfig(){
                    return {
                        route: getTypesRoute(),
                        form: getWithoutExtToForm(),
                        lists : {
                            types : lists.getExperimentTypes,
                            instrumentTypes : lists.getInstrumentUsedTypes
                        }
                    };
                }

                function getPreviousConfig(){
                    return {
                        route: getNodesRoute(),
                        form: {},
                        lists : {
                            types : lists.getExperimentTypes
                        }
                    };
                }

                function getCrossTableConfig(){
                    return {
                        route: getTypesRoute(),
                        form: getWithoutExtToForm(),
                        lists : {
                            types : lists.getExperimentTypes,
                            instrumentTypes : lists.getInstrumentUsedTypes
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
                    row.name = propDef.name; 
                    row.code = propDef.code; 
                    row.required = propDef.required; 
                    row.levels = propDef.levels;
                    row.valueType = propDef.valueType;
                    row.defaultValue = propDef.defaultValue;
                    row.possibleValues = propDef.possibleValues;
                    row.displayFormat = propDef.displayFormat;
                    row.displayOrder = propDef.displayOrder;
                    row.choiceInList = propDef.choiceInList;
                    return row;
                },
                instrumentUsedTypeValues : function(row, instrumentUsedType){
                    row.typeName = instrumentUsedType.name;
                    row.typeCode = instrumentUsedType.code;
                    return row;
                },
                previousExperimentTypeValues : function(row, previousExperimentType){
                    row.previousName = previousExperimentType.name;
                    row.previousCode = previousExperimentType.code;
                    return row;
                },
            };

            var extract = (function(){
                return {    
                    experimentType : function(rawExperimentType){
                        return {
                            name: rawExperimentType.name,
                            code: rawExperimentType.code,
                            active: rawExperimentType.active
                        };
                    },
                    atomicTransfertMethod : function(rawExperimentTypeNode){
                        var experimentType = rawExperimentTypeNode.experimentType;
                        return {
                            experimentType: {
                                atomicTransfertMethod: experimentType.atomicTransfertMethod,
                                category: {
                                    code: experimentType.category.code,
                                }, 
                                name: experimentType.name, 
                                code: experimentType.code,
                            },
                            doPurification: rawExperimentTypeNode.doPurification,
                            mandatoryPurification: rawExperimentTypeNode.mandatoryPurification,
                            doQualityControl: rawExperimentTypeNode.doQualityControl,
                            mandatoryQualityControl: rawExperimentTypeNode.mandatoryQualityControl,
                            doTransfert: rawExperimentTypeNode.doTransfert,
                            mandatoryTransfert: rawExperimentTypeNode.mandatoryTransfert,
                        };
                    },    
                    properties : function(rawExperimentType){
                        return extractFromSubList(rawExperimentType.propertiesDefinitions, buildBasicPropertiesRow, assign.propertyDefinitonValues);
            
                        //---
            
                        function buildBasicPropertiesRow(){
                            return {
                                "typeName": rawExperimentType.name, 
                                "typeCode": rawExperimentType.code
                            };
                        }
                    },
                    instrumentUsedTypes : function(rawExperimentType){
                        return extractFromSubList(rawExperimentType.instrumentUsedTypes, buildBasicInstrumentsRow, assign.instrumentUsedTypeValues);
            
                        //---
            
                        function buildBasicInstrumentsRow(){
                            return {
                                "name": rawExperimentType.name
                            };
                        }
                    },
                    previousExperimentTypes : function(rawExperimentTypeNode){
                        return extractFromSubList(rawExperimentTypeNode.previousExperimentTypes, buildBasicPreviousRow, assign.previousExperimentTypeValues);
            
                        //---
            
                        function buildBasicPreviousRow(){
                            var experimentType = rawExperimentTypeNode.experimentType;
                            return {
                                experimentTypeName: experimentType.name,
                            };
                        }
                    },
                    crossData : function(rawExperimentTypeNode) {
                        var row = {
                            "code": rawExperimentTypeNode.code,
                            "name": rawExperimentTypeNode.name
                        };
                        rawExperimentTypeNode.instrumentUsedTypes.forEach(addInstrumentColumn);
                        return row;

                        //---

                        function addInstrumentColumn(instrumentUsedType) {
                            var instrumentUsedTypeCode = instrumentUsedType.code.replaceAll("-", "");
                            row[instrumentUsedTypeCode] = true;
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
            
            var searchService = {
                getColumns:getColumns,
                isRouteParam:false,
                lists : lists,
                tabs : getTabsConfig(),

                resetForms : function(){
                    this.resetTypesForm();
                    this.resetPropertiesForm();
                    this.resetInstrumentsForm();
                    this.resetAtmsForm();
                    this.resetPreviousForm();
                    this.resetCrossTableForm();
                },

                resetTypesForm : function(){
                    searchService.tabs.types.form = getWithoutExtToForm();
                },
    
                resetPropertiesForm : function(){
                    searchService.tabs.properties.form = getWithoutExtToForm();
                },

                resetInstrumentsForm : function(){
                    searchService.tabs.instruments.form = getWithoutExtToForm();
                },

                resetAtmsForm : function(){
                    searchService.tabs.atms.form = {};
                },

                resetPreviousForm : function(){
                    searchService.tabs.previous.form = {};
                },

                resetCrossTableForm : function(){
                    searchService.tabs.crossTable.form = getWithoutExtToForm();
                },

                searchTypes : function(){
                    var datatable = searchService.tabs.types.datatable;
                    spinnerOn(datatable);
                    return callTypesRoute()
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
                        var rawExperimentTypes = queryResult.data;
                        if(!rawExperimentTypes) return [];
                        return rawExperimentTypes.map(extract.experimentType);
                    }
    
                    function setTypes(types){
                        datatable.setData(types);
                    }
                },

                searchAtms : function(){
                    var datatable = searchService.tabs.atms.datatable;
                    spinnerOn(datatable);
                    return callAtmsRoute()
                        .then(transformData)
                        .then(setAtms)
                        .then(function(){
                            spinnerOff(datatable);
                        });
                    
                    //---
    
                    function callAtmsRoute(){
                        var atmsRoute = searchService.tabs.atms.route;
                        var form = searchService.tabs.atms.form;
                        return callDescriptionRoute(atmsRoute, form);
                    }
    
                    function transformData(queryResult) {
                        var rawExperimentTypeNodes = queryResult.data;
                        if(!rawExperimentTypeNodes) return [];
                        return rawExperimentTypeNodes.map(extract.atomicTransfertMethod);
                    }
    
                    function setAtms(atms){
                        datatable.setData(atms);
                    }
                },

                searchProperties : function(){
                    var datatable = searchService.tabs.properties.datatable;
                    spinnerOn(datatable);
                    return callPropertiesRoute()
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
                        var rawExperimentTypes = queryResult.data;
                        if(!rawExperimentTypes) return [];
                        return rawExperimentTypes.flatMap(extract.properties);
                    }
    
                    function setProperties(properties){
                        datatable.setData(properties);
                    }
                },

                searchInstruments : function(){
                    var datatable = searchService.tabs.instruments.datatable;
                    spinnerOn(datatable);
                    return callInstrumentsRoute()
                        .then(transformData)
                        .then(setInstruments)
                        .then(function(){
                            spinnerOff(datatable);
                        });
                    
                    //---
    
                    function callInstrumentsRoute(){
                        var instrumentsRoute = searchService.tabs.instruments.route;
                        var form = searchService.tabs.instruments.form;
                        return callDescriptionRoute(instrumentsRoute, form);
                    }
    
                    function transformData(queryResult) {
                        var rawExperimentTypes = queryResult.data;
                        if(!rawExperimentTypes) return [];
                        return rawExperimentTypes.flatMap(extract.instrumentUsedTypes);
                    }
    
                    function setInstruments(instruments){
                        datatable.setData(instruments);
                    }
                },

                searchPrevious : function(){
                    var datatable = searchService.tabs.previous.datatable;
                    spinnerOn(datatable);
                    return callPreviousRoute()
                        .then(transformData)
                        .then(setPrevious)
                        .then(function(){
                            spinnerOff(datatable);
                        });
                    
                    //---
    
                    function callPreviousRoute(){
                        var previousRoute = searchService.tabs.previous.route;
                        var form = searchService.tabs.previous.form;
                        return callDescriptionRoute(previousRoute, form);
                    }
    
                    function transformData(queryResult) {
                        var rawExperimentTypeNodes = queryResult.data;
                        if(!rawExperimentTypeNodes) return [];
                        return rawExperimentTypeNodes.flatMap(extract.previousExperimentTypes);
                    }
    
                    function setPrevious(previous){
                        datatable.setData(previous);
                    }
                },

                searchCrossTable : function(){
                    var datatable = searchService.tabs.crossTable.datatable;
                    spinnerOn(datatable);
                    return callCrossTableRoute()
                        .then(getQueryData)
                        .then(setColumnsConfig)
                        .then(transformData)
                        .then(setCrossData)
                        .then(function(){
                            spinnerOff(datatable);
                        });
                    
                    //---
    
                    function callCrossTableRoute(){
                        var crossTableRoute = searchService.tabs.crossTable.route;
                        var form = searchService.tabs.crossTable.form;
                        return callDescriptionRoute(crossTableRoute, form);
                    }

                    function getQueryData(queryResult){
                        var rawExperimentTypes = queryResult.data;
                        return rawExperimentTypes ? rawExperimentTypes : [];
                    }
    
                    function setColumnsConfig(rawExperimentTypes){
                        var instrumentUsedTypes = rawExperimentTypes.flatMap(getInstrumentUsedTypes);
                        var crossTableColumnsConfig = getColumns("crossTable", instrumentUsedTypes);
                        datatable.setColumnsConfig(crossTableColumnsConfig);
                        return rawExperimentTypes;

                        //---

                        function getInstrumentUsedTypes(experimentType) {
                            return experimentType.instrumentUsedTypes;
                        }
                    }

                    function transformData(rawExperimentTypes) {
                        return rawExperimentTypes.map(extract.crossData);
                    }
    
                    function setCrossData(data){
                        datatable.setData(data);
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
                    handleForm();
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
                                    by:'name',
                                    reverse :false,
                                    mode:'local'
                                }
                            });
                            default: return datatableConfig;
                        }

                        //---

                        function withParams(params){
                            var config = Object.assign({}, datatableConfig);
                            return  Object.assign(config, params);
                        }
                    }
    
                    function registerDatatables() {
                        if(!datatableConfig) {
                            if(!isMainDatatable()) return;
                            registerMainDatatable();
                        } else {
                            registerDatatable('types');
                            registerDatatable('atms');
                            registerDatatable('properties');
                            registerDatatable('instruments');
                            registerDatatable('previous');
                            registerDatatable('crossTable');
                        }                            
    
                        //---

                        function isMainDatatable() {
                            return angular.isDefined(mainService.getDatatable());
                        }

                        function registerMainDatatable() {
                            searchService.datatable = mainService.getDatatable();
                        }
    
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
                        setRouteParams();
    
                        //---

                        function setRouteParams(){
                            var count = 0;
                            for(var p in $routeParams){
                                count++;
                                break;
                            }
                            if(count > 0){
                                searchService.isRouteParam = true;
                                searchService.form = $routeParams;
                            }
                        }
                    }

                    function handleForm() {
                        if(isMainForm()){
                            setMainForm();
                        }else{
                            searchService.resetForms();						
                        }

                        //---

                        function isMainForm() {
                            return angular.isDefined(mainService.getForm());
                        }

                        function setMainForm() {
                            searchService.form = mainService.getForm();
                        }
                    }
                    
                }

            };
        return searchService;				
    }
]);                                