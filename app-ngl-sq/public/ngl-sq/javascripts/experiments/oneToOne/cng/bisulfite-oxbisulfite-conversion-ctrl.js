// 05/05/2020  copie de bisulfite-conversion.js
// 26/08/ supression prise en charge des plaques
angular.module('home').controller('BisulfiteOxbisulfiteConversionCtrl',['$scope', '$parse','atmToSingleDatatable','datatable',
                                                               function($scope,$parse, atmToSingleDatatable, datatable) {
	
	var inputExtraHeaders=Messages("experiments.inputs");
	var outputExtraHeaders=Messages("experiments.outputs");
	
	var datatableConfig = {
			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[
						//--------------------- INPUT containers section -----------------------
					{ //container code
						"header" : Messages("containers.table.code"),
						"property" : "inputContainer.support.code",
						"order" : true,
						"edit" : false,
						"hide" : true,
						"type" : "text",
						"position" : 1,
						"mergeCells" : true,
						"extraHeaders" : {0: inputExtraHeaders}
					},
					{ // Projet(s)
						"header":Messages("containers.table.projectCodes"),
						"property": "inputContainer.projectCodes",
						"order":true,
						"hide":true,
						"type":"text",
						"position":4,
						"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
						"extraHeaders":{0: inputExtraHeaders}
					},
					{ // Echantillon(s)
						"header":Messages("containers.table.sampleCodes"),
						"property": "inputContainer.sampleCodes",
						"order":true,
						"hide":true,
						"type":"text",
						"position":5,
						"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
						"extraHeaders":{0: inputExtraHeaders}
					},
					{ //sample Aliquots
						"header":Messages("containers.table.codeAliquot"),
						"property": "inputContainer.contents", 
						"filter": "getArray:'properties.sampleAliquoteCode.value'",
						"order":true,
						"hide":true,
						"type":"text",
						"position":6,
						"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
						"extraHeaders":{0: inputExtraHeaders}
					},
					{ // c'est la premiere experience du process utiliser processProperties; marche a new et en cours et plus a terminé, c'est normal !!!
					  // si filtre codes:'value' alors header =>libProcessType
						"header": Messages("containers.table.libProcessType"),
						"property" : "inputContainerUsed.contents",
						"filter" : "getArray:'processProperties.libProcessTypeCode.value' | unique | codes:'value'",
						"order":true,
						"edit":false,
						"hide":true,
						"type":"text",
						"position":6.5,
						"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
						"extraHeaders":{0:inputExtraHeaders}
					},
					{
						"header":Messages("containers.table.concentration") + " (ng/µl)",
						"property":"inputContainerUsed.concentration.value",
						"order":true,
						"edit":false,
						"hide":true,
						"type":"number",
						"position":7,
						"extraHeaders":{0: inputExtraHeaders}
					},
					{
						"header":Messages("containers.table.volume") + " (µL)",
						"property":"inputContainerUsed.volume.value",
						"order":true,
						"edit":false,
						"hide":true,
						"type":"number",
						"position":8,
						"extraHeaders":{0: inputExtraHeaders}
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
						"extraHeaders":{0: inputExtraHeaders}
					},
					//--->  colonnes specifiques experience s'inserent ici  (inputUsed ??)
					
					//------------------------- OUTPUT containers section --------------------------
					
					//--->  colonnes specifiques experience s'inserent ici  (outputUsed ??)
				    // GA: meme pour les tubes utiliser  x.locationOnContainerSupport.code  et pas x.code
					{
						"header":Messages("containers.table.code"),
						"property":"outputContainerUsed.locationOnContainerSupport.code",
						"order":true,
						"edit":true,
						"hide":true,
						"type":"text",
						"position":100,
						"extraHeaders":{0:outputExtraHeaders}
					},
					{
						"header":Messages("containers.table.stateCode"),
						"property":"outputContainer.state.code | codes:'state'",
						"order":true,
						"edit":false,
						"hide":true,
						"type":"text",
						"position":600,
						"extraHeaders":{0: outputExtraHeaders}
					},
					{
						"header":Messages("containers.table.storageCode"),
						"property":"outputContainerUsed.locationOnContainerSupport.storageCode",
						"order":true,
						"edit":true,
						"hide":true,
						"type":"text",
						"position":700,
						"extraHeaders":{0: outputExtraHeaders}
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
	        	changeClass:false,
	        	showButton:false,
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
			}
	};
	
	// colonnes variables
	// INPUT  26/05/2020 NON
	// OUTPUT 26/05/2020 NON
	
	datatableConfig.order.by = 'inputContainer.sampleCodes';
	
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
	
	// 26/08/2020 forcer tube en sortie 
	$scope.$watch("experiment.instrument.outContainerSupportCategoryCode", function(){
		$scope.experiment.instrument.outContainerSupportCategoryCode = "tube";
	});
	
	
	//Init 
	var atmService = atmToSingleDatatable($scope, datatableConfig);

	//defined new atomictransfertMethod.... l et C pour cas ou sortie de type variable
	atmService.newAtomicTransfertMethod = function(l,c){
		return {
			class:"OneToOne",
			// 20/02/2020 attention si tube en sortie forcer l=1 et c=1 !!!
			line:($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube")?undefined:"1",
			column:($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube")?undefined:"1",
			inputContainerUseds:new Array(0),
			outputContainerUseds:new Array(0)
		};
	};
	
	//defined default output unit
	atmService.defaultOutputUnit = {
			volume : "µL",
			concentration : "ng/µl",
			quantity : "ng"
	}
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	//26/08/2020 n'accepter que des tubes en input
	//  utilisation  $scope.plateFluoUtils.getSupportCategoryCode()
	//  $scope.plateFluoUtils est dans details-ctrl.js; details-ctrl.js est deja appelé/inclus par ????
	var inputSupportCategoryCode = $scope.plateFluoUtils.getSupportCategoryCode();
	// retourne 'tube', 'plate-96-well' ou 'mixte'
	
	if ( inputSupportCategoryCode !== "tube") {
		$scope.messages.setError(Messages("experiments.input.error.only-tubes"));
	} else {
		$scope.messages.clear();
		$scope.atmService = atmService;
	}
	
}]);