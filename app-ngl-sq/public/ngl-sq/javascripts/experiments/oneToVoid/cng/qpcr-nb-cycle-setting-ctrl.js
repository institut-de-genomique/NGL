angular.module('home').controller('OneToVoidQPCRNbCycleSettingCtrl',['$scope', '$parse','$http',
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
    	"header":"Code aliquot",
		"property": "inputContainer.contents",
		"filter": "getArray:'properties.sampleAliquoteCode.value'| unique",
		"order":false,
		"hide":true,
		"type":"text",
		"position":7.5,
		"render": "<div list-resize='cellValue' list-resize-min-size='3'>",
		"extraHeaders":{0:Messages("experiments.inputs")}
	});
	columns.push({
		"header": Messages("containers.table.libProcessType"),
		"property" : "inputContainerUsed.contents",
		"filter" : "getArray:'properties.libProcessTypeCode.value' | unique | codes:'value'",
		"order" : false,
		"hide" : true,
		"type" : "text",
		"position" : 9,
		"render" : "<div list-resize='cellValue' list-resize-min-size='3'>",
		"extraHeaders" : {0 : Messages("experiments.inputs")}
	});	

	$scope.atmService.data.setColumnsConfig(columns);
	
	//nécessaire meme si rien a faire !!! appelle dans onSave du controler parent
	$scope.$parent.copyPropertiesToInputContainer = function(experiment){
		//NOP
	};

	$scope.$watch("profil",function(imgNew, imgOld){
		if(imgNew){			
			
			angular.forEach($scope.atmService.data.displayResult, function(dr){
				// PB.... on a pas de profil de nveau experiment !!!!!
				$parse('inputContainerUsed.experimentProperties.amplificationProfile').assign(dr.data, this);
			}, imgNew);
			
		}
		angular.element('#importFile')[0].value = null;
	});
	
	// FDS 08/04/2020 NGL-2928 fichier a importer
	var importData = function(){
		console.log("importData ");
		$scope.messages.clear();
		
		// parametre qui permet au programme d'import fichier du lightcycler de traiter 2 fichiers differents
		var mode= "nbCycle";
		
		uploadFile=$scope.fileLightcycler;
		console.log("File :"+uploadFile.fullname+", mode :"+mode);
		
		$http.post(jsRoutes.controllers.instruments.io.IO.importFile($scope.experiment.code).url+"?mode="+mode,  uploadFile )
		.success(function(data, status, headers, config) {
			$scope.messages.clazz="alert alert-success";
			$scope.messages.text=Messages('experiments.msg.import.success');
			$scope.messages.showDetails = false;
			$scope.messages.open();	
			//only atm because we cannot override directly experiment on scope.parent
			$scope.experiment.atomicTransfertMethods = data.atomicTransfertMethods;
			
			$scope.fileLightcycler= undefined;
			angular.element('#importFileLightcycler')[0].value = null;
			
			$scope.$emit('refresh');
		})
		.error(function(data, status, headers, config) {
			$scope.messages.clazz = "alert alert-danger";
			$scope.messages.text = Messages('experiments.msg.import.error');
			$scope.messages.setDetails(data);
			$scope.messages.open();	
			
			$scope.fileLightcycler= undefined;
			angular.element('#importFileLightcycler')[0].value = null;
		});
	};
	
	$scope.buttons = {
			// NGL-2928 renommer en isShowProfile
			isShowProfile:function(){
				// 06/05/2020 ajout admin mode !!!!
				return ( ($scope.isInProgressState() && !$scope.mainService.isEditMode())|| Permissions.check("admin") )
			},	
			// NGL-2928 fichier de valeurs à importer 
			isShowLightcycler:function(){
					return ( $scope.buttons.isLightcycler() && ( ($scope.isInProgressState() && !$scope.mainService.isEditMode() ) || Permissions.check("admin") ) );
			},
			isFileSetLightcycler:function(){
				return ($scope.fileLightcycler === null || $scope.fileLightcycler === undefined)?"disabled":"";
			},
			clickLightcycler:function(){ 
				return importData("Lightcycler");
			},
			isLightcycler:function(){
				return ($scope.experiment.instrument.typeCode === 'qpcr-lightcycler-480II');
			}
	};

}]);