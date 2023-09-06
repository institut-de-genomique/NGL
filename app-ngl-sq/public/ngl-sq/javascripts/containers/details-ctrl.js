"use strict";

angular.module('home').controller('DetailsCtrl', ['$scope', '$http', '$q', '$routeParams', '$filter','$window', '$sce','mainService', 'tabService', 'lists', 'messages', 
                                          function($scope,$http,$q,$routeParams,$filter,$window,$sce,mainService,tabService,lists,messages){
	
	$scope.angular = angular;
	
	$scope.getTabClass = function(value){
		if(value === mainService.get('containerActiveTab')){
			return 'active';
		}
	};
	$scope.setActiveTab = function(value){
		mainService.put('containerActiveTab', value)
	};
	
	$scope.goToSupport = function(){
		$window.open(jsRoutes.controllers.containers.tpl.ContainerSupports.get($scope.container.support.code).url, 'supports');			
	};
	/* move to a directive */
	$scope.setImage = function(imageData, imageName, imageFullSizeWidth, imageFullSizeHeight) {
		$scope.modalImage = imageData;

		$scope.modalTitle = imageName;

		var margin = 25;		
		var zoom = Math.min((document.body.clientWidth - margin) / imageFullSizeWidth, 1);

		$scope.modalWidth = imageFullSizeWidth * zoom;
		$scope.modalHeight = imageFullSizeHeight * zoom; // in order to
															// conserve image
															// ratio
		$scope.modalLeft = (document.body.clientWidth - $scope.modalWidth)/2;

		$scope.modalTop = (window.innerHeight - $scope.modalHeight)/2;

		$scope.modalTop = $scope.modalTop - 50; // height of header and footer
	};
	
	$scope.isSaveInProgress = function(){
		return saveInProgress;
	};
	
	var units = {
		//NGL-3555 Cacher fonctionnalite MGI //NGL-3556 Reactivation MGI
		 "volume":[{"code":"µL","name":"µL"}],	
		 "concentration":[{"code":"ng/µl","name":"ng/µl"},{"code":"nM","name":"nM"},{"code":"fmol/µl","name":"fmol/µl"}],	
		 "quantity":[{"code":"ng","name":"ng"},{"code":"nmol","name":"nmol"}],
		 "size":[{"code":"pb","name":"pb"},{"code":"nt","name":"nt"}]			
	};
	
	
	$scope.getUnits = function(unitType){
		return units[unitType];
	}
	
	$scope.computeQuantity = function(){
		var concentration = $scope.container.concentration;
		var volume = $scope.container.volume;
		
		if(concentration && concentration.value && volume && volume.value){
			var result = volume.value * concentration.value;
			if(angular.isNumber(result) && !isNaN(result)){
				var quantity = {};
				quantity.value = Math.round(result*10)/10;
				quantity.unit = (concentration.unit === 'nM')?'fmol':'ng';
				$scope.container.quantity = quantity;
			}else {
				$scope.container.quantity =  undefined;
			}
		}else {
			$scope.container.quantity =  undefined;
		}
	}
	
	$scope.convertToBr = function(text){
		if(text)return $sce.trustAsHtml(text.replace(/\n/g, "<br>"));
	};
	/* buttons section */
	$scope.save = function(){
		saveInProgress = true;	
		$scope.messages.clear();
		$http.put(jsRoutes.controllers.containers.api.Containers.update($scope.container.code).url, $scope.container)
		.success(function(data, status, headers, config) {
			$scope.container = data;
			$scope.messages.setSuccess("save");						
			mainService.stopEditMode();
			
			saveInProgress = false;									
		})
		.error(function(data, status, headers, config) {
			$scope.messages.setError("save");
			$scope.messages.setDetails(data);				
			saveInProgress = false;				
		});			
	};
	
	$scope.cancel = function(){
		$scope.messages.clear();
		mainService.stopEditMode();
		updateData();				
	};
	
	$scope.activeEditMode = function(){
		$scope.messages.clear();
		mainService.startEditMode();		
	}
	
	$scope.getPropertyDefinition = function(key){
		$http.get(jsRoutes.controllers.common.api.PropertyDefinitions.get(key).url).then(function(response) {
			var propertyDefinitions = {};			
		});
	}

	var updateData = function(){
		$http.get(jsRoutes.controllers.containers.api.Containers.get($routeParams.code).url).then(function(response) {
			$scope.container = response.data;				
		});
	};
	var saveInProgress = false;
	var contentPropertyDefinitionMap = {};	
	var containerPropertyDefinitionMap = {};	
	var init = function(){
		$scope.messages = messages();
		$scope.lists = lists;
		$scope.mainService = mainService;
		mainService.stopEditMode();
			
		$http.get(jsRoutes.controllers.containers.api.Containers.get($routeParams.code).url).then(function(response) {
			$scope.container = response.data;			
			if(tabService.getTabs().length == 0){			
				tabService.addTabs({label:Messages('containers.tabs.search'),href:jsRoutes.controllers.containers.tpl.Containers.home("search").url,remove:true});
				tabService.addTabs({label:$scope.container.code,href:jsRoutes.controllers.containers.tpl.Containers.get($scope.container.code).url,remove:true});
				tabService.activeTab($scope.getTabs(1));
			}
			
			$scope.lists.refresh.containerSupportCategories();
			$scope.lists.refresh.experimentTypes({categoryCodes:["transformation"], withoutOneToVoid:false});
			$scope.lists.refresh.resolutions({"objectTypeCode":"Container"}, "containerResolutions");
					
			if(undefined === mainService.get('containerActiveTab')){
				mainService.put('containerActiveTab', 'general');
			}else if('treeoflife' ===  mainService.get('containerActiveTab')){
				$scope.initGraph();
			}
			
		});
		
		
		$http.get(jsRoutes.controllers.commons.api.PropertyDefinitions.list().url,{params:{'levelCode':'Content'}}).then(function(response) {
			
			response.data.forEach(function(pdef){
					this[pdef.code]=pdef;
			}, contentPropertyDefinitionMap);
			
		});
		
		$http.get(jsRoutes.controllers.commons.api.PropertyDefinitions.list().url,{params:{'levelCode':'Container'}}).then(function(response) {
			
			response.data.forEach(function(pdef){
					this[pdef.code]=pdef;
			}, containerPropertyDefinitionMap);
			
		});
	}
	init();
	
	$scope.getContentPropertyDefinitionValueType = function(key){
		var propertyDef = contentPropertyDefinitionMap[key];
		if(propertyDef){
			return propertyDef.valueType;
		}
		return null;
	};
	
	$scope.getContainerPropertyDefinitionValueType = function(key){
		var propertyDef = containerPropertyDefinitionMap[key];
		if(propertyDef){
			return propertyDef.valueType;
		}
		return null;
	}
	
	
	var containerNodes = undefined;
	$scope.initGraph = function(){
		$scope.setActiveTab('treeoflife');
		$scope.messages.clear();
		if(!containerNodes){
			initTreeOfLife($scope.container);
		}
	}
	$scope.treeLoadInProgress = false;
	
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
					            spacingFactor:0.8,
					          },
					          style: cytoscape.stylesheet()
						          .selector('node')
						            .css({
						              'shape': 'data(faveShape)',
						              'width': '90',
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
						            	'curve-style': 'bezier',
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
				 	cy.on('click', 'node', function(evt){
				 		var data = this.data(); 
				 		$scope.$apply(function(scope){
				 			 tabService.addTabs({label:data.code,href:jsRoutes.controllers.containers.tpl.Containers.get(data.code).url, remove:true});						 		
				 		 });
				 	});
				 	cy.on('click', 'edge', function(evt){
				 		var data = this.data(); 
				 		$scope.$apply(function(scope){
				 			$window.open(jsRoutes.controllers.experiments.tpl.Experiments.get(data.fromExperimentCode).url, 'experiments');
				 		});
				 	});
				 	cy.ready(function(evt){
				 		console.log('ready');	
				 		$scope.treeLoadInProgress = false;	
				 		$scope.$digest();
				 	});
				});	
			 }, 1);
		};
		asynchGraph();
	}
	
	var computeGraphElements = function(containerNodes){
		//nodes
		var graphElements = [];
		containerNodes = $filter('orderBy')(containerNodes,'indexFromCurrent');
		for(var key in containerNodes){
			var currentNode = containerNodes[key];
			var currentContainer = containerNodes[key].container;
			currentContainer.id = currentContainer.code;
			currentContainer.label = currentContainer.code;
			var faveColor = '#6FB1FC';
			if(currentNode.indexFromCurrent < 0){
				faveColor = '#F5A45D'
			}else if(currentNode.indexFromCurrent > 0){
				//faveColor = '#86B342';
				faveColor = '#F5A45D'
			}
			
			currentContainer.faveColor = faveColor;
			currentContainer.faveShape="ellipse";
			if(currentContainer.contents.length > 1){
				currentContainer.faveShape="octagon";
			}
			
			graphElements.push({"data":currentContainer,"group":"nodes"});	
		}
		
		//edges
		for(var key in containerNodes){
			
			var currentNode = containerNodes[key];
			var currentContainer = containerNodes[key].container;
			angular.forEach(currentNode.childNodes, function(childNode){
				var childContainer = childNode.container;
				var currentContainer = this.container;
				var edge = {
						"id":currentContainer.code+"-"+childContainer.code,
						"source":currentContainer.code,
						"target":childContainer.code
				}
				
				if(childContainer.treeOfLife && childContainer.treeOfLife.from){
					edge.label=$filter('codes')(childContainer.treeOfLife.from.experimentTypeCode,'type');
					edge.fromExperimentCode = childContainer.treeOfLife.from.experimentCode;
				}
				
				var faveColor = '#6FB1FC';
				if(this.indexFromCurrent < 0){
					faveColor = '#F5A45D'
				}else if(this.indexFromCurrent > 0){
					//faveColor = '#86B342';
					faveColor = '#F5A45D'
				}
				edge.faveColor=faveColor;
				graphElements.push({"data":edge,"group":"edges"})	
			},currentNode)
		}
		
		return graphElements;
	};
	
	var initTreeOfLife = function(currentContainer){
		//extract parent container codes
		$scope.treeLoadInProgress = true;
		var codes = {parentContainerCodes : []};	
		var treeOfLifePathRegex = '^,'+currentContainer.code;
		
		if(!angular.isUndefined(currentContainer.treeOfLife) && (currentContainer.treeOfLife !== null)){
			if(currentContainer.treeOfLife.paths && currentContainer.treeOfLife.paths.length > 0){
				treeOfLifePathRegex = '^'+currentContainer.treeOfLife.paths[0]+','+currentContainer.code;
			}
			angular.forEach(currentContainer.treeOfLife.paths, function(path){
				path = path.substring(1);
				this.parentContainerCodes = this.parentContainerCodes.concat(path.split(","));
			}, codes);
		}
		
		var promises = [];
		//promises.push($http.get(jsRoutes.controllers.containers.api.Containers.list().url, {params : {treeOfLifePathRegex:','+currentContainer.code+'$|,'+currentContainer.code+','}}));
		promises.push($http.get(jsRoutes.controllers.containers.api.Containers.list().url, {params : {treeOfLifePathRegex:treeOfLifePathRegex+'$'}}));
		promises.push($http.get(jsRoutes.controllers.containers.api.Containers.list().url, {params : {treeOfLifePathRegex:treeOfLifePathRegex+','}}));
		
		if(codes.parentContainerCodes.length > 0){ // Case no paths
			var nbElementByBatch = Math.ceil(codes.parentContainerCodes.length / 6); //6 because 6 request max in parrallel with firefox and chrome
            var queries = [];
            if(nbElementByBatch < 500){
	            for (var i = 0; i < 6 && codes.parentContainerCodes.length > 0; i++) {
	                var subContainerCodes = codes.parentContainerCodes.splice(0, nbElementByBatch);
	                promises.push($http.get(jsRoutes.controllers.containers.api.Containers.list().url, {params : {codes:subContainerCodes}}));                
	            }			
            }else{
            	for (var i = 0; codes.parentContainerCodes.length > 0; i++) {
	                var subContainerCodes = codes.parentContainerCodes.splice(0, 500);
	                promises.push($http.get(jsRoutes.controllers.containers.api.Containers.list().url, {params : {codes:subContainerCodes}}));                
	            }
            }
		}
		
		$q.all(promises).then(function(results){
			containerNodes = {};
			var newNode = function(container){
				return  {container:container, parentNodes:[], childNodes:[],indexFromCurrent:undefined};
			};
			
			containerNodes[$scope.container.code] = newNode($scope.container);
			
			angular.forEach(results, function(result){
					angular.forEach(result.data, function(container){
							this[container.code] = newNode(container);
						}, this);
			}, containerNodes);
			
			var updateParentNodes = function(currentContainerNode, containerNodes){
				//only if parents
				if(currentContainerNode.parentNodes.length === 0){
					
					if(currentContainerNode.container.treeOfLife){
						var parentContainers = currentContainerNode.container.treeOfLife.from.containers;
						
						angular.forEach(parentContainers, function(parentContainer){
						
							if(containerNodes[parentContainer.code]){
								var parentContainerNode = containerNodes[parentContainer.code];
								this.parentNodes.push(parentContainerNode);
								updateParentNodes(parentContainerNode, containerNodes);
							}else{
								//when display a branch of a pool
								//throw 'error not found node for '+parentContainer.code;
							}
						}, currentContainerNode)
					}
				}
			};
			
			for(var key in containerNodes){
				updateParentNodes(containerNodes[key], containerNodes);	
			}
			
			//update child
			for(var key in containerNodes){
				var currentNode = containerNodes[key];
				angular.forEach(currentNode.parentNodes, function(parentNode){
					parentNode.childNodes.push(this);
				},currentNode)
			}
			
			var updateIndexForParents = function(parentNodes, childIndex){
				angular.forEach(parentNodes, function(parentNode){
					parentNode.indexFromCurrent = this - 1;
					updateIndexForParents(parentNode.parentNodes, parentNode.indexFromCurrent);
				}, childIndex);
			};
			
			var updateIndexForChildren = function(childNodes, parentIndex){
				angular.forEach(childNodes, function(childNode){
					childNode.indexFromCurrent = this + 1 ;
					updateIndexForChildren(childNode.childNodes, childNode.indexFromCurrent);
				}, parentIndex);
			};
			
			//update index from current container
			var currentContainerNode = containerNodes[$scope.container.code];
			currentContainerNode.indexFromCurrent = 0;
			updateIndexForParents(currentContainerNode.parentNodes, currentContainerNode.indexFromCurrent);
			updateIndexForChildren(currentContainerNode.childNodes, currentContainerNode.indexFromCurrent);
			
			var graphElements =  computeGraphElements(containerNodes);
			initCytoscape(graphElements);
			
		}, function(results){
			$scope.treeLoadInProgress = false;	
			$scope.messages.setError("get");
		});
		
	}
	
}]);