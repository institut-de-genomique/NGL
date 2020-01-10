angular.module('home').controller('TubesToMapCardCtrl',['$scope', '$parse', 'atmToSingleDatatable',
                                                               function($scope, $parse, atmToSingleDatatable) {
	
	// NGL-1055: name explicite pour fichier CSV exporté: typeCode experience
	// NGL-1055: mettre getArray et codes:'' dans filter et pas dans render
	var datatableConfig = {
			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[
			         {
			        	 "header":Messages("containers.table.supportCode"),
			        	 "property":"inputContainer.support.code",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":0,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			         {
			        	 "header":Messages("containers.table.categoryCode"),
			        	 "property":"inputContainer.support.categoryCode",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":1,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
					 {
			        	 "header":Messages("containers.table.code"),
			        	 "property":"inputContainer.code",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":2,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			         {
			        	"header":Messages("containers.table.projectCodes"),
			 			"property": "inputContainer.projectCodes",
			 			"order":false,
			 			"hide":true,
			 			"type":"text",
			 			"position":3,
			 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
				     },
				     {
			        	"header":Messages("containers.table.sampleCodes"),
			 			"property": "inputContainer.sampleCodes",
			 			"order":false,
			 			"hide":true,
			 			"type":"text",
			 			"position":4,
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
			        	 "position":5,
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
				showButton: false,
				changeClass:false,
				mode:'local',
				callback:function(datatable){
					copyFlowcellCodeToDT(datatable);
				}
			},
			hide:{
				active:true
			},
			edit:{
				active: ($scope.isEditModeAvailable() && $scope.isNewState()),
				showButton: false,
				columnMode:true
			},
			messages:{
				active:false,
				columnMode:true
			},
			extraHeaders:{
				number:2,
				dynamic:true,
			},
			exportCSV:{
				active:true,
				showButton:true,
				delimiter:";",
				start:false
			}
	};
	
	var copyFlowcellCodeToDT = function(datatable){
		
		var dataMain = datatable.getData();
		//copy flowcell code to output code
		var codeFlowcell = $parse("instrumentProperties.containerSupportCode.value")($scope.experiment);
		if(null != codeFlowcell && undefined != codeFlowcell){
			for(var i = 0; i < dataMain.length; i++){
				var atm = dataMain[i].atomicTransfertMethod;
				$parse('outputContainerUsed.code').assign(dataMain[i],codeFlowcell);
				$parse('outputContainerUsed.locationOnContainerSupport.code').assign(dataMain[i],codeFlowcell);
				
				if(dataMain[i].inputContainerUsed.percentage != 100/dataMain.length){
					dataMain[i].inputContainerUsed.percentage = 100/dataMain.length;
				}
				
			}				
		}
		//datatable.setData(dataMain);
	}
	
	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event save");
		$scope.atmService.data.save();
		$scope.atmService.viewToExperimentManyToOne($scope.experiment);
		$scope.$emit('childSaved', callbackFunction);
	});
	
	$scope.$on('refresh', function(e) {
		console.log("call event refresh");
	
		var dtConfig = $scope.atmService.data.getConfig();
		dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isNewState());
		dtConfig.remove.active = ($scope.isEditModeAvailable() && $scope.isNewState());
		$scope.atmService.data.setConfig(dtConfig);
		
		$scope.atmService.refreshViewFromExperiment($scope.experiment);
		$scope.$emit('viewRefeshed');
	});
	
	$scope.$on('cancel', function(e) {
		console.log("call event cancel");
		$scope.atmService.data.cancel();
		
	});
	
	$scope.$on('activeEditMode', function(e) {
		console.log("call event activeEditMode");
		$scope.atmService.data.selectAll(true);
		$scope.atmService.data.setEdit();
	});
	
	var atmService = atmToSingleDatatable($scope, datatableConfig);
	//defined new atomictransfertMethod
	atmService.newAtomicTransfertMethod = function(){
		return {
			class:"ManyToOne",
			line:"1", 
			column:"1", 				
			inputContainerUseds:new Array(0), 
			outputContainerUseds:new Array(0)
		};
	};
	
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	$scope.atmService = atmService;
}]);