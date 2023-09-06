angular.module('home').controller('DetailsCtrl',['$rootScope', '$scope','$sce', '$window','$http','$parse','$q','$position','$routeParams','$location','$filter',
                                                 'mainService','tabService','lists','datatable', 'messages','valuationService',
                                                  function($rootScope, $scope,$sce,$window, $http,$parse,$q,$position,$routeParams,$location,$filter,
                                                		  mainService,tabService,lists,datatable, messages,valuationService) {
	var enableValidation = false;
	$scope.getHasErrorClass = function(formName, property){
		if(enableValidation
			&& $scope[formName] 
			&& $scope[formName][property] 
			&& $scope[formName][property].$error 
			&& $scope[formName][property].$error.required){
			return 'has-error';
		}else{
			return undefined;
		}
	}
	/*
	$scope.isRequired=function(propertyDefinition){
		if($scope.experiment !== undefined){
		if(propertyDefinition.required 
				&& (
					($scope.isCreationMode() && $parse('experiment.state.code')($scope) === "N"  
							&& propertyDefinition.requiredState === 'N')
					|| (!$scope.isCreationMode() && $parse('experiment.state.code')($scope) === "N"  
							&& (propertyDefinition.requiredState === null || propertyDefinition.requiredState === 'N' || propertyDefinition.requiredState === 'IP'))
					|| (($parse('experiment.state.code')($scope) === "IP" || $parse('experiment.state.code')($scope) === "F"))
				))
				return true;
		}
		return false;
	};
	*/
	$scope.isRequired=function(requiredState){
		if($scope.experiment !== undefined){
			if(($scope.isCreationMode() && $parse('experiment.state.code')($scope) === "N"  
								&& requiredState === 'N')
						|| (!$scope.isCreationMode() && $parse('experiment.state.code')($scope) === "N"  
								&& (requiredState === null || requiredState === undefined || requiredState === 'N' || requiredState === 'IP'))
						|| (($parse('experiment.state.code')($scope) === "IP" || $parse('experiment.state.code')($scope) === "F"))
					){
				return true;	
			}				
		}
		return false;
	};
	
	
	$scope.isRequiredForState=function(stateCode){
		return (($scope.isCreationMode() && $parse('experiment.state.code')($scope) === "N"  && stateCode === 'N')
				|| (!$scope.isCreationMode() && $parse('experiment.state.code')($scope) === "N"  && (stateCode === 'N' || stateCode === 'IP'))
				|| ($parse('experiment.state.code')($scope) !== "N" && stateCode !== 'N'));
		
	};
	
	$scope.updateInstrumentProperty = function(pName){
		$scope.$broadcast('updateInstrumentProperty', pName);
	};
	
	$scope.updateExperimentProperty = function(pName){
		$scope.$broadcast('updateExperimentProperty', pName);
	};
	
	// FDS 28/08/2017
	$scope.countInputSupportCodes = function(){
		  // !! en mode creation $scope.experiment.atomicTransfertMethod n'est pas encore chargé=> passer par Basket
		  var supportCode = [];
		  if(!$scope.isCreationMode()){
			var supportCode = $scope.$eval("atomicTransfertMethods|flatArray:'inputContainerUseds'|getArray:'locationOnContainerSupport.code'|unique",$scope.experiment);			
		  }else{
			var supportCode = $scope.$eval("getBasket().get()|getArray:'support.code'|unique", mainService);
		  }
		  return supportCode.length;
	};
	
	// FDS 27/07/2018 NGL-2164
	// verifier que l'instrument en cours d'utilisation a une propriété
	$scope.instrumentHasProperty = function(propertyCode ){
		var itype = mainService.get("instrumentType");
	
		for (i=0 ;i < itype.propertiesDefinitions.length; i++ ){	
			if ( itype.propertiesDefinitions[i].code === propertyCode) { return true;}
		}
		return false;
	}
	
	
	$scope.isCreationMode=function(){
		return creationMode;
	};
	
	$scope.getNbAvailableAdditionnalButtons = function(){
		var nbButtons = 0;
		for(var i = 0; i < additionnalButtons.length; i++){
			if(additionnalButtons[i].isShow()){
				nbButtons++;
			}
		}
		
		return nbButtons;
	};
	$scope.getAdditionnalButtons = function(){
		return additionnalButtons;
	};
	$scope.setAdditionnalButtons = function(buttons){
		additionnalButtons = buttons;
	};

	$scope.getAdditionnalButtonsLabel= function(){
		// default label is :experiments.additionnalButtons
		if ($scope.mainButtonLabel === undefined){ $scope.mainButtonLabel=Messages("experiments.additionnalButtons"); }
		return $scope.mainButtonLabel ;
	};
	$scope.setAdditionnalButtonsLabel= function(buttonLabel){
		$scope.mainButtonLabel = buttonLabel;
	};
	
	/* move to a directive */
	$scope.setImage = function(imageData, imageName, imageFullSizeWidth, imageFullSizeHeight) {
		$scope.modalImage = imageData;

		$scope.modalTitle = imageName;

		var margin = 25;		
		var zoom = Math.min((document.body.clientWidth - margin) / imageFullSizeWidth, 1);

		$scope.modalWidth = imageFullSizeWidth * zoom;
		$scope.modalHeight = imageFullSizeHeight * zoom; // in order to
															// conserve image
															// ratio
		$scope.modalLeft = (document.body.clientWidth - $scope.modalWidth)/2;

		$scope.modalTop = (window.innerHeight - $scope.modalHeight)/2;

		$scope.modalTop = $scope.modalTop - 50; // height of header and footer
	};
	
	$scope.openPrintTagsPage = function(){
		$window.open(jsRoutes.controllers.printing.tpl.Printing.home("tags").url+"?experimentCode="+$scope.experiment.code, 'tags');
	};
	
	$scope.$on('initValuationModalDone', function(e) {
		angular.element('#finalValuationModal').modal('show');
	});
	
	
	
	$scope.isDispatchModalAvailable = function(){
		return $scope.isFinishState();
	};
	
	$scope.initDispatchModal = function(){
		$scope.messages.clear();
		$scope.$broadcast('initDispatchModal');
	};
	
	$scope.$on('initDispatchModalDone', function(e) {
		angular.element('#finalDispatchModal').modal('show');
	});
	
	$scope.$on('dispatchDone', function(e, args) {
		
		if(args.isErrors){
			$scope.messages.setError("save");
			$scope.messages.setDetails(args.errors);
			$scope.messages.showDetails = true;				
		}else{
			$scope.messages.setSuccess("save");
		}
		//First step to have a new experiment after dispatch not fonctionnal
		if(args.newExperiment){
			var creationMode = false;
			var saveInProgress = false;
			var additionnalButtons = [];
			$routeParams.code="new";
			$routeParams.typeCode=args.newExperimentTypeCode;
			
			tabService.resetTabs();			
			tabService.addTabs({label:Messages('experiments.tabs.create'),href:jsRoutes.controllers.experiments.tpl.Experiments.home("new").url,remove:false});
			tabService.activeTab(1);
			init();
		}else{
			updateData();
		}
		
		
		angular.element('#finalDispatchModal').modal('hide');
	});
	
	
	$scope.activeEditMode = function(){
		$scope.messages.clear();
		mainService.startEditMode();
		
		if(mainService.isHomePage('search') 
				&& !tabService.isBackupTabs() 
				&& $scope.isNewState()){
			
			tabService.backupTabs();
			tabService.resetTabs();
			tabService.addTabs({label:Messages('experiments.tabs.create'),href:jsRoutes.controllers.experiments.tpl.Experiments.home("new").url,remove:false});
			tabService.addTabs({label:$scope.experiment.code,href:jsRoutes.controllers.experiments.tpl.Experiments.get($scope.experiment.code).url,remove:true});
			tabService.activeTab(1);
			mainService.setDatatable(undefined);
			
			var form = {};
			form.nextExperimentTypeCode = $scope.experimentType.code;
			form.experimentCategoryCode = $scope.experimentType.category.code;
			form.containerSupportCategory = $parse("instrument.inContainerSupportCategoryCode")($scope.experiment);
			mainService.setForm(form);		
		}
		$scope.$broadcast('activeEditMode');
		
	};
	
	$scope.cancel = function(){
		$scope.$broadcast('cancel');
		$scope.messages.clear();
		mainService.stopEditMode();
		finishEditMode=false;
		saveInProgress = false;
		enableValidation = false;
		updateData();
		
		if(mainService.isHomePage('search') 
				&& tabService.isBackupTabs() ){
			$scope.restoreBackupTabs();
			$scope.activeTab(1);
			$scope.setDatatable(undefined);	
			
			var form = {};
			form.typeCode = $scope.experimentType.code;
			form.categoryCode = $scope.experimentType.category.code;
			
			mainService.setForm(form);								
		}			
	};
	
	
	$scope.isExperimentDeleteAvailable = function(username){
		return ($scope.isWorkflowModeAvailable('F') && (!$scope.isCreationMode() && username === $scope.experiment.traceInformation.createUser || Permissions.check("admin")));
	};
	
	$scope.askDeleteExperiment = function(){
		
		angular.element('#deleteModal').modal('show');
		
	};
	
	$scope.deleteExperiment = function(){
		$scope.messages.clear();
		
		/* NGL-2781 on aimerait un spinner sur la deletion d'expérience pour les cas ou il y a des lenteurs et faire patienter l'utilisateur'
		   mais un spinner ne peut etre défini que sur un datatable: (voir datatable2.js)
			===> '<div class="inProgress" ng-if="dtTable.config.spinner.start"><button class="btn btn-primary btn-lg"><i class="fa fa-spinner fa-spin fa-5x"></i></button></div>'
		*/

		$http.delete(jsRoutes.controllers.experiments.api.Experiments.delete($scope.experiment.code).url)
			.success(function(data, status, headers, config) {
				// ajouter message ici (meme s'il se voit très furtivement en temps normal...')
				$scope.messages.setWarning("Suppression en cours...");
				
				angular.element('#deleteModal').on('hidden.bs.modal', function (e) {
					console.log("call when hide ok");
					$scope.$apply(function(scope){
						tabService.activeTab(tabService.getTab(0),true);
						tabService.removeTab(1);
					});
				});
				angular.element('#deleteModal').modal('hide');
				
			}).error(function(data, status, headers, config) {
				$scope.messages.setError("Echec de suppression");
				$scope.messages.setDetails(data);
				angular.element('#deleteModal').modal('hide');
		});
		
	};
	
	$scope.finishExperimentModals = function(){
		var step2 = false;
		var step3 = false;
		saveInProgress = true;
		if($scope.experiment.categoryCode === "qualitycontrol"){
			var atms = $filter('filter')($scope.experiment.atomicTransfertMethods,{inputContainerUseds:{copyValuationToInput:'UNSET'}});
			if(atms.length > 0){
				saveInProgress = false;
				$scope.$broadcast('initValuationModal');				
			}else{
				angular.element('#finalValuationModal').modal('hide');
				step2 = true;
			}
		}else{
			step2 = true;			
		}
		
		if(step2){
			if($scope.experiment.status.valid === 'UNSET'){
				saveInProgress = false;
				angular.element('#finalResolutionModal').modal('show');				
			}else{
				angular.element('#finalResolutionModal').modal('hide');
				step3 = true;
			}
		}
		
		if(step3){
			var endSaveSuccessCallbackFunction = function(experiment){
				mainService.put("experiment",$scope.experiment);
				$scope.experiment = experiment;
				
				var state =  angular.copy($scope.experiment.state);
				state.code = "F";
				$http.put(jsRoutes.controllers.experiments.api.Experiments.updateState(experiment.code).url, state)
				.success(function(data, status, headers, config) {
					endSaveSuccess(data);
					$scope.initDispatchModal();
				})
				.error(function(data, status, headers, config) {				
					$scope.messages.setError("save");
					$scope.messages.setDetails(data);				
					saveInProgress = false;	
					if(mainService.isEditMode()){
						$scope.$broadcast('activeEditMode');
					}
				});		
			};
			saveOnRemote({endSaveSuccessCallbackFunction:endSaveSuccessCallbackFunction});
		}		
		
	};
	
	$scope.save = function(endSaveSuccessCallbackFunction, endSaveChildCallbackFunction){
		$scope.messages.clear();
		saveInProgress = true;
		
		var callbackFunctions = {
				endSaveChildCallbackFunction : endSaveChildCallbackFunction,
				endSaveSuccessCallbackFunction : endSaveSuccessCallbackFunction
		};
		
		if($scope.experiment.state.code === 'F' && !Permissions.check("admin")){
			callbackFunctions.updateURL ='?fields=status&fields=reagents&fields=state';
		}
		
		$scope.$broadcast('saveReagents', callbackFunctions);		
	};
	$scope.askSaveAdmin = function(){
		
		angular.element('#saveAdminModal').modal('show');
		
	};
	$scope.saveAdmin = function(){
		if(Permissions.check("admin")){
			$scope.messages.clear();
			saveInProgress = true;
			
			var callbackFunctions = {
					
			};
			
			if($scope.experiment.state.code === 'F'){
				callbackFunctions.updateURL ='?fields=all&&fields=updateContentProperties';
			}			
			
			$scope.$broadcast('saveReagents', callbackFunctions);
			angular.element('#saveAdminModal').modal('hide');
		}			
	};
	
	$scope.startExperiment = function(){
		
		var endSaveSuccessCallbackFunction = function(experiment){
			
			mainService.put("experiment",$scope.experiment);
			$scope.experiment = experiment;
			var state = angular.copy($scope.experiment.state);
			state.code = "IP";
			$http.put(jsRoutes.controllers.experiments.api.Experiments.updateState(experiment.code).url, state)
			.success(function(data, status, headers, config) {
				endSaveSuccess(data);															
			})
			.error(function(data, status, headers, config) {
				$scope.messages.setError("save");
				$scope.messages.setDetails(data);				
				saveInProgress = false;
				if(mainService.isEditMode()){
					$scope.$broadcast('activeEditMode');
				}
			});
		};
		$scope.save(endSaveSuccessCallbackFunction);
	};
	
	
	$scope.finishExperiment = function(){
		var endSaveChildCallbackFunction = $scope.finishExperimentModals;
		$scope.$broadcast('finishExperiment'); 
		$scope.save(null, endSaveChildCallbackFunction);					
	};
	
	$scope.$on('reagentsSaved', function(e, callbackFunctions) {
		$scope.$broadcast('save', callbackFunctions);
	});
	
	
	var endSaveSuccess = function(newExperiment){
		resetBasket();					
		mainService.put("experiment",$scope.experiment);
		$scope.experiment = newExperiment;
		$scope.messages.setSuccess("save");						
		mainService.stopEditMode();
		finishEditMode=false;
		saveInProgress = false;
		enableValidation = false;
		
		if(mainService.isHomePage('search') 
				&& tabService.isBackupTabs() ){
			$scope.restoreBackupTabs();
			$scope.activeTab(1);
			$scope.setDatatable(undefined);	
			
			var form = {};
			form.typeCode = $scope.experimentType.code;
			form.categoryCode = $scope.experimentType.category.code;
			
			mainService.setForm(form);								
		}
		
		$scope.$broadcast('refresh'); // utile seulement si l'update fonctionne				
	}
	
	$scope.$on('askRefreshReagents',function(){
		$scope.$broadcast('refreshReagents');
	}); 
	
	// FDS 24/08/2017
	$scope.$on('childSavedError', function(e, callbackFunctions) {
		saveInProgress = false;	
		if(mainService.isEditMode()){
			$scope.$broadcast('activeEditMode');
		}	
	});
	
	$scope.$on('childSaved', function(e, callbackFunctions) {
		
		updatePropertyUnit($scope.experiment);
		
		if($scope.allForm.$invalid){
			enableValidation = true;
			$scope.messages.setError("save");
			saveInProgress = false;	
			if(mainService.isEditMode()){
				$scope.$broadcast('activeEditMode');
			}
		}else if(callbackFunctions.endSaveChildCallbackFunction){
				callbackFunctions.endSaveChildCallbackFunction();
		}else{
			saveOnRemote(callbackFunctions);
		} 
		
	});
	
	var saveOnRemote = function(callbackFunctions){
		if(creationMode){
			$http.post(jsRoutes.controllers.experiments.api.Experiments.save().url, $scope.experiment, {callbackFunction:callbackFunctions.endSaveSuccessCallbackFunction})
				.success(function(data, status, headers, config) {
					
					creationMode = false;
					mainService.setHomePage('search')
					tabService.resetTabs();
					tabService.addTabs({label:Messages('experiments.tabs.search'),href:jsRoutes.controllers.experiments.tpl.Experiments.home("search").url,remove:true});
					tabService.addTabs({label:data.code,href:jsRoutes.controllers.experiments.tpl.Experiments.get(data.code).url,remove:true});
					tabService.activeTab(1);
					
					if(config.callbackFunction){
						config.callbackFunction(data);
					}else{
						endSaveSuccess(data);
					}						
				})
				.error(function(data, status, headers, config) {
					if(isImageTooLarge(status)){
						setImageSizeError();
					} else {
						setSaveError(data);
					}				
					saveInProgress = false;	
					if(mainService.isEditMode()){
						$scope.$broadcast('activeEditMode');
					}
				});
		}else{
			var url = jsRoutes.controllers.experiments.api.Experiments.update($scope.experiment.code).url;	
			if(callbackFunctions.updateURL !== undefined && callbackFunctions.updateURL !== null){
				url +=callbackFunctions.updateURL;
			}
		
			$http.put(url, $scope.experiment, {callbackFunction:callbackFunctions.endSaveSuccessCallbackFunction})
			.success(function(data, status, headers, config) {
				if(config.callbackFunction){
					config.callbackFunction(data);
				}else{
					endSaveSuccess(data);
				}											
			})
			.error(function(data, status, headers, config) {
				if(isImageTooLarge(status)){
					setImageSizeError();
				} else {
					setSaveError(data);
				}
				saveInProgress = false;	
				if(mainService.isEditMode()){
					$scope.$broadcast('activeEditMode');
				}
			});			
		}

		//---

		function isImageTooLarge(status) {
			// connection reset or request entity too large
			return status === -1 || status === 413;
		}

		function setImageSizeError(){
			$scope.messages.setError(Messages("experiments.error.imageSize"));
		}

		function setSaveError(data) {
			$scope.messages.setError("save");
			$scope.messages.setDetails(data);
		}
	};
	
	
	var finishEditMode = false;
	$scope.isFinishEditMode = function(){		
		return finishEditMode;	
	};
	
	$scope.activeFinishEditMode = function(){		
		finishEditMode=true;
		$scope.$broadcast('activeFinishEditMode');
	};
	
	
	$scope.isWorkflowModeAvailable = function(nextStateCode){
		if($scope.experiment !== undefined){
			return ((nextStateCode === 'IP' && $parse('experiment.state.code')($scope) === "N") 
				|| (nextStateCode === 'F' && $parse('experiment.state.code')($scope) !== "F")
					|| (nextStateCode === 'F' && Permissions.check("admin")));
		}else{
			return false;
		}
	};
	
	$scope.isEditModeAvailable = function(){		
		if($scope.experiment !== undefined){
			return ($parse('experiment.state.code')($scope) !== "F" || Permissions.check("admin"));
		}else{
			return false;
		}		
	};
	
	$scope.isNewState = function(){				
		return ($parse('experiment.state.code')($scope) === "N");
	};
	
	$scope.isInProgressState = function(){				
		return ($parse('experiment.state.code')($scope) === "IP");
	};
	
	$scope.isFinishState = function(){				
		return ($parse('experiment.state.code')($scope) === "F");
	};
	
	$scope.isSaveInProgress = function(){
		return saveInProgress;
	};
	
	
	$scope.isOutputATMVoid = function(){
		return ($scope.experimentType.atomicTransfertMethod === "OneToVoid");
	};
	
	$scope.changeInstrumentType = function(){
		$scope.messages.clear();
		if($scope.experiment && $scope.experiment.instrument && $scope.experiment.instrument.typeCode){
			var instrumentTypeCode = $scope.experiment.instrument.typeCode;
			$scope.experiment.instrument = {};
			$scope.experiment.instrumentProperties = undefined;
			$scope.instrumentType = undefined;
			loadInstrumentType(instrumentTypeCode);												
		}else if($scope.experiment){
			$scope.experiment.instrument = {};
			$scope.experiment.instrumentProperties = undefined;
			$scope.instrumentType = undefined;
		}
		$scope.experimentTypeTemplate = undefined;		
	};
	
	$scope.loadTemplate = function(){
		if($scope.experimentType && $scope.experiment.instrument && $scope.experiment.instrument.outContainerSupportCategoryCode){
			$scope.experimentTypeTemplate =  jsRoutes.controllers.experiments.tpl.Experiments.getTemplate($scope.experimentType.atomicTransfertMethod.toLowerCase(),
			$scope.experiment.instrument.outContainerSupportCategoryCode,
			$scope.experimentType.category.code,
			$scope.experimentType.code).url;
		}else{
			$scope.experimentTypeTemplate =  undefined;
		}				
	};
	
	$scope.fileUtils = {
			generateSampleSheet : function(params, msgSuccessKey, msgErrorKey){
				
				if(!msgSuccessKey)msgSuccessKey='experiments.msg.generateSampleSheet.success';
				if(!msgErrorKey)msgErrorKey='experiments.msg.generateSampleSheet.error';
				
				$http.get(jsRoutes.controllers.instruments.io.IO.generateFile($scope.experiment.code).url, {params:params})
				.success(function(data, status, headers, config) {
					var header = headers("Content-disposition");
					var filepath = header.split("filename=")[1];
					var filename = filepath.split(/\/|\\/);
					filename = filename[filename.length-1];
					filename = filename.replace(/\"/g,"");
					if(data!=null){
						$scope.messages.clazz="alert alert-success";
						$scope.messages.text=Messages(msgSuccessKey)+" : "+filepath;
						$scope.messages.showDetails = false;
						$scope.messages.open();	
						
						var blob = new Blob([data], {type: "text/plain;charset=utf-8"});    					
						saveAs(blob, filename);
					}
				})
				.error(function(data, status, headers, config) {
					$scope.messages.clazz = "alert alert-danger";
					$scope.messages.text = Messages(msgErrorKey);
					$scope.messages.setDetails(data);
					$scope.messages.showDetails = true;
					$scope.messages.open();				
				});
			}
	};
	
	$scope.plateUtils = {
			/**
			 * Compute A1, B1, C1, etc.
			 */
			computeColumnMode : function(atmService, udt, maxLine){
				var wells = udt.displayResult;
				var nbCol = 12;
				var nbLine = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
				var x = 0;
				for(var i = 0; i < nbCol ; i++){
					for(var j = 0; j < nbLine.length && j <= maxLine; j++){
						if(x < wells.length && x < 96){
							wells[x].data.outputContainerUsed.locationOnContainerSupport.line = nbLine[j]+'';
							wells[x].data.outputContainerUsed.locationOnContainerSupport.column = i+1;					
						}
						x++;
					}
				}		
			},
			
			/**
			 * Compute A1, A2, A3, etc.
			 */
			computeLineMode : function(atmService, udt, maxColumn){
				var wells = udt.displayResult;
				var nbCol = 12;
				var nbLine = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
				var x = 0;
				for(var j = 0; j < nbLine.length; j++){
					for(var i = 0; i < nbCol && i <= maxColumn; i++){
						if(x < wells.length && x < 96){
							wells[x].data.outputContainerUsed.locationOnContainerSupport.line = nbLine[j]+'';
							wells[x].data.outputContainerUsed.locationOnContainerSupport.column = i+1;					
						}
						x++;
					}
				}		
			},
			copyMode : function(atmService, udt){
				var wells = udt.displayResult;
				for(var j = 0; j < wells.length; j++){
					if(wells[j].data.inputContainerUsed.categoryCode ==='well'){
						wells[j].data.outputContainerUsed.locationOnContainerSupport.line = wells[j].data.inputContainerUsed.locationOnContainerSupport.line;
						wells[j].data.outputContainerUsed.locationOnContainerSupport.column = wells[j].data.inputContainerUsed.locationOnContainerSupport.column;
					}
				}
				
			},
			plateCells : undefined,
			computePlateCells : function(atmService){
				var plateCells = [];
				var wells = atmService.data.displayResult;
				angular.forEach(wells, function(well){
					var containerUsed = undefined;
					if($scope.experimentType.atomicTransfertMethod === 'OneToVoid'){
						containerUsed = well.data.inputContainerUsed;
					}else{
						containerUsed = well.data.outputContainerUsed;
					}
					
					var line = containerUsed.locationOnContainerSupport.line;
					var column = containerUsed.locationOnContainerSupport.column;
					if(line && column){
						if(plateCells[line] == undefined){
							plateCells[line] = [];
						}
						var sampleCodeAndTags = [];
						angular.forEach(containerUsed.contents, function(content){
							var value = content.projectCode+" / "+content.sampleCode;
							
							if(content.properties && content.properties.libProcessTypeCode){
								value = value +" / "+content.properties.libProcessTypeCode.value;
							}
							
							if(content.properties && content.properties.tag){
								value = value +" / "+content.properties.tag.value;
							}
							if(content.properties && content.properties.secondaryTag){
								value = value +" / "+content.properties.secondaryTag.value;
							}
							
							sampleCodeAndTags.push(value);
						});
						plateCells[line][column] = sampleCodeAndTags;
						
					}						
				})	
				this.plateCells = plateCells;
			},
			isPlate:function(){
				if($scope.experimentType.atomicTransfertMethod === 'OneToVoid'){
					return ($scope.experiment.instrument.inContainerSupportCategoryCode === '96-well-plate');	
				}else{
					return ($scope.experiment.instrument.outContainerSupportCategoryCode === '96-well-plate');
				}								
			},
			isPlateToPlate:function(){
				return ($scope.experiment.instrument.inContainerSupportCategoryCode === '96-well-plate' 
						&& $scope.experiment.instrument.outContainerSupportCategoryCode === '96-well-plate');								
			},
			getPlateCode:function(){
				if($scope.experimentType.atomicTransfertMethod === 'OneToVoid'){
					if ($scope.experiment.inputContainerSupportCodes)
						return $scope.experiment.inputContainerSupportCodes[0];
				}else if($scope.experiment.outputContainerSupportCodes){
					return $scope.experiment.outputContainerSupportCodes[0];
				}

			},
			/**
			 * Info on plate design
			 */
			getCellPlateData : function(line, column){
				if(this.plateCells && this.plateCells[line] && this.plateCells[line][column]){
					return this.plateCells[line][column];
				}
			},
			templates : {
				buttonLineMode : function(udtName){
					if(!udtName)udtName='atmService.data';
					return ''
					+'<div class="btn-group" style="margin-left:5px" ng-if="plateUtils.isPlate()">'
	            	+'<button class="btn btn-default" ng-click="plateUtils.computeLineMode(atmService, '+udtName+', 11)" data-toggle="tooltip" title="'+Messages("experiments.button.plate.computeLineMode")+'"  ng-disabled="!isEditMode()"><i class="fa fa-magic"></i><i class="fa fa-arrow-right"></i></button>'
	            	+'<div class="btn-group" role="group">'
	            	+'<button type="button" title="'+Messages("experiments.button.plate.computeLineMode.advanced")+'" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false" ng-disabled="!isEditMode()">'
	            	+'  <span class="caret"></span>'
	            	+'</button>'
	            	+' <ul class="dropdown-menu">'
	            	+'  <li><a href="#" ng-click="plateUtils.computeLineMode(atmService, '+udtName+', 0)" >1</a></li>'
	            	+'  <li><a href="#" ng-click="plateUtils.computeLineMode(atmService, '+udtName+', 1)" >2</a></li>'
	            	+'  <li><a href="#" ng-click="plateUtils.computeLineMode(atmService, '+udtName+', 2)" >3</a></li>'
	            	+'  <li><a href="#" ng-click="plateUtils.computeLineMode(atmService, '+udtName+', 3)" >4</a></li>'
	            	+'  <li><a href="#" ng-click="plateUtils.computeLineMode(atmService, '+udtName+', 4)" >5</a></li>'
	            	+'  <li><a href="#" ng-click="plateUtils.computeLineMode(atmService, '+udtName+', 5)" >6</a></li>'
	            	+'  <li><a href="#" ng-click="plateUtils.computeLineMode(atmService, '+udtName+', 6)" >7</a></li>'
	            	+'  <li><a href="#" ng-click="plateUtils.computeLineMode(atmService, '+udtName+', 7)" >8</a></li>'
	            	+'  <li><a href="#" ng-click="plateUtils.computeLineMode(atmService, '+udtName+', 8)" >9</a></li>'
	            	+'  <li><a href="#" ng-click="plateUtils.computeLineMode(atmService, '+udtName+', 9)" >10</a></li>'
	            	+'  <li><a href="#" ng-click="plateUtils.computeLineMode(atmService, '+udtName+', 10)" >11</a></li>'
	            	+'  <li><a href="#" ng-click="plateUtils.computeLineMode(atmService, '+udtName+', 11)" >12</a></li>'
	            	+'</ul>'
	            	+'</div>'
	            	+'</div>'
	            },
				buttonColumnMode : function(udtName){
					if(!udtName)udtName='atmService.data';
					return ''
					+'<div class="btn-group" ng-if="plateUtils.isPlate()">'
	            	+'<button class="btn btn-default" ng-click="plateUtils.computeColumnMode(atmService, '+udtName+', 7)" data-toggle="tooltip" title="'+Messages("experiments.button.plate.computeColumnMode")+'" ng-disabled="!isEditMode()"><i class="fa fa-magic"></i><i class="fa fa-arrow-down"></i></button>'
	            	+'<div class="btn-group" role="group">'
	            	+'<button type="button"  title="'+Messages("experiments.button.plate.computeColumnMode.advanced")+'" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false" ng-disabled="!isEditMode()">'
	            	+'  <span class="caret"></span>'
	            	+'</button>'
	            	+' <ul class="dropdown-menu">'
	            	+'  <li><a href="#" ng-click="plateUtils.computeColumnMode(atmService, '+udtName+', 0)" >A</a></li>'
	            	+'  <li><a href="#" ng-click="plateUtils.computeColumnMode(atmService, '+udtName+', 1)" >B</a></li>'
	            	+'  <li><a href="#" ng-click="plateUtils.computeColumnMode(atmService, '+udtName+', 2)" >C</a></li>'
	            	+'  <li><a href="#" ng-click="plateUtils.computeColumnMode(atmService, '+udtName+', 3)" >D</a></li>'
	            	+'  <li><a href="#" ng-click="plateUtils.computeColumnMode(atmService, '+udtName+', 4)" >E</a></li>'
	            	+'  <li><a href="#" ng-click="plateUtils.computeColumnMode(atmService, '+udtName+', 5)" >F</a></li>'
	            	+'  <li><a href="#" ng-click="plateUtils.computeColumnMode(atmService, '+udtName+', 6)" >G</a></li>'
	            	+'  <li><a href="#" ng-click="plateUtils.computeColumnMode(atmService, '+udtName+', 7)" >H</a></li>'
	            	+'</ul>'
	            	+'</div>'
	            	+'</div>'
	            },
	            buttonCopyPosition : function(udtName){
	            	if(!udtName)udtName='atmService.data';
	            	return ''
	            	+'<div class="btn-group" ng-if="plateUtils.isPlate()">'
	            	+'<button class="btn btn-default" ng-click="plateUtils.copyMode(atmService, '+udtName+')" data-toggle="tooltip" title="'+Messages("experiments.button.plate.copyMode.title")+'" ng-disabled="!isEditMode()">'
	            	+'	<i class="fa fa-copy"></i> '+Messages("experiments.button.plate.copyMode")
	            	+'</button>'
	            	+'</div>'
	            }
			}
	};
	
	$scope.plateFluoUtils = {
			
			getSupportCategoryCode:function(){
				var tmp = [];
				if(!$scope.isCreationMode()){
					tmp = $scope.$eval("atomicTransfertMethods|flatArray:'inputContainerUseds'|getArray:'locationOnContainerSupport.categoryCode'|unique",$scope.experiment);			
				}else{
					tmp = $scope.$eval("getBasket().get()|getArray:'support.categoryCode'|unique",mainService);
				}
				var supportCategoryCode = undefined;
				if(tmp.length === 1){
					this.supportCategoryCode=tmp[0];
				}else{
					this.supportCategoryCode="mixte";
				}
				return this.supportCategoryCode;
				
			},
			isPlate:function(){
				category = this.getSupportCategoryCode();
				return (category === '96-well-plate')
										
			},
			/**
			 * Compute A1, B1, C1, etc.
			 */
			computeColumnMode : function(atmService, udt, maxLine){
				var wells = udt.displayResult;
				var nbCol = 12;
				var nbLine = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
				var x = 0;
				for(var i = 0; i < nbCol ; i++){
					for(var j = 0; j < nbLine.length && j <= maxLine; j++){
						if(x < wells.length && x < 96){
							//init instrumentProperties if undefined
							if(wells[x].data.inputContainerUsed.instrumentProperties==undefined)
								wells[x].data.inputContainerUsed.instrumentProperties={};
							if(wells[x].data.inputContainerUsed.instrumentProperties.fluoroskanLine==undefined)
								wells[x].data.inputContainerUsed.instrumentProperties.fluoroskanLine={};
							if(wells[x].data.inputContainerUsed.instrumentProperties.fluoroskanColumn==undefined)
								wells[x].data.inputContainerUsed.instrumentProperties.fluoroskanColumn={};
							
							wells[x].data.inputContainerUsed.instrumentProperties.fluoroskanLine.value = nbLine[j]+'';
							wells[x].data.inputContainerUsed.instrumentProperties.fluoroskanColumn.value = i+1;					
						}
						x++;
					}
				}		
			},
			
			/**
			 * Compute A1, A2, A3, etc.
			 */
			computeLineMode : function(atmService, udt, maxColumn){
				var wells = udt.displayResult;
				var nbCol = 12;
				var nbLine = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
				var x = 0;
				for(var j = 0; j < nbLine.length; j++){
					for(var i = 0; i < nbCol && i <= maxColumn; i++){
						if(x < wells.length && x < 96){
							if(wells[x].data.inputContainerUsed.instrumentProperties==undefined)
								wells[x].data.inputContainerUsed.instrumentProperties={};
							if(wells[x].data.inputContainerUsed.instrumentProperties.fluoroskanLine==undefined)
								wells[x].data.inputContainerUsed.instrumentProperties.fluoroskanLine={};
							if(wells[x].data.inputContainerUsed.instrumentProperties.fluoroskanColumn==undefined)
								wells[x].data.inputContainerUsed.instrumentProperties.fluoroskanColumn={};
							
							wells[x].data.inputContainerUsed.instrumentProperties.fluoroskanLine.value = nbLine[j]+'';
							wells[x].data.inputContainerUsed.instrumentProperties.fluoroskanColumn.value = i+1;					
						}
						x++;
					}
				}		
			},
			
	        plateCells : undefined,
			computePlateCells : function(atmService){
					var plateCells = [];
					var wells = atmService.data.displayResult;
					angular.forEach(wells, function(well){
						var containerUsed = well.data.inputContainerUsed;
						
						if(containerUsed.instrumentProperties!=undefined && containerUsed.instrumentProperties.fluoroskanLine!=undefined && containerUsed.instrumentProperties.fluoroskanColumn!=undefined){
							var line = containerUsed.instrumentProperties.fluoroskanLine.value;
							var column = containerUsed.instrumentProperties.fluoroskanColumn.value;
							if(line && column){
								if(plateCells[line] == undefined){
									plateCells[line] = [];
								}
								var sampleCodeAndTags = [];
								angular.forEach(containerUsed.contents, function(content){
									var value = content.projectCode+" / "+content.sampleCode;
								
									if(content.properties && content.properties.libProcessTypeCode){
										value = value +" / "+content.properties.libProcessTypeCode.value;
									}
								
									if(content.properties && content.properties.tag){
										value = value +" / "+content.properties.tag.value;
									}
								
									sampleCodeAndTags.push(value);
								});
								plateCells[line][column] = sampleCodeAndTags;
							
							}
						}
					});	
					this.plateCells = plateCells;
			},
			getCellPlateData : function(line, column){
				if(this.plateCells && this.plateCells[line] && this.plateCells[line][column]){
					return this.plateCells[line][column];
				}
			},
			templates : {
				buttonLineMode : function(udtName){
					if(!udtName)udtName='atmService.data';
					return ''
					+'<div class="btn-group" style="margin-left:5px">'
	            	+'<button class="btn btn-default" ng-click="plateFluoUtils.computeLineMode(atmService, '+udtName+', 11)" data-toggle="tooltip" title="'+Messages("experiments.button.plate.computeLineMode")+'"  ng-disabled="!isEditMode()"><i class="fa fa-magic"></i><i class="fa fa-arrow-right"></i></button>'
	            	+'<div class="btn-group" role="group">'
	            	+'<button type="button" title="'+Messages("experiments.button.plate.computeLineMode.advanced")+'" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false" ng-disabled="!isEditMode()">'
	            	+'  <span class="caret"></span>'
	            	+'</button>'
	            	+' <ul class="dropdown-menu">'
	            	+'  <li><a href="#" ng-click="plateFluoUtils.computeLineMode(atmService, '+udtName+', 0)" >1</a></li>'
	            	+'  <li><a href="#" ng-click="plateFluoUtils.computeLineMode(atmService, '+udtName+', 1)" >2</a></li>'
	            	+'  <li><a href="#" ng-click="plateFluoUtils.computeLineMode(atmService, '+udtName+', 2)" >3</a></li>'
	            	+'  <li><a href="#" ng-click="plateFluoUtils.computeLineMode(atmService, '+udtName+', 3)" >4</a></li>'
	            	+'  <li><a href="#" ng-click="plateFluoUtils.computeLineMode(atmService, '+udtName+', 4)" >5</a></li>'
	            	+'  <li><a href="#" ng-click="plateFluoUtils.computeLineMode(atmService, '+udtName+', 5)" >6</a></li>'
	            	+'  <li><a href="#" ng-click="plateFluoUtils.computeLineMode(atmService, '+udtName+', 6)" >7</a></li>'
	            	+'  <li><a href="#" ng-click="plateFluoUtils.computeLineMode(atmService, '+udtName+', 7)" >8</a></li>'
	            	+'  <li><a href="#" ng-click="plateFluoUtils.computeLineMode(atmService, '+udtName+', 8)" >9</a></li>'
	            	+'  <li><a href="#" ng-click="plateFluoUtils.computeLineMode(atmService, '+udtName+', 9)" >10</a></li>'
	            	+'  <li><a href="#" ng-click="plateFluoUtils.computeLineMode(atmService, '+udtName+', 10)" >11</a></li>'
	            	+'  <li><a href="#" ng-click="plateFluoUtils.computeLineMode(atmService, '+udtName+', 11)" >12</a></li>'
	            	+'</ul>'
	            	+'</div>'
	            	+'</div>'
	            },
				buttonColumnMode : function(udtName){
					if(!udtName)udtName='atmService.data';
					return ''
					+'<div class="btn-group">'
	            	+'<button class="btn btn-default" ng-click="plateFluoUtils.computeColumnMode(atmService, '+udtName+', 7)" data-toggle="tooltip" title="'+Messages("experiments.button.plate.computeColumnMode")+'" ng-disabled="!isEditMode()"><i class="fa fa-magic"></i><i class="fa fa-arrow-down"></i></button>'
	            	+'<div class="btn-group" role="group">'
	            	+'<button type="button"  title="'+Messages("experiments.button.plate.computeColumnMode.advanced")+'" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false" ng-disabled="!isEditMode()">'
	            	+'  <span class="caret"></span>'
	            	+'</button>'
	            	+' <ul class="dropdown-menu">'
	            	+'  <li><a href="#" ng-click="plateFluoUtils.computeColumnMode(atmService, '+udtName+', 0)" >A</a></li>'
	            	+'  <li><a href="#" ng-click="plateFluoUtils.computeColumnMode(atmService, '+udtName+', 1)" >B</a></li>'
	            	+'  <li><a href="#" ng-click="plateFluoUtils.computeColumnMode(atmService, '+udtName+', 2)" >C</a></li>'
	            	+'  <li><a href="#" ng-click="plateFluoUtils.computeColumnMode(atmService, '+udtName+', 3)" >D</a></li>'
	            	+'  <li><a href="#" ng-click="plateFluoUtils.computeColumnMode(atmService, '+udtName+', 4)" >E</a></li>'
	            	+'  <li><a href="#" ng-click="plateFluoUtils.computeColumnMode(atmService, '+udtName+', 5)" >F</a></li>'
	            	+'  <li><a href="#" ng-click="plateFluoUtils.computeColumnMode(atmService, '+udtName+', 6)" >G</a></li>'
	            	+'  <li><a href="#" ng-click="plateFluoUtils.computeColumnMode(atmService, '+udtName+', 7)" >H</a></li>'
	            	+'</ul>'
	            	+'</div>'
	            	+'</div>'
	            }
			}
	            
		};
	
	var updatePropertyUnit = function(experiment){
		$scope.experimentType.propertiesDefinitions.forEach(function(propertyDef){
			if(propertyDef.saveMeasureValue){
				experiment.atomicTransfertMethods.forEach(function(atm){
					if(atm.inputContainerUseds){
						atm.inputContainerUseds.forEach(function(icu){
							if(icu.experimentProperties && icu.experimentProperties[propertyDef.code]
								&& (icu.experimentProperties[propertyDef.code].unit === null || icu.experimentProperties[propertyDef.code].unit === undefined)){
								icu.experimentProperties[propertyDef.code].unit = propertyDef.saveMeasureValue.code;
							}
						});
					}
					if(atm.outputContainerUseds){
						atm.outputContainerUseds.forEach(function(ocu){
							if(ocu.experimentProperties && ocu.experimentProperties[propertyDef.code]
							&& (ocu.experimentProperties[propertyDef.code].unit === null || ocu.experimentProperties[propertyDef.code].unit === undefined)){
								ocu.experimentProperties[propertyDef.code].unit = propertyDef.saveMeasureValue.code;
							}
						});
					}
				});
			}
		});		
	};
	
	var resetBasket = function(){
		if(mainService.getBasket())mainService.getBasket().reset();
	};
	
	var loadInstrumentType = function(code){
		$http.get(jsRoutes.controllers.instruments.api.InstrumentUsedTypes.get(code).url)
			.success(function(data, status, headers, config) {
				if ($parse('experiment.state.code')($scope) !== "F" || !Permissions.check("admin")){
					data.instruments = $filter("filter")(data.instruments,{active:true},true);
				}
				$scope.instrumentType = data;
				updateInstrumentIfNeeded();			
				mainService.put("instrumentType",$scope.instrumentType);				
				
			})
			.error(function(data, status, headers, config) {
				$scope.messages.setError("get");
			});
	};
	
	var updateInstrumentIfNeeded = function(){
		var instrument = $scope.experiment.instrument;
		if(undefined === instrument.typeCode || null === instrument.typeCode){
			instrument.typeCode = $scope.instrumentType.code;
		}
		if(undefined === instrument.categoryCode || null === instrument.categoryCode){
			instrument.categoryCode = $scope.instrumentType.category.code;
		}
		if(undefined === instrument.inContainerSupportCategoryCode || null === instrument.inContainerSupportCategoryCode){
			instrument.inContainerSupportCategoryCode = getInContainerSupportCategoryCode();
		}
		if(undefined === instrument.outContainerSupportCategoryCode|| null === instrument.outContainerSupportCategoryCode){
			instrument.outContainerSupportCategoryCode = getOutContainerSupportCategoryCode();
		}
		
		if($scope.experimentTypeTemplate === undefined){
			$scope.loadTemplate();
		}
	};
	
	
	var getInContainerSupportCategoryCode = function(){
		if(mainService.getBasket() && mainService.getBasket().get() && mainService.getBasket().get()[0]){
			return mainService.getBasket().get()[0].support.categoryCode; 
		}else {
			return $parse('atomicTransfertMethods[0].inputContainerUseds[0].locationOnContainerSupport.categoryCode')($scope.experiment);			
		}
	};
	
	
	var getOutContainerSupportCategoryCode = function(){
		if($scope.isOutputATMVoid()){
			return "void";					
		}else{
			return undefined
		}
	};
	
	var updateData = function(){
		if($scope.experiment.code){
			$http.get(jsRoutes.controllers.experiments.api.Experiments.get($scope.experiment.code).url).success(function(data) {
				$scope.experiment = data;		
				mainService.put("experiment",$scope.experiment);
				$scope.$broadcast('refresh');
			});
		}		
	};
	
	var clearLists = function(){
		$scope.lists.clear("instrumentUsedTypes");
		$scope.lists.clear("protocols");
		$scope.lists.clear("resolutions");
		$scope.lists.clear("states");
		$scope.lists.clear("experimentTypeCodes");
		$scope.lists.clear("experimentCategories");
		$scope.lists.refresh.valuationCriterias({typeCode:$scope.experiment.typeCode,objectTypeCode:"Experiment"});
		$scope.lists.refresh.experimentTypes({categoryCode:$scope.experimentType.category.code},$scope.experimentType.category.code);
		
		
		 if ($parse('experiment.state.code')($scope) === "F" && Permissions.check("admin")){
			 $scope.lists.refresh.protocols({"experimentTypeCode":$scope.experimentType.code});
			 $scope.lists.refresh.instrumentUsedTypes({"experimentTypeCode":$scope.experimentType.code});
		 } else {
			 $scope.lists.refresh.protocols({"experimentTypeCode":$scope.experimentType.code,"isActive":true});
			 $scope.lists.refresh.instrumentUsedTypes({"experimentTypeCode":$scope.experimentType.code, "isActive":true});
		 }
		
		$scope.lists.refresh.resolutions({"typeCode":$scope.experimentType.code});
		$scope.lists.refresh.states({"objectTypeCode":"Experiment"});
		$scope.lists.refresh.kitCatalogs({"experimentTypeCodes":$scope.experiment.typeCode});
		$scope.lists.refresh.experimentCategories();
		
		
		
	};
	
	var creationMode = false;
	var saveInProgress = false;
	var additionnalButtons = [];
	
	var init = function(){
		
		$scope.messages = messages();
		$scope.lists = lists;
		$scope.mainService = mainService;
		$scope.valuationService= valuationService();
		$scope.dispatchConfiguration = {/*orderBy*/};
		if(angular.isUndefined($scope.getHomePage())){
			mainService.setHomePage('search');
			tabService.addTabs({label:Messages('experiments.tabs.search'),href:jsRoutes.controllers.experiments.tpl.Experiments.home("search").url,remove:true});
			tabService.addTabs({label:$routeParams.code,href:jsRoutes.controllers.experiments.tpl.Experiments.get($routeParams.code).url,remove:true});			
			tabService.activeTab($scope.getTabs(1));
		}
		
		
		var promise=undefined;
		
		if($routeParams.code === 'new'){
			var defaultExperiment = mainService.get("newExp");
			creationMode = true;
			$scope.startEditMode();
			
			if(!defaultExperiment || defaultExperiment.typeCode !== $routeParams.typeCode){
				defaultExperiment = {
					state : {
						resolutionCodes : [],
						code : "N"
					},
					status : {
						valid : "UNSET"
					},
					reagents : [],
					atomicTransfertMethods : [],
					comments : [],
					typeCode:$routeParams.typeCode,
					instrument:{}
				};				
				mainService.put("newExp", defaultExperiment);
			}
			
			promise=$q.when(defaultExperiment);
		}else{
			promise = $http.get(jsRoutes.controllers.experiments.api.Experiments.get($routeParams.code).url)							
							.error(function(data, status, headers, config) {
								$scope.messages.setError("get");									
							});
		}
		
		promise.then(function(result) {
			var experiment = undefined;
			if(result.data){
				experiment = result.data;
			} else {
				experiment = result;
			}
			
			// experiment.state.code = 'N';
			
			return experiment;
		}).then(function(experiment){			
			$http.get(jsRoutes.controllers.experiments.api.ExperimentTypes.get(experiment.typeCode).url).error(function(data, status, headers, config) {
				$scope.messages.setError("get");
			}).then(function(result) {
				$scope.experiment = experiment;
				$rootScope.$broadcast("ExperimentTypeCode", experiment.typeCode)
				$scope.experimentType = result.data;
				
				$scope.experiment.typeCode =  $scope.experimentType.code;
				$scope.experiment.categoryCode = $scope.experimentType.category.code;
				
				clearLists();
				if($scope.experiment.instrument && $scope.experiment.instrument.typeCode){
					loadInstrumentType($scope.experiment.instrument.typeCode);
				}
				
				mainService.put("experiment",$scope.experiment);
				mainService.put("experimentType",$scope.experimentType);
				
				
				$scope.loadTemplate();
			});			
		})
		
		
	};
	
	$scope.setIsShowInformation=function(bool){
		$scope.isShowInformation=bool;
	}
	// show/hide div information
	$scope.toggleIsShowInformation=function(){
		console.log ('toggleIsShowInformation');
		if ( $scope.isShowInformation===false) { $scope.isShowInformation=true; }
		else {$scope.isShowInformation=false}
	}
	
	init();
	
}]).controller('ReagentsCtrl',['$rootScope', '$scope','$http','lists','$parse','$filter','datatable', 
                                                   function($rootScope,$scope,$http,lists,$parse,$filter,datatable) {
	
	var datatableConfigReagents = {
			name:"reagents",
			columns:[
			         {
			        	 "header":Messages("reagents.table.kitname"),
						 "property":"kitCatalogCode",
						 "filter":"codes:'reagentKit'",
			        	 "order":true,
			        	 "type":"text",
			        	 "listStyle":"bt-select-filter",
			        	 "choiceInList":true,
			        	 "possibleValues": "kitCatalogIsActive()",
			        	 "render":'<div bt-select ng-model="value.data.kitCatalogCode" bt-options="v.code as v.name for v in lists.getKitCatalogs()" ng-edit="false"></div>',
			        	 "edit":true
			         },
			         {
			        	 "header":Messages("reagents.table.boxname"),
			        	 "property":"boxCatalogCode",
			        	 "order":false,
			        	 "type":"text",
			        	 "listStyle":"bt-select-filter",
			        	 "choiceInList":true,
			        	 "possibleValues": 'getBoxCatalogs(value,true)',
			        	 "render":'<div bt-select ng-model="value.data.boxCatalogCode" bt-options="v.code as v.name for v in getBoxCatalogs(value)" ng-edit="false"></div>',			        	 
			        	 "edit":true
			         },			         
			         {
			        	 "header":Messages("reagents.table.boxcode"),
			        	 "property":"boxCode",
			        	 "order":true,
			        	 "type":"text",
			        	 "edit":true,
			        	 "editDirectives":'ng-keydown="scan($event,value.data,\'boxCode\')"'
			         },
			         {
			        	 "header":Messages("reagents.table.reagentname"),
			        	 "property":"reagentCatalogCode",
			        	 "order":false,
			        	 "type":"text",
			        	 "listStyle":"bt-select-filter",
			        	 "choiceInList":true,
			        	 "possibleValues": 'getReagentCatalogs(value)',
			        	 "render":'<div bt-select ng-model="value.data.reagentCatalogCode" bt-options="v.code as v.name for v in getReagentCatalogs(value)" ng-edit="false"></div>',			        	 
			        	 "edit":true
			         },
			         {
			        	 "header":Messages("reagents.table.reagentcode"),
			        	 "property":"code",
			        	 "order":true,
			        	 "type":"text",
			        	 "edit":true,
			        	 "editDirectives":'ng-keydown="scan($event,value.data,\'code\')"'
			         },
			         {
			        	 "header":Messages("reagents.table.description"),
			        	 "property":"description",
			        	 "order":true,
			        	 "type":"text",
			        	 "edit":true
			         }
			         ],
			         compact:true,
			         pagination:{
			        	 mode:'local',
			        	 active:false
			         },		
			         search:{
			        	 active:false
			         },
			         order:{
			        	 mode:'local',
			        	 active:true,
			        	 by:'outputPositionX'
			         },
			         remove:{
			        	 mode:'local',
			        	 active:Permissions.check("writing")?true:false,
			        	 withEdit:true,
			        	 callback:function(datatable){
			        		 var reagents = datatable.allResult;
			        		 $scope.experiment.reagents = reagents;
			        	 }
			         },
			         hide:{
			        	 active:false
			         },
			         edit:{
			        	 active:Permissions.check("writing")?true:false,
			        	 columnMode:false,
			        	 showButton : false,
			        	 withoutSelect:true,
			        	 byDefault : false
			         },
			         save:{
			        	 active:Permissions.check("writing")?true:false,
			        	 showButton:false,
			        	 withoutEdit:true,
			        	 mode:'local'
			         },
			         messages:{
			        	 active:false,
			        	 columnMode:true
			         },
			         exportCSV:{
			        	 active:true,
			        	 showButton:true,
			        	 delimiter:";"
			         },
			         add:{
			        	 active:Permissions.check("writing")?true:false,
			         }
			         /*
						 * otherButtons:{ active:true, template:'<button
						 * class="btn btn btn-info"
						 * ng-click="addNewReagentLine()"
						 * title="'+Messages("experiments.addNewReagentLine")+'">'+Messages("experiments.addNewReagentLine")+'</button>' }
						 */
	};
	
	var listKitCatalogsActive = undefined;
	var listBoxCatalogsActive = undefined;
	
	$scope.kitCatalogIsActive = function() {
		var liste = lists.getKitCatalogs();
		if (listKitCatalogsActive == undefined && liste!=undefined) {
			listKitCatalogsActive = $filter('filter')(liste, {active:true});
		}
		return listKitCatalogsActive;
	}
	
	
	$scope.scan = function(e, property, propertyName){
		if(e.keyCode === 9 || e.keyCode === 13){
			property[propertyName] += '_';
			e.preventDefault();
		}
	};

	$scope.searchReagentsEvent = function(e){
		if(e.keyCode === 9 || e.keyCode === 13){
			$scope.searchReagents();
			e.preventDefault();
		}
	};

	$scope.getBoxCatalogs = function(value, isActive){
		
		var kitCatalogCode = $parse("data.kitCatalogCode")(value);
		
		if(null !== kitCatalogCode && undefined !== kitCatalogCode){			
			var key = "boxCatalogs-"+kitCatalogCode+((isActive)?"+-true":"-all");
			
			var result = lists.get(key);
			if(!result){
				if(!isActive){
					result = lists.refresh.boxCatalogs({"kitCatalogCode":kitCatalogCode},key);
				}else{
					result = lists.refresh.boxCatalogs({"kitCatalogCode":kitCatalogCode,"isActive":isActive},key);
				}			
			}	
			return result;
		} 				
	}
	
	$scope.getReagentCatalogs = function(value){
		
		var kitCatalogCode = $parse("data.kitCatalogCode")(value);
		var boxCatalogCode = $parse("data.boxCatalogCode")(value);
		
		if(null !== kitCatalogCode && undefined !== kitCatalogCode
				&& null !== boxCatalogCode && undefined !== boxCatalogCode){
			return lists.getReagentCatalogs({"kitCatalogCode":kitCatalogCode,"boxCatalogCode":boxCatalogCode},"reagentCatalogs-"+kitCatalogCode+"-"+boxCatalogCode);
		}				
	}

	$scope.isReagentAdded = function(code){
		var datatableData = $scope.datatableReagent.getData();
		for(var i=0;i<datatableData.length;i++){
			if(datatableData[i].code === code){
				return true;
			}
		}
		return false
	};
	/*
	 * $scope.addNewReagentLine = function(){ $scope.datatableReagent.save();
	 * $scope.datatableReagent.addData([{}]); $scope.datatableReagent.setEdit(); };
	 */
	
	$scope.datatableReagent = datatable(datatableConfigReagents);
	
	$scope.$watch('experiment', function() {		
		if (angular.isDefined($scope.experiment)){
			if($scope.experiment.reagents === null || $scope.experiment.reagents === undefined || $scope.experiment.reagents.length === 0){
				$scope.datatableReagent.setData([]);				
			}else{
				$scope.datatableReagent.setData($scope.experiment.reagents);
			}
		}
	
	});
	
	$scope.$on('refreshReagents', function(e, callbackFunction) {	
		console.log("call refresh reagents");
		if (angular.isDefined($scope.experiment)){
			if($scope.experiment.reagents === null || $scope.experiment.reagents === undefined || $scope.experiment.reagents.length === 0){
				$scope.datatableReagent.setData([]);				
			}else{
				$scope.datatableReagent.setData($scope.experiment.reagents);
			}
		}
	});

	$scope.$on('saveReagents', function(e, callbackFunctions) {	
		$scope.datatableReagent.save()
		$scope.experiment.reagents = $scope.datatableReagent.getData();
		$scope.$emit('reagentsSaved', callbackFunctions);
	});
	
	$scope.$on('cancel', function(e) {
		$scope.datatableReagent.cancel();						
	});
	
	$scope.$on('activeEditMode', function(e) {
		$scope.datatableReagent.selectAll(true);
		$scope.datatableReagent.setEdit();
	});
	
	$scope.$on('activeFinishEditMode', function(e) {
		$scope.datatableReagent.selectAll(true);
		$scope.datatableReagent.setEdit();
	});
	
	$scope.setWorkLabel = function(workLabel) {
		$scope.workLabelSelected = workLabel;
	};

	$scope.loadWorkLabel = function() {
		if($scope.workLabelSelected) {
			$scope.datatableReagent.addData($scope.reagentReceptions[$scope.workLabelSelected]);
		}
	};

	$scope.initInstitute = function(isCNSInstitute) {
		if(isCNSInstitute) {
			$rootScope.$on("ExperimentTypeCode", function(e, experimentTypeCode) {
				var receptionToLine = function(reception) {
					var line = {
						kitCatalogCode: reception.catalogKit.code,
						description: reception.comments ? reception.comments.map(function(c){return c.comment}).join(", ") : null
					}
					var concatCodes = function(codes) {
						return codes.filter(function(code){return code}).join("_") + "_";
					}
					switch(reception.category) {
						case "Box": 
						line.boxCatalogCode = reception.catalog.code;
						line.boxCode = concatCodes([reception.catalogRefCode, reception.batchNumber, reception.fromProviderId]);
						break;
						case "Reagent": 
						line.boxCatalogCode = reception.catalogBox.code;
						line.reagentCatalogCode = reception.catalog.code;
						line.code = concatCodes([reception.catalogRefCode, reception.batchNumber, reception.fromProviderId]);
						break;
						default: throw "Invalid reception category!";
					} return line;
				};
				$http.get(jsRoutes.controllers.reagents.api.Receptions.list().url, {params: {"experimentTypeCode":experimentTypeCode, "stateCode":"IP"}})
				.then(function(result) {
					workLabels = [];
					$scope.reagentReceptions = result.data
					.filter(function(reception){return reception.workLabel})
					.reduce(function(mapper, reception) {
						if(mapper[reception.workLabel]) {
							mapper[reception.workLabel].push(receptionToLine(reception));
						} else {
							mapper[reception.workLabel] = [receptionToLine(reception)];
							workLabels.push({name: reception.workLabel});
						} return mapper;
					}, {});
					workLabels.sort(function(wlabelA, wlabelB) {
						return wlabelA.name ? wlabelA.name.localeCompare(wlabelB.name) : 1;
					});
					$scope.WorkLabels = workLabels;
				});
			});
		}
	};
	
}]).controller('CommentsCtrl',['$scope','$sce', '$http','lists','$parse','$filter','datatable', 
                               function($scope,$sce,$http,lists,$parse,$filter,datatable) {

	$scope.currentComment = {comment:undefined};
	
	
	$scope.analyseText = function(e){
		
		if(e.keyCode === 9){
			e.preventDefault();
		}
	};
	
	$scope.convertToBr = function(text){
		return $sce.trustAsHtml(text.replace(/\n/g, "<br>"));
	};
	
	$scope.cancel = function(){	
		$scope.currentComment = {comment:undefined};
		$scope.index = undefined;
	};
	
	$scope.save = function(){	
		if($scope.isCreationMode()){
			$scope.experiment.comments.push($scope.currentComment);
			$scope.currentComment = {comment:undefined};
		}else{
			$scope.messages.clear();
			$http.post(jsRoutes.controllers.experiments.api.ExperimentComments.save($scope.experiment.code).url, $scope.currentComment)
			.success(function(data, status, headers, config) {
				if(data!=null){
					$scope.messages.setSuccess("save");
					$scope.experiment.comments.push(data);
					$scope.currentComment = {comment:undefined};
				}
			})
			.error(function(data, status, headers, config) {
				$scope.messages.setError("save");
				$scope.messages.setDetails(data);
			});		
		}		
	};
	
	$scope.isUpdate = function(){
		return ($scope.index != undefined);		
	};
	
	$scope.setUpdate = function(comment, index){
		$scope.currentComment = angular.copy(comment);
		$scope.index = index;
	};
	
	$scope.update = function(){		
		if($scope.isCreationMode()){
			$scope.experiment.comments[$scope.index] = $scope.currentComment;
			$scope.currentComment = {comment:undefined};
			$scope.index = undefined;			
		}else{	
			$scope.messages.clear();
			$http.put(jsRoutes.controllers.experiments.api.ExperimentComments.update($scope.experiment.code, $scope.currentComment.code).url, $scope.currentComment)
			.success(function(data, status, headers, config) {
				if(data!=null){
					$scope.messages.setSuccess("save");
					$scope.experiment.comments[$scope.index] = $scope.currentComment;
					$scope.currentComment = {comment:undefined};
					$scope.index = undefined;
				}
			})
			.error(function(data, status, headers, config) {
				$scope.messages.setError("save");
				$scope.messages.setDetails(data);
			});
		}
	};
	
	$scope.remove = function(comment, index){
		if($scope.isCreationMode()){
			$scope.currentComment = {comment:undefined};
			$scope.experiment.comments.splice(index, 1);
		}else if (confirm(Messages("comments.remove.confirm"))) {
			$scope.messages.clear();
			$http.delete(jsRoutes.controllers.experiments.api.ExperimentComments.delete($scope.experiment.code, comment.code).url)
			.success(function(data, status, headers, config) {
				if(data!=null){
					$scope.messages.setSuccess("save");
					$scope.currentComment = {comment:undefined};
					$scope.experiment.comments.splice(index, 1);
				}
			})
			.error(function(data, status, headers, config) {
				$scope.messages.setError("remove");
				$scope.messages.setDetails(data);				
			});
		}
	};
}]).controller('DispatchCtrl',['$scope', '$http','$q','$parse','$filter','lists','mainService','datatable','commonAtomicTransfertMethod', 
                               function($scope,$http,$q,$parse,$filter,lists,mainService,datatable, commonAtomicTransfertMethod) {
	console.log("Dispatch Ctrl");
	
	getColumns = function(isContainerView){
		var columns = [];
		if(!$scope.isOutputATMVoid()){
			if(isContainerView){
				columns.push({
					"header":Messages("containers.table.container.in.code"),
					"property":"container.treeOfLife.from.containers",
					"render":'<div list-resize="cellValue | collect:\'code\' | unique" list-resize-min-size="3" vertical="true">',
					"order":true,
					"edit":false,
					"hide":true,
					"type":"text",
					"position":0.5
				});
				
				columns.push({
					"header":Messages("containers.table.container.out.code"),
					"property":"code",
					"order":true,
					"edit":false,
					"hide":true,
					"type":"text",
					"position":1
				});
			}else{
				columns.push({
					"header":Messages("containers.table.support.in.code"),
					"property":"container.treeOfLife.from.containers",
					"render":'<div list-resize="cellValue | collect:\'supportCode\' | unique" list-resize-min-size="3" vertical="true">',
					"order":true,
					"edit":false,
					"hide":true,
					"type":"text",
					"position":0.5
				});
				
				columns.push({
					"header":Messages("containers.table.support.out.code"),
					"property":"code",
					"order":true,
					"edit":false,
					"hide":true,
					"type":"text",
					"position":1
				});
			}
		}else{
			if(isContainerView){
				columns.push({
					"header":Messages("containers.table.container.in.code"),
					"property":"code",
					"order":true,
					"edit":false,
					"hide":true,
					"type":"text",
					"position":1
				});
			}else{
				columns.push({
					"header":Messages("containers.table.support.in.code"),
					"property":"code",
					"order":true,
					"edit":false,
					"hide":true,
					"type":"text",
					"position":1
				});
			}
		}
		columns.push({
	        "header":Messages("containers.table.projectCodes"),
	 		"property": "container.projectCodes",
	 		"order":true,
	 		"hide":true,
	 		"type":"text",
	 		"position":2,
	 		"render":"<div list-resize='cellValue|unique' list-resize-min-size='3'>"
	    });
		columns.push({
        	"header":Messages("containers.table.sampleCodes"),
 			"property": "container.sampleCodes",
 			"order":true,
 			"hide":true,
 			"type":"text",
 			"position":3,
 			"render":"<div list-resize='cellValue|unique' list-resize-min-size='3'>"
	    });
		columns.push({
        	"header":Messages("containers.table.tags"),
 			"property": "container.contents",
 			"filter": "getArray:'properties.tag.value'",
 			"order":true,
 			"hide":true,
 			"type":"text",
 			"position":4,
 			"render":"<div list-resize='cellValue|unique' ' list-resize-min-size='3'>"
	    });
		columns.push({
			"header":Messages("containers.table.state.code"),
			"property":"container.state.code",
			"order":true,
			"edit":false,
			"hide":true,
			"type":"text",
			"filter":"codes:'state'",
			"position":7
		});
		columns.push({
			"header":Messages("containers.table.status"),
			"property":"status",
			"order":true,
			"edit":true,
			"hide":false,
			"type":"text",
			"required":true,
			"filter":"codes:'valuation'",
			"choiceInList":true,
			"listStyle":"bt-select",
			"possibleValues":"lists.get('status')",					     
			"position":8,
			"thClass":"columnWidth70px"
		});
		columns.push({
	       	"header":Messages("containers.table.dispatch"),
	    	"property":"dispatch",
	    	"render":"<span ng-bind='getDispatchValue(cellValue)'></span>",
	    	"order":false,
			"edit":true,
			"hide":false,
	    	"type":"text",
	    	"required":true,
	    	"choiceInList":true,
		    "listStyle":"radio",
		    "possibleValues":"getDispatchValues()",
		    "editDirectives":"ng-if='isDispatchValueAvailable(opt.code, value)'",
	    	"position":9
		});
		columns.push({
	       	"header":Messages("containers.table.processResolutions"),
	    	"property":"processResolutions",
	    	"order":false,
			"edit":true,
			"hide":false,
	    	"type":"text",
	    	"choiceInList":true,
		    "listStyle":"bt-select-multiple",
		    "possibleValues":"lists.get('processResolutions')",
		    "editDirectives":"ng-if='isProcessResolutionsMustBeSet(value)'",
		    "position":10
		});		
		
		if($scope.experiment.categoryCode === 'qualitycontrol'){
			columns.push({
				 "header":Messages("containers.table.isValid"),
	        	 "property":"container.valuation.valid", //NGL-2457 => ca c'est la validite du container, ce qu'on veut c'est la validite donnee dans l'experience
				 //"property": "inputContainerUseds[0].valuation.valid", NON NE CORRIGE PAS...TODO !!!
	        	 "filter":"codes:'valuation'",
	        	 "order":true,
				 "edit":false,
				 "hide":false,
	        	 "type":"text",
	        	 "position":7.5
			});	
		}
		
		return columns;
	};
	
	var datatableConfig = {
			name:"dispatch",
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
				//by:'code'
			},
			remove:{
				active:false,
			},
			save:{
				active:Permissions.check("writing")?true:false,
	        	withoutEdit: true,
	        	showButton:false,
	        	mode:'local',
	        	changeClass: false,
	        	keepEdit: true,
	        	callback:function(datatable){
	        		
	        		//beginning of algo
	        		var data = datatable.getData();
	        		var isError = false;
	        		for(var i = 0 ; i < data.length ; i++){
	        			if(data[i].dispatch === undefined || data[i].dispatch === null){
	        				datatable.addErrorsForKey(i, {"dispatch":[Messages("containers.dispatch.value.mandatory")]}, "dispatch");
	        				isError = true;
	        			}
	        			if(data[i].status === undefined || data[i].status === null || data[i].status === 'UNSET'){
	        				datatable.addErrorsForKey(i, {"status":[Messages("containers.status.value.mandatory")]}, "status");
	        				isError = true;
	        			}
	        		}
	        		
	        		if(!isError && !$scope.isOutputATMVoid()){
	        			callbackSaveForOutputContainer(datatable);
	        		}else if(!isError){
	        			callbackSaveForInputContainer(datatable);
	        		}
	        	}
	        		
			},
			edit:{
				active:Permissions.check("writing")?true:false,
				columnMode:true,
				byDefault: true,
				showButton:true
			},
			hide:{
				active:false
			},
			messages:{
				active:false,
				columnMode:true
			},			
			extraHeaders:{
				number:2,
				dynamic:true,
			},
			mergeCells: {
                 active: false
            },
            showTotalNumberRecords: false,
            otherButtons :{
	        	 active:true,
	        	 template:''
	        	 +' <button ng-click="swithView()"   class="btn btn-info" ng-switch="supportView">'+Messages("baskets.switchView")+
	        	 ' '+'<b ng-switch-when="true" class="switchLabel">'+
	        	 Messages("baskets.switchView.containers")+'</b>'+
	        	 '<b ng-switch-when="false" class="switchLabel">'+Messages("baskets.switchView.supports")+'</b></button></button>'
	         }
	};
	
	$scope.isDispatchValueAvailable = function(dispatchCode, value){
		if(value !== undefined){
			
			var intersection = function(arrays){
				  return arrays.reduce(function(previous, current){
				      if(previous && current){
				    	  return previous.filter(function(element){
				    		  return current.indexOf(element) > -1;				    	
				    	  });
				      }
				  });
				};
			
			var dvet = intersection(value.data.container.fromTransformationTypeCodes.map(function(fromTransformationTypeCode){
				var dvets = value.data.container.processTypeCodes.map(function(processTypeCode){
					var dvet = dispatchValuesForExperimentType[fromTransformationTypeCode+"-"+processTypeCode];						
					if($scope.isOutputATMVoid() && $scope.experiment.categoryCode !== 'qualitycontrol'){
						dvet = dispatchValuesForExperimentType[$scope.experiment.typeCode+"-"+processTypeCode];
		    		}
					return dvet
				});
				
				return intersection(dvets.filter(function(elt){return elt !== undefined}));
			}));
			
			/*
			var dvet = dispatchValuesForExperimentType[value.data.container.fromTransformationTypeCodes[0]+"-"+value.data.container.processTypeCodes[0]];						
			if($scope.isOutputATMVoid() && $scope.experiment.categoryCode !== 'qualitycontrol'){
				dvet = dispatchValuesForExperimentType[$scope.experiment.typeCode+"-"+value.data.container.processTypeCodes[0]];
    		}
			*/
			if(value.data.status === 'FALSE' && (value.data.dispatch !== 5 && value.data.dispatch !== 6)){
				value.data.dispatch = undefined;
			}else if(value.data.status === 'TRUE' && (value.data.dispatch === 5)){
				value.data.dispatch = undefined;
			}else if(value.data.status === 'UNSET'){
				value.data.dispatch = undefined;
			}else if(dvet && dvet.indexOf(dispatchCode) === -1 && value.data.dispatch === dispatchCode){
				value.data.dispatch = undefined;
			}
			
			if(dvet && dvet.indexOf(dispatchCode) !== -1){
				if(value.data.status === 'FALSE' && (dispatchCode === 5 || dispatchCode === 6)){
					return true;
				}else if(value.data.status === 'TRUE' && (dispatchCode !== 5 && dispatchCode !== 6
						|| (dispatchCode === 6 && dvet.indexOf(4) === -1 ))){ //To avoid to have finish and stop together
					return true;
				}else{
					return false;
				}
			}else{
				return false;
			}
			
			
		}else if(dispatchValuesForExperimentType["all"].indexOf(dispatchCode) > -1){
			return true;
		}
	};
	
	var processTypes = {};
	$scope.isProcessResolutionsMustBeSet = function(value){
		//TODO GA rename to fromTransformationCodes
		if(value !== undefined){
			
			/* Old test, comment because dont see resolutions on dispatch === 4 with QC
			var fromTransformationTypeCode = ($scope.isOutputATMVoid())?$scope.experiment.typeCode:value.data.container.fromTransformationTypeCodes[0];
			if(value.data.dispatch === 6 || 
					(value.data.dispatch === 4 
							&& fromTransformationTypeCode === processTypes[value.data.container.processTypeCodes[0]].lastExperimentType.code)){
				return true;
			}*/
			if(value.data.dispatch === 6 || 
					value.data.dispatch === 4){
				return true;
			}
			else{
				value.data.processResolutions = undefined;
				return false;
			}
		}else if(dispatchValuesForExperimentType["all"].indexOf(6) > -1
				|| dispatchValuesForExperimentType["all"].indexOf(4) > -1){
			return true;
		}
	};
	
	//TODO used cache to optimize the update of support and add priority on stateCode
	/*
	var getContainerSupportStateRequests = function(index, supportCodes, stateCode){
		var supportPromises = [];
		supportCodes.forEach(function(value){
			this.push({index:index, data:{code:value, state:{code:stateCode}}});
		},supportPromises);
		
		return supportPromises;
	};
	*/
	var getContainerStateRequests = function(index, containerCodes, stateCode){
		var containerPromises = [];
		containerCodes.forEach(function(value){
			this.push({index:index, data:{code:value, state:{code:stateCode}}});
		},containerPromises);
		
		return containerPromises;
		
	};
	
	var getProcessStateRequests = function(index, processCodes, stateCode, resolutionCodes){
		var processPromises = [];
		processCodes.forEach(function(value){
			this.push({index:index, data:{code:value, state:{code:stateCode, resolutionCodes:resolutionCodes}}});
		},processPromises);	        			
		return processPromises;	        			
	};
	
	var getInputStateForRetry = function(){
		var stateCode = 'A-TM';
		switch($scope.experiment.categoryCode){
			case "qualitycontrol": 
				stateCode = 'A-QC';
				break;
			case "transfert":
				stateCode = 'A-TF';
				break;
			case "purification":
				stateCode = 'A-PF';
				break;
			case "transformation":
				stateCode = 'A-TM';
				break;								   		        			
		} 
		return stateCode;
	};
	
	var callbackSaveForInputContainer = function(datatable){
		//usable function
		var getXCodes = function(inputContainer){
			var codes = {	
					inputContainerCodes:inputContainer.code, 
					processCodes:inputContainer.processCodes
			};
			
			return codes;
		};
		
		var containers = [], processes = [];
		
		//update input container and support, //update container container and support, // update process
		var containerPromises = [];
		var supportPromises = [];
		var processPromises = [];
		var data = datatable.getData();
		for(var i = 0 ; i < data.length ; i++){
			
			var codes = getXCodes(data[i].container);
			
			if(data[i].status === 'TRUE'){
				
				if($scope.experiment.categoryCode === 'qualitycontrol'){
					if(data[i].dispatch === 0){
						containerPromises = containerPromises.concat(getContainerStateRequests(i, codes.inputContainerCodes, "A-TM"));
												
					}else if(data[i].dispatch === 1){
						containerPromises = containerPromises.concat(getContainerStateRequests(i, codes.inputContainerCodes, "A-TF"));
						
					}else if(data[i].dispatch === 2){
						containerPromises = containerPromises.concat(getContainerStateRequests(i, codes.inputContainerCodes, "A-QC"));
						
					}else if(data[i].dispatch === 3){
						containerPromises = containerPromises.concat(getContainerStateRequests(i, codes.inputContainerCodes,  "A-PF"));
						
					}else if(data[i].dispatch === 4){
						
						if(nextExperimentsForExperimentType[data[i].container.fromTransformationTypeCodes[0]]){
							containerPromises = containerPromises.concat(getContainerStateRequests(i, codes.inputContainerCodes, "IW-P"));
						}else{
							containerPromises = containerPromises.concat(getContainerStateRequests(i, codes.inputContainerCodes, "IS"));
						}
						
						//containerPromises = containerPromises.concat(getContainerStateRequests(i, [codes.inputContainerCode], "IW-P"));
						processPromises = processPromises.concat(getProcessStateRequests(i, codes.processCodes,"F", data[i].processResolutions));
					}else if(data[i].dispatch === 6){
						containerPromises = containerPromises.concat(getContainerStateRequests(i, codes.inputContainerCodes, "IS"));
						processPromises = processPromises.concat(getProcessStateRequests(i, codes.processCodes,"F", data[i].processResolutions));
					}
				}else if(data[i].dispatch !== 7){
				
					containerPromises = containerPromises.concat(getContainerStateRequests(i, codes.inputContainerCodes, "IS"));
					if(data[i].dispatch === 4 || data[i].dispatch === 6){
						processPromises = processPromises.concat(getProcessStateRequests(i, codes.processCodes,"F", data[i].processResolutions));
					}				
				}
				
			}else if(data[i].status === 'FALSE'){
				if($scope.experiment.categoryCode === 'qualitycontrol'){
					if(data[i].dispatch === 5){
						containerPromises = containerPromises.concat(getContainerStateRequests(i, codes.inputContainerCodes, getInputStateForRetry()));
						
					}else if(data[i].dispatch === 6){		
						containerPromises = containerPromises.concat(getContainerStateRequests(i, codes.inputContainerCodes, "UA"));
						
						processPromises = processPromises.concat(getProcessStateRequests(i, codes.processCodes,"F", data[i].processResolutions));
					}
				}else{
					if(data[i].dispatch === 5){
						containerPromises = containerPromises.concat(getContainerStateRequests(i, codes.inputContainerCodes, getInputStateForRetry()));
						
					}else if(data[i].dispatch === 6){		
						containerPromises = containerPromises.concat(getContainerStateRequests(i, codes.inputContainerCodes, "IS"));
						
						processPromises = processPromises.concat(getProcessStateRequests(i, codes.processCodes,"F", data[i].processResolutions));
					}
				}
			}
									
		}
		
		
		saveData(supportPromises, containerPromises, processPromises);
		
	};
	var callbackSaveForOutputContainer = function(datatable){
		//usable function
		var getXCodes = function(outputContainer){
			var codes = {	
					outputContainerCodes:outputContainer.code, 
					inputContainerCodes:[], 
					//outputSupportCode:outputContainer.support.code, 
					//inputSupportCodes:[], 
					processCodes:outputContainer.processCodes
			};
			
			inputContainers = outputContainer.treeOfLife.from.containers;
			for(var i = 0 ; i < inputContainers.length ; i++){
				codes.inputContainerCodes.push(inputContainers[i].code);
				/*
				if(codes.inputSupportCodes.indexOf(inputContainers[i].supportCode) === -1){
					codes.inputSupportCodes.push(inputContainers[i].supportCode);
				}
				*/
			}
			return codes;
		};
		
		var containers = [], processes = [];
		
		//update input container and support, //update container container and support, // update process
		var containerPromises = [];
		var supportPromises = [];
		var processPromises = [];
		
		var data = datatable.getData();
		for(var i = 0 ; i < data.length ; i++){
			
			var codes = getXCodes(data[i].container);
			
			if(data[i].status === 'TRUE'){
				if(data[i].dispatch !== 7){
					containerPromises = containerPromises.concat(getContainerStateRequests(i, codes.inputContainerCodes, "IS"));
				}
				//supportPromises = supportPromises.concat(getContainerSupportStateRequests(i, codes.inputSupportCodes, "IS"));
				var outputStateCode = null;
				if(data[i].dispatch === 0){
					outputStateCode = "A-TM";					 		        					
				}else if(data[i].dispatch === 1){
					outputStateCode = "A-TF";
				}else if(data[i].dispatch === 2){
					outputStateCode = "A-QC";
				}else if(data[i].dispatch === 3){
					outputStateCode = "A-PF";
				}else if(data[i].dispatch === 4){
					if(nextExperimentsForExperimentType[data[i].container.fromTransformationTypeCodes[0]]){
						outputStateCode = "IW-P";
					}else{
						outputStateCode = "IS";
					}
					processPromises = processPromises.concat(getProcessStateRequests(i, codes.processCodes,"F", data[i].processResolutions));
				}else if(data[i].dispatch === 6){
					outputStateCode = "IS";
					processPromises = processPromises.concat(getProcessStateRequests(i, codes.processCodes,"F", data[i].processResolutions));
				}
				
				if(null !== outputStateCode){
					containerPromises = containerPromises.concat(getContainerStateRequests(i, codes.outputContainerCodes, outputStateCode));
					//supportPromises = supportPromises.concat(getContainerSupportStateRequests(i, [codes.outputSupportCode], outputStateCode));
				}else{
					console.log("ERROR no outputStateCode");
				}
				
			}else if(data[i].status === 'FALSE'){
				if(data[i].dispatch === 5){
					containerPromises = containerPromises.concat(getContainerStateRequests(i, codes.inputContainerCodes, getInputStateForRetry()));
					//supportPromises = supportPromises.concat(getContainerSupportStateRequests(i, codes.inputSupportCodes, getInputStateForRetry()));
					
					containerPromises = containerPromises.concat(getContainerStateRequests(i, codes.outputContainerCodes, "UA"));
					//supportPromises = supportPromises.concat(getContainerSupportStateRequests(i, [codes.outputSupportCode], "UA"));
					
				}else if(data[i].dispatch === 6){		
					containerPromises = containerPromises.concat(getContainerStateRequests(i, codes.inputContainerCodes, "IS"));
					//supportPromises = supportPromises.concat(getContainerSupportStateRequests(i, codes.inputSupportCodes, "IS"));
					
					containerPromises = containerPromises.concat(getContainerStateRequests(i, codes.outputContainerCodes, "UA"));
					//supportPromises = supportPromises.concat(getContainerSupportStateRequests(i, [codes.outputSupportCode], "UA"));
					
					processPromises = processPromises.concat(getProcessStateRequests(i, codes.processCodes,"F", data[i].processResolutions));
				}
			}
			
		}
		
		saveData(supportPromises, containerPromises, processPromises);		
	};
	
	var saveData = function(supports, containers, processes){
		//$http.put(jsRoutes.controllers.containers.api.ContainerSupports.updateStateBatch().url,supports)
		//	.then(function(data, status,headers,config){
				$http.put(jsRoutes.controllers.containers.api.Containers.updateStateBatch().url,containers)
					.then(function(result){
						var data = result.data;
						var containers = result.config.data
						var errors = []; var isErrors = false;
						
						data.forEach(function(value, key){
							if(value.status !== 200){
								errors[result.config.data[key].data.code] = value.data.code;
								isErrors = true;
							}
						})
						
						if(processes.length > 0){
							$http.put(jsRoutes.controllers.processes.api.Processes.updateStateBatch().url,processes)
								.then(function(result){
									var data = result.data;
									
									data.forEach(function(value, key){
										if(value.status !== 200){
											errors[result.config.data[key].data.code] = value.data.code;
											isErrors = true;
										}
									})
									$scope.$emit('dispatchDone', {errors:errors, isErrors:isErrors});
								});
						}else{
							$scope.$emit('dispatchDone', {errors:errors, isErrors:isErrors});
						}
				},function(data, status,headers,config){
					console.log("ERROR to update state for containers");
				});	
	};
	
	var dispatchValues;
	var dispatchValuesForExperimentType = [];
	dispatchValuesForExperimentType["all"] = [];
	
	var nextExperimentsForExperimentType = [];
	
	$scope.getDispatchValue = function(code){
		if(code)return dispatchValues.find(function(dv){return (dv.code === code);}).name;		
	};
	
	$scope.getDispatchValues = function(){
		return dispatchValues;		
	};
	
	
	
	$scope.swithView = function(){		
		if($scope.supportView){
			swithToContainerView();
		}else{
			swithToSupportView()
		}
	};
	
	var containerViewData = [];
	var supportViewData = [];
	
	var swithToContainerView = function(){
		if($scope.dispatchConfiguration.orderBy){
			containerViewData = $filter("orderBy")(containerViewData,$scope.dispatchConfiguration.orderBy);
		}else if(containerViewData[0] && containerViewData[0].container.categoryCode === 'well'){
			containerViewData = $filter("orderBy")(containerViewData,['container.support.column*1', 'container.support.line']);							
		}else{
			containerViewData = $filter("orderBy")(containerViewData,'container.code');
		}
		
		datatableConfig.columns = getColumns(true),
		$scope.containersDT = datatable(datatableConfig);						
		$scope.containersDT.setData(containerViewData);	
		$scope.supportView = false;
		
	};

	var swithToSupportView = function(){
		supportViewData = $filter("orderBy")(supportViewData,'code');
		
		datatableConfig.columns = getColumns(false),
		$scope.containersDT = datatable(datatableConfig);						
		$scope.containersDT.setData(supportViewData);	
		$scope.supportView = true;
	};
	
	$scope.$on('initDispatchModal', function(e) {	
		
		if($parse('experiment.state.code')($scope) === 'F'){
			
			var atmService = commonAtomicTransfertMethod($scope);
			$scope.lists.refresh.resolutions({"objectTypeCode":"Process"}, "processResolutions");
			
			if(dispatchValues === undefined){
				dispatchValues = [];
				for(var i = 0; i <= 7 ; i++){
					if($scope.experimentType.atomicTransfertMethod !== "OneToMany" || 
							($scope.experimentType.atomicTransfertMethod === "OneToMany" && i !== 5)){
						dispatchValues.push({"code":i,"name":Messages("containers.dispatch.value."+i)});
					}
											
				}
			}
			
			var initDisplayValues = function(fromTransformationTypeCodes, processTypeCodes){
				initNextExperimentAvailable(fromTransformationTypeCodes);
				var fromTransformationTypeCode = fromTransformationTypeCodes[0];			
				var processTypeCode = processTypeCodes[0];
				var key = fromTransformationTypeCode+"-"+processTypeCode;
				if(undefined === dispatchValuesForExperimentType[key]){	
					dispatchValuesForExperimentType[key] = [];
					$http.get(jsRoutes.controllers.experiments.api.ExperimentTypes.list().url,{params:{previousExperimentTypeCode:fromTransformationTypeCode,processTypeCode:processTypeCode}})
						.success(function(data, status,headers,config){
							var isNextExperimentType = (data.length > 0) ? true:false;	
							//extract the node to have their configuration
							$http.get(jsRoutes.controllers.experiments.api.ExperimentTypeNodes.get(fromTransformationTypeCode).url)
								.success(function(data, status,headers,config){
									for(var i = 0; i <= 7 ; i++){
										if((i === 1 && data.doTransfert) || 
												(i === 2 && data.doQualityControl) || 
													(i === 3 && data.doPurification) ||
														(i === 4 && !isNextExperimentType) ||
														(i === 0 && isNextExperimentType) ||
													i > 4){
											dispatchValuesForExperimentType[key].push(i);
											if(dispatchValuesForExperimentType["all"].indexOf(i) < 0){
												dispatchValuesForExperimentType["all"].push(i);
											}											
										}
									}
								});
						});
				}				
			};
					
			var initNextExperimentAvailable = function(fromTransformationTypeCodes){
				var fromTransformationTypeCode = fromTransformationTypeCodes[0];			
				var key = fromTransformationTypeCode;
				if(undefined === nextExperimentsForExperimentType[key]){	
					nextExperimentsForExperimentType[key] = [];
					$http.get(jsRoutes.controllers.experiments.api.ExperimentTypes.list().url,{params:{previousExperimentTypeCode:fromTransformationTypeCode}})
						.success(function(data, status,headers,config){
							var isNextExperimentType = (data.length > 0) ? true:false;	
							nextExperimentsForExperimentType[key] = isNextExperimentType;
						});
				}				
			};
			
			if(!$scope.isOutputATMVoid()){
				atmService.loadOutputContainerFromAtomicTransfertMethods($scope.experiment.atomicTransfertMethods).then(function (result) {
					
					var getValidStatus = function(){
						return $scope.experiment.status.valid;
					};
					
					if(result.output){
						var processTypeCodes = [];
						containerViewData = [];
						var supportViewDataTmp = [];
						var containers = result.output;
						for(var key in containers){
							var container = containers[key];
							if(container.state.code === 'N'){
								container.code = [container.code];
								containerViewData.push({code:container.code, container:container, status:getValidStatus(), dispatch:undefined, processResolutions:[]});
								
								processTypeCodes = processTypeCodes.concat(container.processTypeCodes);
								initDisplayValues(container.fromTransformationTypeCodes, container.processTypeCodes);
							
								if(supportViewDataTmp[container.support.code]){
									supportViewDataTmp[container.support.code].code = container.support.code;
									supportViewDataTmp[container.support.code].container.code = supportViewDataTmp[container.support.code].container.code.concat(container.code);
									supportViewDataTmp[container.support.code].container.projectCodes = supportViewDataTmp[container.support.code].container.projectCodes.concat(container.projectCodes);
									supportViewDataTmp[container.support.code].container.sampleCodes = supportViewDataTmp[container.support.code].container.sampleCodes.concat(container.sampleCodes);
									supportViewDataTmp[container.support.code].container.contents = supportViewDataTmp[container.support.code].container.contents.concat(container.contents);
									supportViewDataTmp[container.support.code].container.treeOfLife.from.containers = supportViewDataTmp[container.support.code].container.treeOfLife.from.containers.concat(container.treeOfLife.from.containers);
									supportViewDataTmp[container.support.code].container.processCodes = supportViewDataTmp[container.support.code].container.processCodes.concat(container.processCodes);
									supportViewDataTmp[container.support.code].container.processTypeCodes = supportViewDataTmp[container.support.code].container.processTypeCodes.concat(container.processTypeCodes);
									supportViewDataTmp[container.support.code].container.fromTransformationTypeCodes = supportViewDataTmp[container.support.code].container.fromTransformationTypeCodes.concat(container.fromTransformationTypeCodes);
								}else{
									supportViewDataTmp[container.support.code] = {
											container : $.extend(true,{},container), 
											status:getValidStatus(),
											dispatch:undefined, 
											processResolutions:[]	
									};
									supportViewDataTmp[container.support.code].code = container.support.code;									
								}
							}
						}
						supportViewData = []
						for(key in supportViewDataTmp){
							supportViewDataTmp[key].container.processTypeCodes = $parse("processTypeCodes|unique")(supportViewDataTmp[key].container);
							supportViewDataTmp[key].container.fromTransformationTypeCodes = $parse("fromTransformationTypeCodes|unique")(supportViewDataTmp[key].container);
							supportViewData.push(supportViewDataTmp[key]);
						}
											
						swithToContainerView();
						
					
						$http.get(jsRoutes.controllers.processes.api.ProcessTypes.list().url,{params:{codes:processTypeCodes}})
							.success(function(data, status,headers,config){
								data.forEach(function(value){
									this[value.code] = value;
								}, processTypes);
								
							});
						
					}
					
				});
			}else {
				atmService.loadInputContainerFromAtomicTransfertMethods($scope.experiment.atomicTransfertMethods).then(function (result) {
					
					var getValidStatus = function(){
						return $scope.experiment.status.valid;
					};
					
					if(result.input){
						containerViewData = [];
						var supportViewDataTmp = [];
						var processTypeCodes = [];
						
						var containers = result.input;
						
						for(var key in containers){
							var container = containers[key];
							if(container.state.code === 'IW-D'){
								
								container.code = [container.code];
								containerViewData.push({code:container.code, container:container, status:getValidStatus(), dispatch:undefined, processResolutions:[]});
								
								processTypeCodes = processTypeCodes.concat(container.processTypeCodes);
								if($scope.experiment.categoryCode === 'qualitycontrol'){
									initDisplayValues(container.fromTransformationTypeCodes, container.processTypeCodes);
								}else{
									initDisplayValues([$scope.experiment.typeCode], container.processTypeCodes);
								}
								
								if(supportViewDataTmp[container.support.code]){
									supportViewDataTmp[container.support.code].code = container.support.code;
									supportViewDataTmp[container.support.code].container.code = supportViewDataTmp[container.support.code].container.code.concat(container.code);
									supportViewDataTmp[container.support.code].container.projectCodes = supportViewDataTmp[container.support.code].container.projectCodes.concat(container.projectCodes);
									supportViewDataTmp[container.support.code].container.sampleCodes = supportViewDataTmp[container.support.code].container.sampleCodes.concat(container.sampleCodes);
									supportViewDataTmp[container.support.code].container.contents = supportViewDataTmp[container.support.code].container.contents.concat(container.contents);
									//supportViewDataTmp[container.support.code].container.treeOfLife.from.containers = supportViewDataTmp[container.support.code].container.treeOfLife.from.containers.concat(container.treeOfLife.from.containers);
									supportViewDataTmp[container.support.code].container.processCodes = supportViewDataTmp[container.support.code].container.processCodes.concat(container.processCodes);
									supportViewDataTmp[container.support.code].container.processTypeCodes = supportViewDataTmp[container.support.code].container.processTypeCodes.concat(container.processTypeCodes);
									supportViewDataTmp[container.support.code].container.fromTransformationTypeCodes = supportViewDataTmp[container.support.code].container.fromTransformationTypeCodes.concat(container.fromTransformationTypeCodes);
								}else{
									supportViewDataTmp[container.support.code] = {
											container : $.extend(true,{},container), 
											status:getValidStatus(),
											dispatch:undefined, 
											processResolutions:[]	
									};
									supportViewDataTmp[container.support.code].code = container.support.code;									
								}
								
							}
						}
						
						supportViewData = []
						for(key in supportViewDataTmp){
							supportViewDataTmp[key].container.processTypeCodes = $parse("processTypeCodes|unique")(supportViewDataTmp[key].container);
							supportViewDataTmp[key].container.fromTransformationTypeCodes = $parse("fromTransformationTypeCodes|unique")(supportViewDataTmp[key].container);
							supportViewData.push(supportViewDataTmp[key]);
						}
											
						swithToContainerView();
						
						$http.get(jsRoutes.controllers.processes.api.ProcessTypes.list().url,{params:{codes:processTypeCodes}})
							.success(function(data, status,headers,config){
								data.forEach(function(value){
									this[value.code] = value;
								}, processTypes);
								
							});
						
					}//TODO les void and when ok
										
				});
			}
			$scope.$emit('initDispatchModalDone');
		}
	});

	
}]).controller('ValuationCtrl',['$scope', '$http','$q','$parse','$filter','lists','mainService','datatable','commonAtomicTransfertMethod', 
    function($scope,$http,$q,$parse,$filter,lists,mainService,datatable, commonAtomicTransfertMethod) {
	console.log("Valuation Ctrl");
	
	var datatableConfig = {
			name:"copyToInput",
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
				//by:'code'
			},
			remove:{
				active:false,
			},
			save:{
				active:Permissions.check("writing")?true:false,
	        	withoutEdit: true,
	        	showButton:false,
	        	mode:'local',
	        	changeClass: false,
	        	keepEdit: true,
	        	callback:function(datatable){
	        		
	        		//beginning of algo
	        		var data = datatable.getData();
	        		var isError = false;
	        		for(var i = 0 ; i < data.length ; i++){
	        			if(data[i].inputContainerUseds[0].copyValuationToInput === undefined 
	        					|| data[i].inputContainerUseds[0].copyValuationToInput === null
	        					|| data[i].inputContainerUseds[0].copyValuationToInput === 'UNSET'){
	        				datatable.addErrorsForKey(i, {"inputContainerUseds[0].copyValuationToInput":[Messages("containers.copyValuationToInput.value.mandatory")]}, "inputContainerUseds[0].copyValuationToInput");
	        				isError = true;
	        			}	        			
	        		}
	        		if(!isError){
	        			$scope.experiment.atomicTransfertMethods = data;
	        			$scope.finishExperimentModals();
	        		}
	        	}
	        		
			},
			edit:{
				active:Permissions.check("writing")?true:false,
				columnMode:true,
				byDefault: true,
				showButton:false
			},
			hide:{
				active:false
			},
			messages:{
				active:false,
				columnMode:true
			},			
			extraHeaders:{
				number:2,
				dynamic:true,
			},
			mergeCells: {
                 active: false
            },
            showTotalNumberRecords: false,
	};
	
	var getColumns = function() {
		var columns = [];
		columns.push({
			"header" : Messages("containers.table.code"),
			"property" : "inputContainerUseds[0].code",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "text",
			"position" : 3,
			"extraHeaders" : {0 : Messages("experiments.inputs")}
		});
		
		columns.push({
			"header" : Messages("containers.table.fromTransformationTypeCodes"),
			"property" : "inputContainerUseds[0].fromTransformationTypeCodes",
			"filter": "unique | codes:'type'",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "text",
			"render" : "<div list-resize='cellValue' list-resize-min-size='3'>",
			"position" : 4,
			"extraHeaders" : {0 : Messages("experiments.inputs")}
		});
		columns.push({
			"header" : Messages("containers.table.valuation.valid"),
			"property" : "inputContainerUseds[0].valuation.valid",
			"filter" : "codes:'valuation'",
			"order" : true,
			"edit" : false,
			"hide" : false,
			"type" : "text",
			"position" : 5,
			"extraHeaders" : {0 : Messages("experiments.inputs")}
		});
		columns.push({
			"header" : Messages("containers.table.projectCodes"),
			"property" : "inputContainerUseds[0].projectCodes",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "text",
			"position" : 6,
			"render" : "<div list-resize='cellValue' list-resize-min-size='3'>",
			"extraHeaders" : {0 : Messages("experiments.inputs")}
		});
		
		columns.push({
			"header" : Messages("containers.table.sampleCodes"),
			"property" : "inputContainerUseds[0].sampleCodes",
			"order" : true,
			"edit" : false,
			"hide" : true,
			"type" : "text",
			"position" : 7,
			"render" : "<div list-resize='cellValue' list-resize-min-size='3'>",
			"extraHeaders" : {0 : Messages("experiments.inputs")}
		});

		columns.push({
			"header" : Messages("containers.table.valuationqc.valid"),
			"property" : "inputContainerUseds[0].valuation.valid",
			"filter" : "codes:'valuation'",
			"order" : true,
			"edit" : true,
			"hide" : false,
			"type" : "text",
			"choiceInList" : true,
			"listStyle" : 'bt-select',
			"possibleValues" : 'lists.getValuations()',
			"position" : 30,
			"extraHeaders" : {0 : Messages("experiments.inputs")}
		});
		
		columns.push({
			"header" : Messages("containers.table.valuationqc.copyToInput"),
			"property" : "inputContainerUseds[0].copyValuationToInput",
			"filter" : "codes:'containers.table.valuationqc.copyToInput.value'",
			"order" : true,
			"edit" : true,
			"hide" : false,
			"type" : "text",
			"choiceInList" : true,
			"listStyle" : 'bt-select',
			"possibleValues" : [{code:"TRUE", name:Messages("containers.table.valuationqc.copyToInput.value.TRUE")}, 
				{code:"FALSE", name:Messages("containers.table.valuationqc.copyToInput.value.FALSE")},
				{code:"UNSET", name:Messages("containers.table.valuationqc.copyToInput.value.UNSET")}],
			"position" : 30.1,
			"extraHeaders" : {0 : Messages("experiments.inputs")}
		});
		return columns;
	};
	
	$scope.$on('initValuationModal', function(e) {
		console.log("valuationModal");
		
		datatableConfig.columns = getColumns(),						
		$scope.containersDT = datatable(datatableConfig);
		$scope.containersDT.setData($scope.experiment.atomicTransfertMethods);
		
		$scope.$emit('initValuationModalDone');
	});
}]);
