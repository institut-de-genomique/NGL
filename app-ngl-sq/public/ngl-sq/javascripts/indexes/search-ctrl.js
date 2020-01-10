/* FDS NGL-836 19/11/2018 d'apres Samples */
"use strict";

angular.module('home').controller('SearchCtrl', ['$scope','$filter','$routeParams','datatable','lists','mainService','tabService','indexesSearchService',
                                         function($scope,  $filter,  $routeParams,  datatable,  lists,  mainService,  tabService,  indexesSearchService) {
	var datatableConfig = {
		group:{active:true},
		
		search:{
			// !! pour l'instant on réutilise l'API Parameters. TODO séparer les index des autres parametres
			url:jsRoutes.controllers.commons.api.Parameters.list()
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
			active: false, // Demande de Julie a ce qu'il ne soient pas épinglés pour l'instant...
			// il s'agit des onglets de lefttab (elements qui ont ete epingles !!!!)
			add:function(line){
				tabService.addTabs({label:line.code,href:jsRoutes.controllers.indexes.tpl.Indexes.get(line.code).url, remove:true});
			}
		}
		
		/* PAS EDIT NI SAVE POUR INDEX ...pour l'instant
		,
		edit:{
			active:Permissions.check("writing")?true:false,
			columnMode:true
		},
		save:{
			active:Permissions.check("writing")?true:false,
			url:function(value){
				var fields = "fields=valuation";
					if(value.comments)fields = fields+"&fields=comments";
				
				return jsRoutes.controllers.samples.api.Samples.update(value.code).url+"?"+fields;
			},
			method:'put',
			mode:'remote'
		}
		*/
	}

	$scope.search = function(){
		$scope.searchService.search();
	};
	
	$scope.reset = function(){
		$scope.searchService.resetForm();
	};
	
	//init
	if(angular.isUndefined($scope.getHomePage())){
		mainService.setHomePage('search');
		// necessaire pour le lefttab !!!
		tabService.addTabs({label:Messages('indexes.tabs.search'),href:jsRoutes.controllers.indexes.tpl.Indexes.home("search").url, remove:true});
		tabService.activeTab(0);
	}
	
	$scope.searchService = indexesSearchService;
	$scope.searchService.init($routeParams, datatableConfig)
	if($scope.searchService.isRouteParam){
		$scope.search();
	}
}]);