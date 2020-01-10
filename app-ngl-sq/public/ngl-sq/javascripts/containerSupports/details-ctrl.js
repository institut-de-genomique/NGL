"use strict";

angular.module('home').controller('DetailsCtrl', ['$scope', '$http','$window', '$q', '$filter', '$routeParams', 'datatable', 'mainService', 'tabService',
                                                  function($scope,$http,$window,$q,$filter,$routeParams,datatable,mainService,tabService){


	/*
	 * ActiveTab System
	 */
	$scope.getTabClass = function(value){
		if(value === mainService.get('containerSupportActiveTab')){
			return 'active';
		}
	}
	$scope.setActiveTab = function(value){
		mainService.put('containerSupportActiveTab', value);
	};
	
	$scope.getViewClass = function(value){
		if(value === mainService.get('containerSupportActiveView')){
			return 'active';
		}
	}
	$scope.setActiveView = function(value){
		mainService.put('containerSupportActiveView', value);
	};
	
	$scope.goToContainer = function(x, y){
		if(angular.isDefined($scope.containers)){
			for(var i=0; i<$scope.containers.length; i++){
				if($scope.containers[i].support.column === (x+'') && $scope.containers[i].support.line===(y+'')){
					$window.open(jsRoutes.controllers.containers.tpl.Containers.get($scope.containers[i].code).url, 'containers');
				}
			}
		}
	};
	
	
	//To display sample and tag in one cell
	$scope.getContentInfos = function(container){
		var sampleCodeAndTags = [];
		angular.forEach(container.contents, function(content){
			var value = content.projectCode+" / "+content.sampleCode;
			
			if(content.properties && content.properties.libProcessTypeCode){
				value = value +" / "+content.properties.libProcessTypeCode.value;
			}
			
			if(content.properties && content.properties.tag){
				value = value +" / "+content.properties.tag.value;
			}
			
			sampleCodeAndTags.push(value);
		});
		return sampleCodeAndTags;
	};

	/*
	 * Get Bootstrap + Display Method for all views
	 */
	$scope.getClass = function(x, y){
		if(angular.isDefined($scope.containers)){
			for(var i=0; i<$scope.containers.length; i++){
				if($scope.containers[i].support.column === (x+'') && $scope.containers[i].support.line===(y+'')){
		        	 $scope.data = $scope.containers[i];		        	 
		        	 if($scope.data.valuation.valid === "FALSE"){
		        		 return "alert alert-danger";
		        	 }else if($scope.data.valuation.valid === "TRUE"){
		        		 return "alert alert-success";
		        	 }else{
		        		 return "alert alert-default";
		        	 }					
				}
			}
		}
		$scope.data = undefined;
		return "";
     }

	/*
	 * Set Coordinates $scope.nbCol & $scope.nbLine
	 */
	var setColXLine = function(column, line){
		var azArray = ['A','B','C','D','E','F','G','H','I','J','K','L','M',
		               'N','O','P','Q','R','S','T','U','V','W','X','Y','Z'];
		$scope.nbCol = [];
		for(var i=1; i<=column ; i++){
			$scope.nbCol.push(i);
		}
		if($scope.target === "plate"){
			$scope.nbLine = azArray.slice(0,line);
		}else{ // Case when display Method would not work
			$scope.nbLine = [];
			for(var k=1; k<=line ; k++){
				$scope.nbLine.push(k);
			}
		}
	};

	/*
	 * Method filterCategorySupport() config the display... 
	 *  //	=> TODO
	 */
	var filterCategorySupport = function(){
		var categoryCode;
		if(!angular.isUndefined($scope.support.categoryCode)){
			categoryCode = $scope.support.categoryCode;
			if(categoryCode.includes('irys')){
				$scope.target = 'iryschip';
				setColXLine(1,2); 
			}else if(categoryCode.includes('flowcell')){
				$scope.target = 'flowcell';
				for(var i=1; i<=8; i++){
					if(categoryCode.includes(i.toString())){
						setColXLine(1,i);
					}
				}
			}else if(categoryCode.includes('mapcard')){
				$scope.target = 'mapcard';
				setColXLine(1,1);
			}else if(categoryCode.includes('plate')){
				$scope.target = 'plate';
				if(categoryCode.includes('96')){
					setColXLine(12,8);
				}else if(categoryCode.includes('384')){
					setColXLine(24,16);
				}
			}else{
				$scope.target = undefined;				
			}
		}
		if(!angular.isUndefined($scope.target)){
			$scope.dynamicMessage = Messages("containerSupports.button."+$scope.target); // Build msg for the button			
		}
		
		if(undefined === mainService.get('containerSupportActiveView')){
			mainService.put('containerSupportActiveView', 'udt');
		}
		
	};
	
	/*
	 * Configuration of the datatable
	 */
	var datatableConfig = {
			select:{
				active:false
			},
			cancel:{
				active:false
			},
			pagination:{
				active:false
			},
			search:{
				active:false
			},
			order:{
				active:true,
				mode:'local'
			},
			hide:{
				active:true,
				showButton:true,
				showButtonColumn:true
			},
			exportCSV:{
				active:true,
				showButton:true
			},
			/*
			lines:{
				trClass: function(value){
					if(value.valuation.valid === "TRUE"){
						return "success";
					}else if(value.valuation.valid === "FALSE"){
						return "danger";
					}
				}
			},*/
			

			columns: [
						{
							"header":Messages("containers.table.support.line"),
							"property":"support.line",
							"order":true,
							"hide":true,
							"position":1,
							"type":"text"
						},
						{
							"header":Messages("containers.table.support.column"),
							"property":"support.column*1",
							"order":true,
							"hide":true,
							"position":1.1,
							"type":"number"							
						},
						// NGL-1055: mettre stringToArray dans filter et pas dans render	
						{
							"header":Messages("containers.table.code"),
							"property":"code",
							"filter":"stringToArray | unique",
							"order":true,
							"hide":true,
							"position":3,
							"type":"text",
							"render":"<div list-resize='cellValue' list-resize-min-size='2'>",
							"groupMethod":"collect"							
						},
						{
							"header":Messages("containers.table.fromTransformationTypeCodes"),
							"property":"fromTransformationTypeCodes",
						  	"position":4,
						  	"hide":true,
						  	"order":false,
						  	"type":"text",
						  	"render":"<div list-resize='cellValue | unique' list-resize-min-size='3'>",
						  	"filter":"unique | codes:\"type\"",
						  	"groupMethod":"collect"	
						},					
						{
							"header":Messages("containerSupports.table.projectCodes"),
							"property":"projectCodes",
							"position":5,
							"render":"<div list-resize='value.data.projectCodes | unique' list-resize-min-size='3'>",
							"order":true,
							"type":"text",
							"hide":true,
						},
						{
							"header":Messages("containerSupports.table.sampleCodes"),
							"property":"sampleCodes",
							"position":5.1,
							"order":false,
							"hide":true,
							"type":"text",
							"render":"<div list-resize='value.data.sampleCodes | unique' list-resize-min-size='3'>",
							"groupMethod":"collect"
						},

						{
							"header":Messages("containers.table.sampleCodes.length"),
							"property":"sampleCodes.length",
							"order":true,
							"hide":true,
							"position":5.2,
							"type":"number",
							"groupMethod":"sum"
						},

						//SampleTypes
						{
							"header":Messages("containers.table.sampleTypes"),
							"property":"contents",
							"order":false,
							"hide":false,
							"position":5.3,
							"type":"text",
							"filter":"getArray:'sampleTypeCode' | unique | codes:\"type\"",
							"groupMethod":"collect"
						},
						//LibProcessType
						{
							"header":Messages("containers.table.libProcessType"),
							"property":"contents",
							"order":false,
							"hide":false,
							"position":5.4,
							"type":"text",
							"filter":"getArray:'properties.libProcessTypeCode.value'|codes:'value'|unique",
							"groupMethod":"collect"
						},
						{
							"header":Messages("containers.table.contents.length"),
							"property":"contents.length",
							"order":true,
							"hide":true,
							"position":8,
							"type":"number",
							"groupMethod":"sum"
						},
						{
							"header":Messages("containers.table.tags"),
							"property": "contents",
							"order":false,
							"hide":true,
							"type":"text",
							"position":9,
							"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
							"filter":"getArray:'properties.tag.value' | unique",
							"groupMethod":"collect"
						},

						{
							"header":Messages("containers.table.concentration.value"),
							"property":"concentration.value",
							"order":true,
							"hide":true,
							"position":10,
							"format":3,
							"type":"number",
							"groupMethod":"unique"
						},
						{
							"header":Messages("containers.table.concentration.unit"),
							"property":"concentration.unit",
							"order":true,
							"hide":true,
							"position":10.1,
							"type":"text",
							"groupMethod":"unique"
						},
						{
							"header":Messages("containers.table.state.code"),
							"property":"state.code",
							"order":true,
							"hide":true,
							"type":"text",
							"edit":true,
							"position":10.2,
							"choiceInList": true,
							"listStyle":"bt-select",
							"possibleValues":"searchService.lists.getStates()", 
							"filter":"codes:'state'",
							"groupMethod":"unique"	
						},
						{
							"header":Messages("containers.table.valid"),
							"property":"valuation.valid",
							"order":true,
							"type":"text",
							"edit":false,
							"hide":true,
							"position":10.3,
							"choiceInList": true,
							"listStyle":"bt-select",
							"possibleValues":"searchService.lists.getValuations()", 
							"filter":"codes:'valuation'"
						}

			          ]
	};
	
	/*
	 * init()
	 */
	var init = function(){
		
		var promise = [];
		promise.push($http.get(jsRoutes.controllers.containers.api.ContainerSupports.get($routeParams.code).url));
		promise.push($http.get(jsRoutes.controllers.containers.api.Containers.list().url, {params: {supportCode:$routeParams.code}}));
		
		$q.all(promise).then(function(results){
			if(tabService.getTabs().length == 0){
				tabService.addTabs({label:Messages('containerSupports.tabs.search'),href:jsRoutes.controllers.containers.tpl.ContainerSupports.home("search").url,remove:true});
				tabService.addTabs({label:$routeParams.code,href:jsRoutes.controllers.containers.tpl.ContainerSupports.home($routeParams.code).url,remove:true});
				tabService.activeTab($scope.getTabs(1));
			}
			
			$scope.support = results[0].data;
			$scope.containers = results[1].data;
			$scope.datatable = datatable(datatableConfig);
			$scope.datatable.setData($filter('orderBy')($scope.containers, ["support.column*1","support.line"]), $scope.containers.length);
			
			if(undefined === mainService.get('containerSupportActiveTab')){
				mainService.put('containerSupportActiveTab', 'general');
			}
			
			filterCategorySupport();
		});		
	};

	init();
	
}]);