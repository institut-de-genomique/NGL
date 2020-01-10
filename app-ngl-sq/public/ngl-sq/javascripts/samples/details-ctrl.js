"use strict";

angular.module('home').controller('DetailsCtrl', ['$scope', '$http', '$q', '$routeParams', '$filter','$window', '$sce','mainService', 'tabService', 'lists', 'messages', 
                                                  function($scope,$http,$q,$routeParams,$filter,$window,$sce,mainService,tabService,lists,messages){

	$scope.angular = angular;

	$scope.getTabClass = function(value){
		if(value === mainService.get('sampleActiveTab')){
			return 'active';
		}
	};
	$scope.setActiveTab = function(value){
		mainService.put('sampleActiveTab', value)
	};


	// move to a directive 
/*	$scope.setImage = function(imageData, imageName, imageFullSizeWidth, imageFullSizeHeight) {
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
	};*/
	 //buttons section 
	
	$scope.convertToBr = function(text){
		if(text)return $sce.trustAsHtml(text.replace(/\n/g, "<br>"));
	};
	
	 $scope.save = function(){
		saveInProgress = true;	
		console.log("sample "+$scope.sample.code);
		$http.put(jsRoutes.controllers.samples.api.Samples.update($scope.sample.code).url, $scope.sample)
	//	$http.put("/api/samples/"+$scope.sample.code, $scope.sample)
		.success(function(data, status, headers, config) {
		
			$scope.sample = data;
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
	$scope.isSaveInProgress = function(){
		return saveInProgress;
	};  

	var saveInProgress = false;
	var samplePropertyDefinitionMap = {};	
	var init = function(){
		$scope.messages = messages();
		$scope.lists = lists;
		$scope.mainService = mainService;
		mainService.stopEditMode();

		$http.get(jsRoutes.controllers.samples.api.Samples.get($routeParams.code).url).then(function(response) {
			$scope.sample = response.data;			
			if(tabService.getTabs().length == 0){			
				tabService.addTabs({label:Messages('samples.tabs.search'),href:jsRoutes.controllers.samples.tpl.Samples.home("search").url,remove:true});
				tabService.addTabs({label:$scope.sample.code,href:jsRoutes.controllers.samples.tpl.Samples.get($scope.sample.code).url,remove:true});
				tabService.activeTab($scope.getTabs(1));
			}
			$scope.lists.refresh.resolutions({"objectTypeCode":"Sample"}, "sampleResolutions");
			if(undefined === mainService.get('sampleActiveTab')){
				mainService.put('sampleActiveTab', 'general');

			}else if('treeoflife' ===  mainService.get('sampleActiveTab')){
				$scope.initGraph();
			}

		});
	
		$http.get(jsRoutes.controllers.commons.api.PropertyDefinitions.list().url,{params:{'levelCode':'Sample'}}).then(function(response) {

			response.data.forEach(function(pdef){
					this[pdef.code]=pdef;
			}, samplePropertyDefinitionMap);

		});
	}
	init();

	$scope.getSamplePropertyDefinitionValueType = function(key){
		var propertyDef = samplePropertyDefinitionMap[key];
		if(propertyDef){
			return propertyDef.valueType;
		}
		return null;
	}

	var sampleNodes = undefined;
	$scope.initGraph = function(){
		$scope.messages.clear();
		$scope.setActiveTab('treeoflife');
		if(!sampleNodes){	
			initTreeOfLife($scope.sample);			

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
							tabService.addTabs({label:data.code,href:jsRoutes.controllers.samples.tpl.Samples.get(data.code).url, remove:true});						 		
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


	var computeGraphElements = function(sampleNodes){
		//nodes
		var graphElements = [];
		sampleNodes = $filter('orderBy')(sampleNodes,'indexFromCurrent');
		for(var key in sampleNodes){
			var currentNode = sampleNodes[key];
			var currentSample = sampleNodes[key].sample;
			currentSample.id = currentSample.code;
			currentSample.label = currentSample.code;
			var faveColor = '#6FB1FC';
			if(currentNode.indexFromCurrent < 0){
				faveColor = '#F5A45D'
			}else if(currentNode.indexFromCurrent > 0){
				//faveColor = '#86B342';
				faveColor = '#F5A45D'
			}

			currentSample.faveColor = faveColor;
			currentSample.faveShape="ellipse";
			/*	if(currentSample.contents.length > 1){
				currentSample.faveShape="octagon"; //!!!!GS ne pas modifier la forme de la bulle!!!
			}*/

			graphElements.push({"data":currentSample,"group":"nodes"});

		}

		//edges

		for(var key in sampleNodes){

			var currentNode = sampleNodes[key];
			var currentSample = sampleNodes[key].sample;
			angular.forEach(currentNode.childNodes, function(childNode){
				var childSample = childNode.sample;
				var currentSample = this.sample;
				var edge = {
						"id":currentSample.code+"-"+childSample.code,
						"source":currentSample.code,
						"target":childSample.code

				}

				if(childSample.life && childSample.life.from){
					edge.label=$filter('codes')(childSample.life.from.experimentTypeCode,'type');
					edge.fromExperimentCode = childSample.life.from.experimentCode;
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



	var initTreeOfLife = function(currentSample){
		$scope.treeLoadInProgress = true;
		//extract parent sample codes
		var treeOfLifePathRegex = '^,'+currentSample.code;
		
		var codes = {parentSampleCodes : []};	//For the moment just 1 parent
		if(!angular.isUndefined(currentSample.life) && (currentSample.life !== null)){
			treeOfLifePathRegex = '^'+currentSample.life.path+','+currentSample.code;
			codes.parentSampleCodes = codes.parentSampleCodes.concat(currentSample.life.path.split(",")); //path commence par 1 ,
		}

		var promises = [];
		//promises.push($http.get(jsRoutes.controllers.samples.api.Samples.list().url,{params : {treeOfLifePathRegex:','+currentSample.code+'$|,'+currentSample.code+','}}));
		promises.push($http.get(jsRoutes.controllers.samples.api.Samples.list().url,{params : {treeOfLifePathRegex:treeOfLifePathRegex}}));
		
		if(codes.parentSampleCodes.length > 0){ // Case no paths
			var nbElementByBatch = Math.ceil(codes.parentSampleCodes.length / 6); //6 because 6 request max in parrallel with firefox and chrome
			var queries = [];
			for (var i = 0; i < 6 && codes.parentSampleCodes.length > 0; i++) {
				if (codes.parentSampleCodes[i] == ""){
				    codes.parentSampleCodes.splice(i,i+1); 
				}else{
					var subSampleCode = codes.parentSampleCodes.splice(0, nbElementByBatch); 
					promises.push($http.get(jsRoutes.controllers.samples.api.Samples.list().url, {params : {codes:subSampleCode}}));               		
				}
			}			
		}
		
		$q.all(promises).then(function(results){
			sampleNodes = {};
			var newNode = function(sample){
				return  {sample:sample, parentNode:undefined, childNodes:[],indexFromCurrent:undefined};
			};

			sampleNodes[$scope.sample.code] = newNode($scope.sample);

			angular.forEach(results, function(result){		
				angular.forEach(result.data, function(sample){
					this[sample.code] = newNode(sample);
						}, this)
			}, sampleNodes)


			var updateParentNode = function(currentSampleNode, sampleNodes){
				//only if parent
				if(! currentSampleNode.parentNode){

					if(currentSampleNode.sample.life){
						var parentSample = currentSampleNode.sample.life.from.sampleCode;

						if(sampleNodes[parentSample]){
							var parentSampleNode = sampleNodes[parentSample];
							currentSampleNode.parentNode=parentSampleNode;							
							updateParentNode(parentSampleNode, sampleNodes);								
						}else{
							//when display a branch of a pool
							//throw 'error not found node for '+parentSample.code;
						}
					}
				}			
			};

			for(var key in sampleNodes){
				updateParentNode(sampleNodes[key], sampleNodes);							
			}

			//update child
			for(var key in sampleNodes){
				var currentNode = sampleNodes[key];
			
				if (currentNode.parentNode){
					currentNode.parentNode.childNodes.push(currentNode);
					}		
			} 


			var updateIndexForParents = function(parentNode, childIndex){
				parentNode.indexFromCurrent = childIndex - 1;
				if (parentNode.parentNode){
					updateIndexForParents(parentNode.parentNode, parentNode.indexFromCurrent);
				}
			};

			var updateIndexForChildren = function(childNodes, parentIndex){
				angular.forEach(childNodes, function(childNode){
					childNode.indexFromCurrent = this + 1 ;
					updateIndexForChildren(childNode.childNodes, childNode.indexFromCurrent);
				}, parentIndex);
			}; 

			//update index from current sample
			var currentSampleNode = sampleNodes[$scope.sample.code];			

			currentSampleNode.indexFromCurrent = 0;	
			if (currentSampleNode.parentNode){
				updateIndexForParents(currentSampleNode.parentNode, currentSampleNode.indexFromCurrent);
			}

			updateIndexForChildren(currentSampleNode.childNodes, currentSampleNode.indexFromCurrent);


			var graphElements =  computeGraphElements(sampleNodes);
			initCytoscape(graphElements);
		}, function(results){
			$scope.treeLoadInProgress = false;	
			$scope.messages.setError("get");
		});



	}

}]);