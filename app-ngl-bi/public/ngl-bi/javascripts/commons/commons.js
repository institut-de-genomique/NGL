"use strict";

angular.module('biCommonsServices', []).
    	factory('treatments',['$q','$http','$filter', function($q,$http,$filter){
    		var _treatments = [];
    		var _allTreatments = {};
    		var _treatment = {};    		
    		var codeLastActive = undefined;
    		/**
    		 * Set one element of list active
    		 */
    		function activeTreatment(value){
    			if(angular.isDefined(value)){
    				_treatment = value;
    				for(var i = 0; i < _treatments.length; i++){
    					if(_treatments[i].code != _treatment.code){
    						_treatments[i].clazz='';
    					}else{
    						_treatments[i].clazz='active';
    						codeLastActive=_treatments[i].code;
    					}
    				}
    			} 
    		};
    		
    		function activeLastTreatment(){
    			var find = false;
				for(var i = 0; i < _treatments.length; i++){
					if(_treatments[i].code == codeLastActive){
						_treatment = _treatments[i];
						_treatments[i].clazz='active';
						find = true;
					}else{
						_treatments[i].clazz='';    						
					}
				}
				if(!find)activeTreatment(_treatments[0]);
    		};
    		
    		function prepareCurrentTreatments(){
    			for(var key in _allTreatments){
    				if(_allTreatments[key].show){
    					_treatments.push(_allTreatments[key]);
    				}    				
    			}
    			_treatments = $filter("orderBy")(_treatments,"order");
				activeLastTreatment();	
    		};
    		
    		function init(treatments, url, msgkey, excludes){
    			_treatment = {};
    			_treatments = [];
    			var queries = [];
				
    			for(var key in _allTreatments){
    				_allTreatments[key].show = false;
    			}
    			
    			for (var key in treatments) {
					var treatment = treatments[key];	
					if(!_allTreatments[key+"-"+treatment.typeCode] && (angular.isUndefined(excludes) || angular.isUndefined(excludes[treatment.code]))){
						queries.push($http.get(jsRoutes.controllers.treatmenttypes.api.TreatmentTypes.get(treatment.typeCode).url, 
								{key:key})	
						);
					}else if(_allTreatments[key+"-"+treatment.typeCode]){
						_allTreatments[key+"-"+treatment.typeCode].show = true;						
					}
    			}	
    			if(queries.length > 0){
    				$q.all(queries).then(function(results){
    					for(var i = 0; i  < results.length; i++){
    						var result = results[i];
    						
    						_allTreatments[result.config.key+"-"+result.data.code]={code:result.config.key, name:Messages(msgkey+".treatments."+result.config.key), url:url(result.data.code).url, order:displayOrder(result, key), show:true, propDefinitions:result.data.propertiesDefinitions};
    					}
    					prepareCurrentTreatments();    					
    				});
    			}else{
    				prepareCurrentTreatments();
    			}
    			
    		};
    		
    		function displayOrder(result, key) {
    			var position = "-1"; 
    			if (result.data.displayOrders.indexOf(",") != -1) {
	    			var names = [];
	    			names = result.data.names.split(",");
	    			var orders = [];
	    			orders = result.data.displayOrders.split(",");
	    			
	    			for (var i=0; i<names.length; i++) {
	    				if (names[i] == result.config.key) {
	    					position = orders[i];
	    					break;
	    				}
	    			}
    			}
    			else {
    				position = result.data.displayOrders;
    			}
    			return Number(position);
    		}
    		
    		function getTreatment(){
    			return _treatment;
    		};
    		
    		function getTreatments(){
    			return _treatments;
    		};
    		
    		return {
    			init : init,
    			activeTreatment : activeTreatment,
    			getTreatment : getTreatment,
    			getTreatments : getTreatments
    		};
    	}]).directive('treatments', function() {
    		return {
    			restrict: 'A',
    			scope: {
    				treatments: '=treatments'
    				},
    			template: '<ul class="nav nav-tabs">'+
    				      '<li ng-repeat="treament in treatments.getTreatments()" ng-class="treament.clazz">'+
    					  '<a href="#" ng-click="treatments.activeTreatment(treament)" >{{treament.code}}</a></li>'+		   
    					  '</ul>'+
    					  '<div class="tab-content">'+
    					  '<div class="tab-pane active" ng-include="treatments.getTreatment().url"/>'
    			};
    	}).directive('ngBindSrc', ['$parse',function($parse){ //used to include krona
    		return {
    			restrict: 'A',
    			link: function(scope, element, attr) {
    				var parsed = $parse(attr.ngBindSrc);
    				function getStringValue() { return (parsed(scope) || '').toString(); }
    				
    				scope.$watch(getStringValue, function ngBindHtmlWatchAction(value) {
    					element.attr("src", parsed(scope) || '');
    				    });
    			}
    		}
    	}]).directive('reportingConfigTreatments', function($parse){
    		return {
    			restrict: 'A',
  		    	replace:true,
  		    	scope:true,
  		    	template:''
  		    			+'<div class="row">'
  		    			+'<div class="col-md-4 col-lg-4"><div reporting-properties-select></div></div>'  		    			
  		    			+'<div class="col-md-8 col-lg-8"><div reporting-properties-config></div></div>' 
  		    			+'</div>'
  		    			,  	
  		    	controller: function($scope){ 
  		    		//private
  		    		var selectedProperties = {};
  		    		var allProperties = {};
  		    		var prefix = '';
  		    		
  		    		//public 
  		    		this.init = function(prefixMsg, properties){
  		    			prefix = prefixMsg;
  		    			for(var i = 0; i < properties.length ; i++){
  		    				properties[i].show = false;
  		    				properties[i].toggleShow = function(){
								this.show = !this.show
  		    				};  		  		    					    			
		    			}  		    				
  		    			allProperties = properties;
  		    		};
  		    		
  		    		
  		    		//scope
  		    		$scope.selectShowAll = false;
  		    		$scope.configShowAll = false;
  		    		
  		    		
  		    		$scope.isSelectedProperties = {};
  		    		
  		    		$scope.toggleSelectShowAll = function(){
  		    			$scope.selectShowAll = !$scope.selectShowAll;
  		    			for(var i = 0; i < allProperties.length ; i++){
  		    				allProperties[i].show = !$scope.selectShowAll;
  		    				allProperties[i].toggleShow();
		    			}
  		    		}
  		    		
  		    		$scope.toggleConfigShowAll = function(){
  		    			$scope.configShowAll = !$scope.configShowAll;
  		    			for(var key in selectedProperties){
  		    				selectedProperties[key].show = !$scope.configShowAll;
  		    				selectedProperties[key].toggleShow();
		    			}
  		    		}
  		    		
  		    		$scope.toggleSelectProperty = function(treatmentType, context, property){
  		    			if($scope.isSelectedProperties[treatmentType.instanceCode+"."+context.code+"."+property.code]){
  		    				if(!selectedProperties[treatmentType.instanceCode]){
  		    					$parse(treatmentType.instanceCode+".code").assign(selectedProperties, treatmentType.code);
  		    					$parse(treatmentType.instanceCode+".show").assign(selectedProperties, $scope.configShowAll);
  	  		    				$parse(treatmentType.instanceCode+".toggleShow").assign(selectedProperties, function(){this.show = !this.show});  	  		    			
  		    				}
  		    				$parse(treatmentType.instanceCode+".contexts."+context.code+"."+property.code).assign(selectedProperties, property);  		    				
  		    			} else {
  		    				delete selectedProperties[treatmentType.instanceCode]["contexts"][context.code][property.code];
  		    				var exist = false;
  		    				for (var x in selectedProperties[treatmentType.instanceCode]["contexts"][context.code]){
  		    					if(x)exist = true;
  		    				}
  		    				if(!exist){delete selectedProperties[treatmentType.instanceCode]["contexts"][context.code];	}
  		    				
  		    				exist = false;
  		    				for (var x in selectedProperties[treatmentType.instanceCode]["contexts"]){
  		    					if(x)exist = true;
  		    				}
  		    				if(!exist){delete selectedProperties[treatmentType.instanceCode]["contexts"];}
  		    				
  		    				exist = false;
  		    				for (var x in selectedProperties[treatmentType.instanceCode]){
  		    					if(x)exist = true;
  		    				}
  		    				if(!exist){delete selectedProperties[treatmentType.instanceCode];}  		    					
  		    			}
  		    		}
  		    		
  		    		$scope.getMessage = function(key){
  		    			return Messages(key);
  		    		};
  		    		
  		    		$scope.getTreatmentName = function(value){
  		    			return Messages(prefix+".treatments."+value.replace("-","_"));
  		    		};
  		    		$scope.getContextName = function(value){
  		    			return Messages("treatments.context."+value);
  		    		};
  		    		$scope.getPropertyName = function(treatmentCode, value){
  		    			return Messages(prefix+".treatments."+treatmentCode.replace("-","_")+"."+value);
  		    		};
  		    		
  		    		$scope.getSelectedProperties = function(){
  		    			return selectedProperties;
  		    		}
  		    		
  		    		$scope.getAllProperties = function(){
  		    			return allProperties;
  		    		}
  		    		
  		    		$scope.addToDatatable = function(){
  		    			var columns = [];
  		    			for(var treatmentInstanceName in selectedProperties){
  		    				var treatmentType = selectedProperties[treatmentInstanceName];
  		    				for(var contextCode in treatmentType.contexts){
  		    					var context = treatmentType.contexts[contextCode];
  		    					for(var propertyCode in context){
  		    						var property = context[propertyCode].subProperties[0];
  		    						var column = {
  		    								id:$scope.datatable.generateColumnId(),
  		    								header:$scope.getPropertyName(treatmentType.code,property.code),
  		    								property:"treatments."+treatmentInstanceName+"."+contextCode+"."+property.code+".value", 
											type:property.valueType,
											order:true,
											format:property.format 										
  		    						};
  		    						$scope.datatable.addColumn(4,column);
  		    					}
  		    				}  		  		    			
  		    			}
  		    		}
  		    		
  		    	
  		    	},
  		    	link: function(scope, element, attr, ctrl) {
  		    		if(!attr.reportingConfigTreatments) return;
  		    		var prefix = attr.prefixMsg;
  		    		
  		    		scope.$watch(attr.reportingConfigTreatments, function(newValue, oldValue) {
  		    			if(newValue && (newValue !== oldValue || !scope.dtTable)){
  		    				var reportingConfigTreatments = $parse(attr.reportingConfigTreatments)(scope);
  		    				ctrl.init(prefix, reportingConfigTreatments);
  		    			}
		            });
  		    		
  		    		
  		    	}
    		};
    	}).directive('reportingPropertiesSelect', function($parse){
			return {
				restrict: 'A',
				replace:true,
				scope:true,
		    	template:''
		    			+'<div class="panel panel-default">'   		    			
		    			+'<div class="panel-heading">'
		    			+'	<button class="btn btn-default btn-xs pull-right" ng-click="toggleSelectShowAll()">'
		  		    	+'	<i class="fa fa-plus-square" ng-if="!selectShowAll"></i>'
	  		    		+'	<i class="fa fa-minus-square" ng-if="selectShowAll"></i>' 		
		  		    	+'	</button>'		  		    	
		  		    	+'  <span ng-bind="getMessage(\'title.report.property.selection\')" ng-click="toggleSelectShowAll()"/>'
		    			+'</div>'  		    			
		    			+'<ul class="list-group">'
		    			+'<li class="list-group-item" ng-repeat="treatmentType in getAllProperties()">'
		    			+'	<button class="btn btn-default btn-xs pull-right" ng-click="treatmentType.toggleShow()">'
		  		    	+'	<i class="fa fa-plus-square" ng-if="!treatmentType.show"></i>'
	  		    		+'	<i class="fa fa-minus-square" ng-if="treatmentType.show"></i>' 		
		  		    	+'	</button>'
		    			+'	<h4 class="list-group-item-heading margin-bottom-7" ng-bind="getTreatmentName(treatmentType.instanceCode)" ng-click="treatmentType.toggleShow()"></h4>'
		    			+'	<div class="row" ng-if="treatmentType.show">'
		    			+' 		<div class="col-md-6 col-lg-6" ng-repeat="context in treatmentType.contexts">'
		    			+'		<h5 class="list-group-item-heading"><strong ng-bind="getContextName(context.code)"></strong></h5>'		
		  		    	+'		<ul class="list-unstyled">'
		  		    	+'			<li ng-repeat="property in context.properties">'
		  		    	+'			<div class="checkbox text-overflow">'
		  		    	+'			  <label>'
		  		    	+'			    <input type="checkbox" ng-model="isSelectedProperties[treatmentType.instanceCode+\'.\'+context.code+\'.\'+property.code]" ng-change="toggleSelectProperty(treatmentType, context, property)"><span ng-bind="getPropertyName(treatmentType.code, property.code)"/>'
		  		    	+'			  </label>'
		  		    	+'			</div>'
		  		    	+'			</li>'
		  		    	+'		</ul>'
		    			+'		</div>'  		    			
		    			+'	</div>'
		    			+'</li>'
		    			+'</ul>'
		    			+'</div>'
			};    			
	}).directive('reportingPropertiesConfig', function($parse){
    			return {
    				restrict: 'A',
    				scope:true,
    				replace:true,
    		    	template:''
    		    		+'<div class="panel panel-default">'   		    			
  		    			+'<div class="panel-heading">'
  		    			+'	<button class="btn btn-default btn-xs pull-right" ng-click="toggleConfigShowAll()">'
		  		    	+'	<i class="fa fa-plus-square" ng-if="!configShowAll"></i>'
	  		    		+'	<i class="fa fa-minus-square" ng-if="configShowAll"></i>' 		
		  		    	+'	</button>'
		  		    	+'	<button class="btn btn-default btn-xs pull-right" ng-click="addToDatatable()">'
		  		    	+'	<i class="fa fa-table"></i>'
	  		    		+'	</button>'
  		    			+'  <span ng-bind="getMessage(\'title.report.property.configuration\')" ng-click="toggleConfigShowAll()"/>'
  		    			+'</div>'
  		    			+'<ul class="list-group">'
		    			+'<li class="list-group-item" ng-repeat="(instanceCode, treatmentType) in getSelectedProperties()">'
		    			+'	<button class="btn btn-default btn-xs pull-right" ng-click="treatmentType.toggleShow()">'
		  		    	+'		<i class="fa fa-plus-square" ng-if="!treatmentType.show"></i>'
	  		    		+'		<i class="fa fa-minus-square" ng-if="treatmentType.show"></i>' 		
		  		    	+'	</button>'
		    			+'	<h4 class="list-group-item-heading margin-bottom-7" ng-bind="getTreatmentName(instanceCode)"></h4>'
		    			+'	<div class="row" ng-if="treatmentType.show">'
		    			+' 		<div class="col-md-6 col-lg-6" ng-repeat="(contextCode, context) in treatmentType.contexts">'
		    			+'		<h5 class="list-group-item-heading"><strong ng-bind="getContextName(contextCode)"></strong></h5>'		
		  		    	+'		<ul class="list-unstyled">'
		  		    	+'			<li ng-repeat="(propertyCode, property) in context">'
		  		    	+'				<span ng-bind="getPropertyName(treatmentType.code, property.code)"/>'
		  		    	+'			</li>'
		  		    	+'		</ul>'
		    			+'		</div>'  		    			
		    			+'	</div>'
		    			+'</li>'
		    			+'</ul>'
  		    			+'</div>'  		    			
    			};    			
    	});

