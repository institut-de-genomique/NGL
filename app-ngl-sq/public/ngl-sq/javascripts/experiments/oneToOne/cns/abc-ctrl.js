angular.module('home').controller('ABCCtrl', ['$scope', '$parse', 'atmToSingleDatatable',
                                                    function($scope, $parse, atmToSingleDatatable){

	var datatableConfig = {
		name: $scope.experiment.typeCode.toUpperCase(),
		columns: [
		// Colonnes pour le container en entrée.
		{
			 "header":Messages("containers.table.container"),
			 "property":"inputContainer.code",
			 "order":true,
			 "edit":false,
			 "hide":true,
			 "type":"text",
			 "position":1,
			 "extraHeaders":{0:Messages("experiments.inputs")}
		}, {
			 "header":Messages("containers.table.projectCodes"),
			 "property": "inputContainer.projectCodes",
			 "order":true,
			 "hide":true,
			 "type":"text",
			 "position":2,
			 "render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			 "extraHeaders":{0:Messages("experiments.inputs")}
		}, {
			 "header":Messages("containers.table.sampleCodes"),
			 "property": "inputContainer.sampleCodes",
			 "order":true,
			 "hide":true,
			 "type":"text",
			 "position":3,
			 "render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			 "extraHeaders":{0:Messages("experiments.inputs")}
		}, {
			 "header":Messages("containers.table.fromTransformationTypeCodes"),
			 "property":"inputContainer.fromTransformationTypeCodes",
			 "filter":"unique | codes:'type'",
			 "order":true,
			 "edit":false,
			 "hide":true,
			 "type":"text",
			 "render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			 "position":4,
			 "extraHeaders":{0:Messages("experiments.inputs")}
		}, {
			"header":Messages("containers.table.tags"),
			 "property": "inputContainer.contents",
			 "filter": "getArray:'properties.tag.value'| unique",
			 "order":true,
			 "hide":true,
			 "type":"text",
			 "position":4.5,
			 "render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			 "extraHeaders":{0:Messages("experiments.inputs")}
		}, {
			"header":Messages("containers.table.storageCodeIn"),			        	 
			"property":"inputContainer.support.storageCode",
			"type":"text",
			"order":true,
			"hide":true,
			"position":4.6,
			"extraHeaders":{0:Messages("experiments.inputs")}
		},
		{
			"header":Messages("containers.table.concentration") + " (ng/µl)",
			"property":"inputContainerUsed.concentration.value",
			"order":true,
			"edit":false,
			"hide":true,
			"type":"number",
			"position":5,
			"extraHeaders":{0:Messages("experiments.inputs")}
		}, {
			"header":function(){return Messages("containers.table.volume") + " (µl)"},
			"property":"inputContainerUsed.volume.value",
			"order":true,
			"edit":false,
			"hide":true,
			"type":"number",
			"position":6,
			"extraHeaders":{0:Messages("experiments.inputs")}
		}, {
			"header":Messages("containers.table.quantity")+ " (ng)",
			"property":"inputContainerUsed.quantity.value",
			"order":true,
			"editDirectives":" udt-change='updatePropertyFromUDT(value,col)' ",
			"edit":false,
			"hide":true,
			"type":"number",
			"position":7,
			"extraHeaders": { 0: Messages("experiments.inputs") }
		}, {
			"header":Messages("containers.table.size")+ " (pb)",
			"property": "inputContainerUsed.size.value",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "number",
			"position" :8,
			"extraHeaders" : { 0 : Messages("experiments.inputs") }
		}, {
			"header":Messages("containers.table.state.code"),
			"property":"inputContainerUsed.state.code",
			"order":true,
			"edit":false,
			"hide":true,
			"type":"text",
			"filter":"codes:'state'",
			"position":9,
			"extraHeaders": { 0: Messages("experiments.inputs") }
		}, 
		// Colonnes pour le container de sortie.
		{
			"header":function(){return Messages("containers.table.finalVolume") + " (µl)"},
			"property":"outputContainerUsed.volume.value",
			"order":true,
			"edit":true,
			"hide":true,
			"type":"number",
			"position":399,
			"extraHeaders":{0:Messages("experiments.outputs")}
		},
		{
			"header":Messages("containers.table.code"),
			"property":"outputContainerUsed.code",
			"order":true,
			"edit":false,
			"hide":true,
			"type":"text",
			"position":400,
			"extraHeaders":{0:Messages("experiments.outputs")}
		}, {
			"header":Messages("containers.table.stateCode"),
			"property":"outputContainer.state.code | codes:'state'",
			"order":true,
			"edit":false,
			"hide":true,
			"type":"text",
			"position":500,
			"extraHeaders":{0:Messages("experiments.outputs")}
		}, {
			"header":Messages("containers.table.storageCode"),
			"property":"outputContainerUsed.locationOnContainerSupport.storageCode",
			"order":true,
			"edit":true,
			"hide":true,
			"type":"text",
			"position":600,
			"extraHeaders":{0:Messages("experiments.outputs")}
		}],
		compact: true,
		pagination: {
			active: false
		},		
		search: {
			active: false
		},
		order: {
			mode: 'local',
			active: true,
			by:'inputContainer.sampleCodes'
		},
		remove: {
			active: ($scope.isEditModeAvailable() && $scope.isNewState()),
			showButton: ($scope.isEditModeAvailable() && $scope.isNewState()),
			mode: 'local'
		},
		save: {
			active: true,
			withoutEdit: true,
			showButton: false,
			changeClass: false,
			mode: 'local'
		},
		hide: {
			active: true
		},
		edit: {
			active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
			showButton: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
			byDefault: ($scope.isCreationMode()),
			columnMode: true
		},
		messages: {
			active: false
		},
		exportCSV: {
			active: true,
			showButton: true,
			delimiter: ";",
			start: false
		},
		extraHeaders: {
			number: 2,
			dynamic: true,
		}
	};

	$scope.$watch("experiment.instrument.outContainerSupportCategoryCode", function(){
		$scope.experiment.instrument.outContainerSupportCategoryCode = "tube";
	});

	$scope.updatePropertyFromUDT = function(value, col){
		if(col === 'inputContainerUsed.quantity.value') {
			$scope.computeConcNm(value.data);
		} else if(col.property === 'inputContainerUsed.experimentProperties.inputVolume.value'){
			$scope.computeInputQuantity(value.data);
		}
	};
	
	var atmService = atmToSingleDatatable($scope, datatableConfig);

	atmService.newAtomicTransfertMethod = function(){
		return {
			class:"OneToOne",
			line:"1", 
			column:"1", 				
			inputContainerUseds:new Array(0), 
			outputContainerUseds:new Array(0)
		};
	};
	
	atmService.defaultOutputUnit = {
			volume : "µL"
	}

	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	$scope.atmService = atmService;
	
	if ($scope.experiment.instrument.inContainerSupportCategoryCode === "tube") {
		$scope.messages.clear();
		$scope.atmService = atmService;
	} else{
		$scope.messages.setError(Messages('experiments.input.error.only-tubes'));					
	}

	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event save");

		$scope.atmService.data.save();
		$scope.atmService.viewToExperimentOneToOne($scope.experiment);

		$scope.$emit('childSaved', callbackFunction);
	});

	$scope.$on('activeEditMode', function(e) {
		console.log("call event activeEditMode");
		$scope.atmService.data.selectAll(true);
		$scope.atmService.data.setEdit();
	});

	$scope.$on('refresh', function(e) {
		console.log("call event refresh");		
		var dtConfig = $scope.atmService.data.getConfig();
		dtConfig.edit.active = $scope.isEditModeAvailable();
		dtConfig.edit.byDefault = false;
		dtConfig.remove.active = ($scope.isEditModeAvailable() && $scope.isNewState());
		$scope.atmService.data.setConfig(dtConfig);
		$scope.atmService.refreshViewFromExperiment($scope.experiment);
		$scope.$emit('viewRefeshed');
	});
	
	$scope.$on('cancel', function(e) {
		console.log("call event cancel");

		$scope.atmService.data.cancel();
	});

	$scope.computeConcNm = function(udtData) {
		var getter= $parse("outputContainerUsed.experimentProperties.nMcalculatedConcentration.value");
		var nmConc = getter(udtData);

		var compute = {
				middleConc : $parse ("outputContainerUsed.experimentProperties.middleConcentration.value")(udtData),
				size : $parse ("inputContainerUsed.size.value")(udtData),
				unit : $parse ("inputContainerUsed.size.unit")(udtData),
				isReady:function() {
					return (this.middleConc && this.size && this.unit === "pb");
				}
		};
		
		if (compute.isReady()){
			var result = $parse("(middleConc / 660 / size * 1000000)")(compute);

			if (angular.isNumber(result) && !isNaN(result)){
				nmConc=result;
			} else {
				nmConc = undefined;
			}	

			getter.assign(udtData, nmConc);
		} else{
			getter.assign(udtData,undefined);
			console.log("not ready to nMcalculatedConcentration");
		}			
	};

	$scope.computeInputQuantity = function(udtData){
		var getter = $parse("inputContainerUsed.experimentProperties.inputQuantity.value");
		var inputQuantity = getter(udtData);
		
		var compute = {
				inputVol1 : $parse("inputContainerUsed.experimentProperties.inputVolume.value")(udtData),
				inputConc1 : $parse("inputContainerUsed.concentration.value")(udtData),
				isReady:function(){
					return (this.inputVol1 && this.inputConc1);
				}
			};
		
		if(compute.isReady()){
			var result = $parse("(inputVol1 * inputConc1)")(compute);
			console.log("result = "+result);
			if(angular.isNumber(result) && !isNaN(result)){
				inputQuantity = Math.round(result*10)/10;				
			}else{
				inputQuantity = undefined;
			}	
			getter.assign(udtData, inputQuantity);
		}else{
			getter.assign(udtData, undefined);
			console.log("not ready to inputQuantity");
		}
		
	}
}]);