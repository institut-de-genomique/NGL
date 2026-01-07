"use strict";

angular.module('home').controller('ConsultationCtrl',
		  [ '$http', '$scope', '$routeParams' , '$q', 'mainService', 'lists', 'tabService','messages','toolsServices','datatable',
	function($http,   $scope,   $routeParams,    $q,   mainService,   lists,   tabService,  messages , toolsServices, datatable) { 

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
       	columns.push({property:"refCollabSub",
    		header: Messages("bilans.refCollabSub"),
     		type  :"String",		    	  	
    		hide  :true,
    		order :true,
    		edit  :false
    	});
    	columns.push({property:"analysisAC",
    		header: Messages("bilans.analysisAC"),
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
	
	//console.log("Dans bilans.consultation-ctrl.js");
	$scope.messages = messages();	// enleve le message mais pas la partie details + efficace d'utiliser $scope.messages.clear();
    $scope.messages.clear();
	//Initialisation
	$scope.form = {};  // important. 
	$scope.lists = lists;
	//$scope.form.stateCodes = "ebiKnown";  // recherche de tous les states après SUB-F (SUBU..., SUBR...)
	$scope.form.pseudoStateCodes = ["pseudo_ebiKnown"];  // recherche de tous les states après SUB-F (SUBU..., SUBR...)
	$scope.sraVariables = {};
	$scope.treeLoadInProgress = false;
	//console.log("Dans consultation-ctrl.js, scope.treeLoadInProgress = "   , $scope.treeLoadInProgress);

	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('consultation');
		tabService.addTabs({label:Messages('bilans.menu.consultation'),href:jsRoutes.controllers.sra.bilans.tpl.Bilans.home("consultation").url,remove:true});
		tabService.activeTab(0); // active l'onglet en le mettant en bleu
	}

//	Initialisation ProjectCodes:
	//console.log("Dans consultation-ctrl.js, refresh projects");
	$scope.lists.refresh.projects();
	//console.log("Dans consultation-ctrl.js, scope.list = ", $scope.lists);
	//lists.refresh.states({objectTypeCode:"SRASubmission"});


//	Initialisation datatable :
	$scope.bilansDT = datatable(bilansDTConfig);
	$scope.bilansDT.setColumnsConfig(getBilanColumns());
	
//	bouton_reset
	$scope.reset = function() {
		$scope.form = {};
		$scope.form.pseudoStateCodes = ["ebiKnown"];
		$scope.messages = messages();
		$scope.messages.clear();
		$scope.treeLoadInProgress = false;
		$scope.bilansDT = datatable(bilansDTConfig);
	    $scope.bilansDT.setColumnsConfig(getBilanColumns());
	};	
	
//	bouton_search
	$scope.search = function() {
		$scope.messages = messages();
		$scope.messages.clear();
		$scope.treeLoadInProgress = true;
//		console.log("Dans consultation-ctrl.search");
//		console.log("Dans consultation-ctrl.search, scope.treeLoadInProgress = "   , $scope.treeLoadInProgress);
//		console.log("Dans consultation-ctrl.search, scope.form = "                 , $scope.form);
//		console.log("Dans consultation-ctrl.search, scope.form.studyIdentifier= " , $scope.form.studyIdentifier);
//		console.log("Dans consultation-ctrl.search, scope.form.sampleIdentifier = " , $scope.form.sampleIdentifier);
//		console.log("Dans consultation-ctrl.search, scope.form.projCodes = "       , $scope.form.projCodes);


		// Les 2 ecritures marchent :
//		if ((typeof $scope.form.studyIdentifier  === 'undefined' || $scope.form.studyIdentifier   === null) &&
//			(typeof $scope.form.sampleIdentifier  === 'undefined' || $scope.form.sampleIdentifier  === null) &&
//		if	(typeof $scope.form.projCodes        === 'undefined' || $scope.form.projCodes        === null || $scope.form.projCodes.length <= 0 ) ) {
//	
		//NGL-3666
		// imposer de definir au moins un code projet car sinon requete ne passe pas :
//		if (toolsServices.isBlank($scope.form.projCodes)  ) {
//			//console.log("Pas de code projet");
		if (! $scope.form.projCodes             &&
			! $scope.form.codes                 &&
			! $scope.form.studyCodes            &&			
			! $scope.form.studyIdentifiers      &&
			! $scope.form.sampleCodes           &&
			! $scope.form.sampleIdentifiers     &&
			! $scope.form.submissionCodes       
			) {
			$scope.treeLoadInProgress = false;
			$scope.messages.setError("Ajouter un parametre de recherche"); 
			$scope.bilansDT = datatable(bilansDTConfig);
	    	$scope.bilansDT.setColumnsConfig(getBilanColumns());
			throw "Ajoutez un parametre de recherche";
		}
		
		$scope.search_experiment_analysis();

	}; // fin bouton $scope.search
	
		
	
	$scope.search_experiment_analysis = function() {
		var tab_experiments = [];
		var tab_analyzes    = [];
		var tab_bilans      = [];
		var readsets        = {}; 
		var studies         = {};
		var samples         = {};
		
//		console.log("Dans search_exp_readsets");
//		console.log("Dans search_exp_readsets, scope.form = " + $scope.form);
//		console.log("Dans search_exp_readsets, scope.form.studyIdentifier = " + $scope.form.studyIdentifier);
//		console.log("Dans search_exp_readsets, scope.form.sampleIdentifier = " + $scope.form.sampleIdentifier);
//		console.log("Dans search_exp_readsets, scope.form.projCodes = " + $scope.form.projCodes);
		$scope.form.pseudoStateCodes = ['pseudo_ebiKnown']; // liste des stateCodescorrespondant à donnees soumises
		//$scope.experimentsDT.search($scope.form); // ne pas utiliser car asynchrone et on est dans la suite 
													// du code avant la fin de la requete.
		var allRequestExp = [];
//		var request = $http.get(jsRoutes.controllers.sra.experiments.api.Experiments.list().url,{
//			params:{studyIdentifier:$scope.form.studyIdentifier, 
//				    sampleIdentifier:$scope.form.sampleIdentifier, 
//				    projCodes:$scope.form.projCodes,
//				    stateCode:"ebiKnown"}})  // pour avoir bilan des données vraimment soumises 

//		var request = $http.get(jsRoutes.controllers.sra.experiments.api.Experiments.list().url,{
//			params:{studyIdentifier:$scope.form.studyIdentifier, 
//				    sampleIdentifier:$scope.form.sampleIdentifier, 
//				    projCodes:$scope.form.projCodes,
//				    pseudoStateCodes:$scope.form.pseudoStateCodes}})  // pour avoir bilan des données vraimment soumises 

		var request = $http.get(jsRoutes.controllers.sra.experiments.api.Experiments.list().url,{params: $scope.form})
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
		.then(resultExp => { // resultat de l'ensemble des requetes, avec result[0]=resultat de la requete 1 
			              // avec tableau de data si la requete retourne une liste (url de la forme list) 
			              // et data si la requete retourne un seul resultat(url de la forme get)
//			console.log("apres req exp , result = " , result); 	
//			console.log("result[0] = " , result[0]); 	
//			console.log("result[1] = " , result[1]); 	
//			console.log("result[0].data = " , result[0].data); 
//			console.log("result[0].data[0] = " , result[0].data[0]); 
//			console.log("result[0].data[0].code = " , result[0].data[0].code); 
//			console.log("result[0].data[1].code = " , result[0].data[1].code); 
//			console.log("result[0].data[2].code = " , result[0].data[2].code); 
//			console.log("result[0].data[3].code = " , result[0].data[3].code); 
			

			
			// construction des requetes pour analysis :
			var allRequestAnalyzes = [];

//			var requestAnalyzes = $http.get(jsRoutes.controllers.sra.analyzes.api.Analyzes.list().url,{
//				params:{studyIdentifier:$scope.form.studyIdentifier, 
//				   	 	sampleIdentifier:$scope.form.sampleIdentifier, 
//				    	projCodes:$scope.form.projCodes,
//				    	pseudoStateCodes:$scope.form.pseudoStateCodes}})  // pour avoir bilan des données vraimment soumises 

			var requestAnalyzes = $http.get(jsRoutes.controllers.sra.analyzes.api.Analyzes.list().url,{params: $scope.form})
				.catch (error => {
				console.log(error);
				console.log(error.data);
				$scope.messages.addDetails(error.data); 
				$scope.treeLoadInProgress = false;
				return $q.reject(error);                // propage erreur, comme throw 
			});
		
			//		console.log("req =" + request);
			allRequestAnalyzes.push(requestAnalyzes);
			$q.all(allRequestAnalyzes)
			.then(resultAnalyzes => { 
				// construction des requetes pour study, samples et readset en fonction du resultat de la requete des exp et analyse:
				//console.log("resultAnalyzes=", resultAnalyzes);
				var allRequestReadset = [];
				var allRequestStudy   = [];
				var	allRequestSample  = [];
				$scope.buildRequests(resultExp, resultAnalyzes, allRequestReadset, allRequestStudy, allRequestSample, tab_experiments, tab_analyzes);
				var count_readsets = 0;
				
				// execution requete readset construction du hash des readsets :
				 $q.all(allRequestReadset)
				.then(result => { 
					for (var j = 0; j < result.length; j++) {
						for (var k = 0; k < result[j].data.length; k++) {
							var readsetCode = result[j].data[k].code;
							var readset = result[j].data[k];
							count_readsets++
							//console.log("readset Code avant integration dans hash : " , readset.code);
							//Object.assign(readsets, {readsetCode: readset}); // ne marche pas prendre ecriture suivante :	
							readsets[readsetCode] = readset;
						}
					}
//				console.log("Nombre de readsets recuperes = ", count_readsets);
				// execution requete study et construction du hash des studies :
				$q.all(allRequestStudy)
				.then(result => { 
					for (var j = 0; j < result.length; j++) {
						for (var k = 0; k < result[j].data.length; k++) {
							var study = result[j].data[k];
							var study_ac = study.accession;
							studies[study_ac] = study;
//							console.log("study avant integration dans hash : " , study.code);
//							console.log("study avant integration dans hash : " , study.externalId);
//							console.log("apres integration dans hash, study_ac=", study_ac);
//							console.log("apres integration dans hash, studies[study_ac].accession = " , studies[study_ac].accession);
//							console.log("apres integration dans hash, studies[study_ac].code = " , studies[study_ac].code);
//							console.log("apres integration dans hash, studies[study_ac].externalId = " , studies[study_ac].externalId);
						}
					}
					//  execution requete sample et construction du hash des samples :
					$q.all(allRequestSample)
					.then(result => { 
						for (var j = 0; j < result.length; j++) {
							for (var k = 0; k < result[j].data.length; k++) {
								var sample = result[j].data[k];
								var sample_ac = sample.accession;			
								samples[sample_ac] = sample;
//								console.log("sample avant integration dans hash : "  , sample.code);
//								console.log("sample avant integration dans hash : "  , sample.externalId);
//								console.log("apres integration dans hash, sample_ac=", sample_ac);
//								console.log("apres integration dans hash, samples[sample_ac].accession = "  , samples[sample_ac].accession);
//								console.log("apres integration dans hash, samples[sample_ac].code = "       , samples[sample_ac].code);
//								console.log("apres integration dans hash, samples[sample_ac].externalId = " , samples[sample_ac].externalId);
							}
						}
						
						// Construire tableau des objets bilan a partir des experiments :
						$scope.buildBilanFromExperiment(tab_experiments, readsets, studies, samples, tab_bilans);

						// Construire tableau des objets bilan a partir des rawDataBionano :
						$scope.buildBilanFromAnalyzes(tab_analyzes, readsets, studies, samples, tab_bilans);

//						console.log("tab_bilans.length = ", tab_bilans.length);

						// Attention initialisation de bilansDT deja faite à l'initialisation et pb si refait ici,
						// $scope.bilansDT est perdu !!!!
						// $scope.bilansDT = datatable(bilansDTConfig); 
						$scope.bilansDT.setData(tab_bilans, tab_bilans.length);	
//						console.log("Dans code requetes, tab_experiments.length = ", tab_experiments.length);
				 		$scope.treeLoadInProgress = false;
					}); // fin requetes samples
				}); // fin requetes studies
				}); // fin requetes readset	
				}); // fin requetes analyzes	
				});// fin requete experiment

		// Ne pas mettre de code apres la requete car executé avant la fin de la requete 
		
	}; // fin bouton $scope.search_exp_readsets
	
	
	$scope.buildRequests = function(resultExp, resultAnalyzes, allRequestReadset, allRequestStudy, allRequestSample, tab_experiments, tab_analyzes) {
		var tab_all_readsetCodes = []; // tableau provisoire pour construire requete
		var tab_all_studyAc      = []; // tableau provisoire pour construire requete
		var h_all_studyIdentifier= {}; // hash provisoire pour construire requete
		var tab_all_sampleAc     = []; // tableau provisoire pour construire requete
		var h_all_sampleIdentifier= {}; // hash provisoire pour construire requete

		var max = 100;             // maximum de donneés recherchées en une fois pour une requete.

		for (var i = 0; i < resultExp[0].data.length; i++) {
//			console.log("XXXXXXXXXXXX    result[0].data[i].readSetCode=" , result[0].data[i].readSetCode)
			tab_experiments.push(resultExp[0].data[i]);
			tab_all_readsetCodes.push(resultExp[0].data[i].readSetCode);
			if( ! h_all_studyIdentifier.hasOwnProperty(resultExp[0].data[i].studyIdentifier)) {
				h_all_studyIdentifier[resultExp[0].data[i].studyIdentifier] = resultExp[0].data[i].studyIdentifier;
			}
			if( ! h_all_sampleIdentifier.hasOwnProperty(resultExp[0].data[i].sampleIdentifier)) {
				h_all_sampleIdentifier[resultExp[0].data[i].sampleIdentifier] = resultExp[0].data[i].sampleIdentifier;
			}			
		}
		
		for (var i = 0; i < resultAnalyzes[0].data.length; i++) {
//			console.log("XXXXXXXXXXXX    result[0].data[i].readSetCode=" , result[0].data[i].readSetCode)
			var analysis = resultAnalyzes[0].data[i];
			tab_analyzes.push(analysis);
			if( ! h_all_studyIdentifier.hasOwnProperty(analysis.studyIdentifier)) {
				h_all_studyIdentifier[analysis.studyIdentifier] = analysis.studyIdentifier;
			}	
			if( ! h_all_sampleIdentifier.hasOwnProperty(analysis.sampleIdentifier)) {
				h_all_sampleIdentifier[analysis.sampleIdentifier] = analysis.sampleIdentifier;
			}			
			for (var j = 0; j < analysis.listRawData.length; j++) {
				if (toolsServices.isNotBlank(analysis.listRawData[j].readsetCode)) {
					tab_all_readsetCodes.push(analysis.listRawData[j].readsetCode);
				} 
			}
		}
		$scope.buildRequeteReadset(tab_all_readsetCodes, allRequestReadset, max);
		//$scope.buildRequeteStudyAc(tab_all_studyAc, allRequestStudy, max);
		$scope.buildRequeteStudyAc(Object.keys(h_all_studyIdentifier), allRequestStudy, max);
		$scope.buildRequeteSampleAc(Object.keys(h_all_sampleIdentifier), allRequestSample, max);

	}// end buildRequest
	
	
			
	$scope.buildRequeteReadset = function(tab_all_readsetCodes, allRequestReadset, max) {
		var tab_readsetCodes = []; // tableau provisoire pour construire requete

		for (var i=0; i < tab_all_readsetCodes.length; i++) {
			tab_readsetCodes.push(tab_all_readsetCodes[i]);
			if(tab_readsetCodes.length > max) {
				// construire requetes readsets
				// On n'utilise pas la requete get qui nous ferait une requete par experiment.
				//var request = $http.get(jsRoutes.controllers.readsets.api.ReadSets.get(experiment.readSetCode).url)
				//var requestReadset = $http.get(jsRoutes.controllers.readsets.api.ReadSets.list().url,{params:{codes:tab_readsetCodes}})
				var requestReadset = $http.get(jsRoutes.controllers.readsets.api.ReadSets.list().url,
				{params:{codes:tab_readsetCodes, includes:["code", "sampleCode", "sampleOnContainer.referenceCollab","properties.refCollabSub" ]}})
				.catch (error => {
					console.log(error);
					console.log(error.data);
					$scope.messages.addDetails(error.data); 
					return $q.reject(error);                // propage erreur, comme throw 
				});
				allRequestReadset.push(requestReadset);
				// Reinitialiser
				tab_readsetCodes = [];
			}
		}	
		// correction effet de bord pour req readset:
		if(tab_readsetCodes.length > 0) {
			//var requestReadset = $http.get(jsRoutes.controllers.readsets.api.ReadSets.list().url,
			//		{params:{codes:tab_readsetCodes}})
			var requestReadset = $http.get(jsRoutes.controllers.readsets.api.ReadSets.list().url,
					{params:{codes:tab_readsetCodes, includes:["code", "sampleCode", "sampleOnContainer.referenceCollab","properties.refCollabSub" ]}})

			.catch (error => {
				console.log(error);
				console.log(error.data);
				$scope.messages.addDetails(error.data); 
				$scope.treeLoadInProgress = false;
				return $q.reject(error);                // propage erreur, comme throw 
			});
			allRequestReadset.push(requestReadset);
	     } // end correction effet de bord	
	}// end buildRequeteReadset 	
	
	$scope.buildRequeteStudyAc = function(tab_all_studyAc, allRequestStudy, max) {
		var tab_studyAc      = []; // tableau provisoire pour construire requete

		for (var i=0; i < tab_all_studyAc.length; i++) {			
			// construire requetes study internes (les ExternalStudy ne nous interressent pas)
			//var requestStudy = $http.get(jsRoutes.controllers.sra.studies.api.Studies.list().url,
			//		{params:{accessions:tab_studyAc, type:"Study"}})
			//NGL-3666
			tab_studyAc.push(tab_all_studyAc[i]);
			if(tab_studyAc.length > max) {
				var requestStudy = $http.get(jsRoutes.controllers.sra.studies.api.Studies.list().url, {params:{identifiers:tab_studyAc}})
				.catch (error => {
					console.log(error);
					console.log(error.data);
					$scope.messages.addDetails(error.data); 
					$scope.treeLoadInProgress = false;
					return $q.reject(error);                // propage erreur, comme throw 
				});
				allRequestStudy.push(requestStudy);		
				// Reinitialiser
				tab_studyAc = [];
			}	
		} // end for					

		// correction effet de bord pour req studies:
		if (tab_studyAc.length > 0) {
			var requestStudy = $http.get(jsRoutes.controllers.sra.studies.api.Studies.list().url,
					{params:{identifiers:tab_studyAc}})
			.catch (error => {
				console.log(error);
				console.log(error.data);
				$scope.messages.addDetails(error.data); 
				$scope.treeLoadInProgress = false;
				return $q.reject(error);                // propage erreur, comme throw 
			});
			allRequestStudy.push(requestStudy);
	     } // end correction effet de bord
	} // end buildRequeteStudyAc
	
	
	$scope.buildRequeteSampleAc = function(tab_all_sampleAc, allRequestSample, max) {
		var tab_sampleAc      = []; // tableau provisoire pour construire requete
		
		for (var i=0; i < tab_all_sampleAc.length; i++) {			
			tab_sampleAc.push(tab_all_sampleAc[i]);	
			if(tab_sampleAc.length > max) {
				// construire requetes sample internes (les ExternalSample ne nous interressent pas)
				var requestSample = $http.get(jsRoutes.controllers.sra.samples.api.Samples.list().url, {params:{identifiers:tab_sampleAc}})
					.catch (error => {
						console.log(error);
						console.log(error.data);
						$scope.messages.addDetails(error.data); 
						$scope.treeLoadInProgress = false;
						return $q.reject(error);                // propage erreur, comme throw 
				});
				allRequestSample.push(requestSample);						
				
				
				// Reinitialiser
				tab_sampleAc = [];
			} 
		} // end for

		// correction effet de bord pour req samples:
		if(tab_sampleAc.length > 0) {
			var requestSample = $http.get(jsRoutes.controllers.sra.samples.api.Samples.list().url,
					{params:{identifiers:tab_sampleAc}})
			.catch (error => {
				console.log(error);
				console.log(error.data);
				$scope.messages.addDetails(error.data); 
				$scope.treeLoadInProgress = false;
				return $q.reject(error);                // propage erreur, comme throw 
			});
			allRequestSample.push(requestSample);
	     }	// end correction effet de bord
	} // end buildRequeteSampleAc	
	
	
	$scope.buildBilanFromExperiment	= function (tab_experiments, readsets, studies, samples, tab_bilans) {
		// Construire tableau des objets bilan a partir des experiments :
		for (var j = 0; j < tab_experiments.length; j++) {	
			var exp = tab_experiments[j];
			var date = tab_experiments[j].traceInformation.creationDate;
			var readsetCode     = tab_experiments[j].readSetCode;
			var studyIdentifier  = tab_experiments[j].studyIdentifier;
			var sampleIdentifier = tab_experiments[j].sampleIdentifier;
						
			//console.log("construction tableau bilans, experiment.sampleIdentifier = ", tab_experiments[j].sampleIdentifier);
							

			for(var k = 0; k < exp.run.listRawData.length; k++) {
				var echantillon      = "-----";
				var refCollabSub     = "-----";
				var studyExternalId  = "-----";
				var studyAC          = "-----";
				var sampleExternalId = "-----";
				var sampleAC         = "-----";
				var analysisAc       = "-----";
				
				if(readsets.hasOwnProperty(readsetCode)) {
					var readset = readsets[readsetCode];
//					console.log("readsetCode recuperé du hash = " , readset);
					echantillon = readset.sampleCode;
					//console.log("echantillon = " , echantillon);
					refCollabSub = readset.sampleOnContainer.referenceCollab;
					//console.log("refCollabSub = " , refCollabSub);
					if (toolsServices.isNotBlank(readset.properties) &&
						toolsServices.isNotBlank(readset.properties.refCollabSub) &&
						toolsServices.isNotBlank(readset.properties.refCollabSub.value)) {
						refCollabSub = readset.properties.refCollabSub.value;
//							console.log("HOURRAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", readset);
					}
				}
				if(studies.hasOwnProperty(studyIdentifier)) {
//					console.log("study trouve pour "                , studyIdentifier);
//					console.log("studies[studyIdentifier] "          ,  studies[studyAccession]);
//					console.log("studies[studyIdentifier].code "     , studies[studyAccession].code);
//					console.log("studies[studyIdentifier].externalId", studies[studyAccession].externalId);
					studyExternalId = studies[studyIdentifier].externalId;
					studyAC = studies[studyIdentifier].accession;
				}
				if(samples.hasOwnProperty(sampleIdentifier)) {
//					console.log("sample trouve pour " , sampleIdentifier);
//					console.log("sampleSame "         , samples[sampleIdentifier].externalId);
					sampleExternalId = samples[sampleIdentifier].externalId;
					sampleAC = samples[sampleIdentifier].accession;

				}
								
				var bilan = { rawData             : exp.run.listRawData[k].relatifName,
							  collabFileName      : exp.run.listRawData[k].collabFileName,
							  firstSubmissionDate : exp.firstSubmissionDate,
							  submissionCreationDate  : exp.traceInformation.creationDate,
							  submissionCreationUser  : exp.traceInformation.createUser,
							  readSetCode     : readsetCode,
							  echantillon     : echantillon,
							  refCollabSub    : refCollabSub,
							  typePlatform    : exp.typePlatform,
							  instrumentModel : exp.instrumentModel,
							  analysisAC      : analysisAc,
							  experimentAC    : exp.accession,
							  runAC           : exp.run.accession,
							  studyAC         : studyAC,
							  studyExternalId : studyExternalId,
							  sampleAC        : sampleAC,
							  sampleExternalId: sampleExternalId
							};
				 tab_bilans.push(bilan);			  
			} // end for rawData
		} // end form exp
		return tab_bilans;
	}// end buildBilanFromExperiment



	
	$scope.buildBilanFromAnalyzes = function (tab_analyzes, readsets, studies, samples, tab_bilans) {
		// Construire tableau des objets bilan a partir des analyzes :
		for (var j = 0; j < tab_analyzes.length; j++) {	
			var analysis     = tab_analyzes[j];
			var creationDate = analysis.traceInformation.creationDate;
			var creationUser = analysis.traceInformation.createUser;
			var firstSubmissionDate = analysis.firstSubmissionDate;
			var studyAc             = analysis.studyAccession;
			var sampleAc            = analysis.sampleAccession;
			var analysisAc          = analysis.accession;			
			//console.log("construction tableau bilans, analysis.sampleAccession = ", analysis.sampleAccession);
							

			
			for(var k = 0; k < analysis.listRawData.length; k++) {
				var echantillon      = "-----";
				var refCollabSub     = "-----";
				var studyExternalId  = "-----";
				var sampleExternalId = "-----";
				var experimentAc     = "-----";
				var runAc            = "-----";
				var typePlatform     = "-----";
				var instrumentModel  = "-----";
			
				var rawData = analysis.listRawData[k];
				var readsetCode = rawData.readsetCode;
				//console.log("readsetCode=", readsetCode);
				if(toolsServices.isNotBlank(readsetCode) && readsets.hasOwnProperty(readsetCode)) {				
					var readset = readsets[readsetCode];
					//console.log("readset bien trouve=", readset.code);
//					if (readSet.typeCode.toLowerCase() === "rsillumina") {
//						typePlatform = "Illumina";
//					} else if (readSet.typeCode.toLowerCase() === "rsnanopore" ){
//						typePlatform = "oxford_nanopore";
//					} else if ()
//						typePlatform = "Bionano";
//					}
					typePlatform = "Bionano";
//					console.log("readsetCode recuperé du hash = " , readset);
					instrumentModel = rawData.instrumentUsedTypeCode;
					echantillon = readset.sampleCode;
					//console.log("echantillon = " , echantillon);
					refCollabSub = readset.sampleOnContainer.referenceCollab;
					//console.log("refCollabSub = " , refCollabSub);
					
					if (toolsServices.isNotBlank(readset.properties) &&
						toolsServices.isNotBlank(readset.properties.refCollabSub) &&
						toolsServices.isNotBlank(readset.properties.refCollabSub.value)) {
						refCollabSub = readset.properties.refCollabSub.value;
//							console.log("HOURRAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", readset);
					}
				}
				if(studies.hasOwnProperty(studyAc)) {
//					console.log("study trouve pour "         , studyAc);
//					console.log("studies[studyAc] "          ,  studies[studyAc]);
//					console.log("studies[studyAc].code "     , studies[studyAc].code);
//					console.log("studies[studyAc].externalId", studies[studyAc].externalId);
					studyExternalId = studies[studyAc].externalId;
				}
				if(samples.hasOwnProperty(sampleAc)) {
//					console.log("sample trouve pour " , sampleAc);
//					console.log("sampleSame "         , samples[sampleAc].externalId);
					sampleExternalId = samples[sampleAc].externalId;
				}
								
				var bilan = { rawData                 : rawData.relatifName,
							  collabFileName          : rawData.collabFileName,
							  firstSubmissionDate     : firstSubmissionDate,
							  submissionCreationDate  : creationDate,
							  submissionCreationUser  : creationUser,
							  readSetCode             : readsetCode,
							  echantillon             : echantillon,
							  refCollabSub            : refCollabSub,
							  typePlatform            : typePlatform,
							  instrumentModel         : instrumentModel,
							  analysisAC              : analysisAc,
							  experimentAC            : experimentAc,
							  runAC                   : runAc,
							  studyAC                 : studyAc,
							  studyExternalId         : studyExternalId,
							  sampleAC                : sampleAc,
							  sampleExternalId        : sampleExternalId
							};
				 tab_bilans.push(bilan);	
					  
			} // end for rawData
			//console.log("tab_bilans_bionano = ",tab_bilans);
		} // end form exp
		return tab_bilans;
	}// end buildBilanFromAnalysis
				
}]);
