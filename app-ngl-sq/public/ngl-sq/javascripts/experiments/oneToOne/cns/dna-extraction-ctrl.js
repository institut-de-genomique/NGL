angular.module('home').controller('DnaExtractionCtrl',['$scope', '$parse', '$filter', 'atmToSingleDatatable','lists','mainService',
                                                       function($scope, $parse, $filter, atmToSingleDatatable,lists,mainService) {


	var datatableConfig =  {
			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[   
			         /*      {
			        	 "header":Messages("containers.table.code"),
			        	 "property":"inputContainer.support.code",
			        	 "order":true,
			        	 "edit":false,
			        	 "hide":true,
			        	 "type":"text",
			        	 "mergeCells" : true,
			        	 "position":1,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },	*/	         
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

			        /*   {
			        	 "header":Messages("containers.table.code"),
			        	 "property":"outputContainerUsed.code",
			        	 "order":true,
			        	 "edit":false,
			        	 "hide":true,
			        	 "type":"text",
			        	 "position":400,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },*/
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

	var updateATM = function(experiment){
		if(experiment.instrument.outContainerSupportCategoryCode!=="tube"){
			experiment.atomicTransfertMethods.forEach(function(atm){
				atm.line = atm.outputContainerUseds[0].locationOnContainerSupport.line;
				atm.column = atm.outputContainerUseds[0].locationOnContainerSupport.column;
			});
		}		
	}
	

	$scope.updateInputVolume = function(experiment){
		for(var i=0 ; i < experiment.atomicTransfertMethods.length ; i++){
			var atm = experiment.atomicTransfertMethods[i];

			var volume = {input:0};

			angular.forEach(atm.outputContainerUseds, function(output){
				this.input += Number(output.volume.value);
			}, volume);

			if(angular.isNumber(volume.input)){
				$parse('inputContainerUseds[0].experimentProperties["inputVolume"]').assign(atm, {value:volume.input, unit:"µL"});
			}
			//atm.inputContainerUseds[0].experimentProperties["inputVolume"] = {value:volume.input, unit:"µL"};
		}				
	};


	/**
	 * Update concentration. Copy input concentration to all outputs
	 */
	$scope.updateConcentration = function(experiment){

		for(var j = 0 ; j < experiment.atomicTransfertMethods.length && experiment.atomicTransfertMethods != null; j++){
			var atm = experiment.atomicTransfertMethods[j];
			if(atm.inputContainerUseds[0].concentration !== null 
					&& atm.inputContainerUseds[0].concentration !== undefined){
				var concentration = atm.inputContainerUseds[0].concentration;				
				for(var i = 0 ; i < atm.outputContainerUseds.length ; i++){
					$parse("outputContainerUseds["+i+"].concentration").assign(atm, concentration);
				}
			}

		}		
	};



	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event save on dna-extraction");
		$scope.atmService.data.save();
		$scope.atmService.viewToExperimentOneToOne($scope.experiment);
		checkExtractionBlankSampleCode($scope);
		$scope.refreshExtractionBlankSampleCodeLists();
		updateATM($scope.experiment);
		
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
		$scope.refreshExtractionBlankSampleCodeLists();
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

	//Met à jour selon selection de l'instrument
	//$scope.$watch("experiment.instrument.outContainerSupportCategoryCode", function(newValue, oldValue){
	//	$scope.experiment.instrument.outContainerSupportCategoryCode = "96-well-plate";		
	//});	

	//Initialise = valeur par défaut
	//$scope.experiment.instrument.outContainerSupportCategoryCode = "96-well-plate";

	//Init		
	if($scope.experiment.instrument.inContainerSupportCategoryCode!=="tube"){
		datatableConfig.columns.push({
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
		});
		datatableConfig.columns.push({
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
		});
		datatableConfig.columns.push({
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
		});

	} else {
		datatableConfig.columns.push({
			"header" : Messages("containers.table.code"),
			"property" : "inputContainer.support.code",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "text",
			"position" : 1,
			"extraHeaders" : {
				0 : Messages("experiments.inputs")
			}
		});
		datatableConfig.order.by = 'inputContainer.sampleCodes';

	}

	if($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube") {
		datatableConfig.columns.push({
			// barcode plaque sortie == support Container used code... faut Used
			"header" : Messages("containers.table.support.name"),
			"property" : "outputContainerUsed.locationOnContainerSupport.code",
			"hide" : true,
			"type" : "text",
			"position" : 400,
			"extraHeaders" : {
				0 : Messages("experiments.outputs")
			}
		});
		datatableConfig.columns.push({
			// Ligne
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
		});
		datatableConfig.columns.push({// colonne
			"header" : Messages("containers.table.support.column"),
			// astuce GA: pour pouvoir trier les colonnes dans l'ordre naturel
			// forcer a numerique.=> type:number, property: *1
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
		});

	} else {
		datatableConfig.columns.push({
			"header" : Messages("containers.table.code"),
			"property" : "outputContainerUsed.code",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "text",
			"position" : 400,
			"extraHeaders" : {
				0 : Messages("experiments.outputs")
			}
		});
	}


	var atmService = atmToSingleDatatable($scope, datatableConfig);
	//defined new atomictransfertMethod
	atmService.newAtomicTransfertMethod =  function(line, column){
		var getLine = function(line){
			if($scope.experiment.instrument.outContainerSupportCategoryCode 
					=== $scope.experiment.instrument.inContainerSupportCategoryCode){
				return line;
			}else if($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube" 
				&& $scope.experiment.instrument.inContainerSupportCategoryCode === "tube") {
				return undefined;
			}else if($scope.experiment.instrument.outContainerSupportCategoryCode === "tube"){
				return "1";
			}

		}
		var getColumn=getLine;

		return {
			class:"OneToOne",
			line:getLine(line), 
			column:getColumn(column), 				
			inputContainerUseds:new Array(0), 
			outputContainerUseds:new Array(0)
		};
	};
	//defined default output unit
	atmService.defaultOutputUnit = {
			volume : "µL"
	}

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

	var checkExtractionBlankSampleCode = function($scope){
		var experiment=$scope.experiment;
		for(var i=0 ; i < experiment.atomicTransfertMethods.length && experiment.atomicTransfertMethods != null; i++){
			var atm = experiment.atomicTransfertMethods[i];
			for(var j=0 ; j < atm.outputContainerUseds.length ; j++){		
				var ocu = atm.outputContainerUseds[j];
				var getter = $parse("experimentProperties.extractionBlankSampleCode.value");

				if(ocu.experimentProperties && ocu.experimentProperties.sampleTypeCode.value && ocu.experimentProperties.sampleTypeCode.value == "DNA"){				
					if ($scope.sample.extractionDNABlankSampleCode){
						var value = $scope.sample.extractionDNABlankSampleCode;
						getter.assign(atm.outputContainerUseds[j],value);	
					}
				}		
			}
		}				
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

	$scope.refreshExtractionBlankSampleCodeLists=function(){
		$scope.lists.clear('sampleDNA');
		$scope.lists.refresh.samples({"projectCodes":"CDW"}, 'sampleDNA'); //CDW
	};


	$scope.sample = {
			extractionDNABlankSampleCode:null
	};

	$scope.refreshExtractionBlankSampleCodeLists();
	atmService.experimentToView($scope.experiment, $scope.experimentType);					
	$scope.atmService = atmService;

}]);