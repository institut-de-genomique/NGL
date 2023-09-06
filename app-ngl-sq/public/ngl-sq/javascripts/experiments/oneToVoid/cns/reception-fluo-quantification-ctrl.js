angular.module('home')
.factory('fluoQuantificationCNSHelpers', ['$http', '$parse', '$filter', '$q', 'mainService', 
	function($http, $parse,$filter, $q, mainService){

	var helpers = {
			computeConcentration : function(udtData, concentrationPName, concentrationDilPName, dilutionFactorPName){
				var getter = $parse("inputContainerUsed.experimentProperties."+concentrationPName+".value");
				var concentration1 = getter(udtData);
				var compute = {
						conc1 : $parse("inputContainerUsed.experimentProperties."+concentrationDilPName+".value")(udtData),
						dilution1 :  (($parse("inputContainerUsed.experimentProperties."+dilutionFactorPName+".value")(udtData)).indexOf("1/") ==0 ? ($parse("inputContainerUsed.experimentProperties."+dilutionFactorPName+".value")(udtData)).substring(2) : undefined ) ,
						isReady:function(){
							return (this.conc1 && this.dilution1);
						}
				};

				if(compute.isReady()){

					var result = $parse("(conc1 * dilution1)")(compute);
					console.log(concentrationPName+" result = "+result);
					if(angular.isNumber(result) && !isNaN(result)){
						concentration1 = result;				
					}else{
						concentration1 = undefined;
					}	
					getter.assign(udtData, concentration1);
				}else{
					getter.assign(udtData, undefined);
					console.log("not ready to "+concentrationPName);
				}
			},


			computeConcentrationBR1 : function(udtData){
				this.computeConcentration(udtData, "concentrationBR1","concentrationDilBR1","dilutionFactorBR1");				
			},
			computeConcentrationHS1 : function(udtData){
				this.computeConcentration(udtData, "concentrationHS1","concentrationDilHS1","dilutionFactorHS1");				
			},
			computeConcentrationHS2 : function(udtData){
				this.computeConcentration(udtData, "concentrationHS2","concentrationDilHS2","dilutionFactorHS2");				
			},
			computeConcentrationHS3 : function(udtData){
				this.computeConcentration(udtData, "concentrationHS3","concentrationDilHS3","dilutionFactorHS3");
			},
			computeConcentration1 : function(udtData){

				var getter = $parse("inputContainerUsed.experimentProperties.concentration1.value");
				var concentration1 = getter(udtData);
				var compute = undefined;

				var calMethod=$parse("inputContainerUsed.experimentProperties.calculationMethod.value")(udtData);
				if(calMethod){
					console.log("CalMethod "+calMethod);
					if(calMethod==="Moyenne HS1 HS2"){

						compute = {
								inputConcHS1 : $parse("inputContainerUsed.experimentProperties.concentrationHS1.value")(udtData),
								inputConcHS2 : $parse("inputContainerUsed.experimentProperties.concentrationHS2.value")(udtData),
								isReady:function(){
									return (this.inputConcHS1 && this.inputConcHS2);
								},
								getResult : function(){
									return $parse("(inputConcHS1 + inputConcHS2)/2")(this);
								}
						};

					}else if(calMethod==="Moyenne HS2 HS3"){

						compute = {
								inputConcHS2 : $parse("inputContainerUsed.experimentProperties.concentrationHS2.value")(udtData),
								inputConcHS3 : $parse("inputContainerUsed.experimentProperties.concentrationHS3.value")(udtData),
								isReady:function(){
									return (this.inputConcHS2 && this.inputConcHS3);
								},
								getResult : function(){
									return $parse("(inputConcHS2 + inputConcHS3)/2")(this);
								}
						};

					}else if(calMethod==="Moyenne HS1 HS3"){

						compute = {
								inputConcHS1 : $parse("inputContainerUsed.experimentProperties.concentrationHS1.value")(udtData),
								inputConcHS3 : $parse("inputContainerUsed.experimentProperties.concentrationHS3.value")(udtData),
								isReady:function(){
									return (this.inputConcHS1 && this.inputConcHS3);
								},
								getResult : function(){
									return $parse("(inputConcHS1 + inputConcHS3)/2")(this);
								}
						};

					}else if(calMethod==="Moyenne HS1 HS2 HS3"){

						compute = {
								inputConcHS1 : $parse("inputContainerUsed.experimentProperties.concentrationHS1.value")(udtData),
								inputConcHS2 : $parse("inputContainerUsed.experimentProperties.concentrationHS2.value")(udtData),
								inputConcHS3 : $parse("inputContainerUsed.experimentProperties.concentrationHS3.value")(udtData),

								isReady:function(){
									return (this.inputConcHS1 && this.inputConcHS2 && this.inputConcHS3);
								},
								getResult : function(){
									return $parse("(inputConcHS1 + inputConcHS2 + inputConcHS3)/3")(this);
								}
						};

					}else if(calMethod==="BR si > 25 et HS1 si BR <= 25"){
						compute = {
								inputConcHS1 : $parse("inputContainerUsed.experimentProperties.concentrationHS1.value")(udtData),
								inputConcBR1 : $parse("inputContainerUsed.experimentProperties.concentrationBR1.value")(udtData),
								isReady:function(){
									return (this.inputConcHS1 && this.inputConcBR1);
								},
								getResult : function(){
									return (this.inputConcBR1 > 25 ? this.inputConcBR1 : this.inputConcHS1);
								}							
						};						
					}else if(calMethod==="BR 1 seul"){
						compute = {
								inputConcBR1 : $parse("inputContainerUsed.experimentProperties.concentrationBR1.value")(udtData),
								isReady:function(){
									return (this.inputConcBR1);
								},
								getResult : function(){
									return this.inputConcBR1;
								}							
						};
					}else if(calMethod==="HS 1 seul"){
						compute = {
								inputConcHS1 : $parse("inputContainerUsed.experimentProperties.concentrationHS1.value")(udtData),
								isReady:function(){
									return (this.inputConcHS1);
								},
								getResult : function(){
									return this.inputConcHS1;
								}								
						};
					}else if(calMethod==="HS 2 seul"){	
						compute = {
								inputConcHS2 : $parse("inputContainerUsed.experimentProperties.concentrationHS2.value")(udtData),
								isReady:function(){
									return (this.inputConcHS2);
								},
								getResult : function(){
									return this.inputConcHS2;
								}								
						};
					}else if(calMethod==="HS 3 seul"){	
						compute = {
								inputConcHS3 : $parse("inputContainerUsed.experimentProperties.concentrationHS3.value")(udtData),
								isReady:function(){
									return (this.inputConcHS3);
								},
								getResult : function(){
									return this.inputConcHS3;
								}								
						};
					}else if(calMethod==="Non quantifiable"){	
						compute = {
								isReady:function(){
									return false
								},
								getResult : function(){
									return undefined;
								}								
						};
					}
					else {	throw ("calMethod "+calMethod+" not implemented");}


					if(compute.isReady()){
						var result = compute.getResult(udtData);
						if(angular.isNumber(result) && !isNaN(result)){
							concentration1 = result;				
						}else{
							concentration1 = undefined;
						}	
						getter.assign(udtData, concentration1);
					}else{
						console.log("not ready to compute concentration");
						concentration1=undefined;
						getter.assign(udtData,concentration1);
					}

				}

				this.computeQuantity1(udtData);

			},
			computeQuantity1 : function(udtData){
				var getter= $parse("inputContainerUsed.experimentProperties.quantity1.value");
				var quantity1=getter(udtData);

				var compute = {
						inputVol1 : $parse("inputContainerUsed.experimentProperties.volume1.value")(udtData),
						inputConc1 : $parse("inputContainerUsed.experimentProperties.concentration1.value")(udtData),
						isReady:function(){
							return (this.inputVol1 && this.inputConc1);
						}
				};

				if(compute.isReady()){
					var result = $parse("(inputVol1 * inputConc1)")(compute);
					console.log("computeQuantity1 result = "+result);
					if(angular.isNumber(result) && !isNaN(result)){
						quantity1 = Math.round(result*10)/10;				
					}else{
						quantity1 = undefined;
					}	
					getter.assign(udtData, quantity1);
				}else{
					getter.assign(udtData,undefined);
					console.log("not ready to computeQuantity1");
				}

			},
			computeConcNm : function(udtData){
				var getter= $parse("inputContainerUsed.experimentProperties.nMcalculatedConcentration.value");
				var nmConc=getter(udtData);

				var compute = {
						conc1 : $parse ("inputContainerUsed.experimentProperties.concentration1.value")(udtData),
						size : $parse ("inputContainerUsed.size.value")(udtData),
						unit : $parse ("inputContainerUsed.size.unit")(udtData),
						isReady:function(){
							return (this.conc1 && this.size && this.unit === "pb");
						}
				};
				
				if(compute.isReady()){
					var result = $parse("(conc1 / 660 / size * 1000000)")(compute);
					console.log("computeConcNm result = "+result);
					if(angular.isNumber(result) && !isNaN(result)){
						//	nmConc= Math.round(result*10)/10;	
						nmConc=result;
					}else{
						nmConc = undefined;
					}	
					getter.assign(udtData, nmConc);
				}else{
					getter.assign(udtData,undefined);
					console.log("not ready to nMcalculatedConcentration");
				}
						
			},

			computeConcSs : function(udtData){
				var getter= $parse("inputContainerUsed.experimentProperties.ssConvertedConcentration.value");
				var ssConc=getter(udtData);

				var compute = {
						conc1 : $parse ("inputContainerUsed.experimentProperties.concentration1.value")(udtData),
						size : $parse ("inputContainerUsed.size.value")(udtData),
						unit : $parse ("inputContainerUsed.size.unit")(udtData),	
							
						isReady:function(){
							return (this.conc1 && this.size && this.unit === "nt");
						}
				};
				
				if(compute.isReady()){
					var result = $parse("(conc1 / 330 / size * 1000000)")(compute);
					console.log("compute ssConvertedConc result = "+result);
					if(angular.isNumber(result) && !isNaN(result)){
						ssConc=result;
					}else{
						ssConc = undefined;
					}	
					getter.assign(udtData, ssConc);
				}else{
					getter.assign(udtData,undefined);
					console.log("not ready to compute ssConvertedConc");
				}

			},

			computeCircuMgiRatioSs : function(udtData){
				var getter= $parse("inputContainerUsed.experimentProperties.sscircuMgiRatio.value");
				var ssRatio=getter(udtData);

				var compute = {
						ssConc : $parse ("inputContainerUsed.experimentProperties.ssConvertedConcentration.value")(udtData),
						volume1 : $parse ("inputContainerUsed.experimentProperties.volume1.value")(udtData),
						ssQtty : $parse ("inputContainerUsed.contents[0].properties.inputQuantitySsCircu.value")(udtData),
						fromTransfoType: $parse ("inputContainerUsed.fromTransformationTypeCodes[0]")(udtData),

						isReady:function(){
							return (this.ssConc && this.volume1 && this.ssQtty && this.fromTransfoType === "mgi-sscircularisation");
						}
				};
				
				if(compute.isReady()){
					var result = $parse("((ssConc*volume1) / ssQtty * 100)")(compute);
					if(angular.isNumber(result) && !isNaN(result)){
						ssRatio=result;
					}else{
						ssRatio = undefined;
					}	
					getter.assign(udtData, ssRatio);
				}else{
					getter.assign(udtData,undefined);
					console.log("not ready to compute sscircuMgiRatio");
				}

			},

			hideColumns:function(atmService, visualChoice){
				var udt = atmService.data;
				var columns = udt.getColumnsConfig(columns);

				for(var i = 13; i <= 36 ; i++){
					//cf prop dans QualityControl.java (verifier que les positions listéees ci-dessous sont toujours à jour)	
					// BR prop i = 14,15,16,17	
					// HS1 prop i = 18,19,20,21
					//HS2 prop i = 22, 23,24,25
					//HS3 prop i = 26, 27,28,29
					//prop communes dosage fluo 30 à 34 
					//prop spécifiques MGI 35 à 36

					var selectedColumns = $filter('filter')(columns,{position:i});
					if(selectedColumns.length === 1){
						var column = selectedColumns[0];
						
						if (35 <= i && i <= 36){
							if (("HS1 (single stranded) (MGI)" === visualChoice)
									&& udt.isHide(column.id)){
								udt.setHideColumn(column);
							}else if (("HS1 (single stranded) (MGI)" != visualChoice)
									&& ! udt.isHide(column.id)){
								udt.setHideColumn(column);
							}
						} else if(30 <= i && i <= 34){
								//udt.setHideColumn(column);
						}
						//udt.isHide(column.id)
						else if(("Tout" === visualChoice && 14 <= i && i <= 29 
								|| "BR1 + HS1 + HS2"  === visualChoice && 14 <= i && i <= 25
								|| "HS1 + HS2 + HS3"  === visualChoice && 18 <= i && i <= 29 )
								&& udt.isHide(column.id)){
							udt.setHideColumn(column);
						}else if ( ("HS1 (single stranded) (MGI)" === visualChoice && 18 <= i && i <= 21
								||  "HS1" === visualChoice && 18 <= i && i <= 21)
								&& udt.isHide(column.id)){
							udt.setHideColumn(column);
						}
						//!udt.isHide(column.id)
						else if( ("BR1 + HS1 + HS2"  === visualChoice && i > 25
								|| "HS1 + HS2 + HS3"  === visualChoice && i < 18)
								&& !udt.isHide(column.id) ){
							udt.setHideColumn(column);
						}else if ( ("HS1 (single stranded) (MGI)" === visualChoice && i < 18 
								|| "HS1 (single stranded) (MGI)" === visualChoice && 21 < i  
								|| "HS1" === visualChoice  &&i < 18 
								|| "HS1" === visualChoice  &&  21 < i)
								&& !udt.isHide(column.id)){
							udt.setHideColumn(column);								
						}else{
							console.log("not specified! "+i);
						}
					} 
				}				
			},
			computeFinalVol: function(udtData) {
				var getter = $parse("inputContainerUsed.experimentProperties.volume1.value");
				var vol = {
						inputVol: 	   $parse("inputContainerUsed.experimentProperties.inputVolume.value")(udtData),
						preQuantifVol: $parse("inputContainerUsed.experimentProperties.preQuantificationVolume.value")(udtData),
						
						final: function(){
							var arrayQcR = $parse("inputContainer.qualityControlResults")(udtData);
							var qcExtArr = $filter('filter')(arrayQcR, {'typeCode': 'external-qc'});
							var providedVol;
							
							if (qcExtArr && qcExtArr.length > 0) {
							providedVol = $parse("properties.providedVolume.value")($filter('filter')(arrayQcR, {'typeCode': 'external-qc'})[0]);
							}
							
							if(this.inputVol){
								if(this.preQuantifVol) {
									return this.preQuantifVol - this.inputVol;
								} else if(arrayQcR != undefined && (typeof arrayQcR == 'object') && (arrayQcR[0] != undefined) && providedVol) {
				    				return providedVol - this.inputVol;
				    			} else {
									return undefined;
								}
							} else {
								return undefined;
							}
						}
				}
				getter.assign(udtData, vol.final());
				this.computeQuantity1(udtData);
			},
			computeVolume1: function(udtData){
				var getter = $parse("inputContainerUsed.experimentProperties.volume1");
				var compute = {
						totalVol : $parse("inputContainerUsed.volume")(udtData),
						inputVol : $parse("inputContainerUsed.experimentProperties.inputVolume")(udtData),
						isReady:function(){
							return (this.totalVol && this.totalVol.value && this.inputVol && this.inputVol.value);
						}
				};
				if(compute.isReady()){
					getter.assign(udtData, function(volumeTot, volume) {
						if (volumeTot && volumeTot.value && volume && volume.value) {
							var result = volumeTot.value - volume.value;
							if (angular.isNumber(result) && !isNaN(result) && result >= 0 ) {
								return {
									value: result,
									unit: volumeTot.unit
								};
							}
						}
						return undefined;
					}(compute.totalVol, compute.inputVol));
				}else{
					getter.assign(udtData, undefined);
					console.log("not ready to computeVolume1");
				}
				this.computeQuantity1(udtData);
			}			
	}


	return helpers;
}]);


