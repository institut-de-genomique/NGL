angular.module('home').controller('CNSXToTubesCtrl',['$scope', '$http','$parse',
                                                               function($scope, $http,$parse) {
	$scope.atmService.updateOutputConcentration = function(atm){
		
		if(atm){
		// ne pas faire l'update si déjà renseigné
			var concentration = undefined;
			var unit = undefined;
			var isSame = true;
			for(var i=0;i<atm.inputContainerUseds.length;i++){
				if(atm.inputContainerUseds[i].concentration !== null 
						&& atm.inputContainerUseds[i].concentration !== undefined){
					if(concentration === undefined && unit === undefined){
						concentration = atm.inputContainerUseds[i].concentration.value;
						unit = atm.inputContainerUseds[i].concentration.unit;
					}else{
						if(concentration !== atm.inputContainerUseds[i].concentration.value 
								|| unit !== atm.inputContainerUseds[i].concentration.unit){
							isSame = false;
							break;
						}
					}
				}else if(concentration !== undefined || unit !== undefined){
					isSame = false;
					break;
				}
			}
			if(isSame 
					&& (atm.outputContainerUseds[0].concentration === null
							|| atm.outputContainerUseds[0].concentration.value === null
						|| atm.outputContainerUseds[0].concentration === undefined
						|| atm.outputContainerUseds[0].concentration.value === undefined)){
				atm.outputContainerUseds[0].concentration = angular.copy(atm.inputContainerUseds[0].concentration);				
			}
		}

	};
	
	$scope.showSrcDest = function(){
		return ($scope.experiment.typeCode !== 'pool' || ($scope.experiment.typeCode === 'pool' && $scope.experiment.instrument.typeCode !== 'biomek-fx'));
	}
	
	$scope.update = function(atm, containerUsed, propertyName){
		console.log("update "+propertyName);
		if(propertyName === 'outputContainerUseds[0].concentration.value' ||
				propertyName === 'outputContainerUseds[0].concentration.unit' ||
				propertyName === 'outputContainerUseds[0].volume.value'){
			console.log("compute all input volume");
			
			angular.forEach(atm.inputContainerUseds, function(inputContainerUsed){
				computeInputVolume(inputContainerUsed, atm);
			});
			
		}else if(propertyName.match(/inputContainerUseds\[\d+\].percentage/) != null){
			console.log("compute one input volume");
			computeInputVolume(containerUsed, atm);
		}
		
		computeBufferVolume(atm);
	}
	
	var computeInputVolume = function(inputContainerUsed, atm){
		var getter = $parse("experimentProperties.inputVolume");
		var inputVolume = getter(inputContainerUsed);
		if(null === inputVolume  || undefined === inputVolume){
			inputVolume = {value : undefined, unit : 'µL'};
		}
		//we compute only if empty
		
		var compute = {
			inputPercentage : $parse("percentage")(inputContainerUsed),
			inputConc : $parse("concentration")(inputContainerUsed),
			outputConc : $parse("outputContainerUseds[0].concentration")(atm),
			outputVol : $parse("outputContainerUseds[0].volume")(atm)
		
		};
		if($parse("(outputConc.unit ===  inputConc.unit)")(compute)){
			var result = $parse("(inputPercentage * outputConc.value *  outputVol.value) / (inputConc.value * 100)")(compute);
			console.log("result = "+result);
			if(angular.isNumber(result) && !isNaN(result)){
				inputVolume.value = Math.round(result*10)/10;				
			}else{
				inputVolume.value = undefined;
			}	
			getter.assign(inputContainerUsed, inputVolume);
		}else{
			inputVolume.value = undefined;
			getter.assign(inputContainerUsed, inputVolume);
		}
		return inputVolume.value;
		
	}
	
	var computeBufferVolume = function(atm){
		
		var inputVolumeTotal = 0;
		var getterInputVolume = $parse("experimentProperties.inputVolume");
		
		atm.inputContainerUseds.forEach(function(icu){
			var inputVolume = getterInputVolume(icu);
			if(null === inputVolume  || undefined === inputVolume || undefined === inputVolume.value ||  null === inputVolume.value){
				inputVolumeTotal = undefined;
			}else if(inputVolumeTotal !== undefined){
				inputVolumeTotal += inputVolume.value;
			}						
		})
		
		var outputVolume  = $parse("outputContainerUseds[0].volume")(atm);
		
		if(outputVolume && outputVolume.value && inputVolumeTotal){
			var bufferVolume = {value : undefined, unit : 'µL'};
			var result = outputVolume.value - inputVolumeTotal;
			
			if(angular.isNumber(result) && !isNaN(result)){
				bufferVolume.value = Math.round(result*10)/10;				
			}else{
				bufferVolume.value = undefined;
			}	
			
			$parse("outputContainerUseds[0].experimentProperties.bufferVolume").assign(atm, bufferVolume);
		}
	}
	
	$scope.computeInSizeToOut= function(){
		
		console.log("Compute out size");
		var atm = $scope.atmService.data.atm;	
		angular.forEach(atm, function(value){
			var sizeTotal=0;
			var nbContentTotal=0;
			value.inputContainerUseds.forEach(function(icu){
				if(icu.size==null || icu.size.value==null || icu.contents==null || icu.contents.length==0){
					sizeTotal=undefined;
				}
				if(sizeTotal!=undefined){
					sizeTotal+=(icu.size.value*icu.contents.length);
					nbContentTotal+=icu.contents.length;
				}
			})
			if(value.outputContainerUseds!=null && sizeTotal!=undefined){
				var size= {value : undefined, unit : 'pb'};
				size.value= Math.round(sizeTotal/nbContentTotal*1)/1;
				$parse("outputContainerUseds[0].size").assign(value, size);
			}else{
				$scope.messages.setError(Messages('experiments.input.warn.unquantifiableSample'));		
				$scope.messages.clear();
				$scope.messages.clazz = "alert alert-warning";
				$scope.messages.text = "Le calcul \"moyenne pondérée des tailles\" ne peut pas s'effectuer car la taille est manquante chez au moins un container d'entrée";
				$scope.messages.showDetails = false;
				$scope.messages.open();
			}
			
		});
		$scope.atmService.data.updateDatatable();
	}

	if($scope.atmService.inputContainerSupportCategoryCode !== "tube"){
		$scope.setAdditionnalButtons([{
			isDisabled : function(){return $scope.isNewState();} ,
			isShow:function(){return !$scope.isNewState();},
			click:$scope.fileUtils.generateSampleSheet,
			label:Messages("experiments.sampleSheet")
		}]);
	}
	
	//Only tube is authorized
	$scope.$watch("experiment.instrument.outContainerSupportCategoryCode", function(){
		$scope.experiment.instrument.outContainerSupportCategoryCode = "tube";
	});
	
	var config = $scope.atmService.$atmToSingleDatatable.data.getConfig();
	config.otherButtons= {
		active : ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
        template: 
        	'<button class="btn btn-default" ng-click="computeInSizeToOut()" data-toggle="tooltip" ng-disabled="!(isEditModeAvailable() && isWorkflowModeAvailable(\'F\'))"  title="'+Messages("experiments.button.title.computeSizeWeighted")+'" "><i class="fa fa-magic" aria-hidden="true"></i> '+ Messages("experiments.button.computeSize")+' </button>'                	                	
    };
	

	var columns = $scope.atmService.$atmToSingleDatatable.data.getColumnsConfig();

	columns.push({
        "header": Messages("containers.table.contents.length"),
 		"property": "inputContainer.contents.length",
 		"filter": "getArray:'properties.secondaryTag.value'| unique",
 		"order":true,
 		"hide":true,
 		"type":"number",
 		"position":5.9,
        "extraHeaders":{0:Messages("experiments.inputs")}
	});
	
	columns.push({
        "header": Messages("containers.table.secondaryTags"),
 		"property": "inputContainer.contents",
 		"filter": "getArray:'properties.secondaryTag.value'| unique",
 		"order":true,
 		"hide":true,
 		"type":"text",
 		"position":6.9,
 		"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
        "extraHeaders":{0:Messages("experiments.inputs")}
	});
	
	
	columns.push({
		"header" : Messages("containers.table.size"),
		"property": "inputContainerUsed.size.value",
		"order" : true,
		"edit" : false,
		"hide" : true,
		"type" : "number",
		"position" :7.5,
		"extraHeaders" : {
			0 : Messages("experiments.inputs")
		}
	});
	
	
	columns.push({
		"header" : Messages("containers.table.size"),
		"property": "outputContainerUsed.size.value",
		"order" : true,
		"edit" : false,
		"hide" : true,
		"type" : "number",
		"position" :100,
		"extraHeaders" : {
			0 : Messages("experiments.outputs")
		}
	});
	
	$scope.atmService.$atmToSingleDatatable.data.setColumnsConfig(columns);
	
	
}]);