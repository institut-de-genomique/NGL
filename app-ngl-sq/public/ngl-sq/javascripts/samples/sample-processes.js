angular.module('ngl-sq.samplesServices')
.directive('displaySampleProcesses', [ '$parse', '$filter', '$window', '$sce', function($parse, $filter, $window, $sce) {
	return {
		restrict : 'EA',
		scope : {
			dspProcesses :'=',
			dspProcessCategoryCodes : '=',
			dspShowRs : '=',
			dspShowPercent : '='
		},
		template: "<ul class='list-group' style='margin-bottom:0px'>"
				+" 	<li  ng-repeat='(typeCode, values) in processesByTypeCode' class='list-group-item'>"
				+" 	{{typeCode|codes:'type':false}} :  "
				+"  <a href='#' role='button' ng-repeat='p in values|orderBy:\"traceInformation.creationDate\"' ng-click='goTo(p,$event)' ng-class='getProcessClass(p)' style='margin-right:2px' title='{{p.currentExperimentTypeCode|codes:\"type\"}}' ng-bind-html='getInfo(p)'>"
				+"  </a>"
				+ "<span class='badge' ng-bind='values.length' ng-click='goToAllProcesses(values,$event)'></span>"
				+"  </li>"
				+" </ul>"
		,
		link : function(scope, element, attr, ctrl) {
			
			scope.getProcessClass = function(process){
				if(process.state){
					if(process.state.code === 'N'){
						return "label label-info";
					}else if(process.state.code === 'IP'){
						return "label label-warning"
					}else if(process.state.code === 'IW-C'){
						return "label label-perso"
					}else if(process.state.code === 'F' && process.experiments && process.experiments.length > 0){
						return "label label-primary"
					}else if(process.state.code === 'F' && (!process.experiments || process.experiments.length === 0)){
						return "label label-default"
					}
				}else{
					return "label label-primary"
				}
			};
			
			scope.getInfo = function(process){
				var value = "";
				if(scope.dspShowRs && process.readsets && process.readsets.length > 1){
					value =  process.readsets.length+"rs";					
				}else if(scope.dspShowRs && process.readsets && process.readsets.length === 1){
					value =  "rs";									
				}else if(scope.dspShowPercent && process.progressInPercent){
					value =  process.progressInPercent;
				}else{
					value =  "  ";
				}
				
				return $sce.trustAsHtml(value+"");
			}
			
			scope.goTo = function(process,$event){
				if(scope.dspShowRs && process.readsets && process.readsets.length > 1){
					var params = "";
					process.readsets.forEach(function(readset){
						params +="codes="+readset.code+"&";
					}, params);
					$window.open(AppURL("bi")+"/readsets/search/home?"+params, 'readsets');
				}else if(scope.dspShowRs && process.readsets && process.readsets.length === 1){
					$window.open(AppURL("bi")+"/readsets/"+process.readsets[0].code, 'readset');
				}else{
					$window.open(jsRoutes.controllers.processes.tpl.Processes.home("search").url+"?code="+process.code+"&categoryCodes="+process.categoryCode+"&typeCodes="+process.typeCode, 'processes');
				}	
				$event.stopPropagation();
			}
			
			scope.goToAllProcesses = function(processes,$event){
				if(processes && processes.length > 1){
					var params = "";
					processes.forEach(function(p){
						params +="codes="+p.code+"&";
					}, params);
					$window.open(jsRoutes.controllers.processes.tpl.Processes.home("search").url+"?"+params+"&typeCode="+processes[0].typeCode, 'processes');
				}	
				$event.stopPropagation();
			}
			
			scope.$parent.$watchCollection(attr.dspProcesses, function(newValue, oldValue){
				 init(newValue, scope.dspProcessCategoryCodes);	    	  
			}, true);
			
			scope.$parent.$watch(attr.dspProcessCategoryCodes, function(newValue, oldValue){
	    	     if(oldValue !== newValue){		    		
	    	    	 init(scope.dspProcesses, newValue);
	    	     }		    	  
			}, true);
			
			scope.$parent.$watch(attr.dspShowRs, function(newValue, oldValue){
	    	     if(oldValue !== newValue){		    		
	    	    	 scope.dspShowRs = newValue;
	    	     }		    	  
			}, true);
			
			scope.$parent.$watch(attr.dspShowPercent, function(newValue, oldValue){
	    	     if(oldValue !== newValue){		    		
	    	    	 scope.dspShowPercent = newValue;
	    	     }		    	  
			}, true);
			
			
			var init = function(dspProcesses, dspProcessCategoryCodes){
				var filterProcesses = dspProcesses;
				if(dspProcessCategoryCodes && dspProcessCategoryCodes.length > 0){
					filterProcesses = []
					for(var i = 0; i < dspProcessCategoryCodes.length; i++){
						filterProcesses = filterProcesses.concat($filter('filter')(scope.dspProcesses, {categoryCode:dspProcessCategoryCodes[i]},true));
					}					
				}
				
				//organize by typeCode
				var processesByTypeCode = {};
				if(filterProcesses && filterProcesses.length > 0){
					filterProcesses.forEach(function(p){
						if(!processesByTypeCode[p.typeCode]){
							processesByTypeCode[p.typeCode] = [];
						}
						processesByTypeCode[p.typeCode].push(p);
					}, processesByTypeCode)
				}
				scope.processesByTypeCode = processesByTypeCode;
			};
			init(scope.dspProcesses, scope.dspProcessCategoryCodes);
		}
	};
}]).directive('legendSampleProcesses', [ '$parse', '$filter', function($parse, $filter) {
	return {
		restrict : 'EA',
		template:'<a id="legendSampleProcesses" class="btn btn-info btn-xs">?</a>',
		link : function(scope, element, attr, ctrl) {
			
			var options = {
					placement : "top",
					title : Messages('legendSampleProcesses.title'),
					html:true,
					content : '<ul class="list-group">'
							+'	<li class="list-group-item"><a class="label label-primary"  style="margin-right:2px"> </a> : '+Messages('legendSampleProcesses.label.primary')+'</li>'
							+'	<li class="list-group-item"><a class="label label-primary"  style="margin-right:2px">rs</a> : '+Messages('legendSampleProcesses.label.primary.rs')+'</li>'							
							+'	<li class="list-group-item"><a class="label label-warning"  style="margin-right:2px"> </a> : '+Messages('legendSampleProcesses.label.warning')+'</li>'
							+'	<li class="list-group-item"><a class="label label-info"  style="margin-right:2px"> </a> : '+Messages('legendSampleProcesses.label.info')+'</li>'
							+'	<li class="list-group-item"><a class="label label-perso"  style="margin-right:2px"> </a> : '+Messages('legendSampleProcesses.label.perso')+'</li>'
							+'	<li class="list-group-item"><a class="label label-default"  style="margin-right:2px"> </a> : '+Messages('legendSampleProcesses.label.default')+'</li>'							
							+'</ul>',
					trigger : "click"					
			};
			
			angular.element("#legendSampleProcesses").popover(options);
		}
	};
}]).directive('presentSampleProcesses', [ '$parse', '$filter', function($parse, $filter) {
	return {
		restrict : 'A',
		scope : {
			value:'=presentSampleProcesses',
			inverse:'='
		},
		template:'<button type="button" ng-class="getButtonClass()" ng-if="value != null && value != undefined && value <= 1">'
						+'<i ng-class="getIconClass()" aria-hidden="true"></i>'						
					+'</button>'
					+'<span ng-if="value > 1" ng-bind="value"></span>',
		link : function(scope, element, attr, ctrl) {
			
			if(null == scope.inverse || undefined == scope.inverse  || scope.inverse  === false || scope.inverse  === 'false'){
				scope.inverse  = false;
			}else{
				scope.inverse  = true;
			}
			
			scope.getIconClass = function(){
				/* v1
				if((scope.value === 1 && !scope.inverse) || (scope.value === 0 && scope.inverse)){
					return "fa fa-check";
				}else if((scope.value === 0 && !scope.inverse) || (scope.value === 1 && scope.inverse)){
					return "fa fa-times";
				}
				*/
				if(scope.value === 1 ){
					return "fa fa-check";
				}else if(scope.value === 0 ){
					return "fa fa-times";
				}
				
			}
			
			scope.getButtonClass = function(){
				/* v1
				if(scope.value === 1 ){
					return 'btn btn-success btn-xs';
				}else if(scope.value === 0){
					return 'btn btn-danger btn-xs';
				}
				*/
				if((scope.value === 1 && !scope.inverse) || (scope.value === 0 && scope.inverse)){
					return 'btn btn-success btn-xs';
				}else if((scope.value === 0 && !scope.inverse) || (scope.value === 1 && scope.inverse)){
					return 'btn btn-danger btn-xs';
				}
				
			}
		}
	};
}]).filter('presentSampleProcesses',['$filter',function ($filter) {
	return function (processes, filters, level, inverse) {
		if(null == inverse || undefined == inverse || inverse === false || inverse === 'false'){
			inverse = false;
		}else{
			inverse = true;
		}
		
		if(null !== filters && undefined !== filters && filters.length > 0
				&& null !== processes && undefined !== processes){
			if (!angular.isArray(filters)) filters = [filters];
			var filteredData = [];
			for(var i = 0; i < filters.length; i++){
				if(level === "process"){
					for(var i = 0; i < filters.length; i++){
						var result =  $filter('filter')(processes, {typeCode:filters[i]},true);
						if(result && result.length > 0)filteredData = filteredData.concat(result);
					}
				}else if(level === "experiment"){
					for(var i = 0; i < filters.length; i++){
						var result = $filter('filter')(processes, {experiments:{typeCode:filters[i]}},true);
						if(result && result.length > 0)filteredData = filteredData.concat(result);
					}
				}				    				
			}
			
			if((filteredData.length > 0 && !inverse) 
					|| (filteredData.length === 0 && inverse)){return 1;}
			else {return 0;}		
			
		}else if(null !== filters && undefined !== filters && 
				(null === processes || undefined === processes)){
			if(!inverse)return 0;
			else return 1;
		}else{
			return undefined;
		}
	};
}]).filter('isConditions',['$filter',function ($filter) {
	return function (processes, conditions, alternateConditions) { //$or between conditions and alternateConditions
		if(!angular.isObject(conditions)) return;
		if(!angular.isArray(processes))return 0;
		
		if(angular.isArray(conditions)){
			conditions = [conditions];
		}else if(!angular.isArray(conditions) && !conditions["$or"]){ //only one conditions
			conditions = [[conditions]];
		}else if(!angular.isArray(conditions) && conditions["$or"] && angular.isArray(conditions["$or"])){
			conditions = conditions["$or"].map(function(elt){return (!angular.isArray(elt))?[elt]:elt;});
		}else {
			throw "conditions configuration not managed !";
		}
		
		if(angular.isObject(alternateConditions)) {
			(!angular.isArray(alternateConditions))?conditions.push([alternateConditions]):conditions.push(alternateConditions);			
		}
		
		
		
		var check = function(condition){
			//by default strict comparison
			if(condition.comparator === undefined)condition.comparator = true;
			var results = $filter('filter')([this], condition.criteria, condition.comparator);
			if((results.length > 0 && condition.expected === true)
					|| (results.length === 0 && condition.expected === false)){
				return true;
			}else{
				return false;
			}
		};
		
		var process = processes.find(function(process){
			var test = conditions.some(function(insideConditions){
				return insideConditions.every(check, process);
			});
			
			return test;
		});

		if(process){
			return 1;
		}else return 0;
		
	};

}]).filter('conditionFilters',['$filter',function ($filter) {
	return function (processes, conditions, alternateConditions) { //$or between conditions and alternateConditions
		if(!angular.isObject(conditions)) return;
		if(!angular.isArray(processes))return 0;
		
		if(angular.isArray(conditions)){
			conditions = [conditions];
		}else if(!angular.isArray(conditions) && !conditions["$or"]){ //only one conditions
			conditions = [[conditions]];
		}else if(!angular.isArray(conditions) && conditions["$or"] && angular.isArray(conditions["$or"])){
			conditions = conditions["$or"].map(function(elt){return (!angular.isArray(elt))?[elt]:elt;});
		}else {
			throw "conditions configuration not managed !";
		}
		
		if(angular.isObject(alternateConditions)) {
			(!angular.isArray(alternateConditions))?conditions.push([alternateConditions]):conditions.push(alternateConditions);			
		}
		
		
		
		var check = function(condition){
			//by default strict comparison
			if(condition.comparator === undefined)condition.comparator = true;
			var results = $filter('filter')([this], condition.criteria, condition.comparator);
			if((results.length > 0 && condition.expected === true)
					|| (results.length === 0 && condition.expected === false)){
				return true;
			}else{
				return false;
			}
		};
		
		var process = processes.filter(function(process){
			var test = conditions.some(function(insideConditions){
				return insideConditions.every(check, process);
			});
			
			return test;
		});
		
		return process;
		
	};

}]);