///////////////////FIN des methodes du Helper//////////////////////////////////////////////////////////

//Specifique fluoQuantification
angular.module('home').controller('OneToVoidFluoQuantificationCNSCtrl',['$scope', '$parse','$http','fluoQuantificationCNSHelpers',
	function($scope,$parse,$http,fluoQuantificationCNSHelpers) {

	//Add specific column for line/column fluoroskan plate
	var supportCategoryCode = $scope.plateFluoUtils.getSupportCategoryCode();

	// NGL-1055: surcharger la variable "name" definie dans le controleur parent ( one-to-void-qc-ctrl.js) => nom de fichier CSV exporté 
	var config = $scope.atmService.data.getConfig();
	config.name = $scope.experiment.typeCode.toUpperCase();
	config.order.by = "inputContainer.sampleCodes";

	if(supportCategoryCode==="tube" && ($scope.experiment.instrument.typeCode==="fluoroskan" || $scope.experiment.instrument.typeCode==="thermo-scientific-fluoroskan")){
		config.otherButtons.template=''
			+$scope.plateFluoUtils.templates.buttonLineMode()
			+$scope.plateFluoUtils.templates.buttonColumnMode();   
	}


	$scope.atmService.data.setConfig(config );

	$scope.$parent.copyPropertiesToInputContainer = function(experiment){

		experiment.atomicTransfertMethods.forEach(function(atm){
			var inputContainerUsed =$parse("inputContainerUseds[0]")(atm);
			if(inputContainerUsed){
				var concentration1 = $parse("experimentProperties.concentration1")(inputContainerUsed);
				if(concentration1){
					inputContainerUsed.newConcentration = concentration1;
				}else{
					inputContainerUsed.newConcentration = null;
				}

				var volume1 = $parse("experimentProperties.volume1")(inputContainerUsed);
				if(volume1){
					inputContainerUsed.newVolume = volume1;
				}else{
					inputContainerUsed.newVolume = null;
				}

				var quantity1 = $parse("experimentProperties.quantity1")(inputContainerUsed);
				if(quantity1){
					inputContainerUsed.newQuantity = quantity1;
				}else{
					inputContainerUsed.newQuantity = $scope.computeQuantity(
							(concentration1)?inputContainerUsed.newConcentration:inputContainerUsed.concentration, 
									(volume1)?inputContainerUsed.newVolume:inputContainerUsed.volume);
				}

			}


		});			
	};


	var columns = $scope.atmService.data.getColumnsConfig();

	columns.push({
		"header" : Messages("containers.table.libProcessType"),
		"property" : "inputContainer.contents",
		"order" : false,
		"hide" : true,
		"type" : "text",
		"position" : 8,
		"render" : "<div list-resize='cellValue | getArray:\"properties.libProcessTypeCode.value\" | unique' list-resize-min-size='3'>",
		"extraHeaders" : {
			0 : Messages("experiments.inputs")
		}
	});
	columns.push({
		"header":Messages("containers.table.size"),
		"property": "inputContainerUsed.size.value",
		"order":false,
		"hide":true,
		"type":"text",
		"position":8.05,
		"extraHeaders":{0:Messages("experiments.inputs")}			 						 			
	});
	columns.push({
		"header":Messages("containers.table.size.unit"),
		"property": "inputContainerUsed.size.unit",
		"order":false,
		"hide":true,
		"type":"text",
		"position":8.06,
		"extraHeaders":{0:Messages("experiments.inputs")}			 						 			
	});
	columns.push({
		"header" : Messages("containers.table.tags"),
		"property" : "inputContainer.contents",
		"order":true,
		"hide" : true,
		"type" : "text",
		"position" : 8.1,
		"filter":"getArray:\"properties.tag.value\"",
		"render" : "<div list-resize='cellValue' list-resize-min-size='3'>",
		"extraHeaders" : {
			0 : Messages("experiments.inputs")
		}

	});
		columns.push({
			"header":Messages("containers.table.expectedPrimaryTags"),
			"property": "inputContainer.contents",
			"filter": "getArray:'properties.expectedPrimaryTags.value'| unique",
			"order":true,
			"hide":true,
			"type":"text",
			"position":8.101,
			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			"extraHeaders":{0:Messages("experiments.inputs")}
		});

	columns.push({
		"header" : Messages("containers.table.volume") + " (µL)",
		"property" : "inputContainerUsed.volume.value",
		"order" : true,
		"edit" : false,
		"hide" : true,
		"type" : "number",
		"position" : 8.2,
		"extraHeaders" : {
			0 : Messages("experiments.inputs")
		}
	});

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

	if(supportCategoryCode==="tube" && ($scope.experiment.instrument.typeCode==="fluoroskan" || $scope.experiment.instrument.typeCode==="thermo-scientific-fluoroskan")){
		console.log("Support category "+supportCategoryCode);
		columns.push({
			"header" : Messages("containers.table.fluoroskanLine"),
			"property" : "inputContainerUsed.instrumentProperties.fluoroskanLine.value",
			"order" : true,
			"edit" : true,
			"editDirectives":" udt-change='updatePropertyFromUDT(value,col)' ",
			"choiceInList":true,
			"possibleValues":[{"name":'A',"code":"A"},{"name":'B',"code":"B"},{"name":'C',"code":"C"},{"name":'D',"code":"D"},
				{"name":'E',"code":"E"},{"name":'F',"code":"F"},{"name":'G',"code":"G"},{"name":'H',"code":"H"}],
				"hide" : true,
				"type" : "text",
				"position" : 10.5,
				"extraHeaders" : {
					0 : Messages("experiments.inputs")
				}
		});
		columns.push({
			"header" : Messages("containers.table.fluoroskanColumn"),
			"property" : "inputContainerUsed.instrumentProperties.fluoroskanColumn.value",
			"order" : true,
			"edit" : true,
			"editDirectives":" udt-change='updatePropertyFromUDT(value,col)' ",
			"choiceInList":true,
			"possibleValues":[{"name":'1',"code":"1"},{"name":'2',"code":"2"},{"name":'3',"code":"3"},{"name":'4',"code":"4"},
				{"name":'5',"code":"5"},{"name":'6',"code":"6"},{"name":'7',"code":"7"},{"name":'8',"code":"8"},
				{"name":'9',"code":"9"},{"name":'10',"code":"10"},{"name":'11',"code":"11"},{"name":'12',"code":"12"}], 
				"hide" : true,
				"type" : "number",
				"position" : 10.55,
				"extraHeaders" : {
					0 : Messages("experiments.inputs")
				}
		});
	}

	$scope.atmService.data.setColumnsConfig(columns);

	$scope.isMGI = function(){
		return $scope.experiment.protocolCode==='annexe-ssdna-mgi-v1';
	}
	
	$scope.isFluoroskan = function(){
		return ($scope.experiment.instrument.typeCode==='fluoroskan' || $scope.experiment.instrument.typeCode==='thermo-scientific-fluoroskan');
	}

	$scope.updatePropertyFromUDT = function(value, col){
		console.log("update from property : "+col.property);
		if(col.property === 'inputContainerUsed.experimentProperties.dilutionFactorBR1.value'
			|| col.property === 'inputContainerUsed.experimentProperties.concentrationDilBR1.value'){
			fluoQuantificationCNSHelpers.computeConcentrationBR1(value.data);
			fluoQuantificationCNSHelpers.computeConcentration1(value.data);
			fluoQuantificationCNSHelpers.computeConcNm(value.data);
			//NGL-3555 Cacher fonctionnalite MGI //NGL-3556 Reactivation MGI
			fluoQuantificationCNSHelpers.computeConcSs(value.data);
			fluoQuantificationCNSHelpers.computeCircuMgiRatioSs(value.data);			
		}else if(col.property === 'inputContainerUsed.experimentProperties.dilutionFactorHS1.value'
			|| col.property === 'inputContainerUsed.experimentProperties.concentrationDilHS1.value'){
			fluoQuantificationCNSHelpers.computeConcentrationHS1(value.data);
			fluoQuantificationCNSHelpers.computeConcentration1(value.data);
			fluoQuantificationCNSHelpers.computeConcNm(value.data);
			//NGL-3555 Cacher fonctionnalite MGI //NGL-3556 Reactivation MGI
			fluoQuantificationCNSHelpers.computeConcSs(value.data);
			fluoQuantificationCNSHelpers.computeCircuMgiRatioSs(value.data);			
		}else if(col.property === 'inputContainerUsed.experimentProperties.dilutionFactorHS2.value'
			|| col.property === 'inputContainerUsed.experimentProperties.concentrationDilHS2.value'){
			fluoQuantificationCNSHelpers.computeConcentrationHS2(value.data);
			fluoQuantificationCNSHelpers.computeConcentration1(value.data);
			fluoQuantificationCNSHelpers.computeConcNm(value.data);
			//NGL-3555 Cacher fonctionnalite MGI // NGL-3556 Reactivation MGI
			fluoQuantificationCNSHelpers.computeConcSs(value.data);
			fluoQuantificationCNSHelpers.computeCircuMgiRatioSs(value.data);			
		}else if(col.property === 'inputContainerUsed.experimentProperties.dilutionFactorHS3.value'
			|| col.property === 'inputContainerUsed.experimentProperties.concentrationDilHS3.value'){
			fluoQuantificationCNSHelpers.computeConcentrationHS3(value.data);
			fluoQuantificationCNSHelpers.computeConcentration1(value.data);
			fluoQuantificationCNSHelpers.computeConcNm(value.data);
			//NGL-3555 Cacher fonctionnalite MGI // NGL-3556 Reactivation MGI
			fluoQuantificationCNSHelpers.computeConcSs(value.data);
			fluoQuantificationCNSHelpers.computeCircuMgiRatioSs(value.data);			
		}else if(col.property === 'inputContainerUsed.experimentProperties.calculationMethod.value'){
			fluoQuantificationCNSHelpers.computeConcentration1(value.data);
			fluoQuantificationCNSHelpers.computeConcNm(value.data);
			//NGL-3555 Cacher fonctionnalite MGI //NGL-3556 Reactivation MGI
			fluoQuantificationCNSHelpers.computeConcSs(value.data);
			fluoQuantificationCNSHelpers.computeCircuMgiRatioSs(value.data);			
		}else if(col.property === 'inputContainerUsed.experimentProperties.volume1.value'){
			fluoQuantificationCNSHelpers.computeQuantity1(value.data);
			fluoQuantificationCNSHelpers.computeConcNm(value.data);
			//NGL-3555 Cacher fonctionnalite MGI //NGL-3556 Reactivation MGI
			fluoQuantificationCNSHelpers.computeConcSs(value.data);
			fluoQuantificationCNSHelpers.computeCircuMgiRatioSs(value.data);			
		} else if (col.property === 'inputContainerUsed.experimentProperties.inputVolume.value') {
			fluoQuantificationCNSHelpers.computeVolume1(value.data);
			fluoQuantificationCNSHelpers.computeConcNm(value.data);
			//NGL-3555 Cacher fonctionnalite MGI //NGL-3556 Reactivation MGI
			fluoQuantificationCNSHelpers.computeConcSs(value.data);
			fluoQuantificationCNSHelpers.computeCircuMgiRatioSs(value.data);			
		}

		if(col.property === 'inputContainerUsed.instrumentProperties.fluoroskanColumn.value' ||
				col.property === 'inputContainerUsed.instrumentProperties.fluoroskanLine.value'){
			console.log("update "+$scope.mainService.isEditMode()+","+$scope.isCreationMode());
			if(!$scope.isCreationMode()){
				$scope.messages.clazz = "alert alert-warning";
				$scope.messages.text = "Vous venez de modifier le plan de plaque fluoroskan";
				$scope.messages.text += ", pensez à recharger vos résultats (BR/HS).";
				$scope.messages.showDetails = false;
				$scope.messages.open();
			}
		}
	}

	// Specifique a fluoQuantif 
	// Pour modifier receptionFluoquantif modifier le importData en 2ème partie de ce code
	var importData = function(typeQC){
		$scope.messages.clear();
		$http.post(jsRoutes.controllers.instruments.io.IO.importFile($scope.experiment.code).url+"?gamme="+typeQC, ($scope.fileBR===null || $scope.fileBR===undefined)?$scope.fileHS:$scope.fileBR)
		.success(function(data, status, headers, config) {
			$scope.messages.clazz="alert alert-success";			
			$scope.messages.text= Messages('experiments.msg.import.success');

			if ($scope.fileHS != null)
				$scope.messages.text+=" Fichier fluoroskan HS chargé: \'"+$scope.fileHS.fullname+"\'.";
			//autre ecriture $scope.messages.text+=" Fichier fluoroskan HS chargé: \'"+angular.element('#importFileHS')[0].files[0].name+"\'.";

			if ($scope.fileBR != null)
				$scope.messages.text+=" Fichier fluoroskan BR chargé: \'"+$scope.fileBR.fullname+"\'.";
			//$scope.messages.text+=" Fichier fluoroskan BR chargé: \'"+angular.element('#importFileBR')[0].files[0].name+"\'.";


			$scope.messages.showDetails = false;
			$scope.messages.open();	
			//only atm because we cannot override directly experiment on scope.parent
			$scope.experiment.atomicTransfertMethods = data.atomicTransfertMethods;
			$scope.fileHS = undefined;
			$scope.fileBR = undefined;
			angular.element('#importFileHS')[0].value = null;
			angular.element('#importFileBR')[0].value = null;

			$scope.$emit('refresh');

		})
		.error(function(data, status, headers, config) {
			$scope.messages.clazz = "alert alert-danger";
			$scope.messages.text = Messages('experiments.msg.import.error');
			$scope.messages.setDetails(data);
			$scope.messages.open();	
			$scope.fileHS = undefined;
			$scope.fileBR = undefined;
			angular.element('#importFileHS')[0].value = null;
			angular.element('#importFileBR')[0].value = null;

		});
	};

	$scope.button = {
			isShow:function(){
				return (("fluoroskan" === $scope.experiment.instrument.typeCode ||  "thermo-scientific-fluoroskan" === $scope.experiment.instrument.typeCode) && !$scope.mainService.isEditMode() 
						&& ($scope.isInProgressState() || Permissions.check("admin")))

			},
			isFileSetHS:function(){
				return ($scope.fileHS ===null || $scope.fileHS === undefined)?"disabled":"";
			},
			isFileSetBR:function(){
				return ($scope.fileBR === null || $scope.fileBR === undefined)?"disabled":"";
			},
			clickHS:function(){ return importData("HS");},
			clickBR:function(){ return importData("BR");}
	};


	$scope.$watch("experiment.experimentProperties.displayChoice.value", function(newValue, oldValue){
		if (newValue !== oldValue)  {
			fluoQuantificationCNSHelpers.hideColumns($scope.atmService, newValue);
		}
	});

	if($scope.experiment.experimentProperties.displayChoice.value){
		fluoQuantificationCNSHelpers.hideColumns($scope.atmService, $scope.experiment.experimentProperties.displayChoice.value);
	}

	if(supportCategoryCode==="mixte" && ($scope.experiment.instrument.typeCode === "fluoroskan" || $scope.experiment.instrument.typeCode === "thermo-scientific-fluoroskan")){
		$scope.messages.setError(Messages("experiments.input.error.instrument-input.mixte"));
		$scope.atmService = null;
	} else {
		$scope.messages.clear();
	}
}]);



