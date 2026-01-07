// 01/07/2025 copie pour V2
angular.module('home').controller('PrepSeqPlatePacbioV2Ctrl',['$scope', '$parse', 'atmToGenerateMany',
                                                               function($scope, $parse, atmToGenerateMany) {
		
	var inputExtraHeaders=Messages("experiments.inputs");
	var outputExtraHeaders=Messages("experiments.outputs");	
				
	// datatable de parametrage
	// 03/07/2025 positionnement de plusieurs parametres a false pour alleger l'affichage ( et eviter des problemes ?)
	var datatableConfigTubeParam = {
		name: $scope.experiment.typeCode+'_PARAM'.toUpperCase(),
		columns:[
				{
			     "header":Messages("containers.table.code"),
			     "property":"inputContainer.code",
			     "order":true,
				 "edit": false, 
				 "hide": true,
			     "type":"text",
			     "position":1,
			     "extraHeaders":{0:inputExtraHeaders}
			    },	
			    {
			     "header":Messages("containers.table.outputNumber.pacbio"),
			     "property":"outputNumber",
			     "order":false,
				 "edit":true,
				 "hide":false,
			     "type":"number",						
			     "position":2,
			     "extraHeaders":{0:inputExtraHeaders}
			    }
		],
		compact:true,
		showTotalNumberRecords:false,
		pagination:{
			active:false
		},		
		search:{
			active:false
		},
		order:{
			mode:'local',
			active:true, // laisser le tri actif !!! mais le bouton de tri sur "property":"inputContainer.code" ne marche pas !!! pourquoi ???
			by:'inputContainer.code'
		},
		remove:{
			/// => false
			//active: ($scope.isEditModeAvailable() && $scope.isNewState()),
			//showButton: ($scope.isEditModeAvailable() && $scope.isNewState()),
			active: false,
			showButton: false,
			mode:'local'
		},
		save:{
			active: true, /// true nécessaire meme sans le bouton !!!!!!
			withoutEdit: true,
			keepEdit:true,
			changeClass : false,
			mode:'local',
			showButton:false   
		},			
		select:{
			active: false /// => false
		},
		edit:{
			active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('IP')),
			///showButton: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('IP')),=> false
			showButton: false, 
			byDefault:($scope.isCreationMode()),
			columnMode:true
		},
		cancel : {
			active: false /// ==>false
		},
		extraHeaders:{
			number:1,
			dynamic:true,
		}
	};	
	
	var datatableConfigTubeConfig =  {	
			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[   
					//--------------------- INPUT containers section -----------------------
					 {
			        	 "header":Messages("containers.table.code"),
			        	 "property":"inputContainer.code",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "mergeCells" : true,
			        	 "position":1,
			        	 "extraHeaders":{0:inputExtraHeaders}
			         },		         
			         {
			        	"header":Messages("containers.table.projectCodes"),
			 			"property": "inputContainer.projectCodes",
			 			"order":true,
			 			"hide":true,
			 			"type":"text",
			 			"mergeCells" : true,
			 			"position":2,
			 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	"extraHeaders":{0:inputExtraHeaders}
				     },
				     {
			        	"header":Messages("containers.table.sampleCodes"),
			 			"property": "inputContainer.sampleCodes",
			 			"order":true,
			 			"hide":true,
			 			"type":"text",
			 			"mergeCells" : true,
			 			"position":3,
			 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	"extraHeaders":{0:inputExtraHeaders}
				     },
				     {
			        	 "header":Messages("containers.table.fromTransformationTypeCodes"),
			        	 "property":"inputContainer.fromTransformationTypeCodes",
			        	 "filter" : "unique | codes:'type'",
			        	 "order":false,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "mergeCells" : true,
			 			 "render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	 "position":4,
			        	 "extraHeaders":{0:inputExtraHeaders}
			         },
					 {
						"header":Messages("containers.table.tags"),
						 "property": "inputContainer.contents",
						 "filter": "getArray:'properties.tag.value'| unique",
						 "order":false,
						 "hide":true,
						 "edit":false,
						 "type":"text",
						 "position":5,
						 "render":"<div list-resize='cellValue' list-resize-min-size='3'>",
						 "extraHeaders":{0:inputExtraHeaders}
					 },
					 {
						"header":Messages("containers.table.size")+ " (pb)",
						"property": "inputContainerUsed.size.value",
						"order" : true,
						"edit" : false,
						"hide" : true,
						"type" : "number",
						"position" :6,
						"extraHeaders" : { 0 : inputExtraHeaders }
					 },
					 {
						"header":Messages("containers.table.concentration") + " (ng/µL)",
						"property":"inputContainerUsed.concentration.value",
						"order":true,
						"edit":false,
						"hide":true,
						"type":"number",
						"format" : "2", // 2 décimales
						"position":7,
						"extraHeaders":{0:inputExtraHeaders}
					 },
					 { // calcul conc en nm=conc/(660 x taille)* 1000000
						// fait ici pour être affiché au plus tot sans nécessiter d'action utilisateur
						"header":Messages("containers.table.concentration")+ " (nM)",
						"property": "inputContainerUsed.concentration.value / (660 * inputContainerUsed.size.value) * 1000000",
						"order" : true,
						"edit" : false,
						"hide" : true,
						"type" : "number",
						"format" : "2", // 2 décimales
						"position" :8,
						"extraHeaders" : { 0 : inputExtraHeaders }
					 },
					 //--->  colonnes specifiques experience s'inserent ici  (inputContainerUsed)
					 // Concentration On Plate
					 // vol Engagé
					 // vol final
					 // conc finale calculée
			         {
			        	 "header":Messages("containers.table.state.code"),
			        	 "property":"inputContainer.state.code",
			        	 "order":false,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "filter":"codes:'state'",
			        	 "position":14,
			        	 "extraHeaders":{0:inputExtraHeaders}
			         },
			         
			        //------------------------- OUTPUT containers section --------------------------
			         
			        //--->  colonnes specifiques experience s'inserent ici  (outputContainerUsed)
					// ID plaque seq
					// position plaque Seq
					{
						"header" : Messages("containers.table.supportCode"),
						"property" : "outputContainerUsed.locationOnContainerSupport.code",
						"order" : false,
						"edit" : false, // Exceptionnellement au CNRGH ici il faut laisser NGL générer le code support 
						"hide" : true,
						"type" : "text",
						"position" : 105,
						"extraHeaders" : {0:outputExtraHeaders}
					},
					{
						"header" : Messages("containers.table.support.line"),
						"property" : "outputContainerUsed.locationOnContainerSupport.line",
						"order" : false,
						"edit" : false,  
						"hide" : true,
						"type" : "text",
						"position" : 106,
						"extraHeaders" : {0:outputExtraHeaders}
					},
					{
						"header" : Messages("containers.table.support.column"),
						"property" : "outputContainerUsed.locationOnContainerSupport.column",
						"order" : false,
						"edit" : false,
						"hide" : true,
						"type" : "number",
						"position" : 107,
						"extraHeaders" : {0:outputExtraHeaders}
					},
			    	{
			        	 "header":Messages("containers.table.stateCode"),
			        	 "property":"outputContainer.state.code | codes:'state'",
			        	 "order":false,
						 "edit":false,
						 "hide":true,
						 "type":"text",
			        	 "position":500,
			        	 "extraHeaders":{0:outputExtraHeaders}
			        },
			        {
			        	 "header":Messages("containers.table.storageCode"),
			        	 "property":"outputContainerUsed.locationOnContainerSupport.storageCode",
			        	 "order":false,
						 "edit":true,
						 "hide":true,
			        	 "type":"text",
			        	 "position":600,
			        	 "extraHeaders":{0:outputExtraHeaders}
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
				by:'inputContainer.code'
			},
			remove:{
				active:false,
			},
			save:{
				active:true,
				withoutEdit: true,
				mode:'local',
				showButton:false,
				changeClass:false
			},
			hide:{
				active:true
			},
			mergeCells:{
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
				number:1,
				dynamic:true,
			}
	};	
	
	// copier les positionOnSeqPlate dans le/les outputs containrs...
	var copyPlatePositions = function(experiment) {
		experiment.atomicTransfertMethods.forEach(copyAtomicTransfertMethodPlatePositions);

		function copyAtomicTransfertMethodPlatePositions(atomicTransfertMethod) {
			atomicTransfertMethod.outputContainerUseds.forEach(copyOutputContainerUsedPlatePositions);
		}

		function copyOutputContainerUsedPlatePositions(outputContainerUsed) {
			if(!isValidPositionOnSeqPlate(outputContainerUsed.experimentProperties)) return;
			var positionOnSeqPlate = outputContainerUsed.experimentProperties.positionOnSeqPlate.value;
			var line = positionOnSeqPlate.charAt(0);
			var column = parseInt(positionOnSeqPlate.substring(1));
			outputContainerUsed.locationOnContainerSupport.line = line;
			outputContainerUsed.locationOnContainerSupport.column = column;
		}

		// les 4 positions possibles étant choisies dans une drop down, le test est un peu superflu !!!
		//    a garder si on veut pouvoir interdire des positions ???
		function isValidPositionOnSeqPlate(experimentProperties){
			return experimentProperties && experimentProperties.positionOnSeqPlate && experimentProperties.positionOnSeqPlate.value && experimentProperties.positionOnSeqPlate.value.match(/[A-Z]\d\d/g);
		}
	};

	var updateATM = function(experiment){
		if(experiment.instrument.outContainerSupportCategoryCode !== "tube"){
			experiment.atomicTransfertMethods.forEach(function(atm){
				atm.line = atm.outputContainerUseds[0].locationOnContainerSupport.line;
				atm.column = atm.outputContainerUseds[0].locationOnContainerSupport.column;
			});
		}		
	};

	// tous les changements dans les proporiétés du datatable déclenchent cette fonction !!!
	$scope.updatePropertyFromUDT = function(value, col) {
		console.log("update from property : "+col.property);
		var udtData = value.data;

		if ((col.property === 'inputContainerUsed.experimentProperties.inputVolume.value')||
		    (col.property === 'inputContainerUsed.experimentProperties.finalVolume.value'))
		{
			// recalcul concentration finale
			computeFinalConc(udtData);
		}

		// verifier si l'utilisateur a entré un doublon
		if ( (col.property === 'outputContainerUsed.experimentProperties.positionOnSeqPlate.value')||
		     (col.property === 'outputContainerUsed.experimentProperties.seqPlate.value') ){

			// quelle est la ligne du datatable (?) en cours d'edition ?
			var editLine=value.line.id;
			//console.log('line='+editLine);

			var plate=$parse ("outputContainerUsed.experimentProperties.seqPlate.value")(udtData);
			var pos=$parse ("outputContainerUsed.experimentProperties.positionOnSeqPlate.value")(udtData);
			//console.log( 'plate : '+ plate);
			//console.log( 'pos : '+ pos);

			// ne verifier que s'il y a les 2 valeurs
			if ( pos && plate) {
				checkDuplicatePos(editLine, plate, pos);
			}
		}
	};
	
	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event save");
		$scope.atmService.viewToExperiment($scope.experiment);
		copyPlatePositions($scope.experiment);
		updateATM($scope.experiment);

		//  copier les propriétés d'expérience finalVolume / finalConcentration dans container de sortie
		copyExperimentPropertiesToContainerOut($scope.experiment);

		// verifier s'il a des doublons de postion
		if ( mapHasDuplicates(posMap) ){ 
			// ici on n'a pas le détails des doublons...
			$scope.messages.setError("Doublon(s) de position sur une/des plaque(s) de séquençage.");
			// bloquer le save
			$scope.$emit('childSavedError', callbackFunction);
		} else {
			$scope.$emit('childSaved', callbackFunction);
		}
	});
	
	
	$scope.$on('refresh', function(e) {
		console.log("call event refresh");
		
		var dtConfig = $scope.atmService.data.datatableParam.getConfig();
		dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isNewState());
		dtConfig.remove.active = ($scope.isEditModeAvailable() && $scope.isNewState());
		dtConfig.edit.byDefault = false;
		$scope.atmService.data.datatableParam.setConfig(dtConfig);
		
		dtConfig = $scope.atmService.data.datatableConfig.getConfig();
		dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));
		dtConfig.edit.showButton = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));
		dtConfig.edit.byDefault = false;
		$scope.atmService.data.datatableConfig.setConfig(dtConfig);
		
		$scope.atmService.refreshViewFromExperiment($scope.experiment);
		$scope.$emit('viewRefeshed');
	});
	
	$scope.$on('cancel', function(e) {
		console.log("call event cancel");
		$scope.atmService.data.datatableParam.cancel();
		$scope.atmService.data.datatableConfig.cancel();
				
		if($scope.isCreationMode()){
			var dtConfig = $scope.atmService.data.datatableParam.getConfig();
			dtConfig.edit.byDefault = false;
			$scope.atmService.data.datatableParam.setConfig(dtConfig);
			
			dtConfig = $scope.atmService.data.datatableConfig.getConfig();
			dtConfig.edit.byDefault = false;
			$scope.atmService.data.datatableConfig.setConfig(dtConfig);
		}
	});
	
	$scope.$on('activeEditMode', function(e) {
		console.log("call event activeEditMode");
		$scope.atmService.data.datatableParam.selectAll(true);
		$scope.atmService.data.datatableParam.setEdit();
		
		$scope.atmService.data.datatableConfig.selectAll(true);
		$scope.atmService.data.datatableConfig.setEdit();
	});
	
	// forcer "96-well-plate" qq soit le choix utilisateur !!!
	$scope.$watch("experiment.instrument.outContainerSupportCategoryCode", function(){
		$scope.experiment.instrument.outContainerSupportCategoryCode = "96-well-plate";
	});

	var atmService = atmToGenerateMany($scope, datatableConfigTubeParam, datatableConfigTubeConfig);

	//defined new atomictransfertMethod
	atmService.newAtomicTransfertMethod = function(){
		return {
			class:"OneToMany",				
			inputContainerUseds:new Array(0), 
			outputContainerUseds:new Array(0)
		};		
	};
	
	//defined default output unit
	atmService.defaultOutputUnit = {
			volume : "µL",
			quantity:"ng"
	}

	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	$scope.atmService = atmService;

	// caculer concentration nM  et 
	// calculer final concentration pM= inputVolume*concentration nM/finalVolume*1000
	var computeFinalConc = function(udtData) {
		console.log(">>>>>> computeFinalConc");
		var getter= $parse("inputContainerUsed.experimentProperties.finalConcentration.value");

		var compute = {
				conc : $parse ("inputContainerUsed.concentration.value")(udtData),
				size : $parse ("inputContainerUsed.size.value")(udtData),
				unit : $parse ("inputContainerUsed.size.unit")(udtData),
				volumeEngage: $parse("inputContainerUsed.experimentProperties.inputVolume.value")(udtData),
				volumeFinal: $parse("inputContainerUsed.experimentProperties.finalVolume.value")(udtData), 

				isReady:function() {
					return (this.conc && this.size && (this.unit === "pb") && this.volumeEngage && this.volumeFinal);
				}
		};
		
		if (compute.isReady()){
			// calcul intermédiare: calculer la concentration en nM (on ne peut repartir de la valeur déja caculée dans datatableConfigTubeConfig )
			var concNm= $parse("conc / ( 660 * size ) * 1000000")(compute);
			console.log ('concNm='+concNm);

			// calculer la concentration finale en pM 
			//  NON la prorpiété est définie avec saveMeasureUNit=nM  et displayMeasureUnit=pM
			//=> l'affichage converti lui meme nM=>mP
			// donc il faut calculer en nM !!!
			//var concPm= $parse("volumeEngage * "+ concNm +" / volumeFinal  * 1000 ")(compute);
			var conc= $parse("volumeEngage * "+ concNm +" / volumeFinal ")(compute);
			if (angular.isNumber(conc) && !isNaN(conc)){
				finalConc = Math.round(conc*100)/100;  
			} else {
				finalConc = undefined;
			}
			
			getter.assign(udtData, finalConc); 
			// on assigne donc une valeur en nM et elle s'affichera en pM !!
			console.log ('assign outputContainerUsed.experimentProperties.finalConcentration.value='+finalConc);

		} else{
			getter.assign(udtData,undefined);
			console.log("Valeurs manquantes pour calcul de finalConcentration");
		}			
	}

	var copyExperimentPropertiesToContainerOut = function(experiment){	
		console.log(">>copyExperimentPropertiesToContainerOut");

		for(var i = 0 ; i < experiment.atomicTransfertMethods.length;  i++){
			var atm = experiment.atomicTransfertMethods[i];

			// !! finalConcentration et finalVolume sont des propriétés des inputContainers !!!
			for(var j=0 ; j < atm.inputContainerUseds.length ; j++){	
				var icu = atm.inputContainerUseds[j]; 

				if (icu.experimentProperties !== null &&
					icu.experimentProperties.finalConcentration !== undefined &&
					icu.experimentProperties.finalConcentration.value !== null ){
						var finalConcentrationVal = icu.experimentProperties.finalConcentration.value;
						//utiliser le meme index j ??
						console.log("assign finalConcentration "+ finalConcentrationVal +" => outputContainerUseds["+j+"].concentration.value");
						$parse("outputContainerUseds["+j+"].concentration.value").assign(atm, finalConcentrationVal);
						// unit est defini dans defaultOutputUnit ??
				}

				if (icu.experimentProperties !== null &&
					icu.experimentProperties.finalVolume !== undefined &&
					icu.experimentProperties.finalVolume.value !== null ){
						var finalVolumeVal = icu.experimentProperties.finalVolume.value;
						//utiliser le meme index j ??
						console.log("assign finalVolume "+ finalVolumeVal +" => outputContainerUseds["+j+"].volume.value");
						$parse("outputContainerUseds["+j+"].volume.value").assign(atm, finalVolumeVal);
						// unit est defini dans defaultOutputUnit ??
				}

				// et ABCconcentration ?? elle est de niveau content donc se propage toute seule???
			
			}
		}		
	}

	var posMap = new Map();

	// pour une experience en cours/nouveau (IP) il faut peupler le map avec les valeur deja definies par l'utilisateur
	// et non pas partir d'un map vide
	//////if ( ! $scope.isCreationMode() ){  
	///// if ( $scope.isNewState());

	function initMap(map){
		console.log( '>>>>initMap');
		//console.log('experiment:' + $scope.experiment.atomicTransfertMethods.length); // pas defini a l'état in progress 
		//console.log('atmservice :' +$scope.atmService.data.atm.length);
	
		var line=-1; // il faut retomber sur 0-->N pour être cohérent avec editLine dans updatePropertyFromUDT
		for(var i = 0 ; i < $scope.experiment.atomicTransfertMethods.length;  i++){
			var atm = $scope.experiment.atomicTransfertMethods[i];

			for(var j=0 ; j < atm.outputContainerUseds.length ; j++){	
				var ocu = atm.outputContainerUseds[j]; 
				//console.log ('ocu'+j);
				line++;

				if (ocu.experimentProperties !== null &&
					ocu.experimentProperties.seqPlate !== undefined &&
					ocu.experimentProperties.seqPlate.value !== null ){
						var plate = ocu.experimentProperties.seqPlate.value;
				}
				if (ocu.experimentProperties !== null &&
					ocu.experimentProperties.positionOnSeqPlate !== undefined &&
					ocu.experimentProperties.positionOnSeqPlate.value !== null ){
						var pos = ocu.experimentProperties.positionOnSeqPlate.value;
				}

				if ( plate && pos) {
					posMap.set(line, plate+pos);
					console.log ('set in map '+line +'=>'+ plate+pos);
				} else {
					// ne doit pas arriver en IP car les 2 valeurs sont obligatoires
					console.log ( 'line '+line +'empty ????');
				}
			}
		}
	}

	// pour mise a jour d'une valeur dans le datatable
	var checkDuplicatePos= function(line, plate,pos){
		console.log('>>>>checkDuplicatePos')
		$scope.messages.clear();

		if  ( posMap.has(line) ){
			//Dans le cas de la modification d'une ligne deja existante, supprimer l'ancienne valeur
			//console.log('edit line'+ line);
			posMap.delete(line);
		}
		// stocker la sélection de l'utilisateur
		console.log('set in map '+line+ ":"+ plate+pos)
		posMap.set(line, plate+pos);

		// verififier si doublons dans posMap
		if ( mapHasDuplicates(posMap) ){
			console.log (plate+pos+' already in set => ERROR');
			
			$scope.messages.clazz="alert alert-danger";
			$scope.messages.text="Doublon de position sur une plaque de séquençage"; // titre de l'alerte

			$scope.alert = {"Erreur de position":[] }; // pourrait stocker plusieurs messages
			$scope.alert['Erreur de position'].push("Position '"+ pos + "' déjà définie pour la plaque '"+ plate+ '"');
			$scope.messages.setDetails($scope.alert);
			$scope.messages.showDetails = true;
			$scope.messages.open();
		} 
	}

	// aussi appelée au save pour re-verifier et bloquer le save ( ne donne pas de détails!!)
	var mapHasDuplicates= function(map){

		//1 get all values and keys in arrays
		var values=map.values(); 
		var keys=map.keys();

		//2 convert arrays to a set to remove duplicates
		var uniqueValues = Array.from(new Set(values));
		var uniqueKeys = Array.from(new Set(keys));
		
		if (uniqueValues.length < uniqueKeys.length ) {
			return true;
		} else { 
			return false;
		}
	}

	initMap(posMap);

}]);