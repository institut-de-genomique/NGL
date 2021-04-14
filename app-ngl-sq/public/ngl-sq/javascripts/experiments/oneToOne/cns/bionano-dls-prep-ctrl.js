angular.module('home').controller('BionanoDLSPrepCtrl',['$scope', '$parse', 'atmToSingleDatatable',
                                                    function($scope, $parse, atmToSingleDatatable){
	
	// NGL-1055: name explicite pour fichier CSV exporté: typeCode experience
	// NGL-1055: mettre getArray et codes:'' dans filter et pas dans render
	var datatableConfig = {
			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[			  
					 {
			        	 "header":Messages("containers.table.code"),
			        	 "property":"inputContainer.code",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":1,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			         {
			        	"header":Messages("containers.table.projectCodes"),
			 			"property": "inputContainer.projectCodes",
			 			"order":true,
			 			"hide":true,
			 			"type":"text",
			 			"position":2,
			 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	"extraHeaders":{0:Messages("experiments.inputs")}
				     },
				     {
			        	"header":Messages("containers.table.sampleCodes"),
			 			"property": "inputContainer.sampleCodes",
			 			"order":true,
			 			"hide":true,
			 			"type":"text",
			 			"position":3,
			 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	"extraHeaders":{0:Messages("experiments.inputs")}
				     },
				     {
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
			         },
			         {
			        	 "header":Messages("containers.table.volume") + " (µL)",
			        	 "property":"inputContainerUsed.volume.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":6,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			         {
			        	 "header":Messages("containers.table.state.code"),
			        	 "property":"inputContainer.state.code",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
						 "filter":"codes:'state'",
			        	 "position":7,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },		
			         {
			        	 "header":Messages("containers.table.volume")+" (µL)",
			        	 "property":"outputContainerUsed.volume.value",
			        	 "order":true,
						 "edit":true,
						 "hide":true,
						 "required":"isRequired()",
						 "type":"number",
			        	 "position":51,
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
			         },
			         {
			        	 "header":Messages("containers.table.stateCode"),
			        	 "property":"outputContainer.state.code | codes:'state'",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":500,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         }
			         ],
			compact:true,
			pagination:{
				active:false
			},		
			search:{
				active:false
			},
			order:{
				mode:'local', //or 
				active:true
			},
			remove:{
				active: ($scope.isEditModeAvailable() && $scope.isNewState()),
				showButton: ($scope.isEditModeAvailable() && $scope.isNewState()),				
				mode:'local'
			},
			save:{
				active:true,
	        	withoutEdit: true,
	        	showButton:false,
	        	changeClass:false,
	        	mode:'local'	        		
			},
			hide:{
				active:true
			},
			edit:{
				active: $scope.isEditModeAvailable(),
				showButton: $scope.isEditModeAvailable(),
				byDefault:$scope.isCreationMode(),				
				columnMode:true
			},
			messages:{
				active:false,
				columnMode:true
			},
			exportCSV:{
				active:true,
				showButton:true,
				delimiter:";",
				start:false
			},
			extraHeaders:{
				number:2,
				dynamic:true,
			}
	};

	$scope.$on('save', function(e, promises, func, endPromises) {	
		console.log("call event save");
		$scope.atmService.data.save();
		$scope.atmService.viewToExperimentOneToOne($scope.experiment);
		copyOutputConcToAttribute($scope.experiment);
		$scope.$emit('childSaved', promises, func, endPromises);
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
		
		if($scope.isCreationMode()){
			var dtConfig = $scope.atmService.data.getConfig();
			dtConfig.edit.byDefault = false;
			$scope.atmService.data.setConfig(dtConfig);
		}
		
	});
	
	$scope.$on('activeEditMode', function(e) {
		console.log("call event activeEditMode");
		$scope.atmService.data.selectAll(true);
		$scope.atmService.data.setEdit();
	});
	//Init		

	$scope.$watch("experiment.instrument.outContainerSupportCategoryCode", function(newValue, oldValue){
		$scope.experiment.instrument.outContainerSupportCategoryCode = "tube";		
	});	
	
	$scope.experiment.instrument.outContainerSupportCategoryCode = "tube";
	
	
	$scope.updatePropertyFromUDT = function(value, col){
		console.log("update from property : "+col.property);
		
		if(col.property === 'inputContainerUsed.experimentProperties.inputVolume.value'){
			computeInputQuantity(value.data);			
		}else if(col.property === 'outputContainerUsed.experimentProperties.measuredConc1.value'
			|| col.property === 'outputContainerUsed.experimentProperties.measuredConc2.value'){
			computeAverageConcentration(value.data, "averageConcentration", "measuredConc1" ,"measuredConc2");
			computeVariationCoefficient(value.data, "variationCoefficient", "averageConcentration", "measuredConc1" ,"measuredConc2");
		}else if(col.property === 'outputContainerUsed.experimentProperties.measuredConc3.value'
			|| col.property === 'outputContainerUsed.experimentProperties.measuredConc4.value'){
			computeAverageConcentration(value.data, "averageConcentration2", "measuredConc3" ,"measuredConc4");
			computeVariationCoefficient(value.data, "variationCoefficient2", "averageConcentration2", "measuredConc3" ,"measuredConc4");
		}		
	}
	
	var computeInputQuantity = function(udtData){
		var getter = $parse("inputContainerUsed.experimentProperties.inputQuantity");
		var inputQuantity = getter(udtData);
		if(undefined == inputQuantity)inputQuantity={};
		
		var compute = {
				intputConc : $parse("inputContainerUsed.concentration.value")(udtData),
				intputVol : $parse("inputContainerUsed.experimentProperties.inputVolume.value")(udtData),			
				isReady:function(){
					return (this.intputConc && this.intputVol);
				}
			};
		
		if(compute.isReady()){
			var result = $parse("intputConc * intputVol")(compute);
			console.log("result = "+result);
			if(angular.isNumber(result) && !isNaN(result)){
				inputQuantity.value = Math.round(result*100)/100;	
				inputQuantity.unit = 'ng'; 
			}else{
				inputQuantity = undefined;
			}	
			getter.assign(udtData, inputQuantity);
		}else{
			inputQuantity = null;
			getter.assign(udtData, inputQuantity);
			console.log("not ready to computeInputQuantity");
		}
		
	};
	
	var computeAverageConcentration = function(udtData, avgConc, conc1, conc2){
		var getter = $parse("outputContainerUsed.experimentProperties."+avgConc);
		var averageConcentration = getter(udtData);
		if(undefined == averageConcentration)averageConcentration={};
		
		var compute = {
				conc1 : $parse("outputContainerUsed.experimentProperties."+conc1+".value")(udtData),
				conc2 : $parse("outputContainerUsed.experimentProperties."+conc2+".value")(udtData),			
				isReady:function(){
					return (this.conc1 && this.conc2);
				}
			};
		
		if(compute.isReady()){
			var result = $parse("(conc1 + conc2)/2")(compute);
			console.log("result = "+result);
			if(angular.isNumber(result) && !isNaN(result)){
				averageConcentration.value = Math.round(result*100)/100;	
				averageConcentration.unit = 'ng/µl'; 
			}else{
				averageConcentration = undefined;
			}	
			getter.assign(udtData, averageConcentration);
		}else{
			averageConcentration = null;
			getter.assign(udtData, averageConcentration);
			console.log("not ready to computeAverageConcentration");
		}
		
	};
	
	var computeVariationCoefficient = function(udtData, variationCoefficient, avgConc, conc1, conc2){
		var getter = $parse("outputContainerUsed.experimentProperties."+variationCoefficient);
		var variationCoefficient = getter(udtData);
		if(undefined == variationCoefficient)variationCoefficient={};
		
		var compute = {
				conc1 : $parse("outputContainerUsed.experimentProperties."+conc1+".value")(udtData),
				conc2 : $parse("outputContainerUsed.experimentProperties."+conc2+".value")(udtData),			
				avgConc : $parse("outputContainerUsed.experimentProperties."+avgConc+".value")(udtData),
				isReady:function(){
					return (this.conc1 && this.conc2 && this.avgConc);
				}
			};
		
		if(compute.isReady()){
			// we divide by 2-1 to apply the STDEV STANDARD (sample on a population) and not STDEV PEARSON (full population).
			//this strategy is defined by bionano enterprise
			var result = $parse("(((conc1 - avgConc) * (conc1 - avgConc)) + ((conc2 - avgConc) * (conc2 - avgConc)))/(2-1)")(compute);
			console.log("result = "+result);
			if(angular.isNumber(result) && !isNaN(result)){
				variationCoefficient.value = (Math.sqrt(result)/compute.avgConc)*100;					
			}else{
				variationCoefficient = undefined;
			}	
			getter.assign(udtData, variationCoefficient);
		}else{
			variationCoefficient = null;
			getter.assign(udtData, variationCoefficient);
			console.log("not ready to computeAverageConcentration");
		}
		
	};
	
	var copyOutputConcToAttribute = function(experiment){
		for(var i=0 ; i < experiment.atomicTransfertMethods.length && experiment.atomicTransfertMethods != null; i++){
			var atm = experiment.atomicTransfertMethods[i];
			for(var j=0 ; j < atm.outputContainerUseds.length ; j++){		
				var ocu = atm.outputContainerUseds[j];
				if(ocu.experimentProperties && ocu.experimentProperties.averageConcentration && ocu.experimentProperties.averageConcentration.value){
					if (ocu.experimentProperties.averageConcentration2 && ocu.experimentProperties.averageConcentration2.value){
						ocu.concentration.value= ocu.experimentProperties.averageConcentration2.value;	
						ocu.concentration.unit=ocu.experimentProperties.averageConcentration.unit;	
					}else{
						ocu.concentration= ocu.experimentProperties.averageConcentration;
					}

				}else{
					ocu.concentration.value = null;
					ocu.concentration.unit = null;
				}		
				console.log("output concentration "+ocu.concentration.value);
			}
		}				
	};
	
	var atmService = atmToSingleDatatable($scope, datatableConfig);
	//defined new atomictransfertMethod
	atmService.newAtomicTransfertMethod = function(){
		return {
			class:"OneToOne",
			line:"1", 
			column:"1", 				
			inputContainerUseds:new Array(0), 
			outputContainerUseds:new Array(0)
		};
	};
	
	//defined default output unit
	atmService.defaultOutputUnit = {
			volume : "µL",
			concentration : "ng/µl"
	}
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	$scope.atmService = atmService;
	
}]);