// TEST ajouter mainService pour 3599
angular.module('home').controller('IlluminaDepotCtrl',['$scope', '$parse','$http','atmToSingleDatatable','dateServices', 'mainService',
                                                             function($scope,$parse, $http, atmToSingleDatatable, dateServices, mainService) {
	
	 // NGL-1055: name explicite pour fichier CSV exporté: typeCode experience
	 // NGL-1055: mettre getArray et codes '' dans filter et pas dans render
	 var datatableConfig = {
			name: $scope.experiment.typeCode.toUpperCase(),
			columns:[
			         {
			        	 "header":Messages("containers.table.supportCode"),
			        	 "property":"inputContainer.support.code",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":1,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			         {
			        	 "header":Messages("containers.table.categoryCode"),
			        	 "property":"inputContainer.support.categoryCode",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":2,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
					 {
			        	 "header":Messages("containers.table.code"),
			        	 "property":"inputContainer.code",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":3,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
					 {
			        	 "header":Messages("containers.table.projectCodes"),
			        	 "property":"inputContainer.projectCodes",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":4,
			        	 "render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			         // NGL-2378
					 {
			        	 "header":Messages("containers.table.tagCategory"),
			        	 "property":"inputContainer.contents| getArray:'properties.tagCategory.value'| unique",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":4.5,
			        	 "render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
					 {
			        	 "header":Messages("containers.table.sampleCodes"),
			        	 "property":"inputContainer.sampleCodes",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			        	 "position":5,
			        	 "render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
					 {
			        	 "header":Messages("containers.table.stateCode"),
			        	 "property":"inputContainer.state.code",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
						 "filter":"codes:'state'",
			        	 "position":6,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			         {
			        	 "header":Messages("containers.table.fromTransformationTypeCodes"),
			        	 "property":"inputContainer.fromTransformationTypeCodes",
			        	 "filter":"unique | codes:'type'",
			        	 "order":true,
						 "edit":false,
						 "hide":true,
			        	 "type":"text",
			 			 "render":"<div list-resize='cellValue' list-resize-min-size='3'>",
			        	 "position":7,
			        	 "extraHeaders":{0:Messages("experiments.inputs")}
			         },
			        ],
			compact:true,
			pagination:{
				active:false
			},		
			search:{
				active:false
			},
			order:{
				mode:'local',
				active:true,
				by:'inputContainer.code'
			},
			remove:{
				active:false,
			},
			save:{
				active:true,
	        	changeClass:false,
	        	showButton:false,
	        	withoutEdit: true,
				mode:'local',
			},
			hide:{
				active:true
			},
			edit:{
				active: false
			},
			messages:{
				active:false,
				columnMode:true
			},
			exportCSV:{
				active:true,
				showButton:true,
				delimiter:";",
				start:false
			},
			extraHeaders:{
				number:2,
				dynamic:true,
			}
	};

	//appellée par ng-init dans le scala.html
	$scope.setIsCNG = function(isCNG) {
		console.log('isCNG ???'+ isCNG);
		if (isCNG) {$scope.isCNG=true; }else{$scope.isCNG=false;}
	}
	
	// NGL-3000 ajouter une vérification de la date saisie
	// 21/10/2020 isValidDateFormat ne controle pas une date entrée sur 2 digits
	$scope.$on('save', function(e, callbackFunction) {
		console.log("call event save");
			// controler la date de run (remise du controle le 29/10/2021)
			if ( ! dateService.isValidDateFormat($scope.experiment.experimentProperties.runStartDate.value, Messages("date.format"))){
				$scope.messages.setError(Messages("experiment.msg.badformat", "Date réelle de dépôt", Messages("date.format")));
				$scope.$emit('childSavedError', callbackFunction);
			} else {
				//tout OK sauvegarder !!!
				$scope.atmService.data.save();
				$scope.atmService.viewToExperimentOneToVoid($scope.experiment);
				$scope.$emit('childSaved', callbackFunction);
			}
			
			// NGL-3180 : pour (CNG) ajout warning si pas de kit "NovaSeq XP" dans les réactifs et que novaseqWorkflowXp=oui 
			// 04/11/2022 inverser message CNG et $scope.$emit   ??? NONN  sinon cas ou $scope.experiment.atomicTransfertMethods[0] pas definis...
			if ( $scope.isCNG){
				var novaseqWorkflowXp=undefined;
				// !! pour les run non NOVASEQ6000 la propriété n'existe pas !!
				// utiliser la syntaxe get/parse pour simplifier l'écriture ??'
				if ( $scope.experiment.atomicTransfertMethods[0].inputContainerUseds[0].contents[0].properties.novaseqWorkflowXp) {
				     novaseqWorkflowXp=$scope.experiment.atomicTransfertMethods[0].inputContainerUseds[0].contents[0].properties.novaseqWorkflowXp.value;
				}
				console.log ('novaseqWorkflowXp='+ novaseqWorkflowXp);
				if (novaseqWorkflowXp === 'oui'){
					// TEST ajout parametre initAlertes
					$scope.checkReagentNovaSeqXPKit(true);
				}
			}
	});
	
	$scope.$on('refresh', function(e) {
		console.log("call event refresh on one-to-void (default)");
		var dtConfig = $scope.atmService.data.getConfig();
		$scope.atmService.data.setConfig(dtConfig);
		
		$scope.atmService.refreshViewFromExperiment($scope.experiment);
		$scope.$emit('viewRefeshed');
	});
		
	$scope.$on('cancel', function(e) {
		console.log("call event cancel on one-to-void (default)");
		$scope.atmService.data.cancel();
	});
		
	$scope.$on('activeEditMode', function(e) {
		console.log("call event activeEditMode on one-to-void (default)"); // Ne se declenche pas si experience Finished !!!!!
		$scope.atmService.data.selectAll(true);
		$scope.atmService.data.setEdit();
	});
	
	// init !!!
	var atmService = atmToSingleDatatable($scope, datatableConfig, true);
	//defined new atomictransfertMethod
	atmService.newAtomicTransfertMethod = function(line, column){
		return {
			class:"OneToVoid",
			line:line, 
			column:column, 				
			inputContainerUseds:new Array(0)
		};
	};
	
	atmService.experimentToView($scope.experiment, $scope.experimentType);
	
	$scope.atmService = atmService;
	
	// ajouté pour NGL-3000
	var dateService=dateServices($scope);

	// NGL-2456: pas de feuille de route pour sequenceur NEXTSEQ500
	$scope.setAdditionnalButtons([{
		isDisabled : function(){return $scope.isCreationMode();},
		isShow:function(){return ($scope.experiment.instrument.typeCode != 'NEXTSEQ500')},
		click:$scope.fileUtils.generateSampleSheet,
		label:Messages("experiments.sampleSheet")
	}]);
	
	// en mode creation positionner la date courante par defaut
	if($scope.isCreationMode()){
		if(!$parse("experimentProperties.runStartDate.value")($scope.experiment)){
			console.log('setting current date as default');
			// dans le javascript positionner une valeur en millisecondes en utilisant la librairie moment.js
			// le rendu au format date est executé par la directive date-timestamp (dateTimestamp) ou date-timestamp2 (dateTimestamp2) 
			/* !!!!!!!!!    SUPSQCNG-902 le timestamp doit etre arrondi pour que NGSRG retrouve le fichier du run
			   garder le code ci-dessous qui effectue l'arrondi !!!!!!! */
			var format = Messages("date.format").toUpperCase();
			var curdateMS_round = moment().format(format);
			curdateMS_round = moment(curdateMS_round, format).valueOf();
			console.log('timestamp arrondi='+curdateMS_round);
			//var curdateMS_exact= moment().valueOf();
			//console.log('timestamp exact  ='+curdateMS_exact);

			$parse("experimentProperties.runStartDate.value").assign($scope.experiment, curdateMS_round);
		}
	}
	
	//22/01/2018 NGL-1768 22/01/2018: importer le fichier XML du NovaSeq 6000
	var importNOVASEQ6000XMLfile = function(){
		console.log('Import NOVASEQ6000 RunParameters XML file');
		
		$scope.messages.clear();
		
		$http.post(jsRoutes.controllers.instruments.io.IO.importFile($scope.experiment.code).url, $scope.file)
		.success(function(data, status, headers, config) {
			$scope.messages.setSuccess(Messages('experiments.msg.import.success'));
			
			// data est l'experience retournée par input.java
			// PAS BESOIN ICI ....$scope.experiment.instrumentProperties= data.instrumentProperties;
			
			// et reagents ....
			$scope.experiment.reagents=data.reagents;
			
			// reinit select File...
			$scope.file = undefined;
			angular.element('#NOVASEQ6000XMLfile')[0].value = null;
			
			//refresh  reagents !!!
			$scope.$emit('askRefreshReagents');	
			})
		.error(function(data, status, headers, config) {
			$scope.messages.clazz = "alert alert-danger";
			$scope.messages.text = Messages('experiments.msg.import.error');
			$scope.messages.setDetails(data);
			$scope.messages.showDetails = true;
			$scope.messages.open();	
		
			// reinit select File..
			$scope.file = undefined;
			// il faut aussi réinitaliser le bouton d'import
			angular.element('#NOVASEQ6000XMLfile')[0].value = null;
		});		
	};
			
	$scope.buttonNOVASEQ6000XMLfile = {
			isShow:function(){
				//return (( $scope.isInProgressState() || $scope.isFinishState() ) &&  $scope.isEditMode() ); // MARCHE pas, editMode pas vu voir plus haut
				return ( $scope.isInProgressState() || $scope.isFinishState() );
				},
			isFileSet:function(){
				return ($scope.file === undefined)?"disabled":"";
			},
			click:importNOVASEQ6000XMLfile	
	};
	
	//---NGL-3180 -- Spécifique CNG, mais comme checkReagentNovaSeqXPKit est appelé dans le $on(save) ca doit etre dans ce controleur
	/// if ( $scope.isCNG){     si on ajoute ce if ==>  MARCHE PLUS !!!
		
	$scope.kitCatalogs={};
	
	function getReagentKitCode(kitName){
		return $scope.kitCatalogs[kitName].code;
	}

	// appellée soit dans on.save (donc en final dans controleur parent [ici]) et viens appres un message "sauvegarde réussie" ou ""erreur sauvegarde""
	//          soit dans les check du controleur fils...
	// difficilement paramètrable...pour être réutilisée....
	//   => ajout parametre initAlertes pour NGL-3599
	$scope.checkReagentNovaSeqXPKit = function (initAlertes){
		console.log("checkReagentNovaSeqXPKit...initAlertes="+initAlertes);

	  // 04/01/2022  NGL-3599 PB d'écrasement des messages du controleur fils!!!=> n'initialiser/effacer que s'il n'y a encore aucun message...???
      if ( initAlertes ) {
		$scope.data = { 'Alertes':[] };
		      ////=====> écrase le message d'erreur de sauvegarde incorrectes a l'état New'!!!'

		$scope.messages.clear(); // 23/12/2021 marche plus si $scope.data déclarée en dehors de la fonction !!!
		$scope.messages.clazz = "alert alert-warning";// !! écrasé par la sauvegarde finale!!!
		$scope.messages.text = "Alertes de configuration";// !! ecrasé par la sauvegarde finale!!!
	  }

		if ( ! $scope.experiment.reagents ) { 
			setAlert("Alertes", "Kit NovaSeq XP: Aucun réactif.");
		} else {
			// !! reagent peut etre un element vide..
			var found=0;
			$scope.experiment.reagents.forEach(function(reag){
				if (reag.kitCatalogCode == getReagentKitCode('NovaSeq XP')){
					found++;
					if ( ! reag.boxCode ){
						setAlert("Alertes","Kit NovaSeq XP: Aucun barcode de boîte.");
					} else { 
						// BUG: match(/_/)pas assez contraignant !!!!!!
						// les barcodes sont normalement saisis par douchette ( configurée pour ajouter "_" automatiquement en caractere final)
						//  1 barcode=> WWWW_ ; 2 barcodes=> WWWW_XXXX_
						if ( ! reag.boxCode.match(/_.+_$/) ){
							setAlert("Alertes", "Kit NovaSeq XP: Il faut 2 barcodes de boîtes.(format: BARCODE1_BARCODE2_)");
						}
					}
				}
			});
			if ( found==0){
				setAlert("Alertes", "Kit NovaSeq XP: Aucun kit.");
			} else if ( found > 1){
				setAlert("Alertes", "Kit NovaSeq XP: Plus d'un kit.");
			}
		}
		
		if ( $scope.data["Alertes"].length > 0 ){
			$scope.messages.showDetails = true;
			$scope.messages.open();
		} 
	}
	
	function setAlert(msgKey, msgDetails){
		// 04/01/2022 ne pas injecter plusieurs fois le meme message !!!
		if ( ! $scope.data[msgKey].includes(msgDetails) ){
			//console.log('SETALERTE '+msgDetails);
			$scope.data[msgKey].push(msgDetails);
			$scope.messages.setDetails($scope.data);
		}
	}
	
	//}	
}]);