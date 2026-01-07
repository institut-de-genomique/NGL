// NGL-4902 duplication depuis qpcr-quantification-ctrl.js
angular.module('home').controller('OneToVoidQPCRConversionDNACtrl',['$scope', '$parse','$http',
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
		$scope.messages.clear();
		
		if($scope.isCreationMode() && pName === 'sector96'){	
			var sector96 = $scope.experiment.instrumentProperties[pName].value
			console.log("update sector96 "+ sector96);

			// le mode full plate n'est pas envisagé pour l'instant...
			if (sector96 === '1-96'){
				$scope.messages.setError(Messages("Secteur non géré dans cette expérience"));
				///sortir !!
				return;
			}

			resetConvertModeSelect();

			var data = $scope.atmService.data.getData();
			if(data){
				var newData = [];
				angular.forEach(data, function(value){
					if(value.inputContainer.support.column*1 <= 6 && sector96 === '1-48'){
						this.push(value);
					}else if(value.inputContainer.support.column*1 > 6 && sector96 === '49-96'){
						this.push(value);
					}
					/* le mode full plate n'est pas envisagé pour l'instant...
					} else if (sector96 === '1-96'){ 
						this.push(value);
					}
					*/
				}, newData);

				if (newData.length == 0){
					$scope.messages.setError(Messages("Aucun puits ne correspondant au secteur choisi."));
				} else {
					$scope.atmService.data.setData(newData);
				}
			}
		}
	});

	//  obligatoire, en profiter pour compter les warnings...
	$scope.$parent.copyPropertiesToInputContainer = function(experiment){
		console.log ( 'SAVE CALLED....count warnings here');
		$scope.nbWarnings=countWarnings();
		return;
	};

	
	var importData = function(mode, convertModeCode){
		console.log("importData =>"+mode + "/"+ convertModeCode );
		$scope.messages.clear();
		
		uploadFile=$scope.qpcrFile;
		console.log("uploading file :"+uploadFile.fullname);

		// le secteur n'est pas passé en paramètre, importFile le récupère dans l'expérience
		// NGL-4902 passer convertModeCode (=parameterCode)
		$http.post(jsRoutes.controllers.instruments.io.IO.importFile($scope.experiment.code).url+"?mode="+mode+"&parameterCode="+convertModeCode,  uploadFile )
		.success(function(data, status, headers, config) {
			$scope.messages.clazz="alert alert-success";
			$scope.messages.text=Messages('experiments.msg.import.success');
			$scope.messages.showDetails = false;
			$scope.messages.open();	

			//only atm because we cannot override directly experiment on scope.parent
			$scope.experiment.atomicTransfertMethods = data.atomicTransfertMethods;
			
			updateMapQpcrParameter(convertModeCode); 

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
		/* issu de la copie de qpcr-quantification... le tecan n'est pas envisagé pour l'instant...
		isTecan:function(){
			return ($scope.experiment.instrument.typeCode === 'tecan-evo-150-and-qpcr-lightcycler-480II');
		},
		*/
		isBravo:function(){
			return ($scope.experiment.instrument.typeCode === 'bravows-and-qpcr-lightcycler-480II');
		},
		// necessaire !!
		isFullPlate:function(){
			return ($scope.experiment.instrumentProperties.sector96.value === '1-96');
		},
		/* inutile actuellement seul le bravo est defini
		isShowBravo:function(){
			return ( $scope.buttons.isBravo() && ( ($scope.isInProgressState() && !$scope.mainService.isEditMode() ) || Permissions.check("admin") ) );
		},
		// le tecan n'est pas envisagé pour l'instant...
		isShowTecan:function(){
			return ( $scope.buttons.isTecan() && ( ($scope.isInProgressState() && !$scope.mainService.isEditMode() ) || Permissions.check("admin") ) );
		},
		*/
		isFileSet:function(){
			return ($scope.qpcrFile === null || $scope.qpcrFile === undefined)?"disabled":"";
		},
		clickImport:function(){ 
			return importData("convert",$scope.convertMode.code);
		}
	};
	
	var updateMapQpcrParameter = function (convertModeCode){
		var match = $scope.qpcrParameters.find(function (element) {
			return element.code === convertModeCode;
		});
		// on affiche le nom du mapQpcrParameter
		$parse("instrumentProperties.mapQpcrParameter.value").assign($scope.experiment, match.name);
	};

	// mecanisme pour prevoir plusieurs modes possibles !!
    $scope.convertMode=null;

	function resetConvertModeSelect(){
		if ( $scope.buttons.isBravo() && ! $scope.buttons.isFullPlate() ){ 
			$scope.convertMode=$scope.bravoConvertModes_halfPlate[0]; 
		}
		/* le tecan n'est pas envisagé pour l'instant...le mode full plate n'est pas envisagé pour l'instant...
		else if ( $scope.buttons.isTecan() && ! $scope.buttons.isFullPlate() ){ $scope.convertMode=$scope.tecanconvertModes_halfPlate[0]; }
		else if ( $scope.buttons.isBravo() &&   $scope.buttons.isFullPlate() ){ $scope.convertMode=$scope.bravoconvertModes_fullPlate[0]; }
		*/
	}

	$scope.setConvertMode=function(mode){
		$scope.convertMode=mode; //!!! pourquoi l'assignation par ng-model ne le fait pas automatiquement ????
		console.log ("mapping mode code="+$scope.convertMode.code);
	};

	// si besoin ajouter de nouveaux modes
	$scope.convertModes=[
		{ robot:'bravo', type:'half', code:'bravo_conv_mt',    name:'Prod - convert mtDNA'}
	];

	$scope.bravoConvertModes_halfPlate=$scope.convertModes.filter(bravoHalfPlate);
	/*  le tecan n'est pas envisagé pour l'instant...le mode full plate n'est pas envisagé pour l'instant...
	$scope.tecanConvertModes_halfPlate=$scope.convertModes.filter(tecanHalfPlate);
	$scope.bravoConvertModes_fullPlate=$scope.convertModes.filter(bravoFullPlate); 
	*/

	function bravoHalfPlate(value) {
		return (value.robot === 'bravo' && value.type === 'half');
	}
	/* le tecan n'est pas envisagé pour l'instant...le mode full plate n'est pas envisagé pour l'instant...
	function bravoFullPlate(value) {
		return (value.robot === 'bravo' && value.type === 'full');
	}
	function tecanHalfPlate(value) {
		return (value.robot === 'tecan' && value.type === 'half');
	}
	*/

	// promise pour map-qpcr-convert-parameter
	function fetchQpcrParameters() {
		return  $http.get(jsRoutes.controllers.commons.api.Parameters.list().url,{ params:{typeCodes:['map-qpcr-convert-parameter']} })
			.then(function(response){
				console.log('map-qpcr-convert-parameter récupérés depuis Mongo');
				$scope.qpcrParameters=response.data;/// data est la partie de la reponse qui contient les données.
			})
	}
	
	//appeler la promise
	fetchQpcrParameters();

	// affichage d'un warning si au moins une valeur hors seuil (normalement des highlights seront mis en place...)
	$scope.nbWarnings=countWarnings();

	function countWarnings(){	
		// !!!!  garder en cohérence les seuils ici et dans les highlights !!
		sdCtGen_max=0.2;
		sdCtConv_max=0.2;
		convPercentage_min=95;

		var count=0;
		$scope.experiment.atomicTransfertMethods.forEach(function(atm){
			if ( atm.inputContainerUseds[0].experimentProperties ) {
				if (atm.inputContainerUseds[0].experimentProperties.sdCtGen.value > sdCtGen_max ){ count++; }
				if (atm.inputContainerUseds[0].experimentProperties.sdCtConv.value > sdCtConv_max ){ count++; }
				if (atm.inputContainerUseds[0].experimentProperties.convPercentage.value < convPercentage_min ){ count++; }
			}
		});

		//console.log (count + " warnings in experiment");
		return count;
 	}
	
}]);