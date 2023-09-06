angular.module('atomicTransfereServices', [])
.factory('commonAtomicTransfertMethod', ['$http', '$parse', '$q', 'mainService', 
                                         function($http, $parse, $q, mainService){

	var constructor = function($scope){
		var common = {
				
				removeNullProperties : function(properties){
					for (var p in properties) {
						if(properties[p] != undefined && (properties[p].value === undefined || properties[p].value === null || properties[p].value === "")){
							properties[p] = undefined;
						}
					}
				},
				getDisplayUnitFromProperty:function(propertyDefinition){
					var unit = $parse("displayMeasureValue.value")(propertyDefinition);
					if(undefined !== unit && null !== unit) return " ("+unit+")";
					else return "";
				},
				getPropertyColumnType : function(type){
					if(type === "java.lang.String"){
						return "text";
					}else if(type === "java.lang.Double" || type === "java.lang.Integer" || type === "java.lang.Long"){
						return "number";
					}else if(type === "java.util.Date"){
						return "date";
					}else if(type ==="java.io.File"){
						return "file";
					}else if(type ==="java.awt.Image"){
						return "img";
					}else if(type ==="java.lang.Boolean"){
						return "boolean";	
					}else{
						throw 'not manage : '+type;
					}

					return type;
				},
				convertSinglePropertyToDatatableColumn : function(propertyDefinition, propertyNamePrefix, extraHeaders){
					return this.convertPropertyToDatatableColumn(propertyDefinition, propertyNamePrefix, ".value", extraHeaders); 
				},
				convertObjectPropertyToDatatableColumn : function(propertyDefinition, propertyNamePrefix, extraHeaders){
					return this.convertPropertyToDatatableColumn(propertyDefinition, propertyNamePrefix, "", extraHeaders); 
				},
				convertFilePropertyToDatatableColumn : function(propertyDefinition, propertyNamePrefix, extraHeaders){
					return this.convertPropertyToDatatableColumn(propertyDefinition, propertyNamePrefix, "", extraHeaders); 
				},
				convertImagePropertyToDatatableColumn : function(propertyDefinition, propertyNamePrefix, extraHeaders){
					return this.convertPropertyToDatatableColumn(propertyDefinition, propertyNamePrefix, "", extraHeaders); 
				},
				convertObjectListPropertyToDatatableColumn : function(propertyDefinition, propertyNamePrefix, extraHeaders){
					//in case of list the datatable manage the list so we remove the prefix of the property definition					
					var pd = angular.copy(propertyDefinition);
					pd.code = pd.code.substring(pd.code.indexOf(".")+1, pd.code.length);					
					return this.convertPropertyToDatatableColumn(pd, propertyNamePrefix, "", extraHeaders); 
				},
				convertTypePropertyToDatatableColumn : function(propertyDefinition, propertyNamePrefix, extraHeaders){
					if(propertyDefinition.propertyValueType==="file"){
						return this.convertFilePropertyToDatatableColumn(propertyDefinition, propertyNamePrefix, extraHeaders);
					}else if(propertyDefinition.propertyValueType==="img"){
						return this.convertImagePropertyToDatatableColumn(propertyDefinition, propertyNamePrefix, extraHeaders);
					}
					return this.convertSinglePropertyToDatatableColumn(propertyDefinition, propertyNamePrefix, extraHeaders); 
				},
				convertPropertyToDatatableColumn : function(propertyDefinition, propertyNamePrefix, propertyNameSuffix,extraHeaders){
    				var column = {};
    				column.watch=true;
    				column.header = propertyDefinition.name + this.getDisplayUnitFromProperty(propertyDefinition);
    				
    				if(propertyDefinition.required){
    					if(null != propertyDefinition.requiredState || undefined != propertyDefinition.requiredState){
    						column.required="isRequired('"+propertyDefinition.requiredState+"')";	    				
    					}else{
    						column.required="isRequired()";
    					}
    				}
    				column.property = propertyNamePrefix+propertyDefinition.code+propertyNameSuffix;
    				column.edit = propertyDefinition.editable;
    				column.hide =  true;
    				column.order = true;
    				column.tdClass="valuationService.valuationCriteriaClass(value.data, experiment.status.criteriaCode, col.property)"
    				
    				column.type = this.getPropertyColumnType(propertyDefinition.valueType);
    				column.choiceInList = propertyDefinition.choiceInList;
    				column.position=propertyDefinition.displayOrder;
    				column.defaultValues = propertyDefinition.defaultValue;
    				column.format = propertyDefinition.displayFormat;
    				
    				if(column.choiceInList){
    					if(propertyDefinition.possibleValues.length > 100){
    						column.editTemplate='<input class="form-control" type="text" #ng-model typeahead="v.code as v.name for v in col.possibleValues | filter:$viewValue | limitTo:20" typeahead-min-length="1" udt-change="updatePropertyFromUDT(value,col)"/>';        					
    					}
    					column.possibleValues = propertyDefinition.possibleValues; 
    					column.filter = "codes:'value."+propertyDefinition.code+"'";    					
    				}
    				if(extraHeaders!=undefined){
    					column.extraHeaders = extraHeaders;
    				}
    				if(propertyDefinition.displayMeasureValue != undefined && propertyDefinition.displayMeasureValue != null){
    					column.convertValue = {"active":true, "displayMeasureValue":propertyDefinition.displayMeasureValue.value, 
    							"saveMeasureValue":propertyDefinition.saveMeasureValue.value};
    				}
    				
    				if(propertyDefinition.editable){
    					column.editDirectives=' udt-change="updatePropertyFromUDT(value,col)" ';       				
    				}
    				return column;
    			},
				//Common for all but try to replace slowly
				convertContainerToInputContainerUsed : function(container){
					var input = {
						code:container.code,
						categoryCode:container.categoryCode, 
						contents:container.contents, //used in rules							
					    locationOnContainerSupport:container.support,
					    volume:container.volume, //used in rules
						concentration:container.concentration,  //used in rules
						quantity:container.quantity,
						size:container.size,
						instrumentProperties:undefined,
					    experimentProperties:undefined,
					    percentage:100, //rules by defaut need check with server
						//can be updated
						sampleCodes:container.sampleCodes,
						projectCodes:container.projectCodes,
						fromTransformationTypeCodes:container.fromTransformationTypeCodes,
						fromTransformationCodes:container.fromTransformationCodes,
						fromPurificationTypeCode:container.fromPurificationTypeCode,
						fromTransfertTypeCode:container.fromTransfertTypeCode,
					    processTypeCodes:container.processTypeCodes,
					    processCodes:container.processCodes,
						state : container.state,
						valuation : {valid:'UNSET'},
						copyValuationToInput:'TRUE'
						//not valuation to have the default value
					};
					
					input.locationOnContainerSupport.storageCode = undefined;
					
					return input;					
				},
				updateInputContainerUsedFromContainer : function(containerUsed, container, experimentStateCode){
					containerUsed.categoryCode = container.categoryCode; 
					containerUsed.contents = container.contents;
					//GA for storage we keep the experiment information to change it. used in QC.
					
					var storageCode = $parse("locationOnContainerSupport.storageCode")(containerUsed);
					containerUsed.locationOnContainerSupport = container.support;
					containerUsed.locationOnContainerSupport.storageCode = storageCode;
					if(experimentStateCode != null && experimentStateCode != undefined && experimentStateCode !== 'F'){
						containerUsed.volume = container.volume;
						containerUsed.concentration = container.concentration;
						containerUsed.quantity = container.quantity;
						containerUsed.size = container.size;
						
						containerUsed.fromTransformationTypeCodes=container.fromTransformationTypeCodes;
						containerUsed.fromTransformationCodes=container.fromTransformationCodes;
						containerUsed.processTypeCodes=container.processTypeCodes;
						containerUsed.processCodes=container.processCodes;
						
						containerUsed.fromPurificationTypeCode=container.fromPurificationTypeCode;
						containerUsed.fromTransfertTypeCode=container.fromTransfertTypeCode;
					}
					containerUsed.sampleCodes=container.sampleCodes;
					containerUsed.projectCodes=container.projectCodes;
					containerUsed.state = container.state; //TODO GA need to be remove but keep for backward compatibility
					return containerUsed;
				},
				getContainerListPromise : function(containerCodes){
					if(containerCodes.length > 0){
						var nbElementByBatch = Math.ceil(containerCodes.length / 6); //6 because 6 request max in parrallel with firefox and chrome
		                var queries = [];
		                for (var i = 0; i < 6 && containerCodes.length > 0; i++) {
		                    var subContainerCodes = containerCodes.splice(0, nbElementByBatch);
		                    queries.push( $http.get(jsRoutes.controllers.containers.api.Containers.list().url,{params:{codes:subContainerCodes}, atomicObject:this}) );
		                }
						
						return $q.all(queries); 		
					}else{
						return $q(function(resolve, reject){
							resolve({data:[]}); //empty array
						});
					}								 				 	
				},
				loadInputContainerFromAtomicTransfertMethods : function(atomicTransfertMethods){
					var containerInputCodes = [];
					angular.forEach(atomicTransfertMethods, function(atomicTransfertMethod) {
						if(atomicTransfertMethod !== null){
							angular.forEach(atomicTransfertMethod.inputContainerUseds, function(inputContainerUsed){
									this.push(inputContainerUsed.code);
									}, this);	
						}
					}, containerInputCodes);
						
					return this.getContainerListPromise(containerInputCodes).then(function(results){
				 		var inputContainers = {};
				 		angular.forEach(results, function(result){
				 			angular.forEach(result.data, function(container) {
								this[container.code] = container;
								}, inputContainers);				 			
				 		});
				 		
						return {"input":inputContainers};
				 	});
				},
				
				loadOutputContainerFromAtomicTransfertMethods : function(atomicTransfertMethods){
					var containerOutpuCodes = [];
					angular.forEach(atomicTransfertMethods, function(atomicTransfertMethod) {
						if(atomicTransfertMethod !== null){
							angular.forEach(atomicTransfertMethod.outputContainerUseds, function(outputContainerUsed){
									if(null !== outputContainerUsed.code && undefined !== outputContainerUsed.code){
										this.push(outputContainerUsed.code);
									}									
								}, this);	
						}
																			
					}, containerOutpuCodes);
															
					return this.getContainerListPromise(containerOutpuCodes).then(function(results){
						var outputContainers = {};
						angular.forEach(results, function(result){
				 			angular.forEach(result.data, function(container) {
								this[container.code] = container;
								}, outputContainers);				 			
				 		});
				 		return {"output":outputContainers};
				 	});
				},
				
				loadInputContainerFromBasket : function(basketValues){					
						var containerInputCodes = [];
						angular.forEach(basketValues, function(containerInput) {
							if(containerInput !== null){
								this.push(containerInput.code);
							}							
						}, containerInputCodes);
						return this.getContainerListPromise(containerInputCodes).then(function(results){
							var containers = [];
							angular.forEach(results, function(result){
					 			angular.forEach(result.data, function(container) {
									this.push(container);
									}, containers);				 			
					 		});							
							return containers;
					 	});					
				},
				
				/**
				 * Create a new OutputContainerUsed. By default unit is the same as inputContainer for volume, concentration, quantity
				 * In second time, we need to find the default concentration because several concentration are available for one container
				 */
				newOutputContainerUsed : function(defaultOutputUnit, defaultOutputValue, atmLine, atmColumn, inputContainer){
					return {
						code:undefined,
						categoryCode:this.getContainerCategoryCode(), 
						locationOnContainerSupport:{
							categoryCode:this.getSupportContainerCategoryCode(), 
							line:atmLine,
							column:atmColumn
						},
						contents:undefined, //populate by the server
						volume:{unit:this.getUnit($parse('volume')(inputContainer), defaultOutputUnit.volume), value:this.getValue($parse('volume')(inputContainer), defaultOutputValue.volume)}, 
						concentration:{unit:this.getUnit($parse('concentration')(inputContainer), defaultOutputUnit.concentration), value:this.getValue($parse('concentration')(inputContainer), defaultOutputValue.concentration)}, 
						quantity:{unit:this.getUnit($parse('quantity')(inputContainer), defaultOutputUnit.quantity), value:this.getValue($parse('quantity')(inputContainer), defaultOutputValue.quantity)},
						size:{unit:this.getUnit($parse('size')(inputContainer), defaultOutputUnit.size), value:this.getValue($parse('size')(inputContainer), defaultOutputValue.size)},
						instrumentProperties:undefined,
					    experimentProperties:undefined
					};
				},
				updateOutputContainerUsed:function(outputContainer, atmLine, atmColumn){
					if(null === outputContainer.categoryCode || undefined === outputContainer.categoryCode){
						outputContainer.categoryCode = this.getContainerCategoryCode();
					}
					if(null === outputContainer.locationOnContainerSupport || undefined === outputContainer.locationOnContainerSupport){
						outputContainer.locationOnContainerSupport = {};
					}
					if(null === outputContainer.locationOnContainerSupport.categoryCode || undefined === outputContainer.locationOnContainerSupport.categoryCode){
						outputContainer.locationOnContainerSupport.categoryCode = this.getSupportContainerCategoryCode();
					}
					if(null === outputContainer.locationOnContainerSupport.line || undefined === outputContainer.locationOnContainerSupport.line){
						outputContainer.locationOnContainerSupport.line = atmLine;
					}
					if(null === outputContainer.locationOnContainerSupport.column || undefined === outputContainer.locationOnContainerSupport.column){
						outputContainer.locationOnContainerSupport.column = atmColumn;
					}
					return outputContainer;
					
				},				
				getContainerCategoryCode :function(){
					var supportContainerCategoryCode = this.getSupportContainerCategoryCode();
					var instrumentType = mainService.get("instrumentType");
					var containerCategoryCode = [];
					angular.forEach(instrumentType.outContainerSupportCategories,function(value){
						if(supportContainerCategoryCode === value.code){
							containerCategoryCode[0] = value.containerCategory.code;
						}
					},containerCategoryCode);
					if(containerCategoryCode.length === 1){
						return containerCategoryCode[0];
					}else{
						throw "not found containerCategoryCode";
					}
					
				},				
				getSupportContainerCategoryCode :function(){
					return mainService.get("experiment").instrument.outContainerSupportCategoryCode;
				},				
				/**
				 * We take the same unit as inputContainer when create the output container
				 */
				getUnit: function(object, defaultValue){
					var unit = $parse("unit")(object);
					if(undefined === unit || null === unit || (undefined !== defaultValue && null !== defaultValue && unit !== defaultValue))unit = defaultValue
					return unit;
				},
				/**
				 * We take the same unit as inputContainer when create the output container
				 */
				getValue: function(object, defaultValue){
					value = undefined;
					if(angular.isFunction(defaultValue)){
						value = defaultValue(value);
					}else if($parse("copyInputContainer")(defaultValue)){
						value = $parse("value")(object);
					}
					return value;
				}
		};
		return common;
	};
	return constructor;


	
}]).factory('atmToSingleDatatable', ['$http', '$parse', '$filter', '$q', 'commonAtomicTransfertMethod','mainService', 'datatable', '$rootScope', 
                         function($http, $parse, $filter, $q, commonAtomicTransfertMethod, mainService, datatable, $rootScope){
	
	
	var constructor = function($scope, datatableConfig, outputIsVoid){
		
		var $outputIsVoid = (outputIsVoid !== undefined)?outputIsVoid : false; //false when void in output
		var $commonATM = commonAtomicTransfertMethod($scope);
		datatableConfig.formName="experimentDatatableForm";
		var $datatable = datatable(datatableConfig);
		
		var view = {
				$outputIsVoid : $outputIsVoid,
				$commonATM : $commonATM,
				data:$datatable,
				isAddNew:true, //used to add or not new input container in datatable
				defaultOutputUnit:{volume:undefined, concentration:undefined, quantity:undefined},
				defaultOutputValue:{volume:undefined, concentration:undefined, quantity:undefined},
				newAtomicTransfertMethod : function(line, column){
					throw 'newAtomicTransfertMethod not defined in atmToSingleDatatable client';
				},
				
				/* function to override in controler, used in addNewAtomicTransfertMethodsInDatatable function  */
				updateNewAtmBeforePushInUDT : function(atm){
					
				},
				addColumnToDatatable:function(columns, newColumn){
					if(null !== newColumn && undefined !== newColumn){
						columns.push(newColumn);
					}
				},				
				convertOutputPropertiesToDatatableColumn : function(property, pName){
					return  $commonATM.convertTypePropertyToDatatableColumn(property,"outputContainerUsed."+pName+".",{"0":Messages("experiments.outputs")});
				},
				convertInputPropertiesToDatatableColumn : function(property, pName){
					return  $commonATM.convertTypePropertyToDatatableColumn(property,"inputContainerUsed."+pName+".",{"0":Messages("experiments.inputs")});
				},	
				
				addExperimentPropertiesToDatatable : function(experimentProperties){
					var expProperties = experimentProperties;
					var newColums = []; 
					var $that = this;
					if(expProperties != undefined && expProperties != null){
						if(!$that.$outputIsVoid){
							var outNewColumn = $filter('filter')(expProperties, 'ContainerOut');
							angular.forEach(outNewColumn, function(property){
								$that.addColumnToDatatable(this, $that.convertOutputPropertiesToDatatableColumn(property, "experimentProperties"));														
							}, newColums);
						}
						
						var inNewColumn = $filter('filter')(expProperties, 'ContainerIn')
						angular.forEach(inNewColumn, function(property){
							$that.addColumnToDatatable(this, $that.convertInputPropertiesToDatatableColumn(property, "experimentProperties"));														
						}, newColums);
												
					}
					this.data.setColumnsConfig(this.data.getColumnsConfig().concat(newColums))
				},
				
				addInstrumentPropertiesToDatatable : function(instrumentProperties){
					var instProperties = instrumentProperties;
					var newColums = []; 
					var $that = this;
					if(instProperties != undefined && instProperties != null){
						if(!$that.$outputIsVoid){
							var outNewColumn = $filter('filter')(instProperties, 'ContainerOut');
							angular.forEach(outNewColumn, function(property){
								$that.addColumnToDatatable(this, $that.convertOutputPropertiesToDatatableColumn(property, "instrumentProperties"));														
							}, newColums);
						}
						
						var inNewColumn = $filter('filter')(instProperties, 'ContainerIn')
						angular.forEach(inNewColumn, function(property){
							$that.addColumnToDatatable(this, $that.convertInputPropertiesToDatatableColumn(property, "instrumentProperties"));														
						}, newColums);
												
					}
					this.data.setColumnsConfig(this.data.getColumnsConfig().concat(newColums))
				},
				
				customExperimentToView : undefined, //used to cutom the view with one atm
				
				convertExperimentATMToDatatable : function(experimentATMs, experimentStateCode){
					var promises = [];
					
					var atms = experimentATMs;
					
					promises.push($commonATM.loadInputContainerFromAtomicTransfertMethods(atms));					
					promises.push($commonATM.loadOutputContainerFromAtomicTransfertMethods(atms));
					
					var $that = this;
	                $q.all(promises).then(function (result) {
						var allData = [];
						var inputContainers, outputContainers;
						if(result[0].input){
							inputContainers = result[0].input;
						}else if(result[1].input){
							inputContainers = result[1].input;
						}
						
						if(!$that.$outputIsVoid && result[1].output){
							outputContainers = result[1].output;
						}else if(!$that.$outputIsVoid && result[0].output){
							outputContainers = result[0].output;
						}
					
						var l=0, atomicIndex=0;
						for(var i=0; i< atms.length;i++){
							
							if(atms[i] === null){
								continue;
							}
							//var atm = angular.copy(atms[i]);
							var atm = $.extend(true,{}, atms[i]);
							
							atm.inputContainerUseds = $filter('orderBy')(atm.inputContainerUseds, 'code'); //only interesting for oneToMany
							
							for(var j=0; j<atm.inputContainerUseds.length ; j++){
								
								var inputContainerCode = atm.inputContainerUseds[j].code;
								var inputContainer = inputContainers[inputContainerCode];
								if(!$that.$outputIsVoid){
									for(var k=0 ; k < atm.outputContainerUseds.length ; k++){
							              var outputContainerCode = atm.outputContainerUseds[k].code;
							              var outputContainer = outputContainers[outputContainerCode];
							              
							              allData[l] = {atomicIndex:atomicIndex};
							              allData[l].atomicTransfertMethod = atm;
							              allData[l].inputContainer = inputContainer;							              
							              
							              //allData[l].inputContainerUsed = angular.copy(atm.inputContainerUseds[j]);
							              allData[l].inputContainerUsed = $.extend(true,{}, atm.inputContainerUseds[j]);
							              allData[l].inputContainerUsed = $commonATM.updateInputContainerUsedFromContainer(allData[l].inputContainerUsed, inputContainer, experimentStateCode);
							              
							              //allData[l].outputContainerUsed = angular.copy(atm.outputContainerUseds[k]);
							              allData[l].outputContainerUsed =  $.extend(true,{}, atm.outputContainerUseds[k]);
							              allData[l].outputContainerUsed = $commonATM.updateOutputContainerUsed(allData[l].outputContainerUsed, atm.line, atm.column);
							              allData[l].outputContainer = outputContainer;
							              l++;							             
							        }
									if($that.customExperimentToView !== undefined){
										$that.customExperimentToView(atm, inputContainers, outputContainers);
									}
								}else{
									allData[l] = {atomicIndex:atomicIndex};
									allData[l].atomicTransfertMethod = atm;							              
									//allData[l].inputContainerUsed = angular.copy(atm.inputContainerUseds[j]);
									allData[l].inputContainerUsed = $.extend(true,{}, atm.inputContainerUseds[j]);
									allData[l].inputContainerUsed = $commonATM.updateInputContainerUsedFromContainer(allData[l].inputContainerUsed, inputContainer, experimentStateCode);
									allData[l].inputContainer = inputContainer;	
									if($that.customExperimentToView !== undefined){
										$that.customExperimentToView(atm, inputContainers);
									}
									l++;
									
								}
							}
							atomicIndex++;
						}
						
						// FDS: line*1 a un sens pour les strips et les flowcell car la ligne est numérique 
						if(allData[0] && allData[0].inputContainer.categoryCode === 'well'){
							// GA 16/03/2017 added inputContainer.support.code
							allData = $filter('orderBy')(allData, ['inputContainer.support.code','inputContainer.support.column*1', 'inputContainer.support.line']);
						}else{
							// FDS 10/03/2017 added inputContainer.suport.line and inputContainer.support.column; 
							allData = $filter('orderBy')(allData,['inputContainer.support.code','inputContainer.support.column*1','inputContainer.support.line*1']);
						}
						
						//GA : used directly _setData for not cancel the configuration !!! but may be some problems
						$that.data._setData(allData, allData.length);
						//add new atomic in datatable
						$that.addNewAtomicTransfertMethodsInDatatable();
	                });
				},
				//One atomic by input only for OneToOne but not manyToOne ???
				/**
				 * type = OneToOne or ManyToOne
				 */
				addNewAtomicTransfertMethodsInDatatable : function(){
					if(null != mainService.getBasket() && null != mainService.getBasket().get() && this.isAddNew){
						$that = this;
						
						var type = $that.newAtomicTransfertMethod().class;
						
						$commonATM.loadInputContainerFromBasket(mainService.getBasket().get())
							.then(function(containers) {								
								var allData = [], i = 0;
								var atomicTransfertMethod = undefined;
								
								if($that.data.getData() !== undefined && $that.data.getData().length > 0){
									allData = $that.data.getData();
									i = allData.length;
								}
								
								if(type === "ManyToOne" && i === 0){
									atomicTransfertMethod =  $that.newAtomicTransfertMethod();
								}else if(type === "ManyToOne" && i > 0){
									atomicTransfertMethod =  allData[0].atomicTransfertMethod;
								}
								
								angular.forEach(containers, function(container){
									var line = {};
									if(type === "ManyToOne"){
										line.atomicTransfertMethod = atomicTransfertMethod;
										line.atomicIndex=0;
									}else{
										//GA 08/02/2016 input plate organization is the same as ouput plate organization !!
										line.atomicTransfertMethod = $that.newAtomicTransfertMethod(container.support.line, container.support.column);
										line.atomicIndex=i++;
									}
										
									line.inputContainer = container;
									line.inputContainerUsed = $commonATM.convertContainerToInputContainerUsed(line.inputContainer);
									if(!$that.$outputIsVoid){
										line.outputContainerUsed = $commonATM.newOutputContainerUsed($that.defaultOutputUnit,$that.defaultOutputValue,line.atomicTransfertMethod.line,
												line.atomicTransfertMethod.column,line.inputContainer);
										line.outputContainer = undefined;
										$that.updateNewAtmBeforePushInUDT(line);
									}
									allData.push(line);
								});
								
								// FDS: line*1 a un sens pour les strips et les flowcell car la ligne est numérique 
								if(allData[0].inputContainer.categoryCode === 'well'){
									// GA 16/03/2017 added inputContainer.support.code
									allData = $filter('orderBy')(allData, ['inputContainer.support.code','inputContainer.support.column*1', 'inputContainer.support.line']);
								}else{
									// FDS 10/03/2017 added  inputContainer.support.line and inputContainer.support.column
									allData = $filter('orderBy')(allData,['inputContainer.support.code','inputContainer.support.column*1','inputContainer.support.line*1']);					
								}
								$that.data.setData(allData, allData.length);											
						});
					}					
				},
				
				experimentToView:function(experiment, experimentType){
					if(null === experiment || undefined === experiment){
						throw 'experiment is required';
					}
					if(!$scope.isCreationMode()){
						this.convertExperimentATMToDatatable(experiment.atomicTransfertMethods, experiment.state.code);													
					}else{
						this.addNewAtomicTransfertMethodsInDatatable();
					}
					this.addExperimentPropertiesToDatatable(experimentType.propertiesDefinitions);					
				},
				
				refreshViewFromExperiment : function(experiment){
					if(null === experiment || undefined === experiment){
						throw 'experiment is required';
					}
					this.convertExperimentATMToDatatable(experiment.atomicTransfertMethods, experiment.state.code);				
				},
				viewToExperimentOneToVoid :function(experimentIn){
					this.viewToExperimentOneToOne(experimentIn);
				},
				viewToExperimentOneToOne :function(experimentIn){		
					if(null === experimentIn || undefined === experimentIn){
						throw 'experiment is required';
					}
					experiment = experimentIn;
					var allData = this.data.getData();
					if(allData != undefined){
						experiment.atomicTransfertMethods = []; // to manage remove
						for(var i=0;i<allData.length;i++){
							var atomicIndex = allData[i].atomicIndex;								
							experiment.atomicTransfertMethods[atomicIndex] = allData[i].atomicTransfertMethod
							experiment.atomicTransfertMethods[atomicIndex].inputContainerUseds[0] = allData[i].inputContainerUsed;	
							
							$commonATM.removeNullProperties(experiment.atomicTransfertMethods[atomicIndex].inputContainerUseds[0].instrumentProperties);
							$commonATM.removeNullProperties(experiment.atomicTransfertMethods[atomicIndex].inputContainerUseds[0].experimentProperties);
							
							if(!this.$outputIsVoid){
								experiment.atomicTransfertMethods[atomicIndex].outputContainerUseds[0] = allData[i].outputContainerUsed;
								$commonATM.removeNullProperties(experiment.atomicTransfertMethods[atomicIndex].outputContainerUseds[0].instrumentProperties);
								$commonATM.removeNullProperties(experiment.atomicTransfertMethods[atomicIndex].outputContainerUseds[0].experimentProperties);
							}
	
						}
						//remove atomic null
						var cleanAtomicTransfertMethods = [];
						for(var i = 0; i < experiment.atomicTransfertMethods.length ; i++){
							if(experiment.atomicTransfertMethods[i]){
								cleanAtomicTransfertMethods.push(experiment.atomicTransfertMethods[i]);
							}
						}
						experiment.atomicTransfertMethods = $filter('orderBy')(cleanAtomicTransfertMethods,["inputContainerUseds[0].code"]);
					}								
				},
				viewToExperimentOneToMany :function(experimentIn){		
					if(null === experimentIn || undefined === experimentIn){
						throw 'experiment is required';
					}
					experiment = experimentIn;
					var allData = this.data.getData();
					if(allData != undefined){
						experiment.atomicTransfertMethods = []; // to manage remove
						//first reinitialise atomicTransfertMethod
						for(var i=0;i<allData.length;i++){
							var atomicIndex = allData[i].atomicIndex;								
							experiment.atomicTransfertMethods[atomicIndex] = allData[i].atomicTransfertMethod
							experiment.atomicTransfertMethods[atomicIndex].inputContainerUseds = new Array(0);
							experiment.atomicTransfertMethods[atomicIndex].outputContainerUseds = new Array(0);
							
							//oneTo
							var inputContainerUsed = allData[i].inputContainerUsed;
							$commonATM.removeNullProperties(inputContainerUsed.instrumentProperties);
							$commonATM.removeNullProperties(inputContainerUsed.experimentProperties);
							experiment.atomicTransfertMethods[atomicIndex].inputContainerUseds.push(inputContainerUsed);	
							
						}
						//ToMany
						for(var i=0;i<allData.length;i++){
							var atomicIndex = allData[i].atomicIndex;								
							
							var outputContainerUsed = allData[i].outputContainerUsed;
							$commonATM.removeNullProperties(outputContainerUsed.instrumentProperties);
							$commonATM.removeNullProperties(outputContainerUsed.experimentProperties);
							experiment.atomicTransfertMethods[atomicIndex].outputContainerUseds.push(outputContainerUsed);
							
	
						}
						//remove atomic null
						var cleanAtomicTransfertMethods = [];
						for(var i = 0; i < experiment.atomicTransfertMethods.length ; i++){
							if(experiment.atomicTransfertMethods[i] !== null){
								cleanAtomicTransfertMethods.push(experiment.atomicTransfertMethods[i]);
							}
						}
						experiment.atomicTransfertMethods = $filter('orderBy')(cleanAtomicTransfertMethods,["inputContainerUseds[0].code"]);
					}								
				},
				viewToExperimentManyToOne :function(experimentIn){		
					if(null === experimentIn || undefined === experimentIn){
						throw 'experiment is required';
					}
					experiment = experimentIn;
					var allData = this.data.getData();
					if(allData != undefined){
						experiment.atomicTransfertMethods = []; // to manage remove
						//first reinitialise atomicTransfertMethod
						for(var i=0;i<allData.length;i++){
							var atomicIndex = allData[i].atomicIndex;								
							experiment.atomicTransfertMethods[atomicIndex] = allData[i].atomicTransfertMethod
							experiment.atomicTransfertMethods[atomicIndex].inputContainerUseds = new Array(0);
							experiment.atomicTransfertMethods[atomicIndex].outputContainerUseds = new Array(0);
							
							//ToOne
							var outputContainerUsed = allData[i].outputContainerUsed;
							$commonATM.removeNullProperties(outputContainerUsed.instrumentProperties);
							$commonATM.removeNullProperties(outputContainerUsed.experimentProperties);
							experiment.atomicTransfertMethods[atomicIndex].outputContainerUseds.push(outputContainerUsed);	
							
						}
						//ManyTo
						for(var i=0;i<allData.length;i++){
							var atomicIndex = allData[i].atomicIndex;								
							
							var inputContainerUsed = allData[i].inputContainerUsed;
							$commonATM.removeNullProperties(inputContainerUsed.instrumentProperties);
							$commonATM.removeNullProperties(inputContainerUsed.experimentProperties);
							experiment.atomicTransfertMethods[atomicIndex].inputContainerUseds.push(inputContainerUsed);
							
	
						}
						//remove atomic null
						var cleanAtomicTransfertMethods = [];
						for(var i = 0; i < experiment.atomicTransfertMethods.length ; i++){
							if(experiment.atomicTransfertMethods[i] !== null){
								cleanAtomicTransfertMethods.push(experiment.atomicTransfertMethods[i]);
							}
						}
						experiment.atomicTransfertMethods = cleanAtomicTransfertMethods;
					}								
				},
				/* NGL-2371 FDS 07/03/2019 mise en commun (existait dans plusieurs experiences CNG avec des variantes...)
				                  n'a de sens que si outputContainer ne sont PAS des tubes (96-well-plate, strip-8)
				                  ajout parametre, "pos" si 'auto '=> positions sur outputContainer= positions sur inputContainer  (n'as de sens que si plaque en entree et 1 seule plaque !!!)
				                                         si 'userdef' => c'est l'utilisateur qui détermine par ses choix les positions sur outputContainer de type plaque
				                                         si 'chip'    => c'est l'utilisateur qui détermine par ses choix les positions sur outputContainer de type "chip"(=strip)
				*/
				copyContainerSupportCodeAndStorageCodeToDT : function(datatable, pos){
					if (pos !=='auto' && pos !=='userdef'&& pos !=='chip' ){
						throw 'pos param must be "auto","userdef" or "chip"';
					}
					if($scope.experiment.instrument.outContainerSupportCategoryCode == "tube"){ 
						throw 'output support category code must not be "tube"';
					}
					var dataMain = datatable.getData();
					var outputContainerSupportCode = $scope.outputContainerSupport.code;
					var outputContainerSupportStorageCode = $scope.outputContainerSupport.storageCode;
				
					for(var i = 0; i < dataMain.length; i++){
						//NGL-2371 si le champ scope.outputContainerSupport.code est vidé par l'utilisateur (il supprime ce qu'il a tapé)
						//il contient alors une chaine vide et pas null ce qui genere des erreurs:
						//     ->  atomictransfertmethods[1].outputContainerUseds[1].locationOnContainerSupport.code : Propriété obligatoire
						// => ajouter   && "" != outputContainerSupportCode
						var atm = dataMain[i].atomicTransfertMethod;
						
						if ( null != outputContainerSupportCode && undefined != outputContainerSupportCode && "" != outputContainerSupportCode ){
							if (pos == 'auto'){
								//calcul automatique du code container
								var newContainerCode = outputContainerSupportCode+"_"+atm.line + atm.column;
								$parse('outputContainerUsed.code').assign(dataMain[i],newContainerCode);
								
							} else if (pos == 'chip' ){
								var newChipPos =$parse("inputContainerUsed.instrumentProperties.chipPosition.value")(dataMain[i]);
								console.log("data :"+ i + "=> new chip position =" + newChipPos);
								
								if ( null != newChipPos){
									// creation du code du container
									var newContainerCode = outputContainerSupportCode+"_"+newChipPos ;
									$parse('outputContainerUsed.code').assign(dataMain[i],newContainerCode);
									
									// NGL-2551...manquait ces lignes
									//POUR CHIP faire qd meme l'assignation column !!!!!!!
									$parse('outputContainerUsed.locationOnContainerSupport.column').assign(dataMain[i],newChipPos);
									
									// Historique mais continuer a renseigner car effets de bord possible ????
									//inutile car forcé a 1 a l'init... $parse('line').assign(atm,1);
									$parse('column').assign(atm,newChipPos);
									
								}
							} 
							// else if userdef => ne rien faire
							
							$parse('outputContainerUsed.locationOnContainerSupport.code').assign(dataMain[i],outputContainerSupportCode);
						} /*else {
							// si l'utilisateur a effacé le SupportCode=> annuler dans outputContainerUsed
							$parse('outputContainerUsed.locationOnContainerSupport.code').assign(dataMain[i],null);
							// et aussi le code pour rester coherent !!
							$parse('outputContainerUsed.code').assign(dataMain[i],null);
						
							/// POUR CHIP faire qd meme l'assignation column !!!!!!!
							if (pos == 'chip' ){
								var newChipPos =$parse("inputContainerUsed.instrumentProperties.chipPosition.value")(dataMain[i]);
								//inutile car forcé a 1 a l'init... $parse('outputContainerUsed.locationOnContainerSupport.line').assign(dataMain[i],1);
								$parse('outputContainerUsed.locationOnContainerSupport.column').assign(dataMain[i],newChipPos);
							
								// Historique mais continuer a renseigner car effets de bord possible ????
								//inutile car forcé a 1 a l'init... $parse('line').assign(atm,1);
								$parse('column').assign(atm,newChipPos);
							}
						}*/
							
						if( null != outputContainerSupportStorageCode && undefined != outputContainerSupportStorageCode && ""!=outputContainerSupportStorageCode ){
							$parse('outputContainerUsed.locationOnContainerSupport.storageCode').assign(dataMain[i],outputContainerSupportStorageCode);
						}/* else {
							//si l'utilisateur a effacé le storageCode=> annuler dans outputContainerUsed
							$parse('outputContainerUsed.locationOnContainerSupport.storageCode').assign(dataMain[i],null);
						}*/
					}
				},
				// FDS 06/05/2021 mise en commun ( avec nom plus explicite que simplement updateATM )
				updateATMLineAndColum : function(experiment){
					if(experiment.instrument.outContainerSupportCategoryCode !== "tube"){
						experiment.atomicTransfertMethods.forEach(function(atm){
							atm.line = atm.outputContainerUseds[0].locationOnContainerSupport.line;
							atm.column = atm.outputContainerUseds[0].locationOnContainerSupport.column;
						});
					}
				},
				//FDS 26/03/2019 NGL-2487 
				// marche ici mais est-ce le bon endroit pour ca ???
				emptyToNull: function(data, property ){
					if ($parse(property)(data) === ""){ $parse(property).assign(data,null);}
				}
		};
		return view;		
	};
	return constructor;
	
}]).factory('atmToDragNDrop', ['$http', '$parse', '$q', 'commonAtomicTransfertMethod','mainService', 'atmToSingleDatatable', 
                               function($http, $parse, $q, commonAtomicTransfertMethod, mainService, atmToSingleDatatable){	
	
	var constructor = function($scope, nbATM, datatableConfig){
		var $commonATM = commonAtomicTransfertMethod($scope);
		var $nbATM = nbATM;	
		var $atmToSingleDatatable = atmToSingleDatatable($scope, datatableConfig);
		$atmToSingleDatatable.isAddNew = false;
		var view = {
				$commonATM : $commonATM,
				$atmToSingleDatatable:$atmToSingleDatatable,
				defaultOutputUnit:{volume:undefined, concentration:undefined, quantity:undefined},
				defaultOutputValue:{volume:undefined, concentration:undefined, quantity:undefined},
				data : {
					$atmToSingleDatatable : $atmToSingleDatatable,
					inputContainers:[],
					atm : [], 
					datatable : $atmToSingleDatatable.data,
					deleteInputContainer : function(inputContainer){
						this.inputContainers.splice(this.inputContainers.indexOf(inputContainer), 1);
					},
					duplicateInputContainer : function(inputContainer, position){
						this.inputContainers.splice(position+1, 0 , $.extend(true, {}, inputContainer));						
					},
					dropInAllInputContainer : function(atmIndex){
						var percentage = {value:0};
						
						var inputContainerUseds = this.atm[atmIndex].inputContainerUseds.concat(this.inputContainers);
						
						angular.forEach(inputContainerUseds, function(container){
							if(container.percentage !== undefined && container.percentage !== null){
								this.value +=  parseFloat(container.percentage);
							}			
						}, percentage)
						
						
						if(percentage.value != 100){
							var percentageForOneContainer = Math.floor(100000/inputContainerUseds.length)/1000
							
							angular.forEach(inputContainerUseds, function(container){
								container.percentage = percentageForOneContainer;
							}, percentageForOneContainer)
							
						}
						
						this.inputContainers = [];
						this.atm[atmIndex].inputContainerUseds = inputContainerUseds;
						this.updateDatatable();
					},
					dropOutAllInputContainer : function(atmIndex){						
						var inputContainers = this.inputContainers.concat(this.atm[atmIndex].inputContainerUseds);
						this.inputContainers = inputContainers;
						this.atm[atmIndex].inputContainerUseds = [];	
						this.updateDatatable();
					},
					/**
					 * Call by drop directive
					 */
					drop : function(e, data, ngModel, alreadyInTheModel, fromModel){
						if(!alreadyInTheModel){
							$scope.atmService.data.updateDatatable();		
						}
					},
					
					updateDatatable : function(){
						this.$atmToSingleDatatable.convertExperimentATMToDatatable(this.atm);
					},
					updateFromDatatable : function(){
						var experiment = {};
						this.$atmToSingleDatatable.data.save();					
						this.$atmToSingleDatatable.viewToExperimentManyToOne(experiment);
						this.atm = experiment.atomicTransfertMethods;
					}
					
				},
				newAtomicTransfertMethod : function(line, column){
					throw 'newAtomicTransfertMethod not defined in atmToDragNDrop client';
				},
				
				convertExperimentToDnD:function(experimentATMs, experimentStateCode){
					var promises = [];
					
					var atms = experimentATMs;
					
					promises.push($commonATM.loadInputContainerFromAtomicTransfertMethods(atms));					
					
					var $that = this;
	                $q.all(promises).then(function (result) {
						var allData = [];
						var inputContainers, outputContainers;
						if(result[0].input){
							inputContainers = result[0].input;
						}else if(result[1].input){
							inputContainers = result[1].input;
						}
						
						//$that.data.atm = angular.copy(atms);
						$that.data.atm = $.extend(true,[], atms);
						for(var i=0; i< $that.data.atm.length;i++){
							var atm = $that.data.atm[i];
							for(var j=0; j<	atm.inputContainerUseds.length ; j++){
								var inputContainerCode = atm.inputContainerUseds[j].code;
								var inputContainer = inputContainers[inputContainerCode];
								atm.inputContainerUseds[j] = $commonATM.updateInputContainerUsedFromContainer(atm.inputContainerUseds[j], inputContainer, experimentStateCode);
							}
						}
						
						//add new atomic in datatable
						$that.addNewAtomicTransfertMethodsInDnD();
	                });
				},
				
				//exact for ManyToOne not for other
				addNewAtomicTransfertMethodsInDnD : function(){
					if(null != mainService.getBasket() && null != mainService.getBasket().get()){
						$that = this;
						$commonATM.loadInputContainerFromBasket(mainService.getBasket().get())
							.then(function(containers) {								
								var allData = [], i = 0;
								if($that.data.inputContainers !== undefined){
									allData = $that.data.inputContainers;									
								}
								
								angular.forEach(containers, function(container){
									var inputContainerUsed = $commonATM.convertContainerToInputContainerUsed(container);
									allData.push(inputContainerUsed);
								});
								$that.data.inputContainers = allData;	
								
						});
					}
					
					for(var i = this.data.atm.length; i < $nbATM; i++){
						var atm = this.newAtomicTransfertMethod(i+1); //TODO GA Not work for plate to plate
						atm.outputContainerUseds.push($commonATM.newOutputContainerUsed(this.defaultOutputUnit, this.defaultOutputValue, atm.line, atm.column));
						atm.viewIndex=i+1;
						this.data.atm.push(atm);
					}
					
				},
				experimentToView:function(experiment, experimentType){
					if(null === experiment || undefined === experiment){
						throw 'experiment is required';
					}
					if(!$scope.isCreationMode()){
						this.convertExperimentToDnD(experiment.atomicTransfertMethods, experiment.state.code);	
						this.$atmToSingleDatatable.convertExperimentATMToDatatable(experiment.atomicTransfertMethods, experiment.state.code);
					}else{
						this.addNewAtomicTransfertMethodsInDnD();
					}	
					this.$atmToSingleDatatable.addExperimentPropertiesToDatatable(experimentType.propertiesDefinitions);
					
				},
				viewToExperiment :function(experiment){		
					if(null === experiment || undefined === experiment){
						throw 'experiment is required';
					}
					this.$atmToSingleDatatable.data.save();					
					this.$atmToSingleDatatable.viewToExperimentManyToOne(experiment);
					this.data.atm = experiment.atomicTransfertMethods;
				},
				refreshViewFromExperiment:function(experiment){
					if(null === experiment || undefined === experiment){
						throw 'experiment is required';
					}
					this.convertExperimentToDnD(experiment.atomicTransfertMethods);
					this.$atmToSingleDatatable.convertExperimentATMToDatatable(experiment.atomicTransfertMethods, experiment.state.code);
				}
		}
		
		return view;
	};
	return constructor;

}]).factory('atmToDragNDrop2', ['$http', '$parse', '$filter', '$q', 'commonAtomicTransfertMethod','mainService', 'atmToSingleDatatable', 
                               function($http, $parse, $filter, $q, commonAtomicTransfertMethod, mainService, atmToSingleDatatable){	
	
	var constructor = function($scope, nbATM, datatableConfig){
		var $commonATM = commonAtomicTransfertMethod($scope);
		var $nbATM = nbATM;	
		var $atmToSingleDatatable = atmToSingleDatatable($scope, datatableConfig);
		$atmToSingleDatatable.isAddNew = false;
		
		var $utils = {
				convertExperimentToDnD:function($service, experimentATMs, experimentStateCode){
					var promises = [];
					
					var atms = experimentATMs;
					
					promises.push($commonATM.loadInputContainerFromAtomicTransfertMethods(atms));					
					
					var $that = this;
	                $q.all(promises).then(function (result) {
						var allData = [], allSupports = [];
						var inputContainers, outputContainers;
						if(result[0].input){
							inputContainers = result[0].input;							
						}else if(result[1].input){
							inputContainers = result[1].input;
						}
						
						//$that.data.atm = angular.copy(atms);
						$service.data.atm = $.extend(true,[], atms);
						for(var i=0; i< $service.data.atm.length;i++){
							var atm = $service.data.atm[i];
							for(var j=0; j<	atm.inputContainerUseds.length ; j++){
								var inputContainerCode = atm.inputContainerUseds[j].code;
								var inputContainer = inputContainers[inputContainerCode];
								atm.inputContainerUseds[j] = $commonATM.updateInputContainerUsedFromContainer(atm.inputContainerUseds[j], inputContainer, experimentStateCode);								
							}
							if(!$service.data.atmViewOpen[i]){
								$service.data.atmViewOpen[i] = false;
							}
						}
						$service.data.inputContainerSupports = allSupports;							
						//add new atomic in datatable
						$that.addNewAtomicTransfertMethodsInDnD($service);
	                });
				},
				
				//exact for ManyToOne not for other
				addNewAtomicTransfertMethodsInDnD : function($service){
					if(null != mainService.getBasket() && null != mainService.getBasket().get()){
						$that = this;
						$commonATM.loadInputContainerFromBasket(mainService.getBasket().get())
							.then(function(containers) {								
								var allContainers = [], allSupports = [], i = 0;
								if($service.data.inputContainers !== undefined){
									allContainers = $service.data.inputContainers;	
									if($view.inputContainerSupportCategoryCode === '96-well-plate'){
										allSupports = $service.data.inputContainerSupports;	
									}
								}
								
								angular.forEach(containers, function(container){
									var inputContainerUsed = $commonATM.convertContainerToInputContainerUsed(container);
									allContainers.push(inputContainerUsed);
									if($service.inputContainerSupportCategoryCode === '96-well-plate' && !allSupports.includes(container.support.code)){
										allSupports.push(container.support.code);	
									}
								});
								$service.data.inputContainers = allContainers;	
								$service.data.inputContainerSupports = allSupports;
								$service.data.inputContainersByLine = [];
								$service.data.currentSupportCode = undefined;
						});
					}
					
					for(var i = $service.data.atm.length; i < $nbATM; i++){
						this.addNewAtomicTransfertMethod($service, i);						
					}
					
				},
				
				addNewAtomicTransfertMethod : function($service, i){
					var atm = $service.newAtomicTransfertMethod(i+1);
					atm.outputContainerUseds.push($commonATM.newOutputContainerUsed($service.defaultOutputUnit, $service.defaultOutputValue, atm.line, atm.column));
					atm.viewIndex=$service.data.atm.length+1;
					$service.data.atm.push(atm);
				}
				
				
		};
		
		
		var $view = {
				$atmToSingleDatatable : $atmToSingleDatatable, //used in js parent
				inputContainerSupports:[],
				inputContainers:[],
				inputContainersByLine:[],
				currentSupportCode:undefined,
				atm : [], 
				datatable : $atmToSingleDatatable.data,
				atmViewOpen : [],
				isAllATMViewClose : false,
				deleteInputContainer : function(inputContainer){
					this.inputContainers.splice(this.inputContainers.indexOf(inputContainer), 1);
				},
				duplicateInputContainer : function(inputContainer, position){
					this.inputContainers.splice(position+1, 0 , $.extend(true, {}, inputContainer));						
				},
				//deprecated ?
				dropInAllInputContainer : function(atmIndex){
					var percentage = {value:0};
					
					var inputContainerUseds = this.atm[atmIndex].inputContainerUseds.concat(this.inputContainers);
					
					angular.forEach(inputContainerUseds, function(container){
						if(container.percentage !== undefined && container.percentage !== null){
							this.value +=  parseFloat(container.percentage);
						}			
					}, percentage)
					
					
					if(percentage.value != 100){
						var percentageForOneContainer = Math.floor(100000/inputContainerUseds.length)/1000
						
						angular.forEach(inputContainerUseds, function(container){
							container.percentage = percentageForOneContainer;
						}, percentageForOneContainer)
						
					}
					
					this.inputContainers = [];
					this.atm[atmIndex].inputContainerUseds = inputContainerUseds;
					
					if($service.updateOutputConcentration){
						$service.updateOutputConcentration(this.atm[atmIndex]);
					}
					
					
					this.updateDatatable();
				},
				//deprecated ?
				dropOutAllInputContainer : function(atmIndex){						
					var inputContainers = this.inputContainers.concat(this.atm[atmIndex].inputContainerUseds);
					this.inputContainers = inputContainers;
					this.atm[atmIndex].inputContainerUseds = [];	
					this.updateDatatable();
				},
				/**
				 * Call by drop directive
				 */
				drop : function(e, data, ngModel, alreadyInTheModel, fromModel){
					if(!alreadyInTheModel){
						$scope.atmService.data.updateDatatable();		
					}
				},
				
				dropInSelectInputContainer : function(){
					//1 extract selected input container
					var selectedInputContainers = $filter('filter')(this.inputContainers,{_addToOutputContainer:true});
					if(selectedInputContainers.length > 0){
					
						var percentage = {value:0};
						
						//2 new atm
						var atmIndex = this.atm.length;
						$utils.addNewAtomicTransfertMethod($service,atmIndex);
						
						//3 compute percentage
						angular.forEach(selectedInputContainers, function(container){							
							if(container.percentage !== undefined && container.percentage !== null){
								this.value +=  parseFloat(container.percentage);
							}			
						}, percentage)
						
						if(percentage.value != 100){
							var percentageForOneContainer = Math.floor(100000/selectedInputContainers.length)/1000
							
							angular.forEach(selectedInputContainers, function(container){
								container.percentage = percentageForOneContainer;
							}, percentageForOneContainer)
							
						}
						
						//4 assign to atm
						this.atm[atmIndex].inputContainerUseds = selectedInputContainers;
						this.atmViewOpen[atmIndex] = true;
						
						if($service.updateOutputConcentration){
							$service.updateOutputConcentration(this.atm[atmIndex]);
						}
						//5 update datatable
						this.updateDatatable();
						
						//6 remove from inputContainers
						var newinputContainers = []
						for(var i = 0 ; i < this.inputContainers.length ; i++){
							if(!this.inputContainers[i]._addToOutputContainer){
								newinputContainers.push(this.inputContainers[i]);
																
							}else{
								this.inputContainers[i]._addToOutputContainer = undefined;
								this.inputContainersByLine[this.inputContainers[i].locationOnContainerSupport.code+"_"+this.inputContainers[i].locationOnContainerSupport.line] = undefined;
							}
						}
						this.inputContainers = newinputContainers;
					}
				},
				
				deleteATM : function(atm){
					this.inputContainers = this.inputContainers.concat(atm.inputContainerUseds);
					
					var rowIndex = this.atm.findIndex(function(_atm){
						return(_atm.viewIndex === atm.viewIndex);
					})
					
					this.atm.splice(rowIndex,1);					
					this.updateDatatable();
					this.inputContainersByLine = [];
					this.atm = $filter('orderBy')(this.atm,'viewIndex');
					this.atm.forEach(function(atm, index){
						atm.viewIndex = index+1;
					});
				},
				
				updateDatatable : function(){
					$atmToSingleDatatable.convertExperimentATMToDatatable(this.atm);
				},
				updateFromDatatable : function(){
					var experiment = {};
					$atmToSingleDatatable.data.save();					
					$atmToSingleDatatable.viewToExperimentManyToOne(experiment);
					this.atm = experiment.atomicTransfertMethods;
				},
				
				getInputContainers : function(supportCode, line, columns){
					if(this.inputContainersByLine[supportCode+"_"+line]){
						return this.inputContainersByLine[supportCode+"_"+line];
					}else if(supportCode !== undefined){
						var finalResults = [];
						var inputContainers = this.inputContainers;
						columns.forEach(function(column){
							var results = $filter('filter')(inputContainers, {locationOnContainerSupport:{code:supportCode, line:line+"", column:column+""}},true);
							if(results.length > 1) throw "several containers for : "+supportCode+", "+line+", "+column;
							this.push(results[0]);
						}, finalResults)
						this.inputContainersByLine[supportCode+"_"+line] = finalResults;
						return finalResults;
					}
					
				},
				
				getCurrentSupportCode : function(){
					if(!this.currentSupportCode){
						this.currentSupportCode = this.inputContainerSupports[0];
					}
					return this.currentSupportCode;
				},
				
				setCurrentSupportCode : function(code){
					if(code){
						this.currentSupportCode = code;
					}					
				},
				
				getPlateBtnClass : function(supportCode){
					if(supportCode === this.currentSupportCode){
						return "btn btn-info btn-lg btn-block";
					}else{
						return "btn btn-default btn-lg btn-block";
					}
					
					
				},
				
				isSelectInputContainers : function(){
					var selectedInputContainers = $filter('filter')(this.inputContainers,{_addToOutputContainer:true});
					return (selectedInputContainers.length > 0);
				},				
				
				selectInputContainers : function($event, supportCode, line, column){
					if((this.startSelect && $event.type === 'mouseenter') || $event.type === 'mousedown'){
						var results = $filter('filter')(this.inputContainers, {locationOnContainerSupport:{code:supportCode, 
								line:(line)?line+"":undefined, column:(column)?column+"":undefined}},true);
						
						var b = results[0]._addToOutputContainer;
						
						results.forEach(function(container){
							container._addToOutputContainer = !b;
						})
						if($event && $event.type !== 'mousedown'){
							$event.preventDefault();
							$event.stopPropagation();
						}
					}
				},
				
				selectInputContainer : function($event, container){
					if(container !== undefined && this.startSelect){
						container._addToOutputContainer = !container._addToOutputContainer;
						
						if($event){
							$event.preventDefault();
							$event.stopPropagation();
						}
					}	
					
				},
				
				startSelectInputContainer : function($event, container){
					this.startSelect = true;
					if(container){
						this.selectInputContainer($event, container)
					}
					if($event){
						$event.preventDefault();
						$event.stopPropagation();
					}
				},
				
				stopSelectInputContainer : function($event){
					this.startSelect = false;
					if($event){
						$event.preventDefault();
						$event.stopPropagation();
					}
				},
				
				getInputContainerCellClass : function(container){
					if(container !== undefined){
						return (container && container._addToOutputContainer)?'info':'';
					}
				},
				
				hideAllATM : function(){
					for (var i=0; i<this.atmViewOpen.length;i++){	
						this.atmViewOpen[i] = false;
					}	    
					this.isAllATMViewClose = true;	    
				},

				showAllATM : function(){
					for (var i=0; i<this.atmViewOpen.length;i++){	
						this.atmViewOpen[i] = true;
					}	    
					this.isAllATMViewClose = false;
				},
				
				toggleATM : function(rowIndex){
					this.atmViewOpen[rowIndex] = !this.atmViewOpen[rowIndex];	
					
					if($filter('filter')(this.atmViewOpen,false).length === this.atmViewOpen.length){
						this.isAllATMViewClose = true;
					}else{
						this.isAllATMViewClose = false;
					}
					
				},
				
				getATMViewMode : function(atm, rowIndex){					
					if(atm.inputContainerUseds.length === 0){
						return "empty";
					}else if(atm.inputContainerUseds.length > 0 && this.atmViewOpen[rowIndex]){
						return "open";
					}else{
						return "compact";
					}		
				}				
			};
		
		var $service = {
				$commonATM : $commonATM, //used in js parent
				$atmToSingleDatatable:$atmToSingleDatatable,  //used in js parent
				defaultOutputUnit:{volume:undefined, concentration:undefined, quantity:undefined},
				defaultOutputValue:{volume:undefined, concentration:undefined, quantity:undefined},
				inputContainerSupportCategoryCode:undefined,
				outputContainerSupportCategoryCode:undefined,
				data : $view,
				
				newAtomicTransfertMethod : function(line, column){
					throw 'newAtomicTransfertMethod not defined in atmToDragNDrop client';
				},
				
				
				experimentToView:function(experiment, experimentType){
					if(null === experiment || undefined === experiment){
						throw 'experiment is required';
					}
					if(!$scope.isCreationMode()){
						this.data.isAllATMViewClose = true;
						$utils.convertExperimentToDnD(this, experiment.atomicTransfertMethods, experiment.state.code);	
						$atmToSingleDatatable.convertExperimentATMToDatatable(experiment.atomicTransfertMethods, experiment.state.code);
					}else{
						this.data.isAllATMViewClose = false;
						$utils.addNewAtomicTransfertMethodsInDnD(this);
					}	
					$atmToSingleDatatable.addExperimentPropertiesToDatatable(experimentType.propertiesDefinitions);
					
				},
				viewToExperiment :function(experiment, saveUDTBefore){		
					if(null === experiment || undefined === experiment){
						throw 'experiment is required';
					}
					if(saveUDTBefore){
						$atmToSingleDatatable.data.save();
						$atmToSingleDatatable.viewToExperimentManyToOne(experiment);
						this.data.atm = experiment.atomicTransfertMethods;
					}else{
						experiment.atomicTransfertMethods = this.data.atm;
					}
					
					
				},
				refreshViewFromExperiment:function(experiment){
					if(null === experiment || undefined === experiment){
						throw 'experiment is required';
					}
					$utils.convertExperimentToDnD(this, experiment.atomicTransfertMethods);
					$atmToSingleDatatable.convertExperimentATMToDatatable(experiment.atomicTransfertMethods, experiment.state.code);
				}
		}
		
		return $service;
	};
	return constructor;

}]).factory('atmToGenerateMany', ['$http', '$parse', '$q', 'commonAtomicTransfertMethod','mainService', 'atmToSingleDatatable', 'datatable', 
                               function($http, $parse, $q, commonAtomicTransfertMethod, mainService, atmToSingleDatatable, datatable){

	var constructor = function($scope, datatableConfigTubeParam, datatableConfigTubeConfig){
		var $commonATM = commonAtomicTransfertMethod($scope);
		datatableConfigTubeParam.formName="experimentDatatableForm";
		var $datatable = datatable(datatableConfigTubeParam);
		var $atmToSingleDatatable = atmToSingleDatatable($scope, datatableConfigTubeConfig);
		$atmToSingleDatatable.isAddNew = false;
		var view = {
				$commonATM : $commonATM,
				$atmToSingleDatatable:$atmToSingleDatatable,
				defaultOutputUnit:{volume:undefined, concentration:undefined, quantity:undefined},
				defaultOutputValue:{volume:undefined, concentration:undefined, quantity:undefined},
				data : {
					$atmToSingleDatatable : $atmToSingleDatatable,
					datatableParam : $datatable,
					atm : [], 
					datatableConfig : $atmToSingleDatatable.data,
					updateDatatable : function(){
						this.$atmToSingleDatatable.convertExperimentATMToDatatable(this.atm);						
					},					
					
				},
				newAtomicTransfertMethod : function(){
					throw 'newAtomicTransfertMethod not defined in atmToGenerateMany client';
				},
				generateATM:function(){
					this.data.datatableParam.save();
					var allData = this.data.datatableParam.getData();
					this.data.atm = [];
					for(var i = 0; i < allData.length; i++){
						var data = allData[i];
						var atm = this.newAtomicTransfertMethod();
						atm.inputContainerUseds.push($commonATM.convertContainerToInputContainerUsed(data.inputContainer));
						
						for(var j = 0; j < data.outputNumber ; j++){
							atm.outputContainerUseds.push($commonATM.newOutputContainerUsed(this.defaultOutputUnit,this.defaultOutputValue,atm.line,atm.column, data.inputContainer));
						}
						this.data.atm.push(atm);
					}
					this.data.updateDatatable();					
				},
				convertExperimentToData:function(experimentATMs){
					var promises = [];
					
					var atms = experimentATMs;
					var $that = this;
					$commonATM.loadInputContainerFromAtomicTransfertMethods(atms).then(function (result) {
						var allData = [];
						var inputContainers = result.input;
					
						//$that.data.atm = angular.copy(atms);
						$that.data.atm = $.extend(true,[], atms);
						var allData = []
						for(var i=0; i< $that.data.atm.length;i++){
							var atm = $that.data.atm[i];
							var inputContainerCode = atm.inputContainerUseds[0].code;
							var inputContainer = inputContainers[inputContainerCode];
							allData.push({inputContainer:inputContainer, outputNumber:atm.outputContainerUseds.length});
						}
						$that.data.datatableParam.setData(allData, allData.length);
						//add new atomic in datatable
						$that.addNewAtomicTransfertMethodsInData();
	                });
				},
				
				//exact for ManyToOne
				addNewAtomicTransfertMethodsInData : function(){
					if(null != mainService.getBasket() && null != mainService.getBasket().get()){
						$that = this;
						$commonATM.loadInputContainerFromBasket(mainService.getBasket().get())
							.then(function(containers) {								
								var allData = [];
								if($that.data.datatableParam.getData() !== undefined){
									allData = $that.data.datatableParam.getData();									
								}
								
								angular.forEach(containers, function(container){									
									allData.push({inputContainer:container, outputNumber:undefined});
								});
								$that.data.datatableParam.setData(allData);									
						});
					}										
				},
				experimentToView:function(experiment, experimentType){
					if(null === experiment || undefined === experiment){
						throw 'experiment is required';
					}
					if(!$scope.isCreationMode()){
						this.convertExperimentToData(experiment.atomicTransfertMethods);	
						this.$atmToSingleDatatable.convertExperimentATMToDatatable(experiment.atomicTransfertMethods, experiment.state.code);
					}else{
						this.addNewAtomicTransfertMethodsInData();
					}	
					this.$atmToSingleDatatable.addExperimentPropertiesToDatatable(experimentType.propertiesDefinitions);					
				},
				viewToExperiment :function(experiment){		
					if(null === experiment || undefined === experiment){
						throw 'experiment is required';
					}
					this.$atmToSingleDatatable.data.save();					
					this.$atmToSingleDatatable.viewToExperimentOneToMany(experiment);					
				},
				refreshViewFromExperiment:function(experiment){
					if(null === experiment || undefined === experiment){
						throw 'experiment is required';
					}					
					this.convertExperimentToData(experiment.atomicTransfertMethods);
					this.$atmToSingleDatatable.convertExperimentATMToDatatable(experiment.atomicTransfertMethods, experiment.state.code);
				}
		}
		
		return view;
	};
	return constructor;
	
}]);
