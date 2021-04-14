"use strict";

angular.module('home').controller('DetailsCtrl',[ '$http', '$scope', '$routeParams' , 'mainService', 'lists', 'tabService','messages','datatable',
                                                  function($http, $scope, $routeParams, mainService, lists, tabService, messages, datatable) { 



	
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
						type :"String",		    	  	
						hide:true,
						edit:true,
						order:false
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
						type :"String",		    	  	
						hide:true,
						edit:true,
						order:false
					},
					{property:"libraryConstructionProtocol",
						 header: Messages("experiment.libraryConstructionProtocol"),
						 type :"String",		    	  	
						 hide:true,
						 edit:true,
						 order:false
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
			           {property:"relatifName",
			        	header: Messages("rawData.relatifName"),
			        	type :"text",		    	  	
			        	order:true
			           }
		    		  ]	        
		};	

	
	
	
	
	
	
	
	// si on declare dans services => var sraVariables = {};
	// si on declare dans le controlleur :
	
	
	var init = function(userName){
		$scope.mainService = mainService;
		$scope.messages = messages();

		
		$scope.isPublic=false;
		$scope.visibility="undefined";
		$scope.isReleasable = function(userName) { return false; }; // fonction qui renvoie toujours false
		// si on souhaite affichage bouton si on vient du menu release :
		//$scope.release=$scope.mainService.get("release");
		
       // console.log("$routeParams.code:"+$routeParams.code);

		// Recuperer le study en passant par list 
		//$http.get(jsRoutes.controllers.sra.studies.api.Studies.list().url,  {params: {code:$routeParams.code}}).success(function(data){
		// en ajoutant $code dans studySearchForm.
		
		// Recuperer le study en passant par get :		
		$http.get(jsRoutes.controllers.sra.studies.api.Studies.get($routeParams.code).url).success(function(data){
		//console.log("nbre de study:"+data.length);
	
		$scope.study = data;
		
		// Ajout des onglets à gauche si rafraichissement page
		if(tabService.getTabs().length == 0){			
			//tabService.addTabs({label:Messages('studies.menu.consultation'),href:jsRoutes.controllers.sra.studies.tpl.Studies.home("consultation").url,remove:true});
			tabService.addTabs({label:$scope.study.code,href:jsRoutes.controllers.sra.studies.tpl.Studies.get($scope.study.code).url,remove:true});
			tabService.activeTab($scope.getTabs(0)); // active l'onglet indiqué, le met en bleu.
		}
		
		// On compare les date :
		// $scope.study.releaseDate correspond à la date de release exprimée en milliseconde ecoulées depuis 01/01/1970 (Date Epoch) 
		// Date.now() correspond à la date courante exprimee en ms depuis Epoch
		/*if ($scope.study.traceInformation.createUser!=userName) {
			$scope.isReleasable = false;
			console.log("utilisateur" + userName + "!= createur" + $scope.study.traceInformation.createUser);
		} else */

		
		
		if ( !$scope.study.releaseDate) {
			//$scope.isReleasable = function(userName){return false;}; // isReleasable est une fonction
			console.log("study.releaseDate non renseigné");
			$scope.visibility="undefined";
		} else {		
			console.log("study.releaseDate" + $scope.study.releaseDate);
			console.log("Date.now" + Date.now());
			if (($scope.study.releaseDate > Date.now()) && $scope.study.state.code == "SUB-F") {
				$scope.isReleasable = function(userName){return $scope.study.traceInformation.createUser==userName;};
				console.log("donnée confidentielle");
				console.log("isReleasable: " + $scope.isReleasable);
				$scope.visibility="private";
			} else {
				//$scope.isReleasable = function(userName){return false;};// return false && $scope.study.traceInformation.createUser==userName;
				console.log("donnée publique");
				$scope.isPublic=true;
				$scope.visibility="public";	
			}
		}
		console.log("scope.visibility " + $scope.visibility);
		
		/* Interressant pour le formatage :
		console.log("releaseDate="+$scope.study.releaseDate);
		console.log("releaseDate="+ moment($scope.study.releaseDate).calendar());
		console.log("date.now="+ Date.now());
		console.log("releaseDate.endOf="+ moment($scope.study.releaseDate).endOf('day'));
		console.log("releaseDate.fromNow="+ moment($scope.study.releaseDate).fromNow());
		console.log("releaseDate.endOf.fromNow="+ moment($scope.study.releaseDate).endOf('day').fromNow());
		*/
		
		});		
		
		
			//Get experiments (and runs)
			$http.get(jsRoutes.controllers.sra.experiments.api.Experiments.list().url, {params: {studyCode:$routeParams.code}}).success(function(data)
					{
					//console.log("$routeParams.code:"+$routeParams.code);
			
					$scope.experiments = data;
					//Init datatable
					$scope.experimentDT = datatable(experimentsDTConfig);
					$scope.experimentDT.setData($scope.experiments, $scope.experiments.length);
					//Get Runs
					$scope.runDT = datatable(runsDTConfig);
					// Comme on a un seul run par experiment, on n'a pas besoin de boucler pour recuperer les données :
					$scope.runDT.setData($scope.experiments, $scope.experiments.length);
					
					
					// Get RawDatas : construction de la liste des rawData puis injection dans datatable :
					var maListRawDatas = [];
					for (var i=0; i<$scope.experiments.length; i++) {
						var run = $scope.experiments[i].run;
						for (var j=0; j<run.listRawData.length; j++) {
							maListRawDatas.push(run.listRawData[j]);
						}
					}
					
					$scope.rawDataDT = datatable(rawDatasDTConfig);
					$scope.rawDataDT.setData(maListRawDatas, maListRawDatas.length);
					});			
		
//		});
		
		
	};

	init();
		
	/* buttons section */
	
	$scope.userRelease = function(){
		$scope.messages.clear();
		console.log("je suis dans le bouton studies.details-ctrl.js.userRelease");
		
		/*Partie faites maintenant en passant par createFromStudyRelease
		var state = angular.copy($scope.study.state);
		state.code = "SUBR-SMD-IW";
		$http.put(jsRoutes.controllers.sra.studies.api.Studies.updateState($scope.study.code).url, state)
			.success(function(data){
		   		$scope.messages.clazz="alert alert-success";
				$scope.messages.text=Messages('studies.msg.release.success');
				$scope.messages.open();
			})
			.error(function(data){
				$scope.messages.addDetails(data);
				$scope.messages.setError("release");	
			});	
		  */                    
		 $http.put(jsRoutes.controllers.sra.submissions.api.Submissions.createFromStudyRelease($scope.study.code).url)
			.success(function(data){
//		   		$scope.messages.clazz="alert alert-success";
//				$scope.messages.text=Messages('studies.msg.release.success');
//				$scope.messages.open();
				console.log("code de la soumission crée " + data.code);
				var submissionCode = data.code;
				var submissionState = angular.copy(data.state);
				submissionState.code = "SUBR-SMD-IW";
				$http.put(jsRoutes.controllers.sra.submissions.api.Submissions.updateState(submissionCode).url, submissionState)
					.success(function(data){
				   		$scope.messages.clazz="alert alert-success";
						$scope.messages.text=Messages('studies.msg.release.success');
						$scope.messages.open();
					})				
					.error(function(data){
						$scope.messages.addDetails(data);
						$scope.messages.setError("Probleme pour la release du study");	
				});		
			})
			.error(function(data){
				$scope.messages.addDetails(data);
				$scope.messages.setError("Probleme pour la release");	
			});	
	 
		 
		 
	};
	
	$scope.cancel = function(){
		console.log("call cancel");
		$scope.messages.clear();
		$scope.sampleDT.cancel();
		$scope.experimentDT.cancel();
		$scope.mainService.stopEditMode();		
	};
	
	$scope.activeEditMode = function(){
		$scope.messages.clear();
		$scope.mainService.startEditMode();
		$scope.sampleDT.setEdit();
		$scope.experimentDT.setEdit();
	};
	
}]);	


