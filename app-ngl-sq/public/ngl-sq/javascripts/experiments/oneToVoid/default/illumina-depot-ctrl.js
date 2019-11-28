angular.module('home').controller('IlluminaDepotCtrl',['$scope', '$parse','$http','atmToSingleDatatable',
                                                             function($scope,$parse, $http, atmToSingleDatatable) {
	
	 // NGL-1055: name explicite pour fichier CSV exporté: typeCode experience
	 // NGL-1055: mettre getArray et codes '' dans filter et pas dans render
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
			         // NGL-2378
					 {
			        	 "header":Messages("containers.table.tagCategory"),
			        	 "property":"inputContainer.contents| getArray:'properties.tagCategory.value'| unique",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":4.5,
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
			        	 "filter":"unique | codes:'type'",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			 			 "render":"<div list-resize='cellValue' list-resize-min-size='3'>",
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
				mode:'local',
				active:true,
				by:'inputContainer.code'
			},
			remove:{
				active:false,
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
				active: false
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
		console.log("call event save on one-to-void(default)");
		$scope.atmService.data.save();
		$scope.atmService.viewToExperimentOneToVoid($scope.experiment);
		$scope.$emit('childSaved', callbackFunction);
	});
		
	$scope.$on('refresh', function(e) {
		console.log("call event refresh on one-to-void (default)");
		var dtConfig = $scope.atmService.data.getConfig();
		$scope.atmService.data.setConfig(dtConfig);
		
		$scope.atmService.refreshViewFromExperiment($scope.experiment);
		$scope.$emit('viewRefeshed');
	});
		
	$scope.$on('cancel', function(e) {
		console.log("call event cancel on one-to-void (default)");
		$scope.atmService.data.cancel();
	});
		
	$scope.$on('activeEditMode', function(e) {
		console.log("call event activeEditMode on one-to-void (default)");// PB ne sort jamais si experience a etat Finished!!!
		$scope.atmService.data.selectAll(true);
		$scope.atmService.data.setEdit();
	});
	
	// init !!!
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
	
	// NGL-2456: pas de feuille de route pour sequenceur NEXTSEQ500
	$scope.setAdditionnalButtons([{
		isDisabled : function(){return $scope.isCreationMode();},
		isShow:function(){return ($scope.experiment.instrument.typeCode != 'NEXTSEQ500')},
		click:$scope.fileUtils.generateSampleSheet,
		label:Messages("experiments.sampleSheet")
	}]);
	
	if($scope.isCreationMode()){
		if(!$parse("experimentProperties.runStartDate.value")($scope.experiment)){
			var format = Messages("date.format").toUpperCase();
			var date = moment().format(format);
			date = moment(date, format).valueOf();
			$parse("experimentProperties.runStartDate.value").assign($scope.experiment, date); 
		}
	}
	
	//22/01/2018 NGL-1768 22/01/2018: importer le fichier XML du NovaSeq 6000
	var importNOVASEQ6000XMLfile = function(){
		console.log('Import NOVASEQ6000 RunParameters XML file');
		
		$scope.messages.clear();
		
		$http.post(jsRoutes.controllers.instruments.io.IO.importFile($scope.experiment.code).url, $scope.file)
		.success(function(data, status, headers, config) {
			$scope.messages.setSuccess(Messages('experiments.msg.import.success'));
			
			// data est l'experience retournée par input.java
			// PAS BESOIN ICI ....$scope.experiment.instrumentProperties= data.instrumentProperties;
			
			// et reagents ....
			$scope.experiment.reagents=data.reagents;
			
			// reinit select File...
			$scope.file = undefined;
			angular.element('#NOVASEQ6000XMLfile')[0].value = null;
			
			//refresh  reagents !!!
			$scope.$emit('askRefreshReagents');	
			})
		.error(function(data, status, headers, config) {
			$scope.messages.clazz = "alert alert-danger";
			$scope.messages.text = Messages('experiments.msg.import.error');
			$scope.messages.setDetails(data);
			$scope.messages.showDetails = true;
			$scope.messages.open();	
		
			// reinit select File..
			$scope.file = undefined;
			// il faut aussi réinitaliser le bouton d'import
			angular.element('#NOVASEQ6000XMLfile')[0].value = null;
		});		
	};
			
	$scope.buttonNOVASEQ6000XMLfile = {
			isShow:function(){
				//return (( $scope.isInProgressState() || $scope.isFinishState() ) &&  $scope.isEditMode() ); // MARCHE pas, editMode pas vu voir plus haut
				return ( $scope.isInProgressState() || $scope.isFinishState() );
				},
			isFileSet:function(){
				return ($scope.file === undefined)?"disabled":"";
			},
			click:importNOVASEQ6000XMLfile	
	};

}]);