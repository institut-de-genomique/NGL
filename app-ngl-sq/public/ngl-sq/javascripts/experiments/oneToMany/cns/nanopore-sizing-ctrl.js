angular.module('home').controller('NanoporeSizingCtrl',['$scope', '$parse', 'atmToGenerateMany',
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
			        	 "header":"Nb sizing",
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
	
	// NGL-1055: mettre getArray et codes:'' dans filter et pas dans render
	// NGL-1055: name explicite pour fichier CSV exporté: typeCode experience
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
			        /* {
			 			"header" : Messages("containers.table.workName"),
			 			"property" : "inputContainer.properties.workName.value",
			 			"order" : true,
			 			"edit" : false,
			 			"hide" : true,
			 			"type" : "text",
			 			"position" : 1.1,
			 			"extraHeaders" : {0 : Messages("experiments.inputs")}
			 		},*/
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
				        	"header":Messages("containers.table.tags"),
				 			"property": "inputContainer.contents",
				 			"filter": "getArray:'properties.tag.value'| unique",
				 			"order":true,
				 			"hide":true,
				 			"type":"text",
				 			"position":4.5,
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
			        	 "position":6,
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
			        	 "header":Messages("containers.table.size")+" (pb)",
			        	 "property":"outputContainerUsed.size.value",
			        	 "order":true,
						 "edit":true,
						 "hide":true,
						 "type":"number",
			        	 "position":52,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         }, 
			         {
			        	 "header":Messages("containers.table.concentration")+" (ng/µL)",
			        	 "property":"outputContainerUsed.concentration.value",
			        	 "order":true,
						 "edit":true,
						 "hide":true,
						 "type":"number",
			        	 "position":53,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },
			        
			         {
			        	 "header":Messages("containers.table.volume")+" (µL)",
			        	 "property":"outputContainerUsed.volume.value",
			        	 "order":true,
						 "edit":true,
						 "hide":true,
						 "type":"number",
			        	 "position":54,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },
			         {
			        	 "header":Messages("containers.table.quantity")+" (ng)",
			        	 "property":"outputContainerUsed.quantity.value",
			        	 "order":true,
						 "edit":true,
						 "hide":true,
						 "type":"number",
			        	 "position":55,
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
			         } ,
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
				by:'inputContainer.code'
			},
			remove:{
				active:false,
			},
		
			save:{
				active:true,
	        	withoutEdit: true,
	        	showButton:false,
	        	changeClass:false,
	        	mode:'local',
	        	//callback:function(datatable){
	        		//copyOutputContainerUsedAttributesToContentProperties(datatable);
	        	//}
	        		
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
	
	var copyOutputContainerUsedAttributesToContentProperties = function(experiment){
		for(var i=0 ; i < experiment.atomicTransfertMethods.length && experiment.atomicTransfertMethods != null; i++){
			var atm = experiment.atomicTransfertMethods[i];
			/*
			copie de Concentration dans propriété content ligationConcentrationPostSizing A LA CONDITION du from transformation type = nanopore-library
			copie de Quantity dans propriété content ligationQuantityPostSizing A LA CONDITION du from transformation type = nanopore-library
			copie de Taille dans propriété content measuredSizePostSizing A LA CONDITION du from transformation type = nanopore-frg ou ext-to-nanopore-rep-lib-depot
			OU Ext to Process nanopore DEV OU Ext to Process nanopore DEV v2 OU Ext to Frg, Rep ADN, Lib, Dépôt OU Ext to Frg (sans rep), Lib, Dépôt OU Ext to Lib ONT, Dépôt
			*/
			var icu = atm.inputContainerUseds[0]; //only one because oneToMany
			for(var j=0 ; j < atm.outputContainerUseds.length ; j++){		
				var ocu = atm.outputContainerUseds[j];
				if((icu.fromTransformationTypeCodes.indexOf('nanopore-library') > -1 
						||  icu.fromTransformationTypeCodes.indexOf('nanopore-final-ligation') > -1	
						||  icu.fromTransformationTypeCodes.indexOf('ext-to-nanopore-run') > -1)
						&& ocu.concentration && ocu.concentration.value){
					var concentration = ocu.concentration;
					console.log("conc",concentration);	
					$parse('experimentProperties.ligationConcentrationPostSizing').assign(ocu,concentration);
				}else{
					$parse('experimentProperties.ligationConcentrationPostSizing').assign(ocu, undefined);
				}
				
				if((icu.fromTransformationTypeCodes.indexOf('nanopore-library') > -1 
						||  icu.fromTransformationTypeCodes.indexOf('nanopore-final-ligation') > -1
						||  icu.fromTransformationTypeCodes.indexOf('ext-to-nanopore-run') > -1)
						&& ocu.quantity && ocu.quantity.value){
					var quantity = ocu.quantity.value;	
					$parse('experimentProperties.ligationQuantityPostSizing').assign(ocu, {value:quantity, unit:"ng"});
				}else{
					$parse('experimentProperties.ligationQuantityPostSizing').assign(ocu, undefined);
				}
		
				//si from transfo type=frg ou ext-to-nanopore-rep-lib-depot
				if((icu.fromTransformationTypeCodes.indexOf('nanopore-frg') > -1 
						||  icu.fromTransformationTypeCodes.indexOf('nanopore-fragmentation') > -1 
						||  icu.fromTransformationTypeCodes.indexOf('ext-to-nanopore-rep-lib-depot') > -1
						|| icu.fromTransformationTypeCodes.indexOf('ext-to-nanopore-process-dev') > -1
						||  icu.fromTransformationTypeCodes.indexOf('ext-to-nanopore-process-dev-2') > -1
						||  icu.fromTransformationTypeCodes.indexOf('ext-to-nanopore-frg-rep-lib-depot') > -1
						||  icu.fromTransformationTypeCodes.indexOf('ext-to-nanopore-frg-lib-depot') > -1
					
						||  icu.fromTransformationTypeCodes.indexOf('ext-to-nanopore-frg-rependprep-lig-depot-process') > -1
						||  icu.fromTransformationTypeCodes.indexOf('ext-to-nanopore-rependprep-lig-depot-process') > -1
						
						||  icu.fromTransformationTypeCodes.indexOf('ext-to-nanopore-process-library-no-frg') > -1
				)
				&& ocu.size && ocu.size.value){
					var size = ocu.size.value;	
					$parse('experimentProperties.measuredSizePostSizing').assign(ocu, {value:size, unit:"pb"});
				}else{
					$parse('experimentProperties.measuredSizePostSizing').assign(ocu, undefined);
				}				
			}
		}				
	};
	
	
	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event save on sizing");
		$scope.atmService.viewToExperiment($scope.experiment);
		copyOutputContainerUsedAttributesToContentProperties($scope.experiment);
		$scope.$emit('childSaved', callbackFunction);
	});
	
	
	$scope.$on('refresh', function(e) {
		console.log("call event refresh on sizing");
		
		var dtConfig = $scope.atmService.data.datatableParam.getConfig();
		dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isNewState());
		dtConfig.remove.active = ($scope.isEditModeAvailable() && $scope.isNewState());
		dtConfig.edit.byDefault = false;
		$scope.atmService.data.datatableParam.setConfig(dtConfig);
		
		dtConfig = $scope.atmService.data.datatableConfig.getConfig();
		dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));
		dtConfig.edit.showButton = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));
		dtConfig.remove.active = ($scope.isEditModeAvailable() && $scope.isNewState());
		dtConfig.edit.byDefault = false;
		$scope.atmService.data.datatableConfig.setConfig(dtConfig);
		
		$scope.atmService.refreshViewFromExperiment($scope.experiment);
		$scope.$emit('viewRefeshed');
	});
	
	$scope.$on('cancel', function(e) {
		console.log("call event cancel on sizing");
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
		console.log("call event activeEditMode on sizing");
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
			quantity:"ng",
			size:"pb",
			concentration:"ng/µl"
			
	}
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	$scope.atmService = atmService;
}]);
