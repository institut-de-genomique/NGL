// FDS 05/04/2018 - copiee depuis pcr-and-indexing-ctrl
// 22/06/2018 utilisation de la factory tagPlates dans le module tools (tag-plate-helpers.js)
// 02/02/2022 NGL-3710 ajout tagService qui remprend certaines fonctions de tagPlates
angular.module('home').controller('SmallRNASeqLibPrepCtrl',['$scope', '$parse',  '$filter', 'atmToSingleDatatable','$http','tagPlates','tagService',
                                                     function($scope, $parse, $filter, atmToSingleDatatable, $http, tagPlates, tagService){
	
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
					 { // Concentration
			        	 "header":Messages("containers.table.concentration") + " (ng/µL)",
			        	 "property":"inputContainerUsed.concentration.value",
			        	 "order":true,
						 "hide":true,
			        	 "type":"number",
			        	 "position":6,
			        	 "extraHeaders":{0: inputExtraHeaders}
			         }, 
			         { //Volume 
			        	 "header":Messages("containers.table.volume") + " (µL)", 
			        	 "property":"inputContainerUsed.volume.value",
			        	 "order":true,
						 "hide":true,
			        	 "type":"number",
			        	 "position":6,
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
			         
		            /* ne pas afficher les containercodes  sauf pour DEBUG
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
	        		//NGL-2371 FDS 11/03/2019 copyContainerSupportCodeAndStorageCodeToDT depacé dans atmService + ajout 2eme param "pos"
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
	
	//5/10/2018 seule la sortie en plaque est geree ==> forcer (pour le cas instrument=main)
	$scope.$watch("experiment.instrument.outContainerSupportCategoryCode", function(){
		$scope.experiment.instrument.outContainerSupportCategoryCode = "96-well-plate";
	});
	
	//Init
	
	var atmService = atmToSingleDatatable($scope, datatableConfig);
	
	// deplace apres experimentToView $scope.atmService = atmService;

	//defined new atomictransfertMethod
	// ne gere pas la sortie en tubes !!!
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
	// !! les surcharges doivent etre faites avant   $scope.atmService = atmService; !!!
	atmService.convertOutputPropertiesToDatatableColumn = function(property, pName){
		var column = atmService.$commonATM.convertTypePropertyToDatatableColumn(property,"outputContainerUsed."+pName+".",{"0":Messages("experiments.outputs")});
		if(property.code==="tag"){
			// amelioration: afficher le nom aux utilisateurs et pas le code 
			//column.editTemplate='<input class="form-control" type="text" #ng-model typeahead="tag.code as tag.name for tag in getTags() | filter:{groupNames:selectedTagGroup.value} | filter:{name:$viewValue} | limitTo:20" typeahead-min-length="1" udt-change="updatePropertyFromUDT(value,col)"/>'; 	
			// NGL-2246: utiliser bt-select au lieu input
			column.editTemplate='<div class="form-control" bt-select  #ng-model filter="true" bt-options="tag.code as tag.name for tag in lists.getTags()" udt-change="updatePropertyFromUDT(value,col)" /></div>';
//			column.editTemplate='<div class="form-control" bt-select  #ng-model filter="true" bt-options="tag.code as tag.name for tag in getTags()" udt-change="updatePropertyFromUDT(value,col)" /></div>';

		}
		return column;
	};
	
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	// 20/03/2019 TEST deplace ici
	$scope.atmService = atmService;
	
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
		
		// 22/10/2018 pour permettre l'import de fichier workbook par 'main' utiliser  "extra-instrument"   pour forcer
		//  dans ExperimentService on a  pour "small-rnaseq-lib-prep" :getInstrumentUsedTypes("sciclone-ngsx-and-zephyr","tecan-evo-150-and-zephyr","hand"), 
		//  en fait les 2 intruments utilisent tous les 2 la classe Input de covarisandsciclone, mais on ne peut pas l'utiliser directement ici
	    //     => simuler l'appel a sciclonengsxandzephyr
		var queryString='';// si null ou undefined plante Chrome !!!
		
		if ( $scope.experiment.instrument.categoryCode === "hand"){
			queryString="?extraInstrument=sciclonengsxandzephyr";
			console.log("'hand' remplacé par 'sciclonengsxandzephyr'...");
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
	
		$scope.outputContainerSupport.storageCode=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.storageCode;
		//console.log("previous storageCode: "+ $scope.outputContainerSupport.storageCode);
	}
	
	// importer un fichier definissant quels index sont déposés dans quels containers
	// NGL-2012 :Ajouter les permissions pour admin; supprimer condition sur EditMode; NGL-2296 les import de fichiers ne marchent que a InProgress
	$scope.button = {
		isShow:function(){
			return ( $scope.isInProgressState() || Permissions.check("admin") ) ;
			},
		isFileSet:function(){
			return ($scope.file === undefined)?"disabled":"";
		},
		click:importData
	};
	
	// Autre mode possible : utiliser une plaque d'index prédéfinis, l'utilisateur a juste a indiquer a partir de quelle colonne
	// de cette plaque le robot doit prelever les index
	$scope.columns = [ {name:"---", position: undefined },
	                   {name:"1", position:0}, {name:"2", position:8}, {name:"3", position:16}, {name:"4",  position:24}, {name:"5",  position:32}, {name:"6",  position:40},
	                   {name:"7", position:48},{name:"8", position:56},{name:"9", position:64}, {name:"10", position:72}, {name:"11", position:80}, {name:"12", position:88}
	                 ];
	
	$scope.tagPlateColumn = $scope.columns[0]; // defaut du select

	$scope.plates=[];
	$scope.plates.push( {name: "---",                         tagCategory: undefined,   tags: undefined });
	$scope.plates.push( {name:"NebNext small RNA plaque 48",  tagCategory:"SINGLE-INDEX", tags: tagPlates.populateIndex_NebNext48() });
	$scope.plates.push( {name:"QiaSeq miRNA NGS 48 index IL", tagCategory:"SINGLE-INDEX", tags: tagPlates.populateIndex_QiaSeq48IL() });
	$scope.plates.push( {name:"QiaSeq miRNA NGS 96 index IL", tagCategory:"SINGLE-INDEX", tags: tagPlates.populateIndex_QiaSeq96IL() });
	//NGL-3520 plaques RUDI
	$scope.plates.push( {name:"RUDI plate A", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_RUDI_plate_A() });
	$scope.plates.push( {name:"RUDI plate B", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_RUDI_plate_B() });
	$scope.plates.push( {name:"RUDI plate C", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_RUDI_plate_C() });
	$scope.plates.push( {name:"RUDI plate D", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_RUDI_plate_D() });
	// NGL-4198 ajouter 4 plaques RUDI
	$scope.plates.push( {name:"RUDI plate E", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_RUDI_plate_E() });
	$scope.plates.push( {name:"RUDI plate F", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_RUDI_plate_F() });
	$scope.plates.push( {name:"RUDI plate G", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_RUDI_plate_G() });
	$scope.plates.push( {name:"RUDI plate H", tagCategory:"DUAL-INDEX", tags: tagPlates.populateIndex_RUDI_plate_H() });
	
	$scope.tagPlate = $scope.plates[0]; // defaut du select
	
	// NGL-2944 11/05/2020 mémorisation des choix pour le nettoyage éventuel....
	$scope.tagPlateToClean= undefined;
	$scope.tagPlateColumnToClean=undefined;
	
	// NGL-2012 :Ajouter les permissions pour admin; supprimer condition sur EditMode;
	$scope.selectColOrPlate = {
		isShow:function(){
			return ( $scope.isNewState() || $scope.isInProgressState() || Permissions.check("admin") );
		},
		select:function(){
			// NGL-2944 11/05/2020 nettoyage avec paramètres mémorisés
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
	
	/* demandé ?????????????????????????
	// 31/08/2018 NGL-1350 aide a la saisie des tags
	// appeller initTags() isNewState() necessaire sinon ils ne sont pas initialisés au moment du chgt etat de l'experience a InProgress
	if ( $scope.isNewState() || $scope.isInProgressState() || Permissions.check("admin") ){
	   tagService.initTags('index-illumina-sequencing'); // ajout filtre
	   
		/// essai remise getTags locale ==> long ???
	   //$scope.getTags= function(){return tagService.getAllTags()};

	   $scope.getTagGroups= function(){return tagService.getAllTagGroups()};
	   $scope.selectedTagGroup= $scope.getTagGroups()[0]; // valeur defaut du select (qui maintenant existe car definie sans attendre le retour de la promise)
	}
	
	$scope.selectGroup = {
			isShow:function(){
				// NGL-2246 afficher dès l'etat Nouveau mais pour l'instant un bug existe: la liste de tous les tags n'est pas initialisée !!
				return ( $scope.isNewState() || $scope.isInProgressState() || Permissions.check("admin") );
			},
			// NGL-2246 ajout; GA recuperer ici l'objet groupName
	        select:function(groupName){       	
	        	console.log( 'groupe choisi :'+  groupName.value );
	        	
	        	//GA: creer une variable $scope.tags au lieu de d'ecraser la fonction getTags
	        	if (groupName.value === undefined ){ 
        			//$scope.tags = tagService.getAllTags(); //!! L'affichage de TOUS les index dans le bt-select qui est long...
        			$scope.lists.refresh.tags({typeCodes:'index-illumina-sequencing'});// supression refresh nanopre
        		} else { 
        			//$scope.tags = $filter('filter')(tagService.getAllTags(),{groupNames:groupName.value}, true);
        			$scope.lists.refresh.tags({typeCodes:'index-illumina-sequencing',groupNames:[groupName.value]});// supression refresh nanopre
        		}
	        }
	};
	
	// 26/06/2018 ajout pour selection manuelle d'index
	$scope.updatePropertyFromUDT = function(value, col){
		//console.log("update from property : "+col.property);
		if(col.property === 'outputContainerUsed.experimentProperties.tag.value'){
			tagService.computeTagCategory(value.data);
		}
	}
	
	??????????????????*/
	
}]);
