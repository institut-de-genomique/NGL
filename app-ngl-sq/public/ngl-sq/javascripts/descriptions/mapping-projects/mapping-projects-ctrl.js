"use strict"
angular.module('home').controller('MappingProjectsCtrl', ['$scope', '$routeParams', 'mainService', 'tabService', "descriptionsMappingProjectsSearchService", 'mappingProjectNewService',
	function ($scope, $routeParams, mainService, tabService, descriptionsMappingProjectsSearchService, mappingProjectNewService) {

		handleHomePage();
		setDatatableConfig();
		setSearchService();
		setResetsForm();
		initSearchService();
		defineScopeFunctions();

		//---
		function handleHomePage() {
			if (isHomePageDefined()) return;
			defineHomePage();
			setTabs();

			//---

			function isHomePageDefined() {
				return angular.isDefined($scope.getHomePage());
			}

			function defineHomePage() {
				mainService.setHomePage('mappingprojects');
			}

			function setTabs() {
				tabService.addTabs(controllerTabs());
				tabService.activeTab(0);

				//---

				function controllerTabs() {
					return {
						label: Messages('descriptions.tabs.mapping.projects'),
						href: defaultTabUrl(),
						remove: false
					};
				}

				function defaultTabUrl() {
					return jsRoutes.controllers.descriptions.tpl.Descriptions.home("mappingprojects").url;
				}
			}
		}

		function setDatatableConfig() {
			$scope.datatableConfig = {
				show: {
					active: true
				},
				order: {
					by: 'parentCode',
					reverse: true,
					mode: 'local'
				},
				pagination: {
					mode: 'local',
					numberRecordsPerPage: 50,
				},
				select: {
					active: false,
					showButton: false,
				},
				group: {
					active: false,
					showOnlyGroups: false,
					showButton: false
				},
				show: {
					active: false,
					showButton: false,
				},
				hide: {
					active: true,
					showButton: true,
				},
				cancel: {
					active: false,
					showButton: false
				},
				edit: {
					active: false
				},
				exportCSV: {
					active: true,
					showButton: true,
				}
			};
		}

		function setSearchService() {
			$scope.searchService = descriptionsMappingProjectsSearchService;
		}

		function setResetsForm() {
			$scope.resetDnaExtractionForm = function () {
				$scope.searchService.resetDnaExtractionForm();
			};

			$scope.resetRnaExtractionForm = function () {
				$scope.searchService.resetRnaExtractionForm();
			};

			$scope.resetTagPcrForm = function () {
				$scope.searchService.resetTagPcrForm();
			};

			$scope.resetPrepHicForm = function () {
				$scope.searchService.resetPrepHicForm();
			};

			$scope.resetLargeRnaIsolationForm = function () {
				$scope.searchService.resetLargeRnaIsolationForm();
			};

			$scope.resetSmallRnaIsolationForm = function () {
				$scope.searchService.resetSmallRnaIsolationForm();
			};
			$scope.resetCreateMappingProjectForm = function () {
				$scope.searchService.resetCreateMappingProjectForm();
			};

		}

		function defineScopeFunctions() {
			$scope.isClickable = function () {
				var entry = $scope.searchService.tabs.createMappingProject.entry;
				return entry.mapParameter && entry.comment;
			}

			$scope.addMappingTab = function () {
				var mapParameter = $scope.searchService.tabs.createMappingProject.entry.mapParameter;
				if (!mapParameter) return;
				var tabId = nextTabIndex()
				tabService.addTabs({ label: mapParameter, href: '/descriptions/mappingprojects/new/' + mapParameter + '/' + tabId, remove: true });
				createNewService()

				//---

				function nextTabIndex() {
					var tabIndex = getTabIndex();
					incrTabIndex();
					saveTabIndex();
					return tabIndex;

					//---

					function getTabIndex() {
						if(!isExistingIndex()) return 0;
						return mainService.get("MapParameterServiceIndex");
					}

					function isExistingIndex() {
						return mainService.get("MapParameterServiceIndex") !== undefined
					}

					function incrTabIndex() {
						tabIndex++;
					}

					function saveTabIndex() {
						mainService.put("MapParameterServiceIndex", tabIndex)
					}
				}

				function createNewService(){
					var serviceKey = mapParameter + "#" + tabId;
					var newService = mappingProjectNewService();
					mainService.put(serviceKey, newService);
					var defaultComment = $scope.searchService.tabs.createMappingProject.entry.comment;
					newService.setEditableComment(defaultComment);
				}
			};
		}

		function initSearchService() {
			$scope.searchService.init($routeParams, $scope.datatableConfig);
			$scope.searchService.search();
		}

	}]);
angular.module('home').controller('NewMappingProjectsCtrl', ['descriptionsMappingProjectsSearchService', '$scope', '$routeParams', 'mainService',
	function (descriptionsMappingProjectsSearchService, $scope, $routeParams, mainService) {

		setDatatableConfig()
		setSearchService();
		setNewService()
		defineScopeFunctions();

		function setSearchService() {
			$scope.searchService = descriptionsMappingProjectsSearchService
		}

		function setDatatableConfig() {
			$scope.datatableConfig = {
				show: {
					active: true
				},
				order: {
					by: 'parentCode',
					reverse: true,
					mode: 'local'
				},
				pagination: {
					mode: 'local',
					numberRecordsPerPage: 50,
				},
				select: {
					active: false,
					showButton: false,
				},
				group: {
					active: false,
					showOnlyGroups: false,
					showButton: false
				},
				show: {
					active: false,
					showButton: false,
				},
				hide: {
					active: true,
					showButton: true,
				},
				cancel: {
					active: false,
					showButton: false
				},
				edit: {
					active: false
				},
				exportCSV: {
					active: true,
					showButton: true,
				}
			};
		}

		function setNewService() {
			var typeCode = $routeParams.typeCode

			//---

			if (isExistingTypeCode()) {
				var serviceKey = typeCode + "#" + $routeParams.tabId;
				$scope.mappingProjectTabTitle = typeCode

				if (isExistingServiceFor(serviceKey)) {
					useExistingService();
					initNewService()
				} else {
					console.log("Il n'existe pas de service pour la clef : " + serviceKey)
				}
			}

			function isExistingTypeCode() {
				return typeCode !== undefined;
			}

			function isExistingServiceFor(Key) {
				return mainService.get(Key) !== undefined;
			}

			function useExistingService() {
				$scope.newService = mainService.get(serviceKey);
				$scope.messages = $scope.newService.messages;
			}
		}

		function defineScopeFunctions() {
			$scope.upload = function () {
				var mapParameterEntry = getMapParameter()
				var config = getConfig()
				$scope.newService.saveMappingEntry(mapParameterEntry, config)

				//---

				function getMapParameter() {
					var entry = $scope.newService.entry
					return {
						'entry': {
							'parent': entry.parent,
							'child': entry.child
						},
						'comment': entry.comment
					}
				}

				function getConfig() {
					return {
						params: {
							typeCode: 'map-parameter',
							code: $routeParams.typeCode
						}
					}
				}
			}

			$scope.isClickable = function () {
				var entry = $scope.newService.entry
				return entry.parent && entry.child && entry.comment;
			}

			$scope.resetCreateMappingProjectForm = function () {
				$scope.newService.entry = {}
				$scope.newService.messages.clear()
			};
		}

		function initNewService() {
			$scope.newService.init($routeParams, $scope.datatableConfig);
		}

	}
]);