angular.module('home').controller('OneToVoidReceptionFluoQuantificationCNSCtrl',['$scope', '$parse','$http','fluoQuantificationCNSHelpers','mainService',
	function($scope,$parse,$http,fluoQuantificationCNSHelpers, mainService) {

	//Add specific column for line/column fluoroskan plate
	var supportCategoryCode = $scope.plateFluoUtils.getSupportCategoryCode();

	// NGL-1055: surcharger la variable "name" definie dans le controleur parent ( one-to-void-qc-ctrl.js) => nom de fichier CSV exporté 
	var config = $scope.atmService.data.getConfig();
	config.name = $scope.experiment.typeCode.toUpperCase();
	config.order.by = "inputContainer.sampleCodes";

	if(supportCategoryCode==="tube" && ($scope.experiment.instrument.typeCode==="fluoroskan" || $scope.experiment.instrument.typeCode==="thermo-scientific-fluoroskan")){
		config.otherButtons.template=''
			+$scope.plateFluoUtils.templates.buttonLineMode()
			+$scope.plateFluoUtils.templates.buttonColumnMode();   
	}



	$scope.atmService.data.setConfig(config );

	$scope.$parent.copyPropertiesToInputContainer = function(experiment){

		experiment.atomicTransfertMethods.forEach(function(atm){
			var inputContainerUsed =$parse("inputContainerUseds[0]")(atm);
			if(inputContainerUsed){
				var concentration1 = $parse("experimentProperties.concentration1")(inputContainerUsed);
				if(concentration1){
					inputContainerUsed.newConcentration = concentration1;
				}else{
					inputContainerUsed.newConcentration = null;
				}

				var volume1 = $parse("experimentProperties.volume1")(inputContainerUsed);
				if(volume1){
					inputContainerUsed.newVolume = volume1;
				}else{
					inputContainerUsed.newVolume = null;
				}

				var quantity1 = $parse("experimentProperties.quantity1")(inputContainerUsed);
				if(quantity1){
					inputContainerUsed.newQuantity = quantity1;
				}else{
					inputContainerUsed.newQuantity = $scope.computeQuantity(
							(concentration1)?inputContainerUsed.newConcentration:inputContainerUsed.concentration, 
									(volume1)?inputContainerUsed.newVolume:inputContainerUsed.volume);
				}

			}


		});			
	};


	var columns = $scope.atmService.data.getColumnsConfig();



	columns.push({		
		"header" : Messages("containers.table.libraryToDo"),
		"property" : "inputContainerUsed.contents",
		"filter" : "getArray:'processProperties.libraryToDo.value' | unique ",
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
		"position" : 8.05,
		"extraHeaders" : {
			0 : Messages("experiments.inputs")
		}
	});
	columns.push({
		"header" : Messages("containers.table.volume.provided") + " (µL)",
		"property" : "inputContainer.qualityControlResults",
		"filter" : "filter: {'typeCode': 'external-qc'} | get:'properties.providedVolume.value'",
		"tdClass": "valuationService.valuationCriteriaClass(value.data, experiment.status.criteriaCode, col.property)",
		"order" : true,
		"edit" : false,
		"hide" : true,
		"type" : "number",
		"position" : 10.6,
		"extraHeaders" : {
			0 : Messages("experiments.inputs")
		}
	});


	if(supportCategoryCode==="tube" && ($scope.experiment.instrument.typeCode==="fluoroskan" || $scope.experiment.instrument.typeCode==="thermo-scientific-fluoroskan")){
		console.log("Support category "+supportCategoryCode);
		columns.push({
			"header" : Messages("containers.table.fluoroskanLine"),
			"property" : "inputContainerUsed.instrumentProperties.fluoroskanLine.value",
			"order" : true,
			"edit" : true,
			"editDirectives":" udt-change='updatePropertyFromUDT(value,col)' ",
			"choiceInList":true,
			"possibleValues":[{"name":'A',"code":"A"},{"name":'B',"code":"B"},{"name":'C',"code":"C"},{"name":'D',"code":"D"},
				{"name":'E',"code":"E"},{"name":'F',"code":"F"},{"name":'G',"code":"G"},{"name":'H',"code":"H"}],
				"hide" : true,
				"type" : "text",
				"position" : 10.5,
				"extraHeaders" : {
					0 : Messages("experiments.inputs")
				}
		});
		columns.push({
			"header" : Messages("containers.table.fluoroskanColumn"),
			"property" : "inputContainerUsed.instrumentProperties.fluoroskanColumn.value",
			"order" : true,
			"edit" : true,
			"editDirectives":" udt-change='updatePropertyFromUDT(value,col)' ",
			"choiceInList":true,
			"possibleValues":[{"name":'1',"code":"1"},{"name":'2',"code":"2"},{"name":'3',"code":"3"},{"name":'4',"code":"4"},
				{"name":'5',"code":"5"},{"name":'6',"code":"6"},{"name":'7',"code":"7"},{"name":'8',"code":"8"},
				{"name":'9',"code":"9"},{"name":'10',"code":"10"},{"name":'11',"code":"11"},{"name":'12',"code":"12"}], 
				"hide" : true,
				"type" : "number",
				"position" : 10.55,
				"extraHeaders" : {
					0 : Messages("experiments.inputs")
				}
		});
	}


	$scope.atmService.data.setColumnsConfig(columns);

	$scope.isFluoroskan = function(){
		return ($scope.experiment.instrument.typeCode==='fluoroskan' || $scope.experiment.instrument.typeCode==='thermo-scientific-fluoroskan');
	}

	$scope.updatePropertyFromUDT = function(value, col){

		console.log("update from property : "+col.property);
		if(col.property === 'inputContainerUsed.experimentProperties.dilutionFactorBR1.value'
			|| col.property === 'inputContainerUsed.experimentProperties.concentrationDilBR1.value'){
			fluoQuantificationCNSHelpers.computeConcentrationBR1(value.data);
			fluoQuantificationCNSHelpers.computeConcentration1(value.data);
		}else if(col.property === 'inputContainerUsed.experimentProperties.dilutionFactorHS1.value'
			|| col.property === 'inputContainerUsed.experimentProperties.concentrationDilHS1.value'){
			fluoQuantificationCNSHelpers.computeConcentrationHS1(value.data);
			fluoQuantificationCNSHelpers.computeConcentration1(value.data);
		}else if(col.property === 'inputContainerUsed.experimentProperties.dilutionFactorHS2.value'
			|| col.property === 'inputContainerUsed.experimentProperties.concentrationDilHS2.value'){
			fluoQuantificationCNSHelpers.computeConcentrationHS2(value.data);
			fluoQuantificationCNSHelpers.computeConcentration1(value.data);
		}else if(col.property === 'inputContainerUsed.experimentProperties.dilutionFactorHS3.value'
			|| col.property === 'inputContainerUsed.experimentProperties.concentrationDilHS3.value'){
			fluoQuantificationCNSHelpers.computeConcentrationHS3(value.data);
			fluoQuantificationCNSHelpers.computeConcentration1(value.data);
		}else if(col.property === 'inputContainerUsed.experimentProperties.calculationMethod.value'){
			fluoQuantificationCNSHelpers.computeConcentration1(value.data);
		}else if(col.property === 'inputContainerUsed.experimentProperties.volume1.value'){
			fluoQuantificationCNSHelpers.computeQuantity1(value.data);
		} else if(col.property === 'inputContainerUsed.experimentProperties.inputVolume.value' || 
				col.property === 'inputContainerUsed.experimentProperties.preQuantificationVolume.value'){
			fluoQuantificationCNSHelpers.computeFinalVol(value.data);
		}


		if(col.property === 'inputContainerUsed.instrumentProperties.fluoroskanColumn.value' ||
				col.property === 'inputContainerUsed.instrumentProperties.fluoroskanLine.value'){
			console.log("update "+$scope.mainService.isEditMode()+","+$scope.isCreationMode());
			if(!$scope.isCreationMode()){
				$scope.messages.clazz = "alert alert-warning";
				$scope.messages.text = "Vous venez de modifier le plan de plaque fluoroskan";
				$scope.messages.text += ", pensez à recharger vos résultats (BR/HS).";
				$scope.messages.showDetails = false;
				$scope.messages.open();
			}
		}
	}


//	Specifique a receptionFluoQuantif 
//	Pour modifier fluoquantif modifier le importData ci dessus

	var importData = function(typeQC){
		$scope.messages.clear();
		$http.post(jsRoutes.controllers.instruments.io.IO.importFile($scope.experiment.code).url+"?gamme="+typeQC, ($scope.fileBR===null || $scope.fileBR===undefined)?$scope.fileHS:$scope.fileBR)
		.success(function(data, status, headers, config) {
			$scope.messages.clazz="alert alert-success";
			$scope.messages.text= Messages('experiments.msg.import.success');

			if ($scope.fileHS != null)
				$scope.messages.text+=" Fichier fluoroskan HS chargé: \'"+$scope.fileHS.fullname+"\'.";
			//autre ecriture $scope.messages.text+=" Fichier fluoroskan HS chargé: \'"+angular.element('#importFileHS')[0].files[0].name+"\'.";

			if ($scope.fileBR != null)
				$scope.messages.text+=" Fichier fluoroskan BR chargé: \'"+$scope.fileBR.fullname+"\'.";
			//$scope.messages.text+=" Fichier fluoroskan BR chargé: \'"+angular.element('#importFileBR')[0].files[0].name+"\'.";

			$scope.messages.showDetails = false;
			$scope.messages.open();	
			//only atm because we cannot override directly experiment on scope.parent
			$scope.experiment.atomicTransfertMethods = data.atomicTransfertMethods;
			$scope.fileHS = undefined;
			$scope.fileBR = undefined;
			angular.element('#importFileHS')[0].value = null;
			angular.element('#importFileBR')[0].value = null;

			$scope.$emit('refresh');

		})
		.error(function(data, status, headers, config) {
			$scope.messages.clazz = "alert alert-danger";
			$scope.messages.text = Messages('experiments.msg.import.error');
			$scope.messages.setDetails(data);
			$scope.messages.open();	
			$scope.fileHS = undefined;
			$scope.fileBR = undefined;
			angular.element('#importFileHS')[0].value = null;
			angular.element('#importFileBR')[0].value = null;

		});
	};

	$scope.button = {
			isShow:function(){
				return (("fluoroskan" === $scope.experiment.instrument.typeCode || "thermo-scientific-fluoroskan" === $scope.experiment.instrument.typeCode) && !$scope.mainService.isEditMode() 
						&& ($scope.isInProgressState() || Permissions.check("admin")))

			},
			isFileSetHS:function(){
				return ($scope.fileHS ===null || $scope.fileHS === undefined)?"disabled":"";
			},
			isFileSetBR:function(){
				return ($scope.fileBR === null || $scope.fileBR === undefined)?"disabled":"";
			},
			clickHS:function(){ return importData("HS");},
			clickBR:function(){ return importData("BR");}
	};

	$scope.$watch("experiment.experimentProperties.displayChoice.value", function(newValue, oldValue){
		if (newValue !== oldValue)  {
			fluoQuantificationCNSHelpers.hideColumns($scope.atmService, newValue);
		}
	});

	if($scope.experiment.experimentProperties.displayChoice.value){
		fluoQuantificationCNSHelpers.hideColumns($scope.atmService, $scope.experiment.experimentProperties.displayChoice.value);
	}

	if(supportCategoryCode==="mixte" && ($scope.experiment.instrument.typeCode === "fluoroskan" || $scope.experiment.instrument.typeCode === "thermo-scientific-fluoroskan")){
		$scope.messages.setError(Messages("experiments.input.error.instrument-input.mixte"));
		$scope.atmService = null;
	} else {
		$scope.messages.clear();
	}

}]);