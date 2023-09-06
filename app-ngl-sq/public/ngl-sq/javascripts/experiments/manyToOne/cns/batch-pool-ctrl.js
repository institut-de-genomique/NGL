angular.module('home').controller('BatchPoolCtrl',['$scope', '$http','$parse', '$filter', '$timeout', 'atmToDragNDrop2','mainService', 'datatable','atmToSingleDatatable',
	function($scope, $http, $parse, $filter,$timeout, atmToDragNDrop, mainService, datatable, atmToSingleDatatable) {

	var datatableConfig = {		
			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[   					

				{
					"header":Messages("containers.table.supportCategoryCode"),
					"property":"inputContainer.locationOnContainerSupport.categoryCode",
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
					"header":Messages("containers.table.volume") + " (µL)",
					"property":"inputContainer.volume.value",
					"order":true,
					"edit":false,
					"hide":true,
					"type":"number",
					"position":8,
					"extraHeaders":{0:Messages("experiments.inputs")}
				},
				{
					"header":Messages("containers.table.concentration.shortLabel"),
					"property":"inputContainer.concentration.value",
					"order":true,
					"edit":false,
					"hide":true,
					"type":"number",
					"position":9,
					"extraHeaders":{0:Messages("experiments.inputs")}
				},
				{
					"header":Messages("containers.table.concentration.unit.shortLabel"),
					"property":"inputContainer.concentration.unit",
					"order":true,
					"edit":false,
					"hide":true,
					"type":"text",
					"position":10,
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
					"position":11,
					"extraHeaders":{0:Messages("experiments.inputs")}
				},
				{
					"header":Messages("containers.table.percentageInsidePool"),
					"property":"inputContainer.percentage",
					"order":true,
					"edit":false,
					"hide":true,
					"type":"number",
					"position":12,
					"extraHeaders":{0:Messages("experiments.inputs")}
				},
				{
					"header":Messages("containers.table.concentration.shortLabel"),
					"property":"outputContainer.concentration.value",
					"order":true,
					"edit":false,
					"hide":true,
					"type":"number",
					"position":50,
					"extraHeaders":{0:Messages("experiments.outputs")}
				},
				{
					"header":Messages("containers.table.concentration.unit.shortLabel"),
					"property":"outputContainer.concentration.unit",
					"order":true,
					"edit":false,
					"hide":true,
					"type":"text",
					"position":50.5,
					"extraHeaders":{0:Messages("experiments.outputs")}
				},
				{
					"header":Messages("containers.table.volume")+" (µL)",
					"property":"outputContainer.volume.value",
					"order":true,
					"edit":false,
					"hide":true,
					"type":"number",
					"position":51,
					"extraHeaders":{0:Messages("experiments.outputs")}
				},
				{
					"header":Messages("containers.table.code"),
					"property":"outputContainer.code",
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
					"header" : Messages("containers.table.size"),
					"property": "inputContainer.size.value",
					"order" : true,
					"edit" : false,
					"hide" : true,
					"type" : "number",
					"position" :7.5,
					"extraHeaders" : {0 : Messages("experiments.inputs")}
				},
				{
					"header" : Messages("containers.table.size"),
					"property": "outputContainer.size.value",
					"order" : true,
					"edit" : false,
					"hide" : true,
					"type" : "number",
					"position" :100,
					"extraHeaders" : {0 : Messages("experiments.outputs")}
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
					by:"inputContainer.locationOnContainerSupport.code"
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
	
	if(supportCategoryCode === "96-well-plate"){
		datatableConfig.columns.push({
			"header" : Messages("containers.table.supportCode"),
			"property" : "inputContainer.locationOnContainerSupport.code",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "text",
			"position" : 1,
			"extraHeaders" : {
				0 : Messages("experiments.inputs")
			}
		});
		datatableConfig.columns.push({
			"header" : Messages("containers.table.support.line"),
			"property" : "inputContainer.locationOnContainerSupport.line",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "text",
			"position" : 1.1,
			"extraHeaders" : {
				0 : Messages("experiments.inputs")
			}
		});
		datatableConfig.columns.push({
			"header" : Messages("containers.table.support.column"),
			"property" : "inputContainer.locationOnContainerSupport.column*1",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "number",
			"position" : 1.2,
			"extraHeaders" : {
				0 : Messages("experiments.inputs")
			}
		});

	} else {
		datatableConfig.columns.push({
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
		});
		
		datatableConfig.order.by = 'inputContainer.sampleCodes';
	}
	
	datatableConfig.otherButtons= {
			active : ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
	        template: 
	        	'<button class="btn btn-default" ng-click="computeInSizeToOut()" data-toggle="tooltip" ng-disabled="!(isEditModeAvailable() && isWorkflowModeAvailable(\'F\'))"  title="'+Messages("experiments.button.title.computeSizeWeighted")+'" "><i class="fa fa-magic" aria-hidden="true"></i> '+ Messages("experiments.button.computeSize")+' </button>'                	                	
	    };
	
	
	if($scope.isNewState()){
		//New State view to create pool with atmServices
		console.log("Experiment in creation");

		
		$scope.inputContainerProperties = $filter('filter')($scope.experimentType.propertiesDefinitions, 'ContainerIn');
		$scope.outputContainerProperties = $filter('filter')($scope.experimentType.propertiesDefinitions, 'ContainerOut');

		var atmService = atmToDragNDrop($scope, 0, datatableConfig);

		atmService.inputContainerSupportCategoryCode = $scope.experiment.instrument.inContainerSupportCategoryCode;
		atmService.outputContainerSupportCategoryCode = $scope.experiment.instrument.outContainerSupportCategoryCode;

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

		atmService.defaultOutputUnit = {
				volume : "µL",				
		}

		atmService.experimentToView($scope.experiment, $scope.experimentType);
		atmService.data.isAllATMViewClose=true;
		atmService.data.hideAllATM();
		$scope.atmService = atmService;

		//restriction tecan : forcer tube en sortie
		$scope.$watch("experiment.instrument.outContainerSupportCategoryCode", function(){
			if  ($scope.experiment.instrument.typeCode === "tecan-evo-100"){
				$scope.experiment.instrument.outContainerSupportCategoryCode = "tube";
				//To update view
				$scope.atmService.outputContainerSupportCategoryCode = "tube";
			}
		});

		$scope.updateOuputContainers = function (atm, propertyName, propertyValue){
			console.log (" updateOuputContainers :"+ propertyName + " changed");
			if (atm){
				console.log ("updating all ATMs "+ propertyName +" with :" + propertyValue);
				atm.forEach(function(a){	

					// 28/09/2017 supression des   && propertyValue car sinon on ne peut pas effacer completement le champ...
					if (propertyName === "supportStorageCode")  {
						//console.log ("updating "+ a.outputContainerUseds[0].locationOnContainerSupport.storageCode +"=>"+ propertyValue);
						a.outputContainerUseds[0].locationOnContainerSupport.storageCode=propertyValue;

					} else if  (propertyName === "supportCode")  {
						//console.log ("updating "+ a.outputContainerUseds[0].locationOnContainerSupport.code +"=>"+ propertyValue);
						a.outputContainerUseds[0].locationOnContainerSupport.code=propertyValue;

					} else if  ((propertyName === "concentration")||(propertyName === "volume")) {
						//console.log ("updating "+propertyName+".value =>"+ propertyValue);
						$parse(propertyName+'.value').assign(a.outputContainerUseds[0],propertyValue);

						// recalculer les volumes engagés et buffer
						console.log("compute all input volumes");
						angular.forEach(a.inputContainerUseds, function(inputContainerUsed){
							computeInputVolume(inputContainerUsed, a);
						});

						console.log("compute buffer volume");
						computeBufferVolume(a);

					} else if  (propertyName === "conc_unit" ) {
						//console.log ("updating concentration.unit =>"+ propertyValue);
						$parse('concentration.unit').assign(a.outputContainerUseds[0],propertyValue);
					}		    	 
				})
			}
		};

		var computeInputVolume = function(inputContainerUsed, atm){
			var getter = $parse("experimentProperties.inputVolume");
			var inputVolume = getter(inputContainerUsed);
			if(null === inputVolume  || undefined === inputVolume){
				inputVolume = {value : undefined, unit : 'µL'};
			}

			//compute only if empty
			var compute = {
					inputPercentage : $parse("percentage")(inputContainerUsed),
					inputConc : $parse("concentration")(inputContainerUsed),
					outputConc : $parse("outputContainerUseds[0].concentration")(atm),
					outputVol : $parse("outputContainerUseds[0].volume")(atm)
			};

			// 28/09/2017 Julie demande de bloquer le calcul en normalization-and-pooling si unité de concentration n'EST PAS nM 
			//           mais autoriser pour le pool ??

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
		};

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

				// Julie->FDS: laisser les cas negatifs...permet de voir qu'il y a un pb...!!
				if(angular.isNumber(result) && !isNaN(result)){
					bufferVolume.value = Math.round(result*10)/10;				
				}else{
					bufferVolume.value = undefined;
				}	

				$parse("outputContainerUseds[0].experimentProperties.bufferVolume").assign(atm, bufferVolume);
			}
		};


		$scope.update = function(atm, containerUsed, propertyName){
			console.log("update "+propertyName);

			if(propertyName === 'outputContainerUseds[0].concentration.value' ||
					propertyName === 'outputContainerUseds[0].concentration.unit' ||
					propertyName === 'outputContainerUseds[0].volume.value'){

				console.log("compute all input volumes");
				angular.forEach(atm.inputContainerUseds, function(inputContainerUsed){
					computeInputVolume(inputContainerUsed, atm);
				});

				console.log("compute buffer volume");
				computeBufferVolume(atm);

			}else if(propertyName.match(/inputContainerUseds\[\d\].percentage/) != null){
				console.log("compute one input volume");
				computeInputVolume(containerUsed, atm);

				console.log("compute buffer volume");
				computeBufferVolume(atm);
			} 
			else if(propertyName === 'outputContainerUseds[0].locationOnContainerSupport.line' ){
				atm.line =$parse("outputContainerUseds[0].locationOnContainerSupport.line")(atm)
				console.log("support.line="+atm.line);
			}
			else if(propertyName === 'outputContainerUseds[0].locationOnContainerSupport.column' ){
				atm.column =$parse("outputContainerUseds[0].locationOnContainerSupport.column")(atm)
				console.log("support.column="+atm.column);
			}
		};

		$scope.columns = ["1","2","3","4","5","6","7","8","9","10","11","12"]; 
		$scope.lines=["A","B","C","D","E","F","G","H"];  

	}else{
		//Experiment created view only with datatable
			$scope.batchdatatable = datatable(datatableConfig);
			//Calculate data to datatable
			var allData = [];
			var l=0, atomicIndex=0;
			for(var i=0; i< $scope.experiment.atomicTransfertMethods.length;i++){
				var atm = $scope.experiment.atomicTransfertMethods[i];
				for(var j=0; j<atm.inputContainerUseds.length ; j++){
					 allData[l] = {atomicIndex:atomicIndex};
					 allData[l].inputContainer=atm.inputContainerUseds[j];
					 allData[l].outputContainer=atm.outputContainerUseds[0];
					 l++;
				}
				atomicIndex++;
			}
			$scope.outputContainerSupportCategoryCode=allData[0].outputContainer.categoryCode;
			$scope.batchdatatable.setData(allData, allData.length);		
			
			console.log($scope.batchdatatable);
	 }	
		
	$scope.getInputContainerSupports = function(){
		var inputContainerSupports = [];
		if($scope.experiment.atomicTransfertMethods){
			inputContainerSupports = $scope.experiment.inputContainerSupportCodes;
		}
		return inputContainerSupports;
	}
	$scope.getOutputContainers = function(){
		var outputContainers = [];
		if($scope.experiment.atomicTransfertMethods){
			$scope.experiment.atomicTransfertMethods.forEach(function(atm){
				this.push(atm.outputContainerUseds[0]);
				
			}, outputContainers);
		}
		return outputContainers;
	}
	
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

	$scope.isEditMode = function(){
		return ($scope.$parent.isEditMode() && $scope.isNewState());
	};

	if($scope.experiment.instrument.typeCode === "biomek-fx" && $scope.experiment.instrument.outContainerSupportCategoryCode !== "tube"
		&& $scope.experiment.instrument.inContainerSupportCategoryCode !== "tube"){
		$scope.setAdditionnalButtons([{
			isDisabled : function(){return $scope.isNewState();} ,
			isShow:function(){return !$scope.isNewState();},
			click: function(){return $scope.fileUtils.generateSampleSheet({'fdrType':"dna"})},
			label: Messages("experiments.sampleSheet")+ " / ADN"
		},{
			isDisabled : function(){return $scope.isNewState();} ,
			isShow:function(){return !$scope.isNewState();},
			click: function(){return $scope.fileUtils.generateSampleSheet({'fdrType':"buffer"})},
			label:Messages("experiments.sampleSheet")+ " / tampon"
		}]);		
	}

	if($scope.experiment.instrument.typeCode === "tecan-evo-100" && $scope.atmService.inputContainerSupportCategoryCode !== "tube" 
		&& $scope.experiment.instrument.outContainerSupportCategoryCode === "tube"){
		$scope.setAdditionnalButtons([{
			isDisabled : function(){return $scope.isNewState();} ,
			isShow:function(){return !$scope.isNewState();},
			click:$scope.fileUtils.generateSampleSheet,
			label:Messages("experiments.sampleSheet")
		}]);
	}
	
	$scope.select = function(){
		$scope.atmService.data.dropInSelectInputContainer();
		$scope.atmService.data.isAllATMViewClose=true;
		$scope.atmService.data.hideAllATM();
	};
	
	
	$scope.computeInSizeToOut= function(){
	console.log("Compute out size");
	var atm = $scope.experiment.atomicTransfertMethods;	
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
		//TODO service to calculate data
		var allData = [];
		var l=0, atomicIndex=0;
		for(var i=0; i< $scope.experiment.atomicTransfertMethods.length;i++){
			var atm = $scope.experiment.atomicTransfertMethods[i];
			for(var j=0; j<atm.inputContainerUseds.length ; j++){
				 allData[l] = {atomicIndex:atomicIndex};
				 allData[l].inputContainer=atm.inputContainerUseds[j];
				 allData[l].outputContainer=atm.outputContainerUseds[0];
				 l++;
			}
			atomicIndex++;
		}
		$scope.outputContainerSupportCategoryCode=allData[0].outputContainer.categoryCode;
		$scope.batchdatatable.setData(allData, allData.length);		
	});
	
	
	}

	$scope.$on('save', function(e, callbackFunction) {
		console.log("call event save on x-to-tubes");

		if($scope.isNewState() && $scope.atmService.data.atm.length === 0){
			$scope.$emit('childSavedError', callbackFunction);

			$scope.messages.clazz = "alert alert-danger";
			$scope.messages.text = Messages("experiments.msg.nocontainer.save.error");
			$scope.messages.showDetails = false;
			$scope.messages.open();   
		}else{
			if($scope.isNewState()){
				$scope.atmService.viewToExperiment($scope.experiment, false);
			}
			$scope.$emit('childSaved', callbackFunction);
		}
	});

	$scope.$on('refresh', function(e) {
		console.log("call event refresh on x-to-tubes");	
		if($scope.isNewState()){
			$scope.atmService.data.isAllATMViewClose=true;
			$scope.atmService.data.hideAllATM();
			$scope.atmService.refreshViewFromExperiment($scope.experiment);
		}else{
				$scope.batchdatatable = datatable(datatableConfig);
				//Calculate data to datatable
				var allData = [];
				var l=0, atomicIndex=0;
				for(var i=0; i< $scope.experiment.atomicTransfertMethods.length;i++){
					var atm = $scope.experiment.atomicTransfertMethods[i];
					for(var j=0; j<atm.inputContainerUseds.length ; j++){
						 allData[l] = {atomicIndex:atomicIndex};
						 allData[l].inputContainer=atm.inputContainerUseds[j];
						 allData[l].outputContainer=atm.outputContainerUseds[0];
						 l++;
					}
					atomicIndex++;
				}
				$scope.outputContainerSupportCategoryCode=allData[0].outputContainer.categoryCode;
				$scope.batchdatatable.setData(allData, allData.length);		
				console.log($scope.batchdatatable);
		}
		$scope.$emit('viewRefeshed');
	});

}]);