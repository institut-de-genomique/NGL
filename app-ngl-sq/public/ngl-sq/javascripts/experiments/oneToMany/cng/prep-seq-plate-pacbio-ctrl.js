angular.module('home').controller('PrepSeqPlatePacbioCtrl',['$scope', '$parse', 'atmToGenerateMany',
                                                               function($scope, $parse, atmToGenerateMany) {
		
	var inputExtraHeaders=Messages("experiments.inputs");
	var outputExtraHeaders=Messages("experiments.outputs");	
				
	// datatable de parametrage
	var datatableConfigTubeParam = {
		name: $scope.experiment.typeCode+'_PARAM'.toUpperCase(),
		columns:[
				{
			     "header":Messages("containers.table.code"),
			     "property":"inputContainer.code",
			     "order":true,
				 "edit":false,
				 "hide":false,
			     "type":"text",
			     "position":1,
			     "extraHeaders":{0:inputExtraHeaders}
			    },	
			    {
			     "header":Messages("containers.table.outputNumber.pacbio"),
			     "property":"outputNumber",
			     "order":false,
				 "edit":true,
				 "hide":false,
			     "type":"number",						
			     "position":2,
			     "extraHeaders":{0:inputExtraHeaders}
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
			mode:'local',
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
	
	var datatableConfigTubeConfig =  {	
			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[   
				   //--------------------- INPUT containers section -----------------------
					 {
			        	 "header":Messages("containers.table.code"),
			        	 "property":"inputContainer.code",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "mergeCells" : true,
			        	 "position":1,
			        	 "extraHeaders":{0:inputExtraHeaders}
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
			        	"extraHeaders":{0:inputExtraHeaders}
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
			        	"extraHeaders":{0:inputExtraHeaders}
				     },
				     {
			        	 "header":Messages("containers.table.fromTransformationTypeCodes"),
			        	 "property":"inputContainer.fromTransformationTypeCodes",
			        	 "filter" : "unique | codes:'type'",
			        	 "order":false,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "mergeCells" : true,
			 			 "render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	 "position":4,
			        	 "extraHeaders":{0:inputExtraHeaders}
			         },
			         {
						"header":Messages("containers.table.tags"),
						 "property": "inputContainer.contents",
						 "filter": "getArray:'properties.tag.value'| unique",
						 "order":false,
						 "hide":true,
						 "edit":false,
						 "type":"text",
						 "position":5,
						 "render":"<div list-resize='cellValue' list-resize-min-size='3'>",
						 "extraHeaders":{0:inputExtraHeaders}
					 },
			         {
			        	 "header":Messages("containers.table.state.code"),
			        	 "property":"inputContainer.state.code",
			        	 "order":false,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "filter":"codes:'state'",
			        	 "position":7,
			        	 "extraHeaders":{0:inputExtraHeaders}
			         },
					//--->  colonnes specifiques experience s'inserent ici  (inputContainerUsed)     
			         
			        //------------------------- OUTPUT containers section --------------------------
			         
			        //--->  colonnes specifiques experience s'inserent ici  (outputContainerUsed)
					{
						"header" : Messages("containers.table.supportCode"),
						"property" : "outputContainerUsed.locationOnContainerSupport.code",
						"order" : false,
						"edit" : false, // Exceptionnellement ici il faut laisser NGL générer le code support 
						"hide" : true,
						"type" : "text",
						"position" : 105,
						"extraHeaders" : {0:outputExtraHeaders}
					},
					{
						"header" : Messages("containers.table.support.line"),
						"property" : "outputContainerUsed.locationOnContainerSupport.line",
						"order" : false,
						"edit" : false,  
						"hide" : true,
						"type" : "text",
						"position" : 106,
						"extraHeaders" : {0:outputExtraHeaders}
					},
					{
						"header" : Messages("containers.table.support.column"),
						"property" : "outputContainerUsed.locationOnContainerSupport.column",
						"order" : false,
						"edit" : false,
						"hide" : true,
						"type" : "number",
						"position" : 107,
						"extraHeaders" : {0:outputExtraHeaders}
					},
			    	{
			        	 "header":Messages("containers.table.stateCode"),
			        	 "property":"outputContainer.state.code | codes:'state'",
			        	 "order":false,
						 "edit":false,
						 "hide":true,
						 "type":"text",
			        	 "position":500,
			        	 "extraHeaders":{0:outputExtraHeaders}
			        },
			        {
			        	 "header":Messages("containers.table.storageCode"),
			        	 "property":"outputContainerUsed.locationOnContainerSupport.storageCode",
			        	 "order":false,
						 "edit":true,
						 "hide":true,
			        	 "type":"text",
			        	 "position":600,
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
			}

	};	
	
	// attention si dans l'expérience on ne prend pas tous les containerIN du datatable de parametrage => probleme !!!!!!

	var copyPlatePositions = function(experiment) {
		experiment.atomicTransfertMethods.forEach(copyAtomicTransfertMethodPlatePositions);

		//---

		function copyAtomicTransfertMethodPlatePositions(atomicTransfertMethod) {
			atomicTransfertMethod.outputContainerUseds.forEach(copyOutputContainerUsedPlatePositions);
		}

		function copyOutputContainerUsedPlatePositions(outputContainerUsed) {
			if(!isValidPositionOnSeqPlate(outputContainerUsed.experimentProperties)) return;
			var positionOnSeqPlate = outputContainerUsed.experimentProperties.positionOnSeqPlate.value;
			var line = positionOnSeqPlate.charAt(0);
			var column = parseInt(positionOnSeqPlate.substring(1));
			outputContainerUsed.locationOnContainerSupport.line = line;
			outputContainerUsed.locationOnContainerSupport.column = column;
		}

		// les 4 positions possibles étant choisies dans une drop down, le test est un peu superflu !!!
		function isValidPositionOnSeqPlate(experimentProperties){
			return experimentProperties && experimentProperties.positionOnSeqPlate && experimentProperties.positionOnSeqPlate.value && experimentProperties.positionOnSeqPlate.value.match(/[A-Z]\d\d/g);
		}
	};

	var updateATM = function(experiment){
		// 
		if(experiment.instrument.outContainerSupportCategoryCode !== "tube"){
			experiment.atomicTransfertMethods.forEach(function(atm){
				atm.line = atm.outputContainerUseds[0].locationOnContainerSupport.line;
				atm.column = atm.outputContainerUseds[0].locationOnContainerSupport.column;
			});
		}		
	};

	// tous les changement declenchent cette fonction !!!
	$scope.updatePropertyFromUDT = function(value, col) {
		console.log(' updatePropertyFromUDT ');

		var udtData = value.data;
		var getter= $parse("outputContainerUsed.volume.value");
		// volume du container d'entree ( pas affiché !!)
		var inputVolume = $parse ("inputContainerUsed.experimentProperties.inputVolume.value")(udtData);
		console.log('assign "outputContainerUsed.volume.value"='+ inputVolume);
		// volume du container de sortie ( pas affiché !!)
		getter.assign(udtData, inputVolume);
	};
	
	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event save");
		$scope.atmService.viewToExperiment($scope.experiment);
		copyPlatePositions($scope.experiment);
		updateATM($scope.experiment);
		// test !!!!
		//copyPropertiesInToPropertiesOut();

		$scope.$emit('childSaved', callbackFunction);
	});
	
	
	$scope.$on('refresh', function(e) {
		console.log("call event refresh");
		
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
	
	// forcer "96-well-plate" qq soit le choix utilisateur !!!
	$scope.$watch("experiment.instrument.outContainerSupportCategoryCode", function(){
		$scope.experiment.instrument.outContainerSupportCategoryCode = "96-well-plate";
	});

	var atmService = atmToGenerateMany($scope, datatableConfigTubeParam, datatableConfigTubeConfig);
	//defined new atomictransfertMethod
	atmService.newAtomicTransfertMethod = function(){
		return {
			class:"OneToMany",				
			inputContainerUseds:new Array(0), 
			outputContainerUseds:new Array(0)
		};		
	};
	
	//defined default output unit
	atmService.defaultOutputUnit = {
			volume : "µL",
			quantity:"ng"
	}

	// copier concentration du container entree=> container de sortie: 
	atmService.defaultOutputValue = {
		concentration : {copyInputContainer:true}
	};

	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	$scope.atmService = atmService;

}]);