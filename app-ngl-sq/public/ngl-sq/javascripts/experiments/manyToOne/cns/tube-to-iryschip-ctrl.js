angular.module('home').controller('TubesToIrysChipCtrl',['$scope', '$parse', '$window', '$filter', 'atmToDragNDrop', '$http',
                                                             function($scope, $parse, $window, $filter, atmToDragNDrop, $http) {
	
	$scope.isRoadMapAvailable = true;
	
	// NGL-1055: name explicite pour fichier CSV exporté: typeCode experience
	// NGL-1055: mettre getArray et codes:'' dans filter et pas dans render
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
			        	 "header":Messages("containers.table.nlrsConcentration"),
			        	 "property":"inputContainer.contents[0].properties.nlrsConcentration.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":5,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			         {
			        	 "header":Messages("containers.table.concentration.unit"),
			        	 "property":"inputContainer.contents[0].properties.nlrsConcentration.unit",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":5.5,
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
			        	 "header":Messages("containers.table.percentage"),
			        	 "property":"inputContainerUsed.percentage",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":40,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
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
			        	 "property":"outputContainer.state.code| codes:'state'",
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
				by:"outputContainerUsed.code"
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
				active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('IP')),
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
			}
	};	

	$scope.goToBi = function(code){
		var value = AppURL("bi");
		$window.open(value+"/runs/"+code, 'bi');
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
		console.log("call event save");		
		$scope.atmService.viewToExperiment($scope.experiment);
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
	
	
	
	$scope.$on('refresh', function(e) {
		console.log("call event refresh");
		
		var dtConfig = $scope.atmService.data.$atmToSingleDatatable.data.getConfig();
		dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('IP'));
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
			}
		});
		return sampleCodeAndTags;
	};
	
	$scope.getDisplayMode = function(atm, rowIndex){
		
		if(atm.inputContainerUseds.length === 0){
			return "empty";
		}else if(atm.inputContainerUseds.length > 0 && $scope.rows[rowIndex]){
			return "open";
		}else{
			return "compact";
		}		
	};
	
	$scope.isAllOpen = true;
	
	//TODO used container_support_category in future
	//init number of lane
	var cscCode = $parse('experiment.instrument.outContainerSupportCategoryCode')($scope);
	$scope.rows = [];
	var laneCount = 0;
	if(cscCode !== undefined){
		laneCount =2;
		$scope.rows = new Array(laneCount);
		for(var i = 0; i < laneCount; i++){
			$scope.rows[i] = $scope.isAllOpen;
		}
		
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
	$scope.outputContainerProperties = $filter('filter')($scope.experimentType.propertiesDefinitions, 'ContainerOut');;
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
			volume : "µL"			
	}
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	$scope.atmService = atmService;

	if(angular.isArray($scope.experiment.outputContainerSupportCodes) && $scope.experiment.outputContainerSupportCodes.length > 0){
		$http.get(jsRoutes.controllers.runs.api.Runs.list().url,{params:{containerSupportCodes:$scope.experiment.outputContainerSupportCodes}}).success(function(data) {
			$scope.runs = data;
		});
	}
	
	// 26/05/2020 FDS NGL-2853
	$scope.$watch("experiment.instrument.outContainerSupportCategoryCode" , function(newValue, oldValue){
		$scope.messages.clear();
		if (newValue){
			console.log("watch outContainerSupportCategoryCode="+newValue+"---------");
			checkChipPattern ();
		}
	})
	
	$scope.$watch("experiment.instrumentProperties.containerSupportCode.value" , function(newValue, oldValue){
		$scope.messages.clear();
		if (newValue){
			console.log("watch containerSupportCode.value="+newValue+"---------");
			checkChipPattern ();
		}
	})
	
	function setAlert(msg){
		$scope.messages.clazz = "alert alert-warning";
		$scope.messages.text =msg;
		$scope.messages.showDetails = true;
		$scope.messages.open();
	}

	function checkChipPattern (){
		console.log("check Chip pattern...");
		
		if ( undefined==$scope.experiment.instrumentProperties ||
			 undefined==$scope.experiment.instrumentProperties.containerSupportCode ||
			 undefined==$scope.experiment.instrumentProperties.containerSupportCode.value ||
			 undefined==$scope.experiment.instrument.outContainerSupportCategoryCode){ return; }
	
		chipCode=$scope.experiment.instrumentProperties.containerSupportCode.value;
		chipType=$scope.experiment.instrument.outContainerSupportCategoryCode;
		
		console.log (chipCode + '/'+ chipType);
		
		var irysChip2Regex=/^(SN\d{9})$/;                                         // exemple: SN123456789
		if ((chipType === "irys-chip-2") && ( null == chipCode.match(irysChip2Regex))) {
			setAlert("Le Code Chip n'est pas du type 'Irys Chip' : SNxxxxxxxxx (x=chiffre)");
		}
		
		var saphyrChipRegex=/^([A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4})$/;  // exemple: XY12-XY12-XY12-XY12
		if ((chipType === "saphyr-chip") && ( null == chipCode.match(saphyrChipRegex))) {
			setAlert("Le Code Chip n'est pas du type 'Saphyr Chip' : XXXX-XXXX-XXXX-XXXX (X=lettre majuscule ou chiffre)");
		} 
	}
		
}]);
