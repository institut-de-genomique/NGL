"use strict";
 
 angular.module('ngl-sq.descriptionsServices.mappingprojects', []).
	factory('descriptionsMappingProjectsSearchService', ['$http', 'mainService', 'lists', 'datatable', 
		                         function($http,   mainService,   lists,   datatable){

		var getColumns = function(){
			var columns = [];

            columns.push({
                "header":Messages("descriptions.mapping.projects.parent.code"),
                "property":"parentCode",
                "order":true,
                "hide":true,
                "position":1,
                "type":"text",
                "hide":true
            });
            columns.push({
                "header":Messages("descriptions.mapping.projects.parent.name"),
                "property":"parentName",
                "order":true,
                "hide":true,
                "position":2,
                "type":"text",
                "hide":true
            });
            columns.push({
                "header":Messages("descriptions.mapping.projects.child.code"),
                "property":"childCode",
                "order":true,
                "hide":true,
                "position":3,
                "type":"text",
                "hide":true
            });
            columns.push({
                "header":Messages("descriptions.mapping.projects.child.name"),
                "property":"childName",
                "order":true,
                "hide":true,
                "position":4,
                "type":"text",
                "hide":true
            });
		         
			return columns;
        };

        var isInit = false;

        var initListService = function(){
			if(!isInit){
				lists.refresh.projects();
				isInit=true;
			}
		};
		
		var searchService = {
				getColumns:getColumns,
				datatable:undefined,
				isRouteParam:false,
                lists : lists,
                currentList: null,
                sublists: {},
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
				
				search : function(){
                    var that = this;
					$http.get(jsRoutes.controllers.commons.api.Parameters.listByCode("map-parameter").url, {params: {datatable: true}})
                    .then(function(mappings){
                        $http.get(jsRoutes.controllers.projects.api.Projects.list().url, {params: {list: true}})
                        .then(function(projects){
                            var projectNameMap = new Map();
                            projects.data.forEach(function(proj){
                                projectNameMap.set(proj.code, proj.name);
                            });
                            mappings.data.data.forEach(function(mapping) {
                                that.sublists[mapping.code] = Object.keys(mapping.map)
                                .map(function(parentCode){
                                    var childCode = mapping.map[parentCode];
                                    return {
                                        "parentCode": parentCode,
                                        "parentName": projectNameMap.get(parentCode),
                                        "childCode": childCode,
                                        "childName": projectNameMap.get(childCode)
                                    }
                                });
                            });
                            if(that.currentList === null) {
                                that.currentList = "dna-extraction-mapping-rules";
                            }
                            that.changeTab(that.currentList);
                        });
                    });
                },

                clearSublists : function() {
                    this.sublists = {}
                },
                
                changeTab : function(index){
                    this.currentList = index;
                    this.datatable.setData(this.sublists[index], this.sublists[index].length);
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