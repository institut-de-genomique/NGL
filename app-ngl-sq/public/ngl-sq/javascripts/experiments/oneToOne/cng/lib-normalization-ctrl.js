// FDS 15/02/2016 -- JIRA NGL-894 : lib-normalization experiment
angular.module('home').controller('LibNormalizationCtrl',['$scope', '$parse', '$http', 'atmToSingleDatatable',
                                                     function($scope, $parse, $http, atmToSingleDatatable){

	var inputExtraHeaders=Messages("experiments.inputs");
	var outputExtraHeaders=Messages("experiments.outputs");	
	
	// NGL-1055: name explicite pour fichier CSV exporté: typeCode experience
	// NGL-1006/NGL-2041 rendres certasine colonnes variables en fonction des category input et output
	var datatableConfig = {
			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[
			         //--------------------- INPUT containers section -----------------------       
			         { // Projet(s)
			        	"header":Messages("containers.table.projectCodes"),
			 			"property":"inputContainer.projectCodes",
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
				     { //sample Aliquots
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
			         { // libProcessType ajout 08/11/2016
					 	"header":Messages("containers.table.libProcessType"),
					 	"property": "inputContainer.contents",
					 	//"filter": "getArray:'properties.libProcessTypeCode.value'| codes:'libProcessTypeCode'",.. peut on decoder ???? 
					 	"filter": "getArray:'properties.libProcessTypeCode.value'| unique",
					 	"order":false,
					 	"hide":true,
					 	"type":"text",
					 	"position":6.5,
					 	"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
					 	"extraHeaders": {0: inputExtraHeaders}	 						 			
					 },
					 { //Tags
					    "header":Messages("containers.table.tags"),
			 			"property": "inputContainer.contents",
			 			"filter": "getArray:'properties.tag.value'| unique",
					 	"order":true,
					 	"hide":true,
					 	"type":"text",
					 	"position":7,
					 	"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
					    "extraHeaders":{0:inputExtraHeaders}
					 },
			         { // 17/11/2016 expected Coverage
				        "header":Messages("containers.table.expectedCoverage"),
				 		"property": "inputContainer.contents",
				 		"filter": "getArray:'properties.expectedCoverage.value'| unique",
				 		"order":true,
				 		"hide":true,
				 		"type":"text",
				 		"position":7.5,
				 		"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
				        "extraHeaders":{0: inputExtraHeaders}
					 },
					 { //Concentration; 12/09/2016 ne pas inclure l'unité dans le label; 08/11/2016 label court
			        	 "header":Messages("containers.table.concentration.shortLabel"), 
			        	 "property":"inputContainerUsed.concentration.value",  
			        	 "order":true,
						 "hide":true,
			        	 "type":"number",
			        	 "position":8,
			        	 "extraHeaders":{0:inputExtraHeaders}
			         },
			         { // 12/09/2016 afficher l'unité concentration dans une colonne séparée pour récupérer la vraie valeur
			        	 "header":Messages("containers.table.concentration.unit.shortLabel"),
			        	 "property":"inputContainerUsed.concentration.unit",  
			        	 "order":true,
						 "hide":true,
			        	 "type":"text",
			        	 "position":8.5,
			        	 "extraHeaders":{0:inputExtraHeaders}
			         },
			         { //Volume 
			        	 "header":Messages("containers.table.volume") + " (µL)", 
			        	 "property":"inputContainerUsed.volume.value",
			        	 "order":true,
						 "hide":true,
			        	 "type":"number",
			        	 "position":9,
			        	 "extraHeaders":{0:inputExtraHeaders}
			         },
			         { // Etat input Container
			        	 "header":Messages("containers.table.state.code"),
			        	 "property":"inputContainer.state.code | codes:'state'",
			        	 "order":true,
						 "hide":true,
			        	 "type":"text",
			        	 "position":10,
			        	 "extraHeaders":{0:inputExtraHeaders}
			         },
			         // colonnes specifiques experience viennent ici.. 
			         //   => Volume engagé, Volume tampon
			          
			         //------------------------ OUTPUT containers section -------------------
			         { // Concentration; 08/11/2016 shortLabel
			        	 "header":Messages("containers.table.concentration.shortLabel") + " (nM)",
			        	 "property":"outputContainerUsed.concentration.value",
						 "edit":true,
						 "editDirectives":"udt-change='updatePropertyFromUDT(value,col)'", // 26/07/2017 NGL-1519: ajout calculs en Javascript
						 "hide":true,
			        	 "type":"number",
			        	 "defaultValues":4,
			        	 "position":120,
			        	 "extraHeaders":{0:outputExtraHeaders}
			         },
			         { // Volume ; 26/07/2017 supression de la valeur par defaut...
			        	 "header":Messages("containers.table.volume")+ " (µL)",
			        	 "property":"outputContainerUsed.volume.value",
						 "edit":true,
						 "editDirectives":"udt-change='updatePropertyFromUDT(value,col)'",  // 26/07/2017 NGL-1519: ajout calculs en Javascript
						 "hide":true,
			        	 "type":"number",
						 //"defaultValues":15, 
			        	 "position":130,
			        	 "extraHeaders":{0:outputExtraHeaders}
			         },
			         { // Etat outpout container 
			        	 "header":Messages("containers.table.state.code"),
			        	 "property":"outputContainer.state.code | codes:'state'",
						 "hide":true,
			        	 "type":"text",
			        	 "position":160,
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
				// FDS : ce tri donne 1,10,11,12,2.... comment avoir un tri 1,2....10,11,12,13 ??
				//by:"inputContainer.support.column*1"
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
	        	mode:'local',
	        	callback:function(datatable){
	        		copyContainerSupportCodeAndStorageCodeToDT(datatable);
	        	}
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
			// ajout boutons 11/09/2018
			otherButtons: {
                active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
                complex:true,
                template:''
                	+$scope.plateUtils.templates.buttonLineMode()
                	+$scope.plateUtils.templates.buttonColumnMode()
			}
	}; // fin struct datatableConfig
	
	// 07/05/2018 NGL-1006/NGL-2041 colonnes variables
	//INPUT
	// attention: 18/10/2017 experiment.instrument.inContainerSupportCategoryCode  depend de l'ordre de selection des inputs !!!
	// => devrait etre un array et pas une var simple !!

	if ( $scope.experiment.instrument.inContainerSupportCategoryCode !== "tube" ){
		 datatableConfig.columns.push({
			// barcode plaque entree == input support Container code
	        "header":Messages("containers.table.support.name"),
	        "property":"inputContainer.support.code",
			"hide":true,
	        "type":"text",
	        "position":1,
	        "extraHeaders":{0: inputExtraHeaders}
	      });
		 datatableConfig.columns.push({
	        // Ligne
	        "header":Messages("containers.table.support.line"),
	        "property":"inputContainer.support.line",
	        "order":true,
			"hide":true,
	        "type":"text",
	        "position":2,
	        "extraHeaders":{0:inputExtraHeaders}
	     });
		 datatableConfig.columns.push({
	        // colonne
	        "header":Messages("containers.table.support.column"),
		    // astuce GA: pour pouvoir trier les colonnes dans l'ordre naturel forcer a numerique.=> type:number,   property:  *1
	        "property":"inputContainer.support.column*1",
	        "order":true,
			"hide":true,
	        "type":"number",
	        "position":3,
	        "extraHeaders":{0:inputExtraHeaders}
	     });		 
	} else {
			datatableConfig.columns.push({
				"header":Messages("containers.table.code"),
				"property":"inputContainer.support.code",
				"order":true,
				"edit":false,
				"hide":true,
				"type":"text",
				"position":1,
				"extraHeaders":{0:inputExtraHeaders}
			});
	}	
	
	// OUTPUT
	if ( $scope.experiment.instrument.outContainerSupportCategoryCode !== "tube" ){	
		 datatableConfig.columns.push({
			 // barcode plaque sortie == support Container used code... faut Used 
			 "header":Messages("containers.table.support.name"),
			 "property":"outputContainerUsed.locationOnContainerSupport.code", 
			 "order":true,
			 "hide":true,
			 "type":"text",
			 "position":100,
			 "extraHeaders":{0:outputExtraHeaders}
         });
		/// !!!! ceci est vrai pour une plaque en sortie, si c'est un strip-8 fait a la main c'est incorrect!!! il faudrait adapter !!
		 datatableConfig.columns.push({
			 // Line
        	 "header":Messages("containers.table.support.line"),
        	 "property":"outputContainerUsed.locationOnContainerSupport.line", 
 			 "edit" : true,
			 "choiceInList":true,
			 "possibleValues":[{"name":'A',"code":"A"},{"name":'B',"code":"B"},{"name":'C',"code":"C"},{"name":'D',"code":"D"},
			                   {"name":'E',"code":"E"},{"name":'F',"code":"F"},{"name":'G',"code":"G"},{"name":'H',"code":"H"}],
        	 "order":true,
			 "hide":true,
        	 "type":"text",
        	 "position":105,
        	 "extraHeaders":{0:outputExtraHeaders}
         });
		/// !!!! ceci est vrai pour une plaque en sortie, si c'est un strip-8 fait a la main c'est incorrect!!! il faudrait adapter !!
		 datatableConfig.columns.push({
			 // column
        	 "header":Messages("containers.table.support.column"),
        	 "property":"outputContainerUsed.locationOnContainerSupport.column",
 			 "edit" : true,
			 "choiceInList":true,
			 "possibleValues":[{"name":'1',"code":"1"},{"name":'2',"code":"2"},{"name":'3',"code":"3"},{"name":'4',"code":"4"},
			                   {"name":'5',"code":"5"},{"name":'6',"code":"6"},{"name":'7',"code":"7"},{"name":'8',"code":"8"},
			                   {"name":'9',"code":"9"},{"name":'10',"code":"10"},{"name":'11',"code":"11"},{"name":'12',"code":"12"}], 
        	 "order":true,
			 "hide":true,
        	 "type":"number",
        	 "position":110,
        	 "extraHeaders":{0:outputExtraHeaders}
         });		
	} else {
			// tube
		    // GA: meme pour les tubes utiliser  x.locationOnContainerSupport.code  et pas x.code
			datatableConfig.columns.push({
				"header":Messages("containers.table.code"),
				"property":"outputContainerUsed.locationOnContainerSupport.code",
				"order":true,
				"edit":true,
				"editDirectives":"udt-change='atmService.emptyToNull(value.data, col.property)'",
				"hide":true,
				"type":"text",
				"position":100,
				"extraHeaders":{0:outputExtraHeaders}
			});
			
			datatableConfig.columns.push({
				"header":Messages("containers.table.storageCode"),
				"property":"outputContainerUsed.locationOnContainerSupport.storageCode",
				"order":true,
				"edit":true,
				"hide":true,
				"type":"text",
				"position":150,
				"extraHeaders":{0:outputExtraHeaders}
			});		 
	}

	// ajout pour NGL-2225 gestion des plaques en sortie
	var updateATM = function(experiment){
		if(experiment.instrument.outContainerSupportCategoryCode !== "tube"){
			experiment.atomicTransfertMethods.forEach(function(atm){
				atm.line = atm.outputContainerUseds[0].locationOnContainerSupport.line;
				atm.column = atm.outputContainerUseds[0].locationOnContainerSupport.column;
			});
		}		
	};
	
	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event save");
			$scope.atmService.data.save();
			$scope.atmService.viewToExperimentOneToOne($scope.experiment);
		
			// ajout pour NGL-2225 gestion des plaques en sortie
			updateATM($scope.experiment);
			
			// NGL-3776 [1]
			checkWellHighBufferVolume($scope.experiment);// probleme message effacé par "sauvegarde OK"!!!
		
			$scope.$emit('childSaved', callbackFunction);
	});
	
	// Reprise du code code de oxbiseq-and-biseq pour NGL-2225 gestion des plaques en sortie
	var copyContainerSupportCodeAndStorageCodeToDT = function(datatable){

		var dataMain = datatable.getData();
		var outputContainerSupportCode = $scope.outputContainerSupport.code;
		var outputContainerSupportStorageCode = $scope.outputContainerSupport.storageCode;
		
		// 14/03/2019 correction locale pour NGl-2371: ajout && "" !=outputContainerSupportCode
		if ( null != outputContainerSupportCode && undefined != outputContainerSupportCode && "" !=outputContainerSupportCode ){
			for(var i = 0; i < dataMain.length; i++){

				if($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube"){
					$parse('outputContainerUsed.locationOnContainerSupport.code').assign(dataMain[i],outputContainerSupportCode);
				}
				
				if( (null != outputContainerSupportStorageCode) && (undefined != outputContainerSupportStorageCode)){
				    $parse('outputContainerUsed.locationOnContainerSupport.storageCode').assign(dataMain[i],outputContainerSupportStorageCode);
				}
			}
		}
	}
	
	$scope.$on('refresh', function(e) {
		console.log("call event refresh");		
		var dtConfig = $scope.atmService.data.getConfig();
		dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));// 08/01/2018 => mettre 'F' au lieu 'IP'
		//08/01/2018 dtConfig.edit.showButton manquant ???
		dtConfig.edit.byDefault = false;
		dtConfig.edit.start = false;
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
	
	// reprise du code de oxbiseq-and-biseq pour NGL-2225 gestion des plaques en sortie
	// line et column sont indefinis au demarrage pour les plaques, ce sont les valeurs definies par l'utilisateur dans le datatable qui sont
	// positionnees dans copyContainerSupportCodeAndStorageCodeToDT au momemt de la sauvegarde
	// !!! l'original est class:"OneToMany", ici il faut oneToOne
	atmService.newAtomicTransfertMethod = function(l,c){
		return {
			class:"OneToOne",
			line:($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube")?undefined:"1", 
			column:($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube")?undefined:"1",	
			inputContainerUseds:new Array(0), 
			outputContainerUseds:new Array(0)
		};		
	};
	
	//defined default output unit
	atmService.defaultOutputUnit = {
			volume : "µL",
			concentration : "nM"
	}
	// NGL-3171 copie automatique de la taille input => taille output ( value ET unit)
	// !! la taille output n'est pas affichée dans cette expérience c'est normal!
	atmService.defaultOutputValue = {
			size : {copyInputContainer:true}
	};
	
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	$scope.atmService = atmService;
	
	$scope.outputContainerSupport = { code : null , storageCode : null};	
	
	if ( undefined !== $scope.experiment.atomicTransfertMethods[0]) { 
		 $scope.outputContainerSupport.code=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.code;
		 //console.log("previous code: "+ $scope.outputContainerSupport.code);
	}
	if ( undefined !== $scope.experiment.atomicTransfertMethods[0]) {
		$scope.outputContainerSupport.storageCode=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.storageCode;
		//console.log("previous storageCode: "+ $scope.outputContainerSupport.storageCode);
	}
	
	// NGL-3776 2 feuilles de route au lieu d'une seule
	$scope.setAdditionnalButtonsLabel( Messages("experiments.sampleSheets") );  //sampleSheets  avec un S final
	$scope.setAdditionnalButtons([
		{
			isDisabled : function(){ return $scope.isNewState(); },
			isShow:function(){ return ( (!$scope.isNewState()) && ($scope.experiment.instrument.typeCode === 'janus')); },
			click: function(){ return $scope.fileUtils.generateSampleSheet({'fdrType':'standard'}); },
			label: Messages("experiments.sampleSheet") +" / standard plate"
		}
		,
		{
			isDisabled : function(){ return $scope.isNewState(); },
			isShow:function(){ return ( (!$scope.isNewState()) && ($scope.experiment.instrument.typeCode === 'janus')); },
			click: function(){ return $scope.fileUtils.generateSampleSheet({'fdrType':'highVolume'}); },
			label: Messages("experiments.sampleSheet") +" / store plate"
		}
	]);

	// 26/07/2017: remplacer les calculs de calculation.drl par du javascript...
	$scope.updatePropertyFromUDT = function(value, col){
		//console.log("update from property : "+col.property);

		if (( col.property === 'outputContainerUsed.concentration.value')||
			( col.property === 'outputContainerUsed.volume.value')
		){
			var outputConc=$parse("outputContainerUsed.concentration.value")(value.data);
			var inputConc= $parse("inputContainerUsed.concentration.value")(value.data);
			var outputVol= $parse("outputContainerUsed.volume.value")(value.data);
			
			//console.log(">>>outputContainerUsed.concentration.value="+ outputConc );
			//console.log(">>>inputContainerUsed.concentration.value="+ inputConc );
			
			// 11/10/2017 mettre ici la verification d'unite pour englober tous les cas
			var input_unit= $parse("inputContainerUsed.concentration.unit")(value.data);
			if (input_unit==='nM'){		
				// !! les cas ou la conc input est a 0 existent et font planter la generation de la feuille de route
				// => faire comme le cas conc trop forte
				if (( outputConc > inputConc) || (inputConc === 0 ))
				{
					console.log("concentration out trop forte OU concentration in nulle!!");
				
					// forcer valeurs
					$parse("inputContainerUsed.experimentProperties.bufferVolume.value").assign(value.data, 0); 
					$parse("inputContainerUsed.experimentProperties.inputVolume.value").assign(value.data, outputVol);
					$parse("outputContainerUsed.concentration.value").assign(value.data, inputConc);
				} else {
					//console.log("OK calculs");
					computeVolumes(value.data);
				}
			} else {
					console.log("Impossible de calculer les volumes: unité d'entrée n'est pas nM");
			}
		}
	}
	
	// 26/07/2017: remplacer les calculs de volumes de calculation.drl par du javascript....
	var computeVolumes = function(udtData){

		console.log("OK calculs...");
		var getterEngageVol= $parse("inputContainerUsed.experimentProperties.inputVolume.value");
		var getterBufferVol= $parse("inputContainerUsed.experimentProperties.bufferVolume.value");

		var compute = {
				inputConc :  $parse("inputContainerUsed.concentration.value")(udtData), // pas forcement dispo ( si pas de QC avant)
				outputConc:  $parse("outputContainerUsed.concentration.value")(udtData),
				outputVol:   $parse("outputContainerUsed.volume.value")(udtData),
			   
				isReady:function(){
					// attention division par 0 !
					return (this.inputConc && this.outputConc && this.outputVol);
				}
		};
		
		if(compute.isReady()){

			var engageVol=$parse("outputConc * outputVol  / inputConc")(compute);
			// arrondir...
			if(angular.isNumber(engageVol) && !isNaN(engageVol)){
				engageVol = Math.round(engageVol*10)/10;				
			}
			console.log("vol engagé = "+engageVol);
			
			var bufferVol=$parse("outputVol")(compute) - engageVol;
			// arrondir...
			if(angular.isNumber(bufferVol) && !isNaN(bufferVol)){
				bufferVol = Math.round(bufferVol*10)/10;	
			}
			console.log("vol buffer= "+ bufferVol);
			
			getterEngageVol.assign(udtData, engageVol);
			getterBufferVol.assign(udtData, bufferVol);
			
			//NGL-3776 [2]: si volume Buffer > 25 et plaque ==> setWarning
			// ajouter test instrument !== hand
			if (( bufferVol > 25) && 
				( $scope.experiment.instrument.inputContainerSupportCategoryCode !== "tube") &&
				( $scope.experiment.instrument.categoryCode !== 'hand')
			) {
				$scope.messages.setWarning("Attention, volume de tampon > 25µL. Veuillez utiliser le programme JANUS spécial gros volume (Feuille de route / store plate).");
			}
			
		}else{
			console.log("Impossible de calculer les volumes: valeurs manquantes");
			getterEngageVol.assign(udtData, undefined);
			getterBufferVol.assign(udtData, undefined);
		}
	}
	
	// NGL-3776 Vérifier si au moins un puit a un volume Buffer > 25
	// explicitement tester: janus
	var checkWellHighBufferVolume = function(experiment){
		if (( experiment.instrument.inputContainerSupportCategoryCode !== "tube" ) &&
			( $scope.experiment.instrument.typeCode === 'janus') 
			){
			$scope.messages.clear();
			experiment.atomicTransfertMethods.forEach(function(atm){
				
				if ( atm.inputContainerUseds[0].experimentProperties &&
				      atm.inputContainerUseds[0].experimentProperties.bufferVolume ){
					var bufferVol=atm.inputContainerUseds[0].experimentProperties.bufferVolume.value;

					if ( bufferVol > 25) { 
						$scope.messages.setWarning("Attention, volume de tampon > 25µL. Veuillez utiliser le programme JANUS spécial gros volume (Feuille de route / store plate).");
						return;
					}
				}
			});
		}		
	}
	
	// Ne pas afficher a terminer !!
	if ( $scope.isNewState() || $scope.isInProgressState() ){
		checkWellHighBufferVolume($scope.experiment);
	}
	
	}]);