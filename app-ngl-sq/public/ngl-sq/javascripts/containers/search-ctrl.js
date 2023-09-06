"use strict"

angular.module('home').controller('SearchCtrl', ['$scope',  '$window','datatable','lists','$filter','mainService','tabService','containersSearchService','$routeParams', '$http', 
                                                 function($scope, $window, datatable, lists,$filter,mainService,tabService,containersSearchService,$routeParams,$http) {
	var datatableConfig = {
		group:{active:true},
		search:{
			url:jsRoutes.controllers.containers.api.Containers.list()
		},
		pagination:{
			mode:'local'
		},
		group:{
			active:true,
			showOnlyGroups:true,
			enableLineSelection:true,
			showButton:true
		},
		hide:{
			active:true
		},
		order:{
			//by:"['support.code','support.column']",
			by:'traceInformation.creationDate',
			reverse:true,
			mode:'local'
		},
		exportCSV:{
			active:true
		},
		show:{
			active:true,
			add:function(line){
				tabService.addTabs({label:line.code,href:jsRoutes.controllers.containers.tpl.Containers.get(line.code).url, remove:true});
			}
		},
		edit:{
			active:Permissions.check("writing")?true:false,
			columnMode:true
		},
		callbackEndDisplayResult: function() {
			$scope.searchService.colorChoosed = [];

			$scope.searchService.datatable.allResult.forEach(function (r) {
				$scope.searchService.colorChoosed[r.code] = $scope.searchService.getColor(r.qualityControlResults);
			});
		},
		save:{
			active:Permissions.check("writing")?true:false,
			url:function(value){
				var fields = "fields=valuation";
				if(value.state && value.state.resolutionCodes)fields = fields+"&fields=state.resolutionCodes";
				if(value.comments)fields = fields+"&fields=comments";
				if(value.concentration)fields = fields+"&fields=concentration&fields=quantity";
				if(value.volume)fields = fields+"&fields=volume";
				if(value.quantity)fields = fields+"&fields=quantity";
				if(value.size)fields = fields+"&fields=size";
				if (value.qualityControlResults) {
					fields = fields+"&fields=qualityControlResults";
						
					// Mise à jour de tous les éléments du datatable.

					if ($scope.searchService.colorChoosed[value.code] != null) {
						var qcAll = $scope.searchService.datatable.allResult.filter(function (cont) {
							return cont.code == value.code;
						})[0].qualityControlResults;
						
						if (qcAll) {
							var qcEvalAll = qcAll.filter(function (a) {
								return a.typeCode == "chip-migration-rna-evaluation";
							}).reduce(function (a, b) {
								return a.index > b.index ? a : b;
							}, "");

							if (qcEvalAll) {
								qcEvalAll["properties"]["rnaEvaluation"] = {
									value: $scope.searchService.colorChoosed[value.code],
									_type: "single",
									unit: null
								};
							}
						}
							
						// Mise à jour de tous les éléments affichés du datatable.

						var qcDisp = $scope.searchService.datatable.displayResult.filter(function (cont) {
							return cont.data.code == value.code;
						})[0].data.qualityControlResults;

						if (qcDisp) {
							var qcEvalDisp = qcDisp.filter(function (a) {
								return a.typeCode == "chip-migration-rna-evaluation";
							}).reduce(function (a, b) {
								return a.index > b.index ? a : b;
							}, "");

							if (qcEvalDisp) {
								qcEvalDisp["properties"]["rnaEvaluation"] = {
									value: $scope.searchService.colorChoosed[value.code],
									_type: "single",
									unit: null
								};	
							}
						}
					} else { // Si jamais on met une couleur sur la ligne "globale" qui édite toute la page, on passe là.
						if ($scope.searchService.colorChoosed[undefined] != null) {
							var tabDisplay = $scope.searchService.datatable.displayResult.filter(function (cont) {
								return cont.line.edit;
							});

							for (var i = 0; i < tabDisplay.length; i++) {
								if (tabDisplay[i].data.qualityControlResults) {
									var qcEval = tabDisplay[i].data.qualityControlResults.filter(function (a) {
										return a.typeCode == "chip-migration-rna-evaluation";
									}).reduce(function (a, b) {
										return a.index > b.index ? a : b;
									}, "");
									
									if (qcEval != "") {
										qcEval["properties"]["rnaEvaluation"] = {
											value: $scope.searchService.colorChoosed[undefined],
											_type: "single",
											unit: null
										}; 
									}
								}

								var qc = $scope.searchService.datatable.allResult.filter(function (cont) {
									return cont.code == tabDisplay[i].data.code;
								})[0].qualityControlResults;

								if (qc) {
									var qcEvalAll = qc.filter(function (a) {
										return a.typeCode == "chip-migration-rna-evaluation";
									}).reduce(function (a, b) {
										return a.index > b.index ? a : b;
									}, "");

									if (qcEvalAll != "") {
										qcEvalAll["properties"]["rnaEvaluation"] = {
											value: $scope.searchService.colorChoosed[undefined],
											_type: "single",
											unit: null
										};
									}
								}
							}
						}
					}

					// Mise à jour des qualityControlResults dans les experiments.

					var latestExp = value.qualityControlResults.filter(function (a) {
						return a.typeCode == "chip-migration-rna-evaluation";
					}).reduce(function (a, b) {
						return a.index > b.index ? a : b;
					}, "");

					// Si latestExp n'est pas de type object c'est qu'il n'y a pas d'expérience trouvée (donc pas à mettre à jour).
					if (typeof latestExp == 'object') {
						$http.get(jsRoutes.controllers.experiments.api.Experiments.get(latestExp.code).url).success(function(data) {
							if (data.atomicTransfertMethods) {
								data.atomicTransfertMethods.forEach(function (atm) {
									if (atm.inputContainerUseds) {	
										atm.inputContainerUseds.forEach(function (icu) {
											if ($scope.searchService.colorChoosed[icu.code]) {
												if (icu.experimentProperties.rnaEvaluation) { // Si on a déjà une évaluation.
													icu.experimentProperties.rnaEvaluation.value = $scope.searchService.colorChoosed[icu.code];
												} else { // Si on en a pas on la créé.
													icu.experimentProperties.rnaEvaluation = {
														unit: null,
														value: $scope.searchService.colorChoosed[icu.code],
														_type: "single"
													};
												}
											}
										});
									}
								});

								// Mise à jour de l'expérience.
								$http.put(jsRoutes.controllers.experiments.api.Experiments.update(latestExp.code).url + "?fields=atomicTransfertMethods" , data);
							}
						});
					}
				}
				
				return jsRoutes.controllers.containers.api.Containers.update(value.code).url+"?"+fields;
			},
			method:'put',
			mode:'remote'			
		},
		otherButtons :{
			active:PrintTag.isActive(),
			template:'<button class="btn btn-default" ng-click="openPrintTagsPage(searchService.datatable.getSelection(true))"  ng-disabled="!searchService.datatable.isSelect()" title="'+Messages("button.tag.printing")+'"><i class="fa fa-tags" ></i></button>'					
		}
	};

	
	
	$scope.search = function(){		
		$scope.searchService.search();
	};
	
	$scope.reset = function(){
		$scope.searchService.resetForm();	
		$scope.searchService.resetTextareas();	
	};
	
	$scope.openPrintTagsPage = function(supports){
		var params = {value : ""};
		supports.forEach(function(value){
			this.value = this.value + "containerSupportCodes="+value.support.code+"&";
		},params)
		params.value = params.value.slice(0, params.value.length-1);
		$window.open(jsRoutes.controllers.printing.tpl.Printing.home("tags").url+"?"+params.value, 'tags');
	};
	//init
	if(angular.isUndefined($scope.getHomePage())){
		mainService.setHomePage('search');
		tabService.addTabs({label:Messages('containers.tabs.search'),href:jsRoutes.controllers.containers.tpl.Containers.home("search").url,remove:true});
		tabService.activeTab(0);
	}
	if(angular.isUndefined($scope.getForm())){
		$scope.form = {};
		mainService.setForm($scope.form);
	}
	
	$scope.searchService = containersSearchService;
	$scope.searchService.init($routeParams, datatableConfig)
	
	if($scope.searchService.isRouteParam){
		$scope.search();
	}	
}]);


