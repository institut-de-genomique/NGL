"use strict";

angular.module('home').controller('SearchCtrl', ['$scope', '$http', '$q', '$filter', 'datatable' , 
	function($scope, $http, $q, $filter, datatable) {

	var datatableExperimentConfig = {
			order :{by:'traceInformation.creationDate', reverse:true, mode:'local'},
			search:{
				url:jsRoutes.controllers.experiments.api.Experiments.list()
			},
			pagination:{
				mode:'local',
				numberRecordsPerPage:5
			},
			select:{
				active:false
			},
			showTotalNumberRecords:false,
			columns : [
			           {  	property:"code",
					    	header: "Code",
					    	type :"text",
					    	"position":1,
					    	order:true
						},
						{
							"header":Messages("experiments.instrument"),
							"property":"instrument.code",
							"order":true,
							"hide":true,
							"position":2,
							"type":"text",
							"filter":"codes:'instrument'"
						},		
						{
							"header":Messages("experiments.table.typeCode"),
							"property":"typeCode",
							"filter":"codes:'type'",
							"order":true,
							"hide":true,
							"position":4,
							"type":"text"
						},
						{
							"header":Messages("experiments.table.state.code"),
							"property":"state.code",
							"order":true,
							"type":"text",
							"position":5,
							"hide":true,
							"filter":"codes:'state'"
						},
						{
							"header":Messages("containers.table.sampleCodes"),
							"property":"sampleCodes",
							"order":false,
							"hide":true,
							"position":9,
							"type":"text",
							"render":"<div list-resize='value.data.sampleCodes | unique' list-resize-min-size='3'>",
						},
						{
							"header":Messages("experiments.table.projectCodes"),
							"property":"projectCodes",
							"order":false,
							"render":"<div list-resize='value.data.projectCodes | unique' list-resize-min-size='3'>",
							"hide":true,
							"position":10,
							"type":"text"
						},
						{
							"header":Messages("experiments.table.creationDate"),
							"property":"traceInformation.creationDate",
							"order":true,
							"hide":true,
							"position":11,
							"type":"date"
						},
						{
							"header":Messages("experiments.table.createUser"),
							"property":"traceInformation.createUser",
							"order":true,
							"hide":true,
							"position":12,
							"type":"text"
						}
						]
	};
	
	var datatableProcessIPConfig = {
			order :{by:'traceInformation.creationDate', mode:'local'},			
			search:{
				url:jsRoutes.controllers.processes.api.Processes.list()
			},
			pagination:{
				mode:'local',
				numberRecordsPerPage:5
			},
			select:{
				active:false
			},
			group:{
				active:true,
				by:"inputContainerSupportCode",
				showOnlyGroups: true
			},
			showTotalNumberRecords:false,
			columns : [
						{
							"header":Messages("processes.table.inputContainerSupportCode"),
							"property":"inputContainerSupportCode",
							"position":0.5,
							"type":"text"
						},
			           {
							"header":Messages("processes.table.inputContainerCode"),
							"property":"inputContainerCode",
							"position":1,
							"type":"text",
							"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
							"groupMethod":"collect"
						},
			           {
							"header":Messages("processes.table.projectCode"),
							"property":"projectCodes",
							"position":2,
							"type":"text",
							"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
							"groupMethod":"collect:true"
						},
						{
							"header":Messages("processes.table.sampleCode"),
							"property":"sampleCodes",
							"position":3,
							"type":"text",
							"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
							"groupMethod":"collect:true"
						},
			          
			           {
							"header":Messages("processes.table.typeCode"),
							"property":"typeCode",
							"position":4,
							"filter":"codes:'type'",
							"type":"text" ,
							"groupMethod":"unique"							
						} ,
						{
							"header" : Messages("processes.table.currentExperimentTypeCode"),
							"property" : "currentExperimentTypeCode",
							"filter" : "codes:'type'",
							"position" : 5,
							"type" : "text",
							"groupMethod":"unique"		
							
						},
						{
							"header" : Messages("processes.table.outputContainerSupportCodes"),
							"property" : "outputContainerSupportCodes",
							"position" : 6,
							"filter":"unique",
							// "render" : "<div list-resize='cellValue' list-resize-min-size='2'>",
							"type" : "text",
							"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
							"groupMethod":"collect:true"
						},
						{
							"header" : Messages("processes.table.creationDate"),
							"property" : "traceInformation.creationDate",
							"position" : 7,
							"format" : Messages("datetime-hour.format"),
							"type" : "date",
							"groupMethod":"unique"	
						},
						{
							"header" : Messages("processes.table.createUser"),
							"property" : "traceInformation.createUser",
							"position" : 8,
							"type" : "text",
							"groupMethod":"unique"	
						},
						{
							"header" : Messages("processes.table.comments"),
							"property" : "comments[0].comment",
							"position" : 9,
							"type" : "text",
							"groupMethod":"collect:true"	
						}
						
						]
	};  
	
	var datatableProcessNConfig = {
			order :{by:'traceInformation.creationDate', reverse:true, mode:'local'},
			search:{
				url:jsRoutes.controllers.processes.api.Processes.list()
			},
			pagination:{
				mode:'local',
				numberRecordsPerPage:5
			},
			select:{
				active:false
			},
			showTotalNumberRecords:false,
			columns : [
			           {
						"header":Messages("processes.table.inputContainerCode"),
						"property":"inputContainerCode",
						"position":1,
						"type":"text"
						},
			           {
							"header":Messages("processes.table.projectCode"),
							"property":"projectCodes",
							"position":2,
							"type":"text"
						},
						{
							"header":Messages("processes.table.sampleCode"),
							"property":"sampleCodes",
							"position":3,
							"type":"text"
						},
			          
			           {
							"header":Messages("processes.table.typeCode"),
							"property":"typeCode",
							"position":4,
							"filter":"codes:'type'",
							"type":"text"         
						} ,
						
						{
							"header" : Messages("processes.table.creationDate"),
							"property" : "traceInformation.creationDate",
							"position" : 7,
							"format" : Messages("datetime.format"),
							"type" : "date"
						},
						{
							"header" : Messages("processes.table.createUser"),
							"property" : "traceInformation.createUser",
							"position" : 8,
							"type" : "text"
						},
						{
							"header" : Messages("processes.table.comments"),
							"property" : "comments[0].comment",
							"position" : 9,
							"type" : "text"
						}
						
						]
	};  
		$scope.experimentIPDatatable = datatable(datatableExperimentConfig);			
		$scope.experimentIPDatatable.search({stateCodes:["IP"]});
		
		$scope.experimentNDatatable = datatable(datatableExperimentConfig);			
		$scope.experimentNDatatable.search({stateCodes:["N"]});
		
		$scope.processIPDatatable = datatable(datatableProcessIPConfig);			
		$scope.processIPDatatable.search({stateCodes:["IP"]});
		$scope.processIPDatatable.getTotalNumberRecords = function(){
			if($scope.processIPDatatable.config.group.active && $scope.processIPDatatable.config.group.start && !$scope.processIPDatatable.config.group.showOnlyGroups){
				return $scope.processIPDatatable.totalNumberRecords + " - "+$scope.processIPDatatable.allGroupResult.length;
			}else if($scope.processIPDatatable.config.group.active && $scope.processIPDatatable.config.group.start && $scope.processIPDatatable.config.group.showOnlyGroups){
				return ($scope.processIPDatatable.allGroupResult)?$scope.processIPDatatable.allGroupResult.length:0;
			}else{
				return $scope.processIPDatatable.totalNumberRecords;
			}			
		};
		
		$scope.processNDatatable = datatable(datatableProcessNConfig);			
		$scope.processNDatatable.search({stateCodes:["N"]});
		
		
		
		
		
		$http.get(jsRoutes.controllers.containers.api.Containers.list().url,{params:{"stateCodes":"IW-D","list":true}}).then(function(result){
			var containerCodes = result.data.map(function(container){return container.code;});
			console.log("nb containers"+containerCodes.length);
			
			if (containerCodes.length > 0 ) {		
				var promises = [];
				var nbElementByBatch = Math.ceil(containerCodes.length / 3); //6 because 6 request max in parrallel with firefox and chrome
				var queries = [];
				for (var i = 0; i < 3 && containerCodes.length > 0; i++) {
					if (containerCodes[i] == ""){
						containerCodes.splice(i,i+1); 
					}else{
						var subContainerCode = containerCodes.splice(0, nbElementByBatch); 
						promises.push($http.get(jsRoutes.controllers.processes.api.Processes.list().url, {params:{"outputContainerCodes":subContainerCode,"stateCode":"IP","includes":"experimentCodes"}}));  
						promises.push($http.get(jsRoutes.controllers.processes.api.Processes.list().url, {params:{"inputContainerCodes":subContainerCode,"stateCode":"IP","includes":"experimentCodes"}}));               		
					}
				}			
				
				//promises[0] = $http.get(jsRoutes.controllers.processes.api.Processes.list().url, {params:{"outputContainerCodes":containerCodes,"stateCode":"IP","includes":"experimentCodes"}});
				//promises[1] = $http.get(jsRoutes.controllers.processes.api.Processes.list().url, {params:{"inputContainerCodes":containerCodes,"stateCode":"IP","includes":"experimentCodes"}});
				
				
				$q.all(promises).then(function(results){
					
					var extractDate = function(value){
						return value.split(/(\d+_\d+)/)[1];
					}
					var experimentCodes = [];
					angular.forEach(results, function(result){
						experimentCodes = experimentCodes.concat(result.data.map(function(process){
							return $filter('orderBy')(process.experimentCodes,extractDate,true)[0];
						})										
					);
					});
					/*experimentCodes = experimentCodes.concat(results[0].data.map(function(process){
							return $filter('orderBy')(process.experimentCodes,extractDate,true)[0];
						})										
					);
					experimentCodes = experimentCodes.concat(results[1].data.map(function(process){
						return $filter('orderBy')(process.experimentCodes,extractDate,true)[0];
					 })										
					);*/
				
					
					experimentCodes = $filter('unique')(experimentCodes);
					console.log("nb experimentCode"+experimentCodes.length);
					
					$scope.experimentDispatchDatatable = datatable(datatableExperimentConfig);			
					if(experimentCodes.length > 0){
						$scope.experimentDispatchDatatable.search({codes:experimentCodes});
					}
					var params = "";
					experimentCodes.forEach(function(code){
						params +="codes="+code+"&"
					}, params);
					params.replace(/&$/,"");
					
					$scope.getExperimentCodesParams = function(){
						return params
					}
				});
			}else {
				return null;
			}
			
		});
		
}]);


