"use strict";

angular.module('home').controller('ConsultationCtrl',[ '$http', '$scope', '$routeParams' , '$q', 'mainService', 'lists', 'tabService','messages','configurationsConsultationService',
	                                                  function($http, $scope, $routeParams, $q, mainService, lists, tabService, messages, configurationsConsultationService) { 

	
	var configurationDTConfig = {
			name:'configurationDT',
			order :{by:'code',mode:'local', reverse:true},
			search:{
				url:jsRoutes.controllers.sra.configurations.api.Configurations.list()
			},
			pagination:{
				active:true,
				mode:'local',
                numberRecordsPerPage: 100
			},
			select:{active:false},
			showTotalNumberRecords:true,
		
			edit : {
				active: false, // permettre edition des champs editables
				showButton : false,// bouton d'edition visible
				withoutSelect : true,
				columnMode : true,
				lineMode : function(line){
					if(line.state.code === "NONE")
						return true;
					else 
						return false;
				}
			},
						
			
			cancel : {
				showButton:false
			},
			hide:{
				active:true,
				showButton:true
			},
			exportCSV:{
				active:true
			},
			/*show:{                   // bouton pour epingler si on passe par details-ctrl.js 
				active:true,
				add :function(line){
					tabService.addTabs({label:line.code,href:jsRoutes.controllers.sra.configurations.tpl.Configurations.get(line.code).url,remove:true});
				}
			},*/
			save : {
				active:false,
				showButton : false,
				changeClass : false,
				url:function(line){
					return jsRoutes.controllers.sra.configurations.api.Configurations.update(line.code).url; // jamais utilisé en mode local
				},
				method:'put',
				value:function(line){
					return line;
				},
			}
	};
		

	
	$scope.messages = messages();	// enleve le message mais pas la partie details + efficace d'utiliser $scope.messages.clear();
	$scope.messages.clear();
	
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('consultation');
		tabService.addTabs({label:Messages('configurations.menu.consultation'),href:jsRoutes.controllers.sra.configurations.tpl.Configurations.home("consultation").url,remove:true});
		tabService.activeTab(0); //  active l'onglet en le mettant en bleu
	}
	// si on declare dans services => var sraVariables = {};
	// si on declare dans le controlleur : $scope.sraVariables = {};

	$scope.consultationService = configurationsConsultationService;	
	$scope.consultationService.init($routeParams, configurationDTConfig);
	
	$scope.search = function(){
		$scope.messages = messages();	// enleve le message mais pas la partie details + efficace d'utiliser $scope.messages.clear();
		$scope.messages.clear();
		if($scope.consultationService.form.projCodes && $scope.consultationService.form.projCodes.length > 0){
			$scope.consultationService.search();
		} else {
			//console.log("Cancel datatable");
			$scope.consultationService.cancel();
		}	
	};

}]);
