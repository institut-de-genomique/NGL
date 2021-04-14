// GS dupliqué depuis manyToOne.experiments.cng.x-to-plates
angular.module('home').controller('XToPlatesCtrlCNS',['$scope', '$http','$parse', '$filter', 
                                                               function($scope, $http, $parse, $filter) {

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
			console.log("instrument.outContainerSupportCategoryCode="+ $scope.experiment.instrument.outContainerSupportCategoryCode);
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
	
}]);