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
		return column;
	};

	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	$scope.atmService = atmService;
	
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
	
	// 12/04/2018 NGL-2012 ne rien mettre par defaut !!!
	$scope.plates=[];
	$scope.plates.push( {name: "---",                                           tagCategory: undefined,   tags: undefined });
	$scope.plates.push( {name:"DAP TruSeq DNA HT (96 Indexes)",                 tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_XapTruSeqHT() });
	$scope.plates.push( {name:"IDT-ILMN TruSeq DNA UD Indexes (96 Indexes)",    tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_IdtTruSeq96() });
	$scope.plates.push( {name:"IDT-ILMN TruSeq DNA UD Indexes (96 Indexes) V2", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_IdtTruSeq96V2() });// ajout NGL-4038
	$scope.plates.push( {name:"IDT-ILMN TruSeq DNA UD Indexes (24 Indexes x4)", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_IdtTruSeq24x4() });
	
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