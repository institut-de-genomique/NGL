"use strict"
angular.module('home').controller('SearchCtrl', ['$scope','$location','$routeParams','$filter', 'datatable','lists','mainService','tabService','experimentsSearchService', 
	                                     function($scope,  $location,  $routeParams,  $filter,   datatable,  lists,  mainService,  tabService,  experimentsSearchService) {
	$scope.datatableConfig = {	
			show:{
				active:true,
				add :function(line){
					tabService.addTabs({label:line.code,href:jsRoutes.controllers.experiments.tpl.Experiments.get(line.code).url,remove:true});
				}
			},
			search:{
				url:jsRoutes.controllers.experiments.api.Experiments.list()
				
			},
			pagination:{
				mode:'local'
			},
			order:{
				by:'traceInformation.creationDate',
				reverse :true,
				mode:'local'
			},
			group:{
				active:true,
				showOnlyGroups:true,
				showButton:true
			},
			hide:{
		 		 active:true
		 	},
			edit:{
				active:false
			},
			exportCSV:{
				active:true
			}
	};
	
	$scope.search = function(){		
		$scope.searchService.search();
	};
	
	$scope.reset = function(){
		$scope.searchService.resetForm();		
		$scope.searchService.resetTextareas();
	};
	
	
	$scope.changeContainerSupportCode = function(val){
		console.log(val);
		return $scope.searchService.changeContainerSupportCode(val);		 
	};

	$scope.changeExperimentType = function(){
		$scope.searchService.changeExperimentType();
	};
	
	$scope.changeProcessType = function(){
		$scope.searchService.changeProcessType();
	};
	
	$scope.changeProcessCategory = function(){
		$scope.searchService.changeProcessCategory();
	};
	
	//init
	if(angular.isUndefined($scope.getHomePage())){
		mainService.setHomePage('search');
		tabService.addTabs({label:Messages('experiments.tabs.search'),href:jsRoutes.controllers.experiments.tpl.Experiments.home("search").url,remove:true});
		tabService.activeTab(0);
	}
	if(angular.isUndefined($scope.getForm())){
		$scope.form = {};
		mainService.setForm($scope.form);
	}else{
		$scope.form = mainService.getForm();		
	}
	
	$scope.searchService = experimentsSearchService;
	$scope.searchService.init($routeParams, $scope.datatableConfig);	
	
	
	if($scope.searchService.isRouteParam){
		$scope.search();
	}	
}]);



"use strict"
angular.module('home').controller('SearchReagentsCtrl', ['$scope', '$http', '$q', '$routeParams', 'datatable', 'experimentsSearchService', 'mainService', 'tabService', 
                                                 function($scope,   $http,   $q,   $routeParams,   datatable,   experimentsSearchService,   mainService,   tabService){
	$scope.datatableConfig = {
			name:"experimentReagents",
			search:{
				active:true,
				url:jsRoutes.controllers.experiments.api.ExperimentReagents.list()
			},
			order:{
				active:true,
				mode:'local'
			},
	        pagination:{
	        	mode:'local'
	        },	
			show:{
				active:true,
				add :function(line){
					tabService.addTabs({label:line.code,href:jsRoutes.controllers.experiments.tpl.Experiments.get(line.code).url,remove:true});
				}
			},
			edit:{
				active:false
			},
			hide:{
				active:false
			},
			exportCSV:{
				active:true,
				showButton:true,
				delimiter:";"
			},
			compact:true
	};

	
	$scope.search = function(){
		$scope.searchService.search();
	};
	
	$scope.reset = function(){
		$scope.searchService.resetForm();
		$scope.searchService.resetTextareas();
	}
	
	
	//init	
	if(angular.isUndefined($scope.getHomePage())){
		mainService.setHomePage('reagents');
		tabService.addTabs({label:Messages('experiments.tabs.reagents'),href:jsRoutes.controllers.experiments.tpl.Experiments.home("reagents").url,remove:true});
		tabService.activeTab(0);
	}
	
	if(angular.isUndefined($scope.getForm())){
		$scope.form = {};
		mainService.setForm($scope.form);
	}else{
		$scope.form = mainService.getForm();		
	}
	
	$scope.searchService = experimentsSearchService;
	$scope.searchService.init($routeParams, $scope.datatableConfig);

}]);

