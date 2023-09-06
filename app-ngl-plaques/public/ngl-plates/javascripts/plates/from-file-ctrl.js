"use strict";

angular.module('home').controller('FromFileCtrl', ['$scope', '$http','datatable','basket', function($scope, $http, datatable, basket) {

		
	var init = function(){
		$scope.clearMessages();
		if(angular.isUndefined($scope.getHomePage())){
			$scope.setHomePage('creation');
		}
		
		if(angular.isUndefined($scope.getTabs(0))){
			$scope.addTabs({label:Messages('plates.tabs.searchfile'),href:jsRoutes.controllers.plates.tpl.Plates.home("new-from-file").url,remove:false});
			$scope.activeTab($scope.getTabs(0));
		}
		

		if(angular.isUndefined($scope.getBasket())){
			$scope.basket = basket();			
			$scope.setBasket($scope.basket);
		}else{
			$scope.basket = $scope.getBasket();
		}
		
		if(angular.isUndefined($scope.getForm())){
			$scope.form = {
					etmanips:{}
			};
			
			$scope.setForm($scope.form);
			
			$http.get(jsRoutes.controllers.combo.api.Lists.etmanips().url).
			success(function(data, status, headers, config){
				$scope.form.etmanips.options = data;
			});
						
		}else{
			$scope.form = $scope.getForm();			
		}
	};
	
	
	$scope.reset = function(){
		$scope.form.file = null;
		$scope.form.etmanips.selected = null;
		angular.element('#importFile')[0].value = null;
	}
	
	$scope.upload = function(){
		$scope.clearMessages();
		if($scope.form.etmanips.selected && $scope.form.file){
			var etmanipCode = $scope.form.etmanips.selected.code;
			
			$http.post(jsRoutes.controllers.plates.io.Plates.importFile(etmanipCode).url, $scope.form.file)
			.success(function(data, status, headers, config) {
				$scope.message.clazz="alert alert-success";
				$scope.message.text=Messages('plates.msg.import.success');
				$scope.message.showDetails = false;
				//only atm because we cannot override directly experiment on scope.parent
				$scope.basket.add(data);
				$scope.form.file = null;
				angular.element('#importFile')[0].value = null;	
				
				if($scope.basket.length() > 0 && $scope.getTabs().length === 1){
					$scope.addTabs({label:Messages('plates.tabs.new'),href:jsRoutes.controllers.plates.tpl.Plates.get("new").url,remove:false});//$scope.getTab()[1]
				}
				
			})
			.error(function(data, status, headers, config) {
				$scope.message.clazz = "alert alert-danger";
				$scope.message.text = Messages('plates.msg.import.error');
				$scope.message.details = data;
				$scope.message.showDetails = true;
				$scope.basket.reset();
				$scope.form.file = null;
				angular.element('#importFile')[0].value = null;
			});
			
			
		}
	};
	
	$scope.clearMessages  = function(){
		$scope.message = {clazz : undefined, text : undefined, showDetails : false, isDetails : false, details : []};
	}
	
	
	init();
}]);


