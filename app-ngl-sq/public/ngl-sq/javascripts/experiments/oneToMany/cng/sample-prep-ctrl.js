// 11/08/2017 GA/FDS experience One to Many => Il faut refaire un atmService "allégé" par rapport a celui dans atomicTransfereServices.js
// 16/10/2017 finalement il faut qd meme un datatable...

// 20/10/2017 FDS ajout '$q','$routeParams', $http pour les promises
angular.module('home').controller('SamplePrepCtrl',['$scope', '$http', '$parse', '$filter','$q','$routeParams','commonAtomicTransfertMethod','mainService','datatable',
                                                               function($scope, $http, $parse, $filter, $q, $routeParams, commonAtomicTransfertMethod, mainService, datatable ) {
	
	var inputExtraHeaders=Messages("experiments.inputs");
	
	var inputContainerDatatableConfig = {
			columns:[   
					 {
			        	 "header":Messages("containers.table.support.name"),
			        	 "property":"inputContainer.support.code",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":1,
			        	 "extraHeaders":{0:inputExtraHeaders}
			         },
			         { // Ligne
			        	 "header":Messages("containers.table.support.line"),
			        	 "property":"inputContainer.support.line",
			        	 "order":true,
						 "hide":true,
			        	 "type":"text",
			        	 "position":2,
			        	 "extraHeaders":{0:inputExtraHeaders}
			         },
			         { // colonne
			        	 "header":Messages("containers.table.support.column"),
				         // astuce GA: pour pouvoir trier les colonnes dans l'ordre naturel forcer a numerique.=> type:number,   property:  *1
			        	 "property":"inputContainer.support.column*1",
			        	 "order":true,
						 "hide":true,
			        	 "type":"number",
			        	 "position":3,
			        	 "extraHeaders":{0:inputExtraHeaders}
			         },
			         { // Projet(s)
				        "header":Messages("containers.table.projectCodes"),
				 		"property": "inputContainer.projectCodes",
				 		"order":true,
				 		"hide":true,
				 		"type":"text",
				 		"position":4,
				 		"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
				         "extraHeaders":{0: inputExtraHeaders}
					  },
					  { // Echantillon(s) 
				        "header":Messages("containers.table.sampleCodes"),
				 		"property": "inputContainer.sampleCodes",
				 		"order":true,
				 		"hide":true,
				 		"type":"text",
				 		"position":5,
				 		"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
				        "extraHeaders":{0: inputExtraHeaders}
					  },
					  /*
					     { // sampleAliquoteCode 
					        "header":Messages("containers.table.codeAliquot"),
					 		"property": "inputContainer.contents", 
					 		"filter": "getArray:'properties.sampleAliquoteCode.value'",
					 		"order":true,
					 		"hide":true,
					 		"type":"text",
					 		"position":6,
					 		"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
					        "extraHeaders":{0: inputExtraHeaders}
						 },
						*/
					  { 
					    "header": Messages("containers.table.libProcessType"),
					    "property" : "inputContainerUsed.contents",
					    "filter" : "getArray:'properties.libProcessTypeCode.value' |unique | codes:'value'",
					    "order":true,
						"edit":false,
						"hide":true,
					    "type":"text",
					    "position":8.2,
					    "extraHeaders":{0:inputExtraHeaders}
					  },
				      { // niveau process uniquement =>  utiliser processProperties; ne fonctionne que a nouveau et en cours c'est normal !!
				        "header": Messages("containers.table.expectedBaits"),
				        "property" :"inputContainerUsed.contents",
				        "filter" : "getArray:'processProperties.expectedBaits.value' | unique  | codes:'value'",
				      	"order":true,
					    "edit":false,
					    "hide":true,
				      	"type":"text",
				      	"position":8.4,
				      	"extraHeaders":{0:inputExtraHeaders}
				      },
			          { // contents => utiliser properties et pas processProperties
			        	 "header": Messages("containers.table.captureProtocol"),
			        	 "property" : "inputContainerUsed.contents",
			        	 "filter" : "getArray:'properties.captureProtocol.value' | unique  | codes:'value'",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":8.6,
			        	 "extraHeaders":{0:inputExtraHeaders}
			          },
				      { // Etat input Container 
				        "header":Messages("containers.table.state.code"),
				        "property":"inputContainer.state.code | codes:'state'",
				        "order":true,
						"hide":true,
				        "type":"text",
				        "position":9,
				        "extraHeaders":{0: inputExtraHeaders}
				       }   		         
			],
			compact:true,
			// tout a false, on ne fait que de l'affichage
			showTotalNumberRecords:false,
			pagination:{
				active:false
			},		
			search:{
				active:false
			},
			order:{
				active:false
			},
			remove:{
				active: false				
			},
			save:{
				active:false
			},			
			select:{
				active:false
			},
			edit:{
				active: false
			},	
			cancel : {
				active:false
			},
			extraHeaders:{
				number:1,
				dynamic:true,
			}
	};
	
	
   $scope.values=[1,2];
   
   // créer un tableau sur lequel pourra boucler ng-repeat
   // ce tableau est modifié sur onChange de "nbOutputSupport"
   $scope.initOutputContainerSupportCodes = function(nbOutputSupport){
	   
	    //$scope.nbOutputSupport=nbOutputSupport;//necessaire si pas de preselection
	   
		if($scope.isCreationMode() ){
			$scope.outputContainerSupportCodes= new Array(nbOutputSupport*1);// *1 pour forcer en numerique nbOutputSupport qui est est un input type text
			$scope.outputContainerSupportStorageCodes= new Array(nbOutputSupport*1);
		} else {
		    //en mode edition récupérer les codes des outputContainers et reinjecter si possible ce qu'il y avait avant
			previousOutputContainerSupportCodes=$scope.$eval("atomicTransfertMethods| flatArray:'outputContainerUseds'| getArray:'locationOnContainerSupport.code'| unique",$scope.experiment);
			previousOutputContainerSupportStorageCodes=$scope.$eval("atomicTransfertMethods| flatArray:'outputContainerUseds'| getArray:'locationOnContainerSupport.storageCode'| unique",$scope.experiment);
			
			if(previousOutputContainerSupportCodes.length >= nbOutputSupport){
				//l'utilisateur a reduit le nombre de output...tronquer le tableau
				$scope.outputContainerSupportCodes = previousOutputContainerSupportCodes.splice(0, nbOutputSupport);
				$scope.outputContainerSupportStorageCodes = previousOutputContainerSupportStorageCodes.splice(0, nbOutputSupport);
				
			}else if(previousOutputContainerSupportCodes.length < nbOutputSupport){
				//l'utilisateur a augmenté completer le tableau avec des null
				$scope.outputContainerSupportCodes=previousOutputContainerSupportCodes;
				$scope.outputContainerSupportStorageCodes=previousOutputContainerSupportStorageCodes; 
				
				for (var j=previousOutputContainerSupportCodes.length ; j<  nbOutputSupport; j++){
					$scope.outputContainerSupportCodes.push(null);
					$scope.outputContainerSupportStorageCodes.push(null);
				}
			}
		} 
	}

   if($scope.isCreationMode()){

	   if ($scope.experiment.instrument.typeCode === 'bravo-workstation') {
		   $scope.nbOutputSupport=1;
	   } else {
		   $scope.nbOutputSupport=2;
	   }
	   
	   //16/02/2018 en fonction instrument
	   $scope.initOutputContainerSupportCodes($scope.nbOutputSupport);
	   
	   // trouver LE/LES codes des supports de tous les containers en entree de l'experience (ce sont des puits de plaque)   
	   $scope.inputSupportCodes = $scope.$eval("getBasket().get()|getArray:'support.code'|unique", mainService); 
	   var categoryCodes = $scope.$eval("getBasket().get()|getArray:'support.categoryCode'|unique",mainService);
	   
	   //  26/02/2018  experience ne peut marcher qu'avec 1 plaque en entree!!!!!
	   if ( ($scope.inputSupportCodes.length > 1) || ( categoryCodes[0] !== "96-well-plate")){
		   $scope.messages.clear();
		   $scope.messages.setError(Messages("experiments.input.error.only-1-plate"));

		   // faudrait empecher la page de se charger... ATTENTION dans cette experience on a un atmService local car specifique oneToMany
		   //var atmService = null; // n' empeche pas la page de se charger.. car redefini en lignes 310...
	   } else {
		   //22/03/2018 ajouté car supprimé precedemment??? ( bug vu par J.Guy pendant test NGL-1906)
		   $scope.inputSupportCode=$scope.inputSupportCodes[0];
	   }
	   
	} else {
		 getExperimentData();
	}
	
	function getExperimentData(){	
		//1 récupérer LE locationOnContainerSupport.code des containers (il ne peux y en avoir qu'un seul)
	    $scope.inputSupportCode = $scope.$eval("atomicTransfertMethods| flatArray:'inputContainerUseds'| getArray:'locationOnContainerSupport.code'| unique",$scope.experiment)[0];
	     
		//2 récupérer le nbre de nbOutputSupport en se basant sur atomic[0]: tjrs vrai ??? 
        //  => oui si on bloque les cas de sauvegarde sans nbOutputSupport
	    $scope.nbOutputSupport=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds.length; 
			  
	    //3 récupérer les codes des outputContainers et les storageCodes des outputContainers 
	    $scope.outputContainerSupportCodes=$scope.$eval("atomicTransfertMethods| flatArray:'outputContainerUseds'| getArray:'locationOnContainerSupport.code'| unique",        $scope.experiment);
	    $scope.outputContainerSupportStorageCodes=$scope.$eval("atomicTransfertMethods| flatArray:'outputContainerUseds'| getArray:'locationOnContainerSupport.storageCode'| unique", $scope.experiment);
    
	    // 5 NGL-1670- recuperer l'etat des outputContainers
	    $scope.outputContainerSupportStates= [];
	    
	    $scope.outputContainerSupportCodes.forEach(function(code) {
	    	getOutputContainerSupportState(code);
	    });   
	}
	
    // NGL-1670  recuperer l'etat d'un containerSupport ==> utiliser promise (voir containerSupport/details.js)
	// il faut que le containerSupport ait été créé... MARCHE PAS A L"ETAT NOUVEAU !!!
	function getOutputContainerSupportState(supportCode){
		if (undefined !== supportCode ){
			$http.get(jsRoutes.controllers.containers.api.ContainerSupports.get(supportCode).url).then(function(results){ 
				
				var stateCode=results.data.state.code;
				console.log(supportCode +"=>"+stateCode );
				$scope.outputContainerSupportStates.push(stateCode);
			});		
	    }
	}
	
	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event save");

		$scope.atmService.viewToExperimentOneToMany($scope.experiment);
		
		if ( $scope.experiment.atomicTransfertMethods[0].outputContainerUseds.length === 0){
			$scope.$emit('childSavedError', callbackFunction);
			
		    $scope.messages.clazz = "alert alert-danger";
		    $scope.messages.text = Messages('experiments.output.error.minSupports',1);
		    $scope.messages.showDetails = false;
			$scope.messages.open(); 
		} else {
			$scope.$emit('childSaved', callbackFunction);
		}
	});
	
	$scope.$on('refresh', function(e) {
		console.log("call event refresh");
		
		// copié sur capture-ctrl.js
		var dtConfig = $scope.atmService.data.getConfig();
		dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('IP'));
		dtConfig.edit.byDefault = false;
		dtConfig.edit.start = false;
		dtConfig.remove.active = ($scope.isEditModeAvailable() && $scope.isNewState());
		$scope.atmService.data.setConfig(dtConfig);
		
        // pour rafraichissement de l'affichage des outputContainerSupports
        getExperimentData();
		
		$scope.atmService.refreshViewFromExperiment($scope.experiment);
		$scope.$emit('viewRefeshed');
	});
	
	$scope.$on('cancel', function(e) {
		console.log("call event cancel");		
		getExperimentData();
	});
	
	$scope.$on('activeEditMode', function(e) {
		console.log("call event activeEditMode");
		// rien  ????
	});
	
	// FDS 16/03/2018 : NGL-1906 rechercher le robotRunWorkLabel positionné au niveau processus pour le copier dans robotRunCode (sauf s'il y en plusieurs!!)
	$scope.$watch("experiment.instrument.code", function(newValue, OldValue){
		if ((newValue) && (newValue !== null ) && ( newValue !== OldValue ))  {
			// exemple dans prepa-fc-ordered: var categoryCodes = $scope.$eval("getBasket().get()|getArray:'support.categoryCode'|unique",mainService);
			// mais ici mainService n'est pas defini, et pas necessaire...
			// obliger de passer par contents[0], mais normalement ne doit pas poser de probleme...
			var workLabels= $scope.$eval("getBasket().get()|getArray:'contents[0].processProperties.robotRunWorkLabel.value'|unique");
			if ( workLabels.length > 1 ){
				$scope.messages.clear();
				$scope.messages.clazz = "alert alert-warning";
				$scope.messages.text = "Plusieurs noms de travail (robot) trouvés parmi les containers d'entrée (info processus)";
				$scope.messages.open();			
			
				//console.log('>1  run workLabel trouvé !!');		
			} else if ( workLabels.length === 1 ){
				// verifier que TOUS les containers ont une valeur...
				var contents= $scope.$eval("getBasket().get()|getArray:'contents[0]'");
				var labels= $scope.$eval("getBasket().get()|getArray:'contents[0].processProperties.robotRunWorkLabel.value'");
				if ( labels.length < contents.length ) {
					$scope.messages.clear();
					$scope.messages.clazz = "alert alert-warning";
					$scope.messages.text = "Certains containers en entrée n'ont pas de nom de travail run (robot) (info processus)";
					$scope.messages.open();			
				
					//console.log("Certains containers n'ont pas de workLabel.");
				} else {
					// NGL-2160/NGL-2164 ne faire l'assignation que si l'instrument possede la propriété robotRunCode (sinon erreur de sauvegarde experience!)
					if ( $scope.instrumentHasProperty('robotRunCode') ) {
						$parse("instrumentProperties.robotRunCode.value").assign($scope.experiment, workLabels[0]);
					} else {
						console.log("la propriété n'est pas gérée par l'instrument!");
						// faut-il une alerte utilisateur ??
					}
				}
			} 
			// si aucun workLabel ne rien faire
		}
	});
		
	
	//init data
	//GA 16/10/2017 Cette experience est la seule qui fait du one to many avec plaque en entre/ plaques en sortie.
	//=>Il faut refaire un atmService "allégé" par rapport a celui dans atomicTransfereServices.js
	var atmService =  {
			data:datatable(inputContainerDatatableConfig), //UDT
			$commonATM : commonAtomicTransfertMethod($scope),
			defaultOutputUnit : {
					volume : "µL",
					concentration : "nM"
			},
			defaultOutputValue :{},
			platesCells:[],
			newAtomicTransfertMethod : function(l,c){
				return {
					class:"OneToMany",
					line: l, 
					column: c, 				
					inputContainerUseds:new Array(0), 
					outputContainerUseds:new Array(0)
				};
			},
			convertExperimentATMToDatatable: function(experimentATMs, experimentStateCode){
				$that = this;
				var atms = experimentATMs;
				$that.$commonATM.loadInputContainerFromAtomicTransfertMethods(atms)
					.then(function(result) {								
						var allData = [];
						var inputContainers = result.input;
						var atomicIndex=0;
						var plateCells= new Array(0);// array local de travail...
						for(var i=0; i< atms.length;i++){
							
							if(atms[i] === null){
								continue;
							}
							//var atm = angular.copy(atms[i]);
							var atm = $.extend(true,{}, atms[i]);
								
							var inputContainerCode = atm.inputContainerUseds[0].code;
							var inputContainer = inputContainers[inputContainerCode];	   
							var line = {atomicIndex:atomicIndex};
							line.atomicTransfertMethod = atm;							              
							line.inputContainer = inputContainer;	
							line.inputContainerUsed = $.extend(true,{}, atm.inputContainerUseds[0]);
							line.inputContainerUsed = $that.$commonATM.updateInputContainerUsedFromContainer(line.inputContainerUsed, inputContainer, experimentStateCode);							
							allData.push(line);
									
							// creation des plateCells 
							// NOTE: en javascript il est possible d'avoir des index  de tableau non numeriques !!
							var ocu=atm.outputContainerUseds;
							for(var j=0; j < ocu.length;j++){
								var code= ocu[j].locationOnContainerSupport.code;
								 if(plateCells[code] == undefined){
									plateCells[code] = new Array();
								 }
								   
								 var ln = ocu[j].locationOnContainerSupport.line;
								 var col = ocu[j].locationOnContainerSupport.column; 
 
								 var sampleCodeAndTags = [];
								 angular.forEach(ocu[j].contents, function(content){
									var value = content.projectCode+" / "+content.sampleCode;
									
									if(content.properties && content.properties.libProcessTypeCode){
										value = value +" / "+content.properties.libProcessTypeCode.value;
									}
									
									if(content.properties && content.properties.tag){
										value = value +" / "+content.properties.tag.value;
									}
									
									sampleCodeAndTags.push(value);
								 });
									
								 if(plateCells[code][ln] == undefined){
										plateCells[code][ln] = new Array(); 
								 }
		
								plateCells[code][ln][col] = sampleCodeAndTags;
							}
							
							atomicIndex++;
						}
						
						allData = $filter('orderBy')(allData, ['inputContainer.support.code','inputContainer.support.column*1', 'inputContainer.support.line']);							
						$that.data.setData(allData, allData.length);	
						
						$that.plateCells=plateCells; 
				});		
			},
			addNewAtomicTransfertMethodsInData:function(){
				if(null != mainService.getBasket() && null != mainService.getBasket().get()){
					$that = this;
					$that.$commonATM.loadInputContainerFromBasket(mainService.getBasket().get())
						.then(function(containers) {								
							var allData = [], i = 0;
							angular.forEach(containers, function(container){
								var line = {};
								line.atomicIndex=i++;
								line.atomicTransfertMethod =  $that.newAtomicTransfertMethod(container.support.line, container.support.column);
								line.inputContainer = container;
								line.inputContainerUsed = $that.$commonATM.convertContainerToInputContainerUsed(line.inputContainer);
								allData.push(line);
							});
							allData = $filter('orderBy')(allData, ['inputContainer.support.code','inputContainer.support.column*1', 'inputContainer.support.line']);							
							$that.data.setData(allData, allData.length);											
					});
				}
			},
			experimentToView:function(experiment){
				if(null === experiment || undefined === experiment){
					throw 'experiment is required';
				}
				if(!$scope.isCreationMode()){
					this.convertExperimentATMToDatatable(experiment.atomicTransfertMethods, experiment.state.code);	
				}else{
					this.addNewAtomicTransfertMethodsInData();
				}	
								
			},
			viewToExperimentOneToMany :function(experimentIn){		
				if(null === experimentIn || undefined === experimentIn){
					throw 'experiment is required';
				}
				experiment = experimentIn;
				var allData = this.data.getData();
				if(allData != undefined){
					experiment.atomicTransfertMethods = []; // to manage remove
					//first reinitialise atomicTransfertMethod
					for(var i=0;i<allData.length;i++){
						var atomicIndex = allData[i].atomicIndex;								
						experiment.atomicTransfertMethods[atomicIndex] = allData[i].atomicTransfertMethod
						experiment.atomicTransfertMethods[atomicIndex].inputContainerUseds = new Array(0);
						var atm = experiment.atomicTransfertMethods[atomicIndex];
						
						var inputContainerUsed = allData[i].inputContainerUsed;
						this.$commonATM.removeNullProperties(inputContainerUsed.instrumentProperties);
						this.$commonATM.removeNullProperties(inputContainerUsed.experimentProperties);
						atm.inputContainerUseds.push(inputContainerUsed);	
						
						// Ne recreer les outputs que dans les etats Nouveau ou en cours (mais PAS si elle est terminee)
						if('F' !== experiment.state.code){
							experiment.atomicTransfertMethods[atomicIndex].outputContainerUseds = new Array(0);
							
							for(var j = 0; j < $scope.outputContainerSupportCodes.length ; j++){
								// si l'utilisateur a bien entré des supportCodes
								if ($scope.outputContainerSupportCodes[j] !== undefined && $scope.outputContainerSupportCodes[j] !== null && $scope.outputContainerSupportCodes[j] !== ''){
									var outputContainerUsed = this.$commonATM.newOutputContainerUsed(this.defaultOutputUnit, this.defaultOutputValue, atm.line, atm.column, inputContainerUsed);
									//affectation du SupportCode
									outputContainerUsed.locationOnContainerSupport.code=  $scope.outputContainerSupportCodes[j];
									//affectation du storageCode si defini
									if ( $scope.outputContainerSupportStorageCodes[j] !== undefined && $scope.outputContainerSupportStorageCodes[j] !== null){			  
										  outputContainerUsed.locationOnContainerSupport.storageCode=  $scope.outputContainerSupportStorageCodes[j];
									}
									atm.outputContainerUseds.push(outputContainerUsed);		
								}
							}
						}
					}
					
					//remove atomic null
					var cleanAtomicTransfertMethods = [];
					for(var i = 0; i < experiment.atomicTransfertMethods.length ; i++){
						if(experiment.atomicTransfertMethods[i] !== null){
							cleanAtomicTransfertMethods.push(experiment.atomicTransfertMethods[i]);
						}
					}
					experiment.atomicTransfertMethods = cleanAtomicTransfertMethods;
				}								
			},
			// necessaire pour le refresh
			refreshViewFromExperiment : function(experiment){
				if(null === experiment || undefined === experiment){
					throw 'experiment is required';
				}
				this.convertExperimentATMToDatatable(experiment.atomicTransfertMethods, experiment.state.code);				
			},
			getCellPlateData : function(code, column, line){
				//console.log ( code+"/"+column+"/"+line);
				if(this.plateCells && this.plateCells[code][line] && this.plateCells[code][line][column]){
					return this.plateCells[code][line][column];
				}
			}
	};
	
	atmService.experimentToView($scope.experiment);
	
	$scope.atmService = atmService;
		
	
}]);