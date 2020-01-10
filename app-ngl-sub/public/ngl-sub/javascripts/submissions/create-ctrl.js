"use strict";

angular.module('home').controller('CreateCtrl',[ '$http', '$scope', '$routeParams' , 'mainService', 'lists', 'tabService','submissionsCreateService','messages',
                                                 function($http, $scope, $routeParams, mainService, lists, tabService, submissionsCreateService, messages) { 
  
	var submissionDTConfig = {
			pagination:{mode:'local'},			
			order :{mode:'local', by:'code', reverse : true},
			search:{
				url:jsRoutes.controllers.sra.submissions.api.Submissions.list()
			},
			show:{
				active:true,
				add :function(line){
					tabService.addTabs({label:line.code,href:jsRoutes.controllers.sra.submissions.tpl.Submissions.get(line.code).url,remove:true});
				}
			},
			hide:{
				active:true
			},
			name:"Submissions"
	};
	

	$scope.messages = messages();
	
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('create');
		tabService.addTabs({label:Messages('submissions.menu.create'),href:jsRoutes.controllers.sra.submissions.tpl.Submissions.home("create").url,remove:true});
		tabService.activeTab(0); // desactive le lien !
	}
	
	$scope.createService = submissionsCreateService;	
	$scope.createService.init($routeParams, submissionDTConfig);
	
	$scope.save = function(){
		$scope.createService.form.base64UserFileClonesToAc=""; // important si le fichier utilisateur ne peut pas ou ne doit pas etre chargé que form.base64File soit
		// mis à chaine vide et non à null pour l'appel de l'url sra/api/submissions
		
		if ($scope.createService.userFileClonesToAc !==null && $scope.createService.userFileClonesToAc !== undefined) {
			$scope.createService.form.base64UserFileClonesToAc=$scope.createService.userFileClonesToAc.value;
		}
		 
		$scope.createService.form.base64UserFileExperiments=""; 
		if ($scope.createService.userFileExperiments !==null && $scope.createService.userFileExperiments !== undefined) {
			$scope.createService.form.base64UserFileExperiments=$scope.createService.userFileExperiments.value;
		} 		
		$scope.createService.form.base64UserFileSamples=""; 
		if ($scope.createService.userFileSamples !==null && $scope.createService.userFileSamples !== undefined) {
			$scope.createService.form.base64UserFileSamples=$scope.createService.userFileSamples.value;
		} 
		
		$scope.createService.form.base64UserFileReadSet="";
		if ($scope.createService.userFileReadSet !==null && $scope.createService.userFileReadSet !== undefined) {
			$scope.createService.form.base64UserFileReadSet=$scope.createService.userFileReadSet.value;
		} 		
		mainService.setForm($scope.createService.form);
		//$scope.createService.search();
			$http.post(jsRoutes.controllers.sra.submissions.api.Submissions.save().url, mainService.getForm()).success(function(data) {
				$scope.messages.clear();
				$scope.messages.clazz="alert alert-success";
				$scope.messages.text=Messages('submissions.msg.save.success')+" : "+data;
				$scope.messages.open();
				$scope.codeSubmission=data;
				$scope.createService.search();
				$scope.createService.userFileExperiments=null;
				$scope.createService.userFileSamples=null;
				$scope.createService.userFileCloneToAc=null;
				$scope.createService.userFileReadSet=null;
				$scope.createService.acStudy=null;
		        $scope.createService.acSample=null;
			}).error(function(data){
				//$scope.messages.setDetails({"error":{"code":"value","code2":"value2"}});
				$scope.messages.setDetails(data);
				$scope.messages.setError("save");
				//$scope.messages.clear();
				$scope.createService.userFileExperiments=null;
				$scope.createService.userFileSamples=null;
				$scope.createService.userFileCloneToAc=null;
				$scope.createService.userFileReadSet=null;
				$scope.createService.acStudy=null;
				$scope.createService.acSample=null;
				angular.element('#idUserFileReadSet')[0].value = null;
				angular.element('#idUserFileClonesToAc')[0].value = null;	
			});
	};
	
	$scope.reset = function(){
		$scope.createService.resetForm(); // on initialise à null toutes les variables recuperees dans create-ctrl.js dans code : ng-model="createService.form
		$scope.createService.userFileExperiments=null;
		$scope.createService.userFileSamples=null;
		$scope.createService.userFileCloneToAc=null;
		$scope.createService.userFileReadSet=null;
		$scope.createService.acStudy=null;
		$scope.createService.acSample=null;
		angular.element('#idUserFileReadSet')[0].value = null;
		angular.element('#idUserFileClonesToAc')[0].value = null;	
		//$scope.messages.clear();
	};	
	
}]);


