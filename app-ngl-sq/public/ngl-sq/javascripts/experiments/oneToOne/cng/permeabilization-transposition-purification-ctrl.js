/* 02/05/2019  */

angular.module('home').controller('PermeabilizationTranspositionPurificationCtrl',['$scope', '$parse', 'atmToSingleDatatable',
                                                    function($scope, $parse, atmToSingleDatatable){
	// variables pour extraheaders
	var inputExtraHeaders=Messages("experiments.inputs");
	var outputExtraHeaders=Messages("experiments.outputs");	
	
	var datatableConfig = {
			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[
			         //--------------------- INPUT containers section -----------------------
					 // entree tubes
					{
						"header":Messages("containers.table.code"),
						"property":"inputContainer.code",
						"hide":true,
			        	"type":"text",
			        	"position":1,
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
				     // ajouter sampleType 
				     {
						"header":Messages("containers.table.sampleTypes"),
						"property" : "inputContainer.contents",
						"filter" : "getArray:'sampleTypeCode' | unique | codes:'type'",
						"order":true,
						"hide":true,
						"type":"text",
						"position": 5.5,
						"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
					    "extraHeaders":{0: inputExtraHeaders}
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
			        	"position":8,
			        	"extraHeaders":{0:inputExtraHeaders}
			         },
			         // colonnes specifiques experience viennent ici...
			         
			         //--------------------- OUTPUT containers section -----------------------
			         // sortie tube
			         {
			        	"header":Messages("containers.table.volume")+ " (µL)",
			        	"property":"outputContainerUsed.volume.value",
			        	///"editDirectives":"udt-change='updatePropertyFromUDT(value,col)'", commenté 15/02/2022 utilité ???
			        	"tdClass":"valuationService.valuationCriteriaClass(value.data, experiment.status.criteriaCode, col.property)",
			        	"order":true,
						"edit":true,
						"hide":true,
			        	"type":"number",
			        	"position":20,
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
			        	"position":30,
			        	"extraHeaders":{0:outputExtraHeaders}
			         },
			         */
			         { 
			        	"header":Messages("containers.table.code"),
			        	"property":"outputContainerUsed.locationOnContainerSupport.code", 
						"hide":true,
			        	"type":"text",
			        	"edit":true, // editable au CNG
			        	"position":40,
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
			edit:{
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
		dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));
		// showButton pas necessaire...
		dtConfig.edit.byDefault = false;
		dtConfig.remove.active = ($scope.isEditModeAvailable() && $scope.isNewState());
		
		//edition sampleCode en fonction de l'etat de l'experience
		$scope.updateEditModeSampleCode();
		
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
	
	$scope.copyVolumeInToExp = function(){
		var data = $scope.atmService.data.displayResult;
		data.forEach(function(value){
			$parse("inputContainerUsed.experimentProperties.inputVolume").assign(value.data, angular.copy(value.data.inputContainer.volume));
		})		
	};
	
	// ATTENTION !!!
	//    l'experience permeabilization-transposition-purification  creer un sample en sortie d'experience
	//    au CNRGH, c'est l'utilisateur qui choisi le code ( contrairement au CNS ou il est genere automatiquement)
	//    le code entree manuellement oblige a des validations (voir code java) supplementaires a l'etat nouveau (N). 
	//    Une fois l'experience a l'etat en cours (IP) le code barre ne doit plus etre édité sous peine de problemes de coherence
	//    sampleCode est une propriété de l'expérience définie a editable=true dans ExperimentServiceCNG.java sans specification d'etat !!!
	//    ==> ce code javascript permet de contourner ce probleme et de preciser qu'elle n'est editable qu'a l'etat Nouveau
	$scope.updateEditModeSampleCode = function(){
		//Get index SampleCode
		var index = $scope.atmService.data.config.columns.map(function (c) {return c.property;}).indexOf("outputContainerUsed.experimentProperties.sampleCode.value");
		if($scope.isNewState())
			$scope.atmService.data.config.columns[index].edit=true;
		else
			$scope.atmService.data.config.columns[index].edit=false;
	}
	//Init
	var atmService = atmToSingleDatatable($scope, datatableConfig);
	
	//defined new atomictransfertMethod
	atmService.newAtomicTransfertMethod = function(l,c){
		return {
			class:"OneToOne",
			// NGL-3745 si tube en sortie forcer l=1 et c=1 !!!
			// cette correction est inutile si seuls des tubes sont autorisés en entrée ( voir plus bas !)
			line:($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube")?undefined:"1",
			column:($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube")?undefined:"1",
			inputContainerUseds:new Array(0), 
			outputContainerUseds:new Array(0)
		};
	};
	
	//defined default output unit
	atmService.defaultOutputUnit = {
			volume : "µL",
			concentration : "nM"
	}
	
	// copié depuis CNS/dna-extraction pour la partie sampleTypeCode
	/* inutile si propriete definie par: Collections.singletonList(DescriptionFactory.newValue("DNA","ADN ")
	atmService.convertOutputPropertiesToDatatableColumn = function(property, pName){
		var column = atmService.$commonATM.convertTypePropertyToDatatableColumn(property,"outputContainerUsed."+pName+".",{"0":Messages("experiments.outputs")});
		if(property.code=="sampleTypeCode"){
			column.filter="getArray:'sampleTypeCode' | unique | codes:\"type\"";			
		}
		return column;
	};
	*/
	
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	//06/04/2022 n'accepter que des tubes en input car affichage inadapté
	//  utilisation  $scope.plateFluoUtils.getSupportCategoryCode()
	//  $scope.plateFluoUtils est dans details-ctrl.js; details-ctrl.js est deja appelé/inclus par ????
	var inputSupportCategoryCode = $scope.plateFluoUtils.getSupportCategoryCode();
	// retourne 'tube', 'plate-96-well' ou 'mixte'
	
	if ( inputSupportCategoryCode !== "tube") {
		$scope.messages.setError(Messages("experiments.input.error.only-tubes"));
	} else {
		$scope.messages.clear();
		$scope.atmService = atmService;
	}
	
	//edition sampleCode en fonction de l'etat de l'experience
	$scope.updateEditModeSampleCode();
	
	// forcer sortie en tube pour la main
	$scope.$watch("experiment.instrument.outContainerSupportCategoryCode", function(){
		if  ($scope.experiment.instrument.typeCode === "hand"){
			$scope.experiment.instrument.outContainerSupportCategoryCode = "tube";
		}	
	});
	
	/* commenté 15/02/2022 devrait faire quoi ????
	test : recopier le barcode sample dans containerOut.... MARCHE PAS A CONTINUER PLUS TARD
	$scope.updatePropertyFromUDT = function(value, col){
		console.log("update from property : "+col.property);
		
		if(col.property === 'outputContainerUsed.experimentProperties.sampleCode.value'){
			console.log("setting OutpuContainer code = sample code");

			$parse("outputContainerUsed.code").assign(value.data, angular.copy(value.data.outputContainerUsed.experimentProperties.sampleCode.value));
			
		}
	}
	*/
	
}]);