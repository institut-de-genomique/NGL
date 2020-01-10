"use strict"

angular.module('home').controller('SearchCtrl', ['$scope',  '$window','datatable','lists','$filter','mainService','tabService','containersSearchService','$routeParams', 
                                                 function($scope, $window, datatable, lists,$filter,mainService,tabService,containersSearchService,$routeParams) {
	var datatableConfig = {
		group:{active:true},
		search:{
			url:jsRoutes.controllers.containers.api.Containers.list()
		},
		pagination:{
			mode:'local'
		},
		group:{
			active:true,
			showOnlyGroups:true,
			enableLineSelection:true,
			showButton:true
		},
		hide:{
			active:true
		},
		order:{
			//by:"['support.code','support.column']",
			by:'traceInformation.creationDate',
			reverse:true,
			mode:'local'
		},
		exportCSV:{
			active:true
		},
		show:{
			active:true,
			add:function(line){
				tabService.addTabs({label:line.code,href:jsRoutes.controllers.containers.tpl.Containers.get(line.code).url, remove:true});
			}
		},
		edit:{
			active:Permissions.check("writing")?true:false,
			columnMode:true
		},
		save:{
			active:Permissions.check("writing")?true:false,
			url:function(value){
				var fields = "fields=valuation";
				if(value.state && value.state.resolutionCodes)fields = fields+"&fields=state.resolutionCodes";
				if(value.comments)fields = fields+"&fields=comments";
				if(value.concentration)fields = fields+"&fields=concentration";
				if(value.volume)fields = fields+"&fields=volume";
				if(value.quantity)fields = fields+"&fields=quantity";
				if(value.size)fields = fields+"&fields=size";
				
				return jsRoutes.controllers.containers.api.Containers.update(value.code).url+"?"+fields;
			},
			method:'put',
			mode:'remote'			
		},
		otherButtons :{
			active:PrintTag.isActive(),
			template:'<button class="btn btn-default" ng-click="openPrintTagsPage(searchService.datatable.getSelection(true))"  ng-disabled="!searchService.datatable.isSelect()" title="'+Messages("button.tag.printing")+'"><i class="fa fa-tags" ></i></button>'					
		}
	};

	
	
	$scope.search = function(){		
		$scope.searchService.search();
	};
	
	$scope.reset = function(){
		$scope.searchService.resetForm();		
	};
	
	$scope.openPrintTagsPage = function(supports){
		var params = {value : ""};
		supports.forEach(function(value){
			this.value = this.value + "containerSupportCodes="+value.support.code+"&";
		},params)
		params.value = params.value.slice(0, params.value.length-1);
		$window.open(jsRoutes.controllers.printing.tpl.Printing.home("tags").url+"?"+params.value, 'tags');
	};
	//init
	if(angular.isUndefined($scope.getHomePage())){
		mainService.setHomePage('search');
		tabService.addTabs({label:Messages('containers.tabs.search'),href:jsRoutes.controllers.containers.tpl.Containers.home("search").url,remove:true});
		tabService.activeTab(0);
	}
	if(angular.isUndefined($scope.getForm())){
		$scope.form = {};
		mainService.setForm($scope.form);
	}
	
	$scope.searchService = containersSearchService;
	$scope.searchService.init($routeParams, datatableConfig)
	
	if($scope.searchService.isRouteParam){
		$scope.search();
	}	
}]);


"use strict"
angular.module('home').controller('SearchStateCtrl', ['$scope','$location','$routeParams', 'datatable','lists','$filter','$http','$q','mainService','tabService','containersSearchService', 
	function($scope,$location,$routeParams, datatable, lists,$filter,$http,$q,mainService,tabService,containersSearchService) {
	var datatableConfig = {
			search:{
				url:jsRoutes.controllers.containers.api.Containers.list()
				
			},
			order:{
				by:'traceInformation.creationDate',
				reverse : true,
				mode:'local'
			},
			edit:{
				active:Permissions.check("writing")?true:false,
				columnMode:true,
				lineMode:function(value){
					return (value.state.code === 'IS' 
							|| value.state.code === 'UA'
							||	value.state.code === 'IW-P'
							||	value.state.code.startsWith('A'));
				}
				
			},
			pagination:{
				mode:'local'
			},
			save:{
				active:Permissions.check("writing")?true:false,
				url:jsRoutes.controllers.containers.api.Containers.updateStateBatch().url,
				mode:'remote',
				method:'put',
				batch:true,
				value:function(line){return {code:line.code,state:line.state};},	
				beforeSave:function(values){
					
					var queries = values.map(function(value){
						var fields = "fields=valuation";
						if(value.data.state && value.data.state.resolutionCodes)fields = fields+"&fields=state.resolutionCodes";
						if(value.data.comments)fields = fields+"&fields=comments";
						return $http.put(jsRoutes.controllers.containers.api.Containers.update(value.data.code).url+"?"+fields,value.data);																	
					});
					
					return $q.all(queries);
					
				}
			},
			show:{
				active:true,
				add:function(line){
					tabService.addTabs({label:line.code,href:jsRoutes.controllers.containers.tpl.Containers.get(line.code).url, remove:true});
				}
			},
			hide:{
				active:true
			},
			exportCSV:{
				active:true,
			}
	};

	$scope.reset = function(){
		$scope.searchService.resetForm();
	};
	
	$scope.search = function(){	
		$scope.searchService.search();
	};
	
	//init
	if(angular.isUndefined($scope.getHomePage())){
		mainService.setHomePage('state');
		tabService.addTabs({label:Messages('containers.tabs.state'),href:jsRoutes.controllers.containers.tpl.Containers.home("state").url,remove:true});
		tabService.activeTab(0);
	}
	
	if(angular.isUndefined($scope.getForm())){
		$scope.form = {};
		mainService.setForm($scope.form);
	}else{
		$scope.form = mainService.getForm();			
	}
	
	$scope.searchService = containersSearchService;
	$scope.searchService.init($routeParams, datatableConfig)
	
	if($scope.form.project || $scope.form.type){
		$scope.search();
	}
	
	
}]);