angular.module('home').controller('OneToVoidQcardCNSCtrl',['$scope', '$parse','$http',
                                                             function($scope,$parse,$http) {
	
	// NGL-1055: surcharger la variable "name" definie dans le controleur parent ( one-to-void-qc-ctrl.js) => nom de fichier CSV exporté 
	var config = $scope.atmService.data.getConfig();
	config.name = $scope.experiment.typeCode.toUpperCase();
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
	
	$scope.button = {
			isShow:function(){
				return ($scope.isInProgressState() && !$scope.mainService.isEditMode())
				}	
		};
	
	var columns = $scope.atmService.data.getColumnsConfig();
	
	columns.push({
		"header" : Messages("containers.table.libraryToDo"),
		"property": "inputContainer.contents",
		"filter": "getArray:'processProperties.libraryToDo.value'| unique",
		"order" : false,
		"hide" : true,
		"type" : "text",
		"position" : 7.1,
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
		"position" : 7.05,
		"extraHeaders" : {
			0 : Messages("experiments.inputs")
		}
	});
	
	$scope.atmService.data.setColumnsConfig(columns);

	
}]);