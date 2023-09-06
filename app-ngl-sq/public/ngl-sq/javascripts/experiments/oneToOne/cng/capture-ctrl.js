// FDS 27/07/2017 -- JIRA NGL-1201. Duplication a partir de additional-normalization
angular.module('home').controller('CaptureCtrl',['$scope', '$parse', '$http', 'atmToSingleDatatable',
                                                     function($scope, $parse, $http, atmToSingleDatatable){

	var inputExtraHeaders=Messages("experiments.inputs");
	var outputExtraHeaders=Messages("experiments.outputs");	
	
	// NGL-1055: name explicite pour fichier CSV exporté: typeCode experience
	var datatableConfig = {
			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[
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
			         { // 31/08/2017 niveau process ET contents => utiliser properties et pas processProperties; 04/09/2017 si filtre codes:'value' alors header =>libProcessType
			        	 "header": Messages("containers.table.libProcessType"),
			        	 "property" : "inputContainerUsed.contents",
			        	 "filter" : "getArray:'properties.libProcessTypeCode.value' | unique | codes:'value'",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":9.2,
			        	 "extraHeaders":{0:inputExtraHeaders}
			         },      
			         { // 31/08/2017 niveau process uniquement =>  utiliser processProperties; ne fonctionne que a nouveau et en cours c'est normal !!
			        	 "header": Messages("containers.table.expectedBaits"),
			        	 "property" : "inputContainerUsed.contents",
			        	 "filter" : "getArray:'processProperties.expectedBaits.value' | unique  | codes:'value'",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":9.4,
			        	 "extraHeaders":{0:inputExtraHeaders}
			         }, 
			         { // 31/08/2017 niveau process ET contents => utiliser properties et pas processProperties
			        	 "header": Messages("containers.table.captureProtocol"),
			        	 "property" : "inputContainerUsed.contents",
			        	 "filter" : "getArray:'properties.captureProtocol.value' | unique  | codes:'value'",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":9.6,
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

			         // colonnes correspondant aux propriétés experience de niveau containerIN viennent automatiquement ici
                     //   => Volume engagé, Quantité engagée, Baits (sondes)
			          
			         //------------------------ OUTPUT containers section -------------------

		            /* ne pas aficher les containercodes sauf pour DEBUG 
			         {
			        	 "header":"[["+Messages("containers.table.code")+"]]",
			        	 "property":"outputContainerUsed.code",
			        	 "order":true,
						 "hide":true,
						 "edit":false,
			        	 "type":"text",
			        	 "position":100,
			        	 "extraHeaders":{0:"outputExtraHeaders"}
			         },*/
			         { // barcode plaque sortie
			        	 "header":Messages("containers.table.support.name"),
			        	 "property":"outputContainerUsed.locationOnContainerSupport.code", 
			        	 "order":true,
						 "hide":true,
			        	 "type":"text",
			        	 "position":100,
			        	 "extraHeaders":{0: outputExtraHeaders}
			         },
			         { // Line
			        	 "header":Messages("containers.table.support.line"),
			        	 "property":"outputContainerUsed.locationOnContainerSupport.line",
			        	 "order":true,
						 "hide":true,
			        	 "type":"text",
			        	 "position":110,
			        	 "extraHeaders":{0:outputExtraHeaders}
			         },
			         { // column
			        	 "header":Messages("containers.table.support.column"),
			        	 // astuce GA: pour pouvoir trier les colonnes dans l'ordre naturel forcer a numerique.=> type:number,   property:  *1
			        	 "property":"outputContainerUsed.locationOnContainerSupport.column*1",
			        	 "order":true,
						 "hide":true,
			        	 "type":"number",
			        	 "position":111,
			        	 "extraHeaders":{0:outputExtraHeaders}
			         },	
			         /* 30/08/2017 pas de volume ni concentration OUT
			         { // Concentration
			        	 "header":Messages("containers.table.concentration.shortLabel") + " (ng/µL)",
			        	 "property":"outputContainerUsed.concentration.value",
						 //"edit":true,
						 ////"editDirectives":"udt-change='updatePropertyFromUDT(value,col)'",
						 "hide":true,
			        	 "type":"number",
			        	 "position":120,
			        	 "extraHeaders":{0:outputExtraHeaders}
			         },
			         { // Volume 
			        	 "header":Messages("containers.table.volume")+ " (µL)",
			        	 "property":"outputContainerUsed.volume.value",
						 "edit":true,
						 "editDirectives":"udt-change='updatePropertyFromUDT(value,col)'",
						 "hide":true,
			        	 "type":"number",
			        	 "position":130,
			        	 "extraHeaders":{0:outputExtraHeaders}
			         },
			         */
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
	        		// NGL-2371 FDS 11/03/2019 copyContainerSupportCodeAndStorageCodeToDT deplacée dans atmService + ajout 2eme param "pos"
	        		// tous les instruments de cette exp n'ont que plaque en ouputContainer, inutile de le tester
	        		// plaque inputContainer => pos='auto'
	        		atmService.copyContainerSupportCodeAndStorageCodeToDT(datatable,'auto');
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
			}
	}; // fin struct datatableConfig

	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event save");
			$scope.atmService.data.save();
			$scope.atmService.viewToExperimentOneToOne($scope.experiment);
			$scope.$emit('childSaved', callbackFunction);
	});
	
	$scope.$on('refresh', function(e) {
		console.log("call event refresh");		
		var dtConfig = $scope.atmService.data.getConfig();
		//NGL-1735 correction bouton edit: F et pas IP
		dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));
		dtConfig.edit.showButton = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));
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
	
	// FDS 16/03/2018 : NGL-1906 rechercher le robotRunWorkLabel positionné au niveau processus pour le copier dans robotRunCode (sauf s'il y en plusieurs!!)
	$scope.$watch("experiment.instrument.code", function(newValue, OldValue){
		if ((newValue) && (newValue !== null ) && ( newValue !== OldValue ))  {		
			// exemple dans prepa-fc-ordered: var categoryCodes = $scope.$eval("getBasket().get()|getArray:'support.categoryCode'|unique",mainService);
			// mais ici mainService n'est pas defini, et pas necessaire...
			// obliger de passer par contents[0], mais normalement ne doit pas poser de probleme...
			var workLabels= $scope.$eval("getBasket().get()|getArray:'contents[0].processProperties.robotRunWorkLabel.value'|unique");
			if ( workLabels.length > 1 ){
				$scope.messages.clear();
				$scope.messages.clazz = "alert alert-warning";
				$scope.messages.text = "Plusieurs noms de travail (robot) trouvés parmi les containers d'entrée (info processus)";
				$scope.messages.open();			
			
				//console.log('>1  run workLabel trouvé !!');
			} else if ( workLabels.length === 1 ){
				// verifier que TOUS les containers ont une valeur...
				var contents= $scope.$eval("getBasket().get()|getArray:'contents[0]'");
				var labels= $scope.$eval("getBasket().get()|getArray:'contents[0].processProperties.robotRunWorkLabel.value'");
				if ( labels.length < contents.length ) {
					$scope.messages.clear();
					$scope.messages.clazz = "alert alert-warning";
					$scope.messages.text = "Certains containers en entrée n'ont pas de nom de travail run (robot) (info processus)";
					$scope.messages.open();			
				
					//console.log("Certains containers n'ont pas de workLabel.");
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
		
	//Init atmService
	var atmService = atmToSingleDatatable($scope, datatableConfig);
	atmService.newAtomicTransfertMethod = function(l, c){
		return {
			class:"OneToOne",
			line: l, 
			column: c, 				
			inputContainerUseds:new Array(0), 
			outputContainerUseds:new Array(0)
		};
	};
	
	//defined default output unit;  28/07/2017 modif en "ng/µL" (au lieu de nM)
	atmService.defaultOutputUnit = {
			volume : "µL",
			concentration : "ng/µL"
	}
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
	
	$scope.outputContainerSupport = { code : null , storageCode : null};	
	
	if ( undefined !== $scope.experiment.atomicTransfertMethods[0]) { 
		 $scope.outputContainerSupport.code=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.code;
		 //console.log("previous code: "+ $scope.outputContainerSupport.code);
	}
	if ( undefined !== $scope.experiment.atomicTransfertMethods[0]) {
		$scope.outputContainerSupport.storageCode=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.storageCode;
		//console.log("previous storageCode: "+ $scope.outputContainerSupport.storageCode);
	}
		
	$scope.setAdditionnalButtons([{
		isDisabled : function(){return $scope.isCreationMode();},
		// FDS 15/03/2018 SUPSQCNG-547: inutile de tester les instruments=> FDR pour tous !!!
		//isShow:function(){return (($scope.experiment.instrument.typeCode === 'bravo-workstation')||($scope.experiment.instrument.typeCode=== 'bravows-and-mastercycler-epg'))}, 
		isShow:function(){return true;},
		click: $scope.fileUtils.generateSampleSheet,
		label:Messages("experiments.sampleSheet") 
	}]);
	
	
	// FDS 28/07/2017  calculs
	/* FDS 11/2022 NGL-3812 l'expérience capture est maintenant utilisée dans le process capture XH-HS2 et les calculs sont différents:
	             dans le processus initial utilisateur saisi les qtés engagées désirées et NGL calcule les volumes engagés nécessaires
	             dans XH-HS2 l'utilisateur saisi les volumes engagés et NGL calcule les quantités correspondantes
	*/
	// appellé autant de fois qu'il y a d'ATM quand le header est modifié !!!!!!!!!!!
	$scope.updatePropertyFromUDT = function(value, col){
		//console.log("update from property : "+col.property);

		if (col.property === "inputContainerUsed.experimentProperties.computeMode.value"){

			var newComputeMode = $parse("inputContainerUsed.experimentProperties.computeMode.value")(value.data);
			//console.log("newComputeMode="+ newComputeMode);
			
			resetAll(value.data); 
			setVolQtyEditable(newComputeMode); 
		} else {

			var computeMode = $parse("inputContainerUsed.experimentProperties.computeMode.value")(value.data);

			if (computeMode === "volCalc"){ 
				// ancien process
				//console.log("calculer les volumes engagés d'après les quantités demandées")
				computeVolumes(value.data)
			}
			else if(computeMode === "qtyCalc"){
				// process XT-HS2
				//console.log("calculer les quantités engagée d'après les volumes engagés")
				computeQty(value.data);
			}
		}
	}
	
	var resetAll = function(udtData){
		//console.log( "reset volume et quantity");
		$parse("inputContainerUsed.experimentProperties.inputVolume.value").assign(udtData,null);
		$parse("inputContainerUsed.experimentProperties.inputQuantity.value").assign(udtData,null);
	};

	// FDS 28/07/2017 calculs; attention aux pb d'unité, le calcul n'est possible que si l'unité est "ng/µL"
	var computeVolumes = function(udtData){
		var getterEngagedVol=$parse("inputContainerUsed.experimentProperties.inputVolume.value");
		
		var compute = {
				inputConc :      $parse("inputContainerUsed.concentration.value")(udtData), 
				inputConcUnit:   $parse("inputContainerUsed.concentration.unit")(udtData),
				inputVol :       $parse("inputContainerUsed.volume.value")(udtData), 
				inputEngagedQty :$parse("inputContainerUsed.experimentProperties.inputQuantity.value")(udtData),

				isReady:function(){
					// traiter le cas ou la qté engagé est volontairement mise a 0
					// 06/12/2017 SUPSQCNG-505 traiter le cas ou concentration =0 (eau !!)
					return ((this.inputConc||this.inputConc===0) && 
					        (this.inputConcUnit === "ng/µL"||this.inputConcUnit === "ng/µl") && 
                            (this.inputEngagedQty || this.inputEngagedQty === 0) );
				}
		};
		
		if (compute.isReady()){
			//06/12/2017 SUPSQCNG-505  si inputConc === 0  ---> engagedVol=inputVolume...
			if ( compute.inputConc === 0 && compute.inputVol ){
				var engagedVol=compute.inputVol;
			} else {
				var engagedVol= compute.inputEngagedQty / compute.inputConc;
			
				// arrondir...
				if (angular.isNumber(engagedVol) && !isNaN(engagedVol)){
					engagedVol = Math.round(engagedVol*10)/10;
				}
			}
			
			//console.log("vol engagé = "+engagedVol);
			getterEngagedVol.assign(udtData, engagedVol);
			
		} else {
			console.log("Impossible de calculer les volumes: valeurs manquantes OU concentration avec unité incorrecte");
			getterEngagedVol.assign(udtData, undefined);
			//getterEngagedVol.assign(udtData, 999);//DEBUG...
		}
	}
	

	// attention aux pb d'unité, le calcul n'est possible que si l'unité est "ng/µL"
	var computeQty = function(udtData){
		var getterEngagedQty=$parse("inputContainerUsed.experimentProperties.inputQuantity.value");
		
		var compute = {
				inputConc :      $parse("inputContainerUsed.concentration.value")(udtData), 
				inputConcUnit:   $parse("inputContainerUsed.concentration.unit")(udtData),
				inputVol :       $parse("inputContainerUsed.volume.value")(udtData), 
				inputEngagedVol :$parse("inputContainerUsed.experimentProperties.inputVolume.value")(udtData),

				isReady:function(){
					return ((this.inputConc||this.inputConc===0)  && 
					        (this.inputConcUnit === "ng/µL"||this.inputConcUnit === "ng/µl" ) &&  
                            (this.inputEngagedVol|| this.inputEngagedVol === 0) );
				}
		};
		
		if (compute.isReady()){
			//  si inputVolume === 0  ---> engagedQty = 0
			if ( compute.inputVol === 0 && compute.inputConc ){
				var engagedQty=0;
			}else{
				var engagedQty= compute.inputEngagedVol * compute.inputConc;
			
				// arrondir...
				if(angular.isNumber(engagedQty) && !isNaN(engagedQty)){
					engagedQty = Math.round(engagedQty*10)/10;
				}
			}
			
			//console.log("qté engagé = "+engagedQty);
			getterEngagedQty.assign(udtData, engagedQty);
			
		}else{
			console.log("Impossible de calculer les quantité: valeurs manquantes OU concentration avec unité incorrecte");
			getterEngagedQty.assign(udtData, undefined);
			//getterEngagedQty.assign(udtData, 999);//DEBUG...
		}
		
	}
	
	// pourrait devenir commune si utilisee ailleurs !!!
	// ??? ne rendre actif que si le datatable est lui meme en mode edition ????
	var setEditableCol = function(column, boolean){
		//console.log("setEditable "+column+": "+boolean);
		
		var index = $scope.atmService.data.config.columns.map(function (c) {return c.property;}).indexOf(column);
		// traiter le cas ou la colonne demandée n'existe pas !!!
		// que vaut index ??? null ?? undefined ??
		$scope.atmService.data.config.columns[index].edit=boolean;
	};

	var setVolQtyEditable = function(computeMode){
		if (computeMode === "volCalc"){ 
				// ancien process
				//console.log("calculer les volumes engagés d'après les quantités demandées")
				setEditableCol("inputContainerUsed.experimentProperties.inputVolume.value", false);
				setEditableCol("inputContainerUsed.experimentProperties.inputQuantity.value", true);
		} else if(computeMode === "qtyCalc"){
				// process XT-HS2
				//console.log("calculer les quantités engagée d'après les volumes engagés")
				setEditableCol("inputContainerUsed.experimentProperties.inputVolume.value", true);
				setEditableCol("inputContainerUsed.experimentProperties.inputQuantity.value", false);
		}
	}
	
	// si choix protocol Capture WES XT-HS2 BravoWs ==> forcer affichage mode "qtyCalc";
	$scope.$watch("experiment.protocolCode", function(newValue, oldValue){
		if (newValue  && newValue !== oldValue) {
			console.log("watch---protocoleCode="+newValue+"---");
		
			// 1- faire un resetAll
			// Les ATM ne sont pas visibles ==> utiliser displayResult!!
			// !! si le watch est appellé en mode creation, volume et qty peuvent ne pas etre encore définis...tester!!
			for (var i = 0; i < $scope.atmService.data.allResult.length; i++) {
				if ( $scope.atmService.data.displayResult[i].data.inputContainerUsed.experimentProperties.inputVolume )
					$scope.atmService.data.displayResult[i].data.inputContainerUsed.experimentProperties.inputVolume.value=null;
				if ( $scope.atmService.data.displayResult[i].data.inputContainerUsed.experimentProperties.inputQuantity )
					$scope.atmService.data.displayResult[i].data.inputContainerUsed.experimentProperties.inputQuantity.value=null;
			}
		
			if ($scope.experiment.protocolCode === "capture-wes-xt-hs2-bravows"){
				// 2- basculer l'affichage du mode Calcul
				//console.log ("2- inputContainerUsed.experimentProperties.computeMode.value=qtyCalc");
				// utiliser displayResult !!
				for (var i = 0; i < $scope.atmService.data.allResult.length; i++) {
					$scope.atmService.data.displayResult[i].data.inputContainerUsed.experimentProperties.computeMode = { value: 'qtyCalc'};
				}
				
				// 3- modifier setVolQtyEditable				
				//console.log ("3- setVolQtyEditable('qtyCalc')");
				setVolQtyEditable("qtyCalc"); 
			} else {
				// 2- basculer l'affichage du mode Calcul
				//console.log ("2- assign inputContainerUsed.experimentProperties.computeMode.value=volCalc");
				// utiliser displayResult !!
				for (var i = 0; i < $scope.atmService.data.allResult.length; i++) {
					$scope.atmService.data.displayResult[i].data.inputContainerUsed.experimentProperties.computeMode = { value: 'volCalc'};
				}
				
				// 3- modifier setVolQtyEditable	
				//console.log ("3- setVolQtyEditable('volCalc')");
				setVolQtyEditable("volCalc"); 
			}
		}
	});
	
	// il faut démarrer en mode calcul du volume; c'est le fonctionnement original et le plus utilisé
	setVolQtyEditable("volCalc");

}]);