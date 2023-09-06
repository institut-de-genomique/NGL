angular.module('home').controller('OneToVoidBioanalyzerMigrationProfileCNGCtrl',['$scope', '$parse','$http',
                                                             function($scope,$parse,$http) {

	var config = $scope.atmService.data.getConfig();
	
	//surcharger le filtrage... pas normal c'est le filtrage par defaut de atomicTransfereService
	//==> du au fait qu'il y a deja une surcharge dans one-to-void-cq-ctr.js  specifique au CNS
	config.order.by ="inputContainer.support.code";
	
	$scope.atmService.data.setConfig(config );
	
	// 08/01/2020 deplacement en tete de fichier
	var columns = $scope.atmService.data.getColumnsConfig();
	columns.push({
    	"header": Messages("containers.table.codeAliquot"),
		"property": "inputContainer.contents",
		"filter": "getArray:'properties.sampleAliquoteCode.value'| unique",
		"order":false,
		"hide":true,
		"type":"text",
		"position":7.1,
		"render": "<div list-resize='cellValue' list-resize-min-size='3'>",
		"extraHeaders": {0 : Messages("experiments.inputs")}
	});
	columns.push({
		"header" : Messages("containers.table.libProcessTypeCode"),
		"property" : "inputContainer.contents",
		"filter": "getArray: 'properties.libProcessTypeCode.value' | unique",
		"order" : false,
		"hide" : true,
		"type" : "text",
		"position" : 7.2,
		// 05/02/2021 mettre gettAray dans filter....
		//"render" : "<div list-resize='cellValue | getArray:\"properties.libProcessTypeCode.value\" | unique' list-resize-min-size='3'>",
		"render" :"<div list-resize='cellValue' list-resize-min-size='3'>",
		"extraHeaders" : {0 : Messages("experiments.inputs")
		}
	});
	// 08/08/2017 ajout Tag
	columns.push({
		"header" : Messages("containers.table.tags"),
		"property" : "inputContainer.contents",
		"filter" : "getArray:'properties.tag.value'| unique",
		"order" : false,
		"hide" : true,
		"type" : "text",
		"position" : 7.4,
		"render" : "<div list-resize='cellValue' list-resize-min-size='3'>",
		"extraHeaders" : {0 : Messages("experiments.inputs")}
	});
	// NGL-1226 FDS 06/12/2018 ajouter une colonne "concentration.unit" car la colonne "concentration1" issue des 
	// properties de l'experience est maintenant définie sans unité. C'est a l'utilisateur de la preciser
	columns.push({
		"header" :  Messages("containers.table.concentration.unit.shortLabel"),
		"property" : "inputContainerUsed.experimentProperties.concentration1.unit",
		"order" : true,
		"edit" : true,
		//"editDirectives":"udt-change='updatePropertyFromUDT(value,col)'",
		"hide" : true,
		"type" : "text",
		"position" : 15,
		"choiceInList":true,
		"listStyle":"select",
		"possibleValues":[{"name":"nM","code":"nM"},{"name":"ng/µl","code":"ng/µl"} ],
		//"defaultValues":"ng/µl", // essai...
		"extraHeaders" : {0 : Messages("experiments.inputs")}
	});
	
	$scope.atmService.data.setColumnsConfig(columns);
	
	// FDS 30/08/2016 concentration et size de l'expérience doivent etres copiées dans le container
	// NGL-3247 ajout du calcul de quantité meme si elle n'est pas affichée
	$scope.$parent.copyPropertiesToInputContainer = function(experiment){
		
		experiment.atomicTransfertMethods.forEach(function(atm){
			console.log("copyPropertiesToInputContainer...")
			
			var inputContainerUsed =$parse("inputContainerUseds[0]")(atm);
			if(inputContainerUsed){
				//-1-
				//concentration1 est un objet avec value et unit !!!
				var concentration1 = $parse("experimentProperties.concentration1")(inputContainerUsed);
				
				// 04/01/2019: NGL-1226 la copie de la concentration ne doit etre faite que si l'utilisateur le demande explicitement !!!
				if(concentration1 && $scope.experiment.experimentProperties.copyConcentration.value){
					
					// ATTENTION ne pas lier assignation de concentration et calcul quantité !!!
					$parse("newConcentration").assign(inputContainerUsed, concentration1);
					
					// pour NGL-3247 utiliser la structure compute/if(compute.isReady()
					var computeQty = {
						inputVol :    $parse("volume.value")(inputContainerUsed), //NGL-3247 pour le calcul de la qté il faut avoir un volume!!
						inputVolUnit :$parse("volume.unit")(inputContainerUsed),  //NGL-3247 pour le calcul de la qté il faut avoir un volume!!
						isReady:function(){
							return (
									this.inputVol  && (this.inputVol != undefined) && 
									this.inputVolUnit  && (this.inputVolUnit === "µL" || this.inputVolUnit === "µl")); // si autre unité pas géré ??
						}
					};
					
					if(computeQty.isReady()){
						// NGL-3247 calculer la quantité: qté=concentration * volume
						var calcQuantity= $parse("value")(concentration1) * $parse("inputVol")(computeQty);
						calcQuantity=Math.round(calcQuantity*100)/100;
						$parse("newQuantity.value").assign(inputContainerUsed, calcQuantity);
						
						// pour l'unité  il faut connaire l'unité de conc choisie par l'utilisateur:
						// =>  ng/µl ou nM par (seules ces 2 valeurs sont proposées)
						var concUnit=$parse("unit")(concentration1);
						var qtyUnit=null;
						if ( concUnit === "ng/µl"){ qtyUnit= "ng";} else if ( concUnit === "nM"){ qtyUnit= "fmol";}
						$parse("newQuantity.unit").assign(inputContainerUsed, qtyUnit);
						
						console.log("calcQuantity="+calcQuantity + " "+ qtyUnit);
					} else {
						console.log("valeur manquante pour calculer calcQuantity OU unité volume non gérée");
					}
				} else {
					// nécessaire à cause de la possibilité de décocher le bouton 'copyToContainer' uniquement avant 'terminer'
					console.log( "set newConcentration et newQuantity =null")
					//Attention si volume1 renseigné alors calcul newQuantity
					inputContainerUsed.newConcentration = null;
					inputContainerUsed.newQuantity = null;
				}
				
				//-2- copie automatique pour la size 
				var size1 = $parse("experimentProperties.size1")(inputContainerUsed);
				if(size1){
					//inputContainerUsed.newSize = size1;
					$parse("newSize").assign(inputContainerUsed, size1);// avec notation $parse
				}
			}
		});
	};	
	
	// code venant de chip-migration-ctrl.js au CNS: prevus pour LabChipGX ET bionanalyzer => supprimer code pour labchipGX
	var profilsMap = {};
	angular.forEach($scope.experiment.atomicTransfertMethods, function(atm){
		var pos = $parse('inputContainerUseds[0].instrumentProperties.chipPosition.value')(atm);
		var img = $parse('inputContainerUseds[0].experimentProperties.migrationProfile')(atm);
		if(pos && img)this[pos] = img;
	},profilsMap)
	
	var internalProfils = profilsMap;
	/// pas besoin de line ?????
	$scope.getProfil=function(column){
		return internalProfils[column];
	};
	
	$scope.$watch("profils",function(newValues, oldValues){
		if(newValues){			
			var _profilsMap = {};
			angular.forEach(newValues, function(img){
				var pos = img.fullname.match(/_Sample(\d+)\./)[1];
				if(pos && img)this[pos] = img;
							
			}, _profilsMap);
			
			internalProfils = _profilsMap;
			
			angular.forEach($scope.atmService.data.displayResult, function(dr){
				var pos = $parse('inputContainerUsed.instrumentProperties.chipPosition.value')(dr.data);
				if(pos)	$parse('inputContainerUsed.experimentProperties.migrationProfile').assign(dr.data, this[pos]);
			}, _profilsMap);	
		}
		angular.element('#importProfils')[0].value = null;	
	})
	
    // meme s'il n'y a pas de choix possible par l'utilisateur, ce watch est indispensable pour que les proprietes d'instrument soient injectees dans l'interface..	
	// MERCI Maud !!!
	$scope.$watch("instrumentType", function(newValue, OldValue){
		if(newValue)
			$scope.atmService.addInstrumentPropertiesToDatatable(newValue.propertiesDefinitions);
	})
	

	
	// bouton des profils
	$scope.button = {
		isShow:function(){
			return ($scope.isInProgressState() && !$scope.mainService.isEditMode() || Permissions.check("admin"))
			}	
	};

	$scope.isShowInformation = false;

	$scope.setIsShowInformation=function(bool){
		$scope.isShowInformation=bool;
	};

	$scope.toggleIsShowInformation=function() {
		if ($scope.isShowInformation===false) { 
			$scope.isShowInformation=true;
		} else {
			$scope.isShowInformation=false;
		}
	};
	
	// NGL-1226 FDS 11/12/2018 : controler que les 2 colonnes concentration et unité sont remplies ou vides...
	//==> ne bloque pas le save...=> essayer de bloquer sur $scope.$on('save'   ne marche pas non plus==> regle drools
	/*$scope.updatePropertyFromUDT = function(value, col){
		console.log("update from property : "+col.property);
		$scope.messages.clear();

		if (( col.property === 'inputContainerUsed.experimentProperties.concentration1.value')||
			( col.property === 'inputContainerUsed.experimentProperties.concentration1.unit')){
			
			var concValue= $parse("inputContainerUsed.experimentProperties.concentration1.value")(value.data);
			var concUnit=  $parse("inputContainerUsed.experimentProperties.concentration1.unit") (value.data);
			
			//console.log("concentration.value="+ concValue);
			//console.log("concentration.unit="+ concUnit);

			if      ((concValue === undefined || concValue === null ) && (concUnit === undefined ||concUnit === null  )) { console.log("2 MISSING=>OK"); }
			else if ((concValue !== undefined && concValue !== null ) && (concUnit !== undefined && concUnit !== null )) { console.log("2 OK =>OK"); }
			else {
				$scope.messages.clazz = "alert alert-danger";
				$scope.messages.text = "valeur ou concentration manquante";
				$scope.messages.open();
			}
		}
	}
	*/
}]);