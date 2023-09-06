// FDS 23/02/2017 -- JIRA NGL-1167
angular.module('home').controller('ChromiumGemCtrl',['$scope', '$parse',  '$filter', 'atmToSingleDatatable', 
	                                                     function($scope, $parse, $filter, atmToSingleDatatable ){	
	var inputExtraHeaders=Messages("experiments.inputs");
	var outputExtraHeaders=Messages("experiments.outputs");	
	
	var datatableConfig = {
			name: $scope.experiment.typeCode.toUpperCase(),

			"columns":[
			         //--------------------- INPUT containers section -----------------------
			         // entree tubes
					{
			        	 "header":Messages("containers.table.code"),
			        	 "property":"inputContainer.code",
			        	 "order":true,
						 "hide":true,
			        	 "type":"text",
			        	 "position":0,
			        	 "extraHeaders":{0: inputExtraHeaders}
			         },	
			         { // Projet(s)
			        	"header":Messages("containers.table.projectCodes"),
			 			"property": "inputContainer.projectCodes",
			 			"order":true,
			 			"hide":true,
			 			"type":"text",
			 			"position":11,
			 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	 "extraHeaders":{0: inputExtraHeaders}
				     },
				     { // Echantillon(s) 
			        	"header":Messages("containers.table.sampleCodes"),
			 			"property": "inputContainer.sampleCodes",
			 			"order":true,
			 			"hide":true,
			 			"type":"text",
			 			"position":12,
			 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	"extraHeaders":{0: inputExtraHeaders}
				     },
				     { // sampleAliquoteCode 
				        "header":Messages("containers.table.codeAliquot"),
				 		"property": "inputContainer.contents", 
				 		"filter": "getArray:'properties.sampleAliquoteCode.value'",
				 		"order":true,
				 		"hide":true,
				 		"type":"text",
				 		"position":13,
				 		"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
				        "extraHeaders":{0: inputExtraHeaders}
					 },
					 { // Concentration
			        	 "header":Messages("containers.table.concentration") + " (ng/µL)",
			        	 "property":"inputContainerUsed.concentration.value",
			        	 "order":true,
						 "hide":true,
			        	 "type":"number",
			        	 "position":14,
			        	 "extraHeaders":{0: inputExtraHeaders}
			         },  
			         { // Volume
			        	 "header":Messages("containers.table.volume") + " (µL)",
			        	 "property":"inputContainerUsed.volume.value",
			        	 "order":true,
						 "hide":true,
			        	 "type":"number",
			        	 "position":15,
			        	 "extraHeaders":{0: inputExtraHeaders}
			         },
			         { // Etat input Container 
			        	 "header":Messages("containers.table.state.code"),
			        	 "property":"inputContainer.state.code | codes:'state'",
			        	 "order":true,
						 "hide":true,
			        	 "type":"text",
			        	 "position":16,
			        	 "extraHeaders":{0: inputExtraHeaders}
			         },
			         //--->  colonnes specifiques experience s'inserent ici  (inputUsed ??)     
			         
			         //------------------------- OUTPUT containers section --------------------------
			         
			         //--->  colonnes specifiques experience s'inserent ici  (outputUsed ??)
			         
			         { //  barcode du containerSupport strip sortie == support Container used code... faut Used 
			        	 "header":Messages("containers.table.support.name"),
			        	 "property":"outputContainerUsed.locationOnContainerSupport.code", 
						 "hide":true,
			        	 "type":"text",
			        	 "position":35,
			        	 "extraHeaders":{0: outputExtraHeaders}
			         },      
			         { // colonne==> position dans le strip ( renommer ??)
			        	 "header":Messages("containers.table.support.column"),
			        	 // ne pas utiliser  *1 ici car affiche "0" quand n'est pas encore defini...
			        	 "property":"outputContainerUsed.locationOnContainerSupport.column", 
			        	 "order":true,
						 "hide":true,
			        	 "type":"number",
			        	 "position":37,
			        	 "extraHeaders":{0: outputExtraHeaders}
			         },	
			         { // Etat outpout container      
			        	 "header":Messages("containers.table.state.code"),
			        	 "property":"outputContainer.state.code | codes:'state'",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":40,
			        	 "extraHeaders":{0: outputExtraHeaders}
			         }
			         ],
			"compact":true,
			"pagination":{
				"active":false
			},		
			"search":{
				"active":false
			},
			"order":{
				"mode":"local",
				"active":true
			},
			"remove":{
				"active": ($scope.isEditModeAvailable() && $scope.isNewState()),
				"showButton": ($scope.isEditModeAvailable() && $scope.isNewState()),
				"mode":"local"
			},
			"save":{
				"active":true,
	        	"withoutEdit": true,
	        	"changeClass":false,
	        	"showButton":false,
	        	"mode":"local" ,
	        	"callback":function(datatable){
	        		// 26/03/2019 dans les 2 cas il y a un bug si utilisateur ne fourni aucun outputSupportCode
	        		//     => pas un bug liee a la factorisation dans atmService!!!
	        		//copyContainerSupportCodeAndStorageCodeToDT(datatable);
	        		atmService.copyContainerSupportCodeAndStorageCodeToDT(datatable,'chip');
	        	}
			},
			"hide":{
				"active":true
			},
			"edit":{ // editable si mode=Finished ????
				active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
				showButton: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
				byDefault:($scope.isCreationMode()),
				columnMode:true
			},
			"messages":{
				"active":false,
				"columnMode":true
			},
			"exportCSV":{
				"active":true,
				"showButton":true,
				"delimiter":";",
				"start":false
			},
			"extraHeaders":{
				"number":2,
				"dynamic":true,
			}
	}; // fin struct datatableConfig

	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event save");
			$scope.atmService.data.save();
			$scope.atmService.viewToExperimentOneToOne($scope.experiment);
			$scope.$emit('childSaved', callbackFunction);
	});
	
	
	// Remplacé par version dans atmService + pos='chip' 
	var copyContainerSupportCodeAndStorageCodeToDT = function(datatable){
		
		var dataMain = datatable.getData();
		var outputContainerSupportCode = $scope.outputContainerSupport.code;
		var outputContainerSupportStorageCode = $scope.outputContainerSupport.storageCode;

		// 26/03/2019 correction locale pour NGl-2371: ajout && "" != outputContainerSupportCode
		if ( null != outputContainerSupportCode && undefined != outputContainerSupportCode && "" != outputContainerSupportCode ){
			for(var i = 0; i < dataMain.length; i++){
				
				var atm = dataMain[i].atomicTransfertMethod;
				// recuperer la valeur du select "chipPosition"
				var newChipPos =$parse("inputContainerUsed.instrumentProperties.chipPosition.value")(dataMain[i]);
				console.log("data :"+ i + "=> new chip position =" + newChipPos);
						
				if ( null != newChipPos ) {		
					// creation du code du container
					var newContainerCode = outputContainerSupportCode+"_"+newChipPos ;
					console.log("newContainerCode="+ newContainerCode);
					
					$parse('outputContainerUsed.code').assign(dataMain[i],newContainerCode);
					$parse('outputContainerUsed.locationOnContainerSupport.code').assign(dataMain[i],outputContainerSupportCode);
					
					//assigner la column et line du support !!!!
					$parse('outputContainerUsed.locationOnContainerSupport.line').assign(dataMain[i],1);
					$parse('outputContainerUsed.locationOnContainerSupport.column').assign(dataMain[i],newChipPos);
					
					// Historique mais continuer a renseigner car effets de bord possible ????
					$parse('line').assign(atm,1);
					$parse('column').assign(atm,newChipPos);
					//console.log("atm.line="+ atm.line + " atm.column="+atm.column);	
				
					if( null != outputContainerSupportStorageCode && undefined != outputContainerSupportStorageCode){
						$parse('outputContainerUsed.locationOnContainerSupport.storageCode').assign(dataMain[i],outputContainerSupportStorageCode);
					}
				}
			}	
		}
		// Ne plus faire ... datatable.setData(dataMain);
	}
	
	
	// ajout showButton + suppression start = false;
	$scope.$on('refresh', function(e) {
		console.log("call event refresh");		
		var dtConfig = $scope.atmService.data.getConfig();
		dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));
		dtConfig.edit.showButton = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));
		dtConfig.edit.byDefault = false;
		dtConfig.remove.active = ($scope.isEditModeAvailable() && $scope.isNewState());
		$scope.atmService.data.setConfig(dtConfig);
		$scope.atmService.refreshViewFromExperiment($scope.experiment);
		// NGL-2371 FDS 20/03/2019 récupérer outputContainerSupport s'il a été généré automatiquement (pas de barcode entré par l'utilisateur)
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
		
	//Init
	
	var atmService = atmToSingleDatatable($scope, datatableConfig);
	
	//FDS: line forcee a 1 pour strip-8;
	atmService.newAtomicTransfertMethod = function(l, c){
		return {
			class:"OneToOne",
			line: "1", 
			column: undefined,
			inputContainerUseds:new Array(0), 
			outputContainerUseds:new Array(0)
		};
	};
	
	//defined default output unit
	atmService.defaultOutputUnit = {
			volume : "µL"
	};
	
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	// pour que les proprietes d'instrument soient injectees dans le datatable
	// ne marche QUE avec watch... 
	$scope.$watch("instrumentType", function(newValue, oldValue){
		if(newValue)
			$scope.atmService.addInstrumentPropertiesToDatatable(newValue.propertiesDefinitions);
	});
	
	// verification du nombre d'inputs container... il faut passer par le basket
	if ( $scope.isCreationMode() && $scope.mainService.getBasket().length() > 8 ){ 
		//31/03/2022 il exite maintenant setWarning...
		$scope.messages.setWarning("Warning: "+ Messages('experiments.input.error.maxContainers',8));
	}else{
		$scope.messages.clear();
		$scope.atmService = atmService;
	}
	
	
	$scope.outputContainerSupport = { code : null , storageCode : null};	

	if ( undefined !== $scope.experiment.atomicTransfertMethods[0]) { 
		 $scope.outputContainerSupport.code=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.code;
		//console.log("previous code: "+ $scope.outputContainerSupport.code);
	}
	if ( undefined !== $scope.experiment.atomicTransfertMethods[0]) {
		 $scope.outputContainerSupport.storageCode=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.storageCode;
		 //console.log("previous storageCode: "+ $scope.outputContainerSupport.storageCode);
	}

}]);