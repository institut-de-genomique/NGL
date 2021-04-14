//03/13/2019 NGL-2747=> experience factice pour calcul de concentration en nM
angular.module('home').controller('OneToVoidnMConcCalculationCtrl',['$scope', '$parse','$http',
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
	// la difference de colonnes affichees entre tubes et plaque est prise en charge au niveau du controleur parent one-to-void-qc-ctrl.js 
	var columns = $scope.atmService.data.getColumnsConfig();
	columns.push({
    	"header":Messages("containers.table.codeAliquot"),
		"property": "inputContainer.contents",
		"filter": "getArray:'properties.sampleAliquoteCode.value'| unique",
		"order":true,
		"hide":true,
		"type":"text",
		"position":7,
		"render": "<div list-resize='cellValue' list-resize-min-size='3'>",
		"extraHeaders":{0:Messages("experiments.inputs")}
	});
	columns.push({
		"header" : Messages("containers.table.libProcessTypeCode"),
		"property" : "inputContainer.contents",
		"filter" : "getArray:'properties.libProcessTypeCode.value' | unique",
		"order" : true,
		"hide" : true,
		"type" : "text",
		"position" : 8,
		"render" : "<div list-resize='cellValue' list-resize-min-size='3'>",
		"extraHeaders" : {0 : Messages("experiments.inputs")
		}
	});
	columns.push({
		"header" : Messages("containers.table.tags"),
		"property" : "inputContainer.contents",
		"filter" : "getArray:'properties.tag.value'| unique",
		"order" : true,
		"hide" : true,
		"type" : "text",
		"position" : 9,
		"render" : "<div list-resize='cellValue' list-resize-min-size='3'>",
		"extraHeaders" : {0 : Messages("experiments.inputs")}
	});
	// NGL-3246  remplacer inputContainer.concentration.value par "inputContainerUsed.concentration.value"
	columns.push({
		"header" :  Messages("containers.table.concentration"),
		"property" : "inputContainerUsed.concentration.value",
		"order" : true,
		"hide" : true,
		"type" : "number",
		"position" : 10,
		"extraHeaders" : {0 : Messages("experiments.inputs")}
	});
	// NGL-3246  remplacer inputContainer.concentration.unit par "inputContainerUsed.concentration.unit"
	columns.push({
		"header" :  Messages("containers.table.concentration.unit.shortLabel"),
		"property" : "inputContainerUsed.concentration.unit",
		"order" : false,
		"hide" : true,
		"type" : "text",
		"position" : 10.1,
		"extraHeaders" : {0 : Messages("experiments.inputs")}
	});
	columns.push({
		"header" : Messages("containers.table.sizeLong"),
		"property" : "inputContainer.size.value",
		"order" : true,
		"hide" : true,
		"type" : "text",
		"position" : 10.3,
		"render" : "<div list-resize='cellValue' list-resize-min-size='3'>",
		"extraHeaders" : {0 : Messages("experiments.inputs")}
	});
	
	$scope.atmService.data.setColumnsConfig(columns);

	// se déclenche dans onSave du controler parent
	// les propriétés de l'expérience doivent etres copiées dans le container
	// NGL-3247 ajout du calcul de quantité meme si elle n'est pas affichée
	$scope.$parent.copyPropertiesToInputContainer = function(experiment){
		
		experiment.atomicTransfertMethods.forEach(function(atm){
			console.log("copyPropertiesToInputContainer...")
			
			var inputContainerUsed =$parse("inputContainerUseds[0]")(atm);
			if (inputContainerUsed){
				
				// attention ces console.log bloquent si l'élement n'existe pas!!!!
				//console.log("concentration "+ inputContainerUsed.concentration.value);
				//console.log("concentration unit "+ inputContainerUsed.concentration.unit);
				//console.log("size "+ inputContainerUsed.size.value);
				//console.log("volume="+inputContainerUsed.volume.value)
				//console.log("unit="+inputContainerUsed.volume.unit)
				
				//si concentration unit = 'nM' => rien a calculer
				//si concentration undef ou size undef => impossible de calculer;
				// 18/02/2021 !!! ne pas lier le calcul de la concentration à celui de la qté
				var compute = {
						inputConcUnit: $parse("concentration.unit")(inputContainerUsed),
						inputConc : $parse("concentration.value")(inputContainerUsed),
						inputSize : $parse("size.value")(inputContainerUsed),
						isReady:function(){
							return (this.inputSize && (this.inputSize != undefined) && 
									//(this.inputConc) && (this.inputConc != undefined) && // NGL-3255 le cas ou inputConc=0 est calculable =>0 !!	
									(this.inputConc || this.inputConc===0) && (this.inputConc != undefined) &&
									this.inputConcUnit && (this.inputConcUnit != "nM" ));
						}
				};
				
				if(compute.isReady()){
					//Formule fournie Par Julie Guy: conc_nM=concentration_ng/µL / 660 / size * 1000000
					var nmCalcConc = $parse("inputConc / 660 / inputSize * 1000000")(compute); //génère trop de décimales
					nmCalcConc=Math.round(nmCalcConc*100)/100;
					console.log("nmCalcConc = "+nmCalcConc);
					
					//utiliser $parse pour créer les experimentProperties
					$parse("experimentProperties.nMcalculatedConcentration.value").assign(inputContainerUsed, nmCalcConc);
					$parse("experimentProperties.nMcalculatedConcentration.unit").assign(inputContainerUsed, "nM");
					// pas besoin de "experimentProperties.calculatedQuantity.value" 
					// pas besoin de "experimentProperties.calculatedQuantity.unit" 
					
					//et aussi copier ces valeurs "intermédiaires" dans inputContainerUsed pour qu'elles soient a la fin automatiquement copiées dans le container Input
					$parse("newConcentration.value").assign(inputContainerUsed, nmCalcConc);
					$parse("newConcentration.unit").assign(inputContainerUsed, "nM");
					//console.log("nmCalcConc copied in newConcentration!");
					
					//NGL-3247 calculer la quantité une fois que la nmCalcConc est calculée
					var computeQty = {
						inputVol:$parse("volume.value")(inputContainerUsed),
						inputVolUnit:$parse("volume.unit")(inputContainerUsed),
						isReady:function(){
							return ((this.inputVol || this.inputVol===0) && (this.inputVol != undefined) &&
									this.inputVolUnit && ( this.inputVolUnit === "µL" || this.inputVolUnit === "µl"));
						}
					}
					
					if(computeQty.isReady()){
						// qté=concentration * volume
						var calcQuantity=nmCalcConc * $parse("inputVol")(computeQty);;
						calcQuantity=Math.round(calcQuantity*100)/100;
						console.log("calcQuantity="+calcQuantity);
					
						// l'unité de concentration est en nM, l'unité de volume µL => la quantité est en fmol
						$parse("newQuantity.value").assign(inputContainerUsed, calcQuantity);
						$parse("newQuantity.unit").assign(inputContainerUsed, "fmol");
					} else {
						console.log(inputContainerUsed.code+ ":missing value to calculate calcQuantity OU unité volume non gérée");
					}
					
				} else {
					console.log(inputContainerUsed.code+ ":missing value to calculate nmCalcConc  OR  input Concentration Unit already is nM");
				}
			}
		});
	}
}]);
