"use strict";

angular.module('ngl-sq.containerSupportsServices', []).
factory('containerSupportsSearchService', ['$http', 'mainService', 'lists', 'datatable', function($http, mainService, lists, datatable){
	var tags = [];
	var getColumns = function(){
		var columns = [];
		columns.push({
			"header":Messages("containerSupports.table.code"),
			"property":"code",
			"position":1,
			"order":true,
			"type":"text"
		});
		
		columns.push({
			"header":Messages("containerSupports.table.categoryCode"),
			"property":"categoryCode",
			"filter":"codes:'container_support_cat'",
			"position":2,
			"order":true,
			"hide":true,
			"type":"text"
		});
		
		columns.push({
			"header":Messages("containers.table.fromTransformationTypeCodes"),
			"property":"fromTransformationTypeCodes",
			"order":false,
			"hide":true,
			"position":2.5,
			"type":"text",
			"render":"<div list-resize='cellValue | unique' list-resize-min-size='3'>",
			"filter":"unique | codes:\"type\"",
			"groupMethod":"collect"
		});	
		
		// 23/05/2016 FDS: NGL-825: suppression affichage status de validite pour les containerSupports
		//                          factorisation state.code; storageCode editable
		var isStateCodeEditable;
		var isStorageCodeEditable;
		
		if(mainService.getHomePage() === 'state'){
			isStateCodeEditable= true;
			isStorageCodeEditable= false;
		} else { 
			isStateCodeEditable= false;
			isStorageCodeEditable= true;
		}
		
		columns.push({
			"header":Messages("containerSupports.table.state.code"),
			"property":"state.code",
			"position":3,
			"order":true,
			"hide":true,
			"type":"text",
			"edit":isStateCodeEditable,
			"choiceInList": true,
			"listStyle":"bt-select",
			"possibleValues":"searchService.lists.getStates()", 
			"filter":"codes:'state'"
	});
		
		columns.push({
			"header":Messages("containerSupports.table.nbContainers"),
			"property":"nbContainers",
			"position":3.5,
			"order":true,
			"hide":true,
			"type":"text"
		});
		
		
		columns.push({
			"header":Messages("containerSupports.table.sampleCodes.length"),
			"property":"sampleCodes.length",
			"position":4,
			"order":true,
			"hide":true,
			"type":"text"
		});
		
		columns.push({
			"header":Messages("containerSupports.table.sampleCodes"),
			"property":"sampleCodes",
			"position":5,
			"order":true,
			"hide":true,
			"type":"text",
			"render":"<div list-resize='value.data.sampleCodes | unique' list-resize-min-size='3'>",
		});
		
		columns.push({
			"header":Messages("containerSupports.table.projectCodes"),
			"property":"projectCodes",
			"position":6,
			"render":"<div list-resize='value.data.projectCodes | unique' list-resize-min-size='3'>",
			"order":false,
			"hide":true,
			"type":"text"
		});
		
		columns.push({
			"header":Messages("containerSupports.table.nbContents"),
			"property":"nbContents",
			"position":7,
			"order":true,
			"hide":true,
			"type":"text"
		});
		
		columns.push({
			"header":Messages("containerSupports.table.creationDate"),
			"property":"traceInformation.creationDate",
			"position":8,
			"order":true,
			"hide":true,
			"type":"date"
		});
		
		columns.push({
			"header":Messages("containers.table.createUser"),
			"property":"traceInformation.createUser",
			"position":9,
			"order":true,
			"hide":true,
			"type":"text"
		});
		
		columns.push({
			"header":Messages("containers.table.storageCode"),
			"property":"storageCode",
			"order":true,
			"hide":true,
			"type":"text",
			"edit":isStorageCodeEditable,
			"position":9.5,
			"groupMethod":"unique"
				
		});

		
		
		return columns;
	};


	var isInit = false;

	var initListService = function(){
		if(!isInit){
			lists.refresh.containerSupportCategories();
			lists.refresh.containerCategories();
			lists.refresh.experimentTypes({categoryCodes:["transformation"], withoutOneToVoid:false},'transformation');
			lists.refresh.containerSupports();
			lists.refresh.projects();
			lists.refresh.users();
			lists.refresh.processCategories();
			lists.refresh.states({objectTypeCode:"Container"});
			isInit=true;
		}
	};

	var searchService = {
			getColumns:getColumns,
			datatable:undefined,
			isRouteParam:false,
			lists : lists,
			selectedAddColumns:[],
			setRouteParams:function($routeParams){
				var count = 0;
				for(var p in $routeParams){
					count++;
					break;
				}
				if(count > 0){
					this.isRouteParam = true;
					this.form = $routeParams;
				}
			},

			updateForm : function(){
				//this.form.includes = [];
				/*
				this.form.includes = ["default"];
				for(var i = 0 ; i < this.selectedAddColumns.length ; i++){
					//remove .value if present to manage correctly properties (single, list, etc.)
					if(this.selectedAddColumns[i].queryIncludeKeys && this.selectedAddColumns[i].queryIncludeKeys.length > 0){
						this.form.includes = this.form.includes.concat(this.selectedAddColumns[i].queryIncludeKeys);
					}else{
						this.form.includes.push(this.selectedAddColumns[i].property.replace('.value',''));	
					}
					
				}
				*/
			},
			convertForm : function(){
				var _form = angular.copy(this.form);
				if(_form.fromDate)_form.fromDate = moment(_form.fromDate, Messages("date.format").toUpperCase()).valueOf();
				if(_form.toDate)_form.toDate = moment(_form.toDate, Messages("date.format").toUpperCase()).valueOf();		
				return _form
			},

			resetForm : function(){
				this.form = {};									
			},

			resetSampleCodes : function(){
				this.form.sampleCodes = [];									
			},
			
			search : function(){
				this.updateForm();
				mainService.setForm(this.form);
				var jsonSearch = this.convertForm();
				if(jsonSearch != undefined){
					this.datatable.search(jsonSearch);
				}
			},
			refreshSamples : function(){
				if(this.form.projectCodes && this.form.projectCodes.length>0){
					lists.refresh.samples({projectCodes:this.form.projectCodes});
				}
			},
			changeProject : function(){
				if(this.form.project){
					lists.refresh.samples({projectCode:this.form.project.code});
				}else{
					lists.clear("samples");
				}

				if(this.form.type){
					this.search();
				}
			},

			changeProcessType : function(){
				if(this.form.processCategory){
					this.search();
				}else{
					this.form.processType = undefined;	
				}
			},

			changeProcessCategory : function(){
				this.form.processTypeCode = undefined;
				if(this.form.processCategory){
					lists.refresh.processTypes({"categoryCode":this.form.processCategory});
				}
			},
			/**
			 * initialise the service
			 */
			init : function($routeParams, datatableConfig){
				initListService();

				datatableConfig.messages = {
						transformKey: function(key, args) {
	                        return Messages(key, args);
	                    }
				};
				// to avoid to lost the previous search
				if(datatableConfig && angular.isUndefined(mainService.getDatatable())){				
					this.datatable = datatable(datatableConfig);
					mainService.setDatatable(this.datatable);
					this.datatable.setColumnsConfig(getColumns());
				}else if(angular.isDefined(mainService.getDatatable())){
						this.datatable = mainService.getDatatable();
				}
				
				if(angular.isDefined(mainService.getForm())){
					searchService.form = mainService.getForm();
				}else{
					searchService.resetForm();						
				}

				if(angular.isDefined($routeParams)){
					this.setRouteParams($routeParams);
				}
			}
	};

	return searchService;				
}]);
