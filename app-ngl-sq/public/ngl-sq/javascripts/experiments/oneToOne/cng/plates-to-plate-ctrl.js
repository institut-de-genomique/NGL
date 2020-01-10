angular.module('home').controller('CNGPlatesToPlateCtrl',['$scope' ,'$http','$parse', 'mainService', 'atmToSingleDatatable',
	function($scope, $http, $parse, mainService, atmToSingleDatatable) {
	var datatableConfig = {
			name:$scope.experiment.typeCode.toUpperCase(),
			columns:[			  
				{
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
				},
				{
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
				},
				{
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
				},
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
				{
					"header" : Messages("containers.table.concentration"),
					"property": "inputContainerUsed.concentration.value",
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
					"header" : Messages("containers.table.quantity"),
					"property": "inputContainerUsed.quantity.value",
					"order":true,
					"edit":false,
					"hide":true,
					"type":"number",
					"position":6.1,
					"extraHeaders":{0:Messages("experiments.inputs")}
				},
				{
					"header":Messages("containers.table.quantity.unit"),
					"property":"inputContainerUsed.quantity.unit",
					"order":true,
					"edit":false,
					"hide":true,
					"type":"text",
					"position":6.2,
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
					"order":true,
					"edit":false,
					"hide":true,
					"type":"number",
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
					"position":51,
					"extraHeaders":{0:Messages("experiments.outputs")}
				},
				{
					"header":Messages("containers.table.volume")+ " (µL)",
					"property":"outputContainerUsed.volume.value",
					"order":true,
					"edit":true,
					"hide":true,
					"type":"number",
					"position":52,
					"extraHeaders":{0:Messages("experiments.outputs")}
				},
				/* NGL-2115 mises en commentaire car pas utilisees
			         {
			        	 "header":Messages("containers.table.quantity"),
			        	 "property":"outputContainerUsed.quantity.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":53,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },
			         {
			        	 "header":Messages("containers.table.quantity.unit") ,
			        	 "property":"outputContainerUsed.quantity.unit",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":54,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },
				 */
				{ // barcode plaque sortie == support Container used code... faut Used
					"header" : Messages("containers.table.support.name"),
					"property" : "outputContainerUsed.locationOnContainerSupport.code",
					"edit" : false, // NGL-2115 saisie sur la ligne dediée...
					"hide" : true,
					"type" : "text",
					"position" : 400,
					"extraHeaders" : {
						0 : Messages("experiments.outputs")
					}
				},
				{// Ligne
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
				},
				{// colonne
					"header" : Messages("containers.table.support.column"),
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
					mode:'local',
					callback:function(datatable){
						// TODO ?? NGL-2371 attention ici il faut tester le categoryCode en sortie...
						//atmService.copyContainerSupportCodeAndStorageCodeToDT(datatable,'userdef');
						copyContainerSupportCodeAndStorageCodeToDT(datatable);
					}
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
					template:''
						+$scope.plateUtils.templates.buttonLineMode()
						+$scope.plateUtils.templates.buttonColumnMode() 
						+'<div class="btn-group" style="margin-left:5px">'
						+'<button class="btn btn-default" ng-click="copyVolumeInToOut()" data-toggle="tooltip" title="'+Messages("experiments.button.plate.copyVolume")+'"  ng-disabled="!isEditMode()" ng-if="experiment.instrument.outContainerSupportCategoryCode!==\'tube\'"><i class="fa fa-files-o" aria-hidden="true"></i> Volume </button>'                	                	
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
	};

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

	$scope.copyVolumeInToOut = function(){
		var data = $scope.atmService.data.displayResult;		
		data.forEach(function(value){
			//21/12/2017 ne copier que si non null!!
			if ( null !== value.data.inputContainerUsed.volume) { 
				value.data.outputContainerUsed.volume = value.data.inputContainerUsed.volume;
			}
		})		
	};

	$scope.$watch("experiment.instrument.outContainerSupportCategoryCode", function(){
		// forcer le type de support en sortie
		$scope.experiment.instrument.outContainerSupportCategoryCode = "96-well-plate";

	});

	// 19/06/2018 NGL-2115
	// 14/03/2019 TODO ?? NGL-2371  remplacer par
	// "callback":function(datatable){
	//     atmService.copyContainerSupportCodeAndStorageCodeToDT( datatable,'userdef') 
	//}
	var copyContainerSupportCodeAndStorageCodeToDT = function(datatable){

		var dataMain = datatable.getData();
		var outputContainerSupportCode = $scope.outputContainerSupport.code;
		var outputContainerSupportStorageCode = $scope.outputContainerSupport.storageCode;

		// 14/03/2019 correction locale pour NGl-2371: ajout && "" !=outputContainerSupportCode
		if ( null != outputContainerSupportCode && undefined != outputContainerSupportCode  && "" != outputContainerSupportCode){
			for(var i = 0; i < dataMain.length; i++){

				// pas quand les boutons automatiques de choix mode colonne/ligne sont disponibles
				// var atm = dataMain[i].atomicTransfertMethod;
				// var newContainerCode = outputContainerSupportCode+"_"+atm.line + atm.column;
				// $parse('outputContainerUsed.code').assign(dataMain[i],newContainerCode);

				$parse('outputContainerUsed.locationOnContainerSupport.code').assign(dataMain[i],outputContainerSupportCode);

				if( null != outputContainerSupportStorageCode && undefined != outputContainerSupportStorageCode){
					$parse('outputContainerUsed.locationOnContainerSupport.storageCode').assign(dataMain[i],outputContainerSupportStorageCode);
				}
			}
		}
	}

	//Init
	var atmService = atmToSingleDatatable($scope, datatableConfig);

	// defined new atomictransfertMethod
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
			volume : "µL"
	};

	// copie automatique de concentration et size des inputs->outputs
	atmService.defaultOutputValue = {
			concentration : {copyInputContainer:true},
			size : {copyInputContainer:true}
	};

	atmService.experimentToView($scope.experiment, $scope.experimentType);

	// NGL-2115 ajout controles: les supports d'entree ne doivent etre QUE des plaques ( meme a la main...)
	// FDS 03/05/2018 corrections de code suite NGL-2484 ( voir library-prep-ctrl.js)
	if ( $scope.isCreationMode()){
		// !! en mode creation $scope.experiment.atomicTransfertMethod n'est pas encore chargé=> passer par Basket (ajouter mainService dans le controller)
		var categoryCodes = $scope.$eval("getBasket().get()|getArray:'support.categoryCode'|unique",mainService);
		var supports = $scope.$eval("getBasket().get()|getArray:'support.code'|unique",mainService);

		if ((categoryCodes.length === 1) && ( categoryCodes[0] ==="96-well-plate")){
			// plaques uniqt 
			// !!  il y a une limite pour le Janus
			if ( ($scope.experiment.instrument.typeCode === 'janus') && (supports.length > 8 )){ 
				$scope.messages.setError(Messages("experiments.input.error.maxSupports", 8) + " avec l instrument choisi");
				$scope.atmService = null; //empeche la page de se charger...
			}else{
				$scope.messages.clear();
				$scope.atmService = atmService;
			}

			// verifier qu'il n'y a pas plus de 96 inputs (il y a une seule plaque en sortie)
			if ( $scope.isCreationMode() && $scope.mainService.getBasket().length() > 96 ){ 
				$scope.messages.setError(Messages("experiments.input.error.maxContainers",96));
				$scope.atmService = null; //empeche la page de se charger...
			}else{
				$scope.messages.clear();
				$scope.atmService = atmService;
			}
		} else {
			$scope.messages.setError(Messages('experiments.input.error.only-plates')); 
			$scope.atmService = null; //empeche la page de se charger..
		}
	} else {
		$scope.atmService = atmService;
	}

	// NGL-2115 demande de ligne pour saisie plaque output ( et son stockage)
	$scope.outputContainerSupport = { code : null , storageCode : null};	

	if ( undefined !== $scope.experiment.atomicTransfertMethods[0]) { 
		$scope.outputContainerSupport.code=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.code;
		//console.log("previous code: "+ $scope.outputContainerSupport.code);
	}
	if ( undefined !== $scope.experiment.atomicTransfertMethods[0]) {
		$scope.outputContainerSupport.storageCode=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.storageCode;
		//console.log("previous storageCode: "+ $scope.outputContainerSupport.storageCode);
	}

	// NGL-2115 ajout FDR pour Janus
	$scope.setAdditionnalButtons([{
		isDisabled : function(){return $scope.isCreationMode();},
		isShow:function(){return ($scope.experiment.instrument.typeCode === 'janus')}, // FDS ne pas afficher bouton pour "hand"
		click: $scope.fileUtils.generateSampleSheet,
		label:Messages("experiments.sampleSheet") 
	}]);

}]);