 "use strict";
 
 angular.module('ngl-bi.UsersServices', []).
	factory('usersSearchService', ['$http', 'mainService', 'lists','datatable', function($http, mainService, lists, datatable){
		
		var getColumns = function(){
				var columns = [
												
								{	
									"header":Messages("user.login"),
									"property":"login",
									"order":true,
									"type":"text",
									"position":1
								},
								{	
									"header":Messages("user.roles"),
									"property":"roleIds",									
									"type": "text",
									"edit":true,
									"render":'<div bt-select ng-model="value.data.roleIds" bt-options="valid.code as valid.name for valid in searchService.lists.getRoles()" ng-edit="false"></div>',									
									"choiceInList":true,
									"listStyle":'bt-select-multiple',
									"possibleValues":'searchService.lists.getRoles()',
									"order":false,
									"position":2
								}
								];
				return columns;     
		}
		
		var isInit = false;
		
		var initListService = function(){
			if(!isInit){
				lists.refresh.users();
				lists.refresh.roles();
				isInit=true;
			}
		};
		var searchService = {
				getColumns:getColumns,
				getDefaultColumns:getColumns,
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
				
				resetForm : function(){
					this.form = {};
				},
				
				search : function(){
					//this.updateForm();
					mainService.setForm(this.form);
					this.datatable.search(this.form);
				},
				
				/**
				 * initialise the service
				 */
				init : function($routeParams, datatableConfig){
					initListService();
					
					//to avoid to lost the previous search
					if(datatableConfig && angular.isUndefined(mainService.getDatatable())){
						searchService.datatable = datatable(datatableConfig);			
						mainService.setDatatable(searchService.datatable);
						searchService.datatable.setColumnsConfig(getColumns());
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