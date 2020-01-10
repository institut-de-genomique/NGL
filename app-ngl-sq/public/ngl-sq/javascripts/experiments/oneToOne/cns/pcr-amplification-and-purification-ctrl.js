angular.module('home').controller('PCRAmplificationAndPurificationCtrl',['$scope', '$parse', '$http', '$filter', 'atmToSingleDatatable',
                                                    function($scope, $parse, $http, $filter, atmToSingleDatatable){
                                                    
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
				     // render avec "|" !!
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
			        	 "header":Messages("containers.table.volume")+ " (µL)",
			        	 "property":"outputContainerUsed.volume.value",
			        	 "watch":true,
			        	 "order":true,
						 "edit":false,
						 "hide":true,
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
				columnMode:true  /* ??*/
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
                	+$scope.plateUtils.templates.buttonCopyPosition() 
           	
			}
	};
	var updateATM = function(experiment){
		if(experiment.instrument.outContainerSupportCategoryCode!=="tube"){
			experiment.atomicTransfertMethods.forEach(function(atm){
				atm.line = atm.outputContainerUseds[0].locationOnContainerSupport.line;
				atm.column = atm.outputContainerUseds[0].locationOnContainerSupport.column;
			});
		}		
	};
	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event save");
		$scope.atmService.data.save();
		$scope.atmService.viewToExperimentOneToOne($scope.experiment);
		//computeOutputContainerVolumePerPCR ($scope.experiment);
		updateATM($scope.experiment);
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
	
	if($scope.experiment.typeCode==="pcr-amplification-and-purification"){
		datatableConfig.columns.push(// restait un render avec "|" ???
		         {
		        	"header":Messages("containers.table.tags"),
		 			"property": "inputContainer.contents",
		 			"filter": "getArray:'properties.tag.value'",
		 			"order":true,
		 			"hide":true,
		 			"type":"text",
		 			"position":4.5,
		 			"render":"<div list-resize='cellValue | unique' ' list-resize-min-size='3'>",
		        	 "extraHeaders":{0:Messages("experiments.inputs")}
			     }
		);
	}
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
			"choiceInList":true,
 			"possibleValues":[{"name":'A',"code":"A"},{"name":'B',"code":"B"},{"name":'C',"code":"C"},{"name":'D',"code":"D"},
 			                  {"name":'E',"code":"E"},{"name":'F',"code":"F"},{"name":'G',"code":"G"},{"name":'H',"code":"H"}],
			"edit" : true,
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
			"choiceInList":true,
			"possibleValues":[{"name":'1',"code":"1"},{"name":'2',"code":"2"},{"name":'3',"code":"3"},{"name":'4',"code":"4"},
 			                  {"name":'5',"code":"5"},{"name":'6',"code":"6"},{"name":'7',"code":"7"},{"name":'8',"code":"8"},
 			                  {"name":'9',"code":"9"},{"name":'10',"code":"10"},{"name":'11',"code":"11"},{"name":'12',"code":"12"}], 
 		
			"edit" : true,
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

	$scope.updatePropertyFromUDT = function(value, col){
		console.log("update from property : "+col.property);
		
		if(col.property === 'inputContainerUsed.experimentProperties.inputVolume.value'){
			computeInputQuantity(value.data);
		} else if( (col.property === 'inputContainerUsed.experimentProperties.nbPCR.value') || (col.property === 'outputContainerUsed.experimentProperties.PCRvolume.value')){
	            computeOutputContainerVolumePerPCR(value.data);
	    } else if(col.property === 'outputContainerUsed.experimentProperties.tag.value'){
			computeTagCategory(value.data);			
		}    
	}
	
	
	
  var computeOutputContainerVolumePerPCR  = function(udtData){
	     var getter = $parse("outputContainerUsed.volume.value");
         var volume = getter(udtData);
   
        var compute = {
                pcrvolume : $parse("outputContainerUsed.experimentProperties.PCRvolume.value")(udtData),
                nbPCR : $parse("inputContainerUsed.experimentProperties.nbPCR.value")(udtData),
                isReady:function(){
                    return (this.pcrvolume && this.nbPCR);
                }
            };
           
           if(compute.isReady()){
               var result = $parse("(pcrvolume * nbPCR)")(compute);
               console.log("result = "+result);
              
               if(angular.isNumber(result) && !isNaN(result)){
            	   volume = Math.round(result*10)/10;               
               }else{
            	   volume = undefined;
               }   
               getter.assign(udtData, volume);
               console.log("vol = "+volume);
	              
           }else{
               getter.assign(udtData, undefined);
               console.log("not ready to volume");
           }
    
  }
	
	
	var computeInputQuantity = function(udtData){
		var getter = $parse("inputContainerUsed.experimentProperties.inputQuantity.value");
		var inputQuantity = getter(udtData);
		
		var compute = {
				inputVol1 : $parse("inputContainerUsed.experimentProperties.inputVolume.value")(udtData),
				inputConc1 : $parse("inputContainerUsed.concentration.value")(udtData),
				isReady:function(){
					return (this.inputVol1 && this.inputConc1);
				}
			};
		
		if(compute.isReady()){
			var result = $parse("(inputVol1 * inputConc1)")(compute);
			console.log("result = "+result);
			if(angular.isNumber(result) && !isNaN(result)){
				inputQuantity = Math.round(result*10)/10;				
			}else{
				inputQuantity = undefined;
			}	
			getter.assign(udtData, inputQuantity);
		}else{
			getter.assign(udtData, undefined);
			console.log("not ready to inputQuantity");
		}
		
	}
	
	var atmService = atmToSingleDatatable($scope, datatableConfig);
	//defined new atomictransfertMethod
/*	atmService.newAtomicTransfertMethod = function(line, column){
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
	};*/
	
	
	atmService.newAtomicTransfertMethod =  function(line, column){
		var getLine = function(line){
			if($scope.experiment.instrument.outContainerSupportCategoryCode 
					=== $scope.experiment.instrument.inContainerSupportCategoryCode){
				return line;
			}else if($scope.experiment.instrument.outContainerSupportCategoryCode !== "tube" 
				&& $scope.experiment.instrument.inContainerSupportCategoryCode === "tube") {
				return undefined;
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
			volume : "µL"
	}
	
	atmService.convertOutputPropertiesToDatatableColumn = function(property, pName){
		var column = atmService.$commonATM.convertTypePropertyToDatatableColumn(property,"outputContainerUsed."+pName+".",{"0":Messages("experiments.outputs")});
		if(property.code=="tag"){
			column.editTemplate='<input class="form-control" type="text" #ng-model typeahead="v.code as v.code for v in tags | filter:{code:$viewValue} | limitTo:20" typeahead-min-length=1 udt-change="updatePropertyFromUDT(value,col)"/>';        											
		}
		return column;
	};
	
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	$scope.atmService = atmService;
/*	if($scope.experiment.instrument.inContainerSupportCategoryCode === $scope.experiment.instrument.outContainerSupportCategoryCode){
		$scope.messages.clear();
		$scope.atmService = atmService;
	}else{
		$scope.messages.setError(Messages('experiments.input.error.must-be-same-out'));					
	}*/
	
	$http.get(jsRoutes.controllers.commons.api.Parameters.list().url,{params:{typeCode:"index-illumina-sequencing"}})
		.success(function(data, status, headers, config) {
			$scope.tags = data;		
		});
	
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
}]);