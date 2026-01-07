// NGL-4638 ajout $filter
angular.module('home').controller('OneToVoidFluoQuantificationCNGCtrl',['$scope', '$parse','$http', '$filter',
                                                                 function($scope, $parse, $http, $filter) {

	/* 15/01/2020 FINALEMENT laisser le tri par defaut  column/line
	//NGL-2755: trier sur line/colonne par défaut (colonnes héritées du controleur parent one-to-void-qc-ctrl.js)
	// attention on n'est pas forcement en mode plaque... ne pas écraser le order.by hérité
	if($scope.experiment.instrument.inContainerSupportCategoryCode == "96-well-plate"){
		config.order.by =['inputContainer.support.line','inputContainer.support.column*1'];
		$scope.atmService.data.setConfig(config );
	}
	*/

	var inputExtraHeaders=Messages("experiments.inputs");

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
		"extraHeaders":{0:inputExtraHeaders}
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
		"extraHeaders" : {0 : inputExtraHeaders}
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
	   	 "extraHeaders":{0 :inputExtraHeaders}
	  }); 

	// concentration toujours affichée position (displayOrder 13 dans ExperimentServiceCNG.java / getPropertyDefinitionsQuantIt )
	// NGL-4638 colonnes qui ne s'affichent selon valeur displayChoice!! 
	columns.push({
		"header": Messages("containers.table.volume")+ " (μl)",
		"property" : "inputContainerUsed.volume.value",
		"order":true,
		"edit":false,
		"hide":true,
		"type":"text",
		"position":14,
		"extraHeaders":{0 :inputExtraHeaders}
 	});
	// Quantité library", "quantity1" : displayOrder 15
	
	// Rendement library PacBio (%)", "pacbioLibraryYield": displayOrder 17

	$scope.atmService.data.setColumnsConfig(columns);

	// NGL-4638 modification de l'affichage dynamique sur choix utilisateur: classique ou pacbio (d'apres cns/reception-fluo-quantification-ctrl.js)
	// pour modification
	$scope.$watch("experiment.experimentProperties.displayChoice.value", function(newValue, oldValue){
		if (newValue !== oldValue)  {
			toggleColumns($scope.atmService, newValue);
		}
	});

	// pour affichage initial
	if($scope.experiment.experimentProperties.displayChoice.value){
		toggleColumns($scope.atmService, $scope.experiment.experimentProperties.displayChoice.value);
	}

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
	//  26/06/ ajout de l'affichage de la qté==> ajout propriété d'experience quantity1 !!!!!
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
						// conc est définie en ng/µl (voir ExperimentServiceCNG.java / getPropertyDefinitionsQuantIt ); volume est en µL --->  ng/µL * µL => ng
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
	};

	// NGL-4638 
	// updatePropertyFromUDT  est automatiqut defini pour les colonnes injectees dans le datatable....
	$scope.updatePropertyFromUDT = function(value, col){
		console.log("update from property : "+col.property);

		if(col.property === 'inputContainerUsed.experimentProperties.concentration1.value'){
			computeQuantity1(value.data);
			computeYield(value.data);
		} 
	}

	//NGL-4638 l'affichage de la quté est nécessaire meme si la copie n'est pas demandée !!
	//////////  conflits ??????
	//////////  traiter unités  !!!
	//////////  pourquoi  computeQuantity  et  computeQty  plus haut ??
	var computeQuantity1 = function(udtData){
		var getterQuantity1= $parse("inputContainerUsed.experimentProperties.quantity1.value");
		var quantity1= getterQuantity1(udtData);

		var computeQuantity = {
				// c'est le volume du container en input....
				inputVol : $parse("inputContainerUsed.volume.value")(udtData),
				inputConc1 : $parse("inputContainerUsed.experimentProperties.concentration1.value")(udtData),
				isReady:function(){
					return (this.inputVol && this.inputConc1);
				}
		};

		if(computeQuantity.isReady()){
			var result = $parse("(inputVol * inputConc1)")(computeQuantity);
			console.log("computeQuantity1: result = "+result);
			if(angular.isNumber(result) && !isNaN(result)){
				quantity1 = Math.round(result*10)/10;	
			}else{
				quantity1 = undefined;
			}	
			getterQuantity1.assign(udtData, quantity1);
		}else{
			getterQuantity1.assign(udtData,undefined);
			console.log("not ready to computeQuantity1");
		}
	};

	// cette méthode ne fonctionnera que s'il existe "inputContainerUsed.contents[0].properties.pacbioLibraryInputQuantity.value"
	// ==> uniquement si on est dans un processus PacBio!!!
	var computeYield = function(udtData){
		var getterYield=$parse("inputContainerUsed.experimentProperties.pacbioLibraryYield.value");
		var yield= getterYield(udtData);

		var computeYield = {
			inputQuantityPacbio :  $parse("inputContainerUsed.contents[0].properties.pacbioLibraryInputQuantity.value")(udtData),
			inputQuantity1 : $parse("inputContainerUsed.experimentProperties.quantity1.value")(udtData),
			isReady:function(){
				// il faudrait aussi qu'on ait les memes unités ???
				return (this.inputQuantityPacbio && this.inputQuantity1);
			}
		};
		if(computeYield.isReady()){
			var result = $parse("(inputQuantity1 / inputQuantityPacbio *100)")(computeYield);
			console.log("computeYield: result = "+result);
			if(angular.isNumber(result) && !isNaN(result)){
				yield = Math.round(result*10)/10;	
			}else{
				yield = undefined;
			}	
			getterYield.assign(udtData, yield);
		}else{
			getterYield.assign(udtData,undefined);
			console.log("not ready to computeYield");
		}
	}

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

	// NGL-4638
	function toggleColumns( atmService, displayChoice ){
		var udt = atmService.data;
		var columns = udt.getColumnsConfig(columns);
		// !! garder en cohérence avec les valeurs de position déclarées dans le datatable et experience
		// 14=volume; 15=Quantité; 17=Rendement
		for(var i = 14; i <= 17 ; i++){
			var selectedColumns = $filter('filter')(columns,{position:i});
				if(selectedColumns.length === 1){
					var column = selectedColumns[0];
					//console.log( column.position);
					// tester contre "Expérience Classique", perlmet de changer l'autre label si nécessaire
					if      ((displayChoice !== "Expérience Classique" ) &&   udt.isHide(column.id)){ udt.setHideColumn(column);}
					else if ((displayChoice == "Expérience Classique" ) && ! udt.isHide(column.id)){ udt.setHideColumn(column);}
				} // else =plusieurs colonne a la meme position !!!!
		}
	}
	
	$scope.expHasMissingConcentrations=checkMissingConcentrations();
	
}]);