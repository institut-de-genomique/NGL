angular.module('home').controller('NanoporeDepotCtrl',['$scope', '$parse', '$http','$window','atmToSingleDatatable', 'datatable',
                                                               function($scope, $parse,  $http, $window, atmToSingleDatatable, datatable) {
	
	// NGL-1055: name explicite pour fichier CSV exporté: typeCode experience
	// NGL-1055: mettre getArray et codes:'' dans filter et pas dans render
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
			 			"order":true,
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
			 			"filter" : "getArray:'properties.tag.value' | unique",
			 			"order":false,
			 			"edit":false,
			 			"hide":true,
			 			"type":"text",
			 			"position":4.5,
			 			"render":"<div list-resize='cellValue' 'list-resize-min-size='3'>",
			 			"extraHeaders":{0:Messages("experiments.inputs")}			 			
			 		},
			         					 
					 {
			        	 "header":Messages("containers.table.concentration") + " (ng/µL)",
			        	 "property":"inputContainerUsed.concentration.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":5,
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
			        	 "position":6.5,
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
			        	 "header":Messages("containers.table.code"),
			        	 "property":"outputContainerUsed.code",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":500,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },
			         {
			        	 "header":Messages("containers.table.stateCode"),
			        	 "property":"outputContainer.state.code | codes:'state'",
			        	 "order":true,
						 "edit":false,
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
				active:true
			},
			remove:{
				active: ($scope.isEditModeAvailable() && $scope.isNewState()),
				showButton: ($scope.isEditModeAvailable() && $scope.isNewState()),
				mode:'local'
			},
			save:{
				active:true,
	        	withoutEdit: true,
	        	showButton:false,
	        	mode:'local',
	        	changeClass:false,
				callback:function(datatable){
					copyOtherDTToMainDatatable(datatable);
				}
			},
			hide:{
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
			},
			showTotalNumberRecords:false
	};
	
	
	var datatableConfigLoadingReport = {
			//ici laisser ce nom ??
			name:"NanoportLoadingReport",
			columns:[],
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
				by:'creationDate',
				reverse : true
			},
			remove:{
				active: true,
				showButton: true,
				mode:'local'
			},
			save:{
				active:true,
				showButton: false,
				mode:'local',
				changeClass:false
			},
			hide:{
				active:true
			},
			edit:{
				active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
				showButton: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
				byDefault:($scope.isCreationMode()),
				columnMode:false
			},
			add:{
				active:true
			},
			messages:{
				active:false,
				columnMode:true
			},
			extraHeaders:{
				number:1,
				dynamic:true,
			},
			showTotalNumberRecords:false
	};
	

	var datatableConfigQcFlowcell = {
			// ici laiser ce nom ??
			name:"NanoportQcFlowcell",
			columns:[],
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
				by:'code'
			},
			remove:{
				active:false,
			},
			save:{
				active:true,
				showButton: false,
				mode:'local',
				changeClass:false
			},
			hide:{
				active:true
			},
			edit:{
				active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
				showButton: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
				byDefault:($scope.isCreationMode()),				
				columnMode:false
			},
			messages:{
				active:false,
				columnMode:true
			},
			extraHeaders:{
				number:1,
				dynamic:true,
			},
			showTotalNumberRecords:false
	};
	
	//call by callback save datatable
	var copyOtherDTToMainDatatable = function(datatable){
		var dataMain = datatable.getData();
		var dataQCFlowcell = $scope.datatableQcFlowcell.getData();
		var dataLoadingReport = $scope.datatableLoadingReport.getData();
	
		dataMain.forEach(function(dataMain){
			$parse('outputContainerUsed.experimentProperties.qcFlowcell._type').assign(dataMain, "object_list");
			$parse('outputContainerUsed.experimentProperties.qcFlowcell.value').assign(dataMain, dataQCFlowcell);
			
			$parse('inputContainerUsed.experimentProperties.loadingReport._type').assign(dataMain, "object_list");		
			$parse('inputContainerUsed.experimentProperties.loadingReport.value').assign(dataMain, dataLoadingReport);
			
			//copy flowcell code to output code
			var codeFlowcell = $parse("instrumentProperties.containerSupportCode.value")($scope.experiment);
			if(null != codeFlowcell && undefined != codeFlowcell){
				$parse('outputContainerUsed.code').assign(dataMain,codeFlowcell);
				$parse('outputContainerUsed.locationOnContainerSupport.code').assign(dataMain,codeFlowcell);
			}else{
				codeFlowcell = $parse("outputContainerUsed.instrumentProperties.containerSupportCode.value")(dataMain);
				$parse('outputContainerUsed.code').assign(dataMain,codeFlowcell);
				$parse('outputContainerUsed.locationOnContainerSupport.code').assign(dataMain,codeFlowcell);
			}
			
		//	var loadingQtty= $parse('outputContainerUsed.loadingQuantity.value');
			if(dataMain.inputContainerUsed.concentration){
				var concIN = dataMain.inputContainerUsed.concentration.value;
				var reportingList = dataLoadingReport ;
				var reportingVolSum=0;	
				if (null != reportingList){
					for(var j=0; j < reportingList.length; j++){
						reportingVolSum +=reportingList[j].volume;
					}
				}
				
				if(reportingVolSum){
					$parse('inputContainerUsed.experimentProperties.loadingQuantity.value').assign(dataMain,concIN * reportingVolSum);
				}
			}
		});
		
		
		
		//datatable.setData(dataMain);
	}
	
	$scope.$on('save', function(e, callbackFunction) {	
		$scope.datatableQcFlowcell.save();
		$scope.datatableLoadingReport.save();
		
		//save entraine appel copyOtherDTToMainDatatable
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
		dtConfig.remove.active = ($scope.isEditModeAvailable() && $scope.isNewState());
		$scope.atmService.data.setConfig(dtConfig);
		
		dtConfig = $scope.datatableQcFlowcell.getConfig();
		dtConfig.edit.byDefault = false;
		$scope.datatableQcFlowcell.setConfig(dtConfig);
		
		
		dtConfig = $scope.datatableLoadingReport.getConfig();
		dtConfig.edit.byDefault = false;
		dtConfig.edit.start = false; //BUG in UDT but not found //09/02/2017
		$scope.datatableLoadingReport.setConfig(dtConfig);
		
		
		$scope.atmService.refreshViewFromExperiment($scope.experiment);
		$scope.$emit('viewRefeshed');
	});
	
	$scope.$on('cancel', function(e) {
		console.log("call event cancel");
		$scope.atmService.data.cancel();
		$scope.datatableQcFlowcell.cancel();
		$scope.datatableLoadingReport.cancel();
		
		if($scope.isCreationMode()){
			//var dtConfig = $scope.atmService.data.getConfig();
			//dtConfig.edit.byDefault = false;
			//$scope.atmService.data.setConfig(dtConfig);
			
			var dtConfig = $scope.datatableLoadingReport.getConfig();
			dtConfig.edit.byDefault = false;
			$scope.datatableLoadingReport.setConfig(dtConfig);
			
			
			dtConfig = $scope.datatableQcFlowcell.getConfig();
			dtConfig.edit.byDefault = false;
			$scope.datatableQcFlowcell.setConfig(dtConfig);
		}
		
	});
	
	$scope.$on('activeEditMode', function(e) {
		console.log("call event activeEditMode");
		$scope.atmService.data.selectAll(true);
		$scope.atmService.data.setEdit();
		
		$scope.datatableQcFlowcell.selectAll(true);
		$scope.datatableQcFlowcell.setEdit();
		
		$scope.datatableLoadingReport.selectAll(true);
		$scope.datatableLoadingReport.setEdit();
	});
	
	//Init
	$scope.$watch("instrumentType", function(newValue, OldValue){
		if(newValue)
			$scope.atmService.addInstrumentPropertiesToDatatable(newValue.propertiesDefinitions);
	});
	var atmService = atmToSingleDatatable($scope, datatableConfig);
	//defined new atomictransfertMethod
	atmService.newAtomicTransfertMethod = function(){
		return {
			class:"OneToOne",
			line:"1", 
			column:"1", 				
			inputContainerUseds:new Array(0), 
			outputContainerUseds:new Array(0)
		};
	};
	
	//defined default output unit
	atmService.defaultOutputUnit = {
			volume : "µL",
			concentration : "ng/µl",
			quantity : "ng"
	}
	
	//overide defaut method
	atmService.convertOutputPropertiesToDatatableColumn = function(property, pName){
		if(property.propertyValueType === "single"){
			return  this.$commonATM.convertSinglePropertyToDatatableColumn(property,"outputContainerUsed."+pName+".",{"0":Messages("experiments.outputs")});
		}else if(property.propertyValueType === "object_list"){
			var newColum = this.$commonATM.convertObjectListPropertyToDatatableColumn(property,"",{"0":"QC Flowcell"});
			var columns = $scope.datatableQcFlowcell.getColumnsConfig();
			columns.push(newColum);
			$scope.datatableQcFlowcell.setColumnsConfig(columns);
			return undefined;						
		}		
	};
	atmService.convertInputPropertiesToDatatableColumn = function(property, pName){
		if(property.propertyValueType === "single"){
			return this.$commonATM.convertSinglePropertyToDatatableColumn(property,"inputContainerUsed."+pName+".",{"0":Messages("experiments.inputs")});
		}else if(property.propertyValueType === "object_list"){
			var newColum = this.$commonATM.convertObjectListPropertyToDatatableColumn(property,"",{"0":"Bilan chargement"});
			var columns = $scope.datatableLoadingReport.getColumnsConfig();
			columns.push(newColum);
			$scope.datatableLoadingReport.setColumnsConfig(columns);
			return undefined;
		}
		
	};
	//custom view for the two other datatable
	atmService.customExperimentToView = function(atm){
		var loadingReportData = $parse('inputContainerUseds[0].experimentProperties.loadingReport.value')(atm);
		if(null != loadingReportData && undefined !== loadingReportData)
			$scope.datatableLoadingReport.setData(loadingReportData);
		
		var qcFlowcellData = $parse('outputContainerUseds[0].experimentProperties.qcFlowcell.value')(atm);
		if(null != qcFlowcellData && undefined !== qcFlowcellData)
			$scope.datatableQcFlowcell.setData(qcFlowcellData);
	}
	
	$scope.atmService = atmService;
	$scope.datatableLoadingReport = datatable(datatableConfigLoadingReport);
	$scope.datatableLoadingReport.setData([]);
	
	var qcFlowcellDefault =[{group: "total", preLoadingNbActivePores: undefined, postLoadingNbActivePores: undefined}, 
	    	                {group: "groupe1", preLoadingNbActivePores: undefined, postLoadingNbActivePores:undefined},
	    	                {group: "groupe2", preLoadingNbActivePores: undefined, postLoadingNbActivePores:undefined},
	    	                {group: "groupe3", preLoadingNbActivePores: undefined, postLoadingNbActivePores:undefined},
	    	                {group: "groupe4", preLoadingNbActivePores: undefined, postLoadingNbActivePores:undefined}];
	
	$scope.datatableQcFlowcell = datatable(datatableConfigQcFlowcell);
	$scope.datatableQcFlowcell.setData(qcFlowcellDefault);
	
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	//init list runs
	if(angular.isArray($scope.experiment.outputContainerSupportCodes) && $scope.experiment.outputContainerSupportCodes.length > 0){
		$http.get(jsRoutes.controllers.runs.api.Runs.list().url,{params:{containerSupportCodes:$scope.experiment.outputContainerSupportCodes}}).success(function(data) {
			$scope.runs = data;
		});
	}
	$scope.goToBi = function(code){
		var value = AppURL("bi");
		$window.open(value+"/runs/"+code, 'bi');
	};

	if($scope.isCreationMode()){
		if(!$parse("experimentProperties.runStartDate.value")($scope.experiment)){
			var format = Messages("date.format").toUpperCase();
			var date = moment().format(format);
			date = moment(date, format).valueOf();
			$parse("experimentProperties.runStartDate.value").assign($scope.experiment, date); 
		}
	}
}]);