angular.module('home').controller('CNSPrepaSMRTCellCtrl',['$scope', '$parse', 'atmToDragNDrop', '$filter', '$http','$window','atmToDragNDrop',
	function($scope, $parse, atmToDragNDrop, $filter, $http, $window, atmToDragNDrop) {

	
	$scope.isRoadMapAvailable = true;
	
	var datatableConfig = {
			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[  
		             {
		        	 "header":Messages("containers.table.support.number"),
		        	 "property":"atomicTransfertMethod.line",
		        	 "order":true,
					 "edit":false,
					 "hide":true,
		        	 "type":"text",
		        	 "position":0,
		        	 "extraHeaders":{0:Messages("experiments.inputs")}
		         },	
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
		        	"header":Messages("containers.table.tags"),
		 			"property": "inputContainer.contents",
		 			"filter": "getArray:'properties.tag.value'| unique",
		 			"order":false,
		 			"hide":true,
		 			"type":"text",
		 			"position":5,
		 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
		        	 "extraHeaders":{0:Messages("experiments.inputs")}
		         },
				{
		        	"header":Messages("containers.table.libraryProtocol"),
		 			"property": "inputContainer.contents",
		 			"filter": "getArray:'properties.libraryProtocol.value'| unique",
		 			"order":false,
		 			"hide":true,
		 			"type":"text",
		 			"position":6,
		 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
		        	 "extraHeaders":{0:Messages("experiments.inputs")}
		         },
				{
					"header" : Messages("containers.table.size") + " (pb)",
					"property": "inputContainer.size.value",
					"order" : true,
					"edit" : false,
					"hide" : true,
					"type" : "number",
					"position" :7,
					"extraHeaders" : {0 : Messages("experiments.inputs")}
				},
				 {
		        	 "header":Messages("containers.table.concentration"),
		        	 "property":"inputContainerUsed.concentration.value",
		        	 "order":true,
					 "edit":false,
					 "hide":true,
		        	 "type":"number",
		        	 "position":8,
		        	  "extraHeaders":{0:Messages("experiments.inputs")}
		         },
				{
					"header":Messages("containers.table.concentration.unit.shortLabel"),
					"property":"inputContainer.concentration.unit",
					"order":true,
					"edit":false,
					"hide":true,
					"type":"text",
					"position":9,
					"extraHeaders":{0:Messages("experiments.inputs")}
				},
		         {
		        	 "header":Messages("containers.table.volume") + " (µL)",
		        	 "property":"inputContainerUsed.volume.value",
		        	 "order":true,
					 "edit":false,
					 "hide":true,
		        	 "type":"number",
		        	 "position":10,
		        	 "extraHeaders":{0:Messages("experiments.inputs")}
		         },
				 {
			        	 "header":Messages("containers.table.quantity"),
			        	 "property":"inputContainerUsed.quantity.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
						 "type":"number",
			        	 "position":10.5,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
					 {
			        	 "header":Messages("containers.table.quantity.unit"),
			        	 "property":"inputContainerUsed.quantity.unit",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
						 "type":"text",
			        	 "position":11,
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
		        	 "position":12,
		        	 "extraHeaders":{0:Messages("experiments.inputs")}
		         },

				{
		        	 "header":Messages("containers.table.inputQuantity.unit"),
		        	 "property":"inputContainerUsed.experimentProperties.inputQuantity.unit",
		        	 "order":true,
					 "edit":true,
					 "hide":true,
		        	 "type":"text",
					 "watch":true,
					 "choiceInList": true,
					 "listStyle":"bt-select",
					 "possibleValues":[{code:'fmol',name:'fmol'},{code:'ng',name:'ng'}],
		        	 "position":23,
		        	 "extraHeaders":{0:Messages("experiments.inputs")}
		         },

		        	         
					{
		        	 "header":Messages("containers.table.loadingConcentration.unit"),
		        	 "property":"outputContainerUsed.experimentProperties.loadingConcentration.unit",
		        	 "order":true,
					 "edit":true,
					 "hide":true,
		        	 "type":"text",
					 "watch":true,
					 "choiceInList": true,
					 "listStyle":"bt-select",
					 "possibleValues":[{code:'nM',name:'nM'},{code:'ng/µl',name:'ng/µl'}],
		        	 "position":25,
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
					by:"atomicTransfertMethod.line"
				},
				remove:{
					active:false,
				},
				save:{
					active:true,
					withoutEdit: true,
					mode:'local',
					showButton:false,
					changeClass:false,
					callback:function(datatable){
						copyFlowcellCodeToDT(datatable);
					}
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
					number:2,
					dynamic:true,
				}
	};	
	$scope.dragInProgress=function(value){
		$scope.dragIt=value;
	};

	$scope.getDroppableClass=function(){
		if($scope.dragIt){
			return "dropZoneHover";
		}else{
			return "";
		}
	}

	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event save on tubes-to-flowcell");
		$scope.atmService.viewToExperiment($scope.experiment);
		$scope.updateConcentration($scope.experiment);
		$scope.$emit('childSaved', callbackFunction);
	});
	
	var copyFlowcellCodeToDT = function(datatable){
		var dataMain = datatable.getData();
		//copy flowcell code to output code
		var codeFlowcell = $parse("instrumentProperties.containerSupportCode.value")($scope.experiment);
		if(null != codeFlowcell && undefined != codeFlowcell){
			for(var i = 0; i < dataMain.length; i++){
				var atm = dataMain[i].atomicTransfertMethod;
				var containerCode = codeFlowcell;
				if($scope.rows.length > 1){ //other than flowcell 1
					containerCode = codeFlowcell+"_"+atm.line;
				}
				$parse('outputContainerUsed.code').assign(dataMain[i],containerCode);
				$parse('outputContainerUsed.locationOnContainerSupport.code').assign(dataMain[i],codeFlowcell);
			}				
			//datatable.setData(dataMain);
		}
	}

	/**
	 * Update concentration of output if all input are same value and unit
	 */
	$scope.updateConcentration = function(experiment){

		//prendre la propriété atm.inputContainerUseds[0].experimentProperties.finalConcentration2 de l'input pour la comparaison
		for(var j = 0 ; j < experiment.atomicTransfertMethods.length && experiment.atomicTransfertMethods != null; j++){
			var atm = experiment.atomicTransfertMethods[j];
			var concentration = undefined;
			var unit = undefined;
			var isSame = true;
			for(var i=0;i < atm.inputContainerUseds.length;i++){
				var inputContainerUsed = atm.inputContainerUseds[i];
				if(inputContainerUsed.experimentProperties && inputContainerUsed.experimentProperties.finalConcentration2){
					if(concentration === undefined && unit === undefined){
						concentration = inputContainerUsed.experimentProperties.finalConcentration2.value;
						unit = inputContainerUsed.experimentProperties.finalConcentration2.unit;
					}else{
						if(concentration !== inputContainerUsed.experimentProperties.finalConcentration2.value 
								|| unit !== inputContainerUsed.experimentProperties.finalConcentration2.unit){
							isSame = false;
							break;
						}
					}
				}
			}

			var inputContainerUsed = atm.inputContainerUseds[0];
			if(isSame && inputContainerUsed.experimentProperties && inputContainerUsed.experimentProperties.finalConcentration2){	
				if(null === inputContainerUsed.experimentProperties.finalConcentration2.unit 
						|| undefined === inputContainerUsed.experimentProperties.finalConcentration2.unit){
					inputContainerUsed.experimentProperties.finalConcentration2.unit = atmService.defaultOutputUnit.concentration;
				}
				atm.outputContainerUseds[0].concentration = inputContainerUsed.experimentProperties.finalConcentration2;
			}else{
				atm.outputContainerUseds[0].concentration = undefined;
			}
		}
	};

	$scope.$on('refresh', function(e) {
		console.log("call event refresh");

		var dtConfig = $scope.atmService.data.$atmToSingleDatatable.data.getConfig();
		dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));
		dtConfig.edit.byDefault = false;
		$scope.atmService.data.$atmToSingleDatatable.data.setConfig(dtConfig);

		$scope.atmService.refreshViewFromExperiment($scope.experiment);
		$scope.$emit('viewRefeshed');
	});

	$scope.$on('cancel', function(e) {
		console.log("call event cancel");
		$scope.atmService.data.$atmToSingleDatatable.data.cancel();
	});

	$scope.$on('activeEditMode', function(e) {
		console.log("call event activeEditMode");
		$scope.atmService.data.$atmToSingleDatatable.data.selectAll(true);
		$scope.atmService.data.$atmToSingleDatatable.data.setEdit();
	});

	//To display sample and tag in one cell
	$scope.getSampleAndTags = function(container){
		var sampleCodeAndTags = [];
		angular.forEach(container.contents, function(content){
			if(content.properties.tag != undefined && content.sampleCode != undefined){
				sampleCodeAndTags.push(content.sampleCode+" / "+content.properties.tag.value);
			}else if(content.sampleCode != undefined){
				sampleCodeAndTags.push(content.sampleCode);
			}
		});
		return sampleCodeAndTags;
	};

	$scope.getDisplayMode = function(atm, rowIndex){

		if(atm && atm.inputContainerUseds && atm.inputContainerUseds.length === 0){
			return "empty";
		}else if(atm && atm.inputContainerUseds && atm.inputContainerUseds.length > 0 && $scope.rows[rowIndex]){
			return "open";
		}else{
			return "compact";
		}		
	};

	$scope.isAllOpen = true;
	if(!$scope.isCreationMode()){
		$scope.isAllOpen = false;
	}

	//init number of lane
	$scope.rows = [];
	var laneCount = 1;
	$scope.rows = new Array(laneCount);
	for(var i = 0; i < laneCount; i++){
		$scope.rows[i] = $scope.isAllOpen;
	}

	$scope.hideRowAll = function(){
		for (var i=0; i<$scope.rows.length;i++){
			$scope.rows[i] = false;
		}	    
		$scope.isAllOpen = false;
	};

	$scope.showRowAll = function(){
		for (var i=0; i<$scope.rows.length;i++){
			$scope.rows[i] = true;
		}
		$scope.isAllOpen = true;
	};

	$scope.toggleRow = function(rowIndex){
		$scope.rows[rowIndex] = !$scope.rows[rowIndex];
	};


	//init global ContainerOut Properties outside datatable
	$scope.outputContainerProperties = $filter('filter')($scope.experimentType.propertiesDefinitions, 'ContainerOut');
	$scope.outputContainerValues = {};

	$scope.updateAllOutputContainerProperty = function(property){
		var value = $scope.outputContainerValues[property.code];
		var setter = $parse("outputContainerUseds[0].experimentProperties."+property.code+".value").assign;
		for(var i = 0 ; i < $scope.atmService.data.atm.length ; i++){
			var atm = $scope.atmService.data.atm[i];
			if(atm.inputContainerUseds.length > 0){
				setter(atm, value);
			}
		}
		$scope.changeValueOnFlowcellDesign();
	};

	$scope.changeValueOnFlowcellDesign = function(){
		$scope.atmService.data.updateDatatable();
	};
	
	//init atmService
	var atmService = atmToDragNDrop($scope, laneCount, datatableConfig);
	//defined new atomictransfertMethod
	atmService.newAtomicTransfertMethod = function(line){
		return {
			class:"ManyToOne",
			line:line, 
			column:"1", 
			inputContainerUseds:new Array(0), 
			outputContainerUseds:new Array(0)
		};		
	};

	//defined default output unit
	atmService.defaultOutputUnit = {
			volume : "µL",
			concentration:"nM"
	}
	atmService.experimentToView($scope.experiment, $scope.experimentType);

	$scope.atmService = atmService;

	//init list runs
	var containerSupportCode = $parse("instrumentProperties.containerSupportCode")($scope.experiment)
	if(containerSupportCode != null && containerSupportCode != undefined){
		$http.get(jsRoutes.controllers.runs.api.Runs.list().url,{params:{containerSupportCode:containerSupportCode.value}})
		.success(function(data) {
			console.log( "Get run list...OK");
			$scope.runs = data;
		});
	}
	
	$scope.goToBi = function(code){
		var value = AppURL("bi");
		$window.open(value+"/runs/"+code, 'bi');
	};
	
	
	$scope.getSampleAndTagsWithSecondary = function(container){
		var sampleCodeAndTags = [];
		angular.forEach(container.contents, function(content){
			if(content.properties.secondaryTag != undefined && content.properties.tag != undefined && content.sampleCode != undefined){
				sampleCodeAndTags.push(content.sampleCode+" / "+content.properties.secondaryTag.value+" / "+content.properties.tag.value);
			}else if(content.properties.tag != undefined && content.sampleCode != undefined){
 				sampleCodeAndTags.push(content.sampleCode+" / / "+content.properties.tag.value);
 			}else if(content.properties.secondaryTag != undefined && content.sampleCode != undefined){
 				sampleCodeAndTags.push(content.sampleCode+" / "+content.properties.secondaryTag.value+" / ");
 			}else if(content.sampleCode != undefined){
 				sampleCodeAndTags.push(content.sampleCode);
 			}
		});
		return sampleCodeAndTags;
	};
	
	
}]);
