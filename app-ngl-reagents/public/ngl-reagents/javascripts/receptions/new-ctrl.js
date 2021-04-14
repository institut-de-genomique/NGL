"use strict";

angular.module('home').controller('NewFromFileCtrl', ['$scope', '$http','$filter','lists', 'mainService', 'tabService','datatable', 'messages',
                                                  function($scope,$http,$filter,lists,mainService,tabService,datatable, messages){
	
	$scope.upload = function(){
		$scope.messages.clear();
		if($scope.form.file){
			$scope.spinner = true;
			$http.post(jsRoutes.controllers.reagents.io.Receptions.importFile($scope.form.importFormat).url, $scope.form.file)
			.success(function(data, status, headers, config) {
				$scope.messages.clazz="alert alert-success";
				$scope.messages.text=Messages('reagents.msg.reception.success');
				$scope.messages.showDetails = false;
				$scope.messages.open();	
				$scope.file = undefined;
				angular.element('#importFile')[0].value = null;
				$scope.spinner = false;
			})
			.error(function(data, status, headers, config) {
				$scope.messages.clazz = "alert alert-danger";
				$scope.messages.text = Messages('reagents.msg.reception.error');
				$scope.messages.setDetails(data);
				$scope.messages.open();	
				$scope.file = undefined;
				angular.element('#importFile')[0].value = null;
				$scope.spinner = false;
			});
		}
	};
	
	
	$scope.reset = function(){
		$scope.form = {};
		$scope.formprint = {};	
		$scope.messages=messages();	
		if(angular.element('#importFile')[0]!=undefined){
			angular.element('#importFile')[0].value = null;
		}
	};
	
	$scope.getImportFormats = function() {
		return [{code: 'illumina', name: "Illumina"}];
	}
	
	/*
	 * init()
	 */
	var init = function(){
		
		$scope.lists = lists;
		$scope.reset();
		$scope.messages = messages();
		if(angular.isUndefined(mainService.getHomePage())){
			mainService.setHomePage('search');
			tabService.addTabs({label:Messages('reagent.receptions.page.tab.new'),href:jsRoutes.controllers.reagents.tpl.Receptions.home("new").url,remove:true});
			tabService.activeTab(0);
		}
	};

	init();
	
}]);