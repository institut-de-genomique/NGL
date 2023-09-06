// GS dupliqué depuis manyToOne.experiments.cng.x-to-plates
// FDS Ajout PoolingMode (NGL-3401)=> ajouter mainService
angular.module('home').controller('XToPlatesCtrlCNS',['$scope', '$http','$parse', '$filter', 'mainService',
                                                               function($scope, $http, $parse, $filter, mainService ) {

	//-----------------------------------------reutilisables ailleurs ??----------------------------------------------------------
	// FDS 29/06/2016 calcul de la ligne en fonction de position 96 numerotée en mode colonne ( 1=A1, 2=B1... 9=A2...) [ thx NW ]
	var getLineFromPosition96_C = function(pos96){	
		var line= String.fromCharCode (((pos96 -1) % 8 ) + 65 );
		//console.log(">line ="+ line +" ("+pos96+")");
		return line;
	};
	
	//  FDS 29/06/2016  calcul de la colonne en fonction de position-96 numerotée en mode colonne ( 1=A1, 2=B1... 9=A2...) [ thx NW ]
	var getColumnFromPosition96_C = function(pos96){	
		var c=Math.floor((pos96 -1) / 8) +1 ;
		var column= c.toString();
		//console.log(">col ="+column+" ("+pos96+")");
		return column;
	};
	
	
	var getLineFromPosition96_L = function(pos96){	
		//TODO ??
	}
	
	var getColFromPosition96_L = function(pos96){	
		//TODO ??
	}
	
	
	// FDS 03/10/2016 calculer la position-96 en mode colonne a partir de ligne et colonne ( 1=A1, 2=B1... 9=A2...) 
	var getPos96FromLineAndCol_C = function(ln,col){
		if ( ln && col ){
			// numeroter a partir de 1 et pas 0!!   var pos= (col  -1 )*8 + ( ln.charCodeAt(0) -65);
			var pos96=(col -1 )*8 + ( ln.charCodeAt(0) -64);
			//console.log(ln+"/"+col+"=>"+pos96);
			return pos96;
		}
	}
	
	//  FDS 03/10/2016 calculer la position-96 en mode ligne a partir de ligne et colonne ( 1=A1, 2=A2...13=B1...)
	var getPos96FromLineAndCol_L = function(ln,col){
       //TODO ??
	}
	//---------------------------------------------------------------------------------------------------------------------------
	
	// s'execute a la creation de chaque ATM meme sans mise a jour de concentration le nom est trompeur!!!
	$scope.atmService.updateOutputConcentration = function(atm){
		
		if(atm){
		// ne pas faire l'update si déjà renseigné
			var concentration = undefined;
			var unit = undefined;
			var isSame = true;
			for(var i=0;i<atm.inputContainerUseds.length;i++){
				if(atm.inputContainerUseds[i].concentration !== null 
						&& atm.inputContainerUseds[i].concentration !== undefined){
					if(concentration === undefined && unit === undefined){
						concentration = atm.inputContainerUseds[i].concentration.value;
						unit = atm.inputContainerUseds[i].concentration.unit;
					}else{
						if(concentration !== atm.inputContainerUseds[i].concentration.value 
								|| unit !== atm.inputContainerUseds[i].concentration.unit){
							isSame = false;
							break;
						}
					}
				}else if(concentration !== undefined || unit !== undefined){
					isSame = false;
					break;
				}
			}
			if(isSame 
					&& (atm.outputContainerUseds[0].concentration === null
							|| atm.outputContainerUseds[0].concentration.value === null
						|| atm.outputContainerUseds[0].concentration === undefined
						|| atm.outputContainerUseds[0].concentration.value === undefined)){
				atm.outputContainerUseds[0].concentration = angular.copy(atm.inputContainerUseds[0].concentration);				
			}
			
			// FDS 29/06/2016 positionnement automatique de ligne et colonne sur une plaque
			//     19/10/2016 gerer les 2 cas plaques/tubes
			//console.log("instrument.outContainerSupportCategoryCode="+ $scope.experiment.instrument.outContainerSupportCategoryCode);
			//EJ Ne pas mettre les valeurs par défaut des lignes/colonnes demande d'Eric NGL-2542 
			if ( $scope.experiment.instrument.outContainerSupportCategoryCode === "96-well-plate" ){	
				//atm.outputContainerUseds[0].locationOnContainerSupport.column=getColumnFromPosition96_C(atm.viewIndex);
				//atm.outputContainerUseds[0].locationOnContainerSupport.line=getLineFromPosition96_C(atm.viewIndex);
			} else {
				atm.outputContainerUseds[0].locationOnContainerSupport.column=1;
				atm.outputContainerUseds[0].locationOnContainerSupport.line=1;
			}
				
			atm.column=atm.outputContainerUseds[0].locationOnContainerSupport.column;
			atm.line=atm.outputContainerUseds[0].locationOnContainerSupport.line;
		}
	};

	$scope.updateInputContainers = function (atm, propertyName, propertyValue) {
		if (atm) {
			atm.forEach(function(a) {
				if (a.inputContainerUseds) {
					a.inputContainerUseds.forEach(function (icu) {
						if (propertyName === "experimentProperties.inputVolume") {
							icu.experimentProperties = {
								inputVolume: {
									"_type": "single",
									value: propertyValue,	
									"unit": "µL"
								}
							};
						}
					});
				}
			});
		}
	};

	
    // Pseudo header de datatable => mise a jour de tous les outputContainers
	$scope.updateOuputContainers = function (atm, propertyName, propertyValue){
	    console.log (" updateOuputContainers :"+ propertyName + " changed");
		if (atm){
			console.log ("updating all ATMs "+ propertyName +" with :" + propertyValue);
		    atm.forEach(function(a){	
		    	
		    	 // 28/09/2017 supression des   && propertyValue car sinon on ne peut pas effacer completement le champ...
		    	 if (propertyName === "supportStorageCode")  {
		    		//console.log ("updating "+ a.outputContainerUseds[0].locationOnContainerSupport.storageCode +"=>"+ propertyValue);
		    		a.outputContainerUseds[0].locationOnContainerSupport.storageCode=propertyValue;
		    		
		    	 } else if  (propertyName === "supportCode")  {
		 		    //console.log ("updating "+ a.outputContainerUseds[0].locationOnContainerSupport.code +"=>"+ propertyValue);
		 		    a.outputContainerUseds[0].locationOnContainerSupport.code=propertyValue;
		 		 
		    	 } else if  ((propertyName === "concentration")||(propertyName === "volume")) {
			 		    //console.log ("updating "+propertyName+".value =>"+ propertyValue);
			 		    $parse(propertyName+'.value').assign(a.outputContainerUseds[0],propertyValue);
			 		   
			 		    // recalculer les volumes engagés et buffer
			 		    console.log("compute all input volumes");
			 			angular.forEach(a.inputContainerUseds, function(inputContainerUsed){
						computeInputVolume(inputContainerUsed, a);
						});
			 		    
			 			console.log("compute buffer volume");
			 			computeBufferVolume(a);
			 			
			     } else if  (propertyName === "conc_unit" ) {
			    	 //console.log ("updating concentration.unit =>"+ propertyValue);
			    	 $parse('concentration.unit').assign(a.outputContainerUseds[0],propertyValue);
			     }		    	 
		    })
		}
    };
	
	
	// /!\ controller commun a normalization-and-pooling et pool

    /* FDS 27/10/2016  la modification des proprietes line et column n'as pas besoin de recalculer les volumes!!
                       Vu avec Julie: la modification des proprietes experimentProperties.inputVolume et experimentProperties.bufferVolume
                       ne DOIT PAS refaire les calculs => c'est l'utilisteur qui impose son choix!
    */
		
	$scope.update = function(atm, containerUsed, propertyName){
		console.log("update "+propertyName);
		
		if(propertyName === 'outputContainerUseds[0].concentration.value' ||
				propertyName === 'outputContainerUseds[0].concentration.unit' ||
				propertyName === 'outputContainerUseds[0].volume.value'){
			
			console.log("compute all input volumes");
			angular.forEach(atm.inputContainerUseds, function(inputContainerUsed){
				computeInputVolume(inputContainerUsed, atm);
			});
			
			console.log("compute buffer volume");
			computeBufferVolume(atm);
			
		}else if(propertyName.match(/inputContainerUseds\[\d+\].percentage/) != null){
			console.log("compute one input volume");
			computeInputVolume(containerUsed, atm);
			
			console.log("compute buffer volume");
			computeBufferVolume(atm);
		} 
		else if(propertyName === 'outputContainerUseds[0].locationOnContainerSupport.line' ){
			atm.line =$parse("outputContainerUseds[0].locationOnContainerSupport.line")(atm)
			console.log("support.line="+atm.line);
		}
		else if(propertyName === 'outputContainerUseds[0].locationOnContainerSupport.column' ){
			atm.column =$parse("outputContainerUseds[0].locationOnContainerSupport.column")(atm)
			console.log("support.column="+atm.column);
		}
	}
	
	// calcul du "volume engagé"
	var computeInputVolume = function(inputContainerUsed, atm){
		var getter = $parse("experimentProperties.inputVolume");
		var inputVolume = getter(inputContainerUsed);
		if(null === inputVolume  || undefined === inputVolume){
			inputVolume = {value : undefined, unit : 'µL'};
		}
		
		//compute only if empty
		var compute = {
			inputPercentage : $parse("percentage")(inputContainerUsed),
			inputConc : $parse("concentration")(inputContainerUsed),
			outputConc : $parse("outputContainerUseds[0].concentration")(atm),
			outputVol : $parse("outputContainerUseds[0].volume")(atm)
		};
		
		// 28/09/2017 Julie demande de bloquer le calcul en normalization-and-pooling si unité de concentration n'EST PAS nM 
		//           mais autoriser pour le pool ??
		
		if($parse("(outputConc.unit ===  inputConc.unit)")(compute)){
			var result = $parse("(inputPercentage * outputConc.value *  outputVol.value) / (inputConc.value * 100)")(compute);
			console.log("result = "+result);
			
			if(angular.isNumber(result) && !isNaN(result)){
				inputVolume.value = Math.round(result*10)/10;				
			}else{
				inputVolume.value = undefined;
			}	
			getter.assign(inputContainerUsed, inputVolume);
		}else{
			inputVolume.value = undefined;
			getter.assign(inputContainerUsed, inputVolume);
		}
		
		return inputVolume.value;
	}
	
	// calcul du volume tampon a ajouter
	var computeBufferVolume = function(atm){
		
		var inputVolumeTotal = 0;
		var getterInputVolume = $parse("experimentProperties.inputVolume");
		
		atm.inputContainerUseds.forEach(function(icu){
			var inputVolume = getterInputVolume(icu);
			if(null === inputVolume  || undefined === inputVolume || undefined === inputVolume.value ||  null === inputVolume.value){
				inputVolumeTotal = undefined;
			}else if(inputVolumeTotal !== undefined){
				inputVolumeTotal += inputVolume.value;
			}						
		})
		
		var outputVolume  = $parse("outputContainerUseds[0].volume")(atm);
		
		if(outputVolume && outputVolume.value && inputVolumeTotal){
			var bufferVolume = {value : undefined, unit : 'µL'};
			var result = outputVolume.value - inputVolumeTotal;
			
			// Julie->FDS: laisser les cas negatifs...permet de voir qu'il y a un pb...!!
			if(angular.isNumber(result) && !isNaN(result)){
				bufferVolume.value = Math.round(result*10)/10;				
			}else{
				bufferVolume.value = undefined;
			}	
			
			$parse("outputContainerUseds[0].experimentProperties.bufferVolume").assign(atm, bufferVolume);
		}
	}
	

$scope.computeInSizeToOut= function(){
		
		console.log("Compute out size");
		var atm = $scope.atmService.data.atm;	
		angular.forEach(atm, function(value){
			var sizeTotal=0;
			var nbContentTotal=0;
			value.inputContainerUseds.forEach(function(icu){
				if(icu.size==null || icu.size.value==null || icu.contents==null || icu.contents.length==0){
					sizeTotal=undefined;
				}
				if(sizeTotal!=undefined){
					sizeTotal+=(icu.size.value*icu.contents.length);
					nbContentTotal+=icu.contents.length;
				}
			})
			if(value.outputContainerUseds!=null && sizeTotal!=undefined){
				var size= {value : undefined, unit : 'pb'};
				size.value= Math.round(sizeTotal/nbContentTotal*1)/1;
				$parse("outputContainerUseds[0].size").assign(value, size);
			}else{
				$scope.messages.setError(Messages('experiments.input.warn.unquantifiableSample'));		
				$scope.messages.clear();
				$scope.messages.clazz = "alert alert-warning";
				$scope.messages.text = "Le calcul \"moyenne pondérée des tailles\" ne peut pas s'effectuer car la taille est manquante chez au moins un container d'entrée";
				$scope.messages.showDetails = false;
				$scope.messages.open();
			}
			
		});
		$scope.atmService.data.updateDatatable();
	}
	
	if($scope.experiment.instrument.typeCode === "biomek-fx" && $scope.experiment.instrument.outContainerSupportCategoryCode !== "tube"
		&& $scope.experiment.instrument.inContainerSupportCategoryCode !== "tube"){
			$scope.setAdditionnalButtons([{
				isDisabled : function(){return $scope.isNewState();} ,
				isShow:function(){return !$scope.isNewState();},
				click: function(){return $scope.fileUtils.generateSampleSheet({'fdrType':"dna"})},
				label: Messages("experiments.sampleSheet")+ " / ADN"
			},{
				isDisabled : function(){return $scope.isNewState();} ,
				isShow:function(){return !$scope.isNewState();},
				click: function(){return $scope.fileUtils.generateSampleSheet({'fdrType':"buffer"})},
				label:Messages("experiments.sampleSheet")+ " / tampon"
			}]);		
	}
	
	if($scope.experiment.instrument.typeCode === "tecan-evo-100" && $scope.atmService.inputContainerSupportCategoryCode !== "tube" 
		&& $scope.experiment.instrument.outContainerSupportCategoryCode === "tube"){
		$scope.setAdditionnalButtons([{
			isDisabled : function(){return $scope.isNewState();} ,
			isShow:function(){return !$scope.isNewState();},
			click:$scope.fileUtils.generateSampleSheet,
			label:Messages("experiments.sampleSheet")
		}]);
	}

	//restriction tecan : forcer tube en sortie
	$scope.$watch("experiment.instrument.outContainerSupportCategoryCode", function(){
		if  ($scope.experiment.instrument.typeCode === "tecan-evo-100"){
			$scope.experiment.instrument.outContainerSupportCategoryCode = "tube";
			//To update view
			$scope.atmService.outputContainerSupportCategoryCode = "tube";
		}
	});
	
	var config = $scope.atmService.$atmToSingleDatatable.data.getConfig();
	config.otherButtons= {
		active : ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
        template: 
        	'<button class="btn btn-default" ng-click="computeInSizeToOut()" data-toggle="tooltip" ng-disabled="!(isEditModeAvailable() && isWorkflowModeAvailable(\'F\'))"  title="'+Messages("experiments.button.title.computeSizeWeighted")+'" "><i class="fa fa-magic" aria-hidden="true"></i> '+ Messages("experiments.button.computeSize")+' </button>'                	                	
    };
	
	
	var columns = $scope.atmService.$atmToSingleDatatable.data.getColumnsConfig();

	columns.push({
        "header": Messages("containers.table.contents.length"),
 		"property": "inputContainer.contents.length",
 		"filter": "getArray:'properties.secondaryTag.value'| unique",
 		"order":true,
 		"hide":true,
 		"type":"number",
 		"position":5.9,
        "extraHeaders":{0:Messages("experiments.inputs")}
	});

	columns.push({
        "header": Messages("containers.table.secondaryTags.shortLabel"),
 		"property": "inputContainer.contents",
 		"filter": "getArray:'properties.secondaryTag.value'| unique",
 		"order":true,
 		"hide":true,
 		"type":"text",
 		"position":6.9,
 		"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
        "extraHeaders":{0:Messages("experiments.inputs")}
	});
	
	columns.push({
		"header" : Messages("containers.table.size"),
		"property": "inputContainerUsed.size.value",
		"order" : true,
		"edit" : false,
		"hide" : true,
		"type" : "number",
		"position" :7.5,
		"extraHeaders" : {
			0 : Messages("experiments.inputs")
		}
	});
	
	
	columns.push({
		"header" : Messages("containers.table.size"),
		"property": "outputContainerUsed.size.value",
		"order" : true,
		"edit" : false,
		"hide" : true,
		"type" : "number",
		"position" :100,
		"extraHeaders" : {
			0 : Messages("experiments.outputs")
		}
	});
	
	
	$scope.atmService.$atmToSingleDatatable.data.setColumnsConfig(columns);

	// pour selects de position de sortie dans le cas des plaques !!!!
	//// TODO mettre ces tableaux dans le scala.html plutot qu'ici ???
	$scope.columns = ["1","2","3","4","5","6","7","8","9","10","11","12"]; 
	$scope.lines=["A","B","C","D","E","F","G","H"];  
	
	//NGL-3203 ajout information masquable; commencer masqué
	$scope.setIsShowInformation (false);
	
	
	
	//------------NGL-3401 22/11/2021 POOLING INTELLIGENT POUR SAMPLES AVEC BID ---------------------
	
	//???  pourquoi l'assignation par ng-model ne le fait pas automatiquement ???
	$scope.setPoolingMode=function(p){
		$scope.poolingMode=p;
		//console.log( "DEBUG selected poolingMode ="+ p.name);
	}
	
	//???  pourquoi l'assignation par ng-model ne le fait pas automatiquement ???
	$scope.toggleAllowNoNegControls=function(){
		$scope.allowNoNegControls===false ? $scope.allowNoNegControls=true : $scope.allowNoNegControls=false ;
		//console.log("allowNoNegControls="+$scope.allowNoNegControls);
	}
	
	//???  pourquoi l'assignation par ng-model ne le fait pas automatiquement ???
	$scope.toggleSortByBID=function(){
		$scope.sortByBID===false ? $scope.sortByBID=true : $scope.sortByBID=false ;
		//console.log("sortByBID="+$scope.sortByBID);
	}
	
	//???  pourquoi l'assignation par ng-model ne le fait pas automatiquement ???
	$scope.toggleSimulation=function(){
		$scope.simulation===false ? $scope.simulation=true : $scope.simulation=false ;
		console.log("simulation="+$scope.simulation);
	}
	//???  pourquoi l'assignation par ng-model ne le fait pas automatiquement ???
	$scope.setSamplesProject=function(s){
		$scope.samplesProject=s;
		//console.log("project="+$scope.samplesProject);
	}
	
	/* TEST pour effacer tous les atms créés par le pooling 
		MARCHE PAS...abandonné pour l'instant
		la suppression des ATMs un par un marche pourtant dans outputplates.scala.html!!!
		deleteATM(atm)  ne delete pas 1 atm particulier car "atm" est une liste !!!
		
	$scope.deleteAllATMs=function(){
		console.log('DELETE ALL ATMS');
		// !! certains ATMs peuvent avoir été créés manuellement 
		// le nombre d'ATM est alors > data.nbPools !!!
		
		// ne marche pas car deleteATM ne delete pas 1 atm particulier....
		$scope.atmService.data.atm.forEach(function(atm){
			console.log('>>>'+atm.viewIndex);
			console.log(atm);
			$scope.atmService.data.deleteATM(atm); 
			console.log('<<<');
			
		});
	
		// marche pas non plus ???
		var hasATMs=true;
		while (hasATMs){
			var atm=$scope.atmService.data.atm;
			console.log (atm);
			$scope.atmService.data.deleteATM(atm);
			if ( $scope.atmService.data.atm.length > 0 ? hasATMs=true: hasATMs=false );
		}
	}
	*/

	// ajouter autres modes si nécessaire...
	// noms modifiés orde=> consécutifs; progressigs=> en parallèle
	$scope.poolingModes=[ {code:'manual', name:'Libre'},
						  {code:'bidPooling_orderedPools', name:'BID: Pools consécutifs'},
						  {code:'bidPooling_progPools',    name:'BID: Pools en parallèle'}
						];
	
	
	// l'algorithme de "pooling intelligent" a été développé pour le projet CCU, mais une variable permet à l'utilisateur de le changer si nécessaire !!
	// valeurs par défaut déclarés avant l'appel aux fonctions de pooling type BID pour l'affichage initial
	
	$scope.poolingMode=$scope.poolingModes[0]; // par defaut => "Libre"
	$scope.samplesProject="CCU"; 
	$scope.allowNoNegControls=false;
	$scope.sortByBID=false;
	$scope.simulation=true;	// permet de tester plusieurs algorithme sans faire effectivement les pools
							// pour passer ensuite dans l'algorithme choisi, décocher l'option simulation
	
	$scope.doPooling=function (){
		//console.log( "poolingMode ="+ $scope.poolingMode.name);
		
		// on peut dans la même expérience cumuler les modes !! comment l'interdire ???
		switch ( $scope.poolingMode.code ){
			case 'manual':
				// fonctionnement manuel ancien 
		    	$scope.atmService.data.dropInSelectInputContainer(); ///===> cree un seul pool avec la selection courante
				break;
			case 'bidPooling_orderedPools':
				// ne pooler que si qq chose dispo...
				if ( $scope.atmService.data.inputContainers.length > 0 ){
					poolData=bidPooling('orderedPools');
					finalMessage(poolData);
				}
				break;
			case 'bidPooling_progPools':
				// ne pooler que si qq chose dispo...
				if ( $scope.atmService.data.inputContainers.length > 0 ){
					poolData=bidPooling('progPools');
					finalMessage(poolData);
				}
				break;
			//place pour autres algos sans rien a voir avec BID pooling...
		}
	}
	
	// Uniquement pour le bidPooling !!!
	function finalMessage(data) {
		if ($scope.alert.Problemes.length > 0 ){
			if ($scope.simulation) {
				$scope.messages.clazz = "alert alert-warning";
				//$scope.messages.text = Messages("???");
				$scope.messages.text ="Simulation de pooling"; // mettre dans message ?
			} else {
				$scope.messages.clazz = "alert alert-danger";
				//$scope.messages.text = Messages("???");
				$scope.messages.text ="Erreurs de pooling"; // mettre dans message ?
			}
			
			$scope.messages.showDetails = true;
			$scope.messages.open();	
		} else {
			if ($scope.simulation) {
				$scope.messages.clazz = "alert alert-warning";
				$scope.messages.text ="Simulation de pooling : création de "+ data.nbPools+ " pools";
				
				// affichage des pools en mode simulation: OK !!!
				$scope.alert = { pools:[] };
				
				for ( p=0; p < data.nbPools; p++ ){
					$scope.alert['pools'].push(data.pool[p].bid);
					$scope.messages.setDetails($scope.alert);
				}
				
				$scope.messages.showDetails = true;
				$scope.messages.open();	
			}
		}
	}
	
	function bidPooling(algo){
		console.log('=== BID POOLING ===')
		
		/* 29/07/2022 TEST ALGO V2 / NGL-3904
			il existe des samples d'interet qui n'ont pas de concentration. 
			Dans l'algo original ces echantillons stoppent l'algo (car la concentration est necessaire pour compter le nombre d'ecantillons faibles...')
			test: considérer ces échantillons sans concentration comme des témoins ??????
			
			Vérifications supplémentaires à faire ?????
				* controles négatifs sans concentration ou unité nM=============> NON car aucune règle ne l'utilise'
				* samples d'intéret avec concentration en nM
		*/
		
		//réinitialiser les messages
		$scope.messages.clear();
		$scope.alert = { Problemes:[] };
		
		//DEBUG
		console.log("poolingMode= "+$scope.poolingMode.code);
		console.log("allowNoNegControls= "+$scope.allowNoNegControls);
		console.log("sortByBID= "+$scope.sortByBID);
		console.log("samplesProject= "+$scope.samplesProject);
		console.log("simulation= "+$scope.simulation);
		
		// globale?
		var config={
			lowConcVal:2, // seuil de concentration minimale; passer en parametre dans version future ???
			lowConcMax:1, // nbre maximal de samples avec conc <= lowConc par pool; passer en parametre dans version future ???
			poolSizeTreshold:5, // un pool avec taille > 5 element doit avoir 2 negControl, si taille <=5 alors 1 seul negControl
			maxPoolSize:96, // y a-t-il un maximum d'elements par pool ???
			minSampInPool:3 // y a-t-il un minimum de samples par pool ???( par defaut 2 sinon c'est plus un pool!!!)
		};
		
		// globale?
		var data={ 
			negControl:[], // tableau pour les controles négatifs (containers)
			sample:[],     // tableau pour les samples d'intérêt' (containers)
			bid:[],        // tableau des BIDS trouvés (strings)
			pool:[],       // tableau des pools
			maxBid:undefined,         // valeur du BID le plus représenté ( nombre)
			nbPools:undefined,        // nombre de pools a créer
			nbNegContInPool:undefined,// nombre de controle négatif par pool
			nbSampInPool:undefined    // nombre de sample d'intérete par pool
		};
		

		// vérifier qu'il y a au moins un inputContainer avant de commencer ?????
		
		
		//-- 0 -- remplissage des 3 tableaux 
		parseContainers(data);
		if ( $scope.alert.Problemes.length > 0 ){
			// ne pas continuer !!
			console.log('FAILED in parseContainers');
		} else {
			// DEBUG
			console.log('-- NB negControls='+ data.negControl.length +' --');
			console.log('-- NB samples='+ data.sample.length +' --');
			console.log('-- TOTAL='+ ( data.negControl.length + data.sample.length) +' --');
			
			// --1-- calcul de parametres
			setParams(data,config);
			if ( $scope.alert.Problemes.length > 0 ) {
				// ne pas continuer !!
				console.log('FAILED in setParams');
			} else {
				// --3-- tri des samples par bid ?? 
				if ( $scope.sortByBID){
					console.log('SORT !!!');
					// sample.sort((a,b) => (a.bid > b.bid) ? 1 : ((b.bid > a.bid) ? -1 : 0));

					data.sample.sort( function (a,b) { return (a.contents[0].properties.secondaryTag.value > b.contents[0].properties.secondaryTag.value) 
					                         ? 1 : ((b.contents[0].properties.secondaryTag.value > a.contents[0].properties.secondaryTag.value) 
                                             ? -1 : 0)});
				}
				
				// --4-- initialisation des pools
				for ( p=0; p < data.nbPools; p++ ){
					data.pool[p]={
						"bid":[],       // liste des BID déjà dans le pool
						"container":[], // liste des containers du pool (controles et samples)
						"lowConc":0     // nbre de samples a faible concentration
					};
				}
				
				// --5-- 2 algo possibles  
				if (algo === 'orderedPools') orderedPools(data,config);
				else                         progPools(data,config);
				
				/// erreurs bloquantes en sortie des algos ?
				if ( $scope.alert.Problemes.length > 0 ) {
					// ne pas continuer !!
					console.log('FAILED in pooling simulation');
				}  else {
					// --6-- jusqu'ici les pools ne sont que théoriques...creer réllement les pools ?
					if ( ! $scope.simulation) { 
						realPooling(data); 
						
						if ( $scope.alert.Problemes.length > 0 ) {
							console.log('FAILED in real pooling');
						}
					}
				}
			}
		}
		console.log('=== BID POOLING END ===');
		return data;
	}
	
	function realPooling(data){
		console.log('--- realPooling ---');
		// utiliser  _addToOutputContainer   pour marquer les containers a transférer dans un pool (avec hightlight graphique)
		//           $scope.atmService.data.dropInSelectInputContainer() pour créer UN oll
					
		for ( p=0; p < data.nbPools; p++ ){
			//console.log('  remplissage effectif du pool '+p);
			angular.forEach(data.pool[p].container, function(c){
				// utiliser $filter pour éviter une boucle 
				var matchContainers = $filter('filter')($scope.atmService.data.inputContainers, {code: c.code}, true);
				if (matchContainers && matchContainers.length === 1) {
					//console.log ('ajouter input container '+ matchContainers[0].code);
					matchContainers[0]._addToOutputContainer = true;
				} else { 
					// le match est incorrect ????
					console.log('PB de $filter ???');
					setAlert('Problemes','Erreur interne !!!');
				}
			});
			console.log('  création effective du pool '+p);
			$scope.atmService.data.dropInSelectInputContainer();
		}
		
	}
	
	function orderedPools(data,config){
		console.log('---- orderedPools ----');
		
		for ( p=0; p < data.nbPools; p++ ){
			console.log('>>>pool '+p);
			dispatchNegControls(p,data,config);
			dispatchSamples(p,data,config);
		}
		
		finalize(data,config);
	}
	
	function progPools(data,config){
		console.log('---- progPools ----');
		
		//--1-- boucler sur les negControls et les dispatcher dans tous les pools jusqu'a épuisement
		for ( n=1; n<= data.nbNegContInPool; n++ ){
			console.log('>>> neg '+n);
			dispatchNegControls2(data,config);
		}
		
		//--2-- boucler sur les samples et les dispatcher dans tous les pools jusqu'a épuisement
		for ( s=1; s<= data.nbSampInPool; s++ ){
			console.log('>>> samp '+s);
			dispatchSamples2(data,config);
		}
		
		finalize(data,config);
	}
	

	function dispatchNegControls2(data,config){
		// parcourir les pools et trouver 1 negControl qui convient
		for ( p=0; p < data.nbPools; p++ ){
			
			// arrêter si on épuisé tous les controles !!!
			if ( data.negControl.length == 0) {
				//console.log('PLUS DE CONTROLE DISPONIBLE');
				break;
			}
			
			// parcourir les negControls a la recherche du premier qui valide les critères
			 for ( var c=0; c < data.negControl.length; c++ ){
				var found=false;
				
				//console.log( 'pool '+p+' test negControl '+ data.negControl[c].code);
				// bid est le secondary tag !
				var controlBid=data.negControl[c].contents[0].properties.secondaryTag.value;
				
				// 1 seul critère: bid non déjà présent dans le pool
				if ( ! data.pool[p].bid.includes(controlBid) ){
					found=true;
					break;
				}
			}
			
			if ( found ){
				console.log( '=>'+  data.negControl[c].code+'['+ controlBid +']');
				
				//mettre le control c dans les containers du pool courant
				data.pool[p].container.push(data.negControl[c]);
				
				//mettre le bid du control dans les bids du pool
				data.pool[p].bid.push(controlBid);
				
				//supprimer le controle de la liste des controles disponibles
				data.negControl.splice(c,1);
				//console.log('new negControl.length='+ data.negControl.length);
				
			} else {
				//aucun controle ne valide les critères !!
				//console.log('=> aucun controle disponible avec les critères demandés ( bid non deja present dans le pool )');
			}
		}
	
	//console.log(pool[p]);
	}
	
	function dispatchNegControls(p,data,config){
		//il y a nbNegContInPool a mettre dans le pool p
		for ( j=1; j<= data.nbNegContInPool; j++ ){
			//console.log('nbNegContInPool '+j);
			
			// arrêter si on a épuisé tous les controles !!!
			if ( data.negControl.length == 0) {
				//console.log('PLUS DE CONTROLE DISPONIBLE');
				break;
			}
			
			// parcourir les negControls à la recherche du premier qui valide les critères
			for ( var c=0; c < data.negControl.length; c++ ){
				var found=false;
				
				//console.log( 'pool '+p+' test negControl '+ data.negControl[c].code );
				// bid est le secondary tag !
				var controlBid=data.negControl[c].contents[0].properties.secondaryTag.value;
				
				// 1 seul critère: bid non déjà présent dans le pool
				if ( ! data.pool[p].bid.includes(controlBid) ){
					found=true;
					break;
				}
			}
			
			if ( found ){
				//console.log( '=>'+  data.negControl[c].code+'['+ controlBid +']');
				
				//mettre le control c dans les containers du pool courant
				data.pool[p].container.push(data.negControl[c]);
				
				//mettre le bid du control dans les bids du pool
				data.pool[p].bid.push(controlBid);
				
				//supprimer le controle de la liste des controles disponibles
				data.negControl.splice(c,1);
				//console.log('new negControl.length='+ data.negControl.length);
			} else {
				//aucun controle ne valide les critères !!
				//console.log('=> aucun controle disponible avec les critères demandés ( bid non déjà présent dans le pool )');
			}
		}
	
	//console.log(pool[p]);
	}
	
	function dispatchSamples2(data,config){
		
		// boucler sur les pools
		for ( p=0; p < data.nbPools ; p++ ){
			
			// arrêter si on a épuisé tous les samples !!!
			if ( data.sample.length == 0) {
				//console.log('PLUS DE SAMPLE DISPONIBLE');
				break;
			}
			
			// parcourir les samples à la recherche du premier qui valide les critères
			 for ( var s=0; s < data.sample.length; s++ ){
				var found=false;
				
				var sampleBid=data.sample[s].contents[0].properties.secondaryTag.value;
				var sampleConc= data.sample[s].concentration.value;
				//console.log('pool '+ p+ ': check sample :'+ s +':'+ data.sample[s].code +' bid: ' +sampleBid+' conc: '+sampleConc);
				
				// critère 1: bid pas déjà présent dans le pool
				if ( ! data.pool[p].bid.includes( sampleBid) ){
					// critère 2: concentrations faibles
					// éliminé au début...if (sample[s].concentration === ""){ console.log('concentration manquante !');}
					if ( (sampleConc >= config.lowConcVal) || (sampleConc < config.lowConcVal && data.pool[p].lowConc < config.lowConcMax) ){
 						found=true;
						break;
					} else {
						console.log (data.sample[s].code +' : conc faible: '+ sampleConc +' et il y a déjà '+ config.lowConcMax+ ' samples faibles le pool '+p);
						// il faut essayer un autre sample
					}
				} else {
					//DEBUG
					//console.log( 'sample '+data.sample[s].code+' : bid déjà dans le pool: '+ sampleBid);
				}
			}
			
			if ( found ){
				//console.log( '=>'+  data.sample[s].code+'['+sampleBid+']');
				
				//mettre le sample s dans les containers du pool courant
				data.pool[p].container.push( data.sample[s]);
				
				//mettre le bid du sample dans les bids du pool
				data.pool[p].bid.push(sampleBid);
				
				//s'il s'agit d'un sample a faible conc incrementer
				if ( sampleConc < config.lowConcVal ) { data.pool[p].lowConc++ ; }
				
				//supprimer le sample de la liste des samples disponibles
				data.sample.splice(s,1);
				//console.log('new sample.length='+data.sample.length);
			} else {
				//aucun sample ne valide les critères !!
				//console.log('=> aucun sample disponible avec les critères demandés ( bid non déjà présent dans le pool / conc faible)');
			}
		}
		
		//console.log(pool[p]);
	}
	
	function dispatchSamples(p,data,config) {
		
		//il y a nbSampInPool a mettre dans le pool p
		for ( j=1; j<= data.nbSampInPool; j++ ){
			
			// arrêter si on a épuisé tous les samples !!!
			if ( data.sample.length == 0) {
				//console.log ('PLUS DE SAMPLE DISPONIBLE');
				break;
			}
		
			// parcourir les samples à la recherche du premier qui valide les critères
			for ( var s=0; s < data.sample.length; s++ ){
				var found=false;
				
				var sampleBid=data.sample[s].contents[0].properties.secondaryTag.value;
				var sampleConc= data.sample[s].concentration.value;
				//console.log('pool '+ p+ ': check sample :'+ s +':'+ data.sample[s].code +' bid: ' +sampleBid+' conc: '+sampleConc);
				
				// critère 1: bid pas déjà présent dans le pool
				if ( ! data.pool[p].bid.includes( sampleBid) ){
					// critère 2: concentrations faibles
					// éliminé au debut... if (sample[s].concentration === ""){ throw('conc manquante !');}
					if ( (sampleConc >= config.lowConcVal) || (sampleConc < config.lowConcVal &&  data.pool[p].lowConc < config.lowConcMax) ){
						found=true;
						break;
					} else {
						//console.log (data.sample[s].code +' : conc faible: '+ sampleConc +' et il y a déjà '+ config.lowConcMax+ ' samples faibles dans le pool '+p);
						// essayer le sample suivant dans la liste
					}
				} else {
					//DEBUG
					//console.log( 'sample '+data.sample[s].code+' : bid déjà dans le pool: '+ sampleBid);
				}
			}
			
			if ( found ){
				//console.log( '=>'+  data.sample[s].code+'['+sampleBid+']');
				//mettre le sample s dans les containers du pool courant
				data.pool[p].container.push( data.sample[s] );
				
				//mettre le bid du sample dans les bids du pool
				data.pool[p].bid.push( sampleBid );
				
				//s'il s'agit d'un sample à faible conc incrementer
				if ( sampleConc < config.lowConcVal ) { data.pool[p].lowConc++ ; }
			
				//supprimer le sample de la liste des samples disponibles
				data.sample.splice(s,1);
				//console.log('new sample.length='+data.sample.length);
			} else {
				//problème, aucun sample ne valide les critères !!
				//console.log('=> aucun sample disponible avec les criteres demandés ( bid non déjà présent dans le pool / conc faible )');
			}
		}
		
		//console.log (pool[p]);
	}

	function finalize(data,config){
		//traitement final en cas de "restes"
		console.log('--- FINALIZE ---');
		
		// jamais vu !!!!
		// if ( data.negControl.length !== 0 ){ console.log('ERREUR il reste des controles non utilisés !'); console.log(negControl); }
	
		if ( data.sample.length !== 0 ){
			console.log('WARNING 1: il reste '+ data.sample.length+ ' samples non utilisés !');
			
			/* trier les pool en commencant par ceux qui ont le moins de samples...
			 https://stackoverflow.com/questions/1129216/sort-array-of-objects-by-string-property-value
			 objs.sort((a,b) => (a.last_nom > b.last_nom) ? 1 : ((b.last_nom > a.last_nom) ? -1 : 0))
			*/

			//pool.sort((a,b) => (a.nbCont > b.nbCont) ? 1 : ((b.nbCont > a.nbCont) ? -1 : 0));
			// ===>NON NE CHANGE RIEN!!!
			
			// utiliser dispatchSamples2 pour essayer de répartir les samples restants....	
			dispatchSamples2(data,config);
			// verifier a nouveau s'il y a des "restes"'
			if ( data.sample.length !== 0 ){
				// si oui c'est in problème !!!!'
				console.log('WARNING 2: il reste '+ data.sample.length+ ' samples non utilisés !');
				setAlert('Problemes','Impossible d\'aller au bout de l\'algorithme');
			} else {
				console.log('SUCCES 2');
			}
		} else {
			console.log('SUCCES 1');
		}
	}

	function parseContainers(data){
		console.log('parseContainers...');
		//boucler sur les inputContainers de l'experience
		
		// !! en mode creation $scope.experiment.atomicTransfertMethod n'est pas encore chargé
		//    => passer par Basket (ajouter mainService dans le controller)
		//             supprimer si basket plus utilisé !!!!!!
		
		/*  ce cas est pour l'instant filtré dans le html... si mélange d'input 
		  var categoryCodes = $scope.$eval("getBasket().get()|getArray:'support.categoryCode'|unique",mainService);
		if ( (categoryCodes.length === 1) && ( categoryCodes[0] === "96-well-plate") ){
		*/
			
			var inputContainers= $scope.atmService.data.inputContainers;
			angular.forEach(inputContainers, function(container){
				//console.log('INPUT CONTAINER '+ container.code);
				// si plusieurs contents => plusieurs BID !!, plusieurs projets !!!==> donc exclure
				if ( container.contents.length == 1){
					/// !!!!! il faut imperativement tester la presence d'un BID ( sample ET controles )
					if ( hasSecondaryTag(container)){
						if      (isNegControl(container)) { data.negControl.push(container);}
						else if (isSample(container)){ 
							if (missingConcentration(container)){
								/* NGL-3904 essai: considerer ces échantillons comme des NegControl ?????
								   // sans concentration pour un sample il sera impossible d'excuter l'algorithme !!!
								   setAlert('Problemes','Container '+ container.code + ': concentration manquante');
								*/
								data.negControl.push(container);
							} else {
								// NEW tester l'unité de la concentration ????
								if ( ! ngulConcentration(container)){
									setAlert('Problemes','Container '+ container.code + ': unité de concentration non gérée: '+ container.concentration.unit);
								} else {
									data.sample.push(container);
								}
							}
						} else {	
							setAlert('Problemes','Container '+ container.code + ': incorrect project/taxon ['+  container.projectCodes[0] + '/'+ container.contents[0].taxonCode+ ']');
						}
						
						// le bid est un tag secondaire
						//console.log('BID '+ container.contents[0].properties.secondaryTag.value);
						data.bid.push(container.contents[0].properties.secondaryTag.value);
					} else {
						setAlert('Problemes','Container '+ container.code + ': pas de BID');
					}
				} else {
					setAlert('Problemes','Container '+ container.code + ': plusieurs contents');
				}
			});
		/* } else {
			setAlert('only-plates ?????'); 
		}
		*/
	} // fin parseContainers
	
	// amélioration 30/06/2022
	function missingConcentration(container){
		if (   !container.concentration || container.concentration === undefined || container.concentration === ""  
			|| !container.concentration.value || container.concentration.value === undefined || container.concentration.value === ""
			|| container.concentration.value === 0 ){
			return true;
		} else { return false }
	}
	function ngulConcentration(container){
		if ( container.concentration && container.concentration.unit && container.concentration.unit !== 'ng/µl' ){
			console.log('>>'+ container.concentration.unit +' unsupported concentration unit');
			return false;
		} else { return true; }
	}

	function setParams(data,config){
		console.log('setParams...');

		// -1- trouver la valeur du BID le plus représenté (pas échec possible ?)
		setMaxBid(data);
		
		// -2- calculer le nbre de pools a créer (peut positionner une alerte)
		setNbPools(data); 	
		if ( $scope.alert.Problemes.length == 0 )
		{
			// -3-trouver le nombre de sample d'intérêt par pool (peut positionner une alerte)
			setNbSampInPool(data, config);
		}
	}
	
	function setNbSampInPool(data,config){
		// moyenne du nombre d'éléments par pool (controles et samples confondus)
		var averageElementsInPool= (data.negControl.length + data.sample.length) / data.nbPools;

		//arrondir à l'entier au dessus
		var poolSize=Math.ceil(averageElementsInPool);
		console.log('-- NB éléments par pool='+ poolSize +' --');
		
		/* Y a-t-il un maximum d'éléments dans un pool ?
		// bug dans la version original: manque "config;"
		if ( poolSize > config.maxPoolSize ){
			console.log('Plus que '+maxPoolSize+' éléments par pool !');
		}  
		*/
		

		//nombre de sample d'intéret par pool
		data.nbSampInPool=poolSize - data.nbNegContInPool;  // !! peut etre 0!! (cas intercepté avec l'ajout controle sur poolsize)
		console.log('nbSampInPool -> '+ data.nbSampInPool);
		
		/* Y a-t-il un minimum de samples par pool ??  OUI sinon ce n'est plus un pool !!!
		if ( data.sample.length  < (  config.minSampInPool * data.nbPools )) {
			console.log ( 'Il faudrait au minimum '+ (  config.minSampInPool *  data.nbPools ) + ' samples  mais il n y en a que '+  data.sample.sample.length);
 		}*/
		if ( poolSize < config.minSampInPool ){
			setAlert('Problemes','Moins de '+ config.minSampInPool+' containers par pool.');
		}
		
		// si poolSize > 5 alors nbNegContInPool doit etre=2 ;  si poolSise <=5 alors nbNegContInPool doit etre=1
		// bloquant ou pas ?? si oui ==> SetAlert
		if ( (poolSize > config.poolSizeTreshold ) && ( data.nbNegContInPool < 2 ) ){
			console.log('WARNING: poolSize >'+ config.poolSizeTreshold +'=> il faudrait 2 controles/pool (il n\'y a pas assez de controles!)');
		} else if ( (poolSize < config.poolSizeTreshold ) && ( data.nbNegContInPool > 2) ){
			console.log('WARNING: poolSize <'+ config.poolSizeTreshold+'=> il ne devrait y avoir que 1 control/pool (il y a trop de controles !)');
		}
	}
	
	function setMaxBid(data){
		// --compter la répartition des BIDs ( samples + negControls)
		var bids=getCount(data.bid);// c'est un objet...
		//console.log('distribution globale des BIDs:');
		//console.log(bids); 
		
		// bid le plus représenté. si plusieurs bids ont la meme valeur ??? marche quand même
		data.maxBid=getMaxVal(bids);
		console.log('maxBid -> '+ data.maxBid);
	}
	
	//parcourir les éléments d'un object: https://stackoverflow.com/questions/8312459/iterate-through-object-properties
	function getMaxVal(bids){
		var maxVal=0;
		//Object.entries(obj).forEach( function (key,value)  { //erreur ??
		//Object.entries(obj).forEach( function ([key,value]){ //mieux!! mais passe pas non plus au build !!
		// => passer par Object.keys !!
		Object.keys(bids).forEach( function (key){
			//console.log( 'NEW: key='+key + ' value='+bids[key] )
			if (bids[key] > maxVal ) {
				maxVal=bids[key];
			}
		});
		
		return maxVal;
	}
	
	//--- compter les différents BIDs (résultats dans un object)
	//https://stackoverflow.com/questions/368280/javascript-hashmap-equivalent
	function getCount(arr){
		var count = {};
		var maxC=0;
		arr.forEach(function (el){
			count[el] = 1  + (count[el] || 0)
			if ( count[el] > maxC) { 
				maxC=count[el];
			}
		});
		return count;  // c'est un objet !
	}

	function setNbPools(data){
		// le nombre de pools a créer dépend essentiellement de la valeur du bid le plus présent !!!
		// dans la foulée nbNegContInPool (nombre de controles négatifs par pool) est aussi défini
		//ATTENTION: la contrainte dans les specs est: un pool avec > 5 containers doit avoir 2 controles
		//           mais le nombre de containers par pool dépend du nombre total de containers...
		
		// si le nombre de negControl < bid le plus représenté, certains pools n'auront pas de Controles
		if ( data.negControl.length < data.maxBid){
			var nbNoNegContPools= data.maxBid - data.negControl.length;
			console.log ('negCont < maxBid : '+ nbNoNegContPools + ' pool(s) n\'aura(ont) pas de controle ?');
			
			// paramètre permettant de continuer quand meme !!!
			if ( $scope.allowNoNegControls ){
				
				data.nbPools=data.maxBid; // nb Pools=maxBid
				data.nbNegContInPool=1;   // chaque pool contiendra 1 controle ( mais pas tous...)
			} else {
				// Stop !!
				setAlert('Problemes', nbNoNegContPools + ' pool(s) n\'aura(ont) pas de controle.');
			}
		} else {
			// il y assez de controles pour tous les pools
			// peut on en mettre 2 ?
			if ( (data.negControl.length)/2 < data.maxBid ){
				// non
				data.nbPools=data.negControl.length; // nb pools =nb controls
				data.nbNegContInPool=1;              // chaque pool contiendra 1 controle
			} else {
				// Oui
				data.nbNegContInPool=2; // chaque pool contiendra 2 controles
				// si negControl.length est pair (exemple 62 => 31 pools à créer)
				// si negControl.length est impair (exemple 63:  63-1= 62   62/2=31 donc nbre final de pools à créer: 31+1=32 )
				// ceil => arrondir a l'entier supérieur
				data.nbPools=Math.ceil(data.negControl.length/2);
			}
		}
		console.log ('nbPools ->'+	data.nbPools);
		console.log ('nbNegContInPool ->'+ data.nbNegContInPool);
	}
	
	function setAlert(msgKey, msgDetails){
		$scope.alert[msgKey].push(msgDetails);
		$scope.messages.setDetails($scope.alert);
	}
	
	function hasSecondaryTag(container){
		if ( container.contents[0].properties.secondaryTag ) return true;
		else return false;
	}
	
	function isNegControl(container){
		// projectCode CEB et CDX sont valables pour tous les projets ???????
		// taxon control: taxonName= unidentified/taxonCode = 32644 
		// le cas des samples avec plusieurs contents a déjà été exclu
		//   => donc on traite uniquement container.projectCodes[0] et container.contents[0].taxonCode 
		if ( (container.projectCodes[0] === 'CEB') ||  // controle générique
			 (container.projectCodes[0] === 'CDX') ||  // controle générique
			 (container.projectCodes[0] === $scope.samplesProject && container.contents[0].taxonCode === '32644')){ // controle spécifique
			return true; 
		} else {
			return false;
		}
	}
	
	function isSample(container){
		// taxon control: taxonName= unidentified/taxonCode = 32644 
		// le cas des samples avec plusieurs contents a déjà été exclu,
		//   => donc on traite uniquement container.projectCodes[0] et container.contents[0].taxonCode 
		if (container.projectCodes[0] === $scope.samplesProject && container.contents[0].taxonCode !== '32644' ) return true;
		else return false;
	}
	
	// 26/08/2022 Essai image d'aide
	$scope.showModalHelp=function(){
		console.log("showModale");
 		angular.element('#infoModale').modal('show');
	}
	
}]);