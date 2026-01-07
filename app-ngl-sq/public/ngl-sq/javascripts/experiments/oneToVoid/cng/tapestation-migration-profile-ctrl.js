// copie labchip-migration-profil-ctrl + adaptation
angular.module('home').controller('OneToVoidTapestationMigrationProfileCNGCtrl',['$scope', '$parse',
                                                             function($scope,$parse) {

	var inputExtraHeaders=Messages("experiments.inputs");
	
	// la definir plus tot ?? OUI
	$scope.modeTapestation = $scope.experiment.instrumentProperties.modeTapestation.value;

	// surcharger la variable "name" definie dans le controleur parent ( one-to-void-qc-ctrl.js) => nom de fichier CSV exporté 
	var config = $scope.atmService.data.getConfig();
	config.name = $scope.experiment.typeCode.toUpperCase();

	// boutons pour definir automatiquement les positions tapestation (!! il ne s'agit pas des boutons standard "baguettes magiques")
	// probleme avec ng-disabled="!isEditMode()" : marche pas... boutons actif en permanence!!!- ????
	config.otherButtons={
		active: ($scope.isEditModeAvailable() && $scope.isWorkflowModeAvailable('F')),
		complex:true,
		template:''  
		    +$scope.plateTapestationUtils.templates.buttonColumnMode() 
	};

	// ajouter le bouton copyPosition si plaque=> mode plaque
	//utiliser plateFluoUtils.isPlate() !!! ???
	if ( $scope.plateFluoUtils.isPlate() && $scope.modeTapestation == "plaque" ){
		config.otherButtons.template+=$scope.plateTapestationUtils.templates.buttonCopyPosition();
	} 
	
	$scope.atmService.data.setConfig(config);

	// modifs par rapport a labchip-migration-profil-ctrl=> mettre la config colum en premier !!
	// FDS NGL-1055: mettre le getArray|unique dans filter et pas dans render
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
		"extraHeaders":{0: inputExtraHeaders},
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
		"extraHeaders" : {0 : inputExtraHeaders}
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
		"extraHeaders" : {0 : inputExtraHeaders}
	});

	columns.push({
		"header" : Messages("containers.table.tapestationLine"),
		"property" : "inputContainerUsed.instrumentProperties.tapestationLine.value",
		"order" : true,
		"edit" : true,
		"editDirectives":" udt-change='updatePropertyFromUDT(value,col)' ",
		"choiceInList":true,
		"possibleValues":[{"name":'A',"code":"A"},{"name":'B',"code":"B"},{"name":'C',"code":"C"},{"name":'D',"code":"D"},
			{"name":'E',"code":"E"},{"name":'F',"code":"F"},{"name":'G',"code":"G"},{"name":'H',"code":"H"}],
		"hide" : true,
		"type" : "text",
		"position" : 11,
		"extraHeaders" : {0 : inputExtraHeaders}
	});

	if ($scope.experiment.instrumentProperties.modeTapestation.value == 'plaque'){
		columns.push({
			"header" : Messages("containers.table.tapestationColumn"),
			"property" : "inputContainerUsed.instrumentProperties.tapestationColumn.value",
			"order" : true,
			"edit" : true,
			"editDirectives":" udt-change='updatePropertyFromUDT(value,col)' ",
			"choiceInList":true,
			"possibleValues":[{"name":'1',"code":"1"},{"name":'2',"code":"2"},{"name":'3',"code":"3"},{"name":'4',"code":"4"},
				{"name":'5',"code":"5"},{"name":'6',"code":"6"},{"name":'7',"code":"7"},{"name":'8',"code":"8"},
				{"name":'9',"code":"9"},{"name":'10',"code":"10"},{"name":'11',"code":"11"},{"name":'12',"code":"12"}], 
			"hide" : true,
			"type" : "number",
			"position" : 11.5,
			"extraHeaders" : {0 : inputExtraHeaders}
		});
	} else {
		columns.push({
			"header" : Messages("containers.table.tapestationColumn"),
			"property" : "inputContainerUsed.instrumentProperties.tapestationColumn.value",
			"order" : true,
			"edit" : true,
			"editDirectives":" udt-change='updatePropertyFromUDT(value,col)' ",
			"choiceInList":true,
			"possibleValues":[{"name":'1',"code":"1"},{"name":'2',"code":"2"}], 
			"hide" : true,
			"type" : "number",
			"position" : 11.5,
			"extraHeaders" : {0 :inputExtraHeaders}
		});
	}

	//  ajouter une colonne "concentration.unit" car la colonne "concentration1" issue des 
	// properties de l'experience est maintenant définie sans unité. C'est a l'utilisateur de la preciser
	columns.push({
		"header" :  Messages("containers.table.concentration.unit.shortLabel"),
		"property" : "inputContainerUsed.experimentProperties.concentration1.unit",
		"order" : true,
		"edit" : true,
		//"editDirectives":"udt-change='updatePropertyFromUDT(value,col)'",
		"hide" : true,
		"type" : "text",
		"position" : 13.5, //concentration1 est en position 13
		"choiceInList":true,
		"listStyle":"select",
		"possibleValues":[{"name":"nM","code":"nM"},{"name":"ng/µl","code":"ng/µl"} ],
		"extraHeaders" : {0 : Messages("experiments.inputs")}
	});

	$scope.atmService.data.setColumnsConfig(columns);

	// forcer largeur de la colonne "Taille"...
	// classes  dans create-experiments.css
	var dtConfig = $scope.atmService.data.getConfig();
	var sizePosition= $scope.getColumnPosition(dtConfig, "inputContainerUsed.experimentProperties.size1.value");
	if (dtConfig.columns && dtConfig.columns[sizePosition]) {
		dtConfig.columns[sizePosition].header="Taille"+"\xA0"+"(pb)"; // ajout espace inseccable octal: pas beau mais MARCHE !!!
		//dtConfig.columns[sizePosition].thClass="columnWidth70px"; //MARCHE PAS ici !!!!!! 
	}

	function checkNbInputContainers( mode ){
		// info du labo utilisateur: en mode plaque la ladder est sur un strip a part, A1 n'est PAS réservée
		if ( $scope.isCreationMode() ){

			$scope.messages.clear();
			// plaque: max 96 ; strip max 15 (16-1)
			var maxInputContainer;
			if (mode=='plaque'){ 
				maxInputContainer=96;
			} else { 
				maxInputContainer=15
			};

			// passer par basket en mode creation !!
			if ( $scope.mainService.getBasket().length() > maxInputContainer ){ 
				$scope.messages.setError(Messages("experiments.input.error.maxContainers",maxInputContainer));
				return false;
			} else { return true; }
		}
	}

	/* pb de boucle infinie quand on remet la valeur initiale !! => relance le watch
	https://stackoverflow.com/questions/15715672/how-do-i-stop-watching-in-angularjs 
	var unregister = $scope.$watch('foo', function () {
		// Do something interesting here ...
		unregister();
	  });
	  ==> evite la boucle infinie mais n'empeche pas les valeurs non autorisées
	*/
	var unregister= $scope.$watch("experiment.instrumentProperties.modeTapestation.value", function(newValue, oldValue){
		if ((newValue) && (newValue !== null ) && ( newValue !== oldValue )){		
			if ( !$scope.isCreationMode() ){
				// modification non autorisée !!! comment annuler/interdire la modification ????????
				console.log("update modeTapestation "+newValue+ ' INTERDIT !!!!');
				// => relance le watch : pb de boucle infinie  $rootScope:infdig
				// unregister puis remettre l'ancienne valeur
				unregister(); 
				$scope.experiment.instrumentProperties.modeTapestation.value=oldValue;
				//==> evite la boucle infinie, empeche une premiere modification mais pas une suivante

				return;
			} else {
					//console.log("update modeTapestation "+newValue);

					if ( checkNbInputContainers( newValue ) == true ) {
						//  la propriété "possibleValues" de la colonne containers.table.tapestationColumn est variable en fonction de modeTapestation
						var dtConfig=$scope.atmService.data.getConfig();
						var posTapestationColumn=$scope.getColumnPosition(dtConfig, "inputContainerUsed.instrumentProperties.tapestationColumn.value");

						if ( posTapestationColumn != undefined ){
							//console.log('pos tapestationColumn='+ i);
							if ( newValue === "plaque" ){
								dtConfig.columns[posTapestationColumn].possibleValues=[{"name":'1',"code":"1"},{"name":'2',"code":"2"},{"name":'3',"code":"3"},{"name":'4',"code":"4"},
										{"name":'5',"code":"5"},{"name":'6',"code":"6"},{"name":'7',"code":"7"},{"name":'8',"code":"8"},
										{"name":'9',"code":"9"},{"name":'10',"code":"10"},{"name":'11',"code":"11"},{"name":'12',"code":"12"}]
							} else {
								dtConfig.columns[posTapestationColumn].possibleValues=[{"name":'1',"code":"1"},{"name":'2',"code":"2"}]
							}
						}// sinon on a pas trouvé la colonne !!
					} // sinon il y a une erreur de nbre inputs...

					// mettre a jour la $scope utilisée par le scala !!!
					$scope.modeTapestation=newValue;
			} 
			console.log ("new=old");	
		}
		console.log('scope.modeTapestation='+ $scope.modeTapestation);
	});

	// ajouter un 'save' au scope local en plus du 'save' du controleur parent 
	$scope.$on('save', function(e, callbackFunction) {	
		console.log("call event on save TAPESTATION");
		// lancer le comptage pour le cas ou il y a eu des suppressions de profils avec l'icone "poubelle"
		// l'icone poubelle existe si propriété migrationProfile editable...
		checkMissingMigrationProfiles();

		// verifier les positions ;  23/07/2025 ajout childSavedError'
		// mais ne bloque pas car pas traité au niveau du parent ???
		if ( checkTapeStationPositions() > 0 ){
			$scope.$emit('childSavedError', callbackFunction);
		} else {
			$scope.$emit('childSaved', callbackFunction);
		}
	});

	/*  taille et concentration pas copiées dans container !!! mais methode appellée depuis le parent générique one-to-void-qc-ctrl.js 
	   => elle doit donc exister meme si elle ne fait rien....*/
	$scope.$parent.copyPropertiesToInputContainer = function(experiment){
		//console.log("copyPropertiesToInputContainer: RIEN A FAIRE ??");
	}

	// 23/06/2025 ERREUR: le bouton s'affiche avant la sauvegarde initiale et sans le choix par l'utiliateur des positions tapeStation
	$scope.profilsButton = {
		isShow:function(){
			return ( !$scope.mainService.isEditMode() && ( $scope.isInProgressState()|| Permissions.check("admin")) )
			}
	};
	
	// pour une expérience déjà sauvegardée: récupérer les fichiers et les stocker dans profilesMap
	var profilesMap = {};
	angular.forEach($scope.experiment.atomicTransfertMethods, function(atm){
		var pos = atm.inputContainerUseds[0].locationOnContainerSupport.line+atm.inputContainerUseds[0].locationOnContainerSupport.column;
		//console.log ( atm.inputContainerUseds[0].code + ' POS='+pos)
		var img = $parse('inputContainerUseds[0].experimentProperties.migrationProfile')(atm);
		   	//toutes les positions n'ont pas une image !!
			//if ( img !== undefined ) console.log( '=> img exist')
		this[pos] = img;
	}, profilesMap)

	// copie pour l'onglet Profils ??
	var internalProfiles = profilesMap;
	
	// methode appellée dans le scala.html pour afficher 1 profil dans l'onglet "profils"
	$scope.getProfile=function(line, column){
		var pos=line+column;
		return internalProfiles[pos];
	};
	
	// watch sur bouton "Sélect. fichiers" de l'onglet "profils"
	$scope.$watch("selectedProfiles",function(newValues){
		if(newValues){
			var nbAssignedFiles=0;
			var _profilesMap = {}; // pour stocker les fichiers chargés; on repart d'une map vide: on ne regarde pas s'il y avait deja qq chose avant !!!
			                       // donc ecrase des fichier chargés "un par un" dans l'onglet "tableau"
			//initMap(_profilesMap);  !!  pose probleme => creation autre map 'expectedMap'

			var expectedMap ={};
			initMap(expectedMap);
			
			// afficher message intermédiaire=> compter les fichiers corrects sélectionnés par l'utilisateur
			// Si l'utilisateur a choisi le bon dossier de resultats de la tapeStation, le nombre (et le nom) des fichiers présent doit correspondre
			// aux puits (=atm). MAIS il peut :
			//  -1 se tromper de dossier (fichiers ne correspondant pas aux échantillons de l'expérience)
			//  -2 mal sélectionner les fichiers et en oublier !!
			
			angular.forEach(newValues, function(img){
				// les fichiers à importer :<date>_<position>_<sample>.png
				// exemple : zzzzzz_A1_xxxxx.png
				
				var myRegex = new RegExp(/_([A-H]\d+)_.*\.(png|PNG|jpg|JPG|jpeg|JPEG)/);
				var pos = img.fullname.match(myRegex)[1]; // 1er groupe de capture
				if ( pos !== null ){  
					// Ne charger le profil dans la map que s'il correspond a un fichier attendu !!
					if ( expectedMap.hasOwnProperty(pos) ){
						this[pos] = img;
						console.log( pos + ': img loaded :'+ this[pos].fullname );
					}
				}
			}, _profilesMap);
			
			// sauvegarder les fichiers pour l'onglet "Profils"
			internalProfiles = _profilesMap; 

			// affecter  les fichiers dans les experiment properties (onglet profils ??) !! ici on travaille sur displayResult !!
			angular.forEach($scope.atmService.data.displayResult, function(dr){
				// 16/06/2025 utiliser tapestationLine+tapestationColumn
				var pos = dr.data.inputContainerUsed.instrumentProperties.tapestationLine.value+dr.data.inputContainerUsed.instrumentProperties.tapestationColumn.value;
				
				//console.log("Assign Display Result >>>> "+ pos);
				$parse('inputContainerUsed.experimentProperties.migrationProfile').assign(dr.data, this[pos]);
			}, _profilesMap);
			 
			// affecter les fichiers directement sur atm !!! nécessaire pour le comptage intermédiaire ET final
			angular.forEach($scope.experiment.atomicTransfertMethods, function(atm){
				// 16/06/2025 utiliser tapestationLine+tapestationColumn
				var pos = atm.inputContainerUseds[0].instrumentProperties.tapestationLine.value+atm.inputContainerUseds[0].instrumentProperties.tapestationColumn.value;
				
				console.log("Assign atm > "+ pos);
				$parse('inputContainerUseds[0].experimentProperties.migrationProfile').assign(atm, this[pos]);
				// $parse ne permet pas de savoir combien d'assign ont marché!!!
				if ( atm.inputContainerUseds[0].experimentProperties.migrationProfile ){
					nbAssignedFiles++;
					//console.log("nbAssignedFiles="+ nbAssignedFiles);
				}
			}, _profilesMap);
			
			$scope.messages.clear();
			$scope.alert = { "Erreurs fichiers":[] };	
			
			if ( nbAssignedFiles == 0){
				$scope.messages.clazz="alert alert-danger";
				$scope.messages.text="Aucun fichier profil correct sélectionné.";

				$scope.alert['Erreurs fichiers'].push("ne sont pas de la forme _<A-H><1-12>_xxxxx.<png|PNG|jpg|JPG|jpeg|JPEG>");
				$scope.alert['Erreurs fichiers'].push("ne correspondent pas aux positions Tapestation");
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
		
		angular.element('#importProfils')[0].value = null;
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
			//console.log('missing profiles: '+ $scope.missingMigrationProfiles );
		}
	}
	
	// créer les clés de map correspondant aux atm existants
	function initMap(map){
		angular.forEach($scope.experiment.atomicTransfertMethods, function(atm){
			// 16/06/2025 utiliser tapestationLine+tapestationColumn
			var pos = atm.inputContainerUseds[0].instrumentProperties.tapestationLine.value+atm.inputContainerUseds[0].instrumentProperties.tapestationColumn.value;
			this[pos]=null;
			console.log ( pos + ' need profile !!');
		}, map);
	}

	/* NON  l'appel ne permet pas de voir les erreurs en direct !!!
	$scope.updatePropertyFromUDT = function(value, col){
		console.log("update from property : "+col.property);
		if ((col.property === 'inputContainerUsed.instrumentProperties.tapestationColumn.value')||
		    (col.property === 'inputContainerUsed.instrumentProperties.tapestationLine.value')){
			console.log('call countA1');
			countA1();
		}
	}*/

	// NGL controle les doublons de position des outpoutContainer, mais ici ce ne sont pas des outputContainer !!
	// il ne suffit pas de verifier A1, en fait il faut vérifié qu'aucun doublon n'a été donné par l'utilisateur...
	function checkTapeStationPositions(){
		if ( ! $scope.isCreationMode() ){  		 /// faire qd ce check ???
			var errors=0;
			var posMap = {}; 

			// preparation du message
			$scope.messages.clear();
			$scope.alert = { "Erreurs positions TapeStation":[] };// nom interne de l'erreur
			$scope.messages.clazz='alert alert-danger';
			$scope.messages.text='Erreurs positions TapeStation';  // titre affiché de l'erreur

			$scope.experiment.atomicTransfertMethods.forEach(function(atm){
				//attention lors de la creation de l'expérience ces 2 valeurs sont non definies...
				if (atm.inputContainerUseds[0].instrumentProperties && 
					atm.inputContainerUseds[0].instrumentProperties.tapestationLine && 
					atm.inputContainerUseds[0].instrumentProperties.tapestationColumn ){

					var tapestationLine=   atm.inputContainerUseds[0].instrumentProperties.tapestationLine.value;
					var tapestationColumn= atm.inputContainerUseds[0].instrumentProperties.tapestationColumn.value;
					var posTapestation=tapestationLine+tapestationColumn;

					if ( posMap[posTapestation] == null ) { 
						posMap[posTapestation]=1;
					
						// !! A1 autorisée sur plaque
						if ( posTapestation === "A1" && $scope.modeTapestation == "strip"){ 
							errors++;
							//console.log( 'ATM pos A1 found');
							$scope.alert['Erreurs positions TapeStation'].push('A1 est réservée au ladder en mode strip');
						}
					} else {
						errors++;
						//console.log('DUPLICATE ATM pos='+posTapestation );
						$scope.alert['Erreurs positions TapeStation'].push('doublon : '+ posTapestation);
					}
				}
			});

			if ( errors > 0 ){
				$scope.messages.setDetails($scope.alert);
				$scope.messages.showDetails = true;
				$scope.messages.open();
			}
		}
		// test ajout return
		return errors;
 	}

	// verifications initiales
	checkNbInputContainers($scope.modeTapestation); 
	// meme si le nombre est incorrect l'expérience est déja chargée dans le controleur parent (OneToVoidQCCtrl)=> peut pas la bloquer!!
	// note dans le controller parent il y a deja des verif de ce type pour l'experience XXX=> faut il continuer dans ce sens ???

	checkMissingMigrationProfiles();
	checkTapeStationPositions();
	
}]);
