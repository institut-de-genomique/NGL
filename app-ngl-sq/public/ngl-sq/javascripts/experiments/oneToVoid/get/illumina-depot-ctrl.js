angular.module('home').controller('GETIlluminaDepotCtrl',['$scope', '$parse','$http','atmToSingleDatatable',
                                                             function($scope,$parse, $http, atmToSingleDatatable) {

	$scope.$on('updateInstrumentProperty', function(e, pName) {
		console.log("call event updateInstrumentProperty "+pName);
		
		if($scope.isCreationMode() && pName === 'sequencingProgramType'){
			if("SR" == $scope.experiment.instrumentProperties[pName].value){
			console.log("update sequencingProgramType "+$scope.experiment.instrumentProperties[pName].value);
			$scope.experiment.instrumentProperties["nbCyclesRead2"] = undefined;
			}
			
		}
		
	});
	
	 // NGL-1055: name explicite pour fichier CSV exporté: typeCode experience
	 // NGL-1055: mettre getArray et codes '' dans filter et pas dans render
	 var datatableConfig = {
			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[
			         {
			        	 "header":Messages("containers.table.supportCode"),
			        	 "property":"inputContainer.support.code",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":1,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			         {
			        	 "header":Messages("containers.table.categoryCode"),
			        	 "property":"inputContainer.support.categoryCode",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":2,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
					 {
			        	 "header":Messages("containers.table.code"),
			        	 "property":"inputContainer.code",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":3,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
					 {
			        	 "header":Messages("containers.table.projectCodes"),
			        	 "property":"inputContainer.projectCodes",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":4,
			        	 "render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			         {
			        	 "header":Messages("containers.table.nomPool"),
			        	 "property":"inputContainerUsed.contents[0].properties.Nom_pool_sequencage.value",
			        	 "order":true,
						 "edit":false,
						 "hide":false,
			        	 "type":"text",
			        	 "position":4.5,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
					 {
			        	 "header":Messages("containers.table.sampleCodes"),
			        	 "property":"inputContainer.sampleCodes",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":5,
			        	 "render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
					 {
			        	 "header":Messages("containers.table.tagCategory"),
			        	 "property":"inputContainer.contents",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":5.5,
			        	 "render":"<div list-resize='cellValue | getArray:\"properties.tagCategory.value\"| unique | unique' list-resize-min-size='3'>",
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
					 {
			        	 "header":Messages("containers.table.stateCode"),
			        	 "property":"inputContainer.state.code",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
						 "filter":"codes:'state'",
			        	 "position":6,
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
			        	 "position":7,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
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
				mode:'local', //or 
				active:true,
				by:'inputContainer.code'
			},
			remove:{
				active:false,
			},
			save:{
				active:true,
	        	changeClass:false,
	        	showButton:false,
	        	withoutEdit: true,
				mode:'local',
			},
			hide:{
				active:true
			},
//			edit:{
//				active: false
//			},
			edit:{
				active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
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
	 
		
	//add reagents into new experiment
		var addReagents = function() {
			
			 if($parse('experiment.state.code')($scope) === "N" && $scope.experiment.reagents.length === 0 && $parse('experiment.instrumentProperties.sequencingProgramType')($scope)){
				 var lectureType = $scope.experiment.instrumentProperties.sequencingProgramType.value;
				 var nbCyclesRead = $scope.experiment.instrumentProperties.nbCyclesRead1.value;

				 var SBSkit150cycles = [{
						       "kitCatalogCode": "24SC2W81W",
						       "boxCatalogCode": "24SC2YP1F",
						       "reagentCatalogCode": "24SC33OBN" 
						    },
						     {
						       "kitCatalogCode": "24SC2W81W",
						       "boxCatalogCode": "24SC2YP1F",
						       "reagentCatalogCode": "24SC33OBJ" 
						    },
						     {
						       "kitCatalogCode": "24SC2W81W",
						       "boxCatalogCode": "24SC30NML",
						       "reagentCatalogCode": "24SC36K7D" 
						    },
						     {
						       "kitCatalogCode": "24SC2W81W",
						       "boxCatalogCode": "24SC30NML",
						       "reagentCatalogCode": "24SC36K7F" 
						    },
						     {
						       "kitCatalogCode": "24SC2W81W",
						       "boxCatalogCode": "24SC30NML",
						       "reagentCatalogCode": "24SC36K7H" 
						    }];
				 var SBSkit300cycles = [{
							   "kitCatalogCode": "24SB4A78F",
							   "boxCatalogCode": "24SB4HGSM",
							   "reagentCatalogCode": "24SB4MYPJ" 
							},
							 {
							   "kitCatalogCode": "24SB4A78F",
							   "boxCatalogCode": "24SB4HGSM",
							   "reagentCatalogCode": "24SB4R666" 
							},
							 {
							   "kitCatalogCode": "24SB4A78F",
							   "boxCatalogCode": "24SB4YCSJ",
							   "reagentCatalogCode": "24SB4YD67" 
							},
							 {
							   "kitCatalogCode": "24SB4A78F",
							   "boxCatalogCode": "24SB4YCSJ",
							   "reagentCatalogCode": "24SB4YD69" 
							},
							 {
							   "kitCatalogCode": "24SB4A78F",
							   "boxCatalogCode": "24SB4YCSJ",
							   "reagentCatalogCode": "24SB4YD6L" 
								    }];
				
	//			 console.log("Type lectures : " + JSON.stringify($scope.experiment.instrumentProperties));
				 if (lectureType === "PE"){
					 var ReagentUseds = [{
								    "kitCatalogCode": "24SA5FUL0",
								    "boxCatalogCode": "24SB1MUL4",
								    "reagentCatalogCode": "24SB2QMRU" 
								 },
								  {
								    "kitCatalogCode": "24SA5FUL0",
								    "boxCatalogCode": "24SB1MUL4",
								    "reagentCatalogCode": "24SB2TROX" 
								 },
								  {
								    "kitCatalogCode": "24SA5FUL0",
								    "boxCatalogCode": "24SB1MUL4",
								    "reagentCatalogCode": "24SC20L4D" 
								 },
								  {
								    "kitCatalogCode": "24SA5FUL0",
								    "boxCatalogCode": "24SB1MUL4",
								    "reagentCatalogCode": "24SC20L4O" 
								 },
								  {
								    "kitCatalogCode": "24SA5FUL0",
								    "boxCatalogCode": "24SB1MUL4",
								    "reagentCatalogCode": "24SC20L4U" 
								 },
								  {
								    "kitCatalogCode": "24SA5FUL0",
								    "boxCatalogCode": "24SB1MUL4",
								    "reagentCatalogCode": "24SC20L54" 
								 },
								  {
								    "kitCatalogCode": "24SA5FUL0",
								    "boxCatalogCode": "24SB1MUL4",
								    "reagentCatalogCode": "24SC20L58" 
								 }
				                ];
					 if(nbCyclesRead === "151"){
						 ReagentUseds = ReagentUseds.concat(SBSkit300cycles);
					 }
//					 else if (nbCyclesRead === "75"){
//						 ReagentUseds = ReagentUseds.concat(SBSkit150cycles);						 
//					 }
				 }
				 else if(lectureType === "SR"){
					 var ReagentUseds = [{
							       "kitCatalogCode": "24SC16VM8",
							       "boxCatalogCode": "24SC1CQ80",
							       "reagentCatalogCode": "24SC26KNM" 
							    },
							     {
							       "kitCatalogCode": "24SC16VM8",
							       "boxCatalogCode": "24SC1CQ80",
							       "reagentCatalogCode": "24SC26KNR" 
							    },
							     {
							       "kitCatalogCode": "24SC16VM8",
							       "boxCatalogCode": "24SC1CQ80",
							       "reagentCatalogCode": "24SC26KNU" 
							    },
							     {
							       "kitCatalogCode": "24SC16VM8",
							       "boxCatalogCode": "24SC1CQ80",
							       "reagentCatalogCode": "24SC26KO5" 
							    },
							     {
							       "kitCatalogCode": "24SC16VM8",
							       "boxCatalogCode": "24SC1CQ80",
							       "reagentCatalogCode": "24SC26KO6" 
							    },
							     {
							       "kitCatalogCode": "24SC16VM8",
							       "boxCatalogCode": "24SC1CQ80",
							       "reagentCatalogCode": "24SC26KO8" 
							    },
							     {
							       "kitCatalogCode": "24SC16VM8",
							       "boxCatalogCode": "24SC1CQ80",
							       "reagentCatalogCode": "24SC26KOQ" 
							    }];
					 
					 if(nbCyclesRead === "151"){
						 ReagentUseds = ReagentUseds.concat(SBSkit150cycles);
					 }
//					 else if(nbCyclesRead === "251" || nbCyclesRead === "301"){
//						 ReagentUseds = ReagentUseds.concat(SBSkit300cycles);
//					 }
				 }
				 $scope.experiment.reagents = ReagentUseds;
				 $scope.$emit('askRefreshReagents');
				 }
						
		}

		var validateProcessProperties = function(experiment) {
			if(experiment.state.code === "N"){
				for(var j = 0 ; j < experiment.atomicTransfertMethods.length && experiment.atomicTransfertMethods != null; j++){
					var atm = experiment.atomicTransfertMethods[j];
					var concentration = undefined;
					for(var i=0;i < atm.inputContainerUseds.length;i++){
						var inputContainerUsed = atm.inputContainerUseds[i];
						for(var cn=0;cn < inputContainerUsed.contents.length;cn++){
							var content = inputContainerUsed.contents[cn];
//							console.log("validateProcessProperties " + JSON.stringify(inputContainerUsed));
							if(content.processProperties){
								console.log("validateProcessProperties OK " + JSON.stringify(content.processProperties));							
							}else{
								console.log("validateProcessProperties KO " + JSON.stringify(content));

					    		$scope.messages.clazz = "alert alert-danger";
					    		$scope.messages.text = Messages("Pas de processProperties pour " + content.sampleCode);
					    		$scope.messages.showDetails = false;
					    		$scope.messages.open();	
							}
						}
					}
				}			
			}
			
		}
		var processPropertiesToExperiment = function(experiment) {
			for(var j = 0 ; j < experiment.atomicTransfertMethods.length && experiment.atomicTransfertMethods != null; j++){
				var atm = experiment.atomicTransfertMethods[j];
//				console.log("updateProperties");
				if (!atm.inputContainerUseds[0].experimentProperties){
					var value = null;
					var setter = null;
//					console.log("species : " + JSON.stringify(atm.inputContainerUseds[0].contents[0]));
					if (atm.inputContainerUseds[0].contents[0].processProperties.species != null){
						console.log("if 1.1 : " + JSON.stringify(atm.inputContainerUseds[0].contents[0].processProperties.species));
						value = atm.inputContainerUseds[0].contents[0].processProperties.species.value;
						setter = $parse("inputContainerUseds[0].experimentProperties.species.value").assign;
						setter(atm, value);
					}
					if (atm.inputContainerUseds[0].contents[0].processProperties.reference_transcriptome != null){
					console.log("if 1.2 ");
						value = atm.inputContainerUseds[0].contents[0].processProperties.reference_transcriptome.value;
						setter = $parse("inputContainerUseds[0].experimentProperties.reference_transcriptome.value").assign;
						setter(atm, value);
					}
					if (atm.inputContainerUseds[0].contents[0].processProperties.reference_genome != null){
					console.log("if 1.3 ");
						value = atm.inputContainerUseds[0].contents[0].processProperties.reference_genome.value;
						setter = $parse("inputContainerUseds[0].experimentProperties.reference_genome.value").assign;
						setter(atm, value);
					}
//				}else{
//					if (!atm.inputContainerUseds[0].experimentProperties.species & atm.inputContainerUseds[0].contents[0].processProperties.species != null){
//					console.log("else 2.1 ");
////						console.log("if 2 : " + JSON.stringify(atm.inputContainerUseds[0].contents[0]));
//						value = atm.inputContainerUseds[0].contents[0].processProperties.species.value;
//						setter = $parse("inputContainerUseds[0].experimentProperties.species.value").assign;
//						setter(atm, value);
//					}
//					if (!atm.inputContainerUseds[0].experimentProperties.reference_transcriptome & atm.inputContainerUseds[0].contents[0].processProperties.reference_transcriptome != null){
//					console.log("else 2.2 ");
//						value = atm.inputContainerUseds[0].contents[0].processProperties.reference_transcriptome.value;
//						setter = $parse("inputContainerUseds[0].experimentProperties.reference_transcriptome.value").assign;
//						setter(atm, value);
//					}
//					if (!atm.inputContainerUseds[0].experimentProperties.reference_genome & atm.inputContainerUseds[0].contents[0].processProperties.reference_genome != null){
//					console.log("else 2.3 ");
//						value = atm.inputContainerUseds[0].contents[0].processProperties.reference_genome.value;
//						setter = $parse("inputContainerUseds[0].experimentProperties.species.reference_genome.value").assign;
//						setter(atm, value);
//					}
//					
				}
			}
		}
		
		$scope.$on('save', function(e, callbackFunction) {	
			console.log("call event save on one-to-void");
			addReagents();
			$scope.atmService.data.save();
			$scope.atmService.viewToExperimentOneToVoid($scope.experiment);
			processPropertiesToExperiment($scope.experiment);
			$scope.$emit('childSaved', callbackFunction);
		});
		
		$scope.$on('refresh', function(e) {
			console.log("call event refresh on one-to-void");	
//			addButtons();
			var dtConfig = $scope.atmService.data.getConfig();
			$scope.atmService.data.setConfig(dtConfig);
			
			$scope.atmService.refreshViewFromExperiment($scope.experiment);
			$scope.$emit('viewRefeshed');
		});
		
		$scope.$on('cancel', function(e) {
			console.log("call event cancel");
			$scope.atmService.data.cancel();						
		});
		
		$scope.$on('activeEditMode', function(e) {
			console.log("call event activeEditMode");
			validateProcessProperties($scope.experiment);
			$scope.atmService.data.selectAll(true);
			$scope.atmService.data.setEdit();
		});
		
		var atmService = atmToSingleDatatable($scope, datatableConfig, true);
		//defined new atomictransfertMethod
		atmService.newAtomicTransfertMethod = function(line, column){
			return {
				class:"OneToVoid",
				line:line, 
				column:column, 				
				inputContainerUseds:new Array(0)
			};
		};
		
		atmService.experimentToView($scope.experiment, $scope.experimentType);
		
		$scope.atmService = atmService;
		
		//buttons creation
// A MODIFIER récupere le nombre de contents 10x	
//(limiter au nombre de lanes)
		var check10x = function(experiment) {
			var tenX = 0;
			for(var j = 0 ; j < experiment.atomicTransfertMethods.length && experiment.atomicTransfertMethods != null; j++){
				var atm = experiment.atomicTransfertMethods[j];
				for ( var inCU = 0; inCU < atm.inputContainerUseds.length; inCU++) {
					for (var ctt = 0; ctt < atm.inputContainerUseds[inCU].contents.length; ctt++) {
						if(atm.inputContainerUseds[inCU].contents[ctt].properties.tag != null){
							if(atm.inputContainerUseds[inCU].contents[ctt].properties.tag.value.startsWith("10X")){
								console.log ("Tag 10x : " + JSON.stringify(atm.inputContainerUseds[inCU].contents[ctt].properties.tag.value));
								tenX++;
							}
						}
					}
				}
			}
			return tenX;
		}
		
		
// A MODIFIER récupere le nombre total des contents
//(limiter au nombre de lanes)
//		var countContents = function(experiment) {
//			var c = 0;
//			for(var j = 0 ; j < experiment.atomicTransfertMethods.length && experiment.atomicTransfertMethods != null; j++){
//				var atm = experiment.atomicTransfertMethods[j];
//				for ( var inCU = 0; inCU < atm.inputContainerUseds.length; inCU++) {
//					for (var ctt = 0; ctt < atm.inputContainerUseds[inCU].contents.length; ctt++) {
//						c++;
//					}
//				}
//			}
//			return c;
//		}
				
		var generateSampleSheetIEM = function(){
			
			generateSampleSheet("jFlow");
		
//			if(check10x($scope.experiment) < countContents($scope.experiment)){
				generateSampleSheet("IEM");	
//			};
			if(check10x($scope.experiment) > 0){
				console.log ("10x = " + check10x($scope.experiment));
				generateSampleSheet("10x");
			};
			
			
		};
		
//		var generateSampleSheetJFlow = function(){
//			generateSampleSheet("jFlow");
//		};
			
		var generateSampleSheet = function(f){
			$scope.messages.clear();
			$http.post(jsRoutes.controllers.instruments.io.IO.generateFile($scope.experiment.code).url,{'fType':f})
			.success(function(data, status, headers, config) {
				var header = headers("Content-disposition");
				var filepath = header.split("filename=")[1];
				var filename = filepath.split(/\/|\\/);
				filename = filename[filename.length-1];
				if(data!=null){
					$scope.messages.clazz="alert alert-success";
					$scope.messages.text=Messages('experiments.msg.generateSampleSheet.success')+" : "+filepath;
					$scope.messages.showDetails = false;
					$scope.messages.open();	
					
					var blob = new Blob([data], {type: "text/plain;charset=utf-8"});    					
					saveAs(blob, filename);
				}
			})
			.error(function(data, status, headers, config) {
				$scope.messages.clazz = "alert alert-danger";
				$scope.messages.text = Messages('experiments.msg.generateSampleSheet.error');
				$scope.messages.setDetails(data);
				$scope.messages.showDetails = true;
				$scope.messages.open();				
			});
		};

//		var addButtons = function(){
//			if (!$scope.mainService.isEditMode() && $parse('experiment.state.code')($scope) != "N"){
//			if ($parse('experiment.state.code')($scope) === "IP"){
				$scope.setAdditionnalButtons([{
					isDisabled : function(){
						if ($scope.isCreationMode()){
							return $scope.isCreationMode();
						}else{
							return $parse('experiment.state.code')($scope) == "N";
						}
					},
					isShow:function(){return true},
					click:generateSampleSheetIEM,
					label:Messages("experiments.sampleSheet")
				}
//				{
//					isDisabled : function(){return $scope.isCreationMode();},
//					isShow:function(){return true},
//					click:generateSampleSheetJFlow,
//					label:Messages("experiments.sampleSheetJF")
//				}
				]);
//			}
//			else{
//				$scope.setAdditionnalButtons([{
//					isDisabled : function(){return $scope.isCreationMode();},
//					isShow:function(){return true},
//					click:generateSampleSheetIEM,
//					label:Messages("experiments.sampleSheet")
//				}]);			
//			} 
//		}
//		addButtons();
}]);