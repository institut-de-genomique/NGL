// 02/02/2022 NGL-3710 ajout tagService qui reprend certaines fonctions de tagPlates (suppression tagPlates car inutile ici)
angular.module('home').controller('LibAndPcrCtrl',['$scope', '$parse', 'atmToSingleDatatable','mainService','tagService',
                                                    function($scope, $parse, atmToSingleDatatable, mainService, tagService ){
	// variables pour extraheaders
	var inputExtraHeaders=Messages("experiments.inputs");
	var outputExtraHeaders=Messages("experiments.outputs");	
	
	var datatableConfig = {
			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[
		         //--------------------- INPUT containers section -----------------------
     	        
		          { // barcode entree == input support Container code
		        	 "header":Messages("containers.table.support.name"),
		        	 "property":"inputContainer.support.code",
					 "hide":true,
		        	 "type":"text",
		        	 "position":1,
		        	 "extraHeaders":{0: inputExtraHeaders}
		         },    
		         { // Projet(s)
		        	"header":Messages("containers.table.projectCodes"),
		 			"property": "inputContainer.projectCodes",
		 			"order":true,
		 			"hide":true,
		 			"type":"text",
		 			"position":4,
		 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
		        	 "extraHeaders":{0: inputExtraHeaders}
			     },
			     { // Echantillon(s) 
		        	"header":Messages("containers.table.sampleCodes"),
		 			"property": "inputContainer.sampleCodes",
		 			"order":true,
		 			"hide":true,
		 			"type":"text",
		 			"position":5,
		 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
		        	"extraHeaders":{0: inputExtraHeaders}
			     },   
			     { //sample Aliquot ajout 03/09/2019 NGL-2637
				        "header":Messages("containers.table.codeAliquot"),
				 		"property": "inputContainer.contents", 
				 		"filter": "getArray:'properties.sampleAliquoteCode.value'",
				 		"order":true,
				 		"hide":true,
				 		"type":"text",
				 		"position":6,
				 		"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
				        "extraHeaders":{0: inputExtraHeaders}
					 },
				 { // Concentration
		        	 "header":Messages("containers.table.concentration") + " (ng/µL)",
		        	 "property":"inputContainerUsed.concentration.value",
		        	 "order":true,
					 "hide":true,
		        	 "type":"number",
		        	 "position":7,
		        	 "extraHeaders":{0: inputExtraHeaders}
		         }, 
		         { //Volume 
		        	 "header":Messages("containers.table.volume") + " (µL)", 
		        	 "property":"inputContainerUsed.volume.value",
		        	 "order":true,
					 "hide":true,
		        	 "type":"number",
		        	 "position":8,
		        	 "extraHeaders":{0:inputExtraHeaders}
		         }, 
		         { // Etat input Container 
		        	 "header":Messages("containers.table.state.code"),
		        	 "property":"inputContainer.state.code | codes:'state'",
		        	 "order":true,
					 "hide":true,
		        	 "type":"text",
		        	 "position":9,
		        	 "extraHeaders":{0: inputExtraHeaders}
		         },
		         //--->  colonnes specifiques experience s'inserent ici  (inputUsed ??)     
		         
		         //------------------------- OUTPUT containers section --------------------------
		         
		         //--->  colonnes specifiques experience s'inserent ici  (outputUsed ??)
		         
		         { //  barcode du containerSupport sortie == support Container used code... faut Used 
		        	"header":Messages("containers.table.support.name"),
		        	 "property":"outputContainerUsed.locationOnContainerSupport.code", 
					 "hide":true,
		        	 "type":"text",
		        	 "edit": true,
		        	 "position":35,
		        	 "extraHeaders":{0: outputExtraHeaders}
		         },
		         {
		        	 "header":Messages("containers.table.stateCode"),
		        	 "property":"outputContainer.state.code | codes:'state'",
		        	 "order":true,
		        	 "edit":false,
		        	 "hide":true,
		        	 "type":"text",
		        	 "position":500,
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
				mode:'local'
			},
			hide:{
				active:true
			},
			edit:{ // F au lieu de IP
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
				dynamic:true
			}
	};
	
	////////////// SCOPE
	
	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event save");
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
		$scope.atmService.refreshViewFromExperiment($scope.experiment);
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

	// insertion dynamique des propriétés d'instrument de niveau containerOut
	$scope.$watch("instrumentType", function(newValue, OldValue){
		if(newValue)
			$scope.atmService.addInstrumentPropertiesToDatatable(newValue.propertiesDefinitions);
	});
	
	// pour l'insertion automatique des categories de tag
	$scope.updatePropertyFromUDT = function(value, col){
		//console.log("update from property : "+col.property);
		if(col.property === 'outputContainerUsed.experimentProperties.tag.value'){
			tagService.computeTagCategory(value.data);
		}
	}
	
	/////////////// INIT
	
	//1
	var atmService = atmToSingleDatatable($scope, datatableConfig);
	
	//2
	// ceci ne gere que les tubes en sortie
	atmService.newAtomicTransfertMethod = function(){
			return {
				class:"OneToOne",
				line:"1", 
				column:"1",
				inputContainerUseds:new Array(0),
				outputContainerUseds:new Array(0)
			};
	};
	
	//3
	//defined default output unit
	atmService.defaultOutputUnit = {
			volume : "µL"
	};
	
	//4
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	// seuls les tubes sont autorises en entree !!
	if($scope.experiment.instrument.inContainerSupportCategoryCode != "tube"){	
		//console.log("XX en entree");
		
		//$scope.messages.clear();
		//$scope.messages.clazz = "alert alert-danger";
		//$scope.messages.text = Messages("experiments.input.error.only-tubes");
		//$scope.messages.showDetails = false;
		//$scope.messages.open();
		
		$scope.messages.setError("Warning: "+ Messages("experiments.input.error.only-tubes"));
	} else {
		$scope.messages.clear();
		$scope.atmService = atmService;
	}

	// pour la liste des tags et l'insertion automatique des categories de tag
	if ( $scope.isNewState() || $scope.isInProgressState() || Permissions.check("admin") ){ 
		   tagService.initTags('index-illumina-sequencing'); // ajout filtre 
	}

}]);