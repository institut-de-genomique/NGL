// FDS 01/08/2017 - copiee depuis library-prep-ctrl
// 02/02/2022 NGL-3710 ajout tagService qui remprend certaines fonctions de tagPlates
angular.module('home').controller('PcrAndIndexingCtrl',['$scope', '$parse',  '$filter', 'atmToSingleDatatable','$http','tagPlates','tagService',
                                                     function($scope, $parse, $filter, atmToSingleDatatable, $http, tagPlates, tagService ){
	
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
				     /*
				     { // sampleAliquoteCode 
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
					*/
				     { // 31/08/2017 niveau process ET contents =>utiliser properties et pas processProperties;
				       "header": Messages("containers.table.libProcessType"),
				       "property" : "inputContainerUsed.contents",
				       "filter" : "getArray:'properties.libProcessTypeCode.value' |unique | codes:'value'",
				       "order":true,
					   "edit":false,
					   "hide":true,
				       "type":"text",
				       "position":8.2,
				       "extraHeaders":{0:inputExtraHeaders}
				     },
				     { // 31/08/2017 baits rellement utilisees (mises dans l'experience precedente capture) => outputContainerUsed.contents
				       "header": Messages("containers.table.baits"),
				      	"property" : "outputContainerUsed.contents",
				      	"filter" : "getArray:'properties.baits.value' | unique | codes:'value'",
				      	"order":true,
					    "edit":false,
					    "hide":true,
				      	"type":"text",
				      	"position":8.4,
				      	"extraHeaders":{0:inputExtraHeaders}
				     },
				     { // 31/08/2017 niveau process ET contents =>utiliser properties et pas processProperties
				        "header":  Messages("containers.table.captureProtocol"),
				      	"property" : "inputContainerUsed.contents",
				      	"filter" : "getArray:'properties.captureProtocol.value' | unique | codes:'value'",
				      	"order":true,
						"edit":false,
					    "hide":true,
				      	"type":"text",
				      	"position":8.6,
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
			         
			         //TEST NGL-3387: remplacer la propriété finalVolume par un attribut Volume avec les controles obligatoires
			         {
						"header":Messages("containers.table.volume") + " (µL)",
						"property":"outputContainerUsed.volume.value",
						"order":true,
						"edit":true,
						"required":"isRequired('IP')", // doit être complété par validation par drools
						"defaultValues":"30",
						"hide":true,
						"type":"number",
						"position":32,
						"extraHeaders":{0:outputExtraHeaders}
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
	
	// NGL-2972 FDS 04/08/2020 mémorisation des choix pour le nettoyage éventuel...
	//   deplacer ici pour le watch OK !!!!
	$scope.tagPlateToClean= undefined;
	$scope.tagPlateColumnToClean=undefined;
	
	// NGL-3869 FDS 05/08/2022 (préselection des plaques d'index d'après le protocole')
	$scope.$watch("experiment.protocolCode", function(){
		buildPlateListFromProtocol($scope);
		// nettoyer puisqu'on change de plaque !!!!'
		tagService.unsetTags ($scope.tagPlateToClean, $scope.tagPlateColumnToClean, atmService, $scope.messages)
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
	// Ne pas utiliser bt-select...pour l'instant 
	atmService.convertOutputPropertiesToDatatableColumn = function(property, pName){
		var column = atmService.$commonATM.convertTypePropertyToDatatableColumn(property,"outputContainerUsed."+pName+".",{"0":Messages("experiments.outputs")});
		if(property.code==="tag"){
			// malgre code identique a nanopore-library-ctrl.js  affiche le code et pas le nom !!!!!!!!!
			column.editTemplate='<input class="form-control" type="text" #ng-model typeahead="tag.code as tag.name for tag in getTags() | filter:{groupNames:selectedTagGroup.value} | filter:{name:$viewValue} | limitTo:20" typeahead-min-length="1" udt-change="updatePropertyFromUDT(value,col)"/>'; 
			}
		return column;
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
	
		$scope.outputContainerSupport.storageCode=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.storageCode;
		//console.log("previous storageCode: "+ $scope.outputContainerSupport.storageCode);
	}
	
	// importer un fichier definissant quels index sont déposés dans quels containers
	// NGL-2012 :Ajouter les permissions pour admin; supprimer condition sur EditMode
	$scope.button = {
		isShow:function(){
			return ( $scope.isInProgressState() || Permissions.check("admin") )
			},
		isFileSet:function(){
			return ($scope.file === undefined)?"disabled":"";
		},
		click:importData
	};
	
	// Autre mode possible: utiliser une plaque d'index prédéfinis, l'utilisateur doit juste indiquer à partir de quelle colonne
	// de cette plaque le robot doit prélever les index
	$scope.columns = [ {name:'---', position:undefined },
	                   {name:'1', position:0}, {name:'2', position:8}, {name:'3', position:16}, {name:'4',  position:24}, {name:'5',  position:32}, {name:'6',  position:40},
	                   {name:'7', position:48},{name:'8', position:56},{name:'9', position:64}, {name:'10', position:72}, {name:'11', position:80}, {name:'12', position:88},
	                 ];
	
	$scope.tagPlateColumn = $scope.columns[0]; // defaut du select de colonne
	
	// NGL-3869 FDS 05/08/2022 (préselection des plaques d'index d'après le protocole')
	var buildPlateListFromProtocol = function($scope){
		$scope.plates=[];
		$scope.plates.push({name: "---", tagCategory: undefined, tags: undefined }); // entrée minimale pour "pas de choix"
		
		switch ($scope.experiment.protocolCode){
			// 04/08/2022 NGL-3960 nvelles plaques Anchor
			case 'illumina-anchor-strand-totrna-with-ribozeroplus':
			case 'illumina-anchor-strand-mrna':
				$scope.plates.push( {name:"IDT for Illumina Anchor DNA Unique Dual Indexes Set A", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_IDT_Anchor_Set_A() });
				$scope.plates.push( {name:"IDT for Illumina Anchor DNA Unique Dual Indexes Set B", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_IDT_Anchor_Set_B() });
				$scope.plates.push( {name:"IDT for Illumina Anchor DNA Unique Dual Indexes Set C", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_IDT_Anchor_Set_C() });
				$scope.plates.push( {name:"IDT for Illumina Anchor DNA Unique Dual Indexes Set D", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_IDT_Anchor_Set_D() });
				break;
			// 28/10/ 2022 NGL-3869 plaque Capture XT-HS2
			case 'capture-wes-xt-hs2-bravows':
				$scope.plates.push( {name:"Agilent SureSelect XT-HS2 Kit A [pl orange]", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_AglSureSelect_XTHS2_Kit_A() });
				$scope.plates.push( {name:"Agilent SureSelect XT-HS2 Kit B [pl bleue]",  tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_AglSureSelect_XTHS2_Kit_B() });
				$scope.plates.push( {name:"Agilent SureSelect XT-HS2 Kit C [pl verte]",  tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_AglSureSelect_XTHS2_Kit_C() });
				$scope.plates.push( {name:"Agilent SureSelect XT-HS2 Kit D [pl rouge]",  tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_AglSureSelect_XTHS2_Kit_D() });
			break;
			default:
				$scope.plates.push( {name:"Agilent SureSelect [bleue]",  tagCategory:"SINGLE-INDEX", tags: tagPlates.populateIndex_AglSur96() });
		}
		$scope.tagPlate = $scope.plates[0]; // defaut du select=pas de choix; ici a cause du watch
	};
	
	//appel initial  mais l'utilisateur peut éditer le protocole...il faut un watch! (voir plus haut)
	buildPlateListFromProtocol($scope);
	
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
	   
	   /* masquer fonctionnalité de choix d'index par groupes...pour l'instant 
	   $scope.getTagGroups= function(){return tagService.getAllTagGroups()};
	   $scope.selectedTagGroup= $scope.getTagGroups()[0]; // valeur defaut du select (qui maintenant existe car definie sans attendre le retour de la promise)
	   */
	}
   
	/* masquer fonctionnalité de choix d'index par groupes...pour l'instant 
	$scope.selectGroup = {
			isShow:function(){
				//return ( ( $scope.isInProgressState() &&  $scope.isEditMode() ) || Permissions.check("admin") );
				return (  $scope.isInProgressState()  || Permissions.check("admin") );
			}
	};
	*/
	
	// 26/06/2018 ajout pour selection manuelle d'index
	$scope.updatePropertyFromUDT = function(value, col){
		//console.log("update from property : "+col.property);
		if(col.property === 'outputContainerUsed.experimentProperties.tag.value'){
			tagService.computeTagCategory(value.data);
		}
	};
	
}]);