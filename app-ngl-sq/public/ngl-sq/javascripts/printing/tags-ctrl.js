"use strict"

angular.module('home').controller('TagsCtrl', ['$scope', '$http', '$routeParams', 'datatable', 'lists', 'mainService', 'tabService', 'messages',
                                               function($scope, $http, $routeParams, datatable, lists,mainService, tabService, messages) {

	
	console.log("Printing tags");
	var datatableConfig = {	
			columns :[
				{
					"header":Messages("printing.tags.table.label"),
					"property":"label",
					"position":1,
					"order":true,
					"type":"text"
				},
				{
					"header":Messages("printing.tags.table.barcode"),
					"property":"barcode",
					"position":2,
					"order":true,
					"type":"text"
				}   
			
			],
			search:{
				active:false
			},
			pagination:{
				active:false
			},
			order:{
				by:'label',
				mode:'local'
			},
			hide:{
				active:false
		 	},
			edit:{
				active:false
			},
			exportCSV:{
				active:false
			},
			select:{
				active:false
			},
			showTotalNumberRecords:false
	};
	
	$scope.print = function(){
		$scope.messages.clear();
		var tags = $scope.datatable.getData();
		$scope.form.tags = tags;
		
		$http.post(jsRoutes.controllers.printing.api.Tags.print().url, $scope.form)
		.success(function(data, status, headers, config) {
			
			$scope.messages.setSuccess(Messages("printing.tags.msg.success.printing"));
		})
		.error(function(data, status, headers, config) {
			$scope.messages.setError(Messages("printing.tags.msg.error.printing"));
			$scope.messages.setDetails(data);								
		});				
	};
	
	$scope.reset = function(){
		$scope.form = {};
	};
	
	$scope.changePrinter = function(){
		if($scope.form.printerCode){
			$scope.selectedPrinter = $scope.printers.find(function(printer){
				return printer.code == $scope.form.printerCode;
			})
		}else{
			$scope.selectedPrinter = undefined;
		}
		$scope.form.barcodePositionId = undefined;
	}
	
	if(angular.isUndefined($scope.getHomePage())){
		mainService.setHomePage('tags');
		tabService.addTabs({label:Messages('printing.tabs.tags'),href:jsRoutes.controllers.printing.tpl.Printing.home("tags").url,remove:false});
		tabService.activeTab(0);
		$scope.reset();
	}
	
	$http.get(jsRoutes.controllers.commons.api.Parameters.list().url,{params:{typeCode:"BBP11"}})
		.success(function(data, status, headers, config) {
				$scope.printers = data;		
	})
	
	$scope.messages = messages();
	var datatable = datatable(datatableConfig);
	if(angular.isDefined($routeParams.experimentCode) || angular.isDefined($routeParams.containerSupportCodes)){
		$http.get(jsRoutes.controllers.printing.api.Tags.list().url,{params:$routeParams, datatable:datatable})
		.success(function(data, status, headers, config) {
					
				config.datatable.setData(data);
				$scope.datatable = datatable;		
		})
		.error(function(data, status, headers, config) {
			$scope.messages.setError(Messages('printing.tags.msg.error.load'));		
			$scope.messages.setDetails(data);			
		});
	}else{
		$scope.messages.setError(Messages('printing.tags.msg.error.nothing'));		
	}
	
	
}]);
