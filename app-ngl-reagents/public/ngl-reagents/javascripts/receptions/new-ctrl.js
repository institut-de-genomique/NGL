"use strict";

angular.module('home').controller('NewFromFileCtrl', ['$scope', '$http','$filter','lists', 'mainService', 'tabService','datatable', 'messages',
                                                  function($scope,$http,$filter,lists,mainService,tabService,datatable, messages){

	$scope.upload = function(){
		$scope.messages.clear();
		if(isFileSelected()){
			setSpinnerOn();
			postImportFile()
			.success(function(data, status, headers, config) {
				importSuccess(data);
			})
			.error(function(data, status, headers, config) {
				importError(data);
			});
		}

		//---

		function isFileSelected() {
			return $scope.form.file;
		}

		function setSpinnerOn() {
			$scope.spinner = true;
		}

		function setSpinnerOff() {
			$scope.spinner = false;
		}

		function postImportFile() {
			var url = getUrl();
			var importFile = getImportFile();
			return $http.post(url, importFile);

			//---

			function getUrl() {
				return jsRoutes.controllers.reagents.io.Receptions.importFile($scope.form.importFormat).url;
			}

			function getImportFile() {
				return $scope.form.file;
			}
		}

		function sucessMessage() {
			$scope.messages.clazz="alert alert-success";
			$scope.messages.text=Messages('reagents.msg.reception.success');
			$scope.messages.showDetails = false;
			$scope.messages.open();	
		}

		function errorMessage(data) {
			$scope.messages.clazz = "alert alert-danger";
			$scope.messages.text = Messages('reagents.msg.reception.error');
			$scope.messages.setDetails(data);
			$scope.messages.open();	
		}

		function removeImportFile() {
			$scope.file = undefined;
			angular.element('#importFile')[0].value = null;
		}

		function importSuccess(data) {
			sucessMessage();
			removeImportFile();
			setSpinnerOff();
		}

		function importError(data) {
			errorMessage(data);
			removeImportFile();
			setSpinnerOff();
		}
	};
	
	
	$scope.reset = function(){
		resetForm();
		resetMessages();
		resetImportFile();	
		
		//---

		function resetForm() {
			$scope.form = {};
		}

		function resetMessages() {
			$scope.messages=messages();	
		}

		function resetImportFile() {
			if(angular.element('#importFile')[0]!=undefined){
				angular.element('#importFile')[0].value = null;
			}
		}
	};

	var importFormats = [
		{
			code: 'illumina-depot-reagents-reception', 
			name: "Dépôt Illumina"
		}, 
		{
			code: 'nanopore-reagents-reception', 
			name:'Atelier Nanopore'
		}
	];
	
	$scope.getImportFormats = function() {
		return importFormats;
	};
	
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