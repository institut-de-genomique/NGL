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
					{property:"studyAccession",
						header: Messages("analysis.studyAccession"),
						type :"String",		    	  	
						hide:true,
						edit:true,
						order:false
					},					
					{property:"sampleAccession",
						header: Messages("analysis.sampleAccession"),
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
						type :"String",		    	  	
						hide:true,
						edit:true,
						order:false
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
						type :"text",	
						hide:true,
						order:true,
						edit:true,  
						editTemplate:"<textarea class='form-control' #ng-model rows='1'></textarea>",
						choiceInList:false
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
			        {	property:"relatifName",
						header: Messages("rawData.relatifName"),
						type :"text",		    	  	
						order:true
					},
					{	property:"collabFileName",
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
		$scope.messages = messages();
		$scope.messages.clear(); 
		$scope.countAnalysis = 0;
		$scope.countExperiment = 0;
		
		// Recuperer le sample en passant par get :	
		//console.log("routeParams.code=", $routeParams);
		$http.get(jsRoutes.controllers.sra.samples.api.Samples.get($routeParams.code).url).success(function(data){
			//console.log("nbre de sample:"+data.length);
	
			$scope.sample = data;
			//console.log("sample.accession=", $scope.sample.accession);

			// Ajout des onglets à gauche si rafraichissement page
			if(tabService.getTabs().length == 0){			
				tabService.addTabs({label:$scope.sample.code,href:jsRoutes.controllers.sra.samples.tpl.Samples.get($scope.sample.code).url,remove:true});
				tabService.activeTab($scope.getTabs(0)); // active l'onglet indiqué, le met en bleu.
			}
		
		   $scope.sampleAccession= "NONE";
		   if (toolsServices.isNotBlank($scope.sample.accession)) {
				$scope.sampleAccession = $scope.sample.accession;
		   }
			// recuperation des analyses avec sampleAccession:
			$http.get(jsRoutes.controllers.sra.analyzes.api.Analyzes.list().url, {params: {sampleIdentifier:$scope.sampleAccession}}).success(function(data) {
				//console.log("sample.accession=", $scope.sample.accession);
				$scope.analyzes = data;
				$scope.countAnalysis = $scope.analyzes.length;
				//console.log("analysis liés au sample ", $scope.analyzes);
				//Init datatable
				$scope.analysisDT = datatable(analyzesDTConfig);
				$scope.analysisDT.setData($scope.analyzes, $scope.analyzes.length);
				var maListRawDatas = [];
				for (var i=0; i<$scope.analyzes.length; i++) {
					if($scope.analyzes[i].listRawData != null) {
						for (var j=0; j<$scope.analyzes[i].listRawData.length; j++) {
							maListRawDatas.push($scope.analyzes[i].listRawData[j]);
						}
					}
				}
									
				//Get experiments (and runs)
				$http.get(jsRoutes.controllers.sra.experiments.api.Experiments.list().url, {params: {sampleCode:$routeParams.code}}).success(function(data) {
					//console.log("$routeParams.code:"+$routeParams.code);
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
					//var maListRawDatas = [];
					for (var i=0; i<$scope.experiments.length; i++) {
						var run = $scope.experiments[i].run;
						for (var j=0; j<run.listRawData.length; j++) {
							maListRawDatas.push(run.listRawData[j]);
						}
					}
					
					$scope.rawDataDT = datatable(rawDatasDTConfig);
					$scope.rawDataDT.setData(maListRawDatas, maListRawDatas.length);
				});	
			});
  		});		
	}; 

	init();
		
	/* buttons section */
	

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
		return false; // Dans la vue details, on n'est jamais en mode creation d'un sample, le sample existe bien en base.
	};
	
	
}]).controller('CommentsCtrl',['$scope','$sce', '$http','lists','$parse','$filter','datatable', 
                               function($scope,$sce,$http,lists,$parse,$filter,datatable) {

	$scope.currentComment = {comment:undefined};
	//console.log("Dans CommentsCtrl, scope.sample=", $scope.sample);
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
		//console.log("Dans le save de CommentsCtrl, scope.sample = ", $scope.sample );
		//console.log("Dans le save de CommentsCtrl, scope.currentComment = ", $scope.currentComment );

		if($scope.isCreationMode()){
			$scope.sample.comments.push($scope.currentComment);
			$scope.currentComment = {comment:undefined};
		}else{
			$scope.messages.clear();
			$http.post(jsRoutes.controllers.sra.samples.api.SampleComments.save($scope.sample.code).url, $scope.currentComment)
			.success(function(data, status, headers, config) {
				if(data!=null){
					$scope.messages.setSuccess("save");
					$scope.sample.comments.push(data);
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
			$scope.sample.comments[$scope.index] = $scope.currentComment;
			$scope.currentComment = {comment:undefined};
			$scope.index = undefined;			
		}else{	
			$scope.messages.clear();
			$http.put(jsRoutes.controllers.sra.samples.api.SampleComments.update($scope.sample.code, $scope.currentComment.code).url, $scope.currentComment)
			.success(function(data, status, headers, config) {
				if(data!=null){
					$scope.messages.setSuccess("save");
					$scope.sample.comments[$scope.index] = $scope.currentComment;
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
			$scope.sample.comments.splice(index, 1);
		}else if (confirm(Messages("comments.remove.confirm"))) {
			$scope.messages.clear();
			$http.delete(jsRoutes.controllers.sra.samples.api.SampleComments.delete($scope.sample.code, comment.code).url)
			.success(function(data, status, headers, config) {
				if(data!=null){
					$scope.messages.setSuccess("save");
					$scope.currentComment = {comment:undefined};
					$scope.sample.comments.splice(index, 1);
				}
			})
			.error(function(data, status, headers, config) {
				$scope.messages.setError("remove");
				$scope.messages.setDetails(data);				
			});
		}
	};
}]);
	


