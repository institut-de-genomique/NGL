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
	
	// ce code permet de filtrer les inputContainers dans l'expérience et ne garde que ceux qui correspondent au secteur choisi
	$scope.$on('updateInstrumentProperty', function(e, pName) {
		console.log("call event updateInstrumentProperty "+pName);
		
		if($scope.isCreationMode() && pName === 'sector96'){
			console.log("update sector96 "+$scope.experiment.instrumentProperties[pName].value);
			var sector96 = $scope.experiment.instrumentProperties[pName].value
			var data = $scope.atmService.data.getData();
			
			if(data){
				var newData = [];
				angular.forEach(data, function(value){
					if(value.inputContainer.support.column*1 <= 6 && sector96 === '1-48'){
						this.push(value);
					}else if(value.inputContainer.support.column*1 > 6 && sector96 === '49-96'){
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
					inputContainerUsed.newQuantity = null; // necessaire ici ??
				}
			}
		});
	};
	
	//NGL-2237 ajout parametre !! voir CNS/reception-fluo-quantification-ctrl.js
	// 28/01/2019 NGL-2368/NGL-2389 ajout  tecan+lightcycler =>ProdTecan
	// 30/03/2020 NGL-2897 il n'y a pas de Prod Tecan  c'est du dev !!! Renommer
	// 08/04/2020 NGL-2928 ; changer 'mode' et ajouter 'mappingMode'  (pour gérer les 2 nouveaux type de fichiers)
	// 28/04/2020 le fichier "standard n'est pas retenu (pour l'instant), mais la stucture  générale reste (voir aussi qpc-nb-cycle-setting-ctrl.js)
	// FDS 08/04/2020 NGL-2928 ; changer 'mode' et ajouter 'mappingMode'  (pour gérer les 2 nouveaux type de fichiers)
	//     28/04/2020 le fichier "standard n'est pas retenu (pour l'instant), mais la stucture  générale reste (voir aussi qpc-nb-cycle-setting-ctrl.js)
	// 23/06/2022 mettre a jour la propriété mapQpcrParameter  
	var importData = function(mode,mappingMode){
		console.log("importData =>"+mode + "/"+ mappingMode );
		$scope.messages.clear();
		
		if       (mode === "mapping" && mappingMode === "ProdBravo") { uploadFile=$scope.fileProd;}
		else if  (mode === "mapping" && mappingMode === "DevBravo")  { uploadFile=$scope.fileDev; }
		else if  (mode === "mapping" && mappingMode === "DevTecan")  { uploadFile=$scope.fileDevTecan; }
		//else if  (mode === "standard")                               { uploadFile=$scope.fileLightcycler; }
		
		console.log("uploading file :"+uploadFile.fullname);
		$http.post(jsRoutes.controllers.instruments.io.IO.importFile($scope.experiment.code).url+"?mode="+mode+"&mappingMode="+mappingMode,  uploadFile )
		.success(function(data, status, headers, config) {
			$scope.messages.clazz="alert alert-success";
			$scope.messages.text=Messages('experiments.msg.import.success');
			$scope.messages.showDetails = false;
			$scope.messages.open();	
			//only atm because we cannot override directly experiment on scope.parent
			$scope.experiment.atomicTransfertMethods = data.atomicTransfertMethods;
			
			$scope.fileProd = undefined;
			$scope.fileDev = undefined;
			$scope.fileDevTecan = undefined;
			// $scope.fileLightcycler = undefined; // NGL-2928 pas pour l'instant
			
			angular.element('#importFileProd')[0].value = null;
			angular.element('#importFileDev')[0].value = null;
			angular.element('#importFileDevTecan')[0].value = null;
			// angular.element('#importFileLightcycler')[0].value = null; // NGL-2928 pas pour l'instant
			
			// ajout pour NGL-3736
			updateMapQpcrParameter(mappingMode); 
			
			$scope.$emit('refresh');
		})
		.error(function(data, status, headers, config) {
			$scope.messages.clazz = "alert alert-danger";
			$scope.messages.text = Messages('experiments.msg.import.error');
			$scope.messages.setDetails(data);
			$scope.messages.open();	
			
			$scope.fileProd = undefined;
			$scope.fileDev = undefined;
			$scope.fileDevTecan = undefined;
			// $scope.fileLightcycler = undefined;// NGL-2928 pas pour l'instant
			
			angular.element('#importFileProd')[0].value = null;
			angular.element('#importFileDev')[0].value = null;
			angular.element('#importFileDevTecan')[0].value = null;
			// angular.element('#importFileLightcycler')[0].value = null;// NGL-2928 pas pour l'instant
		});
	};
	
	// NGL-2237 renommer en buttons; voir CNS/reception-fluo-quantification-ctrl.js
	// 28/01/2019 NGL-2368/NGL-2389 ajout  tecan+lihtcycler =>ProdTecan
	// 30/03/2020 correction isShowBravo et isShowTecan
	// FDS 08/04/2020 NGL-2928 ajouts pour import fichier lightCycler480ii seul=standard
	//     28/04/2020 pas pour l'instant
	$scope.buttons= {
		isShowBravo:function(){
			return ( $scope.buttons.isBravo() && ( ($scope.isInProgressState() && !$scope.mainService.isEditMode() ) || Permissions.check("admin") ) );
		},
		isShowTecan:function(){
			return ( $scope.buttons.isTecan() && ( ($scope.isInProgressState() && !$scope.mainService.isEditMode() ) || Permissions.check("admin") ) );
		},
		/* NGL-2928 pas pour l'instant
		isShowLightcycler:function(){
				return ( $scope.buttons.isLightcycler() && ( ($scope.isInProgressState() && !$scope.mainService.isEditMode() ) || Permissions.check("admin") ) );
		}, */
		isFileSetProd:function(){
			return ($scope.fileProd ===null || $scope.fileProd === undefined)?"disabled":"";
		},
		isFileSetDev:function(){
			return ($scope.fileDev === null || $scope.fileDev === undefined)?"disabled":"";
		},
		isFileSetDevTecan:function(){
			return ($scope.fileDevTecan === null || $scope.fileDevTecan === undefined)?"disabled":"";
		},
		/* NGL-2928 pas pour l'instant
		isFileSetLightcycler:function(){
			return ($scope.fileLightcycler === null || $scope.fileLightcycler === undefined)?"disabled":"";
		},*/
		clickProd:function(){ 
			return importData("mapping","ProdBravo");
		},
		clickDev:function() { 
			return importData("mapping","DevBravo");
		},
		clickDevTecan:function(){ 
			return importData("mapping","DevTecan");
		},
		/* NGL-2928 pas pour l'instant
		clickLightcycler:function(){ 
			return importData("standard",null);
		},*/
		isTecan:function(){
			return ($scope.experiment.instrument.typeCode === 'tecan-evo-150-and-qpcr-lightcycler-480II');
		},
		isBravo:function(){
			return ($scope.experiment.instrument.typeCode === 'bravows-and-qpcr-lightcycler-480II');
		},
		/* NGL-2928 pas pour l'instant
		isLightcycler:function(){
			return ($scope.experiment.instrument.typeCode === 'qpcr-lightcycler-480II');
		}*/
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
	

	// NGL-3736
	var updateMapQpcrParameter = function (mappingMode){
		var mapQpcrParameterCode = getMapQpcrParameterCode(mappingMode);
		setQpcrParameterName(mapQpcrParameterCode);
	};
	
	//attention les codes des parameters sont hardcodés dans Qpcrlightcycler480iiAloneInput.java  !!!!
	var getMapQpcrParameterCode = function(mappingMode){
		var mapQpcrParameterCode =undefined;
		switch (mappingMode) {
			case 'ProdBravo':  mapQpcrParameterCode ='bravo_5_50'; break;
			case 'DevBravo' :  mapQpcrParameterCode ='bravo_10_100';break;
			case 'DevTecan' :  mapQpcrParameterCode ='tecan_10_100';break;
		}
		return mapQpcrParameterCode;
	};
	
	var setQpcrParameterName = function (mapQpcrParameterCode ){
			var match = $scope.qpcrParameters.find(function (element) {
						return element.code === mapQpcrParameterCode;
			});
			$parse("instrumentProperties.mapQpcrParameter.value").assign($scope.experiment, match.name);
	};
	
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