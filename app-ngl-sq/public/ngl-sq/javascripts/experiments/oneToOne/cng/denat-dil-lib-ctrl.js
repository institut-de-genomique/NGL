// FDS 04/10/2017 -- JIRA NGL-1584: denaturation en tubes et plaques
// 16/10/2017 restriction main: forcer tube en sortie
// 11/05/2018  le robot Epimotion n'est configuré que pour les container sortie=tube
//             ==> mise en commentaire du code des gestion des plaques ( pour le jour ou ca sera demandé...)

angular.module('home').controller('DenatDilLibCtrl',['$scope', '$parse', 'atmToSingleDatatable',
                                                     function($scope, $parse, atmToSingleDatatable){

	var inputExtraHeaders=Messages("experiments.inputs");
	var outputExtraHeaders=Messages("experiments.outputs");	
	
	// NGL-1055: name explicite pour fichier CSV exporté: typeCode experience
	// NGL-1055: mettre getArray et codes:'' dans filter et pas dans render
	var datatableConfig = {
			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[
			         //--------------------- INPUT containers section -----------------------
			         
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
				     { //sample Aliquots
			        	"header":Messages("containers.table.codeAliquot"),
			 			"property": "inputContainer.contents",
			 			"filter": "getArray:'properties.sampleAliquoteCode.value'| unique",
			 			"order":false,
			 			"hide":true,
			 			"type":"text",
			 			"position":6,
			 			"render": "<div list-resize='cellValue' list-resize-min-size='3'>",
			        	"extraHeaders":{0: inputExtraHeaders}
				     },
			         { // libProcessType ajout 04/10/2017
						 "header":Messages("containers.table.libProcessType"),
						 "property": "inputContainer.contents",
						 //"filter": "getArray:'properties.libProcessTypeCode.value'| codes:'libProcessTypeCode'",.. peut on decoder ???? 
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
			 			"order":false,
			 			"hide":true,
			 			"type":"text",
			 			"position":7,
			 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	"extraHeaders":{0: inputExtraHeaders}
			         },				 
					 {  //Concentration en nM;
			        	 "header":Messages("containers.table.concentration") + " (nM)",
			        	 "property":"inputContainerUsed.concentration.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":8,
			        	 "extraHeaders":{0: inputExtraHeaders}
			         },
			        /* { // volume pas necessaire ????
			        	 "header":function(){return Messages("containers.table.volume") + " (µL)"},
			        	 "property":"volume.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":9,
			        	 "extraHeaders":{0: inputExtraHeaders}
			         },*/
			         {  // Etat input Container
			        	 "header":Messages("containers.table.state.code"),
			        	 "property":"inputContainer.state.code",
						 "filter":"codes:'state'",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":10,
			        	 "extraHeaders":{0: inputExtraHeaders}
			         },
			         // colonnes specifiques experience viennent s'insererer ici s'il y en a
			          
			         //------------------------ OUTPUT containers section -------------------

			         { // Concentration shortLabel en pM;
			        	 "header":Messages("containers.table.concentration.shortLabel") + " (pM)",
			        	 "property":"outputContainerUsed.concentration.value",
			        	 "convertValue": {"active":true, "displayMeasureValue":"pM", "saveMeasureValue":"nM"},
			        	 "order":true,
						 "edit":true,
						 "hide":true,
						 "required":"isRequired('IP')",// 23/10/2017 doit être complété par validation par drools
			        	 "type":"number",
			        	 //"defaultValues":10,
			        	 "position":130,
			        	 "extraHeaders":{0:outputExtraHeaders}
			         },
			         { // volume en uL
			        	 "header":Messages("containers.table.volume")+ " (µL)",
			        	 "property":"outputContainerUsed.volume.value",
			        	 "order":true,
						 "edit":true,
						 "hide":true,
						 "required":"isRequired('IP')",// 23/10/2017 doit être complété par validation par drools
			        	 "type":"number",
			        	 "position":140,
			        	 "extraHeaders":{0:outputExtraHeaders}
			         },
			         { 
			        	 "header":Messages("containers.table.stateCode"),
			        	 "property":"outputContainer.state.code | codes:'state'",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":500,
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
				by:'inputContainer.code'
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
	        	mode:'local'
			},
			hide:{
				active:true
			},
			edit:{ /// 08/01/2019 modif a F
				active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
				showButton: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
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
				dynamic:true
			},
			otherButtons: {
                active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
                complex:true,
                template:  ''
                	+$scope.plateUtils.templates.buttonLineMode()
                	+$scope.plateUtils.templates.buttonColumnMode()
			}
	}; // fin struct datatableConfig
	
	// colonnes variables
	//INPUT
	// attention: 18/10/2017 experiment.instrument.inContainerSupportCategoryCode  depend de l'ordre de selection des inputs !!!
	// => devrait etre un array et pas une var simple !!

	if ( $scope.experiment.instrument.inContainerSupportCategoryCode !== "tube" ){
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
	        "extraHeaders":{0: inputExtraHeaders}
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
	        "extraHeaders":{0: inputExtraHeaders}
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
				"extraHeaders":{0: inputExtraHeaders}
			});
	}	
	
	// OUTPUT
	/* 11/05/2018 mise en attente (voir commentaire en debut du fichier)
	if ( $scope.experiment.instrument.outContainerSupportCategoryCode !== "tube" ){	
		 datatableConfig.columns.push({
			 // barcode plaque sortie == support Container used code... faut Used 
			 "header":Messages("containers.table.support.name"),
			 "property":"outputContainerUsed.locationOnContainerSupport.code", 
			 "order":true,
			 "hide":true,
			 "type":"text",
			 "position":100,
			 "extraHeaders":{0: outputExtraHeaders}
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
        	 "position":110,
        	 "extraHeaders":{0:outputExtraHeaders}
         });
		 datatableConfig.columns.push({
			 // column
        	 "header":Messages("containers.table.support.column"),
        	 "property":"outputContainerUsed.locationOnContainerSupport.column",
 			 "edit" : true,
			 "choiceInList":true,
			 "possibleValues":[{"name":'1',"code":"1"},{"name":'2',"code":"2"},{"name":'3',"code":"3"},{"name":'4',"code":"4"},
			                   {"name":'5',"code":"5"},{"name":'6',"code":"6"},{"name":'7',"code":"7"},{"name":'8',"code":"8"},
			                   {"name":'9',"code":"9"},{"name":'10',"code":"10"},{"name":'11',"code":"11"},{"name":'12',"code":"12"}], 
        	 "order":true,
			 "hide":true,
        	 "type":"number",
        	 "position":120,
        	 "extraHeaders":{0:outputExtraHeaders}
         });		
	} else {
	*/
		    // tube
		    // GA: meme pour les tubes utiliser  x.locationOnContainerSupport.code  et pas x.code
			datatableConfig.columns.push({
				"header":Messages("containers.table.code"),
				"property":"outputContainerUsed.locationOnContainerSupport.code",
				"order":true,
				"edit":true,
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
	//}


	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event save");
		
		$scope.atmService.data.save();
		$scope.atmService.viewToExperimentOneToOne($scope.experiment);
		
		if ( $scope.experiment.instrument.outContainerSupportCategoryCode !== "tube" ) {
			copyContainerSupportCodeAndStorageCode($scope.experiment);
		}
		
		$scope.$emit('childSaved', callbackFunction);
	});
	
	$scope.$on('refresh', function(e) {
		console.log("call event refresh");	
		
		var dtConfig = $scope.atmService.data.getConfig();
		dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));// 08/01/2019 modifier IP=>F pour autoriser edition
		//dtConfig.edit.showButton = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')); pas utile ??
		dtConfig.edit.byDefault = false;
		dtConfig.edit.start = false; 
		dtConfig.remove.active = ($scope.isEditModeAvailable() && $scope.isNewState());
		$scope.atmService.data.setConfig(dtConfig);
		$scope.atmService.refreshViewFromExperiment($scope.experiment);
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
	

	// si Output != tube (plaque et strip ??)
	/* 11/05/2018 mise en attente (voir commentaire en debut du fichier)
	if ( $scope.experiment.instrument.outContainerSupportCategoryCode !== "tube" ) {

		$scope.outputContainerSupport = { code : null , storageCode : null};	
	
		if ( undefined !== $scope.experiment.atomicTransfertMethods[0]) { 
			$scope.outputContainerSupport.code=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.code;
			console.log("previous code: "+ $scope.outputContainerSupport.code);
		
			$scope.outputContainerSupport.storageCode=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.storageCode;
			console.log("previous storageCode: "+ $scope.outputContainerSupport.storageCode);
		}
	}
	*/
	

	// modifié a partir de copyContainerSupportCodeAndStorageCodeToDT
	var copyContainerSupportCodeAndStorageCode = function(experiment){
	
		var outputContainerSupportCode = $scope.outputContainerSupport.code;
		var outputContainerSupportStorageCode = $scope.outputContainerSupport.storageCode;

		// 14/03/2019 correction locale de NGL-2371...: ajout && ""!= outputContainerSupportCode 
		//( correction qui ne sert a rien en réalité puisque pour l'instant output forcé a tube !!! voir commentaires 11/05/2018)
		if ( null != outputContainerSupportCode && undefined != outputContainerSupportCode && ""!= outputContainerSupportCode ){
			for(var i = 0; i < experiment.atomicTransfertMethods.length; i++){
				var atm = experiment.atomicTransfertMethods[i];
				
				// on est dans du oneToOne=> 1 seul containerUsed -->  [0]
				$parse('outputContainerUseds[0].locationOnContainerSupport.code').assign(atm,outputContainerSupportCode);
				if( null != outputContainerSupportStorageCode && undefined != outputContainerSupportStorageCode){
				    $parse('outputContainerUseds[0].locationOnContainerSupport.storageCode').assign(atm,outputContainerSupportStorageCode);
				}
				
				// Obligatoire !!!
				atm.line = atm.outputContainerUseds[0].locationOnContainerSupport.line;
				atm.column = atm.outputContainerUseds[0].locationOnContainerSupport.column;
			}
		}
	}	
	
	//Init
	var atmService = atmToSingleDatatable($scope, datatableConfig);
	
	//defined new atomictransfertMethod
	atmService.newAtomicTransfertMethod =  function(line, column){
		// gestion de tubes ET plaques
		var getLine = function(line){
			if($scope.experiment.instrument.outContainerSupportCategoryCode 
					=== $scope.experiment.instrument.inContainerSupportCategoryCode){
				return line;
			}else if($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube" 
				&& $scope.experiment.instrument.inContainerSupportCategoryCode === "tube") {
				return undefined;     // dans ce cas c'est l'utilisateur qui le defini manuellement
			}else if($scope.experiment.instrument.outContainerSupportCategoryCode === "tube"){
				return "1";
			}
			
		}
		var getColumn=getLine;
		
		return {
			class:"OneToOne",
			line:getLine(line), 
			column:getColumn(column),
			inputContainerUseds:new Array(0), 
			outputContainerUseds:new Array(0)
		};
	};
	
	//defined default output unit
	atmService.defaultOutputUnit = {
			volume : "µL",
			concentration : "nM"
	}
	
	atmService.experimentToView($scope.experiment, $scope.experimentType);

	// 16/10/2017 restriction main: forcer tube en sortie
	$scope.$watch("experiment.instrument.outContainerSupportCategoryCode", function(){
		if  ($scope.experiment.instrument.typeCode === "hand"){
			//console.log("instrument.typeCode === "+ $scope.experiment.instrument.typeCode+ " force tube");
			$scope.experiment.instrument.outContainerSupportCategoryCode = "tube";
		}
	});
	
	// 18/10/2017 restriction epmotion: uniquement tube en entree 
    /* $scope.experiment.instrument.inContainerSupportCategoryCode  contient le code du premier input choisi !!!!!
       ce qui ne repond pas a la vrai question=>  il faut lister le type de TOUS les containers en input et verifier qu'aucun n'est un puit!!!!
	    ce type de code est utilisé partout !!!! BUGS....
	*/
	if ( ($scope.experiment.instrument.typeCode === 'epmotion') && ($scope.experiment.instrument.inContainerSupportCategoryCode !=="tube") ){
		console.log("Le robot Epmotion n'autorise que des tubes en entrée");
		$scope.messages.setError(Messages("experiments.input.error.instrument-input.only-tubes"));
	} else {
		$scope.messages.clear();
		$scope.atmService = atmService;
	}

}]);