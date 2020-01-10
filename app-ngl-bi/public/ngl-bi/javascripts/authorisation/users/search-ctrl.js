"use strict";

angular.module('home').controller('SearchUsersCtrl', ['$scope', '$routeParams', 'datatable', 'lists', 'mainService', 'tabService', 'usersSearchService', 
                                                 function($scope, $routeParams, datatable, lists, mainService, tabService, usersSearchService) {
	var datatableConfig = {
			
			order :{mode:'local', by:'login'},
			search:{
				url:jsRoutes.controllers.commons.api.Users.list()
			},
			pagination:{
				mode:'local'
			},			
			edit:{
				active: true,
			},
			save:{
				active:true,
				url:function(line){
					return jsRoutes.controllers.commons.api.Users.update(line.login).url;
					},
				batch: false,
				method:'put'
			},
			name:"Users"
	};
	
	$scope.search = function(){
		$scope.searchService.search();
	};
	
	$scope.reset = function(){
		$scope.searchService.resetForm();
	};
	
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('search');
		tabService.addTabs({label:Messages('authorisation.users.page.tab.search'),href:jsRoutes.controllers.authorisation.tpl.Users.home("search").url,remove:true});
		tabService.activeTab(0); // desactive le lien !
	}
	
	$scope.searchService = usersSearchService;	
	$scope.searchService.init($routeParams, datatableConfig)
	
}]);