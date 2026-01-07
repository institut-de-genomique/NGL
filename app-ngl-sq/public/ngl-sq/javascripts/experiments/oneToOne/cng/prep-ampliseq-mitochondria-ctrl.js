// FDS 01/08/2024 copié depuis pcr-and-indexing
// ADAPT en cours....
angular.module('home').controller('PrepAmpliseqMitochondriaCtrl',['$scope', '$parse',  '$filter', 'atmToSingleDatatable','tagPlates','tagService',
                                                     function($scope, $parse, $filter, atmToSingleDatatable, tagPlates, tagService ){
	
	var inputExtraHeaders=Messages("experiments.inputs");
	var outputExtraHeaders=Messages("experiments.outputs");	
	
	var datatableConfig = {
			name: $scope.experiment.typeCode.toUpperCase(),

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
				//30/08/2017 plus de volume in donc plus besoin de bouton de copie de volume...
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
	
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	// 28/08/2017 OK countInputSupportCodes
	if ( $scope.countInputSupportCodes() > 1) {
		console.log(" > 1 support en entree");
		
		$scope.messages.clear();
		$scope.messages.clazz = "alert alert-danger";
		$scope.messages.text = Messages("experiments.input.error.only-1-plate");
		$scope.messages.showDetails = false;
		$scope.messages.open();
	} else {
		$scope.atmService = atmService;
	}
	
	$scope.outputContainerSupport = { code : null , storageCode : null};	
		
	if ( undefined !== $scope.experiment.atomicTransfertMethods[0]) { 
		 $scope.outputContainerSupport.code=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.code;
		//console.log("previous code: "+ $scope.outputContainerSupport.code);
	
		$scope.outputContainerSupport.storageCode=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.storageCode;
		//console.log("previous storageCode: "+ $scope.outputContainerSupport.storageCode);
	}

	// NGL-4900 2 LibraryTypeCodes, 2 protocoles et 2 sets d'index 
	// controler la cohérence des protocoles et des type librarycode ET construire la liste des plaques d'index
	// peut on changer le protocole d'une expérience terminée ????
	$scope.$watch("experiment.protocolCode", function(newValue) {
		// supprimer éventuel message précedent
		$scope.messages.clear();

		if ( $scope.isNewState() || $scope.isInProgressState() )  {	
			console.log("selected Protocole= "+newValue);
			
			// reinitialiser les plaques
			$scope.plates=[];
			$scope.plates.push( {name: "---",                                           tagCategory: undefined,   tags: undefined });// entrée minimale pour "pas de choix"
	
			// et aussi le choix plaque/column 
			$scope.tagPlateColumn = $scope.columns[1]; // defaut du select:  ATTENTION [1] ici pour travailler en plaque entière !!!
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
	

	// copié de prep-pcr-free   A VERIFIER !!!!!!!!!!!!!!!!!
	var checkLibProcessTypeCodeConsistancy= function(protocolCode){
		// map pour la liste des protocoles associés a chaque libProcessTypeCode !!! TENIR A JOUR !!!
		// !!!!!  le code des protocoles se fini en ...mitochondrie' et pas '..mitochondria'
		var libProtoMap = new Map();
		libProtoMap.set('AB', ['ampliseq-genome-mitochondrie']); 
		libProtoMap.set('AC', ['ampliseq-methylome-mitochondrie']);


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
			// !!!!!  le code des protocoles se fini en ...mitochondrie' et pas '..mitochondria'
			case 'ampliseq-genome-mitochondrie':
				$scope.plates.push( {name:"IDT mtDNA Set A Genome", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_IDT_mtDNAg_Set_A() });
				$scope.plates.push( {name:"IDT mtDNA Set B Genome", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_IDT_mtDNAg_Set_B() });
				$scope.plates.push( {name:"IDT mtDNA Set C Genome", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_IDT_mtDNAg_Set_C() });
				$scope.plates.push( {name:"IDT mtDNA Set D Genome", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_IDT_mtDNAg_Set_D() });
		
				break;
			case 'ampliseq-methylome-mitochondrie':
				$scope.plates.push( {name:"IDT mtDNA Set A Methylation", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_IDT_mtDNAm_Set_A() });
				$scope.plates.push( {name:"IDT mtDNA Set B Methylation", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_IDT_mtDNAm_Set_B() });
				$scope.plates.push( {name:"IDT mtDNA Set C Methylation", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_IDT_mtDNAm_Set_C() });
				$scope.plates.push( {name:"IDT mtDNA Set D Methylation", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_IDT_mtDNAm_Set_D() });
				break;
			default:
				/// ????
		}
	};

	//  utiliser une plaque d'index prédéfinis
	$scope.tagPlateToClean= undefined;
	$scope.tagPlateColumnToClean=undefined;
	
/*       ancien code...
	$scope.plates=[];
	$scope.plates.push( {name: "---",             tagCategory: undefined,   tags: undefined }); // entrée minimale pour "pas de choix"
	$scope.plates.push( {name:"IDT mtDNAg Set A", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_IDT_mtDNAg_Set_A() });
	$scope.plates.push( {name:"IDT mtDNAg Set B", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_IDT_mtDNAg_Set_B() });
	$scope.plates.push( {name:"IDT mtDNAg Set C", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_IDT_mtDNAg_Set_C() });
	$scope.plates.push( {name:"IDT mtDNAg Set D", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_IDT_mtDNAg_Set_D() });
*/

	$scope.columns = [ {name:'---', position:undefined },
					   {name:'1', position:0}, {name:'2', position:8}, {name:'3', position:16}, {name:'4',  position:24}, {name:'5',  position:32}, {name:'6',  position:40},
					   {name:'7', position:48},{name:'8', position:56},{name:'9', position:64}, {name:'10', position:72}, {name:'11', position:80}, {name:'12', position:88},
					 ];
		
	$scope.tagPlateColumn = $scope.columns[1]; // forcer en colonne 1; ici on travaille sur plaque entière sans choix possible pour l'instant
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
		}
	};

	// 31/08/2018 NGL-1350 aide a la saisie des tags
	// meme si la fonctionnalité n'a pas été mise en place dans cette experience, 
	// l'appel a tagService.initTags() est maintenant obligatoire pour l'assignation automatique de tagCategory
	if ( $scope.isNewState() || $scope.isInProgressState() || Permissions.check("admin") ){
	   tagService.initTags('index-illumina-sequencing'); // 03/02/2022 ajout filtre
	   $scope.getTags= function(){return tagService.getAllTags()};
	   
	}

	
}]);