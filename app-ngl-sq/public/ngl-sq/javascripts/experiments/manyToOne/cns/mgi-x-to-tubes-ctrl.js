angular.module('home').controller('MGIXToTubesCtrl',['$scope', '$parse', '$filter','atmToDragNDrop2','mainService',
                                                               function($scope, $parse, $filter, atmToDragNDrop,mainService) {
	
	var datatableConfig = {		
			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[   					
					{
						"header" : Messages("containers.table.code"),
						"property" : "inputContainer.code",
						"order" : true,
						"edit" : false,
						"hide" : true,
						"type" : "text",
						"position" : 1,
						"extraHeaders" : {
							0 : Messages("experiments.inputs")
						}
					},
					{
			        	 "header":Messages("containers.table.workNameInit"),
			        	 "property":"inputContainer.contents",
			        	 "filter" : "getArray:'properties.workName.value'| unique",
			        	 "order":true,
			        	 "edit":false,
			        	 "hide":true,
			        	 "type":"text",
			        	 "render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	 "position":1.5,
			        	 "extraHeaders" : {
							0 : Messages("experiments.inputs")
						}
			         },
			         {
			        	 "header":Messages("containers.table.supportCategoryCode"),
			        	 "property":"inputContainer.support.categoryCode",
			        	 "filter":"codes:'container_support_cat'",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":2,
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
			        	 "position":3,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			         {
			        	"header":Messages("containers.table.projectCodes"),
			 			"property": "inputContainer.projectCodes",
			 			"order":false,
			 			"hide":true,
			 			"type":"text",
			 			"position":4,
			 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			            "extraHeaders":{0:Messages("experiments.inputs")}
				     },
				     {
			        	"header":Messages("containers.table.sampleCodes"),
			 			"property": "inputContainer.sampleCodes",
			 			"order":true,
			 			"hide":true,
			 			"type":"text",
			 			"position":5,
			 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	"extraHeaders":{0:Messages("experiments.inputs")}
				     },
				    {
				         "header": Messages("containers.table.contents.length"),
				  		"property": "inputContainer.contents.length",
				  		"filter": "getArray:'properties.secondaryTag.value'| unique",
				  		"order":true,
				  		"hide":true,
				  		"type":"number",
				  		"position":5.9,
				         "extraHeaders":{0:Messages("experiments.inputs")}
				 	 },
			         {
				 		"header":Messages("containers.table.libProcessType"),
				 		"property": "inputContainer.contents",
				 		"filter": "getArray:'properties.libProcessTypeCode.value'| unique",
				 		"order":false,
				 		"hide":true,
				 		"type":"text",
				 		"position":6,
				 		"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
				 		"extraHeaders": {0:Messages("experiments.inputs")}	 						 			
				 	},
				 	{
				        "header": Messages("containers.table.secondaryTags.shortLabel"),
				 		"property": "inputContainer.contents",
				 		"filter": "getArray:'properties.secondaryTag.value'| unique",
				 		"order":true,
				 		"hide":true,
				 		"type":"text",
				 		"position":6.9,
				 		"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
				        "extraHeaders":{0:Messages("experiments.inputs")}
					},
			        {
				        "header":Messages("containers.table.tags"),
				 		"property": "inputContainer.contents",
				 		"filter": "getArray:'properties.tag.value'| unique",
				 		"order":true,
				 		"hide":true,
				 		"type":"text",
				 		"position":7,
				 		"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
				        "extraHeaders":{0:Messages("experiments.inputs")}
				     },		
				     {
					        "header":Messages("containers.table.expectedPrimaryTags"),
					 		"property": "inputContainer.contents",
					 		"filter": "getArray:'properties.expectedPrimaryTags.value'| unique",
					 		"order":true,
					 		"hide":true,
					 		"type":"text",
					 		"position":7.1,
					 		"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
					        "extraHeaders":{0:Messages("experiments.inputs")}
					  },	
				     {
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
				 	},
				 	 {
				 		"header" : Messages("containers.table.size.unit"),
				 		"property": "inputContainerUsed.size.unit",
				 		"order" : true,
				 		"edit" : false,
				 		"hide" : true,
				 		"type" : "text",
				 		"position" :7.6,
				 		"extraHeaders" : {
				 			0 : Messages("experiments.inputs")
				 		}
				 	},
				 	{
			        	 "header":Messages("containers.table.state.code"),
			        	 "property":"inputContainer.state.code",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
						 "filter":"codes:'state'",
			        	 "position":7.7,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
				     {
			        	 "header":Messages("containers.table.volume") + " (µL)",
			        	 "property":"inputContainerUsed.volume.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":8,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
					 {
			        	 "header":Messages("containers.table.concentration.shortLabel"),
			        	 "property":"inputContainerUsed.concentration.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":9,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			         {
			        	 "header":Messages("containers.table.concentration.unit.shortLabel"),
			        	 "property":"inputContainerUsed.concentration.unit",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":10,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },	        
			         
			         {
			        	 "header":Messages("containers.table.percentageInsidePool"),
			        	 "property":"inputContainerUsed.percentage",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":12,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			         {
			        	 "header":Messages("containers.table.code"),
			        	 "property":"outputContainerUsed.code",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
						 "type":"text",
			        	 "position":48,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },
			         {
			        	 "header":Messages("containers.table.quantity"),
			        	 "property":"outputContainerUsed.quantity.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
						 "type":"number",
			        	 "position":49,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },
			         {
			        	 "header":Messages("containers.table.volume")+" (µL)",
			        	 "property":"outputContainerUsed.volume.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
						 "type":"number",
			        	 "position":50,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },
			         {
			        	 "header":Messages("containers.table.concentration.shortLabel"),
			        	 "property":"outputContainerUsed.concentration.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
						 "type":"number",
			        	 "position":61,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },
			         {
			        	 "header":Messages("containers.table.concentration.unit.shortLabel"),
			        	 "property":"outputContainerUsed.concentration.unit",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
						 "type":"text",
			        	 "position":61.5,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },
			        
			         {
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
			     	},
			     	{
			     		"header" : Messages("containers.table.size.unit"),
			     		"property": "outputContainerUsed.size.unit",
			     		"order" : true,
			     		"edit" : false,
			     		"hide" : true,
			     		"type" : "text",
			     		"position" :101,
			     		"extraHeaders" : {
			     			0 : Messages("experiments.outputs")
			     		}
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
				active:true,
				by:"inputContainer.sampleCodes"
			},
			remove:{
				active:false,
			},
			save:{
				active:true,
				withoutEdit: true,
				mode:'local',
				changeClass:false,
				showButton:false,
	        	callback:function(datatable){
	        		  copyContainerSupportCodeAndStorageCodeToDT(datatable);
	        	}
			},
			hide:{
				active:true
			},
			mergeCells:{
	        	active:true 
	        },
			select:{
				active:false,
				showButton:true,
				isSelectAll:false
			},
			edit:{
				active: false,
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
	
	var tmp = [];
	if(!$scope.isCreationMode()){
		tmp = $scope.$eval("atomicTransfertMethods|flatArray:'inputContainerUseds'|getArray:'locationOnContainerSupport.categoryCode'|unique",$scope.experiment);			
	}else{
		tmp = $scope.$eval("getBasket().get()|getArray:'support.categoryCode'|unique",mainService);
	}
	var supportCategoryCode = undefined;
	if(tmp.length === 1){
		supportCategoryCode=tmp[0];
		$scope.supportCategoryCode = supportCategoryCode;
	}else{
		supportCategoryCode="mixte";
		$scope.supportCategoryCode = "tube";
	}
		
	console.log("supportCategoryCode : "+supportCategoryCode);
	
	$scope.drop = function(e, data, ngModel, alreadyInTheModel, fromModel) {
		//capture the number of the atomicTransfertMethod
		if(!alreadyInTheModel){
			$scope.atmService.data.updateDatatable();
		
		}
	};
	
	// FDS: renommer getOutputContainers car donne une liste de containers et pas de containerSupports, !! contient des doublons 
	$scope.getOutputContainers = function(){
		var outputContainers = [];
		if($scope.experiment.atomicTransfertMethods){
			$scope.experiment.atomicTransfertMethods.forEach(function(atm){
				this.push(atm.outputContainerUseds[0]);
				
			}, outputContainers);
		}
		return outputContainers;
	}
	
	// FDS : liste de containerSupports sans doublons
	$scope.getDistinctOutputContainerSupports = function(){
		var outputContainerSupports = [];
		if($scope.experiment.atomicTransfertMethods){
			var unique = {};
			$scope.experiment.atomicTransfertMethods.forEach(function(atm){
				
				if (!unique[atm.outputContainerUseds[0].locationOnContainerSupport.code]) {
				    this.push(atm.outputContainerUseds[0].locationOnContainerSupport);
				    unique[atm.outputContainerUseds[0].locationOnContainerSupport.code] = true;
				}
			}, outputContainerSupports);
		}
		return outputContainerSupports;
	}
	
	$scope.getInputContainerSupports = function(){
		var inputContainerSupports = [];
		if($scope.experiment.atomicTransfertMethods){
			inputContainerSupports = $scope.experiment.inputContainerSupportCodes;
		}
		return inputContainerSupports;
	}
	
	$scope.isEditMode = function(){
		return ($scope.$parent.isEditMode() && $scope.isNewState());
	};
	
	// fdsantos 28/09/2017 :NGL-1601 ne pas sauvegarder une experience vide.
	$scope.$on('save', function(e, callbackFunction) {
		console.log("call event save on x-to-tubes");
		
		if($scope.atmService.data.atm.length === 0){
			$scope.$emit('childSavedError', callbackFunction);
			
		    $scope.messages.clazz = "alert alert-danger";
		    $scope.messages.text = Messages("experiments.msg.nocontainer.save.error");
		    $scope.messages.showDetails = false;
			$scope.messages.open();   
	
		} else {	
			$scope.atmService.viewToExperiment($scope.experiment, false);
			$scope.$emit('childSaved', callbackFunction);
	    } 
	});
	
	$scope.$on('refresh', function(e) {
		console.log("call event refresh on x-to-tubes");		
		$scope.atmService.refreshViewFromExperiment($scope.experiment);
		$scope.$emit('viewRefeshed');
	});
	
	
	$scope.$on('cancel', function(e) {
		console.log("call event cancel");
		
	});
	
	$scope.$on('activeEditMode', function(e) {
		console.log("call event activeEditMode");
	});
	
	$scope.inputContainerProperties = $filter('filter')($scope.experimentType.propertiesDefinitions, 'ContainerIn');
	$scope.outputContainerProperties = $filter('filter')($scope.experimentType.propertiesDefinitions, 'ContainerOut');
	
	
	
	
	var atmService = atmToDragNDrop($scope, 0, datatableConfig);
	
	atmService.inputContainerSupportCategoryCode = $scope.experiment.instrument.inContainerSupportCategoryCode;
	atmService.outputContainerSupportCategoryCode = $scope.experiment.instrument.outContainerSupportCategoryCode;
	
	
	// 19/10/2016 version de Guillaume pour gerer les cas tubes ou 96-well-plate
	// 27/10/2016 bug vu par JG: au CNS pool generique tube=> tube : line et column sont undefined
	atmService.newAtomicTransfertMethod =  function(line, column){
		var getLine = function(line){
			//TEST correction FDS
			if ($scope.experiment.instrument.outContainerSupportCategoryCode === "tube"){
				return 1; // ligne et colonne=1 pour un tube
			} else {
				return undefined;
			}			
		}
		var getColumn=getLine;
		
		return {
			class:"ManyToOne",
			line:getLine(line), 
			column:getColumn(column), 				
			inputContainerUseds:new Array(0), 
			outputContainerUseds:new Array(0)
		};
	};
	
	//defined default output unit
	atmService.defaultOutputUnit = {
			volume : "µL",				
	}
	
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	//Force output tube
	$scope.$watch("experiment.instrument.outContainerSupportCategoryCode", function(){
		$scope.experiment.instrument.outContainerSupportCategoryCode = "tube";
	});
	//Force input tube
	if($scope.experiment.instrument.inContainerSupportCategoryCode === "tube"){
		$scope.messages.clear();
		$scope.atmService = atmService;
	}else{
		$scope.messages.setError(Messages('experiments.input.error.only-tubes'));					
	}
	
	atmService.updateOutputConcentration = function(atm){
		var nbConvertedConcentration=0;
		var nbSSConvertedtConcentration=0;
		angular.forEach(atm.inputContainerUseds, function(inputContainerUsed){
			//Init value convertedConcentration and ssConvertedConcentration
			if(inputContainerUsed.concentration !=null && inputContainerUsed.size !=null && inputContainerUsed.concentration.unit === 'ng/µl'){
				if(inputContainerUsed.size.unit === 'pb')
				{
					var getter = $parse("experimentProperties.convertedConcentration.value");
					var convertedConcentration=inputContainerUsed.concentration.value / 660 / inputContainerUsed.size.value * 1000000;
					convertedConcentration=Math.round(convertedConcentration*100)/100;
					getter.assign(inputContainerUsed, convertedConcentration);
					nbConvertedConcentration++;
				}else if(inputContainerUsed.size.unit === 'nt'){
					var getter = $parse("experimentProperties.ssConvertedConcentration.value");
					var ssConvertedConcentration=inputContainerUsed.concentration.value / 330 / inputContainerUsed.size.value * 1000000;
					ssConvertedConcentration=Math.round(ssConvertedConcentration*100)/100;
					getter.assign(inputContainerUsed, ssConvertedConcentration);
					nbSSConvertedtConcentration++;
				}
			}	
		});
		
		//Get nb input unit size pb
		var inputSizePb = $filter('filter')(atm.inputContainerUseds,{size:{unit:"pb"}},true);
		//Get nb input unit size nt
		var inputSizeNt = $filter('filter')(atm.inputContainerUseds,{size:{unit:"nt"}},true);
		//If size pb > 0 and input size > 0 error multiple size input
		if(inputSizePb.length>0 && inputSizeNt.length>0){
			$scope.messages.setError(Messages('experiments.input.warn.unquantifiableSample'));		
			$scope.messages.clear();
			$scope.messages.clazz = "alert alert-warning";
			$scope.messages.text = "Les unités de taille ne sont pas homogènes dans les containers d'entrées";
			$scope.messages.showDetails = false;
			$scope.messages.open();
		}
		
		//If nb convertedConcentration > 0 and size not all data error OR  nb ssConvertedConcentration > 0 and size not all data error
		if((nbConvertedConcentration>0 && nbConvertedConcentration!=atm.inputContainerUseds.length) ||
				(nbSSConvertedtConcentration>0 && nbSSConvertedtConcentration!=atm.inputContainerUseds.length)){
			$scope.messages.setError(Messages('experiments.input.warn.unquantifiableSample'));		
			$scope.messages.clear();
			$scope.messages.clazz = "alert alert-warning";
			$scope.messages.text = "Les concentrations ne sont pas homogènes dans les containers d'entrées";
			$scope.messages.showDetails = false;
			$scope.messages.open();
		}
		
		computeSize(atm);
	}
	
	$scope.update = function(atm, containerUsed, propertyName){
			
		if(propertyName.match(/inputContainerUseds\[\d+\].percentage/) != null){
			computeInputVolume(atm, containerUsed);
			computeBufferVolume(atm);
			computeSize(atm);
		}else if(propertyName.match(/inputContainerUseds\[\d+\].experimentProperties.dilutionFactor/) != null){
			computeInputVolume(atm, containerUsed);
			computeBufferVolume(atm);
		}else if(propertyName === 'outputContainerUseds[0].experimentProperties.inputQuantity.value'){
			angular.forEach(atm.inputContainerUseds, function(inputContainerUsed){
				computeInputVolume(atm, inputContainerUsed);
			});
			computeBufferVolume(atm);
			computeConcentration(atm);
			//Copy value in container out
			var inputQuantityValue= $parse("outputContainerUseds[0].experimentProperties.inputQuantity.value")(atm);
			//var inputQuantityUnit= $parse("outputContainerUseds[0].experimentProperties.inputQuantity.unit")(atm);
			var getterValue = $parse("outputContainerUseds[0].quantity.value");
			//var getterUnit = $parse("outputContainerUseds[0].quantity.unit");
			getterValue.assign(atm,inputQuantityValue);
			//getterUnit.assign(atm,inputQuantityUnit);
		}else if(propertyName === 'outputContainerUseds[0].volume.value'){
			computeBufferVolume(atm);
			computeConcentration(atm);
		}
	};
	
	
	
	var computeInputVolume = function(atm, inputContainerUsed){
		//Calcul percentage*inputQuantity/convertedConcentration OR ssConvertedConcentration
		var inputQuantity  = $parse("outputContainerUseds[0].experimentProperties.inputQuantity.value")(atm);
		var percentage = $parse("percentage")(inputContainerUsed);
		var dilFactor = undefined;
		if($parse("experimentProperties.dilutionFactor.value")(inputContainerUsed)!=null)
			dilFactor =(($parse("experimentProperties.dilutionFactor.value")(inputContainerUsed)).indexOf("1/") ==0 ? ($parse("experimentProperties.dilutionFactor.value")(inputContainerUsed)).substring(2) : undefined );
		var concentration = undefined;
		
		if($parse("experimentProperties.convertedConcentration")(inputContainerUsed)!=null){
			concentration = $parse("experimentProperties.convertedConcentration.value")(inputContainerUsed);
		}else if($parse("experimentProperties.ssConvertedConcentration")(inputContainerUsed)!=null){
			concentration = $parse("experimentProperties.ssConvertedConcentration.value")(inputContainerUsed);
		}
		var inputVolume = (percentage/100*inputQuantity)/concentration;
		inputVolume = Math.round(inputVolume*10)/10;
		if(dilFactor != undefined)
			inputVolume = inputVolume*dilFactor;
		var getter = $parse("experimentProperties.inputVolume.value");
		getter.assign(inputContainerUsed,inputVolume);
		
	};
	
	var computeBufferVolume = function(atm){
		//Calcul volumeOut - Somme inputVolume
		var volumeOut  = $parse("outputContainerUseds[0].volume.value")(atm);
		var sumInputVolume = 0;
		angular.forEach(atm.inputContainerUseds, function(inputContainerUsed){
			var inputVolume = $parse("experimentProperties.inputVolume.value")(inputContainerUsed);
			sumInputVolume+=inputVolume;
		});
		var bufferVolume = volumeOut-sumInputVolume;
		bufferVolume=Math.round(bufferVolume*10)/10;
		var getter = $parse("outputContainerUseds[0].experimentProperties.bufferVolume.value");
		getter.assign(atm,bufferVolume);
	};
	
	var computeConcentration = function(atm){
		//Calcul inputQuantity / Volume out
		var inputQuantity = $parse("outputContainerUseds[0].experimentProperties.inputQuantity.value")(atm);
		var volumeOut = $parse("outputContainerUseds[0].volume.value")(atm);
		var concentration = inputQuantity/volumeOut;
		concentration=Math.round(concentration*100)/100;
		var getter = $parse("outputContainerUseds[0].concentration.value");
		getter.assign(atm,concentration);
	};
	
	var computeSize = function(atm){
		var sumPercentage=0;
		var unit=undefined;
		angular.forEach(atm.inputContainerUseds, function(inputContainerUsed){
			sumPercentage+=(inputContainerUsed.percentage/100*inputContainerUsed.size.value);
			sumPercentage=Math.round(sumPercentage*1)/1;
			unit=inputContainerUsed.size.unit;
		});
		var getter = $parse("outputContainerUseds[0].size.value");
		getter.assign(atm, sumPercentage);
		var getterUnit = $parse("outputContainerUseds[0].size.unit");
		getterUnit.assign(atm, unit);
	}
	
	atmService.defaultOutputUnit = {
			concentration : "fmol/µl",
			quantity : "fmol",
			volume : "µl"
	};
	
	$scope.atmService = atmService;
	
}]);
