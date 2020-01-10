/* FDS 02/03/2017 -- JIRA NGL-1167 : processus Chromium
   code copié depuis library-prep-ctrl......==> utiliser plaque d'index Chromium????? Pas encore specifie...
   
   2 fonctionnements  -main     : strip-8       => tubes         ( Julie demande de bloquer  96-well-plate pour l'instant...)
                      -sciclone : 96-well-plate => 96-well-plate
*/
angular.module('home').controller('WgChromiumLibraryPrepCtrl',['$scope', '$parse',  '$filter', 'atmToSingleDatatable','$http',
                                                     function($scope, $parse, $filter, atmToSingleDatatable, $http){
	
	var inputExtraHeaders=Messages("experiments.inputs");
	var outputExtraHeaders=Messages("experiments.outputs");	
	
	var datatableConfig = {
			name: $scope.experiment.typeCode.toUpperCase(),

			"columns":[
			         //--------------------- INPUT containers section -----------------------
			          { // barcode support entree
			        	 "header":Messages("containers.table.support.name"),
			        	 "property":"inputContainer.support.code",
						 "hide":true,
			        	 "type":"text",
			        	 "position":1,
			        	 "extraHeaders":{0: inputExtraHeaders}
			         },    
			         // Ligne:  seulement pour plaques voir + loin
			         { // colonne:  strip-8 ou plaque
			        	 "header":Messages("containers.table.support.column"),
			        	 // astuce GA: pour pouvoir trier les colonnesCode Container dans l'ordre naturel forcer a numerique.=> type:number,   property:  *1
			        	 "property":"inputContainer.support.column*1",
			        	 "order":true,
						 "hide":true,
			        	 "type":"number",
			        	 "position":3,
			        	 "extraHeaders":{0: inputExtraHeaders}
			         },	
			         //--->  colonnes specifiques experience s'inserent ici  (inputUsed ??)     
			         
			         //------------------------- OUTPUT containers section --------------------------
			         
			         //--->  colonnes specifiques experience s'inserent ici  (outputUsed ??)
			         
		            /* ne pas aficher les containercodes  sauf pour DEBUG
			         {
			        	 "header":"DEBUG code",
			        	 "property":"outputContainer.code",
			        	 "order":true,
						 "hide":true,
						 "edit":false,
			        	 "type":"text",
			        	 "position":500,
			        	 "extraHeaders":{0: outputExtraHeaders}
			         },	 */	     
			         { // Volume avec valeur par defaut
			        	 "header":Messages("containers.table.volume") + " (µL)",
			        	 "property":"outputContainerUsed.volume.value",
			        	 "hide":true,
			        	 "edit":true,
			        	 "type":"number",
			        	 "defaultValues":20,
			        	 "position":34,
			        	 "extraHeaders":{0: outputExtraHeaders}
			         }
			         ],
			"compact":true,
			"pagination":{
				"active":false
			},		
			"search":{
				"active":false
			},
			"order":{
				"mode":"local",
				"active":true
			},
			"remove":{
				"active": ($scope.isEditModeAvailable() && $scope.isNewState()),
				"showButton": ($scope.isEditModeAvailable() && $scope.isNewState()),
				"mode":"local"
			},
			"save":{
				"active":true,
	        	"withoutEdit": true,
	        	"changeClass":false,
	        	"showButton":false,
	        	"mode":"local",
	        	"callback":function(datatable){
	        		copyContainerSupportCodeAndStorageCodeToDT(datatable);
	        	}
			},
			"hide":{
				"active":true
			},
			"edit":{ // editable si mode=Finished ????
				active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
				showButton: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
				byDefault:($scope.isCreationMode()),
				columnMode:true
			},
			"messages":{
				"active":false,
				"columnMode":true
			},
			"exportCSV":{
				"active":true,
				"showButton":true,
				"delimiter":";",
				"start":false
			},
			"extraHeaders":{
				"number":2,
				"dynamic":true,
			}
			/*,
			"otherButtons": {
                active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
                complex:true,
                template:''
                	+'<div class="btn-group" style="margin-left:5px">'
                	+'<button class="btn btn-default" ng-click="copyVolumeInToExp()" data-toggle="tooltip" title="'+Messages("experiments.button.plate.copyVolumeTo")+' vol. eng. librairie'
                	+'" ng-disabled="!isEditMode()" ng-if="experiment.instrument.outContainerSupportCategoryCode!==\'tube\'"><i class="fa fa-files-o" aria-hidden="true"></i> Volume </button>'                	                	
                	+'</div>'
             
			}*/
	}; // fin struct datatableConfig
	
	
	// probleme de rafraichissement de la vue en cas de mauvais choix inital de l'utilisateur
	// dans le watch  "$scope.experiment.instrument.categoryCode"   => forcer la vue ici...
	if ( $scope.experiment.instrument.categoryCode === "hand") {
		if($scope.experiment.instrument.outContainerSupportCategoryCode === "tube") {
			// tubes 
			datatableConfig.columns.push({
				"header" : Messages("containers.table.code"),
				"property" : "outputContainerUsed.locationOnContainerSupport.code",
				"order" : true,
				"edit" : true,
				"hide" : true,
				"type" : "text",
				"position" : 400,
				"extraHeaders" : {
					0 : Messages("experiments.outputs")
				}
			});			
			
			datatableConfig.columns.push({
				//storage pour tubes
				"header" : Messages("containers.table.storageCode"),
				"property" : "outputContainerUsed.locationOnContainerSupport.storageCode",
				"order" : true,
				"edit" : true,
				"hide" : true,
				"type" : "text",
				"position" : 401,
				"extraHeaders" : {
					0 : Messages("experiments.outputs")
				}
			});
		} else {
		//strip-8
			datatableConfig.columns.push({
				// barcode plaque sortie == support Container used code
				"header" : Messages("containers.table.support.name"),
				"property" : "outputContainerUsed.locationOnContainerSupport.code",
				"order" : true,
				"edit" : true,  // Est-ce normal de pouvoir editer le code du strip ???????????
				"hide" : true,
				"type" : "text",
				"position" : 400,
				"extraHeaders" : {
					0 : Messages("experiments.outputs")
				}
			});
			
			datatableConfig.columns.push({
				// Ligne
				"header" : Messages("containers.table.support.column"),
				"property" : "outputContainerUsed.locationOnContainerSupport.column",
				"edit" : false, // la position sur un strip n'est pas editable !!!
				"order" : true,
				"hide" : true,
				"type" : "text",
				"position" : 401,
				"extraHeaders" : {
					0 : Messages("experiments.outputs")
				}
			});			
			
		}
	} else {
		// l'autre cas pour l'instant est le sciclone qui n'a que des plaques-96 en sortie
	
		datatableConfig.columns.push({
			// barcode plaque sortie == support Container used code
			"header" : Messages("containers.table.support.name"),
			"property" : "outputContainerUsed.locationOnContainerSupport.code",
			"hide" : true,
			"type" : "text",
			"position" : 400,
			"extraHeaders" : {
				0 : Messages("experiments.outputs")
			}
		});
		

		datatableConfig.columns.push({
			// Ligne
			"header" : Messages("containers.table.support.line"),
			"property" : "outputContainerUsed.locationOnContainerSupport.line",
			"edit" : true,
			"order" : true,
			"hide" : true,
			"type" : "text",
			"position" : 401,
			"extraHeaders" : {
				0 : Messages("experiments.outputs")
			}
		});
		
		datatableConfig.columns.push({
			// colonne
			"header" : Messages("containers.table.support.column"),
			// astuce GA: pour pouvoir trier les colonnes dans l'ordre naturel
			// forcer a numerique.=> type:number, property: *1
			"property" : "outputContainerUsed.locationOnContainerSupport.column*1",
			"edit" : true,
			"order" : true,
			"hide" : true,
			"type" : "number",
			"position" : 402,
			"extraHeaders" : {
				0 : Messages("experiments.outputs")
			}
		});
	} 
	
	// en mode plaque ou strip uniquement !!!!!!
	// 14/03/2019 TODO ?? remplacer par 
	//"callback":function(datatable){
	//          if ($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube") 
	//                   { atmService.copyContainerSupportCodeAndStorageCodeToDT( datatable,'auto')} 
	// }
	var copyContainerSupportCodeAndStorageCodeToDT = function(datatable){		
		if($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube") {
			var dataMain = datatable.getData();
			var outputContainerSupportCode = $scope.outputContainerSupport.code;
			var outputContainerSupportStorageCode = $scope.outputContainerSupport.storageCode;
			
			// 14/03/2019 correction locale pour NGl-2371: ajout && "" != outputContainerSupportCode
			if ( null != outputContainerSupportCode && undefined != outputContainerSupportCode && "" != outputContainerSupportCode){
				for(var i = 0; i < dataMain.length; i++){
				
					var atm = dataMain[i].atomicTransfertMethod;
					var newContainerCode = outputContainerSupportCode+"_"+atm.line + atm.column;

					$parse('outputContainerUsed.code').assign(dataMain[i],newContainerCode);
					$parse('outputContainerUsed.locationOnContainerSupport.code').assign(dataMain[i],outputContainerSupportCode);
				
					// Historique mais continuer a renseigner car effets de bord possible ???? Note 08/03/2019 n'existe pas dans dautres experiences et ca marche qud meme !!!!!
					$parse('line').assign(atm, atm.line);
					$parse('column').assign(atm,atm.column );
					//console.log("atm.line="+ atm.line + " atm.column="+atm.column);	
				
					if( null != outputContainerSupportStorageCode && undefined != outputContainerSupportStorageCode){
						$parse('outputContainerUsed.locationOnContainerSupport.storageCode').assign(dataMain[i],outputContainerSupportStorageCode);
					}
				}
			}		
		}
		// Ne plus faire ... datatable.setData(dataMain);
	}
	
	// correction NGL-2371: attente correction traitement correct strip ? NGL-2491
	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event save");
		$scope.atmService.data.save();
		$scope.atmService.viewToExperimentOneToOne($scope.experiment);
		$scope.$emit('childSaved', callbackFunction);
	});
	
	// correction NGL-2371: attente correction traitement correct strip ? NGL-2491
	$scope.$on('refresh', function(e) {
		console.log("call event refresh");		
		var dtConfig = $scope.atmService.data.getConfig();
		dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));
		
		/// NECESSAIRE ??
		// dtConfig.edit.showButton = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));
		
		dtConfig.edit.byDefault = false;
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
	

	//Init
	
	var atmService = atmToSingleDatatable($scope, datatableConfig);
	//defined new atomictransfertMethod
	atmService.newAtomicTransfertMethod = function(l, c){
		return {
			class:"OneToOne",
			line: l, 
			column: c, 				
			inputContainerUseds:new Array(0), 
			outputContainerUseds:new Array(0)
		};
	};
	
	//defined default output unit
	atmService.defaultOutputUnit = {
			volume : "µL"
	};
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	// le sciclone ne traite que des plaques (le type de container de sortie est deja restreint)
	if ( ( $scope.experiment.instrument.categoryCode !== 'hand') && ($scope.experiment.instrument.inContainerSupportCategoryCode !== $scope.experiment.instrument.outContainerSupportCategoryCode) ) {
		$scope.messages.setError(Messages('experiments.input.error.must-be-same-out'));
	} else {
		$scope.messages.clear();
		$scope.atmService = atmService;
	}
	
    // recuperer les tags existants
	$http.get(jsRoutes.controllers.commons.api.Parameters.list().url,{params:{typeCode:"index-illumina-sequencing"}})
	.success(function(data, status, headers, config) {
			$scope.tags = data;		
	})
	
	// Calculs 
	$scope.updatePropertyFromUDT = function(value, col){
		console.log("update from property : "+col.property);
		
		if(col.property === 'outputContainerUsed.experimentProperties.tag.value'){
			computeTagCategory(value.data);			
		}
	}
	
	// determination  automatique de TagCategory
	var computeTagCategory = function(udtData){
		var getter = $parse("outputContainerUsed.experimentProperties.tagCategory.value");
		var tagCategory = getter(udtData);
		
		var compute = {
				tagValue : $parse("outputContainerUsed.experimentProperties.tag.value")(udtData),
				tag : $filter("filter")($scope.tags,{code:$parse("outputContainerUsed.experimentProperties.tag.value")(udtData)},true),
				isReady:function(){
					return (this.tagValue && this.tag && this.tag.length === 1);
				}
		};
		
		if(compute.isReady()){
			var result = compute.tag[0].categoryCode;
			console.log("result = "+result);
			if(result){
				tagCategory = result;				
			}else{
				tagCategory = undefined;
			}	
			getter.assign(udtData, tagCategory);
		}else if(compute.tagValue){
			getter.assign(udtData, undefined);
		}
	}
	
	$scope.outputContainerSupport = { code : null , storageCode : null};	
		
	if ( undefined !== $scope.experiment.atomicTransfertMethods[0]) { 
		 $scope.outputContainerSupport.code=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.code;
		//console.log("previous code: "+ $scope.outputContainerSupport.code);
	}
	if ( undefined !== $scope.experiment.atomicTransfertMethods[0]) {
		$scope.outputContainerSupport.storageCode=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.storageCode;
		//console.log("previous storageCode: "+ $scope.outputContainerSupport.storageCode);
	}
	
	/* FORCER A STRIP si instrument= main  et  sortie=plaque  */
	$scope.$watch("$scope.experiment.instrument.categoryCode", function(){
			if (($scope.experiment.instrument.categoryCode === "hand") && ($scope.experiment.instrument.outContainerSupportCategoryCode === "96-well-plate"))
				$scope.experiment.instrument.outContainerSupportCategoryCode = "strip-8";
	});	
		
	
/* pas specifié, voir plus tard..................?????????????????????//
 
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
			// reinit select File...
			angular.element('#importFile')[0].value = null;
			$scope.$emit('refresh');
			
		})
		.error(function(data, status, headers, config) {
			
			$scope.messages.clazz = "alert alert-danger";
			$scope.messages.text = Messages('experiments.msg.import.error');
			$scope.messages.setDetails(data);
			$scope.messages.open();	
			$scope.file = undefined;
			// reinit select File...
			angular.element('#importFile')[0].value = null;
		});		
	};
	
	// importer un fichier definissant quels index sont déposés dans quels containers
	$scope.button = {
		isShow:function(){
			return ( $scope.isInProgressState() && !$scope.mainService.isEditMode())
			},
		isFileSet:function(){
			return ($scope.file === undefined)?"disabled":"";
		},
		click:importData,		
	};
	
	// Autre mode possible : utiliser une plaque d'index prédéfinis, l'utilisateur a juste a indiquer a partir de quelle colonne
	// de cette plaque le robot doit prelever les index
	//  voir pcr-and-indexing-ctrl.js; prep-pcr-free-tcrl.js   

*/
	
}]);