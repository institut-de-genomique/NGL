// 02/02/2022 NGL-3710 ajout tagService qui remprend certaines fonctions de tagPlates
angular.module('home').controller('LibraryPrepMetagCtrl',['$scope', '$parse',  '$filter', 'mainService','atmToSingleDatatable','$http','tagPlates','tagService',
                                             function($scope,  $parse,   $filter,    mainService,   atmToSingleDatatable,  $http,  tagPlates, tagService){
	
	var inputExtraHeaders=Messages("experiments.inputs");
	var outputExtraHeaders=Messages("experiments.outputs");	
	
	var datatableConfig = {
			name: $scope.experiment.typeCode.toUpperCase(),

			"columns":[
			         //--------------------- INPUT containers section -----------------------
			         
			         /* plus parlant pour l'utilisateur d'avoir Plate barcode | line | column
					  {
			        	 "header":Messages("containers.table.code"),
			        	 "property":"inputContainer.code",
			        	 "order":true,
						 "hide":true,
			        	 "type":"text",
			        	 "position":0,
			        	 "extraHeaders":{0: inputExtraHeaders}
			          },	
			          */		        
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
				   /*  { // sampleAliquoteCode 
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
					*/
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
			         
		            /* ne pas aficher les containercodes  sauf pour DEBUG
			         {
			        	 "header":"DEBUG code",
			        	 "property":"outputContainer.code",
			        	 "order":true,
						 "hide":true,
						 "edit":false,
			        	 "type":"text",
			        	 "position":99,
			        	 "extraHeaders":{0: outputExtraHeaders}
			         },*/
			         { // Volume avec valeur par defaut
			        	 "header":Messages("containers.table.volume") + " (µL)",
			        	 "property":"outputContainerUsed.volume.value",
			        	 "hide":true,
			        	 "edit":true,
			        	 "type":"number",
			        	 // "defaultValues":20, demande explicite de supprimer le defaut
			        	 "position":34,
			        	 "extraHeaders":{0: outputExtraHeaders}
			         },
			         { //  barcode plaque sortie == support Container used code... faut Used 
			        	 "header":Messages("containers.table.support.name"),
			        	 "property":"outputContainerUsed.locationOnContainerSupport.code", 
						 "hide":true,
			        	 "type":"text",
			        	 "position":35,
			        	 "extraHeaders":{0: outputExtraHeaders}
			         },  
			         { //  Ligne 
			        	 "header":Messages("containers.table.support.line"),
			        	 "property":"outputContainerUsed.locationOnContainerSupport.line", 
			        	 "order":true,
						 "hide":true,
			        	 "type":"text",
			        	 "position":36,
			        	 "extraHeaders":{0: outputExtraHeaders}
			         },     
			         { // colonne
			        	 "header":Messages("containers.table.support.column"),
			        	 // astuce GA: pour pouvoir trier les colonnes dans l'ordre naturel forcer a numerique.=> type:number,   property:  *1
			        	 "property":"outputContainerUsed.locationOnContainerSupport.column*1", 
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
				"active":true//,
				// FDS : ce tri donne 1,10,11,12,2.... comment avoir un tri 1,2....10,11,12,13 ??
				//"by":"inputContainer.support.column*1"
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
	        	"mode":"local",
	        	"callback":function(datatable){
	        		// NGL-2371 FDS 11/03/2019 copyContainerSupportCodeAndStorageCodeToDT deplacée dans atmService + ajout 2eme param "pos"
	        		// tous les instruments de cette exp n'ont que plaque en ouputContainer, inutile de le tester 
	        		// plaque inputContainer => pos='auto'
	        		atmService.copyContainerSupportCodeAndStorageCodeToDT(datatable,'auto');
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
			},
			"otherButtons": {
                active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
                complex:true,
                template:''
                	+'<div class="btn-group" style="margin-left:5px">'
                	+'<button class="btn btn-default" ng-click="copyVolumeInToExp()" data-toggle="tooltip" title="'+Messages("experiments.button.plate.copyVolumeTo")+' vol. eng. librairie'
                	+'" ng-disabled="!isEditMode()" ng-if="experiment.instrument.outContainerSupportCategoryCode!==\'tube\'"><i class="fa fa-files-o" aria-hidden="true"></i> Volume </button>'                	                	
                	+'</div>'
			}
	}; // fin struct datatableConfig
	
	
	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event save");
		$scope.atmService.data.save();
		$scope.atmService.viewToExperimentOneToOne($scope.experiment);
		$scope.copyConcentrationToOutputContainer();

		$scope.$emit('childSaved', callbackFunction);
	});

	// Ajouté suite au NGL-4203 : on veut copier la concentration dans les containers de sortie.
	$scope.copyConcentrationToOutputContainer = function() {	
		if ($scope.experiment.atomicTransfertMethods) {
			for (var i = 0; i < $scope.experiment.atomicTransfertMethods.length; i++) {
				var concentration;

				if ($scope.experiment.atomicTransfertMethods[i].inputContainerUseds[0]) {
					concentration = $scope.experiment.atomicTransfertMethods[i].inputContainerUseds[0].concentration;
				}
				
				if ($scope.experiment.atomicTransfertMethods[i].outputContainerUseds[0]) {
					$scope.experiment.atomicTransfertMethods[i].outputContainerUseds[0].concentration = concentration;
				}
			};
		}	
	};	
	
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
	
    // 24/11/2016 FDS copier le volume containerIn dans le volume engagé Librairie
	//     code adapté depuis copyVolumeInToOut de x-to-plates-ctrl.js
	$scope.copyVolumeInToExp = function(){
		console.log("copyVolumeInToExp");
		
		var data = $scope.atmService.data.displayResult;		
		data.forEach(function(value){
			
			if ( !value.data.inputContainerUsed.experimentProperties ){
				value.data.inputContainerUsed.experimentProperties = {};
			}
			value.data.inputContainerUsed.experimentProperties.inputVolumeLib=value.data.inputContainerUsed.volume;
		})		
	};
	
	// FDS 16/03/2018 : NGL-1906. rechercher le  ngsRunWorkLabel positionné au niveau processus pour le copier dans robotRunCode (sauf s'il y en plusieurs!!)
	$scope.$watch("experiment.instrument.code", function(newValue, OldValue){
		if ((newValue) && (newValue !== null ) && ( newValue !== OldValue ))  {		
			// exemple dans prepa-fc-ordered: var categoryCodes = $scope.$eval("getBasket().get()|getArray:'support.categoryCode'|unique",mainService);
			// mais ici mainService n'est pas defini, et pas necessaire...
			// obliger de passer par contents[0], mais normalement ne doit pas poser de probleme...
			var workLabels= $scope.$eval("getBasket().get()|getArray:'contents[0].processProperties.ngsRunWorkLabel.value'|unique");
			if ( workLabels.length > 1 ){
				$scope.messages.clear();
				$scope.messages.clazz = "alert alert-warning";
				$scope.messages.text = "Plusieurs noms de travail (robot) trouvés parmi les containers d'entrée (info processus)";
				$scope.messages.open();			
			
				//console.log('>1  run workLabel trouvé !!');
			} else if ( workLabels.length === 1 ){
				// verifier que TOUS les containers ont une valeur...
				var contents= $scope.$eval("getBasket().get()|getArray:'contents[0]'");
				var labels= $scope.$eval("getBasket().get()|getArray:'contents[0].processProperties.ngsRunWorkLabel.value'");
				if ( labels.length < contents.length ) {
					$scope.messages.clear();
					$scope.messages.clazz = "alert alert-warning";
					$scope.messages.text = "Certains containers en entrée n'ont pas de nom de travail run (robot) (info processus)";
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
		}
	});
	
	// FDS 20/03/2019 NGL-2484: seule la sortie en plaque est geree ==> forcer (pour le cas instrument=main)
	$scope.$watch("experiment.instrument.outContainerSupportCategoryCode", function(){
		$scope.experiment.instrument.outContainerSupportCategoryCode = "96-well-plate";
	});
		
	//Init
	
	var atmService = atmToSingleDatatable($scope, datatableConfig);
	
	//defined new atomictransfertMethod
	atmService.newAtomicTransfertMethod = function(l, c){
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
			volume : "µL"
	};
	
	// NGL-1350 aide a la saisie des index
	// !! les surcharges doivent etre faites avant experimentToView
	// filter tags.groupNames sur selectedTagGroup
	atmService.convertOutputPropertiesToDatatableColumn = function(property, pName){
		var column = atmService.$commonATM.convertTypePropertyToDatatableColumn(property,"outputContainerUsed."+pName+".",{"0":Messages("experiments.outputs")});
		if(property.code==="tag"){
			// amelioration: afficher le nom aux utilisateurs et pas le code  
			column.editTemplate='<input class="form-control" type="text" #ng-model typeahead="tag.code as tag.name for tag in getTags() | filter:{groupNames:selectedTagGroup.value} | filter:{name:$viewValue} | limitTo:20" typeahead-min-length="1" udt-change="updatePropertyFromUDT(value,col)"/>'; 
			// NGL-2246: utiliser bt-select au lieu input / GA: for tag in <variable> au lieu de for tag in <function>
			// Ne pas utiliser bt-select...pour l'instant 
			//column.editTemplate='<div class="form-control" bt-select  #ng-model filter="true" bt-options="tag.code as tag.name for tag in tags" udt-change="updatePropertyFromUDT(value,col)" /></div>';
		}
		return column;
	};
	
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	// FDS NGL-2484 les supports d'entree ne doivent etre QUE des plaques ( meme a la main...)
	if ( $scope.isCreationMode()){
		// !! en mode creation $scope.experiment.atomicTransfertMethod n'est pas encore chargé=> passer par Basket (ajouter mainService dans le controller)
		var categoryCodes = $scope.$eval("getBasket().get()|getArray:'support.categoryCode'|unique",mainService);
		var supports = $scope.$eval("getBasket().get()|getArray:'support.code'|unique",mainService);

		if ((categoryCodes.length === 1) && ( categoryCodes[0] ==="96-well-plate")){
			// plaques uniqt 
			// Y a-t-il un nbre max de plaques pour le Sciclone ??? si oui voir code plates-to-plate-ctrl.js
			
			// verifier qu'il n'y a pas plus de 96 inputs (il y a une seule plaque en sortie)
			if ( $scope.mainService.getBasket().length() > 96 ){ 
				$scope.messages.setError(Messages("experiments.input.error.maxContainers",96));
				$scope.atmService = null; //empeche la page de se charger...
			}else{
				$scope.messages.clear();
				$scope.atmService = atmService;
			}
		} else {
			$scope.messages.setError(Messages('experiments.input.error.only-plates')); 
			$scope.atmService = null; //empeche la page de se charger...
		}
	} else {
		$scope.atmService = atmService;
	}
	
	// Calculs 
	$scope.updatePropertyFromUDT = function(value, col){
		console.log("update from property : "+col.property);

		// si l'utilisateur défini le volume a engager => calculer la quantité
		if(col.property === 'inputContainerUsed.experimentProperties.inputVolumeLib.value'){
			computeQuantity(value.data);
		}
		
		// 05/08/2016 essai d'ajouter le calcul inverse...===> PB LES 2 MODIFICATIONS SE MARCHENT SUR LES PIEDS !!
		// si l'utilisateur défini la quantité a engager => calculer le volume
		//if(col.property === 'inputContainerUsed.experimentProperties.inputQuantityLib.value'){
		//	computeVolume(value.data);
		//}
		
		// 26/06/2018 ajout pour selection manuelle d'index; SUPSQCNG-619/NGL-2213 c'est ici l'endroit correct !!
		else if(col.property === 'outputContainerUsed.experimentProperties.tag.value'){
			tagService.computeTagCategory(value.data);
		}
	}

	// -1- inputQuantityLib=inputContainerUsed.concentration.value * inputContainerUsed.experimentProperties.inputVolumeLib.value
	var computeQuantity = function(udtData){
		var getter = $parse("inputContainerUsed.experimentProperties.inputQuantityLib.value");

		if($parse("inputContainerUsed.concentration.unit === 'nM'")(udtData)) {
			console.log("unit = nM");
		}
		
		var compute = {
				inputConcUnit: $parse("inputContainerUsed.concentration.unit")(udtData),
				inputConc : $parse("inputContainerUsed.concentration.value")(udtData),
				inputVolume : $parse("inputContainerUsed.experimentProperties.inputVolumeLib.value")(udtData),		
				isReady:function(){
					/// return (this.inputVolume && this.inputConc); bug!!!  le calcul ne se fait pas si inputConc=0 ( par exemple WATER)
					/// bloquer le calcul si l'unité n'est pas nM TODO...
					return (this.inputVolume && (this.inputConc != undefined));
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
			console.log("Missing values to calculate Quantity");
		}
	}
	
	
	var importData = function(){
		$scope.messages.clear();
		
		// 22/10/2018 pour permettre l'import de fichier workbook par 'main' utiliser  "extra-instrument"   pour forcer
		//  dans ExperimentService on a  pour "library-prep" : getInstrumentUsedTypes("sciclone-ngsx","hand"),
		//  en fait l' intrument utilise la classe Input de covarisandsciclone, mais on ne peut pas l'utiliser directement ici
		//    => simuler l'appel a sciclonengsx
		var queryString='';// si null ou undefined plante Chrome !!!
		
		if ( $scope.experiment.instrument.categoryCode === "hand"){
			queryString="?extraInstrument=sciclonengsx";
			console.log("'hand' remplacé par 'sciclonengsx'...");
		}

		$http.post(jsRoutes.controllers.instruments.io.IO.importFile($scope.experiment.code).url+queryString, $scope.file)
		.success(function(data, status, headers, config) {			
			$scope.messages.clazz="alert alert-success";
			$scope.messages.text=Messages('experiments.msg.import.success');
			$scope.messages.showDetails = false;
			$scope.messages.open();	
			//only atm because we cannot override directly experiment on scope.parent
			$scope.experiment.atomicTransfertMethods = data.atomicTransfertMethods;
			$scope.file = undefined;
			// reinit select File...
			angular.element('#importFile')[0].value = null;
			$scope.$emit('refresh');	
		})
		.error(function(data, status, headers, config) {		
			$scope.messages.clazz = "alert alert-danger";
			$scope.messages.text = Messages('experiments.msg.import.error');
			$scope.messages.setDetails(data);
			$scope.messages.open();	
			$scope.file = undefined;
			// reinit select File...
			angular.element('#importFile')[0].value = null;
		});		
	};
	
	$scope.outputContainerSupport = { code : null , storageCode : null};	
		
	if ( undefined !== $scope.experiment.atomicTransfertMethods[0]) { 
		 $scope.outputContainerSupport.code=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.code;
		//console.log("previous code: "+ $scope.outputContainerSupport.code);
	}
	if ( undefined !== $scope.experiment.atomicTransfertMethods[0]) {
		$scope.outputContainerSupport.storageCode=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.storageCode;
		//console.log("previous storageCode: "+ $scope.outputContainerSupport.storageCode);
	}
	
	// importer un fichier definissant quels index sont déposés dans quels containers
	// NGL-2012 :Ajouter les permissions pour admin; supprimer condition sur EditMode; NGL-2296 les import de fichiers ne marchent que a InProgress
	// NGL-3075: Il n'y a de fichier workbook a importer que si l'instrument est le sciclone-ngsx
	$scope.button = {
		isShow:function(){
			return ( ($scope.isInProgressState() || Permissions.check("admin")) && ($scope.experiment.instrument.typeCode == "sciclone-ngsx"));
			},
		isFileSet:function(){
			return ($scope.file === undefined)?"disabled":"";
		},
		click:importData
	};
	
	// Autre mode possible : utiliser une plaque d'index prédéfinis, l'utilisateur a juste a indiquer a partir de quelle colonne
	// de cette plaque le robot doit prelever les index

	$scope.columns = [ {name:'---', position: undefined },
	                   {name:'1', position:0}, {name:'2', position:8}, {name:'3', position:16}, {name:'4',  position:24}, {name:'5',  position:32}, {name:'6',  position:40},
	                   {name:'7', position:48},{name:'8', position:56},{name:'9', position:64}, {name:'10', position:72}, {name:'11', position:80}, {name:'12', position:88}
	                 ];
	$scope.tagPlateColumn = $scope.columns[0]; // defaut du select
	
	// mémorisation des choix pour le nettoyage éventuel....
	$scope.tagPlateToClean= undefined;
	$scope.tagPlateColumnToClean=undefined;
	
	$scope.plates=[];
	$scope.plates.push( {name: "---",                                           tagCategory: undefined,   tags: undefined });
	// NGL-4203
	// utiliser les fonctions utilisant populateIndex_PlateRange
	$scope.plates.push( {name:"IDT for Illumina Nextera DNA Unique Dual Indexes Set A", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_IDT_Nextera_Set_A() });
	$scope.plates.push( {name:"IDT for Illumina Nextera DNA Unique Dual Indexes Set B", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_IDT_Nextera_Set_B() });
	// plaques V2 !!
	$scope.plates.push( {name:"IDT for Illumina Nextera DNA Unique Dual Indexes Set C V2", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_IDT_Nextera_Set_CV2() });
	$scope.plates.push( {name:"IDT for Illumina Nextera DNA Unique Dual Indexes Set D V2", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_IDT_Nextera_Set_DV2() });
	
	$scope.tagPlate = $scope.plates[0]; // defaut du select
	
	// NGL-2012 :Ajouter les permissions pour admin; supprimer condition sur EditMode
	$scope.selectColOrPlate = {
		isShow:function(){
			return ( $scope.isInProgressState() || Permissions.check("admin") );
		},	
		select:function(){
			// NGL-2972 04/08/2020 nettoyage avec paramètres mémorisés
			if (($scope.tagPlate.name === "---") && ($scope.tagPlateColumn.name === "---")){
				return tagService.unsetTags ($scope.tagPlateToClean, $scope.tagPlateColumnToClean, atmService, $scope.messages)
			} else if ( ($scope.tagPlate.name !== "---") && ($scope.tagPlateColumn.name !== "---") ){
				// NGL-2944 02/06/2020 avant de mettre une nouvelle plaque nettoyer l'ancienne si existait !!!
				if ($scope.tagPlateToClean != undefined){
					tagService.unsetTags ($scope.tagPlateToClean, $scope.tagPlateColumnToClean, atmService, $scope.messages)
				}
				// il ne faudrait positionner ces 2 valeurs QUE si setTags s'est bien passée...!!!!!!
				$scope.tagPlateToClean=$scope.tagPlate;
				$scope.tagPlateColumnToClean= $scope.tagPlateColumn;
				return tagService.setTags($scope.tagPlate, $scope.tagPlateColumn, atmService, $scope.messages)
			} 
			// dans le dernier cas (1 seul des 2 selects est positionné=> ne rien faire)
		}
	};
	
	// 31/08/2018 NGL-1350 aide à la saisie des tags
	// meme si la fonctionnalité n'a pas été mise en place dans cette expérience, 
	// l'appel a tagService.initTags() est maintenant obligatoire pour l'assignation automatique de tagCategory
	if ( $scope.isNewState() || $scope.isInProgressState() || Permissions.check("admin") ){
		//02/02/2022 si pas de parametre passé a initTag => tous les types, sinon preciser !!! 2 valeurs existent: 'index-illumina-sequencing','index-nanopore-sequencing'
		tagService.initTags("index-illumina-sequencing");
		$scope.getTags= function(){ return tagService.getAllTags()};
		
		/* masquer fonctionnalité de choix d'index par groupes...pour l'instant 
		$scope.getTagGroups= function(){return tagService.getAllTagGroups()};
		$scope.selectedTagGroup= $scope.getTagGroups()[0]; // valeur defaut du select (qui maintenant existe car definie sans attendre le retour de la promise)
		*/
	}
	
	/* masquer fonctionnalité de choix d'index par groupes...pour l'instant 
	$scope.selectGroup = {
			isShow:function(){
				return (  $scope.isInProgressState()  || Permissions.check("admin") );
			}
	};
	*/
	
}]);