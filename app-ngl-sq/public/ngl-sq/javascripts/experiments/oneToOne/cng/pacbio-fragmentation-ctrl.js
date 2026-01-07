// 20/06/2024 copié d'apres qmp-lib-prep-ctrl
angular.module('home').controller('PacBioFragmentationCtrl',['$scope', '$parse','atmToSingleDatatable','datatable',
                                                               function($scope,$parse, atmToSingleDatatable, datatable) {
	
	var inputExtraHeaders=Messages("experiments.inputs");
	var outputExtraHeaders=Messages("experiments.outputs");
	
	var datatableConfig = {
			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[
					//--------------------- INPUT containers section -----------------------
					{
						"header" : Messages("containers.table.code"),
						"property" : "inputContainer.support.code",
						"order" : true,
						"edit" : false,
						"hide" : true,
						"type" : "text",
						"position" : 1,
						"mergeCells" : true,
						"extraHeaders" : {0: inputExtraHeaders}
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
					{ //sample Aliquot ajout 03/06/2025
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
					{ // Issu du type d'expérience
						"header":Messages("containers.table.fromTransformationTypeCodes"),
						"property":"inputContainer.fromTransformationTypeCodes",
						"filter":"unique | codes:'type'",
						"hide":true,
						"order":false,
						"type":"text",
						"position":8,
						"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
						"extraHeaders":{0:inputExtraHeaders}
					},	
					{ // Etat input Container 
						"header":Messages("containers.table.state.code"),
						"property":"inputContainer.state.code",
						"filter":"codes:'state'",
						"order":true,
						"edit":false,
						"hide":true,
						"type":"text",
						"position":9,
						"extraHeaders":{0: inputExtraHeaders}
					},
					{ // Concentration
						//  et si le container arrive avec une unité differente ??????
						"header":Messages("containers.table.concentration") + " (ng/µL)",
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
					{ // quantité : a calculer !
						"header":Messages("containers.table.quantity")+ " (ng)",
						"property":"inputContainerUsed.concentration.value * inputContainerUsed.volume.value",
						"order":true,
						"hide":true,
						"type":"number",
						"position":12,
						"extraHeaders":{0:inputExtraHeaders}
					},
					//--->  colonnes specifiques experience s'inserent ici  (inputUsed ??)

					//------------------------- OUTPUT containers section --------------------------
					
					//--->  colonnes specifiques experience s'inserent ici  (outputUsed ??)

		         	//attribut Volume avec les controles obligatoires / pas valeur par defaut
				 	{
						"header":Messages("containers.table.volume") + " (µL)",
						"property":"outputContainerUsed.volume.value",
						"order":true,
						"edit":true,
						"required":"isRequired('IP')", // doit être complété par validation par drools 
					  	                             // étoile s'affiche mais experience peut etre terminée sans valeur !!
						"defaultValues":"47",							   
						"hide":true,
						"type":"number",
						"position":90,
						"extraHeaders":{0:outputExtraHeaders}
					},
					// au CNG il faut "outputContainerUsed.locationOnContainerSupport.code" + edit =true car c'est l'utilisateur qui defini
					{
						"header":Messages("containers.table.code"),
						"property":"outputContainerUsed.locationOnContainerSupport.code",
						"hide":true,
						"edit": true,
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
			compact:true,
			pagination:{
				active:false
			},		
			search:{
				active:false
			},
			order:{
				mode:'local',
				active:true,
				by:'inputContainer.sampleCodes'
			},
			remove:{
				active: ($scope.isEditModeAvailable() && $scope.isNewState()),
				showButton: ($scope.isEditModeAvailable() && $scope.isNewState()),
				mode:'local'
			},
			save:{
				active:true,
	        	withoutEdit: true,
	        	changeClass:false,
	        	showButton:false,
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
				active:false,
				columnMode:true
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
			"otherButtons": {
				// !! ng-click doit appeler une fonction dans $scope !!
                //active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
				active: $scope.isEditModeAvailable(),
                complex:true,
                template:''
                	+'<div class="btn-group" style="margin-left:5px">'
                	+'<button class="btn btn-default" ng-click="computeQuantityFragAll()" data-toggle="tooltip" title="'+Messages("experiments.button.plate.copyVolumeTo")+' vol. eng. librairie'
                	+'"><i class="fa fa-files-o" aria-hidden="true"></i> Copier les volumes et calculer les quantités</button>'                	                	
                	+'</div>'
			}
	};// fin struct datatableConfig

	// colonnes variables
    // NON actuellement seul le megaruptor est utilisé=> il ne peut y avoir que des tubes entrée !!! et en sortie !!
	// TODO gerer les strips en sortie !!!!!!!

	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event save");
		$scope.atmService.data.save();  // on ne trouve pas cet appele dans tous les $on('save' )  !!!!!!!!!!
		$scope.atmService.viewToExperimentOneToOne($scope.experiment);

		$scope.copyQuantityToOutputContainer();

		$scope.$emit('childSaved', callbackFunction);
	});
	
	$scope.$on('refresh', function(e) {
		console.log("call event refresh");		
		var dtConfig = $scope.atmService.data.getConfig();
		dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));
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

	$scope.toggleIsShowInformation=function(){
		console.log ('toggleIsShowInformation');
		if ( $scope.isShowInformation===false) { $scope.isShowInformation=true; }
		else {$scope.isShowInformation=false}
	}
	
	// Init
	var atmService = atmToSingleDatatable($scope, datatableConfig);

	//defined new atomictransfertMethod c
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
	}

	atmService.experimentToView($scope.experiment, $scope.experimentType);

	$scope.atmService = atmService;
	
	// updatePropertyFromUDT est automatiqut defini pour les colonnes injectees dans le datatable....
	$scope.updatePropertyFromUDT = function(value, col){
		console.log("update from property : "+col.property);
		if(col.property === 'inputContainerUsed.experimentProperties.inputVolume.value'){
			computeQuantityFrag(value.data); // = udtData
		} 
	}

	// avec une valeur par default de inputVolume, il faudrait déclencher le calcul dès l'affichage initial du datatable sans attendre updatePropertyFromUDT
	// mais ne marche pas car $scope.atmService.data.displayResult pas defini
	//   solution trouvée: passer par otherButton pour declencher tous les calculs!!!
	// Pb restant:  les 2 fonctions computeQuantityFragAll et computeQuantityFrag sont concurentes et ne fonctionnent pas correctement l'une après l'autre..
	//  une fois computeQuantityFrag lancée, computeQuantityFragAll nr fonctionne plus...

	$scope.computeQuantityFragAll= function(){
		console.log("Compute all quantities");
	
		var result = $scope.atmService.data.displayResult;		
		result.forEach(function(value){
		  //  !! Probleme !! le volume,recupéré ici est toujour celui mis à jour  manuellement !!! 
		  // écrire value.data.inputContainer.volume.value  ou  value.data.inputContainerUsed.volume.value c'est pareil !!!!

		  if ( value.data.inputContainerUsed.concentration && value.data.inputContainerUsed.volume) {
			var inputContConc=value.data.inputContainerUsed.concentration.value;
			var inputContVol=value.data.inputContainerUsed.volume.value; // volume disponible dans container
			console.log('contConc='+ inputContConc );
			console.log('contVol='+ inputContVol );

			// creer experimentProperties si nécessaire
			if ( !value.data.inputContainerUsed.experimentProperties ){
			   value.data.inputContainerUsed.experimentProperties={};
			}

			  	if ( !value.data.inputContainerUsed.experimentProperties.inputVolume ){
					value.data.inputContainerUsed.experimentProperties.inputVolume = {};
				}

				// le volume engagé est dans ce cas tout le volume disponible !==>  copier l'objet volume (value ET unit) 
				value.data.inputContainerUsed.experimentProperties.inputVolume=value.data.inputContainerUsed.volume;

				var inputQuantity=inputContConc * inputContVol; // valeur definies avant la copie !!

				console.log(inputContConc + '* '+ inputContVol + "=" +inputQuantity);

				if ( !value.data.inputContainerUsed.experimentProperties.inputQuantity ){
					value.data.inputContainerUsed.experimentProperties.inputQuantity = {};
				}
				value.data.inputContainerUsed.experimentProperties.inputQuantity.value=inputQuantity;
				value.data.inputContainerUsed.experimentProperties.inputQuantity.unit="ng";
		  }
		})			
	};

	// pas de gestion des unités !!!!! TODO ??? voir fluo-quantification-ctrl.js
	var computeQuantityFrag = function(udtData){
		var getter = $parse("inputContainerUsed.experimentProperties.inputQuantity.value");
		//var inputQuantity = 0;
		
		var compute = {
				inputContConc : $parse("inputContainerUsed.concentration.value")(udtData),
				inputContVol : $parse("inputContainerUsed.volume.value")(udtData),
				volumeEngage : $parse("inputContainerUsed.experimentProperties.inputVolume.value")(udtData),
				
				isReady:function(){
					// traiter le cas ou il y a 1 des valeurs (en general c'est la conc) est a 0
					// ne pas prendre plus de volume que ce qui existe !!   il y a des cas ou ca ne marche pas les 2 vol sont égaux !!!!!!!!!!!
					return (this.inputContConc >= 0  && this.inputContVol >= 0 && (this.volumeEngage <= this.inputContVol) );
				}
			};
		
		if(compute.isReady()){
			var quantity = $parse("inputContConc * volumeEngage")(compute);
			console.log("quantity = "+quantity);

			if(angular.isNumber(quantity) && !isNaN(quantity)){
				inputQuantity = Math.round(quantity*10)/10;
				console.log("quantity = "+inputQuantity);

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
			console.log("Missing or incorrect values to exec computeQuantityFrag");
		}
	}

	// SPECS: la valeur de InputQuantity (Quantité engagée) doit être copiée dans l'attribut quantité du container de sortie
	//methode bourrin avec FOR.... toujours un seul inputContainerUsed et un seul outputContainerUsed ???
	$scope.copyQuantityToOutputContainer = function() {	
		if ($scope.experiment.atomicTransfertMethods) {
			for (var i = 0; i < $scope.experiment.atomicTransfertMethods.length; i++) {
				var quantity;
				if ($scope.experiment.atomicTransfertMethods[i].inputContainerUseds[0].experimentProperties) {
					quantity = $scope.experiment.atomicTransfertMethods[i].inputContainerUseds[0].experimentProperties.inputQuantity;
				}
				
				if ($scope.experiment.atomicTransfertMethods[i].outputContainerUseds[0]) {
					$scope.experiment.atomicTransfertMethods[i].outputContainerUseds[0].quantity = quantity; // value  uniqt !!
				}
			};
		}	
	};

	// test calcul a l'init NON $scope.atmService.data.displayResult pas defini !!!
	// $scope.computeQuantityFragAll();

}]);