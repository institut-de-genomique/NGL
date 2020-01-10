

angular.module('home').controller('ReportCtrl', ['$scope', '$http', '$filter', function($scope, $http, $filter) {
	
	var convertJavaValueTypeToJSValueType = function(valueType){
		switch(valueType) {
			case 'java.lang.String':
				valueType = 'text';
				break;
			case 'java.lang.Integer':
				valueType = 'number';
				break;
			case 'java.lang.Double':
				valueType = 'number';
				break;
			case 'java.lang.Float':
				valueType = 'number';
				break;
			case 'java.lang.Long':
				valueType = 'number';
				break;
			case 'java.lang.Date':
				valueType = 'date';
				break;
			case 'java.lang.Boolean':
				valueType = 'boolean';
				break;
			case 'java.awt.Image':
				valueType = 'img';
				break;
			case 'java.io.File':
				valueType = 'file';
				break;			
			default:
				throw 'not managed :'+valueType;
		}
		return valueType;
	};
	var convertPropertyValueTypeToType = function(propertyValueType){
		switch(propertyValueType) {
			case 'single':
				propertyValueType = 'single';
				break;
			case 'list':
				propertyValueType = 'list';
				break;
			case 'file':
				propertyValueType = 'single';
				break;
			case 'img':
				propertyValueType = 'single';
				break;
			case 'map':
				propertyValueType = 'map';
				break;
			case 'object':
				propertyValueType = 'single';
				break;
			case 'object_list':
				propertyValueType = 'list';
				break;
			default:
				throw 'not managed :'+propertyValueType;
		}
		return propertyValueType;
	};
	var convertPropertyValueTypeToIsObject = function(propertyValueType){
		switch(propertyValueType) {
			case 'single':
				propertyValueType = false;
				break;
			case 'list':
				propertyValueType = false;
				break;
			case 'file':
				propertyValueType = false;
				break;
			case 'img':
				propertyValueType = false;
				break;
			case 'map':
				propertyValueType = false;
				break;
			case 'object':
				propertyValueType = true;
				break;
			case 'object_list':
				propertyValueType = true;
				break;
			default:
				throw 'not managed :'+propertyValueType;
		}
		return propertyValueType;
	};
	/**
	 * Convert propertyDefinition in property and filter with level
	 */
	var getProperties = function(treatmentType, level){
		var properties = {};
		var propertiesDef = $filter('filter')(treatmentType.propertiesDefinitions, level)
		
		angular.forEach(propertiesDef, function(value, key){
			if(!this[value.code.split(".")[0]]){
				this[value.code.split(".")[0]] = {code:value.code.split(".")[0], name:value.name.split(".")[0], levels:value.levels,
						type:convertPropertyValueTypeToType(value.propertyValueType), isObject:convertPropertyValueTypeToIsObject(value.propertyValueType), subProperties:[]};
			}
			this[value.code.split(".")[0]].subProperties.push({code:value.code, name:value.name, 
				format:value.displayFormat,	valueType:convertJavaValueTypeToJSValueType(value.valueType)});
			
		}, properties);
		
		var propertiesA = [];
		
		for(var key in properties){
			propertiesA.push(properties[key]);
		}
		
		return propertiesA;
	};
	
	var init = function(){
		$http.get(jsRoutes.controllers.treatmenttypes.api.TreatmentTypes.list().url, {params:{levels: "ReadSet"}}).success(function(data) {
			var treatmentTypes = [];
			angular.forEach(data, function(value, key){
				var properties = getProperties(value, "ReadSet");
				var names = value.names.split(',');
				var orders = value.displayOrders.split(',');
				for(var i = 0 ; i < names.length; i++){
					var contexts = [];
					for(var j = 0; j < value.contexts.length ; j++){
						contexts.push({code:value.contexts[j].code, properties:$filter('filter')(properties, value.contexts[j].code)})
					}
					this.push({code:value.code, name:value.name, instanceCode:names[i], 
						displayOrder:Number(orders[i]), contexts:contexts })	
				}
							
			}, treatmentTypes);
			$scope.treatmentTypes = treatmentTypes;			
		});
		
		
	};
	
	init();
	
}]);

