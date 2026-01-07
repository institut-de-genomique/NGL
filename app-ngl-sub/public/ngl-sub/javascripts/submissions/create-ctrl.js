"use strict";

angular.module('home').controller('CreateCtrl',[ '$http', '$scope', '$routeParams' , 'mainService', 'lists', 'tabService','datatable', 'toolsServices', 'messages',
                                                 function($http, $scope, $routeParams, mainService, lists, tabService, datatable, toolsServices, messages) { 

	// Initialisation :

	$scope.messages = messages();
	$scope.messages.clear();
	$scope.form = {};
	$scope.lists = lists;
	$scope.treeLoadInProgress = false;
	$scope.maxReadSetCodes = 1000;
	$scope.labelSelectAll = "SELECT ALL";
	$scope.treeLoadInProgress = false;
	
	$scope.isClickable=function() {
		if ($scope.treeLoadInProgress){
			return false;
		}
		return true;
	}
	
	// NGL-4952 
	$scope.isShowConsigne=false;
	$scope.toggleShowConsigne=function(){
		//console.log("TOGGLE search-ctrl.js");
		if ( $scope.isShowConsigne===false) { 
			$scope.isShowConsigne=true}
		else {$scope.isShowConsigne=false}
	};
	
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('create');
		tabService.addTabs({label:Messages('submissions.menu.create'),href:jsRoutes.controllers.sra.submissions.tpl.Submissions.home("create").url,remove:true});
		tabService.activeTab(0); //  active l'onglet en le mettant en bleu
	}
	
//	Initialisation ProjectCodes:
	$scope.lists.refresh.projects();
	$scope.refreshSraStudies = function() {
		if($scope.form.projCodes && $scope.form.projCodes.length > 0){
			// appel de refresh.sraStudies dans lists de common.js
			$scope.lists.refresh.sraStudies({projCodes:this.form.projCodes, stateCodes:["NONE", "SUB-F"]});
		}
	};
	
	$scope.refreshSraStudiesNONE = function() {
		if($scope.form.projCodes && $scope.form.projCodes.length > 0){
			// appel de refresh.sraStudies dans lists de common.js
			$scope.lists.refresh.sraStudies({projCodes:this.form.projCodes, stateCodes:["NONE"]});
		}
	};	
	$scope.refreshSraSamplesNONE = function() {
		if($scope.form.projCodes && $scope.form.projCodes.length > 0){
			// appel de refresh.sraSamples dans lists de common.js
			$scope.lists.refresh.sraSamples({projCodes:this.form.projCodes, stateCodes:["NONE"]});
		}
	};
	$scope.refreshSraConfigurations = function() {
		//console.log("Je suis dans refreshSraConfigurations avec ", this.form.projCodes);
		if($scope.form.projCodes && $scope.form.projCodes.length > 0){
			// appel de refresh.sraConfigurations dans lists de common.js
			$scope.lists.refresh.sraConfigurations({projCodes:this.form.projCodes});
		}
	};
	
	$scope.refreshReadSets = function() {
		//console.log("Je suis dans refreshReadSets");
		if($scope.form.projCodes && $scope.form.projCodes.length > 0){
			// Dans ReadSetsSearchForm.java champs projectCodes et non projCodes !!!
			// includes:"code" pour ne rapatrier que les codes des readsets et non les objets entiers
			//$scope.readSets = [];
			//$scope.readSets[0] = {"code": "ALL"};
			if($scope.bionanoCheck) {
				var tab_typeCodes = []; // tableau provisoire pour construire requete
				tab_typeCodes.push("rsbionano");
				if(toolsServices.isNotBlank($scope.form.taxonCode)) {
					// ancienne version avec selection des readsets disponibles:
					//$http.get(jsRoutes.controllers.readsets.api.ReadSets.list().url,{params:{projectCodes:this.form.projCodes, submissionStateCode:"NONE", stateCode:"A", includes:"code", bioinformaticValidCode:"TRUE", typeCodes:tab_typeCodes, taxonCode:this.form.taxonCode}}).success(function(data) {
					// nouvelle version avec selection des readset valide pour bioinfo et QC (production) :	
					$http.get(jsRoutes.controllers.readsets.api.ReadSets.list().url,{params:{projectCodes:this.form.projCodes, submissionStateCode:"NONE", productionValidCode:"TRUE", includes:"code", bioinformaticValidCode:"TRUE", typeCodes:tab_typeCodes, taxonCode:this.form.taxonCode}}).success(function(data) {
						$scope.readSets = data;
						$scope.readSets.unshift({"code": $scope.labelSelectAll});
					});
				} else {
					$http.get(jsRoutes.controllers.readsets.api.ReadSets.list().url,{params:{projectCodes:this.form.projCodes, submissionStateCode:"NONE", productionValidCode:"TRUE", includes:"code", bioinformaticValidCode:"TRUE", typeCodes:tab_typeCodes}}).success(function(data) {
						$scope.readSets = data;
						$scope.readSets.unshift({"code": $scope.labelSelectAll});
					});					
				}
			} else {
				var tab_typeCodes = []; // tableau provisoire pour construire requete
				tab_typeCodes.push("rsillumina");
				tab_typeCodes.push("rsnanopore");
				tab_typeCodes.push("rsmgi");
				tab_typeCodes.push("rspacbio");
				if(toolsServices.isNotBlank($scope.form.taxonCode)) {
					$http.get(jsRoutes.controllers.readsets.api.ReadSets.list().url,{params:{projectCodes:this.form.projCodes, submissionStateCode:"NONE", productionValidCode:"TRUE", includes:"code", bioinformaticValidCode:"TRUE", typeCodes:tab_typeCodes, taxonCode:this.form.taxonCode}}).success(function(data) {
						$scope.readSets = data;
						$scope.readSets.unshift({"code": $scope.labelSelectAll});
					});	
				} else {
					$http.get(jsRoutes.controllers.readsets.api.ReadSets.list().url,{params:{projectCodes:this.form.projCodes, submissionStateCode:"NONE", productionValidCode:"TRUE", includes:"code", bioinformaticValidCode:"TRUE", typeCodes:tab_typeCodes}}).success(function(data) {
						$scope.readSets = data;
						$scope.readSets.unshift({"code": $scope.labelSelectAll});
					});	
				}		
			}
			//console.log ("type de readSets=", typeof $scope.readSets); 
			//console.log("liste des readsets =", $scope.readSets);
		}
		
	};

	
	$scope.refreshSraAnalyzes = function() {
		console.log("Je suis dans refreshSraAnalyzes avec ", this.form.projCodes);
		if($scope.form.projCodes && $scope.form.projCodes.length > 0){
			// appel de refreshSraAnalyzes dans lists de common.js
			$scope.lists.refresh.sraAnalyzes({projCodes:this.form.projCodes, stateCode:"NONE"});		
		}
	};
	
	$scope.refreshAnalyzes_comment = function() {
		console.log("Je suis dans refreshAnalyzes avec ", this.form.projCodes);
		
		if($scope.form.projCodes && $scope.form.projCodes.length > 0){
			//console.log(jsRoutes.controllers);
			$http.get(jsRoutes.controllers.sra.analyzes.api.Analyzes.list().url,{params:{projCodes:this.form.projCodes, stateCode:"NONE"}}).success(function(data) {
				$scope.analyzes=data;
				//console.log("liste des analyses =", $scope.analyzes);
			});
		}
	};	
	
	
	// fonction qui recupere objet configuration dont le code est saisi par utilisateur et qui en fonction
	// de config.strategy_internal_study determine si la variable internal_studies est à true ou false.
	$scope.displayStudies = function() {
		//console.log("dans displayStudies");
		if($scope.form.projCodes && $scope.form.projCodes.length > 0){
			//get configuration
			$scope.STRATEGY_AC_SAMPLE = false;
			$scope.STRATEGY_AC_STUDY = false;
			$scope.STRATEGY_CODE_STUDY=true;
			$http.get(jsRoutes.controllers.sra.configurations.api.Configurations.get($scope.form.configurationCode).url).success(function(data) {
				//console.log("data.strategyStudy", data.strategyStudy);				
				if(data.strategyStudy === 'STRATEGY_AC_STUDY') {
					console.log("cas STRATEGY_AC_STUDY");
					$scope.STRATEGY_CODE_STUDY=false;
					$scope.STRATEGY_AC_STUDY=true;	
				}
				if(data.strategySample === 'STRATEGY_AC_SAMPLE') {
					console.log("cas STRATEGY_AC_SAMPLE");
					$scope.STRATEGY_AC_SAMPLE=true;	
				} else {
					
				}
				//console.log("data", data);
			});
		}
	};
	


//	Initialisation variables sra :

	$scope.save = function() {
		if($scope.form.atlaseaCheck) {
			$scope.saveIfAtlaseaCheck();
		} else {
			$scope.saveDefault();
		}
	}; // end save()
	
	
	$scope.saveDefault = function(){
		$scope.messages = messages();	
		$scope.messages.clear();
		$scope.treeLoadInProgress = true;
		console.log("$scope.form", $scope.form );

        // ci-dessous, le includes equivaut à contains :
		if($scope.form.readSetCodes != null && $scope.form.readSetCodes.includes($scope.labelSelectAll)) {
			// enlever la valeur $scope.labelSelectAll ("SELECT ALL") de readSets ce qui evitera de l'avoir dans $scope.form.readSetCodes 
			$scope.readSets.splice(0,1);
			console.log("titi $scope.readSets" , $scope.readSets);

			// indiquer que tous les readsetCodes sont selectionnés par le user en les mettant dans $scope.form.readSetCodes
			$scope.form.readSetCodes = $scope.readSets.map((readset) => {
				return readset.code;
			});
			
			if($scope.form.readSetCodes.length > $scope.maxReadSetCodes) {
				$scope.messages.setError("N'utilisez pas l'option SELECT ALL qui renvoie ici " + $scope.form.readSetCodes.length + " readsetCodes superieurs à " + $scope.maxReadSetCodes + " readsetCodes maximum conseillés pour une seule soumission");
				$scope.treeLoadInProgress = false;
				throw("Trop de readsetCodes à soumettre dans une meme soumission");  	 
			}
			
		}
		//console.log("XXXXXXXXXXXXXX   $scope.form.readSetCodes", $scope.form.readSetCodes );
		// important si le fichier utilisateur ne peut pas ou ne doit pas etre chargé que form.base64File soit
		// mis à chaine vide et non à null pour l'appel de l'url sra/api/submissions
		//console.log("$scope.userRefFileCollabToAc : '" + $scope.userRefFileCollabToAc + "'");
//		console.log("typeof $scope.userRefFileCollabToAc : '" + typeof $scope.userRefFileCollabToAc + "'");
//		console.log("typeof undefined : '" + typeof undefined + "'");
		
		$scope.form.base64UserFileRefCollabToEbiIdentifier=""; 
		if ($scope.userFileRefCollabToEbiIdentifier != null &&	$scope.userFileRefCollabToEbiIdentifier != undefined) {
			if ($scope.userFileRefCollabToEbiIdentifier.value != null && $scope.userFileRefCollabToEbiIdentifier.value != undefined) {
				$scope.form.base64UserFileRefCollabToEbiIdentifier=$scope.userFileRefCollabToEbiIdentifier.value;
			}
		} 
		$scope.form.base64UserFileReadSet="";
		if ($scope.userFileReadSet != null && $scope.userFileReadSet != undefined) {
			if ($scope.userFileReadSet.value != null && $scope.userFileReadSet.value != undefined) {
				$scope.form.base64UserFileReadSet=$scope.userFileReadSet.value;
			}
		} 	
		$scope.form.base64UserFilePathcmap="";
		if ($scope.userFilePathcmap != null && $scope.userFilePathcmap != undefined) {
			if ($scope.userFilePathcmap.value != null && $scope.userFilePathcmap.value != undefined) {
				$scope.form.base64UserFilePathcmap=$scope.userFilePathcmap.value;
				console.log("scope.form.base64UserFilePathcmap=", $scope.form.base64UserFilePathcmap);

			}
		} 	
//		$scope.form.base64UserFileExperiments = ""; 
//		if ($scope.userFileExperiments != null && $scope.userFileExperiments != undefined) {
//			if ($scope.userFileExperiments.value != null && $scope.userFileExperiments.value != undefined) {
//				$scope.form.base64UserFileExperiments=$scope.userFileExperiments.value;
//			}
//		} 		
//		$scope.form.base64UserFileSamples=""; 
//		if ($scope.userFileSamples != null && $scope.userFileSamples != undefined) {
//			if ($scope.userFileSamples.value != null && $scope.userFileSamples.value != undefined) {
//				$scope.form.base64UserFileSamples=$scope.userFileSamples.value;
//			}
//		} 

		mainService.setForm($scope.form);
		console.log("mainService.getForm=", mainService.getForm());
		//$scope.search();
		$http.post(jsRoutes.controllers.sra.submissions.api.Submissions.save().url, mainService.getForm()).success(function(data) {
			$scope.treeLoadInProgress = false;
			$scope.messages = messages();
			$scope.messages.clear();
			$scope.messages.clazz="alert alert-success";
			$scope.messages.text=Messages('submissions.msg.save.success') + " : " + data.code  + " => Pensez à valider puis demarrer votre soumission !!! ";
			$scope.messages.open();
			$scope.codeSubmission = data;
		    $scope.resetUserData();
		}).error(function(data){
		    $scope.treeLoadInProgress = false;
			//$scope.messages.setDetails({"error":["code":"value","code2":"value2"]});
		    //console.log("data=", data);
			$scope.messages.setDetails(data);
			$scope.messages.setError("save");
			//$scope.resetUserData();
			//$scope.messages.clear();
			//angular.element('#idUserFileReadSet')[0].value = null;
			//angular.element('#idUserFileRefCollabToEbiIdentifier')[0].value = null;
		});
	}; // end saveDefault()
	
	
	$scope.saveIfAtlaseaCheck = function(){
		$scope.messages = messages();	
		$scope.messages.clear();
		$scope.treeLoadInProgress = true;
		
		console.log("$scope.form", $scope.form );

 
		$scope.form.base64UserFileRefCollabToEbiIdentifier=""; 
		if ($scope.userFileRefCollabToEbiIdentifier != null &&	$scope.userFileRefCollabToEbiIdentifier != undefined) {
			if ($scope.userFileRefCollabToEbiIdentifier.value != null && $scope.userFileRefCollabToEbiIdentifier.value != undefined) {
				$scope.form.base64UserFileRefCollabToEbiIdentifier=$scope.userFileRefCollabToEbiIdentifier.value;
			}
		} 

		mainService.setForm($scope.form);
		console.log("Dans scope.saveIfAtlaseaCheck, mainService.getForm=", mainService.getForm());
		//$scope.search();
		$http.put(jsRoutes.controllers.sra.submissions.api.Submissions.createFromTolid().url, mainService.getForm()).success(function(data) {
			$scope.treeLoadInProgress = false;
			$scope.messages = messages();
			$scope.messages.clear();
			$scope.messages.clazz="alert alert-success";
			$scope.messages.text=data;
			$scope.messages.open();
			$scope.codeSubmission = data;
			console.log("codeSubmission=", $scope.codeSubmission);
		    $scope.resetUserData();
		}).error(function(data){
		    $scope.treeLoadInProgress = false;
			//$scope.messages.setDetails({"error":["code":"value","code2":"value2"]});
		    console.log("data=", data);
			$scope.messages.setDetails(data);
			$scope.messages.setError("save");
			//$scope.resetUserData();
			//$scope.messages.clear();
			//angular.element('#idUserFileReadSet')[0].value = null;
			//angular.element('#idUserFileRefCollabToEbiIdentifier')[0].value = null;
		});
	}; // end saveIfAtlaseaCheck()
	
		
	$scope.resetUserData = function(){
		$scope.treeLoadInProgress = false;
		$scope.form = {}; // on initialise à null toutes les variables recuperees dans create-ctrl.js dans code : ng-model="form
		
		console.log("scope.userFilePathcmap = ", $scope.userFilePathcmap);
		
		if (angular.element('#idUserFileReadSet') != null && angular.element('#idUserFileReadSet')[0] != null) {
			angular.element('#idUserFileReadSet')[0] = null;
		}
		if (angular.element('#idUserFileRefCollabToEbiIdentifier') != null && angular.element('#idUserFileRefCollabToEbiIdentifier')[0] != null) {
			angular.element('#idUserFileRefCollabToEbiIdentifier')[0] = null;	
		}
		if (angular.element('#idUserFilePathcmap') != null && angular.element('#idUserFilePathcmap')[0] != null) {
			angular.element('#idUserFilePathcmap')[0] = null;	
		}
		$scope.userFileExperiments=null;
		$scope.userFileSamples=null;
		$scope.userFileRefCollabToEbiIdentifier=null;
		$scope.userFileReadSet=null;
		$scope.userFilePathcmap = null;
		$scope.bionanoCheck = false;
		$scope.defaultCheck = false;
		$scope.metadataCheck = false;
		$scope.atlaseaCheck = false;
		$scope.acStudy=null;
		$scope.acSample=null;
		
	};	
		

	$scope.updateCheck = function(choixCheck) {
		//console.log("Dans updateCheck, scope.choixCheck=", $scope.choixCheck);
		//console.log("Dans updateCheck, choixCheck=", choixCheck);
		$scope.treeLoadInProgress = false;
		$scope.messages = messages();	
		$scope.messages.clear();
		$scope.resetUserData();		
		$scope.choixCheck = choixCheck;
		if(choixCheck=="defaultCheck") {
			$scope.defaultCheck = true;
			$scope.form.defaultCheck = true;
		}
		if(choixCheck=="bionanoCheck") {
			$scope.bionanoCheck = true;
			$scope.form.bionanoCheck = true;

		}
		if(choixCheck=="metadataCheck") {
			$scope.metadataCheck = true;
			$scope.form.metadataCheck = true;
		}	
		if(choixCheck=="atlaseaCheck") {
			$scope.atlaseaCheck = true;
			$scope.form.atlaseaCheck = true;
		}
			
	};
	
	$scope.reset = function(){	
		$scope.updateCheck($scope.choixCheck);
	};	
	
	$scope.isBuilding = function(){
		var retour = false;
		// si bionano et formulaire existe :
		if ($scope.bionanoCheck && $scope.form != null) {
			//console.log("111111111111111111111");
			// si fichier userFilePathcmap renseigné :
			if ($scope.userFilePathcmap != null && $scope.userFilePathcmap != undefined && $scope.userFilePathcmap.value != null && $scope.userFilePathcmap.value != undefined) {	
				//console.log("22222222222222222222222222");
				// si projectCode renseigné :
				if($scope.form.projCodes && $scope.form.projCodes.length > 0) { 
					//console.log("33333333333333333333");
					// si analysisCode renseigné :
					if(toolsServices.isNotBlank($scope.form.analysisCode)){ 
						//console.log("444444444444444444444444");
                        // si readsets renseigné :
						if( ($scope.form.readSetCodes && $scope.form.readSetCodes.length > 0) || $scope.form.base64UserFileReadSet ) { 
							//console.log("5555555555555555555");
							retour = true;
						}
					}  
				}
			}
		}
		// si illumina ou nanopore ou mgi et formulaire existe
		if ($scope.defaultCheck && $scope.form != null) {
			// si configuration renseignée :
			if($scope.form.configurationCode) {
				retour = true;
			}
		}
		
		// si metadonnée et formulaire existe
		if ($scope.metadataCheck && $scope.form != null) {
			// si studyCode ou sampleCodes renseignés :
			if($scope.form.studyCode) {
				retour = true;
			} 
			if($scope.form.sampleCodes) {
				retour = true;
			} 
		}
		// si soumission atlasea et tolid existe	
		if ($scope.atlaseaCheck && $scope.form != null) {
			// si tolid renseigné :
			if($scope.form.tolId) {
				retour = true;
			} 
		}	
		return retour;
		
		
	};
	
}]);


