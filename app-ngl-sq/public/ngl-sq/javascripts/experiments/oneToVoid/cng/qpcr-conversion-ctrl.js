// NGL-4902 duplication depuis qpcr-quantification-ctrl.js
angular.module('home').controller('OneToVoidQPCRConversionCtrl',['$scope', '$parse','$http',
                                                             function($scope,$parse,$http) {
	
	var config = $scope.atmService.data.getConfig();

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
	
	// VIEUX CODE QUANTIFICATION 
	// s'en servir come exemple pour le recalcul des paramètres ?

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
			return importData("convert",$scope.mappingMode.code);
		}
	};
	
	// NGL-3736
	var updateMapQpcrParameter = function (mappingModeCode){
		var match = $scope.qpcrParameters.find(function (element) {
			return element.code === mappingModeCode;
		});
		// on affiche le nom du mapQpcrParameter
		$parse("instrumentProperties.mapQpcrParameter.value").assign($scope.experiment, match.name);
	};

	// Renommer en convertMode pour ne pas confondre avec la quantification ???????????????
	// mecanisme pour prevoir plusieurs modes possibles !!
    $scope.mappingMode=null;

	function resetMappingModeSelect(){
		if (      $scope.buttons.isTecan() && ! $scope.buttons.isFullPlate() ){ $scope.mappingMode=$scope.tecanMappingModes_halfPlate[0]; }
		else if ( $scope.buttons.isBravo() && ! $scope.buttons.isFullPlate() ){ $scope.mappingMode=$scope.bravoMappingModes_halfPlate[0]; }
		// pas de mode fullPLate pour l'instant...
		//else if ( $scope.buttons.isBravo() &&   $scope.buttons.isFullPlate() ){ $scope.mappingMode=$scope.bravoMappingModes_fullPlate[0]; }
	}

	$scope.setMappingMode=function(mode){
		$scope.mappingMode=mode; //!!! pourquoi l'assignation par ng-model ne le fait pas automatiquement ????
		console.log ( "mapping mode code="+$scope.mappingMode.code);
	};

	// si besoin ajouter de nouveaux modes
	$scope.mappingModes=[
		{ robot:'bravo', type:'half', code:'bravo_conv_mt',    name:'Prod - convert mtDNA'}
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
		return  $http.get(jsRoutes.controllers.commons.api.Parameters.list().url,{ params:{typeCodes:['map-qpcr-convert-parameter']} })
			.then(function(response){
				console.log('map-qpcr-convert-parameter récupérés depuis Mongo');
				$scope.qpcrParameters=response.data;/// data est la partie de la reponse qui contient les données.
			})
	}
	
	//appeler la promise
	fetchQpcrParameters();
	
}]);