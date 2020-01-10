"use strict";

angular.module('home').controller('ConsultationCtrl',[ '$http', '$scope', '$routeParams' , '$q', 'mainService', 'lists', 'tabService','messages','studiesConsultationService',
	                                                  function($http, $scope, $routeParams, $q, mainService, lists, tabService, messages, studiesConsultationService) { 


	
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
				active:true, // permettre edition des champs editables
				showButton : true,// bouton d'edition visible
				withoutSelect : true,
				columnMode : true,
				lineMode : function(line){
					if(line.state.code === "N")
						return true;
					else 
						return false;
				}
			},
			
			cancel : {
				showButton:true
			},
			hide:{
				active:true,
				showButton:true
			},
			exportCSV:{
				active:true
			},
			show:{                   // bouton pour epingler si on passe par details-ctrl.js 
				active:true,
				add :function(line){
					tabService.addTabs({label:line.code,href:jsRoutes.controllers.sra.studies.tpl.Studies.get(line.code).url,remove:true});
				}
			},
			save : {
				active:true,
				showButton : true,
				changeClass : false,
				url:function(line){
					return jsRoutes.controllers.sra.studies.api.Studies.update(line.code).url; // jamais utilisé si mode local
				},
				method:'put',
				value:function(line){
					return line;
				},
			},

	};
	

	
	$scope.messages = messages();	

	
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('consultation');
		tabService.addTabs({label:Messages('studies.menu.consultation'),href:jsRoutes.controllers.sra.studies.tpl.Studies.home("consultation").url,remove:true});
		tabService.activeTab(0); // desactive le lien !
	}
	// si on declare dans services => var sraVariables = {};
	// si on declare dans le controlleur : $scope.sraVariables = {};

	$scope.consultationService = studiesConsultationService;
	$scope.consultationService.init($routeParams, studiesDTConfig);
	
	//$scope.search = function(){
		//if(($scope.consultationService.form.projCodes && $scope.consultationService.form.projCodes.length > 0)||
		//($scope.consultationService.form.accessions && $scope.consultationService.form.accessions.length > 0)||
		//($scope.consultationService.form.codes && $scope.consultationService.form.codes.length > 0)){
		//	console.log($scope.consultationService.form.accessions)
		//	$scope.consultationService.search();
		//} else {
		//	console.log("Cancel datatable");
		//	$scope.consultationService.cancel();
		//}	
	//};
	
	
	

}]);