angular.module('home').controller('SearchGraphCtrl', ['$scope', '$http', '$q', 'mainService', 'tabService', 'lists',
	                                          function($scope,   $http,   $q,   mainService,   tabService,   lists){

	
	
	$scope.changeProcessCategory = function(){
		$scope.form.processTypeCode = undefined;
		$scope.lists.clear("processTypes");

		if($scope.form.processCategory !== undefined && $scope.form.processCategory !== null){
			$scope.lists.refresh.processTypes({categoryCode:$scope.form.processCategory,isActive:true});
		}
	};
	
	$scope.changeProcessTypes = function(){
		$http.get(jsRoutes.controllers.processes.api.ProcessTypes.get($scope.form.processTypeCode).url).then(function(result){
			var processExperimentTypes = new Map();
				
			result.data.experimentTypes.forEach(function(experimentType){
				if(this.get(experimentType.experimentTypeCode) === undefined){
					this.set(experimentType.experimentTypeCode, [experimentType.positionInProcess]);
				}else{
					this.get(experimentType.experimentTypeCode).push(experimentType.positionInProcess);
				}
				
			},processExperimentTypes);
			var graphElements  = computeGraphElements($scope.graphNodes, processExperimentTypes);
			initCytoscape(graphElements);
			
		})
	
	}
	var init = function(){
		$scope.form = {};
		$scope.lists = lists;
		$scope.lists.refresh.processCategories();
		
		initGraph();
		
	}
	
	// init
	if (angular.isUndefined($scope.getHomePage())) {
		mainService.setHomePage('reagents');
		tabService.addTabs({label : Messages('experiments.tabs.graph'),href : jsRoutes.controllers.experiments.tpl.Experiments.home("reagents").url,remove : true
		});
		tabService.activeTab(0);
	}
	var initGraph = function(){
		$http.get(jsRoutes.controllers.experiments.api.ExperimentTypeNodes.list().url).then(function(expNodes){
			$scope.graphNodes = computeGraphNodes(expNodes.data);
			var graphElements  = computeGraphElements($scope.graphNodes);
			initCytoscape(graphElements);
		});
	}
	
	var computeGraphNodes = function(expNodes){
		var newNode = function(experimentNode){
			return  {experimentType:experimentNode.experimentType, parentNodes:(experimentNode.previousExperimentTypes)?experimentNode.previousExperimentTypes:[], childNodes:[]};
		};
		
		var graphNodes = {};
		
		expNodes.forEach(function(experimentNode){
			graphNodes[experimentNode.experimentType.code] = newNode(experimentNode);
		})
		expNodes.forEach(function(experimentNode){
			graphNodes[experimentNode.experimentType.code].parentNodes.forEach(function(parent){
				graphNodes[parent.code].childNodes.push(this);
			}, experimentNode.experimentType) 
		})
		return graphNodes;
	};
	
	var computeGraphElements = function(graphNodes, processExperimentTypes){
		//nodes
		var graphElements = [];
		
		var getFaveColor = function(processExperimentTypes, key){
			if(processExperimentTypes === undefined)return '#6FB1FC';
			else if(processExperimentTypes.get(key)[0] > -1 )return '#6FB1FC';
			else return '#F5A45D';
		}
		
		for(var key in graphNodes){
			if(processExperimentTypes === undefined || processExperimentTypes.get(key) !== undefined){
				var currentNode = graphNodes[key];
				var currentExperimentType = graphNodes[key].experimentType;
				if(currentExperimentType.category.code === 'transformation'){
					currentExperimentType.id = currentExperimentType.code;
					currentExperimentType.label = currentExperimentType.name;
					currentExperimentType.faveColor = getFaveColor(processExperimentTypes, key);
					currentExperimentType.faveShape="ellipse";
					
					graphElements.push({"data":currentExperimentType,"group":"nodes"});
				}
			}
		}
		
		//edges
		var isDiffPositionIsOne = function(processExperimentTypes, keyParent, keyChild){
			return processExperimentTypes.get(keyParent).some(function(parentPos){
				return processExperimentTypes.get(keyChild).some(function(childPos){
						return (childPos - parentPos === 1);
				});
			});
			//return (processExperimentTypes.get(keyChild) - processExperimentTypes.get(keyParent) === 1);			
		}
		
		for(var key in graphNodes){
			if(processExperimentTypes === undefined || processExperimentTypes.get(key) !== undefined){
				var currentNode = graphNodes[key];
				var currentExperimentType = graphNodes[key].experimentType;
				if(currentExperimentType.category.code === 'transformation'){
					angular.forEach(currentNode.childNodes, function(childNode){
						var childExperimentType = childNode;
						if(childExperimentType.category.code === 'transformation' 
							&& (processExperimentTypes === undefined 
									|| (processExperimentTypes.get(childExperimentType.code) !== undefined && isDiffPositionIsOne(processExperimentTypes, key, childExperimentType.code)))){
							var currentExperimentType = this;
							var edge = {
									"id":currentExperimentType.code+"-"+childExperimentType.code,
									"source":currentExperimentType.code,
									"target":childExperimentType.code
									
							}
							var faveColor = getFaveColor(processExperimentTypes, key);
							edge.faveColor=faveColor;
							graphElements.push({"data":edge,"group":"edges"})	
						}
					},currentExperimentType)
				}
			}
		}
		
		return graphElements;
	};
	

	var initCytoscape = function(graphElements){
		var asynchGraph = function() {
			 return $q(function(resolve, reject) {
				 setTimeout(function() {
				 	 var cy = 
						cytoscape({
					          container: document.getElementById('graph'),
					          boxSelectionEnabled: false,
					          autounselectify: true,
					          wheelSensitivity: 0.1, //NGL-4082 ajout
					          layout: {
					            name: 'breadthfirst',
					            directed:true,
					            padding:5,
					            spacingFactor:0.5,
					          },
					          style: cytoscape.stylesheet()
						          .selector('node')
						            .css({
						              'shape': 'data(faveShape)',
						              'width': '150',
						              'label': 'data(label)',
						              'text-valign': 'center',
						              //'text-outline-width': 2,
						              //'text-outline-color': 'data(faveColor)',
						              'background-color': 'data(faveColor)',
						              'color': '#fff',
						              'font-size':11,  
						            })
						          .selector(':selected')
						            .css({
						              'border-width': 3,
						              'border-color': '#333'
						            })
						          .selector('edge')
						            .css({
						              'opacity': 0.666,
						              'width': '3',
						              'label': 'data(label)',
						              'color': '#000',
						              'font-size':11,
						              'font-weight': 'bold',
						              'target-arrow-shape': 'triangle',
						              'source-arrow-shape': 'circle',
						              'line-color': 'data(faveColor)',
						              'source-arrow-color': 'data(faveColor)',
						              'target-arrow-color': 'data(faveColor)'
						            })
						            /*
						          .selector('edge.questionable')
						           .css({
						              'line-style': 'dotted',
						              'target-arrow-shape': 'diamond'
						            })
						            */
						          .selector('.faded')
						           .css({
						              'opacity': 0.25,
						              'text-opacity': 0
						            })
						      ,
					          elements : graphElements
					        });
				});	
			 }, 1);
		};
		asynchGraph();
	}
	
	init();
	
}]);
