angular.module('propertyDefServices', [])
.factory('propertyDefHelpers', ['$http', '$parse', '$q', 'mainService', 
                                         function($http, $parse, $q, mainService){
	var constructor =  {
			getDisplayUnitFromProperty : function(propertyDefinition){
				var unit = $parse("displayMeasureValue.value")(propertyDefinition);
				if(undefined !== unit && null !== unit) return " ("+unit+")";
				else return "";
			},
			
			getPropertyColumnType : function(type){
				if(type === "java.lang.String"){
					return "text";
				}else if(type === "java.lang.Double" || type === "java.lang.Integer" || type === "java.lang.Long"){
					return "number";
				}else if(type === "java.util.Date"){
					return "date";
				}else if(type ==="java.io.File"){
					return "file";
				}else if(type ==="java.awt.Image"){
					return "img";
				}else if(type ==="java.lang.Boolean"){
					return "boolean";	
				}else{
					throw 'not manage : '+type;
				}

				return type;
			},
			
			getProcessUDTColumn : function(propertyDefinition){
				var column = {};
				column.watch=true;
				column.header = propertyDefinition.name + this.getDisplayUnitFromProperty(propertyDefinition);
				column.required=propertyDefinition.required;
				    				
				column.property = "properties."+propertyDefinition.code+".value";
				column.edit = (mainService.getHomePage() === 'state')?false:propertyDefinition.editable;
				column.type = this.getPropertyColumnType(propertyDefinition.valueType);
				column.choiceInList = propertyDefinition.choiceInList;
				column.position = (5+(propertyDefinition.displayOrder/1000));
				column.defaultValues = propertyDefinition.defaultValue;
				column.format = propertyDefinition.displayFormat;
				column.order=true;
				
				if(column.choiceInList){
					if(propertyDefinition.possibleValues.length > 100){
						column.editTemplate='<input class="form-control" type="text" #ng-model typeahead="v.code as v.name for v in col.possibleValues | filter:$viewValue | limitTo:20" typeahead-min-length="1" udt-change="updatePropertyFromUDT(value,col)"/>';        					
					}else{
						column.listStyle = "bt-select";
					}
					column.possibleValues = propertyDefinition.possibleValues; 
					column.filter = "codes:'value."+propertyDefinition.code+"'";    					
				}
				
				if(propertyDefinition.displayMeasureValue != undefined && propertyDefinition.displayMeasureValue != null){
					column.convertValue = {"active":true, "displayMeasureValue":propertyDefinition.displayMeasureValue.value, 
							"saveMeasureValue":propertyDefinition.saveMeasureValue.value};
				}
				
				column.groupMethod = "collect:true"; //par defaut pour toutes les propriétés
				return column;
			},
			getHtmlFilter : function(pDef, serviceName){
				var html = null;
				if(pDef.choiceInList){
					html = "<div class='form-control' multiple=true bt-select ng-model='"+serviceName+".form[\"properties["+pDef.code+"]\"]' placeholder=\""+pDef.name+"\" bt-options='possibleValues.code as possibleValues.name for possibleValues in "+serviceName+".lists.getValues({propertyDefinitionCode:\""+pDef.code+"\"},\""+pDef.code+"\")'></div>";
				}else{
					html = "<input type='text' class='form-control' ng-model='"+serviceName+".form[\"properties["+pDef.code+"]\"]' placeholder=\""+pDef.name+"\" title=\""+pDef.name+"\">"; 				             
				}						
				return {
					html:html,
					position:pDef.displayOrder
				};
			}
	};
	

	return constructor;
}]);