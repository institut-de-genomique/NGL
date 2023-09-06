// FDS 08/20/2018 NGL-2272: duplication du controleur du CNS
angular.module('home').controller('CNGAliquotingCtrl',['$scope', '$parse', 'atmToGenerateMany',
                                                               function($scope, $parse, atmToGenerateMany) {
	
    // table 1 : choisir le nombre d'aliquots en sortie
	var datatableConfigTubeParam = {
			columns:[   
					 {
			        	 "header":Messages("containers.table.code"),
			        	 "property":"inputContainer.code",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":1,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },	
			         {
			        	 "header":Messages("containers.table.outputNumber"),
			        	 "property":"outputNumber",
			        	 "order":false,
						 "edit":true,
						 "hide":false,
			        	 "type":"number",			// PAS POSSIBLE DE METTRE UN MAX ????			
			        	 "position":2,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
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
				active:true,
				by:'inputContainer.code'
			},
			remove:{
				active: false
			},
			save:{    //necessaire
				active:true, 
				withoutEdit: true,
				keepEdit:true,
				changeClass  : false,
				mode:'local',
				showButton:false
			},			
			select:{
				active:($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('IP'))
			},
			edit:{   // n'existe qu'en mode creation!!!
				active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('IP')),
				showButton: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('IP')),
				byDefault:($scope.isCreationMode()),
				columnMode:true
				
			},	
			cancel : {     //// PAS DE SENS !!! mettre a false !!!
				active:true
			},
			extraHeaders:{
				number:1,
				dynamic:true,
			}, 
			/* si on l' active on a aussi les messages OK....ce qu'on ne veut pas :-(		 
			   pas trouvé comment generer un erreur
			 messages:{ 
				active: true,
				errorClass:'alert alert-error',
				successClass: 'alert alert-success',
				errorKey:{save:'ERROR FDS'},
				successKey:{save:'OK'},
				text:undefined,
				clazz:undefined,
				transformKey : function(key, args){
					return Messages(key, args);
				}
			}*/
	};	
	
	// table 2: aliquots a creer
	// NGL-1055: mettre getArray et codes:'' dans filter et pas dans render
	// NGL-1055: name explicite pour fichier CSV exporté: typeCode experience
	var datatableConfigTubeConfig =  {
			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[   			          
			         {
			        	"header":Messages("containers.table.projectCodes"),
			 			"property": "inputContainer.projectCodes",
			 			"order":true,
			 			"hide":true,
			 			"type":"text",
			 			"mergeCells" : true,
			 			"position":2,
			 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			            "extraHeaders":{0:Messages("experiments.inputs")}
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
			        	"extraHeaders":{0:Messages("experiments.inputs")}
				     },
				     {
			        	 "header":Messages("containers.table.fromTransformationTypeCodes"),
			        	 "property":"inputContainer.fromTransformationTypeCodes",
			        	 "filter":"unique | codes:'type'",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "mergeCells" : true,
			 			 "render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	 "position":4,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			         // NGL-2700 séparer l'unité de la valeur
			         {
			        	 "header":Messages("containers.table.concentration"),
			        	 "property":"inputContainerUsed.concentration.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":5,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			         {
			        	 "header":Messages("containers.table.concentration.unit"),
			        	 "property":"inputContainerUsed.concentration.unit",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"textr",
			        	 "position":5.5,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			         {
			        	 "header":Messages("containers.table.volume") + " (µL)",
			        	 "property":"inputContainerUsed.volume.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":6,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			         {
			        	 "header":Messages("containers.table.quantity"),
			        	 "property":"inputContainerUsed.quantity.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":7,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
					 {
						"header":Messages("containers.table.quantity.unit"),
						"property":"inputContainerUsed.quantity.unit",
						"order":true,
						"edit":false,
						"hide":true,
						"type":"text",
						"position":8,
						"extraHeaders":{0:Messages("experiments.inputs")}
					},
			         {
			        	 "header":Messages("containers.table.state.code"),
			        	 "property":"inputContainer.state.code",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "filter":"codes:'state'",
			        	 "position":9,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },			        
			         {
			        	 "header":Messages("containers.table.volume")+" (µL)",
			        	 "property":"outputContainerUsed.volume.value",
			        	 "editDirectives":" udt-change='updatePropertyFromUDT(value,col)' ",
			        	 "order":true,
						 "edit":true,
						 "hide":true,
						 "type":"number",
			        	 "position":51,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },
			         {
			        	 "header":Messages("containers.table.quantity"),
			        	 "property":"outputContainerUsed.quantity.value",
						 "editDirectives":" udt-change='updatePropertyFromUDT(value,col)' ",
			        	 "order":true,
						 "edit":true,
						 "hide":true,
						 "type":"number",
			        	 "position":52,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },
					 {
						"header":Messages("containers.table.quantity.unit"),
						"property":"outputContainerUsed.quantity.unit",
						"order":true,
						"edit":true,
						"hide":true,
						"type":"text",
						"position":53,
						"watch":true,
						"choiceInList": true,
						"listStyle":"bt-select",
						"possibleValues":[{code:'fmol',name:'fmol'},{code:'ng',name:'ng'}],
						"extraHeaders":{0:Messages("experiments.outputs")}
					},
			         {
			        	 "header":Messages("containers.table.stateCode"),
			        	 "property":"outputContainer.state.code | codes:'state'",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
						 "type":"text",
			        	 "position":500,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },
			         {
			        	 "header":Messages("containers.table.comments"),
			        	 "property":"outputContainerUsed.comment.comment",
			        	 "order":false,
						 "edit":true,
						 "hide":true,
			        	 "type":"textarea",
			        	 "position":590,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },    
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
			},
			otherButtons: {
                active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
                complex:true,
                template:''
                	+$scope.plateUtils.templates.buttonLineMode('atmService.data.datatableConfig')
                	+$scope.plateUtils.templates.buttonColumnMode('atmService.data.datatableConfig')
			}
	};	
	
	// colonnes specifiques IN
	if($scope.experiment.instrument.inContainerSupportCategoryCode !== "tube"){
		datatableConfigTubeConfig.columns.push({
			"header" : Messages("containers.table.supportCode"),
			"property" : "inputContainer.code",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "text",
			"position" : 1,
			"mergeCells" : true,
			"extraHeaders" : {0 : Messages("experiments.inputs")}
		});
	} else {
		datatableConfigTubeConfig.columns.push({
			"header" : Messages("containers.table.code"),
			"property" : "inputContainer.support.code",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "text",
			"position" : 1,
			"mergeCells" : true,
			"extraHeaders" : {0 : Messages("experiments.inputs")}
		});
		
		datatableConfigTubeConfig.order.by = 'inputContainer.sampleCodes';
	}
	
	// colonnes specifiques OUT
	if($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube") {
		datatableConfigTubeConfig.columns.push({
			// barcode plaque sortie == support Container used code... faut Used
			"header" : Messages("containers.table.support.name"),
			"property" : "outputContainerUsed.locationOnContainerSupport.code",
			"edit" : true,  // difference avec CNS !!!!
			"hide" : true,
			"type" : "text",
			"position" : 400,
			"extraHeaders" : {0 : Messages("experiments.outputs")}
		});
		
		datatableConfigTubeConfig.columns.push({
			// Ligne
 			"header" : Messages("containers.table.support.line"),
 			"property" : "outputContainerUsed.locationOnContainerSupport.line",
 			"edit" : true,
 			"choiceInList":true,
 			"possibleValues":[{"name":'A',"code":"A"},{"name":'B',"code":"B"},{"name":'C',"code":"C"},{"name":'D',"code":"D"},
 			                  {"name":'E',"code":"E"},{"name":'F',"code":"F"},{"name":'G',"code":"G"},{"name":'H',"code":"H"}],
 			"order" : true,
 			"hide" : true,
 			"type" : "text",
 			"position" : 401,
 			"extraHeaders" : {0 : Messages("experiments.outputs")}
		});
		
		datatableConfigTubeConfig.columns.push({
			// colonne
 			"header" : Messages("containers.table.support.column"),
 			"property" : "outputContainerUsed.locationOnContainerSupport.column",
 			"edit" : true,
 			"choiceInList":true,
 			"possibleValues":[{"name":'1',"code":"1"},{"name":'2',"code":"2"},{"name":'3',"code":"3"},{"name":'4',"code":"4"},
 			                  {"name":'5',"code":"5"},{"name":'6',"code":"6"},{"name":'7',"code":"7"},{"name":'8',"code":"8"},
 			                  {"name":'9',"code":"9"},{"name":'10',"code":"10"},{"name":'11',"code":"11"},{"name":'12',"code":"12"}], 
 			"order" : true,
 			"hide" : true,
 			"type" : "number",
 			"position" : 402,
 			"extraHeaders" : {0 : Messages("experiments.outputs")}
		});

	} else {
		// GA: meme pour les tubes utiliser  x.locationOnContainerSupport.code  et pas x.code
		datatableConfigTubeConfig.columns.push({
			//// barcode tube sortie ... faut Used
			"header" : Messages("containers.table.code"),
			/////"property" : "outputContainerUsed.code",      /// C'EST ICI QUE SE JOUE L'ECRASEMENT DU CODE DONNE PAR L'UTILISATEUR
			"property" : "outputContainerUsed.locationOnContainerSupport.code", 
			"order" : true,
			"edit" : true, // difference avec CNS !!!!
			"hide" : true,
			"type" : "text",
			"position" : 400,
			"extraHeaders" : {0 : Messages("experiments.outputs")}
		});		
	}

	$scope.generateATM=function(datatable) {
    	// N'appeller atmService.generateATM() que si les controles sont OK
    	// (au CNS, atmService.generateATM() est appellee directement dans onClick du scala)
		
		console.log('generateATM ???')
		
		$scope.messages.clear(); //effacer possibles messages precedents
		//forcer save sinon icu.outputNumber est undefined
		datatable.save();
		
		var error=0;
		
		/* 22/10/2018 NON, Julie demande qu'il n'y ait pas de limite...
		var maxAliquots=10; // nbre d'aliquot raisonnable...
		var nbElements=0;
		var dataMain = datatable.getData();	
		dataMain.forEach(function(icu){
			if ( icu.outputNumber > maxAliquots ) { 
				 console.log('nb aliquots demandés > '+ maxAliquots); 
				 $scope.messages.setError("Nombre d'aliquots demandé > 10");		 
				 error=1;
			}
			nbElements=nbElements+ icu.outputNumber;
		});
		//console.log('total output='+nbElements);
		*/
		
		/* 23/10/2018 ici ca depend du nombre de plaques en sortie...
		 * 1) si on decide qu'il ne peut y avoir qu'une seule plaque le controle a un sens
		 *    mais il faudrait alors bloquer dans le datatable les champs de saisie et ajouter une ligne pour la saisie du barcode de LA plaque
		 * 2) si on décide de laisser la possibilité de créer plusieurs plaques il faut enlever le controle.
		
		if (($scope.experiment.instrument.outContainerSupportCategoryCode === "96-well-plate") && ( nbElements > 96) ){
			//en  mode plaque le total ne doit pas depasser le nombre de puits d'une plaque!!!!
			 console.log("le total d'aliquots > 96");
			 $scope.messages.setError("Nombre total d'aliquots > 96");	 
			 error=1;
		}
		*/
		
		if ( error === 0){  atmService.generateATM(); }
	}
	
	/**
	 * Update ???
	 */
	var updateInputVolume = function(experiment){
		for(var i=0 ; i < experiment.atomicTransfertMethods.length ; i++){
			var atm = experiment.atomicTransfertMethods[i];
			
			var volume = {input:0};
			
			angular.forEach(atm.outputContainerUseds, function(output){
				this.input += Number(output.volume.value);
			}, volume);
			
			if(angular.isNumber(volume.input)){
				$parse('inputContainerUseds[0].experimentProperties["inputVolume"]').assign(atm, {value:volume.input, unit:"µL"});
			}
			//atm.inputContainerUseds[0].experimentProperties["inputVolume"] = {value:volume.input, unit:"µL"};
		}				
	};
	
	/**
	 * Update concentration. Copy input concentration to all outputs
	 */
	var updateConcentration = function(experiment){	
		for(var j = 0 ; j < experiment.atomicTransfertMethods.length && experiment.atomicTransfertMethods != null; j++){
			var atm = experiment.atomicTransfertMethods[j];
			if(atm.inputContainerUseds[0].concentration !== null && atm.inputContainerUseds[0].concentration !== undefined){
				var concentration = atm.inputContainerUseds[0].concentration;				
				for(var i = 0 ; i < atm.outputContainerUseds.length ; i++){
					$parse("outputContainerUseds["+i+"].concentration").assign(atm, concentration);
				}
			}	
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
	
	$scope.updatePropertyFromUDT = function(value, col){
		var udtData = value.data;	
		if(isVolumeOut()){
			updateQuantityValue();			
		} else if(isQuantityOut()){
			updateQuantityUnit();			
		}

		//---

		function isVolumeOut(){
			return col.property === 'outputContainerUsed.volume.value';
		}

		function isQuantityOut(){
			return col.property === 'outputContainerUsed.quantity.value';
		}

		function getQuantity(){
			return $parse("outputContainerUsed.quantity")(udtData);
		}

		function setQuantity(quantity){
			return $parse("outputContainerUsed.quantity").assign(udtData, quantity);
		}

		function updateQuantityValue() {
			var quantity = getQuantity();
			var inputConc = getInputConcentration();
			var outputVol = getOutputVolume();
			if(isReadyToCompute()) {
				comuteQuantityValue();
			} else {
				clearQuantityValue();
			}

			//---

			function getInputConcentration(){
				return $parse("inputContainerUsed.concentration.value")(udtData);
			}

			function getOutputVolume(){
				return $parse("outputContainerUsed.volume.value")(udtData);
			}

			function isReadyToCompute() {
				return (inputConc && outputVol);
			}

			function comuteQuantityValue() {
				quantity.value = computeValue();
				setQuantity(quantity);
			}

			function clearQuantityValue() {
				quantity.value = null;
				console.log("not ready to quantity");
				setQuantity(quantity);
			}

			function computeValue() {
				var value = inputConc * outputVol;
				var roundedValue = Math.round(value*10)/10;
				return roundedValue;
			}
		}

		function updateQuantityUnit() {
			var quantity = getQuantity();
			var outputConcUnit = getQuantityUnit();
			if(outputConcUnit) doComputation();

			//---

			function getQuantityUnit(){
				return $parse("outputContainerUsed.concentration.unit")(udtData);
			}

			function doComputation() {
				quantity.unit = computeUnit();
				setQuantity(quantity);
			}

			function computeUnit() {
				return (outputConcUnit === 'nM') ? 'fmol' : 'ng';
			}
		}
		
	};
	
	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event save on aliquoting");
		
		$scope.atmService.viewToExperiment($scope.experiment);
		updateInputVolume($scope.experiment);
		updateConcentration($scope.experiment);
		
		updateATM($scope.experiment);
		
		$scope.$emit('childSaved', callbackFunction);
	});
	
	$scope.$on('refresh', function(e) {
		console.log("call event refresh on aliquoting ");
		
		var dtConfig = $scope.atmService.data.datatableParam.getConfig();
		dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isNewState());
		dtConfig.remove.active = ($scope.isEditModeAvailable() && $scope.isNewState());
		dtConfig.edit.byDefault = false;
		$scope.atmService.data.datatableParam.setConfig(dtConfig);
		
		dtConfig = $scope.atmService.data.datatableConfig.getConfig();
		dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));
		//dtConfig.remove.active = ($scope.isEditModeAvailable() && $scope.isNewState());
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
		
		$scope.atmService.data.datatableParam.selectAll($scope.isEditModeAvailable() && $scope.isNewState());
		$scope.atmService.data.datatableParam.setEdit();
		
		$scope.atmService.data.datatableConfig.selectAll(true);
		$scope.atmService.data.datatableConfig.setEdit();
	});

	//Init	
	var atmService = atmToGenerateMany($scope, datatableConfigTubeParam, datatableConfigTubeConfig);
	//defined new atomictransfertMethod
	atmService.newAtomicTransfertMethod = function(){
		return {
			class:"OneToMany",
			line:  ($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube")?undefined:"1", 
			column:($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube")?undefined:"1",				
			inputContainerUseds:new Array(0), 
			outputContainerUseds:new Array(0)
		};		
	};
	
	//defined default output unit
	atmService.defaultOutputUnit = {
			volume : "µL"
	};
	atmService.defaultOutputValue = {
			size : {copyInputContainer:true}
	};
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	$scope.atmService = atmService;
}]);