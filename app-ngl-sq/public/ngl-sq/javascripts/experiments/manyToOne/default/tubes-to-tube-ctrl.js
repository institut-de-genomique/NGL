angular.module('home').controller('TubesToTubeCtrl',['$scope', '$parse', 'atmToDragNDrop2',
                                                               function($scope, $parse, atmToDragNDrop) {
	
	// NGL-1055: mettre getArray et codes:'' dans filter et pas dans render
	// NGL-1055: name explicite pour fichier CSV exporté
	var datatableConfig = {	
			name: $scope.experiment.typeCode.toUpperCase(),
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
			 			"property": "inputContainer.projectCodes",
			 			"order":false,
			 			"hide":true,
			 			"type":"text",
			 			"position":2,
			 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	"extraHeaders":{0:Messages("experiments.inputs")}
				     },
				     {
			        	"header":Messages("containers.table.sampleCodes"),
			 			"property": "inputContainer.sampleCodes",
			 			"order":false,
			 			"hide":true,
			 			"type":"text",
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
			 			 "render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	 "position":4,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			         					 
					 {
			        	 "header":Messages("containers.table.concentration"),
			        	 "property":"inputContainerUsed.concentration.value",
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
			        	 "position":5.5,
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
			        	 "header":Messages("containers.table.percentageInsidePool"),
			        	 "property":"inputContainerUsed.percentage",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":10,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			         {
			        	 "header":Messages("containers.table.concentration"),
			        	 "property":"outputContainerUsed.concentration.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
						 "mergeCells" : true,
			        	 "type":"number",
			        	 "position":50,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },
			         {
			        	 "header":Messages("containers.table.concentration.unit"),
			        	 "property":"outputContainerUsed.concentration.unit",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
						 "mergeCells" : true,
			        	 "type":"text",
			        	 "position":50.5,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },
			         {
			        	 "header":Messages("containers.table.volume")+" (µL)",
			        	 "property":"outputContainerUsed.volume.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
						 "mergeCells" : true,
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
						 "mergeCells" : true,
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
						 "mergeCells" : true,
			        	 "type":"text",
			        	 "position":500,
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
				mode:'local',
				changeClass:false,
				showButton:false
			},
			hide:{
				active:true
			},
			mergeCells:{
	        	active:true 
	        },
			select:{
				active:false,
				showButton:true,
				isSelectAll:false
			},
			edit:{
				active: false,
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
	
	$scope.drop = function(e, data, ngModel, alreadyInTheModel, fromModel) {
		//capture the number of the atomicTransfertMethod
		if(!alreadyInTheModel){
			$scope.atmService.data.updateDatatable();
		
		}
	};
	
	$scope.isEditMode = function(){
		return ($scope.$parent.isEditMode() && $scope.isNewState());
	};
	
	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event save on tubes-to-tube");		
		$scope.atmService.viewToExperiment($scope.experiment);
		$scope.updateConcentration($scope.experiment);
		$scope.$emit('childSaved', callbackFunction);
	});
	
	/**
	 * Update concentration of output if all input are same value and unit
	 */
	$scope.updateConcentration = function(experiment){
		
		if(experiment.atomicTransfertMethods && experiment.atomicTransfertMethods[0]){
		// ne pas faire l'update si déjà renseigné
			var concentration = undefined;
			var unit = undefined;
			var isSame = true;
			for(var i=0;i<experiment.atomicTransfertMethods[0].inputContainerUseds.length;i++){
				if(experiment.atomicTransfertMethods[0].inputContainerUseds[i].concentration !== null 
						&& experiment.atomicTransfertMethods[0].inputContainerUseds[i].concentration !== undefined){
					if(concentration === undefined && unit === undefined){
						concentration = experiment.atomicTransfertMethods[0].inputContainerUseds[i].concentration.value;
						unit = experiment.atomicTransfertMethods[0].inputContainerUseds[i].concentration.unit;
					}else{
						if(concentration !== experiment.atomicTransfertMethods[0].inputContainerUseds[i].concentration.value 
								|| unit !== experiment.atomicTransfertMethods[0].inputContainerUseds[i].concentration.unit){
							isSame = false;
							break;
						}
					}
				}
			}
			if(isSame 
					&& (experiment.atomicTransfertMethods[0].outputContainerUseds[0].concentration === null
							|| experiment.atomicTransfertMethods[0].outputContainerUseds[0].concentration.value === null
						|| experiment.atomicTransfertMethods[0].outputContainerUseds[0].concentration === undefined
						|| experiment.atomicTransfertMethods[0].outputContainerUseds[0].concentration.value === undefined)){
				experiment.atomicTransfertMethods[0].outputContainerUseds[0].concentration = $scope.experiment.atomicTransfertMethods[0].inputContainerUseds[0].concentration;
				
			}
		}

	};
	
	$scope.$on('refresh', function(e) {
		console.log("call event refresh on tubes-to-tube");		
		$scope.atmService.refreshViewFromExperiment($scope.experiment);
		$scope.$emit('viewRefeshed');
	});
	
	
	$scope.$on('cancel', function(e) {
		console.log("call event cancel");
		
	});
	
	$scope.$on('activeEditMode', function(e) {
		console.log("call event activeEditMode");
	});
	
	var atmService = atmToDragNDrop($scope, 1, datatableConfig);
	//defined new atomictransfertMethod
	atmService.newAtomicTransfertMethod = function(){
		return {
			class:"ManyToOne",
			line:"1", //TODO only exact for oneToOne of type  tube to tube but not for plate to plate
			column:"1", 				
			inputContainerUseds:new Array(0), 
			outputContainerUseds:new Array(0)
		};		
	};
	
	//defined default output unit
	atmService.defaultOutputUnit = {
			volume : "µL"			
	}
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	$scope.atmService = atmService;
	
}]);
