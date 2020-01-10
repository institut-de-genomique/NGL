//If the value of the passed model is an Array, this directive collapse all the value in order to show ngItemMin value
//and add a button to deploy and see all the values
//A title is placed on this button, so that the user can see all the value when the pointer hovering on it
//If the value is not an Array, this directive add a span with ng-bind="value"
//Example: <div list-resize="getSampleAndTags(flowcell)" list-resize-min-size="5" [below,below-only-deploy] [vertical]></div>
angular.module('commonsServices').directive('listResize',['$parse', function($parse) {
	return {
		template:'<div ng-if="isArray" style="display:inherit"><span ng-repeat="v in listValue | limitTo:nbItem track by $index">{{v}}{{getSeparator($last)}}<br ng-if="vertical && !$last"><br ng-if="below && $last"></span>'+
		'<button title="{{getFullList(listValue)}}" ng-show="!isDeployed && nbItemMax>nbItemMin" ng-click="switchDeploy()" class="small-litte-icone-transparent"> <i><b>...</b></i> </button>'+
		'<br ng-if="belowOnlyDeployed">'+
		'<button ng-show="isDeployed" ng-click="switchDeploy()" class="small-litte-icone-transparent "> <i class="fa fa-minus-square-o"></i> </button></div>'+
		'<span ng-if="!isArray" ng-bind="listValue"></span>',
		scope:true,
		restrict: 'A',
		link: function(scope, element, attrs, ngModel) {
			var REPEAT_REGEXP = /^\s*(.+)\s+in\s+(.*?)\s*(\s+track\s+by\s+(.+)\s*)?$/;//The regex used in ng-repeat
			scope.nbItemMin = 1;//The size of the list that not need the button, and the init size when displayed
			scope.nbItemMax = 10;//The size of the listValue, default 10
			scope.nbItem = scope.nbItemMin;//The current nbItem displayed by the directive
			scope.isDeployed = false;//false: we display minValue of the values to the user, True we display all to the user
			scope.isArray = false;//Indicate if the value is an Array, if not we just show the span
			scope.listValue = [];//The full list of values or a single value if the model value is not an array
			scope.vertical = false;//true: display values vertically
			scope.below = false;//true: display the button below the value list
			scope.belowOnlyDeployed = false;//true: display the undeploy-button only below the value list


			//The user can add list-resize-min-size attribute with the directive to set a custom min size
			//for the collapse list
			if(attrs.listResizeMinSize !== undefined){
				scope.nbItemMin = attrs.listResizeMinSize;//Set the min size
				scope.nbItem = scope.nbItemMin;//Refresh the current size
			}

			if(attrs.listResize !== undefined && attrs.listResize !== ""){
				var model = attrs.listResize;

				var match = model.match(REPEAT_REGEXP);
				//Because we want to extract the model if the value passed to the directive is a ng-repeat loop
				if(match !== null && match !== undefined && match.length>1){
					model = $parse(match[2]);
				}

				//The user can add  the below attribute for place the deploy/undeploy-button below the list of values.	
				if(attrs.below !== undefined){
					scope.below = true;
				}			

				//The user can add  the below-only-deploy attribute for place the undeploy-button only below the list of values.
				if(attrs.belowOnlyDeploy !== undefined){
					scope.belowOnlyDeployed = true;
				}


				//The user can add  the vertical attribute to set the list of values vertically, with a carriage return behind each item.
				if(attrs.vertical !== undefined){
					scope.vertical = true;
				}

				//We watch the model in order to extract the list/value
				scope.$watch(model, function(newValue, oldValue){
					if(angular.isArray(newValue)){
						scope.isArray = true;//Use in the selection of the template
						scope.nbItemMax = newValue.length;
					}
					if(newValue != undefined && newValue != null){
						scope.listValue = newValue;
					}else{
						scope.listValue = [];
					}
					
				}, true);

				//This function collpase or deploy
				scope.switchDeploy = function(){
					if(scope.isDeployed === false){
						scope.isDeployed = true;//deploy
						scope.nbItem = scope.nbItemMax;
					}else{
						scope.isDeployed = false;//collapse
						scope.nbItem = scope.nbItemMin;
					}
				};

				//The list shown to the user need separators
				scope.getSeparator = function(isLast){
					if(!isLast){//Not deplayed when we show the last element
						return ', ';
					}
					if(scope.isDeployed === true){
						return '\u0020' ;
					}
					return '';
				};

				//This function get the full list and turn it to readable text for the user
				//We need it for the title on the button
				scope.getFullList = function(list){
					var text = '';
					for(var i=0;i<list.length;i++){
						text += list[i] + scope.getSeparator((i===(list.length-1)));
					}
					return text;
				};
			}else{
				throw "Missing model in listResize";
			}
		}
	};
}]);