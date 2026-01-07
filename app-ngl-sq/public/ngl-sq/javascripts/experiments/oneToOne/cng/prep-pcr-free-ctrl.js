// FDS 04/02/2016 -- JIRA NGL-894 : prep pcr free experiment
// 02/02/2022 NGL-3710 ajout tagService qui remprend certaines fonctions de tagPlates
angular.module('home').controller('PrepPcrFreeCtrl',['$scope', '$parse',  '$filter', 'atmToSingleDatatable','$http','tagPlates','tagService',
                                                     function($scope, $parse, $filter, atmToSingleDatatable, $http, tagPlates, tagService ){

	var inputExtraHeaders=Messages("experiments.inputs");
	var outputExtraHeaders=Messages("experiments.outputs");	
	
	// NGL-1055: name explicite pour fichier CSV exporté: typeCode experience
	var datatableConfig = {
			name: $scope.experiment.typeCode.toUpperCase(),
			//Guillaume le 04/03 => utiliser containerUsed seulement pour proprietes dynamiques...
			"columns":[
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
					 { // niveau process et content
					   // processProperties présente a new et en cours et plus a terminé, c'est normal, la propriété passe sur le container enfant !!!
						"header": Messages("containers.table.libProcessType"),
						"property" : "inputContainerUsed.contents",
						"filter" : "getArray:'processProperties.libProcessTypeCode.value' | unique | codes:'value'",
						"order":true,
						"edit":false,
						"hide":true,
						"type":"text",
						"position":13,
						"extraHeaders":{0:inputExtraHeaders}
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
			        	 "defaultValues":20,
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
	        	"mode":"local",
	        	"callback":function(datatable){
	        		// NGL-2371 FDS 08/03/2019 copyContainerSupportCodeAndStorageCodeToDT deplacée dans atmService + ajout 2eme param "pos"
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
                	+'<button class="btn btn-default" ng-click="copyVolumeInToExp()" data-toggle="tooltip" title="'+Messages("experiments.button.plate.copyVolumeTo")+' vol. eng. librairie ET vol. eng. fragmentation'
                	+'" ng-disabled="!isEditMode()" ng-if="experiment.instrument.outContainerSupportCategoryCode!==\'tube\'"><i class="fa fa-files-o" aria-hidden="true"></i> Volume </button>'                	                	
                	+'</div>'
			}
	}; // fin struct datatableConfig
	
	
	$scope.$on('save', function(e, callbackFunction) {
		console.log("call event save");
			$scope.atmService.data.save();
			$scope.atmService.viewToExperimentOneToOne($scope.experiment);
			$scope.$emit('childSaved', callbackFunction);
	});
	
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
	
    // 24/11/2016 FDS copier le volume containerIn dans le volume engagé Librairie ET volume Engagé Frag...
	//     code adapté depuis copyVolumeInToOut de x-to-plates-ctrl.js
	$scope.copyVolumeInToExp = function(){
		console.log("copyVolumeInToExp");
		
		var data = $scope.atmService.data.displayResult;		
		data.forEach(function(value){
			
			if ( !value.data.inputContainerUsed.experimentProperties ){
				value.data.inputContainerUsed.experimentProperties = {};
			}
			value.data.inputContainerUsed.experimentProperties.inputVolumeLib=value.data.inputContainerUsed.volume;
			value.data.inputContainerUsed.experimentProperties.inputVolumeFrag=value.data.inputContainerUsed.volume;	
		})		
	};
		
	// FDS 16/03/2018 : NGL-1906. rechercher le  ngsRunWorkLabel positionné au niveau processus pour le copier dans robotRunCode (sauf s'il y en plusieurs!!)
	$scope.$watch("experiment.instrument.code", function(newValue, OldValue){
		if ((newValue) && (newValue !== null ) && ( newValue !== OldValue ))  {		
			// passer par contents[0], mais normalement ne doit pas poser de probleme...(si le container est un pool homogène)
			var workLabels= $scope.$eval("getBasket().get()|getArray:'contents[0].processProperties.ngsRunWorkLabel.value'|unique");
			if ( workLabels.length > 1 ){
				$scope.messages.clear();

				$scope.messages.clazz = "alert alert-warning";
				$scope.messages.text = "Plusieurs noms de travail (robot) différents trouvés parmi les containers en entrée (info processus)";    
				$scope.messages.open();			
				
			} else if ( workLabels.length === 1 ){
				// verifier que TOUS les containers ont une valeur...
				var contents= $scope.$eval("getBasket().get()|getArray:'contents[0]'");
				var labels= $scope.$eval("getBasket().get()|getArray:'contents[0].processProperties.ngsRunWorkLabel.value'");
				if ( labels.length < contents.length ) {
					$scope.messages.clear();

					$scope.messages.clazz = "alert alert-warning";
					$scope.messages.text = "Certains containers en entrée n'ont pas de nom de travail run (robot) (info processus)";
					$scope.messages.open();			
				
					//console.log("Certains containers n'ont pas de workLabel !!");
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

	// NGL-4609 controler la cohérence des protocoles et des type librarycode ET construire la liste des plaques d'index
	// peut on changer le protocole d'une expérience terminée ???? OUI mais alors processProperties.libProcessTypeCode n'est plus disponible !!!
	$scope.$watch("experiment.protocolCode", function(newValue) {
		// supprimer éventuel message précedent
		$scope.messages.clear();

		// autoriser en mode admin ??? Non car on est a l'etat terminé 
		if ( $scope.isNewState() || $scope.isInProgressState() )  {	
			console.log("selected Protocole= "+newValue);
			
			// reinitialiser les plaques
			$scope.plates=[];
			$scope.plates.push( {name: "---",                                           tagCategory: undefined,   tags: undefined });// entrée minimale pour "pas de choix"
	
			// et aussi le choix plaque/column 
			$scope.tagPlateColumn = $scope.columns[0]; // defaut du select
			$scope.tagPlate = $scope.plates[0]; // defaut du select

			// nettoyer les tags // s'il y en a !!
			if ( ($scope.tagPlateToClean !== undefined ) &&  ($scope.tagPlateColumnToClean !== undefined ) ){
				tagService.unsetTags ($scope.tagPlateToClean, $scope.tagPlateColumnToClean, atmService, $scope.messages);
			}

			if ( newValue && (newValue !== undefined) ){ 
				var consistancy=checkLibProcessTypeCodeConsistancy(newValue);
				//  ne construire la liste que si c'est coherent
				if (consistancy) {
					buildPlateListFromProtocol(newValue);
				} 
			}
		}
	});

	// NGL-4609 Warnings non bloquant pour l'instant !
	var checkLibProcessTypeCodeConsistancy= function(protocolCode){
		// map pour la liste des protocoles associés a chaque libProcessTypeCode !!! TENIR A JOUR !!!
		var libProtoMap = new Map();
		//libProtoMap.set('DA', ['1a-sop-ill-pcrfree', '1a-sop-ill-pcrfree-dap-plate']);  NGL-4777 protocole '1a-sop-ill-pcrfree' désactivé
		libProtoMap.set('DA', ['1a-sop-ill-pcrfree-dap-plate']); 
		libProtoMap.set('DL', ['illumina-dna-pcrfree']);
		libProtoMap.set('DQ', ['nebnext-ultraii-dna-pcrfree']);
		libProtoMap.set('DT', ['sop-nebnext-ultraii-fs-dna-pcrfree']); // NGL-4767 ajout

		// !! en mode creation $scope.experiment.atomicTransfertMethod n'est pas encore chargé=> passer par Basket 
		//  contents[0] ne doit pas poser de probleme...(si le container est un pool homogène)
		if($scope.isCreationMode()){
			var libProcessTypeCodes=$scope.$eval("getBasket().get()|getArray:'contents[0].processProperties.libProcessTypeCode.value'|unique");
		} else {
			// en mode edition d'une expérience a l'etat 'Nouveau'
			var libProcessTypeCodes=$scope.$eval("atomicTransfertMethods|flatArray:'inputContainerUseds'|getArray:'contents[0].processProperties.libProcessTypeCode.value'|unique",$scope.experiment);			
		}

		if ( libProcessTypeCodes.length > 1 ){
			$scope.messages.clazz = "alert alert-warning";
			$scope.messages.text = "Plusieurs types processus banque différents trouvés parmi les containers en entrée.";    
			$scope.messages.open();	
			return false;	
		} else {
			var containersLibProcessTypeCode=libProcessTypeCodes[0];
			console.log(">>>>"+containersLibProcessTypeCode);
			var libCodes = Array.from( libProtoMap.keys() );
			if ( libCodes.includes( containersLibProcessTypeCode ) ){ 
				//console.log('type banque valide !!!');
				console.log("protocole ="+ protocolCode);
				var protos=libProtoMap.get( containersLibProcessTypeCode );
				if ( ! protos.includes(protocolCode)){
					$scope.messages.clazz = "alert alert-warning";
					$scope.messages.text = "Le protocole choisi est incohérent avec le type processus banque du(des) container(s) en entrée.";    
					$scope.messages.open();	
					return false;	
				}
			} else {
				$scope.messages.clazz = "alert alert-warning";
				$scope.messages.text = "Type processus banque '"+ libProcessTypeCodes[0] +"' non valide trouvé parmi les containers en entrée.";    
				$scope.messages.open();	
				return false;
			}
		}
		return true;
	}

	//initialiser les plaques
	$scope.plates=[];
	$scope.plates.push( {name: "---", tagCategory: undefined, tags: undefined }); // entrée minimale pour "pas de choix"
	
	// NGL-4609 liste des plaques proposées dépend du protocole choisi
	var buildPlateListFromProtocol= function(protocolCode){

		switch (protocolCode){
			//case '1a-sop-ill-pcrfree': NGL-4777 protocole '1a-sop-ill-pcrfree' désactivé
			case '1a-sop-ill-pcrfree-dap-plate':
				// NGL-4778 2 plaques obsolètes supprimées de la liste
				//$scope.plates.push( {name:"DAP TruSeq DNA HT (96 Indexes)",                 tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_XapTruSeqHT() });
				//$scope.plates.push( {name:"IDT-ILMN TruSeq DNA UD Indexes (96 Indexes)",    tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_IdtTruSeq96() });
				$scope.plates.push( {name:"IDT-ILMN TruSeq DNA UD Indexes (96 Indexes) V2", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_IdtTruSeq96V2() });
				$scope.plates.push( {name:"IDT-ILMN TruSeq DNA UD Indexes (24 Indexes x4)", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_IdtTruSeq24x4() });
				break;
			case 'nebnext-ultraii-dna-pcrfree':
			case 'sop-nebnext-ultraii-fs-dna-pcrfree': //NGL-4767 ajout case
				$scope.plates.push( {name:"NEBNext UDI UMI Adaptors DNA (set1)", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_NEBNext_UDI_set1() });
				$scope.plates.push( {name:"NEBNext UDI UMI Adaptors DNA (set2)", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_NEBNext_UDI_set2() });
				$scope.plates.push( {name:"NEBNext UDI UMI Adaptors DNA (set3)", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_NEBNext_UDI_set3() });
				$scope.plates.push( {name:"NEBNext UDI UMI Adaptors DNA (set4)", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_NEBNext_UDI_set4() });
				break;
			case 'illumina-dna-pcrfree':
				/*cosmetique : modification du label ...v3 (Set A) --> ...Set A (V3) */
				$scope.plates.push( {name:"Illumina Unique Dual Indexes Set A (V3)", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_Illumina_UDI_Set_A_V3() });
				$scope.plates.push( {name:"Illumina Unique Dual Indexes Set B (V3)", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_Illumina_UDI_Set_B_V3() });
				$scope.plates.push( {name:"Illumina Unique Dual Indexes Set C (V3)", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_Illumina_UDI_Set_C_V3() });
				$scope.plates.push( {name:"Illumina Unique Dual Indexes Set D (V3)", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_Illumina_UDI_Set_D_V3() });
				break;
			default:
				/// ????
		}
	};
	
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
	}
	
	// NGL-1350 aide a la saisie des index
	// !! les surcharges doivent etre faites avant experimentToView
	atmService.convertOutputPropertiesToDatatableColumn = function(property, pName){
		var column = atmService.$commonATM.convertTypePropertyToDatatableColumn(property,"outputContainerUsed."+pName+".",{"0":Messages("experiments.outputs")});
		if(property.code=="tag"){
			// Ne pas utiliser bt-select...pour l'instant ref NGL-2246
			//column.editTemplate='<div class="form-control" bt-select  #ng-model filter="true" bt-options="tag.code as tag.name for tag in lists.getTags()" udt-change="updatePropertyFromUDT(value,col)" /></div>';
			column.editTemplate='<input class="form-control" type="text" #ng-model typeahead="tag.code as tag.name for tag in getTags() | filter:{groupNames:selectedTagGroup.value} | filter:{name:$viewValue} | limitTo:20" typeahead-min-length="1" udt-change="updatePropertyFromUDT(value,col)"/>';		}
			
			// FDS 16/05/2024 NON ca ne marche pas ici: on n'obtient pas la valeur voulue !!!
			//column.thClass="columnWidth100px";
		return column;
	};

	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	$scope.atmService = atmService;

	// FDS  16/05/2024 forcer largeur de la colonne "Tag" 
	var dtConfig = $scope.atmService.data.getConfig();

	var indiceColonne = 13;
	if (dtConfig.columns && dtConfig.columns[indiceColonne]) {
		dtConfig.columns[indiceColonne].thClass="columnWidth70px"; /// numero de colonne hardcodé !! CA MARCHE !!
	}
	
	var importData = function(){
		$scope.messages.clear();

		$http.post(jsRoutes.controllers.instruments.io.IO.importFile($scope.experiment.code).url, $scope.file)
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
	
	// importer un fichier definissant quels index sont déposés dans quels containers;  
	// NGL-2012: Ajouter les permision pour admin; supprimer condition sur EditMode
	// NGL-3075: Il n'y a de fichier workbook a importer que si l'instrument mixte contient le sciclone-ngsx
	$scope.button = {
		isShow:function(){
			//return ( ($scope.isInProgressState() || Permissions.check("admin")) && ($scope.experiment.instrument.typeCode.match(/sciclone-ngsx/)) );// marche pas dans ce sesns la !!!!
			return ( ($scope.experiment.instrument.typeCode.match(/sciclone-ngsx/)) && ($scope.isInProgressState() || Permissions.check("admin")) );
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
	$scope.tagPlate = $scope.plates[0]; // defaut du select
	
	// NGL-2972 04/08/2020 mémorisation des choix pour le nettoyage éventuel....
	$scope.tagPlateToClean= undefined;
	$scope.tagPlateColumnToClean=undefined;
	
	// NGL-2012 Ajouter les permissions pour admin; supprimer condition sur EditMode
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
	
	// 31/08/2018 NGL-1350 aide a la saisie des tags
	// meme si la fonctionnalité n'a pas été mise en place dans cette experience, 
	// l'appel a tagService.initTags() est maintenant obligatoire pour l'assignation automatique de tagCategory
	if ( $scope.isNewState() || $scope.isInProgressState() || Permissions.check("admin") ){ 	
	   tagService.initTags('index-illumina-sequencing'); // 10/02/2022 ajout filtre
	   $scope.getTags= function(){return tagService.getAllTags()};
	   
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
	
	// 24/11/2016  ajouté comme dans prep-wg-nano pour remplacer les valeurs par defaut dans la definition de l'experience
	// calculer les qtés inputQuantityFrag et inputQuantityLib a partir de inputVolumeFrag et inputVolumeLib
	// updatePropertyFromUDT  est automatiqut defini pour les colonnes injectees dans le datatable....
	$scope.updatePropertyFromUDT = function(value, col){
		//console.log("update from property : "+col.property);
		if(col.property === 'inputContainerUsed.experimentProperties.inputVolumeFrag.value'){
			// verifier si le volume saisi est > au volume IN:  si oui ecraser le volume saisi par volume IN
			// TODO...?? plus tard
			computeQuantityFrag(value.data);
		} else if (col.property === 'inputContainerUsed.experimentProperties.inputVolumeLib.value'){
			// verifier si le volume saisi est > au volume IN:  si oui ecraser le volume saisi par volume IN
			// TODO...?? plus tard
			computeQuantityLib(value.data);
		} else if (col.property === 'outputContainerUsed.experimentProperties.tag.value'){
			// 26/06/2018 utilisation de tagService.computeTagCategory
			tagService.computeTagCategory(value.data);
		}
	}

	//inputQuantity=inputContainerUsed.concentration.value * inputContainerUsed.experimentProperties.inputVolume.value
	var computeQuantityFrag = function(udtData){
		var getter = $parse("inputContainerUsed.experimentProperties.inputQuantityFrag.value");
		var getter2 = $parse("inputContainerUsed.experimentProperties.inputVolumeLib.value");

		var compute = {
				inputConc : $parse("inputContainerUsed.concentration.value")(udtData),
				inputVolume : $parse("inputContainerUsed.experimentProperties.inputVolumeFrag.value")(udtData),		
				isReady:function(){
					// traiter le cas ou il y a 1 des 2 valeurs (en general c'est la conc) est a 0
					return (this.inputConc >= 0  && this.inputVolume >= 0);
				}
			};
		
		if(compute.isReady()){
			var result = $parse("inputConc * inputVolume")(compute);
			console.log("frag result = "+result);
			
			if(angular.isNumber(result) && !isNaN(result)){
				inputQuantity = Math.round(result*10)/10;				
			}else{
				inputQuantity = 0;
			}	
			getter.assign(udtData, inputQuantity);
			
			//copie inputVolumeLib--> inputVolumeFrag
			getter2.assign(udtData, $parse("inputVolume")(compute));
			
		}else{
			console.log("Missing values to exec computeQuantityFrag");
		}
	}
	
	/// TODO: faire une seule fonction mais avec un parametre Lib ou Frag
	var computeQuantityLib = function(udtData){
		var getter = $parse("inputContainerUsed.experimentProperties.inputQuantityLib.value");

		var compute = {
				inputConc : $parse("inputContainerUsed.concentration.value")(udtData),
				inputVolume : $parse("inputContainerUsed.experimentProperties.inputVolumeLib.value")(udtData),		
				isReady:function(){
					// traiter le cas ou il y a 1 des 2 valeurs (en general c'est la conc) est a 0
					return (this.inputConc >= 0 && this.inputVolume >= 0 );
				}
			};
		
		if(compute.isReady()){
			var result = $parse("inputConc * inputVolume")(compute);
			console.log("quant result = "+result);
			
			if(angular.isNumber(result) && !isNaN(result)){
				inputQuantity = Math.round(result*10)/10;				
			}else{
				inputQuantity = 0;
			}	
			getter.assign(udtData, inputQuantity);
			
		}else{
			console.log("Missing values to exec computeQuantityLib");
		}
	}	
    
}]);