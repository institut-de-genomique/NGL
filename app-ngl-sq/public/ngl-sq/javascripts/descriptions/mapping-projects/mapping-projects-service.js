"use strict"

angular.module('ngl-sq.descriptionsServices.mappingprojects', []).
    factory('descriptionsMappingProjectsSearchService', ['$q', '$http', 'mainService', 'lists', 'datatable', 'messages',
        function ($q, $http, mainService, lists, datatable, messages) {


            var getColumns = function () {
                return [
                    {
                        "header": Messages("descriptions.mapping.projects.parent.code"),
                        "property": "parentCode",
                        "order": true,
                        "hide": true,
                        "position": 1,
                        "type": "text",
                    },
                    {
                        "header": Messages("descriptions.mapping.projects.parent.name"),
                        "property": "parentCode | codes:'project'",
                        "order": true,
                        "hide": true,
                        "position": 2,
                        "type": "text",
                    },
                    {
                        "header": Messages("descriptions.mapping.projects.child.code"),
                        "property": "childCode",
                        "order": true,
                        "hide": true,
                        "position": 3,
                        "type": "text",
                    },
                    {
                        "header": Messages("descriptions.mapping.projects.child.name"),
                        "property": "childCode | codes:'project'",
                        "order": true,
                        "hide": true,
                        "position": 4,
                        "type": "text",
                    }
                ];
            };

            var getMappingsRoute = function (code, isList) {
                return {
                    url: jsRoutes.controllers.commons.api.Parameters.get('map-parameter', code).url
                };
            };

            var getTabsConfig = function () {
                return {
                    dnaExtraction: getMappingTabConfig('dna-extraction-mapping-rules'),
                    rnaExtraction: getMappingTabConfig('rna-extraction-mapping-rules'),
                    tagPcr: getMappingTabConfig('tag-pcr-mapping-rules'),
                    prepHic: getMappingTabConfig('hi-c-dna-mapping-rules'),
                    largeRnaIsolation: getMappingTabConfig('large-rna-isolation>200nt'),
                    smallRnaIsolation: getMappingTabConfig('small-rna-isolation-17-200nt'),
                    createMappingProject: getCreateMappingConfig(),
                };

                //---

                function getMappingTabConfig(code) {
                    var mappingConfig = {
                        route: getMappingsRoute(code),
                        form: {},
                        lists: {
                            projects: [],
                            setProjects: function (mappingEntries) {
                                var uniqueProjectCodes = new Set(mappingEntries.flat());
                                var projects = Array.from(uniqueProjectCodes).map(toObj);
                                mappingConfig.lists.projects = projects;

                                //---

                                function toObj(projectCode) {
                                    return { code: projectCode };
                                }
                            }
                        }
                    };
                    return mappingConfig;
                }

                function getCreateMappingConfig() {
                    var mappingConfig = {
                        entry: {},
                        messages: messages(),
                        lists: {
                            mapParameter: lists.getMapParameter,
                            mapProjects: lists.getProjects,
                        }
                    };
                    return mappingConfig;
                }
            };

            var spinnerOn = function (datatable) {
                datatable.setSpinner(true);
            };

            var spinnerOff = function (datatable) {
                datatable.setSpinner(false);
            };

            var callDescriptionRoute = function (route) {
                var url = route.url;
                return $http.get(url);
            };

            var extract = {
                mappingProjects: function (mapEntry) {
                    return {
                        parentCode: mapEntry[0],
                        childCode: mapEntry[1]
                    };
                }
            };

            var isInit = false;

            var initListService = function () {
                if (!isInit) {
                    lists.refresh.mapParameter()
                    lists.refresh.projects();
                    isInit = true;
                }
            };

            var searchService = {

                getColumns: getColumns,
                isRouteParam: false,
                lists: lists,
                tabs: getTabsConfig(),
                resetDnaExtractionForm: function () {
                    searchService.tabs.dnaExtraction.form = {};
                },

                resetRnaExtractionForm: function () {
                    searchService.tabs.rnaExtraction.form = {};
                },

                resetTagPcrForm: function () {
                    searchService.tabs.tagPcr.form = {};
                },

                resetPrepHicForm: function () {
                    searchService.tabs.prepHic.form = {};
                },

                resetLargeRnaIsolationForm: function () {
                    searchService.tabs.largeRnaIsolation.form = {};
                },

                resetSmallRnaIsolationForm: function () {
                    searchService.tabs.smallRnaIsolation.form = {};
                },

                resetCreateMappingProjectForm: function () {
                    searchService.tabs.createMappingProject.entry = {}
                },

                search: function () {
                    this.searchDnaExtractionMappings();
                    this.searchRnaExtractionMappings();
                    this.searchTagPcrMappings();
                    this.searchPrepHicMappings();
                    this.searchLargeRnaIsolationMappings();
                    this.searchSmallRnaIsolationMappings();
                },
                searchMappings: function (tab) {
                    var datatable = tab.datatable;
                    spinnerOn(datatable);
                    callTabRoute()
                        .then(transformData)
                        .then(setMappings)
                        .then(function () {
                            spinnerOff(datatable);
                        });

                    //---

                    function callTabRoute() {
                        return callDescriptionRoute(tab.route);
                    }

                    function transformData(queryResult) {

                        var rawMapParameter = getRawMapParameter();
                        return extractRowsFromRawData();

                        //---

                        function getRawMapParameter() {
                            return queryResult.data;
                        }

                        function extractRowsFromRawData() {
                            if (!rawMapParameter) return [];
                            var mappingEntries = Object.entries(rawMapParameter.map);
                            tab.lists.setProjects(mappingEntries);
                            if (isProjectFilter()) mappingEntries = mappingEntries.filter(areProjectsInForm);
                            return mappingEntries.map(extract.mappingProjects);
                        }

                        function isProjectFilter() {
                            return Array.isArray(tab.form.projectCodes) && tab.form.projectCodes.length > 0;
                        }

                        function areProjectsInForm(mapEntry) {
                            return tab.form.projectCodes.includes(mapEntry[0]) || tab.form.projectCodes.includes(mapEntry[1]);
                        }

                    }

                    function setMappings(mappings) {
                        datatable.setData(mappings);
                    }
                },
                searchDnaExtractionMappings: function () {
                    this.searchMappings(searchService.tabs.dnaExtraction);
                },

                searchRnaExtractionMappings: function () {
                    this.searchMappings(searchService.tabs.rnaExtraction);
                },

                searchTagPcrMappings: function () {
                    this.searchMappings(searchService.tabs.tagPcr);
                },

                searchPrepHicMappings: function () {
                    this.searchMappings(searchService.tabs.prepHic);
                },

                searchLargeRnaIsolationMappings: function () {
                    this.searchMappings(searchService.tabs.largeRnaIsolation);
                },

                searchSmallRnaIsolationMappings: function () {
                    this.searchMappings(searchService.tabs.smallRnaIsolation);
                },

                /**
                 * initialise the service
                 */
                init: function ($routeParams, datatableConfig) {

                    var createDatatable = datatable;

                    initListService();
                    addMessagesToDatatableConfig();
                    registerDatatables();
                    handleRouteParams();
                    handleMessages();

                    //---

                    function addMessagesToDatatableConfig() {
                        datatableConfig.messages = {
                            transformKey: function (key, args) {
                                return Messages(key, args);
                            }
                        };
                    }

                    function registerDatatables() {
                        if (!datatableConfig) return;
                        registerDatatable('dnaExtraction');
                        registerDatatable('rnaExtraction');
                        registerDatatable('tagPcr');
                        registerDatatable('prepHic');
                        registerDatatable('largeRnaIsolation');
                        registerDatatable('smallRnaIsolation');
                        registerDatatable('createMappingProject');

                        //---

                        function registerDatatable(tab) {
                            var datatable = createDatatable(datatableConfig);
                            searchService.tabs[tab].datatable = datatable;
                            mainService.setDatatable(datatable);
                            var columns = getColumns();
                            datatable.setColumnsConfig(columns);
                        }
                    }

                    function handleRouteParams() {
                        if (angular.isDefined($routeParams)) setRouteParams($routeParams);

                        //---

                        function setRouteParams() {
                            searchService.isRouteParam = true;
                            searchService.form = $routeParams;
                        }
                    }

                    function handleMessages() {
                        datatableConfig.messages = {
                            transformKey: function (key, args) {
                                return Messages(key, args);
                            }
                        };
                    }
                }
            };
            return searchService
        }
    ])
    .factory('mappingProjectNewService', ['lists', 'messages', '$routeParams', 'datatable', 'mainService', '$http',
        function (lists, messages, $routeParams, datatable, mainService, $http) {

            var getColumns = function () {
                return [
                    {
                        "header": Messages("descriptions.mapping.projects.parent.code"),
                        "property": "parentCode",
                        "order": true,
                        "hide": true,
                        "position": 1,
                        "type": "text",
                    },
                    {
                        "header": Messages("descriptions.mapping.projects.parent.name"),
                        "property": "parentCode | codes:'project'",
                        "order": true,
                        "hide": true,
                        "position": 2,
                        "type": "text",
                    },
                    {
                        "header": Messages("descriptions.mapping.projects.child.code"),
                        "property": "childCode",
                        "order": true,
                        "hide": true,
                        "position": 3,
                        "type": "text",
                    },
                    {
                        "header": Messages("descriptions.mapping.projects.child.name"),
                        "property": "childCode | codes:'project'",
                        "order": true,
                        "hide": true,
                        "position": 4,
                        "type": "text",
                    }
                ];
            };

            var spinnerOn = function (datatable) {
                datatable.setSpinner(true);
            };

            var spinnerOff = function (datatable) {
                datatable.setSpinner(false);
            };

            var extract = {
                mappingProjects: function (mapEntry) {
                    return {
                        parentCode: mapEntry[0],
                        childCode: mapEntry[1]
                    };
                }
            };

            var isInit = false;

            var initListService = function () {
                if (!isInit) {
                    lists.refresh.mapParameter()
                    lists.refresh.projects();
                    isInit = true;
                }
            };

            function createNewService() {
                var newService = {
                    isRouteParam: false,
                    route: {},
                    getColumns: getColumns,
                    entry: {},
                    lists: lists,
                    messages: messages(),
                    setEditableComment : function(copyComment) {
                        newService.entry.comment = copyComment
                    },
                    searchCreateMappingProjectDatatable: function () {
                        var codeMapParameter = $routeParams.typeCode
                        var datatable = newService.datatable;
                        spinnerOn(datatable)
                        callMappingRoute(codeMapParameter)
                            .then(transformData)
                            .then(setMappings)
                            .then(function () {
                                spinnerOff(datatable);
                            });

                        //---

                        function callMappingRoute(code) {
                            var url = getMappingsRoute(code);
                            return $http.get(url);

                            //---

                            function getMappingsRoute(code) {
                                return jsRoutes.controllers.commons.api.Parameters.get('map-parameter', code).url;
                            };
                        };

                        function transformData(queryResult) {

                            var rawMapParameter = getRawMapParameter();
                            return extractRowsFromRawData();

                            //---

                            function getRawMapParameter() {
                                return queryResult.data;
                            }

                            function extractRowsFromRawData() {
                                if (!rawMapParameter) return [];
                                var mappingEntries = Object.entries(rawMapParameter.map);
                                return mappingEntries.map(extract.mappingProjects);
                            }

                        }

                        function setMappings(mappings) {
                            datatable.setData(mappings);
                        }
                    },
                    saveMappingEntry : function(mapParameterEntry,config) {

                        var route = jsRoutes.controllers.commons.api.Parameters.save().url;
                        newService.messages.clear()
                        $http.post(route, mapParameterEntry, config).success(onSuccess).error(onError)

                        function onSuccess() {
                            newService.messages.setSuccess("save");
                            newService.searchCreateMappingProjectDatatable()
                        }

                        function onError(data) {
                            newService.messages.setError("save");
                            newService.messages.setDetails(data);
                        }

                    },
                    init: function ($routeParams, datatableConfig) {
                        var createDatatable = datatable;
                        initListService();
                        registerDatatable();
                        handleRouteParams();

                        function registerDatatable() {
                            if (!datatableConfig) return;
                            var datatable = createDatatable(datatableConfig);
                            newService.datatable = datatable;
                            mainService.setDatatable(datatable);
                            var columns = getColumns();
                            datatable.setColumnsConfig(columns);
                        }

                        function handleRouteParams() {
                            if (angular.isDefined($routeParams)) setRouteParams($routeParams);

                            //---

                            function setRouteParams() {
                                newService.isRouteParam = true;
                                newService.form = $routeParams;
                            }
                        }
                    }
                };
                return newService
            }

            return createNewService;
        }])