"use strict";

angular.module('home').controller('DetailsCtrl',[ '$http', '$scope', '$routeParams' , 'mainService', 'lists', 'tabService','messages','datatable',
                                                  function($http, $scope, $routeParams, mainService, lists, tabService, messages, datatable) { 

		var runsDTConfig = {
			name:'runDT',
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
				columnMode : true
			},
			exportCSV:{
				active:true
			},
			columns : [
				{property:"code",
					header: Messages("run.code"),
					type :"text",		    	  	
					order:true
				},
				{property:"accession",
					header: Messages("run.accession"),
					type :"text",		    	  	
					order:true
				},
				{property:"expCode",
					header: Messages("run.experiment.code"),
					type :"text",		    	  	
					order:true
				},
				{property:"runDate",
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
				active:false,
				mode:'local'
			},
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
				active:true
			},
			//url:function(lineValue){
			//		return jsRoutes.controllers.sra.experiments.api.Experiments.update(lineValue.code).url; // jamais utilisé en mode local
			//	},
			
//			remove : {
//				active:false,
//				withEdit:false, //to authorize to remove a line in edition mode
//				showButton : false,
//				mode:'local', //or local
//				url:function(lineValue) { 
//					return jsRoutes.controllers.sra.experiments.api.ExperimentsRawDatas.delete(lineValue.experimentCode, lineValue.relatifName).url;
//				},
//				callback : undefined, //used to have a callback after remove all element. the datatable is pass to callback method and number of error
//				start:false,
//				counter:0,
//				number:0, //number of element in progress
//				error:0								
//			},
			
			columns : [
				{property:"relatifName",
					header: Messages("rawData.relatifName"),
					type :"text",		    	  	
					order:true
				},
				{property:"collabFileName",
					header: Messages("rawData.collabFileName"),
					type :"text",		    	  	
					order:true
				}
				]	        
	};	


	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('consultation');
		tabService.addTabs({label:Messages('experiments.menu.consultation'),href:jsRoutes.controllers.sra.experiments.tpl.Experiments.home("consultation").url,remove:true});
		tabService.activeTab(0);  //active l'onglet en le mettant en bleu
	}	
	var init = function() {
		$scope.mainService = mainService;
		$scope.messages = messages();	// enleve le message mais pas la partie details + efficace d'utiliser $scope.messages.clear();
		$scope.messages.clear();
		$scope.experiment = null;
       	//console.log("routeParams.code:"+$routeParams.code);
       	$scope.rawDataDT = [];
       	$scope.runDT = [];
		var run = null;
		// Recuperer experiment en passant par get : 
		/* code execute qd il a le temps donc si des choses à faire 
		une fois le run recupéré alors le faire dans fonction success ou bien utiliser watcher
		pour le faire apres execution de la requete
		*/
		 	
		$http.get(jsRoutes.controllers.sra.experiments.api.Experiments.get($routeParams.code).url).success(function(data) {
			$scope.experiment = data;
			run = $scope.experiment.run;
			//console.log("YYYYYYYYYYYYYYYYYYYY run.code = " );//+ run.code);
			//console.log("YYYYYYYYYYYYYYYYYYYY run.code = " + run.code);
			//console.log("YYYYYYYYYYYYYYYYYYYY run.accession = " + run.accession);
			
			var maListRawDatas = [];
			for (var j=0; j<run.listRawData.length; j++) {
				maListRawDatas.push(run.listRawData[j]);
			}
			var runs = [];
			runs.push(run);
			//console.log("runs=", runs);
			$scope.runDT = datatable(runsDTConfig);
			$scope.runDT.setData(runs, runs.length);
			//console.log("scope.runDT", $scope.runDT);
		
			$scope.rawDataDT = datatable(rawDatasDTConfig);
			$scope.rawDataDT.setData(maListRawDatas, maListRawDatas.length);
			//console.log("scope.rawDataDT", $scope.rawDataDT);

		}).error(function(data){
			$scope.messages.addDetails(data); 
			$scope.messages.setError("save");
		});
		
	};

	init();
	
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('details');
		tabService.addTabs({label:Messages('experiments.menu.details'),href:jsRoutes.controllers.sra.experiments.tpl.Experiments.home("details").url,remove:true});
		tabService.activeTab(0);  // active l'onglet en le mettant en bleu
	}	
	// methode utilisée dans le controller CommentCtrl
	$scope.isCreationMode=function() {
		//console.log("Dans isCreationMode");
		return false; // Dans la vue details, on n'est jamais en mode creation d'une soumission, la soumission existe bien en base.
	};		

}]).controller('CommentsCtrl',['$scope','$sce', '$http','lists','$parse','$filter','datatable', 
                               function($scope,$sce,$http,lists,$parse,$filter,datatable) {

	$scope.currentComment = {comment:undefined};
	//console.log("Dans CommentsCtrl, scope.experiment=", $scope.experiment);
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
		//console.log("Dans le save de CommentsCtrl, scope.experiment = ", $scope.experiment );
		//console.log("Dans le save de CommentsCtrl, scope.currentComment = ", $scope.currentComment );

		if($scope.isCreationMode()){
			$scope.experiment.comments.push($scope.currentComment);
			$scope.currentComment = {comment:undefined};
		}else{
			$scope.messages.clear();
			$http.post(jsRoutes.controllers.sra.experiments.api.ExperimentComments.save($scope.experiment.code).url, $scope.currentComment)
			.success(function(data, status, headers, config) {
				if(data!=null){
					$scope.messages.setSuccess("save");
					$scope.experiment.comments.push(data);
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
			$scope.experiment.comments[$scope.index] = $scope.currentComment;
			$scope.currentComment = {comment:undefined};
			$scope.index = undefined;			
		}else{	
			$scope.messages.clear();
			$http.put(jsRoutes.controllers.sra.experiments.api.ExperimentComments.update($scope.experiment.code, $scope.currentComment.code).url, $scope.currentComment)
			.success(function(data, status, headers, config) {
				if(data!=null){
					$scope.messages.setSuccess("save");
					$scope.experiment.comments[$scope.index] = $scope.currentComment;
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
			$scope.experiment.comments.splice(index, 1);
		}else if (confirm(Messages("comments.remove.confirm"))) {
			$scope.messages.clear();
			$http.delete(jsRoutes.controllers.sra.experiments.api.ExperimentComments.delete($scope.experiment.code, comment.code).url)
			.success(function(data, status, headers, config) {
				if(data!=null){
					$scope.messages.setSuccess("save");
					$scope.currentComment = {comment:undefined};
					$scope.experiment.comments.splice(index, 1);
				}
			})
			.error(function(data, status, headers, config) {
				$scope.messages.setError("remove");
				$scope.messages.setDetails(data);				
			});
		}
	};
}]);

