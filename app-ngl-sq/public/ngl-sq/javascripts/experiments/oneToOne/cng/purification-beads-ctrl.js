// FDS 29/04/2021 - NGL-3380 Duplication à partir de additional-normalisation avec modifications EN COURS !!!!!
angular.module('home').controller('PurificationBeadsCtrl',['$scope', '$parse', '$http', 'atmToSingleDatatable',
                                                     function($scope, $parse, $http, atmToSingleDatatable){

	var inputExtraHeaders=Messages("experiments.inputs");
	var outputExtraHeaders=Messages("experiments.outputs");	
	
	var datatableConfig = {
			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[
					//--------------------- INPUT containers section -----------------------       
					{ // Projet(s)
						"header":Messages("containers.table.projectCodes"),
						"property":"inputContainer.projectCodes",
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
					{ //sample Aliquots
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
					{ // Issu du type d'expérience
						"header":Messages("containers.table.fromTransformationTypeCodes"),
						"property":"inputContainer.fromTransformationTypeCodes",
						"filter":"unique | codes:'type'",
						"hide":true,
						"order":false,
						"type":"text",
						"position":8,
						"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
						"extraHeaders":{0:inputExtraHeaders}
					},
					{ // libProcessType ajout 08/11/2016
						"header":Messages("containers.table.libProcessType"),
						"property": "inputContainer.contents",
						"filter": "getArray:'properties.libProcessTypeCode.value'| unique",
						"order":false,
						"hide":true,
						"type":"text",
						"position":6.5,
						"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
						"extraHeaders": {0: inputExtraHeaders}
					},
					{ //Tags
						"header":Messages("containers.table.tags"),
						"property": "inputContainer.contents",
						"filter": "getArray:'properties.tag.value'| unique",
						"order":true,
						"hide":true,
						"type":"text",
						"position":7,
						"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
						"extraHeaders":{0:inputExtraHeaders}
					},
					{ //Volume 
						"header":Messages("containers.table.volume") + " (µL)", 
						"property":"inputContainerUsed.volume.value",
						"order":true,
						"hide":true,
						"type":"number",
						"position":9,
						"extraHeaders":{0:inputExtraHeaders}
					},
					{ // Etat input Container
						"header":Messages("containers.table.state.code"),
						"property":"inputContainer.state.code | codes:'state'",
						"order":true,
						"hide":true,
						"type":"text",
						"position":10,
						"extraHeaders":{0:inputExtraHeaders}
					},
					// colonnes specifiques experience viennent ici.. 
					//   => Volume engagé, Volume tampon
					
					//------------------------ OUTPUT containers section -------------------
					{ //Volume 
						"header":Messages("containers.table.volume")+ " (µL)",
						"property":"outputContainerUsed.volume.value",
						"order":true,
						"edit":true,
						"required":"isRequired('IP')", // ajout + aussi règle drools "Volume not null in purification-beads outputContainer"
						"hide":true,
						"type":"number",
						"position":95,
						"extraHeaders":{0:outputExtraHeaders}
					},
					{ // Etat outpout container 
						"header":Messages("containers.table.state.code"),
						"property":"outputContainer.state.code | codes:'state'",
						"hide":true,
						"type":"text",
						"position":160,
						"extraHeaders":{0:outputExtraHeaders}
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
				mode:'local',
				active:true,
				// FDS : ce tri donne 1,10,11,12,2.... comment avoir un tri 1,2....10,11,12,13 ??
				//by:"inputContainer.support.column*1"
			},
			remove:{
				active: ($scope.isEditModeAvailable() && $scope.isNewState()),
				showButton: ($scope.isEditModeAvailable() && $scope.isNewState()),
				mode:'local'
			},
			save:{
				active:true,
				withoutEdit: true,
				changeClass:false,
				showButton:false,
				mode:'local',
				callback:function(datatable){
					// !!! ne doit pas être appelé si les output containers sont des tubes !! 
					if($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube"){
						atmService.copyContainerSupportCodeAndStorageCodeToDT(datatable,'userdef'); // laisser le choix de l'utilisateur' !!! 
					}
				}
			},
			hide:{
				active:true
			},
			edit:{ 
				active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
				showButton: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
				//// active: true,         pas vu !!!
				//// showButton: true,     pas vu !!!
				byDefault:($scope.isCreationMode()),
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
			},
			// "Baguettes magiques" pour definir les positions de plaque en sortie
			// 3447 ajouter CopyPosition  + le test du type de sortie
			otherButtons: {
                active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F') && ($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube")),
                complex:true,
                template:''
                	+$scope.plateUtils.templates.buttonLineMode()
                	+$scope.plateUtils.templates.buttonColumnMode()
                	+$scope.plateUtils.templates.buttonCopyPosition()
			}
	}; // fin struct datatableConfig
	
	//INPUT
	//  $scope.plateFluoUtils est dans details-ctrl.js; details-ctrl.js est deja appelé/inclus par ????
	var inputSupportCategoryCode = $scope.plateFluoUtils.getSupportCategoryCode();
	// retourne 'tube', 'plate-96-well' ou 'mixte'

	if (inputSupportCategoryCode !== "tube" ) {
		datatableConfig.columns.push({
			// barcode plaque entree == input support Container code
			"header":Messages("containers.table.support.name"),
			"property":"inputContainer.support.code",
			"hide":true,
			"type":"text",
			"position":1,
			"extraHeaders":{0: inputExtraHeaders}
		});
		datatableConfig.columns.push({
			// Ligne
			"header":Messages("containers.table.support.line"),
			"property":"inputContainer.support.line",
			"order":true,
			"hide":true,
			"type":"text",
			"position":2,
			"extraHeaders":{0:inputExtraHeaders}
		});
		datatableConfig.columns.push({
			// colonne
			"header":Messages("containers.table.support.column"),
			// astuce GA: pour pouvoir trier les colonnes dans l'ordre naturel forcer a numerique.=> type:number,   property:  *1
			"property":"inputContainer.support.column*1",
			"order":true,
			"hide":true,
			"type":"number",
			"position":3,
			"extraHeaders":{0:inputExtraHeaders}
		});
	} else {
			datatableConfig.columns.push({
				"header":Messages("containers.table.code"),
				"property":"inputContainer.support.code",
				"order":true,
				"edit":false,
				"hide":true,
				"type":"text",
				"position":1,
				"extraHeaders":{0:inputExtraHeaders}
			});
	}	
	
	// OUTPUT
	if ( $scope.experiment.instrument.outContainerSupportCategoryCode !== "tube" ){
		datatableConfig.columns.push({
			// barcode plaque sortie == support Container used code... faut Used 
			"header":Messages("containers.table.support.name"),
			"property":"outputContainerUsed.locationOnContainerSupport.code", 
			"order":true,
			"hide":true,
			"type":"text",
			"position":100,
			"extraHeaders":{0:outputExtraHeaders}
		});
		datatableConfig.columns.push({
			// Line
			"header":Messages("containers.table.support.line"),
			"property":"outputContainerUsed.locationOnContainerSupport.line", 
			"edit" : true,
			"choiceInList":true,
			"possibleValues":[{"name":'A',"code":"A"},{"name":'B',"code":"B"},{"name":'C',"code":"C"},{"name":'D',"code":"D"},
			                  {"name":'E',"code":"E"},{"name":'F',"code":"F"},{"name":'G',"code":"G"},{"name":'H',"code":"H"}],
        	"order":true,
			"hide":true,
			"type":"text",
			"position":105,
			"extraHeaders":{0:outputExtraHeaders}
		});
		datatableConfig.columns.push({
			 // column
			"header":Messages("containers.table.support.column"),
			"property":"outputContainerUsed.locationOnContainerSupport.column",
			"edit" : true,
			"choiceInList":true,
			"possibleValues":[{"name":'1',"code":"1"},{"name":'2',"code":"2"},  {"name":'3',"code":"3"},  {"name":'4',"code":"4"},
			                  {"name":'5',"code":"5"},{"name":'6',"code":"6"},  {"name":'7',"code":"7"},  {"name":'8',"code":"8"},
			                  {"name":'9',"code":"9"},{"name":'10',"code":"10"},{"name":'11',"code":"11"},{"name":'12',"code":"12"}],
			"order":true,
			"hide":true,
			"type":"number",
			"position":110,
			"extraHeaders":{0:outputExtraHeaders}
		});
	} else {
			// tube
			// GA: meme pour les tubes utiliser  x.locationOnContainerSupport.code  et pas x.code
			datatableConfig.columns.push({
				"header":Messages("containers.table.code"),
				"property":"outputContainerUsed.locationOnContainerSupport.code",
				"order":true,
				"edit":true,
				"editDirectives":"udt-change='atmService.emptyToNull(value.data, col.property)'",
				"hide":true,
				"type":"text",
				"position":100,
				"extraHeaders":{0:outputExtraHeaders}
			});
			datatableConfig.columns.push({
				"header":Messages("containers.table.storageCode"),
				"property":"outputContainerUsed.locationOnContainerSupport.storageCode",
				"order":true,
				"edit":true,
				"hide":true,
				"type":"text",
				"position":150,
				"extraHeaders":{0:outputExtraHeaders}
			});
	}
	
	// OK marche pour les tubes
	// mais il n'y a peut etre pas nécessaire si l'utilisateur ajoute qq chose au volume engagé => laisser saisir le volume de sortie ??
	var copyInputVolume= function(experiment){
		for(var i=0 ; i < experiment.atomicTransfertMethods.length ; i++){
			var atm = experiment.atomicTransfertMethods[i];
			
			volumeEng= atm.inputContainerUseds[0].experimentProperties["inputVolume"]
			console.log("vol eng="+volumeEng.value);
			angular.forEach(atm.outputContainerUseds, function(output){
				output.volume=volumeEng;
			});
		}
	};
	
	// gestion des plaques en sortie
	// !!!!  06/05/2021 correction blanc excédentaire => passait ici meme pour les tubes !!
	//       bug existe partout
	// renommage plus explicite
	//       => centraliser cette fonction !!!!!!!!! comment ca marche au CNS ???
	/*var updateATMLineAndColum= function(experiment){
		if(experiment.instrument.outContainerSupportCategoryCode !== "tube"){
			experiment.atomicTransfertMethods.forEach(function(atm){
				atm.line = atm.outputContainerUseds[0].locationOnContainerSupport.line;
				atm.column = atm.outputContainerUseds[0].locationOnContainerSupport.column;
			});
		}
	};
	*/
	
	$scope.$on('save', function(e, callbackFunction) {
		console.log("call event save");
		$scope.atmService.data.save();
		$scope.atmService.viewToExperimentOneToOne($scope.experiment);
		
		// OK marche pour les tubes mais pas necessaire ??? voir avec Julie
		//copyInputVolume($scope.experiment); 
		// gestion des plaques en sortie
		// essai utilisation  dans atmservice...
		$scope.atmService.updateATMLineAndColum($scope.experiment);
		
		$scope.$emit('childSaved', callbackFunction);
	});
	
	$scope.$on('refresh', function(e) {
		console.log("call event refresh");		
		var dtConfig = $scope.atmService.data.getConfig();
		dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));// 07/01/2019 mettre F ???
		///////dtConfig.edit.showButton = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));// 07/01/2019 ligne manquante !!
		dtConfig.edit.byDefault = false;
		dtConfig.edit.start = false; 
		dtConfig.remove.active = ($scope.isEditModeAvailable() && $scope.isNewState());
		$scope.atmService.data.setConfig(dtConfig);
		$scope.atmService.refreshViewFromExperiment($scope.experiment);
		// récupérer outputContainerSupport s'il été généré automatiquement (pas de barcode entré par l'utilisateur)
		$scope.outputContainerSupport.code=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.code;
		
		$scope.$emit('viewRefeshed');
	});
	
	$scope.$on('cancel', function(e) {
		console.log("call event cancel");
		$scope.atmService.data.cancel();
		
		if($scope.isCreationMode()){
			var dtConfig = $scope.atmService.data.getConfig();
			dtConfig.edit.byDefault = false;
			$scope.atmService.data.setConfig(dtConfig);
		}
	});
	
	$scope.$on('activeEditMode', function(e) {
		console.log("call event activeEditMode");
		$scope.atmService.data.selectAll(true);
		$scope.atmService.data.setEdit();
	});
		
	//Init
	
	var atmService = atmToSingleDatatable($scope, datatableConfig);
	
	// gestion des plaques en sortie
	// line et column sont indefinis au demarrage pour les plaques, ce sont les valeurs definies par l'utilisateur dans le 
	// datatable qui sont positionnees dans copyContainerSupportCodeAndStorageCodeToDT au momemt de la sauvegarde
	atmService.newAtomicTransfertMethod = function(l,c){
		return {
			class:"OneToOne",
			line:($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube")?undefined:"1",
			column:($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube")?undefined:"1",
			inputContainerUseds:new Array(0), 
			outputContainerUseds:new Array(0)
		};
	};
	
	//defined default output unit
	atmService.defaultOutputUnit = {
			volume : "µL",
			// 19/12/2018 Ne rien mettre par defaut ( attention ne pas supprimer la ligne concentration mais  =>concentration : ""  !!!!!!!!!!!!!
			concentration : ""
	};
	
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	
	// empecher la sortie en strip-8 choisi par instrument main; strip-8 pas gérés)'
	if ($scope.experiment.instrument.outContainerSupportCategoryCode !== 'strip-8'){
		$scope.messages.clear();
		$scope.atmService = atmService;
	} else {
		$scope.messages.setError(Messages("experiments.output.error.supportCategory","strip 8"));
	}
	
	$scope.outputContainerSupport = { code : null , storageCode : null};	// uniquement pour output Container=plaque !!
	
	if ( undefined !== $scope.experiment.atomicTransfertMethods[0]) { 
		 $scope.outputContainerSupport.code=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.code;
		 //console.log("previous code: "+ $scope.outputContainerSupport.code);
	}
	if ( undefined !== $scope.experiment.atomicTransfertMethods[0]) {
		$scope.outputContainerSupport.storageCode=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.storageCode;
		//console.log("previous storageCode: "+ $scope.outputContainerSupport.storageCode);
	}

	
	}]);