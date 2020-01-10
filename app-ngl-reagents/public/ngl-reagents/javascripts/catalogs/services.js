 "use strict";
 
 angular.module('ngl-reagent.kitCatalogsService', []).
	factory('kitCatalogsSearchService', ['$http', 'mainService', 'lists', 'datatable', function($http, mainService, lists, datatable){
		var getColumns = function(){
			var columns = [];
			columns.push({
				"header":Messages("kits.name"),
				"property":"name",
				"order":true,
				"type":"text"
			});
			columns.push({
				"header":Messages("kits.providerRefName"),
				"property":"providerRefName",
				"order":true,
				"type":"text"
			});
			columns.push({
				"header":Messages("kits.experimentTypes"),
				"property":"experimentTypeCodes",
				"order":true,
				"type":"text"
			});
			columns.push({
				"header":Messages("kits.provider"),
				"property":"providerCode",
				"order":true,
				"type":"text"
			});
			columns.push({
				"header":Messages("kits.catalogRefCode"),
				"property":"catalogRefCode",
				"order":true,
				"type":"text"
			});
			columns.push({
				"header":Messages("kits.code"),
				"property":"code",
				"order":true,
				"type":"text"
			});
			
			return columns;
		};
		
		
		var isInit = false;
		
		var initListService = function(){
			if(!isInit){
				//lists.refresh.experimentTypes({withoutOneToVoid:true});
				lists.refresh.experimentTypes({categoryCodes:["transfert","transformation","qualitycontrol","purification"]});
				lists.state = [{'name': 'oui',
								'code' : true},
								{'name': 'non',
								 'code': false}];
				//lists.refresh.boxCatalogs();
				//lists.refresh.reagentCatalogs();
				isInit=true;
			}
		};
		
		var searchService = {
				getColumns:getColumns,
				datatable:undefined,
				isRouteParam:false,
				lists : lists,
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
					
				},
				convertForm : function(){
					var _form = angular.copy(this.form);
					
					mainService.setForm(_form);
						
					return _form;	
				},
				
				resetForm : function(){
					this.form = {};									
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
				/*updateBoxCatalogsList : function (){
					
					//test si null undefined
					lists.refresh.boxCatalogs({kitCatalogCodes:this.form.codes});
				},*/
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