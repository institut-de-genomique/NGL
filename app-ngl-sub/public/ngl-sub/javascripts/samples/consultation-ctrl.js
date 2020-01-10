"use strict";

angular.module('home').controller('ConsultationCtrl',[ '$http', '$scope', '$routeParams' , '$q', 'mainService', 'lists', 'tabService','messages','samplesConsultationService',
	                                                  function($http, $scope, $routeParams, $q, mainService, lists, tabService, messages, samplesConsultationService) { 


	
	var samplesDTConfig = {
			name:'samplesDT',
			order :{by:'code',mode:'local', reverse:true},
			search:{
				url:jsRoutes.controllers.sra.samples.api.Samples.list()
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
			/*show:{                   // bouton pour epingler si on passe par details-ctrl.js 
				active:true,
				add :function(line){
					tabService.addTabs({label:line.code,href:jsRoutes.controllers.sra.samples.tpl.Samples.get(line.code).url,remove:true});
				}
			},*/
			
			save : {
				active:true,
				showButton : true,
				changeClass : false,
				url:function(line){
					return jsRoutes.controllers.sra.samples.api.Samples.update(line.code).url; // jamais utilisé si mode local
				},
				method:'put',
				mode:'remote',
				value:function(line){
					return line;
				},
				callback : function(datatable, errors) {
				}
			}
			

	};
	
	$scope.messages = messages();	
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('consultation');
		tabService.addTabs({label:Messages('samples.menu.consultation'),href:jsRoutes.controllers.sra.samples.tpl.Samples.home("consultation").url,remove:true});
		tabService.activeTab(0); // desactive le lien !
	}
	// si on declare dans services => var sraVariables = {};
	// si on declare dans le controlleur : $scope.sraVariables = {};
	
	$scope.sraVariables = samplesConsultationService.sraVariables; // ligne importante sinon ne marche pas pour affecter sraVariables dans services
	$scope.consultationService = samplesConsultationService;
	$scope.consultationService.init($routeParams, samplesDTConfig);

}]);
