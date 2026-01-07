angular.module('home').controller('PacBioDepotV2Ctrl',['$scope', '$parse','atmToSingleDatatable', '$http', '$window',
                                                             function($scope,$parse, atmToSingleDatatable, $http, $window) {
	
	 var datatableConfig = {
			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[
			         {
			        	 "header":Messages("containers.table.container"),
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
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":4,
			        	 "render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
					 {
			        	 "header":Messages("containers.table.sampleCodes"),
			        	 "property":"inputContainer.sampleCodes",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":5,
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
			 			 "render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	 "position":6,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
					 {
						"header":Messages("containers.table.libraryProtocol"),
						"property": "inputContainer.contents",
						"filter": "getArray:'properties.libraryProtocol.value'| unique",
						"order":false,
						"hide":true,
						"type":"text",
						"position":7,
						"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
						"extraHeaders":{0:Messages("experiments.inputs")}
		             },
					 {
						"header":Messages("containers.table.tags"),
						"property": "inputContainer.contents",
						"filter": "getArray:'properties.tag.value'| unique",
						"order":false,
						"hide":true,
						"type":"text",
						"position":8,
						"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
						"extraHeaders":{0:Messages("experiments.inputs")}
		             },
					 {
						"header":function(){return Messages("containers.table.volume")  + " (µl)"},
						"property":"inputContainer.volume.value",
						"order":true,
						"edit":false,
						"hide":true,
						"type":"number",
						"position":8.5,
						"extraHeaders":{0:Messages("experiments.inputs")}
					},
					{
						"header":Messages("containers.table.idPlaqueSequencage"),
						"property": "inputContainer.contents",
						"filter": "getArray:'properties.seqPlate.value'| unique",
						"order":false,
						"hide":true,
						"type":"text",
						"position":8.6,
						"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
						"extraHeaders":{0:Messages("experiments.inputs")}
		             },
					 {
						"header":Messages("containers.table.positionOnSeqPlate"),
						"property": "inputContainer.contents",
						"filter": "getArray:'properties.positionOnSeqPlate.value'| unique",
						"order":false,
						"hide":true,
						"type":"text",
						"position":8.7,
						"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
						"extraHeaders":{0:Messages("experiments.inputs")}
		             },
					 {
			        	 "header":Messages("containers.table.stateCode"),
			        	 "property":"inputContainer.state.code",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
						 "filter":"codes:'state'",
			        	 "position":9,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
					// Colonnes pour le container de sortie.
					{
						"header":Messages("containers.table.code"),
						"property":"outputContainerUsed.code",
						"order":true,
						"edit":false,
						"hide":true,
						"type":"text",
						"position":400,
						"extraHeaders":{0:Messages("experiments.outputs")}
					}, {
						"header":Messages("containers.table.stateCode"),
						"property":"outputContainer.state.code | codes:'state'",
						"order":true,
						"edit":false,
						"hide":true,
						"type":"text",
						"position":600,
						"extraHeaders":{0:Messages("experiments.outputs")}
					}],
			compact: true,
			pagination: {
				active:false
			},		
			search: {
				active: false
			},
			order:{
				mode:'local',
				active:true,
				by:'inputContainer.code'
			},
			remove:{
				active: ( $scope.isNewState()),
				withEdit : true,
				showButton: ($scope.isNewState()),				
				mode:'local'
			},
			save:{
				active:true,
	        	changeClass:false,
	        	showButton:false,
	        	withoutEdit: true,
				mode:'local',
				callback:function(datatable){
					copyValues(datatable);
				}
			},
			hide:{
				active:true
			},
			edit:{
				active: true,
				showButton: true,
				byDefault:$scope.isCreationMode(),				
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
                template: 
                	'<button class="btn btn-default" ng-click="copyVolumeInToOut()" data-toggle="tooltip" title="'+Messages("experiments.button.plate.copyVolume")+'"  ng-disabled="!isEditMode()"><i class="fa fa-files-o" aria-hidden="true"></i> Volume </button>'                	                	
            }
	};

	var copyValues = function(datatable){
		var dataMain = datatable.getData();
	
		dataMain.forEach(function(data) {
			//copy flowcell code to output code
			var codeFlowcell = $parse("outputContainerUsed.experimentProperties.smrtCellCode.value")(data);

			if(null != codeFlowcell && undefined != codeFlowcell){
				$parse('outputContainerUsed.code').assign(data,codeFlowcell);
				$parse('outputContainerUsed.locationOnContainerSupport.code').assign(data,codeFlowcell);
			}
		});
	}

	$scope.$on('save', function(e, callbackFunction) {
		console.log("call event save");

		$scope.atmService.data.save();
		$scope.atmService.viewToExperimentOneToOne($scope.experiment);

		$scope.$emit('childSaved', callbackFunction);
	});

	$scope.goToBi = function(code){
		var value = AppURL("bi");
		$window.open(value+"/runs/"+code, 'bi');
	};

	$scope.copyVolumeInToOut = function(){
		var data = $scope.atmService.data.displayResult;
		data.filter(isInputVolume).forEach(copyContainerVolume);

		//---

		function isInputVolume(value) {
			return value.data.inputContainerUsed.volume && value.data.inputContainerUsed.volume.value !== null;
		}

		function initExperimentProperties(value) {
			if (value.data.inputContainerUsed.experimentProperties) return;
			value.data.inputContainerUsed.experimentProperties = { inputVolume: {} };
		}

		function copyContainerVolume(value) {
			initExperimentProperties(value);
			value.data.inputContainerUsed.experimentProperties.inputVolume.value = value.data.inputContainerUsed.volume.value;
		}
	};

	if(angular.isArray($scope.experiment.outputContainerSupportCodes) && $scope.experiment.outputContainerSupportCodes.length > 0){
		$http.get(jsRoutes.controllers.runs.api.Runs.list().url,{params:{containerSupportCodes:$scope.experiment.outputContainerSupportCodes}}).success(function(data) {
			$scope.runs = data;
		});
	}
	
	$scope.$on('cancel', function(e) {
		console.log("call event cancel on one-to-one (default)");
		$scope.atmService.data.cancel();
	});
		
	$scope.$on('activeEditMode', function(e) {
		console.log("call event activeEditMode on one-to-one (default)"); 
		$scope.atmService.data.selectAll(true);
		$scope.atmService.data.setEdit();
	});

	$scope.$on('refresh', function(e) {
		console.log("call event refresh");		
		var dtConfig = $scope.atmService.data.getConfig();
		dtConfig.edit.active = $scope.isEditModeAvailable();
		dtConfig.edit.byDefault = false;
		dtConfig.remove.active = ($scope.isEditModeAvailable() && $scope.isNewState());
		$scope.atmService.data.setConfig(dtConfig);
		$scope.atmService.refreshViewFromExperiment($scope.experiment);
		$scope.$emit('viewRefeshed');
	});
	
	var atmService = atmToSingleDatatable($scope, datatableConfig);
	
	atmService.newAtomicTransfertMethod = function(){
		return {
			class:"OneToOne",
			line:"1", 
			column:"1", 				
			inputContainerUseds:new Array(0), 
			outputContainerUseds:new Array(0)
		};
	};
		
	atmService.experimentToView($scope.experiment, $scope.experimentType);
		
	if ($scope.isCreationMode() && $scope.mainService.getBasket().length() > 1) { 
		$scope.messages.setError(Messages('experiments.input.error.max1Container'));
	} else{
		$scope.messages.clear();
		$scope.atmService = atmService;
	}
		
	if ($scope.isCreationMode()){
		if (!$parse("experimentProperties.runStartDate.value")($scope.experiment)){
			console.log('setting current date as default');

			var format = Messages("date.format").toUpperCase();
			var curdateMS_round = moment().format(format);
			curdateMS_round = moment(curdateMS_round, format).valueOf();
	
			$parse("experimentProperties.runStartDate.value").assign($scope.experiment, curdateMS_round);
		}
	}
		
	//NGL-5002 ajout Info message
	$scope.isShowInformation = false;

	$scope.setIsShowInformation=function(bool){
		$scope.isShowInformation=bool;
	}
	// show/hide div information
	$scope.toggleIsShowInformation=function(){
		if ( $scope.isShowInformation===false) { $scope.isShowInformation=true; }
		else {$scope.isShowInformation=false}
	}
		
}]);