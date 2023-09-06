angular.module('home').controller('TagPCRCtrl',['$scope', '$parse','$filter', 'atmToSingleDatatable','lists','mainService','$http',
                                                    function($scope, $parse, $filter, atmToSingleDatatable,lists,mainService,$http){
                                                    
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
							"header":Messages("containers.table.volume")+" (µL)",
							"property":"outputContainerUsed.volume.value",
							"order":true,
							"edit":true,
							"hide":true,
							"required":"isRequired('IP')",
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
						mode:'local',
						showButton:false,
						changeClass:false
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
						number:1,
						dynamic:true,
					}

			};	
	
	
	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event save");

		$scope.atmService.data.save();
		$scope.atmService.viewToExperimentOneToOne($scope.experiment);

		if (! checkTagPcrBlankSampleCode($scope)){
			$scope.$emit('childSavedError', callbackFunction);
		}else{
			$scope.$emit('childSaved', callbackFunction);
		}

		checkMissingExtractionNegatveControls();
	});

	$scope.$on('finishExperiment', function() {
		resetNegativeControlsWarningMessage();

		//---

		function resetNegativeControlsWarningMessage() {
			$scope.missingExtractionNegativeControls = null;
		}
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
	
	var checkMissingExtractionNegatveControls = function() {

		resetWarningMessage();
		var inputControlsSet = new Set();
		var extractionNegativeControlSet = new Set();	
		getInputContainers().forEach(registerInformations);
		warnAboutMissingNegativeControls();

		//---

		function resetWarningMessage() {
			$scope.missingExtractionNegativeControls = null;
		}

		function getInputContainers(){
			return $scope.experiment.atomicTransfertMethods.flatMap(function (atm) { return atm.inputContainerUseds; });
		}

		function registerInformations(inputContainer) {
			if(isInputNegativeControl()) {
				inputContainer.sampleCodes.forEach(registerInputControl);
			} else if(isInputNestedNegativeControl()) {
				inputContainer.contents.filter(containsNegativeControl).forEach(registerNestedInputControl);
			} else {
				inputContainer.contents.filter(containsNegativeControl).forEach(registerNegativeControl);
			}

			//---

			function registerInputControl(sampleCode) {
				inputControlsSet.add(sampleCode);
			}

			function registerNestedInputControl(content) {
				inputControlsSet.add(content.properties.extractionBlankSampleCode.value);
			}

			function isInputNegativeControl() {
				return inputContainer.projectCodes.some(isExtractionNegativeControlProject);
			}

			function isExtractionNegativeControlProject(projectCode) {
				return projectCode === "CDW" || projectCode === "CDY";
			}

			function isInputNestedNegativeControl() {
				return inputContainer.projectCodes.some(isNestedExtractionNegativeControlProject);
			}

			function isNestedExtractionNegativeControlProject(projectCode) {
				return projectCode === "CDX" || projectCode === "CDZ";
			}

			function containsNegativeControl(content) {
				return content.properties.extractionBlankSampleCode;
			}

			function registerNegativeControl(content){
				extractionNegativeControlSet.add(content.properties.extractionBlankSampleCode.value);
			}
		}

		function warnAboutMissingNegativeControls() {
			var missingExtractionNegativeControls = getMissingExtractionNegativeControl();
			if (missingExtractionNegativeControls.length) setExtractionNegativeControlsWarningMessage();

			//---

			function getMissingExtractionNegativeControl() {
				return Array.from(extractionNegativeControlSet).filter(isNotInInputSamples);
			}

			function isNotInInputSamples(extractionNegativeControl) {
				return !inputControlsSet.has(extractionNegativeControl);
			}
	
			function setExtractionNegativeControlsWarningMessage() {
				$scope.missingExtractionNegativeControls = missingExtractionNegativeControls.join(", ");	
			}
		}
	};
	
	var checkTagPcrBlankSampleCode = function($scope){
		var experiment=$scope.experiment;

		var nestedDetectionBlank1  = $filter('filter')(experiment.atomicTransfertMethods,{inputContainerUseds:{contents:{properties:{tagPcrBlank1SampleCode:{value:'CEB_'}}}}});
		var nestedDetectionBlank2  = $filter('filter')(experiment.atomicTransfertMethods,{inputContainerUseds:{contents:{properties:{tagPcrBlank2SampleCode:{value:'CEB_'}}}}});
		var nestedNonBlanck =  $filter('filter')(experiment.atomicTransfertMethods,{inputContainerUseds:{contents:{properties:{tagPcrBlank2SampleCode:{value:'!CEA_'}}}}});
		nestedNonBlanck =  $filter('filter')(nestedNonBlanck,{inputContainerUseds:{contents:{properties:{tagPcrBlank2SampleCode:{value:'!CAM_'}}}}});
		
		var isNested = false;
		if(nestedDetectionBlank1.length > 0 && nestedDetectionBlank2.length > 0){
			isNested = true;
		}
				
		if(isNested && (nestedDetectionBlank1.length !== nestedDetectionBlank2.length 
				|| nestedDetectionBlank1.length !== nestedNonBlanck.length)){
			$scope.messages.setError(Messages("Attention problème avec tag-pcr nested, tous les inputs n'ont pas les 2 échantillons témoins"));	
			return false;
		}
		//search atm with output with CEB as new sampleCode
		var atmWithBlanckSamples = $filter('filter')(experiment.atomicTransfertMethods,{outputContainerUseds:{experimentProperties:{sampleCode:{value:'CEB_'}}}});
		
		//cas d'un processus nested
		if(isNested){
			//search only where input is on CEB project
			atmWithBlanckSamples = $filter('filter')(atmWithBlanckSamples,{inputContainerUseds:{contents:{projectCode:'CEB'}}});

			//nouvelles amorces => nouvelles conditions pour Nested NGL-3232
			//avec le proto "metab-primer-fusion-dev" la région ciblée est forcée et choix des amorces libre
			if(experiment.protocolCode==="metab-primer-fusion-dev"){
				$parse("experimentProperties.targetedRegion.value").assign(experiment,'16S_Full Length + 16S_V4V5');		

			} else if(experiment.protocolCode!="tag_its_fl_its_fun" && 
						experiment.protocolCode!="tag16s_full_length_16s_v3v4" && 
						experiment.protocolCode!="tag_18s_full_length_18s_v9"){
				
				//Si le proto est different de "Tag ITS FL + ITS FUN" 
				//!!! les codes sont stockés en base en minuscule!!!!!!!!
					$parse("protocolCode").assign(experiment,"tag16s_full_length_16s_v4v5_fuhrman");
					$parse("experimentProperties.amplificationPrimers.value").assign(experiment,'16S FL 27F/1492R + Fuhrman primers');
					$parse("experimentProperties.targetedRegion.value").assign(experiment,'16S_Full Length + 16S_V4V5');	
				}
			//Dans le cas où le protocole vaut "Tag ITS FL + ITS FUN" on ne force rien!
		}
		
		//check the good number of atm
		if(atmWithBlanckSamples.length === 1){
			$scope.messages.setError(Messages('Attention vous devez renseigner les 2 témoins négatifs pour cette expérience'));	
			return false;
		}else if(atmWithBlanckSamples.length > 2){
			$scope.messages.setError(Messages('Attention vous avez renseigné plus de 2 témoins négatifs pour cette expérience'));	
			return false;
		}else if(atmWithBlanckSamples.length === 2){
			var blanckSamples = [];
			blanckSamples[0] = atmWithBlanckSamples[0].outputContainerUseds[0].experimentProperties.sampleCode.value;
			blanckSamples[1] = atmWithBlanckSamples[1].outputContainerUseds[0].experimentProperties.sampleCode.value;
			blanckSamples = $filter('orderBy')(blanckSamples);
			
			getterBlanck1 = $parse("experimentProperties.tagPcrBlank1SampleCode.value");
			getterBlanck2 = $parse("experimentProperties.tagPcrBlank2SampleCode.value");
			
			for(var i = 0; i < experiment.atomicTransfertMethods.length; i++){
				getterBlanck1.assign(experiment.atomicTransfertMethods[i].outputContainerUseds[0], blanckSamples[0]);
				getterBlanck2.assign(experiment.atomicTransfertMethods[i].outputContainerUseds[0], blanckSamples[1]);
			}
		}
		return true;
		
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
		datatableConfig.columns.push(
		{
 			// Ligne
 			"header" : Messages("containers.table.support.line"),
 			"property" : "outputContainerUsed.locationOnContainerSupport.line",
 			"edit" : true,
 			"choiceInList":true,
 			"possibleValues":[{"name":'A',"code":"A"},{"name":'B',"code":"B"},{"name":'C',"code":"C"},{"name":'D',"code":"D"},
 			                  {"name":'E',"code":"E"},{"name":'F',"code":"F"},{"name":'G',"code":"G"},{"name":'H',"code":"H"}],
 			"order" : true,
 			"hide" : true,
 			"type" : "text",
 			"position" : 401,
 			"extraHeaders" : {
 				0 : Messages("experiments.outputs")
 			}
 		}
		
		);
		datatableConfig.columns.push({// colonne
 			"header" : Messages("containers.table.support.column"),
 			// astuce GA: pour pouvoir trier les colonnes dans l'ordre naturel
 			// forcer a numerique.=> type:number, property: *1
 			"property" : "outputContainerUsed.locationOnContainerSupport.column",
 			"edit" : true,
 			"choiceInList":true,
 			"possibleValues":[{"name":'1',"code":"1"},{"name":'2',"code":"2"},{"name":'3',"code":"3"},{"name":'4',"code":"4"},
 			                  {"name":'5',"code":"5"},{"name":'6',"code":"6"},{"name":'7',"code":"7"},{"name":'8',"code":"8"},
 			                  {"name":'9',"code":"9"},{"name":'10',"code":"10"},{"name":'11',"code":"11"},{"name":'12',"code":"12"}], 
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
		if(property.code=="projectCode"){
			column.editTemplate='<div class="form-control" bt-select #ng-model filter="true" placeholder="'+Messages("search.placeholder.projects")+'" bt-options="project.code as project.code+\' (\'+project.name+\')\' for project in lists.getProjects()" ></div>';
		}else if(property.code=="sampleTypeCode"){
			column.filter="getArray:'sampleTypeCode' | unique | codes:\"type\"";
		}else if(property.code=="secondaryTag"){
			column.editTemplate='<input class="form-control" type="text" #ng-model typeahead="v.code as v.code for v in tags | filter:{code:$viewValue} | limitTo:20" typeahead-min-length="1" udt-change="updatePropertyFromUDT(value,col)"/>';        											
		}
		return column;
	};
	
	
	atmService.updateNewAtmBeforePushInUDT=function(atm){
		var value = $scope.experimentType.sampleTypes[0].code;
		var setter = $parse("outputContainerUsed.experimentProperties.sampleTypeCode.value").assign;
		setter(atm, value);
	}
	
	atmService.experimentToView($scope.experiment, $scope.experimentType);

	if($scope.experiment.instrument.inContainerSupportCategoryCode === $scope.experiment.instrument.outContainerSupportCategoryCode){
		$scope.messages.clear();
		$scope.atmService = atmService;
	}else{
		$scope.messages.setError(Messages('experiments.input.error.must-be-same-out'));					
	}
	
	
	$scope.updatePropertyFromUDT = function(value, col){
		console.log("update from property : "+col.property);
					
		if (col.property === 'inputContainerUsed.experimentProperties.inputVolume.value'   ){
			computeInputQuantityToContentProperties(value.data);
			
		}
		
		if(col.property === 'outputContainerUsed.experimentProperties.secondaryTag.value'){
			computeTagCategory(value.data);	
			checkSecondaryTags($scope.atmService.data);		
		}
	}
	
	 var computeInputQuantityToContentProperties  = function(udtData){
	     var getter = $parse("inputContainerUsed.experimentProperties.inputQuantity.value");
         var inputQtty = getter(udtData);
      
        var compute = {
                inputVolume : $parse("inputContainerUsed.experimentProperties.inputVolume.value")(udtData),
                concentration : $parse("inputContainerUsed.concentration.value")(udtData),
                isReady:function(){
                    return (this.inputVolume && this.concentration);
                }
            };
           
           if(compute.isReady()){
               var result = $parse("(inputVolume * concentration)")(compute);
               console.log("result = "+result);
              
               if(angular.isNumber(result) && !isNaN(result)){
            	   inputQtty = Math.round(result*10)/10;               
               }else{
            	   inputQtty = undefined;
               }   
               getter.assign(udtData, inputQtty);
           }
  }
	 
	 $http.get(jsRoutes.controllers.commons.api.Parameters.list().url,{params:{typeCode:"index-illumina-sequencing",categoryCode:"MID"}})
		.success(function(data, status, headers, config) {
				$scope.tags = data;		
		})
	
		var computeTagCategory = function(udtData){
			var getter = $parse("outputContainerUsed.experimentProperties.secondaryTagCategory.value");
			var tagCategory = getter(udtData);
			
			var compute = {
					tagValue : $parse("outputContainerUsed.experimentProperties.secondaryTag.value")(udtData),
					tag : $filter("filter")($scope.tags,{code:$parse("outputContainerUsed.experimentProperties.secondaryTag.value")(udtData)},true),
					isReady:function(){
						return (this.tagValue && this.tag && this.tag.length === 1);
					}
				};
			if(compute.isReady()){
				var result = compute.tag[0].categoryCode;
				console.log("result secondaryTagCategory = "+result);
				if(result){
					tagCategory = result;				
				}else{
					tagCategory = undefined;
				}	
				getter.assign(udtData, tagCategory);
			}else{
				getter.assign(udtData, undefined);
			}
			
		}
		
		checkSecondaryTags = function(data){
			// check if output container category is well
			if(data.allResult
				.map(function (res) {
					return res.outputContainerUsed.categoryCode;
				})
				.every(function (code) { 
					return code === "well";
				})) {
					$scope.messages.clear();
					var secondaryTags = data.displayResult.map(function(result) {
						return result.data.outputContainerUsed.experimentProperties.secondaryTag;
					});
					// if some tags have been defined and some not, show a warning
					if(secondaryTags.some(function (tag) { return tag && tag.value; }) && secondaryTags.some(function(tag) { return !tag || !tag.value; })) {		
						$scope.messages.clazz = "alert alert-warning";
						$scope.messages.text = Messages('experiments.input.warn.homogene-secondary-tags');
						$scope.messages.showDetails = false;
						$scope.messages.open();
					} 
			} 
		};

		var populateSpecialIndexLinePlate = function(prefix, startIndex, endIndex, excludedIndex, maxCol){

			 var values={};
			 var lines = ["A","B","C","D","E","F","G","H"];
			 var pos=startIndex;
			 for(var i = 1 ; i <= maxCol; i++){
				 while(excludedIndex.includes(pos) && pos<=endIndex){
					 pos=pos+1;
				 }
					
				 for(var j=0; j < lines.length; j++){
					 var line = lines[j];
					 var computePrefix = null;
					 if(pos < 10){
						 computePrefix = prefix+"0";
					 }else if(pos < 100){
						 computePrefix = prefix;
					 }else {
						 computePrefix = prefix;
					 }
					 values[line+i]=computePrefix+pos;
				 }
				 pos=pos+1;
			 }
			 return values
		 }
		
		
		
		
		$scope.indexPlates = [];
		
		$scope.indexPlates.push({label:"Bid1 à Bid18", value:populateSpecialIndexLinePlate("BID", 1, 18, [3,6,7,10,12,14], 12)});
		$scope.indexPlates.push({label:"Bid18 à Bid33", value:populateSpecialIndexLinePlate("BID", 18, 33, [24,26,28,30], 12)});		
		$scope.indexPlates.push({label:"Bid19 à Bid42", value:populateSpecialIndexLinePlate("BID", 19, 42, [24,26,28,30,34,35,36,37,38,39,40,41], 12)});

		$scope.updatePlateWithIndex = function(selectedPlateIndex){
			console.log("choose : "+selectedPlateIndex);
			var getter = $parse("experimentProperties.secondaryTag.value");
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
				computeTagCategory(well.data);
										
			})	
		};
		
		$scope.isNested = function(){
			experiment = $scope.experiment;
			var nestedDetectionBlank1  = $filter('filter')($scope.experiment.atomicTransfertMethods,{inputContainerUseds:{contents:{properties:{tagPcrBlank1SampleCode:{value:'CEB_'}}}}});
			var nestedDetectionBlank2  = $filter('filter')($scope.experiment.atomicTransfertMethods,{inputContainerUseds:{contents:{properties:{tagPcrBlank2SampleCode:{value:'CEB_'}}}}});
			if(nestedDetectionBlank1.length > 0 && nestedDetectionBlank2.length > 0){
				return true;
			}else{
				return false;
			}
		};

		$scope.isMissingExtractionNegativeControlsWarning = function() {
			return $scope.isInProgressState() && $scope.missingExtractionNegativeControls;
		};

		checkMissingExtractionNegatveControls();
	
}]);