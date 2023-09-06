/* FDS 02/03/2017 -- JIRA NGL-1167 : processus Chromium
   2 fonctionnements  -main     : strip-8 => tubes ou strip ( Julie demande de bloquer 96-well-plate pour l'instant...)
                      //NON ....- sciclone : 96-well-plate => 96-well-plate
*/
// 02/02/2022 NGL-3710 ajout tagService qui reprend certaines fonctions de tagPlates (suppression tagPlates car inutile ici)
angular.module('home').controller('WgChromiumLibraryPrepCtrl',['$scope', '$parse', '$filter', 'atmToSingleDatatable','$http','tagService',
                                                     function( $scope,   $parse,    $filter,   atmToSingleDatatable,  $http,  tagService){

	var inputExtraHeaders=Messages("experiments.inputs");
	var outputExtraHeaders=Messages("experiments.outputs");
	
	var datatableConfig = {
			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[
			        //--------------------- INPUT containers section -----------------------
			        { // barcode support entree
			        	 "header":Messages("containers.table.support.name"),
			        	 "property":"inputContainer.support.code",
						 "hide":true,
			        	 "type":"text",
			        	 "position":1,
			        	 "extraHeaders":{0: inputExtraHeaders}
			        },
					{   // colonne
						"header":Messages("containers.table.support.column"),
						// astuce GA: pour pouvoir trier les colonnes dans l'ordre naturel forcer a numerique.=> type:number,   property:  *1
						"property":"inputContainer.support.column*1",
						"order":true,
						"hide":true,
						"type":"number",
						"position":3,
						"extraHeaders":{0: inputExtraHeaders}
					},
					{ // ajout NGL-3941
			        	 "header":Messages("containers.table.concentration") + " (ng/µL)",
			        	 "property":"inputContainerUsed.concentration.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":5,
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
			        	 "position":500,
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
	        	"mode":"local"
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
			/*,
			"otherButtons": {
                active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
                complex:true,
                template:''
                	+'<div class="btn-group" style="margin-left:5px">'
                	+'<button class="btn btn-default" ng-click="copyVolumeInToExp()" data-toggle="tooltip" title="'+Messages("experiments.button.plate.copyVolumeTo")+' vol. eng. librairie'
                	+'" ng-disabled="!isEditMode()" ng-if="experiment.instrument.outContainerSupportCategoryCode!==\'tube\'"><i class="fa fa-files-o" aria-hidden="true"></i> Volume </button>'                	                	
                	+'</div>'
             
			}*/
	}; // fin struct datatableConfig
	
	// probleme de rafraichissement de la vue en cas de mauvais choix inital de l'utilisateur
	// dans le watch  "$scope.experiment.instrument.categoryCode"   => forcer la vue ici...
	if ( $scope.experiment.instrument.categoryCode === "hand") {
		if($scope.experiment.instrument.outContainerSupportCategoryCode === "tube") {
			// tubes 
			datatableConfig.columns.push({
				"header" : Messages("containers.table.code"),
				"property" : "outputContainerUsed.locationOnContainerSupport.code",
				"order" : true,
				"edit" : true,
				"hide" : true,
				"type" : "text",
				"position" : 400,
				"extraHeaders" : {0: outputExtraHeaders}
			});
			datatableConfig.columns.push({
				//storage pour tubes
				"header" : Messages("containers.table.storageCode"),
				"property" : "outputContainerUsed.locationOnContainerSupport.storageCode",
				"order" : true,
				"edit" : true,
				"hide" : true,
				"type" : "text",
				"position" : 401,
				"extraHeaders" : {0: outputExtraHeaders}
			});
		} else {
		//strip-8
			datatableConfig.columns.push({
				// barcode support sortie == support Container used code
				"header" : Messages("containers.table.support.name"),
				"property" : "outputContainerUsed.locationOnContainerSupport.code",
				"order" : true,
				"edit" : true,  // Est-ce normal de pouvoir editer le code du strip ??? permet de creer plusieurs strips en sortie !!
				"hide" : true,
				"type" : "text",
				"position" : 400,
				"extraHeaders" : {0: outputExtraHeaders}
			});
			datatableConfig.columns.push({
				// colonne
				"header" : Messages("containers.table.support.column"),
				"property" : "outputContainerUsed.locationOnContainerSupport.column",
				"edit" : false, // la position sur un strip n'est pas editable !!! NGL-2491
				"order" : true,
				"hide" : true,
				"type" : "text",// pas number ( lignes et colonnes sont en text)
				"position" : 401,
				"extraHeaders" : {0: outputExtraHeaders}
			});
		}
	} 
	/* l'autre cas est le sciclone qui n'a que des plaques-96 en sortie    ==> pas pour l'instant 
	else {
		datatableConfig.columns.push({
			// barcode plaque sortie == support Container used code
			"header" : Messages("containers.table.support.name"),
			"property" : "outputContainerUsed.locationOnContainerSupport.code",
			"hide" : true,
			"type" : "text",
			"position" : 400,
			"extraHeaders" : {0: outputExtraHeaders}
		});
		
		datatableConfig.columns.push({
			// Ligne
			"header" : Messages("containers.table.support.line"),
			"property" : "outputContainerUsed.locationOnContainerSupport.line",
			"edit" : true,
			"order" : true,
			"hide" : true,
			"type" : "text",
			"position" : 401,
			"extraHeaders" : {0: outputExtraHeaders}
		});
		
		datatableConfig.columns.push({
			// colonne
			"header" : Messages("containers.table.support.column"),
			// astuce GA: pour pouvoir trier les colonnes dans l'ordre naturel
			// forcer a numerique.=> type:number, property: *1
			"property" : "outputContainerUsed.locationOnContainerSupport.column*1",
			"edit" : true,
			"order" : true,
			"hide" : true,
			"type" : "number",
			"position" : 402,
			"extraHeaders" : {0: outputExtraHeaders}
		});
	} 
	*/
	
	// ajout 16/12 pour remplacer copyContainerSupportCodeAndStorageCodeToDT  ???????
	//pour gestion des plaques/strips en sortie
	var updateATM = function(experiment){
		if(experiment.instrument.outContainerSupportCategoryCode !== "tube"){
			experiment.atomicTransfertMethods.forEach(function(atm){
				atm.line = atm.outputContainerUseds[0].locationOnContainerSupport.line;
				atm.column = atm.outputContainerUseds[0].locationOnContainerSupport.column;
			});
		}		
	};
	
	// correction NGL-2371: attente correction traitement correct strip ? NGL-2491
	$scope.$on('save', function(e, callbackFunction) {
		console.log("call event save");
		$scope.atmService.data.save();
		$scope.atmService.viewToExperimentOneToOne($scope.experiment);
		// ajout 16/12 pour gestion des strip ou plaques puisque suppression du callback dans save...
		updateATM($scope.experiment);
		
		$scope.$emit('childSaved', callbackFunction);
	});
	
	// correction NGL-2371: attente correction traitement correct strip ? NGL-2491
	$scope.$on('refresh', function(e) {
		console.log("call event refresh");
		var dtConfig = $scope.atmService.data.getConfig();
		dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));
		
		/// NECESSAIRE ??
		// dtConfig.edit.showButton = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));
		
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

	//Init
	
	var atmService = atmToSingleDatatable($scope, datatableConfig);
	
	//defined new atomictransfertMethod
	//NGL-4233 remplacer line:c, column:c par expressions ternaires 
	//  /!\  ?undefined:"1",   ne marche pas ici alors qu'on le trouve dans plusieurs controleurs !!!
	atmService.newAtomicTransfertMethod = function(l, c){
		return {
			class:"OneToOne",
			line:($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube")?l:"1",
			column:($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube")?c:"1",
			inputContainerUseds:new Array(0),
			outputContainerUseds:new Array(0)
		};
	};
	
	//defined default output unit
	atmService.defaultOutputUnit = {
			volume : "µL"
	};
	
	// aide a la saisie des index/tag
	// !! les surcharges doivent etre faites avant experimentToView 
	atmService.convertOutputPropertiesToDatatableColumn = function(property, pName){
		var column = atmService.$commonATM.convertTypePropertyToDatatableColumn(property,"outputContainerUsed."+pName+".",{"0":Messages("experiments.outputs")});
		if(property.code=="tag"){
			// avec drop down
			column.editTemplate='<div class="form-control" bt-select  #ng-model filter="true" bt-options="tag.code as tag.name for tag in lists.getTags()" udt-change="updatePropertyFromUDT(value,col)" /></div>';
		}
		return column;
	};
	
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	// le sciclone ne traite que des plaques (le type de container de sortie est deja restreint)
	if ( ( $scope.experiment.instrument.categoryCode !== 'hand') && 
		 ($scope.experiment.instrument.inContainerSupportCategoryCode !== $scope.experiment.instrument.outContainerSupportCategoryCode) ) {
		     $scope.messages.setError(Messages('experiments.input.error.must-be-same-out'));
	} else {
		$scope.messages.clear();
		$scope.atmService = atmService;
	}
	
	/* pour support en output a taper en tete de datatable.. pas demandé ici...
	$scope.outputContainerSupport = { code : null , storageCode : null};	
		
	if ( undefined !== $scope.experiment.atomicTransfertMethods[0]) { 
		 $scope.outputContainerSupport.code=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.code;
		//console.log("previous code: "+ $scope.outputContainerSupport.code);
	}
	if ( undefined !== $scope.experiment.atomicTransfertMethods[0]) {
		$scope.outputContainerSupport.storageCode=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.storageCode;
		//console.log("previous storageCode: "+ $scope.outputContainerSupport.storageCode);
	}
	*/
	
	// FORCER A STRIP si instrument= main et sortie=plaque
	$scope.$watch("$scope.experiment.instrument.categoryCode", function(){
			if (($scope.experiment.instrument.categoryCode === "hand") && 
				($scope.experiment.instrument.outContainerSupportCategoryCode === "96-well-plate")) {
				$scope.experiment.instrument.outContainerSupportCategoryCode = "strip-8";
			}
	});	
	
	// NGL-3142 15/12/2020 selection de groupe d'index
	// aide a la saisie des index/tag 
	// l'appel a tagService.initTags() est maintenant obligatoire pour l'assignation automatique de tagCategory
	if ( $scope.isNewState() || $scope.isInProgressState() || Permissions.check("admin") ){
		// 14/01/2021 essai avec un tableau pour filter types
		tagService.initTags(['index-illumina-sequencing'],['DUAL-INDEX','POOL-INDEX']);
	   
	   $scope.getTagGroups= function(){return tagService.getAllTagGroups()};
	   $scope.selectedTagGroup= $scope.getTagGroups()[0]; // valeur defaut du select
	}
	
	$scope.selectGroup = {
		isShow:function(){
			return ( $scope.isNewState() || $scope.isInProgressState() || Permissions.check("admin") );
		},
		// recuperer ici l'objet groupName
	    select:function(groupName){
	        console.log( 'groupe choisi :'+  groupName.value );
	         // 08/01/2021 ajout passage paramètre categoryCodes (voir  ... common.js  ?????) OK 
	        if (groupName.value === undefined ){
	        	//$scope.lists.refresh.tags({typeCodes:['index-illumina-sequencing']});
        		$scope.lists.refresh.tags({typeCodes:['index-illumina-sequencing'], categoryCodes:['DUAL-INDEX','POOL-INDEX']});
        	} else { 
        		//$scope.lists.refresh.tags({typeCodes:['index-illumina-sequencing'], groupNames:[groupName.value]});
        		$scope.lists.refresh.tags({typeCodes:['index-illumina-sequencing'], categoryCodes:['DUAL-INDEX','POOL-INDEX'], groupNames:[groupName.value]});
        	}
	    }
	};
	
	// pour selection manuelle d'index/tag
	$scope.updatePropertyFromUDT = function(value, col){
		//console.log("update from property : "+col.property);
		if(col.property === 'outputContainerUsed.experimentProperties.tag.value'){
			tagService.computeTagCategory(value.data);
		}
	};
}]);
