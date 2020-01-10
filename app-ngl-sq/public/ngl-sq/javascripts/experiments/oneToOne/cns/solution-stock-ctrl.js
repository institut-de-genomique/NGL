angular.module('home').controller('SolutionStockCtrl',['$scope' ,'$http','atmToSingleDatatable',
                                                       function($scope, $http,atmToSingleDatatable) {
	
	// NGL-1055: name explicite pour fichier CSV exporté: typeCode experience
	// NGL-1055: mettre getArray et codes '' dans filter et pas dans render
	var datatableConfig = {
			name: $scope.experiment.typeCode.toUpperCase(),
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
			        	 "filter":"unique| codes:'type'",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			 			 "render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	 "position":4,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			         {
			        	"header":Messages("containers.table.tags"),
			 			"property": "inputContainer.contents",
			 			"filter": "getArray:'properties.tag.value'| unique",
			 			"order":true,
			 			"hide":true,
			 			"type":"text",
			 			"position":4,
			 			"render":"<div list-resize='cellValue'  list-resize-min-size='3'>",
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
				 		 "header":Messages("containers.table.size"),
				 		 "property": "inputContainerUsed.size.value",
				 		 "order":false,
				 		 "hide":true,
				 		 "type":"text",
				 		 "position":6.5,
				 		 "extraHeaders":{0:Messages("experiments.inputs")}			 						 			
				 	 },
				 	{
				 		"header" : "Conc. convertie en nM", 
				 		"property" : "(inputContainerUsed.concentration.unit === 'ng/µl')?(inputContainerUsed.concentration.value / 660 / inputContainerUsed.size.value * 1000000):null",
				 		"type" : "number", 
			            "order" : true, 
			            "hide" : true, 
			           "format" : "2", 
			            "position":6.8,
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
			        	 "header":Messages("containers.table.concentration"),
			        	 "property":"outputContainerUsed.concentration.value",
			        	 "editDirectives":' udt-change="calculVolumes(value)" ',
			        	 "order":true,
						 "edit":true,
						 "hide":true,
			        	 "type":"number",
			        	 "defaultValues":10,
			        	 "position":50,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },
			         {
			        	 "header":Messages("containers.table.concentration.unit") ,
			        	 "property":"outputContainerUsed.concentration.unit",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "defaultValues":"nM",
			        	 "position":51,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },
			         {
			        	 "header":Messages("containers.table.volume")+ " (µL)",
			        	 "property":"outputContainerUsed.volume.value",
			        	 //utilisation de la directive utd-change car elle capture les modifications du header puis déclenche la function calculVolume 
			        	 // Si ng-change seul l'evenement utilisateur est capturé, la valeur de la cellule est modifiée mais le calcul non executé
			        	 "editDirectives":' udt-change="calculVolumes(value)" ',
			        	 "order":true,
						 "edit":true,
						 "hide":true,
						 "required":"isRequired()",
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
				number:1,
				dynamic:true,
			}
	};

	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event save");
		$scope.atmService.data.save();
		$scope.atmService.viewToExperimentOneToOne($scope.experiment);
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
	
	//WARNING Old systme to compute better used  updatePropertyFromUDT function see normalization
	var calculVolumeFromValue=function(value){
		console.log("call calculVolumeFromValue");
		if (value.inputContainerUsed.concentration === undefined 
				|| value.inputContainerUsed.concentration === null
				|| value.inputContainerUsed.concentration.value === undefined 
				|| value.inputContainerUsed.concentration.value === null ){				
			value.outputContainerUsed.concentration=undefined;				
		}
		
		
		if(value.inputContainerUsed.experimentProperties===undefined || value.inputContainerUsed.experimentProperties===null){
			value.inputContainerUsed.experimentProperties={};
		}
		if(value.outputContainerUsed.volume!=null && value.outputContainerUsed.volume.value!=null 
				&& value.outputContainerUsed.concentration != null && value.outputContainerUsed.concentration.value!=null){
			if(value.inputContainerUsed.concentration.unit===value.outputContainerUsed.concentration.unit){				
				var requiredVolume=value.outputContainerUsed.concentration.value*value.outputContainerUsed.volume.value/value.inputContainerUsed.concentration.value;
				requiredVolume = Math.round(requiredVolume*10)/10
				
				var bufferVolume = value.outputContainerUsed.volume.value-requiredVolume;
				bufferVolume = Math.round(bufferVolume*10)/10
				
				value.inputContainerUsed.experimentProperties["requiredVolume"]={"_type":"single","value":requiredVolume,"unit":"µl"};
				value.inputContainerUsed.experimentProperties["bufferVolume"]={"_type":"single","value":bufferVolume,"unit":"µl"};
				
			}else if(value.inputContainerUsed.concentration.unit==="ng/µl" 
				&& value.inputContainerUsed.size != null && value.inputContainerUsed.size.value != null) {
				var requiredVolume=value.outputContainerUsed.concentration.value*value.outputContainerUsed.volume.value/(value.inputContainerUsed.concentration.value*1000000/(660*value.inputContainerUsed.size.value));
				requiredVolume = Math.round(requiredVolume*10)/10
				
				var bufferVolume = value.outputContainerUsed.volume.value-requiredVolume;
				bufferVolume = Math.round(bufferVolume*10)/10
						
				value.inputContainerUsed.experimentProperties["requiredVolume"]={"_type":"single","value":requiredVolume,"unit":"µl"};
				value.inputContainerUsed.experimentProperties["bufferVolume"]={"_type":"single","value":bufferVolume,"unit":"µl"};
			}else{
				value.inputContainerUsed.experimentProperties["requiredVolume"]=null;
				value.inputContainerUsed.experimentProperties["bufferVolume"]=null;
			}
	    }else if(value.outputContainerUsed.volume!=null && value.outputContainerUsed.volume.value!=null){
	    	value.inputContainerUsed.experimentProperties["requiredVolume"]={"_type":"single","value":value.outputContainerUsed.volume.value,"unit":"µl"};
			value.inputContainerUsed.experimentProperties["bufferVolume"]={"_type":"single","value":0,"unit":"µl"};			
	    } else{
		   value.inputContainerUsed.experimentProperties["requiredVolume"]=null;
		   value.inputContainerUsed.experimentProperties["bufferVolume"]=null;
	   }
	}
		
	$scope.calculVolumes=function(value){
		if(value!=null & value !=undefined){
			calculVolumeFromValue(value.data);
	   }
	};
	
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
		
		datatableConfig.order.by = 'inputContainer.support.code';
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
			"edit" : false,
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
			"property" : "outputContainerUsed.locationOnContainerSupport.column",
			"edit" : false,
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
	atmService.newAtomicTransfertMethod = function(line, column){
		var getLine = function(line){
			if($scope.experiment.instrument.outContainerSupportCategoryCode === 'tube'){
				return "1";
			}else{
				return line;
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
			volume : "µL",
			concentration : "nM"
	};
	atmService.defaultOutputValue = {
			size : {copyInputContainer:true}
	};
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	if($scope.experiment.instrument.inContainerSupportCategoryCode === $scope.experiment.instrument.outContainerSupportCategoryCode){
		$scope.messages.clear();
		$scope.atmService = atmService;
	}else{
		$scope.messages.setError(Messages('experiments.input.error.must-be-same-out'));					
	}

	
	if("tecan-evo-100" === $scope.experiment.instrument.typeCode){
		$scope.setAdditionnalButtons([{
			isDisabled : function(){return $scope.isNewState();} ,
			isShow:function(){return !$scope.isNewState();},
			click:$scope.fileUtils.generateSampleSheet,
			label:Messages("experiments.sampleSheet")
		}]);
	}
	
	
}]);