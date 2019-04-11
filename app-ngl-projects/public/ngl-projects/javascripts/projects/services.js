 "use strict";
 
 angular.module('ngl-projects.ProjectsServices', []).
	factory('projectsSearchService', ['$http', 'mainService', 'lists', 'datatable', function($http, mainService, lists, datatable){
		
		var getDefaultColumns = function(){
			var columns = [];
			columns.push({  	property:"code",
							   	header: "projects.code",
							   	type :"String",
							   	order:true,
							   	edit:false
			});
			columns.push({  	property:"name",
							   	header: "projects.name",
							   	type :"String",
							   	order:false,
							   	edit:false
			});
			
			columns.push({	property:"bioinformaticParameters.fgGroup",
				header: "projects.bioinformaticParameters.fgGroup",
				type :"String",
				order:true,
				edit:false,
				choiceInList:false
			});
			columns.push({	property:"bioinformaticParameters.fgPriority",
				header: "projects.bioinformaticParameters.fgPriority",
				type :"String",
				order:true,
				edit:false,
				choiceInList:false
			});
			
			columns.push({	property:"bioinformaticParameters.biologicalAnalysis",
				header: "projects.bioinformaticParameters.biologicalAnalysis",
				type :"String",
				filter:"codes:'boolean'",	
				order:true,
				edit:false,
				choiceInList:false
			});
			
			/* TODO EJACOBY AD*/
			columns.push({	property:"properties.unixGroup.value",
				header: "projects.unixGroup",
				type :"text",
				order:true,
				edit:false,
				choiceInList:false
			});
			
			columns.push({	property:"state.code",
								filter:"codes:'state'",					
								header: "projects.stateCode",
								type :"String",
								order:false,
								edit:false,
								choiceInList:false,
							   	listStyle:'bt-select',
							   	possibleValues:'listsTable.getStates()'	
			});
			
			
												
			return columns;
		};
				
		var isInit = false;
				
		var initListService = function(){
			if(!isInit){
				searchService.lists.refresh.projects();
				searchService.lists.refresh.bioinformaticParameters();
				searchService.lists.refresh.states({objectTypeCode:"Project", display:true},'statetrue');				
				searchService.lists.refresh.states({objectTypeCode:"Project"});							
				searchService.lists.refresh.types({objectTypeCode:"Project"});
				/*TODO EJACOBY AD*/
				searchService.lists.refresh.values({propertyDefinitionCode:"unixGroup"},"values");
				isInit=true;
			}
		};
				
		var searchService ={
				getDefaultColumns:getDefaultColumns,
				datatable:undefined,
				isRouteParam:false,
				lists : lists,
				form : undefined,
				
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
				
				convertForm : function(){
					var _form = angular.copy(this.form);
					if(_form.fromDate)_form.fromDate = moment(_form.fromDate, Messages("date.format").toUpperCase()).valueOf();
					if(_form.toDate)_form.toDate = moment(_form.toDate, Messages("date.format").toUpperCase()).valueOf();		
					return _form
				},
				
				search : function(datatable){
					mainService.setForm(this.form);
					this.datatable.search(this.convertForm());
				},
				
				reset : function(){
					this.form = {};
				},
				
				states : function(){
					return this.lists.get('statetrue');
				},
				
				
				init : function($routeParams, datatableConfig){
					initListService();
					
					datatableConfig.messages = {
							transformKey: function(key, args) {
		                        return Messages(key, args);
		                    }
					};
					
					if(datatableConfig && angular.isUndefined(mainService.getDatatable())){
						searchService.datatable = datatable(datatableConfig);
						mainService.setDatatable(searchService.datatable);
						searchService.datatable.setColumnsConfig(getDefaultColumns());		
						this.datatable.search();
					}else if(angular.isDefined(mainService.getDatatable())){
						searchService.datatable = mainService.getDatatable();			
					}	
					
					
					if(angular.isDefined(mainService.getForm())){
						searchService.form = mainService.getForm();
					}else{
						searchService.reset();						
					}
					
					if(angular.isDefined($routeParams)){
						this.setRouteParams($routeParams);
					}
				}
		};
		
		return searchService;
	}
]);
 