//This service and directive are a complete system of drag'n drop. This system drag and drop not only the html element
//but also the data of the ngModel where the draggable directive is added in the droppable ngModel
//The service is use to share the data between the two directives (draggable, droppable)
angular.module('dragndropServices', []).factory('dragndropService', function($rootScope) {
	dragndropService = {
			draggedData : {},
			dragModel:undefined,
			setDraggedData : function(data){
				this.draggedData = data;
			},
			getDraggedData : function(){
				return this.draggedData;
			},
			setDragModel : function(model){
				this.dragModel = model;
			},
			getDragModel :function(){
				return this.dragModel;
			}
	}

	return dragndropService;
}).directive('draggable',['dragndropService', '$parse', function(dragndropService, $parse) {
	return{ 
		scope:{
			drag:'&',
			ngModel:'='
		},
		link: function(scope, element, attrs) {
			var OPTIONS_REGEXP = /^\s*(.*?)(?:\s+as\s+(.*?))?(?:\s+group\s+by\s+(.*))?\s+for\s+(?:([\$\w][\$\w\d]*)|(?:\(\s*([\$\w][\$\w\d]*)\s*,\s*([\$\w][\$\w\d]*)\s*\)))\s+in\s+(.*)$/;
			var REPEAT_REGEXP = /^\s*(.+)\s+in\s+(.*?)\s*\|(.*)\s*(\s+track\s+by\s+(.+)\s*)?$|^\s*(.+)\s+in\s+(.*?)\s*(.*)\s*(\s+track\s+by\s+(.+)\s*)?$/;
			var el = element[0];
			//This function can extract the model from an ng-repeat or ng-options tag
			var getModel = function(){
				if(attrs.ngRepeat){
					var model = attrs.ngRepeat;
					var match = model.match(REPEAT_REGEXP);
					var result = match[8];
					if(match[2] !== undefined){
						result =  match[2];
					}
					return result;
				}else if(attrs.ngOptions){
					var model = attrs.ngOptions;
					var match = model.match(OPTIONS_REGEXP);
					return match[7];
				}

				return "";
			};
			
			var doOnDrag = function(bool){
				scope.$apply(function(scope) {
					var dragFn = scope.drag();
					if (angular.isDefined(dragFn) && angular.isFunction(dragFn)) {
						dragFn(bool);
					}
				});				
			};
			
			element.addClass('draggable'); 
			el.draggable = true;
			
			//We want all the inputs to keep they default highlight method
			var inputs = el.querySelectorAll("input");
			for(var i=0;i<inputs.length;i++){
				inputs[i].addEventListener('focus', function(e) {
					el.draggable = false;
		        });
				
		        inputs[i].addEventListener('blur', function(e) {
		        	el.draggable = true;
		        });
			}
			
			inputs = undefined;//free the inputs array
			
			el.addEventListener(
					'dragstart',
					function(e) {
						doOnDrag(true);
						e.dataTransfer.effectAllowed = 'move';
						e.dataTransfer.setData('Text', this.id);// Angular internal system
						var model = getModel();
						e.dataTransfer.setData('Model', model);	
						
						dragndropService.setDraggedData(scope.ngModel);
						dragndropService.setDragModel($parse(model)(scope.$parent));
						
						this.classList.add('drag');							
						return false;
					},
					false
			);

			el.addEventListener(
					'dragend',
					function(e) {
						doOnDrag(false);
						this.classList.remove('drag');
						return false;
					},
					false
			);
		}
	}}]).directive('droppable', ['dragndropService','$filter','$parse', function(dragndropService,$filter,$parse) {
		return {
			scope: {
				drop: '&', // parent
				model: '=ngModel',
				dropFn: '=dropFn',
				beforeDropFn: '=beforeDropFn'
			},
			link: function(scope, element, attrs) {
				var el = element[0];

				el.addEventListener(
						'dragover',
						function(e) {
							e.dataTransfer.dropEffect = 'move';
							//Allows us to drop
							if (e.preventDefault) e.preventDefault();
							this.classList.add('over');
							return false;
						},
						false
				);

				el.addEventListener(
						'dragenter',
						function(e) {
							this.classList.add('over');
							return false;
						},
						false
				);

				el.addEventListener(
						'dragleave',
						function(e) {
							this.classList.remove('over');
							return false;
						},
						false
				);

				el.addEventListener(
						'drop',
						function(e) {
							// Stops some browsers from redirecting.
							if (e.stopPropagation) e.stopPropagation();
							this.classList.remove('over');

							var draggedData = dragndropService.getDraggedData(); 
							var fromDragModel = dragndropService.getDragModel(); 
							//var test = e.dataTransfer.getData('Model');
							//push the data to the model and call the drop callback function
							scope.$apply(function(scope) {
								//var test = scope.dropFn;
								//We check that the data is not already in the model
								var alreadyInTheModel = (angular.isArray(scope.model) && scope.model.indexOf(draggedData) !== -1);
								var beforeDropDataFn = scope.$parent.beforeDropData || scope.beforeDropFn;
									
								if (!angular.isUndefined(beforeDropDataFn) && angular.isFunction(beforeDropDataFn)) {
									draggedData = beforeDropDataFn(e, draggedData, attrs.ngModel, alreadyInTheModel);
								}

								if(angular.isArray(scope.model) && !alreadyInTheModel){
									scope.model.push(draggedData);
									var indexOf = fromDragModel.indexOf(draggedData);
									if(angular.isArray(fromDragModel) && indexOf !== -1){
										fromDragModel.splice(indexOf, 1);
									}
								}
								
								
								var dropFn = scope.$parent.drop || scope.dropFn;
								if (!angular.isUndefined(dropFn) && angular.isFunction(dropFn)) {
									dropFn(e, draggedData, scope.model, alreadyInTheModel,  dragndropService.getDragModel());
								}
							});

							return false;
						},
						false
				);
			}
		}
	}]);