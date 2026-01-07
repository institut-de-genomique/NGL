"use strict";

angular.module('home').controller('DetailsCtrl',[ '$http', '$scope', '$routeParams' , 'mainService', 'lists', 'tabService','messages', 'toolsServices', 'datatable',
                                                  function($http, $scope, $routeParams, mainService, lists, tabService, messages, toolsServices, datatable) { 



	
	var analyzesDTConfig = {
			name:'analysisDT',
			order :{by:'code',mode:'local', reverse:true},
			search:{active:false},
			pagination:{
				active:true,
				mode:'local'
			},
			select:{active:false, showButton:false},
			showTotalNumberRecords:true,
			edit:{
				active:true,
				showButton : false,
				withoutSelect : true,
				columnMode : true,
				lineMode:function(line){
					return true;
				},
			},
			save : {
				active:true,
				mode:'local',
				showButton : false,
				changeClass : false,
				url:function(lineValue){
					return jsRoutes.controllers.sra.analyzes.api.Analyzes.update(lineValue.code).url; // jamais utilisé en mode local
				},
				method:'put',
				value:function(line){
					return line;
				},
			},
			cancel : {
				showButton:false
			},
			hide:{
				active:false,
				showButton:false
			},
			exportCSV:{
				active:true
			},
		
			columns : [
			        {property:"traceInformation.creationDate",
			        	header: Messages("experiment.traceInformation.creationDate"),
			        	type :"date",		    	  	
			        	order:false
			        },
 					{property:"code",
			        	header: Messages("analysis.code"),
			        	type :"text",		    	  	
			        	order:false
			        },			        
			        {property:"accession",
			        	header: Messages("analysis.accession"),
			        	type :"text",		    	  	
			        	order:false
			        },	
			        {property:"projectCode",
			        	header: Messages("analysis.projectCode"),
			        	type :"text",		    	  	
			        	order:false,
			        	edit:false,
			        	choiceInList:false  
			        },
			        {property:"title",
						header: Messages("analysis.title"),
						type :"text",	
						hide:true,
						order:true,
						edit:true,  
						editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
						choiceInList:false
					},
					{property:"description",
						header: Messages("analysis.description"),
						type :"text",	
						hide:true,
						order:true,
						edit:true,  
						editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
						choiceInList:false
					},
					{property:"studyIdentifier",
						header: Messages("analysis.studyIdentifier"),
						type :"String",		    	  	
						hide:true,
						edit:true,
						order:false
					},					
					{property:"sampleIdentifier",
						header: Messages("analysis.sampleIdentifier"),
						type :"String",		    	  	
						hide:true,
						edit:true,
						order:false
					},	
					{property:"state.code",
					      "filter":"codes:'state'",
			        	  header: Messages("experiment.state"),
			        	  type :"text",		    	  	
			        	  order:true,
			        	  edit:false,
			        	  choiceInList:false
			        }
			 ]	        
	};
	
	

	var samplesDTConfig = {
			name:'sampleDT',
			//order :{by:'code',mode:'local', reverse:true},
			order :{by:'code',mode:'local', reverse:true},
			search:{active:false},
			select:{active:false},
			pagination:{
				active:false,
				mode:'local'
			},
			showTotalNumberRecords:true,
			edit : {
				active:true,
				showButton : false,
				withoutSelect : true,
				columnMode : true,
				lineMode : function(line){
					if(line.state.code === "SUB-N")
						return true;
					else 
						return false;
				}
			},
			save : {
				active:true,
				// important de mettre en mode local pour rafraichissement de la page, mais sauvegarde globale via bouton
				mode:'local',
				showButton : false,
				changeClass : false,				
				url:function(line){
					return jsRoutes.controllers.sra.samples.api.Samples.update(line.code).url; // jamais utilisé en mode local
				},
				method:'put',
				value:function(line){
					return line;
				},
			},

			/*cancel : {
				showButton:true
			},
			hide:{
				active:true
			},
			 */
			exportCSV:{
				active:true
			},
			

			columns : [
			 	{property:"traceInformation.creationDate",
			       	header: Messages("sample.traceInformation.creationDate"),
			       	type :"date",		    	  	
			       	order:true
			    },
			    {property:"traceInformation.createUser",
			       	header: Messages("traceInformation.creationUser"),
			       	type :"text",		    	  	
			       	order:true
			    },
				{property:"state.code",
					"filter":"codes:'state'", 
					header: Messages("state"),
					type :"text",		    	  	
					order:true,
					edit:false
				},
				{property:"code",
					header: Messages("sample.code"),
					type :"text",		    	  	
					order:true
				},
				{property:"accession",
					header: Messages("sample.accession"),
					type :"text",
					edit :false,
					order:true
				},
				{property:"externalId",
					header: Messages("sample.externalId"),
					type :"text",	
					edit :false,
					order:true
				},				
				{property:"projectCode",
					header: Messages("sample.projectCode"),
					type :"text",		    	  	
					order:true,
					edit :false           
				},
				{property:"anonymizedName",
					header: Messages("sample.anonymizedName"),
					type :"text",	
					hide:true,
					order:true,
					edit:true,
					editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
					choiceInList:false
				},
				{property:"title",
					header: Messages("sample.title"),
					type :"text",	
					hide:true,
					order:true,
					edit:true,
					editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
					choiceInList:false
				},
				{property:"description",
					header: Messages("sample.description"),
					type :"text",	
					hide:true,
					order:true,
					edit:true,
					editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
					choiceInList:false
				},				
				{property:"taxonId",
					header: Messages("sample.taxonId"),
					type :"int",
					hide:true,
					order:true,
					edit:false,
					choiceInList:false
				},
				/*{property:"commonName",
					header: Messages("sample.commonName"),
					type :"text",		    	  	
					hide:true,
					order:true,
					edit:false,
					choiceInList:false
				},*/
				{property:"scientificName",
					header: Messages("sample.scientificName"),
					type :"text",		    	  	
					order:true,
					edit:false,
					choiceInList:false
				},
				{property:"attributes",
					header: Messages("sample.attributes"),
					//"filter":"codes:'state'",
					type :"text",		    	  	
					order:true,
					edit:true,
					editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
					choiceInList:false
				},
			    {property     : "comments",
					header       : Messages("comments"),
					type         : "text",
					hide         : true,
					edit         : false, // pas modifiable en consultation car uniquement dans vue detail
					order        : false,
				    choiceInList : false,
					filter       : "collect:'comment'",
					listStyle    : 'bt-select',
				}
				]				
	};

		
	var experimentsDTConfig = {
			name:'experimentDT',
			order :{by:'code',mode:'local', reverse:true},
			search:{active:false},
			pagination:{
				active:true,
				mode:'local'
			},
			select:{active:false, showButton:false},
			showTotalNumberRecords:true,
			edit:{
				active:true,
				showButton : false,
				withoutSelect : true,
				columnMode : true,
				lineMode:function(line){
					return true;
				},
			},
			save : {
				active:true,
				mode:'local',
				showButton : false,
				changeClass : false,
				url:function(lineValue){
					return jsRoutes.controllers.sra.experiments.api.Experiments.update(lineValue.code).url; // jamais utilisé en mode local
				},
				method:'put',
				value:function(line){
					return line;
				},
			},
			cancel : {
				showButton:false
			},
			hide:{
				active:false,
				showButton:false
			},
			exportCSV:{
				active:true
			},
		
			columns : [
			        {property:"traceInformation.creationDate",
			        	header: Messages("experiment.traceInformation.creationDate"),
			        	type :"date",		    	  	
			        	order:false
			        },
 					{property:"code",
			        	header: Messages("experiment.code"),
			        	type :"text",		    	  	
			        	order:false
			        },			        
			        {property:"accession",
			        	header: Messages("experiment.accession"),
			        	type :"text",		    	  	
			        	order:false
			        },	
			        {property:"projectCode",
			        	header: Messages("experiment.projectCode"),
			        	type :"text",		    	  	
			        	order:false,
			        	edit:false,
			        	choiceInList:false  
			        },
			        {property:"title",
						header: Messages("experiment.title"),
						type :"text",	
						hide:true,
						order:true,
						edit:true,  
						editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
						choiceInList:false
					},
			        {property:"librarySelection",
						header: Messages("experiment.librarySelection"),
						type :"String",
			        	hide:true,
			        	edit:true,
						order:false,
				    	choiceInList:true,
				    	//listStyle:'bt-select',
				    	possibleValues:'sraVariables.librarySelection',
				    },
				    {property:"libraryStrategy",
						header: Messages("experiment.libraryStrategy"),
						type :"String",
						hide:true,
						edit:true,
						order:false,
						choiceInList:true,
						//listStyle:'bt-select',
						possibleValues:'sraVariables.libraryStrategy',
				    },
					{property:"librarySource",
						header: Messages("experiment.librarySource"),
						type :"String",
						hide:true,
						edit:true,
						order:false,
						choiceInList:true,
						//listStyle:'bt-select',
						possibleValues:'sraVariables.librarySource',
					},
					{property:"libraryLayout",
						header: Messages("experiment.libraryLayout"),
						type :"String",
						hide:true,
						edit:false,
						order:false,
						choiceInList:true,
						//listStyle:'bt-select',
						possibleValues:'sraVariables.libraryLayout',
					},	
					{property:"libraryLayoutNominalLength",
			        	header: Messages("experiment.libraryLayoutNominalLength"),
			        	type :"integer",		    	  	
			        	hide:true,
						edit:true,
			        	order:false
					},	
					{property:"libraryLayoutOrientation",
						header: Messages("experiment.libraryLayoutOrientation"),
						type :"String",
						hide:true,
//						edit:false,
//						order:false,
//						choiceInList:true,
//						//listStyle:'bt-select',
//						possibleValues:'sraVariables.libraryLayoutOrientation',
					},	
					{property:"libraryName",
						header: Messages("experiment.libraryName"),
						type :"text",	
						hide:true,
						order:true,
						edit:true,  
						editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
						choiceInList:false
					},
					{property:"libraryConstructionProtocol",
						 header: Messages("experiment.libraryConstructionProtocol"),
						type :"text",	
						hide:true,
						order:true,
						edit:true,  
						editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
						choiceInList:false
					},
					{property:"typePlatform",
						 header: Messages("experiment.typePlatform"),
						 type :"String",		    	  	
						 hide:true,
						 edit:false,
						 order:true
					},
					{property:"instrumentModel",
						 header: Messages("experiment.instrumentModel"),
						 type :"String",		    	  	
						 hide:true,
						 edit:true,
						 order:true
					},
					{property:"lastBaseCoord",
			        	header: Messages("experiment.lastBaseCoord"),
			        	type :"integer",		    	  	
			        	hide:true,
						edit:true,
			        	order:false
					},	
					{property:"spotLength",
			        	header: Messages("experiment.spotLength"),
			        	type :"Long",		    	  	
			        	hide:true,
						edit:true,
			        	order:false
					},	
					{property:"sampleCode",
			        	header: Messages("experiment.sampleCode"),
			        	type :"String",		    	  	
			        	hide:true,
						edit:false,
			        	order:true
					},	
					{property:"studyCode",
			        	header: Messages("experiment.studyCode"),
			        	type :"String",		    	  	
			        	hide:true,
						edit:false,
			        	order:true
					},
					 {property:"state.code",
					      "filter":"codes:'state'",
			        	  header: Messages("experiment.state"),
			        	  type :"text",		    	  	
			        	  order:true,
			        	  edit:false,
			        	  choiceInList:false
			        }
			 ]	        
	};
	var runsDTConfig = {
			name:'runDT',
			order :{by:'code',mode:'local', reverse:true},
			pagination:{
				active:true,
				mode:'local'
			},			
			showTotalNumberRecords:true,
			select:{active:false, showButton:false},
			edit : {
				active:false,
				showButton : false,
				withoutSelect : true,
				columnMode : true
			},
			cancel : {
				showButton:false
			},
			hide:{
				active:false,
				showButton:false
			},
			exportCSV:{
				active:true
			},
			columns : [ 
					  {property:"traceInformation.creationDate",
			        	header: Messages("run.traceInformation.creationDate"),
			        	type :"date",		    	  	
			        	order:false
			           },
			           {property:"run.code",
			        	header: Messages("run.code"),
			        	type :"text",		    	  	
			        	order:true
			           },
			           {property:"run.accession",
			        	header: Messages("run.accession"),
			        	type :"text",		    	  	
			        	order:true
			           },
			           {property:"code",
				        	header: Messages("run.experiment.code"),
				        	type :"text",		    	  	
				        	order:true
				       },
			           {property:"run.runDate",
				        header: Messages("run.date"),
				       	type :"date",
				       	order:true
				       }
			          
		    ]	        
		};
		
	var rawDatasDTConfig = {
			name:'rawDataDT',
			order :{by:'code',mode:'local', reverse:true},
			search:{active:false},
			pagination:{
				active:true,
				mode:'local'
			},
			select:{active:false, showButton:false},
			cancel : {
				active:false, showButton:false
			},
			hide:{
				active:false,
				showButton:false
			},
			select:{active:false},
			showTotalNumberRecords:true,
			edit : {
				active:false,
				showButton : false,
				withoutSelect : true,
				columnMode : true
			},
			cancel:{
				showButton:false
			},
			exportCSV:{
				active:true
			},
			columns : [
			          { property:"relatifName",
						header: Messages("rawData.relatifName"),
						type :"text",		    	  	
						order:true
					  },
					  { property:"collabFileName",
						header: Messages("rawData.collabFileName"),
						type :"text",		    	  	
						order:true
					  }
		    		  ]	        
		};	

	
	
	
	
	
	
	
	// si on declare dans services => var sraVariables = {};
	// si on declare dans le controlleur :
	
	
	var init = function(userName){
		$scope.mainService = mainService;
		$scope.treeLoadInProgress = false;
		$scope.messages = messages();
		$scope.messages.clear(); 
		$scope.countAnalysis = 0;
		$scope.countSample = 0;
		$scope.countExperiment = 0;
		$scope.isPublic=false;
		$scope.visibility="undefined";
		$scope.isReleasable = function(userName) { return false; }; // fonction qui renvoie toujours false
		// si on souhaite affichage bouton si on vient du menu release :
		//$scope.release=$scope.mainService.get("release");
		
       // console.log("$routeParams.code:"+$routeParams.code);

		// Recuperer le study en passant par list 
		//$http.get(jsRoutes.controllers.sra.studies.api.Studies.list().url,  {params: {code:$routeParams.code}}).success(function(data){
		// en ajoutant $code dans studySearchForm.
		
		console.log("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		// Recuperer le study en passant par get :		
		$http.get(jsRoutes.controllers.sra.studies.api.Studies.get($routeParams.code).url).success(function(data){
			//console.log("nbre de study:"+data.length);
	
			$scope.study = data;
			//console.log("study.accession=", $scope.study.accession);
			$scope.strLocusTagPrefixs = "";
			//console.log("$scope.study.locusTagPrefixs", $scope.study.locusTagPrefixs);
			//console.log("typeOf $scope.study.locusTagPrefixs", typeof $scope.study.locusTagPrefixs);

			if ($scope.study.locusTagPrefixs != null) {
				for (var j = 0; j < $scope.study.locusTagPrefixs.length; j++) {
					//console.log("XXX  locusTag=", $scope.study.locusTagPrefixs[j]);
					$scope.strLocusTagPrefixs = $scope.strLocusTagPrefixs + $scope.study.locusTagPrefixs[j] + "," ;
				}
				$scope.strLocusTagPrefixs = $scope.strLocusTagPrefixs.replace(/,\s*$/, "");
			}
		
			$scope.strIdsPubmed = "";
			//console.log("$scope.study.idsPubmed", $scope.study.idsPubmed);
			//console.log("typeOf $scope.study.idsPubmed", typeof $scope.study.idsPubmed);

			if ($scope.study.idsPubmed != null) {
				for (var j = 0; j < $scope.study.idsPubmed.length; j++) {
					//console.log("XXX  idPubmed=", $scope.study.idsPubmed[j]);
					$scope.strIdsPubmed = $scope.strIdsPubmed + $scope.study.idsPubmed[j] + "," ;
				}
				$scope.strIdsPubmed = $scope.strIdsPubmed.replace(/,\s*$/, "");
			}
		
			// Ajout des onglets à gauche si rafraichissement page
			if(tabService.getTabs().length == 0){			
				//tabService.addTabs({label:Messages('studies.menu.consultation'),href:jsRoutes.controllers.sra.studies.tpl.Studies.home("consultation").url,remove:true});
				tabService.addTabs({label:$scope.study.code,href:jsRoutes.controllers.sra.studies.tpl.Studies.get($scope.study.code).url,remove:true});
				tabService.activeTab($scope.getTabs(0)); // active l'onglet indiqué, le met en bleu.
			}
		
			// On compare les date :
			// $scope.study.releaseDate correspond à la date de release exprimée en milliseconde ecoulées depuis 01/01/1970 (Date Epoch) 
			// Date.now() correspond à la date courante exprimee en ms depuis Epoch

		
			if($scope.study.consolidatedPublicStatus || $scope.study._type=="ExternalStudy") {
				$scope.visibility="public";	
			} else {		
				if ( !$scope.study.releaseDate) {
					//$scope.isReleasable = function(userName){return false;}; // isReleasable est une fonction
					//console.log("study.releaseDate non renseigné");
					$scope.visibility="undefined";
				} else {		
					//console.log("study.releaseDate" + $scope.study.releaseDate);
					//console.log("Date.now" + Date.now());
					if ($scope.study.releaseDate < Date.now()) {
						$scope.isPublic=true;
						$scope.visibility="public";	
					} else {
						$scope.visibility="private";	
						if ($scope.study.state.code == "SUB-F") {
							//$scope.isReleasable = function(userName){return $scope.study.traceInformation.createUser==userName;};
							$scope.isReleasable = function(userName){return true;};
							//console.log("donnée confidentielle");
							//console.log("isReleasable: " + $scope.isReleasable);
						}
					}
				} 
			}
			//console.log("scope.visibility " + $scope.visibility);
		
			/* Interressant pour le formatage :
			console.log("releaseDate="+$scope.study.releaseDate);
			console.log("releaseDate="+ moment($scope.study.releaseDate).calendar());
			console.log("date.now="+ Date.now());
			console.log("releaseDate.endOf="+ moment($scope.study.releaseDate).endOf('day'));
			console.log("releaseDate.fromNow="+ moment($scope.study.releaseDate).fromNow());
			console.log("releaseDate.endOf.fromNow="+ moment($scope.study.releaseDate).endOf('day').fromNow());
			*/
		
		
			// recuperation des analyses avec studyIdentifier:
		   	$scope.studyIdentifier= "NONE";
		   	if (toolsServices.isNotBlank($scope.study.accession)) {
				$scope.studyIdentifier = $scope.study.accession;
		   	}
			$http.get(jsRoutes.controllers.sra.analyzes.api.Analyzes.list().url, {params: {studyIdentifier:$scope.studyIdentifier}}).success(function(data) {
				//console.log("study.accession=", $scope.study.accession);
				$scope.analyzes = data;
				$scope.countAnalysis = $scope.analyzes.length;
				
				//console.log("analysis liés au study ", $scope.analyzes);
				//Init datatable
				$scope.analysisDT = datatable(analyzesDTConfig);
				$scope.analysisDT.setData($scope.analyzes, $scope.analyzes.length);
				
				var maListRawDatas = [];
				var maListSampleIdentifiers = [];
				for (var i=0; i<$scope.analyzes.length; i++) {
					if (toolsServices.isNotBlank($scope.analyzes[i].sampleAccession)) {
						if ( ! maListSampleIdentifiers.includes($scope.analyzes[i].sampleAccession)) {
							maListSampleIdentifiers.push($scope.analyzes[i].sampleAccession);
						}
					}
					if($scope.analyzes[i].listRawData != null) {
						for (var j=0; j<$scope.analyzes[i].listRawData.length; j++) {
							if ( ! maListRawDatas.includes($scope.analyzes[i].listRawData[j])) {
								maListRawDatas.push($scope.analyzes[i].listRawData[j]);
							}
						}
					}
				}
									
				//Get experiments (and runs)
				$http.get(jsRoutes.controllers.sra.experiments.api.Experiments.list().url, {params: {studyCode:$routeParams.code}}).success(function(data) {
					//console.log("$routeParams.code:"+$routeParams.code);
					var maListSampleCodes = [];
					$scope.experiments = data;
					$scope.countExperiment = $scope.experiments.length;
					//Init datatable
					$scope.experimentDT = datatable(experimentsDTConfig);
					$scope.experimentDT.setData($scope.experiments, $scope.experiments.length);
					//Get Runs
					$scope.runDT = datatable(runsDTConfig);
					// Comme on a un seul run par experiment, on n'a pas besoin de boucler pour recuperer les données :
					$scope.runDT.setData($scope.experiments, $scope.experiments.length);
					
					
					// Get RawDatas : construction de la liste des rawData puis injection dans datatable :
					for (var i=0; i<$scope.experiments.length; i++) {
						if ( ! maListSampleIdentifiers.includes($scope.experiments[i].sampleAccession)) {
							maListSampleIdentifiers.push($scope.experiments[i].sampleAccession);
						}
						if ( ! maListSampleCodes.includes($scope.experiments[i].sampleCode)) {
							maListSampleCodes.push($scope.experiments[i].sampleCode);
						}
						var run = $scope.experiments[i].run;
						for (var j=0; j<run.listRawData.length; j++) {
							maListRawDatas.push(run.listRawData[j]);
						}
					}
					$scope.rawDataDT = datatable(rawDatasDTConfig);
					$scope.rawDataDT.setData(maListRawDatas, maListRawDatas.length);
					// Get samples from identifiers
					if(maListSampleIdentifiers.length==0){
						//console.log("Aucun sampleIdentifiers à rechercher");
						maListSampleIdentifiers.push("NONE"); // pour avoir une requete qui renvoie bien 0 resultats
					} 
					if(maListSampleCodes.length==0){
						//console.log("Aucun sampleCodes à rechercher");
						maListSampleCodes.push("NONE"); // pour avoir une requete qui renvoie bien 0 resultats
					}
										
					$scope.samplesFromIdentifiers = [];
					$http.get(jsRoutes.controllers.sra.samples.api.Samples.list().url, {params: {identifiers:maListSampleIdentifiers}}).success(function(data) {
					//$http.get(jsRoutes.controllers.sra.samples.api.Samples.list().url, {params: {accession:"ERS2467118"}}).success(function(data) {
						$scope.samplesFromIdentifiers = data;
						//console.log("XXXX    XXXXXXXXXXXXXX    samplesFromIdentifiers", $scope.samplesFromIdentifiers);
					
						// Get samples from codes
						$http.get(jsRoutes.controllers.sra.samples.api.Samples.list().url, {params: {codes:maListSampleCodes}}).success(function(data) {
							$scope.samplesFromCodes = data;	
							//console.log("XXXXXXXXXXXXXX    XXXXXXXXXXXXX   samplesFromCodes", $scope.samplesFromCodes);

							// construire liste unique de samples (Dans la base il n'existe pas de sample sans code, en revanche il existe des samples sans AC si pas encore soumis)
							$scope.samples = [];
							var dbSampleCodes = {}; //map des dbSampleCodes
							// construire une liste unique des samples à partir des resultats renvoyees par les 2 requetes :
							for(var i= 0; i < $scope.samplesFromIdentifiers.length; i++) {
								var sample = $scope.samplesFromIdentifiers[i];
								//console.log("ZZZZZZZZZZZZZZZZZ     sampleCode", sample.code);
								dbSampleCodes[sample.code] = "ok";
								$scope.samples.push(sample);
							}
							for(var i= 0; i < $scope.samplesFromCodes.length; i++) {
								var sample = $scope.samplesFromCodes[i];
								//console.log("ZZZZZZZZZZZZZZZZZ     sampleCode", sample.code);
								if(! dbSampleCodes.hasOwnProperty(sample.code)) {
									dbSampleCodes[sample.code] = "ok";
									$scope.samples.push(sample);
								}
							}
							//console.log("uniq samples", $scope.samples);					
							$scope.sampleDT = datatable(samplesDTConfig);
							$scope.sampleDT.setData($scope.samples, $scope.samples.length);
						});
					});	
				});	
			});
	    });		
	}; 

	init();
		
	/* buttons section */
	
	$scope.userRelease = function(){
		$scope.treeLoadInProgress = true;
		$scope.messages = messages();
		$scope.messages.clear();
		//console.log("je suis dans le bouton studies.details-ctrl.js.userRelease");
               
		 $http.put(jsRoutes.controllers.sra.submissions.api.Submissions.createFromStudyRelease($scope.study.code).url)
			.success(function(data){	
				var submission = data;	
		        $scope.treeLoadInProgress = false;
				$scope.messages = messages();
				$scope.messages.clear();
				$scope.messages.clazz="alert alert-success";
				$scope.messages.text="Le study "+ $scope.study.code + " a bien été mis dans le domaine public par l'EBI via la soumission "  + submission.code + " voir ebiReport " + submission.submissionDirectory + "/" + submission.ebiResult;
				$scope.messages.open();
			})
			.error(function(data){
				$scope.treeLoadInProgress = false;
				$scope.messages.addDetails(data);
				$scope.messages.setError("Probleme pour la release à l'EBI du study");	
			});	
	};
	
	$scope.cancel = function(){
		//console.log("call cancel");
		$scope.messages = messages();
		$scope.messages.clear();
		$scope.sampleDT.cancel();
		$scope.experimentDT.cancel();
		$scope.mainService.stopEditMode();		
	};
	
	$scope.activeEditMode = function(){
		$scope.messages = messages();
		$scope.messages.clear();
		$scope.mainService.startEditMode();
		$scope.sampleDT.setEdit();
		$scope.experimentDT.setEdit();
	};
	
	$scope.isActiveAnalysis = function() {  
		var value = false;
		if ($scope.countAnalysis > 0) {
			value = true;
		}
		//console.log("isActiveAnalysis", value);
		return value;
	};
	
	$scope.isActiveExperiment = function() {  
		var value = false;
		if ($scope.countExperiment > 0 
				&& ! $scope.countAnalysis > 0) {
			value = true;
		}
		//console.log("isActiveExperiment", value);
		return value;
	};
	
	$scope.isDataNotActiveExperiment = function() {  
		var value = false;
		if ($scope.countExperiment > 0 
			&& $scope.countAnalysis > 0 ) {
			value = true;
		}
		//console.log("isDataNotActiveExperiment", value);
		return value;
	};
	
	$scope.isObjectLinks = function() {
		var value = false;
		if ($scope.countExperiment > 0 || $scope.countAnalysis > 0) {
			value = true;
		}
		return value;
	};
	
	// methode utilisée dans le controller CommentCtrl
	$scope.isCreationMode=function() {
		//console.log("Dans isCreationMode");
		return false; // Dans la vue details, on n'est jamais en mode creation d'un study, le study existe bien en base.
	};
	
}]).controller('CommentsCtrl',['$scope','$sce', '$http','lists','$parse','$filter','datatable', 
                               function($scope,$sce,$http,lists,$parse,$filter,datatable) {

	$scope.currentComment = {comment:undefined};
	//console.log("Dans CommentsCtrl, scope.study=", $scope.study);
	$scope.analyseText = function(e){
		
		if(e.keyCode === 9){
			e.preventDefault();
		}
	};
	
	$scope.convertToBr = function(text){
		return $sce.trustAsHtml(text.replace(/\n/g, "<br>"));
	};
	
	$scope.cancel = function(){	
		$scope.currentComment = {comment:undefined};
		$scope.index = undefined;
	};
	
	$scope.save = function(){	
		//console.log("Dans le save de CommentsCtrl, scope.study = ", $scope.study );
		//console.log("Dans le save de CommentsCtrl, scope.currentComment = ", $scope.currentComment );

		if($scope.isCreationMode()){
			$scope.study.comments.push($scope.currentComment);
			$scope.currentComment = {comment:undefined};
		}else{
			$scope.messages.clear();
			$http.post(jsRoutes.controllers.sra.studies.api.StudyComments.save($scope.study.code).url, $scope.currentComment)
			.success(function(data, status, headers, config) {
				if(data!=null){
					$scope.messages.setSuccess("save");
					$scope.study.comments.push(data);
					$scope.currentComment = {comment:undefined};
				}
			})
			.error(function(data, status, headers, config) {
				$scope.messages.setError("save");
				$scope.messages.setDetails(data);
			});		
		}		
	};
	
	$scope.isUpdate = function(){
		return ($scope.index != undefined);		
	};
	
	$scope.setUpdate = function(comment, index){
		$scope.currentComment = angular.copy(comment);
		$scope.index = index;
	};
	
	$scope.update = function(){		
		if($scope.isCreationMode()){
			$scope.study.comments[$scope.index] = $scope.currentComment;
			$scope.currentComment = {comment:undefined};
			$scope.index = undefined;			
		}else{	
			$scope.messages.clear();
			$http.put(jsRoutes.controllers.sra.studies.api.StudyComments.update($scope.study.code, $scope.currentComment.code).url, $scope.currentComment)
			.success(function(data, status, headers, config) {
				if(data!=null){
					$scope.messages.setSuccess("save");
					$scope.study.comments[$scope.index] = $scope.currentComment;
					$scope.currentComment = {comment:undefined};
					$scope.index = undefined;
				}
			})
			.error(function(data, status, headers, config) {
				$scope.messages.setError("save");
				$scope.messages.setDetails(data);
			});
		}
	};
	
	$scope.remove = function(comment, index){
		if($scope.isCreationMode()){
			$scope.currentComment = {comment:undefined};
			$scope.study.comments.splice(index, 1);
		}else if (confirm(Messages("comments.remove.confirm"))) {
			$scope.messages.clear();
			$http.delete(jsRoutes.controllers.sra.studies.api.StudyComments.delete($scope.study.code, comment.code).url)
			.success(function(data, status, headers, config) {
				if(data!=null){
					$scope.messages.setSuccess("save");
					$scope.currentComment = {comment:undefined};
					$scope.study.comments.splice(index, 1);
				}
			})
			.error(function(data, status, headers, config) {
				$scope.messages.setError("remove");
				$scope.messages.setDetails(data);				
			});
		}
	};
}]);
	


