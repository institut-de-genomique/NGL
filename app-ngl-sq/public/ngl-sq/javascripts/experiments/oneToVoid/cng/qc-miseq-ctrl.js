angular.module('home').controller('OneToVoidQCMiseqCNGCtrl',['$scope', '$parse','$http','$filter',
                                                    function($scope,$parse,$http,$filter) {


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
	

	// !! attention la methode  udt.setHideColumn fait en réalite un toggleHideColumn  
	//    si hide=false => set hide=true;   si hide=true => set hide=false
	hideColumns=function(software, mode){
		var udt = $scope.atmService.data;
		var columns = udt.getColumnsConfig(columns);
		
		// MSR= 11-->22; LRM= 23-->39 ; 
		// NGL-3685 ajout de 4 colonnes pour LRM: 23-> 43
		console.log('software='+software+ ' mode='+mode);
		for(var i = 11; i<= 38 ; i++){
			 console.log('>>>'+i);
			var selectedColumns = $filter('filter')(columns,{position:i}); // le filtrage ramene un tableau
			if(selectedColumns.length === 1){                              // vérifier que le tableau contient exactement 1 colonne
																		   // si 0 => position inutilisée; si >0 conflit !!!!
				var column = selectedColumns[0];
				// si LRM/init => masquer les positions MSR;
				// si MSR/init => masquer les positions LRM
				// sinon toggle=> basculer le flag pour toutes les positions
				if      (("MSR" === software ) && ( "init" === mode) && ( i >=23 && i <= 43) ){ console.log('hide '+i +'='+ column.header);        udt.setHideColumn(column);}
				else if (("LRM" === software ) && ( "init" === mode) && ( i >=11 && i <= 22) ){ console.log('hide '+i +'='+ column.header);        udt.setHideColumn(column);}
				else if ("toggle" === mode )                                                   { console.log('toggle hide '+i +'='+ column.header); udt.setHideColumn(column);}	
			} else {
				//conflit de position !!!!
				selectedColumns.forEach(function(col){
					console.log('conflict >>>>>>> '+ col.header);
				});
			}
				
		} 
	};
	
	$scope.atmService.data.setColumnsConfig(columns);
	
	// NGL-3194 masquage de colonnes selon displayChoice
	$scope.$watch("experiment.experimentProperties.displayChoice.value", function(newValue, oldValue){
		if (newValue !== oldValue)  {
			hideColumns(newValue, "toggle");
		}
	});
	
	// appel initial, il faut le mode "init"
	if($scope.experiment.experimentProperties.displayChoice.value){
		hideColumns($scope.experiment.experimentProperties.displayChoice.value,"init");
	}
	
	$scope.$parent.copyPropertiesToInputContainer = function(experiment){
		/* ajout de la size: les 2 propriétés de l'expérience doivent etres copiées dans le container */
		experiment.atomicTransfertMethods.forEach(function(atm){
			var inputContainerUsed =$parse("inputContainerUseds[0]")(atm);
			if(inputContainerUsed){
				
				var insertsize = $parse("experimentProperties.measuredInsertSize")(inputContainerUsed);
				if(insertsize){
					//console.log("copy experimentProperties.measuredInsertSize to inputContainerUsed.size :"+ insertsize.value);
					inputContainerUsed.newSize = insertsize;
				} else {
					// NGL-3194 Pour LRM la propriété a copier s'appelle medianInsertSizeLRM
					var insertsize = $parse("experimentProperties.medianInsertSizeLRM")(inputContainerUsed);
					if(insertsize){
					//console.log("copy experimentProperties.measuredInsertSize to inputContainerUsed.size :"+ insertsize.value);
					inputContainerUsed.newSize = insertsize;
					}
				}
				
			}
		});
	};
	
	// ancienne fonction d'import de fichier MCS
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
	
	// NGL-3194  import de fichiers multiple
	var importDataLRM = function(){
		$scope.messages.clear();
		
		$http.post(jsRoutes.controllers.instruments.io.IO.importFiles($scope.experiment.code).url, {files: $scope.LRMfiles})
		.success(function(data, status, headers, config) {
			$scope.messages.clazz="alert alert-success";
			$scope.messages.text=Messages('experiments.msg.import.success');
			$scope.messages.showDetails = false;
			$scope.messages.open();	
			//only atm because we cannot override directly experiment on scope.parent
			$scope.experiment.atomicTransfertMethods = data.atomicTransfertMethods;
			$scope.LRMfiles = undefined;
			angular.element('#importFileLRM')[0].value = null;
			$scope.$emit('refresh');
			
		})
		.error(function(data, status, headers, config) {
			$scope.messages.clazz = "alert alert-danger";
			$scope.messages.text = Messages('experiments.msg.import.error');
			$scope.messages.setDetails(data);
			$scope.messages.open();	
			$scope.LRMfiles = undefined;
			angular.element('#importFileLRM')[0].value = null;
		});
	};
	
	/* 01/03/2021 NGL-3194 ajout nouveau boutton pour import LRM
	   29/04/2021 supprimer la mention des dates dans les 2 values
	*/
	$scope.buttons = {
		isShow:function(){
			return ( !$scope.mainService.isEditMode() && ($scope.experiment.experimentProperties.displayChoice.value === "MSR") 
					  && ( $scope.isInProgressState() || Permissions.check("admin") )
				   )
		},
		isFileSet:function(){
			return ($scope.file === undefined)?"disabled":"";
		},
		click: function(){
			return importData();
		},
		isShowLRM:function(){
			return ( !$scope.mainService.isEditMode() && ($scope.experiment.experimentProperties.displayChoice.value === "LRM") 
					  && ( $scope.isInProgressState() || Permissions.check("admin") )
				   )
		},
		isFileSetLRM:function(){
			return ($scope.LRMfiles === undefined)?"disabled":"";
		},
		clickLRM: function(){
			return importDataLRM();
		}
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
		},
		//NGL-3703 ajout Feuille de route LRM-CrefIX
		{
			isDisabled : function(){return $scope.isNewState();} ,
			isShow:function(){return !$scope.isNewState();},
			click: function(){return $scope.fileUtils.generateSampleSheet({'fdrType':'LRM-CRefIX'})},
			label: Messages("experiments.sampleSheet") + " / LRM-CRefIX"
		},
	]);
	
}]);