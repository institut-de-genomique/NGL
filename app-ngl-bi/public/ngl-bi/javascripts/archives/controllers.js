"use strict";

angular.module('home').controller('SearchCtrl', ['$scope', '$routeParams', 'datatable', function($scope, $routeParams, datatable) {
	$scope.archive = 2; //default only need archive
	
	/* 
    call by the init above and the search() to select the type of archives to visualize (0,1,2)
	 */
	$scope.search = function(param){
		$scope.datatable.search({archive:param});
	}
	
	
	var datatableConfig = {
			search : { 
				url:jsRoutes.controllers.archives.api.ReadSets.list()
			},
			pagination : {
				mode : 'local'
			},
			order : {
				mode : 'local',
				by:'date'
			},
			columns : [
						{	
							"property":"runCode",
						  	"header":Messages("archives.table.runcode"),
						  	"type":"String",
						  	"order":true
						 }, {	
							"property":"projectCode",
							"header":Messages("archives.table.projectcode"),
							"type":"String",
							"order":true
						 }, {	
							"property":"readSetCode",
							"header":Messages("archives.table.readsetcode"),
							"type":"String",
							"order":true
						 }, {	
							"property":"path",
							"header":Messages("archives.table.path"),
							"type":"String",
							"order":true
						 },{	
							"property":"date",
							"header":Messages("archives.table.date"),
							"type":"Date",
							"order":true
						 },{	
							"property":"id",
							"header":Messages("archives.table.backupid"),
							"type":"String",
							"order":true
						 }			           
			 ]			
			
	};
	
	
	var init = function(){
		$scope.datatable = datatable(datatableConfig);
		$scope.search(2);
		
		if(angular.isUndefined($scope.getHomePage())){
			$scope.setHomePage('search');
			$scope.addTabs({label:Messages('archives.menu.search'),href:jsRoutes.controllers.archives.tpl.ReadSets.home("search").url,remove:false});
			$scope.activeTab(0);
		}
	};
	
	init();
	
}]);

