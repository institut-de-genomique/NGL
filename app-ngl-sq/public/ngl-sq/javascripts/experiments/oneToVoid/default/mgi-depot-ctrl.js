angular.module('home').controller('MgiDepotCtrl',['$scope', '$parse','$http','atmToSingleDatatable','dateServices', 
	function($scope,$parse, $http, atmToSingleDatatable, dateServices) {

	// Voir si on laisse ce controlleur dans default ou dans cns selon les specs du CNG!!!
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
					"header":Messages("containers.table.workNameInit"),
					"property":"inputContainer.contents",
					"filter":"getArray:'properties.workName.value'| unique",
					"order":true,
					"edit":false,
					"hide":true,
					"type":"text",
					"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
					"position":3.5,
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
					"position":4.5,
					"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
					"extraHeaders":{0:Messages("experiments.inputs")}
				},
				{
					"header":Messages("containers.table.tagCategory"),
					"property":"inputContainer.contents| getArray:'properties.tagCategory.value'| unique",
					"order":true,
					"edit":false,
					"hide":true,
					"type":"text",
					"position":5,
					"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
					"extraHeaders":{0:Messages("experiments.inputs")}
				},
				{
					"header":Messages("containers.table.expectedPrimaryTags"),
					"property": "inputContainer.contents",
					"filter" : "getArray:'properties.expectedPrimaryTags.value' | unique",
					"order":false,
					"edit":false,
					"hide":true,
					"type":"text",
					"position":5.1,
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

	// NGL-3000 ajouter une vérification de la date saisie + controle date n'est pas dans le futur
	// 21/10/2020 isValidDateFormat ne controle pas une date entrée sur 2 digits...la retirer pour l'instant
	$scope.$on('save', function(e, callbackFunction) {
		console.log("call event save");
		if ( ! $scope.experiment.experimentProperties.starRunDate ) {
			save(callbackFunction);
		} else {
			// une valeur definie existe ( elle est force a undefined a l'etat new...) => la controler
			// avec l'utilisation d'un calendrier cette verification est superflue...
			/*if ( ! dateService.isValidDateFormat($scope.experiment.experimentProperties.runStartDate.value, Messages("date.format"))){
				$scope.messages.setError(Messages("experiment.msg.badformat", "Date réelle de dépôt", Messages("date.format")));
				$scope.$emit('childSavedError', callbackFunction);
			} else {*/
			//tout OK sauvegarder !!!
			save(callbackFunction);
			//}
		}
	});

	// ancien contenu de $scope.$on('save', ......
	function save(callbackFunction){
		$scope.atmService.data.save();
		$scope.atmService.viewToExperimentOneToVoid($scope.experiment);

		$scope.$emit('childSaved', callbackFunction);
	}

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
		console.log("call event activeEditMode on one-to-void (default)");
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

	// var dateService=dateServices($scope);

	//  pas de feuille de route pour l'instant pour dnbseq-g400
	/*$scope.setAdditionnalButtons([{
		isDisabled : function(){return $scope.isCreationMode();},
		isShow:function(){return ($scope.experiment.instrument.typeCode != 'dnbseq-g400')},
		click:$scope.fileUtils.generateSampleSheet,
		label:Messages("experiments.sampleSheet")
	}]);*/

	// en mode creation positionner la date courante par defaut
	if($scope.isCreationMode()){
		if(!$parse("experimentProperties.runStartDate.value")($scope.experiment)){
			console.log('setting current date as default');
			// dans le javascript positionner une valeur en millisecondes en utilisant la librairie moment.js
			// le rendu au format date est executé par la directive date-timestamp (dateTimestamp) ou date-timestamp2 (dateTimestamp2) 
			var curdateMS= moment().valueOf();
			$parse("experimentProperties.runStartDate.value").assign($scope.experiment, curdateMS);
		}
	}

	

}]);