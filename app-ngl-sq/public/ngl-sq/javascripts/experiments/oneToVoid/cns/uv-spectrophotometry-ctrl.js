angular.module('home').controller('OneToVoidUvSpectrophotometryCNSCtrl',['$scope', '$parse','$http',
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
				var concentration1 = $parse("experimentProperties.concentration1")(inputContainerUsed);
				if(volume1){
					inputContainerUsed.newVolume = volume1;
				}
				
				if($scope.experiment.experimentProperties.copyConcentration.value === true){
					console.log("copy concentration and quantity");
				
					if(concentration1){
						inputContainerUsed.newConcentration = concentration1;
					}
					inputContainerUsed.newQuantity =  $scope.computeQuantity(
							(concentration1)?inputContainerUsed.newConcentration:inputContainerUsed.concentration, 
							(volume1)?inputContainerUsed.newVolume:inputContainerUsed.volume);
				}else{
					console.log("not copy concentration and quantity");
					inputContainerUsed.newConcentration = null;
					inputContainerUsed.newQuantity = $scope.computeQuantity(
									inputContainerUsed.concentration, 
									(volume1)?inputContainerUsed.newVolume:inputContainerUsed.volume); 
				}
			}			
		});			
	};
	
	$scope.button = {
			isShow:function(){
				return ($scope.isInProgressState() && !$scope.mainService.isEditMode())
				}	
		};
	
	
	$scope.updatePropertyFromUDT = function(value, col){
		console.log("update from property : "+col.property);
		
		if(col.property === 'inputContainerUsed.experimentProperties.dilutionFactor.value'){
			computeConcentration1(value.data);
		}else if(col.property === 'inputContainerUsed.experimentProperties.concentration0.value'){
			computeConcentration1(value.data);
		}
		
	}
	
	var computeConcentration1 = function(udtData){
		var getter = $parse("inputContainerUsed.experimentProperties.concentration1.value");
		var concentration1 = getter(udtData);
		var compute = {
				conc1 : $parse("inputContainerUsed.experimentProperties.concentration0.value")(udtData),
				dilution1 :  (($parse("inputContainerUsed.experimentProperties.dilutionFactor.value")(udtData)).indexOf("1/") ==0 ? ($parse("inputContainerUsed.experimentProperties.dilutionFactor.value")(udtData)).substring(2) : undefined ) ,
				isReady:function(){
					return (this.conc1 && this.dilution1);
				}
			};
		
		if(compute.isReady()){
			
			var result = $parse("(conc1 * dilution1)")(compute);
			console.log("result = "+result);
			if(angular.isNumber(result) && !isNaN(result)){
				concentration1 = Math.round(result*10)/10;				
			}else{
				concentration1 = undefined;
			}	
			getter.assign(udtData, concentration1);
		}else{
			getter.assign(udtData, undefined);
			console.log("not ready to computeConcentration1");
		}
		
	}
	
	
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