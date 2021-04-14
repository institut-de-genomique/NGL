angular.module('home').controller('OneToVoidChipMigrationCNSCtrl',['$scope', '$parse','$http',
                                                             function($scope,$parse,$http) {
	
	// NGL-1055: surcharger la variable "name" definie dans le controleur parent ( one-to-void-qc-ctrl.js) => nom de fichier CSV exporté 
	var config = $scope.atmService.data.getConfig();
	config.name = $scope.experiment.typeCode.toUpperCase();
	
	$scope.atmService.data.setConfig(config );
	
	$scope.$parent.copyPropertiesToInputContainer = function(experiment){
		
		
		experiment.atomicTransfertMethods.forEach(function(atm){
			var inputContainerUsed =$parse("inputContainerUseds[0]")(atm);
			if(inputContainerUsed){
				
				var measuredSize = $parse("experimentProperties.measuredSize")(inputContainerUsed);
				
				if(measuredSize){
					inputContainerUsed.newSize = measuredSize;
					var firstContent = inputContainerUsed.contents[0]; 
					
					if(experiment.typeCode === "chip-migration" && 
							(inputContainerUsed.fromTransformationTypeCodes.indexOf("pcr-amplification-and-purification") > -1
									|| inputContainerUsed.fromTransformationTypeCodes.indexOf("indexing-and-pcr-amplification") > -1
									|| inputContainerUsed.fromTransformationTypeCodes.indexOf("sizing")  > -1 
									|| inputContainerUsed.fromTransformationTypeCodes.indexOf("spri-select")  > -1
									|| (inputContainerUsed.fromTransformationTypeCodes.indexOf("dna-illumina-indexed-library")  > -1 && firstContent.properties.libProcessTypeCode.value === 'DE'))){
						var experimentProperties = $parse("experimentProperties")(inputContainerUsed);
						
						experimentProperties.insertSize = {value:inputContainerUsed.newSize.value, unit:inputContainerUsed.newSize.unit};
						experimentProperties.insertSize.value = inputContainerUsed.newSize.value - 121;
						
						if(firstContent.properties.libProcessTypeCode.value === 'N'
								|| firstContent.properties.libProcessTypeCode.value === 'A'
									|| firstContent.properties.libProcessTypeCode.value === 'C'){
							experimentProperties.libLayoutNominalLength = {value:-1, unit:"pb"};						
						}else{
							experimentProperties.libLayoutNominalLength = experimentProperties.insertSize;
						}
						
					}
				}
			
				var volume1 = $parse("experimentProperties.volume1")(inputContainerUsed);
				if(volume1){
					inputContainerUsed.newVolume = volume1;
				}
				
				
				if(experiment.typeCode === "chip-migration-rna-evaluation"){
					if($scope.experiment.experimentProperties.copyConcentration.value === true){
						console.log("copy concentration and quantity");
						var concentration1 = $parse("experimentProperties.concentration1")(inputContainerUsed);
						if(concentration1){
							inputContainerUsed.newConcentration = concentration1;
						} else {
							inputContainerUsed.newConcentration = null;
						}
						inputContainerUsed.newQuantity = $scope.computeQuantity(
								(concentration1)?inputContainerUsed.newConcentration:inputContainerUsed.concentration, 
								(volume1)?inputContainerUsed.newVolume:inputContainerUsed.volume);
					}else{
						console.log("not copy concentration and quantity");
						inputContainerUsed.newConcentration = null;
						inputContainerUsed.newQuantity = $scope.computeQuantity(
										inputContainerUsed.concentration, 
										(volume1)?inputContainerUsed.newVolume:inputContainerUsed.volume); 
					}
				}else{
					var quantity1 = $parse("experimentProperties.quantity1")(inputContainerUsed);
					if(quantity1){
						inputContainerUsed.newQuantity = quantity1;
					}else{
						inputContainerUsed.newQuantity = $scope.computeQuantity(inputContainerUsed.concentration,
								(volume1)?inputContainerUsed.newVolume:inputContainerUsed.volume);
					}
				}
			}
			
		});	
	
	};
	
	$scope.updatePropertyFromUDT = function(value, col){
		console.log("update from property : "+col.property);
		
		if ($scope.experiment.typeCode === "chip-migration" && col.property === 'inputContainerUsed.experimentProperties.volume1.value'){
			computeQuantity1(value.data);
		} else if (col.property === 'inputContainerUsed.experimentProperties.measuredSize.value' 
				|| col.property === 'inputContainerUsed.experimentProperties.concentration.value'){
			computeConcNm(value.data);	
		} else if ($scope.experiment.typeCode === "chip-migration" 
				&& col.property === 'inputContainerUsed.experimentProperties.inputVolume.value') {
			computeVolume1(value.data);
		}
	}
	
	var computeVolume1 = function(udtData){
		var getter = $parse("inputContainerUsed.experimentProperties.volume1");
		var volume1 = getter(udtData);
		var compute = {
				totalVol : $parse("inputContainerUsed.volume")(udtData),
				inputVol : $parse("inputContainerUsed.experimentProperties.inputVolume")(udtData),
				isReady:function(){
					return (this.totalVol && this.totalVol.value && this.inputVol && this.inputVol.value);
				}
			};
		if(compute.isReady()){
			getter.assign(udtData, $scope.computeVolume(compute.totalVol, compute.inputVol));
			computeQuantity1(udtData)
		}else{
			getter.assign(udtData, undefined);
			console.log("not ready to computeVolume1");
		}
	}
	
	var computeQuantity1 = function(udtData){
		var getter = $parse("inputContainerUsed.experimentProperties.quantity1");
		var quantity1 = getter(udtData);
		
		var compute = {
				inputVol1 : $parse("inputContainerUsed.experimentProperties.volume1")(udtData),
				inputConc1 : $parse("inputContainerUsed.concentration")(udtData),
				isReady:function(){
					return (this.inputVol1 && this.inputConc1 && this.inputVol1.value && this.inputConc1.value);
				}
			};
		
		if(compute.isReady()){
			getter.assign(udtData, $scope.computeQuantity(compute.inputConc1, compute.inputVol1));
		}else{
			getter.assign(udtData, undefined);
			console.log("not ready to computeQuantity1");
		}
		
	}
	
	
	
	var profilsMap = {};
	angular.forEach($scope.experiment.atomicTransfertMethods, function(atm){
		var pos = null;
		if("labchip-gx" === $scope.experiment.instrument.typeCode){
			pos = atm.inputContainerUseds[0].locationOnContainerSupport.line+atm.inputContainerUseds[0].locationOnContainerSupport.column;
		}else if("agilent-2100-bioanalyzer" === $scope.experiment.instrument.typeCode){
			pos = $parse('inputContainerUseds[0].instrumentProperties.chipPosition.value')(atm);			
		}
		var img = $parse('inputContainerUseds[0].experimentProperties.migrationProfile')(atm);
		if(pos && img)this[pos] = img;
	},profilsMap)
	
	var internalProfils = profilsMap;
	$scope.getProfil=function(line, column){
		if("labchip-gx" === $scope.experiment.instrument.typeCode){
			return internalProfils[line+column];
		}else if("agilent-2100-bioanalyzer" === $scope.experiment.instrument.typeCode){
			return internalProfils[line];					
		}
	};
	
	$scope.$watch("profils",function(newValues, oldValues){
		if(newValues){			
			var _profilsMap = {};
			angular.forEach(newValues, function(img){
				var pos = null;
				if("labchip-gx" === $scope.experiment.instrument.typeCode){
					var pos = img.fullname.match(/_([A-H]\d+)\./)[1];					
				}else if("agilent-2100-bioanalyzer" === $scope.experiment.instrument.typeCode){
					var pos = img.fullname.match(/_Sample(\d+)\./)[1];					
				}
				if(pos && img)this[pos] = img;
							
			}, _profilsMap);
			
			internalProfils = _profilsMap;
			
			angular.forEach($scope.atmService.data.displayResult, function(dr){
				var pos = null;
				if("labchip-gx" === $scope.experiment.instrument.typeCode){
					pos = dr.data.inputContainerUsed.locationOnContainerSupport.line+dr.data.inputContainerUsed.locationOnContainerSupport.column;
				}else if("agilent-2100-bioanalyzer" === $scope.experiment.instrument.typeCode){
					pos = $parse('inputContainerUsed.instrumentProperties.chipPosition.value')(dr.data);			
				}
				if(pos)	$parse('inputContainerUsed.experimentProperties.migrationProfile').assign(dr.data, this[pos]);
			}, _profilsMap);
		
		}
		angular.element('#importProfils')[0].value = null;
		
	})
	$scope.$watch("instrumentType", function(newValue, OldValue){
		if(newValue)
			$scope.atmService.addInstrumentPropertiesToDatatable(newValue.propertiesDefinitions);
	})
	
	
	var columns = $scope.atmService.data.getColumnsConfig();
	
	columns.push({
			"header" : Messages("containers.table.concentration.shortLabel"),
			"property": "inputContainerUsed.concentration.value",
			//"property": "(inputContainerUsed.concentration.value|number).concat(' '+inputContainerUsed.concentration.unit)",
			//"render":"<span ng-bind='cellValue.value|number'/> <span ng-bind='cellValue.unit'/>",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "number",
			"position" : 8,
			"extraHeaders" : {
				0 : Messages("experiments.inputs")
			}
		});
	columns.push({
		"header" : Messages("containers.table.concentration.unit.shortLabel"),
		"property": "inputContainerUsed.concentration.unit",
		"order" : true,
		"edit" : false,
		"hide" : true,
		"type" : "text",
		"position" : 8.1,
		"extraHeaders" : {
			0 : Messages("experiments.inputs")
		}
	});

	columns.push({
			"header" : Messages("containers.table.volume") + " (µL)",
			"property" : "inputContainerUsed.volume.value",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "number",
			"position" : 9,
			"extraHeaders" : {
				0 : Messages("experiments.inputs")
			}
		});
	
	
 if($scope.experiment.typeCode !== "chip-migration-rna-evaluation"){
	 if ($scope.experiment.instrument.inContainerSupportCategoryCode.indexOf('well') == -1) {
		columns.push({
			"header" : Messages("containers.table.workName"),
			"property" : "inputContainer.properties.workName.value",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "text",
			"position" : 3.1,
			"extraHeaders" : {0 : Messages("experiments.inputs")}
		});
	}
	
	 
	 columns.push({
			"header" : Messages("containers.table.libProcessType"),
			"property" : "inputContainer.contents",
			"order" : false,
			"hide" : true,
			"type" : "text",
			"position" : 7.1,
			"render" : "<div list-resize='cellValue | getArray:\"properties.libProcessTypeCode.value\" | unique' list-resize-min-size='3'>",
			"extraHeaders" : {
				0 : Messages("experiments.inputs")
			}
		});
	 
	 columns.push({
			"header" : Messages("containers.table.tags"),
			"property" : "inputContainer.contents",
			"order" : true,
			"hide" : true,
			"type" : "text",
			"position" : 7.2,
			"filter":"getArray:\"properties.tag.value\"",
			"render" : "<div list-resize='cellValue' list-resize-min-size='3'>",
			"extraHeaders" : {
				0 : Messages("experiments.inputs")
			}

		}); 
	 
	 columns.push({
			"header" :  Messages("containers.table.quantity.unit"),
			"property" : "inputContainerUsed.experimentProperties.quantity1.unit",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "text",
			"position" : 19.1,			
			"extraHeaders" : {0 : Messages("experiments.inputs")}
		});
	 
 } else if($scope.experiment.typeCode === "chip-migration-rna-evaluation"){
		columns.push({		
			"header" : Messages("containers.table.libraryToDo"),
			"property" : "inputContainerUsed.contents",
			"filter" : "getArray:'processProperties.libraryToDo.value' | unique ",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "text",
			"position" : 10.1,
			"extraHeaders" : {
				0 : Messages("experiments.inputs")
			}
		});
}
		
	$scope.atmService.data.setColumnsConfig(columns);
	
	$scope.button = {
		isShow:function(){
			return ($scope.isInProgressState() && !$scope.mainService.isEditMode() || Permissions.check("admin"))
			}	
	};
	
	
	var importData = function(){
		$scope.messages.clear();
		
		$http.post(jsRoutes.controllers.instruments.io.IO.importFile($scope.experiment.code).url, $scope.file)
		.success(function(data, status, headers, config) {
			$scope.messages.clazz="alert alert-success";
			$scope.messages.text=Messages('experiments.msg.import.success');
			$scope.messages.showDetails = false;
			$scope.messages.open();	
			//only atm because we cannot override directly experiment on scope.parent
			$scope.experiment.atomicTransfertMethods = data.atomicTransfertMethods;
			$scope.experiment.atomicTransfertMethods.forEach(function(atm){
				computeConcNm(atm,"inputContainerUseds[0]");
			});
			
			$scope.file = undefined;
			angular.element('#importFile')[0].value = null;
			$scope.$emit('refresh');			
		})
		.error(function(data, status, headers, config) {
			$scope.messages.clazz = "alert alert-danger";
			$scope.messages.text = Messages('experiments.msg.import.error');
			$scope.messages.setDetails(data);
			$scope.messages.open();	
			$scope.file = undefined;
			angular.element('#importFile')[0].value = null;
		});
	};
	
	$scope.importButton = {
		isShow:function(){
			return ("labchip-gx" === $scope.experiment.instrument.typeCode  && !$scope.mainService.isEditMode() 
					&& ($scope.isInProgressState() || Permissions.check("admin")))
			},
		isFileSet:function(){
			return ($scope.file === undefined)?"disabled":"";
		},
		click:importData,		
	};
	
	
	if("labchip-gx" === $scope.experiment.instrument.typeCode){
		
		$scope.setAdditionnalButtons([{
			isDisabled : function(){return $scope.isNewState();} ,
			isShow:function(){return !$scope.isNewState();},
			click:$scope.fileUtils.generateSampleSheet,
			label:Messages("experiments.sampleSheet")
		}]);
	}
		
	var computeConcNm = function(udtData, key){
		
		if(key === undefined)key="inputContainerUsed";
		
		var getter= $parse(key+".experimentProperties.nMcalculatedConcentration.value");
		var nmConc=getter(udtData);
		
		var compute = {
				conc : $parse(key+".concentration.value")(udtData),
				size : $parse(key+".experimentProperties.measuredSize.value")(udtData),
				isReady:function(){
					return (this.conc && this.size);
				}
			};
		
		if(compute.isReady()){
			if ("ng/µl" === $parse(key+".concentration.unit")(udtData)){
				console.log("unit OK "+$parse(key+".concentration.unit")(udtData));
			
				var result = $parse("(conc / 660 / size * 1000000 )")(compute);
				console.log("result = "+result);
				if(angular.isNumber(result) && !isNaN(result)){
					//nmConc= Math.round(result*10)/10;	
					nmConc=result;
				}else{
					nmConc = undefined;
				}
			}else{
				console.log("unit "+$parse(key+".concentration.unit")(udtData));
				nmConc = undefined;	
			}
			getter.assign(udtData, nmConc);
		}else{
			getter.assign(udtData,undefined);
			console.log("not ready to nMcalculatedConcentration");
		}

	}

	
}]);
