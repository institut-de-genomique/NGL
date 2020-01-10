"use strict";

angular.module('home').controller('CreateCtrl', ['$scope', '$http','datatable','messages', function($scope, $http,datatable,messages) {
	
	
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
		$scope.setHomePage('create');
	}
	
	//to avoid to lost the previous search
	if(angular.isUndefined($scope.getTabs(0))){
		$scope.addTabs({label:Messages('barcodes.tabs.create'),href:jsRoutes.controllers.barcodes.tpl.Barcodes.home("create").url,remove:false});
		$scope.activeTab($scope.getTabs(0));
	}
	
	if(angular.isUndefined($scope.getDatatable())){
		$scope.datatable = datatable(datatableConfig);		
		$scope.setDatatable($scope.datatable);
	}else{
		$scope.datatable = $scope.getDatatable();
	}
	
	$scope.messages = messages();
	
	if(angular.isUndefined($scope.getForm())){
		$scope.form = {
				projects:{},
				etmanips:{},
				number:null
		};
		
		$scope.setForm($scope.form);
		$http.get(jsRoutes.controllers.combo.api.Lists.projects().url).
			success(function(data, status, headers, config){
				$scope.form.projects.options = data;
			});
					
		$http.get(jsRoutes.controllers.combo.api.Lists.etmanips().url).
			success(function(data, status, headers, config){
				$scope.form.etmanips.options = data;
			});
		
					
	}else{
		$scope.form = $scope.getForm();			
	}
	
	$scope.reset = function(){
		$scope.form = {
				projects:{},
				etmanips:{},
				number:null
		};
	};
	
	$scope.generate = function(){		
		var jsonSearch = {};
		if($scope.form.projects.selected){
			jsonSearch.projectCode = $scope.form.projects.selected.code;
		}
		
		if($scope.form.etmanips.selected){
			jsonSearch.typeCode = $scope.form.etmanips.selected.code;
		}
		
		jsonSearch.number = $scope.form.number
		$scope.messages.clear();
		$http.post(jsRoutes.controllers.barcodes.api.Barcodes.save().url, jsonSearch)
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
	};
	
}]);
