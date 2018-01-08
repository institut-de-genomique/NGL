angular.module('home').controller('OneToVoidQPCRQuantificationCNSCtrl',['$scope', '$parse','$http',
                                                             function($scope,$parse,$http) {
	
	
	$scope.$parent.copyPropertiesToInputContainer = function(experiment){
		
		experiment.atomicTransfertMethods.forEach(function(atm){
			var inputContainerUsed =$parse("inputContainerUseds[0]")(atm);
			if(inputContainerUsed){
				var concentration1 = $parse("experimentProperties.concentration1")(inputContainerUsed);
				if(concentration1){
					inputContainerUsed.concentration = concentration1;
				}
			}
			
		});			
	};
	
	
	var importData = function(){
		$scope.messages.clear();
		
		$http.post(jsRoutes.controllers.instruments.io.IO.importFile($scope.experiment.code).url, $scope.file)
		.success(function(data, status, headers, config) {
			$scope.messages.clazz="alert alert-success";
			$scope.messages.text=Messages('experiments.msg.import.success');
			$scope.messages.showDetails = false;
			$scope.messages.open();	
			//only atm because we cannot override directly experiment on scope.parent
			$scope.experiment.atomicTransfertMethods = data.atomicTransfertMethods;
			$scope.file = undefined;
			angular.element('#importFile')[0].value = null;
			$scope.$emit('refresh');
			
		})
		.error(function(data, status, headers, config) {
			$scope.messages.clazz = "alert alert-danger";
			$scope.messages.text = Messages('experiments.msg.import.error');
			$scope.messages.setDetails(data);
			$scope.messages.open();	
			$scope.file = undefined;
			angular.element('#importFile')[0].value = null;
		});
	};
	
	$scope.button = {
		isShow:function(){
			return ($scope.isInProgressState() && !$scope.mainService.isEditMode())
			},
		isFileSet:function(){
			return ($scope.file === undefined)?"disabled":"";
		},
		click:importData,		
	};
	
	var columns = $scope.atmService.data.getColumnsConfig();
	columns.push({
		"header":Messages("containers.table.concentration"),
		"property": "inputContainer.concentration",
		"render":"<span ng-bind='cellValue.value|number'/> <span ng-bind='cellValue.unit'/>",
		"order":false,
		"hide":true,
		"type":"text",
		"position":10,
		"extraHeaders":{0:Messages("experiments.inputs")}			 						 			
	});
	columns.push({
		"header":Messages("containers.table.size"),
		"property": "inputContainer.size.value",
		"order":false,
		"hide":true,
		"type":"text",
		"position":10,
		"extraHeaders":{0:Messages("experiments.inputs")}			 						 			
	});
	$scope.atmService.data.setColumnsConfig(columns);
	
}]);