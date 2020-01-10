angular.module('home').controller('OneToVoidFluoQuantificationCNGCtrl',['$scope', '$parse','$http',
                                                             function($scope,$parse,$http) {
	
	// NGL-1055 : surcharger la variable "name" definie dans le controleur parent ( one-to-void-qc-ctrl.js) => nom de fichier CSV exporté 
	var config = $scope.atmService.data.getConfig();
	config.name = $scope.experiment.typeCode.toUpperCase();
	
	$scope.atmService.data.setConfig(config);	
	
	$scope.$parent.copyPropertiesToInputContainer = function(experiment){
		// les propriétés de l'expérience doivent etres copiées dans le container
		experiment.atomicTransfertMethods.forEach(function(atm){
			var inputContainerUsed =$parse("inputContainerUseds[0]")(atm);
			if(inputContainerUsed){
				var concentration1 = $parse("experimentProperties.concentration1")(inputContainerUsed);
				// 26/03/2018: NGL-1970 la copie de la concentration ne doit etre faite que si l'utilisateur le demande explicitement !!!
				if (concentration1  &&  $scope.experiment.experimentProperties.copyConcentration.value){
					inputContainerUsed.newConcentration = concentration1;
				} else {
					inputContainerUsed.newConcentration = null;
				}
				
				// pas de newsize 
			}
		});
	};
	
	var importData = function(){
		$scope.messages.clear();
		
		$http.post(jsRoutes.controllers.instruments.io.IO.importFile($scope.experiment.code).url, $scope.file)
		.success(function(data, status, headers, config) {
			$scope.messages.clazz="alert alert-success";
			$scope.messages.text=Messages('experiments.msg.import.success');
			$scope.messages.showDetails = false;
			$scope.messages.open();	
			//only atm because we cannot override directly experiment on scope.parent
			$scope.experiment.atomicTransfertMethods = data.atomicTransfertMethods;
			$scope.file = undefined;
			angular.element('#importFile')[0].value = null;
			$scope.$emit('refresh');
			
		})
		.error(function(data, status, headers, config) {
			$scope.messages.clazz = "alert alert-danger";
			$scope.messages.text = Messages('experiments.msg.import.error');
			$scope.messages.setDetails(data);
			$scope.messages.open();	
			$scope.file = undefined;
			angular.element('#importFile')[0].value = null;
		});
	};
	
	//NGL-1761: pour l'instant seul le fichier spectramax est disponible (masquer le bouton pour qbit)
	$scope.button = {
		isShow:function(){
			return ($scope.experiment.instrument.typeCode === "spectramax" && !$scope.mainService.isEditMode() 
					&&  ( $scope.isInProgressState() || Permissions.check("admin")) )	
			},
		isFileSet:function(){
			return ($scope.file === undefined)?"disabled":"";
		},
		click:importData,		
	};
	
	var columns = $scope.atmService.data.getColumnsConfig();

	columns.push({
    	"header":"Code aliquot",
		"property": "inputContainer.contents",
		"filter": "getArray:'properties.sampleAliquoteCode.value'| unique",
		"order":false,
		"hide":true,
		"type":"text",
		"position":7.5,
		"render": "<div list-resize='cellValue' list-resize-min-size='3'>",
		"extraHeaders":{0:Messages("experiments.inputs")}
	});

	columns.push({
		"header" : Messages("containers.table.tags"),
		"property" : "inputContainer.contents",
		"filter" : "getArray:'properties.tag.value' | unique",
		"order" : false,
		"hide" : true,
		"type" : "text",
		"position" : 8,
		"render" : "<div list-resize='cellValue' list-resize-min-size='3'>",
		"extraHeaders" : {0 : Messages("experiments.inputs")}
	});

	// 04/09/2017 niveau process ET contents =>utiliser properties et pas processProperties
	columns.push({
	   	 "header": Messages("containers.table.libProcessType"),
	   	 "property" : "inputContainerUsed.contents",
	   	 "filter" : "getArray:'properties.libProcessTypeCode.value' | unique | codes:'value'",
	   	 "order":true,
		 "edit":false,
		  "hide":true,
	   	 "type":"text",
	   	 "position":8.1,
	   	 "extraHeaders":{0 : Messages("experiments.inputs")}
	  }); 
	
	/*                  NGL-1226 11/2018  demande de ne plus afficher ces 2 colonnes....
	// 04/09/2017 utiliser processProperties
	columns.push({
		"header": Messages("containers.table.expectedBaits"),
		"property" : "inputContainerUsed.contents",
		"filter" : "getArray:'processProperties.expectedBaits.value' | unique | codes:'value'",
		"order":true,
		"edit":false,
		"hide":true,
		"type":"text",
		"position":8.2,
		"extraHeaders":{0 : Messages("experiments.inputs")}
		});
	
	// 04/09/2017 niveau process ET contents =>utiliser properties et pas processProperties
	columns.push({
	  	 "header": Messages("containers.table.captureProtocol"),
	  	 "property" : "inputContainerUsed.contents",
	  	 "filter" : "getArray:'properties.captureProtocol.value' | unique | codes:'value'",
	  	 "order":true,
		 "edit":false,
		 "hide":true,
	  	 "type":"text",
	  	 "position":8.2,
	  	 "extraHeaders":{0 : Messages("experiments.inputs")}
	   });
     */

	
	$scope.atmService.data.setColumnsConfig(columns);
	
}]);