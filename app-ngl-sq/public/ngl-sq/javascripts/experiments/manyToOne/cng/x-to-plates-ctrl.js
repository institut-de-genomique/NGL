// FDS 21/06/2016 dupliqué depuis manyToOne.experiments.cns.x-to-tubes avec modifs
// /!\ controller commun a normalization-and-pooling et pool
// FDS Ajout de choix de positions (NGL-5175)
angular.module('home').controller('XToPlatesCtrl',['$scope','$http','$parse','$filter', 
                                             function($scope, $http, $parse, $filter) {


//-- NGL-5175 methodes de calcul de position déplacées dans experiment/details-ctrl.js plateUtils et poolUtils

//??????? initaliser ????
$scope.outputMode=undefined;
	
// pour desactiver les boutons du outputMode
$scope.manualPoolingStarted=false;

//pour arrêter l'algo de calcul de position
$scope.ATMposError=false;

$scope.buttonsConsistancyError=false;

/* vérifier consistance des boutons startPositionXX / maxOutputXX */
function checkOutputModeButtonsConsistancy(mode){
	if ( mode === 'C' ){
		var startLine=$scope.startPositionColumn.charAt(0);

		// si startLine  >   $scope.maxOutputLine alors pb pour l'algo de calcul de position ATM !!!!
		// comparaison de strings !!!!
		if ( startLine >  $scope.maxOutputLine ){
			$scope.messages.setError("Output Mode : ligne du puit de départ > ligne max.");
			$scope.buttonsConsistancyError=true;
		} else {$scope.buttonsConsistancyError=false; }
	} else {
		// mode=L
		var startCol=$scope.startPositionLine.substr(1,2);

		// si startCol  >   $scope.maxOutputColumn alors pb pour l'algo de calcul de position ATM !!!!
		if ( parseInt(startCol) > parseInt($scope.maxOutputColumn) ){
			$scope.messages.setError("Output Mode : colonne du puits de départ > colonne maxw.");
			$scope.buttonsConsistancyError=true;
		} else {$scope.buttonsConsistancyError=false; }
	}
}





	
	//---------------------------------------------------------------------------------------------------------------------------
	
	// APR 28/02/2020 met le numéro de colonne au début du code, pour que le tri se fasse d'abord par colonne
	// FDS 03/05/2021 correction algo: incorrect pour colonnes 10,11,12 + ne marche pas pour les tubes
	//                inverser col et ligne si plaque XXXX_[ligne][col]  =>  XXXX_[col][ligne] ( avec aussi ajout 0 si col <10 )
	/// utilisé ou ?
	$scope.atmService.sortByColumn = function(obj) {
		var code = obj.code;

		//var columnIndex = Number(code.substr(code.length - 1)); //FDS: FAUX  !! dans le cas des colonnes 10,11,12 il faudrait faire -2 
		//                                                                     !! ne marche pas pour les tubes
		//console.log("columnIndex ="+columnIndex);
		//return columnIndex + code;
		
		// tester d'abord si  contient "_"   !!! pour les tubes !!!
		if ( null != code.match(/_/g) ) {
			var arr=code.split("_"); 
			var bar=arr[0]; // partie barcode du code container
			var pos=arr[1]; // partie position du code container
			var ligne=pos.substring(0,1);  // récupérer la partie ligne (1 char)
			var   col=pos.substring(1);    // récupérer la partie colonne ( 1 ou 2 char)
			var columnIndex = Number(col)<10 ? '0'+col : col; // ajouter 0 si col<10
			//console.log("return="+bar+"_"+columnIndex + ligne);
			return bar+"_"+columnIndex + ligne ;
		} else {
			//console.log("return="+code);
			return code;
		}
	};
	
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
			if ( $scope.experiment.instrument.outContainerSupportCategoryCode === "96-well-plate" ){	
				//FDS NGL-5175 positionner les valeurs lignes/colonnes en prennant en compte les 3 nouveaux parametres
				//    startPosition / outputMode / maxOuputLine ou maxOutputColumn
				if ( ! $scope.ATMposError ) {
					// si une erreur a été trouvée précédemment, inutile de continuer de calculer...
					var ATMpos=$scope.poolUtils.calculateATMPlatePosFromIndex(atm.viewIndex, $scope.outputMode, $scope.startPositionColumn, $scope.maxOutputLine, $scope.startPositionLine,$scope.maxOutputColumn );
					if ( ATMpos.error){
						$scope.messages.setError(ATMpos.error);
						// pour arreter le calcule de position des ATM suivant ???
						$scope.ATMposError=true;
					}
				}
				console.log( "ATMpos.line="+ ATMpos.line + "...ATMpos.col="+ATMpos.col);

				atm.outputContainerUseds[0].locationOnContainerSupport.column=ATMpos.col;
				atm.outputContainerUseds[0].locationOnContainerSupport.line=ATMpos.line;
			} else {
				atm.outputContainerUseds[0].locationOnContainerSupport.column=1;
				atm.outputContainerUseds[0].locationOnContainerSupport.line=1;
			}
				
			// copier les positions dans l'atm
			atm.column=atm.outputContainerUseds[0].locationOnContainerSupport.column;
			atm.line=atm.outputContainerUseds[0].locationOnContainerSupport.line;
			console.log ("atm line="+ atm.line);
			console.log ("atm column="+ atm.column);
		
			// au CNG il y a d'autre valeurs a mettre a jour
			atm.outputContainerUseds[0].locationOnContainerSupport.code=$scope.outputContainer.supportCode;
			console.log("setting locationOnContainerSupport.code="+$scope.outputContainer.supportCode);	
			
			atm.outputContainerUseds[0].locationOnContainerSupport.storageCode=$scope.outputContainer.supportStorageCode;
			console.log("setting locationOnContainerSupport.StorageCode: "+$scope.outputContainer.supportStorageCode);
			
			// 19/10/2016  affectation de WorkName. !! GA: la propriete n'existe pas=> la creer avec methode angular => parse + assign
			// 11/04/2025 ajouter 0 a l'index dupool si < 10 : pool01,pool02...pool10...
			//$parse('experimentProperties.workName.value').assign(atm.outputContainerUseds[0],$scope.outputContainer.experimentRootWorkName+atm.viewIndex);
			$parse('experimentProperties.workName.value').assign(atm.outputContainerUseds[0],$scope.outputContainer.experimentRootWorkName+getWorkNameIdx(atm.viewIndex));
		}
	};
	
	// 11/04/2025
	var getWorkNameIdx=function(atmViewIndex){
		var idx = atmViewIndex < 10 ? "0"+atmViewIndex : atmViewIndex ;
		return idx;
	}

    // Pseudo header de datatable => mise a jour de tous les outputContainers
	$scope.updateOuputContainers = function (atm, propertyName, propertyValue){
	    console.log (">> updateOuputContainers :"+ propertyName + " changed");
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
	
	// A servi pour déterminer quels mode  de pooling est laissé à l'utilisateur: bouton ou select ?
	// pour l'instant décision de toujours laisser les 2 modes actifs...
	//    $scope.getInstrumentCategoryCode= function() { return $scope.experiment.instrument.categoryCode; }
	
	// 06/2016 FDS: Boutons "Action"
	//   => pas de boutons generateSampleSheet pour la main
	
	//console.log ("instrument="+ $scope.experiment.instrument.categoryCode + " / experiment="+$scope.experiment.typeCode);	
	if ($scope.experiment.instrument.categoryCode !== "hand") {
         // 06/2016 FDS: 2 boutons pour 2 generateSampleSheet de type different
		 /* 25/10/2016 le mode 2 boutons est-il valide pour tous les type d'instrument ?
		          Janus    : Oui
		          Epimotion: ?? pour l'instant laisser 2 boutons...
		 */
			$scope.setAdditionnalButtons([{
				isDisabled : function(){return $scope.isNewState();} ,
				isShow:function(){return !$scope.isNewState();},
				click: function(){return $scope.fileUtils.generateSampleSheet({'fdrType':"samples"})},
				label: Messages("experiments.sampleSheet")+ " / échantillons"
			},{
				isDisabled : function(){return $scope.isNewState();} ,
				isShow:function(){return !$scope.isNewState();},
				click: function(){return $scope.fileUtils.generateSampleSheet({'fdrType':"buffer"})},
				label:Messages("experiments.sampleSheet")+ " / tampon"
			}]);		
	}

	/* 19/10/2016 Only tube is authorized for hand....  NON laisser le choix!!
	$scope.$watch("$scope.experiment.instrument.categoryCode", function(){
			if ($scope.experiment.instrument.categoryCode === "hand")
				$scope.experiment.instrument.outContainerSupportCategoryCode = "tube";
	});	
	*/

	 
	// pour selects de position de sortie dans le cas des plaques !!!!
	$scope.columns = ["1","2","3","4","5","6","7","8","9","10","11","12"]; 
	$scope.lines=["A","B","C","D","E","F","G","H"];

	// 10/2024 NGL-4764 tableaux avec valeur nulle initiale pour la valeur non selectionnée
	///utiliser le delimiteur de string "  et pas '  ( sinon '---' n'est pas correctement interprété: ce n'est pas un char !!)
	$scope.maxColumns = ["---","1","2","3","4","5","6","7","8","9","10","11","12"]; 
	$scope.maxLines=["---","A","B","C","D","E","F","G","H"];  

	// Tableaux générés ( ajouter "---" avec unshift)
	$scope.modeLinePlatePositions=$scope.plateUtils.generatePlatePositions(96,'line');
	$scope.modeLinePlatePositions.unshift("---");
	
	$scope.modeColumnPlatePositions=$scope.plateUtils.generatePlatePositions(96,'column');
	$scope.modeColumnPlatePositions.unshift("---");

	$scope.maxOutputColumn=$scope.maxColumns[0]; // init 
	$scope.maxOutputLine=$scope.maxLines[0]; // init 
	$scope.startPositionLine=$scope.modeLinePlatePositions[0]; //init
	$scope.startPositionColumn=$scope.modeColumnPlatePositions[0]; //init
	
    // experimentRootWorkName= préfixe par defaut pour génération automatique des labels de travail
    $scope.outputContainer = { supportCode: null, supportStorageCode: null, experimentRootWorkName:"pool", concentration: null, conc_unit: null, volume: null};
    
    // 01/10/2016--------------------------- pooling "automatique"--------------------------------------------------------------------
	// 01/2025 NGL-4845, possibilité d'avoir plusieurs plaques en entrée (au lieu d'une seule) et de pooler a cheval sur ces plaques
	$scope.poolingModes=[];

	switch ($scope.experiment.instrument.typeCode){
  		case 'janus':
			// meme au Janus si les entrees ne sont pas des plaques le seul mode possible est "libre"
			$scope.poolingModes.push({code: 'MOUSE', name:'libre'});

			//  supportCategoryCode contient 'tube' dans le cas d'un mix de containers !!!!!!
			//console.log ( ">>>>>>>>>> "+$scope.supportCategoryCode);
		
			if ( $scope.supportCategoryCode === "96-well-plate"){
				$scope.poolingModes.unshift ( {code: 'NONE',  name:'---'} ); // le mettre en position [0] de la liste pour qu'il soit le mode par defaut

				/* 10/2024 autopoolling sur plaques complètes (position de début et positin fin de pooling pas demandées !)
				         => startLine:'A', startColumn: 1, endLine:'H', endColumn:12  ne servent a rien a finalement !!!

				   NGL-4845 02/2025 le mode pooling C9 doit utiliser plusieurs plaques en entrée!
				   cas 1 : pre-prod: faire 16 pools de 9 [ 2 colonnes dans plaque en sortie => 2 lanes a déposer]
				           ==> il faut une plaque et demi
						   cas 1a:  1 plaque 96 complète + 1/2 plaque 96 ===> fin du pooling plaque 2 en position H6 !
						   cas 1b:  1/2 plaque 96 + 1 plaque 96 complète ===> debut du pooling plaque 1 en position A7 !
					cas 2 : en routine faire 32 pools de 9 [ 4 colonnes dans plaque en sortie => 4 lanes a déposer]
					  		 => il faut 3 plaques 96 complètes
				*/    
	
				//-1 mode colonne
   				// 28/03/2018 NGL-1971: ajout 3 modes de pool de type colonne
	  			$scope.poolingModes.push ( {code: 'C4', name:'col 4-plex', type:'C', poolPlex: 4, startLine:'A',startColumn: 1,endLine:'H',endColumn:12 });
	   			$scope.poolingModes.push ( {code: 'C6', name:'col 6-plex', type:'C', poolPlex: 6, startLine:'A',startColumn: 1,endLine:'H',endColumn:12 });
	   			$scope.poolingModes.push ( {code: 'C8', name:'col 8-plex', type:'C', poolPlex: 8, startLine:'A',startColumn: 1,endLine:'H',endColumn:12 });

				//NGL-4845: C9
				$scope.poolingModes.push ( {code: 'C9', name:'col 9-plex', type:'C', poolPlex: 9, startLine:'A',startColumn: 1,endLine:'H',endColumn:12 });

				//-2 mode ligne
				// 17/09/2024  NGL-4750 : ajout pool en ligne 12 plex ; Ajouté par incompréhension du ticket !!
				// les robots (janus compris) travaillent avec des porte-cones en mode colonne, le mode ligne ne sert donc a rien
				//$scope.poolingModes.push({code: 'L12',  name:'ligne 12-plex', type:'L', poolPlex: 12, startLine:'A',startColumn: 1,endLine:'H',endColumn:12});
			
				//-3 mode Illumina Dual
				/* 20/10/2024 plus utilisé en prod !!! demandent a être supprimé
				$scope.poolingModes.push({code: 'ID4', name:'ill Dual 4-plex', poolPlex: 4, type:'C', startLine:'A',startColumn: 1,endLine:'H',endColumn:12});
    			$scope.poolingModes.push({code: 'ID6', name:'ill Dual 6-plex', poolPlex: 6, type:'C', startLine:'A',startColumn: 1,endLine:'H',endColumn:12});
    			$scope.poolingModes.push({code: 'ID8', name:'ill Dual 8-plex', poolPlex: 8, type:'C', startLine:'A',startColumn: 1,endLine:'H',endColumn:12});
				*/
			}
		
			break;

		//14/10/2024  pour la main et le tecan une seule possibilité
		case 'hand':
		case 'tecan-evo-150':
			$scope.poolingModes.push({code: 'MOUSE', name:'libre'});
			break;
	}

	// 10/11/2016 ajout setPoolingMode  !!! pourquoi l'assignation par ng-model ne le fait pas automatiquement ????
	$scope.poolingMode=$scope.poolingModes[0]; // init

	$scope.autoPoolingMode=false;

	// pour les cas d'impossibilité de démarrer l'autoPooling
	$scope.autoPoolingError=false;
	
	// Classe pour identifier quel mode est actif !!
	$scope.getBtnClass=function(curr_om, set_om){
		if ( curr_om === set_om ){ return 'btn-primary'}
		return 'btn-default'
	}
	
	$scope.setPoolingMode=function(pmode){
		console.log("DEBUG selected poolingMode ="+ pmode.name);

		// annuler le pooling précédent
		if ($scope.atmService.data.atm.length != 0){
			resetPreviousPooling();
		}
		$scope.messages.clear();

		// reinitialiser meme si le pooling n'a pas été lancé
		// ne fonctionnent pas correctement !!!
		resetMaxOutputLine(); 
		resetMaxOutputColumn();
		resetStartPositionLine(); 
		resetStartPositionColumn(); 

		$scope.outputMode=undefined;

		// pas de checkAutopooling pour mode "libre" ni "---"
		if ( pmode.code !== 'MOUSE' && pmode.code !== 'NONE'){
			$scope.autoPoolingMode=true;
			$scope.autoPoolingError=false;
			//Verifier si les conditions pour autopooling sont validées
			if ( checkAutoPooling(pmode) === true){
		  		$scope.poolingMode=pmode; 
			}
			// et sinon ??? => reste sur la valeur par defaut!!
			//console.log("DEBUG!!! "+$scope.poolingMode.name);
		} else {
			$scope.poolingMode=pmode; 
			$scope.autoPoolingMode=false;
		}
	}

	var resetPreviousPooling=function(){
		console.log ("removing all ATMs");
		
		$scope.atmService.data.atm.forEach( function(atm){
				$scope.atmService.data.deleteATM(atm);
		});
		// deleteATM ne marche pas comme prevu en meme en manuel!!! => reste 1 atm a la fin !!
		if ($scope.atmService.data.atm[0]) {
			console.log("removing LAST ATM !!");
			$scope.atmService.data.deleteATM($scope.atmService.data.atm[0]);
		}

		if ($scope.poolingMode.code !== 'MOUSE'){
			$scope.autoPoolingDone=false;
			$scope.autoPoolingError=false;
		} else {
			//en mode manuel
			$scope.manualPoolingStarted=false;
		}
	}

	// 10/2024 fonction séparée
	var checkAutoPooling=function(pmode){
		// commencer par effacer de potentiels messages d'erreur précedents
		$scope.messages.clear();

    	if ( pmode.code === "C9" ) {
			console.log (' nb plaques='+ $scope.nbInputContainerSupports );
			/* 05/03/2025 il reste le probleme de l'onglet source/Destination incorrectement trié si l'utilisateur revient
			 sur une experience a nouveau avec 2 plaques et qu'il a changé l'ordre des plaques...Solution a trouver!!
			 ==> bloquer a 3 plaques pour la version actuelle
			if ( $scope.nbInputContainerSupports != 2 && $scope.nbInputContainerSupports != 3   ) {*/
			// le cas 2 plaques a besoin de plus de tests !!!
			if ($scope.nbInputContainerSupports != 3) {
				$scope.messages.setError("Ce mode de pooling ne fonctionne qu'avec 3 plaques 96 puits en entrée.");
				$scope.autoPoolingError=true; // pas nécessaire mais plus cohérent pour le futur ?
				return false;
			} else {
				return true;
			}
		} else { 
			// les autres modes de pooling utilisent des valeurs paires de plex et ne chevauchent pas les plaques
			// le nombre de plaques utilisables n'est donc pas limité 
			return true;
		}
	}

	var checkMaxPools=function(){
		/* Verifier que les valeurs max de ligne ou colonne sont compatibles avec le nbre de plex et de plaques
		  !! ici ce sont les pools théoriques possibles et pas les pools réels !!
		  Autre cas a prendre en compte, l'utilisateur n'a pas mis explicitement une valeur max de colonne ou de ligne
		  ce sont les valeurs par defaut qui sont utilisées mais ca dépasse 96 ! le message doit être different

		  NGL-5175 peut on inclure dans ce calcul la position de départ sur la plaque de sortie ????
		*/

		var totalPools= ( 96 / $scope.poolingMode.poolPlex ) * $scope.nbInputContainerSupports; // harcoded plaque 96 puits !!
		console.log("total pools="+ totalPools);
		var errmessage="";

		if ($scope.outputMode == "C"){   
			var maxLine=$scope.plateUtils.lineCharToNum($scope.maxOutputLine);   // A=1, B=2..                              
			maxPools= 12 * maxLine; // nbColonne Hardcoded pour plaque 96
			//console.log("max pools="+ maxPools);

			if ( totalPools > maxPools){
				if ($scope.maxOutputLine == "H") {
					errmessage="Pool Mode: Le nombre de plaques en entrée ("+  $scope.nbInputContainerSupports +") x le Pool Mode demandé ("+ $scope.poolingMode.poolPlex +") génèrent plus que 96 pools ("+ totalPools +").";
				} else {
					errmessage="Pool Mode: La valeur de ligne maximum choisie pour la plaque de sortie ("+ $scope.maxOutputLine + ") ne permet de déposer que "+ maxPools + " pools sur les "+ totalPools + " (théoriques).";
				}
			} 
		} else {
			//outputMode ="L"
			maxPools= 8 * $scope.maxOutputColumn; // harcoded plaque 96 puits (8x12) !!
			//console.log("max pools="+ maxPools);

			if ( totalPools > maxPools){
				if ( $scope.maxOutputColumn == "12") {
					errmessage="Pool Mode: Le nombre de plaques en entrée ("+  $scope.nbInputContainerSupports +") x le Pool Mode demandé ("+ $scope.poolingMode.poolPlex +") génèrent plus que 96 pools ("+ totalPools +").";
				} else {
					errmessage="Pool Mode: La valeur de colonne maximum choisie pour la plaque de sortie ("+ $scope.maxOutputColumn + ") ne permet de déposer que "+ maxPools + " pools sur les "+ totalPools + " (théoriques).";
				}
			} 
		}

		return errmessage;
	}

	// 10/2024 NGL-4764 sélection du mode se sortie
	$scope.outputMode=undefined; // init pour l'inativation du bouton "output"
	$scope.maxOutputLine=$scope.maxLines[0]; 
	$scope.maxOutputColumn=$scope.maxColumns[0];
	//console.log ('outputMode ='+ $scope.outputMode);

	resetMaxOutputLine=function(){
		// select ne se rafraichit pas dans certains cas !!! bug angular??
		$scope.maxOutputLine=$scope.maxLines[0]; 
		//$scope.maxOutputLine="---"; // ne change rien !!
		//console.log("RESET maxOutputLine ==>"+$scope.maxOutputLine);
	}

	resetMaxOutputColumn=function(){
		// select ne se rafraichit pas dans certains cas!!! bug angular??
		$scope.maxOutputColumn=$scope.maxColumns[0]
		//$scope.maxOutputColumn="---"; // ne change rien !!
		//console.log("RESET maxOutputColumn ==>"+$scope.maxOutputColumn);
	}

	resetStartPositionLine=function(){
		// select ne se rafraichi pas dans certains cas !!! bug angular ??
		$scope.startPositionLine=$scope.modeLinePlatePositions[0];
		//$scope.startPositionLine="---";// ne change rien !!
		//console.log("RESET startPositionLine ==>"+$scope.startPositionLine);
	}

	resetStartPositionColumn=function(){
		// select ne se rafraichi pas dans certains cas !!! bug angular ??
		$scope.startPositionColumn=$scope.modeColumnPlatePositions[0];
		//$scope.startPositionColumn="---";// ne change rien !!
		//console.log("RESET startPositionColumn ==>"+$scope.startPositionColumn);
	}

	// dans scope car appellée depuis scala
	$scope.setStartPositionLine=function(start){
		$scope.startPositionLine=start; // !!! pourquoi l'assignation par ng-model ne le fait pas automatiquement ???? 
		//console.log("DEBUG1 selected startPositionLine ="+ $scope.startPositionLine);
		$scope.setOutputMode('L');
		resetStartPositionColumn()// réinitialiser l'autre select

		// vérifier cohérence des choix
		$scope.buttonsConsistancyError=checkOutputModeButtonsConsistancy('L');
	}

	// dans scope car appellée depuis scala
	$scope.setStartPositionColumn=function(start){
		$scope.startPositionColumn=start; // !!! pourquoi l'assignation par ng-model ne le fait pas automatiquement ???? 
		//console.log("DEBUG1 selected startPositionColumn ="+ $scope.startPositionColumn);
		$scope.setOutputMode('C');
		resetStartPositionLine()// réinitialiser l'autre select

		// vérifier cohérence des choix
		$scope.buttonsConsistancyError=checkOutputModeButtonsConsistancy('C');
	}

	// dans scope car appellée depuis scala
	$scope.setMaxOutputLine=function(mol){
		$scope.maxOutputLine=mol; // !!! pourquoi l'assignation par ng-model ne le fait pas automatiquement ????
		//console.log("DEBUG2 maxOutputLine ="+ $scope.maxOutputLine);
		$scope.setOutputMode('C'); 
		resetMaxOutputColumn();// réinitialiser l'autre select

		// annuler une potentielle erreur de pooling précédente
		$scope.autoPoolingError=false;
		$scope.messages.clear();

		// vérifier cohérence des choix
		$scope.buttonsConsistancyError=checkOutputModeButtonsConsistancy('C');		
	}

	// dans scope car appellée depuis scala
	$scope.setMaxOutputColumn=function(moc){
		$scope.maxOutputColumn=moc; // !!! pourquoi l'assignation par ng-model ne le fait pas automatiquement ???? 
		//console.log("DEBUG2 maxOutputColumn ="+ $scope.maxOutputColumn);	
		$scope.setOutputMode('L');
		resetMaxOutputLine();// réinitialiser l'autre select

		// annuler une potentielle erreur de pooling précédente
		$scope.autoPoolingError=false;
		$scope.messages.clear();

		// vérifier cohérence des choix
		$scope.buttonsConsistancyError=checkOutputModeButtonsConsistancy('L');
	}

	// dans scope car appellée depuis scala
	// ajout param caller pour essayer d'alléger et voir si c'est l'origine du bug des reset ???pas mieux...
	$scope.setOutputMode=function(om, caller){
    	$scope.outputMode=om; // !!! pourquoi l'assignation par ng-model ne le fait pas automatiquement ????
		//console.log("DEBUG0 selected outputMode ="+ $scope.outputMode + ' from '+ caller);

		if (om === 'C' && caller === 'computeColumnMode') { 
			$scope.maxOutputLine="H";        //$scope.maxOutputLine=$scope.maxLines[8]; // essai; idem
			$scope.startPositionColumn="A1"; //$scope.startPositionColumn=$scope.modeColumnPlatePositions[1];// essai; idem
			//console.log("DEBUG0 set default maxOutputLine ="+ $scope.maxOutputLine);
			//console.log("DEBUG0 set startPositionColumn ="+ $scope.startPositionColumn);

			resetMaxOutputColumn();     // refresh select ne marche plus une fois un choix effectué !!
			resetStartPositionLine();   // refresh select ne marche plus une fois un choix effectué !!

		} else if ( om === 'L' && caller === 'computeLineMode'){
			$scope.maxOutputColumn="12";    //$scope.maxOutputColumn=$scope.maxColumns[12]; // essai; idem
			$scope.startPositionLine="A1";  //$scope.startPositionLine=$scope.modeLinePlatePositions[1];// essai; idem
			//console.log("DEBUG0 set default maxOutputColumn ="+ $scope.maxOutputColumn);
			//console.log("DEBUG0 set startPositionLine ="+ $scope.startPositionLine);

			resetMaxOutputLine();        // refresh select ne marche plus une fois un choix effectué !!
			resetStartPositionColumn();  // refresh select ne marche plus une fois un choix effectué !!
		}

		// annuler une potentielle erreur de pooling précédente
		$scope.autoPoolingError=false;
		$scope.messages.clear();

		// reset du flag d'erreur de calcul de position
		$scope.ATMposError=false;
	}

    // dans scope car appellée depuis scala
    $scope.doPooling=function (){
		// effacer de potentiels messages d'erreur précedents
		$scope.messages.clear();
    	console.log("poolingMode ="+ $scope.poolingMode.name);
    	
    	if ( $scope.poolingMode.code === "MOUSE") {
    		// traiter le pool créé a la souris
    	    $scope.atmService.data.dropInSelectInputContainer();
			console.log('MANUAL POOLING STARTED');
			$scope.manualPoolingStarted=true;
		} else {
    		//lancer la creation auto...si checkMaxPools OK
			message=checkMaxPools();
			if ( message != "" ){
				$scope.messages.setError(message);
				// il faut aussi désactiver le bouton "Pool"
				$scope.autoPoolingError=true;
			} else {
    			autoPooling($scope.poolingMode);
			}
		} 	
    }
    
	$scope.autoPoolingDone=false;

	// $scope.getInputContainerSupports est dans controller parent !!
	$scope.inputContainerSupports=$scope.getInputContainerSupports(); // array of string
	$scope.inputContainerSupports.sort(); // 17/03/2025 remettre le tri du tableau pour avoir les plaques dans l'ordre des barcodes
	$scope.nbInputContainerSupports=$scope.inputContainerSupports.length;

	// NGL-5049 mécanisme pour modifier l'ordre des plaques meme pour 3 plaques !! 
	$scope.toggleInputPlatesOrder=function(a,b){
		var temp=$scope.atmService.data.inputContainerSupports[a];
		$scope.atmService.data.inputContainerSupports[a]=$scope.atmService.data.inputContainerSupports[b];
		$scope.atmService.data.inputContainerSupports[b]=temp;

		//recupérer le nouveau tableau qui sera utilisé pour l'autopooling
		$scope.inputContainerSupports=$scope.atmService.data.inputContainerSupports;
	}

	/* !!! Dans le cas d'un pool C9 sur 2 ou 3 plaques, les plaques peuvent avoir été inversées par l'utilisateur !!!
	   quand on revient sur une expérience à l'état "Nouveau (IP)" il faut remettre ce meme ordre dans l'onget Source/destination
	   sinon l'utilisateur peut placer ses plaques sur le robot dans l'ordre par défaut qui peut n'être pas le bon et donc générer 
	   des pools incorrects !!
	*/
	$scope.SetInputPlatesOrderIfNeeded=function(){
		if( ! $scope.isCreationMode() && ( $scope.nbInputContainerSupports === 2 || $scope.nbInputContainerSupports === 3 )){
            console.log ("SetInputPlatesOrderIfNeeded...");
			var inputCode=[];

			if ( $scope.nbInputContainerSupports === 2 || $scope.nbInputContainerSupports === 3 ){
				// grace a l'ATM1 trouver la plaque input 0; grace a l'ATM16 trouver la plaque input 1
				var ATM1=$filter('filter')($scope.atmService.data.atm, {viewIndex:1}, true); ///
				var ATM16=$filter('filter')($scope.atmService.data.atm, {viewIndex:16}, true);
				inputCode.push(ATM1[0].inputContainerUseds[0].code.split('_')[0]);    console.log ('inputCode[0] ='+inputCode[0]);
				inputCode.push(ATM16[0].inputContainerUseds[0].code.split('_')[0]);   console.log ('inputCode[1] ='+inputCode[1]);
				$scope.inputContainerSupports[0]=inputCode[0];
				$scope.inputContainerSupports[1]=inputCode[1];
			}

			if ( $scope.nbInputContainerSupports === 3 ){
				// grace a l'ATM32 trouver la plaque input 2;
				var ATM32=$filter('filter')($scope.atmService.data.atm, {viewIndex:32}, true);
				inputCode.push(ATM32[0].inputContainerUseds[0].code.split('_')[0]);   console.log ('inputCode[2] ='+inputCode[2]);
				$scope.inputContainerSupports[2]=inputCode[2];
			}
        }
    }

	/* ng-model marche pas avec un objet pour un input radio ???
	$scope.C9_2platesPositions={startPos:1, endPos:144};
	*/
	$scope.C9_2PlatesDispo="1_144"; //init   devrait etre initialisé par ng-model !!
	$scope.dispoPositions={startPos:1, endPos:144};
	$scope.toggleDispo=function(dispo){
		//console.log('...TOGGLE DISPO....');
		if      (dispo==="1_144") { $scope.dispoPositions={startPos:1,  endPos:144} }
		else if (dispo==="49_192"){ $scope.dispoPositions={startPos:49, endPos:192} }
		else {
			throw 'incorrect dispo value '+dispo;
		}
	}

	/* 20/02/2025 creer une fonction isNotReadyPooling pour alleger le scala et mieux ajouter prevoir de nouveaux cas !!
 	bouton desactivé si :
				      - aucun pooling mode sélectionné ( cas possible avec pool vers plaque 96:  un des choix listé  est "---")
					  - mode "libre"  ET rien sélectionné à la souris  ET entree autre que plaque
					  - mode "libre"  ET rien sélectionné à la souris  ET entree plaque
					                                                    pas de ouputmode selectionné
					  - mode "auto"   ET qq chose selectionné
					                     pooling mode null
										 autoPooling terminé
										 autoPooling en erreur
										 /// >1 plaque en entrée  ( en complément du message emis dans le controleur js) 
					  - pb de consistance des choix utilisateur
					ajout cas outputContainerSupportCategoryCode === null a cause du cas strip-8 non supporté..
	*/
	$scope.isNotReadyPooling=function(){
		if      ( $scope.atmService.outputContainerSupportCategoryCode === null) { return true; } //a cause du cas strip-8 non supporté..
		else if ( $scope.poolingMode.code === 'NONE' ){ return true; }
		else if ( $scope.poolingMode.code === 'MOUSE' && $scope.atmService.outputContainerSupportCategoryCode !== '96-well-plate' && ! $scope.atmService.data.isSelectInputContainers() ) { return true; }
		else if ( $scope.poolingMode.code === 'MOUSE' && $scope.atmService.outputContainerSupportCategoryCode === '96-well-plate' && ( ! $scope.atmService.data.isSelectInputContainers() ||  $scope.outputMode === undefined ) ){ return true; }
		else if ( ($scope.poolingMode.code !== 'MOUSE' && $scope.poolingMode.code !== 'NONE') && 
				 							 ( $scope.atmService.data.isSelectInputContainers()
				  								|| ($scope.outputMode === undefined)
												|| ($scope.autoPoolingDone === true)
												|| ($scope.autoPoolingError === true)
												//// autoriser N plaques !! || ($scope.atmService.data.inputContainerSupports.length > 3 ) 
											)){ return true; }
		else if ($scope.buttonsConsistancyError) { return true; }
		else { console.log('RIEN DE TOUT CA'); 
		console.log( $scope.poolingMode.code);
		console.log($scope.atmService.outputContainerSupportCategoryCode);
		console.log($scope.outputMode);
		console.log($scope.atmService.data.isSelectInputContainers());
		return false;} // active le bouton
	}	
	
	/*  01/2025 NGL-4845 pooling a cheval sur plusieurs plaques ==> Algo a modifier !!!*/     
    var autoPooling =function (poolingMode){

		for (var p=0; p < $scope.inputContainerSupports.length; p++ ){
			console.log ("AUTOPOOLING: plaque entree "+ p+ ":"+$scope.inputContainerSupports[p]);
		}

		var startPos=getStartPos(poolingMode);
		var endPos=getEndPos(poolingMode);

        console.log (">>> start position ="+ startPos + " / end position ="+ endPos);

		// 10/2024 utilisation d'un switch (les cas Illumina Single ne sont pas implémentés)
		// note 20/08/2025 pooling C10 envisagé pour octobre/novembre 2025 ???
		switch (poolingMode.code){
			//case 'L12':	pas utilisé en fait !!
			case 'C4' :
			case 'C6' :
			case 'C8' :
			case 'C9' :
        		poolContigu( poolingMode.poolPlex, startPos, endPos, poolingMode.type ); 
				break;        	
			/* 22/10/2024 plus utilisés en prod
			case 'ID4':
			case 'ID6':
			case 'ID8':
        		poolIlluminaDual( poolingMode.poolPlex, startPos, endPos );
				break;
			*/
			default :
        		throw "Oops pooling mode not supported: "+ poolingMode.code;
        }
        console.log ("--- FIN DE AUTO POOLING ---");
		$scope.autoPoolingDone=true;
    }
 
	/* Les modes de pooling automatiques avaient des positions départ/fin envisagées variables!!!
	   il s'agit ici de position sur la plaque input ( ne pas confondre avec les positions pour outputMode !!)*/
	var getStartPos= function(poolingMode){
		if ( poolingMode.type === 'L') {
			// neutralisé ligne case 'L12' !!
			var startPos=$scope.plateUtils.getPos96FromLineAndCol_L( poolingMode.startLine, poolingMode.startColumn );
		} else if (poolingMode.type === 'C')  {
			//cas spécial C9_2plaques: valeurs hardcodées
			if ( $scope.isAutoPoolingC9_2plates() ){
				var startPos=$scope.dispoPositions.startPos;
			} else {
				var startPos=$scope.plateUtils.getPos96FromLineAndCol_C( poolingMode.startLine, poolingMode.startColumn );
			}
		} else { 
			throw "OOps incorrect type :"+ poolingMode.type ;
		}
		return startPos;
	}
 
	/* Les modes de pooling automatiques avaient des positions départ/fin envisagées variables!!!
	   il s'agit ici de position sur la plaque input ( ne pas confondre avec les positions pour outputMode !!)*/
	var getEndPos= function(poolingMode){
		if ( poolingMode.type === 'L') {
			// neutralisé ligne 1016 !!
			var endPos=$scope.plateUtils.getPos96FromLineAndCol_L( poolingMode.endLine, poolingMode.endColumn);	
			// !! prise en compte du nombre de plaques en entrée (pour les pools mutiple de 2: permet de travailler sur plusieurs plaques 96 completes)
			endPos=endPos + ( ($scope.atmService.data.inputContainerSupports.length -1) * 96 ); // plaque 96 puits hardcodé !
		}  else if (poolingMode.type === 'C')  {
			//cas spécial C9_2plaques valeurs hardcodées
			if ( $scope.isAutoPoolingC9_2plates() ){
				var endPos=$scope.dispoPositions.endPos;
			} else {
				var endPos=$scope.plateUtils.getPos96FromLineAndCol_C( poolingMode.endLine, poolingMode.endColumn);
				// !! prise en compte du nombre de plaques en entrée
				endPos=endPos + ( ($scope.atmService.data.inputContainerSupports.length -1)* 96 ); // plaque 96 puits hardcodé !!
			}
		} else { 
				throw "OOps incorrect type :"+ poolingMode.type ;
		}
		return endPos;
	}

	/* autoPooling en mode C9 sur 2 plaques nécessite un affichage dédié */
	$scope.isAutoPoolingC9_2plates=function(){
		if ( $scope.autoPoolingMode === true && 
			 $scope.poolingMode.code === 'C9' &&  
			 $scope.nbInputContainerSupports === 2) return true;
		else return false;
	}

	/* autoPooling en mode C9 avec 3 plaques nécessite un affichage dédié */
	$scope.isAutoPoolingC9_3plates=function(){
		if ( $scope.autoPoolingMode === true && 
			 $scope.poolingMode.code === 'C9' &&  
			 $scope.nbInputContainerSupports === 3) return true;
		else return false;
	}

	/* 22/10/2024  modes plus utilisés en prod !!!!
    var poolIlluminaDual = function( poolPlex, startPos, endPos ){
    	// le pooling illumina Dual index est un pooling par blocs contigus...( voir doc Illumina: TruSeq Library prepPooling)
    	//    3-plex =bloc 2x2 avec 1 trou; 4-plex=bloc 2X2;
    	//    5-plex= bloc 2x3 avec 1 trou; 6-plex=bloc 2x3; 
    	//    7-plex= bloc 2x4 avec 1 trou; 8-plex-option1 =bloc 2x4; 8-plex-option2=bloc 4x2
    	//    12-plex= bloc 4x3; 16-plex= bloc xx4
    	//
    	// L'algorithme a été au départ concu pour que l'utilisateur choisisse une position de départ et une position
    	// de fin...meme si maintenant le debut est toujours 1 et la fin toujours 96
    	console.log ("POOLING MODE ILLUMINA DUAL INDEX");
    	///console.log (">> startPos="+ startPos +" endPos="+ endPos );
    	
    	var block=null;
    	// members contient la position relative des autres puits du bloc par rapport au puit "0" du bloc
    	// ATTENTION numerotation en mode colonne des puits!!!!
    	/// cas non geree...if      (poolPlex === 2) { block={ line: 1, col:2, members:['X']}; }  // ESSAI!!!!!!!! marche pas
    	if      (poolPlex === 3) { block={ line: 2, col:2, members:[1,8]}; }  // comme poolPlex 4 mais sans le puit +9
    	else if (poolPlex === 4) { block={ line: 2, col:2, members:[1,8,9]}; }
    	else if (poolPlex === 5) { block={ line: 2, col:3, members:[1,8,9,16]}; } // comme poolPlex 6 mais sans le puit +17
    	else if (poolPlex === 6) { block={ line: 2, col:3, members:[1,8,9,16,17]}; }
    	else if (poolPlex === 7) { block={ line: 2, col:4, members:[1,8,9,16,17,24]}; } // comme poolPlex 8 mais sans le puit +25
    	else if (poolPlex === 8) { block={ line: 2, col:4, members:[1,8,9,16,17,24,25]}; } // le mode 2 (=4x2) pas pris encharge..]
    	else if (poolPlex === 12){ block={ line: 4, col:3, members:[1,2,3,8,9,10,11,16,17,18,19,]}; } // le mode 3x4 n'est pas pertinent pour une plaque 8x12
    	else if (poolPlex === 16){ block={ line: 4, col:4, members:[1,2,3,8,9,10,11,16,17,18,19,24,25,26,27]}; }
    	else {
    		throw "plex "+ poolPlex +" not managed in Illumina Dual Index mode";				
    	}

    	if ( block ){
    		//$scope.messages.clear();
    		//console.log (">> plex="+ poolPlex + "("+ block.line+"/"+block.col+")" );
    		
    	    var blockSize=block.line*block.col;
    	    var lastBlockMember=block.members.slice(-1)[0];
    	    
    	    // vérifier que les positions choisies correspondent a un nombre de blocs complet
    	    //  PB PAS SI SIMPLE !!!!    nbWellsSelected=endPos-StartPos +1;
    	    //  par exemple si debut =0 et fin =12 en mode block de 4 on a 8 puits et pas 12 (11-0 +1)!!! 
    	    
    	    // attention !!! en fonction de la position de depart  le block peut se retrouver a cheval le bord de plaque
    	    // ==> comment controler ca ?????????

    	    // ALGORITHME pour determiner le puit "0" (en haut a gauche) de chaque bloc en fonction de ses dimensions
    	    var pi=0 //pool index
    	    var loop=true;
    	    while ( loop  ) {
    	    	//[thx NW   encore !!]
    	    	//!! cet algo marche pour numerotation a partir de 0 donc:  -1 a startPos, +1 en sortie pour (pi) Pool et pour (wi) puit
    	    	var blockparcol = 8/block.line; // line toujours diviseur de 8 pour l'instant ( 2 ou 4!! voir liste des poolPlex autorisés )
    	        var j= blockparcol * Math.floor(pi / blockparcol) * blockSize;
    	        var k= (pi % blockparcol ) * block.line;
    	        var wi= j + k +(startPos -1);// prendre en compte la position de debut !!
    	        
    	        var lastWell=lastBlockMember + (wi+1);
    	        //console.log ("pool "+ (pi+1) +" commence au puit "+(wi+1)+" et fini au puit "+lastWell);
    	        createPoolBlock( pi+1, wi+1, block.members );
    	        
    	        // il faut sortir quand le plus grand indice de puit des membres du block courant est plus grand que la position de fin demandee
    	        // probleme pour les blocs a trou...compter les trous ( toujours testé avec 1 seul trou !!)
    	        //   !!! marche pas pour le pool 1x2 !!!
    	        var blockHoles=blockSize - block.members.length -1;
    	        
        	    if (lastWell  >=  ( endPos - blockHoles )) { 
    	        	//console.log ("exit loop!!!"); 
    	        	loop=false
    	        } 
    	    	pi++;
   			}
    	}
    }

	var createPoolBlock = function(p, firstWell, otherWellsArray ){
    	console.log ("----- creating pool "+ p+ '-----');
    	var supportCode=$scope.atmService.data.getCurrentSupportCode();
    	
    	//--1-- puit "0" du block ( type toujours en mode Colonne )
    	var line=getLineFromPosition96_C(firstWell);
    	var column=getColumnFromPosition96_C(firstWell);
    	//console.log( ">>container 0:"+ supportCode+"/"+line+"/"+column);

       	// récupérer LE container en position  line/column sur le supportCode et s'il existe le flager ( il ne doit y en avoir qu'un seul a une position donnée)
    	var containers = $filter('filter')($scope.atmService.data.inputContainers, {locationOnContainerSupport:{code:supportCode, line:line+"", column:column+""}},true);	
		if(containers && containers.length === 1)
			containers[0]._addToOutputContainer = true;
    	
    	   //--2-- autres puits  	
    		for (var j=0; j < otherWellsArray.length; j++){
    	    	var line=getLineFromPosition96_C (firstWell + otherWellsArray[j]);
    	    	var column=getColumnFromPosition96_C (firstWell + otherWellsArray[j]);
    	    	
    	    	// récupérer LE container en position  line/column sur le supportCode et s'il existe le flagger
    			var containers = $filter('filter')($scope.atmService.data.inputContainers, {locationOnContainerSupport:{code:supportCode, line:line+"", column:column+""}},true);
    			if(containers && containers.length === 1)
    				containers[0]._addToOutputContainer = true;
    		}
    		
            // creer le container de sortie correspondant au pool (ne sera pas cree si ne contient aucun puit)
        	$scope.atmService.data.dropInSelectInputContainer();	
    }
	*/
 
	// L'algorithme a été au départ concu pour que l'utilisateur choisisse une position de départ et une position de fin sur la MEME PLAQUE !!!
    var poolContigu = function( poolPlex,  startPos, endPos, type ){
    	console.log ("POOLING MODE CONTIGU ("+type+")"  );
    	
		var nbWells=endPos - startPos +1;
		// dans le cas ou on a > 1 plaque, la numerotation des puits se poursuit d'une plaque a l'autre le puit 97 est le premier de la 2eme plaque etc...
		var nbPools= Math.floor(nbWells / poolPlex);
		console.log ("nbPools="+ nbPools);
		
		var remain= nbWells % poolPlex;
		//le dernier pool est donc un pool reduit
		console.log ("reste="+ remain);
		
		// ici les indices pi et wi commencent a 1 et non 0 
		var pi=1;
		var pool=[];
		for ( var wi= startPos; wi < (endPos+1); wi++){
			 console.log (">>ajouter puit "+ wi +" dans pool "+ pi); // creation théorique des pools
			 pool.push(wi);
			 // si wi multiple de poolPlex alors on change de pool  ATTENTION prise en compte de la position de debut 	 
			 if  ((wi - startPos +1 )%poolPlex == 0 ) {
				 createPoolContigu( pi, pool, type);
				 var pool=[];
				 pi++;
			 }
			 // le pool réduit final eventuel
			 if (( wi == endPos ) && ( remain > 0)) {
				 //console.log ("pool final avec le reste");
				 createPoolContigu( pi, pool, type); 
			}
		}
		console.log('FIN POOLING MODE CONTIGU');
    } 
    
	/*  Après la création des pools théoriques  il faut prendre les puits correspondants. NGL-4845 DANS LA BONNE PLAQUE !
	    Si un puit est vide (aucun container n'existe pour cette position), il est ignoré le pool réel contiendra moins de plex que demandé
		C'est un comportement attendu. Le robot pipette d'une facon standard il ne peut pas s'adapter aux puits manquants 
	*/
    var createPoolContigu = function( p, wellsArray, type ){
    	// pour les mode ligne ou colonne
    	console.log ("-- creating pool "+ p +" --");
		
		// NGL-4845 le supportCode devient variable
    	for (var j=0; j < wellsArray.length; j++){

			// faire correspondre wellsArray[j] a une des plaques 96 en entree: 1-96=>0, 97-192=>1, 193-288=>2 ...
			var pl= Math.floor( (wellsArray[j] -1) / 96);
			//console.log ("puit" + wellsArray[j]+" est sur la plaque "+ pl);
			var supportCode=$scope.inputContainerSupports[ pl ];
			//console.log(">>>>"+supportCode);
			
    		// retraduire l'index en position <ligne><colonne> (depend du type de numérotation)
			// NGL-4845 corriger les positions par rapport a la plaque en cours: - (pl*96)
    		if (type === 'C') {
        		var line=$scope.plateUtils.getLineFromPosition96_C( wellsArray[j] - (pl*96));
        		var column=$scope.plateUtils.getColumnFromPosition96_C( wellsArray[j] - (pl*96));
    		} else if (type === 'L') {
        		var line=$scope.plateUtils.getLineFromPosition96_L( wellsArray[j] - (pl*96));
        		var column=$scope.plateUtils.getColumnFromPosition96_L( wellsArray[j] - (pl*96));
    		} else { 
    			throw "OOps incorrect type:"+ type; 
    		}		
    		//console.log( ">>container :"+ supportCode+"/"+line+"-"+column);
    		
        	// recuperer le container en position  line/column sur le support et s'il existe le flagger
    		var containers = $filter('filter')($scope.atmService.data.inputContainers, {locationOnContainerSupport:{code:supportCode, line:line+"", column:column+""}},true);
    		if(containers && containers.length === 1)
    			containers[0]._addToOutputContainer = true;
    	}
    
        // creer le container de sortie correspondant au pool (ne sera PAS créé si contient AUCUN puit)
    	$scope.atmService.data.dropInSelectInputContainer();
    }
    
	//NGL-3203 ajout information masquable; commencer masqué
	$scope.setIsShowInformation (false);
	
	// NGL-4049 ajout de la calcul de taille ponderée
	// code copié dans x-to-plates-ctrl.js du CNS
	var config = $scope.atmService.$atmToSingleDatatable.data.getConfig();
	config.otherButtons= {
		active : ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
        template: 
        	'<button class="btn btn-default" ng-click="computeInSizeToOut()" data-toggle="tooltip" ng-disabled="!(isEditModeAvailable() && isWorkflowModeAvailable(\'F\'))"'  
			+'title="'+Messages("experiments.button.title.computeSizeWeighted")+'" "><i class="fa fa-magic" aria-hidden="true"></i> '+ Messages("experiments.button.computeSize")
			+' </button>'  
			/// 10/10/2024 l'ajout ici des baguettes magiques ne fonctionne pas: le data table n'a pas d'outputContainer comme ceux ou ces boutons sont utilisées d'habitude !!
			/// +$scope.plateUtils.templates.buttonLineMode()
			/// +$scope.plateUtils.templates.buttonColumnMode()             	                	
    };

	var columns = $scope.atmService.$atmToSingleDatatable.data.getColumnsConfig();
	
	columns.push({
        "header": Messages("containers.table.contents.length"),
 		"property": "inputContainer.contents.length",
		// au CNRGH pour l'instant pas de tagSecondaire....'
 		//"filter": "getArray:'properties.secondaryTag.value'| unique",
 		"order":true,
 		"hide":true,
 		"type":"number",
 		"position":5.9,
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
		"extraHeaders" : {0 : Messages("experiments.inputs")}
	});
	// au CNG il y a des propiété supplémentaires qui s'injectent, la position 100 ne convient pas'
	columns.push({
		"header" : Messages("containers.table.size"),
		"property": "outputContainerUsed.size.value",
		"order" : true,
		"edit" : false,
		"hide" : true,
		"type" : "number",
		"position" :55,
		"extraHeaders" : {0 : Messages("experiments.outputs")}
	});
	
	$scope.atmService.$atmToSingleDatatable.data.setColumnsConfig(columns);

	$scope.computeInSizeToOut= function(){

		var atm = $scope.experiment.atomicTransfertMethods;	
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
				console.log ("output size => "+ size.value);
				$parse("outputContainerUseds[0].size").assign(value, size);
				
			}else{
				/* simplification
				$scope.messages.clear();
				$scope.messages.clazz = "alert alert-warning";
				$scope.messages.text = "Le calcul 'moyenne pondérée des tailles' ne peut pas s'effectuer car la taille est manquante chez au moins un container d'entrée";
				$scope.messages.showDetails = false;
				$scope.messages.open();
				*/
				$scope.mesages.setWarning("Le calcul 'moyenne pondérée des tailles' ne peut pas s'effectuer car la taille est manquante chez au moins un container d'entrée");
			}
		});
		
		// besoin de raffraichir l'expérience ????????
		// $scope.atmService.data.updateDatatable();  
		//  ==> NE FONCTIONNE PAS ICI alors que oui dans CNS/x-to-tubes-ctrl.js !!!!!
		// fonctionne OK !!!
		$scope.atmService.refreshViewFromExperiment($scope.experiment);
	}

	// 23/10/2024  desactiver support sortie Strip-8 car pas géré par scala
	$scope.$watch("experiment.instrument.outContainerSupportCategoryCode", function(newValue, oldValue){
		if ( newValue === "strip-8"){
			// trop tard pour rendre bloquant, l'initialisation de la page a été faite !!!!
			$scope.experiment.instrument.outContainerSupportCategoryCode = null;

			//pour desactiver le bouton "Pooling" dans xtox.scala.html
			$scope.atmService.outputContainerSupportCategoryCode=null;
			$scope.messages.setWarning(Messages("experiments.output.error.supportCategory","Strip 8"));
		} 
		else if (oldValue === null){
			$scope.messages.clear();
		}
	});

	// 21/10/2025 image d'aide ( avec paramètre car au CNS il y a plusieurs modales)
	$scope.showModalHelp=function(modale){
		angular.element(modale).modal('show');
		//angular.element('#infoModale').modal('show');
	}
	
}]);