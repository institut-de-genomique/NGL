"use strict"

function DemoCtrl($scope,datatable) {

	$scope.datatableConfig = {
			columns : [{	header:"Code", //the title
							property:"code", //the property to bind
							edit:true, //can be edited or not
							hide:true, //can be hidden or not
							order:true, //can be ordered or not
							type:"String", //the column type
							choiceInList:false, //when the column is in edit mode, the edition is a list of choices or not
							extraHeaders:{"0":"Inputs","1":"prop2"}, //the extraHeaders list
						},
						{	header:"Etat", //the title
							property:"etat", //the property to bind
							edit:true, //can be edited or not
							hide:true, //can be hidden or not
							order:true, //can be ordered or not
							type:"String", //the column type
							choiceInList:true, //when the column is in edit mode, the edition is a list of choices or not
							possibleValues:[{"code":"N","name":"Nouveau"},{"code":"IWP","name":"En attente de processus"},{"code":"IWE","name":"En attente d'experience"}],
							extraHeaders:{"0":"Inputs","1":"prop"}, //the extraHeaders list
						},{	header:"Volume", //the title
							property:"volume", //the property to bind
							edit:true, //can be edited or not
							hide:true, //can be hidden or not
							order:true, //can be ordered or not
							type:"String", //the column type
							choiceInList:false, //when the column is in edit mode, the edition is a list of choices or not
							extraHeaders:{"0":"Inputs","1":"prop"}, //the extraHeaders list
						}],
			compact:true,
			pagination:{
				active:false,
				mode:'local'
			},		
			search:{
				url:"/datatable/get-examples"
			},
			order:{
				mode:'local', //or 
				active:false,
				by:'ContainerInputCode'
			},
			remove:{
				active:true,
			},
			save:{
				active:false,
				mode:'local',
			},
			hide:{
				active:true
			},
			edit:{
				active:false
			},
			messages:{
				active:true
			},
			extraHeaders:{
				number:2,
				dynamic:true,
			},
			name:"datatable"
	};
	
	$scope.datatableConfig2 = {
			columns : [{	header:"Code", //the title
							property:"code", //the property to bind
							edit:true, //can be edited or not
							hide:true, //can be hidden or not
							order:true, //can be ordered or not
							type:"String", //the column type
							choiceInList:false, //when the column is in edit mode, the edition is a list of choices or not
							extraHeaders:{"0":"Inputs","1":"prop2"}, //the extraHeaders list
						},
						{	header:"Etat", //the title
							property:"etat", //the property to bind
							edit:true, //can be edited or not
							hide:true, //can be hidden or not
							order:true, //can be ordered or not
							type:"String", //the column type
							choiceInList:true, //when the column is in edit mode, the edition is a list of choices or not
							listStyle:"multiselect",
							possibleValues:[{"code":"N","name":"Nouveau"},{"code":"IWP","name":"En attente de processus"},{"code":"IWE","name":"En attente d'experience"}],
							extraHeaders:{"0":"Inputs","1":"prop"}, //the extraHeaders list
						},{	header:"Volume", //the title
							property:"volume", //the property to bind
							edit:true, //can be edited or not
							hide:true, //can be hidden or not
							order:true, //can be ordered or not
							type:"String", //the column type
							choiceInList:false, //when the column is in edit mode, the edition is a list of choices or not
							extraHeaders:{"0":"Inputs","1":"prop"}, //the extraHeaders list
						}],
			compact:true,
			pagination:{
				active:false,
				mode:'local'
			},		
			search:{
				url:"/datatable/get-examples"
			},
			order:{
				mode:'local', //or 
				active:false,
				by:'ContainerInputCode'
			},
			remove:{
				active:true,
			},
			save:{
				active:false,
				mode:'local',
			},
			hide:{
				active:true
			},
			edit:{
				active:true
			},
			messages:{
				active:true
			},
			extraHeaders:{
				number:2,
				dynamic:true,
			},
			name:"datatable2"
	};
	
	$scope.init = function(){
		$scope.datatable = new datatable($scope, $scope.datatableConfig);
		$scope.datatable2 = new datatable($scope, $scope.datatableConfig2);
		//$scope.config = JSON.stringify($scope.datatable.config);
	};
	
	
	$scope.apply = function(){
		$scope.datatable.setConfig(JSON.parse($scope.config));
	};
	
	$scope.refresh = function(){
		$scope.config = JSON.stringify($scope.datatable.config);
	};
	
	$scope.search = function(){
		$scope.datatable.search();
		$scope.datatable2.search();
		$scope.config = JSON.stringify($scope.datatable.config);
	};
	
}

DemoCtrl.$inject = ['$scope','datatable'];