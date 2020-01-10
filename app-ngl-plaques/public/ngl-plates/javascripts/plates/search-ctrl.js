"use strict";

angular.module('home').controller('SearchCtrl', ['$scope', '$http','datatable', function($scope, $http,datatable) {
	var datatableConfig = {
			order :{by:'creationDate', mode:'local', reverse:true},
			search:{
				url:jsRoutes.controllers.plates.api.Plates.list()
			},
			pagination:{
				active:true,
				mode:'local'
			},
			show:{
				active:true,
				add :function(line){
					$scope.addTabs({label:line.code,href:jsRoutes.controllers.plates.tpl.Plates.get(line.code).url,remove:true});
				}
			},
			remove:{
				active:true,
				url:function(value){
					return jsRoutes.controllers.plates.api.Plates["remove"](value.code).url
				}
			},
			messages:{
				active:true
			},
			columns:[
					    {  	property:"code",
					    	header: Messages("plates.table.code"),
					    	type :"String",
					    	order:true
						},
						{	property:"creationDate",
							header: Messages("plates.table.creationDate"),
							type :"Date",
					    	order:true
						},
						{	property:"typeName",
							header: Messages("plates.table.typeName"),
							type :"String",
					    	order:true
						},
						{	property:"nbWells",
							header: Messages("plates.table.nbWells"),
							type :"Number",
					    	order:false
						}
					]						
	};
	
	$scope.init = function(){
		if(angular.isUndefined($scope.getHomePage())){
			$scope.setHomePage('search');
		}
		
		//to avoid to lost the previous search
		if(angular.isUndefined($scope.getTabs(0))){
			$scope.addTabs({label:Messages('plates.tabs.search'),href:jsRoutes.controllers.plates.tpl.Plates.home("search").url,remove:false});
			$scope.activeTab($scope.getTabs(0));
		}
		if(angular.isUndefined($scope.getDatatable())){
			$scope.datatable = datatable(datatableConfig);
			$scope.datatable.search();
			$scope.setDatatable($scope.datatable);
		}else{
			$scope.datatable = $scope.getDatatable();
		}
		
		if(angular.isUndefined($scope.getForm())){
			$scope.form = {
					projects:{},
					etmanips:{},
					users:{}
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
			
			$http.get(jsRoutes.controllers.combo.api.Lists.users().url).
			success(function(data, status, headers, config){
				$scope.form.users.options = data;
			});
						
		}else{
			$scope.form = $scope.getForm();			
		}
	};
	
	$scope.search = function(){		
		var jsonSearch = {};
		if($scope.form.projects.selected){
			jsonSearch.project = $scope.form.projects.selected.code;
		}
		
		if($scope.form.etmanips.selected){
			jsonSearch.etmanip = $scope.form.etmanips.selected.code;
		}
		
		if($scope.form.users.selected){
			jsonSearch.percodc = $scope.form.users.selected.code;
		}
		
		if($scope.form.plaqueId){
			jsonSearch.plaqueId = $scope.form.plaqueId;
		}
		
		if($scope.form.matmanom){
			jsonSearch.matmanom = $scope.form.matmanom;
		}
		
		if($scope.form.fromDate){
			jsonSearch.fromDate = $scope.form.fromDate;
		}
		
		if($scope.form.toDate){
			jsonSearch.toDate = $scope.form.toDate;
		}
		
		if($scope.form.matmanom){
			jsonSearch.matmanom = $scope.form.matmanom;
		}
		$scope.datatable.search(jsonSearch);
	};
}]);
