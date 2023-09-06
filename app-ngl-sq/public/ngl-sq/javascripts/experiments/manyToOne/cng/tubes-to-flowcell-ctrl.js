/* 06/01/2017 ajout $http pour l'import de fichier cbot-V2 */
angular.module('home').controller('CNGTubesToFlowcellCtrl',['$scope', '$parse','atmToDragNDrop','$http',
                                                               function($scope, $parse, atmToDragNDrop, $http ) {
	
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
		        	 "extraHeaders":{0:"librairie denaturée"}
		         },	
		         {
		        	 "header":Messages("containers.table.supportCode"),
		        	 "property":"inputContainer.support.code",
		        	 "order":true,
					 "edit":false,
					 "hide":true,
		        	 "type":"text",
		        	 "position":1,
		        	 "extraHeaders":{0:"librairie denaturée"}
		         },	
		         {
		        	"header":"Code aliquot",
		 			"property": "inputContainer.contents",
		 			"filter": "getArray:'properties.sampleAliquoteCode.value'| unique",
		 			"order":false,
		 			"hide":true,
		 			"type":"text",
		 			"position":1.5,
		 			"render": "<div list-resize='cellValue' list-resize-min-size='3'>",
		        	 "extraHeaders":{0:"librairie denaturée"}
			     },
		         {
		        	"header":Messages("containers.table.tags"),
		 			"property": "inputContainer.contents",
		 			"filter": "getArray:'properties.tag.value'|unique",
		 			"order":false,
		 			"hide":true,
		 			"type":"text",
		 			"position":2,
		 			"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
		        	 "extraHeaders":{0:"librairie denaturée"}
		         },				         
				 {
		        	 "header":Messages("containers.table.concentration") + " (nM)",
		        	 "property":"inputContainerUsed.concentration.value",
		        	 "order":true,
					 "edit":false,
					 "hide":true,
		        	 "type":"number",
		        	 "position":5,
		        	 "extraHeaders":{0:"librairie denaturée"}
		         },
		        
		         {
		        	 "header":Messages("containers.table.volume") + " (µL)",
		        	 "property":"inputContainerUsed.volume.value",
		        	 "order":true,
					 "edit":false,
					 "hide":true,
		        	 "type":"number",
		        	 "position":6,
		        	 "extraHeaders":{0:"librairie denaturée"}
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
		        	 "extraHeaders":{0:"librairie denaturée"}
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
	
		
	atmToSingleDatatable.data.setColumnsConfig(columns);
	
	atmToSingleDatatable.convertOutputPropertiesToDatatableColumn = function(property){
		return   this.$commonATM.convertSinglePropertyToDatatableColumn(property,"outputContainerUsed.experimentProperties.",{"0":"prep FC"});
		
	};
	atmToSingleDatatable.convertInputPropertiesToDatatableColumn = function(property){
		return   this.$commonATM.convertSinglePropertyToDatatableColumn(property,"inputContainerUsed.experimentProperties.",{"0":"lib-B"});
	};
	
	atmToSingleDatatable.addExperimentPropertiesToDatatable($scope.experimentType.propertiesDefinitions);
	
	//FDS 15/03/2019 NGL-2474: ajoute controles de coherence 
	//-1- type instrument
	/* jamais declenché sert a rien !!!!!!!
	$scope.$watch("experiment.instrument.typeCode" , function(newValue, oldValue){
		if (newValue && newValue !== oldValue ){
			console.log("1/watch-------instrument.typeCode.value="+newValue+"---------");
			checkAll();
		}
	})
	*/
	
	//-2- code instrument
	$scope.$watch("experiment.instrument.code", function(newValue, oldValue){
		if (newValue && newValue !== oldValue ) {
			console.log("2/watch-------instrument.code.value="+newValue+"---------");
			
			checkAll();
		}
	});
	
	//-3- category support output
	$scope.$watch("experiment.instrument.outContainerSupportCategoryCode", function(newValue, oldValue){
		if (newValue && newValue !== oldValue ) {
			console.log("3/watch--------instrument.outContainerSupportCategoryCode.value="+newValue+"---------");
			
			checkAll();
		}
	});	
	
	//-4- code Flowcell !! Necessaire car sinon dans certains cas ca ne fonctionne pas dans le cas initial
	$scope.$watch("experiment.instrumentProperties.containerSupportCode.value", function(newValue, oldValue){
		if (newValue && newValue !== oldValue ) {
			console.log("4/watch-------containerSupportCode.value="+newValue+"---------");


			checkAll();
		}
	});	
	
	function checkAll(){

		$scope.messages.clear();
		
		$scope.messages.clazz = "alert alert-warning";
		$scope.messages.text = "Alertes de configuration";
		$scope.data = { Alertes:[]} 
		
		//-1 nb lane
		checkNbLaneCompat();
		//-2 eventuellement plus tard les pattern de flowcell ??
		//  possible uniquement pour les cbot-OnBoard car le type est dans le nom
		//  pas de pattern Illumina disponible....
		
		if ( $scope.data.Alertes.length > 0 ){
			$scope.messages.showDetails = true;
			$scope.messages.open();
		}
	}
	
	function setAlert(msgKey, msgDetails){
		$scope.data[msgKey].push(msgDetails);
		$scope.messages.setDetails($scope.data);
	}
	
	function checkNbLaneCompat(){
		console.log("checkNbLaneCompat...");
		
		if ( undefined==$scope.experiment.instrument.outContainerSupportCategoryCode ||
			 undefined==$scope.experiment.instrument.typeCode ||
		     undefined==$scope.experiment.instrument.code) { return; }
		
		outContainerSupportCategoryCode=$scope.experiment.instrument.outContainerSupportCategoryCode
		instrumentTypecode=$scope.experiment.instrument.typeCode;
		instrumentCode=$scope.experiment.instrument.code;
		console.log("checkNbLaneCompat..."+instrumentCode+"/"+outContainerSupportCategoryCode)
		
		if ( instrumentTypecode === "cBot-onboard" ){
			if ( instrumentCode.match(/-Miseq/) && outContainerSupportCategoryCode != "flowcell-1" ){          // !!! (/Miseq/) avec "s"
				setAlert("Alertes","L'instrument Miseq n'accepte que la flowcell 1 piste.");
				//NB il y a aussi un blocage a la sauvegarde=> drools/prepa-flowcell/validation.drl
				
			} else if ( instrumentCode.match(/-NextSeq/) && outContainerSupportCategoryCode != "flowcell-4" ){ // !!! (/NextSeq/) avec "S"
				setAlert("Alertes","L'instrument NextSeq 500 n'accepte que la flowcell 4 pistes.");
				//NB il y a aussi un blocage a la sauvegarde=> drools/prepa-flowcell/validation.drl
				
			} else if ( instrumentCode.match(/-Hi/) && outContainerSupportCategoryCode != "flowcell-2" ){
				// cBot-interne-Hi9 ou 10 ou 11-A ou B), warning si différent de 2 pistes 
				setAlert("Alertes","Les Hiseq 2500 rapides n'acceptent que la flowcell 2 pistes.");
			}	
			// 18/11/2019 NGL-2752: Les NovaSeq 6000 ne doivent pas etre utilises en prepaflowcell classique !!!!!
			else if (isInstrumentNovaSeq6000()){
				setAlert("Alertes","L'instrument NovaSeq 6000 n'est pas utilisable dans cette expérience");
				//NB il faut une règle drools pour générer une erreur si l'utilisateur valide malgré cette alerte
			}
		
		} else if ( instrumentTypecode === "cBotV2" ){
			// cBotA,cBotB,cBotC,cBotD,cBotE,cBotF), warning si différent de 8 pistes 	
			if ( outContainerSupportCategoryCode != "flowcell-8" ){ 
				setAlert("Alertes","Les Hiseq 2000 et Hiseq 2500 High Throughput n'acceptent que la flowcell 8 pistes.");
			}
		}
	}
	
	
	//18/11/2019  NGL-2752 ajout SuperNova => pour prévoir l'arrivée d'autres instruments Novaseq créer fonction dédiée
	//            NB ce n'est pas un oubli, pas de novaseq-xp-fc-dock ici !!!
	function isInstrumentNovaSeq6000(){
		if ( instrumentCode.match(/MarieCurix/) || 
			 instrumentCode.match(/SuperNova/)  ||
			 instrumentCode.match(/BossaNova/) ) {
			return true;
		} else 
			return false;
	}

}]);
