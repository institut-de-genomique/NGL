angular.module('home').controller('PacBioDepotCtrl',['$scope', '$parse','atmToSingleDatatable', '$http', '$window',
                                              function($scope,$parse, atmToSingleDatatable, $http, $window) {
	
	var inputExtraHeaders=Messages("experiments.inputs");
	var outputExtraHeaders=Messages("experiments.outputs");	

	 var datatableConfig = {
			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[
					//--------------------- INPUT containers section -----------------------
			         {
			        	 "header":Messages("containers.table.container"),
			        	 "property":"inputContainer.code",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":1,
			        	 "extraHeaders":{0:inputExtraHeaders}
			         },
					 {
						"header" : Messages("containers.table.support.line"),
						"property" : "inputContainer.support.line",
						"order" : true,
						"edit" : false,
						"hide" : true,
						"type" : "text",
						"position" : 1.1,
						"extraHeaders" : {0:inputExtraHeaders}
					},
					{
						"header" : Messages("containers.table.support.column"),
						"property" : "inputContainer.support.column*1",
						"order" : true,
						"edit" : false,
						"hide" : true,
						"type" : "number",
						"position" : 1.2,
						"extraHeaders" :{0:inputExtraHeaders}
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
			        	 "extraHeaders":{0:inputExtraHeaders}
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
			        	 "extraHeaders":{0:inputExtraHeaders}
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
			        	 "position":6,
			        	 "extraHeaders":{0:inputExtraHeaders}
			         },
					 {
						"header": Messages("containers.table.libProcessType"),
						"property" : "inputContainerUsed.contents",
						"filter" : "getArray:'properties.libProcessTypeCode.value' |unique | codes:'value'",
						"order":true,
						"edit":false,
						"hide":true,
						"type":"text",
						"position":7,
						"extraHeaders":{0:inputExtraHeaders}
					  },
					 {
						"header":Messages("containers.table.tags"),
						"property": "inputContainer.contents",
						"filter": "getArray:'properties.tag.value'| unique",
						"order":false,
						"hide":true,
						"type":"text",
						"position":8,
						"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
						"extraHeaders":{0:inputExtraHeaders}
		             },
					 {
						"header": Messages("containers.table.volume")  + " (µl)",
						"property":"inputContainer.volume.value",
						"order":true,
						"edit":false,
						"hide":true,
						"type":"number",
						"position":9,
						"extraHeaders":{0:inputExtraHeaders}
					},
					{   // ABCplateConcentration doit etre déclarée de niveau content dans prep-seq-plate-pacbio !!!
						"header": Messages("containers.table.ABCplateConcentration")+" (pM)",	
						// la valeur est stockée en nM il faut la convertir en pM
						"filter" : "getArray:'properties.ABCplateConcentration.value'| unique",
						"property":"inputContainer.contents[0].properties.ABCplateConcentration.value",
						"convertValue": {"active":true, "displayMeasureValue":"pM", "saveMeasureValue":"nM"},
			        	"order":true,
						"edit":false,
						"hide":true,
						"type":"number",
						"format": 2,
						"position":10,
						"extraHeaders":{0:inputExtraHeaders}
					},
					{
						"header":Messages("containers.table.idPlaqueSequencage"),
						"property": "inputContainer.contents",
						"filter": "getArray:'properties.seqPlate.value'| unique",
						"order":false,
						"hide":true,
						"type":"text",
						"position":11,
						"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
						"extraHeaders":{0:inputExtraHeaders}
		             },
					 {
						"header":Messages("containers.table.positionOnSeqPlate"),
						"property": "inputContainer.contents",
						"filter": "getArray:'properties.positionOnSeqPlate.value'| unique",
						"order":false,
						"hide":true,
						"type":"text",
						"position":12,
						"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
						"extraHeaders":{0:inputExtraHeaders}
		             },
                     {
			        	 "header":Messages("containers.table.stateCode"),
			        	 "property":"inputContainer.state.code",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
						 "filter":"codes:'state'",
			        	 "position":13,
			        	 "extraHeaders":{0:inputExtraHeaders}
			         },
					//--->  colonnes specifiques experience s'inserent ici  (inputContainerUsed ??)   
			         
			        //------------------------- OUTPUT containers section --------------------------
			         
			        //--->  colonnes specifiques experience s'inserent ici  (outputContainerUsed ??)
					// Code SMRT Cell
					
					//container de sortie
					{
						"header":Messages("containers.table.code"),
						"property":"outputContainerUsed.code",
						"order":true,
						"edit":false, // exceptionnellement l'utilistateur ne doit pas entrer de code support
						"hide":true,
						"type":"text",
						"position":400,
						"extraHeaders":{0:outputExtraHeaders}	
					},
					{
						"header":Messages("containers.table.stateCode"),
						"property":"outputContainer.state.code | codes:'state'",
						"order":false,
						"edit":false,
						"hide":true,
						"type":"text",
						"position":500,
						"extraHeaders":{0:Messages("experiments.outputs")}
					},
				],
			compact: true,
			pagination: {
				active:false
			},		
			search: {
				active: false
			},
			order:{
				mode:'local',
				active:true,
				by:'inputContainer.code'
			},
			remove:{
				active: ( $scope.isNewState()),
				withEdit : true,
				showButton: ($scope.isNewState()),				
				mode:'local'
			},
			save:{
				active:true,
	        	changeClass:false,
	        	showButton:false,
	        	withoutEdit: true,
				mode:'local',
				callback:function(datatable){
					copyValues(datatable);
				}
			},
			hide:{
				active:true
			},
			edit:{
				active: true,
				showButton: true,
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

	var copyValues = function(datatable){
		var dataMain = datatable.getData();
	
		dataMain.forEach(function(data) {
			//copy smrtCellCode code to output code
			var smrtCellCode = $parse("outputContainerUsed.experimentProperties.smrtCellCode.value")(data);

			if(null != smrtCellCode && undefined != smrtCellCode){
				$parse('outputContainerUsed.code').assign(data, smrtCellCode);
				$parse('outputContainerUsed.locationOnContainerSupport.code').assign(data, smrtCellCode);
			}
		});
	}

	$scope.goToBi = function(code){
		var value = AppURL("bi");
		$window.open(value+"/runs/"+code, 'bi');
	};

	$scope.$on('save', function(e, callbackFunction) {
		console.log("call event save");

		$scope.atmService.data.save();
		$scope.atmService.viewToExperimentOneToOne($scope.experiment);

		$scope.$emit('childSaved', callbackFunction);
	});
	
	$scope.$on('cancel', function(e) {
		console.log("call event cancel on one-to-one (default)");
		$scope.atmService.data.cancel();
	});
		
	$scope.$on('activeEditMode', function(e) {
		console.log("call event activeEditMode on one-to-one (default)"); 
		$scope.atmService.data.selectAll(true);
		$scope.atmService.data.setEdit();
	});

	if(angular.isArray($scope.experiment.outputContainerSupportCodes) && $scope.experiment.outputContainerSupportCodes.length > 0){
		$http.get(jsRoutes.controllers.runs.api.Runs.list().url,{params:{containerSupportCodes:$scope.experiment.outputContainerSupportCodes}}).success(function(data) {
			$scope.runs = data;
		});
	}	

	$scope.$on('refresh', function(e) {
		console.log("call event refresh");		
		var dtConfig = $scope.atmService.data.getConfig();
		dtConfig.edit.active = $scope.isEditModeAvailable();
		dtConfig.edit.byDefault = false;
		dtConfig.remove.active = ($scope.isEditModeAvailable() && $scope.isNewState());
		$scope.atmService.data.setConfig(dtConfig);
		$scope.atmService.refreshViewFromExperiment($scope.experiment);
		$scope.$emit('viewRefeshed');
	});

	var atmService = atmToSingleDatatable($scope, datatableConfig);
	
	atmService.newAtomicTransfertMethod = function(){
		return {
			class:"OneToOne",
			line:"1", 
			column:"1", 				
			inputContainerUseds:new Array(0), 
			outputContainerUseds:new Array(0)
		};
	};
		
	atmService.experimentToView($scope.experiment, $scope.experimentType);
		
	if ($scope.isCreationMode() && $scope.mainService.getBasket().length() > 1) { 
		$scope.messages.setError(Messages('experiments.input.error.max1Container'));
	} else{
		$scope.messages.clear();
		$scope.atmService = atmService;
	}
		
	if ($scope.isCreationMode()){
		if (!$parse("experimentProperties.runStartDate.value")($scope.experiment)){
			console.log('setting current date as default');

			var format = Messages("date.format").toUpperCase();
			var curdateMS_round = moment().format(format);
			curdateMS_round = moment(curdateMS_round, format).valueOf();
	
			$parse("experimentProperties.runStartDate.value").assign($scope.experiment, curdateMS_round);
		}
	}

	//NGL-5002 ajout Info message
	$scope.isShowInformation = false;

	$scope.setIsShowInformation=function(bool){
		$scope.isShowInformation=bool;
	}
	// show/hide div information
	$scope.toggleIsShowInformation=function(){
		if ( $scope.isShowInformation===false) { $scope.isShowInformation=true; }
		else {$scope.isShowInformation=false}
	}
		
}]);