"use strict";

angular.module('home').controller('DetailsCtrl',[ '$http', '$scope', '$routeParams' , 'mainService', 'lists', 'tabService','messages', 'toolsServices', 'datatable',
                                                  function($http, $scope, $routeParams, mainService, lists, tabService, messages, toolsServices, datatable) { 



	
	var init = function(userName){
		$scope.mainService = mainService;
		$scope.messages = messages();
		$scope.messages.clear(); 

		// Recuperer le project en passant par get :		
		$http.get(jsRoutes.controllers.sra.projects.api.Projects.get($routeParams.code).url).success(function(data){
	
			$scope.project = data;
			//console.log("project.accession=", $scope.project.accession);
			$scope.strIdsPubmed = "";
			//console.log("$scope.study.idsPubmed", $scope.study.idsPubmed);
			//console.log("typeOf $scope.study.idsPubmed", typeof $scope.study.idsPubmed);

			if ($scope.project.idsPubmed != null) {
				for (var j = 0; j < $scope.project.idsPubmed.length; j++) {
					//console.log("XXX  idPubmed=", $scope.project.idsPubmed[j]);
					$scope.strIdsPubmed = $scope.strIdsPubmed + $scope.project.idsPubmed[j] + "," ;
				}
				$scope.strIdsPubmed = $scope.strIdsPubmed.replace(/,\s*$/, "");
			}
		
			// Ajout des onglets à gauche si rafraichissement page
			if(tabService.getTabs().length == 0){			
				tabService.addTabs({label:$scope.project.code,href:jsRoutes.controllers.sra.projects.tpl.Projects.get($scope.project.code).url,remove:true});
				tabService.activeTab($scope.getTabs(0)); // active l'onglet indiqué, le met en bleu.
			}
	    });		
	}; 

	init();
		
	/* buttons section */
	

	$scope.cancel = function(){
		//console.log("call cancel");
		$scope.messages = messages();
		$scope.messages.clear();	
	};
	

	// methode utilisée dans le controller CommentCtrl
	$scope.isCreationMode=function() {
		//console.log("Dans isCreationMode");
		return false; // Dans la vue details, on n'est jamais en mode creation d'un project, le project existe bien en base.
	};
	
}]).controller('CommentsCtrl',['$scope','$sce', '$http','lists','$parse','$filter','datatable', 
                               function($scope,$sce,$http,lists,$parse,$filter,datatable) {

	$scope.currentComment = {comment:undefined};
	//console.log("Dans CommentsCtrl, scope.project=", $scope.project);
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
		//console.log("Dans le save de CommentsCtrl, scope.project = ", $scope.project );
		//console.log("Dans le save de CommentsCtrl, scope.currentComment = ", $scope.currentComment );

		if($scope.isCreationMode()){
			$scope.project.comments.push($scope.currentComment);
			$scope.currentComment = {comment:undefined};
		}else{
			$scope.messages.clear();
			$http.post(jsRoutes.controllers.sra.projects.api.ProjectComments.save($scope.project.code).url, $scope.currentComment)
			.success(function(data, status, headers, config) {
				if(data!=null){
					$scope.messages.setSuccess("save");
					$scope.project.comments.push(data);
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
			$scope.project.comments[$scope.index] = $scope.currentComment;
			$scope.currentComment = {comment:undefined};
			$scope.index = undefined;			
		}else{	
			$scope.messages.clear();
			$http.put(jsRoutes.controllers.sra.projects.api.ProjectComments.update($scope.project.code, $scope.currentComment.code).url, $scope.currentComment)
			.success(function(data, status, headers, config) {
				if(data!=null){
					$scope.messages.setSuccess("save");
					$scope.project.comments[$scope.index] = $scope.currentComment;
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
			$scope.project.comments.splice(index, 1);
		}else if (confirm(Messages("comments.remove.confirm"))) {
			$scope.messages.clear();
			$http.delete(jsRoutes.controllers.sra.projects.api.ProjectComments.delete($scope.project.code, comment.code).url)
			.success(function(data, status, headers, config) {
				if(data!=null){
					$scope.messages.setSuccess("save");
					$scope.currentComment = {comment:undefined};
					$scope.project.comments.splice(index, 1);
				}
			})
			.error(function(data, status, headers, config) {
				$scope.messages.setError("remove");
				$scope.messages.setDetails(data);				
			});
		}
	};
}]);
	


