angular.module('home').controller('FragmentationCtrl',['$scope','$http', '$parse', 'atmToSingleDatatable','lists','mainService',
                                                    function($scope, $http, $parse, atmToSingleDatatable,lists,mainService){
                                                    
	var datatableConfig = {
					name: $scope.experiment.typeCode.toUpperCase(),
					columns:[   
					         /*
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
					         */	         
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
					        	 "order":true,
								 "edit":false,
								 "hide":true,
					        	 "type":"text",
					        	 "render":"<div list-resize='cellValue | unique | codes:\"type\"' list-resize-min-size='3'>",
					        	 "position":4,
					        	 "extraHeaders":{0:Messages("experiments.inputs")}
					         },
					         {
					        	 "header":Messages("containers.table.concentration"),
					        	 "property":"inputContainerUsed.concentration.value",
					        	 "order":true,
								 "edit":false,
								 "hide":true,
					        	 "type":"number",
					        	 "position":5,
					        	 "extraHeaders":{0:Messages("experiments.inputs")}
					         },
							 {
					        	 "header":Messages("containers.table.concentration.unit"),
					        	 "property":"inputContainerUsed.concentration.unit",
					        	 "order":true,
								 "edit":false,
								 "hide":true,
					        	 "type":"text",
					        	 "position":5.1,
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
					        	 "header":Messages("containers.table.quantity") + " (ng)",
					        	 "property":"inputContainerUsed.quantity.value",
					        	 "order":true,
								 "edit":false,
								 "hide":true,
					        	 "type":"number",
					        	 "position":7,
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
					        	 "editDirectives":" udt-change='updatePropertyFromUDT(value,col)' ",
					        	 "order":true,
								 "edit":true,
								 "hide":true,
								 "required":true,
								 "type":"number",
					        	 "position":51,
					        	 "extraHeaders":{0:Messages("experiments.outputs")}
							 },
							 {
								"header":Messages("containers.table.quantity") + " (ng)",
								"property":"outputContainerUsed.quantity.value",
								"order":true,
								"edit":false,
								"hide":true,
								"required":false,
								"type":"number",
								"position":52,
								"extraHeaders":{0:Messages("experiments.outputs")}
							},
					         /*
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
					         */
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
					        	 "header":Messages("containers.table.storageCode"),
					        	 "property":"outputContainerUsed.locationOnContainerSupport.storageCode",
					        	 "order":true,
								 "edit":true,
								 "hide":true,
					        	 "type":"text",
					        	 "position":600,
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
						mode:'local',
						showButton:false,
						changeClass:false
					},
					hide:{
						active:true
					},
					mergeCells:{
						active:true 
					},			
					edit:{
						active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
						showButton: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
						byDefault:($scope.isCreationMode()),
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
						number:1,
						dynamic:true,
					},
					otherButtons: {
		                active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
		                complex:true,
		                template:  ''
		                	+$scope.plateUtils.templates.buttonLineMode()
		                	+$scope.plateUtils.templates.buttonColumnMode()   
							+$scope.plateUtils.templates.buttonCopyPosition()
							+'<div class="btn-group" style="margin-left:5px">'
							+'<button class="btn btn-default" ng-click="copyQuantityInToOut()" data-toggle="tooltip" title="'+Messages("experiments.button.plate.copyQuantity.title")+'"  ng-disabled="!isEditMode()"><i class="fa fa-files-o" aria-hidden="true"></i> '+Messages("experiments.button.plate.copyQuantity")+'</button>'                	                	
							+'</div>'
		            }

			};	
	
	
	var updateATM = function(experiment){
		if(experiment.instrument.outContainerSupportCategoryCode!=="tube"){
			experiment.atomicTransfertMethods.forEach(function(atm){
				atm.line = atm.outputContainerUseds[0].locationOnContainerSupport.line;
				atm.column = atm.outputContainerUseds[0].locationOnContainerSupport.column;
			});
		}
		if(experiment.instrument.outContainerSupportCategoryCode=="tube"){
			experiment.atomicTransfertMethods.forEach(function(atm){
				atm.line = "1";
				atm.column = "1";
				atm.outputContainerUseds[0].locationOnContainerSupport.line="1";
				atm.outputContainerUseds[0].locationOnContainerSupport.column="1";
			});
		}
	}
	
	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event save");
		$scope.atmService.data.save();
		$scope.atmService.viewToExperimentOneToOne($scope.experiment);
		updateATM($scope.experiment);
		$scope.$emit('childSaved', callbackFunction);
	});
	
	$scope.$on('refresh', function(e) {
		console.log("call event refresh");		
		var dtConfig = $scope.atmService.data.getConfig();
		dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));
		dtConfig.edit.showButton = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));
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
	
	$scope.updatePropertyFromUDT = function(value, col){
		console.log("update from property : " + col.property);
		
		if(col.property === 'outputContainerUsed.volume.value' || col.property === 'inputContainerUsed.experimentProperties.requiredQuantity.value'){
			computeRequiredVolume(value.data);
			computeInputVolume(value.data);
			computeInputQuantity(value.data);
			computeBufferVolume(value.data);
			updateVolUnquantifiableSamples(value.data);
		}		
	};

	$scope.$on('finishExperiment', function(event, e){ 
		$scope.atmService.data.displayResult.map(function(data) { return data.data; }).forEach(function(data) {
			// copy 'Qté réelle engagée ds FRG' to 'Quantité (ng)'
			data.outputContainerUsed.quantity = data.inputContainerUsed.experimentProperties.frgInputQuantity;
			data.outputContainerUsed.quantity.unit = $scope.atmService.defaultOutputUnit.quantity;
		});	
	});

	$scope.copyQuantityInToOut = function(){
		var data = $scope.atmService.data.displayResult;
		data.forEach(function(value){
			if (value.data.inputContainerUsed.quantity && value.data.inputContainerUsed.quantity.value !== null) { 
				if (!value.data.inputContainerUsed.experimentProperties) {
					value.data.inputContainerUsed.experimentProperties = {requiredQuantity: {}};
				}
				value.data.inputContainerUsed.experimentProperties.requiredQuantity.value = value.data.inputContainerUsed.quantity.value;
			}
		})		
	};
	
	var updateVolUnquantifiableSamples = function (udtData){
		var outputVol = $parse("outputContainerUsed.volume.value")(udtData);
		var vol = $parse("inputContainerUsed.volume.value")(udtData);
		var conc = $parse("inputContainerUsed.concentration.value")(udtData);

		var getter = $parse("inputContainerUsed.experimentProperties.bufferVolume.value");
		var bufferVol = getter(udtData);
		
		var getter2 = $parse("inputContainerUsed.experimentProperties.inputVolume.value");
		var inputVol = getter2(udtData);
		
		if (conc == null){
			$scope.messages.setError(Messages('experiments.input.warn.unquantifiableSample'));		
			console.log("UnquantifiableSample conc null");
			if (outputVol <= vol){
				inputVol = outputVol;	
			}else if(outputVol > vol){
				inputVol = vol;
			}
			bufferVol = outputVol - inputVol;
			getter.assign(udtData,bufferVol);
			getter2.assign(udtData,inputVol);
		}
	}
	
	// requiredQuantity * concentrationIn
	var computeRequiredVolume = function(udtData){
		var getter = $parse("inputContainerUsed.experimentProperties.requiredVolume.value");
		var requiredVolume = getter(udtData);
		
		var compute = {
				requiredQt : $parse("inputContainerUsed.experimentProperties.requiredQuantity.value")(udtData),
				inputConc : $parse("inputContainerUsed.concentration.value")(udtData),			
				isReady:function(){
					return (this.requiredQt && this.inputConc);
				}
			};
		
		if(compute.isReady()){
			var result = $parse("requiredQt / inputConc")(compute);
			console.log("result = "+result);
			if(angular.isNumber(result) && !isNaN(result)){
				requiredVolume = Math.round(result*10)/10;				
			}else{
				requiredVolume = undefined;
			}	
			getter.assign(udtData, requiredVolume);
		}else{
			console.log("not ready to computerequiredVolume");
			getter.assign(udtData, undefined);
		}		
	}
	
	
	//if requiredVol > inputVol then outputVol else requiredVol
	var computeInputVolume = function(udtData){
		var getter = $parse("inputContainerUsed.experimentProperties.inputVolume.value");
		var inputVolume = getter(udtData);
		
		var compute = {
				requiredVol : $parse("inputContainerUsed.experimentProperties.requiredVolume.value")(udtData),
				outputVol : $parse("outputContainerUsed.volume.value")(udtData),
				isReady:function(){
					return (this.requiredVol && this.outputVol);
				}
			};
		
		if(compute.isReady()){
			
			var result;
			if( compute.requiredVol> compute.outputVol){
				result=compute.outputVol;
			} else {
				result=compute.requiredVol;
			}
			
			console.log("result = "+result);
			if(angular.isNumber(result) && !isNaN(result)){
				inputVolume = Math.round(result*10)/10;				
			}else{
				inputVolume = undefined;
			}	
			getter.assign(udtData, inputVolume);
		}else{
			console.log("not ready to computeInputVolume");
			getter.assign(udtData, undefined);
		}
		
	}
	
	
	// inputVol * concIn 
	var computeInputQuantity = function(udtData){
		var getter = $parse("inputContainerUsed.experimentProperties.frgInputQuantity.value");
		var frgInputQuantity = getter(udtData);
		
		var compute = {
				inputConc : $parse("inputContainerUsed.concentration.value")(udtData),
				inputVol : $parse("inputContainerUsed.experimentProperties.inputVolume.value")(udtData),			
				isReady:function(){
					return (this.inputConc && this.inputVol);
				}
			};
		
		if(compute.isReady()){
			var result = $parse("(inputConc * inputVol)")(compute);
			console.log("result = "+result);
			if(angular.isNumber(result) && !isNaN(result)){
				frgInputQuantity = Math.round(result*10)/10;				
			}else{
				frgInputQuantity = undefined;
			}	
			getter.assign(udtData, frgInputQuantity);
		}else{
			console.log("not ready to computeInputQuantity");
			getter.assign(udtData, undefined);
		}
		
	}
	
	//vOut - inputVolume
	//outputContainerUsed.volume.value - inputContainerUsed.experimentProperties.inputVolume.value
	var computeBufferVolume = function(udtData){
		var getter = $parse("inputContainerUsed.experimentProperties.bufferVolume.value");
		var bufferVolume = getter(udtData);
		
		var compute = {
				inputVol : $parse("inputContainerUsed.experimentProperties.inputVolume.value")(udtData),			
				outputVol : $parse("outputContainerUsed.volume.value")(udtData),			
				isReady:function(){
					return (this.outputVol && this.inputVol);
				}
			};
		
		if(compute.isReady()){
			var result = $parse("(outputVol - inputVol)")(compute);
			console.log("result = "+result);
			if(angular.isNumber(result) && !isNaN(result)){
				bufferVolume = Math.round(result*10)/10;				
			}else{
				bufferVolume = undefined;
			}	
			getter.assign(udtData, bufferVolume);
		}else{
			console.log("not ready to computeBufferVolume");
			getter.assign(udtData, undefined);
		}
	}
	
	
	
	//Init		
	if($scope.experiment.instrument.inContainerSupportCategoryCode!=="tube"){
		datatableConfig.columns.push({
			"header" : Messages("containers.table.supportCode"),
			"property" : "inputContainer.support.code",
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
			"property" : "inputContainer.support.line",
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
			"property" : "inputContainer.support.column*1",
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
			"property" : "inputContainer.support.code",
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
	
	if($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube") {
		datatableConfig.columns.push({
			// barcode plaque sortie == support Container used code... faut Used
			"header" : Messages("containers.table.support.name"),
			"property" : "outputContainerUsed.locationOnContainerSupport.code",
			"hide" : true,
			"type" : "text",
			"position" : 400,
			"extraHeaders" : {
				0 : Messages("experiments.outputs")
			}
		});
		datatableConfig.columns.push({
			// Ligne
			"header" : Messages("containers.table.support.line"),
			"property" : "outputContainerUsed.locationOnContainerSupport.line",
			"edit" : true,
			"choiceInList":true,
			"possibleValues":[{"name":'A',"code":"A"},{"name":'B',"code":"B"},{"name":'C',"code":"C"},{"name":'D',"code":"D"},
			                  {"name":'E',"code":"E"},{"name":'F',"code":"F"},{"name":'G',"code":"G"},{"name":'H',"code":"H"}],
			"order" : true,
			"hide" : true,
			"type" : "text",
			"position" : 401,
			"extraHeaders" : {
				0 : Messages("experiments.outputs")
			}
		});
		datatableConfig.columns.push({// colonne
			"header" : Messages("containers.table.support.column"),
			// astuce GA: pour pouvoir trier les colonnes dans l'ordre naturel
			// forcer a numerique.=> type:number, property: *1
			"property" : "outputContainerUsed.locationOnContainerSupport.column",
			"edit" : true,
			"choiceInList":true,
			"possibleValues":[{"name":'1',"code":"1"},{"name":'2',"code":"2"},{"name":'3',"code":"3"},{"name":'4',"code":"4"},
			                  {"name":'5',"code":"5"},{"name":'6',"code":"6"},{"name":'7',"code":"7"},{"name":'8',"code":"8"},
			                  {"name":'9',"code":"9"},{"name":'10',"code":"10"},{"name":'11',"code":"11"},{"name":'12',"code":"12"}], 
			"order" : true,
			"hide" : true,
			"type" : "number",
			"position" : 402,
			"extraHeaders" : {
				0 : Messages("experiments.outputs")
			}
		});

	} else {
		datatableConfig.columns.push({
			"header" : Messages("containers.table.code"),
			"property" : "outputContainerUsed.code",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "text",
			"position" : 400,
			"extraHeaders" : {
				0 : Messages("experiments.outputs")
			}
		});
	}
	
	
	var atmService = atmToSingleDatatable($scope, datatableConfig);
	//defined new atomictransfertMethod
	/*atmService.newAtomicTransfertMethod =  function(line, column){
		var getLine = function(line){
			if($scope.experiment.instrument.outContainerSupportCategoryCode 
					=== $scope.experiment.instrument.inContainerSupportCategoryCode){
				return line;
			}else if($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube" 
				&& $scope.experiment.instrument.inContainerSupportCategoryCode === "tube") {
				return undefined;
			}else if($scope.experiment.instrument.outContainerSupportCategoryCode === "tube"){
				return "1";
			}
			
		}
		var getColumn=getLine;
		
		return {
			class:"OneToOne",
			line:getLine(line), 
			column:getColumn(column), 				
			inputContainerUseds:new Array(0), 
			outputContainerUseds:new Array(0)
		};
	};*/
	 atmService.newAtomicTransfertMethod =  function(line, column){
	        
	        return {
	            class:"OneToOne",
	            line:undefined,
	            column:undefined,                 
	            inputContainerUseds:new Array(0),
	            outputContainerUseds:new Array(0)
	        };
	    };
	//defined default output unit
	atmService.defaultOutputUnit = {
			volume : "µL",
			quantity : "ng"
	}
	
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	/*
	if($scope.experiment.instrument.typeCode !== "biomek-fx-and-covaris-e220"){
		if($scope.experiment.instrument.inContainerSupportCategoryCode === $scope.experiment.instrument.outContainerSupportCategoryCode){
			$scope.messages.clear();
			$scope.atmService = atmService;
		}else{
			$scope.messages.setError(Messages('experiments.input.error.must-be-same-out'));					
		}
	}else{
		$scope.messages.clear();
		$scope.atmService = atmService;
	}
	*/
	$scope.messages.clear();
	$scope.atmService = atmService;
	
	var generateSampleSheetNormalisation = function(){
		$scope.fileUtils.generateSampleSheet({"type":"normalisation"});
	};
	
	if($scope.experiment.instrument.typeCode === "biomek-fx-and-covaris-e220"){
		
		$scope.setAdditionnalButtons([{
			isDisabled : function(){return $scope.isNewState();} ,
			isShow:function(){return !$scope.isNewState();},
			click:generateSampleSheetNormalisation,
			label:Messages("experiments.sampleSheet")+" normalisation"
		}]);
	}
	
	
	
	
}]);