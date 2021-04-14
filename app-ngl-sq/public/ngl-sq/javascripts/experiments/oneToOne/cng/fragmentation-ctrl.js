/* 30/06/2016 dupliqué a partir de pcr-and-purification-ctrl.js
   25/07/2017 Ne pas faire apparaitre les volumes.... */

angular.module('home').controller('FragmentationCNGCtrl',['$scope', '$parse', 'atmToSingleDatatable','mainService',
	function($scope, $parse, atmToSingleDatatable, mainService){
	// variables pour extraheaders
	var inputExtraHeaders=Messages("experiments.inputs");
	var outputExtraHeaders=Messages("experiments.outputs");	

	var datatableConfig = {
			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[	
				//--------------------- INPUT containers section -----------------------

				{ // barcode plaque entree == input support Container code
					"header":Messages("containers.table.support.name"),
					"property":"inputContainer.support.code",
					"hide":true,
					"type":"text",
					"position":1,
					"extraHeaders":{0: inputExtraHeaders}
				},    
				{ // Ligne
					"header":Messages("containers.table.support.line"),
					"property":"inputContainer.support.line",
					"order":true,
					"hide":true,
					"type":"text",
					"position":2,
					"extraHeaders":{0: inputExtraHeaders}
				},
				{ // colonne
					"header":Messages("containers.table.support.column"),
					// astuce GA: pour pouvoir trier les colonnes dans l'ordre naturel forcer a numerique.=> type:number,   property:  *1
					"property":"inputContainer.support.column*1",
					"order":true,
					"hide":true,
					"type":"number",
					"position":3,
					"extraHeaders":{0: inputExtraHeaders}
				},
				{ // Projet(s)
					"header":Messages("containers.table.projectCodes"),
					"property": "inputContainer.projectCodes",
					"order":true,
					"hide":true,
					"type":"text",
					"position":4,
					"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
					"extraHeaders":{0:inputExtraHeaders}
				},
				{ // Echantillon(s) 
					"header":Messages("containers.table.sampleCodes"),
					"property": "inputContainer.sampleCodes",
					"order":true,
					"hide":true,
					"type":"text",
					"position":5,
					"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
					"extraHeaders":{0:inputExtraHeaders}
				},
				{
					"header":Messages("containers.table.fromTransformationTypeCodes"),
					"property":"inputContainerUsed.fromTransformationTypeCodes",
					"filter":"unique | codes:'type'",
					"order":true,
					"edit":false,
					"hide":true,
					"type":"text",
					"render":"<div list-resize='cellValue'  list-resize-min-size='3'>",
					"position":6,
					"extraHeaders":{0:inputExtraHeaders}
				},
				{ // 31/08/2017 niveau process ET contents 
					// c'est la premiere experience du process utiliser processProperties; marche a new et en cours et plus a terminé, c'est normal !!!
					// si filtre codes:'value' alors header =>libProcessType
					"header": Messages("containers.table.libProcessType"),
					"property" : "inputContainerUsed.contents",
					"filter" : "getArray:'processProperties.libProcessTypeCode.value' | unique | codes:'value'",
					"order":true,
					"edit":false,
					"hide":true,
					"type":"text",
					"position":7,
					"extraHeaders":{0:inputExtraHeaders}
				},    
				{ // 31/08/2017 niveau process uniquement => processProperties;  marche a new et en cours et plus a terminé, c'est normal !!!
					"header": Messages("containers.table.expectedBaits"),
					"property" : "inputContainerUsed.contents",
					"filter" : "getArray:'processProperties.expectedBaits.value' | unique | codes:'value'",
					"order":true,
					"edit":false,
					"hide":true,
					"type":"text",
					"position":7.2,
					"extraHeaders":{0:inputExtraHeaders}
				},
				{ // 30/08/2017 niveau process ET contents 
					// c'est la premiere experience du process utiliser processProperties; marche a new et en cours et plus a terminé, c'est normal !!!
					"header": Messages("containers.table.captureProtocol"),
					"property" : "inputContainerUsed.contents",
					"filter" : "getArray:'processProperties.captureProtocol.value' | unique | codes:'value'",
					"order":true,
					"edit":false,
					"hide":true,
					"type":"text",
					"position":7.4,
					"extraHeaders":{0:inputExtraHeaders}
				},
				{ // Etat input Container
					"header":Messages("containers.table.state.code"),
					"property":"inputContainer.state.code",
					"order":true,
					"edit":false,
					"hide":true,
					"type":"text",
					"filter":"codes:'state'",
					"position":9,
					"extraHeaders":{0:inputExtraHeaders}
				},

				// colonnes specifiques experience viennent ici...

				//--------------------- OUTPUT containers section -----------------------
				{ //  barcode plaque sortie == support Container used 
					"header":Messages("containers.table.support.name"),
					"property":"outputContainerUsed.locationOnContainerSupport.code", 
					"hide":true,
					"type":"text",
					"position":500,
					"extraHeaders":{0: outputExtraHeaders}
				},  
				{ //  Ligne 
					"header":Messages("containers.table.support.line"),
					"property":"outputContainerUsed.locationOnContainerSupport.line", 
					"order":true,
					"hide":true,
					"type":"text",
					"position":600,
					"extraHeaders":{0: outputExtraHeaders}
				},     
				{ // colonne
					"header":Messages("containers.table.support.column"),
					// astuce GA: pour pouvoir trier les colonnes dans l'ordre naturel forcer a numerique.=> type:number,   property:  *1
					"property":"outputContainerUsed.locationOnContainerSupport.column*1", 
					"order":true,
					"hide":true,
					"type":"number",
					"position":700,
					"extraHeaders":{0: outputExtraHeaders}
				},	
				{
					"header":Messages("containers.table.stateCode"),
					"property":"outputContainer.state.code | codes:'state'",
					"order":true,
					"edit":false,
					"hide":true,
					"type":"text",
					"position":800,
					"extraHeaders":{0:outputExtraHeaders}
				},
				{
					"header":Messages("containers.table.storageCode"),
					"property":"outputContainerUsed.locationOnContainerSupport.storageCode",
					"order":true,
					"edit":false,
					"hide":true,
					"type":"text",
					"position":900,
					"extraHeaders":{0:outputExtraHeaders}
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
					mode:'local',
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
					mode:'local',
					callback:function(datatable){
						// NGL-2371 FDS 11/03/2019 copyContainerSupportCodeAndStorageCodeToDT deplacée dans atmService + ajout 2eme param "pos"
						// tous les instruments de cette exp n'ont que plaque en ouputContainer, inutile de le tester 
						// plaque inputContainer => pos='auto'
						atmService.copyContainerSupportCodeAndStorageCodeToDT(datatable,'auto');
					}
				},
				hide:{
					active:true
				},
				edit:{
					active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('IP')),
					showButton: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('IP')),
					byDefault:($scope.isCreationMode()),
					columnMode:true 
				},
				messages:{
					active:false
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
					/* 25/07/2017 pas necessaire tant que les volumes ne sont pas affichés
                active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
                complex:true,
                template:''
                	+'<div class="btn-group" style="margin-left:5px">'
                	+'<button class="btn btn-default" ng-click="copyVolumeInToExp()" data-toggle="tooltip" title="'+Messages("experiments.button.plate.copyVolumeTo")+' volume container de sortie'
                	+'" ng-disabled="!isEditMode()" ng-if="experiment.instrument.outContainerSupportCategoryCode!==\'tube\'"><i class="fa fa-files-o" aria-hidden="true"></i> Volume </button>'                	                	
                	+'</div>'
					 */
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
		dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('IP'));
		dtConfig.edit.byDefault = false;
		dtConfig.remove.active = ($scope.isEditModeAvailable() && $scope.isNewState());
		$scope.atmService.data.setConfig(dtConfig);
		$scope.atmService.refreshViewFromExperiment($scope.experiment);
		// NGL-2371 FDS 20/03/2019 récupérer outputContainerSupport s'il été généré automatiquement (pas de barcode entré par l'utilisateur)
		$scope.outputContainerSupport.code=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.code;

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

	// NGL-2371 FDS 11/03/2019 copyContainerSupportCodeAndStorageCodeToDT deplacee dans atomicTransfereService.js

	/* 25/07/2017 pas necessaire tant que les volumes ne sont pas demandés...
	   copier volume in vers volume out

	$scope.copyVolumeInToExp = function(){
		console.log("copyVolumeInToExp");

		var data = $scope.atmService.data.displayResult;		
		data.forEach(function(value){
			$parse("outputContainerUsed.volume").assign(value.data, angular.copy(value.data.inputContainer.volume));			
		})		
	};
	 */

	// En fragmentation le covaris n'est utilisé qu'en mode plaque
	$scope.$watch("experiment.instrument.outContainerSupportCategoryCode", function(){
		$scope.experiment.instrument.outContainerSupportCategoryCode = "96-well-plate";
	});

	//Init
	var atmService = atmToSingleDatatable($scope, datatableConfig);

	//defined new atomictransfertMethod
	atmService.newAtomicTransfertMethod = function(l,c){
		return {
			class:"OneToOne",
			line: l, 
			column: c, 				
			inputContainerUseds:new Array(0), 
			outputContainerUseds:new Array(0)
		};
	};


	//defined default output unit
	atmService.defaultOutputUnit = {
			volume : "µL",
			concentration : "nM"
	}

	atmService.experimentToView($scope.experiment, $scope.experimentType);

	// 28/08/2017 OK countInputSupportCodes
	if ( $scope.countInputSupportCodes() > 1) {
		console.log(" > 1 support en entree");

		$scope.messages.clear();
		$scope.messages.clazz = "alert alert-danger";
		$scope.messages.text = Messages("experiments.input.error.only-1-plate");
		$scope.messages.showDetails = false;
		$scope.messages.open();
	} else {
		$scope.atmService = atmService;
	}	

	$scope.outputContainerSupport = { code : null , storageCode : null};	

	if ( undefined !== $scope.experiment.atomicTransfertMethods[0]) { 
		$scope.outputContainerSupport.code=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.code;
		//console.log("previous code: "+ $scope.outputContainerSupport.code);
	}
	if ( undefined !== $scope.experiment.atomicTransfertMethods[0]) {
		$scope.outputContainerSupport.storageCode=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.storageCode;
		//console.log("previous storageCode: "+ $scope.outputContainerSupport.storageCode);
	}



}]);