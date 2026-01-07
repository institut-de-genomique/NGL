"use strict"

angular.module('home').controller('SearchCtrl', ['$http', '$scope',  '$window','datatable','lists','$filter','mainService','tabService','samplesSearchService','$routeParams', 'projectSearchFromUmbrella',
                                                 function($http, $scope, $window, datatable, lists,$filter,mainService,tabService,samplesSearchService,$routeParams, projectSearchFromUmbrella) {
	var datatableConfig = {
		group:{active:true},
		search:{
			url:jsRoutes.controllers.samples.api.Samples.list()
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
			by:'code',
			mode:'local'
		},
		exportCSV:{
			active:true
		},
		show:{
			active:true,
			add:function(line){
				tabService.addTabs({label:line.code,href:jsRoutes.controllers.samples.tpl.Samples.get(line.code).url, remove:true});
			}
		}
		,
		edit:{
			active:Permissions.check("writing")?true:false,
			columnMode:true
		},
		save:{
			active:Permissions.check("writing")?true:false,
			url:function(value){
				var fields = [];
				
				if(value.comments) {
					fields.push("fields=comments");
				}

				if(value.resolutionCodes) {
					fields.push("fields=resolutionCodes");
				}

				var fieldsQueryParams = fields.join("&");
				
				return jsRoutes.controllers.samples.api.Samples.update(value.code).url+"?"+fieldsQueryParams;
			},
			method:'put',
			mode:'remote'			
		}
	};	

	$scope.selectProjetsFromUmbrellas = function() {
		var projCodes = projectSearchFromUmbrella.search($scope.searchService.form.umbrellaCodes);
		$scope.searchService.form.projectCodes = projCodes;
	}
	
	$scope.search = function(){		
		$scope.searchService.search();
	};
	
	$scope.reset = function(){
		$scope.searchService.resetForm();
		$scope.searchService.resetTextareas();		
	};
	
	$scope.goToReadSet = function(code, $event){
		$window.open(AppURL("bi")+"/readsets/"+code, 'readset');
		if($event)$event.stopPropagation();
	}

	$scope.isShowInfo=false;

	$scope.toggleShowInfo=function(){
		$scope.isShowInfo = !$scope.isShowInfo;
	}
	
	//init
	if(angular.isUndefined($scope.getHomePage())){
		mainService.setHomePage('search');
		tabService.addTabs({label:Messages('samples.tabs.search'),href:jsRoutes.controllers.samples.tpl.Samples.home("search").url,remove:true});
		tabService.activeTab(0);
	}
	
	$scope.searchService = samplesSearchService;
	$scope.searchService.init($routeParams, datatableConfig)
	if($scope.searchService.isRouteParam){
		$scope.search();
	}	
}]);

