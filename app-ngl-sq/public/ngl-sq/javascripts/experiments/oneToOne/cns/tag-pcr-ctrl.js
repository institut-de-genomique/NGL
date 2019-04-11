angular.module('home').controller('TagPCRCtrl',['$scope', '$parse','$filter', 'atmToSingleDatatable','lists','mainService','$http',
                                                    function($scope, $parse, $filter, atmToSingleDatatable,lists,mainService,$http){
                                                    
	var datatableConfig = {
					name: $scope.experiment.typeCode.toUpperCase(),
					columns:[  
					         /*
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
					         */         
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
					         /*{
					        	 "header":Messages("containers.table.quantity") + " (ng)",
					        	 "property":"inputContainerUsed.quantity.value",
					        	 "order":true,
								 "edit":false,
								 "hide":true,
					        	 "type":"number",
					        	 "position":7,
					        	 "extraHeaders":{0:Messages("experiments.inputs")}
					         },*/
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
					         /*
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
					         */
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
		var dtConfig = $scope.atmService.data.getConfig();

		$scope.atmService.data.save();
		$scope.atmService.viewToExperimentOneToOne($scope.experiment);

		if (! checkTagPcrBlankSampleCode($scope)){
			$scope.$emit('childSavedError', callbackFunction);
		}else{
			$scope.$emit('childSaved', callbackFunction);
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
	//	$scope.refreshExtractionBlankSampleTagCodeLists();
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
	
	
	var checkTagPcrBlankSampleCode = function($scope){
		var experiment=$scope.experiment;
		var blank1;
		var blank2;
		var sampleCodeAvailable;

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
		if(isNested){
			//search only where input is on CEB project
			atmWithBlanckSamples = $filter('filter')(atmWithBlanckSamples,{inputContainerUseds:{contents:{projectCode:'CEB'}}});
			if(experiment.protocolCode==="metab-primer-fusion-dev"){
				$parse("experimentProperties.targetedRegion.value").assign(experiment,'16S_Full Length + 16S_V4V5');
			}else{
				$parse("protocolCode").assign(experiment,"tag16s_full_length_16s_v4v5_fuhrman");
				$parse("experimentProperties.amplificationPrimers.value").assign(experiment,'16S FL 27F/1492R + Fuhrman primers');
				$parse("experimentProperties.targetedRegion.value").assign(experiment,'16S_Full Length + 16S_V4V5');
			}
			
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
		
		
		/*
		for(var i=0 ; i < experiment.atomicTransfertMethods.length && experiment.atomicTransfertMethods != null; i++){
			var atm = experiment.atomicTransfertMethods[i];
			for(var j=0 ; j < atm.outputContainerUseds.length ; j++){		
				var ocu = atm.outputContainerUseds[j];
				
				if(ocu.experimentProperties && ocu.experimentProperties.projectCode   && ocu.experimentProperties.projectCode.value==="CEB"){				
					if(ocu.experimentProperties.sampleCode){
						sampleCodeAvailable=true;
						var value = ocu.experimentProperties.sampleCode.value;

						if (! blank1){
							blank1= value;					
						}else if (! blank2){
							blank2= value;			
						}else {
							$scope.messages.setError(Messages('Attention vous avez renseigné plus de 2 témoins négatifs pour cette expérience'));	
							return false;
						}
					}
				}
			}
		}	

		if (! blank2 && sampleCodeAvailable){
			$scope.messages.setError(Messages('Attention vous devez renseigner les 2 témoins négatifs pour cette expérience'));	
			return false;
		}else if (blank2 && sampleCodeAvailable){

			for(var i=0 ; i < experiment.atomicTransfertMethods.length && experiment.atomicTransfertMethods != null; i++){
				var atm2 = experiment.atomicTransfertMethods[i];

				for(var j=0 ; j < atm2.outputContainerUseds.length ; j++){		
					var ocu = atm2.outputContainerUseds[j];
					var getter = $parse("experimentProperties.tagPcrBlank1SampleCode.value");
					var getter2 = $parse("experimentProperties.tagPcrBlank2SampleCode.value");

					if (blank1){
						getter.assign(atm2.outputContainerUseds[j],blank1);	
					}

					if(blank2){
						getter2.assign(atm2.outputContainerUseds[j],blank2);	
					}	
				}
			}
		}
		
		return true;
		*/
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
				console.log("result = "+result);
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
	 
	 $http.get(jsRoutes.controllers.commons.api.Parameters.list().url,{params:{typeCode:"index-illumina-sequencing",categoryCode:"MID"}})
		.success(function(data, status, headers, config) {
				$scope.tags = data;		
		})
	
	/*
	 * Supprime la poss de remplir le champs manuellement
	 * $scope.refreshExtractionBlankSampleTagCodeLists=function(){
		$scope.lists.clear('sampleTag');
		$scope.lists.refresh.samples({"projectCodes":"CEB"}, 'sampleTag'); 
			};
	
	
	 * $scope.sample = {
			tagPcrBlank1SampleCode:null,
			tagPcrBlank2SampleCode:null
	};
	
	$scope.refreshExtractionBlankSampleTagCodeLists();*/
	
}]);