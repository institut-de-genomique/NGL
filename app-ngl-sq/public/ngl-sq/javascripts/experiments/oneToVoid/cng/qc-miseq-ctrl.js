angular.module('home').controller('OneToVoidQCMiseqCNGCtrl',['$scope', '$parse','$http',
                                                             function($scope,$parse,$http) {
	
	var config = $scope.atmService.data.getConfig();

	/* 15/01/2020 FINALEMENT laisser le tri par defaut  column/line
	//NGL-2755: trier sur line/colonne par défaut (colonnes héritées du controleur parent one-to-void-qc-ctrl.js)
	// attention on n'est pas forcement en mode plaque... ne pas écraser le order.by hérité
	if($scope.experiment.instrument.inContainerSupportCategoryCode == "96-well-plate"){
		config.order.by =['inputContainer.support.line','inputContainer.support.column*1'];
		$scope.atmService.data.setConfig(config );
	}
	*/
	
	// 08/01/2020 deplacement en tete de fichier
	// la difference de colonnes affichees entre tubes et plaque est prise en charge au niveau du controleur parent one-to-void-qc-ctrl.js 
	var columns = $scope.atmService.data.getColumnsConfig();
	columns.push({
    	"header" :Messages("containers.table.codeAliquot"),
		"property" : "inputContainer.contents",
		"filter" : "getArray:'properties.sampleAliquoteCode.value'| unique",
		"order" :false,
		"hide" :true,
		"type" :"text",
		"position" :7.5,
		"render" : "<div list-resize='cellValue' list-resize-min-size='3'>",
		"extraHeaders" :{0:Messages("experiments.inputs")}
	});
	columns.push({
		"header" : Messages("containers.table.concentration"),
		"property": "(inputContainerUsed.concentration.value|number).concat(' '+inputContainerUsed.concentration.unit)",
		"order" : true,
		"edit" : false,
		"hide" : true,
		"type" : "text",
		"position" : 10, 
		"extraHeaders" : {0 : Messages("experiments.inputs")}
	});	
	columns.push({
		"header" : Messages("containers.table.libProcessTypeCode"),
		"property" : "inputContainer.contents",
		"filter" : " getArray:'properties.libProcessTypeCode.value'| unique", 
		"order" : false,
		"hide" : true,
		"type" : "text",
		"position" : 8,
		"render" : "<div list-resize='cellValue' list-resize-min-size='3'>",
		"extraHeaders" : {0 : Messages("experiments.inputs")}
	});
	columns.push({
		"header" : Messages("containers.table.tags"),
		"property" : "inputContainer.contents",
		"filter" : "getArray:'properties.tag.value'| unique",
		"order":true,
		"hide" : true,
		"type" : "text",
		"position" : 9,
		"render" : "<div list-resize='cellValue' list-resize-min-size='3'>",
		"extraHeaders" : {0 : Messages("experiments.inputs")}
	});
	
	$scope.atmService.data.setColumnsConfig(columns);
	
	$scope.$parent.copyPropertiesToInputContainer = function(experiment){
		/* FDS decommenté le 30/08 + ajout de la size: les 2 propriétés de l'expérience doivent etres copiées dans le container */
		experiment.atomicTransfertMethods.forEach(function(atm){
			var inputContainerUsed =$parse("inputContainerUseds[0]")(atm);
			if(inputContainerUsed){
				
				var insertsize = $parse("experimentProperties.measuredInsertSize")(inputContainerUsed);
				if(insertsize){
					console.log("copy experimentProperties.measuredInsertSize to inputContainerUsed.size :"+ insertsize.value);
					inputContainerUsed.newSize = insertsize;
				}
			}
		});		
	};
	
	
	var importData = function(){
		$scope.messages.clear();
		
		$http.post(jsRoutes.controllers.instruments.io.IO.importFile($scope.experiment.code).url, $scope.file)
		.success(function(data, status, headers, config) {
			$scope.messages.clazz="alert alert-success";
			$scope.messages.text=Messages('experiments.msg.import.success');
			$scope.messages.showDetails = false;
			$scope.messages.open();	
			//only atm because we cannot override directly experiment on scope.parent
			$scope.experiment.atomicTransfertMethods = data.atomicTransfertMethods;
			$scope.file = undefined;
			angular.element('#importFile')[0].value = null;
			$scope.$emit('refresh');
			
		})
		.error(function(data, status, headers, config) {
			$scope.messages.clazz = "alert alert-danger";
			$scope.messages.text = Messages('experiments.msg.import.error');
			$scope.messages.setDetails(data);
			$scope.messages.open();	
			$scope.file = undefined;
			angular.element('#importFile')[0].value = null;
		});
	};
	
	$scope.button = {
		isShow:function(){
			return ($scope.isInProgressState() && !$scope.mainService.isEditMode() || Permissions.check("admin"))
			},
		isFileSet:function(){
			return ($scope.file === undefined)?"disabled":"";
		},
		click:importData,		
	};
	
	// NGL-3185: générer la feuille de route au format LRM (en plus de l'ancien format)
	//           l'ancien format finira par disparaitre...=> nommer l'ancien format MSR
	$scope.setAdditionnalButtonsLabel( Messages("experiments.sampleSheets") );  //sampleSheets  avec un S final
	$scope.setAdditionnalButtons([
		{
          isDisabled : function(){return $scope.isNewState();} ,
          isShow:function(){return !$scope.isNewState();},
		  click: function(){return $scope.fileUtils.generateSampleSheet({'fdrType':'MSR'})},
		  label: Messages("experiments.sampleSheet") + " / MSR"
		}
		,
		{
          isDisabled : function(){return $scope.isNewState();} ,
          isShow:function(){return !$scope.isNewState();},
		  click: function(){return $scope.fileUtils.generateSampleSheet({'fdrType':'LRM'})},
		  label: Messages("experiments.sampleSheet") + " / LRM"
	}]);
	
}]);