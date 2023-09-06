"use strict";

angular.module('home').controller('DetailsCtrl',[ '$http', '$scope', '$routeParams' , 'mainService', 'lists', 'tabService','messages','datatable',
                                                  function($http, $scope, $routeParams, mainService, lists, tabService, messages, datatable) { 


	var rawDatasDTConfig = {	
			name:'rawDataDT',
			order :{by:'code',mode:'local', reverse:true},
			pagination:{
				active:true,
				mode:'local'
			},			
			showTotalNumberRecords:true,
			select:{active: false, showButton: false},
			cancel:{showButton:false},
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
				{property:"relatifName",
					header: Messages("rawData.relatifName"),
					type :"text",		    	  	
					order:true
				},
				{property:"collabFileName",
					header: Messages("rawData.collabFileName"),
					type :"text",		    	  	
					order:true
				},
				{property:"readsetCode",
					header: Messages("rawData.readsetCode"),
					type :"text",		    	  	
					order:true
				}	      
		    ]	        
	};	

	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('consultation');
		tabService.addTabs({label:Messages('analyzes.menu.consultation'),href:jsRoutes.controllers.sra.analyzes.tpl.Analyzes.home("consultation").url,remove:true});
		tabService.activeTab(0);  //active l'onglet en le mettant en bleu
	}	
	var init = function() {
		$scope.mainService = mainService;
		$scope.messages = messages();	// enleve le message mais pas la partie details + efficace d'utiliser $scope.messages.clear();
		$scope.messages.clear();
		$scope.analysis = null;
       	//console.log("routeParams.code:"+$routeParams.code);
       	$scope.rawDataDT = [];

		$http.get(jsRoutes.controllers.sra.analyzes.api.Analyzes.get($routeParams.code).url).success(function(data) {
			$scope.analysis = data;	
			//console.log("scope.analysis=", $scope.analysis);
			var maListRawDatas = [];
			if ( $scope.analysis != null && $scope.analysis.hasOwnProperty("listRawData") && $scope.analysis.listRawData != null) {
				if ($scope.analysis.listRawData.length > 0) {
					for (var j=0; j<$scope.analysis.listRawData.length; j++) {
						maListRawDatas.push($scope.analysis.listRawData[j]);
					}
				}
			}
			$scope.rawDataDT = datatable(rawDatasDTConfig);
			$scope.rawDataDT.setData(maListRawDatas, maListRawDatas.length);
			//console.log("scope.rawDataDT=", $scope.rawDataDT);
		}).error(function(data){
			$scope.messages.addDetails(data); 
			$scope.messages.setError("save");
		});
		
	};

	init();
	
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('details');
		tabService.addTabs({label:Messages('analyzes.menu.details'),href:jsRoutes.controllers.sra.analyzes.tpl.Analyzes.home("details").url,remove:true});
		tabService.activeTab(0);  // active l'onglet en le mettant en bleu
	}	
	
	$scope.isRawData = function() {
		var value = false;
		//console.log("Dans isRawData, scope.analysis = ", $scope.analysis);
		if ( $scope.analysis != null && $scope.analysis.hasOwnProperty("listRawData") && $scope.analysis.listRawData != null) {
			if ($scope.analysis.listRawData.length > 0) {
				value = true;
			}
		}
		//console.log("Dans isRawData, value = ", value);
		return value;

	};
	
	// methode utilisée dans le controller CommentCtrl
	$scope.isCreationMode=function() {
		//console.log("Dans isCreationMode");
		return false; // Dans la vue details, on n'est jamais en mode creation d'une soumission, la soumission existe bien en base.
	};		

}]).controller('CommentsCtrl',['$scope','$sce', '$http','lists','$parse','$filter','datatable', 
                               function($scope,$sce,$http,lists,$parse,$filter,datatable) {

	$scope.currentComment = {comment:undefined};
	//console.log("Dans CommentsCtrl, scope.analysis=", $scope.analysis);
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
		//console.log("Dans le save de CommentsCtrl, scope.analysis = ", $scope.analysis );
		//console.log("Dans le save de CommentsCtrl, scope.currentComment = ", $scope.currentComment );

		if($scope.isCreationMode()){
			$scope.analysis.comments.push($scope.currentComment);
			$scope.currentComment = {comment:undefined};
		}else{
			$scope.messages.clear();
			$http.post(jsRoutes.controllers.sra.analyzes.api.AnalysisComments.save($scope.analysis.code).url, $scope.currentComment)
			.success(function(data, status, headers, config) {
				if(data!=null){
					$scope.messages.setSuccess("save");
					$scope.analysis.comments.push(data);
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
			$scope.analysis.comments[$scope.index] = $scope.currentComment;
			$scope.currentComment = {comment:undefined};
			$scope.index = undefined;			
		}else{	
			$scope.messages.clear();
			$http.put(jsRoutes.controllers.sra.analyzes.api.AnalysisComments.update($scope.analysis.code, $scope.currentComment.code).url, $scope.currentComment)
			.success(function(data, status, headers, config) {
				if(data!=null){
					$scope.messages.setSuccess("save");
					$scope.analysis.comments[$scope.index] = $scope.currentComment;
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
			$scope.analysis.comments.splice(index, 1);
		}else if (confirm(Messages("comments.remove.confirm"))) {
			$scope.messages.clear();
			$http.delete(jsRoutes.controllers.sra.analyzes.api.AnalysisComments.delete($scope.analysis.code, comment.code).url)
			.success(function(data, status, headers, config) {
				if(data!=null){
					$scope.messages.setSuccess("save");
					$scope.currentComment = {comment:undefined};
					$scope.analysis.comments.splice(index, 1);
				}
			})
			.error(function(data, status, headers, config) {
				$scope.messages.setError("remove");
				$scope.messages.setDetails(data);				
			});
		}
	};
}]);

