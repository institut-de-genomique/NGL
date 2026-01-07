angular.module('home').controller('NucleiIsolationCtrl',['$scope', 'atmToGenerateMany',
                                                 function($scope, atmToGenerateMany) {
	
	var datatableConfigTubeParam = {
			name: $scope.experiment.typeCode + '_PARAM'.toUpperCase(),
			columns: [   
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
					"header":Messages("containers.table.outputNumberNuclei"),
					"property":"outputNumber",
					"order":false,
					"edit":true,
					"hide":false,
					"type":"number",						
					"position":8,
					"extraHeaders":{0:Messages("experiments.inputs")}
				}
				
			],
			compact: true,
			showTotalNumberRecords: false,
			pagination: {
				active: false
			},		
			search:{
				active: false
			},
			order: {
				mode: 'local', 
				active: true,
				by: 'inputContainer.code'
			},
			remove: {
				active: ($scope.isEditModeAvailable() && $scope.isNewState()),
				showButton: ($scope.isEditModeAvailable() && $scope.isNewState()),
				mode: 'local'
			},
			save: {
				active: true,
				withoutEdit: true,
				keepEdit: true,
				changeClass: false,
				mode: 'local',
				showButton: false
			},			
			select: {
				active:($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('IP'))
			},
			edit: {
				active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('IP')),
				showButton: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('IP')),
				byDefault: ($scope.isCreationMode()),
				columnMode: true
			},	
			cancel: {
				active: true
			},
			extraHeaders: {
				number: 1,
				dynamic: true,
			}
	};	

	var datatableConfigTubeConfig =  {
		name: $scope.experiment.typeCode.toUpperCase(),
		columns: [   	          
			{
				"header":Messages("containers.table.code"),
				"property":"inputContainer.code",
				"order":true,
				"position":1,
				"render":"<div list-resize='cellValue | stringToArray | unique' ' list-resize-min-size='2'>",
				"type":"text",
				"groupMethod":"collect",
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
				"position":6.5,
				"extraHeaders":{0:Messages("experiments.inputs")}
			},
			{
				"header":Messages("containers.table.quantity.unit"),
				"property":"inputContainerUsed.quantity.unit",
				"order":true,
				"edit":false,
				"hide":true,
				"type":"text",
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
				"position":9,
				"extraHeaders":{0:Messages("experiments.inputs")}
			},				        		        
			{
				"header":Messages("containers.table.volume")+" de sortie (µL)",
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
				"header":Messages("containers.table.containerComments"),
			        	 "property":"outputContainerUsed.comment.comment",
				"order":true,
				"edit":true,
				"hide":true,
				"type":"text",
				"position":700,
				"extraHeaders":{0:Messages("experiments.outputs")}
			}
		],
		compact: true,
		pagination: {
			active: false
		},		
		search: {
			active: false
		},
		order: {
			mode: 'local', 
			active: true,
			by: 'inputContainer.code'
		},
		remove: {
			active: false,
		},
		save: {
			active: true,
			withoutEdit: true,
			mode: 'local',
			showButton: false,
			changeClass: false
		},
		hide: {
			active: true
		},
		mergeCells:{
			active: true 
		},			
		edit:{
			active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
			showButton: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
			byDefault: ($scope.isCreationMode()),
			columnMode: true
		},
		messages:{
			active: false,
			columnMode: true
		},
		exportCSV:{
			active:- true,
			showButton: true,
			delimiter: ";",
			start: false
		},
		extraHeaders:{
			number: 1,
			dynamic: true,
		}
	};	

	$scope.$on('save', function(e, callbackFunction) {	
		$scope.atmService.viewToExperiment($scope.experiment);

		$scope.$emit('childSaved', callbackFunction);
	});
	
	
	$scope.$on('refresh', function(e) {		
		var dtConfig = $scope.atmService.data.datatableParam.getConfig();
		dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isNewState());
		dtConfig.remove.active = ($scope.isEditModeAvailable() && $scope.isNewState());
		dtConfig.edit.byDefault = false;

		$scope.atmService.data.datatableParam.setConfig(dtConfig);
		
		dtConfig = $scope.atmService.data.datatableConfig.getConfig();
		dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));
		dtConfig.edit.byDefault = false;

		$scope.atmService.data.datatableConfig.setConfig(dtConfig);
		
		$scope.atmService.refreshViewFromExperiment($scope.experiment);
		$scope.$emit('viewRefeshed');		
	});
	
	$scope.$on('cancel', function(e) {
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
		$scope.atmService.data.datatableParam.selectAll($scope.isEditModeAvailable() && $scope.isNewState());
		$scope.atmService.data.datatableParam.setEdit();
		
		$scope.atmService.data.datatableConfig.selectAll(true);
		$scope.atmService.data.datatableConfig.setEdit();
	});

	$scope.$watch("experiment.instrument.outContainerSupportCategoryCode", function(){
		$scope.experiment.instrument.outContainerSupportCategoryCode = "tube";
	});

	var atmService = atmToGenerateMany($scope, datatableConfigTubeParam, datatableConfigTubeConfig);

	atmService.newAtomicTransfertMethod = function(){
		return {
			class:"OneToMany",	
			line: "1", 
			column: "1",			
			inputContainerUseds:new Array(0), 
			outputContainerUseds:new Array(0)
		};		
	};

	atmService.defaultOutputUnit = {
		volume : "µL"
	};

	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	$scope.atmService = atmService;
}]);