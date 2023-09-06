angular.module('home').controller('HiCPrepCtrl',['$scope', '$parse', '$filter', 'atmToSingleDatatable','lists','mainService',
                                                       function($scope, $parse, $filter, atmToSingleDatatable,lists,mainService) {

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
			         {
			        	 "header":Messages("containers.table.quantity") + " (ng)",
			        	 "property":"inputContainerUsed.quantity.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":7,
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
			        	 "header":Messages("containers.table.volume")+" (µL)",
			        	 "property":"outputContainerUsed.volume.value",
			        	 "order":true,
			        	 "edit":true,
			        	 "hide":true,
			        	 "type":"number",
			        	 "position":30,
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
			         },
			         otherButtons: {
			        	 active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
			        	 complex:true,
			        	 template:  ''
			        		 +$scope.plateUtils.templates.buttonLineMode()
			                 +$scope.plateUtils.templates.buttonColumnMode()     
			                 +$scope.plateUtils.templates.buttonCopyPosition()             	   
			         }
	};	

	
	

	


	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event save on dna-extraction");
		$scope.atmService.data.save();
		$scope.atmService.viewToExperimentOneToOne($scope.experiment);
		$scope.$emit('childSaved', callbackFunction);		
	});


	$scope.$on('refresh', function(e) {
		console.log("call event refresh");		
		var dtConfig = $scope.atmService.data.getConfig();
		dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));
		dtConfig.edit.showButton = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));
		dtConfig.edit.byDefault = false;
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

	$scope.$watch("experiment.instrument.outContainerSupportCategoryCode", function(){
		$scope.experiment.instrument.outContainerSupportCategoryCode = "tube";
	});

	var atmService = atmToSingleDatatable($scope, datatableConfig);
	//defined new atomictransfertMethod
	atmService.newAtomicTransfertMethod =  function(line, column){
		return {
			class:"OneToOne",
			line:1, 
			column:1, 				
			inputContainerUseds:new Array(0), 
			outputContainerUseds:new Array(0)
		};
	};
	//defined default output unit
	atmService.defaultOutputUnit = {
			volume : "µL",
			quantity:"ng"
	}


	atmService.convertOutputPropertiesToDatatableColumn = function(property, pName){
		var column = atmService.$commonATM.convertTypePropertyToDatatableColumn(property,"outputContainerUsed."+pName+".",{"0":Messages("experiments.outputs")});
		if(property.code=="projectCode"){
			column.editTemplate='<div class="form-control" bt-select #ng-model filter="true" placeholder="'+Messages("search.placeholder.projects")+'" bt-options="project.code as project.code+\' (\'+project.name+\')\' for project in lists.getProjects()" ></div>';			
		}else if(property.code=="sampleTypeCode"){
			column.filter="getArray:'sampleTypeCode' | unique | codes:\"type\"";			
		}
		return column;
	};

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
					tmpLine.atomicIndex=i++;

					tmpLine.inputContainer = container;
					tmpLine.inputContainerUsed = $that.$commonATM.convertContainerToInputContainerUsed(tmpLine.inputContainer);

					for(var j = 0; j < $scope.experimentType.sampleTypes.length ; j++){
						var line = {};
						line.atomicTransfertMethod = tmpLine.atomicTransfertMethod;
						line.atomicIndex = tmpLine.atomicIndex;
						line.inputContainer = tmpLine.inputContainer;
						line.inputContainerUsed = tmpLine.inputContainerUsed;
						line.outputContainerUsed = $that.$commonATM.newOutputContainerUsed($that.defaultOutputUnit,$that.defaultOutputValue,line.atomicTransfertMethod.line,
								line.atomicTransfertMethod.column,line.inputContainer);

						var value = $scope.experimentType.sampleTypes[j].code;
						var setter = $parse("experimentProperties.sampleTypeCode.value").assign;
						setter(line.outputContainerUsed, value);

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