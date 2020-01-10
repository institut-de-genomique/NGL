"use strict"

angular.module('home').controller('SwitchIndexSearchCtrl', ['$scope', '$filter', '$http', 'lists','mainService','tabService', 'messages','datatable',
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
			        	 "header":"Proprerty",
			        	 "property":"contentPropertyNameUpdated",
			        	 "order":true,
						 "edit":false,
						 "type":"text",
			        	 "position":6
			         },
			         {
			        	 "header":"Current value",
			        	 "property":"currentValue",
			        	 "order":true,
						 "edit":false,
						 "type":"text",
			        	 "position":7
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
			
			resetSampleCodes : function(){
				this.form.sampleCode = undefined;									
			},
		
			refreshSamples : function(){
				if(this.form.projectCode){
					lists.refresh.samples({projectCodes:[this.form.projectCode]});
				}
			},
			
			refreshReadSets : function(){
				if(this.form.projectCode){
					lists.refresh.readSets({projectCode:this.form.projectCode});
				}
			},
			
			reset : function(){
				this.form = {};									
			},
			
			updateForm : function(){
				if(!this.form.collectionNames){
					this.form.collectionNames = ["ngl_sq.Container","ngl_sq.Process","ngl_sq.Experiment","ngl_bi.ReadSetIllumina"];
					this.form.contentPropertyNameUpdated = "tag";
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
				
			}
			
	};
	
	//init
	if(angular.isUndefined($scope.getHomePage())){
		mainService.setHomePage('search');
		tabService.addTabs({label:Messages('admin.supports.switch-index.tabs.search'),href:jsRoutes.controllers.admin.supports.tpl.Supports.home('switch-index').url,remove:false});
		tabService.activeTab(0);
	}
	$scope.messages = messages();
	
	var dtConfigSQ = angular.copy(datatableConfig);
	dtConfigSQ.columns.push({
		   	 "header":"New value",
			 "property":"newValue",
			 "order":true,
			 "edit":true,
			 "editTemplate":'<input class="form-control" type="text" #ng-model typeahead="tag.code as tag.name for tag in searchService.lists.getTags() | filter:$viewValue | limitTo:20" typeahead-min-length="1" />',
			 "type":"text",
			 "position":8
			},
			{
		   	 "header":"Action",
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
	   	 "header":"New value",
		 "property":"newValue",
		 "order":true,
		 "edit":true,
		 "editTemplate":'<input class="form-control" type="text" #ng-model typeahead="tag.code as tag.name for tag in searchService.lists.getTags() | filter:$viewValue | limitTo:20" typeahead-min-length="1" />',
		 "type":"text",
		 "position":8
		},
		{
		   	 "header":"ReadSet to switch",
			 "property":"readSetToSwitchCode",
			 "order":true,
			 "edit":true,
			 "editTemplate":'<div class="form-control" bt-select #ng-model filter="true" placeholder="'+Messages("search.placeholder.readsets")+'" bt-options="readset.code as readset.code for readset in searchService.lists.getReadSets()" ng-focus="searchService.refreshReadSets()"></div>',
			 "type":"text",
			 "position":9
			},
		{
	   	 "header":"Action",
		 "property":"action",
		 "order":true,
		 "edit":true,
		 "choiceInList":true,
		 "possibleValues":[{"code":"exchange","name":"Switch"}],
		 "type":"text",
		 "position":10
		}
	);
	$scope.searchService.datatableBI =  datatable(dtConfigBI);
	
}]);