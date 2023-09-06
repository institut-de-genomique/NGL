// 08/11/2017 NGL-1326:  le bouton specifique CNG d'import des balances Mettler
// ajout mainService pour getBasket
angular.module('home').controller('IlluminaDepotCNGCtrl',['$scope', '$parse','$http', 'mainService',
                                                   function($scope, $parse, $http, mainService) {
	
	var importDataMettler = function(){
		console.log('Import Mettler file');
		
		/////// ATTENTION TESTER AUSSI avec $scope.$parent.messages!!!!
		$scope.messages.clear();
		$http.post(jsRoutes.controllers.instruments.io.IO.importFile($scope.experiment.code).url+"?extraInstrument=labxmettlertoledo", $scope.file)
		.success(function(data, status, headers, config) {
			$scope.messages.setSuccess(Messages('experiments.msg.import.success'));
			
			// data est l'experience retournée par input.java
			// aucune propriete d'instrument mise a jour par cet import !!!
			//$scope.experiment.instrumentProperties= data.instrumentProperties;
			
			// et reagents ....
			$scope.experiment.reagents=data.reagents;
			
			// reinit select File...
			$scope.file = undefined;
			angular.element('#importFileMettler')[0].value = null;
			
			//refresh  reagents !!!
			$scope.$emit('askRefreshReagents');
			
		})
		.error(function(data, status, headers, config) {
			///$scope.messages.setError(Messages('experiments.msg.import.error')); // Ne fonctionne que pour une seule erreur !!!!!!!
			
			$scope.messages.clazz = "alert alert-danger";
			$scope.messages.text = Messages('experiments.msg.import.error');
			$scope.messages.setDetails(data);
			$scope.messages.showDetails = true;
			$scope.messages.open();	
	
			// reinit select File..
			$scope.file = undefined;
			// il faut aussi réinitaliser le bouton d'import
			angular.element('#importFileMettler')[0].value = null;
		});		
	};
	
	$scope.$on('activeEditMode', function(e) {
		console.log("activeEditMode");
		//console.log("instrument:"+ $scope.experiment.instrument.typeCode );
	});
	
	// 25/10/2017 FDS ajout pour l'import du fichier Mettler; 08/11/2017 renommage button2=>buttonMettler
	// 31/01/2018 pas de fichier Mettler pour les novaseq 
	$scope.buttonMettler = {
			isShow:function(){
				//console.log('editMode:'+$scope.isEditMode() );// isEditMode n'est pas vu si experience etat Finished !
				return ( ( $scope.isInProgressState()||$scope.isFinishState()) &&  $scope.isEditMode() && ($scope.experiment.instrument.typeCode !="NOVASEQ6000") );
				},
			isFileSet:function(){
				return ($scope.file === undefined)?"disabled":"";
			},
			click:importDataMettler	
	};
	
	/*	23/12/2021 NGL-3599=> tous les checks doivent être fait ensembles
		Déporter le code des watchs dans des functions dédiées ce qui permettrait de refaire le checkAll au save 
		si on redefini $scope.$on('save') dans un controleur fils: s'ajoute t-il a celui du parent ou le remplace-t-il ??'
		==>garder quand meme checkAll !!
	});*/

	// NGL-2738 04/10/2019 Mettre un WARNING quand Nb cycles Read Index 2 = 0 mais qu'on trouve du DUAL-INDEX dans la Flowcell
	// ici c'est une valeur saisie par l'utilisateur=> newValue,oldValue sont necesaires !!
	$scope.$watch("experiment.instrumentProperties.nbCyclesReadIndex2.value" , function(newValue, oldValue){
		console.log("watch nbCyclesReadIndex2:"+ newValue);
		if (newValue && newValue != undefined && newValue != oldValue  ){ 
			// 23/12/2021 code déplacé dans checkNbCyclesReadIndex2() appelée dans checkAll()
			//checkNbCyclesReadIndex2()
			checkAll('watch nbCyclesReadIndex2.value');
		} 
	});
	
	// récupérer les tagCategory distincts de TOUS les contents des inputs de l'expérience et vérifier s'il y a la catégorie demandée
	// !! Avant d'avoir sauver une experience $scope.experiment.atomicTransfertMethod n'est pas encore chargé 
	//   => passer par Basket (ajouter mainService dans le controller)
	function hasTagCategory(tagCategory){
		console.log("hasIndexCategory "+ tagCategory);
		var tagCategories=[];
		
		// le basket est defini dans une experience a "A sauvegarder" mais apres le save il n'est pas supprimé mais simplement vidé => 2 tests a faire
		if ( mainService.getBasket() != undefined  && mainService.getBasket().get().length > 0) {
			tagCategories= $scope.$eval("getBasket().get()| flatArray:'contents'| getArray:'properties.tagCategory.value' | unique");
		} else {
			for(var j = 0 ; j < $scope.experiment.atomicTransfertMethods.length &&  $scope.experiment.atomicTransfertMethods != null; j++){
				var atm =  $scope.experiment.atomicTransfertMethods[j];
				for(var i=0;i < atm.inputContainerUseds.length;i++){
					var icu = atm.inputContainerUseds[i];
					for (var k=0; k < icu.contents.length; k++){ 
						// Attention aux lanes sans index !!! 
						if (icu.contents[k].properties.tagCategory) {  tagCategories.push(icu.contents[k].properties.tagCategory.value);}
					}
				}
			}
		}
	
		// indexOf =-1 si pas trouvé dans le tableau
		if (tagCategories.indexOf(tagCategory) >= 0) { 
			return true;
		} else {
			return false;
		}
	}
	
	// NGL-3180 les messages d'alertes en sauvegarde sur le kit NovaSeq XP peuvent etre écrasés/non vus par l'utilisateur dans certains cas
	// ==> rajouter une alerte préliminaire sur le type d'instrument qui sera toujours vue
	$scope.$watch("experiment.instrument.typeCode", function(){
		console.log("watch experiment.instrument.typeCode:"+ $scope.experiment.instrument.typeCode);
		//  23/12/2021 code déplacé dans checkNovaseqWorkflowXp() appelée dans checkAll()
		//checkNovaseqWorkflowXp();
		checkAll('watch instrument.typeCode');
	});
	
	// NGL-3599 vérifier si le type de lecture choisi dans l'expérience de dépot est cohérente avec celui déclaré dans
	// la prepa-FC (qui a été automatiquement copié dans le support de sortie et qui est ici entree)
	$scope.supportSequencingProgramType=undefined;
	
	// !!! a l'état 'A sauvegarder' on n'a pas inputContainerSupportCodes[0] 
	if ( mainService.getBasket() != undefined  && mainService.getBasket().get().length > 0) {
		containerSupportCode=$scope.$eval("getBasket().get()| flatArray:'support'| getArray:'code'| unique");
	} else {
		containerSupportCode=$scope.experiment.inputContainerSupportCodes[0];
	}
	
	// promise pour recupérer le sequencingProgramType.value du containerSupport '
	// fonctionne meme sans return !!
	function getSupportSequencingProgramType(containerSupportCode) {
		console.log('promise pour sequencingProgramType.value...')
		return $http.get(jsRoutes.controllers.containers.api.ContainerSupports.get(containerSupportCode).url)
			.then(function(response){
				$scope.supportSequencingProgramType=response.data.properties.sequencingProgramType.value;
				console.log( "...DONE=>"+ $scope.supportSequencingProgramType);
			})
			// enchainer directectement avec check 
			// ou meme avec checkAll ????==> NON car nécessite l'exécution de l'autre promise !!
			.then( function(){
				checkSequencingProgramType();
				//checkAll('init');
			});
	}
	
	//appeler la promise
	getSupportSequencingProgramType(containerSupportCode);
	
	$scope.$watch("experiment.instrumentProperties.sequencingProgramType.value", function(newValue, oldValue){
		console.log("watch sequencingProgramType: "+ newValue);
		if (newValue && newValue != oldValue ){ 
			// 23/12/2021 code déplacé dans checkSequencingProgramType() appelée dans checkAll()
			//checkSequencingProgramType()
			checkAll('watch sequencingProgramType.value');
		}
	});

	// pour appeller séquentiellement les différents check à faire, et afficher les alertes
	function checkAll (source){
		console.log('CHECKALL '+source);
		
		// initialisation: utiliser  $scope.$parent.data au lieu de $scope local  OK
		$scope.$parent.data = { 'Alertes':[] }; 
		
		//doivent etre executés AVANT les setAlerte !!!!!!!!
		$scope.$parent.messages.clear();
		$scope.$parent.messages.clazz = "alert alert-warning";  
		$scope.$parent.messages.text = "Alertes de configuration"; 
		
		checkNovaseqWorkflowXp(source); // mettre en premier car peut initaliser alert ??? OUI !!
		checkSequencingProgramType();
		checkNbCyclesReadIndex2();
		
		//utiliser  $scope.$parent au lieu de $scope local
		if ( $scope.$parent.data["Alertes"].length > 0 ){
			
			//////  si fait ici alors showDetails/open marchent plus !!!
			//$scope.$parent.messages.clear();
			//$scope.$parent.messages.clazz = "alert alert-warning";  
			//$scope.$parent.messages.text = "Alertes de configuration"; 
			
			$scope.$parent.messages.showDetails = true;
			$scope.$parent.messages.open();
		}
	}

	function checkSequencingProgramType(){
		console.log(">> checkSequencingProgramType");
		if ($scope.experiment.instrumentProperties && $scope.supportSequencingProgramType != undefined){
			if ( $scope.experiment.instrumentProperties.sequencingProgramType.value != $scope.supportSequencingProgramType ){
				setAlert("Alertes","Le type de lecture choisi ne correspond pas à celui déclaré lors de l'expérience 'Préparation flowcell' ou 'Prép. flowcell ordonnée'");
			}
		}
	}
	
	function checkNbCyclesReadIndex2(){
		console.log(">> checkNbCyclesReadIndex2");
		if ($scope.experiment.instrumentProperties && $scope.experiment.instrumentProperties.nbCyclesReadIndex2 ){
			// NGL-2893 le controle sur le HISQEX ne s'applique qu'a ce cas
			if ($scope.experiment.instrumentProperties.nbCyclesReadIndex2.value == 0 && hasTagCategory("DUAL-INDEX") && $scope.experiment.instrument.typeCode != "HISEQX"){
				setAlert("Alertes","Nb cycles Read Index2 = 0 alors que la flowcell contient au moins une piste avec du DUAL-INDEX");
			}
			
			// WARNING inverse...Nb cycles Read Index 2 <> 0 mais qu'il n'y a pas de DUAL-INDEX dans la flowcell
			if ($scope.experiment.instrumentProperties.nbCyclesReadIndex2.value != 0 && !hasTagCategory("DUAL-INDEX")){
				setAlert("Alertes","Nb cycles Read Index2 > 0 alors que la flowcell ne contient pas de DUAL-INDEX");
			}
		}
	}
	
	function checkNovaseqWorkflowXp(source){
		console.log(">> checkNovaseqWorkflowXp "+source);
		novaseqWorkflowXp=undefined;
		if ( $scope.experiment.instrument.typeCode == 'NOVASEQ6000') {
			// a l'état 'A sauvegarder' on n'a pas encore les inputContainerUseds !!!=> passer par getBasket
			if ( mainService.getBasket() != undefined  && mainService.getBasket().get().length > 0) {
				novaseqWorkflowXp=$scope.$eval("getBasket().get()| flatArray:'contents'| getArray:'properties.novaseqWorkflowXp.value' | unique");
			} else {
				if ( $scope.experiment.atomicTransfertMethods[0].inputContainerUseds[0].contents[0].properties.novaseqWorkflowXp) {
					novaseqWorkflowXp=$scope.experiment.atomicTransfertMethods[0].inputContainerUseds[0].contents[0].properties.novaseqWorkflowXp.value;
				}
			}
			
			if ( novaseqWorkflowXp == 'oui' ){
				// si la propriété workflowXp est oui, vérifier si le reagent est là
				// Attention  a l'inialisations des alertes !!( ecrasement parent/enfant)
				if (source === 'init'){initAlertes=true} else {initAlertes=false}
				$scope.checkReagentNovaSeqXPKit(initAlertes);
			}
		}
	}
	
	// !!! utiliser $scope.$parent
	function setAlert(msgKey, msgDetails){
		console.log("setalert..."+msgDetails);
		$scope.$parent.data[msgKey].push(msgDetails);
		$scope.$parent.messages.setDetails($scope.$parent.data);
	}
	
	// promise pour récupérer un kit =>  utiliser .then et pas .success !!
	// sans le return il y a qq disfonctionnements....
	function fetchReagentKit(kitName) {
		console.log("promise pour "+kitName+"...");
		return $http.get(jsRoutes.controllers.reagents.api.KitCatalogs.list().url,{params:{'name':kitName}})
			.then( function(response) {
				$scope.kitCatalogs[kitName] = response.data[0]; //data est la partie de la réponse http qui contient les données
				// c'est un objet, peut pas etre affiché en console!
				console.log( "...DONE=> [object]"); 
			})
			/// enchainer le check !!! ou meme le checkAll ???? OUI
			.then( function() {
				//checkNovaseqWorkflowXp('init');
				checkAll('init');
			});
	};

	// appeller la promise
	fetchReagentKit('NovaSeq XP');
	
	
}]);