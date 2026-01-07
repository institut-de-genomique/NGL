angular.module('home').controller('CNGPrepaFlowcellOrderedCtrl',['$scope', '$parse', '$http','atmToDragNDrop','mainService',
                                                               function($scope, $parse, $http, atmToDragNDrop, mainService) {
	
	//surcharge default/tubes-to-flowcell-ctrl.js !!!
	var atmToSingleDatatable = $scope.atmService.$atmToSingleDatatable;

	// Pour le datatable dans l'onglet "feuille de calcul"
	var columns = [  
	             {
		        	 "header":Messages("containers.table.support.number"),
		        	 "property":"atomicTransfertMethod.line",
		        	 "order":true,
					 "edit":false,
					 "hide":true,
		        	 "type":"text",
		        	 "position":0,
		        	 "extraHeaders":{0:"lib normalisée"}
		         },	
		         {
		        	 "header":Messages("containers.table.supportCode"),
		        	 "property":"inputContainer.support.code",
		        	 "order":true,
					 "edit":false,
					 "hide":true,
		        	 "type":"text",
		        	 "position":1,
		        	 "extraHeaders":{0:"lib normalisée"}
		         },	
		         {
		        	"header":Messages ("containers.table.codeAliquot"),
		 			"property": "inputContainer.contents",
		 			"filter": "getArray:'properties.sampleAliquoteCode.value'| unique",
		 			"order":false,
		 			"hide":true,
		 			"type":"text",
		 			"position":3,
		 			"render": "<div list-resize='cellValue' list-resize-min-size='3'>",
		        	 "extraHeaders":{0:"lib normalisée"}
			     },
		         {
		        	"header":Messages("containers.table.tags"),
		 			"property": "inputContainer.contents",
		 			"filter": "getArray:'properties.tag.value'| unique",
		 			"order":false,
		 			"hide":true,
		 			"type":"text",
		 			"position":4,
		 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
		        	 "extraHeaders":{0:"lib normalisée"}
		         },				         
				 {
		        	 "header":Messages("containers.table.concentration") + " (nM)",
		        	 "property":"inputContainerUsed.concentration.value",
		        	 "order":true,
					 "edit":false,
					 "hide":true,
		        	 "type":"number",
		        	 "position":5,
		        	 "extraHeaders":{0:"lib normalisée"}
		         },
		         {
		        	 "header":Messages("containers.table.volume") + " (µL)",
		        	 "property":"inputContainerUsed.volume.value",
		        	 "order":true,
					 "edit":false,
					 "hide":true,
		        	 "type":"number",
		        	 "position":6,
		        	 "extraHeaders":{0:"lib normalisée"}
		         },
		         {
		        	 "header":Messages("containers.table.state.code"),
		        	 "property":"inputContainer.state.code",
		        	 "order":true,
					 "edit":false,
					 "hide":true,
		        	 "type":"text",
					 "filter":"codes:'state'",
		        	 "position":7,
		        	 "extraHeaders":{0:"lib normalisée"}
		         },
		         {
		        	 "header":Messages("containers.table.percentage"),
		        	 "property":"inputContainerUsed.percentage",
		        	 "order":true,
					 "edit":false,
					 "hide":true,
		        	 "type":"number",
		        	 "position":50,
		        	 "extraHeaders":{0:"prep FC"}
		         },		  
		         //-- output section
		         {
		        	 "header":Messages("containers.table.code"),
		        	 "property":"outputContainerUsed.code",
		        	 "order":true,
					 "edit":false,
					 "hide":true,
					 "type":"text",
		        	 "position":400,
		        	 "extraHeaders":{0:"prep FC"}
		         },
		         {
		        	 "header":Messages("containers.table.stateCode"),
		        	 "property":"outputContainer.state.code | codes:'state'",
		        	 "order":true,
					 "edit":false,
					 "hide":true,
					 "type":"text",
		        	 "position":500,
		        	 "extraHeaders":{0:"prep FC"}
		         }
	];
	
	// 09/01/2024 !! il peut y avoir un mix tubes/plates
	var tmp = [];
	if(!$scope.isCreationMode()){
		tmp = $scope.$eval("atomicTransfertMethods|flatArray:'inputContainerUseds'|getArray:'locationOnContainerSupport.categoryCode'|unique",$scope.experiment);			
	}else{
		tmp = $scope.$eval("getBasket().get()|getArray:'support.categoryCode'|unique",mainService);
	}
	var supportCategoryCode = undefined;
	if(tmp.length === 1){
		$scope.supportCategoryCode = tmp[0];
	}else{
		$scope.supportCategoryCode = "mixte";
	}

	// 25/04/2017  si utilisation du janus alors il faut aussi afficher la colonne de la plaque
	//if($scope.experiment.instrument.inContainerSupportCategoryCode !== "tube"){
	if ($scope.supportCategoryCode !== "tube"){
		columns.push(
			 {
	        	 "header":Messages("containers.table.well"),
	        	 "property":"inputContainer.support.line+inputContainer.support.column",
	        	 "order":true,
				 "edit":false,
				 "hide":true,
	        	 "type":"text",
	        	 "position":1.1,
	        	 "extraHeaders":{0:"lib normalisée"}
	         }
		);
	}
	
	// NGL-2083: adapté depuis CNS pour feuille de calcul dynamique en fonction du type de sequencage choisi
	// soit utiliser les valeurs converties (200, 400) destinee a l'affichage mais dans ce cas les setter dans le datatable.( fait au CNS)
	// soit utiliser les valeurs brutes 200000000, 40000000 et les setter  dans l'experience
	// GA gérer a part le seul cas qui pose réellement problème ici: trisHCLConcentration
	var defaultValues = {
			"Hiseq 4000":{
				"inputContainerUsed.experimentProperties.inputVolume2.value":5,
				"inputContainerUsed.experimentProperties.NaOHVolume.value":5,
				"inputContainerUsed.experimentProperties.NaOHConcentration.value":"0.1N",
				"inputContainerUsed.experimentProperties.trisHCLVolume.value":5,
				"inputContainerUsed.experimentProperties.trisHCLConcentration.value":200, 
				"inputContainerUsed.experimentProperties.trisHCLConcentration.value.model":200000000, 
				"inputContainerUsed.experimentProperties.masterEPXVolume.value":35,
				
				"outputContainerUsed.experimentProperties.finalVolume.value":50
			},
			"Hiseq X":{
				"inputContainerUsed.experimentProperties.inputVolume2.value":5,
				"inputContainerUsed.experimentProperties.NaOHVolume.value":5,
				"inputContainerUsed.experimentProperties.NaOHConcentration.value":"0.1N",
				"inputContainerUsed.experimentProperties.trisHCLVolume.value":5,
				"inputContainerUsed.experimentProperties.trisHCLConcentration.value":200, 
				"inputContainerUsed.experimentProperties.trisHCLConcentration.value.model":200000000, 
				"inputContainerUsed.experimentProperties.masterEPXVolume.value":35,
				
				"outputContainerUsed.experimentProperties.finalVolume.value":50
			},
			// NGL-21-91 ajout S1
			// NGL-2624 renommage "S1" => "S1-SP"
			"NovaSeq 6000 / S1-SP": {
				"inputContainerUsed.experimentProperties.inputVolume2.value":100,
				"inputContainerUsed.experimentProperties.NaOHVolume.value":25,
				"inputContainerUsed.experimentProperties.NaOHConcentration.value":"0.2N",
				"inputContainerUsed.experimentProperties.trisHCLVolume.value":25,
				"inputContainerUsed.experimentProperties.trisHCLConcentration.value":400,
				"inputContainerUsed.experimentProperties.trisHCLConcentration.value.model":400000000, 
				"inputContainerUsed.experimentProperties.masterEPXVolume.value":350,
				
				"outputContainerUsed.experimentProperties.finalVolume.value":500
			},
			"NovaSeq 6000 / S2": {
				"inputContainerUsed.experimentProperties.inputVolume2.value":150,
				"inputContainerUsed.experimentProperties.NaOHVolume.value":37,
				"inputContainerUsed.experimentProperties.NaOHConcentration.value":"0.2N",
				"inputContainerUsed.experimentProperties.trisHCLVolume.value":38,
				"inputContainerUsed.experimentProperties.trisHCLConcentration.value":400,
				"inputContainerUsed.experimentProperties.trisHCLConcentration.value.model":400000000, 
				"inputContainerUsed.experimentProperties.masterEPXVolume.value":525,
				
				"outputContainerUsed.experimentProperties.finalVolume.value":750
			},
			//  le S3 pas encore au point chez Illumina...
			"NovaSeq 6000 / S4": {
				"inputContainerUsed.experimentProperties.inputVolume2.value":310,
				"inputContainerUsed.experimentProperties.NaOHVolume.value":77,
				"inputContainerUsed.experimentProperties.NaOHConcentration.value":"0.2N",
				"inputContainerUsed.experimentProperties.trisHCLVolume.value":78,
				"inputContainerUsed.experimentProperties.trisHCLConcentration.value":400, 
				"inputContainerUsed.experimentProperties.trisHCLConcentration.value.model":400000000,
				"inputContainerUsed.experimentProperties.masterEPXVolume.value":1085,
				
				"outputContainerUsed.experimentProperties.finalVolume.value":1550
			},
			// NGL-2219 18/09/2018  0 valeurs par defaut pour les "XP"
			// NGL-2624 renommage "S1" => "S1-SP"; valeurs de feuille de calcul
			"NovaSeq 6000 / S1-SP / XP": {
				"inputContainerUsed.experimentProperties.inputVolume2.value":18,
				"inputContainerUsed.experimentProperties.NaOHVolume.value":4,
				"inputContainerUsed.experimentProperties.NaOHConcentration.value":"0.2N",
				"inputContainerUsed.experimentProperties.trisHCLVolume.value":5,
				"inputContainerUsed.experimentProperties.trisHCLConcentration.value":400,
				"inputContainerUsed.experimentProperties.trisHCLConcentration.value.model":400000000,
				"inputContainerUsed.experimentProperties.masterEPXVolume.value":63,
				
				"outputContainerUsed.experimentProperties.finalVolume.value":90
			},
			// NGL-2634 02/08/2019 
			"NovaSeq 6000 / S2 / XP": {
				"inputContainerUsed.experimentProperties.inputVolume2.value":22,
				"inputContainerUsed.experimentProperties.NaOHVolume.value":5,
				"inputContainerUsed.experimentProperties.NaOHConcentration.value":"0.2N",
				"inputContainerUsed.experimentProperties.trisHCLVolume.value":6,
				"inputContainerUsed.experimentProperties.trisHCLConcentration.value":400,
				"inputContainerUsed.experimentProperties.trisHCLConcentration.value.model":400000000,
				"inputContainerUsed.experimentProperties.masterEPXVolume.value":77,
				
				"outputContainerUsed.experimentProperties.finalVolume.value":110
			},
			// NGL-2634 02/08/2019 
			"NovaSeq 6000 / S4 / XP": {
				"inputContainerUsed.experimentProperties.inputVolume2.value":30,
				"inputContainerUsed.experimentProperties.NaOHVolume.value":7,
				"inputContainerUsed.experimentProperties.NaOHConcentration.value":"0.2N",
				"inputContainerUsed.experimentProperties.trisHCLVolume.value":8,
				"inputContainerUsed.experimentProperties.trisHCLConcentration.value":400,
				"inputContainerUsed.experimentProperties.trisHCLConcentration.value.model":400000000,
				"inputContainerUsed.experimentProperties.masterEPXVolume.value":105,
				
				"outputContainerUsed.experimentProperties.finalVolume.value":150
			},
			// NGL-4421 NovaSeq Xplus
			"NovaSeq Xplus / 10B": {
				//"inputContainerUsed.experimentProperties.inputVolume2.value":........ l'input volume va etre calculé
				"inputContainerUsed.experimentProperties.NaOHVolume.value":8.5,
				"inputContainerUsed.experimentProperties.NaOHConcentration.value":"0.2N",				
				"inputContainerUsed.experimentProperties.trisHCLVolume.value":0,              // pas utilisé; historique 
				"inputContainerUsed.experimentProperties.trisHCLConcentration.value":0,       // pas utilisé; historique 
				"inputContainerUsed.experimentProperties.trisHCLConcentration.value.model":0, // pas utilisé; historique
				"inputContainerUsed.experimentProperties.masterEPXVolume.value":127.5,        // est maintenant affiché "vol Master mix"

				"outputContainerUsed.experimentProperties.finalVolume.value":170
			},
			"NovaSeq Xplus / 25B": {
				//"inputContainerUsed.experimentProperties.inputVolume2.value":........ l'input volume va etre calculé
				"inputContainerUsed.experimentProperties.NaOHVolume.value":14,
				"inputContainerUsed.experimentProperties.NaOHConcentration.value":"0.2N",
				"inputContainerUsed.experimentProperties.trisHCLVolume.value":0,              // pas utilisé; historique 
				"inputContainerUsed.experimentProperties.trisHCLConcentration.value":0,       // pas utilisé; historique 
				"inputContainerUsed.experimentProperties.trisHCLConcentration.value.model":0, // pas utilisé; historique
				"inputContainerUsed.experimentProperties.masterEPXVolume.value":210,          // est maintenant affiché "vol Master mix"

				"outputContainerUsed.experimentProperties.finalVolume.value":280
			},
			// NGL-4723 arrivée  1.5B. volumes identiques a la 10B
			"NovaSeq Xplus / 1.5B": {
				//"inputContainerUsed.experimentProperties.inputVolume2.value":........ l'input volume va etre calculé
				"inputContainerUsed.experimentProperties.NaOHVolume.value":8.5,
				"inputContainerUsed.experimentProperties.NaOHConcentration.value":"0.2N",
				"inputContainerUsed.experimentProperties.trisHCLVolume.value":0,              // pas utilisé; historique 
				"inputContainerUsed.experimentProperties.trisHCLConcentration.value":0,       // pas utilisé; historique 
				"inputContainerUsed.experimentProperties.trisHCLConcentration.value.model":0, // pas utilisé; historique
				"inputContainerUsed.experimentProperties.masterEPXVolume.value":127.5,        // est maintenant affiché "vol Master mix"

				"outputContainerUsed.experimentProperties.finalVolume.value":170
			}
	};

	// appelé ?????? fois !!!!!
	var getDefaultValueForWorkSheet = function(line, col){
		//inputVolume2
		//NaOHVolume
		//NaOHConcentration
		//trisHCLVolume
		//trisHCLConcentration 
		//masterEPXVolume
		//final Volume
		//console.log('get default value for '+  col.property);
		
		var worksheet = $parse("experimentProperties.sequencingType.value")($scope.experiment);
		
		if(worksheet && defaultValues[worksheet][col.property]){
			var def=defaultValues[worksheet][col.property];
			//console.log( '3> def='+ def);
			return def;
		}else {
			return undefined;
		}
	}
	
	atmToSingleDatatable.data.setColumnsConfig(columns);
	
	// appellée pour chacune des propriétés des outputcontainerUsed
	atmToSingleDatatable.convertOutputPropertiesToDatatableColumn = function(property, pName){
		console.log ('0> output property.code='+ property.code +'...'+pName);
		return   this.$commonATM.convertSinglePropertyToDatatableColumn(property,"outputContainerUsed."+pName+".",{"0":"prep FC"});
	};
	
    // appellée pour chacune des propriétés des inputcontainerUsed (inputVolume2, NaOHVolume, NaOHConcentration...)
    // SUPSQCNG610=>  propriete source:  additionnal header "lib normalisée" et non "Dénaturation - neutralisation"
    atmToSingleDatatable.convertInputPropertiesToDatatableColumn = function(property, pName){
        console.log ('1> input property.code='+ property.code +'...'+pName );

        if(property.code === "source"){
            var column = this.$commonATM.convertSinglePropertyToDatatableColumn(property,"inputContainerUsed."+pName+".",{"0":"lib normalisée"});
        }else{
        		var column = this.$commonATM.convertSinglePropertyToDatatableColumn(property,"inputContainerUsed."+pName+".",{"0":"Dénaturation - neutralisation"});
				column.defaultValues = getDefaultValueForWorkSheet;
		}
        return column;
    }
	
	atmToSingleDatatable.addExperimentPropertiesToDatatable($scope.experimentType.propertiesDefinitions);
	
	//-0-
	$scope.$watch("instrumentType", function(newValue, oldValue){
		if(newValue)
			atmToSingleDatatable.addInstrumentPropertiesToDatatable(newValue.propertiesDefinitions);
	})
	
	// bouton de génération de sampleSheet pour 'janus-and-cBotV2'
	$scope.setAdditionnalButtons([{
		isDisabled : function(){return $scope.isCreationMode();},
		isShow:function(){return ($scope.experiment.instrument.typeCode === 'janus-and-cBotV2')},
		click:$scope.fileUtils.generateSampleSheet,
		label:Messages("experiments.sampleSheet")
	}]);

	// 06/01/2017 FDS ajout pour l'import du fichier Cbot-V2
	var importDataCbot = function(){
		console.log('Import cBot file');
		
		$scope.messages.clear();
		$http.post(jsRoutes.controllers.instruments.io.IO.importFile($scope.experiment.code).url, $scope.file)
		.success(function(data, status, headers, config) {		
			$scope.messages.setSuccess(Messages('experiments.msg.import.success'));
			
			// data est l'experience retournée par input.java=> récupérer instrumentProperties et reagents...
			$scope.experiment.instrumentProperties= data.instrumentProperties;
			$scope.experiment.reagents=data.reagents;
			
			// reinit select File...
			$scope.file = undefined;
			angular.element('#importFilecBot')[0].value = null;
			
			// NGL-1256 refresh special pour les reagents !!!
			$scope.$emit('askRefreshReagents');		
		})
		.error(function(data, status, headers, config) {	
			// correction 18/12/2017 ; setError ne peut afficher qu'une seule erreur....
			$scope.messages.clazz = "alert alert-danger";
			$scope.messages.text = Messages('experiments.msg.import.error');
			$scope.messages.setDetails(data);
			$scope.messages.showDetails = true;
			$scope.messages.open();	
			
			// reinit select File et le bouton d'import
			$scope.file = undefined;
			angular.element('#importFileCbot')[0].value = null;
		});		
	};
	
	$scope.buttonCbot = {
			isShow:function(){
				// 31/01/2017 activer le bouton d'import si l'experience est a InProgress
				// 19/12/2018 et pas en edit mode  || afficher bouton pour admin
				return ($scope.isInProgressState() && !$scope.mainService.isEditMode() || Permissions.check("admin")  );
				},
			isFileSet:function(){
				return ($scope.file == undefined)?"disabled":"";
			},
			click:importDataCbot
		};	
	
	// 25/10/2017 FDS ajout pour l'import du fichier Mettler
	var importDataMettler = function(){
		console.log('Import Mettler file');
		
		$scope.messages.clear();
		$http.post(jsRoutes.controllers.instruments.io.IO.importFile($scope.experiment.code).url+"?extraInstrument=labxmettlertoledo", $scope.file)
		.success(function(data, status, headers, config) {
			$scope.messages.setSuccess(Messages('experiments.msg.import.success'));
			
			// data est l'experience retournée par input.java=> recuperer instrumentProperties et reagents...
			$scope.experiment.instrumentProperties= data.instrumentProperties;
			$scope.experiment.reagents=data.reagents;
			
			// reinit select File et le bouton d'import
			$scope.file = undefined;
			angular.element('#importFileMettler')[0].value = null;
			
			//refresh reagents !!!
			$scope.$emit('askRefreshReagents');	
		})
		.error(function(data, status, headers, config) {
			$scope.messages.clazz = "alert alert-danger";
			$scope.messages.text = Messages('experiments.msg.import.error');
			$scope.messages.setDetails(data);
			$scope.messages.showDetails = true;
			$scope.messages.open();	
			
			// reinit select File et le bouton d'import
			$scope.file = undefined;
			angular.element('#importFileMettler')[0].value = null;
		});		
	};
	
	// 25/10/2017 FDS ajout pour l'import du fichier Mettler
	// 31/01/2018 pas de fichier Mettler pour les cbots interne des NovaSeq6000
	// 24/10/2023 pas de fichier Mettler pour les cbots interne des NovaSeqXPlus
	$scope.buttonMettler = {
		isShow:function(){
			// visible meme si terminé, mais seulement en mode edition
			//return ( ($scope.isInProgressState()||$scope.isFinishState()) && $scope.isEditMode() ); // MARCHE PAS: editMode pas vu ici
			return ( ( $scope.isInProgressState()||$scope.isFinishState()) && ! isInstrumentNovaSeq6000() && ! isInstrumentNovaSeqXplus());
			},
		isFileSet:function(){
			return ($scope.file == undefined)?"disabled":"";
		},
		click:importDataMettler	
	};
	
	// 07/02/2017 si l'utilisateur modifie le codeStrip OU le code Flowcell OU l'instrument 
	// il doit recharger le fichier cBot pour qu'on puisse garantir la coherence !!
    //-1- stripCode.value
	$scope.$watch("experiment.instrumentProperties.stripCode.value", function(newValue, oldValue){
		if (newValue && newValue !== oldValue ) {
			console.log("1/watch-------stripCode.value="+newValue+"---------");
			if ( $scope.experiment.instrumentProperties.cbotFile ) { $scope.experiment.instrumentProperties.cbotFile.value = undefined;}
		}
	});	
	
	//-2- code Flowcell
	$scope.$watch("experiment.instrumentProperties.containerSupportCode.value", function(newValue, oldValue){
		if (newValue && newValue !== oldValue ) {
			console.log("2/watch-------containerSupportCode.value="+newValue+"---------");
			
			// reset du fichier Cbot si existe
			if ( $scope.experiment.instrumentProperties.cbotFile ) { $scope.experiment.instrumentProperties.cbotFile.value = undefined; }
			checkAll();
		}
	});	
	
	//-3- code instrument
	$scope.$watch("experiment.instrument.code" , function(newValue, oldValue){	
		if (newValue && newValue !== oldValue ){
			console.log("3/watch-------instrument.code="+newValue+"---------");
			// reset du fichier Cbot si existe
			if ($scope.experiment.instrumentProperties.cbotFile ){ $scope.experiment.instrumentProperties.cbotFile.value = undefined; }
			checkAll();
		}
	});
	
	// -4- NGL-2397 controler que le sequencing type est compatible avec le nombre de lanes du support output
	$scope.$watch("experiment.instrument.outContainerSupportCategoryCode", function(newValue, oldValue){
		if (newValue && newValue !== oldValue ) {
			console.log("4/watch-------outContainerSupportCategoryCode="+newValue+"---------");
			checkAll();
		}
	});
	
	//-5- sequencingType
	$scope.$watch("experiment.experimentProperties.sequencingType.value", function(newValue, oldValue){
		if (newValue  && newValue !== oldValue) {
			console.log("5/watch-------sequencingType.value="+newValue+"---------");
			checkAll();
		}

		// creation d'une methode séparée pour pouvoir l'appeler depuis le html
		$scope.sampleSheetCalc();
	});

	// Appellé aussi depuis flowcell.scala.html quand selection du TAB #roadmap# (experiments.calculation.sheet)
	// permet de selectionner le type de sequencage en premier 
	$scope.sampleSheetCalc = function(){
		// garder le nom newValue!!!
		var newValue=$scope.experiment.experimentProperties.sequencingType.value;
		console.log('>>>>>>>>>>CALC !!!!!'+newValue);

		// ancien comportement: l'utilisateur spécifie le volume engagé et NGL calcule la concentration finale (le volume final est fixe)
		// NGL-4421 NovaseqXplus : l'utilisateur spécifie la concentration finale et le volume engagé est calculé; ( le volume RSB est également calculé)
		$scope.atmService.data.atm.forEach(function(atm){

			// assignation du volume final (sequencingType dependant) [ idem ancien  mode calcul et nouveau mode calcul !!]
			atm.outputContainerUseds.forEach(function(ocu){					
				if(newValue && defaultValues[newValue]["outputContainerUsed.experimentProperties.finalVolume.value"]){
					$parse("experimentProperties.finalVolume.value").assign(ocu, defaultValues[newValue]["outputContainerUsed.experimentProperties.finalVolume.value"]);	
				}else{
					// select "Sequencing type" remis a undefined 
					$parse("experimentProperties.finalVolume.value").assign(ocu, undefined);
				}																				
			});
				
			// modif des proprietes input 
			/* Vu avec Guillaume: normallement ne pas injecter de valeurs dans l'experience, car les valeurs sont recuperees au niveau du datatable qui lui effectue les conversions entre
			 * l'unite de stockage et l'unite d'affichage a l'utilisateur!! ( c'est dans atmToSingleDatatable => getDefaultValueForWorkSheet)
			 * MAIS pour arriver a calculer la concentration finale dès la selection du type sequencage le faire quand meme!! attention au trisHCLConcentration dont les unites sont differentes
			 * entre l'experience et le datatable !!!!
			 */
			atm.inputContainerUseds.forEach(function(icu){	
				if(newValue){
					if  ( newValue.match(/NovaSeq Xplus/) ) {
						console.log ('/NovaSeq Xplus/');
						// -1 assignation
						// pas d'assignation de inputVolume2.value  car va etre calculé
						$parse("experimentProperties.NaOHVolume.value").assign(icu,           defaultValues[newValue]["inputContainerUsed.experimentProperties.NaOHVolume.value"]);
						$parse("experimentProperties.NaOHConcentration.value").assign(icu,    defaultValues[newValue]["inputContainerUsed.experimentProperties.NaOHConcentration.value"]);
						$parse("experimentProperties.trisHCLVolume.value").assign(icu,        defaultValues[newValue]["inputContainerUsed.experimentProperties.trisHCLVolume.value"]);
						$parse("experimentProperties.trisHCLConcentration.value").assign(icu, defaultValues[newValue]["inputContainerUsed.experimentProperties.trisHCLConcentration.value.model"]);
						$parse("experimentProperties.masterEPXVolume.value").assign(icu,      defaultValues[newValue]["inputContainerUsed.experimentProperties.masterEPXVolume.value"]);

						// -2 calculs
						var inputConc = $parse("concentration.value")(icu);
						var finalConc = $parse("experimentProperties.finalConcentration2.value")(icu);
						//var finalVol= defaultValues[newValue]["outputContainerUsed.experimentProperties.finalVolume.value"]; // !! paraissait logique
						var finalVol = $parse("outputContainerUseds[0].experimentProperties.finalVolume.value")(atm); // !!!!! atm    copier dans l'ancien code
						var NaOHVol = $parse("experimentProperties.NaOHVolume.value")(icu);
						var masterEPXVol =$parse("experimentProperties.masterEPXVolume.value")(icu);

						// et le cas ou inputConc=0 ???
						var engagedVol= finalConc * finalVol / inputConc;
						engagedVol = Math.round(engagedVol*100.0)/100.0;
						var RSBVol = finalVol - masterEPXVol - NaOHVol  - engagedVol;

						console.log ('sequencingType: engaged Vol='+ finalConc + '*' + finalVol+ '/' + inputConc +'='+ engagedVol );
						console.log ('sequencingType: RSB Vol='+ finalVol + '-' + masterEPXVol + '-' + NaOHVol + '-' + engagedVol +'='+ RSBVol);

						// 3-assignation des calculs
						$parse("experimentProperties.inputVolume2.value").assign(icu, engagedVol);
						$parse("experimentProperties.RSBVolume.value").assign(icu, RSBVol);

					} else {
						//old code
						// -1 assignation
						$parse("experimentProperties.inputVolume2.value").assign(icu,         defaultValues[newValue]["inputContainerUsed.experimentProperties.inputVolume2.value"]);
						$parse("experimentProperties.NaOHVolume.value").assign(icu,           defaultValues[newValue]["inputContainerUsed.experimentProperties.NaOHVolume.value"]);
						$parse("experimentProperties.NaOHConcentration.value").assign(icu,    defaultValues[newValue]["inputContainerUsed.experimentProperties.NaOHConcentration.value"]);
						$parse("experimentProperties.trisHCLVolume.value").assign(icu,        defaultValues[newValue]["inputContainerUsed.experimentProperties.trisHCLVolume.value"]);
						$parse("experimentProperties.trisHCLConcentration.value").assign(icu, defaultValues[newValue]["inputContainerUsed.experimentProperties.trisHCLConcentration.value.model"]);
						$parse("experimentProperties.masterEPXVolume.value").assign(icu,      defaultValues[newValue]["inputContainerUsed.experimentProperties.masterEPXVolume.value"]);
					
						// -2 calcul
						var engagedVol=$parse("experimentProperties.inputVolume2.value")(icu);
						var inputConc=$parse("concentration.value")(icu);
						var finalVol=$parse("outputContainerUseds[0].experimentProperties.finalVolume.value")(atm); // !!!!! atm
						var finalConc=inputConc * engagedVol / finalVol;  // finalVol n'est jamais null a ce niveau
						finalConc = Math.round(finalConc*100.0)/100.0;

						console.log ('finalConc='+ inputConc+ '*'+ engagedVol+'/'+ finalVol+'='+finalConc);
					   
						// -3 assignation du calcul
						$parse("experimentProperties.finalConcentration2.value").assign(icu, finalConc);	
						
					}
				} else {
					//select "Sequencing type" remis a undefined 

					$parse("experimentProperties.inputVolume2.value").assign(icu, undefined);
					$parse("experimentProperties.NaOHVolume.value").assign(icu, undefined);
					$parse("experimentProperties.NaOHConcentration.value").assign(icu, undefined);
					$parse("experimentProperties.trisHCLVolume.value").assign(icu, undefined);
					$parse("experimentProperties.trisHCLConcentration.value").assign(icu, undefined);
					$parse("experimentProperties.masterEPXVolume.value").assign(icu, undefined);
			
					// raz systematique!!
					$parse("experimentProperties.finalConcentration2.value").assign(icu, undefined);

					// NGL-4421 
					//if ( newValues.match(/NovaSeq Xplus/) ) {    if peut pas marcher si select remis aundefined !!!
						//raz volume RSB ( n'existait pas avant NGL-4421 )
						$parse("experimentProperties.RSBVolume.value").assign(icu, undefined);
					//}
				}
			});	
		});
		
		$scope.atmService.data.updateDatatable();
	}
	
	//6- instrument type NGL-2397
	/* jamais declenché !!! voir aussi tubes-to-flowcell-ctrl.js
	$scope.$watch("experiment.instrument.typeCode" , function(newValue, oldValue){
		if (newValue && newValue !== oldValue ){
		//if (newValue){
			console.log("6/watch-------typeCode="+newValue+"---------");
			checkAll();
		}
	})
	*/

	//*7-  15/09/2023 NGL-4246: Controle de cohérence des processus des containers
	//     seuls les processus ayant prepa-fc-ordered dans leur experimentTypes sont autorisés
    if ( $scope.isCreationMode() ){
        // A faire uniquement  s'il y a au moins un pool dans le basket [ contents[] > 1]
		var allowedTypeCodes=[];
        var poolInBasket=false;
        poolInBasket= $scope.getBasket().get().some(function(input) {
			// un pool est un input avec plus d'un content 
            return input.contents.length > 1;
        });

        console.log('poolInBasket ? '+ poolInBasket)
        if (poolInBasket ){
			checkProcessTypeCodes("prepa-fc-ordered");
			//checkProcessTypeCodes("TEST");
        }
    }
	
	// 30/01/2019 tous les checks doivent etre faite ensembles !!
	function checkAll(){

		$scope.messages.clear();
		//NGL-4731 passer les alertes en "rouge"
		//$scope.messages.clazz = "alert alert-warning";
		$scope.messages.clazz = "alert alert-danger";
		$scope.messages.text = "Alertes de configuration";
		$scope.data = { Alertes:[]} 
		
		// pour eviter un empilement de messages ne pas faire les autres controles si isAllowedOnBoard est false
		if ( isAllowedOnBoard() ){ 
			checkNovaSeqInstrument();
			checkXPSequencing();
			checkNbLaneCompat();
			//checkFcPattern (); // mettre en dernier ; NGL-4730 ne plus faire ce controle !!
		}
		
		if ( $scope.data.Alertes.length > 0 ){
			$scope.messages.showDetails = true;
			$scope.messages.open();
		}
	}
	
	function setAlert(msgKey, msgDetails){
		$scope.data[msgKey].push(msgDetails);
		$scope.messages.setDetails($scope.data);
	}
	
	// 18/12/2017 NGL-1754 : restreindre instrument a MarieCurix-A ou MarieCurix-B quand le type de sequencage choisi est NovaSeq 6000
	// 18/11/2019 NGL-2752 ajout /SuperNova/=> creation isInstrumentNovaSeq6000()
	function checkNovaSeqInstrument(){	
		console.log("checkNovaSeqInstrument...");
		// tout tester en interne
		if ( undefined==$scope.experiment.experimentProperties||
			 undefined==$scope.experiment.experimentProperties.sequencingType.value||
			 undefined==$scope.experiment.instrument.code) { return; }
		
		sequencingType=$scope.experiment.experimentProperties.sequencingType.value;
		instrumentCode=$scope.experiment.instrument.code;
		
		console.log("checkNovaSeqInstrument..."+sequencingType+ "/"+instrumentCode);
		
		//18/11/2019 NGL-2752 utiliser isInstrumentNovaSeq6000()
		if ( ! sequencingType.match(/NovaSeq 6000/) && isInstrumentNovaSeq6000()){
			setAlert("Alertes","Le type de séquencage doit etre 'NovaSeq 6000 / *' pour un NovaSeq 6000");	
		} 
		
		//ajouter la verification inverse;
		//18/11/2019 NGL-2752 utiliser isInstrumentNovaSeq6000()
		if ( sequencingType.match(/NovaSeq 6000/) && ! isInstrumentNovaSeq6000()){
			setAlert("Alertes","L'instrument choisi n'est pas compatible avec un séquençage sur NovaSeq 6000");
		} 

		//11/10/2023 NGL-4319 NovaSeq Xplus
		if ( ! sequencingType.match(/NovaSeq Xplus/) && isInstrumentNovaSeqXplus()){
			setAlert("Alertes","Le type de séquencage doit etre 'NovaSeq Xplus / *' pour un NovaSeq Xplus");	
		} 
		
		if ( sequencingType.match(/NovaSeq Xplus/) && ! isInstrumentNovaSeqXplus()){
			setAlert("Alertes","L'instrument choisi n'est pas compatible avec un séquençage sur NovaSeq Xplus");
		} 
	}
	
	// NGL-2397 les mode sequencage XP ne sont possibles que si l'instrument est cBot-onBoard+MarieCurix OU novaseq-xp-fc-dock
	// 18/11/2019 NGL-2752 utiliser isInstrumentNovaSeq6000()
	function checkXPSequencing(){
		console.log( "checkXPSequencing...");
		if ( undefined==$scope.experiment.experimentProperties||
		     undefined==$scope.experiment.experimentProperties.sequencingType.value  ||
		     undefined==$scope.experiment.instrument.code ) { return; }
		
		sequencingType=$scope.experiment.experimentProperties.sequencingType.value;
		instrumentCode=$scope.experiment.instrument.code;
		
		console.log( "checkXPSequencing..."+sequencingType +"/"+instrumentCode)
		
		// 18/11/2019 utilisation isInstrumentNovaSeq6000()
		if ( sequencingType.match(/XP/) && ! isInstrumentNovaSeq6000()){
			setAlert("Alertes","Le mode XP n'est possible qu'avec un NovaSeq 6000 (cBot-onboard ou Novaseq Xp Flow Cell Dock).");
		}
		
		//06/02/2019 ajout verification inverse: pour Novaseq-XP-fc-dock le sequencing type DOIT etre XP (vu avec C. Besse)
		/* 11/02/2019 J. Guy ne veut pas de ce message
		if ( instrumentCode.match(/novaseq-xp-fc-dock/) && ! sequencingType.match(/XP/) ){
			setAlert("Alertes","Le type de séquencage doit etre du type NovaSeq 6000 / * / XP.");
		}
		*/
	}
	
	// NGL-4730 obsolete !!
	function checkFcPattern (){	
		console.log("check FC pattern...");
		if ( undefined==$scope.experiment.experimentProperties || 
			 undefined==$scope.experiment.experimentProperties.sequencingType.value ||
			 undefined==$scope.experiment.instrumentProperties ||
			 undefined==$scope.experiment.instrumentProperties.containerSupportCode ||
			 undefined==$scope.experiment.instrumentProperties.containerSupportCode.value ){ return; }
		
		fcBarcode=$scope.experiment.instrumentProperties.containerSupportCode.value;
		sequencingType=$scope.experiment.experimentProperties.sequencingType.value;
		
		console.log('check FC pattern: FC='+ fcBarcode + ' sequencing type='+sequencingType );
		
		// 04/05/2018 NGL-2028: modification par Illumina du pattern des FC = *BB*
		// var H4000fcRegexp= /^[A-Za-z0-9]*BBXX$/;
		var H4000fcRegexp= /^[A-Za-z0-9]*BB[A-Za-z0-9]{2}$/;
		
		// 19/06/2018 NGL-2112: modification par Illumina du pattern des FC HiseqX
		//var HXfcRegexp= /^[A-Za-z0-9]*ALXX$/;
		// 17/12/2018 NGL-2364 Illumina va remplacer les barcodes CCXY par CCX2==> gérer les 2 (jusqu'a quand ??)
		var HXfcRegexp= /^[A-Za-z0-9]*CCX[Y2]$/;
		
		// 28/01/2022 renommage Nv6000SPS1fcRegexp
		var Nv6000SPS1fcRegexp= /^[A-Za-z0-9]*DRXY$/; // DRXX 30/07/2018 => DRXY 28/01/2022 (NGL-3698)
		var Nv6000S2fcRegexp= /^[A-Za-z0-9]*DMXY$/;   // DMXX 25/01/2018 => DMXY 28/01/2022 (NGL-3698)
		var Nv6000S4fcRegexp= /^[A-Za-z0-9]*DSX3$/;   // info Illumina 25/01/2018; 19/06/2020 NGL-2989 => DSXY; 
													  // 29/03/2021 NGL-3315 => DSX2; 28/01/2022 NGL-3698 => DSX3 uniquement
		
		if ((sequencingType === "Hiseq 4000") && ( null == fcBarcode.match(H4000fcRegexp))) {
			setAlert("Alertes", "Le Code Flowcell n'est pas du type 'Hiseq 4000' (*BB--).");
		
		} else if ((sequencingType === "Hiseq X") && ( null == fcBarcode.match(HXfcRegexp))) {
			setAlert("Alertes", "Le Code Flowcell n'est pas du type 'Hiseq X' (*CCXY ou *CCX2).");	
		}
		// NGL-2219 ajout NovaSeq 6000 / S* / XP
		// NGL-2624 renommage "S1" => "S1-SP" 
		else if (((sequencingType === "NovaSeq 6000 / S1-SP")||
			      (sequencingType === "NovaSeq 6000 / S1-SP / XP")) &&  ( null == fcBarcode.match(Nv6000SPS1fcRegexp))) {
			setAlert("Alertes","Le Code Flowcell n'est pas du type 'NovaSeq 6000 / S1-SP' (*DRXY)");
					   
		} else if (((sequencingType === "NovaSeq 6000 / S2")||
				    (sequencingType === "NovaSeq 6000 / S2 / XP")) && (null == fcBarcode.match(Nv6000S2fcRegexp))) {
			setAlert("Alertes","Le Code Flowcell n'est pas du type 'NovaSeq 6000 / S2' (*DMXY).");
			
		} else if (((sequencingType === "NovaSeq 6000 / S4")||
			        (sequencingType === "NovaSeq 6000 / S4 / XP")) && (null == fcBarcode.match(Nv6000S4fcRegexp))) {
			setAlert("Alertes","Le Code Flowcell n'est pas du type 'NovaSeq 6000 / S4' (*DSX3).");
		}
		// les specs des flowcells pour NovaSeqXPlus ne sont pas encore connues...
	}
	
	// NG-2397 verification sequencingType VS outContainerSupportCategoryCode
	function checkNbLaneCompat(){
		console.log("checkNbLaneCompat...");
		if ( undefined==$scope.experiment.experimentProperties || 
		     undefined==$scope.experiment.experimentProperties.sequencingType.value ||
		     undefined==$scope.experiment.instrument.outContainerSupportCategoryCode) { return; }
		
		sequencingType=$scope.experiment.experimentProperties.sequencingType.value
		outContainerSupportCategoryCode=$scope.experiment.instrument.outContainerSupportCategoryCode
		
		console.log("checkNbLaneCompat..."+sequencingType+"/"+outContainerSupportCategoryCode)
		
		//NovaSeq 6000 / SP => similaire a S1 donc fait dans S1-SP
		//NGL-2624 renommage "S1" =>"S1-SP"
		if        (((sequencingType === "Hiseq 4000") || (sequencingType === "Hiseq X"))
		    		&& ( outContainerSupportCategoryCode != "flowcell-8")) {
			setAlert("Alertes", "Le type de séquencage "+ sequencingType +" nécessite une flowcell 8 pistes.");
			
		} else if (((sequencingType === "NovaSeq 6000 / S1-SP")||(sequencingType === "NovaSeq 6000 / S1-SP / XP")||
				    (sequencingType === "NovaSeq 6000 / S2")   ||(sequencingType === "NovaSeq 6000 / S2 / XP")) 
					&& (outContainerSupportCategoryCode != "flowcell-2")){ 
			setAlert("Alertes","Le type de séquencage "+ sequencingType +" nécessite une flowcell 2 pistes.");
		
		} else if (((sequencingType === "NovaSeq 6000 / S4")||(sequencingType === "NovaSeq 6000 / S4 / XP")) 
					&& (outContainerSupportCategoryCode != "flowcell-4")){
			setAlert("Alertes","Le type de séquencage "+ sequencingType +" nécessite une flowcell 4 pistes.");
		}
		// NGL-4723 arrivée de FC 1.5B
		else if (((sequencingType === "NovaSeq Xplus / 25B")||(sequencingType === "NovaSeq Xplus / 10B")) 
				&& (outContainerSupportCategoryCode != "flowcell-8") ){
			setAlert("Alertes","Le type de séquencage "+ sequencingType +" nécessite une flowcell 8 pistes.");
		}
		else if ((sequencingType === "NovaSeq Xplus / 1.5B") 
				&& (outContainerSupportCategoryCode != "flowcell-2") ){
			setAlert("Alertes","Le type de séquencage "+ sequencingType +" nécessite une flowcell 2 pistes.");
		}
	}
	
	function isAllowedOnBoard(){
		// 19/11/2019 ajout controle pour le type d'instrument cBot-onboard qui en fait n'est pas un type d'instrument mais plutot un mode de fonctionnement
		// puisqu'il existe pour Hiseq, Miseq, NextSeq et NovaSeq !!!!!!
		console.log("isAllowedOnBoard...");
		
		if ( undefined==$scope.experiment.instrument.typeCode ||
		     undefined==$scope.experiment.instrument.code) { return; }
		
		instrumentTypecode=$scope.experiment.instrument.typeCode;
		instrumentCode=$scope.experiment.instrument.code;
		console.log("isAllowedOnBoard:"+ instrumentTypecode+"/"+ instrumentCode);
		
		if ( instrumentTypecode === "cBot-onboard"  ){
			//  les NovaSeq6000 sont autorisés !!! NGL-4319 les NovaSeqXplus aussi 
			if (! isInstrumentNovaSeq6000() && ! isInstrumentNovaSeqXplus()){
				setAlert("Alertes","Pour le type d'instrument cBot-onboard, seuls les cBot internes NovaSeq 6000 ou NovaSeq Xplus sont autorisés.");
				return false;
				//NB il faut une regle drools pour generer une erreur si l'utilisateur valide malgres cette alerte
			}
		}
		return true;
	}
	
	//18/11/2019 NGL-2752 ajout SuperNova => pour prévoir l'arrivée d'autres instruments NovaSeq créer fonction dédiée
	//02/01/2010 NGL-2785 ajout BossaNova
	function isInstrumentNovaSeq6000(){
		var instrumentCode=$scope.experiment.instrument.code;
		
		if (instrumentCode && ( instrumentCode.match(/MarieCurix/) || 
								instrumentCode.match(/SuperNova/)  ||
								instrumentCode.match(/BossaNova/)  ||
								instrumentCode.match(/novaseq-xp-fc-dock/)) ){
			return true;
		} else 
			return false;
	}

	// 11/10/2023 NGL-4319 ajout NovaSeqXplus
	function isInstrumentNovaSeqXplus(){
		var instrumentCode=$scope.experiment.instrument.code;
		// 24/09/2024 correction bug: manquait RayonX créé en février 2024!!!
		if (instrumentCode && ( instrumentCode.match(/JoliotCurix/) ||
								instrumentCode.match(/RayonX/)) ){
			return true;
		} else 
			return false;
	}
	
	// ajout 25/04/2017 NGL-1287: les supports d'entree ne doivent etre QUE des plaques pour le Janus
	if ( $scope.isCreationMode() && ($scope.experiment.instrument.typeCode === 'janus-and-cBotV2')){
		// !! en mode creation $scope.experiment.atomicTransfertMethod n'est pas encore chargé=> passer par Basket (ajouter mainService dans le controller)
		// $parse marche pas ici.... var tmp = $scope.$parse("getBasket().get()|getArray:'support.categoryCode'|unique",mainService); 
		var categoryCodes = $scope.$eval("getBasket().get()|getArray:'support.categoryCode'|unique",mainService);
		var supports = $scope.$eval("getBasket().get()|getArray:'support.code'|unique",mainService);
		
		if ( ((categoryCodes.length === 1) && ( categoryCodes[0] ==="tube")) || (categoryCodes.length > 1) ){
			                          // only tubes                                      mixte
			$scope.messages.setError(Messages('experiments.input.error.only-plates')+ ' si vous utilisez cet instrument.'); 
			
			$scope.experiment.instrument.typeCode =null;
			$scope.atmService = null; //===> empeche la page de se charger...
			$scope.experimentTypeTemplate = null;
		} else {
			// plaques uniqt mais il y a une limite !! 09/08/2017 NGL-1550: passage a 8 sources pour le Janus
			if ( supports.length > 8 ){ 
				$scope.messages.setError(Messages('experiments.input.error.maxSupports', 8));
				$scope.atmService = null; //empeche la page de se charger...
				$scope.experimentTypeTemplate = null;
			}
		}
	}

	//  15/09/2023 NGL-4246: Controle de cohérence des processus des containers
	//  seuls les processus ayant prepa-fc-ordered dans leur experimentTypes sont autorisés
	function checkProcessConsistancy (){

		var alertTypeCodes=[];
		var alertCodes=[];
		//console.log (allowedTypeCodes);

		//-1 liste des processTypesCode dans l'expérience courante
		var inputProcessTypeCodes=$scope.$eval("getBasket().get()|flatArray:'processTypeCodes'|unique",mainService);
		//console.log (inputProcessTypeCodes);

		//-2 vérifier s'il y en a qui ne sont pas autorisés (pas dans la liste des autorisés )
		//   s'ils n'ont pas le droit d'être là, les mettre dans la liste des warning
		inputProcessTypeCodes.forEach(function(processTypeCode) {
			if (! allowedTypeCodes.includes(processTypeCode) ) {
				//console.log ( processTypeCode + ' not allowed');
		        alertTypeCodes.push(processTypeCode);
		    } /*else {
				console.log ( processTypeCode + ' allowed');
			}*/
		});

		if (alertTypeCodes.length > 0 ){
			//-3 afficher les process correspondants aux types non autorisés
		    console.log ( alertTypeCodes.length + ' inconsistant processes found:');
		    var inputProcessCodes=$scope.$eval("getBasket().get()|flatArray:'processCodes'",mainService);
		
	        alertTypeCodes.forEach (function (type){
		            console.log(type.toUpperCase());
		            inputProcessCodes.forEach(function(processCode) {
	                	//console.log('>'+processCode);
	                    // si un processCode contient (en majusule le type inconsistant le mettre dans la liste)
	                   	if ( processCode.includes(type.toUpperCase()) ) {
		                   alertCodes.push(processCode);
	                	}
              		});
		    });
		
	    	$scope.messages.setWarning("ATTENTION, les processus associés aux containers de cette expérience ne sont pas cohérents pour enchaîner la suite des expériences. Contactez l'équipe NGL.");
		    $scope.messages.setDetails( {'Processus': alertCodes});
		    $scope.messages.showDetails = true;
        }
	}

	function getProcessTypeListByExpType(expTypeCode){
		var url = jsRoutes.controllers.processes.api.ProcessTypes.list().url+"?experimentTypes="+expTypeCode;
		return $http.get(url)
	}

	function checkProcessTypeCodes(expTypeCode) {
		var allowedTypeCodes=[];
		getProcessTypeListByExpType(expTypeCode)
			.then( function(response){
				console.log('ProcessTypeslist récupérée');
				getAllowedTypeCodes(response.data);
			})
			.then( checkProcessConsistancy );	
	}

	/*  GARDER COMME EXEMPLE DE PARSING DE REPONSE ( l'ancienne URL retournait tout !!)
	function getAllowedTypeCodes (data, experimentCode){
		console.log('building allowedTypeCodes = have '+experimentCode);
		data.forEach(function(processType){
			//console.log ('--'+processType.code+'--');
			if ( processType.experimentTypes.some(function(expType) {
				return expType.experimentTypeCode === experimentCode
				//return expType.experimentTypeCode === "TEST"
			}) ){ 
				allowedTypeCodes.push(processType.code);
			}
		});
	}
	*/

	function getAllowedTypeCodes (data){
		data.forEach(function(processType){
				allowedTypeCodes.push(processType.code);
		});
	}

	//------------ 16/01/2018 : NGL-1767 modification dynamique de la feuille de calcul en fonction du mode de sequencage-------
	//             03/07/2018 2eme version avec utilisation du code CNS
	//             04/01/2023 NGL-4421 : nouveau mode de calcul ajouté=> computeEngagedVolume
	//               !! bug ancien: si le type de sequencage est défini avant la composition de flowcell=> pb... a resoudre !!!
	
	//appelé quand le datatable de "composition Flowcell" est modifié depuis l'entete
	// surcharger celle de tubes-to-flowcell-ctrl.js  pour qu'elle appelle la changeValueOnFlowcellDesign locale qui utilise un parametre	
	$scope.updateAllOutputContainerProperty = function(property){
		console.log('updateAllOutputContainerProperty !!!! ' + property.code);	
		
		var value = $scope.outputContainerValues[property.code];
		var setter = $parse("outputContainerUseds[0].experimentProperties."+property.code+".value").assign;
		
		for(var i = 0 ; i < $scope.atmService.data.atm.length ; i++){
			var atm = $scope.atmService.data.atm[i];
			if(atm.inputContainerUseds.length > 0){
				setter(atm, value);
				// NGL-4421 passer property.code en parametre pour debug
				$scope.changeValueOnFlowcellDesign(i+1, property.code );
			}
		}
	};
	
	//appelé quand le datatable de "composition Flowcell" est modifié depuis un ATM !!
	// surcharger celle de tubes-to-flowcell-ctrl.js pour declencher les calculs
	// appellée depuis le scala.html et $scope.updateAllOutputContainerProperty (ci dessus )
	$scope.changeValueOnFlowcellDesign = function(l, propertyCode){ //l=atm.line si appel depuis scala.html
		// seuls "% a déposer", "volume final" "% PhiX" sont modifiables
		// si on arrive depuis un ATM on n'a pas propertyCode !!!!
		console.log('>>>>>colonne '+ propertyCode +' modifiée  line: '+ l );

		// seul "volume final" induit un recalcul, mais dans certain cas on ne l'a pas...
		//if ( propertyCode == 'finalVolume'){
			if (undefined  !== $scope.experiment.experimentProperties && undefined  !== $scope.experiment.experimentProperties.sequencingType ){
				var sequencingType=$scope.experiment.experimentProperties.sequencingType.value;

				if ( sequencingType.match(/NovaSeq Xplus/) ){
					// recalculer le volume engagé pour tous les inputContainerUsed de l'atm [l-1] 
					for ( var j=0; j < $scope.atmService.data.atm[l-1].inputContainerUseds.length; j++ ){
						console.log('(1) changeValueOnFlowcellDesign for input container :'+ j);
						computeEngagedVolumeAtm($scope.atmService.data.atm[l-1], l-1 , j);	
					}
				} else {
					//old code
					// recalculer la concentration finale pour tous les inputContainerUsed de l'atm [l-1] 
					for ( var j=0; j < $scope.atmService.data.atm[l-1].inputContainerUseds.length; j++ ){
						console.log('(2) changeValueOnFlowcellDesign for input container :'+ j);
						computeFinalConcentrationAtm($scope.atmService.data.atm[l-1], l-1 , j);	
					}
				}
			} else {
				console.log(' sequencingType undefined on ne peut pas refaire les calculs !!');
			}
		//}

		$scope.atmService.data.updateDatatable(); 
	};	
	
	// Utilisé qd la feuille de calcul est modifiée
	$scope.updatePropertyFromUDT = function(value, col){
		// NGL-4421 tester le sequencing type !!!
		var sequencingType=$scope.experiment.experimentProperties.sequencingType.value;
		if (sequencingType){
			if (sequencingType.match(/NovaSeq Xplus/) ){
				if ( col.property === 'inputContainerUsed.experimentProperties.finalConcentration2.value'){ 
					// console.log('propriéte concentration finale modifiée !!!'  );
					// recalculer le volume engagé  (value.data ne contient qu'une seule ligne du datatable ??? )
					computeEngagedVolume(value.data);
				}
			} else {
				//Old code
				if ( col.property === 'inputContainerUsed.experimentProperties.inputVolume2.value'){ 
					// console.log('propriéte volume engagé modifiée !!!');
					// recalculer la concentration finale  (value.data ne contient qu'une seule ligne du datatable ??? )
		   	 		computeFinalConcentration(value.data);
				}
			}
		} // rien a calculer !!
	}
	// NGL-4421 calculer le volume engagé ET le volume RSB d'une ligne datatable
	var computeEngagedVolume = function(udtData){	
		console.log(">>>computeEngagedVolume ... (udtData)");
		var getterEngagedVolume2=$parse("inputContainerUsed.experimentProperties.inputVolume2.value");
		var getterRSBVolume=$parse("inputContainerUsed.experimentProperties.RSBVolume.value");

		var compute = {
		    finalConc :    $parse("inputContainerUsed.experimentProperties.finalConcentration2.value")(udtData),	// input !!!
			finalVol:      $parse("outputContainerUsed.experimentProperties.finalVolume.value")(udtData), 
			inputConc:     $parse("inputContainerUsed.concentration.value")(udtData),	
			NaOHVol :      $parse("inputContainerUsed.experimentProperties.NaOHVolume.value")(udtData),
			masterEPXVol : $parse("inputContainerUsed.experimentProperties.masterEPXVolume.value")(udtData),

			isReady:function(){
				// !!input conc doit imperativement etre != 0 sinon div by 0; 
				return (this.finalVol && this.finalConc && this.inputConc);
			}
		};

		if(compute.isReady()){
			//-1 calcul engagedVol
			// la difference d'unités des concentrations finalConc (pM) et inputConc (nM) sont automatiquement prises en charge
			var engagedVol= compute.finalConc * compute.finalVol / compute.inputConc;
			// arrondir...
			if(angular.isNumber(engagedVol) && !isNaN(engagedVol)){
				engagedVol = Math.round(engagedVol*100.0)/100.0;
			}
			console.log('engagedVol :'+  compute.finalConc +'*'+  compute.finalVol +'/'+  compute.inputConc+'='+ engagedVol);
			getterEngagedVolume2.assign(udtData, engagedVol);

			//-2 calcul RSBVol
			var RSBVol = compute.finalVol - compute.masterEPXVol - compute.NaOHVol  - engagedVol;
			console.log('RSBVol :'+  compute.finalVol +'-'+  compute.masterEPXVol +'-'+  compute.NaOHVol +'-'+ engagedVol +'='+ RSBVol);
			getterRSBVolume.assign(udtData, RSBVol);

		}else{
			console.log("computeEngagedVolume : Impossible de calculer volume engagé et volume RSB: valeurs manquantes");
			getterEngagedVolume2.assign(udtData, undefined);
			getterRSBVolume.assign(udtData, undefines);
		}
	}	

	// calculer la concentration finale d'une ligne datatable
	var computeFinalConcentration = function(udtData){	
		console.log(">>>computeConcentration ... (udtData)");
		var getterFinalConcentration2=$parse("inputContainerUsed.experimentProperties.finalConcentration2.value");
		
		var compute = {
		    inputConc : $parse("inputContainerUsed.concentration.value")(udtData),	
			engagedVol: $parse("inputContainerUsed.experimentProperties.inputVolume2.value")(udtData), 
			finalVol:   $parse("outputContainerUsed.experimentProperties.finalVolume.value")(udtData), 
			
			isReady:function(){
				// !! final volume doit imperativement etre != 0 sinon div by 0; engagedVol doit aussi etre !=0
				//console.log('computeFinalConcentration: inputConc='+ this.inputConc +'  engagedVol='+ this.engagedVol +'  finalVol='+ this.finalVol);
				return (this.finalVol && this.engagedVol);
			}
		};
		
		if(compute.isReady()){
			var finalConcentration= compute.inputConc * compute.engagedVol / compute.finalVol;
			// arrondir...
			if(angular.isNumber(finalConcentration) && !isNaN(finalConcentration)){
				finalConcentration = Math.round(finalConcentration*100.0)/100.0;
			}
			//console.log('finalConcentration :'+  compute.inputConc +'*'+  compute.engagedVol +'/'+  compute.finalVol+'='+ finalConcentration);
			getterFinalConcentration2.assign(udtData, finalConcentration);
			
		}else{
			console.log("computeFinalConcentration : Impossible de calculer la concentration finale: valeurs manquantes");
			getterFinalConcentration2.assign(udtData, undefined);
		}
	}

	// version pour les cas ou on modifie seulement l'input container j de l'atm i (onglet Composition Flowcell)
	// Note 21/12/2023 : seule la modification du volume final induit un recalcul, or le volume final est sensé etre imposé par le sequencing type
	var computeFinalConcentrationAtm = function(atm, i, j){
		// note: i ne sert que pour debug...
		console.log(">>>computeFinalConcentrationAtm  atm "+ i +".inputContainerUsed:"+ j);
		var getterFinalConcentration2=$parse("inputContainerUseds["+ j +"].experimentProperties.finalConcentration2.value");
			
	    //SUPSQCNG-611/ NGLSQ-2190: pb engagedVol undefined...car les experimentProperties  des inputContainers sont undefined qd on passe ici!!! => setter inputVolume2 !!
		if (undefined  !== $scope.experiment.experimentProperties && undefined  !== $scope.experiment.experimentProperties.sequencingType ){
		      var sequencingType=$scope.experiment.experimentProperties.sequencingType.value;
		      if ( undefined  !== defaultValues[sequencingType]["inputContainerUsed.experimentProperties.inputVolume2.value"]) {
		        $parse("inputContainerUseds["+ j +"].experimentProperties.inputVolume2.value").assign(atm, defaultValues[sequencingType]["inputContainerUsed.experimentProperties.inputVolume2.value"]);
		      }
	    }
	
		var compute = {
				inputConc : $parse("inputContainerUseds["+ j +"].concentration.value")(atm),
				engagedVol: $parse("inputContainerUseds["+ j +"].experimentProperties.inputVolume2.value")(atm), 
				finalVol:   $parse("outputContainerUseds[0].experimentProperties.finalVolume.value")(atm),	
				
				isReady:function(){
					// !! final volume doit imperativement etre != 0 sinon div by 0
					// console.log('inputConc='+ this.inputConc +' engagedVol='+ this.engagedVol +' finalVol='+ this.finalVol);
					return (this.finalVol && this.engagedVol );
				}
		};
		
		if(compute.isReady()){
			var finalConcentration = compute.inputConc * compute.engagedVol / compute.finalVol;
			// arrondir...
			if(angular.isNumber(finalConcentration) && !isNaN(finalConcentration)){
				finalConcentration = Math.round(finalConcentration*100.0)/100.0;	
			}
			console.log('finalConcentration='+ compute.inputConc +'*'+ compute.engagedVol +'/'+  compute.finalVol +'='+ finalConcentration);
			getterFinalConcentration2.assign(atm, finalConcentration);
		} else {
			console.log("computeConcentrationAtm: Impossible de calculer la concentration finale: valeurs manquantes");
			getterFinalConcentration2.assign(atm, undefined);
		}
	}

	// version pour les cas ou on modifie seulement l'input container j de l'atm i (onglet Composition Flowcell)
	var computeEngagedVolumeAtm = function(atm, i, j){
		// note: i ne sert que pour debug...
		console.log(">>>computeEngagedVolumeAtm  atm "+ i +".inputContainerUsed:"+ j);
		var getterEngagedVolume2=$parse("inputContainerUseds["+ j +"].experimentProperties.inputVolume2.value");
		var getterRSBVolume=$parse("inputContainerUseds["+ j +"].experimentProperties.RSBVolume.value");

    	// !! les experimentProperties  des inputContainers sont undefined qd on passe ici!!! => setter inputVolume2 !!
		if (undefined  !== $scope.experiment.experimentProperties && undefined  !== $scope.experiment.experimentProperties.sequencingType ){
			var sequencingType=$scope.experiment.experimentProperties.sequencingType.value;
			if ( undefined  !== defaultValues[sequencingType]["inputContainerUsed.experimentProperties.finalConcentration2.value"]) {
				console.log(" !!!!!!!!!!!!!!!!");
		 		$parse("inputContainerUseds["+ j +"].experimentProperties.finalConcentration2.value").assign(atm, defaultValues[sequencingType]["inputContainerUsed.experimentProperties.finalConcentration2.value"]);
			}
  		}

		var compute = {
		    finalConc :   $parse("inputContainerUseds["+ j +"].experimentProperties.finalConcentration2.value")(atm),	
			inputConc :   $parse("inputContainerUseds["+ j +"].concentration.value")(atm),
			NaOHVol:      $parse("inputContainerUseds["+ j +"].experimentProperties.NaOHVolume.value")(atm),	
			masterEPXVol: $parse("inputContainerUseds["+ j +"].experimentProperties.masterEPXVolume.value")(atm),	
			finalVol:     $parse("outputContainerUseds[0].experimentProperties.finalVolume.value")(atm),	

			isReady:function(){
				// !!input conc doit imperativement etre != 0 sinon div by 0; 
				console.log('computeEngagedVolumeAtm: inputConc='+ this.inputConc +' finalConc='+ this.finalConc +'  finalVol='+ this.finalVol);
				return (this.finalVol && this.finalConc && this.inputConc);
			}
		};
		
		if(compute.isReady()){
			// -1 calcul engagedVol
			// la difference d'unités des concentrations finalConc (pM) et inputConc (nM) sont automatiquement prises en charge
			var engagedVol= compute.finalConc * compute.finalVol / compute.inputConc;

			// arrondir...
			if(angular.isNumber(engagedVol) && !isNaN(engagedVol)){
				engagedVol = Math.round(engagedVol*100.0)/100.0;
			}
			console.log('engagedVol :'+  compute.finalConc +'*'+  compute.finalVol +'/'+  compute.inputConc+'='+ engagedVol);
			getterEngagedVolume2.assign(atm, engagedVol);

			// !-2 calcul RSBVol
			var RSBVol = compute.finalVol - compute.masterEPXVol - compute.NaOHVol  - engagedVol;
			console.log('RSBVol :'+  compute.finalVol +'-'+  compute.masterEPXVol +'-'+  compute.NaOHVol +'-'+ engagedVol +'='+ RSBVol);
			getterRSBVolume.assign(atm, RSBVol);
		}else{
			console.log("computeEngagedVolumeAtm : Impossible de calculer volume engagé et volume RSB: valeurs manquantes");
			getterEngagedVolume2.assign(atm, undefined);
			getterRSBVolume.assign(atm, undefined);
		}
	}	

}]);