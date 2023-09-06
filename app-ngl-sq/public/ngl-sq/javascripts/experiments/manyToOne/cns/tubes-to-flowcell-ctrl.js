angular.module('home').controller('CNSTubesToFlowcellCtrl',['$scope', '$parse', 'atmToDragNDrop',
                                                               function($scope, $parse, atmToDragNDrop) {
	
	var atmToSingleDatatable = $scope.atmService.$atmToSingleDatatable;
	
	var columns = [  
	             {
		        	 "header":Messages("containers.table.support.number"),
		        	 "property":"atomicTransfertMethod.line",
		        	 "order":true,
					 "edit":false,
					 "hide":true,
		        	 "type":"text",
		        	 "position":0,
		        	 "extraHeaders":{0:"solution stock"}
		         },	
		         {
		        	 "header":Messages("containers.table.supportCode"),
		        	 "property":"inputContainer.support.code",
		        	 "order":true,
					 "edit":false,
					 "hide":true,
		        	 "type":"text",
		        	 "position":1,
		        	 "extraHeaders":{0:"solution stock"}
		         },
		         {
		        	 "header":Messages("containers.table.workName"),
		        	 "property":"inputContainer.properties.workName.value",			        	
		        	 "order":true,
					 "edit":false,
					 "hide":true,
		        	 "type":"text",
		        	 "position":1.2,
		        	 "extraHeaders":{0:"solution stock"}
		         },	
		         {
		        	"header":Messages("containers.table.tags"),
		 			"property": "inputContainer.contents",
		 			"filter": "getArray:'properties.tag.value'| unique",
		 			"order":false,
		 			"hide":true,
		 			"type":"text",
		 			"position":2,
		 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
		        	"extraHeaders":{0:"solution stock"}
		         },			         
				 {
		        	 "header":Messages("containers.table.concentration") + " (nM)",
		        	 "property":"inputContainerUsed.concentration.value",
		        	 "order":true,
					 "edit":false,
					 "hide":true,
		        	 "type":"number",
		        	 "position":5,
		        	 "extraHeaders":{0:"solution stock"}
		         },
		        
		         {
		        	 "header":Messages("containers.table.volume") + " (µL)",
		        	 "property":"inputContainerUsed.volume.value",
		        	 "order":true,
					 "edit":false,
					 "hide":true,
		        	 "type":"number",
		        	 "position":6,
		        	 "extraHeaders":{0:"solution stock"}
		         },
		         {
		        	 "header":Messages("containers.table.state.code"),
		        	 "property":"inputContainer.state.code",
		        	 "order":true,
					 "edit":false,
					 "hide":true,
		        	 "type":"text",
					 "filter":"codes:'state'",
		        	 "position":7,
		        	 "extraHeaders":{0:"solution stock"}
		         },
		         {
		        	 "header":Messages("containers.table.percentage"),
		        	 "property":"inputContainerUsed.percentage",
		        	 "order":true,
					 "edit":false,
					 "hide":true,
		        	 "type":"number",
		        	 "position":50,
		        	 "extraHeaders":{0:"prep FC"}
		         },		         
		         {
		        	 "header":Messages("containers.table.code"),
		        	 "property":"outputContainerUsed.code",
		        	 "order":true,
					 "edit":false,
					 "hide":true,
					 "type":"text",
		        	 "position":400,
		        	 "extraHeaders":{0:"prep FC"}
		         },
		         {
		        	 "header":Messages("containers.table.stateCode"),
		        	 "property":"outputContainer.state.code | codes:'state'",
		        	 "order":true,
					 "edit":false,
					 "hide":true,
					 "type":"text",
		        	 "position":500,
		        	 "extraHeaders":{0:"prep FC"}
		         }
		         ];
	
	//overide defaut method
	atmToSingleDatatable.convertOutputPropertiesToDatatableColumn = function(property){
		return   this.$commonATM.convertSinglePropertyToDatatableColumn(property,"outputContainerUsed.experimentProperties.",{"0":"prep FC"});
		
	};
	atmToSingleDatatable.convertInputPropertiesToDatatableColumn = function(property){
		if(property.displayOrder < 20){		
			return   this.$commonATM.convertSinglePropertyToDatatableColumn(property,"inputContainerUsed.experimentProperties.",{"0":"denaturation"});
		}else if(property.displayOrder < 30){
			return   this.$commonATM.convertSinglePropertyToDatatableColumn(property,"inputContainerUsed.experimentProperties.",{"0":"dilution"});
		}else if(property.displayOrder < 50){
			return   this.$commonATM.convertSinglePropertyToDatatableColumn(property,"inputContainerUsed.experimentProperties.",{"0":"prep FC"});
		}
	};
	
		
	atmToSingleDatatable.data.setColumnsConfig(columns);
	atmToSingleDatatable.addExperimentPropertiesToDatatable($scope.experimentType.propertiesDefinitions);
	
	$scope.$parent.changeValueOnFlowcellDesign = function(){
		$scope.atmService.data.updateDatatable();
		
		if($scope.mainService.isEditMode() && !$scope.isCreationMode()){
			$scope.messages.clazz = "alert alert-warning";
			$scope.messages.text = "Vous venez de modifier une valeur";
			$scope.messages.text += ", vous devez impérativement cliquer sur sauvegarder pour que les calculs de la FDR se remettent à jour.";
			$scope.messages.showDetails = false;
			$scope.messages.open();
		}
	};
	
	$scope.getSampleAndTagsWithSecondary = function(container){
		var sampleCodeAndTags = [];
		angular.forEach(container.contents, function(content){
			if(content.properties.secondaryTag != undefined && content.properties.tag != undefined && content.sampleCode != undefined){
				sampleCodeAndTags.push(content.sampleCode+" / "+content.properties.secondaryTag.value+" / "+content.properties.tag.value);
			}else if(content.properties.tag != undefined && content.sampleCode != undefined){
 				sampleCodeAndTags.push(content.sampleCode+" / / "+content.properties.tag.value);
 			}else if(content.properties.secondaryTag != undefined && content.sampleCode != undefined){
 				sampleCodeAndTags.push(content.sampleCode+" / "+content.properties.secondaryTag.value+" / ");
 			}else if(content.sampleCode != undefined){
 				sampleCodeAndTags.push(content.sampleCode);
 			}
		});
		return sampleCodeAndTags;
	};
	
}]);
