// 30/01/2020
angular.module('home').controller('QmpLibPrepCtrl',['$scope', '$parse','atmToSingleDatatable','datatable',
                                                               function($scope,$parse, atmToSingleDatatable, datatable) {
	
	var inputExtraHeaders=Messages("experiments.inputs");
	var outputExtraHeaders=Messages("experiments.outputs");
	
	var datatableConfig = {
			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[
					//--------------------- INPUT containers section -----------------------
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
					{ // Type processus banque
						"header": Messages("containers.table.libProcessType"),
						"property" : "inputContainer.contents",
						"filter":"getArray:'properties.libProcessTypeCode.value'| unique | codes:'value'",
						"order":true,
						"edit":false,
						"hide":true,
						"type":"text",
						"position":7,
						"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
						"extraHeaders":{0:inputExtraHeaders}
					}, 
					{ // Issu du type d'expérience
						"header":Messages("containers.table.fromTransformationTypeCodes"),
						"property":"inputContainer.fromTransformationTypeCodes",
						"filter":"unique | codes:'type'",
						"hide":true,
						"order":false,
						"type":"text",
						"position":8,
						"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
						"extraHeaders":{0:inputExtraHeaders}
					},	
					{ // Etat input Container 
						"header":Messages("containers.table.state.code"),
						"property":"inputContainer.state.code",
						"filter":"codes:'state'",
						"order":true,
						"edit":false,
						"hide":true,
						"type":"text",
						"position":9,
						"extraHeaders":{0: inputExtraHeaders}
					},
					//--->  colonnes specifiques experience s'inserent ici  (inputUsed ??)
					
					//------------------------- OUTPUT containers section --------------------------
					
					//--->  colonnes specifiques experience s'inserent ici  (outputUsed ??)
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
			},
			// ajouter le test du type de sortie
			otherButtons: {
                active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F') && ($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube")),
                complex:true,
                template:''
                	+$scope.plateUtils.templates.buttonLineMode()
                	+$scope.plateUtils.templates.buttonColumnMode()
                	+$scope.plateUtils.templates.buttonCopyPosition()
			}
	};
	
	// colonnes variables
	// INPUT 
	
	// 20/02/2020 utilisation   $scope.plateFluoUtils.getSupportCategoryCode()
	//  $scope.plateFluoUtils est dans details-ctrl.js; details-ctrl.js est deja appelé/inclus par ????
	var inputSupportCategoryCode = $scope.plateFluoUtils.getSupportCategoryCode();
	// retourne 'tube', 'plate-96-well' ou 'mixte'
	
	if ( inputSupportCategoryCode !== "tube") {
		datatableConfig.columns.push({
			"header":Messages("containers.table.supportCode"),
			"property":"inputContainer.support.code",
			"hide":true,
			"type":"text",
			"position":1,
			"extraHeaders":{0: inputExtraHeaders}
		});
		datatableConfig.columns.push({
			// Ligne
			"header":Messages("containers.table.support.line"),
			"property":"inputContainer.support.line",
			"order":true,
			"hide":true,
			"type":"text",
			"position":2,
			"extraHeaders":{0: inputExtraHeaders}
		});
		datatableConfig.columns.push({
			// colonne
			"header":Messages("containers.table.support.column"),
			// astuce GA: pour pouvoir trier les colonnes dans l'ordre naturel forcer a numerique.=> type:number,   property:  *1
			"property":"inputContainer.support.column*1",
			"order":true,
			"hide":true,
			"type":"number",
			"position":3,
			"extraHeaders":{0: inputExtraHeaders}
		});
		
		// laisser le order par defaut
		
	} else {
		datatableConfig.columns.push({
			"header" : Messages("containers.table.code"),
			"property" : "inputContainer.support.code",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "text",
			"position" : 1,
			"mergeCells" : true,
			"extraHeaders" : {0: inputExtraHeaders}
		});
		
		datatableConfig.order.by = 'inputContainer.sampleCodes';
	}
	
	// OUTPUT
	if ( $scope.experiment.instrument.outContainerSupportCategoryCode !== "tube" ){	
		 datatableConfig.columns.push({
			// barcode plaque sortie == support Container used code... faut Used 
			"header":Messages("containers.table.support.name"),
			"property":"outputContainerUsed.locationOnContainerSupport.code", 
			"order":true,
			"edit":true,
			"hide":true,
			"type":"text",
			"position":100,
			"extraHeaders":{0: outputExtraHeaders}
		});
		datatableConfig.columns.push({
			// Line
			"header":Messages("containers.table.support.line"),
			"property":"outputContainerUsed.locationOnContainerSupport.line", 
			"edit" : true,
			"choiceInList":true,
			"possibleValues":[{"name":'A',"code":"A"},{"name":'B',"code":"B"},{"name":'C',"code":"C"},{"name":'D',"code":"D"},
			                  {"name":'E',"code":"E"},{"name":'F',"code":"F"},{"name":'G',"code":"G"},{"name":'H',"code":"H"}],
			"order":true,
			"hide":true,
			"type":"text",
			"position":110,
			"extraHeaders":{0:outputExtraHeaders}
		});
		datatableConfig.columns.push({
			// column
			"header":Messages("containers.table.support.column"),
			"property":"outputContainerUsed.locationOnContainerSupport.column",
			"edit" : true,
			"choiceInList":true,
			"possibleValues":[{"name":'1',"code":"1"},{"name":'2',"code":"2"},{"name":'3',"code":"3"},{"name":'4',"code":"4"},
			                  {"name":'5',"code":"5"},{"name":'6',"code":"6"},{"name":'7',"code":"7"},{"name":'8',"code":"8"},
			                  {"name":'9',"code":"9"},{"name":'10',"code":"10"},{"name":'11',"code":"11"},{"name":'12',"code":"12"}], 
			"order":true,
			"hide":true,
			"type":"number",
			"position":120,
			"extraHeaders":{0:outputExtraHeaders}
		});
	} else {
		// tube
		// GA: meme pour les tubes utiliser  x.locationOnContainerSupport.code  et pas x.code
		datatableConfig.columns.push({
			"header":Messages("containers.table.code"),
			"property":"outputContainerUsed.locationOnContainerSupport.code",
			"order":true,
			"edit":true,
			"hide":true,
			"type":"text",
			"position":100,
			"extraHeaders":{0:outputExtraHeaders}
		});
	}
	
	// pour gestion des plaques en sortie
	var updateATM = function(experiment){
		if(experiment.instrument.outContainerSupportCategoryCode !== "tube"){
			experiment.atomicTransfertMethods.forEach(function(atm){
				atm.line = atm.outputContainerUseds[0].locationOnContainerSupport.line;
				atm.column = atm.outputContainerUseds[0].locationOnContainerSupport.column;
			});
		}		
	};
	
	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event save1");
		$scope.atmService.data.save();
		$scope.atmService.viewToExperimentOneToOne($scope.experiment);
		// pour gestion des plaques en sortie
		updateATM($scope.experiment);
		
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
	
	// Init
	var atmService = atmToSingleDatatable($scope, datatableConfig);
	
	//defined new atomictransfertMethod...l et c pour cas ou sortie de type variable 
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
	
	// empecher la sortie en strip-8
	if($scope.experiment.instrument.outContainerSupportCategoryCode === 'strip-8'){
		$scope.messages.setError(Messages("experiments.output.error.supportCategory","strip 8"));
	} else {
		//empecher l'entree d'autre chose que des plaques...
		if ((inputSupportCategoryCode==="tube" || inputSupportCategoryCode==="mixte" )&& ($scope.experiment.instrument.typeCode==="tecan-evo-150")){
			$scope.messages.setError(Messages("experiments.input.error.only-plates")+ ' si vous utilisez cet instrument');
		} else {
			// il faudrait aussi verifier qu'il n'y a qu'une seule plaque ????==> experiments.input.error.only-1-plate
			// le tecan peut-il prendre plusieurs plaques en entree ???
			
			$scope.messages.clear();
			$scope.atmService = atmService;
		}
	}
}]);