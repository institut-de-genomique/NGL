// copie tapestation-migration-profil-ctrl
// $http pour l'import fichier CSV
angular.module('home').controller('OneToVoidFemtoPulseMigrationProfileCNGCtrl',['$scope', '$parse','$http',
                                                             function($scope,$parse,$http) {
			
	var inputExtraHeaders=Messages("experiments.inputs");

	// surcharger la variable "name" definie dans le controleur parent ( one-to-void-qc-ctrl.js) => nom de fichier CSV exporté 
	var config = $scope.atmService.data.getConfig();
	config.name = $scope.experiment.typeCode.toUpperCase();

	// bouton pour definir automatiquement les positions femtoPulse (!! il ne s'aggit pas des boutons standard "baguettes magiques")
	// probleme avec ng-disabled="!isEditMode()" : marche pas... bouton actif en permanence!!!
	// EN COURS.... computeFemtopulseColumMode  ou   computeFemtopulseLineMode ????
	config.otherButtons={
		active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
		//active: $scope.mainService.isEditMode(),
		complex:true,
		template:  ''
		+'<div class="btn-group" style="margin-left:5px">'
		+'<button class="btn btn-default" ng-disabled="!(isEditModeAvailable() && isWorkflowModeAvailable(\'F\'))" ng-click="plateFemtopulseUtils.computeLineMode(atmService, atmService.data)" data-toggle="tooltip"  title="Ligne auto"><i class="fa fa-magic"></i><i class="fa fa-arrow-right"></i></button>'                	                	
		+'</div>'
	};

	$scope.atmService.data.setConfig(config);

	// modifs par rapport a labchip-migration-profil-ctrl=> mettre la config colum en premier !!
	var columns = $scope.atmService.data.getColumnsConfig();

	columns.push({ //sample Aliquot ajout 03/06/2025
		"header":Messages("containers.table.codeAliquot"),
		"property": "inputContainer.contents", 
		"filter": "getArray:'properties.sampleAliquoteCode.value'",
		"order":true,
		"hide":true,
		"type":"text",
		"position":8,
		"render":"<div list-resize='cellValue' list-resize-min-size='3'>",
		"extraHeaders":{0: inputExtraHeaders}
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
		"extraHeaders":{0: inputExtraHeaders}
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
		"extraHeaders":{0: inputExtraHeaders}
	});

	columns.push({
		"header" : Messages("containers.table.femtopulseLine"),
		"property" : "inputContainerUsed.instrumentProperties.femtopulseLine.value",
		"order" : true,
		"edit" : true,
		"editDirectives":" udt-change='updatePropertyFromUDT(value,col)' ",
		"choiceInList":true,
		"possibleValues":[{"name":'A',"code":"A"},{"name":'B',"code":"B"},{"name":'C',"code":"C"},{"name":'D',"code":"D"},
			{"name":'E',"code":"E"},{"name":'F',"code":"F"},{"name":'G',"code":"G"},{"name":'H',"code":"H"}],
			"hide" : true,
			"type" : "text",
			"position" : 11,
			"extraHeaders" : {
				0 : Messages("experiments.inputs")
			}
	});

	columns.push({
		"header" : Messages("containers.table.femtopulseColumn"),
		"property" : "inputContainerUsed.instrumentProperties.femtopulseColumn.value",
		"order" : true,
		"edit" : true,
		"editDirectives":" udt-change='updatePropertyFromUDT(value,col)' ",
		"choiceInList":true,
		"possibleValues":[{"name":'1',"code":"1"},{"name":'2',"code":"2"},{"name":'3',"code":"3"},{"name":'4',"code":"4"},
			{"name":'5',"code":"5"},{"name":'6',"code":"6"},{"name":'7',"code":"7"},{"name":'8',"code":"8"},
			{"name":'9',"code":"9"},{"name":'10',"code":"10"},{"name":'11',"code":"11"},{"name":'12',"code":"12"}], 
			"hide" : true,
			"type" : "number",
			"position" : 12,
			"extraHeaders" : {
				0 : Messages("experiments.inputs")
			}
	});

	$scope.atmService.data.setColumnsConfig(columns);

	// ????? EN COURS
	// se déclenche dans on Save du controler parent
	// size de l'expérience doivent etres copiées dans le container IN
	$scope.$parent.copyPropertiesToInputContainer = function(experiment){
		experiment.atomicTransfertMethods.forEach(function(atm){
			var inputContainerUsed =$parse("inputContainerUseds[0]")(atm);
			if (inputContainerUsed){
				// copie de la size 
				var measuredSize = $parse("experimentProperties.measuredSize")(inputContainerUsed);
				if(measuredSize){
					console.log('Assign '+measuredSize);
					$parse("newSize").assign(inputContainerUsed, measuredSize);
				}	
			}	
		});	
	};

	// ajouter un 'save' au scope local en plus du 'save' du controleur parent 
	$scope.$on('save', function(e) {	
		//console.log("call event on save FEMTO PULSE");
		// lancer le comptage pour le cas ou il y a eu des suppressions de profils avec l'icone "poubelle"
		// faut il garder l'icone poubelle ????? ( existe que si propriété migrationProfile editable...)
		checkMissingMigrationProfiles();
	});

	//  import par fichier CSV
	var importData = function(){
		$scope.messages.clear();
		
		$http.post(jsRoutes.controllers.instruments.io.IO.importFile($scope.experiment.code).url, $scope.file)
		.success(function(data, status, headers, config) {
			$scope.messages.clazz="alert alert-success";
			$scope.messages.text=Messages('experiments.msg.import.success');
			$scope.messages.showDetails = true;
			$scope.messages.open();	
			//only atm because we cannot override directly experiment on scope.parent
			$scope.experiment.atomicTransfertMethods = data.atomicTransfertMethods;
			$scope.file = undefined;
			angular.element('#importFile')[0].value = null;
			
			//NGL-4008  les profils sont effacés de l'onglet "tableau" quand on importe les données !
			// mais s'il y en a eu de chargé dans l'onglet "profils", ils y sont encore !
			// on peut les récupérer dans internalProfils
			//console.log('RECUPERER LES PROFILS...s"il y en a');		
			angular.forEach($scope.experiment.atomicTransfertMethods, function(atm){
				//var pos = atm.inputContainerUseds[0].locationOnContainerSupport.line+atm.inputContainerUseds[0].locationOnContainerSupport.column;
				// 24/06/2025 utiliser femtopulseLine et femtopulseColumn
				var pos = atm.inputContainerUseds[0].instrumentProperties.femtopulseLine.value + atm.inputContainerUseds[0].instrumentProperties.femtopulseColumn.value;
				$parse('inputContainerUseds[0].experimentProperties.migrationProfile').assign(atm, this[pos]);
			}, internalProfiles);
					
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
	
	$scope.importButton = {
		isShow:function(){
			return ( !$scope.mainService.isEditMode() && ( $scope.isInProgressState() || Permissions.check("admin")) )
			},
		isFileSet:function(){
			return ($scope.file === undefined)?"disabled":"";
		},
		click:importData,
	};

	$scope.profilsButton = {
		isShow:function(){
			return ( !$scope.mainService.isEditMode() && ( $scope.isInProgressState()|| Permissions.check("admin")) )
			}
	};
	
	
	// pour une expérience déjà sauvegardée: récupérer les fichiers et les stocker dans profilesMap
	var profilesMap = {}; 
	
	// pour une expérience déjà sauvegardée: récupérer les fichiers dans profilesMap
	angular.forEach($scope.experiment.atomicTransfertMethods, function(atm){
		var pos = atm.inputContainerUseds[0].locationOnContainerSupport.line + atm.inputContainerUseds[0].locationOnContainerSupport.column;
		console.log ( atm.inputContainerUseds[0].code + ' POS='+pos)
		var img = $parse('inputContainerUseds[0].experimentProperties.migrationProfile')(atm);
		    //DEBUG  toutes les positions n'ont pas une image !!
		    //if ( img !== undefined ) console.log( '=> img exist')
		this[pos] = img;
	}, profilesMap);
	
	// copie pour l'onglet Profils
	var internalProfiles = profilesMap;
	
	// methode appellé dans le scala.html pour afficher 1 profil dans l'onglet "profils"
	$scope.getProfile=function(line, column){
		return internalProfiles[line+column];
	};
	
	// watch sur bouton "select fichier" de l'onglet "profils" ( plaques 96 uniquement)
	$scope.$watch("selectedProfiles",function(newValues){
		if(newValues){
			var nbAssignedFiles=0;
			var _profilesMap = {}; // pour stocker les fichiers chargés; on repart d'une map vide: on ne regarde pas s'il y avait deja qq chose avant !!!
			                       // donc ecrase des fichier chargés "un par un" dans l'onglet tableau
			//  initialiser la map avec les positions des atm ?
			//initMap(_profilesMap);  !! non pose probleme => creation autre map

			var expectedMap ={};
			initMap(expectedMap);
			
			// afficher message intermédiaire=> compter les fichiers corrects sélectionnés par l'utilisateur
			// Si l'utilisateur a choisi le bon dossier de resultats du labGX, le nombre (et le nom) des fichiers présent doit correspondre
			// aux puits (=atm). MAIS il peut :
			//  -1 se tromper de dossier (fichiers ne correspondant pas aux puits de l'expérience)
			//  -2 mal sélectionner les fichiers et en oublier !!
			
			angular.forEach(newValues, function(img){
				// les fichiers à importer :<date> <position> <sample>.png
				// exemple:  "2024 05 24 9H 55M C1  4ID.PNG"  ( !! plusieurs espaces !!)
				// ===>"H <chiffre><chiffre>M <pos> "
				var myRegex = new RegExp(/H \d\dM ([A-H]\d+) .*\.(png|PNG|jpg|JPG|jpeg|JPEG)/);
				var pos = img.fullname.match(myRegex)[1]; // 1er groupe de capture
				if ( pos !== null ){  
					// Ne charger le profil dans la map que s'il correspond a un fichier attendu !!
					if ( expectedMap.hasOwnProperty(pos) ){
						this[pos] = img;
						//console.log( pos + ': img loaded !');
					}
				}
			}, _profilesMap);
			
			// sauvegarder les fichiers pour l'onglet "Profils"
			internalProfiles = _profilesMap;
			
			// affecter les fichiers dans les experiment properties (onglet profils ??) !! ici on travaille sur displayResult !!
			angular.forEach($scope.atmService.data.displayResult, function(dr){
				// 22/06/2025 utiliser femtopulseLine+femtopulseColumn
				var pos = dr.data.inputContainerUsed.instrumentProperties.femtopulseLine.value + dr.data.inputContainerUsed.instrumentProperties.femtopulseColumn.value;
				//console.log("Assign Display Result >>>> "+ pos);

				$parse('inputContainerUsed.experimentProperties.migrationProfile').assign(dr.data, this[pos]);
			}, _profilesMap);
			
			// NGL-4008 ajout 
			// travailler directement sur atm !!! nécessaire pour le comptage intermédiaire ET final
			angular.forEach($scope.experiment.atomicTransfertMethods, function(atm){
				// 22/06/2025 utiliser femtopulseLine+femtopulseColumn
				var pos = atm.inputContainerUseds[0].instrumentProperties.femtopulseLine.value + atm.inputContainerUseds[0].instrumentProperties.femtopulseColumn.value;
				
				//console.log("Assign atm > "+ pos);
				$parse('inputContainerUseds[0].experimentProperties.migrationProfile').assign(atm, this[pos]);
				// $parse ne permet pas de savoir combien d'assign ont marché!!!
				if ( atm.inputContainerUseds[0].experimentProperties.migrationProfile ){
					nbAssignedFiles++;
					//console.log("nbAssignedFiles="+ nbAssignedFiles);
				}
			}, _profilesMap);
			
			// NGL-4008 ajout message intermédiaire
			$scope.messages.clear();
			$scope.alert = { "Erreurs fichiers":[] };	
			
			if ( nbAssignedFiles == 0){
				$scope.messages.clazz="alert alert-danger";
				$scope.messages.text="Aucun fichier profil correct sélectionné.";

				$scope.alert['Erreurs fichiers'].push("ne sont pas de la forme <date> <A-H><1-12> xxxxx.<png|PNG|jpg|JPG|jpeg|JPEG>");
				$scope.alert['Erreurs fichiers'].push("ne correspondent pas aux puits de l'expérience");
				$scope.messages.setDetails($scope.alert);
				$scope.messages.showDetails = true;
				$scope.messages.open();
			} else {
				$scope.messages.clazz="alert alert-success";
				$scope.messages.text='Chargement de '+ nbAssignedFiles + ' profil(s).';
				$scope.messages.showDetails = false;
				$scope.messages.open();
			}
			
			// mise à jour du message de profils manquants
			checkMissingMigrationProfiles();
		}
		
		angular.element('#importProfils')[0].value = null;// ajouté 04/01/2018
	});

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
		if ( ! $scope.isCreationMode() ){  
			var nbInputs;
			nbInputs=$scope.experiment.atomicTransfertMethods.length;
			//console.log ('nb inputs: '+ nbInputs);
		
			$scope.missingMigrationProfiles=nbInputs - countMigrationProfiles();
			console.log('missing profiles: '+ $scope.missingMigrationProfiles );
		}
	}

	// créer les clés correspondant aux atm existants 
	function initMap(map){
		angular.forEach($scope.experiment.atomicTransfertMethods, function(atm){
			// 22/06/2025 utiliser femtopulseLine+femtopulseColumn
			var pos = atm.inputContainerUseds[0].instrumentProperties.femtopulseLine.value + atm.inputContainerUseds[0].instrumentProperties.femtopulseColumn.value;
			this[pos]= null;
			console.log ( pos + ' need profile');
		}, map);
	}

	// verifications initiales
	checkMissingMigrationProfiles();

}]);