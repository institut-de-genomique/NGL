"use strict"

angular.module('home').controller('ProcessesGraphCtrl', ['$scope', '$http', '$q', '$routeParams','mainService', 'tabService', 'lists', "descriptionsProcessesGraphSearchService", 
	function($scope,   $http,   $q,   $routeParams,   mainService,   tabService,   lists, descriptionsProcessesGraphSearchService) {

        $scope.reset = function() {
            $scope.searchService.resetForm();
            removeWarningMessage();
            removeGraphs();
            cyInstances = {};
        };

        var getProcessTypeListByExpType = function(expTypeCode) {
            return $http.get(jsRoutes.controllers.processes.api.ProcessTypes.list().url,{params:{experimentTypes:expTypeCode,isActive:true, light:false}});
        }

        var getProcessType = function(processTypeCode) {
            return $http.get(jsRoutes.controllers.processes.api.ProcessTypes.get(processTypeCode).url);
        }

        var getExperimentType = function(experimentTypeCode) {
            return $http.get(jsRoutes.controllers.experiments.api.ExperimentTypes.get(experimentTypeCode).url);
        }

        var getExperimentTypeNodeList = function() {
            return $http.get(jsRoutes.controllers.experiments.api.ExperimentTypeNodes.list().url);
        }

        function isExpOnlyNegativePosition(experimentTypes, experimentTypeCode) {
            var isInNegativePosition = experimentTypes.some(function(exp) {return exp.experimentTypeCode === experimentTypeCode && exp.positionInProcess < 0});
            var isInPositivePosition = experimentTypes.some(function(exp) {return exp.experimentTypeCode === experimentTypeCode && exp.positionInProcess >= 0});
            return isInNegativePosition && !isInPositivePosition;
        }

        $scope.addProccessesTab = function() {
            $scope.searchService.search();
            removeWarningMessage();
            removeGraphs();
            if(isExpSelected()) {
                getProcessTypeListByExpType($scope.searchService.form.experimentTypeCode).then(function(result) {
                    displayWarningMsgOrProcessGraph(result.data);
                });
            }
        
            //---
            function isExpSelected() {
                return ($scope.searchService.form.experimentTypeCode !== undefined) && ($scope.searchService.form.experimentTypeCode !== null);
            }

            function displayWarningMsgOrProcessGraph(processTypeList) {
                if(isExpOnlyNegativePositionForAllProcessTypes(processTypeList)) {
                    getExperimentType($scope.searchService.form.experimentTypeCode).then(function(res) {
                        addHTMLforWarningMessage(res.data.name);
                    })
                } else {
                    displayProcessGraph(processTypeList);
                    setDraggableElement();
                }

                //---
                function isExpOnlyNegativePositionForAllProcessTypes(processTypes) {
                    return processTypes.every(function(processType) { return isExpOnlyNegativePosition(processType.experimentTypes, $scope.searchService.form.experimentTypeCode)});
                }

                function addHTMLforWarningMessage(experimentTypeName) {
                    var warningDiv = document.createElement('div');
                    warningDiv.classList.add('alert', 'alert-warning');
                    warningDiv.id = "warningMsg";
                    warningDiv.innerHTML = "L'expérience " + experimentTypeName +" est décrite seulement en previous"; 
        
                    var graphsDiv = document.getElementById('graphs');
                    graphsDiv.parentNode.insertBefore(warningDiv, graphsDiv);
                }

                function displayProcessGraph(processTypes) {
                    var count = 1;
                    processTypes.forEach(function(processType) {
                        if(!isExpOnlyNegativePosition(processType.experimentTypes, $scope.searchService.form.experimentTypeCode)) {				 		
                            addHTMLforGraph(processType.name, processType.code, "panel-body"+count, "inProgressSpinner"+count, "graph"+count, count);
                            setProcessGraph(processType.code, $scope.searchService.form.experimentTypeCode, "panel-body"+count, "inProgressSpinner"+count, "graph"+count);
                            count++;                   
                        }
                    });
                }
            }
        }

        var removeGraphs = function() {
            var element = document.getElementById("graphs");
            while(element.firstChild) {
                element.removeChild(element.firstChild);
            }
        }

        var removeWarningMessage = function() {
            var warningDiv = document.getElementById("warningMsg");
            if(warningDiv) {
                warningDiv.remove();
            }
        }

        var addHTMLforGraph = function(processTypeName, processTypeCode, panelBodyDivId, inProgressSpinnerId, graphdivId, count) {
            var graphsDiv = document.getElementById("graphs");
            var colDiv = document.createElement("div");
            colDiv.classList.add("col-md-6", "col-lg-6");
            colDiv.id = "div"+count;
            colDiv.setAttribute("draggable", false);
            var panelDiv = createPanelDiv();
            colDiv.appendChild(panelDiv);
            graphsDiv.appendChild(colDiv);

            toggleSpinner(true, panelBodyDivId, inProgressSpinnerId);

            //---
            function createPanelDiv() {
                var panelDiv = document.createElement("div");
                panelDiv.classList.add("panel", "panel-default");
                var panelHeadingDiv = createPanelHeadingDiv();
                var panelBodyDiv = createPanelBodyDiv(graphdivId);
                panelDiv.appendChild(panelHeadingDiv);
                panelDiv.appendChild(panelBodyDiv);
                return panelDiv

                //---
                function createPanelHeadingDiv() {
                    var panelHeadingDiv = document.createElement("div");
                    panelHeadingDiv.classList.add("panel-heading");
                    var h4_1 = createH4Title("Nom processus : ", processTypeName);
                    var h4_2 = createH4Title("Code processus : ", processTypeCode);
                    panelHeadingDiv.appendChild(h4_1);
                    panelHeadingDiv.appendChild(h4_2);
                    return panelHeadingDiv;
    
                    //---
                    function createH4Title(smallTextContent, spanTextContent) {
                        var h4 = document.createElement("h4");
                        var small = document.createElement("small");
                        small.textContent = smallTextContent;
                        var span = document.createElement("span");
                        span.textContent = spanTextContent;
                        h4.appendChild(small);
                        h4.appendChild(span);
                        return h4;
                    }
                }
    
                function createPanelBodyDiv(graphDivId) {
                    var panelBodyDiv = document.createElement("div");
                    panelBodyDiv.classList.add("panel-body");
                    panelBodyDiv.id = panelBodyDivId;
                    var graphDiv = createGraphDiv();
                    panelBodyDiv.appendChild(graphDiv);
                    return panelBodyDiv
    
                    //---
                    function createGraphDiv() {
                        var graphDiv = document.createElement("div");
                        graphDiv.id = graphDivId;
                        graphDiv.style.height = "350px";
                        return graphDiv
                    }
                }
            }
        }

        $scope.hasMoreThan2Graphs = function() {
            var graphsDiv = document.getElementById("graphs");
            return graphsDiv.childElementCount >= 3;
        }

        var setDraggableElement = function() {
            var graphsDiv = document.getElementById("graphs");
            $scope.hasMoreThan2Graphs() ? enableDragAndDrop() : disableDragAndDrop();

            //--- 
            function enableDragAndDrop() {
                Array.from(graphsDiv.children).forEach(function(colDiv) {
                    colDiv.querySelector(".panel-heading").classList.add("draggable");
                    
                    colDiv.querySelector(".panel-heading").addEventListener('mouseenter', function() {
                        colDiv.setAttribute("draggable", true); 
                    });

                    colDiv.querySelector(".panel-heading").addEventListener('mouseleave', function() {
                        colDiv.setAttribute("draggable", false);  
                    });

                    colDiv.querySelector(".panel-heading").addEventListener('click', function() {
                        colDiv.setAttribute("draggable", true);
                    });
                    addDragAndDropEvents(colDiv, graphsDiv);
                });
            }

            function disableDragAndDrop() {
                Array.from(graphsDiv.children).forEach(function(child) {
                    child.setAttribute("draggable", false);
                });
            }
        }

        var resizeAllGraphs = function() {
            for(var graphID in cyInstances) {
                if(cyInstances.hasOwnProperty(graphID)) {
                    var cy = cyInstances[graphID];
                    cy.resize();
                }
            }
        }

        var addDragAndDropEvents = function(colDiv, graphsDiv) {
            colDiv.addEventListener('dragstart', function(e) {
                if(colDiv.getAttribute("draggable") === "true") {
                    e.dataTransfer.effectAllowed = 'move';
                    e.dataTransfer.setData('text', colDiv.id);
                    colDiv.classList.add('drag');
                }
            });

            colDiv.addEventListener('dragend', function() {
                colDiv.setAttribute("draggable", false);
                colDiv.classList.remove('drag');
                resizeAllGraphs();
            });

            colDiv.addEventListener('dragover', function(e) {
                e.preventDefault();
                e.dataTransfer.dropEffect = 'move';
                colDiv.classList.add('over');
            });

            colDiv.addEventListener('dragleave', function() {
                colDiv.classList.remove('over');
            });

            colDiv.addEventListener('drop', function(e) {
                e.preventDefault();
                var draggedId = e.dataTransfer.getData('text');
                var draggedElement = document.getElementById(draggedId);
                if(draggedElement !== colDiv) {
                    handleDropEvent(draggedElement);
                }
                colDiv.classList.remove('over');
            });

            //---
            function handleDropEvent(draggedElement) {
                var children = Array.from(graphsDiv.children);
                var draggedIndex = children.indexOf(draggedElement);
                draggedIndex < children.indexOf(colDiv) ? graphsDiv.insertBefore(draggedElement, colDiv.nextSibling) : graphsDiv.insertBefore(draggedElement, colDiv);
            }
        }

        var toggleSpinner = function(treeLoadInProgress, panelBodyDivId, inProgressSpinnerId) {
            var panelBodyDiv = document.getElementById(panelBodyDivId);
            return treeLoadInProgress ? addSpinner() : deleteSpinner();

            //---
            function addSpinner() {
                var inProgressSpinnerDiv = document.createElement("div");
                inProgressSpinnerDiv.classList.add("inProgressSpinner");
                inProgressSpinnerDiv.id = inProgressSpinnerId;
    
                var button = document.createElement("button");
                button.classList.add("btn", "btn-primary", "btn-lg");
    
                var icon = document.createElement("i");
                icon.classList.add("fa", "fa-spinner", "fa-spin", "fa-5x");
    
                button.appendChild(icon);
                inProgressSpinnerDiv.appendChild(button);
    
                panelBodyDiv.prepend(inProgressSpinnerDiv);
            }
    
            function deleteSpinner() {
                var inProgressSpinnerDiv = document.getElementById(inProgressSpinnerId);
                if(panelBodyDiv.firstChild === inProgressSpinnerDiv) {
                    panelBodyDiv.removeChild(panelBodyDiv.firstChild);
                }
            }
        }

        var setProcessGraph = function(processTypeCode, expTypeCode, panelBodyDivId, inProgressSpinnerId, graphID) {
            getExperimentTypeNodeList().then(function(expNodes) {
                getProcessType(processTypeCode).then(function(result) {
                    var processExperimentTypes = new Map();
                    mapPositionsToExperimentType(result.data.experimentTypes, processExperimentTypes);
                    $scope.graphNodes = computeGraphNodes(expNodes.data);
                    var graphElements  = computeGraphElements($scope.graphNodes, processExperimentTypes, expTypeCode);
                    initCytoscape(graphElements, panelBodyDivId, inProgressSpinnerId, graphID);	
                });
            });

            //---
            function mapPositionsToExperimentType(experimentTypes, processExperimentTypes) {
                experimentTypes.forEach(function(experimentType) {
                    if(this.get(experimentType.experimentTypeCode) === undefined) {
                        this.set(experimentType.experimentTypeCode, [experimentType.positionInProcess]);
                    } else {
                        this.get(experimentType.experimentTypeCode).push(experimentType.positionInProcess);
                    }	
                },processExperimentTypes);
            }
        }

        var computeGraphNodes = function(expNodes) {
            var graphNodes = {};
            createNodeForEachExpNodes();
            setChildNodesForParentNodes();
            return graphNodes;

            //---
            function newNode(experimentNode) {
                return {experimentType:experimentNode.experimentType, parentNodes:(experimentNode.previousExperimentTypes)?experimentNode.previousExperimentTypes:[], childNodes:[]};
            };

            function createNodeForEachExpNodes() {
                expNodes.forEach(function(experimentNode) {
                    graphNodes[experimentNode.experimentType.code] = newNode(experimentNode);
                })
            }

            function setChildNodesForParentNodes() {
                expNodes.forEach(function(experimentNode) {
                    graphNodes[experimentNode.experimentType.code].parentNodes.forEach(function(parent){
                        graphNodes[parent.code].childNodes.push(this);
                    }, experimentNode.experimentType) 
                })
            }
        }
        
        var computeGraphElements = function(graphNodes, processExperimentTypes, experimentTypeCode) {
            var graphElements = [];
            computeNodes();
            computeEdges();
            return graphElements;

            //---
            function isTransfoExperiment(experimentType) {
                return experimentType.category.code === 'transformation';
            }

            function isVoidProcessExperiment(experimentType) {
                return experimentType.category.code === 'voidprocess';
            }

            function getFaveColor(key, currentExperimentTypeCode) {
                var blueColor = '#6FB1FC';
                var orangeColor = '#F5A45D';
                var greyColor = '#BDBDBD';
                if(processExperimentTypes === undefined) return greyColor;
                else if(currentExperimentTypeCode === experimentTypeCode) return blueColor;
                else if(processExperimentTypes.get(key)[0] > -1 ) return orangeColor;
                else return greyColor;
            }

            function computeNodesForTransoAndVoidProcessExp(currentExperimentType, key) {
                if(isTransfoExperiment(currentExperimentType)) {
                    setupGraphNodeData(currentExperimentType.name);
                } else if(isVoidProcessExperiment(currentExperimentType)) {
                    setupGraphNodeData("None");
                }

                //---
                function setupGraphNodeData(label) {
                    currentExperimentType.id = currentExperimentType.code;
                    currentExperimentType.label = label;
                    currentExperimentType.faveColor = getFaveColor(key, currentExperimentType.code);
                    currentExperimentType.faveShape="ellipse";
                    graphElements.push({"data":currentExperimentType,"group":"nodes"});
                }
            }

            function computeNodes() {
                for(var key in graphNodes) {
                    if(processExperimentTypes === undefined || processExperimentTypes.get(key) !== undefined) {
                        var currentExperimentType = graphNodes[key].experimentType;
                        computeNodesForTransoAndVoidProcessExp(currentExperimentType, key);
                    }
                }
            }

            function computeEdges() {
                for(var key in graphNodes){
                    if(processExperimentTypes === undefined || processExperimentTypes.get(key) !== undefined) {
                        var currentNode = graphNodes[key];
                        var currentExperimentType = graphNodes[key].experimentType;
                        if(isTransfoExperiment(currentExperimentType) || isVoidProcessExperiment(currentExperimentType)) {
                            createParentChildsEdge(currentNode, key)
                        }
                    }
                }

                //---
                function createParentChildsEdge(currentNode, key) {
                    angular.forEach(currentNode.childNodes, function(childNode) {
                        var childExperimentType = childNode;
                        if(isTransfoExperiment(childExperimentType) 
                            && (processExperimentTypes === undefined
                                    || (processExperimentTypes.get(childExperimentType.code) !== undefined && isDiffPositionIsOne(key, childExperimentType.code)))) {
                            var currentExperimentType = this;
                            setupGraphEdgeData(currentExperimentType, childExperimentType, key);
                        }
                    },currentExperimentType);

                    //---
                    function isDiffPositionIsOne(keyParent, keyChild) {
                        return processExperimentTypes.get(keyParent).some(function(parentPos) {
                            return processExperimentTypes.get(keyChild).some(function(childPos) {
                                    return (childPos - parentPos === 1);
                            });
                        });		
                    }

                    function setupGraphEdgeData(currentExperimentType, childExperimentType, key) {
                        var edge = {
                            "id":currentExperimentType.code+"-"+childExperimentType.code,
                            "source":currentExperimentType.code,
                            "target":childExperimentType.code       
                        }
                        edge.faveColor = getFaveColor(key, currentExperimentType.code);
                        graphElements.push({"data":edge,"group":"edges"})
                    }
                }
            }
        }

        var cyInstances = {}

        var highlightSelectedNodeAndNeighbors = function(cyInstance, node) {
            cyInstance.elements().difference(node.incomers()).difference(node.outgoers()).not(node).addClass('semitransp');
            node.addClass('highlight');
            node.incomers().addClass('highlight');
            node.outgoers().addClass('highlight');
        }

        var removeHighlight = function(cyInstance) {
            cyInstance.elements().removeClass('semitransp');  
        }

        var initCytoscape = function(graphElements, panelBodyDivId, inProgressSpinnerId, graphID) {
            var asynchGraph = function() {
                 return $q(function(resolve, reject) {
                     setTimeout(function() {
                        cyInstances[graphID] = 
                            cytoscape({
                                  container: document.getElementById(graphID),
                                  boxSelectionEnabled: false,
                                  autounselectify: true,
                                  wheelSensitivity: 0.1, //NGL-4082 ajout
                                  layout: {
                                    name: 'dagre',
                                    directed:true,
                                    padding:5,
                                    spacingFactor:0.8,
                                    avoidOverlap: true,
                                    animate : false
                                  },
                                  style: cytoscape.stylesheet()
                                      .selector('node')
                                        .css({
                                          'shape': 'data(faveShape)',
                                          'width': '190',
                                          'label': 'data(label)',
                                          'text-valign': 'center',
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
                                      .selector('node.highlight')
                                      .selector('node.semitransp')
                                       .css({
                                          'opacity': '0.5'
                                       })
                                      .selector('edge.highlight')
                                       .css({
                                          'mid-target-arrow-color': '#FFF'
                                       })
                                      .selector('edge.semitransp')
                                       .css({
                                          'opacity': '0.2'
                                       })
                                  ,
                                  elements : graphElements
                                });
                                cyInstances[graphID].on('tap', 'node', function(evt) {
                                    var target = evt.target;
                                    highlightSelectedNodeAndNeighbors(cyInstances[graphID], target);
                                });
                                cyInstances[graphID].on('click',function(evt) {
                                    removeHighlight(cyInstances[graphID]);   
                                });
                                cyInstances[graphID].ready(function(evt) {	
                                    toggleSpinner(false, panelBodyDivId, inProgressSpinnerId);
                                });
                    });	
                 }, 1);
            };
            asynchGraph();
        }

        var handleHomePage = function() {
            if(isHomePageDefined()) return;
            defineHomePage();
            setTabs();

            //---
            function isHomePageDefined() {
                return angular.isDefined($scope.getHomePage());
            }

            function defineHomePage() {
                mainService.setHomePage('instruments');
            }

            function setTabs() {
                tabService.addTabs(controllerTabs());
                tabService.activeTab(0);
    
                //---
                function controllerTabs() {
                    return {
                        label:Messages('descriptions.tabs.processes.graph'),
                        href:defaultTabUrl(),
                        remove:true
                    };
                }
        
                function defaultTabUrl() {
                    return jsRoutes.controllers.descriptions.tpl.Descriptions.home("processesgraph").url;
                }
            }
        }
        
        var initSearchService = function() {
            $scope.searchService = descriptionsProcessesGraphSearchService;
            $scope.searchService.init($routeParams);
        }

        var init = function() {
            handleHomePage();
            initSearchService();
        }

        init();                                                                       
}]);