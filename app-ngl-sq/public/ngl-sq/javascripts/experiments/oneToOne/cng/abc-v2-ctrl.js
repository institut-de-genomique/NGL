// 30/06/2025 création du controleur V2 !!!
angular.module('home').controller('ABCV2Ctrl', ['$scope', '$parse', 'atmToSingleDatatable',
                                                    function($scope, $parse, atmToSingleDatatable){

	var inputExtraHeaders=Messages("experiments.inputs");
	var outputExtraHeaders=Messages("experiments.outputs");	

	var datatableConfig = {
		name: $scope.experiment.typeCode.toUpperCase(),
		columns: [
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
			 	"header":Messages("containers.table.projectCodes"),
			 	"property": "inputContainer.projectCodes",
			 	"order":true,
			 	"hide":true,
			 	"type":"text",
			 	"position":2,
			 	"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			 	"extraHeaders":{0:inputExtraHeaders}
			},
			{
			 	"header":Messages("containers.table.sampleCodes"),
			 	"property": "inputContainer.sampleCodes",
			 	"order":true,
			 	"hide":true,
			 	"type":"text",
			 	"position":3,
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
			 	"position":4,
			 	"extraHeaders":{0:inputExtraHeaders}
			}, 
			{
				"header":Messages("containers.table.tags"),
			 	"property": "inputContainer.contents",
			 	"filter": "getArray:'properties.tag.value'| unique",
			 	"order":true,
			 	"hide":true,
			 	"type":"text",
			 	"position":4.5,
			 	"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			 	"extraHeaders":{0:inputExtraHeaders}
			}, 
			{
				"header":Messages("containers.table.concentration") + " (ng/µL)",
				"property":"inputContainerUsed.concentration.value",
				"order":true,
				"edit":false,
				"hide":true,
				"type":"number",
				"position":6,
				"extraHeaders":{0:inputExtraHeaders}
			},
			{
				"header":Messages("containers.table.volume") + " (µL)",
				"property":"inputContainerUsed.volume.value",
				"order":true,
				"edit":false,
				"hide":true,
				"type":"number",
				"position":7,
				"extraHeaders":{0:inputExtraHeaders}
			}, 
			{
				"header":Messages("containers.table.quantity")+ " (ng)",
				"property":"inputContainerUsed.volume.value * inputContainerUsed.concentration.value",
				"order":true,
				"edit":false,
				"hide":true,
				"type":"number",
				"position":8,
				"extraHeaders": { 0:inputExtraHeaders }
			}, 
			/* en V2 on ne l'affiche pas mais on la copie dans le container de sortie !!
			   voir ligne 299
			{
				"header":Messages("containers.table.size")+ " (pb)",
				"property": "inputContainerUsed.size.value",
				"order" : true,
				"edit" : false,
				"hide" : true,
				"type" : "number",
				"position" :9,
				"extraHeaders" : { 0 : inputExtraHeaders }
			},
			*/
			{
				"header":Messages("containers.table.state.code"),
				"property":"inputContainerUsed.state.code",
				"order":true,
				"edit":false,
				"hide":true,
				"type":"text",
				"filter":"codes:'state'",
				"position":24,
				"extraHeaders": { 0: inputExtraHeaders }
			},
					
			//--->  colonnes specifiques experience s'inserent ici  (inputContainerUsed)     
			         
			//------------------------- OUTPUT containers section --------------------------
			         
			//--->  colonnes specifiques experience s'inserent ici  (outputContainerUsed)

			{ // Volume final optionnel avec valeur par defaut
				"header":Messages("containers.table.finalVolume") + " (µL)",
				"property":"outputContainerUsed.volume.value",
				"hide":true,
				"edit":true,
				"type":"number",
				"defaultValues":26, // NGL-5251 passage a 26
				"position":34,
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
		compact: true,
		pagination: {
			active: false
		},		
		search: {
			active: false
		},
		order: {
			mode: 'local',
			active: true,
			by: 'inputContainer.code'
		},
		remove: {
			active: ($scope.isEditModeAvailable() && $scope.isNewState()),
			showButton: ($scope.isEditModeAvailable() && $scope.isNewState()),
			mode: 'local'
		},
		save: {
			active: true,
			withoutEdit: true,
			showButton: false,
			changeClass: false,
			mode: 'local',
			callback:function(datatable){
				if ( $scope.experiment.instrument.outContainerSupportCategoryCode === "strip-8" ){
					atmService.copyContainerSupportCodeAndStorageCodeToDT(datatable,'userdef'); // les positions sont controlees par updateATMLineAndColum
				}
			}
		},
		hide: {
			active: true
		},
		edit: {
			active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
			showButton: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
			byDefault: ($scope.isCreationMode()),
			columnMode: true
		},
		messages: {
			active: false
		},
		exportCSV: {
			active: true,
			showButton: true,
			delimiter: ";",
			start: false
		},
		extraHeaders: {
			number: 2,
			dynamic: true,
		},
		otherButtons: {
			active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F') && $scope.stripUtils.isStripToStrip()),
			"complex":true,
			"template":''
				+$scope.stripUtils.templates.buttonCopyPosition()
		}
	
	};

		// OUT: colonnes variables
		if ( $scope.experiment.instrument.outContainerSupportCategoryCode === "strip-8" ){	
			datatableConfig.columns.push({
				"header":Messages("containers.table.support.name"),
				"property":"outputContainerUsed.locationOnContainerSupport.code", 
				"order":true,
				"hide":true,
				"type":"text",
				"position":100,
				"extraHeaders":{0:outputExtraHeaders}
			});			
			datatableConfig.columns.push({
				"header":Messages("containers.table.support.column"),
				"property":"outputContainerUsed.locationOnContainerSupport.column",
				"edit" : true,
				"choiceInList":true,
				"possibleValues":[{"name":'1',"code":"1"},{"name":'2',"code":"2"},{"name":'3',"code":"3"},{"name":'4',"code":"4"},
				                  {"name":'5',"code":"5"},{"name":'6',"code":"6"},{"name":'7',"code":"7"},{"name":'8',"code":"8"}],
				"order":true,
				"hide":true,
				"type":"text",
				"position":110,
				"extraHeaders":{0:outputExtraHeaders}
			});		
	   } else {
			// tube
			 // meme pour les tubes utiliser  x.locationOnContainerSupport.code  et pas x.code
			datatableConfig.columns.push({
				"header":Messages("containers.table.code"),
			  	"property":"outputContainerUsed.locationOnContainerSupport.code",
			    "order":true,
				"edit":true,
				"editDirectives":"udt-change='atmService.emptyToNull(value.data, col.property)'",
				"hide":true,
				"type":"text",
				"position":100,
				"extraHeaders":{0:outputExtraHeaders}
			});
			datatableConfig.columns.push({
				"header":Messages("containers.table.storageCode"),
				"property":"outputContainerUsed.locationOnContainerSupport.storageCode",
				"order":true,
				"edit":true,
				"hide":true,
				"type":"text",
				"position":150,
				"extraHeaders":{0:outputExtraHeaders}
			});		 
	   }

	// interdire les plaques-96 en sortie
	$scope.$watch("experiment.instrument.outContainerSupportCategoryCode", function(){
		if ( $scope.experiment.instrument.outContainerSupportCategoryCode === '96-well-plate'){
			$scope.experiment.instrument.outContainerSupportCategoryCode = undefined;
			$scope.messages.setError(Messages('experiments.output.error.supportCategory','Plaque 96'));	
		} 
		//NE PAS METTRE ELSE CAR SINON ERREUR NE S'AFFICHE PAS !!!
		//else {
		//	$scope.messages.clear();
		//}
	});

	// !! si strip en sortie alors il faut max 8 tubes en entree ou 1 strip en entree
	// controle TODO ??

	$scope.updatePropertyFromUDT = function(value, col){
		console.log("update from property : "+col.property);
		if(col.property === 'inputContainerUsed.experimentProperties.inputVolume.value'){
			computeInputQuantity(value.data);	
		}
	};
	
	var atmService = atmToSingleDatatable($scope, datatableConfig);

	/*  line=1 pour strip ou tube; column=1 pour tube, a calculer pour strip */
	atmService.newAtomicTransfertMethod = function(l, c){
		return {
			class:"OneToOne",
			line:1, 
			column:($scope.experiment.instrument.outContainerSupportCategoryCode === "strip-8")?undefined:1,
			inputContainerUseds:new Array(0), 
			outputContainerUseds:new Array(0)
		};
	};
	
	atmService.defaultOutputUnit = {
			volume : "µL",
			concentration : "nM",
			size : "pb" // ajout 09/07/2025: copier la taille input=>output
	}

	// ajout 09/07/2025: copier la taille input=>output
	atmService.defaultOutputValue = {
		size : {copyInputContainer:true}
	};	

	atmService.experimentToView($scope.experiment, $scope.experimentType);

	/* V2: en entree tubes ou strip-8*/
	if (($scope.experiment.instrument.inContainerSupportCategoryCode === "tube") || ($scope.experiment.instrument.inContainerSupportCategoryCode === "strip-8")){
		$scope.messages.clear();
		$scope.atmService = atmService;
	} else{
		$scope.messages.setError(Messages('experiments.input.error.only-tubes-or-strips'));					
	}
	
	/*  pour strip mais ne gene pas avec tubes...
	   cette facon de faire ne permet de generer qu'un seul strip en sortie. Si besoin de plusieurs supprimer ce mécanisme 
	   et laisser l'utilisateur saisir les codes containers ! */
	$scope.outputContainerSupport = { code : null , storageCode : null};
	
	if ( undefined !== $scope.experiment.atomicTransfertMethods[0]) { 
		$scope.outputContainerSupport.code=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.code;
	   //console.log("previous code: "+ $scope.outputContainerSupport.code);
   }
   if ( undefined !== $scope.experiment.atomicTransfertMethods[0]) {
		$scope.outputContainerSupport.storageCode=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.storageCode;
		//console.log("previous storageCode: "+ $scope.outputContainerSupport.storageCode);
   }

	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event save");

		$scope.atmService.data.save();
		$scope.atmService.viewToExperimentOneToOne($scope.experiment);
		//  pour gestion des strip
		atmService.updateATMLineAndColum($scope.experiment);

		$scope.$emit('childSaved', callbackFunction);
	});

	$scope.$on('activeEditMode', function(e) {
		console.log("call event activeEditMode");
		$scope.atmService.data.selectAll(true);
		$scope.atmService.data.setEdit();
	});

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
	
	$scope.$on('cancel', function(e) {
		console.log("call event cancel");

		$scope.atmService.data.cancel();
	});

	var computeInputQuantity = function(udtData){
		console.log("computeInputQuantity...");

		var getter = $parse("inputContainerUsed.experimentProperties.inputQuantity.value");
		var volumeIn = $parse("inputContainerUsed.volume.value")(udtData);

		var compute = {
				inputConc : $parse("inputContainerUsed.concentration.value")(udtData),
				inputVolume : $parse("inputContainerUsed.experimentProperties.inputVolume.value")(udtData),		
				isReady:function(){
					// ajouter contrainte sur volume engagé qui doit etre <= volume disponible
					return (this.inputConc && this.inputVolume && (this.inputVolume <= volumeIn ));
					//return (this.inputConc && this.inputVolume);
				}
			};
		
		if(compute.isReady()){
			var result = $parse("inputConc * inputVolume")(compute);
			
			if(angular.isNumber(result) && !isNaN(result)){
				inputQuantity = Math.round(result*100)/100;				
			}else{
				inputQuantity = undefined;
			}	
			getter.assign(udtData, inputQuantity);
			
		}else{
			console.log("Valeurs manquantes pour calcul computeInputQuantity");
			getter.assign(udtData, undefined);
		}
	}

	

/////////////////////// ESSAI POUR AVOIR DES CALCULS DES L'AFFICHAGE.... MARCHE PAS !!!!!
/* function computeInputQuantitycINIT(){
	console.log ('computeInputQuantityINIT')

	// sur une expérience a nouveau les atms n'existent pas encore ???
	angular.forEach($scope.experiment.atomicTransfertMethods, function(atm){

		///var volumeIn = $parse("inputContainerUsed.volume.value")(atm);
		var volumeIn = atm.inputContainerUseds[0].volume.value;
		
	});
	

	// pas de displayResult non plus :-(   
	angular.forEach($scope.atmService.data.displayResult, function(dr){
		var volumeIn = dr.data.inputContainerUsed.volume.value;
		console.log ('>>>>>>>>>'+volumeIn);
	});
	
}

// calculs initiaux 
computeInputQuantityINIT();
*/
////////////////////////////////////////////////////

}]);