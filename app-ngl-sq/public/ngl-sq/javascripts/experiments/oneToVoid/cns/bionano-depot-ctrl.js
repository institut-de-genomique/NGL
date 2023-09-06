// FDS 13/10/2020 NGL-3000 ajout dateServices
angular.module('home').controller('BionanoDepotCtrl',['$scope', '$parse','atmToSingleDatatable', 'dateServices',
                                                             function($scope,$parse, atmToSingleDatatable, dateServices) {
	
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
				mode:'local', //or 
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
				mode:'local'
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
	 
	// NGL-3000 ajouter une vérification de la date saisie + controle date n'est pas dans le futur
	// 21/10/2020 isValidDateFormat ne controle pas une date entrée sur 2 digits...la retirer pour l'instant
	$scope.$on('save', function(e, callbackFunction) {
		console.log("call event save");
		// tester si la propriété existe (existe pas a l'etat new???)
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
			console.log("call event refresh on one-to-void");		
			var dtConfig = $scope.atmService.data.getConfig();
			dtConfig.edit.byDefault= false;
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
		
		var atmService = atmToSingleDatatable($scope, datatableConfig, true);
		//defined new atomictransfertMethod
		atmService.newAtomicTransfertMethod = function(){
			return {
				class:"OneToVoid",
				line:"1", 
				column:"1", 				
				inputContainerUseds:new Array(0)
			};
		};
		
		atmService.experimentToView($scope.experiment, $scope.experimentType);
		
		$scope.atmService = atmService;
		
		// ajouté pour NGL-3000
		// var dateService=dateServices($scope);
		
		//en mode creation initialiser experimentProperties.runStartDate sinon pb avec NGL-3000
		if($scope.isCreationMode()){
			if(!$parse("experimentProperties.runStartDate.value")($scope.experiment)){
				console.log('initialiser experimentProperties.runStartDate.value');
				$parse("experimentProperties.runStartDate.value").assign($scope.experiment, undefined); 
			}
		}
}]);