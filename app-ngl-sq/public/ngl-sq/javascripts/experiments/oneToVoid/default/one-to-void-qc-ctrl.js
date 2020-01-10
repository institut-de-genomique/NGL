angular.module('home').controller('OneToVoidQCCtrl',['$scope', '$parse','$filter','atmToSingleDatatable','mainService',
                                                             function($scope,$parse, $filter,atmToSingleDatatable,mainService) {
	 
	
	// NGL-1055: mettre les getArray et codes'' dans filter et pas dans render
	var getDefaultDatatableColumn = function() {
		var columns = [];
		
		columns.push({
			"header" : Messages("containers.table.fromTransformationTypeCodes"),
			"property" : "inputContainer.fromTransformationTypeCodes",
			"filter": "unique | codes:'type'",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "text",
			"render" : "<div list-resize='cellValue' list-resize-min-size='3'>",
			"position" : 4,
			"extraHeaders" : {0 : Messages("experiments.inputs")}
		});
		columns.push({
			"header" : Messages("containers.table.valuation.valid"),
			"property" : "inputContainer.valuation.valid",
			"filter" : "codes:'valuation'",
			"order" : true,
			"edit" : false,
			"hide" : false,
			"type" : "text",
			"position" : 5,
			"extraHeaders" : {0 : Messages("experiments.inputs")}
		});
		columns.push({
			"header" : Messages("containers.table.projectCodes"),
			"property" : "inputContainer.projectCodes",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "text",
			"position" : 6,
			"render" : "<div list-resize='cellValue' list-resize-min-size='3'>",
			"extraHeaders" : {0 : Messages("experiments.inputs")}
		});
		columns.push({
			"header" : Messages("containers.table.sampleCodes"),
			"property" : "inputContainer.sampleCodes",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "text",
			"position" : 7,
			"render" : "<div list-resize='cellValue' list-resize-min-size='3'>",
			"extraHeaders" : {0 : Messages("experiments.inputs")}
		});

				
	/*	columns.push({
			"header" : Messages("containers.table.concentration"),
			"property": "inputContainerUsed.concentration",
			"render":"<span ng-bind='cellValue.value|number'/> <span ng-bind='cellValue.unit'/>",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "text",
			"position" : 8,
			"extraHeaders" : {
				0 : Messages("experiments.inputs")
			}
		});

		columns.push({
			"header" : Messages("containers.table.volume") + " (µL)",
			"property" : "inputContainerUsed.volume.value",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "number",
			"position" : 9,
			"extraHeaders" : {
				0 : Messages("experiments.inputs")
			}
		});
		
		
		columns.push({
			"header" : Messages("containers.table.libProcessType"),
			"property" : "inputContainer.contents",
			"order" : false,
			"hide" : true,
			"type" : "text",
			"position" : 10,
			"render" : "<div list-resize='cellValue | getArray:\"properties.libProcessTypeCode.value\" | unique' list-resize-min-size='3'>",
			"extraHeaders" : {
				0 : Messages("experiments.inputs")
			}
		});
		columns.push({
			"header" : Messages("containers.table.tags"),
			"property" : "inputContainer.contents",
			"order":true,
			"hide" : true,
			"type" : "text",
			"position" : 11,
			"filter":"getArray:\"properties.tag.value\" ",
			"render" : "<div list-resize='cellValue' list-resize-min-size='3'>",
			"extraHeaders" : {
				0 : Messages("experiments.inputs")
			}

		}); */
		columns.push({
			"header" : Messages("containers.table.stateCode"),
			"property" : "inputContainer.state.code",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "text",
			"filter" : "codes:'state'",
			"position" : 10.5,
			"extraHeaders" : {0 : Messages("experiments.inputs")}
		});
		columns.push({
			"header" : Messages("containers.table.valuationqc.valid"),
			"property" : "inputContainerUsed.valuation.valid",
			"filter" : "codes:'valuation'",
			"order" : true,
			"edit" : true,
			"hide" : false,
			"type" : "text",
			"choiceInList" : true,
			"listStyle" : 'bt-select',
			"possibleValues" : 'lists.getValuations()',
			"position" : 530,
			"extraHeaders" : {0 : Messages("experiments.inputs")}
		});
		
		columns.push({
			"header" : Messages("containers.table.valuationqc.copyToInput"),
			"property" : "inputContainerUsed.copyValuationToInput",
			"filter" : "codes:'containers.table.valuationqc.copyToInput.value'",
			"order" : true,
			"edit" : true,
			"hide" : false,
			"type" : "text",
			"choiceInList" : true,
			"listStyle" : 'bt-select',
			"possibleValues" : [{code:"TRUE", name:Messages("containers.table.valuationqc.copyToInput.value.TRUE")}, 
								{code:"FALSE", name:Messages("containers.table.valuationqc.copyToInput.value.FALSE")},
								{code:"UNSET", name:Messages("containers.table.valuationqc.copyToInput.value.UNSET")}],
			"position" : 531,
			"extraHeaders" : {0 : Messages("experiments.inputs")}
		});
		
		columns.push({
			"header" : Messages("containers.table.valuationqc.comment"),
			"property" : "inputContainerUsed.valuation.comment",
			"editTemplate":"<textarea class='form-control' #ng-model rows='3'></textarea>",
			"order" : false,
			"edit" : true,
			"hide" : true,
			"type" : "text",			
			"position" : 532,
			"extraHeaders" : {0 : Messages("experiments.inputs")}
		});
		columns.push({
        	 "header":Messages("containers.table.storageCode"),
        	 "property":"inputContainerUsed.locationOnContainerSupport.storageCode",
        	 "order":true,
			 "edit":true,
			 "hide":true,
        	 "type":"text",
        	 "position":600,
        	 "extraHeaders":{0:Messages("experiments.inputs")}
		});
		
		//GA 16/03/2017 colonnes affichées variables en fonction du type de container en entrée
		// !! en mode creation $scope.experiment.atomicTransfertMethod n'est pas encore chargé=> passer par Basket ( ajouter mainService dans le controller )
		var tmp = [];
		if(!$scope.isCreationMode()){
			tmp = $scope.$eval("atomicTransfertMethods|flatArray:'inputContainerUseds'|getArray:'locationOnContainerSupport.categoryCode'|unique",$scope.experiment);			
		}else{
			tmp = $scope.$eval("getBasket().get()|getArray:'support.categoryCode'|unique",mainService);
		}
		var supportCategoryCode = undefined;
		if(tmp.length === 1){
			supportCategoryCode=tmp[0];
		}else{
			supportCategoryCode="mixte";
		}
				
		console.log("supportCategoryCode : "+supportCategoryCode);
		
		if(supportCategoryCode ==="tube"){
			columns.push({
				"header" : Messages("containers.table.code"),
				"property" : "inputContainer.support.code",
				"order" : true,
				"edit" : false,
				"hide" : true,
				"type" : "text",
				"position" : 3,
				"extraHeaders" : {0 : Messages("experiments.inputs")}
			});
		}else if(supportCategoryCode ==="96-well-plate"){
			columns.push({
				"header" : Messages("containers.table.supportCode"),
				"property" : "inputContainer.support.code",
				"order" : true,
				"edit" : false,
				"hide" : true,
				"type" : "text",
				"position" : 1,
				"extraHeaders" : {0 : Messages("experiments.inputs")}
			});
			columns.push({
				"header" : Messages("containers.table.support.line"),
				"property" : "inputContainer.support.line",
				"order" : true,
				"edit" : false,
				"hide" : true,
				"type" : "text",
				"position" : 1.1,
				"extraHeaders" : {0 : Messages("experiments.inputs")}
			});		
			columns.push({
				"header" : Messages("containers.table.support.column"),
				"property" : "inputContainer.support.column*1",
				"order" : true,
				"edit" : false,
				"hide" : true,
				"type" : "number",
				"position" : 1.2,
				"extraHeaders" : {0 : Messages("experiments.inputs")}
			});
		}else if(supportCategoryCode ==="strip-8"){
				columns.push({
					"header" : Messages("containers.table.supportCode"),
					"property" : "inputContainer.support.code",
					"order" : true,
					"edit" : false,
					"hide" : true,
					"type" : "text",
					"position" : 1,
					"extraHeaders" : {0 : Messages("experiments.inputs")}
				});
			 
				columns.push({
					"header" : Messages("containers.table.support.column"),
					"property" : "inputContainer.support.column*1",
					"order" : true,
					"edit" : false,
					"hide" : true,
					"type" : "number",
					"position" : 1.2,
					"extraHeaders" : {0 : Messages("experiments.inputs")}
				});
		}else{
			// mixte !!
			columns.push({
				"header" : Messages("containers.table.code"),
				"property" : "inputContainer.code",
				"order" : true,
				"edit" : false,
				"hide" : true,
				"type" : "text",
				"position" : 3,
				"extraHeaders" : {0 : Messages("experiments.inputs")}
			});
		}
		
		return columns;
	}
	
	
	
	// NGL-1055: name explicite pour fichier CSV exporté: typeCode experience
	var datatableConfig = {		
			name: $scope.experiment.typeCode.toUpperCase(),
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
	        	changeClass:false,
	        	showButton:false,
	        	withoutEdit: true,
				mode:'local'
			},
			hide:{
				active:true
			},
			edit:{
				active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
				showButton: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
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
			}
	};
	
	// attention ce order by est prevu pour le CNS...Il doit ensuite etre surchargé dans chaque QC du CNG...
	// il faudrait en fait le supprimer d'ici et l'ajouter dans chque QC du CNS ??? a confirmer
	if($scope.experiment.instrument.inContainerSupportCategoryCode ==="tube"){
		datatableConfig.order.by = 'inputContainer.sampleCodes';
	}
	
	
	
	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event save on one-to-void-qc");
		$scope.atmService.data.save();			
		$scope.atmService.viewToExperimentOneToVoid($scope.experiment);
		$scope.copyPropertiesToInputContainer($scope.experiment); //overrided from child		//// PB ???????? empeche le save de qc-nb-cycle-setting....
		$scope.$emit('childSaved', callbackFunction);
	});
	
	$scope.$on('refresh', function(e) {
		console.log("call event refresh on one-to-void-qc");		
		var dtConfig = $scope.atmService.data.getConfig();
		dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));
		dtConfig.edit.showButton = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));
		dtConfig.remove.active = ($scope.isEditModeAvailable() && $scope.isNewState());
		//$scope.atmService.data.setConfig(dtConfig);
		$scope.atmService.refreshViewFromExperiment($scope.experiment);
		$scope.$emit('viewRefeshed');
	});
	
	$scope.$on('cancel', function(e) {
		console.log("call event cancel on one-to-void-qc");
		$scope.atmService.data.cancel();				
	});
	
	$scope.$on('activeEditMode', function(e) {
		console.log("call event activeEditMode on one-to-void-qc");
		$scope.atmService.data.selectAll(true);
		$scope.atmService.data.setEdit();
	});
	
	datatableConfig.columns = getDefaultDatatableColumn();
	var atmService = atmToSingleDatatable($scope, datatableConfig, true);
	//defined new atomictransfertMethod
	atmService.newAtomicTransfertMethod = function(line, column){
		return {
			class:"OneToVoid",
			line:line, 
			column:column, 				
			inputContainerUseds:new Array(0)
		};
	};
	
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	$scope.atmService = atmService;
		
	$scope.computeQuantity = function(concentration, volume){
		if(concentration && concentration.value && volume && volume.value){
			var result = volume.value * concentration.value;
			if(angular.isNumber(result) && !isNaN(result)){
				quantity = {};
				quantity.value = Math.round(result*10)/10;
				quantity.unit = (concentration.unit === 'nM')?'fmol':'ng';
				return quantity;
			}
		}
		return undefined;
	};
	
	$scope.computeVolume = function(volumeTot, volume) {
		if (volumeTot && volumeTot.value && volume && volume.value) {
			var result = volumeTot.value - volume.value;
			if (angular.isNumber(result) && !isNaN(result) && result >= 0 ) {
				return {
					value: result,
					unit: volumeTot.unit
				};
			}
		}
		return undefined;
	};
}]);