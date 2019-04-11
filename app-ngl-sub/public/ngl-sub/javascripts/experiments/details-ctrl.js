"use strict";

angular.module('home').controller('DetailsCtrl',[ '$http', '$scope', '$routeParams' , 'mainService', 'lists', 'tabService','messages','datatable',
                                                  function($http, $scope, $routeParams, mainService, lists, tabService, messages, datatable) { 

	var runsDTConfig = {
			name:'runDT',
			order :{by:'code',mode:'local', reverse:true},
			pagination:{
				active:true,
				mode:'local'
			},			
			showTotalNumberRecords:true,
			edit : {
				active:true,
				showButton : false,
				withoutSelect : true,
				columnMode : true
			},
			columns : [{property:"traceInformation.creationDate",
			        	header: Messages("experiment.traceInformation.creationDate"),
			        	type :"date",		    	  	
			        	order:true
			           },
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
			           {property:"runDate",
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
			select:{active:true},
			showTotalNumberRecords:true,
			edit : {
				active:true,
				showButton : false,
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
				active:false
			},
			columns : [{property:"traceInformation.creationDate",
			        	header: Messages("rawData.traceInformation.creationDate"),
			        	type :"text",		    	  	
			        	order:true
			           },
			           {property:"relatifName",
			        	header: Messages("rawData.relatifName"),
			        	type :"text",		    	  	
			        	order:true
			           }
		    ]	        
	};	

	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('consultation');
		tabService.addTabs({label:Messages('experiments.menu.consultation'),href:jsRoutes.controllers.sra.experiments.tpl.Experiments.home("consultation").url,remove:true});
		tabService.activeTab(0); // desactive le lien !
	}	
	var init = function() {
		$scope.mainService = mainService;
		$scope.messages = messages();
		$scope.experiment = null;
       	console.log("routeParams.code:"+$routeParams.code);
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
			console.log("scope.run.code = " + $scope.experiment.run.code);
			console.log("XXXXXXXXXXXXXXX run.code = " + run.code);
		
			// Recuperer run (un seul run par experiment), et ses rawData :
			run = $scope.experiment.run;
			console.log("YYYYYYYYYYYYYYYYYYYY run.code = " );//+ run.code);
			console.log("YYYYYYYYYYYYYYYYYYYY run.code = " + run.code);
			console.log("YYYYYYYYYYYYYYYYYYYY run.accession = " + run.accession);
			
			var maListRawDatas = [];
			for (var j=0; j<run.listRawData.length; j++) {
				maListRawDatas.push(run.listRawData[j]);
			}
			var runs = [];
			runs.push(run);
			$scope.runDT = datatable(runsDTConfig);
			$scope.runDT.setData(runs, runs.length);
			$scope.rawDataDT = datatable(rawDatasDTConfig);
			$scope.rawDataDT.setData(maListRawDatas, maListRawDatas.length);
		}).error(function(data){
			//$scope.messages.addDetails(data);
			//$scope.messages.setError("save");
		});
		
	};

	init();
	
	if(angular.isUndefined(mainService.getHomePage())){
		mainService.setHomePage('details');
		tabService.addTabs({label:Messages('experiments.menu.details'),href:jsRoutes.controllers.sra.experiments.tpl.Experiments.home("details").url,remove:true});
		tabService.activeTab(0); // desactive le lien !
	}			

}]);

