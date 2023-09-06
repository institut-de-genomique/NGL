"use strict";

angular.module('home').controller('DetailsCtrl',[ '$http', '$scope', '$routeParams' , 'mainService', 'lists', 'tabService','messages','datatable',
                                                  function($http, $scope, $routeParams, mainService, lists, tabService, messages, datatable) { 


	var configurationsDTConfig = {
			name:'experimentDT',
			order :{by:'code',mode:'local', reverse:true},
			search:{active:false},
			pagination:{
				active:false,// important de mettre à false pour modification en masse ou via fichier
				mode:'local',
                numberRecordsPerPage: 100
			},
			select:{active:true},
			showTotalNumberRecords:false,
			edit : {
				active:false,  // au demarrage savoir si editable ou non
				showButton : true, // bouton visible
				withoutSelect : true,
				columnMode : true
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
			save : {
				active:true,
				showButton : true,
				changeClass : false,
				url:function(line){
					return jsRoutes.controllers.sra.configurations.api.Configurations.update(line.code).url; // jamais utilisé en mode local
				},
				method:'put',
				value:function(line){
					return line;
				},
			},
			columns : [
			        {property:"traceInformation.creationDate",
			        	header: "traceInformation.creationDate",
			        	type :"date",		    	  	
			        	order:true
			        },	
			        {property:"code",
			        	header: "code",
			        	type :"text",		    	  	
			        	order:true
			        },	
			        {property:"projectCodes",
			        	header: "projectCodes",
			        	type :"text",		    	  	
			        	order:false,
			        	edit:false,
			        	choiceInList:false  
			        },
			        {property:"strategySample",
						header: "strategySample",
						type :"String",
			        	hide:true,
			        	edit:true,
						order:false,
				    	choiceInList:true,
				    	listStyle:'bt-select-multiple',
				    	possibleValues:'sraVariables.strategySample',
				    },
			        {property:"strategyStudy",
						header: "strategyStudy",
						type :"String",
			        	hide:true,
			        	edit:true,
						order:false,
				    	choiceInList:true,
				    	listStyle:'bt-select-multiple',
				    	possibleValues:'sraVariables.strategyStudy',
				    },
				   	{property:"librarySelection",
						header: "librarySelection",
						type :"String",
			        	hide:true,
			        	edit:true,
						order:false,
				    	choiceInList:true,
				    	listStyle:'bt-select-multiple',
				    	possibleValues:'sraVariables.librarySelection',
				    },
				    {property:"libraryStrategy",
						header: "libraryStrategy",
						type :"String",
						hide:true,
						edit:true,
						order:false,
						choiceInList:true,
						listStyle:'bt-select-multiple',
						possibleValues:'sraVariables.libraryStrategy',
				    },
					{property:"librarySource",
						header: "librarySource",
						type :"String",
						hide:true,
						edit:true,
						order:false,
						choiceInList:true,
						listStyle:'bt-select-multiple',
						possibleValues:'sraVariables.librarySource',
					},
					{property:"libraryConstructionProtocol",
						 header: "libraryConstructionProtocol",
						 type :"String",		    	  	
						 hide:true,
						 edit:true,
					},

					 {property:"state.code",
					 	  "filter":"codes:'state'",
			        	  header: "state",
			        	  type :"text",		    	  	
			        	  order:false,
			        	  edit:false,
			        	  choiceInList:false
			        }
			 ]	        
	};

	
/*	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('create');
		tabService.addTabs({label:Messages('configurations.menu.create'),href:jsRoutes.controllers.sra.configurations.tpl.Configurations.home("create").url,remove:true});
		tabService.activeTab(0); //  active l'onglet en le mettant en bleu
	}
	*/
	// si on declare dans services.js => var sraVariables = {};
	// si on declare dans le controlleur : $scope.sraVariables = {};

	$scope.sraVariables = {};
	
	
	var init = function(){
		$scope.messages = messages();	// enleve le message mais pas la partie details + efficace d'utiliser $scope.messages.clear();
		$scope.messages.clear();
		$scope.mainService = mainService;

		// Recuperation dans data de l'objet configuration stokee dans database dont le code est indique en parametre.
		// Attention appel de get du controller api.sra.configurations qui est herite
		$http.get(jsRoutes.controllers.sra.configurations.api.Configurations.get($routeParams.code).url).success(function(data) {

			$scope.configuration = data;	
			console.log("$routeParams.code:"+$routeParams.code);
			console.log("configuration.code :"+$scope.configuration.code);

			$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'librarySelection'}})
			.success(function(data) {
				// initialisation de la variable sraVariables.librarySelection
				$scope.sraVariables.librarySelection = data;
			});	
		
			$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'libraryStrategy'}})
			.success(function(data) {
				// initialisation de la variable sraVariables.libraryStrategy 
				$scope.sraVariables.libraryStrategy = data;
			});
		
			$http.get(jsRoutes.controllers.sra.api.Variables.list().url, {params:{type:'librarySource'}})
			.success(function(data) {
				// initialisation de la variable sraVariables.librarySource 
				$scope.sraVariables.librarySource = data;
			});
			
			
			//Init datatable
			var maListconfigurations = [];
			maListconfigurations.push($scope.configuration)
			$scope.configurationDT = datatable(configurationsDTConfig);
			$scope.configurationDT.setData(maListconfigurations, maListconfigurations.length);
			$scope.configurationDT.setEdit()
		});
		
	};

	init();
	
	$scope.cancel = function(){
		console.log("call cancel");
		$scope.messages = messages();
		$scope.messages.clear();

		$scope.mainService.stopEditMode();		
	};
	
	$scope.activeEditMode = function(){
		$scope.messages = messages();
		$scope.messages.clear();
		$scope.mainService.startEditMode();
		$scope.configurationDT.setEdit();
	};
	
	
}]);

