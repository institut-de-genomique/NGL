// 02/02/2022 NGL-3710 ajout tagService qui reprend certaines fonctions de tagPlates (suppression tagPlates car inutile ici)
angular.module('home').controller('NanoporeLibraryCtrlCNG',['$scope', '$parse', 'atmToSingleDatatable','tagService',
                                                         function($scope, $parse, atmToSingleDatatable, tagService) {
	
	// NGL-1055: name explicite pour fichier CSV exporté: typeCode experience
	// NGL-1055: mettre getArray et codes:'' dans filter et pas dans render	
	var datatableConfig = {
			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[			  
					 {
			        	 "header":Messages("containers.table.code"),
			        	 "property":"inputContainer.code",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":1,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },		         
			         {
			        	"header":Messages("containers.table.projectCodes"),
			 			"property": "inputContainer.projectCodes",
			 			"order":true,
			 			"hide":true,
			 			"type":"text",
			 			"position":2,
			 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
				     },
				     {
			        	"header":Messages("containers.table.sampleCodes"),
			 			"property": "inputContainer.sampleCodes",
			 			"order":true,
			 			"hide":true,
			 			"type":"text",
			 			"position":3,
			 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
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
			        	 "position":4,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },					 
					 {
			        	 "header":Messages("containers.table.concentration") + " (ng/µL)",
			        	 "property":"inputContainerUsed.concentration.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":5,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			         {
			        	 "header":Messages("containers.table.volume") + " (µL)",
			        	 "property":"inputContainerUsed.volume.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":6,
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
			        	 "position":7,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },	
			       //pass by copie of dynamic properties
			         /*
			         {
			        	 "header":"Conc. finale Ligation (ng/µL)",
			        	 "property":"outputContainerUsed.concentration.value",
			        	 "order":true,
						 "edit":true,
						 "hide":true,
			        	 "type":"number",
			        	 "position":50,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },*/
			         //pass by copie of dynamic properties
			         /*
			         {
			        	 "header":"Qté finale Ligation (ng)",
			        	 "property":"outputContainerUsed.quantity.value",
			        	 "order":true,
						 "edit":true,
						 "hide":true,
			        	 "type":"number",
			        	 "position":51,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },
			         */
			         // au CNG il faut "outputContainerUsed.locationOnContainerSupport.code" + edit: true
			         {
			        	 "header":Messages("containers.table.code"),
			        	 "property":"outputContainerUsed.locationOnContainerSupport.code",
			        	 "order":true,
						 "edit":true,
						 "hide":true,
			        	 "type":"text",
			        	 "position":400,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },
			         {
			        	 "header":Messages("containers.table.comments"),
			        	 "property":"outputContainerUsed.comment.comment",
			        	 "order":false,
						 "edit":true,
						 "hide":true,
			        	 "type":"textarea",
			        	 "position":590,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },
			         {
			        	 "header":Messages("containers.table.stateCode"),
			        	 "property":"outputContainer.state.code | codes:'state'",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":600,
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
	        	mode:'local',
	        	callback:function(datatable){
	        		copyLigationPropertiesToOutputContainerUsedQuantity(datatable);
	        	}
	        		
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
	
	var copyLigationPropertiesToOutputContainerUsedQuantity = function(datatable){
		var data = datatable.getData();
		angular.forEach(data, function(value){
			var ligationQuantity = $parse("outputContainerUsed.experimentProperties.ligationQuantity")(value);
			$parse("outputContainerUsed.quantity").assign(value, ligationQuantity);
			
			var ligationConcentration = $parse("outputContainerUsed.experimentProperties.ligationConcentration")(value);
			$parse("outputContainerUsed.concentration").assign(value, ligationConcentration);
		})
		//datatable.setData(data);
		
	} ;
	
	// copie du CNS, pas sur que ca serve...
	var removeTagCategoryIfNeeded = function(experiment){
		if(null !== experiment.atomicTransfertMethods && undefined !== experiment.atomicTransfertMethods){
			experiment.atomicTransfertMethods.forEach(function(atm){
				var tagCategory = $parse("outputContainerUseds[0].experimentProperties.tagCategory")(atm);
				var tag = $parse("outputContainerUseds[0].experimentProperties.tag")(atm);
				
				if((tag === null || tag === undefined) && 
						tagCategory !== null && tagCategory !== undefined 
						){
					atm.outputContainerUseds[0].experimentProperties.tagCategory = undefined;
				}
			})
		}
	};
	
	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event save");
		$scope.atmService.data.save();
		$scope.atmService.viewToExperimentOneToOne($scope.experiment);
		removeTagCategoryIfNeeded($scope.experiment);
		
		$scope.$emit('childSaved', callbackFunction);
	});
	
	$scope.$on('refresh', function(e) {
		console.log("call event refresh");		
		var dtConfig = $scope.atmService.data.getConfig();
		dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));
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
	
	//force ouptut = "tube"
	$scope.$watch("experiment.instrument.outContainerSupportCategoryCode", function(){
		$scope.experiment.instrument.outContainerSupportCategoryCode = "tube";
	});
	
	//Init
	
	var atmService = atmToSingleDatatable($scope, datatableConfig);
	//defined new atomictransfertMethod
	atmService.newAtomicTransfertMethod = function(){
		return {
			class:"OneToOne",
			line:"1", 
			column:"1", 				
			inputContainerUseds:new Array(0), 
			outputContainerUseds:new Array(0)
		};
	};
	
	//defined default output unit
	atmService.defaultOutputUnit = {
			volume : "µL",
			concentration : "ng/µl",
			quantity : "ng"
	}
	
	// NGL-3477 utiliser une liste dynamique pour les tags
	// !! les surcharges doivent etre faites avant experimentToView 
	atmService.convertOutputPropertiesToDatatableColumn = function(property, pName){
		var column = atmService.$commonATM.convertTypePropertyToDatatableColumn(property,"outputContainerUsed."+pName+".",{"0":Messages("experiments.outputs")});
		if(property.code==="tag"){
			column.editTemplate='<input class="form-control" type="text" #ng-model typeahead="tag.code as tag.name for tag in getTags() | filter:{groupNames:selectedTagGroup.value} | filter:{name:$viewValue} | limitTo:20" typeahead-min-length="1" udt-change="updatePropertyFromUDT(value,col)"/>'; 
		}
		return column;
	};
	
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	$scope.atmService = atmService;
	
	// NGL-3477 utiliser une liste dynamique pour les tags
	// l'appel a tagService.initTags() est maintenant obligatoire pour l'assignation automatique de tagCategory
	if ( $scope.isNewState() || $scope.isInProgressState() || Permissions.check("admin") ){
	   tagService.initTags('index-nanopore-sequencing');
	   $scope.getTags= function(){return tagService.getAllTags()};
	}
	
	$scope.updatePropertyFromUDT = function(value, col){
		console.log("update from property : "+col.property);
		if(col.property === 'outputContainerUsed.experimentProperties.tag.value'){
			tagService.computeTagCategory(value.data);
		}
	}
}]);