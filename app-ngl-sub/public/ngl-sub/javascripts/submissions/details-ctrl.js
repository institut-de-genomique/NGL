"use strict";

angular.module('home').controller('DetailsCtrl',[ '$http', '$scope', '$routeParams' , '$q', 'mainService', 'lists', 'tabService','messages','datatable',
	function($http, $scope, $routeParams, $q, mainService, lists, tabService, messages, datatable) { 


    $scope.isRemovable = function() {
    	return $scope.submission != null;
    };


	var studiesDTConfig = {
			name:'studiesDT',
			order :{by:'code',mode:'local', reverse:true},
			search:{
				url:jsRoutes.controllers.sra.studies.api.Studies.list()
			},
			pagination:{
				active:true,
				mode:'local'
			},
			select:{active:true},
			showTotalNumberRecords:true,
			edit : {
				active:true,
				showButton : false,
				withoutSelect : true,
				columnMode : true,
				lineMode : function(line){
					if((line.state.code === "NONE")||(line.state.code === "N"))
						return true;
					else 
						return false;
				}
			},
			save : {
				active:true,
				showButton : false,
				changeClass : false,
				// important de mettre en mode local pour rafraichissement de la page, mais sauvegarde globale via bouton
				mode:'local',
				url:function(line){
					return jsRoutes.controllers.sra.samples.api.Studies.update(line.code).url; // jamais utilisé en mode local
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
				active:true,
				showButton:true
			},
			exportCSV:{
				active:false
			},
			show:{                   // bouton pour epingler si on passe par details-ctrl.js 
				active:true,
				add :function(line){
					tabService.addTabs({label:line.code,href:jsRoutes.controllers.sra.studies.tpl.Studies.get(line.code).url,remove:true});
				}
			},*/
			columns : [
			    {property:"traceInformation.creationDate",
			       	header: Messages("traceInformation.creationDate"),
			       	type :"date",		    	  	
			       	order:false
			    },
				{property:"code",
					header: Messages("study.code"),
					type :"text",		    	  	
					order:true
				},
				{property:"accession",
					header: Messages("study.accession"),
					type :"text",		    	  	
					order:true
				}, 
				{property:"state.code",
					header: Messages("study.state.code"),
					type :"text",		    	  	
					order:true
				}, 
				{property:"releaseDate",
					header: Messages("study.releaseDate"),
					type :"date",		    	  	
					order:false,
					edit:false,
					choiceInList:false  
				},
				{property:"centerProjectName",
			        	header: Messages("study.centerProjectName"),
			        	type :"text",		    	  	
			        	order:false,
			        	edit:true,
			        	choiceInList:false  
			    },
				{property:"projectCodes",
					header: Messages("study.projectCodes"),
					type :"text",		    	  	
					order:false,
					edit:false,
					choiceInList:false  
				},
				{property:"title",
					header: Messages("study.title"),
					type :"text",	
					hide:true,
					order:false,
					edit:true,
					editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
					choiceInList:false
				},
				{property:"studyAbstract",
					header: Messages("study.abstract"),
					type :"String",
					hide:true,
					edit:true,
					editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
					order:false,
					choiceInList:false
				},
				{property:"description",
					header: Messages("study.description"),
					type :"text",	
					hide:true,
					order:false,
					edit:true,
					editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
					choiceInList:false
				},

				]			
	};		        


	var samplesDTConfig = {
			name:'sampleDT',
			order :{by:'code',mode:'local', reverse:true},
			search:{active:false},
			select:{active:false},
			pagination:{
				active:true,
				mode:'local'
			},
			select:{active:true},
			showTotalNumberRecords:true,
			edit : {
				active:true,
				showButton : false,
				withoutSelect : true,
				columnMode : true,
				lineMode : function(line){
					if(line.state.code === "N")
						return true;
					else 
						return false;
				}
			},

			save : {
				active:true,
				showButton : false,
				changeClass : false,
				// important de mettre en mode local pour rafraichissement de la page, mais sauvegarde globale via bouton
				mode:'local',
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
			columns : [
			 	{property:"traceInformation.creationDate",
			       	header: Messages("sample.traceInformation.creationDate"),
			       	type :"date",		    	  	
			       	order:false
			    },
				{property:"code",
					header: Messages("sample.code"),
					type :"text",		    	  	
					order:true
				},
				{property:"accession",
					header: Messages("sample.accession"),
					type :"text",		    	  	
					order:true
				},
				{property:"projectCode",
					header: Messages("sample.projectCode"),
					type :"text",		    	  	
					order:false,
					edit:true,            // false
					choiceInList:false  
				},
				{property:"anonymizedName",
					header: Messages("sample.anonymizedName"),
					type :"text",	
					hide:true,
					order:false,
					edit:true,
					choiceInList:false
				},
				{property:"title",
					header: Messages("sample.title"),
					type :"text",	
					hide:true,
					order:false,
					edit:true,
					editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
					choiceInList:false
				},
				{property:"description",
					header: Messages("sample.description"),
					type :"text",	
					hide:true,
					order:false,
					edit:true,
					editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
					choiceInList:false
				},				
				{property:"clone",
					header: Messages("sample.clone"),
					type :"text",	
					hide:true,
					order:false,
					edit:true,
					choiceInList:false
				},
				{property:"taxonId",
					header: Messages("sample.taxonId"),
					type :"int",
					hide:true,
					order:false,
					edit:false,
					choiceInList:false
				},
				/*{property:"commonName",
					header: Messages("sample.commonName"),
					type :"text",		    	  	
					hide:true,
					order:false,
					edit:false,
					choiceInList:false
				},*/
				{property:"scientificName",
					header: Messages("sample.scientificName"),
					type :"text",		    	  	
					order:false,
					edit:false,
					choiceInList:false
				},
				{property:"state.code",
					header: Messages("sample.state"),
					//"filter":"codes:'state'",
					type :"text",		    	  	
					order:false,
					edit:false,
					choiceInList:false
				},
				{property:"attributes",
					header: Messages("sample.attributes"),
					//"filter":"codes:'state'",
					type :"text",		    	  	
					order:true,
					edit:true,
					choiceInList:false
				}
				]				
	};

	var experimentsDTConfig = {
			name:'experimentDT',
			order :{by:'code',mode:'local', reverse:true},
			search:{active:false},
			select:{active:false},
			pagination:{
				active:true,
				mode:'local'
			},
			showTotalNumberRecords:true,
			edit:{
				active:true,
				showButton : false,
				withoutSelect : true,
				columnMode : true,
				lineMode:function(line){
					if(line.state.code === "N"){
						return true;
					}else {
						return false;
					}
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
			/*cancel : {
				showButton:true
			},
			hide:{
				active:true,
				showButton:true
			},
			exportCSV:{
				active:false
			},
			 */
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
					editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
					order:false
				},
				{property:"librarySelection",
					header: Messages("experiment.librarySelection"),
					type :"String",
					hide:true,
					edit:true,
					order:false,
					choiceInList:true,
					listStyle:'bt-select-multiple',
					possibleValues:'sraVariables.librarySelection',
				},
				{property:"libraryStrategy",
					header: Messages("experiment.libraryStrategy"),
					type :"String",
					hide:true,
					edit:true,
					order:false,
					choiceInList:true,
					listStyle:'bt-select-multiple',
					possibleValues:'sraVariables.libraryStrategy',
				},
				{property:"librarySource",
					header: Messages("experiment.librarySource"),
					type :"String",
					hide:true,
					edit:true,
					order:false,
					choiceInList:true,
					listStyle:'bt-select-multiple',
					possibleValues:'sraVariables.librarySource',
				},
				{property:"libraryLayout",
					header: Messages("experiment.libraryLayout"),
					type :"String",
					hide:true,
					edit:false,
					order:false,
					choiceInList:true,
					listStyle:'bt-select-multiple',
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
					edit:true,
					order:false,
					choiceInList:true,
					listStyle:'bt-select-multiple',
					possibleValues:'sraVariables.libraryLayoutOrientation',
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
			search:{active:false},
			pagination:{
				active:true,
				mode:'local'
			},
			select:{active:true},
			showTotalNumberRecords:true,
			edit : {
				active:true,
				showButton : false,
				withoutSelect : true,
				columnMode : true
			},
			columns : [
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
			order :{by:'code', mode:'local', reverse:true},
			search:{active:false},
			select:{active:false},
			pagination:{
				active:true,
				mode:'local'
			},
			select:{active:true},
			showTotalNumberRecords:true,
			edit : {
				active:true,
				showButton :false,
				withoutSelect : true,
				columnMode : true
			},
			cancel : {
				showButton:true
			},
			hide:{
				active:true
			},
			exportCSV:{
				active:false
			},
			//url:function(lineValue){
			//		return jsRoutes.controllers.sra.experiments.api.Experiments.update(lineValue.code).url; // jamais utilisé en mode local
			//	},
			
			remove : {
				active:false,
				withEdit:false, //to authorize to remove a line in edition mode
				showButton : false,
				mode:'remote', //or local
				url:function(lineValue) { 
					return jsRoutes.controllers.sra.experiments.api.ExperimentsRawDatas.delete(lineValue.experimentCode, lineValue.relatifName).url;
				},
				callback : undefined, //used to have a callback after remove all element. the datatable is pass to callback method and number of error
				start:false,
				counter:0,
				number:0, //number of element in progress
				error:0								
			},
			
			columns : [
				{property:"relatifName",
					header: Messages("rawData.relatifName"),
					type :"text",		    	  	
					order:true
				}
				]	        
	};	


	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('create');
		tabService.addTabs({label:Messages('submissions.menu.create'),href:jsRoutes.controllers.sra.submissions.tpl.Submissions.home("create").url,remove:true});
		tabService.activeTab(0); // desactive le lien !
	}
	// si on declare dans services => var sraVariables = {};
	// si on declare dans le controlleur :
	$scope.sraVariables = {};
	$scope.subList={};
	$scope.checkSample=false;

	var init = function(){
		$scope.messages = messages();
		$scope.mainService = mainService;
		$scope.mainService.stopEditMode();
		$scope.sampleCheck=false;
		$scope.experimentCheck=false;
		$scope.studyCheck=false;
		$scope.runCheck=false;
		$scope.rawDataCheck=false;
		// Attention appel de get du controller api.sra.submissions qui est herite
		// $routeParams.code contient le code qui apparait dans l'url de consultation donc le code de la submission
		$http.get(jsRoutes.controllers.sra.submissions.api.Submissions.get($routeParams.code).url).success(function(data) {
			$scope.submission = data;	
			console.log("$routeParams.code:"+$routeParams.code);
			console.log("Submission.code :"+$scope.submission.code);
			console.log("Submission.refSampleCodes :"+$scope.submission.refSampleCodes);
			console.log("Submission.sampleCodes :"+$scope.submission.sampleCodes);
			console.log("Submission.experimentCodes :"+$scope.submission.experimentCodes);
			console.log("Submission.runCodes :"+$scope.submission.runCodes);
			$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'librarySelection'}})
			.success(function(data) {
				// initialisation de la variable sraVariables.librarySelection utilisee dans experimentsDTConfig
				$scope.sraVariables.librarySelection = data;
			});	
			$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'libraryStrategy'}})
			.success(function(data) {
				// initialisation de la variable sraVariables.libraryStrategy utilisee dans experimentsDTConfig
				$scope.sraVariables.libraryStrategy = data;
			});
			$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'librarySource'}})
			.success(function(data) {
				// initialisation de la variable sraVariables.librarySource utilisee dans experimentsDTConfig
				$scope.sraVariables.librarySource = data;
			});
			$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'libraryLayout'}})
			.success(function(data) {
				// initialisation de la variable sraVariables.libraryLayout utilisee dans experimentsDTConfig
				$scope.sraVariables.libraryLayout = data;
			});
			$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'libraryLayoutOrientation'}})
			.success(function(data) {
				// initialisation de la variable sraVariables.libraryLayoutOrientation utilisee dans experimentsDTConfig
				$scope.sraVariables.libraryLayoutOrientation = data;
			});	

			if($scope.submission.studyCode !=null){
				$http.get(jsRoutes.controllers.sra.studies.api.Studies.get($scope.submission.studyCode).url).success(function(data)
						{
					$scope.studies = [];
					$scope.studies.push(data);
					console.log("$scope.submission.study: " + $scope.submission.studyCode);
					console.log("$scope.studies :" + $scope.studies);

					//Init datatable
					$scope.studyDT = datatable(studiesDTConfig);
					$scope.studyDT.setData($scope.studies, $scope.studies.length);
						});
			}
			if($scope.submission.refSampleCodes.length>0){
				var nbElementByBatch = Math.ceil($scope.submission.refSampleCodes.length / 6); //6 because 6 request max in parrallel with firefox and chrome
				var queries = [];
				for (var i = 0; i < 6 && $scope.submission.refSampleCodes.length > 0; i++) {
					var subSampleCodes = $scope.submission.refSampleCodes.splice(0, nbElementByBatch);
					//queries.push( $http.get(jsRoutes.controllers.sra.samples.api.Samples.list().url, {params: {listSampleCodes:subSampleCodes}}) );
					queries.push( $http.get(jsRoutes.controllers.sra.samples.api.Samples.list().url, {params: {codes:subSampleCodes}}) );
				}
				$q.all(queries).then(function(results) {
					var allData = [];
					results.forEach(function(result){
						allData = allData.concat(result.data);
					});

					$scope.samples = allData;

					console.log("$scope.submission.sampleCodes: " + $scope.submission.sampleCodes);
					console.log("$scope.samples :" + $scope.samples);

					//Init datatable
					$scope.sampleDT = datatable(samplesDTConfig);
					$scope.sampleDT.setData($scope.samples, $scope.samples.length);
				});
			}

			//Get samples
			//$http.get(jsRoutes.controllers.sra.samples.api.Samples.list().url, {params: {listSampleCodes:$scope.submission.refSampleCodes}}).success(function(data)
			/*$http.get(jsRoutes.controllers.sra.samples.api.Samples.list().url, {params: {codes:$scope.submission.refSampleCodes}}).success(function(data)
					{
					$scope.samples = data;

					console.log("$scope.submission.sampleCodes: " + $scope.submission.sampleCodes);
					console.log("$scope.samples :" + $scope.samples);

					//Init datatable
					$scope.sampleDT = datatable(samplesDTConfig);
					$scope.sampleDT.setData($scope.samples, $scope.samples.length);
					});
			*/

			if($scope.submission.experimentCodes.length>0){
				var nbElementByBatch = Math.ceil($scope.submission.experimentCodes.length / 6); //6 because 6 request max in parrallel with firefox and chrome
				var queries = [];
				for (var i = 0; i < 6 && $scope.submission.experimentCodes.length > 0; i++) {
					var subExperimentCodes = $scope.submission.experimentCodes.splice(0, nbElementByBatch);
					//queries.push( $http.get(jsRoutes.controllers.sra.experiments.api.Experiments.list().url, { params : { listExperimentCodes : subExperimentCodes } }) );
					queries.push( $http.get(jsRoutes.controllers.sra.experiments.api.Experiments.list().url, { params : { codes : subExperimentCodes } }) );
				}
				$q.all(queries).then(function(results) {
					var allData = [];
					results.forEach(function(result){
						allData = allData.concat(result.data);
					});
                    console.log("XXXXXXXXXXXXXXXXXXXXXx");
                    
					$scope.experiments = allData;
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
						var experiment = $scope.experiments[i];
						var run = experiment.run;
						for (var j=0; j<run.listRawData.length; j++) {
							var rawdataPlus = run.listRawData[j];
							rawdataPlus.experimentCode = experiment.code;
							//console.log("experimentCode = " +rawdataPlus.experimentCode);
							//maListRawDatas.push(run.listRawData[j]);
							maListRawDatas.push(rawdataPlus);
						}
					}
					// Partie de la configuration (rawDatasDTConfig) à definir seulement
					// lorsque $scope.submission est definit.
					rawDatasDTConfig.remove.active=($scope.submission.state.code==='N');
					rawDatasDTConfig.remove.showButton=($scope.submission.state.code==='N');
					$scope.rawDataDT = datatable(rawDatasDTConfig);
					$scope.rawDataDT.setData(maListRawDatas, maListRawDatas.length);
					console.log("XXXXXXXXXXXXXXXXXXXXX " + $scope.submission.state.code);
					
				});	
			}

			//Get experiments (and runs)
			/*$http.get(jsRoutes.controllers.sra.experiments.api.Experiments.list().url, {params: {listExperimentCodes:$scope.submission.experimentCodes}}).success(function(data)
					{
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
					
					});		*/	

		});


	};

	init();

	function closeSubmission(){
		// $scope.submission.state.code = "V-SUB";		
		//$http.put(jsRoutes.controllers.sra.submissions.api.Submissions.update($scope.submission.code).url, $scope.submission)
		var state = angular.copy($scope.submission.state);
		state.code = "V-SUB";
		$http.put(jsRoutes.controllers.sra.submissions.api.Submissions.updateState($scope.submission.code).url, state)
		.success(function(data) {
			//Set success message
			$scope.messages.clazz="alert alert-success";
			$scope.messages.text=Messages('submissions.msg.validate.success');
			$scope.messages.open();
		}).error(function(data){
			$scope.messages.addDetails(data);
			$scope.messages.setError("save");
		});
	}

	//en Javascript, ce n'est pas vous qui choisissez le mode de passage ; 
	//ça fonctionne un peu comme en Java[1] : 
	//si c'est un type natif (Number, String, etc.) c'est passé par valeur, 
	//tandis que si c'est un objet d'une classe à vous, c'est passé par référence.
	// => d'ou l'importance du return ici
	function processInSubmission(decompte, error) { // pas d'indication de retour dans la signature.
		decompte = decompte - 1;
		if (decompte === 0) {
			if (error){
				// afficher message d'erreur sans sauver la soumission.
				$scope.messages.setError("save");
			} else {
				// sauver la soumission dans base et afficher resultat de la requete 
				closeSubmission();
			}
		}
		return decompte;
	}


	/* buttons section */

	$scope.userValidate = function(){
		$scope.messages.clear();

		var error = false;
		var tab_studies = [];
		if($scope.submission.studyCode !=null){
			// Recuperation des studies :
			$scope.studyDT.save();	// sauvegarde dans client des studies avec valeurs editees (valeurs utilisateurs)	
			tab_studies = $scope.studyDT.getData();
		}

		// Recuperation des samples :
		$scope.sampleDT.save();	// sauvegarde dans client des samples avec valeurs editees (valeurs utilisateurs)	
		var tab_samples = $scope.sampleDT.getData();

		// Recuperation des experiments :	
		$scope.experimentDT.save();		// recuperation saisie utilisateur et sauvegarde dans client.
		var tab_experiments = $scope.experimentDT.getData();


		var decompte = tab_samples.length +  tab_experiments.length + tab_studies.length;


		// Mise à jour du status  des studies :
		for(var i = 0; i < tab_studies.length ; i++){
			console.log("studyCode = " + tab_studies[i].code + " state = "+ tab_studies[i].state.code);
			//tab_studies[i].state.code = "V-SUB";
			console.log("studyTitle = " + tab_studies[i].title + " state = "+ tab_studies[i].state.code);
			console.log("studyCode = " + tab_studies[i].code + " state = "+ tab_studies[i].state.code);
			// sauvegarde dans database asynchrone
			$http.put(jsRoutes.controllers.sra.studies.api.Studies.update(tab_studies[i].code).url, tab_studies[i])
			.success(function(data){
				//Set success message
				//$scope.messages.clazz="alert alert-success";
				//$scope.messages.text=Messages('submissions.msg.validate.success');
				//$scope.messages.open();

				decompte = processInSubmission(decompte, error);
			}).error(function(data){
				$scope.messages.addDetails(data);
				//$scope.messages.setError("save");
				error = true;
				decompte = processInSubmission(decompte, error);
			});			
			console.log("studyTitle = " + tab_studies[i].title + " state = "+ tab_studies[i].state.code);
		}
		if($scope.submission.studyCode !=null){
			$scope.studyDT.setData(tab_studies, tab_studies.length);
			// sauvegarde cote client des studies avec bon statut
			$scope.studyDT.save(); // fait le save cote client mais n'utilise pas url et ne fait pas save dans database.
		}



		// Mise à jour du status  des samples :
		for(var i = 0; i < tab_samples.length ; i++){
			console.log("sampleCode = " + tab_samples[i].code + " state = "+ tab_samples[i].state.code);
			//tab_samples[i].state.code = "V-SUB";
			console.log("sampleTitle = " + tab_samples[i].title + " state = "+ tab_samples[i].state.code);
			console.log("sampleCode = " + tab_samples[i].code + " state = "+ tab_samples[i].state.code);
			// sauvegarde dans database asynchrone
			$http.put(jsRoutes.controllers.sra.samples.api.Samples.update(tab_samples[i].code).url, tab_samples[i])
			.success(function(data){
				//Set success message
				//$scope.messages.clazz="alert alert-success";
				//$scope.messages.text=Messages('submissions.msg.validate.success');
				//$scope.messages.open();

				decompte = processInSubmission(decompte, error);
			}).error(function(data){
				$scope.messages.addDetails(data);
				//$scope.messages.setError("save");
				error = true;
				decompte = processInSubmission(decompte, error);
			});			
			console.log("sampleTitle = " + tab_samples[i].title + " state = "+ tab_samples[i].state.code);
		}
		$scope.sampleDT.setData(tab_samples, tab_samples.length);
		// sauvegarde cote client des samples avec bon statut
		$scope.sampleDT.save(); // fait le save cote client mais n'utilise pas url et ne fait pas save dans database.


		// Mise à jour du statut des experiments :
		for(var i = 0; i < tab_experiments.length ; i++){
			console.log("experimentCode = " + tab_experiments[i].code + " state = "+ tab_experiments[i].state.code);
			//tab_experiments[i].state.code = "V-SUB";
			console.log("experimentCode = " + tab_experiments[i].code + " state = "+ tab_experiments[i].state.code);
			// sauvegarde dans database :
			$http.put(jsRoutes.controllers.sra.experiments.api.Experiments.update(tab_experiments[i].code).url, tab_experiments[i]).success(function(data){
				//Set success message
				//$scope.messages.clazz="alert alert-success";
				//$scope.messages.text=Messages('submissions.msg.validate.success');
				//$scope.messages.open();
				decompte = processInSubmission(decompte, error);
			}).error(function(data){
				$scope.messages.addDetails(data);
				error = true;
				decompte = processInSubmission(decompte, error);
				//$scope.messages.setError("save");
			});			
		}
		// initialisation inutile $scope.experimentDT = datatable(experimentsDTConfig);
		$scope.experimentDT.setData(tab_experiments, tab_experiments.length);
		// sauvegarde cote client des experiments avec bon statut :
		$scope.experimentDT.save(); // fait le save cote client mais n'utilise pas url et ne fait pas save dans database.

		//mise a jour l'etat de submission a V-SUB realise dans closeSubmission appelé par processInSubmission

	};

	$scope.cancel = function(){
		console.log("call cancel");
		$scope.messages.clear();
		$scope.studyDT.cancel();
		$scope.sampleDT.cancel();
		$scope.experimentDT.cancel();
		$scope.mainService.stopEditMode();		
	};

	$scope.activeEditMode = function(){
		$scope.messages.clear();
		$scope.mainService.startEditMode();
		console.log("studyDT = "+$scope.studyDT);
		console.log("expDT = "+$scope.experimentDT);
		if ($scope.studyDT) {
			$scope.studyDT.setEdit();
		}
		if ($scope.sampleDT) {
			$scope.sampleDT.setEdit();
		}

		if ($scope.experimentDT) {
			$scope.experimentDT.setEdit();
		}
		if ($scope.rawDataDT) {
			$scope.rawDataDT.setEdit();
		}
		
	};

}]);

