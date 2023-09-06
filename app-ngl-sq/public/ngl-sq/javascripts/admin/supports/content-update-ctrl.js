"use strict"

angular.module('home').controller('ContentUpdateCtrl', ['$scope', '$filter', '$http', 'lists','mainService','tabService', 'messages','datatable',
                                                 function($scope, $filter, $http, lists,mainService,tabService, messages, datatable) {
	
	
	
	var datatableConfig = {
			name:"swichIndex",
			columns:[
			  		 {
			        	 "header":"Code",
			        	 "property":"code",
			        	 "order":true,
						 "edit":false,
						 "type":"text",
			        	 "position":1
			         },
			         {
			        	 "header":"Collection",
			        	 "property":"collectionName",
			        	 "order":true,
						 "edit":false,
						 "type":"text",
			        	 "position":2
			         },
			         {
			        	 "header":"Type",
			        	 "property":"typeCode",
			        	 "filter":"codes:'type'",
			        	 "order":true,
						 "edit":false,
						 "type":"text",
			        	 "position":3
			         },
			         {
			        	 "header":"Project",
			        	 "property":"projectCode",
			        	 "order":true,
						 "edit":false,
						 "type":"text",
			        	 "position":4
			         },
			         {
			        	 "header":"Sample",
			        	 "property":"sampleCode",
			        	"order":true,
						 "edit":false,
						 "type":"text",
			        	 "position":5
			         },			         
			         {
			        	 "header":Messages("admin.supports.table.property"),
			        	 "property":"contentPropertyNameUpdated",
			        	 "order":true,
						 "edit":false,
						 "type":"text",
			        	 "position":6
			         },
			         {
			        	 "header":Messages("admin.supports.table.currentValue"),
			        	 "property":"currentValue",
			        	 "order":true,
						 "edit":false,
						 "type":"text",
			        	 "position":7
			         },
			         {
			        	 "header":Messages("admin.supports.table.nbOccurences"),
			        	 "property":"nbOccurrences",
			        	 "order":true,
						 "edit":false,
						 "type":"number",
			        	 "position":8
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
				mode:'local',  
				active:true,
				by:'code'
			},
			remove:{
				active: false
			},
			save:{
				active:true,
	        	mode:'remote',
	        	url: function(data){return jsRoutes.controllers.admin.supports.api.NGLObjects.update(data.code).url;},				
				method:'put'
	        		
			},
			edit:{
				active: true,
				columnMode:true
			},
			messages:{
				active:true,
				columnMode:true
			},
			exportCSV:{
				active:true
			},			
			showTotalNumberRecords:true
	};
	
	$scope.searchService = {
			
			form : {},
			lists : lists,
			
			datatableSQ : undefined,
			datatableBI : undefined,
			searchInProgress:false,
			resetSampleCodes : function(){
				this.form.sampleCode = undefined;
			},
		
			refreshSamples : function(){
				if(this.form.projectCode){
					this.lists.refresh.samples({projectCodes:[this.form.projectCode]});
				}
			},
			
			refreshReadSets : function(){
				if(this.form.projectCode){
					this.lists.refresh.readSets({projectCode:this.form.projectCode});
				}
			},
			
			refreshPropertyDefValuesCodes:function(){
				this.form["contentProperties"] = undefined;
				this.lists.clear("values");
				if(this.form.contentPropertyNameUpdated){
					this.lists.refresh.values({propertyDefinitionCode:this.form.contentPropertyNameUpdated},"values");
				}
				
			},
			
			
			reset : function(){
				this.form = {};	
				this.lists.clear("values");
			},
			
			updateForm : function(){
				if(!this.form.collectionNames){
					this.form.collectionNames = ["ngl_sq.Container","ngl_sq.Process","ngl_sq.Experiment","ngl_bi.ReadSetIllumina"];
				}
			},
			isSearchInProgress : function(){
				return this.searchInProgress;
			},			
			search : function(){
				$scope.messages.clear();
				$scope.searchService.searchInProgress=true;
				$scope.searchService.datatableSQ.setData([]);
		 		$scope.searchService.datatableBI.setData([]);
				this.updateForm();
				 $http.get(jsRoutes.controllers.admin.supports.api.NGLObjects.list().url,{params:this.form})
				 	.success(function(results){
				 		
				 		var data = {sq:[], bi:[]};
				 		
				 		angular.forEach(results, function(result){
				 			if(result.collectionName === 'ngl_bi.ReadSetIllumina'){
				 				this.bi.push(result);
				 			}else{
				 				this.sq.push(result);
				 			}
				 		},data);
				 		
				 		
				 		$scope.searchService.datatableSQ.setData(data.sq);
				 		$scope.searchService.datatableBI.setData(data.bi);
				 		$scope.searchService.searchInProgress=false;
				 	}).error(function(data){
				 		$scope.messages.setError("get");
				 		$scope.messages.setDetails(data);
				 		$scope.messages.showDetails=true;
				 		$scope.searchService.searchInProgress=false;
				 	});
				
			},

			getContentPropertyDefinitions: function() {
				var contentProperties = this.lists.getPropertyDefinitions({"levelCode":"Content"});
				return contentProperties ? distinct(contentProperties) : contentProperties;

				//---

				function distinct(propertyDefinitions) {
					return propertyDefinitions.reduce(distinctReducer, aggregator());

					//---

					function distinctReducer(distincts, propertyDefinition) {
						if(!distincts.contains(propertyDefinition)) distincts.push(propertyDefinition);
						return distincts;
					}

					function aggregator() {
						var array = [];
						array.contains = function(propertyDefinition) {
							return this.some(function(value){
								return propertyDefinition.code === value.code;
							});
						};
						return array;
					}
				}
			}
			
	};
	
	//init
	// FDS 10/02/2020  correction du label; mais le template reste le meme????
	if(angular.isUndefined($scope.getHomePage())){
		mainService.setHomePage('search');
		tabService.addTabs({label:Messages('admin.supports.content-update.tab.search'),href:jsRoutes.controllers.admin.supports.tpl.Supports.home('switch-index').url,remove:false});
		tabService.activeTab(0);
	}
	$scope.messages = messages();
	
	var dtConfigSQ = angular.copy(datatableConfig);
	dtConfigSQ.columns.push({
		   	 "header":Messages("admin.supports.table.newValue"),
			 "property":"newValue",
			 "order":true,
			 "edit":true,
			 "editTemplate":""
	   			+"<div ng-show=\"searchService.lists.get('values').length > 0\" class='form-control' bt-select  #ng-model filter='true' placeholder='Property definition value' bt-options='v.code as v.code for v in searchService.lists.get(\"values\")' ></div>"
	   			+"<input ng-show=\"searchService.lists.get('values').length == 0\" type=\"text\" class='form-control' #ng-model />",	   			 
			 "type":"text",
			 "position":8
			},
			{
		   	 "header":Messages("admin.supports.table.action"),
			 "property":"action",
			 "order":true,
			 "edit":true,
			 "choiceInList":true,
			 "possibleValues":[{"code":"replace","name":"Replace"}],
			 "type":"text",
			 "position":9
			}
		);
	$scope.searchService.datatableSQ =  datatable(dtConfigSQ);
	
	var dtConfigBI = angular.copy(datatableConfig);
	dtConfigBI.columns.push({
	   	 "header":Messages("admin.supports.table.newValue"),
		 "property":"newValue",
		 "order":true,
		 "edit":true,
		 "editTemplate":""
	   			+"<div ng-show=\"searchService.lists.get('values').length > 0\" class='form-control' bt-select  #ng-model filter='true' placeholder='Property definition value' bt-options='v.code as v.code for v in searchService.lists.get(\"values\")' ></div>"
	   			+"<input ng-show=\"searchService.lists.get('values').length == 0\" type=\"text\" class='form-control' #ng-model />",	   			 
			 "type":"text",
		 "position":8
		},		
		{
	   	 "header":Messages("admin.supports.table.action"),
		 "property":"action",
		 "order":true,
		 "edit":true,
		 "choiceInList":true,
		 "possibleValues":[{"code":"replace","name":"Replace"}],
		 "type":"text",
		 "position":10
		}
	);
	$scope.searchService.datatableBI =  datatable(dtConfigBI);
	
}]);