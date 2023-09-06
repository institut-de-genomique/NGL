"use strict";
 
 angular.module('ngl-sq.descriptionsServices.imports', []).
	factory('descriptionsImportsSearchService', ['$http', 'mainService', 'lists', 'datatable', 
		                         function($http,   mainService,   lists,   datatable){

		var getColumns = function(index){

			if(!index || index === "types") return getTypesColumns();
			if(index === "properties") return getPropertiesColumns();
			return [];

			//---

			function getTypesColumns() {
				return [
					{
						"header":Messages("descriptions.imports.table.type.name"),
						"property":"name",
						"order":true,
						"hide":false,
						"position":1,
						"type":"text" 
					},
					{
						"header":Messages("descriptions.imports.table.type.code"),
						"property":"code",
						"order":true,
						"hide":false,
						"position":2,
						"type":"text"
					},
					{
						"header":Messages("descriptions.imports.table.type.active"),
						"property":"active",
						"order":true,
						"hide":true,
						"position":3,
						"type":"boolean"    
					}
				];
			}

			function getPropertiesColumns() {
				return [
					{
						"header":Messages("descriptions.imports.table.property.definitions.name"),
						"property":"name",
						"order":true,
						"hide":true,
						"position":1,
						"type":"text",
						"mergeCells":true,
					},
					{
						"header":Messages("descriptions.imports.table.property.definitions.code"),
						"property":"code",
						"order":true,
						"hide":true,
						"position":2,
						"type":"text",
						"mergeCells":true,
					},
					{
						"header":Messages("descriptions.imports.table.property.definitions.common.type.name"),
						"property":"commonInfoType.name",
						"order":true,
						"hide":true,
						"position":3,
						"type":"text",
						"mergeCells":false,
					},
					{
						"header":Messages("descriptions.imports.table.property.definitions.common.type.code"),
						"property":"commonInfoType.code",
						"order":true,
						"hide":true,
						"position":4,
						"type":"text",
						"mergeCells":false,
					},
					{
						"header":Messages("descriptions.imports.table.property.definitions.active"),
						"property":"active",
						"order":true,
						"hide":true,
						"position":5,
						"type":"boolean"
					},
					{
						"header":Messages("descriptions.imports.table.property.definitions.levels.code"),
						"property":"levels",
						"order":true,
						"hide":false,
						"position":6,
						"type":"text",
						"filter": "getArray:'code'",
						"render":"<div list-resize='cellValue' list-resize-min-size='3'>"
					},
					{
						"header":Messages("descriptions.imports.table.property.definitions.required"),
						"property":"required",
						"order":true,
						"hide":true,
						"position":7,
						"type":"boolean",
						"mergeCells":false,
					},
					{
						"header":Messages("descriptions.imports.table.property.definitions.value.type"),
						"property":"valueType",
						"order":true,
						"hide":false,
						"position":8,
						"type":"text"
					},
					{
						"header":Messages("descriptions.imports.table.property.definitions.value.possibles"),
						"property":"possibleValues",
						"order":true,
						"hide":true,
						"position":9,
						"type":"text",
						"mergeCells":true,
						"filter": "getArray:'value' | orderBy",
						"render":"<div list-resize='cellValue' list-resize-min-size='5'vertical>",
					}
				];
			}

		};

		var getRouteParams = function(isList){
			var params = {};
			if(isList) params.list = true;
			return params;
		};

		var getTypesRoute = function() {
			var typesParams = getRouteParams(false);
			return {
				url: jsRoutes.controllers.samples.api.ImportTypes.list().url,
				params: typesParams
			};
		};

		var getPropertiesRoute = function() {
			var propertiesParams = getRouteParams(false);
			Object.assign(propertiesParams, getImportPropertiesParams());
			return {
				url: jsRoutes.controllers.commons.api.PropertyDefinitions.list().url,
				params: propertiesParams
			};
		};

		var getTabsConfig = function(){
            return {
                types: getTypesConfig(),
                properties: getPropertiesConfig()
            };
    
            //---

			function getTypesConfig(){
                return {
                    route: getTypesRoute(),
                    form: {},
                    lists: {}
                };
            }
    
            function getPropertiesConfig(){
                return {
                    route: getPropertiesRoute(),
                    form: {},
                    lists: {
                        types: lists.getImportTypes,
						properties: getListForTab("propertyDefinitions(import)", lists.getPropertyDefinitions, getImportPropertiesParams)
                    }
                };
            }

			function getListForTab(label, getter, getParams) {
                return function(){
                    if(!getter) {return [];}
                    var params = getParams ? getParams() : {};
                    var key = label + JSON.stringify(params);
                    params.list = true;
                    return getter(params, key);
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

		var getImportPropertiesParams = function(){
			return { objectTypeCode: "Import" };
		};

		var isInit = false;
		
		var initListService = function(){
			if(!isInit){
				lists.refresh.propertyDefinitions(getImportPropertiesParams());
				lists.refresh.importTypes();
				isInit=true;
			}
		};
		
		var searchService = {
				getColumns:getColumns,
				isRouteParam:false,
                lists : lists,
				tabs : getTabsConfig(),

                resetForms : function(){
                    this.resetTypesForm();
                    this.resetPropertiesForm();
                },

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
						return getRawImportTypes();
	
						//---
	
						function getRawImportTypes(){
							return queryResult.data;
						}
					}
    
                    function setTypes(types){
                        datatable.setData(types);
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
						return getRawPropertyDefinitions();
	
						//---
	
						function getRawPropertyDefinitions(){
							return queryResult.data;
						}
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
							case "types": return withParams({ 
								order:{
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
                            registerDatatable('properties');
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