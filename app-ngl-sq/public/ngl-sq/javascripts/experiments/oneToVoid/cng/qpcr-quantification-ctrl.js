angular.module('home').controller('OneToVoidQPCRQuantificationCNGCtrl',['$scope', '$parse','$http',
                                                             function($scope,$parse,$http) {
	
	// NGL-1055 : surcharger la variable "name" definie dans le controleur parent ( one-to-void-qc-ctrl.js) => nom de fichier CSV exporté 
	var config = $scope.atmService.data.getConfig();
	config.name = $scope.experiment.typeCode.toUpperCase();
	$scope.atmService.data.setConfig(config );	
	
	
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
	
	$scope.$parent.copyPropertiesToInputContainer = function(experiment){
		
		experiment.atomicTransfertMethods.forEach(function(atm){
			var inputContainerUsed =$parse("inputContainerUseds[0]")(atm);
			if(inputContainerUsed){
				var concentration1 = $parse("experimentProperties.concentration1")(inputContainerUsed);
				if(concentration1){
					inputContainerUsed.newConcentration = concentration1;
				}
			}
		});
	};
	
	//NGL-2237 ajout parametre !! voir CNS/reception-fluo-quantificatio-ctrl.js
	// 28/01/2019 NGL-2368/NGL-2389 ajout  tecan+lightcycler =>ProdTecan
	var importData = function(typeQC){
		console.log("importData =>"+typeQC);
		$scope.messages.clear();
		if       (typeQC === "ProdBravo") { uploadFile=$scope.fileProd; }
		else if  (typeQC === "DevBravo")  { uploadFile=$scope.fileDev;  }
		else if  (typeQC === "ProdTecan") { uploadFile=$scope.fileProdTecan;  }
		console.log("File :"+uploadFile.fullname+", mode :"+typeQC);
		
		$http.post(jsRoutes.controllers.instruments.io.IO.importFile($scope.experiment.code).url+"?mode="+typeQC,  uploadFile )
		.success(function(data, status, headers, config) {
			$scope.messages.clazz="alert alert-success";
			$scope.messages.text=Messages('experiments.msg.import.success');
			$scope.messages.showDetails = false;
			$scope.messages.open();	
			//only atm because we cannot override directly experiment on scope.parent
			$scope.experiment.atomicTransfertMethods = data.atomicTransfertMethods;
			
			$scope.fileProd = undefined;
			$scope.fileDev = undefined;
			$scope.fileProdTecan= undefined;
			
			angular.element('#importFileProd')[0].value = null;
			angular.element('#importFileDev')[0].value = null;
			angular.element('#importFileProdTecan')[0].value = null;
			
			$scope.$emit('refresh');
		})
		.error(function(data, status, headers, config) {
			$scope.messages.clazz = "alert alert-danger";
			$scope.messages.text = Messages('experiments.msg.import.error');
			$scope.messages.setDetails(data);
			$scope.messages.open();	
			
			$scope.fileProd = undefined;
			$scope.fileDev = undefined;
			$scope.fileProdTecan= undefined;
			
			angular.element('#importFileProd')[0].value = null;
			angular.element('#importFileDev')[0].value = null;
			angular.element('#importFileProdTecan')[0].value = null;
		});
	};
	
	// NGL-2237 renommer en buttons; voir CNS/reception-fluo-quantification-ctrl.js
	// 28/01/2019 NGL-2368/NGL-2389 ajout  tecan+lihtcycler =>ProdTecan
	// 06/02/2019 dans le cas de lightCycler seul pas de bouton d'import ???
	$scope.buttons= {
		isShowBravo:function(){
			return ( ($scope.isInProgressState() && !$scope.mainService.isEditMode() && $scope.buttons.isBravo()) || (Permissions.check("admin") && !$scope.buttons.isTecan()) )
			},
		isShowTecan:function(){
			return ( ($scope.isInProgressState() && !$scope.mainService.isEditMode() && $scope.buttons.isTecan()) || (Permissions.check("admin") && !$scope.buttons.isTecan()) )
			},
		isFileSetProd:function(){
			return ($scope.fileProd ===null || $scope.fileProd === undefined)?"disabled":"";
		},
		isFileSetDev:function(){
			return ($scope.fileDev === null || $scope.fileDev === undefined)?"disabled":"";
		},
		isFileSetProdTecan:function(){
			return ($scope.fileProdTecan === null || $scope.fileProdTecan === undefined)?"disabled":"";
		},
		clickProd:function(){ 
			return importData("ProdBravo");
		},
		clickDev:function() { 
			return importData("DevBravo");
		},
		clickProdTecan:function(){ 
			console.log("click on ProdTecan...");
			return importData("ProdTecan");
		},
		isTecan:function(){
			return ($scope.experiment.instrument.typeCode === 'tecan-evo-150-and-qpcr-lightcycler-480II');
		},
		isBravo:function(){
			return ($scope.experiment.instrument.typeCode === 'bravows-and-qpcr-lightcycler-480II');
		}
		
	};
	
	
	// NGL-1055: mettre les getArray et codes'' dans filter et pas dans render
	var columns = $scope.atmService.data.getColumnsConfig();

	columns.push({
    	"header":"Code aliquot",
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
	
	//28/04/2017 NGL-980: ajout d'un bouton supplementaire pour copier la size dans le facteur correctif
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
	$scope.atmService.data.setConfig(config );	
	
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
	
	// updatePropertyFromUDT  est automatiqut defini pour les colonnes injectees dans le datatable...
	// detecter qu'une modification de facteur corectif a eut lieu
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
	
}]);