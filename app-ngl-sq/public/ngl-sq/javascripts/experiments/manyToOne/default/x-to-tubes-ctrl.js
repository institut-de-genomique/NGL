angular.module('home').controller('XToTubesCtrl',['$scope', '$parse', '$filter','atmToDragNDrop2','mainService', 
                                                               function($scope, $parse, $filter, atmToDragNDrop, mainService) {
	
	// NGL-1055: name explicite pour fichier CSV exporté
	// NGL-1055: mettre getArray et codes:'' dans filter et pas dans render
	var datatableConfig = {		
			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[   					
					
			         {
			        	 "header":Messages("containers.table.supportCategoryCode"),
			        	 "property":"inputContainer.support.categoryCode",
			        	 "filter":"codes:'container_support_cat'",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":2,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },	
			         {
			        	 "header":Messages("containers.table.fromTransformationTypeCodes"),
			        	 "property":"inputContainer.fromTransformationTypeCodes",
			        	 "filter":"unique | codes:'type'",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			 			 "render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	 "position":3,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			         {
			        	"header":Messages("containers.table.projectCodes"),
			 			"property": "inputContainer.projectCodes",
			 			"order":false,
			 			"hide":true,
			 			"type":"text",
			 			"position":4,
			 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			            "extraHeaders":{0:Messages("experiments.inputs")}
				     },
				     {
			        	"header":Messages("containers.table.sampleCodes"),
			 			"property": "inputContainer.sampleCodes",
			 			"order":true,
			 			"hide":true,
			 			"type":"text",
			 			"position":5,
			 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	"extraHeaders":{0:Messages("experiments.inputs")}
				     },
			         {
				 		"header":Messages("containers.table.libProcessType"),
				 		"property": "inputContainer.contents",
				 		"filter": "getArray:'properties.libProcessTypeCode.value'| unique",
				 		"order":false,
				 		"hide":true,
				 		"type":"text",
				 		"position":6,
				 		"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
				 		"extraHeaders": {0:Messages("experiments.inputs")}	
				 	},
			        {
				        "header":Messages("containers.table.tags"),
				 		"property": "inputContainer.contents",
				 		"filter": "getArray:'properties.tag.value'| unique",
				 		"order":true,
				 		"hide":true,
				 		"type":"text",
				 		"position":7,
				 		"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
				        "extraHeaders":{0:Messages("experiments.inputs")}
				     },
				     {
			        	 "header":Messages("containers.table.volume") + " (µL)",
			        	 "property":"inputContainerUsed.volume.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":8,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
					 {
			        	 "header":Messages("containers.table.concentration.shortLabel"),
			        	 "property":"inputContainerUsed.concentration.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":9,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			         {
			        	 "header":Messages("containers.table.concentration.unit.shortLabel"),
			        	 "property":"inputContainerUsed.concentration.unit",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":10,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },	        
			         {
			        	 "header":Messages("containers.table.state.code"),
			        	 "property":"inputContainer.state.code",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
						 "filter":"codes:'state'",
			        	 "position":11,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			         {
			        	 "header":Messages("containers.table.percentageInsidePool"),
			        	 "property":"inputContainerUsed.percentage",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":12,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			         {
			        	 "header":Messages("containers.table.concentration.shortLabel"),
			        	 "property":"outputContainerUsed.concentration.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
						 "type":"number",
			        	 "position":50,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },
			         {
			        	 "header":Messages("containers.table.concentration.unit.shortLabel"),
			        	 "property":"outputContainerUsed.concentration.unit",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
						 "type":"text",
			        	 "position":50.5,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },
			         {
			        	 "header":Messages("containers.table.volume")+" (µL)",
			        	 "property":"outputContainerUsed.volume.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
						 "type":"number",
			        	 "position":51,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },
			         {
			        	 "header":Messages("containers.table.code"),
			        	 "property":"outputContainerUsed.code",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
						 "type":"text",
			        	 "position":400,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },
			         {
			        	 "header":Messages("containers.table.stateCode"),
			        	 "property":"outputContainer.state.code | codes:'state'",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
						 "type":"text",
			        	 "position":500,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         }
			         ],
			compact:true,
			pagination:{
				active:false
			},		
			search:{
				active:false
			},
			order:{
				mode:'local', //or 
				active:true,
				by:"inputContainer.support.code"
			},
			remove:{
				active:false,
			},
			save:{
				active:true,
				withoutEdit: true,
				mode:'local',
				changeClass:false,
				showButton:false,
	        	callback:function(datatable){
	        		  copyContainerSupportCodeAndStorageCodeToDT(datatable);
	        	}
			},
			hide:{
				active:true
			},
			mergeCells:{
	        	active:true 
	        },
			select:{
				active:false,
				showButton:true,
				isSelectAll:false
			},
			edit:{
				active: false,
				columnMode:true
			},
			messages:{
				active:false,
				columnMode:true
			},
			exportCSV:{
				active:true,
				showButton:true,
				delimiter:";",
				start:false
			},
			extraHeaders:{
				number:2,
				dynamic:true,
			}
	};	
	
	// retourne le type ET le nombre de supports. creation pour NGL-3593
	// ????????? mettre ou pour etre utilisable dans d'autres controleurs  ???????
	////  existe deja  $scope.plateFluoUtils.getSupportCategoryCode(); mais ne donne pas le nombre
	//// plateFluoUtils est dans details-ctrl.js
	
	var getInputSupportInfo= function($scope){
		var categoryCodes = [];
		var supportCodes = [];
		if ( ! $scope.isCreationMode()){
			categoryCodes = $scope.$eval("atomicTransfertMethods|flatArray:'inputContainerUseds'|getArray:'locationOnContainerSupport.categoryCode'|unique",$scope.experiment);
			supportCodes  = $scope.$eval("atomicTransfertMethods|flatArray:'inputContainerUseds'|getArray:'locationOnContainerSupport.code'|unique",$scope.experiment);
		} else {
			// en mode création il faut passer par basket !!
			categoryCodes = $scope.$eval("getBasket().get()|getArray:'support.categoryCode'|unique",mainService);
			supportCodes  = $scope.$eval("getBasket().get()|getArray:'support.code'|unique",mainService);
		}
		var categoryCode = undefined;
		if(categoryCodes.length === 1){
			// les supports sont homogènes
			categoryCode=categoryCodes[0];
			$scope.supportCategoryCode =categoryCodes[0];
		} else {
			categoryCode="mixte";
			// NGL-3927 
			// !!! $scope.supportCategoryCode est utilisé dans cns/xtox.scala avec un switch: le cas "mixte" n'est pas prévu' !
			// => forcer le comportement tube  ( le comportement plaque autorise le pool intelligent)
			$scope.supportCategoryCode = "tube";
		}
		
		return { categoryCode: categoryCode,
				 count:supportCodes.length};
	}
	//--------------------------------------------------------------------
	
	var inputSupports=getInputSupportInfo($scope);
	console.log ("categoryCode="+inputSupports.categoryCode +" / count="+inputSupports.count);
	
	if (inputSupports.categoryCode === "96-well-plate"){
		datatableConfig.columns.push({
			"header" : Messages("containers.table.supportCode"),
			"property" : "inputContainer.support.code",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "text",
			"position" : 1,
			"extraHeaders" : {
				0 : Messages("experiments.inputs")
			}
		});
		datatableConfig.columns.push({
			"header" : Messages("containers.table.support.line"),
			"property" : "inputContainer.support.line",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "text",
			"position" : 1.1,
			"extraHeaders" : {
				0 : Messages("experiments.inputs")
			}
		});
		datatableConfig.columns.push({
			"header" : Messages("containers.table.support.column"),
			"property" : "inputContainer.support.column*1",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "number",
			"position" : 1.2,
			"extraHeaders" : {
				0 : Messages("experiments.inputs")
			}
		});
	
	} else {
		// tubes et mixte tubes/plaques
		datatableConfig.columns.push({
			"header" : Messages("containers.table.code"),
			"property" : "inputContainer.code",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "text",
			"position" : 1,
			"extraHeaders" : {
				0 : Messages("experiments.inputs")
			}
		});
		
		datatableConfig.order.by = 'inputContainer.sampleCodes';
	}
	
	$scope.drop = function(e, data, ngModel, alreadyInTheModel, fromModel) {
		//capture the number of the atomicTransfertMethod
		if(!alreadyInTheModel){
			$scope.atmService.data.updateDatatable();
		}
	};
	
	// FDS: renommer getOutputContainers car donne une liste de containers et pas de containerSupports, !! contient des doublons 
	$scope.getOutputContainers = function(){
		var outputContainers = [];
		if($scope.experiment.atomicTransfertMethods){
			$scope.experiment.atomicTransfertMethods.forEach(function(atm){
				this.push(atm.outputContainerUseds[0]);
				
			}, outputContainers);
		}
		return outputContainers;
	}
	
	// FDS : liste de containerSupports sans doublons
	$scope.getDistinctOutputContainerSupports = function(){
		var outputContainerSupports = [];
		if($scope.experiment.atomicTransfertMethods){
			var unique = {};
			$scope.experiment.atomicTransfertMethods.forEach(function(atm){
				
				if (!unique[atm.outputContainerUseds[0].locationOnContainerSupport.code]) {
				    this.push(atm.outputContainerUseds[0].locationOnContainerSupport);
				    unique[atm.outputContainerUseds[0].locationOnContainerSupport.code] = true;
				}
			}, outputContainerSupports);
		}
		return outputContainerSupports;
	}
	
	// NGL-3593 ajout test creationMode
	$scope.getInputContainerSupports = function(){
		var inputContainerSupports = [];
		if ( ! $scope.isCreationMode()){
			if($scope.experiment.atomicTransfertMethods){
				inputContainerSupports = $scope.experiment.inputContainerSupportCodes;
			}
		} else {
			inputContainerSupports  = $scope.$eval("getBasket().get()|getArray:'support.code'|unique",mainService);
		}
		return inputContainerSupports;
	}
	
	$scope.isEditMode = function(){
		return ($scope.$parent.isEditMode() && $scope.isNewState());
	};
	
	/* 31/03/2022 NGL-3593 : warning Pool/CNRGH si intrument est un robot et uniqt des plaques en entree et nbre plaque >1 
	 **** mettre dans details-ctrl.js si devient utile au CNS...***
	*/
	var checkNbInputPlates =function(){
		if ( $scope.experiment.instrument.categoryCode !== 'hand' &&
			 inputSupports.categoryCode === "96-well-plate" && 
			 inputSupports.count > 1  ) {
				var inputContainerSupports=$scope.getInputContainerSupports(); // array of string
				// !!! faut le meme tri que dans l'onglet Source/destination !!
				inputContainerSupports.sort();// sort by quoi ???
					
				$scope.messages.clazz = "alert alert-warning";
				$scope.messages.text ="Attention vous avez "+ inputSupports.count +" sources, vérifiez que le positionnement de vos plaques sur le robot correspond au plan proposé par NGL:";
				var data = { 'sources':[], 'destinations':[] };
					
				for (var p=0; p < inputContainerSupports.length; p++ ){
					var tab=[inputContainerSupports[p], p+1];
					data['sources'].push(tab);
				}	
					
				var outputContainerSupports=$scope.getDistinctOutputContainerSupports(); // array of object !!!
				// !!! faut le meme tri que dans l'onglet Source/destinantion !!
				outputContainerSupports.sort();// sort by quoi ???
					
				for (var p=0; p < outputContainerSupports.length; p++ ){
					var tab=[outputContainerSupports[p].code, p+1];
					data['destinations'].push(tab);
				}
				
				$scope.messages.setDetails(data);
				$scope.messages.showDetails = true;
				$scope.messages.open();	
			}
	}
	
	//  !!!  COMMUN CNS/CNG !!!  
	$scope.$on('save', function(e, callbackFunction) {
		console.log("call event save on default/x-to-tubes"); // ajout default pour voir si apparait au cns!!!
		
		// fdsantos 28/09/2017 :NGL-1601 ne pas sauvegarder une experience vide.
		if($scope.atmService.data.atm.length === 0){
			$scope.$emit('childSavedError', callbackFunction);
			
			// 01/04/2022 simplification
			$scope.messages.setError(Messages("experiments.msg.nocontainer.save.error"));
	
		} else {
			// NGL-3593 Warning au CNG
			if ($scope.isCNG) checkNbInputPlates();
			
			$scope.atmService.viewToExperiment($scope.experiment, false);
			$scope.$emit('childSaved', callbackFunction);
			
			// 07/04/2022 pour éviter que le message final de sauvegarde OK ne récupère les détails du warning de checkNbInputPlates (quand il y en a)
			$scope.messages.clear();
		} 
	});
	
	$scope.$on('refresh', function(e) {
		console.log("call event refresh on x-to-tubes");
		$scope.atmService.refreshViewFromExperiment($scope.experiment);
		$scope.$emit('viewRefeshed');
	});
	
	$scope.$on('cancel', function(e) {
		console.log("call event cancel");
	});
	
	$scope.$on('activeEditMode', function(e) {
		console.log("call event activeEditMode");
	});
	
	$scope.inputContainerProperties = $filter('filter')($scope.experimentType.propertiesDefinitions, 'ContainerIn');
	$scope.outputContainerProperties = $filter('filter')($scope.experimentType.propertiesDefinitions, 'ContainerOut');
	
	var atmService = atmToDragNDrop($scope, 0, datatableConfig);
	
	atmService.inputContainerSupportCategoryCode = $scope.experiment.instrument.inContainerSupportCategoryCode;
	atmService.outputContainerSupportCategoryCode = $scope.experiment.instrument.outContainerSupportCategoryCode;
	
	// 19/10/2016 version de Guillaume pour gerer les cas tubes ou 96-well-plate
	// 27/10/2016 bug vu par JG: au CNS pool generique tube=> tube : line et column sont undefined
	atmService.newAtomicTransfertMethod =  function(line, column){
		var getLine = function(line){
			if ($scope.experiment.instrument.outContainerSupportCategoryCode === "tube"){
				return 1; // ligne et colonne=1 pour un tube
			} else {
				return undefined;
			}			
		}
		var getColumn=getLine;
		
		return {
			class:"ManyToOne",
			line:getLine(line), 
			column:getColumn(column),
			inputContainerUseds:new Array(0), 
			outputContainerUseds:new Array(0)
		};
	};
	
	//defined default output unit
	atmService.defaultOutputUnit = {
			volume : "µL",
	}
	
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	$scope.atmService = atmService;
	
	// NGL-3593 comportement different CNG/CNS
	//appellée par ng-init dans le scala.html
	$scope.setIsCNG = function(isCNG) {
		console.log('isCNG ???'+ isCNG);
		$scope.isCNG=isCNG;
	}
	
	//NGL-3593; PB $scope.setIsCNG n'est pas évaluée des le debut=> mettre un watch sur la variable !!
	$scope.$watch("isCNG", function(){
		if ($scope.isCNG && ! $scope.isFinishState()) {         //faut il afficher le warning a Terminé ???
		//if ($scope.isCNG ) {
			checkNbInputPlates();
		}
	});

}]);
