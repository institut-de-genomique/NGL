angular.module('home').controller('NanoporeBarcodeLigationCtrl',['$scope', '$parse', 'atmToSingleDatatable',
                                                    function($scope, $parse, atmToSingleDatatable){
	
	
	var datatableConfig = {

			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[			  
					 {
			        	 "header":Messages("containers.table.code"),
			        	 "property":"inputContainer.support.code",
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
			        	 "filter": "unique| codes: 'type'",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	 "position":4,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
				     },
				     {
				        	"header":Messages("containers.table.tags"),
				 			"property": "inputContainer.contents",
				 			"filter": "getArray:'properties.tag.value'| unique",
				 			"order":true,
				 			"hide":true,
				 			"type":"text",
				 			"position":4.5,
				 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
				        	 "extraHeaders":{0:Messages("experiments.inputs")}
				         },		 
					 {
			        	 "header":Messages("containers.table.concentration") + " (ng/µl)",
			        	 "property":"inputContainerUsed.concentration.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":5,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			         {
			        	 "header":function(){return Messages("containers.table.volume") + " (µl)"},
			        	 "property":"inputContainerUsed.volume.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":6,
			        	 "editDirectives":' udt-change="updatePropertyFromUDT(value,col)" ',
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
			        	 "header":Messages("containers.table.concentration") + " (ng/µl)",
			        	 "property":"outputContainerUsed.concentration.value",
			        	 "order":true,
						 "edit":true,
						 "hide":true,
						 "required":false,
			        	 "type":"number",
			        	 "position":50.2,
			        	 "editDirectives":' udt-change="updatePropertyFromUDT(value,col)" ',
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },
			         {
			        	 "header":Messages("containers.table.quantity")+ " (ng)",
			        	 "property":"outputContainerUsed.quantity.value",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"number",
			        	 "position":52,
			        	 "watch":true,
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         }, 
			         {
			        	 "header":Messages("containers.table.volume")+ " (µl)",
			        	 "property":"outputContainerUsed.volume.value",
			        	 "order":true,
						 "edit":true,
						 "hide":true,
			        	 "type":"number",
			        	 "position":51,
			        	 "editDirectives":' udt-change="updatePropertyFromUDT(value,col)" ',
			        	 "extraHeaders":{0:Messages("experiments.outputs")}
			         },
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
	
	
	$scope.updatePropertyFromUDT = function(value, col){
		console.log("update from property : "+col.property);
					
		if (col.property === 'outputContainerUsed.volume.value' || col.property === 'outputContainerUsed.concentration.value'  ){
			computeOutputQuantityToContentProperties(value.data);			
		}else if (col.property === 'inputContainerUsed.experimentProperties.inputVolume.value'  ){
			computeInputQuantityToContentProperties(value.data);			
		}
	}
	
	
	  var computeOutputQuantityToContentProperties  = function(udtData){
		     var getter = $parse("outputContainerUsed.quantity.value");
	         var outputQtty = getter(udtData);
	         console.log("computeOutputQuantityToContentProperties");
	        var compute = {
	                outputvolume : $parse("outputContainerUsed.volume.value")(udtData),
	                concentration : $parse("outputContainerUsed.concentration.value")(udtData),
	                isReady:function(){
	                    return (this.outputvolume && this.concentration);
	                }
	            };
	           
	           if(compute.isReady()){
	               var result = $parse("(outputvolume * concentration)")(compute);
	               console.log("result = "+result);
	              
	               if(angular.isNumber(result) && !isNaN(result)){
	            	   outputQtty = Math.round(result*10)/10;           
	               }else{
	            	   outputQtty = undefined;
	               }   
	               getter.assign(udtData, outputQtty);
	              
	           }else{
	               getter.assign(udtData, undefined);
	               console.log("not ready to outputQtty");
	           }
	  }
		
	  var computeInputQuantityToContentProperties  = function(udtData){
		     var getter = $parse("inputContainerUsed.experimentProperties.inputQuantity.value");
	         var inputQtty = getter(udtData);
	         console.log("computeInputQuantityToContentProperties");
	        var compute = {
	                inputvolume : $parse("inputContainerUsed.experimentProperties.inputVolume.value")(udtData),
	                concentration : $parse("inputContainerUsed.concentration.value")(udtData),
	                isReady:function(){
	                    return (this.inputvolume && this.concentration);
	                }
	            };
	           
	           if(compute.isReady()){
	               var result = $parse("(inputvolume * concentration)")(compute);
	               console.log("result = "+result);
	              
	               if(angular.isNumber(result) && !isNaN(result)){
	            	   inputQtty = Math.round(result*10)/10;           
	               }else{
	            	   inputQtty = undefined;
	               }   
	               getter.assign(udtData, inputQtty);
	              
	           }else{
	               getter.assign(udtData, undefined);
	               console.log("not ready to InputQtty");
	           }
	  }
	  
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
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	$scope.atmService = atmService;
}]);