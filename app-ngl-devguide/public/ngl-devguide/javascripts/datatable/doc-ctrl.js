"use strict"

function DocCtrl($scope,datatable) {

	
	$scope.datatableConfig = {
			columns : [{	header:"Code Container", //the title
							property:"code", //the property to bind
							id:'test', //the column id
							edit:false, //can be edited or not
							hide:true, //can be hidden or not
							order:true, //can be ordered or not
							type:"String", //the column type
							choiceInList:false, //when the column is in edit mode, the edition is a list of choices or not
							//extraHeaders:{"0":"Inputs"}, //the extraHeaders list
						},
						{	header:"test 1", //the title
							property:"test", //the property to bind
							id:'test2', //the column id
							edit:false, //can be edited or not
							hide:true, //can be hidden or not
							order:true, //can be ordered or not
							type:"String", //the column type
							choiceInList:false, //when the column is in edit mode, the edition is a list of choices or not
							//extraHeaders:{"0":"Inputs"}, //the extraHeaders list
						}],
			compact:false,
			pagination:{
				active:true
			},		
			search:{
				url:"",
				active:false
			},
			order:{
				mode:'local', //or 
				active:true,
				by:'ContainerInputCode'
			},
			remove:{
				active:false,
			},
			save:{
				active:true,
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
				number:0,
				dynamic:true,
			},
			name:"datatable"
	};
	
	
	$scope.init = function(){
		$scope.datatable = new datatable($scope, $scope.datatableConfig);
	};
	
}

DocCtrl.$inject = ['$scope','datatable'];