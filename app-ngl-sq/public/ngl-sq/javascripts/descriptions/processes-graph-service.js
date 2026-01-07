"use strict";
 
angular.module('ngl-sq.descriptionsServices.processesgraph', []).
	factory('descriptionsProcessesGraphSearchService', ['$http', 'mainService', 'lists', 
		function($http, mainService, lists){

            var isInit = false;
		
            var initListService = function(){
                if(!isInit){
                    lists.refresh.experimentTypes({categoryCode: "transformation"});
                    isInit=true;
                }
            };

            var searchService = {
                lists : lists,
				search : function() {
					mainService.setForm(this.form);				
				},
                resetForm : function(){					
					this.form = {};					
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
                init : function($routeParams){
                    initListService();
                
					if(angular.isDefined(mainService.getForm())){
						searchService.form = mainService.getForm();
					}else{
						searchService.resetForm();						
					}
					
					if(angular.isDefined($routeParams)){
						this.setRouteParams($routeParams);
					}					
				}
            }
            return searchService;
        }
]);