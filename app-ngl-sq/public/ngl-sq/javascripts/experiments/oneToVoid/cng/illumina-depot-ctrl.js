// 08/11/2017 NGL-1326:  le bouton specifique CNG d'import des balances Mettler
// ajout mainService pour getBasket
angular.module('home').controller('IlluminaDepotCNGCtrl',['$scope', '$parse','$http', 'mainService',
                                                   function($scope, $parse, $http, mainService) {
	
	var importDataMettler = function(){
		console.log('Import Mettler file');
		
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

	//NGL-2738  04/10/2019 Mettre un WARNING quand Nb cycles Read Index 2 = 0 mais qu'on trouve du DUAL-INDEX dans la Flowcell
	$scope.$watch("experiment.instrumentProperties.nbCyclesReadIndex2.value" , function(newValue, oldValue){
		console.log("watch nbCyclesReadIndex2");
		//if (newValue && newValue !== oldValue ){ /// probleme apres un save ???? oldvalue n'est plus une string.... il faut utiliser != au lieu de !==
		if (newValue && newValue != oldValue ){ 
			$scope.messages.clear();
			
			if (newValue == 0 && hasTagCategory("DUAL-INDEX")){
				$scope.messages.clazz = "alert alert-warning";
				$scope.messages.text = "Attention: Nb cycles Read Index2 = 0 alors que la flowcell contient au moins une piste avec du DUAL-INDEX";
				$scope.messages.open();
			}
			
			// et du coup WARNING inverse...Nb cycles Read Index 2 <>0 mais qu'il n'y a pas de DUAL-INDEX dans la flowcell
			if (newValue != 0 && !hasTagCategory("DUAL-INDEX")){
				$scope.messages.clazz = "alert alert-warning";
				$scope.messages.text = "Attention: Nb cycles Read Index2 > 0 alors que la flowcell ne contient pas de DUAL-INDEX";
				$scope.messages.open();
			}
		}
	})
	
	// recuperer les tagCategory distincts de TOUS les contents des inputs de l'experience et verifier s'il y a la categorie demandee
	// !! Avant d'avoir sauver une experience $scope.experiment.atomicTransfertMethod n'est pas encore chargé 
	//   => passer par Basket (ajouter mainService dans le controller ??)

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
	
}]);