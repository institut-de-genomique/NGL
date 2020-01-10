angular.module('home').controller('PulsedFieldElectrophoresisCtrl',['$scope', '$parse','$http',
                                                             function($scope,$parse,$http) {
	
	$scope.dispatchConfiguration.orderBy = "container.sampleCodes";
	
	var config = $scope.atmService.data.getConfig();
	config.name = $scope.experiment.typeCode.toUpperCase();
	config.order.by = "inputContainer.sampleCodes";		
	$scope.atmService.data.setConfig(config );
	
	$scope.$parent.copyPropertiesToInputContainer = function(experiment){
		
		experiment.atomicTransfertMethods.forEach(function(atm){
			var inputContainerUsed =$parse("inputContainerUseds[0]")(atm);
			if(inputContainerUsed){
				var volume1 = $parse("experimentProperties.volume1")(inputContainerUsed);
				if(volume1){
					inputContainerUsed.newVolume = volume1;
					inputContainerUsed.newQuantity = $scope.computeQuantity(inputContainerUsed.concentration, inputContainerUsed.newVolume);
				}else{
					inputContainerUsed.newQuantity = $scope.computeQuantity(inputContainerUsed.concentration, inputContainerUsed.volume);
				}				
			}
				
		});			
	};
	

$scope.$watch("gel",function(imgNew, imgOld){
		if(imgNew){			
			
			angular.forEach($scope.atmService.data.displayResult, function(dr){
				$parse('inputContainerUsed.experimentProperties.electrophoresisGelPhoto').assign(dr.data, this);
			}, imgNew);
			
		}
		angular.element('#importFile1')[0].value = null;
		
	});
	
	$scope.button = {
			isShow:function(){
				return ($scope.isInProgressState() && !$scope.mainService.isEditMode())
				}	
		};
	
	var columns = $scope.atmService.data.getColumnsConfig();
	
	columns.push({
		"header" : Messages("containers.table.sampleTypes"),
		"property" : "inputContainer.contents",
		"filter" : "getArray:'sampleTypeCode' | unique | codes:'type'",
		"order" : false,
		"hide" : true,
		"type" : "text",
		"position" : 7,
		"render" : "<div list-resize='cellValue' list-resize-min-size='3'>",
		"extraHeaders" : {
			0 : Messages("experiments.inputs")
		}

	});
	
	columns.push({
		"header" : Messages("containers.table.concentration"),
		"property": "(inputContainerUsed.concentration.value|number).concat(' '+inputContainerUsed.concentration.unit)",
		"order" : true,
		"edit" : false,
		"hide" : true,
		"type" : "text",
		"position" : 9,
		"extraHeaders" : {
			0 : Messages("experiments.inputs")
		}
	});

	columns.push({
		"header" : Messages("containers.table.volume") + " (µL)",
		"property" : "inputContainerUsed.volume.value",
		"order" : true,
		"edit" : false,
		"hide" : true,
		"type" : "number",
		"position" : 10,
		"extraHeaders" : {
			0 : Messages("experiments.inputs")
		}
	});
	

	
	columns.push({
		"header" : Messages("containers.table.libraryToDo"),
		"property" : "inputContainerUsed.contents",
		"filter" : "getArray:'processProperties.libraryToDo.value' | unique ",
		"order" : true,
		"edit" : false,
		"hide" : true,
		"type" : "text",
		"position" : 10.1,
		"extraHeaders" : {
			0 : Messages("experiments.inputs")
		}
	});
	
	

	
	$scope.atmService.data.setColumnsConfig(columns);

	
}]);