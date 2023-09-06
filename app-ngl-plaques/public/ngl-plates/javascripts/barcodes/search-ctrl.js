"use strict";

angular.module('home').controller('SearchCtrl', ['$scope', '$http','datatable','messages', function($scope, $http,datatable,messages) {
	
	
	var datatableConfig = {
			name : "barcodes",
			order :{by:'barcode', mode:'local'},
			
			pagination:{
				active:true,
				mode:'local'
			},
			exportCSV : {
				active:true
			},
			remove:{
				active:true,
				url:function(value){
					return jsRoutes.controllers.barcodes.api.Barcodes["delete"](value.barcode).url
				}
			},
			messages:{
				active:true
			},
			columns:[
					    {  	property:"barcode",
					    	header: Messages("plates.table.barcode"),
					    	type :"String",
					    	order:true
						}
					]						
	};
	
	if(angular.isUndefined($scope.getHomePage())){
		$scope.setHomePage('search');
	}
	
	//to avoid to lost the previous search
	if(angular.isUndefined($scope.getTabs(0))){
		$scope.addTabs({label:Messages('barcodes.tabs.search'),href:jsRoutes.controllers.barcodes.tpl.Barcodes.home("search").url,remove:false});
		$scope.activeTab($scope.getTabs(0));
	}
	
	if(angular.isUndefined($scope.getDatatable())){
		$scope.datatable = datatable(datatableConfig);		
		$scope.setDatatable($scope.datatable);
	}else{
		$scope.datatable = $scope.getDatatable();
	}
	
	$scope.messages = messages();
	
	
	$http.get(jsRoutes.controllers.barcodes.api.Barcodes.list().url)
	.success(function(data, status, headers, config){
		var objects = [];
		data.forEach(function(elt){
			objects.push({barcode:elt});
		});
		
		$scope.datatable.setData(objects);
	}).error(function(data, status, headers, config){
		$scope.messages.setError("save");
		$scope.messages.setDetails(data);
	});
	
	
	
}]);
