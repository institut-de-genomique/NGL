"use strict";

angular.module('home').controller('ReleaseCtrl',[ '$http', '$scope', '$routeParams' , '$q', 'mainService', 'lists', 'tabService','messages','studiesReleaseService',
	                                                  function($http, $scope, $routeParams, $q, mainService, lists, tabService, messages, studiesReleaseService) { 



	
	var studiesDTConfig = {
			name:'studiesDT',
			order :{by:'code',mode:'local', reverse:true},
			search:{
				url:jsRoutes.controllers.sra.studies.api.Studies.list()
			},
			pagination:{active:false},
			select:{active:true},
			showTotalNumberRecords:false,
			edit : {
				active:false, // permettre edition des champs editables
				showButton : false,// bouton d'edition visible
				withoutSelect : true,
				columnMode : true,
				lineMode : function(line){
					return true;
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
				active:false
			},
			show:{                   // bouton pour epingler si on passe par details-ctrl.js 
				active:true,
				add :function(line){
					tabService.addTabs({label:line.code,href:jsRoutes.controllers.sra.studies.tpl.Studies.get(line.code).url,remove:true});
				}
			},
			

	};
	

	
	$scope.messages = messages();	

	
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('release');
		tabService.addTabs({label:Messages('studies.menu.release'),href:jsRoutes.controllers.sra.studies.tpl.Studies.home("release").url,remove:true});
		tabService.activeTab(0); // desactive le lien !
	}
	// si on declare dans services => var sraVariables = {};
	// si on declare dans le controlleur : $scope.sraVariables = {};

	$scope.releaseService = studiesReleaseService;
	$scope.releaseService.init($routeParams, studiesDTConfig);
	$scope.search = function(){
		if($scope.releaseService.form.projCodes && $scope.releaseService.form.projCodes.length > 0){
			$scope.releaseService.search();
		} else {
			console.log("Cancel datatable");
			$scope.releaseService.cancel();
		}	
	};
	
 
}]);
