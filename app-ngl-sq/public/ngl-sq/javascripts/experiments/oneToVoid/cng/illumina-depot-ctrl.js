// 08/11/2017 NGL-1326:  le bouton specifique CNG d'import des balances Mettler
// essai avec ou sans mainService
angular.module('home').controller('IlluminaDepotCNGCtrl',['$scope', '$parse','$http',
                                                             function($scope,$parse, $http) {
	
	
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
	
	//  recu  a l'etat InProgress
	$scope.$on('activeEditMode', function(e) {
		console.log("XXXXXXXX  activeEditMode");
		console.log("instrument:"+ $scope.experiment.instrument.typeCode );
	});
	
	// 25/10/2017 FDS ajout pour l'import du fichier Mettler; 08/11/2017 renommage button2=>buttonMettler
	// 31/01//2018  pas de fichier Mettler pour les novaseq 
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
		
}]);