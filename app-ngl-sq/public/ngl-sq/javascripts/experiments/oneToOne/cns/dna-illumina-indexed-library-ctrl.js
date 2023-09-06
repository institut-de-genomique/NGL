// NGL-3667 16/02/2022 aide à la saisie des tags: ajout tagService
angular.module('home').controller('DNAIlluminaIndexedLibraryCtrl',['$scope', '$parse', '$http', '$filter', 'atmToSingleDatatable', 'tagService',
                                                    function($scope, $parse, $http, $filter, atmToSingleDatatable, tagService){
                                                    
	var datatableConfig = {
			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[	
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
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			 			"render":"<div list-resize='cellValue | unique | codes:\"type\"' list-resize-min-size='3'>",
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
			        	 "header":Messages("containers.table.quantity") + " (ng)",
			        	 "property":"inputContainerUsed.quantity.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":6.5,
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
			         {
			        	 "header":Messages("containers.table.volume")+ " (µL)",
			        	 "property":"outputContainerUsed.volume.value",
			        	 "order":true,
						 "edit":true,
						 "hide":true,
			        	 "type":"number",
			        	 "position":51,
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
			        	 "header":Messages("containers.table.storageCode"),
			        	 "property":"outputContainerUsed.locationOnContainerSupport.storageCode",
			        	 "order":true,
						 "edit":true,
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
				number:1,
				dynamic:true,
			},
			otherButtons: {
				active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
				complex:true,
				template:  ''
					+$scope.plateUtils.templates.buttonLineMode()
					+$scope.plateUtils.templates.buttonColumnMode()   
					+$scope.plateUtils.templates.buttonCopyPosition()
					+'<div class="btn-group" style="margin-left:5px">'
						+'<button class="btn btn-default" ng-click="copyVolumeInToOut()" data-toggle="tooltip" title="'+Messages("experiments.button.plate.copyVolumeDnaIlluminaBq")+'"  ng-disabled="!isEditMode()"><i class="fa fa-files-o" aria-hidden="true"></i> '+Messages("experiments.button.plate.copyVolumeDnaIlluminaBq.title")+'</button>'                	                	
					+'</div>'
					+'<div class="btn-group" style="margin-left:5px">'
						+'<button class="btn btn-default" ng-click="copyQuantityInToOut()" data-toggle="tooltip" title="'+Messages("experiments.button.plate.copyQuantity.title")+'"  ng-disabled="!isEditMode()"><i class="fa fa-files-o" aria-hidden="true"></i> '+Messages("experiments.button.plate.copyQuantity")+'</button>'                	                	
					+'</div>'
			}
	};

	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event save");
		$scope.atmService.data.save();
		$scope.atmService.viewToExperimentOneToOne($scope.experiment);
		$scope.$emit('childSaved', callbackFunction);
	});
	
	$scope.$on('refresh', function(e) {
		console.log("call event refresh");		
		var dtConfig = $scope.atmService.data.getConfig();
		dtConfig.edit.active = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));
		dtConfig.edit.showButton = ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F'));
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
	
	$scope.copyVolumeInToOut = function(){
		var data = $scope.atmService.data.displayResult;

		data.forEach(function(value){
			if (value.data.inputContainerUsed.volume && value.data.inputContainerUsed.volume.value !== null) { 
				if (!value.data.inputContainerUsed.experimentProperties) {
					value.data.inputContainerUsed.experimentProperties = {
						inputVolume: {}
					};
				}

				value.data.inputContainerUsed.experimentProperties.inputVolume.value = value.data.inputContainerUsed.volume.value;
			}
		});
	};

	$scope.copyQuantityInToOut = function() {
		var data = $scope.atmService.data.displayResult;

		data.forEach(function(value) {
			if (value.data.inputContainerUsed.quantity && value.data.inputContainerUsed.quantity.value !== null) { 
				if (!value.data.inputContainerUsed.experimentProperties) {
					value.data.inputContainerUsed.experimentProperties = {
						libraryInputQuantity: {}
					};
				}

				value.data.inputContainerUsed.experimentProperties.libraryInputQuantity.value = value.data.inputContainerUsed.quantity.value;
			}
		});
	};
	
	$scope.updatePropertyFromUDT = function(value, col){
		console.log("update from property : "+col.property);
		
		if(col.property === 'outputContainerUsed.experimentProperties.tag.value'){
			// NGL-3667 computeTagCategory dans tagService 
			tagService.computeTagCategory(value.data);
		}
	
		if(col.property === 'inputContainerUsed.experimentProperties.inputVolume.value' ){
			computelibraryInputQuantity(value.data);
		}
	}
	
	var computelibraryInputQuantity = function(udtData){
		var getter = $parse("inputContainerUsed.experimentProperties.libraryInputQuantity.value");
		var libraryInputQuantity = getter(udtData);
		
		var compute = {
				inputVolume : $parse("inputContainerUsed.experimentProperties.inputVolume.value")(udtData),
				conc : $parse("inputContainerUsed.concentration.value")(udtData),
				
				isReady:function(){
					return (this.inputVolume && this.conc);
				}
			};
		if(compute.isReady()){
			var result = compute.inputVolume * compute.conc;
			console.log("result = "+result);
			if(result){
				libraryInputQuantity = Math.round(result*10)/10;
			}else{
				libraryInputQuantity = undefined;
			}	
			getter.assign(udtData, libraryInputQuantity);
		}else {
			console.log("not ready");
			getter.assign(udtData, undefined);
		}
		
	}
	
	/**
	 * More Generic than populateIndex12LinePlate
	 */
	var populateIndexLinePlate = function(prefix, startIndex, endIndex){
		var currentIndex = startIndex;
		
		var values={};
		var lines = ["A","B","C","D","E","F","G","H"];
		
		for(var j=0; j < lines.length; j++){
			var line = lines[j];
			for(var i = 1 ; i <= 12; i++){
				var pos = currentIndex+i-1;
				var computePrefix = null;
				if(pos < 10){
					computePrefix = prefix+"00";
				}else if(pos < 100){
					computePrefix = prefix+"0";
				}else {
					computePrefix = prefix;
				}
				values[line+i]=computePrefix+pos;;
			}
			currentIndex = currentIndex+12;
		}
		return values
	}
	
	/**
	 * NGL-3496 : populate plate with index replace mapping
	 */
	var populateIndexLinePlateWithMapReplace = function(prefix, startIndex, endIndex, map){
		var currentIndex = startIndex;
		
		var values={};
		var lines = ["A","B","C","D","E","F","G","H"];
		
		for(var j=0; j < lines.length; j++){
			var line = lines[j];
			for(var i = 1 ; i <= 12; i++){
				var pos = currentIndex+i-1;
				var computePrefix = null;
				if(pos < 10){
					computePrefix = prefix+"00";
				}else if(pos < 100){
					computePrefix = prefix+"0";
				}else {
					computePrefix = prefix;
				}
				var valueIndex = computePrefix+pos;
				if(map.has(valueIndex)){
					values[line+i]=map.get(valueIndex);
				}else{
					values[line+i]=valueIndex;
				}
			}
			currentIndex = currentIndex+12;
		}
		return values
	}
	
	var populateIndexColumnPlate = function(prefix, startIndex, endIndex){
		var currentIndex = startIndex;
		
		var values={};
		var lines = ["A","B","C","D","E","F","G","H"];
		
		for(var i = 1 ; i <= 12; i++){
			for(var j=0; j < lines.length; j++){
				var line = lines[j];
				
				var computePrefix = null;
				if(currentIndex < 10){
					computePrefix = prefix+"00";
				}else if(currentIndex < 100){
					computePrefix = prefix+"0";
				}else {
					computePrefix = prefix;
				}
				values[line+i]=computePrefix+currentIndex;
				currentIndex++;
			}
			
		}
		return values
	}
	
	var populateIndex6ColumnPlate = function(startIndex, endIndex){
		var currentIndex = startIndex;
		
		var values={};
		var lines = ["A","B","C","D","E","F","G","H"];
		for(var i = 1 ; i <= 12; i++){
			
			for(var j=0; j < lines.length; j++){
				var line = lines[j];
				var indexName = "IND"+currentIndex;
				if(currentIndex === 41)indexName=indexName+"b";
				values[line+i]=indexName;
				
				if(currentIndex === endIndex){
					currentIndex = startIndex;
				}else{
					currentIndex++;
				}
			}			
		}
		return values
	}
	
	var populateEPGVDualIndexColumnPlate = function(prefix, startIndex, endIndex){
		var currentIndex = startIndex;
		
		var values={};
		var lines = ["A","B","C","D","E","F","G","H"];
		
		for(var i = 1 ; i <= 12; i++){
			for(var j=0; j < lines.length; j++){
				var line = lines[j];
				
				var computePrefix = null;
				if(currentIndex < 10){
					computePrefix = prefix+"0";
				}else {
					computePrefix = prefix;
				}
				values[line+i]=computePrefix+currentIndex+"_i7-"+computePrefix+currentIndex+"_i5"; //ex "udi0001_i7-udi0001_i5"
				if(currentIndex === endIndex){
					currentIndex = startIndex;
				}else{
					currentIndex++;
				}
			}
		}
		return values
	}
	
	$scope.indexPlates = [];
	//12BA001-12BA096 ; 12BA097-12BA192 ; 12BA193-12BA288 ; 12BA289-12BA384
	
	$scope.indexPlates.push({label:"12BA001-12BA096", value:populateIndexLinePlate("12BA", 1, 96)});
	$scope.indexPlates.push({label:"12BA097-12BA192", value:populateIndexLinePlate("12BA", 97, 192)});
	$scope.indexPlates.push({label:"12BA193-12BA288", value:populateIndexLinePlate("12BA", 193, 288)});
	$scope.indexPlates.push({label:"12BA289-12BA384", value:populateIndexLinePlate("12BA", 289, 384)});
	$scope.indexPlates.push({label:"IND1-IND48", value:populateIndex6ColumnPlate(1, 48)});
	
	//NGL-3496 
	//$scope.indexPlates.push({label:"UDI001-UDI096", value:populateIndexLinePlate("UDI", 1, 96)});
	var mapIndex = new Map();
	mapIndex.set('UDI056','UDI056B');
	$scope.indexPlates.push({label:"UDI001-UDI096 (avec UDI056B)", value:populateIndexLinePlateWithMapReplace("UDI", 1, 96,mapIndex)});
	$scope.indexPlates.push({label:"UDI097-UDI192", value:populateIndexLinePlate("UDI", 97, 192)});
	$scope.indexPlates.push({label:"UDI193-UDI288", value:populateIndexLinePlate("UDI", 193, 288)});
	$scope.indexPlates.push({label:"UDI289-UDI384", value:populateIndexLinePlate("UDI", 289, 384)});
	
	$scope.indexPlates.push({label:"FLD0001-FLD0096 (EPGV)", value:populateIndexColumnPlate("fld0", 1, 96)});
	$scope.indexPlates.push({label:"FLD0097-FLD0192 (EPGV)", value:populateIndexColumnPlate("fld0", 97, 192)});
	$scope.indexPlates.push({label:"FLD0193-FLD0288 (EPGV)", value:populateIndexColumnPlate("fld0", 193, 288)});
	$scope.indexPlates.push({label:"FLD0289-FLD0384 (EPGV)", value:populateIndexColumnPlate("fld0", 289, 384)});
	$scope.indexPlates.push({label:"FLD0289-FLD0384 (EPGV)", value:populateIndexColumnPlate("fld0", 289, 384)});
	
	$scope.indexPlates.push({label:"IDT-ILMN TruSeq DNA UD Indexes (24 Indexes) (EPGV)", value:populateEPGVDualIndexColumnPlate("udi00", 1, 24)});
	
	$scope.updatePlateWithIndex = function(selectedPlateIndex){
		console.log("choose : "+selectedPlateIndex);
		var getter = $parse("experimentProperties.tag.value");
		var wells = atmService.data.displayResult;
		angular.forEach(wells, function(well){
			var outputContainerUsed = well.data.outputContainerUsed;;
			var pos = outputContainerUsed.locationOnContainerSupport.line+outputContainerUsed.locationOnContainerSupport.column;
			if(selectedPlateIndex){
				var index = selectedPlateIndex[pos];
				if(index){
					getter.assign(outputContainerUsed,index);
				}else{
					getter.assign(outputContainerUsed,null);
				}
			}else{
				getter.assign(outputContainerUsed,null);
			}
			 // NGL-3667 computeTagCategory maintenant dans tagService
			tagService.computeTagCategory(well.data);
		})	
	};
	
	//Init		
	if($scope.experiment.instrument.inContainerSupportCategoryCode!=="tube"){
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
		datatableConfig.columns.push({
			"header" : Messages("containers.table.code"),
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
		datatableConfig.order.by = 'inputContainer.sampleCodes';
	}
	
	if($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube") {
		datatableConfig.columns.push({
			// barcode plaque sortie == support Container used code... faut Used
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
			"edit" : false,
			"order" : true,
			"hide" : true,
			"type" : "text",
			"position" : 401,
			"extraHeaders" : {
				0 : Messages("experiments.outputs")
			}
		});
		datatableConfig.columns.push({// colonne
			"header" : Messages("containers.table.support.column"),
			// astuce GA: pour pouvoir trier les colonnes dans l'ordre naturel
			// forcer a numerique.=> type:number, property: *1
			"property" : "outputContainerUsed.locationOnContainerSupport.column",
			"edit" : false,
			"order" : true,
			"hide" : true,
			"type" : "number",
			"position" : 402,
			"extraHeaders" : {
				0 : Messages("experiments.outputs")
			}
		});

	} else {
		datatableConfig.columns.push({
			"header" : Messages("containers.table.code"),
			"property" : "outputContainerUsed.code",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "text",
			"position" : 400,
			"extraHeaders" : {
				0 : Messages("experiments.outputs")
			}
		});
	}

	var atmService = atmToSingleDatatable($scope, datatableConfig);
	//defined new atomictransfertMethod
	atmService.newAtomicTransfertMethod = function(line, column){
		var getLine = function(line){
			if($scope.experiment.instrument.outContainerSupportCategoryCode === 'tube'){
				return "1";
			}else{
				return line;
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

	atmService.convertOutputPropertiesToDatatableColumn = function(property, pName){
		var column = atmService.$commonATM.convertTypePropertyToDatatableColumn(property,"outputContainerUsed."+pName+".",{"0":Messages("experiments.outputs")});
		if(property.code==="tag"){
			// NGL-3667 16/02/2022 pour aide à la saisie des tags... essai ajout 'lists' dans le cas <input class   OUI!!!
			column.editTemplate='<input class="form-control" type="text" #ng-model typeahead="tag.code as tag.name for tag in lists.getTags() | filter:{groupNames:selectedTagGroup.value} | filter:{name:$viewValue} | limitTo:20" typeahead-min-length="1" udt-change="updatePropertyFromUDT(value,col)"/>';
		}
		return column;
	};
	
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	if($scope.experiment.instrument.inContainerSupportCategoryCode === $scope.experiment.instrument.outContainerSupportCategoryCode){
		$scope.messages.clear();
		$scope.atmService = atmService;
	}else{
		$scope.messages.setError(Messages('experiments.input.error.must-be-same-out'));					
	}
	
	$http.get(jsRoutes.controllers.commons.api.Parameters.list().url,{params:{typeCode:"index-illumina-sequencing"}})
	.success(function(data, status, headers, config) {
			$scope.tags = data;		
	})
	
	// NGL-3667 16/02/2022 aide à la saisie des tags
	// l'appel a tagService.initTags() est obligatoire pour l'assignation automatique de tagCategory
	if ( $scope.isNewState() || $scope.isInProgressState() || Permissions.check("admin") ){
		// si pas de parametre passé a initTag => tous les types, sinon préciser !!! 'index-illumina-sequencing' ou 'index-nanopore-sequencing'
		tagService.initTags("index-illumina-sequencing");
		$scope.getTags= function(){ return tagService.getAllTags()};
		
		$scope.getTagGroups= function(){return tagService.getAllTagGroups()};
		$scope.selectedTagGroup= $scope.getTagGroups()[0]; // valeur defaut du select
	}
	
	$scope.selectGroup = {
			isShow:function(){
				return ( $scope.isNewState() || $scope.isInProgressState() || Permissions.check("admin") );
			},
			select:function(groupName){       	
				console.log( 'groupe choisi :'+  groupName.value );
				//GA: creer une variable $scope.tags au lieu de d'ecraser la fonction getTags
				if (groupName.value === undefined ){ 
					$scope.lists.refresh.tags({typeCodes:'index-illumina-sequencing'});
				} else { 
					$scope.lists.refresh.tags({typeCodes:'index-illumina-sequencing',groupNames:[groupName.value]});
				}
			}
	};

}]);