angular.module('home').controller('DilutionCtrl',['$scope' ,'$http','$parse', 'atmToSingleDatatable',
                                                       function($scope, $http,$parse,atmToSingleDatatable) {
	var datatableConfig = {
			name:$scope.experiment.typeCode.toUpperCase(),
			columns:[			  
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
						 "filter":"unique | codes:'type'",
			        	 "type":"text",
			 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	 "position":4,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			         /*
			         {
			        	"header":Messages("containers.table.tags"),
			 			"property": "inputContainer.contents",
			 			"filter": "getArray:'properties.tag.value'",
			 			"order":true,
			 			"hide":true,
			 			"type":"text",
			 			"position":4,
			 			"render":"<div list-resize='cellValue | unique' ' list-resize-min-size='3'>",
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
					*/			 
					 {
			        	 "header" : function(){return Messages("containers.table.concentration") + " (ng/µl)"},
			 			 "property": "inputContainerUsed.concentration.value",
			 			 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":5,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			        /*
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
			         */
			         {
			        	 "header":function(){return Messages("containers.table.volume") + " (µL)"},
			        	 "property":"inputContainerUsed.volume.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":6,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			         {
			        	 "header":function(){return Messages("containers.table.quantity") + " (ng)"},
			        	 "property":"inputContainerUsed.quantity.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":6,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			        /*
			         {
				 			"header":Messages("containers.table.size"),
				 			"property": "inputContainerUsed.size.value",
				 			"order":false,
				 			"hide":true,
				 			"type":"text",
				 			"position":6.5,
				 			"extraHeaders":{0:Messages("experiments.inputs")}			 						 			
				 	 },
				 	 */
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
			        	 "header":Messages("containers.table.concentration") + " (ng/µl)",
			        	 "property":"outputContainerUsed.concentration.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
						 "watch":true,
			        	 "type":"number",
			        	 "format":"3",
			        	 "position":50,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },
			         /*
			         {
			        	 "header":Messages("containers.table.concentration.unit") ,
			        	 "property":"outputContainerUsed.concentration.unit",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":51,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },*/
			         {
			        	 "header":Messages("containers.table.volume")+ " (µL)",
			        	 "property":"outputContainerUsed.volume.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
						 "watch":true,
			        	 "type":"number",
			        	 "position":52,
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
	        	showButton:false,
	        	changeClass:false,
	        	mode:'local'
			},
			hide:{
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
				number:2,
				dynamic:true,
			},
			otherButtons: {
                active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
                complex:true,
                template:  ''
                	+$scope.plateUtils.templates.buttonLineMode()
                	+$scope.plateUtils.templates.buttonColumnMode()                	   
            }
			
	};

	var updateATM = function(experiment){
		if(experiment.instrument.outContainerSupportCategoryCode!=="tube"){
			experiment.atomicTransfertMethods.forEach(function(atm){
				atm.line = atm.outputContainerUseds[0].locationOnContainerSupport.line;
				atm.column = atm.outputContainerUseds[0].locationOnContainerSupport.column;
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
		
		datatableConfig.columns.push({
			"header" : Messages("containers.table.workName"),
			"property" : "inputContainer.properties.workName.value",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "text",
			"position" : 1.1,
			"extraHeaders" : {0 : Messages("experiments.inputs")}
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
	// defined new atomictransfertMethod
	atmService.newAtomicTransfertMethod =  function(line, column){
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
	};
	
	//defined default output unit
	atmService.defaultOutputUnit = {
			volume : "µL"
	};
	atmService.defaultOutputValue = {
			size : {copyInputContainer:true}
	};
	
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	$scope.atmService = atmService;
	
	$scope.updatePropertyFromUDT = function(value, col){
		console.log("update from property : "+col.property);
		
		if(col.property === 'inputContainerUsed.experimentProperties.inputVolume.value' ||
				col.property === 'inputContainerUsed.experimentProperties.dilutionFactor.value'){
			computeBufferVolume(value.data);
			computeFinalVolume(value.data);		
			computeOutputConcentration(value.data);		
		}
		
	}
	//cOut * vOut / cIn : 
	//outputContainerUsed.concentration.value * outputContainerUsed.volume.value / inputContainerUsed.concentration.value
	var computeBufferVolume = function(udtData){
		var getter = $parse("inputContainerUsed.experimentProperties.bufferVolume.value");
		var bufferVolume = getter(udtData);

		var compute = {
				dilFactor : (($parse("inputContainerUsed.experimentProperties.dilutionFactor.value")(udtData)).indexOf("1/") ==0 ? ($parse("inputContainerUsed.experimentProperties.dilutionFactor.value")(udtData)).substring(2) : undefined ) ,
				inputVol : $parse("inputContainerUsed.experimentProperties.inputVolume.value")(udtData),
				isReady:function(){
					return (this.inputVol && this.dilFactor);
				}
		};

		if(compute.isReady()){
			if ($parse("dilFactor")(compute) == 1){
				var result = 0;
			}else{
				var result = $parse("(inputVol * dilFactor) - inputVol")(compute);
			}
			console.log("computeBufferVolume result = "+result);
			if(angular.isNumber(result) && !isNaN(result)){
				bufferVolume = result;				
			}else{
				bufferVolume = undefined;
			}	
			getter.assign(udtData, bufferVolume);
		}else{
			bufferVolume = undefined;
			getter.assign(udtData, bufferVolume);
			console.log("not ready to computeBufferVolume");
		}

	}
	//inputContainerUsed.experimentProperties.inputVolume.value + inputContainerUsed.experimentProperties.bufferVolume.value
	var computeFinalVolume = function(udtData){
		var getter = $parse("outputContainerUsed.volume.value");
		var finalVolume = getter(udtData);

		var compute = {
				inputVol : $parse("inputContainerUsed.experimentProperties.inputVolume.value")(udtData),			
				bufferVol : $parse("inputContainerUsed.experimentProperties.bufferVolume.value")(udtData),	

				isReady:function(){
					return (angular.isNumber(this.inputVol) 
							&& angular.isNumber(this.bufferVol));
				}
		};

		if(compute.isReady()){
			var result = $parse("(inputVol + bufferVol)")(compute);
			if(angular.isNumber(result) && !isNaN(result)){
				finalVolume = result;				
			}else{
				finalVolume = undefined;
			}	
			getter.assign(udtData, finalVolume);
		}else{
			finalVolume = undefined;
			getter.assign(udtData, finalVolume);
			console.log("not ready to computeFinalVolume");
		}
	}
	
	var computeOutputConcentration = function(udtData){
		var getter = $parse("outputContainerUsed.concentration.value");
		var finalConc = getter(udtData);
		
		var compute = {
				finalVol : $parse("outputContainerUsed.volume.value")(udtData),			
				inputVol : $parse("inputContainerUsed.experimentProperties.inputVolume.value")(udtData),
				inputConc : $parse("inputContainerUsed.concentration.value")(udtData),
				isReady:function(){
					return (this.finalVol && this.inputVol && this.inputConc);
				}
			};
		
		if(compute.isReady()){
			var result = $parse("(inputVol * inputConc / finalVol)")(compute);
			console.log("finalConc result = "+result);
			if(angular.isNumber(result) && !isNaN(result)){
				finalConc = result;				
			}else{
				finalConc = undefined;
			}	
			getter.assign(udtData, finalConc);
		}else{
			finalConc = undefined;
			getter.assign(udtData, finalConc);
			console.log("not ready to computeOutputConcentration");
		}
		
	}
	
	var generateSampleSheetNormalisation = function(){
		$scope.fileUtils.generateSampleSheet({"type":"normalisation"});
	};
	var generateSampleSheetNormalisationPostPCR = function(){
		$scope.fileUtils.generateSampleSheet({"type":"normalisation-post-pcr"});
	};
	
	if($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube" 
		|| $scope.experiment.instrument.inContainerSupportCategoryCode !== "tube"){
		
		$scope.setAdditionnalButtons([{
			isDisabled : function(){return $scope.isNewState();} ,
			isShow:function(){return !$scope.isNewState();},
			click:generateSampleSheetNormalisation,
			label:Messages("experiments.sampleSheet")+" normalisation"
		},{
			isDisabled : function(){return $scope.isNewState();} ,
			isShow:function(){return !$scope.isNewState();},
			click:generateSampleSheetNormalisationPostPCR,
			label:Messages("experiments.sampleSheet")+" normalisation post PCR"
		}]);
	}
	
	
}]);