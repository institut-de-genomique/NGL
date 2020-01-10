angular.module('home').controller('CNSPrepaFlowcellOrderedCtrl',['$scope', '$parse', 'atmToDragNDrop',
                                                               function($scope, $parse, atmToDragNDrop) {
	

	var atmToSingleDatatable = $scope.atmService.$atmToSingleDatatable;
	
	var columns = [  
	             {
		        	 "header":Messages("containers.table.support.number"),
		        	 "property":"atomicTransfertMethod.line",
		        	 "order":true,
					 "edit":false,
					 "hide":true,
		        	 "type":"text",
		        	 "position":0,
		        	 "extraHeaders":{0:"Solution Stock"}
		         },	
		         {
		        	 "header":Messages("containers.table.supportCode"),
		        	 "property":"inputContainer.support.code",
		        	 "order":true,
					 "edit":false,
					 "hide":true,
		        	 "type":"text",
		        	 "position":1,
		        	 "extraHeaders":{0:"Solution Stock"}
		         },	
		         {
		        	 "header":Messages("containers.table.workName"),
		        	 "property":"inputContainer.properties.workName.value",
		        	 "order":true,
					 "edit":false,
					 "hide":true,
		        	 "type":"text",
		        	 "position":1.2,
		        	 "extraHeaders":{0:"Solution Stock"}
		         },		
		         {
		        	"header":Messages("containers.table.tags"),
		 			"property": "inputContainer.contents",
		 			"filter": "getArray:'properties.tag.value'| unique",
		 			"order":false,
		 			"hide":true,
		 			"type":"text",
		 			"position":2,
		 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
		        	 "extraHeaders":{0:"Solution Stock"}
		         },
				 {
		        	 "header":Messages("containers.table.concentration") + " (nM)",
		        	 "property":"inputContainerUsed.concentration.value",
		        	 "order":true,
					 "edit":false,
					 "hide":true,
		        	 "type":"number",
		        	 "position":5,
		        	 "extraHeaders":{0:"Solution Stock"}
		         },
		        
		         {
		        	 "header":Messages("containers.table.volume") + " (µL)",
		        	 "property":"inputContainerUsed.volume.value",
		        	 "order":true,
					 "edit":false,
					 "hide":true,
		        	 "type":"number",
		        	 "position":6,
		        	 "extraHeaders":{0:"Solution Stock"}
		         },
		         {
		        	 "header":Messages("containers.table.state.code"),
		        	 "property":"inputContainer.state.code",
		        	 "order":true,
					 "edit":false,
					 "hide":true,
		        	 "type":"text",
					 "filter":"codes:'state'",
		        	 "position":7,
		        	 "extraHeaders":{0:"Solution Stock"}
		         },
		         {
		        	 "header":Messages("containers.table.percentage"),
		        	 "property":"inputContainerUsed.percentage",
		        	 "order":true,
					 "edit":false,
					 "hide":true,
		        	 "type":"number",
		        	 "position":50,
		        	 "extraHeaders":{0:"prep FC"}
		         },		         
		         {
		        	 "header":Messages("containers.table.code"),
		        	 "property":"outputContainerUsed.code",
		        	 "order":true,
					 "edit":false,
					 "hide":true,
					 "type":"text",
		        	 "position":400,
		        	 "extraHeaders":{0:"prep FC"}
		         },
		         {
		        	 "header":Messages("containers.table.stateCode"),
		        	 "property":"outputContainer.state.code | codes:'state'",
		        	 "order":true,
					 "edit":false,
					 "hide":true,
					 "type":"text",
		        	 "position":500,
		        	 "extraHeaders":{0:"prep FC"}
		         }
		         ];
	
	
	var defaultValues = {
			"4000":{
				"inputContainerUsed.experimentProperties.inputVolume2.value":5,
				"inputContainerUsed.experimentProperties.NaOHVolume.value":5,
				"inputContainerUsed.experimentProperties.NaOHConcentration.value":"0.1N",
				"inputContainerUsed.experimentProperties.trisHCLVolume.value":5,
				"inputContainerUsed.experimentProperties.trisHCLConcentration.value":200, 
				"inputContainerUsed.experimentProperties.masterEPXVolume.value":35,
				"outputContainerUsed.experimentProperties.finalVolume.value":50
				
			},
			"NovaSeq S2 / onboard":{
				"inputContainerUsed.experimentProperties.inputVolume2.value":150,
				"inputContainerUsed.experimentProperties.NaOHVolume.value":37,
				"inputContainerUsed.experimentProperties.NaOHConcentration.value":"0.2N",
				"inputContainerUsed.experimentProperties.trisHCLVolume.value":38,
				"inputContainerUsed.experimentProperties.trisHCLConcentration.value":400, 
				"inputContainerUsed.experimentProperties.masterEPXVolume.value":525,
				"outputContainerUsed.experimentProperties.finalVolume.value":750
			},
			"NovaSeq S2 / XP FC":{
				"inputContainerUsed.experimentProperties.inputVolume2.value":22,
				"inputContainerUsed.experimentProperties.NaOHVolume.value":5,
				"inputContainerUsed.experimentProperties.NaOHConcentration.value":"0.2N",
				"inputContainerUsed.experimentProperties.trisHCLVolume.value":6,
				"inputContainerUsed.experimentProperties.trisHCLConcentration.value":400, 
				"inputContainerUsed.experimentProperties.masterEPXVolume.value":77,
				"outputContainerUsed.experimentProperties.finalVolume.value":110
			},
			"NovaSeq S4 / onboard":{
				"inputContainerUsed.experimentProperties.inputVolume2.value":310,
				"inputContainerUsed.experimentProperties.NaOHVolume.value":77,
				"inputContainerUsed.experimentProperties.NaOHConcentration.value":"0.2N",
				"inputContainerUsed.experimentProperties.trisHCLVolume.value":78,
				"inputContainerUsed.experimentProperties.trisHCLConcentration.value":400, 
				"inputContainerUsed.experimentProperties.masterEPXVolume.value":1085,
				"outputContainerUsed.experimentProperties.finalVolume.value":1550				
			},
			"NovaSeq S4 / XP FC":{
				"inputContainerUsed.experimentProperties.inputVolume2.value":30,
				"inputContainerUsed.experimentProperties.NaOHVolume.value":7,
				"inputContainerUsed.experimentProperties.NaOHConcentration.value":"0.2N",
				"inputContainerUsed.experimentProperties.trisHCLVolume.value":8,
				"inputContainerUsed.experimentProperties.trisHCLConcentration.value":400, 
				"inputContainerUsed.experimentProperties.masterEPXVolume.value":105,
				"outputContainerUsed.experimentProperties.finalVolume.value":150			
			},
			"NovaSeq S1-SP / XP FC":{
				"inputContainerUsed.experimentProperties.inputVolume2.value":18,
				"inputContainerUsed.experimentProperties.NaOHVolume.value":4,
				"inputContainerUsed.experimentProperties.NaOHConcentration.value":"0.2N",
				"inputContainerUsed.experimentProperties.trisHCLVolume.value":5,
				"inputContainerUsed.experimentProperties.trisHCLConcentration.value":400, 
				"inputContainerUsed.experimentProperties.masterEPXVolume.value":63,
				"outputContainerUsed.experimentProperties.finalVolume.value":90			
			}
			
			
	};
	
	var getDefaultValueForWorkSheet = function(line, col){
		//inputVolume2
		//NaOHVolume
		//NaOHConcentration
		//trisHCLVolume
		//trisHCLConcentration 
		//masterEPXVolume

		var worksheet = $parse("experimentProperties.worksheet.value")($scope.experiment);
		
		if(worksheet && defaultValues[worksheet][col.property]){
			return defaultValues[worksheet][col.property];
		}else {
			return undefined;
		}
		
		
	}
	
	//overide defaut method
	atmToSingleDatatable.convertOutputPropertiesToDatatableColumn = function(property){
		return this.$commonATM.convertSinglePropertyToDatatableColumn(property,"outputContainerUsed.experimentProperties.",{"0":"prep FC"});				
	};
	atmToSingleDatatable.convertInputPropertiesToDatatableColumn = function(property){
		if(property.displayOrder < 20){		
			return   this.$commonATM.convertSinglePropertyToDatatableColumn(property,"inputContainerUsed.experimentProperties.",{"0":"Dilution"});
		}else if(property.displayOrder < 30){
			var column = this.$commonATM.convertSinglePropertyToDatatableColumn(property,"inputContainerUsed.experimentProperties.",{"0":"Dénaturation - neutralisation"});
			column.defaultValues = getDefaultValueForWorkSheet;
			return column;
		}else if(property.displayOrder < 50){
			return   this.$commonATM.convertSinglePropertyToDatatableColumn(property,"inputContainerUsed.experimentProperties.",{"0":"prep FC"});
		}
	};
	
		
	atmToSingleDatatable.data.setColumnsConfig(columns);
	atmToSingleDatatable.addExperimentPropertiesToDatatable($scope.experimentType.propertiesDefinitions);
	
	$scope.$parent.changeValueOnFlowcellDesign = function(){
		$scope.atmService.data.updateDatatable();
		
		if($scope.mainService.isEditMode() && !$scope.isCreationMode()){
			$scope.messages.clazz = "alert alert-warning";
			$scope.messages.text = "Vous venez de modifier une valeur";
			$scope.messages.text += ", vous devez impérativement cliquer sur sauvegarder pour que les calculs de la FDR se remettent à jour.";
			$scope.messages.showDetails = false;
			$scope.messages.open();
		}
	};
	
	//reset some properties when change worksheet
	$scope.$watch("experiment.experimentProperties.worksheet.value", function(newValue, OldValue){
			console.log('worksheet changed to :'+ newValue);
			$scope.atmService.data.atm.forEach(function(atm){
				atm.outputContainerUseds.forEach(function(ocu){					
					if(newValue && defaultValues[newValue]["outputContainerUsed.experimentProperties.finalVolume.value"]){
						$parse("experimentProperties.finalVolume.value").assign(ocu, defaultValues[newValue]["outputContainerUsed.experimentProperties.finalVolume.value"]);
					}else{
						$parse("experimentProperties.finalVolume.value").assign(ocu, undefined);
					}																						
				});
				atm.inputContainerUseds.forEach(function(icu){					
					$parse("experimentProperties.inputVolume2.value").assign(icu, undefined);
					$parse("experimentProperties.NaOHVolume.value").assign(icu, undefined);
					$parse("experimentProperties.NaOHConcentration.value").assign(icu, undefined);
					$parse("experimentProperties.trisHCLVolume.value").assign(icu, undefined);
					$parse("experimentProperties.trisHCLConcentration.value").assign(icu, undefined);
					$parse("experimentProperties.masterEPXVolume.value").assign(icu, undefined);
					
					$parse("experimentProperties.inputVolume.value").assign(icu, undefined);
					$parse("experimentProperties.phixVolume.value").assign(icu, undefined);
					$parse("experimentProperties.rsbVolume.value").assign(icu, undefined);
					$parse("experimentProperties.finalConcentration2.value").assign(icu, undefined);
					
				});
				
			});
			$scope.atmService.data.updateDatatable();		
	});
	
	
	$scope.updatePropertyFromUDT = function(value, col){
		console.log("update from property : "+col.property);
		
		computeInputVolume(value.data);
		computePhiXVolume(value.data);
		computeRSBVolume(value.data);
		computeFinalConcentration2(value.data);
	}
	//inputVolume (finalConcentration1*finalVolume1)/concentrationIN;		
	var computeInputVolume = function(udtData){
		var getter = $parse("inputContainerUsed.experimentProperties.inputVolume.value");
		var inputVolume = getter(udtData);

		var compute = {
				finalConcentration1 : $parse("inputContainerUsed.experimentProperties.finalConcentration1.value")(udtData),			
				finalVolume1 : $parse("inputContainerUsed.experimentProperties.finalVolume1.value")(udtData),	
				concentrationIN : $parse("inputContainer.concentration.value")(udtData),
				isReady:function(){
					return (this.finalConcentration1 && this.finalVolume1 && this.concentrationIN);
				}
		};

		if(compute.isReady()){
			var result = $parse("(finalConcentration1*finalVolume1)/concentrationIN")(compute);
			if(angular.isNumber(result) && !isNaN(result)){
				inputVolume =result;				
			}else{
				inputVolume = undefined;
			}	
			getter.assign(udtData, inputVolume);
		}else{
			inputVolume = undefined;
			getter.assign(udtData, inputVolume);
			console.log("not ready to computeInputVolume");
		}
	};
	//phiXVolume (phixPercent*finalConcentration1*finalVolume1)/phixConcentration;
	var computePhiXVolume = function(udtData){
		var getter = $parse("inputContainerUsed.experimentProperties.phixVolume.value");
		var phixVolume = getter(udtData);

		var compute = {
				finalConcentration1 : $parse("inputContainerUsed.experimentProperties.finalConcentration1.value")(udtData),			
				finalVolume1 : $parse("inputContainerUsed.experimentProperties.finalVolume1.value")(udtData),	
				phixPercent : $parse("outputContainerUsed.experimentProperties.phixPercent.value")(udtData),
				phixConcentration : $parse("inputContainerUsed.experimentProperties.phixConcentration.value")(udtData),
				
				isReady:function(){
					return (this.finalConcentration1 && this.finalVolume1 && this.phixPercent && this.phixConcentration);
				}
		};

		if(compute.isReady()){
			var result = $parse("(phixPercent*finalConcentration1*finalVolume1/phixConcentration)/100")(compute);
			if(angular.isNumber(result) && !isNaN(result)){
				phixVolume =result;				
			}else{
				phixVolume = undefined;
			}	
			getter.assign(udtData, phixVolume);
		}else{
			phixVolume = undefined;
			getter.assign(udtData, phixVolume);
			console.log("not ready to computePhiXVolume");
		}
	};
	//rsbVolume (finalVolume1-inputVolume-phixVolume);
	var computeRSBVolume = function(udtData){
		var getter = $parse("inputContainerUsed.experimentProperties.rsbVolume.value");
		var rsbVolume = getter(udtData);

		var compute = {
				inputVolume : $parse("inputContainerUsed.experimentProperties.inputVolume.value")(udtData),			
				finalVolume1 : $parse("inputContainerUsed.experimentProperties.finalVolume1.value")(udtData),	
				phixVolume : $parse("inputContainerUsed.experimentProperties.phixVolume.value")(udtData),
				
				isReady:function(){
					return (this.inputVolume && this.finalVolume1 && this.phixVolume);
				}
		};

		if(compute.isReady()){
			var result = $parse("(finalVolume1-inputVolume-phixVolume)")(compute);
			if(angular.isNumber(result) && !isNaN(result)){
				rsbVolume =result;				
			}else{
				rsbVolume = undefined;
			}	
			getter.assign(udtData, rsbVolume);
		}else{
			rsbVolume = undefined;
			getter.assign(udtData, rsbVolume);
			console.log("not ready to computeRSBVolume");
		}
	};
	//finalConcentration2 (finalConcentration1*inputVolume2)/finalVolume;
	var computeFinalConcentration2 = function(udtData){
		var getter = $parse("inputContainerUsed.experimentProperties.finalConcentration2.value");
		var finalConcentration2 = getter(udtData);

		var compute = {
				inputVolume2 : $parse("inputContainerUsed.experimentProperties.inputVolume2.value")(udtData),			
				finalConcentration1 : $parse("inputContainerUsed.experimentProperties.finalConcentration1.value")(udtData),			
				finalVolume : $parse("outputContainerUsed.experimentProperties.finalVolume.value")(udtData),
				
				isReady:function(){
					return (this.inputVolume2 && this.finalConcentration1 && this.finalVolume);
				}
		};

		if(compute.isReady()){
			var result = $parse("(finalConcentration1*inputVolume2)/finalVolume")(compute);
			if(angular.isNumber(result) && !isNaN(result)){
				finalConcentration2 =result;				
			}else{
				finalConcentration2 = undefined;
			}	
			getter.assign(udtData, finalConcentration2);
		}else{
			finalConcentration2 = undefined;
			getter.assign(udtData, finalConcentration2);
			console.log("not ready to finalConcentration2");
		}
	};
	$scope.getSampleAndTagsWithSecondary = function(container){
		var sampleCodeAndTags = [];
		angular.forEach(container.contents, function(content){
			if(content.properties.secondaryTag != undefined && content.properties.tag != undefined && content.sampleCode != undefined){
				sampleCodeAndTags.push(content.sampleCode+" / "+content.properties.secondaryTag.value+" / "+content.properties.tag.value);
			}else if(content.properties.tag != undefined && content.sampleCode != undefined){
 				sampleCodeAndTags.push(content.sampleCode+" / / "+content.properties.tag.value);
 			}else if(content.properties.secondaryTag != undefined && content.sampleCode != undefined){
 				sampleCodeAndTags.push(content.sampleCode+" / "+content.properties.secondaryTag.value+" / ");
 			}else if(content.sampleCode != undefined){
 				sampleCodeAndTags.push(content.sampleCode);
 			}
		});
		return sampleCodeAndTags;
	};
}]);
