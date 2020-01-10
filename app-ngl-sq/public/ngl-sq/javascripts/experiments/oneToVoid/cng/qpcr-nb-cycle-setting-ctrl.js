angular.module('home').controller('OneToVoidQPCRNbCycleSettingCtrl',['$scope', '$parse','$http',
                                                             function($scope,$parse,$http) {
	
	var config = $scope.atmService.data.getConfig();
	config.name = $scope.experiment.typeCode.toUpperCase();
	$scope.atmService.data.setConfig(config );	
	

	//nécessaire meme si rien a faire !!! appelle dans onSave du controler parent
	$scope.$parent.copyPropertiesToInputContainer = function(experiment){
		//NOP
	};

	// EN TEST: ajout pour import profil d'Amplification (copier dans gel-migration-ctrl.js du CNS....)
	$scope.$watch("profil",function(imgNew, imgOld){
		if(imgNew){			
			
			angular.forEach($scope.atmService.data.displayResult, function(dr){
				// PB.... on a pas de profil de nveau experiment !!!!!
				$parse('inputContainerUsed.experimentProperties.amplificationProfile').assign(dr.data, this);
			}, imgNew);
			
		}
		angular.element('#importFile')[0].value = null;
	});
	
	$scope.button = {
			isShow:function(){
				return ($scope.isInProgressState() && !$scope.mainService.isEditMode())
			}	
	};

	var columns = $scope.atmService.data.getColumnsConfig();
	
	columns.push({
    	"header":"Code aliquot",
		"property": "inputContainer.contents",
		"filter": "getArray:'properties.sampleAliquoteCode.value'| unique",
		"order":false,
		"hide":true,
		"type":"text",
		"position":7.5,
		"render": "<div list-resize='cellValue' list-resize-min-size='3'>",
		"extraHeaders":{0:Messages("experiments.inputs")}
	});
	columns.push({
		"header": Messages("containers.table.libProcessType"),
		"property" : "inputContainerUsed.contents",
		"filter" : "getArray:'properties.libProcessTypeCode.value' | unique | codes:'value'",
		"order" : false,
		"hide" : true,
		"type" : "text",
		"position" : 9,
		"render" : "<div list-resize='cellValue' list-resize-min-size='3'>",
		"extraHeaders" : {0 : Messages("experiments.inputs")}
	});	

	$scope.atmService.data.setColumnsConfig(columns);
	
}]);