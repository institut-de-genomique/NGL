// 02/05/2019  Copié a partir de  pcr-and-purification et adapté
// 15/09/2019 gestion des tubes en sortie => code recupere dans denat-dil-lib-ctrl
// 02/02/2022 NGL-3710 ajout tagService qui remprend certaines fonctions de tagPlates (suppression tagPlates car inutile ici)
angular.module('home').controller('PcrAmplifAndPurifAtacChipSeqCtrl',['$scope', '$parse', 'atmToSingleDatatable','tagService',
                                                    function($scope, $parse, atmToSingleDatatable, tagService){
	// variables pour extraheaders
	var inputExtraHeaders=Messages("experiments.inputs");
	var outputExtraHeaders=Messages("experiments.outputs");	
	
	var datatableConfig = {
			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[
			         //--------------------- INPUT containers section -----------------------
					 // entree tubes
					 {
		        		"header":Messages("containers.table.code"),
		        		"property":"inputContainer.code",
						"hide":true,
			        	"type":"text",
			        	"position":1,
			        	"extraHeaders":{0: inputExtraHeaders}
			         },    
			         {
			        	"header":Messages("containers.table.projectCodes"),
			 			"property": "inputContainer.projectCodes",
			 			"order":true,
			 			"hide":true,
			 			"type":"text",
			 			"position":4,
			 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	"extraHeaders":{0:inputExtraHeaders}
				     },
				     {
			        	"header":Messages("containers.table.sampleCodes"),
			 			"property": "inputContainer.sampleCodes",
			 			"order":true,
			 			"hide":true,
			 			"type":"text",
			 			"position":5,
			 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	"extraHeaders":{0:inputExtraHeaders}
				     },
			         { 
				       "header": Messages("containers.table.libProcessType"),
				       "property" : "inputContainerUsed.contents",
				       "filter" : "getArray:'properties.libProcessTypeCode.value' |unique | codes:'value'",
				       "order":true,
					   "edit":false,
					   "hide":true,
				       "type":"text",
				       "position":5.8,
				       "extraHeaders":{0:inputExtraHeaders}
				     },
			         {
			        	"header":Messages("containers.table.volume") + " (µL)",
			        	"property":"inputContainerUsed.volume.value",
			        	"order":true,
						"edit":false,
						"hide":true,
			        	"type":"number",
			        	"position":6,
			        	"extraHeaders":{0:inputExtraHeaders}
			         },
			         {
			        	"header":Messages("containers.table.state.code"),
			        	"property":"inputContainer.state.code",
			        	"order":true,
						"edit":false,
						"hide":true,
			        	"type":"text",
						"filter":"codes:'state'",
			        	"position":7,
			        	"extraHeaders":{0:inputExtraHeaders}
			         },
			         // colonnes specifiques experience viennent ici...

				     //--------------------- OUTPUT containers section -----------------------
				     // sortie tube 
				     // ajout volume NGL-2607
			         {
			        	"header":Messages("containers.table.volume")+ " (µL)",
			        	"property":"outputContainerUsed.volume.value",
			        	"editDirectives":"udt-change='updatePropertyFromUDT(value,col)'",
			        	"tdClass":"valuationService.valuationCriteriaClass(value.data, experiment.status.criteriaCode, col.property)",
			        	"order":true,
						"edit":true,
						"hide":true,
			        	"type":"number",
			        	/////"defaultValues":30,
			        	"position":300,
			        	"extraHeaders":{0:outputExtraHeaders}
			         },
			         {
			        	"header":Messages("containers.table.stateCode"),
			        	"property":"outputContainer.state.code | codes:'state'",
			        	"order":true,
						"edit":false,
						"hide":true,
			        	"type":"text",
			        	"position":800,
			        	"extraHeaders":{0:outputExtraHeaders}
			         },
			         {
			        	 "header":Messages("containers.table.comments"),
			        	 "property":"outputContainerUsed.comment.comment",
			        	 "order":false,
						 "edit":true,
						 "hide":true,
			        	 "type":"textarea",
			        	 "position":900,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },
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
				active:true
			},
			remove:{
				active: ($scope.isEditModeAvailable() && $scope.isNewState()),
				showButton: ($scope.isEditModeAvailable() && $scope.isNewState()),
				mode:'local'
			},
			save:{
				active:true,
	        	withoutEdit: true,
	        	showButton:false,
	        	changeClass:false,
	        	mode:'local'
			},
			hide:{
				active:true
			},
			edit:{
				active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
				showButton: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
				byDefault:($scope.isCreationMode()),
				columnMode:true 
			},
			messages:{
				active:false
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
			otherButtons: {
                active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
                complex:true,
                template:''
                	+$scope.plateUtils.templates.buttonLineMode()
                	+$scope.plateUtils.templates.buttonColumnMode()
			}
    }; // fin struct datatableConfig
	
	// colonnes variables	
	
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
	}
	
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
		dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));
		// showButton pas necessaire...
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
	
	$scope.copyVolumeInToExp = function(){
		var data = $scope.atmService.data.displayResult;
		data.forEach(function(value){
			$parse("inputContainerUsed.experimentProperties.inputVolume").assign(value.data, angular.copy(value.data.inputContainer.volume));
		})		
	};
	
	// recuperer les proprietes de niveau Instrument+containerOut qui ne sont pas injectees automatiquement dans le datatable, mais uniquement a la selection de l'instrument
	$scope.$watch("instrumentType", function(newValue, OldValue){
        if(newValue)
            $scope.atmService.addInstrumentPropertiesToDatatable(newValue.propertiesDefinitions);
    });
	
	// si Output != tube (plaque et strip ??)
	if ( $scope.experiment.instrument.outContainerSupportCategoryCode !== "tube" ) {

		$scope.outputContainerSupport = { code : null , storageCode : null};	
	
		if ( undefined !== $scope.experiment.atomicTransfertMethods[0]) { 
			$scope.outputContainerSupport.code=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.code;
			console.log("previous code: "+ $scope.outputContainerSupport.code);
		
			$scope.outputContainerSupport.storageCode=$scope.experiment.atomicTransfertMethods[0].outputContainerUseds[0].locationOnContainerSupport.storageCode;
			console.log("previous storageCode: "+ $scope.outputContainerSupport.storageCode);
		}
	}
	
	// modifié a partir de copyContainerSupportCodeAndStorageCodeToDT
	var copyContainerSupportCodeAndStorageCode = function(experiment){
	
		var outputContainerSupportCode = $scope.outputContainerSupport.code;
		var outputContainerSupportStorageCode = $scope.outputContainerSupport.storageCode;

		// 14/03/2019 correction locale de NGL-2371...: ajout && ""!= outputContainerSupportCode 
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
	
	// aide a la saisie des index
	// !! les surcharges doivent etre faites avant experimentToView 
	atmService.convertOutputPropertiesToDatatableColumn = function(property, pName){
		var column = atmService.$commonATM.convertTypePropertyToDatatableColumn(property,"outputContainerUsed."+pName+".",{"0":Messages("experiments.outputs")});
		if(property.code==="tag"){ 
			//d'ou vient ce "lists" qui prefixe getTags() ??; comment est fait le filtrage puisqu'il n'a plus: | filter:{groupNames:selectedTagGroup.value} | filter:{name:$viewValue} | limitTo:20"
			column.editTemplate='<div class="form-control" bt-select  #ng-model filter="true" bt-options="tag.code as tag.name for tag in lists.getTags()" typeahead-min-length="1" udt-change="updatePropertyFromUDT(value,col)" /></div>';
		}
		return column;
	};
	
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	$scope.atmService = atmService;
	
	// NGL-1350 aide a la saisie des tags
	// l'appel a tagService.initTags() est maintenant obligatoire pour l'assignation automatique de tagCategory
	if ( $scope.isNewState() || $scope.isInProgressState() || Permissions.check("admin") ){
	   tagService.initTags(); 
	   
	   $scope.getTagGroups= function(){return tagService.getAllTagGroups()};
	   $scope.selectedTagGroup= $scope.getTagGroups()[0]; // valeur defaut du select 
	}
   
	$scope.selectGroup = {
			isShow:function(){
				//return ( ( $scope.isInProgressState() &&  $scope.isEditMode() ) || Permissions.check("admin") );
				// affichage dès l'etat New
				return (  $scope.isNewState() || $scope.isInProgressState()  || Permissions.check("admin") );
			},
	
		    select:function(groupName){
		    	console.log( 'groupe choisi :'+  groupName.value );
		    	
		    	//GA: creer une variable $scope.tags au lieu de d'ecraser la fonction getTags
		    	if (groupName.value === undefined ){ 
					$scope.lists.refresh.tags({typeCodes:'index-illumina-sequencing'}); // 14/02/2022 supression nanopore
				} else { 
					$scope.lists.refresh.tags({typeCodes:'index-illumina-sequencing',groupNames:[groupName.value]});// 14/02/2022 supression nanopore
				}
		    }
	};

	
	$scope.updatePropertyFromUDT = function(value, col){
		//console.log("update from property : "+col.property);
		if(col.property === 'outputContainerUsed.experimentProperties.tag.value'){
			tagService.computeTagCategory(value.data);
		}
	}

}]);
