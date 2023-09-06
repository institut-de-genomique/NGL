angular.module('commonsServices').directive('btInput', [ '$parse', '$filter', function($parse, $filter) {
	return {
		restrict : 'EA',
		scope : {
			inputNgModel :'=',
			textareaNgModel : '=',
			textareaPlaceholderNgModel : '@'
		},
		template : '<div>'
			+ '<div class="dropdown" ng-show="isTextarea()">' //don't use nf-if scope conflict; see angular documentation
			+ '<div class="input-group">'
			+ '<div class="input-group-btn">'
			//textarea mode
			+ '<button tabindex="-1" data-toggle="dropdown" class="btn btn-default btn-xs dropdown-toggle" type="button" ng-disabled="isDisabled()" ng-click="open()">' 
			+ '  <i ng-if="textareaValue !== undefined" class="fa fa-list-ul fa-rotate-90"></i><i ng-if="textareaValue === undefined" class="fa fa-list-ul"></i>' 
			+ '</button>' 
			+ '<ul class="dropdown-menu dropdown-menu-left"  role="menu">' 
			+ '  <li>' 
			// FDS 28/10/2020 NGL-3148 le copier/coller à la souris ne marche pas=> ajouter ng-paste=setTextareaNgModelFromPaste
			//+ '<textarea ng-class="inputClass" ng-model="textareaValue" ng-keydown="intercept($event)" ng-keyup="setTextareaNgModel()" rows="5" ></textarea>' 
			//   peut etre plus propre/simple de passer par ng-change comme dans btSelect mais trop de chgts a faire...
			// FDS NGL-4049 ajout placeholder parametrable.
			+'   <textarea placeholder="{{textareaPlaceholderNgModel}}" ng-class="inputClass" ng-model="textareaValue" ng-keydown="intercept($event)" ng-keyup="setTextareaNgModel()" rows="5" ng-paste="setTextareaNgModelFromPaste($event)" ></textarea>' 
			+ '  </li>' 
			+ '</ul>' 
			+ '</div>'
			//select mode
			+ '<input type="text" ng-class="inputClass" ng-model="inputNgModel" placeholder="{{placeholder}}" title="{{placeholder}}"/>' 
			+ '</div>' 
			+ '</div>'
			+ '<input type="text" ng-class="inputClass" ng-model="inputNgModel" placeholder="{{placeholder}}" title="{{placeholder}}" ng-hide="isTextarea()"/>' 
			+ '</div>' ,
		require : [ '?inputNgModel' ],
		link : function(scope, element, attr, ctrl) {
			scope.inputClass = element.attr("class");
			scope.placeholder = attr.placeholder;
			scope.textareaPlaceholderNgModel = attr.textareaPlaceholderNgModel;
			
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
					scope.textareaValue = scope.textareaValue+'\n';
					event.preventDefault();
				}
				scope.setTextareaNgModel();
			};
			
			scope.isTextarea = function() {
				return textarea;
			};
			
			// FDS 28/10/2020 NGL-3148: utiliser $event.originalEvent.clipboardData.getData('text/plain') pour récupérer le clipboard
			scope.setTextareaNgModelFromPaste = function($event){
				//29/03/2021 NGL-3148 suite....ajout trim() 
				//           car s'il existe un espace final le dernier élément n'est pas vu, s'il existe un espace initial le premier élément n'est pas vu !!!
				var values = $event.originalEvent.clipboardData.getData('text/plain').trim().split(/\s*[,;\n\t]\s*/);

				scope.textareaNgModel = values;
			}
			
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
