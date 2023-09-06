/* FDS NGL-836 19/11/2018 d'apres Samples   PAS DEMANDEE POUR L'INSTANT */
"use strict";

angular.module('home').controller('DetailsCtrl', ['$scope', '$http', '$q', '$routeParams', '$filter','$sce','mainService', 'lists', 'messages', 
                                                  function($scope,$http,$q,$routeParams,$filter,$sce,mainService,lists,messages){

	$scope.angular = angular;
	
	$scope.convertToBr = function(text){
		if(text)return $sce.trustAsHtml(text.replace(/\n/g, "<br>"));
	};
	
	/* PAS DE SAVE ...pour l'instant
	 $scope.save = function(){
		saveInProgress = true;	
		console.log("sample "+$scope.sample.code);
		$http.put(jsRoutes.controllers.samples.api.Samples.update($scope.sample.code).url, $scope.sample)
	//	$http.put("/api/samples/"+$scope.sample.code, $scope.sample)
		.success(function(data, status, headers, config) {
		
			$scope.sample = data;
			$scope.messages.setSuccess("save");						
			mainService.stopEditMode();
			
			saveInProgress = false;									
		})
		.error(function(data, status, headers, config) {
		
			$scope.messages.setError("save");
			$scope.messages.setDetails(data);				
			saveInProgress = false;				
		});				
	};
	*/

	$scope.cancel = function(){
		$scope.messages.clear();
		mainService.stopEditMode();
		updateData();				
	};

	/* PAS DE EDIT MODE...pour l'instant
	$scope.activeEditMode = function(){
		$scope.messages.clear();
		mainService.startEditMode();		
	}
	*/
	
	$scope.getPropertyDefinition = function(key){
		$http.get(jsRoutes.controllers.common.api.PropertyDefinitions.get(key).url).then(function(response) {
			var propertyDefinitions = {};
		});
	}
	
	/* PAS DE SAVE ...pour l'instant
	$scope.isSaveInProgress = function(){
		return saveInProgress;
	};  
	
	var saveInProgress = false;\
	
	*/
	var indexPropertyDefinitionMap = {};
	
	var init = function(){
		$scope.messages = messages();
		$scope.lists = lists;
		$scope.mainService = mainService;
		mainService.stopEditMode();

		/// Parameter ???? Index 
		
		// ARRIVE PAS A TROUVER LE PREMIER PARAMETRE DYNAMIQUEMENT !!!!!! pourquoi ?????
		//$http.get(jsRoutes.controllers.commons.api.Parameters.get($routeParams.typeCode, $routeParams.code).url).then(function(response) {

		
		$http.get(jsRoutes.controllers.commons.api.Parameters.get('index-illumina-sequencing', $routeParams.code).url).then(function(response) {
		///$http.get(jsRoutes.controllers.indexes.api.Indexes.get($routeParams.code).url).then(function(response) {
			$scope.index = response.data;		
			if(tabService.getTabs().length == 0){			
				tabService.addTabs({label:Messages('index.tabs.search'),href:jsRoutes.controllers.indexes.tpl.Indexes.home("search").url,remove:true});
				tabService.addTabs({label:$scope.index.code,href:jsRoutes.controllers.indexes.tpl.Indexes.get($scope.index.code).url,remove:true});
				tabService.activeTab($scope.getTabs(1));
			}
			
			//$scope.lists.refresh.resolutions({"objectTypeCode":"Sample"}, "sampleResolutions");
			

			if(undefined === mainService.get('indexActiveTab')){
				mainService.put('indexActiveTab', 'general');

			}
		});
	
		/// Parameter ???? Index 
		$http.get(jsRoutes.controllers.commons.api.PropertyDefinitions.list().url,{params:{'levelCode':'Parameter'}}).then(function(response) {
			response.data.forEach(function(pdef){
					this[pdef.code]=pdef;
			}, indexPropertyDefinitionMap);

		});
	}
	init();

	$scope.getIndexPropertyDefinitionValueType = function(key){
		var propertyDef = indexPropertyDefinitionMap[key];
		if(propertyDef){
			return propertyDef.valueType;
		}
		return null;
	}

}]);