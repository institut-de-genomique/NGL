 "use strict";
 
 angular.module('ngl-reagents.ReagentReceptionsServices', []).
	factory('reagentReceptionsSearchService', ['$http', 'mainService', 'lists', 'datatable', function($http, mainService, lists, datatable){
		
		var getDefaultColumns = function(){
			const columns = [];
			
			columns.push({	property:"receptionDate",
							header: Messages("reagent.receptions.receptionDate"),
							type :"date",
							order:true,
							hide: true,
				    	  	position:0});
			columns.push({	property:"catalogKit.name",
							header: Messages("reagent.receptions.catalogKit.name"),
							type :"text",
							order:true,
							hide: true,
							group:true,
							groupMethod:"collect:true",
							render: "<div list-resize='cellValue'list-resize-min-size='3' vertical>",
				    	  	position:1});
			columns.push({	property:"catalogKit.catalogRefCode",
							header: Messages("reagent.receptions.catalogKit.catalogRefCode"),
							type :"text",
							order:true,
							hide: true,
							group:true,
							groupMethod:"collect:true",
							render: "<div list-resize='cellValue'list-resize-min-size='3' vertical>",
				    	  	position:2});
		    columns.push({	property:"catalogKit.experimentTypeCodes",
						    header: Messages("reagent.receptions.catalogKit.experimentTypeCodes"),
						    type :"text",
						    order:true,
						    hide: true,
							groupMethod:"collect:true",
							render: "<div list-resize='cellValue'list-resize-min-size='3' vertical>",
							position:3});
			columns.push({	property:"catalogBox.name",
							header: Messages("reagent.receptions.catalogBox.name"),
							type :"text",
							order:true,
							hide: true,
				    	  	position:4});
			columns.push({	property:"catalogBox.catalogRefCode",
							header: Messages("reagent.receptions.catalogBox.catalogRefCode"),
							type :"text",
							order:true,
							hide: true,
				    	  	position:4});
			columns.push({	property:"providerCode",
							header: Messages("reagent.receptions.providerCode"),
							type :"text",
							order:true,
							hide: true,
							groupMethod:"unique",
				    	  	position:5});
			columns.push({	render: "<div>{{ searchService.getStorageConditions(value.data) }}</div>",
							header: Messages("reagent.receptions.catalogBox.storageConditions"),
							type :"text",
							hide: true,
				    	  	position:6});
			columns.push({	property:"category",
							header: Messages("reagent.receptions.category"),
							type :"text",
							order:true,
							hide: true,
				    	  	position:7});
			columns.push({	property:"code",
							"header":Messages("reagent.receptions.code"),
				    	  	type :"text",		    	  	
				    	  	order:true,
							groupMethod:"count",
				    	  	position:8});
			columns.push({	property:"catalog.name",
							header: Messages("reagent.receptions.catalog.name"),
							type :"text",
							hide: true,
							position:9});
			columns.push({	property:"catalog.catalogRefCode",
							header: Messages("reagent.receptions.catalog.catalogRefCode"),
							type :"text",
							order:true,
							hide: true,
							position:9});
			columns.push({	property:"batchNumber",
							header: Messages("reagent.receptions.batchNumber"),
							type :"text",
							order:true,
							hide: true,
				    	  	position:10});
			columns.push({	property:"fromProviderId",
							header: Messages("reagent.receptions.fromProviderId"),
							type :"text",
							order:true,
							hide: true,
							position:11});
			if(mainService.getHomePage() == 'search'){
				columns.push({	property:"expirationDate",
								header: Messages("reagent.receptions.expirationDate"),
								type :"date",
								order:true,
								hide: true,
								edit: true,
								position:12});
				columns.push({	property:"workLabel",
								header: Messages("reagent.receptions.workLabel"),
								type :"text",
								order:true,
								hide: true,
								edit: true,
								group:true,
								groupMethod:"collect:true",
								render: "<div list-resize='cellValue'list-resize-min-size='3' vertical>",
								position:13});
				columns.push({	property:"comments",
								header: Messages("reagent.receptions.comments"),
								type :"text",
								order:true,
								hide: true,
								filter: 'getArray:"comment"',
								render: "<div list-resize='cellValue' list-resize-min-size='3'>",
								position:14});
				columns.push({	property:"newComment",
								header: Messages("reagent.receptions.newComment"),
								type :"text",
								order:true,
								hide: true,
								edit: true,
								position:15});
				columns.push({	property:"state.code",
								filter: "codes:'state'",
								header: Messages("reagent.receptions.state"),
								type :"text",
								order:true,
								hide: true,
								position:16});
				columns.push({	property:"startUseDate",
								header: Messages("reagent.receptions.startUseDate"),
								type :"date",
								order:true,
								hide: true,
								edit: true,
								position:17});
				columns.push({	property:"endUseDate",
								header: Messages("reagent.receptions.endUseDate"),
								type :"date",
								order:true,
								hide: true,
								edit: true,
								position:18});
				columns.push({	property:"traceInformation.createUser",
								header: Messages("reagent.receptions.createUser"),
								type :"date",
								order:true,
								hide: true,
								position:19});
				columns.push({	property:"traceInformation.creationDate",
								header: Messages("reagent.receptions.creationDate"),
								type :"date",
								order:true,
								hide: true,
								position:20});
				columns.push({	property:"traceInformation.modifyUser",
								header: Messages("reagent.receptions.modifyUser"),
								type :"date",
								order:true,
								hide: true,
								position:21});
				columns.push({	property:"traceInformation.modifyDate",
								header: Messages("reagent.receptions.modifyDate"),
								type :"date",
								order:true,
								hide: true,
								position:22});
			} else if(mainService.getHomePage() == 'state'){
				columns.push({	property:"expirationDate",
								header: Messages("reagent.receptions.expirationDate"),
								type :"date",
								order:true,
								hide: true,
								position:12});
				columns.push({	property:"workLabel",
								header: Messages("reagent.receptions.workLabel"),
								type :"text",
								order:true,
								hide: true,
								group:true,
								groupMethod:"collect:true",
								render: "<div list-resize='cellValue'list-resize-min-size='3' vertical>",
								position:13});
				columns.push({	property:"comments",
								header: Messages("reagent.receptions.comments"),
								type :"text",
								order:true,
								hide: true,
								filter: 'getArray:"comment"',
								render: "<div list-resize='cellValue' list-resize-min-size='3'>",
								position:14});
				columns.push({	property:"state.code",
								filter: "codes:'state'",
								header: Messages("reagent.receptions.state"),
								type :"text",
								order:true,
								hide: true,
								edit: true,
								choiceInList: true,
								listStyle: 'bt-select',
								possibleValues: 'searchService.lists.getStates()',
								position:16});
				columns.push({	property:"startUseDate",
								header: Messages("reagent.receptions.startUseDate"),
								type :"date",
								order:true,
								hide: true,
								position:17});
				columns.push({	property:"endUseDate",
								header: Messages("reagent.receptions.endUseDate"),
								type :"date",
								order:true,
								hide: true,
								position:18});
				columns.push({	property:"traceInformation.createUser",
								header: Messages("reagent.receptions.createUser"),
								type :"date",
								order:true,
								hide: true,
								position:19});
				columns.push({	property:"traceInformation.creationDate",
								header: Messages("reagent.receptions.creationDate"),
								type :"date",
								order:true,
								hide: true,
								position:20});
				columns.push({	property:"traceInformation.modifyUser",
								header: Messages("reagent.receptions.modifyUser"),
								type :"date",
								order:true,
								hide: true,
								position:21});
				columns.push({	property:"traceInformation.modifyDate",
								header: Messages("reagent.receptions.modifyDate"),
								type :"date",
								order:true,
								hide: true,
								position:22});
			}
			
			return columns;
		};
		
		var isInit = false;
		
		var initListService = function(){
			if(!isInit){
				lists.refresh.states({objectTypeCode:"ReagentReception"});
				lists.refresh.experimentTypes({categoryCodes:["transfert","transformation","qualitycontrol","purification"]});
				lists.refresh.users();
				isInit=true;
			}
		};
		
		var searchService = {
				getDefaultColumns:getDefaultColumns,
				datatable:undefined,
				isRouteParam:false,
				lists : lists,
				form : undefined,
				
				setRouteParams:function($routeParams){
					let count = 0;
					for(var p in $routeParams){
						count++;
						break;
					}
					if(count > 0){
						this.isRouteParam = true;
						this.form = $routeParams;
					}
				},
				
				getStorageConditions: function(reception) {
					if(!reception) return;
					var boxCatalog = getBoxCatalog();
					if(!boxCatalog.storageConditions) return;
					return boxCatalog.storageConditions;

					function getBoxCatalog() {
						return reception.category === "Box" ? reception.catalog : reception.catalogBox;
					}

				},
				
				getReceptionCategories : function() {
					return this.categories;
				},
				
				convertForm : function(){
					return angular.copy(this.form);
				},
				
				resetForm : function(){
					this.form = {};	
				},

				resetTextareas : function(){
					Array.from(document.getElementsByTagName('textarea')).forEach(element => {
						var elementScope = angular.element(element).scope();
						if(elementScope.textareaValue){
							elementScope.textareaValue = null;
						}
					});
				},
				
				search : function(){
					mainService.setForm(this.form);
					this.datatable.search(this.convertForm());
				},
				
				isCategoryBox: function(){
					return this.form.category === 'box';
				},

				init : function($routeParams, datatableConfig){
					initListService();
					
					datatableConfig.messages = {
							transformKey: function(key, args) {
		                        return Messages(key, args);
		                    }
					};
					
					//to avoid to lost the previous search
					if(datatableConfig && angular.isUndefined(mainService.getDatatable())){
						searchService.datatable = datatable(datatableConfig);
						mainService.setDatatable(searchService.datatable);
						searchService.datatable.setColumnsConfig(getDefaultColumns());		
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
}]).
	factory('reagentReceptionsCreationService', ['$http', '$parse', 'messages', 'mainService', 'lists', function($http, $parse, messages, mainService, lists){
		
		var isInit = false;
		
		var initListService = function(){
			if(!isInit){
				isInit=true;
			}
		};
		
		var createService = {
				isRouteParam:false,
				lists : lists,
				reception : {type: "nanopore"},
				messages: messages(),
				
				setRouteParams:function($routeParams){
					let count = 0;
					for(var p in $routeParams){
						count++;
						break;
					}
					if(count > 0){
						this.isRouteParam = true;
						this.form = $routeParams;
					}
				},
				
				convertForm : function(){
					var _form = angular.copy(this.form);
					return _form
				},
				
				resetForm : function(){
					this.form = {};	
				},

				resetTextareas : function(){
					Array.from(document.getElementsByTagName('textarea')).forEach(element => {
						var elementScope = angular.element(element).scope();
						if(elementScope.textareaValue){
							elementScope.textareaValue = null;
						}
					});
				},
				
				isCategoryReagent: function(){
					return this.form.category === 'reagent';
				},
				
				save: function(){
					if(!this.reception.providerId) {
						createService.messages.clazz = "alert alert-danger";
						createService.messages.text = Messages("reagents.msg.error.providerId");
						createService.messages.open();	
					} else {
						$http.post(jsRoutes.controllers.reagents.api.Receptions.save().url,
						createService.reception)
						.then(function(result){
							createService.messages.setSuccess("save");
						}, function(error) {
							createService.messages.setError("save");	
							createService.messages.setDetails(error.data);
						});	
					}					
				},
				
				cancel: function(){
					this.messages.clear();
					this.reception = {typeCode: "Flowcell nanopore"};
				},
				
				init : function($routeParams){
					initListService();
					
					if(angular.isDefined($routeParams)){
						this.setRouteParams($routeParams);
					}
				}
			};
		
		return createService;
}]);
 