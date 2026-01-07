angular.module('home').controller('GrindingCtrl',['$scope', '$parse', 'atmToGenerateMany',
                                                               function($scope, $parse, atmToGenerateMany) {
	
	// NGL-1055: name explicite pour fichier CSV exporté: typeCode experience	
	var datatableConfigTubeParam = {
			// peut etre exporté CSV ??
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
						"header":Messages("containers.table.projectCodes"),
						"property":"inputContainer.projectCodes",
						"order":false,
						"edit":false,
						"hide":false,
						"type":"text",						
						"position":2,
						"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
						"extraHeaders":{0:Messages("experiments.inputs")}
					},
					{
						"header":Messages("containers.table.sampleCodes"),
						"property":"inputContainer.sampleCodes",
						"order":false,
						"edit":false,
						"hide":false,
						"type":"text",						
						"position":2,
						"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
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
				by:'inputContainer.sampleCodes'
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
				active:true
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
	
	// NGL-1055: name explicite pour fichier CSV exporté: typeCode experience
	// NGL-1055: mettre getArray et codes:'' dans filter et pas dans render
	var datatableConfigTubeConfig =  {	
			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[   
					 {
			        	 "header":Messages("containers.table.code"),
			        	 "property":"inputContainer.code",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "mergeCells" : true,
			        	 "position":1,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },		         
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
						"header":Messages("containers.table.volume") + " (µL)",
						"property":"inputContainerUsed.volume.value",
						"order":true,
						"edit":false,
						"hide":true,
						"type":"number",
						"mergeCells" : true,
						"position":4,
						"extraHeaders":{0:Messages("experiments.inputs")}
					},
					{
						"header":Messages("containers.table.quantity"),
						"property":"inputContainerUsed.quantity.value",
						"order":true,
						"edit":false,
						"hide":true,
						"type":"number",
						"mergeCells" : true,
						"position":5,
						"extraHeaders":{0:Messages("experiments.inputs")}
					},
					{
						"header":Messages("containers.table.quantity.unit"),
						"property":"inputContainerUsed.quantity.unit",
						"order":true,
						"edit":false,
						"hide":true,
						"type":"text",
						"mergeCells" : true,
						"position":6,
						"extraHeaders":{0:Messages("experiments.inputs")}
					},
					{
						"header":Messages("containers.properties.eukNbCells"),
						"property":"properties.eukNbCells.value",
						"order":true,
						"url":"'/api/containers/'+inputContainer.code",
						"edit":false,
						"hide":true,
						"type":"number",
						"mergeCells" : true,
						"position":6.5,
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
						"header":Messages("containers.table.quantitySortie") + " (mg)",
						"property":"outputContainerUsed.quantity.value",
						"order":true,
						"edit":true,
						"hide":true,
						"type":"number",
						"position":50,
						"extraHeaders":{0:Messages("experiments.outputs")}
					},
			         {
			        	 "header":Messages("containers.table.volumeSortie")+" (µL)",
			        	 "property":"outputContainerUsed.volume.value",
			        	 "order":true,
						 "edit":true,
						 "hide":true,
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
				active: true,
				complex:true,
				template:  ''
					+'<div class="btn-group" style="margin-left:5px">'
						+'<button class="btn btn-default" ng-click="copyVolumeInToInput()" data-toggle="tooltip" title="'+Messages("experiments.button.plate.copyVolumeToInput")+'"  ng-disabled="!isEditMode()"><i class="fa fa-files-o" aria-hidden="true"></i> '+Messages("experiments.button.plate.copyVolumeToInput.title")+'</button>'                	                	
					+'</div>'
					+'<div class="btn-group" style="margin-left:5px">'
						+'<button class="btn btn-default" ng-click="copyQuantityInToInput()" data-toggle="tooltip" title="'+Messages("experiments.button.plate.copyQuantity.title")+'"  ng-disabled="!isEditMode()"><i class="fa fa-files-o" aria-hidden="true"></i> '+Messages("experiments.button.plate.copyQuantity")+'</button>'                	                	
					+'</div>'
			}

	};	

	$scope.copyVolumeInToInput = function(){
		var data = $scope.atmService.$atmToSingleDatatable.data.displayResult; 

		data.forEach(function(value){
			if (value.data.inputContainerUsed.volume && value.data.inputContainerUsed.volume.value !== null) { 
				if (!value.data.inputContainerUsed.experimentProperties) {
					value.data.inputContainerUsed.experimentProperties = {}
				}
				
				if (!value.data.inputContainerUsed.experimentProperties.grindingInputVolume) {
					value.data.inputContainerUsed.experimentProperties.grindingInputVolume = {}
				}

				value.data.inputContainerUsed.experimentProperties.grindingInputVolume.value = value.data.inputContainerUsed.volume.value;
			}
		});
	};

	$scope.copyQuantityInToInput = function() {
		var data = $scope.atmService.$atmToSingleDatatable.data.displayResult;

		data.forEach(function(value) {
			if (value.data.inputContainerUsed.quantity && value.data.inputContainerUsed.quantity.value !== null) { 
				if (!value.data.inputContainerUsed.experimentProperties) {
					value.data.inputContainerUsed.experimentProperties = {}
				}

				if (value.data.inputContainerUsed.experimentProperties && !value.data.inputContainerUsed.experimentProperties.grindingInputQuantity) {
					value.data.inputContainerUsed.experimentProperties.grindingInputQuantity = {};
				}
				
				value.data.inputContainerUsed.experimentProperties.grindingInputQuantity.value = value.data.inputContainerUsed.quantity.value;
			}
		});
	};
	
	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event save on tube-to-tubes");
		
		$scope.atmService.viewToExperiment($scope.experiment);

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
		dtConfig.edit.showButton = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));
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
		$scope.atmService.data.datatableParam.selectAll(true);
		$scope.atmService.data.datatableParam.setEdit();
		
		$scope.atmService.data.datatableConfig.selectAll(true);
		$scope.atmService.data.datatableConfig.setEdit();
	});
	
	var atmService = atmToGenerateMany($scope, datatableConfigTubeParam, datatableConfigTubeConfig);
	//defined new atomictransfertMethod
	atmService.newAtomicTransfertMethod = function(){
		return {
			class:"OneToMany",
			line:"1", 
			column:"1", 				
			inputContainerUseds:new Array(0), 
			outputContainerUseds:new Array(0)
		};		
	};
	
	//defined default output unit
	atmService.defaultOutputUnit = {
			volume : "µL",
			quantity:"mg"
	}
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	$scope.atmService = atmService;
}]);