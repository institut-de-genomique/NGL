angular.module('home').controller('OneToVoidCtrl',['$scope', '$parse','atmToSingleDatatable',
                                                             function($scope,$parse, atmToSingleDatatable) {
	
	 // NGL-1055: name explicite pour fichier CSV exporté: typeCode experience
	 // NGL-1055: mettre les getArray et codes'' dans filter et pas dans render
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
			        	 "position":1,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			         {
			        	 "header":Messages("containers.table.categoryCode"),
			        	 "property":"inputContainer.support.categoryCode",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":2,
			 			 "filter":"codes:'container_support_cat'",
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
					 {
			        	 "header":Messages("containers.table.code"),
			        	 "property":"inputContainer.code",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":3,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
					 {
			        	 "header":Messages("containers.table.projectCodes"),
			        	 "property":"inputContainer.projectCodes",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":4,
			        	 "render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
					 {
			        	 "header":Messages("containers.table.sampleCodes"),
			        	 "property":"inputContainer.sampleCodes",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":5,
			        	 "render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
					 {
			        	 "header":Messages("containers.table.stateCode"),
			        	 "property":"inputContainer.state.code",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
						 "filter":"codes:'state'",
			        	 "position":6,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			         {
			        	 "header":Messages("containers.table.fromTransformationTypeCodes"),
			        	 "property":"inputContainer.fromTransformationTypeCodes",
			        	 "filter": "unique | codes:'type'",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			 			"render":"<div list-resize='cellValue ' list-resize-min-size='3'>",
			        	 "position":7,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
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
			},
			save:{
				active:true,
	        	changeClass:false,
	        	showButton:false,
	        	withoutEdit: true,
				mode:'local',
			},
			hide:{
				active:true
			},
			edit:{
				active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('IP')),
				showButton: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('IP')),
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

		$scope.$on('save', function(e, callbackFunction) {	
			console.log("call event save on one-to-void");
			$scope.atmService.data.save();
			$scope.atmService.viewToExperimentOneToVoid($scope.experiment);
			$scope.$emit('childSaved', callbackFunction);
		});
		
		$scope.$on('refresh', function(e) {
			console.log("call event refresh on one-to-void");		
			var dtConfig = $scope.atmService.data.getConfig();
			dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('IP'));
			dtConfig.edit.byDefault = false;
			dtConfig.remove.active = ($scope.isEditModeAvailable() && $scope.isNewState());
			$scope.atmService.data.setConfig(dtConfig);
			$scope.atmService.refreshViewFromExperiment($scope.experiment);
			$scope.$emit('viewRefeshed');
		});
		
		$scope.$on('cancel', function(e) {
			console.log("call event cancel on one-to-void");
			$scope.atmService.data.cancel();	
			if($scope.isCreationMode()){
				var dtConfig = $scope.atmService.data.getConfig();
				dtConfig.edit.byDefault = false;
				$scope.atmService.data.setConfig(dtConfig);
			}
		});
		
		$scope.$on('activeEditMode', function(e) {
			console.log("call event activeEditMode on one-to-void ");
			$scope.atmService.data.selectAll(true);
			$scope.atmService.data.setEdit();
		});
		
		var atmService = atmToSingleDatatable($scope, datatableConfig, true);
		//defined new atomictransfertMethod
		atmService.newAtomicTransfertMethod = function(line, column){
			return {
				class:"OneToVoid",
				line:line, 
				column:column, 				
				inputContainerUseds:new Array(0)
			};
		};
		
		atmService.experimentToView($scope.experiment, $scope.experimentType);
		
		$scope.atmService = atmService;
		

}]);