/* 01/08/2016 dupliqué a partir de CNS/pcr-amplification-and-purification-ctrl.js' 
 *            JIRA NGL-1027: pcr et purification en plaques*/

angular.module('home').controller('PcrAndPurificationCtrl',['$scope', '$parse', 'atmToSingleDatatable',
                                                    function($scope, $parse, atmToSingleDatatable){
	// variables pour extraheaders
	var inputExtraHeaders=Messages("experiments.inputs");
	var outputExtraHeaders=Messages("experiments.outputs");	
	
	var datatableConfig = {
			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[	
			         //--------------------- INPUT containers section -----------------------

			         { // barcode plaque entree == input support Container code
			        	 "header":Messages("containers.table.support.name"),
			        	 "property":"inputContainer.support.code",
						 "hide":true,
			        	 "type":"text",
			        	 "position":1,
			        	 "extraHeaders":{0: inputExtraHeaders}
			         },    
			         { // Ligne
			        	 "header":Messages("containers.table.support.line"),
			        	 "property":"inputContainer.support.line",
			        	 "order":true,
						 "hide":true,
			        	 "type":"text",
			        	 "position":2,
			        	 "extraHeaders":{0: inputExtraHeaders}
			         },
			         { // colonne
			        	 "header":Messages("containers.table.support.column"),
			        	 // astuce GA: pour pouvoir trier les colonnes dans l'ordre naturel forcer a numerique.=> type:number,   property:  *1
			        	 "property":"inputContainer.support.column*1",
			        	 "order":true,
						 "hide":true,
			        	 "type":"number",
			        	 "position":3,
			        	 "extraHeaders":{0: inputExtraHeaders}
			         },
			         {
			        	"header":Messages("containers.table.projectCodes"),
			 			"property": "inputContainer.projectCodes",
			 			"order":true,
			 			"hide":true,
			 			"type":"text",
			 			"position":4,
			 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	"extraHeaders":{0:inputExtraHeaders}
				     },
				     {
			        	"header":Messages("containers.table.sampleCodes"),
			 			"property": "inputContainer.sampleCodes",
			 			"order":true,
			 			"hide":true,
			 			"type":"text",
			 			"position":5,
			 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	"extraHeaders":{0:inputExtraHeaders}
				     },
				     { // 31/08/2017 ajout demandé: sampleAliquoteCode 
					     "header":Messages("containers.table.codeAliquot"),
					 	 "property": "inputContainer.contents", 
					 	 "filter": "getArray:'properties.sampleAliquoteCode.value'",
					 	 "order":true,
					 	 "hide":true,
					 	 "type":"text",
					 	 "position": 5.5,
					 	 "render":"<div list-resize='cellValue' list-resize-min-size='3'>",
					     "extraHeaders":{0: inputExtraHeaders}
					 },
				     {
			        	 "header":Messages("containers.table.fromTransformationTypeCodes"),
			        	 "property":"inputContainer.fromTransformationTypeCodes",
			        	 "filter":"unique | codes:'type'",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			 			 "render":"<div list-resize='cellValue'  list-resize-min-size='3'>",
			        	 "position":5.6,
			        	 "extraHeaders":{0:inputExtraHeaders}
			         },
			         { // 31/08/2017 ajout demandé: libProcessTypeCode;
				       "header": Messages("containers.table.libProcessType"),
				       "property" : "inputContainerUsed.contents",
				       "filter" : "getArray:'properties.libProcessTypeCode.value' |unique | codes:'value'",
				       "order":true,
					   "edit":false,
					   "hide":true,
				       "type":"text",
				       "position":5.8,
				       "extraHeaders":{0:inputExtraHeaders}
				     },
			         {
			        	"header":Messages("containers.table.tags"),
			 			"property": "inputContainer.contents",
			 			"filter": "getArray:'properties.tag.value'| unique",
			 			"order":true,
			 			"hide":true,
			 			"type":"text",
			 			"position": 5.9,
			 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	"extraHeaders":{0:inputExtraHeaders}
			         },	
			         {
			        	 "header":Messages("containers.table.volume") + " (µL)",
			        	 "property":"inputContainerUsed.volume.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":6,
			        	 "extraHeaders":{0:inputExtraHeaders}
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
			        	 "extraHeaders":{0:inputExtraHeaders}
			         },
			         // colonnes specifiques experience viennent ici...
			         //--------------------- OUTPUT containers section -----------------------
			         // 27/09/2016 ajout defaut value=30
			         {
			        	 "header":Messages("containers.table.volume")+ " (µL)",
			        	 "property":"outputContainerUsed.volume.value",
			        	 "editDirectives":"udt-change='updatePropertyFromUDT(value,col)'",
			        	 "tdClass":"valuationService.valuationCriteriaClass(value.data, experiment.status.criteriaCode, col.property)",
			        	 "order":true,
						 "edit":true,
						 "hide":true,
			        	 "type":"number",
			        	 "defaultValues":30,
			        	 "position":300,
			        	 "extraHeaders":{0:outputExtraHeaders}
			         },
			         /* ne pas afficher les containercodes  sauf pour DEBUG
			         {
			        	 "header":Messages("containers.table.code"),
			        	 "property":"outputContainerUsed.code",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":400,
			        	 "extraHeaders":{0:outputExtraHeaders}
			         },
			         */
			         { //  barcode plaque sortie == support Container used 
			        	 "header":Messages("containers.table.support.name"),
			        	 "property":"outputContainerUsed.locationOnContainerSupport.code", 
						 "hide":true,
			        	 "type":"text",
			        	 "position":500,
			        	 "extraHeaders":{0: outputExtraHeaders}
			         },  
			         { //  Ligne 
			        	 "header":Messages("containers.table.support.line"),
			        	 "property":"outputContainerUsed.locationOnContainerSupport.line", 
			        	 "order":true,
						 "hide":true,
			        	 "type":"text",
			        	 "position":600,
			        	 "extraHeaders":{0: outputExtraHeaders}
			         },     
			         { // colonne
			        	 "header":Messages("containers.table.support.column"),
			        	 // astuce GA: pour pouvoir trier les colonnes dans l'ordre naturel forcer a numerique.=> type:number,   property:  *1
			        	 "property":"outputContainerUsed.locationOnContainerSupport.column*1", 
			        	 "order":true,
						 "hide":true,
			        	 "type":"number",
			        	 "position":700,
			        	 "extraHeaders":{0: outputExtraHeaders}
			         },	
			         {
			        	 "header":Messages("containers.table.stateCode"),
			        	 "property":"outputContainer.state.code | codes:'state'",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":800,
			        	 "extraHeaders":{0:outputExtraHeaders}
			         },
			         {
			        	 "header":Messages("containers.table.storageCode"),
			        	 "property":"outputContainerUsed.locationOnContainerSupport.storageCode",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":900,
			        	 "extraHeaders":{0:outputExtraHeaders}
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
	        	changeClass:false,
	        	mode:'local',
	        	callback:function(datatable){
	        		// NGL-2371 FDS 11/03/2019 copyContainerSupportCodeAndStorageCodeToDT deplacée dans atmService + ajout 2eme param "pos"
	        		// tous les instruments de cette exp n'ont que plaque en ouputContainer, inutile de le tester
	        		// plaque inputContainer => pos='auto'
	        		atmService.copyContainerSupportCodeAndStorageCodeToDT(datatable,'auto');
	        	}
			},
			hide:{
				active:true
			},
			edit:{ // 08/01/2019 modif 'IP'=>'F' pour edition
				active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
				showButton: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
				byDefault:($scope.isCreationMode()),
				columnMode:true 
			},
			messages:{
				active:false
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
			otherButtons: {
                active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
                complex:true,
                template:''
                	+'<div class="btn-group" style="margin-left:5px">'
                	+'<button class="btn btn-default" ng-click="copyVolumeInToExp()" data-toggle="tooltip" title="'+Messages("experiments.button.plate.copyVolumeTo")+' volume engagé'
                	+'" ng-disabled="!isEditMode()" ng-if="experiment.instrument.outContainerSupportCategoryCode!==\'tube\'"><i class="fa fa-files-o" aria-hidden="true"></i> Volume </button>'                	                	
                	+'</div>'
			}
	};

	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event save");
			$scope.atmService.data.save();
			$scope.atmService.viewToExperimentOneToOne($scope.experiment);
			$scope.$emit('childSaved', callbackFunction);
	});
	
	$scope.$on('refresh', function(e) {
		console.log("call event refresh");		
		var dtConfig = $scope.atmService.data.getConfig();
		dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')); // modif IP=>F pour edition
		// showButton pas necessaire...
		dtConfig.edit.byDefault = false;
		dtConfig.remove.active = ($scope.isEditModeAvailable() && $scope.isNewState());
		$scope.atmService.data.setConfig(dtConfig);
		$scope.atmService.refreshViewFromExperiment($scope.experiment);
		// NGL-2371 FDS 20/03/2019 récupérer outputContainerSupport s'il été généré automatiquement (pas de barcode entré par l'utilisateur)
		$scope.outputContainerSupport.code=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.code;
		
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
	
	$scope.copyVolumeInToExp = function(){
		var data = $scope.atmService.data.displayResult;		
		data.forEach(function(value){
			$parse("inputContainerUsed.experimentProperties.inputVolume").assign(value.data, angular.copy(value.data.inputContainer.volume));			
		})		
	};
	
	// FDS 20/03/2018 : NGL-1906 rechercher le robotRunWorkLabel positionné au niveau processus pour le copier dans robotRunCode (sauf s'il y en plusieurs!!)
	/* NON.... le 22/03/2018
	$scope.$watch("experiment.instrument.code", function(newValue, OldValue){
		if ((newValue) && (newValue !== null ) && ( newValue !== OldValue ))  {
			// exemple dans prepa-fc-ordered: var categoryCodes = $scope.$eval("getBasket().get()|getArray:'support.categoryCode'|unique",mainService);
			// mais ici mainService n'est pas defini, et pas necessaire...
			// obliger de passer par contents[0], mais normalement ne doit pas poser de probleme...
			var workLabels= $scope.$eval("getBasket().get()|getArray:'contents[0].processProperties.robotRunWorkLabel.value'|unique");
			if ( workLabels.length > 1 ){
				$scope.messages.clear();
				$scope.messages.clazz = "alert alert-warning";
				$scope.messages.text = "Plusieurs 'nom de travail de run' trouvés";
				$scope.messages.open();			
			
				//console.log('>1  run workLabel trouvé !!');	
			} else if ( workLabels.length === 1 ){
				// verifier que TOUS les containers ont une valeur...
				var contents= $scope.$eval("getBasket().get()|getArray:'contents[0]'");
				var labels= $scope.$eval("getBasket().get()|getArray:'contents[0].processProperties.ngsRunWorkLabel.value'");
				if ( labels.length < contents.length ) {
					$scope.messages.clear();
					$scope.messages.clazz = "alert alert-warning";
					$scope.messages.text = "Certains containers n'ont pas de 'nom de travail de run'.";
					$scope.messages.open();			
				
					//console.log("Certains containers n'ont pas de workLabel.");
				} else {
					// NGL-2160/NGL-2164 ne faire l'assignation que si l'instrument possede la propriété robotRunCode (sinon erreur de sauvegarde experience!)
					if ( $scope.instrumentHasProperty('robotRunCode') ) {
						$parse("instrumentProperties.robotRunCode.value").assign($scope.experiment, workLabels[0]);
					} else {
						console.log("la propriété n'est pas gérée par l'instrument!");
						// faut-il une alerte utilisateur ??
					}
				}
			} 
			// si aucun workLabel ne rien faire
	});
	*/
	
	// NGL-3346 03/06/2021 ajout instrument main dans l'expérience mais seule la sortie en plaque est gérée ==> forcer 
	$scope.$watch("experiment.instrument.outContainerSupportCategoryCode", function(){
		$scope.experiment.instrument.outContainerSupportCategoryCode = "96-well-plate";
	});
	
	//Init
	var atmService = atmToSingleDatatable($scope, datatableConfig);
	
	//defined new atomictransfertMethod
	atmService.newAtomicTransfertMethod = function(l,c){
		return {
			class:"OneToOne",
			line: l, 
			column: c, 				
			inputContainerUseds:new Array(0), 
			outputContainerUseds:new Array(0)
		};
	};
	

	//defined default output unit
	atmService.defaultOutputUnit = {
			volume : "µL",
			concentration : "nM"
	}
	
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	// 01/08/2017 TEST ajout checkOneSupportInput
	//if ( $scope.checkOneSupportInput() ){
	  $scope.atmService = atmService;
	//}
	
	
	$scope.outputContainerSupport = { code : null , storageCode : null};	
	
	if ( undefined !== $scope.experiment.atomicTransfertMethods[0]) { 
		 $scope.outputContainerSupport.code=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.code;
		 //console.log("previous code: "+ $scope.outputContainerSupport.code);
	}
	if ( undefined !== $scope.experiment.atomicTransfertMethods[0]) {
		$scope.outputContainerSupport.storageCode=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.storageCode;
		//console.log("previous storageCode: "+ $scope.outputContainerSupport.storageCode);
	}
	
	
	/*  inputQuantity   supprimee avant mise en prod 01/09/2016....donc plus de calcul
	// Calculs 
	$scope.updatePropertyFromUDT = function(value, col){
		console.log("update from property : "+col.property);

		if(col.property === 'inputContainerUsed.experimentProperties.inputVolume.value'){
			computeQuantity(value.data);
		}
	}

	//inputQuantity=inputContainerUsed.concentration.value * inputContainerUsed.experimentProperties.inputVolume.value
	var computeQuantity = function(udtData){
		var getter = $parse("inputContainerUsed.experimentProperties.inputQuantity.value");

		var compute = {
				inputConc : $parse("inputContainerUsed.concentration.value")(udtData),
				inputVolume : $parse("inputContainerUsed.experimentProperties.inputVolume.value")(udtData),		
				isReady:function(){
					return (this.inputConc && this.inputConc);
				}
			};
		
		if(compute.isReady()){
			var result = $parse("inputConc * inputVolume")(compute);
			console.log("result = "+result);
			
			if(angular.isNumber(result) && !isNaN(result)){
				inputQuantity = Math.round(result*10)/10;				
			}else{
				inputQuantity = undefined;
			}	
			getter.assign(udtData, inputQuantity);
			
		}else{
			console.log("Missing values to exec computeQuantity");
		}
	}
	*/
	
	
}]);