"use strict"
angular.module('home').controller('SearchStateCtrl', ['$scope','$location','$routeParams', 'datatable','lists','$filter','$http','$q','mainService','tabService','containersSearchService', 
	function($scope,$location,$routeParams, datatable, lists,$filter,$http,$q,mainService,tabService,containersSearchService) {
	var datatableConfig = {
			search:{
				url:jsRoutes.controllers.containers.api.Containers.list()
				
			},
			order:{
				by:'traceInformation.creationDate',
				reverse : true,
				mode:'local'
			},
			edit:{
				active:Permissions.check("writing")?true:false,
				columnMode:true,
				lineMode:function(value){
					return (value.state.code === 'IS' 
							|| value.state.code === 'UA'
							||	value.state.code === 'IW-P'
							||	value.state.code.startsWith('A'));
				}
				
			},
			pagination:{
				mode:'local'
			},
			save:{
				active:Permissions.check("writing")?true:false,
				url:jsRoutes.controllers.containers.api.Containers.updateStateBatch().url,
				mode:'remote',
				method:'put',
				batch:true,
				value:function(line){return {code:line.code,state:line.state};},	
				beforeSave:function(values){
					
					var queries = values.map(function(value){
						var fields = "fields=valuation";
						if(value.data.state && value.data.state.resolutionCodes)fields = fields+"&fields=state.resolutionCodes";
						if(value.data.comments)fields = fields+"&fields=comments";
						return $http.put(jsRoutes.controllers.containers.api.Containers.update(value.data.code).url+"?"+fields,value.data);																	
					});
					
					return $q.all(queries);
					
				}
			},
			show:{
				active:true,
				add:function(line){
					tabService.addTabs({label:line.code,href:jsRoutes.controllers.containers.tpl.Containers.get(line.code).url, remove:true});
				}
			},
			hide:{
				active:true
			},
			exportCSV:{
				active:true,
			}
	};

	$scope.reset = function(){
		$scope.searchService.resetForm();
		$scope.searchService.resetTextareas();
	};
	
	$scope.search = function(){	
		$scope.searchService.search();
	};
	
	//init
	if(angular.isUndefined($scope.getHomePage())){
		mainService.setHomePage('state');
		tabService.addTabs({label:Messages('containers.tabs.state'),href:jsRoutes.controllers.containers.tpl.Containers.home("state").url,remove:true});
		tabService.activeTab(0);
	}
	
	if(angular.isUndefined($scope.getForm())){
		$scope.form = {};
		mainService.setForm($scope.form);
	}else{
		$scope.form = mainService.getForm();			
	}
	
	$scope.searchService = containersSearchService;
	$scope.searchService.init($routeParams, datatableConfig)
	
	if($scope.form.project || $scope.form.type){
		$scope.search();
	}
	
	
}]);