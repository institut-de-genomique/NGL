angular.module('home').controller('PacBioLibraryCtrl',['$scope', '$parse',  '$filter', 'atmToSingleDatatable','tagService', 
                                                     function($scope, $parse, $filter, atmToSingleDatatable, tagService  ){
	
	var inputExtraHeaders=Messages("experiments.inputs");
	var outputExtraHeaders=Messages("experiments.outputs");	
	
	var datatableConfig = {
			name: $scope.experiment.typeCode.toUpperCase(),

			"columns":[
			         //--------------------- INPUT containers section -----------------------
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
					 { //sample Aliquot ajout 03/06/2025
				        "header":Messages("containers.table.codeAliquot"),
				 		"property": "inputContainer.contents", 
				 		"filter": "getArray:'properties.sampleAliquoteCode.value'",
				 		"order":true,
				 		"hide":true,
				 		"type":"text",
				 		"position":5.5,
				 		"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
				        "extraHeaders":{0: inputExtraHeaders}
					},
					 { // Issu du type d'expérience
						"header":Messages("containers.table.fromTransformationTypeCodes"),
						"property":"inputContainer.fromTransformationTypeCodes",
						"filter":"unique | codes:'type'",
						"hide":true,
						"order":false,
						"type":"text",
						"position":6,
						"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
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
					 { // Concentration
						// et si le container arrive avec une unité differente ??????
						// utiliser le label court pour gagner de la place
						"header":Messages("containers.table.concentration.shortLabel") + " (ng/µL)",
						"property":"inputContainerUsed.concentration.value",
						"order":true,
						"hide":true,
						"type":"number",
						"position":10,
						"extraHeaders":{0: inputExtraHeaders}
					},  
					{ // Volume
						"header":Messages("containers.table.volume") + " (µL)",
						"property":"inputContainerUsed.volume.value",
						"order":true,
						"hide":true,
						"type":"number",
						"position":11,
						"extraHeaders":{0: inputExtraHeaders}
					},
					{ // Quantité : a afficher !
						"header":Messages("containers.table.quantity")+ " (ng)",
						"property":"inputContainerUsed.quantity.value",
						"order":true,
						"hide":true,
						"type":"number",
						"position":12,
						"extraHeaders":{0:inputExtraHeaders}
					},

			         //--->  colonnes specifiques experience s'inserent ici  (inputUsed ??)     
			         
			         //------------------------- OUTPUT containers section --------------------------
			         
			         //--->  colonnes specifiques experience s'inserent ici  (outputUsed ??)
			         
			         
				 	{ //attribut Volume avec les controles obligatoires / pas valeur par defaut
						"header":Messages("containers.table.volume") + " (µL)",
						"property":"outputContainerUsed.volume.value",
						"order":true,
						"edit":true,
						"required":"isRequired('IP')", // étoile s'affiche mais experience peut etre terminée sans valeur !!
					  	                               // doit être complété par validation drools !!
						"defaultValues":"25",		// NGL-5251	passage a 25			   
						"hide":true,
						"type":"number",
						"position":90,
						"extraHeaders":{0:outputExtraHeaders}
					},
					{ // au CNG il faut "outputContainerUsed.locationOnContainerSupport.code" + edit =true car c'est l'utilisateur qui defini
						"header":Messages("containers.table.supportCode"),
						"property":"outputContainerUsed.locationOnContainerSupport.code",
						"hide":true,
						"edit": true,
						"editDirectives":"udt-change='atmService.emptyToNull(value.data, col.property)'", // etre sur qu'il ne reste pas un blanc...
						"type":"text",
						"position":100,
						"extraHeaders":{0:outputExtraHeaders}
					},
					{
						"header":Messages("containers.table.stateCode"),
						"property":"outputContainer.state.code | codes:'state'",
						"order":true,
						"edit":false,
						"hide":true,
						"type":"text",
						"position":600,
						"extraHeaders":{0: outputExtraHeaders}
					},
					{
						"header":Messages("containers.table.storageCode"),
						"property":"outputContainerUsed.locationOnContainerSupport.storageCode",
						"order":true,
						"edit":true,
						"hide":true,
						"type":"text",
						"position":700,
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
	}; // fin struct datatableConfig

	// colonnes variables
    // ENTREE: tube ou strip... mixte ???

	var inputSupportCategoryCode = $scope.plateFluoUtils.getSupportCategoryCode();
	// retourne 'tube', 'strip-8', '96-well-plate' ou 'mixte'

	if ( inputSupportCategoryCode === "strip-8" || inputSupportCategoryCode === "mixte" ) {
		datatableConfig.columns.push({
			"header":Messages("containers.table.support.name"),
			"property":"inputContainer.support.code", 
			"order":true,
			"hide":true,
			"type":"text",
			"position":1,
			"extraHeaders":{0:inputExtraHeaders}
		});			
		datatableConfig.columns.push({
			"header":Messages("containers.table.support.column"),
			"property":"inputContainer.support.column",
			"order":true,
			"hide":true,
			"type":"text",
			"position":2,
			"extraHeaders":{0:inputExtraHeaders}
		});		
    } else if ( inputSupportCategoryCode === "tube") {
		datatableConfig.columns.push({
				"header":Messages("containers.table.code"),
				"property":"inputContainer.code",
				"hide":true,
				"type":"text",
				"position":1,
				"extraHeaders":{0: inputExtraHeaders}
			});
	} else {
		console.log('input category code '+inputSupportCategoryCode + ' non géré!!!');
		///$scope.messages.setError(Messages("experiments.input.error.only-tubes-or-strips"));
	}

	if ( $scope.experiment.instrument.outContainerSupportCategoryCode === "strip-8") {
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
	}

	// interdire la sortie plaque 
	// ne pas pas mettre le clear dans un else simple car sinon le undefined efface le message !!
	$scope.$watch("experiment.instrument.outContainerSupportCategoryCode", function(){

			if  ($scope.experiment.instrument.outContainerSupportCategoryCode === "96-well-plate"){
				console.log('output category code '+ $scope.experiment.instrument.outContainerSupportCategoryCode+ ' non géré!!!');
				// setError ne fonctionne pas ici...
				//$scope.setError(Messages('experiments.output.error.supportCategory','Plaque 96'));
				$scope.messages.clazz = "alert alert-danger";
				$scope.messages.text = Messages('experiments.output.error.supportCategory','Plaque 96');
				$scope.messages.open();

				$scope.experiment.instrument.outContainerSupportCategoryCode = undefined;
				
			} else if (($scope.experiment.instrument.outContainerSupportCategoryCode === "strip-8") || 
			           ($scope.experiment.instrument.outContainerSupportCategoryCode === "tube")){
		
				$scope.messages.clear();
			}
	});


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
			volume : "µL",
			concentration : "ng/µl",
			quantity : "ng"
	};
	
	// aide a la saisie des index
	// !! les surcharges doivent etre faites avant experimentToView 
	// filter tags.groupNames sur selectedTagGroup 
	atmService.convertOutputPropertiesToDatatableColumn = function(property, pName){
		var column = atmService.$commonATM.convertTypePropertyToDatatableColumn(property,"outputContainerUsed."+pName+".",{"0":Messages("experiments.outputs")});
		if(property.code==="tag"){
			column.editTemplate='<input class="form-control" type="text" #ng-model typeahead="tag.code as tag.name for tag in getTags() | filter:{groupNames:selectedTagGroup.value} | filter:{name:$viewValue} | limitTo:20" typeahead-min-length="1" udt-change="updatePropertyFromUDT(value,col)"/>'; 
			}
		return column;
	};
	
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	$scope.atmService = atmService;

	//  forcer largeur de la colonne "Tag"
	var dtConfig = $scope.atmService.data.getConfig();

	var indiceColonne = 13; // attention ne correspond pas au displayOrder !!
	if (dtConfig.columns && dtConfig.columns[indiceColonne]) {
		dtConfig.columns[indiceColonne].thClass="columnWidth80px"; 
	}
	
	// pas de gestion des unités !!!!! TODO ??? voir fluo-quantification-ctrl.js
	var computeQuantityLib = function(udtData){
		var getter = $parse("inputContainerUsed.experimentProperties.pacbioLibraryInputQuantity.value");

		var compute = {
				inputContConc : $parse("inputContainerUsed.concentration.value")(udtData),
				inputContVolume : $parse("inputContainerUsed.volume.value")(udtData), // ajouté pour le control de qté max
				inputVolume : $parse("inputContainerUsed.experimentProperties.inputVolume.value")(udtData),		

				isReady:function(){
					// traiter le cas ou il y a 1 des valeurs (en general c'est la conc) est a 0
					return (this.inputContConc >= 0  && this.inputVolume >= 0 && this.inputContVolume >= 0);
				}
			};
		
		if(compute.isReady()){
			var quantity = $parse("inputContConc * inputVolume")(compute);
			console.log("quantity = "+quantity);

			if(angular.isNumber(quantity) && !isNaN(quantity)){
				inputQuantity = Math.round(quantity*10)/10;

				// ne pas dépasser la quantité max disponible dans container
			    var	maxQuantity=$parse("inputContConc * inputContVolume")(compute);
				if (inputQuantity > maxQuantity){
					inputQuantity = maxQuantity;
				}
			}else{
				inputQuantity = 0;
			}

			getter.assign(udtData, inputQuantity); // value uniqt !!!

		}else{
			console.log("Missing values to exec computeQuantityLib");
		}
	}

	// aide a la saisie des tags ( dès NewState ??alias)
	// l'appel a tagService.initTags() est maintenant obligatoire pour l'assignation automatique de tagCategory
	//if ( $scope.isNewState() || $scope.isInProgressState() || Permissions.check("admin") ){
	if ( $scope.isNewState() || Permissions.check("admin") ){
	   tagService.initTags('index-pacbio-sequencing');
	   $scope.getTags= function(){return tagService.getAllTags()};
	   
	   $scope.getTagGroups= function(){return tagService.getAllTagGroups()};
	   $scope.selectedTagGroup= $scope.getTagGroups()[0]; // valeur defaut du select
	}

	// afficher dès isNewState() ????
	$scope.selectGroup = {
			isShow:function(){
				//return ( ( $scope.isInProgressState() &&  $scope.isEditMode() ) || Permissions.check("admin") );
				return (  $scope.isInProgressState()  || Permissions.check("admin") );
			}
	};
	
	// updatePropertyFromUDT  est automatiqut defini pour les colonnes injectees dans le datatable....
	$scope.updatePropertyFromUDT = function(value, col){
		console.log("update from property : "+col.property);

		if(col.property === 'inputContainerUsed.experimentProperties.inputVolume.value'){
			computeQuantityLib(value.data);
		} 

		if(col.property === 'outputContainerUsed.experimentProperties.tag.value'){
			tagService.computeTagCategory(value.data);
		}
	};

}]);