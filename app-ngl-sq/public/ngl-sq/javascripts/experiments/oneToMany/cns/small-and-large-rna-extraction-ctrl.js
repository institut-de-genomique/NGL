angular.module('home').controller('SmallAndLargeRNAIsolation',['$scope', '$parse', '$filter', 'atmToSingleDatatable','lists','mainService',
                                                               function($scope, $parse, $filter, atmToSingleDatatable,lists,mainService) {
	

	$scope.dispatchConfiguration.orderBy = "container.sampleCodes";
	
	var datatableConfig =  {
			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[   
					 {
			        	 "header":Messages("containers.table.code"),
			        	 "property":"inputContainer.support.code",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "mergeCells" : true,
			        	 "position":1,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },		         
			         {
			        	"header":Messages("containers.table.projectCodes"),
			 			"property": "inputContainer.projectCodes",
			 			"order": true,
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
				        	"header":"Code aliquot",
				 			"property": "inputContainer.contents",
				 			"filter": "get:'properties.sampleAliquoteCode.value'| unique",
				 			"order":false,
				 			"hide":true,
				 			"type":"text",
				 			"position":3.5,
				 			"render": "<div list-resize='cellValue' list-resize-min-size='3'>",
				        	 "extraHeaders":{0:Messages("experiments.inputs")}
					 },
				     {
			        	 "header":Messages("containers.table.fromTransformationTypeCodes"),
			        	 "property":"inputContainer.fromTransformationTypeCodes",
			        	 "filter" : "unique | codes:'type'",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	 "position":4,
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
			         /*{
			        	 "header":Messages("containers.table.quantity") + " (ng)",
			        	 "property":"inputContainerUsed.quantity.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":7,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },*/
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
			        	 "header":Messages("containers.table.volume")+" (µL)",
			        	 "property":"outputContainerUsed.volume.value",
			        	 "order":true,
						 "edit":true,
						 "hide":true,
						 "type":"number",
			        	 "position":10,
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
				by:'inputContainer.sampleCodes'
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
	
	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event save on tube-to-tubes");
		$scope.atmService.data.save();
		$scope.atmService.viewToExperimentOneToMany($scope.experiment);
		$scope.$emit('childSaved', callbackFunction);
	});
	
	
	$scope.$on('refresh', function(e) {
		console.log("call event refresh");		
		var dtConfig = $scope.atmService.data.getConfig();
		dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));
		dtConfig.edit.showButton = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));
		dtConfig.edit.byDefault = false;
		//dtConfig.remove.active = ($scope.isEditModeAvailable() && $scope.isNewState());
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
	
	
	var atmService = atmToSingleDatatable($scope, datatableConfig);
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
			quantity:"ng"
	}
	
	atmService.addNewAtomicTransfertMethodsInDatatable = function(){
		if(null != mainService.getBasket() && null != mainService.getBasket().get() && this.isAddNew){
			$that = this;
			
			var type = $that.newAtomicTransfertMethod().class;
			
			$that.$commonATM.loadInputContainerFromBasket(mainService.getBasket().get())
				.then(function(containers) {								
					var allData = [], i = 0;
					
					if($that.data.getData() !== undefined && $that.data.getData().length > 0){
						allData = $that.data.getData();
						i = allData.length;
					}
					
					angular.forEach(containers, function(container){
						var tmpLine = {};
						tmpLine.atomicTransfertMethod = $that.newAtomicTransfertMethod(container.support.line, container.support.column);
						tmpLine.atomicIndex=i/2;i=i+2;
							
						tmpLine.inputContainer = container;
						tmpLine.inputContainerUsed = $that.$commonATM.convertContainerToInputContainerUsed(tmpLine.inputContainer);
						var rnaSize = ["17-200nt",">200nt"]
						for(var j = 0; j < rnaSize.length ; j++){
							var line = {};
							line.atomicTransfertMethod = tmpLine.atomicTransfertMethod;
							line.atomicIndex = tmpLine.atomicIndex;
							line.inputContainer = tmpLine.inputContainer;
							line.inputContainerUsed = tmpLine.inputContainerUsed;
							line.outputContainerUsed = $that.$commonATM.newOutputContainerUsed($that.defaultOutputUnit,$that.defaultOutputValue,line.atomicTransfertMethod.line,
									line.atomicTransfertMethod.column,line.inputContainer);
							
							var value = rnaSize[j];
							var setter = $parse("experimentProperties.rnaSize.value").assign;
							setter(line.outputContainerUsed, value);
							
							setter = $parse("experimentProperties.sampleTypeCode.value").assign;
							setter(line.outputContainerUsed, "RNA");
							
							line.outputContainer = undefined;
							allData.push(line);
						}						
					});
					
					allData = $filter('orderBy')(allData,'inputContainer.support.code');
					$that.data.setData(allData, allData.length);											
			});
		}					
	};
	
	
	atmService.experimentToView($scope.experiment, $scope.experimentType);					
    $scope.atmService = atmService;
	

}]);