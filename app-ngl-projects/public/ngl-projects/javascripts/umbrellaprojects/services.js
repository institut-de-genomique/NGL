 "use strict";
 
 angular.module('ngl-projects.ProjectsServices', []).
	factory('searchService', ['$http', 'mainService', 'lists', function($http, mainService, lists){
		
		var searchService = {
				getColumns:function(){
					var columns = [
								    {  	property:"code",
								    	header: "projects.code",
								    	type :"text",
								    	order:true,
								    	edit:false
									},
								    {  	property:"name",
								    	header: "projects.name",
								    	type :"text",
								    	order:false,
								    	edit:false
									}
								];
					return columns;
				},
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
					datatable.search(this.convertForm());
				},
				
				reset : function(){
					this.form = {};
				}
		};
		
		return function() {			
			searchService.lists.refresh.umbrellaProjects();
			
			if(angular.isDefined(mainService.getForm())){
				searchService.form = mainService.getForm();
			}else{
				searchService.reset();
			}
			return searchService;		
		}
	}
]);