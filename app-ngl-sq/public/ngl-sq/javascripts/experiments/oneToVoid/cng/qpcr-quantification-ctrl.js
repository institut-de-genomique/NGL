angular.module('home').controller('OneToVoidQPCRQuantificationCNGCtrl',['$scope', '$parse','$http',
                                                             function($scope,$parse,$http) {
	
	var config = $scope.atmService.data.getConfig();

	/* 15/01/2020 FINALEMENT laisser le tri par defaut  column/line
	//NGL-2755: trier sur line/colonne par défaut (colonnes héritées du controleur parent one-to-void-qc-ctrl.js)
	// attention on n'est pas forcement en mode plaque... ne pas écraser le order.by hérité
	if($scope.experiment.instrument.inContainerSupportCategoryCode == "96-well-plate"){
		config.order.by =['inputContainer.support.line','inputContainer.support.column*1'];
		$scope.atmService.data.setConfig(config );
	}
	*/
	
	// 08/01/2020 deplacement en tete de fichier
	// la différence de colonnes affichées entre tubes et plaque est prise en charge au niveau du controleur parent one-to-void-qc-ctrl.js 
	var columns = $scope.atmService.data.getColumnsConfig();

	columns.push({
    	"header":Messages("containers.table.codeAliquot"),
		"property": "inputContainer.contents",
		"filter": "getArray:'properties.sampleAliquoteCode.value'| unique",
		"order":false,
		"hide":true,
		"type":"text",
		"position":7.5,
		"render": "<div list-resize='cellValue' list-resize-min-size='3'>",
		"extraHeaders":{0:Messages("experiments.inputs")}
	});
	columns.push({
		"header" : Messages("containers.table.libProcessTypeCode"),
		"property" : "inputContainer.contents",
		"filter": "getArray:'properties.libProcessTypeCode.value' | unique",
		"order" : false,
		"hide" : true,
		"type" : "text",
		"position" : 9,
		"render" : "<div list-resize='cellValue' list-resize-min-size='3'>",
		"extraHeaders" : {0 : Messages("experiments.inputs")}
	});	
	columns.push({
		"header" : Messages("containers.table.tags"),
		"property" : "inputContainer.contents",
		"filter" : "getArray:'properties.tag.value' | unique",
		"order":true,
		"hide" : true,
		"type" : "text",
		"position" : 10,
		"render" : "<div list-resize='cellValue' list-resize-min-size='3'>",
		"extraHeaders" : {0 : Messages("experiments.inputs")}
	});
	//  28/04/2017 NGL-980: ajout colonne inputContainer size.... en position 4.5
	columns.push({
		"header" : Messages("containers.table.sizeLong"),
		"property" : "inputContainer.size.value",
		"order" : false,
		"hide" : true,
		"type" : "text",
		"position" : 4.5,
		"render" : "<div list-resize='cellValue' list-resize-min-size='3'>",
		"extraHeaders" : {0 : Messages("experiments.inputs")}
	});
	$scope.atmService.data.setColumnsConfig(columns);
	
	// Filtrer les inputContainers dans l'expérience et ne garder que ceux qui correspondent au secteur choisi
	$scope.$on('updateInstrumentProperty', function(e, pName) {
		console.log("call event updateInstrumentProperty "+pName);
		
		if($scope.isCreationMode() && pName === 'sector96'){	
			var sector96 = $scope.experiment.instrumentProperties[pName].value
			console.log("update sector96 "+ sector96);

			// ajout NGL-4902
			resetMappingModeSelect();

			var data = $scope.atmService.data.getData();
			if(data){
				var newData = [];
				angular.forEach(data, function(value){
					if(value.inputContainer.support.column*1 <= 6 && sector96 === '1-48'){
						this.push(value);
					}else if(value.inputContainer.support.column*1 > 6 && sector96 === '49-96'){
						this.push(value);
					} else if (sector96 === '1-96'){ // NGL-4635 ajout du cas 1-96 ==> on ne fait aucun filtrage on garde tous les inputContainers
						this.push(value);
					}
				}, newData);
				$scope.atmService.data.setData(newData);
			}
		}
	});
	
	// NGL-3247 ajout du calcul de quantité meme si elle n'est pas affichée
	$scope.$parent.copyPropertiesToInputContainer = function(experiment){
		
		experiment.atomicTransfertMethods.forEach(function(atm){
			var inputContainerUsed =$parse("inputContainerUseds[0]")(atm);
			if(inputContainerUsed){
				//concentration1 est un objet avec value et unit !!!
				var concentration1 = $parse("experimentProperties.concentration1")(inputContainerUsed);
				
				// 18/02/2021: la copie de la concentration ne doit etre faite que si l'utilisateur le demande explicitement ??
				if(concentration1){
					// ATTENTION ne pas lier assignation de concentration et calcul quantité !!!
					$parse("newConcentration").assign(inputContainerUsed, concentration1);
					
					// pour NGL-3247 utiliser la structure compute/if(compute.isReady()
					var compute = {
						inputVol :    $parse("volume.value")(inputContainerUsed), //NGL-3247 pour le calcul de la qté il faut avoir un volume!!
						inputVolUnit :$parse("volume.unit")(inputContainerUsed),  //NGL-3247 pour le calcul de la qté il faut avoir un volume!!
						isReady:function(){
							return (
									this.inputVol  && (this.inputVol != undefined) && 
									this.inputVolUnit  && (this.inputVolUnit === "µL" || this.inputVolUnit === "µl")); // si autre unité pas géré ??
						}
					};
					
					if(compute.isReady()){
						// NGL-3247 calculer la quantité: qté=concentration * volume
						var calcQuantity= $parse("value")(concentration1) * $parse("inputVol")(compute);
						calcQuantity=Math.round(calcQuantity*100)/100;
						$parse("newQuantity.value").assign(inputContainerUsed, calcQuantity);
						
						// l'unité de concentration est nM, l'unité de volume µL => la quantité est en fmol
						$parse("newQuantity.unit").assign(inputContainerUsed, "fmol");
						
						console.log("calcQuantity="+calcQuantity + " fmol");
					} else {
						console.log("valeur manquante pour calculer calcQuantity OU unité volume non gérée");
					}
				} else {
					inputContainerUsed.newConcentration = null;
					inputContainerUsed.newQuantity = null;
				}
			}
		});
	};
	
	//NGL-2237 ajout parametre !! voir CNS/reception-fluo-quantification-ctrl.js
	// FDS 08/04/2020 NGL-2928 ; changer 'mode' et ajouter 'mappingMode'  (pour gérer les 2 nouveaux type de fichiers)
	//     28/04/2020 le fichier "standard n'est pas retenu (pour l'instant), mais la stucture  générale reste (voir aussi qpc-nb-cycle-setting-ctrl.js)
	// 23/06/2022 mettre a jour la propriété mapQpcrParameter  
	// NGL-4902 nouveau mode de mapping 'ProdBravoConvertMt' => reorganiser le code !!!
	var importData = function(mode, mappingModeCode){
		console.log("importData =>"+mode + "/"+ mappingModeCode );
		$scope.messages.clear();
		
		uploadFile=$scope.qpcrFile;
		console.log("uploading file :"+uploadFile.fullname);

		// le secteur n'est pas passé en paramètre, importFile le récupère dans l'expérience
		// NGL-4902 passer mappingModeCode (=parameterCode)
		$http.post(jsRoutes.controllers.instruments.io.IO.importFile($scope.experiment.code).url+"?mode="+mode+"&parameterCode="+mappingModeCode,  uploadFile )
		.success(function(data, status, headers, config) {
			$scope.messages.clazz="alert alert-success";
			$scope.messages.text=Messages('experiments.msg.import.success');
			$scope.messages.showDetails = false;
			$scope.messages.open();	

			//only atm because we cannot override directly experiment on scope.parent
			$scope.experiment.atomicTransfertMethods = data.atomicTransfertMethods;
			
			// ajout pour NGL-3736
			updateMapQpcrParameter(mappingModeCode); 

			// reinitialiser les elements qui servent a importer
			$scope.qpcrFile= undefined;
			angular.element('#importQpcrFile')[0].value = null; // le input id dans le scala
			
			$scope.$emit('refresh');
		})
		.error(function(data, status, headers, config) {
			$scope.messages.clazz = "alert alert-danger";
			$scope.messages.text = Messages('experiments.msg.import.error');
			$scope.messages.setDetails(data);
			$scope.messages.open();	
			
			// reinitialiser les elements qui servent a importer
			$scope.qpcrFile= undefined;
			angular.element('#importQpcrFile')[0].value = null; // le input id dans le scala
		});
	};
	
	// NGL-2237 renommer en buttons; voir CNS/reception-fluo-quantification-ctrl.js
	// 28/01/2019 NGL-2368/NGL-2389 ajout  tecan+lihtcycler =>ProdTecan
	// 30/03/2020 correction isShowBravo et isShowTecan
	// FDS 08/04/2020 NGL-2928 ajouts pour import fichier lightCycler480ii seul=standard
	// NGL_4902 : grosses modifications... EN COURS !!!!
	$scope.buttons= {
		isTecan:function(){
			return ($scope.experiment.instrument.typeCode === 'tecan-evo-150-and-qpcr-lightcycler-480II');
		},
		isBravo:function(){
			return ($scope.experiment.instrument.typeCode === 'bravows-and-qpcr-lightcycler-480II');
		},
		/* NGL-2928 pas pour l'instant
		isLightcycler:function(){
			return ($scope.experiment.instrument.typeCode === 'qpcr-lightcycler-480II');
		},
		*/
		// NGL-4635 . !!!! au demarrrage pas encore définie !!!!! donc return false....
		isFullPlate:function(){
			return ($scope.experiment.instrumentProperties.sector96.value === '1-96');
		},
		isShowBravo:function(){
			return ( $scope.buttons.isBravo() && ( ($scope.isInProgressState() && !$scope.mainService.isEditMode() ) || Permissions.check("admin") ) );
		},
		isShowTecan:function(){
			return ( $scope.buttons.isTecan() && ( ($scope.isInProgressState() && !$scope.mainService.isEditMode() ) || Permissions.check("admin") ) );
		},
		/* NGL-2928 pas pour l'instant
		isShowLightcycler:function(){
				return ( $scope.buttons.isLightcycler() && ( ($scope.isInProgressState() && !$scope.mainService.isEditMode() ) || Permissions.check("admin") ) );
		}, 
		*/
		isFileSet:function(){
			return ($scope.qpcrFile === null || $scope.qpcrFile === undefined)?"disabled":"";
		},
		clickImport:function(){ 
			return importData("mapping",$scope.mappingMode.code);
		}
	};
	
	
	//28/04/2017 NGL-980: ajout d'un bouton supplémentaire pour copier la size dans le facteur correctif
	var config = $scope.atmService.data.getConfig();
	config.otherButtons= {
	        active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
	        complex:true,
	        template:''
	        	+'<div class="btn-group" style="margin-left:5px">'
	        	+'<button class="btn btn-default" ng-click="copySizeToCorrFactor()" data-toggle="tooltip" title="'+Messages("experiments.button.plate.copySizeTo")+ ' facteur correctif'
	        	+'"   ng-if="experiment.instrument.outContainerSupportCategoryCode!==\'tube\'"><i class="fa fa-files-o" aria-hidden="true"></i> Taille </button>'                	                	
	        	+'</div>'
	};
	$scope.atmService.data.setConfig(config);
	
	$scope.copySizeToCorrFactor = function(){
		var data = $scope.atmService.data.displayResult;
		data.forEach(function(value){
			if (value.data.inputContainer.size ) {
				//console.log('copy inputContainer.size => inputContainerUsed.experimentProperties.correctionFactorLibrarySize');
				$parse("inputContainerUsed.experimentProperties.correctionFactorLibrarySize").assign(value.data, angular.copy(value.data.inputContainer.size));
							
			}
		})
		$scope.messages.clear();
		$scope.messages.clazz = "alert alert-warning";
		$scope.messages.text = "Le facteur de correction a été modifié: n'oubliez pas de sauvegarder et de réimporter le fichier pour recalculer la concentration";
		$scope.messages.showDetails = false;
		$scope.messages.open();
	};
	
	// updatePropertyFromUDT  est automatiqut défini pour les colonnes injectées dans le datatable...
	// détecter qu'une modification de facteur correctif a eu lieu
	$scope.updatePropertyFromUDT = function(value, col){
		//console.log("update from property : "+col.property);
		if(col.property === 'inputContainerUsed.experimentProperties.correctionFactorLibrarySize.value'){
			$scope.messages.clear();
			$scope.messages.clazz = "alert alert-warning";
			$scope.messages.text = "Le facteur de correction a été modifié: n'oubliez pas de sauvegarder et de réimporter le fichier pour recalculer la concentration";
			$scope.messages.showDetails = false;
			$scope.messages.open();
		}
	};
	

	// NGL-3736 stocker dans l'expérience le choix de l'utilisateur
	var updateMapQpcrParameter = function (mappingModeCode){
		var match = $scope.qpcrParameters.find(function (element) {
			return element.code === mappingModeCode;
		});
		// on affiche le nom du mapQpcrParameter
		$parse("instrumentProperties.mapQpcrParameter.value").assign($scope.experiment, match.name);
	};

	//NGL-4902---------------------------------------------------------------------------------
    $scope.mappingMode=null;

	function resetMappingModeSelect(){
		if (      $scope.buttons.isTecan() && ! $scope.buttons.isFullPlate() ){ $scope.mappingMode=$scope.tecanMappingModes_halfPlate[0]; }
		else if ( $scope.buttons.isBravo() && ! $scope.buttons.isFullPlate() ){ $scope.mappingMode=$scope.bravoMappingModes_halfPlate[0]; }
		else if ( $scope.buttons.isBravo() &&   $scope.buttons.isFullPlate() ){ $scope.mappingMode=$scope.bravoMappingModes_fullPlate[0]; }
	}

	$scope.setMappingMode=function(mode){
		$scope.mappingMode=mode; //!!! pourquoi l'assignation par ng-model ne le fait pas automatiquement ????
		console.log ( "mapping mode code="+$scope.mappingMode.code);
	};

	// ajouter dans le libelle le facteur correctif !!!! garder en coherence avec la valeur dans map-qpcr-parameter
	$scope.mappingModes=[
		{ robot:'bravo', type:'half', code:'bravo_5_50',       name:'Prod - dilutions 5.000 et 50.000 (facteur correctif:570)'},
		{ robot:'bravo', type:'half', code:'bravo_10_100',     name:"Dev  - dilutions 10.000 et 100.000(facteur correctif:470)"} ,
		{ robot:'bravo', type:'full', code:'bravo_10',         name:'Prod - dilution 10.000 (facteur correctif:570)'},
		{ robot:'tecan', type:'half', code:'tecan_10_100',    name:'Dev  - dilutions 10.000 et 100.000 (facteur correctif:470)'},
	];

	$scope.tecanMappingModes_halfPlate=$scope.mappingModes.filter(tecanHalfPlate);
	$scope.bravoMappingModes_fullPlate=$scope.mappingModes.filter(bravoFullPlate);
	$scope.bravoMappingModes_halfPlate=$scope.mappingModes.filter(bravoHalfPlate);

	function bravoFullPlate(value) {
		return (value.robot === 'bravo' && value.type === 'full');
	}
	function bravoHalfPlate(value) {
		return (value.robot === 'bravo' && value.type === 'half');
	}
	function tecanHalfPlate(value) {
		return (value.robot === 'tecan' && value.type === 'half');
	}

	// promise pour map-qpcr-parameter
	function fetchQpcrParameters() {
		return  $http.get(jsRoutes.controllers.commons.api.Parameters.list().url,{ params:{typeCodes:['map-qpcr-parameter']} })
			.then(function(response){
				console.log('map-qpcr-parameter récupérés depuis Mongo');
				$scope.qpcrParameters=response.data;/// data est la partie de la reponse qui contient les données.
			})
	}
	
	//appeler la promise
	fetchQpcrParameters();
	
}]);