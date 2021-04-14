"use strict";

angular.module('home').controller('ConsultationCtrl',
		  [ '$http', '$scope', '$routeParams' , '$q', 'mainService', 'lists', 'tabService','messages','datatable',
	function($http,   $scope,   $routeParams,    $q,   mainService,   lists,   tabService,  messages ,  datatable) { 

// Dans bilans.consultation-trl.js
	//methode utilisée pour definir les colonnes 
			  	  
    var getBilanColumns = function() {				
    	var columns = [];
    	columns.push({property:"firstSubmissionDate",
    		header: Messages("firstSubmissionDate"),
      		type  :"date",	    	  	
    		hide  :true,
    		order :true,
    		edit  :false
    	});	
     	columns.push({property:"submissionCreationDate",
    		header: Messages("traceInformation.creationDate"),
      		type  :"date",	    	  	
    		hide  :true,
    		order :true,
    		edit  :false
    	});	
       	columns.push({property:"submissionCreationUser",
    		header: Messages("traceInformation.creationUser"),
      		type  :"date",	    	  	
    		hide  :true,
    		order :true,
    		edit  :false
    	});	
    	columns.push({property:"rawData",
    		header: Messages("bilans.rawData"),
     		type  :"String",		    	  	
    		hide  :true,
    		order :true,
    		edit  :false
    	});
    	columns.push({property:"collabFileName",
    		header: Messages("bilans.collabFileName"),
     		type  :"String",		    	  	
    		hide  :true,
    		order :true,
    		edit  :false
    	});
    	columns.push({property:"readSetCode",
    		header: Messages("bilans.readSetCode"),
    		type  :"String",		    	  	
    		hide  :true,
    		order :true,
    		edit  :false
    	});
    	columns.push({property:"typePlatform",
    		header: Messages("bilans.typePlatform"),
     		type  :"String",		    	  	
    		hide  :true,
    		order :true,
    		edit  :false
    	});
    	columns.push({property:"instrumentModel",
    		header: Messages("bilans.instrumentModel"),
     		type  :"String",		    	  	
    		hide  :true,
    		order :true,
    		edit  :false
    	});
       	columns.push({property:"echantillon",
    		header: Messages("bilans.echantillon"),
     		type  :"String",		    	  	
    		hide  :true,
    		order :true,
    		edit  :false
    	});
       	columns.push({property:"refCollab",
    		header: Messages("bilans.refCollab"),
     		type  :"String",		    	  	
    		hide  :true,
    		order :true,
    		edit  :false
    	});
    	columns.push({property:"experimentAC",
    		header: Messages("bilans.experimentAC"),
     		type  :"String",		    	  	
    		hide  :true,
    		order :true,
    		edit  :false
    	});
    	columns.push({property:"runAC",
    		header: Messages("bilans.runAC"),
     		type  :"String",		    	  	
    		hide  :true,
    		order :true,
    		edit  :false
    	});
    	columns.push({property:"studyAC",
    		header: Messages("bilans.studyAC"),
     		type  :"String",		    	  	
    		hide  :true,
    		order :true,
    		edit  :false
    	});
       	columns.push({property:"studyExternalId",
    		header: Messages("bilans.studyExternalId"),
     		type  :"String",		    	  	
    		hide  :true,
    		order :true,
    		edit  :false
    	});
    	columns.push({property:"sampleAC",
    		header: Messages("bilans.sampleAC"),
     		type  :"String",		    	  	
    		hide  :true,
    		order :true,
    		edit  :false
    	});
       	columns.push({property:"sampleExternalId",
    		header: Messages("bilans.sampleExternalId"),
     		type  :"String",		    	  	
    		hide  :true,
    		order :true,
    		edit  :false
    	});

    	return columns;
    };
    
	
	
	var bilansDTConfig = {
			name:'bilansDT',
			order :{by:'readSetCode',mode:'local', reverse:false},
			pagination:{
				active:true,
				mode:'local',
                numberRecordsPerPage: 100
			},
			select:{active:false},
			cancel:{active:false, showButton:false},
			showTotalNumberRecords:true,
			
			hide:{
				active:true,
				showButton:true
			},
			exportCSV:{
				active:true
			}			
		};
	

	
	

	//---------------------------------------------------------------------------------------------------
	
	console.log("Dans bilans.consultation-ctrl.js");
	$scope.messages = messages();	

	//Initialisation
	$scope.form = {};  // important. 
	$scope.lists = lists;
	//$scope.form.stateCodes = "ebiKnown";  // recherche de tous les states après SUB-F (SUBU..., SUBR...)
	$scope.form.pseudoStateCodes = ["pseudo_ebiKnown"];  // recherche de tous les states après SUB-F (SUBU..., SUBR...)
	$scope.sraVariables = {};
	$scope.messages.clear();
	$scope.treeLoadInProgress = false;
	console.log("Dans consultation-ctrl.js, scope.treeLoadInProgress = "   , $scope.treeLoadInProgress);

	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('consultation');
		tabService.addTabs({label:Messages('bilans.menu.consultation'),href:jsRoutes.controllers.sra.bilans.tpl.Bilans.home("consultation").url,remove:true});
		tabService.activeTab(0); // active l'onglet en le mettant en bleu
	}

//	Initialisation ProjectCodes:
	console.log("Dans consultation-ctrl.js, refresh projects");
	$scope.lists.refresh.projects();
	console.log("Dans consultation-ctrl.js, scope.list = ", $scope.lists);
	//lists.refresh.states({objectTypeCode:"SRASubmission"});


//	Initialisation datatable :
	$scope.bilansDT = datatable(bilansDTConfig);
	$scope.bilansDT.setColumnsConfig(getBilanColumns());
	
//	bouton_reset
	$scope.reset = function() {
		$scope.form = {};
		$scope.form.pseudoStateCodes = ["ebiKnown"];
		$scope.messages.clear();
		$scope.treeLoadInProgress = false;
	};	
	
//	bouton_search
	$scope.search = function() {
		$scope.messages.clear();
		$scope.treeLoadInProgress = true;
		console.log("Dans consultation-ctrl.search");
		console.log("Dans consultation-ctrl.search, scope.treeLoadInProgress = "   , $scope.treeLoadInProgress);
		console.log("Dans consultation-ctrl.search, scope.form = "                 , $scope.form);
		console.log("Dans consultation-ctrl.search, scope.form.studyAccession = "  , $scope.form.studyAccession);
		console.log("Dans consultation-ctrl.search, scope.form.studyExternalId = " , $scope.form.studyExternalId);
		console.log("Dans consultation-ctrl.search, scope.form.sampleAccession = " , $scope.form.sampleAccession);
		console.log("Dans consultation-ctrl.search, scope.form.studySampleId = "   , $scope.form.sampleExternalId);
		console.log("Dans consultation-ctrl.search, scope.form.projCodes = "       , $scope.form.projCodes);

		var studyExternalId = "NO_VALUE"; // variable utilise pour la recherche du study (si absence de valeur dans formulaire)
		var sampleExternalId = "NO_VALUE";

		if ((typeof $scope.form.studyExternalId  === 'undefined' || $scope.form.studyExternalId  === null) &&
			(typeof $scope.form.sampleExternalId === 'undefined' || $scope.form.sampleExternalId === null) &&
			(typeof $scope.form.studyAccession   === 'undefined' || $scope.form.studyAccession   === null) &&
			(typeof $scope.form.sampleAccession  === 'undefined' || $scope.form.sampleAccession  === null) &&
			(typeof $scope.form.projCodes        === 'undefined' || $scope.form.projCodes        === null) ) {
			console.log("Aucun parametre");
			$scope.treeLoadInProgress = false;
			$scope.messages.setError("Indiquez au moins un parametre pour votre recherche"); 

			throw "Veuillez definir un parametre";
		} else {
			console.log("Au moins un parametre defini");
		}
		if (!(typeof $scope.form.studyExternalId === 'undefined' || $scope.form.studyExternalId === null)) {
			studyExternalId = $scope.form.studyExternalId;
		}
		if (!(typeof $scope.form.sampleExternalId === 'undefined' || $scope.form.sampleExternalId === null)) {
			sampleExternalId = $scope.form.sampleExternalId;
		}
		var allRequest = [];
		var request = $http.get(jsRoutes.controllers.sra.studies.api.Studies.list().url,{
			params:{externalId:studyExternalId}})
		.catch (error => {
			console.log(error);
			console.log(error.data);
			$scope.messages.addDetails(error.data); 
			$scope.treeLoadInProgress = false;
			return $q.reject(error);                // propage erreur, comme throw 
		});
		console.log("req =" + request);
		allRequest.push(request);
		
		request = $http.get(jsRoutes.controllers.sra.samples.api.Samples.list().url,{
			params:{externalId:sampleExternalId}})
		.catch (error => {
			console.log(error);
			console.log(error.data);
			$scope.messages.addDetails(error.data); 
			$scope.treeLoadInProgress = false;
			return $q.reject(error);                // propage erreur, comme throw 
		});
		console.log("req =" , request);
		allRequest.push(request);
		// lancement des requetes pour study et sample
		$q.all(allRequest)
		.then(result => { // resultat de l'ensemble des requetes, avec result[0]=resultat de la requete 1 
			              // avec tableau de data si la requete retourne une liste (url de la forme list) 
			              // et data si la requete retourne un seul resultat(url de la forme get)
			console.log("result = " + result); 	
			console.log("result[0] = " + result[0]); 	
			console.log("result[0].data = " + result[0].data); 
			console.log("result[0].data[0] = " + result[0].data[0]); 
			var study = result[0].data[0];
			if (typeof study === 'undefined' || study === null) {
			    // variable is undefined or null
				console.log("Pas de study recuperé");
			} else {
				// Mise à jour du formulaire pour studyAccession
				$scope.form.studyAccession = study.accession;
			}
			var sample = result[1].data[0];
			if (typeof sample === 'undefined' || sample === null) {
			    // variable is undefined or null
				console.log("Pas de sample recuperé");
			} else {
				// Mise à jour du formulaire pour sampleAccession
				$scope.form.sampleAccession = sample.accession;
			}
			$scope.search_exp_readsets();
			//$scope.treeLoadInProgress = false;
		});// fin requete externalId

	}; // fin bouton $scope.search
	
		
	
	$scope.search_exp_readsets = function() {
		var tab_experiments = [];
		var tab_bilans = [];
		var readsets = {}; 
		var studies = {};
		var samples = {};
		
		console.log("Dans consultation-ctrl.search");
		console.log("Dans consultation-ctrl.search, scope.form = " + $scope.form);
		console.log("Dans consultation-ctrl.search, scope.form.studyAccession = " + $scope.form.studyAccession);
		console.log("Dans consultation-ctrl.search, scope.form.sampleAccession = " + $scope.form.sampleAccession);
		console.log("Dans consultation-ctrl.search, scope.form.projCodes = " + $scope.form.projCodes);
		$scope.form.pseudoStateCodes = ['pseudo_ebiKnown']; // liste des stateCodescorrespondant à donnees soumises
		//$scope.experimentsDT.search($scope.form); // ne pas utiliser car asynchrone et on est dans la suite 
													// du code avant la fin de la requete.
		var allRequestExp = [];
//		var request = $http.get(jsRoutes.controllers.sra.experiments.api.Experiments.list().url,{
//			params:{studyAccession:$scope.form.studyAccession, 
//				    sampleAccession:$scope.form.sampleAccession, 
//				    projCodes:$scope.form.projCodes,
//				    stateCode:"ebiKnown"}})  // pour avoir bilan des données vraimment soumises 
		var request = $http.get(jsRoutes.controllers.sra.experiments.api.Experiments.list().url,{
			params:{studyAccession:$scope.form.studyAccession, 
				    sampleAccession:$scope.form.sampleAccession, 
				    projCodes:$scope.form.projCodes,
				    pseudoStateCodes:$scope.form.pseudoStateCodes}})  // pour avoir bilan des données vraimment soumises 
			.catch (error => {
			console.log(error);
			console.log(error.data);
			$scope.messages.addDetails(error.data); 
			$scope.treeLoadInProgress = false;
			return $q.reject(error);                // propage erreur, comme throw 
		});
		
//		console.log("req =" + request);
		allRequestExp.push(request);
		$q.all(allRequestExp)
		.then(result => { // resultat de l'ensemble des requetes, avec result[0]=resultat de la requete 1 
			              // avec tableau de data si la requete retourne une liste (url de la forme list) 
			              // et data si la requete retourne un seul resultat(url de la forme get)
			console.log("apres req exp , result = " , result); 	
			console.log("result[0] = " , result[0]); 	
			console.log("result[1] = " , result[1]); 	
			console.log("result[0].data = " , result[0].data); 
			console.log("result[0].data[0] = " , result[0].data[0]); 
			//console.log("result[0].data[0].code = " , result[0].data[0].code); 
//			console.log("result[0].data[1].code = " , result[0].data[1].code); 
//			console.log("result[0].data[2].code = " , result[0].data[2].code); 
//			console.log("result[0].data[3].code = " , result[0].data[3].code); 
			

			
			// construction des requetes pour study, samples et readset en fonction du resultat de la requete des exp :
			
			var allRequestReadset = [];
			var allRequestStudy   = [];
			var	allRequestSample  = [];
			$scope.buildRequests(result, allRequestReadset, allRequestStudy, allRequestSample, tab_experiments);

			// construction du hash des readsets :
			 $q.all(allRequestReadset)
			.then(result => { 
				for (var j = 0; j < result.length; j++) {
					for (var k = 0; k < result[j].data.length; k++) {
						var readsetCode = result[j].data[k].code;
						var readset = result[j].data[k];
						//console.log("readset Code avant integration dans hash : " , readset.code);
						//Object.assign(readsets, {readsetCode: readset}); // ne marche pas prendre ecriture suivante :	
						readsets[readsetCode] = readset;
					}
				}
				// construction du hash des studies :
				$q.all(allRequestStudy)
				.then(result => { 
					for (var j = 0; j < result.length; j++) {
						for (var k = 0; k < result[j].data.length; k++) {
							var study = result[j].data[k];
							var study_ac = study.accession;
							studies[study_ac] = study;
							console.log("study avant integration dans hash : " , study.code);
//							console.log("study avant integration dans hash : " , study.externalId);
//							console.log("apres integration dans hash, study_ac=", study_ac);
//							console.log("apres integration dans hash, studies[study_ac].accession = " , studies[study_ac].accession);
//							console.log("apres integration dans hash, studies[study_ac].code = " , studies[study_ac].code);
//							console.log("apres integration dans hash, studies[study_ac].externalId = " , studies[study_ac].externalId);
						}
					}
					// construction du hash des samples :
					$q.all(allRequestSample)
					.then(result => { 
						for (var j = 0; j < result.length; j++) {
							for (var k = 0; k < result[j].data.length; k++) {
								var sample = result[j].data[k];
								var sample_ac = sample.accession;			
								samples[sample_ac] = sample;
								console.log("sample avant integration dans hash : "  , sample.code);
//								console.log("sample avant integration dans hash : "  , sample.externalId);
//								console.log("apres integration dans hash, sample_ac=", sample_ac);
//								console.log("apres integration dans hash, samples[sample_ac].accession = "  , samples[sample_ac].accession);
//								console.log("apres integration dans hash, samples[sample_ac].code = "       , samples[sample_ac].code);
//								console.log("apres integration dans hash, samples[sample_ac].externalId = " , samples[sample_ac].externalId);
							}
						}
						
						// Construire tableau des objets bilan :
						for (var j = 0; j < tab_experiments.length; j++) {	
							var exp = tab_experiments[j];
							var date = tab_experiments[j].traceInformation.creationDate;
							var readsetCode     = tab_experiments[j].readSetCode;
							var studyAccession  = tab_experiments[j].studyAccession;
							var sampleAccession = tab_experiments[j].sampleAccession;
							console.log("construction tableau bilans, experiment.sampleAccession = ", tab_experiments[j].sampleAccession);
							
							var echantillon  = "-----";
							var refCollab = "-----";
							var studyExternalId = "-----";
							var sampleExternalId = "-----";

							for(var k = 0; k < exp.run.listRawData.length; k++) {
								if(readsets.hasOwnProperty(readsetCode)) {
									var readset = readsets[readsetCode];
									console.log("readsetCode recuperé du hash = " , readset.code);
									echantillon = readset.sampleCode;
									console.log("echantillon = " , echantillon);
									refCollab = readset.sampleOnContainer.referenceCollab;
									console.log("refCollab = " , refCollab);
								}
								if(studies.hasOwnProperty(studyAccession)) {
//									console.log("study trouve pour "                , studyAccession);
//									console.log("studies[studyAccession] "          ,  studies[studyAccession]);
//									console.log("studies[studyAccession].code "     , studies[studyAccession].code);
//									console.log("studies[studyAccession].externalId", studies[studyAccession].externalId);
									studyExternalId = studies[studyAccession].externalId;
								}
								if(samples.hasOwnProperty(sampleAccession)) {
//									console.log("sample trouve pour " , sampleAccession);
//									console.log("sampleSame "         , samples[sampleAccession].externalId);
									sampleExternalId = samples[sampleAccession].externalId;
								}
								
								var bilan = { rawData         : exp.run.listRawData[k].relatifName,
										  collabFileName      : exp.run.listRawData[k].collabFileName,
										  firstSubmissionDate : exp.firstSubmissionDate,
										  submissionCreationDate  : exp.traceInformation.creationDate,
										  submissionCreationUser  : exp.traceInformation.createUser,
											  readSetCode     : readsetCode,
											  echantillon     : echantillon,
											  refCollab       : refCollab,
											  typePlatform    : exp.typePlatform,
											  instrumentModel : exp.instrumentModel,
											  experimentAC    : exp.accession,
											  runAC           : exp.run.accession,
											  studyAC         : exp.studyAccession,
											  studyExternalId : studyExternalId,
											  sampleAC        : exp.sampleAccession,
											  sampleExternalId: sampleExternalId
											};
								tab_bilans.push(bilan);			  
							}
						}// fin construction bilan
						console.log("tab_bilans.length = ", tab_bilans.length);
						// Attention initialisation de bilansDT deja faite à l'initialisation et pb si refait ici,
						// $scope.bilansDT est perdu !!!!
						// $scope.bilansDT = datatable(bilansDTConfig); 
						$scope.bilansDT.setData(tab_bilans, tab_bilans.length);	
						console.log("Dans code requetes, tab_experiments.length = ", tab_experiments.length);
				 		$scope.treeLoadInProgress = false;
					}); // fin requetes samples
				}); // fin requetes studies
					}); // fin requetes readset
				});// fin requete experiment

		// Ne pas mettre de code apres la requete car executé avant la fin de la requete 
		//console.log("Apres code requetes , tab_experiments.length = " + tab_experiments.length);
		
	}; // fin bouton $scope.search_exp_readsets
	
	
	$scope.buildRequests = function(result, allRequestReadset, allRequestStudy, allRequestSample, tab_experiments) {
		var tab_readsetCodes = []; // tableau provisoire pour construire requete
		var tab_studyAc      = []; // tableau provisoire pour construire requete
		var tab_sampleAc     = []; // tableau provisoire pour construire requete
		var max = 100;             // maximum de donneés recherchées pour une requete.

		for (var i = 0; i < result[0].data.length; i++) {
//			console.log("XXXXXXXXXXXX    result[0].data[i].readSetCode=" , result[0].data[i].readSetCode)
			tab_experiments.push(result[0].data[i]);
			tab_readsetCodes.push(result[0].data[i].readSetCode);
			tab_studyAc.push(result[0].data[i].studyAccession); 
			tab_sampleAc.push(result[0].data[i].sampleAccession); 

			if(tab_readsetCodes.length > max) {
				// construire requetes readsets
				// On n'utilise pas la requete get qui nous ferait une requete par experiment.
				//var request = $http.get(jsRoutes.controllers.readsets.api.ReadSets.get(experiment.readSetCode).url)
				var requestReadset = $http.get(jsRoutes.controllers.readsets.api.ReadSets.list().url,{params:{codes:tab_readsetCodes}})
				.catch (error => {
					console.log(error);
					console.log(error.data);
					$scope.messages.addDetails(error.data); 
					return $q.reject(error);                // propage erreur, comme throw 
				});
				allRequestReadset.push(requestReadset);
				
				// construire requetes study internes (les ExternalStudy ne nous interressent pas)
				var requestStudy = $http.get(jsRoutes.controllers.sra.studies.api.Studies.list().url,
						{params:{accessions:tab_studyAc, type:"Study"}})
				.catch (error => {
					console.log(error);
					console.log(error.data);
					$scope.messages.addDetails(error.data); 
					$scope.treeLoadInProgress = false;
					return $q.reject(error);                // propage erreur, comme throw 
				});
				allRequestStudy.push(requestStudy);					
				// construire requetes sample internes (les ExternalSample ne nous interressent pas)
				var requestSample = $http.get(jsRoutes.controllers.sra.samples.api.Samples.list().url,
						{params:{accessions:tab_sampleAc, type:"Sample"}})
				.catch (error => {
					console.log(error);
					console.log(error.data);
					$scope.messages.addDetails(error.data); 
					$scope.treeLoadInProgress = false;
					return $q.reject(error);                // propage erreur, comme throw 
				});
				allRequestSample.push(requestSample);						
				
				// Reinitialiser
				tab_readsetCodes = [];
				tab_studyAc = [];
				tab_sampleAc = [];
			}
		}
		
		// correction effet de bord pour req readset:
		if(tab_readsetCodes.length > 0) {
			var requestReadset = $http.get(jsRoutes.controllers.readsets.api.ReadSets.list().url,
					{params:{codes:tab_readsetCodes}})
			.catch (error => {
				console.log(error);
				console.log(error.data);
				$scope.messages.addDetails(error.data); 
				$scope.treeLoadInProgress = false;
				return $q.reject(error);                // propage erreur, comme throw 
			});
			allRequestReadset.push(requestReadset);
	     }
		// correction effet de bord pour req studies:
		if(tab_studyAc.length > 0) {
			var requestStudy = $http.get(jsRoutes.controllers.sra.studies.api.Studies.list().url,
					{params:{accessions:tab_studyAc, type:"Study"}})
			.catch (error => {
				console.log(error);
				console.log(error.data);
				$scope.messages.addDetails(error.data); 
				$scope.treeLoadInProgress = false;
				return $q.reject(error);                // propage erreur, comme throw 
			});
			allRequestStudy.push(requestStudy);
	     }	
		// correction effet de bord pour req samples:
		if(tab_sampleAc.length > 0) {
			var requestSample = $http.get(jsRoutes.controllers.sra.samples.api.Samples.list().url,
					{params:{accessions:tab_sampleAc, type:"Sample"}})
			.catch (error => {
				console.log(error);
				console.log(error.data);
				$scope.messages.addDetails(error.data); 
				$scope.treeLoadInProgress = false;
				return $q.reject(error);                // propage erreur, comme throw 
			});
			allRequestSample.push(requestSample);
	     }			
	}// end buildRequest
	
}]);
