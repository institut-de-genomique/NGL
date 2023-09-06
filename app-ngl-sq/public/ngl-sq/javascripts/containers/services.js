"use strict";

angular.module('ngl-sq.containersServices', []).
factory('containersSearchService', ['$rootScope', '$http', 'mainService', 'lists', 'datatable', function($rootScope, $http, mainService, lists, datatable){
	//var tags = [];
	var getColumnsDefault = function(){
		var columns = [];
		columns.push({
			"header":Messages("containers.table.supportCode"),
			"property":"support.code",
			"order":true,
			"hide":true,
			"position":2, // NGL-3445 1=>2 (!! changer aussi la reportingConfiguration CNG "Container Config.")
			"type":"text",
			"group":true,
			"groupMethod": "count:true"
		});
		columns.push({
			"header":Messages("containers.table.supportCategoryCode"),
			"property":"support.categoryCode",
			"filter":"codes:'container_support_cat'",
			"order":true,
			"position":1, // NGL-3445 2=>1 (!! changer aussi la reportingConfiguration CNG "Container Config.")
			"hide":true,
			"type":"text",
			"groupMethod":"unique"
		});
		
		columns.push({
			"header":Messages("containers.table.support.line"),
			"property":"support.line",
			"order":true,
			"hide":true,
			"position":3,
			"type":"text"
		});
		columns.push({
			"header":Messages("containers.table.support.column"),
			"property":"support.column*1",
			"order":true,
			"hide":true,
			"position":4,
			"type":"number"
		});		
		columns.push({
			"header":Messages("containers.table.code"),
			"property":"code",
			"order":true,
			"position":5,
			"type":"text",
			"render":"<div list-resize='cellValue | stringToArray | unique' ' list-resize-min-size='2'>",
			"groupMethod":"collect"
		});
		columns.push({
			"header":Messages("containers.table.projectCodes"),
			"property":"projectCodes",
			"order":false,
			"hide":true,
			"group":true,
			"position":6,
			"filter":"unique",
			"render":"<div list-resize='cellValue' list-resize-min-size='2'>",
			"type":"text",
			"groupMethod":"collect"
		});			
		columns.push({
			"header":Messages("containers.table.sampleCodes.length"),
			"property":"sampleCodes.length",
			"order":true,
			"hide":true,
			"position":7,
			"type":"number",
			"groupMethod":"sum"
		});
		columns.push({
			"header":Messages("containers.table.sampleCodes"),
			"property":"sampleCodes",
			"order":true,
			"hide":true,
			"position":8,
			"type":"text",
			"render":"<div list-resize='cellValue|unique' list-resize-min-size='3'>",
			"groupMethod":"collect"
			
		});
		columns.push({
			"header":Messages("containers.table.contents.length"),
			"property":"contents.length",
			"order":true,
			"hide":true,
			"position":9,
			"type":"number",
			"groupMethod":"sum"
				
		});
		columns.push({
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
			
		});
		columns.push({
			"header":Messages("containers.table.fromTransformationTypeCodes"),
			"property":"fromTransformationTypeCodes",
			"order":true,
			"hide":true,
			"position":5.5,
			"type":"text",
			"render":"<div list-resize='cellValue | unique' list-resize-min-size='3'>",
			"filter":"unique | codes:\"type\"",
			"groupMethod":"collect"
		});	
		columns.push({
			"header":Messages("containers.table.concentration.value"),
			"property":"concentration.value",
			"order":true,
			"hide":true,
			"edit":(mainService.getHomePage() === 'search')?true:false,
			"position":11.1,
			"format":3,
			"type":"number",
			"groupMethod":"unique",
			"editDirectives":"  udt-change='searchService.computeQuantity(value)'"
		});	
		columns.push({
			"header":Messages("containers.table.concentration.unit"),
			"property":"concentration.unit",
			"order":true,
			"hide":true,
			"edit":(mainService.getHomePage() === 'search')?true:false,
			"editTemplate":'<div bt-select class="form-control" #ng-model   udt-change="searchService.computeQuantity(value)" bt-options="unit.code as unit.name for unit in searchService.getUnits(\'concentration\')" auto-select></div>',
			"choiceInList":true,
			"position":11.2,
			"type":"text",
			"groupMethod":"unique",
			"editDirectives":""
		});
		columns.push({
			"header":Messages("containers.table.creationDate"),
			"property":"traceInformation.creationDate",
			"order":true,
			"hide":true,
			"position":14,			
			"type":"date",
			"groupMethod":"unique"
				});
		columns.push({
			"header":Messages("containers.table.createUser"),
			"property":"traceInformation.createUser",
			"order":true,
			"hide":true,
			"position":15,
			"type":"text",
			"groupMethod":"unique"
		});
		columns.push({
			"header":Messages("containers.table.storageCode"),
			"property":"support.storageCode",
			"order":true,
			"hide":true,
			"type":"text",
			"edit":false,
			"position":15.5,
			"groupMethod":"unique"
				
		});
		columns.push({
			"header":Messages("containers.table.processTypeCodes"),
			"property":"processTypeCodes",
			"filter":"codes:'type'",
			"order":false,
			"hide":true,
			"type":"text",
			"position":15.6,
			"render":"<div list-resize='cellValue' list-resize-min-size='3' vertical>",
			"groupMethod":"collect"
		});
		/*
		columns.push({
				"header":Messages("containers.table.processCodes"),
				"property":"processCodes",
				"order":false,
				"hide":true,
				"type":"text",
				"position":16,
				"render":"<div list-resize='cellValue' list-resize-min-size='3' vertical>",
				"groupMethod":"collect"
			});
		*/
		columns.push({
			"header":Messages("containers.table.state.code"),
			"property":"state.code",
			"order":true,
			"hide":true,
			"type":"text",
			"edit":(mainService.getHomePage() === 'state')?true:false,
			"position":12,
			"choiceInList": true,
			"listStyle":"bt-select",
			"possibleValues":"searchService.getStates(value)", 
			"filter":"codes:'state'",
			"group": true,
			"groupMethod":"unique"
		});
		
		
		columns.push({
			"header" : Messages("containers.table.resolutionCodes"),
			"property" : "state.resolutionCodes",
			"filter":"codes:'resolution'",
			"position" : 12.5,
			"order" :true,
			"edit" : true,
			"hide" : true,
			"choiceInList":true,
		    "listStyle":"bt-select-multiple",
		    "possibleValues":"searchService.lists.get('containerResolutions')",
			"type" : "text",
			"group": true
		});
		
		columns.push({
			"header":Messages("containers.table.valid"),
			"property":"valuation.valid",
			"order":true,
			"type":"text",
			"edit":true,
			"hide":true,
			"position":13,
			"choiceInList": true,
			"listStyle":"bt-select",
			"possibleValues":"searchService.lists.getValuations()", 
			"filter":"codes:'valuation'"
		});
		
		return columns;
	};
	
	var isInit = false;

	var initListService = function(){
		if(!isInit){
			lists.refresh.containerSupportCategories();
			lists.refresh.containerCategories();
			lists.refresh.experimentTypes({categoryCodes:["transformation"], withoutOneToVoid:false},"transformation");
			lists.refresh.experimentTypes({categoryCode:"purification"}, "purification");
			lists.refresh.experimentTypes({categoryCode:"qualitycontrol"}, "qualitycontrol");
			lists.refresh.experimentTypes({categoryCode:"transfert"}, "transfert");
			lists.refresh.containerSupports();
			lists.refresh.projects();
			lists.refresh.processCategories();
			lists.refresh.states({objectTypeCode:"Container"});
			lists.refresh.users();
			lists.refresh.reportConfigs({pageCodes:["containers"+"-"+mainService.getHomePage()]});
			lists.refresh.reportConfigs({pageCodes:["containers-addcolumns"]}, "containers-addcolumns");
			lists.refresh.filterConfigs({pageCodes:["containers-search-addfilters"]}, "containers-search-addfilters");
			lists.refresh.resolutions({"objectTypeCode":"Container"}, "containerResolutions");
			lists.refresh.values({propertyDefinitionCode:'rnaEvaluation'},"rnaEvaluation");
			isInit=true;
		}
	};

	var searchService = {
			getColumns:getColumnsDefault,
			getDefaultColumns:getColumnsDefault,
			datatable:undefined,
			isRouteParam:false,
			lists : lists,
			form:undefined,
			mainFilters:[],
			additionalFilters:[],
			additionalColumns:[],
			selectedAddColumns:[],
			authorizedStates : null,
			units : {
				//NGL-3555 Cacher fonctionnalite MGI //NGL-3556 Reactivation MGI
				 "volume":[{"code":"µL","name":"µL"}],	
				 "concentration":[{"code":"ng/µl","name":"ng/µl"},{"code":"nM","name":"nM"},{"code":"fmol/µl","name":"fmol/µl"}],	
				 "quantity":[{"code":"ng","name":"ng"},{"code":"nmol","name":"nmol"}],	
				 "size":[{"code":"pb","name":"pb"},{"code":"nt","name":"nt"}]
			},
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
			getRIN: function(cellValue) { 
				if (cellValue) {	 				
					var latestExp = cellValue.filter(function (a) {
						return a.typeCode == "chip-migration-rna-evaluation";
					}).reduce(function (a, b) {
						return a.index > b.index ? a : b;
					}, "");

					if (latestExp && latestExp["properties"]["rin"]) {
						return latestExp["properties"]["rin"]["value"];
					} 
			    }
			},
			getColor: function(cellValue) { 
				if (cellValue) {	 				
					var latestExp = cellValue.filter(function (a) {
						return a.typeCode == "chip-migration-rna-evaluation";
					}).reduce(function (a, b) {
						return a.index > b.index ? a : b;
					}, "");

					if (latestExp) {
						if (latestExp["properties"]["rnaEvaluation"]) {
							return latestExp["properties"]["rnaEvaluation"]["value"];
						} 
					} else {
						// return "Mise à jour non possible car aucun QC de type \"chip-migration-rna-evaluation\" n'existe.";
					}
			    }
			},
			// Si ce n'était pas un tableau la couleur choisie se propagerait sur toutes les lignes du datatable.
			colorChoosed: [],
			/**
			 * Méthode exécutée lorsqu'une couleur sur une ligne est sélectionnée dans le datatable.
			 * @param cellValue La valeur de la ligne du datatable.
			 */
			updateContainerEvalARN:function(cellValue) {
				if (cellValue == undefined) {
					var that = this;
						
					// On met à jour notre tableau de couleurs. Sans cette mise à jour, les données sont mises à jour mais pas l'interface.
					this.datatable.displayResult.filter(function (cont) {
						return cont.line.edit;
					}).forEach(function (r) {
						that.colorChoosed[r.data.code] = that.colorChoosed[undefined];
					});
				} 
			},
			initAuthorizedStates:function(){
				if(null === this.authorizedStates){
					var states = this.lists.getStates();
					if(null !== states && undefined !== states){
						this.authorizedStates = {'IW-P':[],'IS':[],'UA':[],'A-QC':[],'A-TM':[],'A-TF':[],'A-PF':[]};
						states.forEach(function(state){
							if(state.code === 'IS'){
								this['IW-P'].push(state);
								this['UA'].push(state);
								this['A-QC'].push(state);
								this['A-TM'].push(state);
								this['A-TF'].push(state);
								this['A-PF'].push(state);
							}else if(state.code === 'UA'){
								this['IW-P'].push(state);
								this['IS'].push(state);
								this['A-QC'].push(state);
								this['A-TM'].push(state);
								this['A-TF'].push(state);
								this['A-PF'].push(state);
							}else if(state.code === 'IW-P'){
								this['UA'].push(state);
								this['IS'].push(state);
							}else if(state.code === 'A-TF'){
								this['A-QC'].push(state);
								this['A-TM'].push(state);
								this['A-PF'].push(state);
							}else if(state.code === 'A-QC'){
								this['A-TF'].push(state);
								this['A-TM'].push(state);
								this['A-PF'].push(state);
							}else if(state.code === 'A-TM'){
								this['A-TF'].push(state);
								this['A-QC'].push(state);
								this['A-PF'].push(state);
							}else if(state.code === 'A-PF'){
								this['A-TF'].push(state);
								this['A-QC'].push(state);
								this['A-TM'].push(state);
							}
						}, this.authorizedStates);
					}
				}
			},
			getStates : function(value){
				/*
				this.initAuthorizedStates();
				if(value && value.data){
					return this.authorizedStates[value.data.state.code];
				}else{
					return this.lists.getStates();
				}
				*/
				return this.lists.getStates();
			},
				
			getUnits : function(unitType){
				return this.units[unitType];
			},
			updateForm : function(){
				this.form.includes = [];
				if(this.reportingConfiguration){
					for(var i = 0 ; i < this.reportingConfiguration.columns.length ; i++){
						if(this.reportingConfiguration.columns[i].queryIncludeKeys && this.reportingConfiguration.columns[i].queryIncludeKeys.length > 0){
							this.form.includes = this.form.includes.concat(this.reportingConfiguration.columns[i].queryIncludeKeys);
						}else{
							this.form.includes.push(this.reportingConfiguration.columns[i].property.replace('.value','').replace(".unit", ''));
						}
					}
				}else{
					this.form.includes = ["default"];
				}
				
				
				//this.form.includes = ["default"];
				for(var i = 0 ; i < this.selectedAddColumns.length ; i++){
					//remove .value if present to manage correctly properties (single, list, etc.)
					if(this.selectedAddColumns[i].queryIncludeKeys && this.selectedAddColumns[i].queryIncludeKeys.length > 0){
						this.form.includes = this.form.includes.concat(this.selectedAddColumns[i].queryIncludeKeys);
					}else{
						this.form.includes.push(this.selectedAddColumns[i].property.replace('.value','').replace(".unit", ''));
					}
					
				}
				
				//override with reportingConfiguration
				if(this.reportingConfiguration && this.reportingConfiguration.queryConfiguration 
						&& this.reportingConfiguration.queryConfiguration.query){
					this.form.reportingQuery = this.reportingConfiguration.queryConfiguration.query;
					this.form.reporting=true;
					this.form.aggregate=false;
					if(this.reportingConfiguration.queryConfiguration.type === 'aggregate'){
						this.form.aggregate=true;
					}
					
					for(var key in this.aggregateForm){
						//replace "#someThing" by "somethingValue" but in case of number does not work because number cannot be inside a double quote
						this.form.reportingQuery = this.form.reportingQuery.replace(new RegExp("#"+key,"g"),this.aggregateForm[key]);
					}					
				}else if(this.form.reportingQuery){
					this.form.reportingQuery.trim();
					if(this.form.reportingQuery.length > 0){
						this.form.reporting=true;
					}else{
						this.form.reporting=false;
					}
				}else{
					this.form.reporting=false;
				}
			},
			convertForm : function(){
				var _form = angular.copy(this.form);
				//if(_form.fromDate)_form.fromDate = moment(_form.fromDate, Messages("date.format").toUpperCase()).valueOf();
				//if(_form.toDate)_form.toDate = moment(_form.toDate, Messages("date.format").toUpperCase()).valueOf();		
				return _form

			},

			resetForm : function(){
				this.form = {};									
			},

			resetTextareas : function(){
				Array.from(document.getElementsByTagName('textarea')).forEach(function(element) { 
					var elementScope = angular.element(element).scope();
					if(elementScope.textareaValue){
						elementScope.textareaValue = null;
					}
				});
			},
			
			resetSampleCodes : function(){
				this.form.sampleCodes = [];									
			},

			computeQuantity : function(value){
				var container = value.data;
				var concentration = container.concentration;
				var volume = container.volume;
				
				if(concentration && concentration.value && volume && volume.value){
					var result = volume.value * concentration.value;
					if(angular.isNumber(result) && !isNaN(result)){
						var quantity = {};
						quantity.value = Math.round(result*10)/10;
						quantity.unit = (concentration.unit === 'nM')?'fmol':'ng';
						container.quantity = quantity;
					}else {
						container.quantity= {"_type": "single","value": null,"unit": null};
						
					}
				}else {
					container.quantity= {"_type": "single","value": null,"unit": null};
				}
				
				if(concentration && (concentration.value == undefined || concentration.value == null))
					concentration.unit=null;
				if(volume && (volume.value == undefined || volume.value == null))
					volume.unit=null;
			},
			
			search : function(callback){
				this.updateForm();
				mainService.setForm(this.form);				
				this.datatable.search(this.convertForm(), callback);
				
			},
			refreshSamples : function(){
				if(this.form.projectCodes && this.form.projectCodes.length>0){
					lists.refresh.samples({projectCodes:this.form.projectCodes});
				}else{
					lists.clear('samples');
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
			initAdditionalColumns : function(){
				this.additionalColumns=[];
				this.selectedAddColumns=[];
				
				if(lists.get("containers-addcolumns") && lists.get("containers-addcolumns").length === 1){
					var formColumns = [];
					//we used the order in the document to order column in display and not the position value !!!!
					var allColumns = angular.copy(lists.get("containers-addcolumns")[0].columns);
					var nbElementByColumn = Math.ceil(allColumns.length / 5); //5 columns
					for(var i = 0; i  < 5 && allColumns.length > 0 ; i++){
						formColumns.push(allColumns.splice(0, nbElementByColumn));
					}
					//complete to 5 five element to have a great design 
					while(formColumns.length < 5){
						formColumns.push([]);
					}
					this.additionalColumns = formColumns;
				}
			},
			getAddColumnsToForm : function(){
				if(this.additionalColumns.length === 0){
					this.initAdditionalColumns();
				}
				return this.additionalColumns;
			},
			addColumnsToDatatable : function(){
				//this.reportingConfiguration = undefined;
				//this.reportingConfigurationCode = undefined;
				
				this.selectedAddColumns = [];
				for(var i = 0 ; i < this.additionalColumns.length ; i++){
					for(var j = 0; j < this.additionalColumns[i].length; j++){
						if(this.additionalColumns[i][j].select){
							this.selectedAddColumns.push(this.additionalColumns[i][j]);
						}
					}
				}
				if(this.reportingConfigurationCode){
					this.datatable.setColumnsConfig(this.reportingConfiguration.columns.concat(this.selectedAddColumns));
				}else{
					this.datatable.setColumnsConfig(this.getDefaultColumns().concat(this.selectedAddColumns));
				}
				if(this.datatable.isData()){
					this.search();
				}
			},	
			resetDatatableColumns : function(){
				this.updateColumn();
				//this.initAdditionalColumns();
				//this.datatable.setColumnsConfig(this.getDefaultColumns());
				if(this.datatable.isData()){
					this.search();
				}
			},
			/**
			 * Update column when change reportingConfiguration
			 */
			updateColumn : function(){
				this.initAdditionalColumns();
				if(this.reportingConfigurationCode){
					$http.get(jsRoutes.controllers.reporting.api.ReportingConfigurations.get(this.reportingConfigurationCode).url,{searchService:this, datatable:this.datatable})
							.success(function(data, status, headers, config) {
								config.searchService.reportingConfiguration = data;
								if(config.searchService.lists.get('reportConfigs').length > 1 && config.datatable.isData()){
									config.searchService.search();
								}
								config.datatable.setColumnsConfig(data.columns);
								config.searchService.mainFilters = config.searchService.organizeFilters(data.filters);
					});
				}else{
					this.reportingConfiguration = undefined;
					this.initAdditionalFilters();
					this.mainFilters = [];							
					this.datatable.setColumnsConfig(this.getDefaultColumns());
					if(this.datatable.isData()){
						this.search();
					}
				}
				
			},
			initAdditionalFilters:function(){
				this.additionalFilters=[];
				
				if(lists.get("containers-search-addfilters") && lists.get("containers-search-addfilters").length === 1){
					var allFilters = angular.copy(lists.get("containers-search-addfilters")[0].filters);
					
					/* add static filters here*/
					allFilters.push({property:"comments.comment",html:"<textarea class='form-control' ng-model='searchService.form.commentRegex' placeholder='"+Messages("search.placeholder.commentRegex")+"' title='"+Messages("search.placeholder.commentRegex")+"'></textarea>",position:allFilters.length+1});
					
					this.additionalFilters = this.organizeFilters(allFilters);					
				}
			},
			organizeFilters : function(allFilters){
				if(allFilters !== undefined && allFilters !== null && allFilters.length > 0){
				
					var formFilters = [];
					var nbElementByColumn = Math.ceil(allFilters.length / 5); //5 columns
					for(var i = 0; i  < 5 && allFilters.length > 0 ; i++){
						formFilters.push(allFilters.splice(0, nbElementByColumn));
					}
					//complete to 5 five element to have a great design 
					while(formFilters.length < 5){
						formFilters.push([]);
					}
						
					return formFilters;
				}else{
					return [];
				}
			},
			getAddFiltersToForm : function(){
				if(this.additionalFilters.length === 0){
					this.initAdditionalFilters();
				}
				return this.additionalFilters;
			},	
			
			getMainFiltersToForm:function(){
				return this.mainFilters;	
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
					searchService.datatable = datatable(datatableConfig);
					mainService.setDatatable(searchService.datatable);
					searchService.datatable.setColumnsConfig(getColumnsDefault());
				}else if(angular.isDefined(mainService.getDatatable())){
					searchService.datatable = mainService.getDatatable();
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
}
]);