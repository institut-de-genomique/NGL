"use strict";

angular.module('biWorkflowChartServices', []).
	directive('workflowChart', ['$compile', '$http', '$q', '$filter', 'lists', function ($compile, $http, $q, $filter, lists) {
    	
		
		var modalTemplate = 	"<div id='{{modalId}}' class='modal fade'  tabindex='-1' role='dialog'"
								+"	aria-labelledby='myModalLabel' aria-hidden='true'>"
								+"	<div class='modal-dialog'>"
							    +"		<div class='modal-content' style='width:{{modalContentWidth+2}}px'>"
								+"			<div class='modal-header'>"
								+"				<button type='button' class='close' data-dismiss='modal' aria-hidden='true'>&times;</button>"
								+"				<h3 class='modal-title'>{{modalHeaderText}}</h3>"													
								+"			</div>"
								+"			<div class='modal-body' style='padding:0px'>"
								+"				<div id='container0'></div>"
								+"			</div>"
								+"			<div class='modal-footer'>"
								+"			</div>"
								+"		</div>"
								+"	</div>"
								+"</div>";
	    
	    var linkTemplate = "<p class='form-control-static'><a href='#{{modalId}}' id='linkTo{{modalId}}' role='button' data-toggle='modal'>{{modalCurrentCode | codes:'state'}}</a></p>";
	    
	    var linker = function (scope, element, attrs) {
	    	
	        scope.modalHeaderText = attrs.modalHeaderText;
	        scope.modalBodyText = attrs.modalBodyText;
	        scope.modalId = attrs.modalId || "modalWorflowChart";
	        scope.modalBgColor = attrs.modalBgColor;
	        scope.modalWidth = attrs.modalWidth;
	        scope.modalHeight = attrs.modalHeight;
	        scope.modalContentWidth = parseFloat(scope.modalWidth) + 100;
	        scope.modalContentHeight = parseFloat(scope.modalHeight) + 100;
	        scope.lists = lists;
	        
			element.on('click', function(event) {
			    event.preventDefault();
	            $q.when(createEmptyChart()).then(function(chart) {
	            		populateChart(chart);
	            	}
	            );                
			});
			
	        
			function createEmptyChart() {
			 	var chart1 = new Highcharts.Chart({
					chart : {
						renderTo : 'container0',
						backgroundColor: scope.modalBgColor,
						height: scope.modalHeight,
						width:scope.modalWidth
					},	
					title : {text : scope.modalBodyText}
				});
			 	return chart1;
			};
			
			
			function drawLabel(ren, data, offsetXText, offsetYText, globalParam) {
				ren.label(data.childStateName, offsetXText, offsetYText)
                .attr({
                    fill: getFillColor(data.childStateCode, scope.modalCurrentCode),
                    stroke: getBorderColor(data.childStateCode, scope.modalCurrentCode, data.specificColor),
                    'stroke-width': 2,
                    padding: 5,
                    width: globalParam.boxWidth,
                    height:globalParam.boxHeight,
                    r: 5
                })
                .css({
                    color: getFontColor(data.childStateCode, scope.modalCurrentCode, data.specificColor),
                    fontStyle: '10px',
                    fontWeight: 'normal',
                    fontFamily: 'arial'
                })
                .add()
                .shadow(true);
				
				drawComment(ren, data, offsetXText, offsetYText, globalParam);
			};
			
			
			function drawComment(ren, data, offsetXText, offsetYText, globalParam) {
				if (data.comment != undefined && data.comment != null) {
					var lbl = data.comment.label;
					if (data.comment.type == 'datetime') {
						lbl = $filter('date')(data.comment.label, Messages("datetime.format"))
					}
					if (data.comment.type == 'date') {
						lbl = $filter('date')(data.comment.label, Messages("date.format"))
					}
    				ren.label(lbl, offsetXText + globalParam.boxWidth+15, offsetYText)
    				.css({
	                    color: 'darkgray',
	                    fontStyle: '9px',
	                    fontWeight: 'italic',
	                    fontFamily: 'arial'
	                })
	                .add()
				}	
			};
			
			
			
			function drawArrow(ren, offsetXText, offsetXText2, offsetYText, offsetYText2, globalParam) {
				var offsetXArrow = offsetXText + (globalParam.boxWidth/2 -1);
				var offsetXArrow2 = offsetXText2 + (globalParam.boxWidth/2 -1);    				
				var offsetYArrow = offsetYText + globalParam.boxHeight;
				var offsetYArrow2 = offsetYText2;
				var arrow;
				
				if (offsetXArrow == offsetXArrow2) {
    				arrow = ren.path(['M', offsetXArrow, offsetYArrow, 'L', offsetXArrow2,  offsetYArrow2, /*body arrow*/ 
                              'L', offsetXArrow2-5, offsetYArrow2-5, 'M', offsetXArrow2,  offsetYArrow2, /*left side*/ 
                              'L', offsetXArrow2+5, offsetYArrow2-5, 'M', offsetXArrow2,  offsetYArrow2]) /*right side*/;
				}
				else {
    				arrow = ren.path(['M', offsetXArrow, offsetYArrow, 'L', offsetXArrow2,  offsetYArrow2, 
                              'L', offsetXArrow2-1, offsetYArrow2-5, 'M', offsetXArrow2,  offsetYArrow2, 
                              'L', offsetXArrow2+3, offsetYArrow2+4, 'M', offsetXArrow2,  offsetYArrow2]);
				}
				
				arrow.attr({'stroke-width': 2, stroke: 'darkgray'}).add();
 			};
			
			
			function drawSeparatorLine(ren, offsetY, globalParam) {
				var offsetXLine = globalParam.offsetXText -10;
				var offsetYLine = offsetY;
				var offsetXLine2 = globalParam.offsetXText + globalParam.boxWidth+20;
				
                ren.path(['M', offsetXLine, offsetYLine, 'L', offsetXLine2, offsetYLine])
                .attr({
                    'stroke-width': 2,
                    stroke: 'silver',
                    dashstyle: 'dash'
                })
                .add();
			};
			
			
			function renderChart(renderer, data, globalParam) {				
				var offsetXText = globalParam.offsetXText;
				var offsetYText = globalParam.offsetYText;				
				var offsetXText2 = offsetXText, offsetYText2 = offsetYText;    	
				var bSeparator, bOldSeparator = false;

				for (var i=0; i<data.length; i++) {	
					bSeparator = false;
					if (i > 0) {
						if (data[i].parentStateCode == data[i-1].childStateCode) {
							//increase offsetYText2 (vertical moving)
							offsetYText2 += globalParam.spaceVbetween2box + globalParam.boxHeight;
							
							if ((data[i].functionnalGroup != undefined) && (data[i].functionnalGroup != null) && (data[i].functionnalGroup != data[i-1].functionnalGroup)) {
								
								drawSeparatorLine(renderer, offsetYText2 - globalParam.spaceVbetween2box/4, globalParam); 
								
								offsetYText2 += globalParam.spaceVbetween2box / 2;
								bSeparator = true;
							}
							drawArrow(renderer, offsetXText, offsetXText2, offsetYText, offsetYText2, globalParam);
						}
						else {
							if (data[i].parentStateCode == data[i-1].parentStateCode) {
								//increase offsetXText2 (horizontal moving)
								offsetXText2 = offsetXText + (globalParam.spaceHbetween2box + globalParam.boxWidth);
								
								offsetYText = offsetYText -  globalParam.spaceVbetween2box - globalParam.boxHeight;
								
								if (bOldSeparator) {
									offsetYText -=  globalParam.spaceVbetween2box / 2;
								}
								
								drawArrow(renderer, offsetXText, offsetXText2, offsetYText, offsetYText2, globalParam);
							}
							else {
								var x=1;
								while (data[i].parentStateCode != data[i-x].childStateCode) {
									x++;	
								}
								x--;
								
								offsetXText = offsetXText - x*(globalParam.spaceHbetween2box + globalParam.boxWidth);
								offsetXText2 = offsetXText;
								
								offsetYText2 += globalParam.spaceVbetween2box + globalParam.boxHeight;
								
								if ((data[i].functionnalGroup != undefined) && (data[i].functionnalGroup != null) && (data[i].functionnalGroup != data[i-1].functionnalGroup)) {
									
									drawSeparatorLine(renderer, offsetYText2 - globalParam.spaceVbetween2box/4, globalParam); 
									
									offsetYText2 += globalParam.spaceVbetween2box / 2;
									bSeparator = true;
								}
								
								drawArrow(renderer, offsetXText, offsetXText2, offsetYText, offsetYText2, globalParam);
							}
						}	
					}
					drawLabel(renderer, data[i], offsetXText2, offsetYText2, globalParam); 
					bOldSeparator = bSeparator;
					
					offsetXText = offsetXText2;
					offsetYText = offsetYText2;
				}
			};
			
			
			function populateChart(chart) {	
				 scope.$watch('modalData', function() {  
					
					var data = orderData(scope.modalData); 
    	            
    	        	if (scope.modalHistoricalData != undefined && scope.modalHistoricalData != null && scope.modalHistoricalData.length > 0) {
	    	        	data = updateDataWithComment(data, scope.modalHistoricalData);
	    	        }
    	        	
    	            var globalParam = { spaceVbetween2box:20, 
					    	            spaceHbetween2box:160,
					    	            boxWidth:160,
					    	            boxHeight:25,
					    				offsetXText:100,
					    	            offsetYText:32 };

	            	renderChart(chart.renderer, data, globalParam);        
		        }, true);
			};
			
			
			
			function orderByDescent(newData, data, d) {
				var bChildExists = false;
				for (var i=1; i<data.length; i++) {
					if (data[i].parentStateCode == d.childStateCode) {
						bChildExists = true; //node
						data[i].level = d.level + 1;
						newData.push(data[i]);
						orderByDescent(newData, data, data[i]); 
					}
				}
				if (!bChildExists) {
					if (newData.indexOf(d) == -1) {
						newData.push(d); //leaf
					}
				}
				return newData;
			}
			
			
			function orderData(data) {
				//find root and make it the first data 
				var rootData, bRootData = false;
				var newData = [];
				for (var i=0; i<data.length; i++) {
					if (data[i].childStateCode == data[i].parentStateCode) {
						rootData = data[i];
						bRootData = true;
						data.splice(i,1);
						break;
					}
				}
				data.splice(0,0,rootData);
				rootData.level = 0;
				newData.push(rootData);
				//order data
				newData = orderByDescent(newData, data, rootData);
				//re-order by position for data at the same hierarchical level
				newData.sort(function(a, b) { return (a.level > b.level ? 1 : (a.level < b.level ? -1 : (a.position > b.position ? 1 : -1))); });
				//error alert 
				if (!bRootData) {
					alert("Missing a root for the data : could not find a starting point to render the workflow");
				} 
				return newData;
			};
			
	    	
	    	function getFillColor(code1, code2) {
				return (code1==code2?'#4BACC6':'#F2F2F2');
			};
			
	    	function getBorderColor(code1, code2, specificColor) {
	    		return (code1==code2?'#31859C':(specificColor===undefined?'#BFBFBF':'#D9D9D9'));
			};
			
	    	function getFontColor(code1, code2, specificColor) {
	    		return (code1==code2?'white':(specificColor===undefined?'black':'#A6A6A6'));
			};
			
			
			function updateDataWithComment(data, historical) {
				for (var i=0; i<data.length; i++) {
					for (var j=0; j<historical.length; j++) {
						if (data[i].childStateCode == historical[j].code) {
							data[i].comment = {label:historical[j].date,type:'datetime'};	
							break;
						} 
					}
				}	
				return data;
			};
			 		
	    } //end of linker
	        
	    return {
	        restrict: "E",
	        replace: false,
	        link: linker,
	        template: modalTemplate + linkTemplate,
	        transclude: false,
	        scope: {modalCurrentCode: "=", modalHistoricalData: "=", modalData: "="}
	    };
	    
	}]);