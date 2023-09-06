angular.module('home').controller('MGISSCircularisationCtrl',['$scope', '$parse', '$filter', 'atmToSingleDatatable','lists','mainService',
                                                       function($scope, $parse, $filter, atmToSingleDatatable,lists,mainService) {


	var datatableConfig =  {
			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[ 
			         {
			        	 "header":Messages("containers.table.projectCodes"),
			        	 "property": "inputContainer.projectCodes",
			        	 "order": true,
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
			        	 "filter" : "unique | codes:'type'",
			        	 "order":true,
			        	 "edit":false,
			        	 "hide":true,
			        	 "type":"text",
			        	 "render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	 "position":4,
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
			        	 "position":5,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },		
			         {
				        	"header":Messages("containers.table.tags"),
				 			"property": "inputContainer.contents",
				 			"filter": "getArray:'properties.tag.value'| unique",
				 			"order":true,
				 			"hide":true,
				 			"type":"text",
				 			"position":6,
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
				 			"position":7,
				 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
				        	 "extraHeaders":{0:Messages("experiments.inputs")}
				     },
			         {
			        	 "header":Messages("containers.table.volume") + " (µl)",
			        	 "property":"inputContainerUsed.volume.value",
			        	 "order":true,
			        	 "edit":false,
			        	 "hide":true,
			        	 "type":"number",
			        	 "position":8,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			         {
				 		 "header":Messages("containers.table.size") + "(pb)",
				 		 "property": "inputContainerUsed.size.value",
				 		 "order":false,
				 		 "hide":true,
				 		 "type":"number",
				 		 "position":9,
				 		 "extraHeaders":{0:Messages("experiments.inputs")}			 						 			
				 	 },
				 	 {
			        	 "header" : function(){return Messages("containers.table.concentration")},
			 			 "property": "inputContainerUsed.concentration.value",
			 			 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":10,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			         {
			        	 "header":Messages("containers.table.concentration.unit"),
			        	 "property":"inputContainerUsed.concentration.unit",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":11,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			         {
					 		"header" : "Conc. convertie en fmol/µl", 
					 		"property" : "(inputContainerUsed.concentration.unit === 'ng/µl')?(inputContainerUsed.concentration.value / 660 / inputContainerUsed.size.value * 1000000):null",
					 		"type" : "number", 
				            "order" : true, 
				            "hide" : true, 
				           "format" : "2", 
				            "position":12,
				        	"extraHeaders":{0:Messages("experiments.inputs")}
				     },
			         {
			        	 "header":Messages("containers.table.stateCode"),
			        	 "property":"outputContainer.state.code | codes:'state'",
			        	 "order":true,
			        	 "edit":false,
			        	 "hide":true,
			        	 "type":"text",
			        	 "position":41,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },
			         {
			        	 "header":Messages("containers.table.storageCode"),
			        	 "property":"outputContainerUsed.locationOnContainerSupport.storageCode",
			        	 "order":true,
			        	 "edit":true,
			        	 "hide":true,
			        	 "type":"text",
			        	 "position":42,
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
			        	 by:'inputContainer.sampleCodes'
			         },
			         remove:{
			        	 active:false,
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
		console.log("call event save on dna-extraction");
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
		datatableConfig.order.by = 'inputContainer.sampleCodes';

	}

	if($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube") {
		datatableConfig.columns.push({
			// barcode plaque sortie == support Container used code... faut Used
			"header" : Messages("containers.table.support.name"),
			"property" : "outputContainerUsed.locationOnContainerSupport.code",
			"hide" : true,
			"type" : "text",
			"position" : 30,
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
			                  "position" : 31,
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
			                  "position" : 32,
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
			"position" : 33,
			"extraHeaders" : {
				0 : Messages("experiments.outputs")
			}
		});
	}


	var atmService = atmToSingleDatatable($scope, datatableConfig);
	//defined new atomictransfertMethod
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
	atmService.experimentToView($scope.experiment, $scope.experimentType);					
	$scope.atmService = atmService;

	$scope.updatePropertyFromUDT = function(value, col){
		if(col.property === 'inputContainerUsed.experimentProperties.inputQuantitySsCircu.value' || col.property === 'inputContainerUsed.experimentProperties.requiredVolume.value'){
			//calcul containerIn inputVolume avec concentration ou concentration convertie
			console.log("computeInputVolume");
			var computeInputVolume=undefined;
			var inputContainer = $parse("inputContainerUsed")(value.data);
			var outputContainer = $parse("outputContainerUsed")(value.data);
			var fromTransfoType = $parse ("inputContainerUsed.fromTransformationTypeCodes[0]")(value.data);
			
			//Calcul inputVolume and bufferVolume si from different bq index. mgi
			if(inputContainer.concentration!=undefined && inputContainer.concentration!=null && fromTransfoType != "mgi-indexed-library"){
			
				if(inputContainer.concentration.unit==="fmol/µl"){	
					computeInputVolume=inputContainer.experimentProperties.inputQuantitySsCircu.value / inputContainer.concentration.value;
				}else if(inputContainer.concentration.unit==="ng/µl"){
					computeInputVolume=inputContainer.experimentProperties.inputQuantitySsCircu.value / (inputContainer.concentration.value*1000000/(660*inputContainer.size.value));
				}			
				if(computeInputVolume!=undefined){
					computeInputVolume = Math.round(computeInputVolume*10)/10;
					//calcul bufferedVolume a partir inputVolume
					//Valeur 0 si pool
					if(inputContainer.experimentProperties.requiredVolume!=undefined){
						var bufferedVolume=inputContainer.experimentProperties.requiredVolume.value-computeInputVolume;
						bufferedVolume = Math.round(bufferedVolume*10)/10;
						$parse("inputContainerUsed.experimentProperties.bufferVolume.value").assign(value.data,bufferedVolume);
					}
			
					$parse("inputContainerUsed.experimentProperties.inputVolume.value").assign(value.data,computeInputVolume);
					
					
				}
			}
			
		}
		if(col.property === 'outputContainerUsed.experimentProperties.finalVolume.value'){
			//Copie valeur dans attribut volume container
			var outputContainer = $parse("outputContainerUsed")(value.data);
			$parse("outputContainerUsed.volume.value").assign(value.data, outputContainer.experimentProperties.finalVolume.value);	
			$parse("outputContainerUsed.volume.unit").assign(value.data, "µl");	
		}
		
	}
	
	
	atmService.defaultOutputValue = {
			size : {copyInputContainer:true}
	};
	
	atmService.defaultOutputUnit = {
			size : "nt"
	};
	
}]);