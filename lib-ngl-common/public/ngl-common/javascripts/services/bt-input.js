angular.module('commonsServices').directive('btInput', [ '$parse', '$filter', function($parse, $filter) {
	return {
		restrict : 'EA',
		scope : {
			inputNgModel :'=',
			textareaNgModel : '='
		},
		template : '<div>'
			+ '<div class="dropdown" ng-show="isTextarea()">' //not used nf-if scope conflict see angular documentation
			+ '<div class="input-group">'
			+ '<div class="input-group-btn">'
			//textarea mode
			+ '<button tabindex="-1" data-toggle="dropdown" class="btn btn-default btn-xs dropdown-toggle" type="button" ng-disabled="isDisabled()" ng-click="open()">' 
			+ '<i ng-if="textareaValue !== undefined" class="fa fa-list-ul fa-rotate-90"></i><i ng-if="textareaValue === undefined" class="fa fa-list-ul"></i>' + '</button>' + '<ul class="dropdown-menu dropdown-menu-left"  role="menu">' 
			+ '<li>' 
			+ '<textarea ng-class="inputClass" ng-model="textareaValue" ng-keydown="intercept($event)" ng-keyup="setTextareaNgModel()" rows="5" ></textarea>' 
			+ '</li>' 
			+ '</ul>' 
			+ '</div>'
			//select mode
			+ '<input type="text" ng-class="inputClass" ng-model="inputNgModel" placeholder="{{placeholder}}" title="{{placeholder}}"/>' 
			+ '</div>' 
			+ '</div>'
			+ '<input type="text" ng-class="inputClass" ng-model="inputNgModel" placeholder="{{placeholder}}" title="{{placeholder}}" ng-hide="isTextarea()"/>' 
			+ '<div>' ,
		require : [ '?inputNgModel' ],
		link : function(scope, element, attr, ctrl) {
			scope.inputClass = element.attr("class");
			scope.placeholder = attr.placeholder;
			
			element.attr("class", ''); //remove custom class

			var textarea = false;
			if(attr.textareaNgModel){
				if(Array.isArray(scope.textareaNgModel)){
					scope.textareaValue = scope.textareaNgModel.toString().replace(/,/g,",\n");
				}else{
					scope.textareaValue = undefined;					
				}
				textarea = true;
				
				scope.$parent.$watch(attr.textareaNgModel, function(newValue, oldValue){
			    	  if(newValue === undefined || newValue === null){
			    		  scope.textareaValue = undefined;      		    				    		   		    		
			    	  }
			      });
			}
			
			var ngFocus = attr.ngFocus;
			
			scope.intercept = function(event){
				if(event.keyCode === 9){ //tab event
					scope.textareaValue = scope.textareaValue+'\n'
					event.preventDefault();
				}
				//scope.setTextareaNgModel()				
			};
			
			scope.isTextarea = function() {
				return textarea;
			};
			
			scope.setTextareaNgModel = function(){
				var values = scope.textareaValue.split(/\s*[,;\n\t]\s*/);
				scope.textareaNgModel = values;
			}
			
			scope.open = function() {
				if (ngFocus) {
					$parse(ngFocus)(scope);
				}
			};

			scope.isDisabled = function() {
				return (attr.ngDisabled) ? scope.$parent.$eval(attr.ngDisabled) : false;
			};

			scope.getMessage = function(value) {
				return Messages(value);
			};
			
			
		}
	};
}]);