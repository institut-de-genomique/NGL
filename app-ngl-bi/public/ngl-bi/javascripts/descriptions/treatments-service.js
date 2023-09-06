"use strict";
 
 angular.module('ngl-sq.descriptionsServices.treatments', []).
	factory('descriptionsTreatmentsSearchService', ['$http', 'mainService', 'lists', 'datatable', 
		                         function($http,   mainService,   lists,   datatable){ 

		var getColumns = function(){
			return [
                {
                    "header":Messages("descriptions.treatments.table.category"),
                    "property":"typeCategory",
                    "order":true,
                    "position":1,
                    "type":"text",
                    "mergeCells":true,
                    "hide":true
                },
                {
                    "header":Messages("descriptions.treatments.table.type.name"),
                    "property":"typeName",
                    "order":true,
                    "position":2,
                    "type":"text",
                    "mergeCells":true,
                    "hide":true
                },
                {
                    "header":Messages("descriptions.treatments.table.type.code"),
                    "property":"typeCode",
                    "order":true,
                    "position":3,
                    "type":"text",
                    "mergeCells":true,
                    "hide":true
                },
                {
                    "header":Messages("descriptions.treatments.table.type.names"),
                    "property":"typeNames",
                    "order":true,
                    "position":4,
                    "type":"text",
                    "filter": "split:','",
                    "render":"<div list-resize='cellValue' vertical list-resize-min-size='10'>",
                    "mergeCells":true,
                    "hide":true
                },
                {
                    "header":Messages("descriptions.treatments.table.type.context.code"),
                    "property":"typeContexts",
                    "order":true,
                    "position":5,
                    "type":"text",
                    "filter": "getArray:'code'",
                    "render":"<div list-resize='cellValue' list-resize-min-size='5'>",
                    "mergeCells":true,
                    "hide":true
                },
                {
                    "header":Messages("descriptions.treatments.table.properties.definitions.name"),
                    "property":"name",
                    "order":true,
                    "position":6,
                    "type":"text",
                    "hide":true  
                },
                {
                    "header":Messages("descriptions.treatments.table.properties.definitions.code"),
                    "property":"code",
                    "order":true,
                    "position":7,
                    "type":"text",
                    "hide":true
                },
                {
                    "header":Messages("descriptions.treatments.table.properties.definitions.required"),
                    "property":"required",
                    "order":true,
                    "position":8,
                    "type":"boolean",
                    "hide":true   
                },
                {
                    "header":Messages("descriptions.treatments.table.properties.definitions.levels.code"),
                    "property":"levels",
                    "order":true,
                    "position":9,
                    "type":"text",
                    "filter": "getArray:'code'",
                    "render":"<div list-resize='cellValue' list-resize-min-size='6'>",
                    "hide":true
                },
                {
                    "header":Messages("descriptions.treatments.table.properties.definitions.value.type"),
                    "property":"valueType",
                    "order":true,
                    "position":10,
                    "type":"text",
                    "hide":true
                },
                {
                    "header":Messages("descriptions.treatments.table.properties.definitions.value.typeObject"),
                    "property":"propertyValueType",
                    "order":true,
                    "position":11,
                    "type":"text",
                    "hide":true
                }
            ];
        };

        var spinnerOn = function() {
            searchService.datatable.setSpinner(true);
        };

        var spinnerOff = function() {
            searchService.datatable.setSpinner(false);
        };

        var setData = function(treatments){
            searchService.datatable.setData(treatments);
        };

        var getTreatmentTypesRoute = function(isList) {
            return {
                url: jsRoutes.controllers.treatmenttypes.api.TreatmentTypes.list().url,
                params: getRouteParams(isList)
            };
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

        var getRouteParams = function(isList){
            var params = {};
            if(isList) params.list = true;
            return params;
        };

        var callTreatmentsRoute = function(){
            var route = getTreatmentTypesRoute();
            var form = searchService.form;
            return callDescriptionRoute(route, form);
        };

        var getTreatmentslists = function(){

            return {
                categories: lists.getTreatmentCategories,
                types: getFilteredList("treatmentTypes", lists.getTreatmentTypes, function(){
                    return { categoryNames: searchService.form.categoryNames };
                }, getUniqueNames)
            };

            //---

            function getUniqueNames(listObjects) {
                if(!listObjects) return listObjects;
                var names = new Set();
                return listObjects.filter(isntDuplicate);

                //---

                function isntDuplicate(listObject) {
                    if(names.has(listObject.name)) return false;
                    names.add(listObject.name);
                    return true;
                }
            }

            function getFilteredList(label, getter, getParams, callback) {
                return function(){
                    if(!getter) {return [];}
                    var params = getParams ? getParams() : {};
                    var key = label + JSON.stringify(params);
                    params.list = true;
                    var values = getter(params, key);
                    return callback ? callback(values) : values;
                };
            }

        };

        var assign = {
            propertyDefinitonValues : function(row, propDef) {
                row.name = propDef.name;
                row.code = propDef.code;
                row.required = propDef.required;
                row.levels = propDef.levels;
                row.valueType = propDef.valueType;
                row.propertyValueType = propDef.propertyValueType;
                return row;
            }
        };

        var extract = (function(){
            
            return {
                treatmentType : function(rawTreatmentType){
                    return extractFromSubList(rawTreatmentType.propertiesDefinitions, buildBasicRow, assign.propertyDefinitonValues);
        
                    //---

                    function buildBasicRow(){
                        return {
                            "typeCategory": rawTreatmentType.category.name,
                            "typeName": rawTreatmentType.name, 
                            "typeCode": rawTreatmentType.code,
                            "typeNames": rawTreatmentType.names, 
                            "typeContexts": rawTreatmentType.contexts,
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

        var isInit = false;

        var initListService = function(){
            if(isInit) return;
			lists.refresh.treatmentTypes();
			isInit=true;
		};
		
		var searchService = {
				getColumns:getColumns,
				datatable:undefined,
				isRouteParam:false,
                lists : getTreatmentslists(),

				resetForm : function(){
					searchService.form = {};
				},
				
				search : function(){
                    setMainServiceForm();
                    spinnerOn();
                    callTreatmentsRoute()
                        .then(transformData)
                        .then(setData)
                        .then(spinnerOff);

                    //---

                    function setMainServiceForm(){
                        mainService.setForm(searchService.form);
                    }

                    function transformData(queryResult) {

                        var rawTreatmentTypes = queryResult.data;
                        return extractRowsFromRawData();

                        //---

                        function extractRowsFromRawData(){
                            return rawTreatmentTypes.flatMap(extract.treatmentType);
                        }
                    }
                },

				/**
				 * initialise the service
				 */
				init : function($routeParams, datatableConfig){
                    
                    var createDatatable = datatable;
                    
                    initListService();
                    addMessagesToDatatableConfig();
                    handleDatatable();
                    handleMainServiceForm();
                    handleRouteParams();

                    //---

                    function addMessagesToDatatableConfig() {
                        datatableConfig.messages = {
							transformKey: function(key, args) {
		                        return Messages(key, args);
		                    }
					    };
                    }

                    function handleDatatable() {

                        if(isMainServiceDatatable()){
                           setMainSeriveDatatable();
                        } else if(datatableConfig) {
                            registerDatatable();
                        }

                        //---

                        function isMainServiceDatatable(){
                            return angular.isDefined(mainService.getDatatable());
                        }

                        function setMainSeriveDatatable(){
                            searchService.datatable = mainService.getDatatable();
                        }

                        function registerDatatable() {
                            var datatable = createDatatable(datatableConfig);
                            searchService.datatable = datatable;
                            mainService.setDatatable(datatable);
                            var columns = getColumns();
                            datatable.setColumnsConfig(columns);
                        }
                    }

                    function handleMainServiceForm(){
                        if(isMainServiceForm()){
                            setMainServiceForm();
                        }else{
                            resetForm();				
                        }

                        function isMainServiceForm(){
                            return angular.isDefined(mainService.getForm());
                        }

                        function setMainServiceForm() {
                            searchService.form = mainService.getForm();
                        }

                        function resetForm(){
                            searchService.resetForm();
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