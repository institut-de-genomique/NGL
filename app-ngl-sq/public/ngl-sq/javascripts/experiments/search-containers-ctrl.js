"use strict";

angular.module('home').controller('SearchContainersCtrl', ['$scope','$routeParams', '$filter','datatable','basket','lists','$http','mainService','tabService', 
                                                           function ($scope,$routeParams, $filter, datatable,basket, lists, $http,mainService,tabService) {
	$scope.searchService = {};
	$scope.searchService.lists = lists;
	
	$scope.datatableConfig = {
		columns:[{
			"header":Messages("containers.table.supportCode"),
			"property":"support.code",
			"order":true,
			"hide":true,
			"position":1,
			"type":"text",
			"group":true
		},
		{
			"header":Messages("containers.table.supportCategoryCode"),
			"property":"support.categoryCode",
			"filter":"codes:'container_support_cat'",
			"order":true,
			"hide":true,
			"position":2,
			"type":"text",
			"groupMethod":"unique"
		},		
		{
			"header":Messages("containers.table.support.line"),
			"property":"support.line",
			"order":true,
			"hide":true,
			"position":3,
			"type":"text"
		},
		{
			"header":Messages("containers.table.support.column"),
			"property":"support.column*1",
			"order":true,
			"hide":true,
			"position":4,
			"type":"number"
		},
		{
			"header":Messages("containers.table.code"),
			"property":"code",
			"order":true,
			"position":5,
			"render":"<div list-resize='cellValue | stringToArray | unique' ' list-resize-min-size='2'>",
			"type":"text",
			"groupMethod":"collect"
		},
		{
			"header":Messages("containers.table.projectCodes"),
			"property":"projectCodes",
			"order":true,
			"hide":true,
			"position":6,					
			"render":"<div list-resize='cellValue | unique' ' list-resize-min-size='2'>",
			"type":"text",
			"groupMethod":"collect"
		},
		{
			"header":Messages("containers.table.sampleCodes.length"),
			"property":"sampleCodes.length",
			"order":true,
			"hide":true,
			"position":7,
			"type":"number",
			"groupMethod":"sum"
		},
		{
			"header":Messages("containers.table.sampleCodes"),
			"property":"sampleCodes",
			"order":true,
			"hide":true,
			"hide":true,
			"position":8,
			"type":"text",
			"render":"<div list-resize='cellValue | unique' list-resize-min-size='3'>",
			"groupMethod":"collect"
			
		},
		{
			"header":Messages("containers.table.contents.length"),
			"property":"contents.length",
			"order":true,
			"hide":true,
			"hide":true,
			"position":9,
			"type":"number",
			"groupMethod":"sum"
				
		},
		{
			"header":Messages("containers.table.tags"),
			"property": "contents",
			"order":false,
			"hide":true,
			"order":true,
			"type":"text",
			"position":10,
			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			"filter":"getArray:'properties.tag.value' | unique",
			"groupMethod":"collect"
			
		},
		
		{
			"header":Messages("containers.table.fromTransformationTypeCodes"),
			"property":"fromTransformationTypeCodes",
			"hide":true,
			"order":true,
			"position":5.5,
			"type":"text",
			"render":"<div list-resize='cellValue | unique | codes:\"type\"' list-resize-min-size='3'>",
			"groupMethod":"collect"
		},
		{
			"header":Messages("containers.table.concentration.value"),
			"property":"concentration.value",
			"order":true,
			"hide":true,
			"position":11.1,
			"format":3,
			"type":"number",
			"groupMethod":"unique"
		},	
		{
			"header":Messages("containers.table.concentration.unit"),
			"property":"concentration.unit",
			"order":true,
			"hide":true,
			"position":11.2,
			"type":"text",
			"groupMethod":"unique"
		},
		
		{
			"header":Messages("containers.table.state.code"),
			"property":"state.code",
			"order":true,
			"hide":true,
			"type":"text",
			"edit":false,
			"position":12,
			"choiceInList": true,
			"possibleValues":"searchService.lists.getStates()", 
			"filter":"codes:'state'",
			"groupMethod":"unique"
				
		},
		 
		{
			"header":Messages("containers.table.valid"),
			"property":"valuation.valid",
			"order":true,
			"hide":true,
			"type":"text",
			"edit":true,
			"position":13,
			"choiceInList": true,
			"possibleValues":"searchService.lists.getValuations()", 
			"filter":"codes:'valuation'",
       	 	"groupMethod":"unique"
		},
		{
			"header":Messages("containers.table.creationDate"),
			"property":"traceInformation.creationDate",
			"order":true,
			"hide":true,
			"position":14,
			"type":"date",
			"groupMethod":"unique"
		},
		{
			"header":Messages("containers.table.createUser"),
			"property":"traceInformation.createUser",
			"order":true,
			"hide":true,
			"position":15,
			"type":"text",
			"groupMethod":"unique"
		},
		{
			"header":Messages("containers.table.storageCode"),
			"property":"support.storageCode",
			"order":true,
			"hide":true,
			"type":"text",
			"edit":false,
			"position":15.5,
			"groupMethod":"unique"
				
		 },
		 /*
		{
			"header":Messages("containers.table.processCodes"),
			"property":"processCodes",
			"order":false,
			"hide":true,
			"type":"text",
			"position":16,
			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			"groupMethod":"collect"
		},
		*/
		{
			"header":Messages("containers.table.processTypeCodes"),
			"property":"processTypeCodes",
			"filter":"codes:'type'",
			"order":false,
			"hide":true,
			"type":"text",
			"position":16,
			"render":"<div list-resize='cellValue' list-resize-min-size='3' vertical>",
			"groupMethod":"collect"
		}
		
		],	
		search:{
			url:jsRoutes.controllers.containers.api.Containers.list()
		},
		group:{
			active:true,
			showOnlyGroups:true,
			enableLineSelection:true,
			showButton:true,
			by:"support.code"
		},
		pagination:{
			mode:'local'
		},
		filter:{
			"active":false,
			"highlight":true,
			"columnMode":false
		},
		order:{
			active:true,
			by:'traceInformation.creationDate',
			reverse : true,
			mode:'local'
		},
		hide:{
	 		 active:true
	 	},
		otherButtons :{
			active:true,
			template:'<button class="btn" ng-disabled="!datatable.isSelect() && !datatable.isSelectGroup()" ng-click="addToBasket(datatable.getSelection(true))" data-toggle="tooltip" title="'+Messages("button.addbasket")+'">'
					+'<i class="fa fa-shopping-cart fa-lg"></i> ({{basket.length()}})</button>'
		},
		messages : {
				transformKey: function(key, args) {
                    return Messages(key, args);
                }
		},
		exportCSV:{
			active:true
		}
	};
	
	
	
	$scope.changeExperimentType = function(experimentCategory){
		tabService.removeTab(2);
		tabService.removeTab(1);

		$scope.basket.reset();
		$scope.searchService.form.containerSupportCategory = undefined;
		$scope.searchService.lists.clear("containerSupportCategories");
		if($scope.searchService.form.nextExperimentTypeCode){
			$scope.searchService.lists.refresh.containerSupportCategories({experimentTypeCode:$scope.searchService.form.nextExperimentTypeCode});
		}
		$scope.experimentCategory = experimentCategory;
		this.search();
	};
	
	$scope.changeProcessCategory = function(){
		$scope.additionalFilters=[];
		$scope.searchService.form.processTypeCode = undefined;
		if($scope.searchService.form.processCategory !== undefined)
			$scope.searchService.lists.refresh.processTypes({"categoryCode":$scope.searchService.form.processCategory});
	};
	
	$scope.changeProcessType = function(){
		//lists.refresh.filterConfigs({pageCodes:["process-"+$scope.searchService.form.processTypeCode]}, "process-"+$scope.searchService.form.processTypeCode);
		if(angular.isDefined($scope.searchService.form.processCategory)){						
			$scope.searchService.lists.refresh.filterConfigs({pageCodes:["process-"+$scope.searchService.form.processTypeCode]}, "process-"+$scope.searchService.form.processTypeCode);			                                    		 
		} else{
			$scope.searchService.form.processTypeCode = undefined;			                                    		
		}		

		$scope.searchService.initAdditionalFilters();
		$scope.searchService.initAdditionalProcessFilters();
	};
	
	$scope.reset = function(){
		$scope.searchService.form = {};
		$scope.searchService.additionalProcessFilters=[];
		$scope.searchService.isProcessFiltered=false;
		
	};
	
	$scope.resetSampleCodes = function(){
		$scope.searchService.form.sampleCodes = [];									
	};
	
	$scope.loadExperimentTypesLists = function(){
		$scope.searchService.lists.refresh.experimentTypes({categoryCode:"purification","isActive":true}, "purification");
		$scope.searchService.lists.refresh.experimentTypes({categoryCode:"qualitycontrol","isActive":true}, "qualitycontrol");
		$scope.searchService.lists.refresh.experimentTypes({categoryCode:"transfert","isActive":true}, "transfert");
		$scope.searchService.lists.refresh.experimentTypes({categoryCode:"transformation","isActive":true},"transformation");
		
		$scope.searchService.lists.refresh.experimentTypes({categoryCode:"purification"}, "fromPurification");
		$scope.searchService.lists.refresh.experimentTypes({categoryCode:"transfert"}, "fromTransfert");
		$scope.searchService.lists.refresh.experimentTypes({categoryCode:"transformation", withoutOneToVoid:true},"fromTransformation");
	};
	
	$scope.refreshSamples = function(){
		if($scope.searchService.form.projectCodes && $scope.searchService.form.projectCodes.length>0){
			lists.refresh.samples({projectCodes:$scope.searchService.form.projectCodes});
		}
	};
	
	$scope.getContainerStateCode = function(experimentCategory){
		var stateCode = "A";
		console.log(experimentCategory);
		switch(experimentCategory){
			case "qualitycontrol": stateCode = 'A-QC';
								   break;
			case "transfert": 	   stateCode = 'A-TF';
							       break;
			case "purification":   stateCode = 'A-PF';
								   break;
			case "transformation":   stateCode = 'A-TM';
			   						break;								   
			default:               stateCode = 'A';
		}
		
		return stateCode;
	};
	
	$scope.search = function(){
		$scope.searchService.updateForm();
		var _form = angular.copy($scope.searchService.form);
		$scope.errors.experimentType = {};
		$scope.errors.containerSupportCategory = {};
		
		
		if(_form.nextExperimentTypeCode){
			_form.stateCode = $scope.getContainerStateCode($scope.experimentCategory);	 
		
			var formTemp = angular.copy(_form.nextExperimentTypeCode);
			if($scope.experimentCategory!='transformation') _form.nextExperimentTypeCode=undefined;
			if(_form.fromDate)_form.fromDate = moment($scope.searchService.form.fromDate, Messages("date.format").toUpperCase()).valueOf();
			if(_form.toDate)_form.toDate = moment($scope.searchService.form.toDate, Messages("date.format").toUpperCase()).valueOf();
			_form.processCategory = undefined;
			
			$scope.datatable.search(_form);
			
			if(angular.isDefined(formTemp) && angular.isUndefined(_form.nextExperimentTypeCode)){
				_form.nextExperimentTypeCode=angular.copy(formTemp);				
			}
			
			mainService.setForm($scope.searchService.form);
		}else{
			if(!_form.nextExperimentTypeCode){
				$scope.errors.experimentType = "has-error";
			}
			$scope.datatable.setData([],0);
			$scope.basket.reset();
		}						
	};
	
	$scope.searchService.initAdditionalColumns = function(){
		$scope.searchService.additionalColumns=[];
		$scope.searchService.selectedAddColumns=[];
		
		if($scope.searchService.lists.get("containers-addcolumns") && $scope.searchService.lists.get("containers-addcolumns").length === 1){
			var formColumns = [];
			var allColumns = angular.copy($scope.searchService.lists.get("containers-addcolumns")[0].columns);
			var nbElementByColumn = Math.ceil(allColumns.length / 5); //5 columns
			for(var i = 0; i  < 5 && allColumns.length > 0 ; i++){
				formColumns.push(allColumns.splice(0, nbElementByColumn));	    								
			}
			//complete to 5 five element to have a great design 
			while(formColumns.length < 5){
				formColumns.push([]);
			}
			$scope.searchService.additionalColumns = formColumns;
		}
	};
	
	$scope.searchService.updateForm = function(){
		$scope.searchService.form.includes = [];
		if($scope.searchService.reportingConfiguration){
			for(var i = 0 ; i < $scope.searchService.reportingConfiguration.columns.length ; i++){
				if($scope.searchService.reportingConfiguration.columns[i].queryIncludeKeys && $scope.searchService.reportingConfiguration.columns[i].queryIncludeKeys.length > 0){
					$scope.searchService.form.includes = $scope.searchService.form.includes.concat($scope.searchService.reportingConfiguration.columns[i].queryIncludeKeys);
				}else{
					$scope.searchService.form.includes.push($scope.searchService.reportingConfiguration.columns[i].property.replace('.value','').replace(".unit", ''));
				}
			}
		}else{
			$scope.searchService.form.includes = ["default"];
		}
		
		
		//this.form.includes = ["default"];
		for(var i = 0 ; i < $scope.searchService.selectedAddColumns.length ; i++){
			//remove .value if present to manage correctly properties (single, list, etc.)
			if($scope.searchService.selectedAddColumns[i].queryIncludeKeys && $scope.searchService.selectedAddColumns[i].queryIncludeKeys.length > 0){
				$scope.searchService.form.includes = $scope.searchService.form.includes.concat($scope.searchService.selectedAddColumns[i].queryIncludeKeys);
			}else{
				$scope.searchService.form.includes.push($scope.searchService.selectedAddColumns[i].property.replace('.value','').replace(".unit", ''));
			}
			
		}
		if(this.form.reportingQuery){
			this.form.reportingQuery.trim();
			if(this.form.reportingQuery.length > 0){
				this.form.reporting=true;
			}else{
				this.form.reporting=false;
			}
		}else{
			this.form.reporting=false;
		}
	};
	
	$scope.searchService.getAddColumnsToForm = function(){
		if($scope.searchService.additionalColumns.length === 0){
			$scope.searchService.initAdditionalColumns();
		}
		return $scope.searchService.additionalColumns;									
	};
	
	$scope.searchService.addColumnsToDatatable=function(){
		//this.reportingConfiguration = undefined;
		//this.reportingConfigurationCode = undefined;
		
		$scope.searchService.selectedAddColumns = [];
		for(var i = 0 ; i < $scope.searchService.additionalColumns.length ; i++){
			for(var j = 0; j < $scope.searchService.additionalColumns[i].length; j++){
				if($scope.searchService.additionalColumns[i][j].select){
					$scope.searchService.selectedAddColumns.push($scope.searchService.additionalColumns[i][j]);
				}
			}
		}
		if($scope.searchService.reportingConfigurationCode){
			$scope.datatable.setColumnsConfig($scope.searchService.reportingConfiguration.columns.concat($scope.searchService.selectedAddColumns));
		}else{
			$scope.datatable.setColumnsConfig($scope.searchService.getDefaultColumns().concat($scope.searchService.selectedAddColumns));						
		}
		$scope.search();
	};	
	$scope.searchService.resetDatatableColumns = function(){
		$scope.searchService.initAdditionalColumns();
		$scope.datatable.setColumnsConfig($scope.searchService.getDefaultColumns());
		$scope.search();
	};
	/**
	 * Update column when change reportingConfiguration
	 */
	$scope.searchService.updateColumn = function(){
		$scope.searchService.initAdditionalColumns();
		if($scope.searchService.reportingConfigurationCode){
			$http.get(jsRoutes.controllers.reporting.api.ReportingConfigurations.get($scope.searchService.reportingConfigurationCode).url,{searchService:$scope.searchService, datatable:$scope.datatable})
					.success(function(data, status, headers, config) {
						config.searchService.reportingConfiguration = data;
						//config.searchService.search();
						config.datatable.setColumnsConfig(data.columns);																								
			});
		}else{
			$scope.searchService.reportingConfiguration = undefined;
			$scope.datatable.setColumnsConfig($scope.searchService.getDefaultColumns());
			//$scope.search();
		}
		
	};
	
	$scope.searchService.initAdditionalFilters = function(){
		var additionalFilters = $scope.searchService.additionalFilters = [];
		var allFilters = undefined;
		var formFilters = [];
		if($scope.searchService.lists.get("containers-search-addfilters") && $scope.searchService.lists.get("containers-search-addfilters").length === 1){
			allFilters = angular.copy($scope.searchService.lists.get("containers-search-addfilters")[0].filters);
		}

		if(angular.isDefined(allFilters)){ 
			var nbElementByColumn = Math.ceil(allFilters.length / 5); //5 columns
			for(var i = 0; i  < 5 && allFilters.length > 0 ; i++){
				formFilters.push(allFilters.splice(0, nbElementByColumn));	    								
			}
			//complete to 5 five element to have a great design 
			while(formFilters.length < 5){
				formFilters.push([]);
			}
		}
			

			$scope.searchService.additionalFilters = additionalFilters = formFilters;
		
	};
	
	$scope.searchService.getAddFiltersToForm = function(){
		if($scope.searchService.additionalFilters.length === 0){
			$scope.searchService.initAdditionalFilters();
		}
		return $scope.searchService.additionalFilters;									
	};	
	
	$scope.searchService.initAdditionalProcessFilters = function(){
		$scope.searchService.additionalProcessFilters=[];
    	 var formFilters = [];
    	 var allFilters = undefined;
    	 var nbElementByColumn = undefined;	
    	 
    	 if(angular.isDefined($scope.searchService.form.processTypeCode) && $scope.searchService.lists.get("process-"+$scope.searchService.form.processTypeCode) && $scope.searchService.lists.get("process-"+$scope.searchService.form.processTypeCode).length === 1){ 
    		 allFilters = angular.copy($scope.searchService.lists.get("process-"+$scope.searchService.form.processTypeCode)[0].filters);
    		 
    		 allFilters.forEach(function(filter){
    			 filter.html = filter.html.replace('searchService.form["properties','searchService.form["processProperties');
    		 })
    		 
    		 $scope.searchService.isProcessFiltered = true;
    	 }else{
    		 $scope.searchService.isProcessFiltered = false;
    	 }
    	 if(angular.isDefined(allFilters)){   
    	 nbElementByColumn = Math.ceil(allFilters.length / 5); //5 columns
    	 for(var i = 0; i  < 5 && allFilters.length > 0 ; i++){
    		 formFilters.push(allFilters.splice(0, nbElementByColumn));	    								
    	 }
    	//complete to 5 five element to have a great design 
    	 while(formFilters.length < 5){
    		 formFilters.push([]);
    	 }
    	 }                               	 

    	 $scope.searchService.additionalProcessFilters = formFilters;			                                    	 
     },
     
     $scope.searchService.getAddProcessFiltersToForm = function(){
    	 if($scope.searchService.additionalProcessFilters !== undefined && $scope.searchService.additionalProcessFilters.length === 0){
    		 $scope.searchService.initAdditionalProcessFilters();
    	 }
    	 return $scope.searchService.additionalProcessFilters;									
     },
	
	
	$scope.addToBasket = function(containers){
		for(var i = 0; i < containers.length; i++){
			var alreadyOnBasket = false;
			for(var j=0;j<this.basket.get().length && !alreadyOnBasket;j++){
				if(containers[i].group === undefined){
					if(this.basket.get()[j].code === containers[i].code){
						alreadyOnBasket = true;
					}
				}else{
					var test = [];
					test = test.concat($scope.datatable.getGroupColumnValue(containers[i], "code"));
					if(test.indexOf(this.basket.get()[j].code) > -1){
						alreadyOnBasket = true;
					}
				}
			}
			if(!alreadyOnBasket){
				if(containers[i].group === undefined){
					this.basket.add(containers[i]);
					if(($scope.searchService.form.nextExperimentTypeCode) && this.basket.length() > 0 && tabService.getTabs().length === 1){
						tabService.addTabs({label:"Configuration "+$filter('codes')($scope.searchService.form.nextExperimentTypeCode,'type'),href:"/experiments/new/"+$scope.searchService.form.nextExperimentTypeCode,remove:false});
					}
				}else{
					var basket = this.basket;
					var codes = $scope.datatable.getGroupColumnValue(containers[i], "code");
					var supportCode = $scope.datatable.getGroupColumnValue(containers[i], "support.code");
					$http.get(jsRoutes.controllers.containers.api.Containers.list().url,{"params":{"supportCode":supportCode,"stateCode":$scope.getContainerStateCode($scope.experimentCategory)}, "codes":codes})
					.success(function(data, status, headers, config) {
						if(data!=null){
							angular.forEach(data, function(container){
								if(config.codes.indexOf(container.code) > -1){
									basket.add(container);
								}
							});
							if(($scope.searchService.form.nextExperimentTypeCode) && basket.length() > 0 && tabService.getTabs().length === 1){
								tabService.addTabs({label:"Configuration "+$filter('codes')($scope.searchService.form.nextExperimentTypeCode,'type'),href:"/experiments/new/"+$scope.searchService.form.nextExperimentTypeCode,remove:false});
							}
						}
					})
					.error(function(data, status, headers, config) {
						alert("error");
					});
					//var container = {"code": $scope.datatable.getGroupColumnValue(containers[i], "support.code"), "projectCodes": $scope.datatable.getGroupColumnValue(containers[i], "projectCodes"), "sampleCodes": $scope.datatable.getGroupColumnValue(containers[i], "sampleCodes")}
					
				}
			}
		}
	};
	
	$scope.searchService.getDefaultColumns = function(){ return $scope.datatableConfig.columns;};
	
	//init
	$scope.errors = {};
	
	if(angular.isUndefined(mainService.getDatatable())){
		$scope.datatable = datatable($scope.datatableConfig);
		mainService.setDatatable($scope.datatable);	
	} else {
		$scope.datatable = mainService.getDatatable();
	}
	if($routeParams.newExperiment === undefined){
		$scope.newExperiment = "new";
	}
	
	if(angular.isUndefined($scope.getHomePage())){
		mainService.setHomePage('new');
		tabService.addTabs({label:Messages('experiments.tabs.create'),href:jsRoutes.controllers.experiments.tpl.Experiments.home("new").url,remove:false});
		tabService.activeTab(0);
	}
	
	if(angular.isUndefined(mainService.getBasket())){
		$scope.basket = basket();			
		mainService.setBasket($scope.basket);
	} else {
		$scope.basket = mainService.getBasket();
	}
	
	//$scope.searchService.lists.clear("processTypes");
	$scope.searchService.lists.refresh.projects();
	$scope.searchService.lists.refresh.types({objectTypeCode:"Process"}, true);
	$scope.searchService.lists.refresh.processCategories();
	$scope.searchService.lists.refresh.experimentCategories();
	$scope.searchService.lists.refresh.users();
	$scope.searchService.lists.refresh.containerSupports();
	//$scope.searchService.lists.refresh.experiments();
	$scope.searchService.lists.refresh.states({objectTypeCode:"Container"});
	$scope.searchService.lists.refresh.reportConfigs({pageCodes:["containers-search"]});
	$scope.searchService.lists.refresh.reportConfigs({pageCodes:["containers-addcolumns"]}, "containers-addcolumns");
	$scope.searchService.lists.refresh.filterConfigs({pageCodes:["containers-search-addfilters"]}, "containers-search-addfilters");
	$scope.form = {};
	$scope.loadExperimentTypesLists();
	$scope.searchService.additionalFilters=[];
	$scope.searchService.additionalProcessFilters=[];
	$scope.searchService.isProcessFiltered=false;
	$scope.searchService.additionalColumns=[];
	$scope.searchService.selectedAddColumns=[];
	$scope.searchService.getColumns=$scope.datatableConfig.columns;
	
	$http.get(jsRoutes.controllers.processes.api.ProcessTypes.list().url,{params:{"list":true}})
		.success(function(data, status, headers, config) {
			var processesTypes = data;
			angular.forEach(processesTypes, function(processType) {
				$scope.searchService.lists.refresh.filterConfigs({pageCodes:["process-"+processType.code]}, "process-"+processType.code);
			})       	 			
	 
		});
	
	
	if(angular.isUndefined(mainService.getForm())){
		$scope.searchService.form = {};
		mainService.setForm($scope.searchService.form);
		
	} else {
		$scope.searchService.form = {};
		$scope.searchService.form =  mainService.getForm();
		if($scope.experimentCategory === undefined){
			$scope.experimentCategory = $scope.searchService.form.experimentCategoryCode;
		}
		$scope.searchService.form.experimentCategoryCode = undefined;
		$scope.searchService.lists.refresh.containerSupportCategories({experimentTypeCode:$scope.searchService.form.nextExperimentTypeCode});
		$scope.changeProcessCategory();
		//$scope.search();
	}
}]);