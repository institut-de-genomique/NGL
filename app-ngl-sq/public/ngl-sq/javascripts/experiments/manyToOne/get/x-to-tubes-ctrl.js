angular.module('home').controller('GETPlatesToTubesCtrl',['$scope', '$http','$parse',
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
	
	$scope.update = function(atm, containerUsed, propertyName){
		console.log("update "+propertyName);
		if(propertyName === 'outputContainerUseds[0].concentration.value' ||
				propertyName === 'outputContainerUseds[0].concentration.unit' ||
				propertyName === 'outputContainerUseds[0].volume.value'){
			console.log("compute all input volume");
			
			angular.forEach(atm.inputContainerUseds, function(inputContainerUsed){
				computeInputVolume(inputContainerUsed, atm);
			});
			
		}else if(propertyName.match(/inputContainerUseds\[\d\].percentage/) != null){
			console.log("compute one input volume");
			computeInputVolume(containerUsed, atm);
		}
		
	}
	
	var computeInputVolume = function(inputContainerUsed, atm){
		var getter = $parse("experimentProperties.inputVolume");
		var inputVolume = getter(inputContainerUsed);
		if(null === inputVolume  || undefined === inputVolume){
			inputVolume = {value : undefined, unit : 'µl'};
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
				inputVolume.value = result;				
			}else{
				inputVolume.value = undefined;
			}	
			getter.assign(inputContainerUsed, inputVolume);
		}else{
			inputVolume.value = undefined;
			getter.assign(inputContainerUsed, inputVolume);
		}
		
	}
	
	var generateSampleSheet = function(){
		$http.post(jsRoutes.controllers.instruments.io.IO.generateFile($scope.experiment.code).url,{})
		.success(function(data, status, headers, config) {
			var header = headers("Content-disposition");
			var filepath = header.split("filename=")[1];
			
			var filename = filepath.split(/\/|\\/);
			filename = filename[filename.length-1];
			if(data!=null){
				$scope.messages.clazz="alert alert-success";
				$scope.messages.text=Messages('experiments.msg.generateSampleSheet.success')+" : "+filepath;
				$scope.messages.showDetails = false;
				$scope.messages.open();	
				
				var blob = new Blob([data], {type: "text/plain;charset=utf-8"});    					
				saveAs(blob, filename);
			}
		})
		.error(function(data, status, headers, config) {
			$scope.messages.clazz = "alert alert-danger";
			$scope.messages.text = Messages('experiments.msg.generateSampleSheet.error');
			$scope.messages.showDetails = false;
			$scope.messages.open();				
		});
	};

	$scope.setAdditionnalButtons([{
		isDisabled : function(){return $scope.isNewState();} ,
		isShow:function(){return !$scope.isNewState();},
		click:generateSampleSheet,
		label:Messages("experiments.sampleSheet")
	}]);
	
}]);