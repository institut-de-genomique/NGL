"use strict";


angular.module('home').controller('SearchContainerCtrl', ['$scope', 'datatable','basket','lists','$filter','$http','mainService','tabService','$parse', 
                                                          function($scope, datatable,basket, lists,$filter,$http,mainService, tabService, $parse) {
	$scope.lists = lists;	
	$scope.searchService = {};
	$scope.searchService.lists = lists;

	var datatableConfig = {
			columns:[
			         {
			        	 "header":Messages("containers.table.supportCode"),
			        	 "property":"support.code",
			        	 "order":true,
			        	 "hide":true,
			        	 "type":"text",
			        	 "position":1,
			 			 "group":true
			         },
			         {
			        	 "header":Messages("containers.table.supportCategoryCode"),
			        	 "property":"support.categoryCode",
			        	 "filter":"codes:'container_support_cat'",
			        	 "order":true,
			        	 "hide":true,
			        	 "type":"text",
			        	 "position":2,
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
			        	 "type":"text",
			        	 "position":5,
			 			 "render":"<div list-resize='cellValue | stringToArray | unique' ' list-resize-min-size='2'>",
			 			 "groupMethod":"collect"
			         },
			         {
			        	 "header":Messages("processes.table.projectCode"),
			        	 "property":"projectCodes",
			        	 "order":true,
			        	 "hide":true,
			        	 "type":"text",
			        	 "position":6,					
			 			 "render":"<div list-resize='cellValue | unique' ' list-resize-min-size='2'>",
			 			 "groupMethod":"collect"
			         },
			         {
			 			"header":Messages("containers.table.sampleCodes.length"),
			 			"property":"sampleCodes.length",
			 			"order":true,
			 			"hide":true,
			 			"type":"text",
			        	"position":7,
			 			"groupMethod":"sum"
				 	},
			 		{
						"header":Messages("containers.table.sampleCodes"),
						"property":"sampleCodes",
						"order":true,
						"hide":true,
						"type":"text",
						"render":"<div list-resize='cellValue | unique' list-resize-min-size='3'>",
						"groupMethod":"collect",
						"position":8
					},
			 		{
						"header":Messages("containers.table.contents.length"),
						"property":"contents.length",
						"order":true,
						"hide":true,
						"type":"number",
			        	 "position":9,
			 			"groupMethod":"sum"
					},
					{
						"header":Messages("containers.table.tags"),
						"property": "contents",
						"order":false,
						"hide":true,
						"type":"text",
						"render":"<div list-resize='cellValue | getArray:\"properties.tag.value\" | unique' ' list-resize-min-size='3'>",
						"groupMethod":"collect",
			        	"position":10
					},
			        {
			        	 "header":Messages("containers.table.fromTransformationTypeCodes"),
			        	 "property":"fromTransformationTypeCodes",			        	 
			        	 "order":false,
			        	 "hide":true,
			        	 "type":"text",
			        	 "position":5.5,
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
			        	 "filter":"codes:'state'",
			        	 "position":12,
			 			"groupMethod":"unique"
			         },	        
			        {
			        	 "header":Messages("containers.table.valid"),
			        	 "property":"valuation.valid",
			        	 "order":true,
			        	 "hide":true,
			        	 "type":"text",
			        	 "filter":"codes:'valuation'",
			        	 "position":13,
			        	 "groupMethod":"unique"
			         },
			         {
						"header":Messages("containers.table.creationDate"),
						"property":"traceInformation.creationDate",
						"order":true,
						"hide":true,
						"type":"date",
			        	 "position":14,
			        	 "groupMethod":"unique"
					 },
					 {
						"header":Messages("containers.table.createUser"),
						"property":"traceInformation.createUser",
						"order":true,
						"hide":true,
						"type":"text",
			        	 "position":15,
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
				 	 hide:{
				 		 active:true
				 	 },
				 	 order:{
				 		active:true,
				 		by:'traceInformation.creationDate',
				 		reverse : true,
			 			mode:'local'
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
					}
	};


	$scope.changeProcessCategory = function(){
		$scope.searchService.form.nextProcessTypeCode = undefined;
		$scope.lists.clear("processTypes");

		if($scope.searchService.form.processCategory !== undefined && $scope.searchService.form.processCategory !== null){
			$scope.lists.refresh.processTypes({categoryCode:$scope.searchService.form.processCategory,isActive:true});
		}
	};

	$scope.changeProcessType = function(){
		$scope.removeTab(1);
		$scope.basket.reset();
	};
	
	$scope.selectDefaultFromExperimentType = function(){
		var selectionList = {};	
		$scope.searchService.form.fromTransformationTypeCodes=[];
			
		if($scope.searchService.form.nextProcessTypeCode){
			
			selectionList = angular.copy($scope.lists.get('transformation',true));
			$http.get(jsRoutes.controllers.experiments.api.ExperimentTypes.getDefaultFirstExperiments($scope.searchService.form.nextProcessTypeCode).url)
			.success(function(data, status, headers, config) {
				
				data = data.filter(function(exp){
					return !exp.code.startsWith("ext");
				});
				
				data.unshift({name: "None", code: "none"});
				
				$scope.defaultFirstExperimentTypes = data;
				
				/*
				angular.forEach(defaultFirstExperimentTypes, function(experimentType, key){
					angular.forEach(selectionList, function(item, index){
						if(experimentType.code==item.code){
							$scope.searchService.form.fromTransformationTypeCodes.push(item.code);							
							
						}
					});
				});				
				$scope.search();
				*/
			});
		}else{
			$scope.defaultFirstExperimentTypes = [];
		}		
	};

	$scope.reset = function(){
		$scope.searchService.form = {};
	};

	$scope.resetSampleCodes = function(){
		$scope.searchService.form.sampleCodes = [];									
	};
	
	$scope.refreshSamples = function(){
		if($scope.searchService.form.projectCodes && $scope.searchService.form.projectCodes.length>0){
			lists.refresh.samples({projectCodes:$scope.searchService.form.projectCodes});
		}
	};

	$scope.search = function(){	
		$scope.searchService.updateForm();
		var _form = angular.copy($scope.searchService.form);
		$scope.errors.processCategory = {};
		$scope.errors.processType = {};
		
		
		
		
		if((_form.processCategory && _form.nextProcessTypeCode) || _form.createUser){			
			_form.stateCode = 'IW-P';
			if(_form.fromDate)_form.fromDate = moment(_form.fromDate, Messages("date.format").toUpperCase()).valueOf();
			if(_form.toDate)_form.toDate = moment(_form.toDate, Messages("date.format").toUpperCase()).valueOf();

			$scope.datatable.search(_form);
			mainService.setForm($scope.searchService.form);
		}else{
			if(_form.processCategory === null || _form.processCategory === undefined || _form.processCategory === "" ){
				$scope.errors.processCategory = "has-error";
				$scope.errors.processType = "has-error";
				$scope.searchService.form.nextProcessTypeCode = undefined;
			}
			if(_form.nextProcessTypeCode === null || _form.nextProcessTypeCode === undefined || _form.nextProcessTypeCode === "" ){
				$scope.errors.processType = "has-error";
			}
			$scope.datatable.setData([],0);
			$scope.basket.reset();

		}
	};
	
	
	$scope.searchService.initAdditionalColumns = function(){
		$scope.searchService.additionalColumns=[];
		$scope.searchService.selectedAddColumns=[];
		
		if($scope.searchService.lists.get("containers-addcolumns-processes-creation") && $scope.searchService.lists.get("containers-addcolumns-processes-creation").length === 1){
			var formColumns = [];
			var allColumns = angular.copy($scope.searchService.lists.get("containers-addcolumns-processes-creation")[0].columns);
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
	};
	
	$scope.searchService.getAddColumnsToForm = function(){
		if($scope.searchService.additionalColumns !== undefined && $scope.searchService.additionalColumns.length === 0){
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
		
		if($scope.searchService.lists.get("containers-search-addfilters") && $scope.searchService.lists.get("containers-search-addfilters").length === 1){
			var formFilters = [];
			var allFilters = angular.copy($scope.searchService.lists.get("containers-search-addfilters")[0].filters);
			var nbElementByColumn = Math.ceil(allFilters.length / 5); //5 columns
			for(var i = 0; i  < 5 && allFilters.length > 0 ; i++){
				formFilters.push(allFilters.splice(0, nbElementByColumn));	    								
			}
			//complete to 5 five element to have a great design 
			while(formFilters.length < 5){
				formFilters.push([]);
			}
				
			$scope.searchService.additionalFilters = additionalFilters = formFilters;
		}
	};
	
	$scope.searchService.getAddFiltersToForm = function(){
		if($scope.searchService.additionalFilters=== undefined || $scope.searchService.additionalFilters.length === 0){
			$scope.searchService.initAdditionalFilters();
		}
		return $scope.searchService.additionalFilters;									
	};	
	

	$scope.addToBasket  = function(containers){
		containers.forEach(function(container){
			var codes = [];
			if(container.group){
				codes = $scope.datatable.getGroupColumnValue(container, "code")				
			}else if($scope.basket.get().indexOf(container.code) === -1){
				codes[0] = container.code;												
			}	
			
			codes.forEach(function(code){
				if($scope.basket.get().indexOf(code) === -1){
					$scope.basket.add(code);	
				}	
			});
		});	
		if(($scope.searchService.form.nextProcessTypeCode) && this.basket.length() > 0 && tabService.getTabs().length === 1){
			tabService.addTabs({label:$filter('codes')($scope.searchService.form.nextProcessTypeCode,"type"),href:$scope.searchService.form.nextProcessTypeCode,remove:false});
		}
	};
	
	
	$scope.searchService.getDefaultColumns = function(){ return datatableConfig.columns;};

	//init
	$scope.errors = {};
	if(angular.isUndefined($scope.getDatatable())){
		$scope.datatable = datatable(datatableConfig);			
		mainService.setDatatable($scope.datatable);	
	}else{
		$scope.datatable = mainService.getDatatable();
	}

	if(angular.isUndefined($scope.getHomePage())){
		mainService.setHomePage('new');
		tabService.addTabs({label:Messages('processes.tabs.create'),href:jsRoutes.controllers.processes.tpl.Processes.home("new").url,remove:false});
		tabService.activeTab(0);
	}

	if(angular.isUndefined($scope.getBasket())){
		$scope.basket = basket();			
		mainService.setBasket($scope.basket);
	}else{
		$scope.basket = mainService.getBasket();
	}
	
	

	if(angular.isUndefined(mainService.getForm())){
		$scope.searchService.form = {};
		mainService.setForm($scope.searchService.form);
		$scope.lists.refresh.projects();
		$scope.lists.refresh.processCategories();
		$scope.lists.refresh.containerSupports();
		$scope.lists.refresh.containerSupportCategories();
		$scope.lists.refresh.users();
		lists.refresh.experimentTypes({categoryCode:"transformation", withoutOneToVoid:true},'transformation');
		lists.refresh.reportConfigs({pageCodes:["containers-search"]});
		$scope.lists.refresh.reportConfigs({pageCodes:["containers-addcolumns-processes-creation"]}, "containers-addcolumns-processes-creation");
		$scope.lists.refresh.filterConfigs({pageCodes:["containers-search-addfilters"]}, "containers-search-addfilters");
		$scope.searchService.additionalFilters=[];
		$scope.searchService.additionalColumns=[];
		$scope.searchService.selectedAddColumns=[];
		$scope.searchService.getColumns=datatableConfig.columns;
		$scope.searchService.lists = lists;


	}else{
		$scope.searchService.form = mainService.getForm();			
	}
	
	
	
}]);


angular.module('home').controller('ListNewCtrl', ['$scope','$http','$q','$filter','$parse','$routeParams', 'mainService','tabService', 'datatable', 
                                                     function($scope,$http,$q,$filter,$parse,$routeParams,mainService,tabService,datatable) {

	
	var	datatableConfig = {
			 columns: [],
	         pagination:{
	        	 active:false
	         },		
	         search:{
	        	 active:false
	         },
	         order:{
	        	 mode:'local',
	        	 active:true
	         },
	         edit:{  		
	        	 active:true,
	        	 columnMode:true,
	        	 byDefault : true,
	        	 showButton:false
	         },
	         save:{
	        	 active: true,
	        	 withoutEdit:true,
	        	 showButton : true,
	        	 mode:"local",
	        	 changeClass : false,
	        	 callback : function(datatable){
	        		 save(datatable.getData());
	        	 }
	         },
	         remove:{
	        	 active:true,
	        	 mode:'local',
	        	 withEdit:true,
	        	 callback : function(datatable){
	        		 mainService.getBasket().reset();
	        		 datatable.getData().forEach(function(elt){
	        			 mainService.getBasket().add(elt.code);
	        		 });
	        		 computeData();	        		 
	        	 }
	         },
	         lines:{
	        	trClass:function(data, line){
	        		if($scope.supportView && supportViewData[data.support.code]){
	        			return supportViewData[data.support.code].trClass
	        		}else if(containerViewData[data.code[0]]){	        			
	        			return containerViewData[data.code[0]].trClass	        			
	        		}else{
	        			return '';
	        		}	        		
	        	} 
	         },
	         messages:{
	        	 active:false,
	        	 transformKey: function(key, args) {
		             return Messages(key, args);
	        	 }
	         },
	         otherButtons :{
	        	 active:true,
	        	 template:''
	        	 +' <button ng-click="swithView()" ng-disabled="loadView"  class="btn btn-info" ng-switch="supportView" ng-if="!containerErroView">'+Messages("baskets.switchView")+
	        	 ' '+'<b ng-switch-when="true" class="switchLabel">'+
	        	 Messages("baskets.switchView.containers")+'</b>'+
	        	 '<b ng-switch-when="false" class="switchLabel">'+Messages("baskets.switchView.supports")+'</b></button></button>'
	         }
	};

	var getProcessCreationColumns = function(view){
		var columns = [];
		
		
		if("container" === view){
			
			columns.push({
		       	 "header":Messages("processes.table.supportCode"),
		       	 "property":"support.code",
		       	 "order":true,
		       	 "hide":true,
		       	 "position":1,
		       	 "type":"text"
			});
			columns.push({
		       	 "header":Messages("containers.table.nomPool"),
		       	 "property":"contents[0].properties.Nom_pool_sequencage.value",
//		       	 "property":"properties.Nom_pool_sequencage.value",
		       	 "order":true,
		       	 "hide":true,
		       	 "position":2,
		       	 "type":"text"
			});
			
//			columns.push({
//		       	 "header":Messages("processes.table.line"),
//		       	 "property":"support.line",
//		       	 "order":true,
//		       	 "hide":true,
//		       	 "position":2,
//		       	 "type":"text"
//			});
//			columns.push({
//		       	 "header":Messages("processes.table.columns"),
//		       	 "property":"support.column*1",
//		       	 "order":true,
//		       	 "hide":true,
//		       	 "position":3,
//		       	 "type":"number"
//			});			
			
		}else{
			columns.push({
	       	 "header":Messages("processes.table.supportCode"),
	       	 "property":"support.code",
	       	 "order":true,
	       	 "hide":true,
	       	 "position":1,
	       	 "type":"text"
			});
			
		}
		
		columns.push({
       	 "header":Messages("processes.table.projectCode"),
       	 "property":"projectCodes",
       	 "order":true,
       	 "hide":true,
       	 "position":4,
       	 "render":"<div list-resize='value.data.projectCodes | unique' list-resize-min-size='3'>",
       	 "type":"text"
		});
		columns.push({
       	 "header":Messages("processes.table.sampleCode"),
       	 "property":"sampleCodes",
       	 "order":true,
       	 "hide":true,
       	 "position":5,
       	 "render":"<div list-resize='value.data.sampleCodes | unique' list-resize-min-size='3'>",
       	 "type":"text"
		});
		
		columns.push({
			"header":Messages("containers.table.sampleTypes"),
			"property":"contents",
			"order":false,
			"hide":false,
			"position":5.01,
			"type":"text",
			"filter":"getArray:'sampleTypeCode' | unique | codes:\"type\"",
			"groupMethod":"collect"
		});
		
		columns.push({
		"header":Messages("containers.table.contents.length"),
		"property":"contents.length",
		"order":true,
		"hide":true,
		"position":5.05,
		"type":"number"
		});
		columns.push({
       	 "header":Messages("containers.table.stateCode"),
       	 "property":"state.code",
       	 "order":true,
       	 "hide":true,
       	 "position":6,
       	 "filter": "codes:'state'",
       	 "type":"text"
		});
		columns.push({
       	"header" : Messages("processes.table.comments"),
			"property" : "comments[0].comment",
			"position" : 500,
			"order" : false,
			"edit" : true,
			"editTemplate":"<textarea class='form-control' #ng-model rows='3'></textarea>",
			"hide" : true,
			"type" : "text"
		});
		return columns;
	};
	
	var	datatableConfigProcessOK = {
			 columns: [],
	         pagination:{
	        	 active:true,
	        	 mode:'local',
	        	 numberRecordsPerPage:50
	         },		
	         search:{
	        	 active:false
	         },
	         order:{
	        	 mode:'local',
	        	 active:true
	         },
	         lines:{
	        	trClass:function(data, line){
	        		if(containerViewData[data.inputContainerCode]){	        			
	        			return containerViewData[data.inputContainerCode].trClass	        			
	        		}else{
	        			return '';
	        		}	        		
	        	} 
	         }
	};
	
	
	var getProcessOKColumns = function(){
		var columns = [
		         {
		        	 "header":Messages("processes.table.inputContainerCode"),
		        	 "property":"inputContainerCode",
		        	 "order":true,
		        	 "hide":true,
		        	 "position":1,
		        	 "type":"text"
		         },
		         {
		 			"header":Messages("containers.table.contents.length"),
		 			"property":"contents.length",
		 			"url":"'/api/containers/'+inputContainerCode",
		 			"order":true,
		 			"hide":true,
		 			"position":2,
		 			"type":"number"
			 	},
		         {
		        	 "header":Messages("processes.table.sampleCode"),
		        	 "property":"sampleCodes",
		        	 "order":true,
		        	 "hide":true,
		        	 "position":2.01,
		        	 "type":"text"
		         },			        
		         {
		        	"header" : Messages("containers.table.tags"),
		 			"property" : "sampleOnInputContainer.properties.tag.value",
		 			"type" : "text",
		 			"order" : true,
		 			"hide" : true,
		 			"position":4,
		 			"groupMethod" : "collect",
		 			"render" : "<div list-resize='cellValue | unique' list-resize-min-size='3'>",		        	
		         },		         
		         {
		        	 "header" : Messages("processes.table.typeCode"),
		 			"property" : "typeCode",
		 			"filter" : "codes:'type'",
		 			"order" : true,
		 			"hide" : true,
		 			"position" : 9,
		 			"type" : "text"		        	
		         },
		         {
		        	 "header":Messages("processes.table.stateCode"),
		        	 "property":"state.code",
		        	 "order":true,
		        	 "hide":true,
		        	 "filter": "codes:'state'",
		        	 "position":30,
		        	 "type":"text"
		         },
		         {
		        	 "header":Messages("processes.table.code"),
		        	 "property":"code",
		        	 "order":true,
		        	 "hide":true,
		        	 "position":33,
		        	 "type":"text"
		         },
		         {
		        	 "header":Messages("processes.table.creationDate"),
		        	 "property":"traceInformation.creationDate",
		        	 "order":true,
		        	 "hide":true,
		        	 "position":34,
		        	 "type":"date"
		         },
		         {
		        	 "header":Messages("processes.table.projectCode"),
		        	 "property":"projectCodes",
		        	 "order":true,
		        	 "hide":true,
		        	 "position":37,
		        	 "type":"text"
		         },
		         {
		         "header" : Messages("processes.table.comments"),
					"property" : "comments[0].comment",
					"position" : 500,
					"order" : false,
					"edit" : true,
					"hide" : true,
					"type" : "text"
			        }
		 ];
		
		columns = columns.concat(processPropertyColumns);
	
		return columns;
	};
	
	
	var getDisplayUnitFromProperty = function(propertyDefinition){
		var unit = $parse("displayMeasureValue.value")(propertyDefinition);
		if(undefined !== unit && null !== unit) return " ("+unit+")";
		else return "";
	};
	var getPropertyColumnType = function(type){
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
	};
	
	var processPropertyColumns = [];
	var computeProcessColumns = function(properties){
		
		if(properties){
			properties.forEach(function(propertyDefinition){
				
				var column = {};
				column.watch=true;
				column.header = propertyDefinition.name + getDisplayUnitFromProperty(propertyDefinition);
				column.required=propertyDefinition.required;
				    				
				column.property = "properties."+propertyDefinition.code+".value";
				column.edit = propertyDefinition.editable;
				column.type = getPropertyColumnType(propertyDefinition.valueType);
				column.choiceInList = propertyDefinition.choiceInList;
				column.position = (9+(propertyDefinition.displayOrder/1000));
				column.defaultValues = propertyDefinition.defaultValue;
				column.format = propertyDefinition.displayFormat;
				
				if(column.choiceInList){
					if(propertyDefinition.possibleValues.length > 100){
						column.editTemplate='<input class="form-control" type="text" #ng-model typeahead="v.code as v.name for v in col.possibleValues | filter:$viewValue | limitTo:20" typeahead-min-length="1" udt-change="updatePropertyFromUDT(value,col)"/>';        					
					}else{
						column.listStyle = "bt-select";
					}
					column.possibleValues = propertyDefinition.possibleValues; 
					column.filter = "codes:'value."+propertyDefinition.code+"'";    					
				}
				
				if(propertyDefinition.displayMeasureValue != undefined && propertyDefinition.displayMeasureValue != null){
					column.convertValue = {"active":true, "displayMeasureValue":propertyDefinition.displayMeasureValue.value, 
							"saveMeasureValue":propertyDefinition.saveMeasureValue.value};
				}
				
				processPropertyColumns.push(column);					
			});
		}
		
	};
	
	$scope.swithView = function(){		
		if($scope.supportView){
			swithToContainerView();
		}else{
			swithToSupportView()
		}
	};
	
	var containerViewData = {};
	var supportViewData = {};
	
	
	var swithToContainerErrorView = function(){
		var containers = [];
		for(var key in containerViewData){
			if(containerViewData[key].onError)containers.push(containerViewData[key]);
		}
		containers = $filter('orderBy')(containers, ['support.code', 'support.column*1', 'support.line']);
		$scope.datatable.setColumnsConfig(getProcessCreationColumns("container").concat(processPropertyColumns));
		$scope.datatable.setData(containers);
		$scope.containerErroView = true;
	};
	
	
	var swithToContainerView = function(){
		var containers = [];
		for(var key in containerViewData){
			containers.push(containerViewData[key]);
		}
		containers = $filter('orderBy')(containers, ['support.code', 'support.column*1', 'support.line']);
		$scope.datatable.setColumnsConfig(getProcessCreationColumns("container").concat(processPropertyColumns));
		$scope.datatable.setData(containers);	
		$scope.supportView = false;
		
	};

	var swithToSupportView = function(){
		var supports = [];
		for(var key in supportViewData){
			supports.push(supportViewData[key]);
		}
		supports = $filter('orderBy')(supports, 'support.code');
		$scope.datatable.setColumnsConfig(getProcessCreationColumns("support").concat(processPropertyColumns));
		$scope.datatable.setData(supports);			
		$scope.supportView = true;
	};
	
	
	var computeData = function(){
		containerViewData = {};
		supportViewData = {};
		
		var containerCodes = [];
		containerCodes = containerCodes.concat(mainService.getBasket().get());
		
		if(containerCodes.length > 0){
			var nbElementByBatch = Math.ceil(containerCodes.length / 6); //6 because 6 request max in parrallel with firefox and chrome
            var queries = [];
            for (var i = 0; i < 6 && containerCodes.length > 0; i++) {
                var subContainerCodes = containerCodes.splice(0, nbElementByBatch);
                queries.push( $http.get(jsRoutes.controllers.containers.api.Containers.list().url,{params:{codes:subContainerCodes}}) );
            }
			
            return $q.all(queries).then(function(results) {
				var allData = [];
				results.forEach(function(result){
					allData = allData.concat(result.data);
				});
				
				allData.forEach(function(data){
					data.properties = null;
					data.comments = [];
					containerViewData[data.code]=data;
					containerViewData[data.code].code = [data.code];
					if(supportViewData[data.support.code]){
						supportViewData[data.support.code].code = supportViewData[data.support.code].code.concat(data.code);
						supportViewData[data.support.code].projectCodes = supportViewData[data.support.code].projectCodes.concat(data.projectCodes);
						supportViewData[data.support.code].sampleCodes = supportViewData[data.support.code].sampleCodes.concat(data.sampleCodes);
						supportViewData[data.support.code].contents = supportViewData[data.support.code].contents.concat(data.contents);
					}else{
						supportViewData[data.support.code] = $.extend(true,{},data);						
					}	
				});
				
				
            });		
		}
	};
	
	var save = function(data){
		var allProcesses = [];
		data.forEach(function(value, index){
			var process = {};
			process.typeCode = processType.code;
			process.categoryCode = processType.category.code;
			process.properties = value.properties;
			process.inputContainerSupportCode = value.support.code;
			process.comments = value.comments;
			value.code.forEach(function(containerCode){
				var processContainer =  $.extend(true,{},process);
				processContainer.inputContainerCode = containerCode;
				allProcesses.push({data:processContainer, index:index});
			})			
		});
		
		var nbElementByBatch = Math.ceil(allProcesses.length / 6);
		var queries = [];
        for (var i = 0; i < 6 && allProcesses.length > 0; i++) {
        	var subsetOfProcesses = allProcesses.splice(0, nbElementByBatch);
        	queries.push($http.post(jsRoutes.controllers.processes.api.Processes.saveBatch().url, subsetOfProcesses,{subsetOfProcesses:subsetOfProcesses}));
        }
		
		$q.all(queries).then(function(results) {
			$scope.containerErroView = false;
			var atLeastOneError = false;
			
			results.forEach(function(result){
				if (result.status !== 200) {
					console.log("Batch in error");					
	            } else {
	            	result.data.forEach(function(data){
	            		
	            		if (data.status === 200) {
	            			containerViewData[data.data[0].inputContainerCode].trClass = "success";
	            			if(supportViewData[data.data[0].inputContainerSupportCode].trClass !== "danger"){
	            				supportViewData[data.data[0].inputContainerSupportCode].trClass = "success";
	            			}
	            			processesDoneWithSuccess = processesDoneWithSuccess.concat(data.data);
	            		}else{
	            			var process = $filter('filter')(result.config.subsetOfProcesses,{index:data.index}, true)[0];
	            			containerViewData[process.data.inputContainerCode].trClass = "danger";
	            			containerViewData[process.data.inputContainerCode].onError = true;
	            			supportViewData[process.data.inputContainerSupportCode].trClass = "danger";
	            			atLeastOneError = true;
	            		}	            		
	            	});	            	
	            }
			});
			
			if(atLeastOneError){
    			swithToContainerErrorView();
    		}else{
    			datatableConfigProcessOK.columns = getProcessOKColumns();
    			$scope.datatable = datatable(datatableConfigProcessOK);
    			$scope.datatable.setData(processesDoneWithSuccess);
    		}
			
		});
	};
		
	var processType = undefined;
	var processesDoneWithSuccess = [];
	var init = function(){
		
		if($routeParams.processTypeCode){
			$http.get(jsRoutes.controllers.processes.api.ProcessTypes.get($routeParams.processTypeCode).url)
				.success(function(data, status,headers,config){
					processType = data;
					computeProcessColumns(processType.propertiesDefinitions);
					//load containers by codes
					$scope.supportView = false;
					computeData().then(function(){
						$scope.datatable = datatable(datatableConfig);
						if(!$scope.supportView){
							swithToContainerView();
						}else{
							swithToSupportView()
						}
					});					
				});
		}
		
	};
	
	init();
	
}]);
