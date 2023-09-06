 "use strict";

function DataTableCtrl($scope, $http, datatable) {
	
	$scope.types=[{"code":'1',"label":'L1'},{"code":'2',"label":'L2'},{"code":'3',"label":'L3'}];
	
	
//search parameter
	$scope.name = "test";
	$scope.type = {code : undefined};
	
	//datatable config
	$scope.datatableConfig = {
			edit: false,
			orderReverse:false,
			orderBy:undefined,
			editColumn: {
				all:undefined,
				code:undefined,
				name:undefined,
				collectionName:undefined
			},
			updateColumn: {
				code:undefined,
				name:undefined,
				collectionName:undefined
			},
			hideColumn: {
				id:undefined,
				code:undefined,
				name:undefined,
				objectType:{type:undefined},
				collectionName:undefined
			},
			orderColumn:{
				id:undefined,
				code:undefined,
				name:undefined,
				objectType:{type:undefined},
				collectionName:undefined
			},
			url:{
				//save:"/admin/types?format=json",
				remove:"",
				search:'/admin/types'
			}
	};
	
	$scope.init = function(){
		$scope.datatable = datatable($scope, $scope.datatableConfig);
		
	}
	
	$scope.search = function(){
		$scope.datatable.search({name:$scope.name,typeCode:$scope.type.code});
	}
	
}

DataTableCtrl.$inject = ['$scope', '$http', 'datatable'];