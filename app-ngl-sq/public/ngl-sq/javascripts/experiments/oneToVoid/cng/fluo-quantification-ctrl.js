angular.module('home').controller('OneToVoidFluoQuantificationCNGCtrl',['$scope', '$parse','$http',
                                                                 function($scope,$parse,$http) {

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
    	"header":"Code aliquot",
		"property": "inputContainer.contents",
		"filter": "getArray:'properties.sampleAliquoteCode.value'| unique",
		"order":true,
		"hide":true,
		"type":"text",
		"position":7.5,
		"render": "<div list-resize='cellValue' list-resize-min-size='3'>",
		"extraHeaders":{0:Messages("experiments.inputs")}
	});
	columns.push({
		"header" : Messages("containers.table.tags"),
		"property" : "inputContainer.contents",
		"filter" : "getArray:'properties.tag.value' | unique",
		"order" : false,
		"hide" : true,
		"type" : "text",
		"position" : 8,
		"render" : "<div list-resize='cellValue' list-resize-min-size='3'>",
		"extraHeaders" : {0 : Messages("experiments.inputs")}
	});
	// 04/09/2017 niveau process ET contents =>utiliser properties et pas processProperties
	columns.push({
	   	 "header": Messages("containers.table.libProcessType"),
	   	 "property" : "inputContainerUsed.contents",
	   	 "filter" : "getArray:'properties.libProcessTypeCode.value' | unique | codes:'value'",
	   	 "order":true,
		 "edit":false,
		  "hide":true,
	   	 "type":"text",
	   	 "position":8.1,
	   	 "extraHeaders":{0 : Messages("experiments.inputs")}
	  }); 
	/*                  NGL-1226 11/2018  demande de ne plus afficher ces 2 colonnes....
	// 04/09/2017 utiliser processProperties
	columns.push({
		"header": Messages("containers.table.expectedBaits"),
		"property" : "inputContainerUsed.contents",
		"filter" : "getArray:'processProperties.expectedBaits.value' | unique | codes:'value'",
		"order":true,
		"edit":false,
		"hide":true,
		"type":"text",
		"position":8.2,
		"extraHeaders":{0 : Messages("experiments.inputs")}
		});
	// 04/09/2017 niveau process ET contents =>utiliser properties et pas processProperties
	columns.push({
	  	 "header": Messages("containers.table.captureProtocol"),
	  	 "property" : "inputContainerUsed.contents",
	  	 "filter" : "getArray:'properties.captureProtocol.value' | unique | codes:'value'",
	  	 "order":true,
		 "edit":false,
		 "hide":true,
	  	 "type":"text",
	  	 "position":8.2,
	  	 "extraHeaders":{0 : Messages("experiments.inputs")}
	   });
     */
	
	$scope.atmService.data.setColumnsConfig(columns);
	
	// 04/02/2021 ajout de ce code après première utilisation par la prod....
	// => permet de filter les inputContainers dans l'expérience et ne garde que ceux qui correspondent au secteur choisi
	$scope.$on('updateInstrumentProperty', function(e, pName) {
		console.log("call event updateInstrumentProperty "+pName);
		
		if($scope.isCreationMode() && pName === 'sector96'){
			//console.log("update sector96 "+$scope.experiment.instrumentProperties[pName].value);
			var sector96 = $scope.experiment.instrumentProperties[pName].value
			var data = $scope.atmService.data.getData();
			
			if(data){
				var newData = [];
				angular.forEach(data, function(value){
					// !! dans cette expérience les secteurs ont des noms différents de qpr-quantification  'A1-H6' vs '1-48' / 'A7-H12' vs '49-96'
					if(value.inputContainer.support.column*1 <= 6 && sector96 === 'A1-H6'){
						this.push(value);
					}else if(value.inputContainer.support.column*1 > 6 && sector96 === 'A7-H12'){
						this.push(value);
					}
				}, newData);
				$scope.atmService.data.setData(newData);
			}
		}
	});
	
	// se déclenche dans onSave du controler parent
	// les propriétés de l'expérience doivent etres copiées dans le container
	// NGL-3247 ajout du calcul de quantité meme si elle n'est pas affichée
	$scope.$parent.copyPropertiesToInputContainer = function(experiment){
		
		experiment.atomicTransfertMethods.forEach(function(atm){
			var inputContainerUsed =$parse("inputContainerUseds[0]")(atm);
			//console.log("copyPropertiesToInputContainer...");
			
			if(inputContainerUsed){
				//concentration1 est un objet avec value et unit !!!
				var concentration1 = $parse("experimentProperties.concentration1")(inputContainerUsed); 
				
				// 26/03/2018: NGL-1970 la copie de la concentration ne doit etre faite que si l'utilisateur le demande explicitement !!!
				if (concentration1  &&  $scope.experiment.experimentProperties.copyConcentration.value){
					
					// pour NGL-3247 utiliser la structure compute/if(compute.isReady()
					// ATTENTION ne pas lier assignation de concentration et calcul quantité !!!
					$parse("newConcentration").assign(inputContainerUsed, concentration1);
					
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
						
						// conc est définie en ng/µl (voir ExpirementServiceCNG.java / getPropertyDefinitionsQuantIt ); volume est en µL
						// --->  ng/µL * µL => ng
						$parse("newQuantity.unit").assign(inputContainerUsed, "ng");
						//console.log("calcQuantity="+calcQuantity + " ng");
					} else {
						console.log("valeur manquante pour calculer calcQuantity OU unité volume non gérée");
					}
				} else {
					// nécessaire à cause de la possibilité de décocher le bouton 'copyToContainer' uniquement avant 'terminer'
					//console.log( "set newConcentration et newQuantity =null");
					//Attention si volume1 renseigné alors calcul newQuantity
					inputContainerUsed.newConcentration = null;
					inputContainerUsed.newQuantity = null;
				} 
			}
		});
		
		// NGL_4153 mise à jour du message de concentrations manquantes
		//console.log("save puis copyPropertiesToInputContainer > checkMissing ..." );
		//$scope.expHasMissingConcentrations=checkMissingConcentrations();
		
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
			$scope.file = undefined;
			angular.element('#importFile')[0].value = null;
			$scope.$emit('refresh');
			
			// NGL-4153 Vérifier les concentrations manquantes apres import du fichier
			$scope.expHasMissingConcentrations=checkMissingConcentrations();
			
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
	
	//NGL-3198: import pour fluoroskan => inverser sens du test ( afficher bouton si pas qubit )
	$scope.button = {
		isShow:function(){
			return ($scope.experiment.instrument.typeCode !== "qubit" && !$scope.mainService.isEditMode() 
					&&  ( $scope.isInProgressState() || Permissions.check("admin")) )	
			},
		isFileSet:function(){
			return ($scope.file === undefined)?"disabled":"";
		},
		click:importData,		
	};
	
	
	//NGL-4153 ajout message alerte si concentrations manquantes
	$scope.missingConcentrations=0;
	
	function countConcentrations(){	
		var count=0;
		$scope.experiment.atomicTransfertMethods.forEach(function(atm){
			// !! 0 est une valeur valide pour le comptage !
			if ( atm.inputContainerUseds[0].experimentProperties && 
			     atm.inputContainerUseds[0].experimentProperties.concentration1 &&
			     (atm.inputContainerUseds[0].experimentProperties.concentration1.value !== null )){ 
				//console.log ( atm.inputContainerUseds[0].code +": conc="+ atm.inputContainerUseds[0].experimentProperties.concentration1.value);
				count++; 
			}
		});
		
		//console.log (count + " concentrations in experiment");
		return count;
 	}
	
	function checkMissingConcentrations(){
		if ( $scope.isCreationMode() ) return false; // les atomicTransfertMethods n'existent pas encore
		var nbInputs;
		nbInputs=$scope.experiment.atomicTransfertMethods.length;
		//console.log ('nb inputs: '+ nbInputs);
		
		$scope.missingConcentrations=nbInputs - countConcentrations();
		console.log('missing concentrations: '+ $scope.missingConcentrations );
		
		return ( $scope.missingConcentrations > 0 ? true : false );
	}
	
	$scope.expHasMissingConcentrations=checkMissingConcentrations();
	
}]);