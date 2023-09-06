angular.module('home').controller('AliquotingCtrl',['$scope', '$parse', 'atmToGenerateMany',
                                                               function($scope, $parse, atmToGenerateMany) {
	
	// NGL-1055: name explicite pour fichier CSV exporté: typeCode experience	
	var datatableConfigTubeParam = {
			//peut etre exporté CSV ??
			name: $scope.experiment.typeCode+'_PARAM'.toUpperCase(),
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
			        	 "header":Messages("containers.table.outputNumber"),
			        	 "property":"outputNumber",
			        	 "order":false,
						 "edit":true,
						 "hide":false,
			        	 "type":"number",						
			        	 "position":8,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         }
			         
			         ],
			compact:true,
			showTotalNumberRecords:false,
			pagination:{
				active:false
			},		
			search:{
				active:false
			},
			order:{
				mode:'local', //or 
				active:true,
				by:'inputContainer.code'
			},
			remove:{
				active: ($scope.isEditModeAvailable() && $scope.isNewState()),
				showButton: ($scope.isEditModeAvailable() && $scope.isNewState()),
				mode:'local'
			},
			save:{
				active:true,
				withoutEdit: true,
				keepEdit:true,
				changeClass : false,
				mode:'local',
				showButton:false
			},			
			select:{
				active:($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('IP'))
			},
			edit:{
				active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('IP')),
				showButton: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('IP')),
				byDefault:($scope.isCreationMode()),
				columnMode:true
			},	
			cancel : {
				active:true
			},
			extraHeaders:{
				number:1,
				dynamic:true,
			}

	};	
	
	// NGL-1055: mettre getArray et codes:'' dans filter et pas dans render
	// NGL-1055: name explicite pour fichier CSV exporté: typeCode experience
	var datatableConfigTubeConfig =  {
			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[   
					          
			         {
			        	"header":Messages("containers.table.projectCodes"),
			 			"property": "inputContainer.projectCodes",
			 			"order":true,
			 			"hide":true,
			 			"type":"text",
			 			"mergeCells" : true,
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
			 			"mergeCells" : true,
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
			        	 "mergeCells" : true,
			 			 "render":"<div list-resize='cellValue' list-resize-min-size='3'>",
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
						"type":"textr",
						"position":5.5,
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
			        	 "header":Messages("containers.table.quantity"),
			        	 "property":"inputContainerUsed.quantity.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":7,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
					{
					   "header":Messages("containers.table.quantity.unit"),
					   "property":"inputContainerUsed.quantity.unit",
					   "order":true,
					   "edit":false,
					   "hide":true,
					   "type":"text",
					   "position":8,
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
			        	 "position":9,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },			        
			         {
			        	 "header":Messages("containers.table.volume")+" (µL)",
			        	 "property":"outputContainerUsed.volume.value",
			        	 "editDirectives":" udt-change='updatePropertyFromUDT(value,col)' ",
			        	 "order":true,
						 "edit":true,
						 "hide":true,
						 "type":"number",
			        	 "position":51,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },
			         {
			        	 "header":Messages("containers.table.quantity"),
			        	 "property":"outputContainerUsed.quantity.value",
						 "editDirectives":" udt-change='updatePropertyFromUDT(value,col)' ",
			        	 "order":true,
						 "edit":true,
						 "hide":true,
						 "type":"number",
			        	 "position":52,
						 "watch":true,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },
					 {
						"header":Messages("containers.table.quantity.unit"),
						"property":"outputContainerUsed.quantity.unit",
						"order":true,
						"edit":true,
						"hide":true,
						"type":"text",
						"position":53,
						"watch":true,
						"choiceInList": true,
						"listStyle":"bt-select",
						"possibleValues":[{code:'fmol',name:'fmol'},{code:'ng',name:'ng'}],
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
			        	 "header":Messages("containers.table.comments"),
			        	 "property":"outputContainerUsed.comment.comment",
			        	 "order":false,
						 "edit":true,
						 "hide":true,
			        	 "type":"textarea",
			        	 "position":590,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },    
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
				by:'inputContainer.code'
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
                template:''
                	+$scope.plateUtils.templates.buttonLineMode('atmService.data.datatableConfig')
                	+$scope.plateUtils.templates.buttonColumnMode('atmService.data.datatableConfig')
			}

	};	
	
	//Init		
	if($scope.experiment.instrument.inContainerSupportCategoryCode!=="tube"){
		datatableConfigTubeConfig.columns.push({
			"header" : Messages("containers.table.supportCode"),
			"property" : "inputContainer.code",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "text",
			"position" : 1,
			"mergeCells" : true,
			"extraHeaders" : {
				0 : Messages("experiments.inputs")
			}
		});
		
		/*
		datatableConfigTubeConfig.columns.push({
			"header" : Messages("containers.table.supportCode"),
			"property" : "inputContainer.support.code",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "text",
			"position" : 1,
			"mergeCells" : true,
			"extraHeaders" : {
				0 : Messages("experiments.inputs")
			}
		});
		
		datatableConfigTubeConfig.columns.push({
			"header" : Messages("containers.table.support.line"),
			"property" : "inputContainer.support.line",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "text",
			"position" : 1.1,
			"mergeCells" : true,
			"extraHeaders" : {
				0 : Messages("experiments.inputs")
			}
		});
		datatableConfigTubeConfig.columns.push({
			"header" : Messages("containers.table.support.column"),
			"property" : "inputContainer.support.column*1",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "number",
			"position" : 1.2,
			"mergeCells" : true,
			"extraHeaders" : {
				0 : Messages("experiments.inputs")
			}
		});
		*/

	} else {
		datatableConfigTubeConfig.columns.push({
			"header" : Messages("containers.table.code"),
			"property" : "inputContainer.support.code",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "text",
			"position" : 1,
			"mergeCells" : true,
			"extraHeaders" : {
				0 : Messages("experiments.inputs")
			}
		});
		
		datatableConfigTubeConfig.order.by = 'inputContainer.sampleCodes';
	}
	
	if($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube") {
		datatableConfigTubeConfig.columns.push({
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
		
		datatableConfigTubeConfig.columns.push({
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
		datatableConfigTubeConfig.columns.push({// colonne
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
		datatableConfigTubeConfig.columns.push({
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
	
	
	var updateInputVolume = function(experiment){
		for(var i=0 ; i < experiment.atomicTransfertMethods.length ; i++){
			var atm = experiment.atomicTransfertMethods[i];
			
			var volume = {input:0};
			
			angular.forEach(atm.outputContainerUseds, function(output){
				this.input += Number(output.volume.value);
			}, volume);
			
			if(angular.isNumber(volume.input)){
				$parse('inputContainerUseds[0].experimentProperties["inputVolume"]').assign(atm, {value:volume.input, unit:"µL"});
			}
			//atm.inputContainerUseds[0].experimentProperties["inputVolume"] = {value:volume.input, unit:"µL"};
		}				
	};
	
	
	/**
	 * Update concentration. Copy input concentration to all outputs
	 */
	
	var updateConcentration = function(experiment){
		
		for(var j = 0 ; j < experiment.atomicTransfertMethods.length && experiment.atomicTransfertMethods != null; j++){
			var atm = experiment.atomicTransfertMethods[j];
			if(atm.inputContainerUseds[0].concentration !== null 
					&& atm.inputContainerUseds[0].concentration !== undefined){
				var concentration = atm.inputContainerUseds[0].concentration;				
				for(var i = 0 ; i < atm.outputContainerUseds.length ; i++){
					$parse("outputContainerUseds["+i+"].concentration").assign(atm, concentration);
				}
			}
			
		}		
	};
	
	
	var updateATM = function(experiment){
		if(experiment.instrument.outContainerSupportCategoryCode!=="tube"){
			experiment.atomicTransfertMethods.forEach(function(atm){
				atm.line = atm.outputContainerUseds[0].locationOnContainerSupport.line;
				atm.column = atm.outputContainerUseds[0].locationOnContainerSupport.column;
			});
		}		
	};
	
	
	$scope.updatePropertyFromUDT = function(value, col){
		var udtData = value.data;		
		if(isVolumeOut()){
			updateQuantityValue();			
		} else if(isQuantityOut()){
			updateQuantityUnit();			
		}

		//---

		function isVolumeOut(){
			return col.property === 'outputContainerUsed.volume.value';
		}

		function isQuantityOut(){
			return col.property === 'outputContainerUsed.quantity.value';
		}

		function getQuantity(){
			return $parse("outputContainerUsed.quantity")(udtData);
		}

		function setQuantity(quantity){
			return $parse("outputContainerUsed.quantity").assign(udtData, quantity);
		}

		function updateQuantityValue() {
			var quantity = getQuantity();
			var inputConc = getInputConcentration();
			var outputVol = getOutputVolume();
			if(isReadyToCompute()) {
				comuteQuantityValue();
			} else {
				clearQuantityValue();
			}

			//---

			function getInputConcentration(){
				return $parse("inputContainerUsed.concentration.value")(udtData);
			}

			function getOutputVolume(){
				return $parse("outputContainerUsed.volume.value")(udtData);
			}

			function isReadyToCompute() {
				return (inputConc && outputVol);
			}

			function comuteQuantityValue() {
				quantity.value = computeValue();
				setQuantity(quantity);
			}

			function clearQuantityValue() {
				quantity.value = null;
				console.log("not ready to quantity");
				setQuantity(quantity);
			}

			function computeValue() {
				var value = inputConc * outputVol;
				var roundedValue = Math.round(value*10)/10;
				return roundedValue;
			}
		}

		function updateQuantityUnit() {
			var quantity = getQuantity();
			var outputConcUnit = getQuantityUnit();
			if(outputConcUnit) doComputation();

			//---

			function getQuantityUnit(){
				return $parse("outputContainerUsed.concentration.unit")(udtData);
			}

			function doComputation() {
				quantity.unit = computeUnit();
				setQuantity(quantity);
			}

			function computeUnit() {
				return (outputConcUnit === 'nM') ? 'fmol' : 'ng';
			}
		}
		
	};
	
	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event save on tube-to-tubes");
		$scope.atmService.viewToExperiment($scope.experiment);
		updateInputVolume($scope.experiment);
		updateConcentration($scope.experiment);
		updateATM($scope.experiment);
		$scope.$emit('childSaved', callbackFunction);
	});
	
	
	$scope.$on('refresh', function(e) {
		console.log("call event refresh on tube-to-tubes");
		
		var dtConfig = $scope.atmService.data.datatableParam.getConfig();
		dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isNewState());
		dtConfig.remove.active = ($scope.isEditModeAvailable() && $scope.isNewState());
		dtConfig.edit.byDefault = false;
		$scope.atmService.data.datatableParam.setConfig(dtConfig);
		
		dtConfig = $scope.atmService.data.datatableConfig.getConfig();
		dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));
		//dtConfig.remove.active = ($scope.isEditModeAvailable() && $scope.isNewState());
		dtConfig.edit.byDefault = false;
		$scope.atmService.data.datatableConfig.setConfig(dtConfig);
		
		$scope.atmService.refreshViewFromExperiment($scope.experiment);
		$scope.$emit('viewRefeshed');
	});
	
	$scope.$on('cancel', function(e) {
		console.log("call event cancel");
		$scope.atmService.data.datatableParam.cancel();
		$scope.atmService.data.datatableConfig.cancel();
				
		if($scope.isCreationMode()){
			var dtConfig = $scope.atmService.data.datatableParam.getConfig();
			dtConfig.edit.byDefault = false;
			$scope.atmService.data.datatableParam.setConfig(dtConfig);
			
			dtConfig = $scope.atmService.data.datatableConfig.getConfig();
			dtConfig.edit.byDefault = false;
			$scope.atmService.data.datatableConfig.setConfig(dtConfig);
		}
	});
	
	$scope.$on('activeEditMode', function(e) {
		console.log("call event activeEditMode");
		$scope.atmService.data.datatableParam.selectAll($scope.isEditModeAvailable() && $scope.isNewState());
		$scope.atmService.data.datatableParam.setEdit();
		
		$scope.atmService.data.datatableConfig.selectAll(true);
		$scope.atmService.data.datatableConfig.setEdit();
	});
	
	var atmService = atmToGenerateMany($scope, datatableConfigTubeParam, datatableConfigTubeConfig);
	//defined new atomictransfertMethod
	atmService.newAtomicTransfertMethod = function(){
		return {
			class:"OneToMany",
			line:($scope.experiment.instrument.outContainerSupportCategoryCode!=="tube")?undefined:"1", 
			column:($scope.experiment.instrument.outContainerSupportCategoryCode!=="tube")?undefined:"1",				
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
}]);