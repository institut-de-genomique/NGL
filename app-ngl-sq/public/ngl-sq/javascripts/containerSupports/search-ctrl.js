"use strict"
angular.module('home').controller('SearchCtrl', ['$scope', '$window', 'datatable','lists','$filter','mainService','tabService','containerSupportsSearchService','$routeParams', 
                                                 function($scope, $window, datatable, lists,$filter,mainService,tabService,containerSupportsSearchService,$routeParams) {
	$scope.datatableConfig = {
		search:{
			url:jsRoutes.controllers.containers.api.ContainerSupports.list()
		},
		pagination:{
			mode:'local'
		},
		order:{
			by:'traceInformation.creationDate',
			reverse:true,
			mode:'local'
		},
		edit:{
			active:Permissions.check("writing")?true:false,
			columnMode:true
		},
		save:{
			active:Permissions.check("writing")?true:false,
			url:function(line){return jsRoutes.controllers.containers.api.ContainerSupports.update(line.code).url+"?fields=storageCode";},
			mode:'remote',
			method:'put',
		},
		exportCSV:{
			active:true
		},
		hide:{
			active:true
		},
		show:{
			active:true,
			add:function(line){
				tabService.addTabs({label:line.code,href:jsRoutes.controllers.containers.tpl.ContainerSupports.get(line.code).url, remove:true});
			}
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
		$scope.searchService.resetTextareas();	
	};
	
	$scope.openPrintTagsPage = function(supports){
		var params = {value : ""};
		supports.forEach(function(value){
			this.value = this.value + "containerSupportCodes="+value.code+"&";
		},params)
		params.value = params.value.slice(0, params.value.length-1);
		$window.open(jsRoutes.controllers.printing.tpl.Printing.home("tags").url+"?"+params.value, 'tags');
	};
	//init
	$scope.datatable = datatable($scope.datatableConfig);		
	if(angular.isUndefined($scope.getHomePage())){
		mainService.setHomePage('search');
		tabService.addTabs({label:Messages('containerSupports.tabs.search'),href:jsRoutes.controllers.containers.tpl.ContainerSupports.home("search").url,remove:true});
		tabService.activeTab(0);
	}
	if(angular.isUndefined($scope.getForm())){
		$scope.form = {};
		mainService.setForm($scope.form);
	}
	
	$scope.searchService = containerSupportsSearchService;
	$scope.searchService.init($routeParams, $scope.datatableConfig)

}]);


"use strict"
angular.module('home').controller('SearchStateCtrl', ['$scope','$location','$routeParams', 'datatable','lists','$filter','$http','mainService','tabService','containerSupportsSearchService', 
                                                      function($scope,$location,$routeParams, datatable, lists,$filter,$http,mainService,tabService,containerSupportsSearchService) {
	$scope.datatableConfig = {
			search:{
				url:jsRoutes.controllers.containers.api.ContainerSupports.list()
				
			},
			order:{
				by:'traceInformation.creationDate',
				reverse:true,
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
			save:{
				active:Permissions.check("writing")?true:false,
				url:function(line){return jsRoutes.controllers.containers.api.ContainerSupports.updateStateBatch().url;},
				mode:'remote',
				method:'put',
				batch:true,
				value:function(line){return {code:line.code,state:line.state};}
			},
			show:{
				active:true,
				add:function(line){
					tabService.addTabs({label:line.code,href:jsRoutes.controllers.containers.tpl.ContainerSupports.get(line.code).url, remove:true});
				}
			},
			pagination:{
				mode:'local'
			}
	};

	$scope.reset = function(){
		$scope.searchService.resetForm();
		$scope.searchService.resetTextareas();	
	};
	
	$scope.search = function(){	
		$scope.searchService.search();
	};
	
	//init
	if(angular.isUndefined($scope.getHomePage())){
		mainService.setHomePage('state');
		tabService.addTabs({label:Messages('containerSupports.tabs.state'),href:jsRoutes.controllers.containers.tpl.ContainerSupports.home("state").url,remove:true});
		tabService.activeTab(0);
	}
	
	if(angular.isUndefined($scope.getForm())){
		$scope.form = {};
		mainService.setForm($scope.form);
	}else{
		$scope.form = mainService.getForm();			
	}
	
	$scope.searchService = containerSupportsSearchService;
	$scope.searchService.init($routeParams, $scope.datatableConfig)
	
	if($scope.form.project || $scope.form.type){
		$scope.search();
	}
		
}]);