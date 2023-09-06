angular.module('home').controller('OneToVoidLabChipMigrationProfileCNGCtrl',['$scope', '$parse','$http',
                                                             function($scope,$parse,$http) {

	// NGL-1055: surcharger la variable "name" definie dans le controleur parent ( one-to-void-qc-ctrl.js) => nom de fichier CSV exporté 
	var config = $scope.atmService.data.getConfig();
	config.name = $scope.experiment.typeCode.toUpperCase();
	$scope.atmService.data.setConfig(config);

	// FDS 30/08/2016 concentration et size de l'expérience doivent etres copiées dans le container
	// NGL-3247 ajout du calcul de quantité meme si elle n'est pas affichée
	$scope.$parent.copyPropertiesToInputContainer = function(experiment){
		
		experiment.atomicTransfertMethods.forEach(function(atm){
			var inputContainerUsed =$parse("inputContainerUseds[0]")(atm);
			if(inputContainerUsed){
				//-1-
				//concentration1 est un objet avec value et unit !!!
				var concentration1 = $parse("experimentProperties.concentration1")(inputContainerUsed);
				// 07/03/2018: NGL-1859 la copie de la concentration ne doit etre faite que si l'utilisateur le demande explicitement !!!
				if (concentration1  &&  $scope.experiment.experimentProperties.copyConcentration.value){
					
					// ATTENTION ne pas lier assignation de concentration et calcul quantité !!!
					$parse("newConcentration").assign(inputContainerUsed, concentration1);
					
					// pour NGL-3247 utiliser la structure compute/if(compute.isReady()
					var computeQty = {
						inputVol :    $parse("volume.value")(inputContainerUsed), //NGL-3247 pour le calcul de la qté il faut avoir un volume!!
						inputVolUnit :$parse("volume.unit")(inputContainerUsed),  //NGL-3247 pour le calcul de la qté il faut avoir un volume!!
						isReady:function(){
							return (
									this.inputVol  && (this.inputVol != undefined) && 
									this.inputVolUnit  && (this.inputVolUnit === "µL" || this.inputVolUnit === "µl")); // si autre unité pas géré ??
						}
					};
					
					if(computeQty.isReady()){
						// NGL-3247 calculer la quantité: qté=concentration * volume
						var calcQuantity= $parse("value")(concentration1) * $parse("inputVol")(computeQty);
						calcQuantity=Math.round(calcQuantity*100)/100;
						$parse("newQuantity.value").assign(inputContainerUsed, calcQuantity);
						
						// pour l'unité  il faut connaire l'unité de conc choisie par l'utilisateur:
						// =>  ng/µl ou nM par (seules ces 2 valeurs sont proposées)
						var concUnit=$parse("unit")(concentration1);
						var qtyUnit=null;
						if ( concUnit === "ng/µl"){ qtyUnit= "ng";} else if ( concUnit === "nM"){ qtyUnit= "fmol";}
						$parse("newQuantity.unit").assign(inputContainerUsed, qtyUnit);
						
						console.log("calcQuantity="+calcQuantity + " "+ qtyUnit);
					} else {
						console.log("valeur manquante pour calculer calcQuantity OU unité volume non gérée");
					}
				} else {
					// nécessaire à cause de la possibilité de décocher le bouton 'copyToContainer' uniquement avant 'terminer'
					console.log( "set newConcentration et newQuantity =null")
					//Attention si volume1 renseigné alors calcul newQuantity
					inputContainerUsed.newConcentration = null;
					inputContainerUsed.newQuantity = null;
				}
				//-2- copie automatique pour la size 
				var size1 = $parse("experimentProperties.size1")(inputContainerUsed);
				if(size1){
					//inputContainerUsed.newSize = size1;
					$parse("newSize").assign(inputContainerUsed, size1);// avec notation $parse
				}
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
			
			//NGL-4008  les profils sont effacés de l'onglet "tableau" quand on importe les données !
			// mais s'il y en a eu de chargé dans l'onglet "profils", ils y sont encore !
			// on peut les récupérer dans internalProfils
			console.log('RECUPERER LES PROFILS...s"il y en a');		
			_recupProfilesMap=internalProfiles;
			angular.forEach($scope.experiment.atomicTransfertMethods, function(atm){
				var pos = atm.inputContainerUseds[0].locationOnContainerSupport.line+atm.inputContainerUseds[0].locationOnContainerSupport.column;
				$parse('inputContainerUseds[0].experimentProperties.migrationProfile').assign(atm, this[pos]);
			}, _recupProfilesMap);
					
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
	
	// 04/01/2018 bug il y a 2 boutons, il faut donc 2 variables differentes !!!
	$scope.importButton = {
		isShow:function(){
			// ajout parenthese pour grouper isInProgressState() et check("admin")
			return ( !$scope.mainService.isEditMode() && ( $scope.isInProgressState() || Permissions.check("admin")) )
			},
		isFileSet:function(){
			return ($scope.file === undefined)?"disabled":"";
		},
		click:importData,
	};
	
	//    bouton profil, etait manquant...voir aussi le watch ("profils") car il il n'y a pas d'action click ici !!
	$scope.profilsButton = {
		isShow:function(){
			// ajout parenthese pour grouper isInProgressState() et check("admin")
			return ( !$scope.mainService.isEditMode() && ( $scope.isInProgressState()|| Permissions.check("admin")) )
			}
	};
	
	// FDS NGL-1055: mettre le getArray|unique dans filter et pas dans render
	var columns = $scope.atmService.data.getColumnsConfig();
	columns.push({
    	"header": Messages("containers.table.codeAliquot"),
		"property": "inputContainer.contents",
		"filter": "getArray:'properties.sampleAliquoteCode.value'| unique",
		"order":false,
		"hide":true,
		"type":"text",
		"position":7.5,
		"render": "<div list-resize='cellValue' list-resize-min-size='3'>",
		"extraHeaders": {0 : Messages("experiments.inputs")}
	});
	columns.push({
		"header" : Messages("containers.table.libProcessTypeCode"),
		"property" : "inputContainer.contents",
		"filter": "getArray: 'properties.libProcessTypeCode.value'| unique",
		"order" : false,
		"hide" : true,
		"type" : "text",
		"position" : 9,
		"render" : "<div list-resize='cellValue' list-resize-min-size='3'>",
		"extraHeaders" : {0 : Messages("experiments.inputs")}
	});
	columns.push({
		"header" : Messages("containers.table.tags"),
		"property" : "inputContainer.contents",
		"filter": "getArray:'properties.tag.value'| unique",
		"order":true,
		"hide" : true,
		"type" : "text",
		"position" : 10,
		"render" : "<div list-resize='cellValue' list-resize-min-size='3'>",
		"extraHeaders" : {0 : Messages("experiments.inputs")}
	});
	
	// FDS 08/09/2016 ajouter une colonne "concentration.unit" car la colonne "concentration" issue des 
	// properties de l'experience est maintenant définie sans unité car cette derniere est variable...
    columns.push({
		"header" :  Messages("containers.table.concentration.unit"),
		"property" : "inputContainerUsed.experimentProperties.concentration1.unit",
		"order" : true,
		"edit" : true,
		"hide" : true,
		"type" : "text",
		"position" : 11.1,
		"choiceInList":true,
		"listStyle":"select",
		"possibleValues":[{"name":"nM","code":"nM"},{"name":"ng/µl","code":"ng/µl"} ],
		"defaultValues":"ng/µl", // essai...
		"extraHeaders" : {0 : Messages("experiments.inputs")}
	});
	
	$scope.atmService.data.setColumnsConfig(columns);
	
	var profilesMap = {};
	
	// pour une expérience déjà sauvegardée: récupérer les fichiers dans profilesMap
	angular.forEach($scope.experiment.atomicTransfertMethods, function(atm){
		var pos = atm.inputContainerUseds[0].locationOnContainerSupport.line+atm.inputContainerUseds[0].locationOnContainerSupport.column;
		var img = $parse('inputContainerUseds[0].experimentProperties.migrationProfile')(atm);
		this[pos] = img;
	}, profilesMap)
	
	var internalProfiles = profilesMap;
	
	// methode appellé dans labchipmigrationprofile.scala.html
	$scope.getProfile=function(line, column){
		return internalProfiles[line+column];
	};
	
	// watch sur bouton img 
	$scope.$watch("selectedProfiles",function(newValues){
		if(newValues){
			var nbAssignedFiles=0;
			var _profilesMap = {}; // stocker les fichiers chargés; on repart d'une map vide: on ne regarde pas s'il y avait deja qq chose avant !!!
			//  initialiser la map avec les position des atm ?
			//initMap(_profilesMap);  !! non pose probleme => creation autre map
			var expectedMap ={};
			initMap(expectedMap);
			
			// NGL-4008 ; afficher message intermédiaire=> compter les fichiers corrects sélectionnés par l'utilisateur
			// Si l'utilisateur a choisi le bon dossier de resultats du labGX, le nombre (et le nom) des fichiers présent doit correspondre
			// aux puits (=atm). MAIS il peut :
			//  -1 se tromper de dossier (fichiers ne correspondant pas aux puits de l'expérience)
			//  -2 mal sélectionner les fichiers et en oublier !!
			
			angular.forEach(newValues, function(img){
				// les fichiers à importer doivent commencer par une position <col><line> et avoir une extension d'image valide:
				// exemple : A1_xxxxx.png
				//var pos = img.fullname.split('_')[0]; // !! ATTENTION si le split ne fonctionne pas; pos contient le nom complet du fichier !!!
				// !! au CNS dans chip-migration-ctrl.js on trouve: var pos = img.fullname.match(/_([A-H]\d+)\./)[1];
				//    pourquoi une telle difference de nomenclature dans les noms de fichier profil ???
				var myRegex = new RegExp(/([A-H]\d+)_/);
				var result = img.fullname.match(myRegex);
				if ( result !== null ){  
					var pos=result[0].split('_')[0];
					// Ne charger le profil dans la map que s''il correspond a un fichier attendu !!
					if ( expectedMap.hasOwnProperty(pos) ){
						this[pos] = img;
						console.log( pos + ': img loaded !');
					}
				} /*else {
					//fichier incorrect: stocker pour message d'erreur !!! NON PAS DEMANDE'...
					incorrectFiles.push(img.fullname);
				}*/
			}, _profilesMap);
			
			// sauvegarder les fichiers pour l'onglet "Profils"
			internalProfiles = _profilesMap;
			
			// affecter les fichiers dans les experiment properties (onglet profils ??) !! ici on travaille sur displayResult !!
			angular.forEach($scope.atmService.data.displayResult, function(dr){
				var pos = dr.data.inputContainerUsed.locationOnContainerSupport.line+dr.data.inputContainerUsed.locationOnContainerSupport.column;
				$parse('inputContainerUsed.experimentProperties.migrationProfile').assign(dr.data, this[pos]);
			}, _profilesMap);
			
			// NGL-4008 ajout 
			// travailler directement sur atm !!! nécessaire pour le comptage intermédiaire ET final
			angular.forEach($scope.experiment.atomicTransfertMethods, function(atm){
				var pos = atm.inputContainerUseds[0].locationOnContainerSupport.line+atm.inputContainerUseds[0].locationOnContainerSupport.column;
				$parse('inputContainerUseds[0].experimentProperties.migrationProfile').assign(atm, this[pos]);
				// $parse ne permet pas de savoir combien d'assign ont marché!!!
				if ( atm.inputContainerUseds[0].experimentProperties.migrationProfile ){
					nbAssignedFiles++;
				}
			}, _profilesMap);
			
			// NGL-4008 ajout message intermédiaire
			$scope.messages.clear();
			$scope.alert = { "Erreurs fichiers":[] };	
			
			if ( nbAssignedFiles == 0){
				$scope.messages.clazz="alert alert-danger";
				$scope.messages.text="Aucun fichier profil correct sélectionné.";
				$scope.alert['Erreurs fichiers'].push("ne sont pas de la forme <A-H><1-12>_xxxxx.<jpg|png>");
				$scope.alert['Erreurs fichiers'].push("ne correspondent pas aux puits de l'expérience");
				$scope.messages.setDetails($scope.alert);
				$scope.messages.showDetails = false;
				$scope.messages.open();
			} else {
				$scope.messages.clazz="alert alert-success";
				$scope.messages.text='Chargement de '+ nbAssignedFiles + ' profil(s).';
				$scope.messages.showDetails = false;
				$scope.messages.open();
			}
			
			// mise à jour du message de profils manquants
			$scope.expHasMissingMigrationProfiles=checkMissingMigrationProfiles();
		}
		
		angular.element('#importProfils')[0].value = null;// ajouté 04/01/2018
	});
	
	// NGL-3801; remis en NGL-4008
	function countMigrationProfiles(){	
		var count=0;
		$scope.experiment.atomicTransfertMethods.forEach(function(atm){
			if ( atm.inputContainerUseds[0].experimentProperties &&
			     atm.inputContainerUseds[0].experimentProperties.migrationProfile){ count++; }
		});
		//console.log (count + " profiles in experiment");
		return count;
 	}

	$scope.missingMigrationProfiles=0;

	function checkMissingMigrationProfiles(){
		// !!! a l'etat "a sauvegarder" il n'y a pas d'atm => passer par basket
		var nbInputs;
		if ( $scope.isCreationMode()){ nbInputs=$scope.mainService.getBasket().length(); }
		else{                          nbInputs=$scope.experiment.atomicTransfertMethods.length;}
		//console.log ('nb inputs: '+ nbInputs);
		
		$scope.missingMigrationProfiles=nbInputs - countMigrationProfiles();
		//console.log('missing profiles: '+ $scope.missingMigrationProfiles );
		
		return ( $scope.missingMigrationProfiles > 0 ? true : false );
	}
	
	$scope.expHasMissingMigrationProfiles=checkMissingMigrationProfiles();
	
	// créer les clés correspondant aux atm existant 
	function initMap(map){
		angular.forEach($scope.experiment.atomicTransfertMethods, function(atm){
			var pos = atm.inputContainerUseds[0].locationOnContainerSupport.line+atm.inputContainerUseds[0].locationOnContainerSupport.column;
			this[pos]=null;
			//console.log ( pos + ' needFile !!');
		}, map);
	}

}]);