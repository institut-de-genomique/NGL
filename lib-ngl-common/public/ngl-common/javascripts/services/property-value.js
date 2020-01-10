angular.module('commonsServices').factory('propertyDefinitions', ['$http', function($http){
	
	var datas = new Map();
	
	var promise = $http.get(jsRoutes.controllers.commons.api.PropertyDefinitions.list().url).success(function(data) {
		for(var i=0; i<data.length; i++){
			datas.set(data[i].code,data[i]);
		}
	});
	
	return {
		datas : datas,
		get : function get(type){
			if(datas !== undefined){
				
				return datas.get(type);
			}else
				return null;
		}
	};
	
}]).directive('propertyValue',[function() {
	return {
		restrict : 'EA',
		scope : {
			valueNgModel :'=',
			keyNgModel : '='
		},
		template : '<div ng-if="valueNgModel._type === \'object_list\'">'
						+'<object-list value-ng-model="valueNgModel" key-ng-model="keyNgModel" format-ng-model="table"/>'
					+'</div>'
					+'<div ng-if="valueNgModel._type === \'single\'">'
						+'<single value-ng-model="valueNgModel" key-ng-model="keyNgModel"/>'
					+'</div>'
		};
}]).directive('objectList',[ function() {
	return {
		restrict : 'EA',
		scope : {
			valueNgModel :'=',
			keyNgModel : '=',
			formatNgModel : '=',
		},
			template :'<div ng-if="format === \'line\'" ng-repeat="property in valueNgModel.value">'
						+'<div class="row">'
							+'<label class="col-md-6 col-lg-6 control-label">{{keyNgModel|codes:\'property_definition\'}}</label>'
							+'<div class="col-md-6 col-lg-6">'
							+'<span ng-repeat="(keyProp,valueProp) in property"> <label class="control-label">{{keyProp|codes:\'property_definition.\'+keyNgModel}}</label> : <value value-ng-model=valueProp key-ng-model=keyProp key-prop-def-ng-model=keyNgModel+\'.\'+keyProp/></span>'
							+'</div>'
						+'</div>'
					  +'</div>'
					  +'<div ng-if="format === \'paragraph\'" ng-repeat="property in valueNgModel.value">'
						+'<div class="row">'
							+'<label class="col-md-6 col-lg-6 control-label">{{keyNgModel|codes:\'property_definition\'}}</label>'
							+'<div class="col-md-6 col-lg-6">'
							+'<p ng-repeat="(keyProp,valueProp) in property"> <label class="control-label">{{keyProp|codes:\'property_definition.\'+keyNgModel}}</label> : <value value-ng-model=valueProp key-ng-model=keyProp key-prop-def-ng-model=keyNgModel+\'.\'+keyProp/></p>'
							+'</div>'
						+'</div>'
					  +'</div>'
					  +'<div ng-if="format === \'table\'">'
					  		+'<label class="col-md-6 col-lg-6 control-label">{{keyNgModel|codes:\'property_definition\'}}</label>'
					  		+'<div class="col-md-6 col-lg-6">'
					  		+'<table class="table table-condensed table-hover table-bordered">'
					  		+'<thead>'
					  			+'<tr>'
					  				+'<th ng-repeat="key in keys">{{key|codes:\'property_definition.\'+keyNgModel}}</th>'
					  			+'</tr>'
					  		+'</thead>'
					  		+'<tbody>'
					  			+'<tr ng-repeat="property in valueNgModel.value">'
					  				+'<td ng-repeat="key in keys"><value value-ng-model=property[key] key-ng-model=key key-prop-def-ng-model=keyNgModel+\'.\'+key/></td>'
					  			+'</tr>'
					  		+'</tbody>'
					  		+'</table>'
					  		+'</div>'
					  +'</div>',
			link : function(scope, element, attr){
				scope.format="line";
				if(attr.formatNgModel && attr.formatNgModel !== "line"){
					scope.format = attr.formatNgModel;
				}
				if(scope.format == "table"){
					scope.keys = [];
					for(index=0; index <scope.valueNgModel.value.length; index++){
						scope.keys = scope.keys.concat(Object.keys(scope.valueNgModel.value[index]).filter(function(i){
							return scope.keys.indexOf(i) == -1;
						}));
					}
				}
			}
		};
}]).directive('single',[function() {
	return {
		restrict : 'EA',
		scope : {
			valueNgModel :'=',
			keyNgModel : '='
		},					
		template : '<label class="col-md-6 col-lg-6 control-label" ng-if="valueNgModel.unit !== null"> {{keyNgModel|codes:\'property_definition\'}} ({{valueNgModel.unit}})</label>'
					+'<label class="col-md-6 col-lg-6 control-label" ng-if="valueNgModel.unit === null" ng-bind="keyNgModel|codes:\'property_definition\'"></label>' 
					+'<p class="col-md-6 col-lg-6 form-control-static"><value value-ng-model=valueNgModel.value key-ng-model=keyNgModel key-prop-def-ng-model=keyNgModel/></p>'			
		};
}]).directive('value',['propertyDefinitions', function(propertyDefinitions) {
	return {
		restrict : 'EA',
		scope : {
			valueNgModel :'=',
			//For property type object_list concatenation of parent key and property key
			keyPropDefNgModel : '=',
			//property key
			keyNgModel : '='
		},					
		template :
				'<div ng-if="propertyDefinitions.get(keyPropDefNgModel).choiceInList"><span ng-bind="valueNgModel|codes:\'value.\'+keyPropDefNgModel:false"/></div>'
				+'<div ng-if="!propertyDefinitions.get(keyPropDefNgModel).choiceInList" ng-switch on="propertyDefinitions.get(keyPropDefNgModel).valueType">'
					+'<span ng-switch-when="java.lang.String" ng-bind="valueNgModel|codes:\'value.\'+keyPropDefNgModel:false" />'
					+'<span ng-switch-when="java.lang.Boolean" ng-bind="valueNgModel|codes:\'boolean\':false" />'
					+'<span ng-switch-when="java.util.Date" ng-bind="valueNgModel|date:\''+Messages("date.format")+'\'" />'
					+'<span ng-switch-default ng-bind="valueNgModel|number" >'	
				 +'</div>' ,
		link : function(scope, element, attr){
			scope.propertyDefinitions = propertyDefinitions;
		}
		};
}]);
