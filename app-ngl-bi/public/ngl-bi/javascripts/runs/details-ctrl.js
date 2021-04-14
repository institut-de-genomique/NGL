"use strict";

angular.module('home').controller('DetailsCtrl', ['$scope', '$http', '$q', '$routeParams', '$window', '$filter', '$sce', 'mainService', 'tabService', 'datatable', 'messages', 'lists', 'treatments', 'valuationService', 'convertValueServices', 
                                                  function($scope, $http, $q, $routeParams, $window, $filter, $sce, mainService, tabService, datatable, messages, lists, treatments, valuationService, convertValueServices) {
	/* configuration datatables */	
	var lanesDTConfig = {
			name:'lanesDT',
			order :{by:'number',mode:'local'},
			search:{active:false},
			pagination:{active:false},
			select:{active:false},
			showTotalNumberRecords:false,
			edit : {
				active:Permissions.check("writing")?true:false,
				showButton : false,
				withoutSelect : true,
				columnMode : true
			},
			save : {
				active:Permissions.check("writing")?true:false,
				showButton : false,
				changeClass : false,
				url:function(line){
					return jsRoutes.controllers.runs.api.Lanes.valuation($scope.run.code, line.number).url;
				},
				method:'put',
				value:function(line){
					return line.valuation;
				},
				callback:function(datatable, nbError){
					if(nbError == 0){
						saveRun();					
					}else{
						$scope.messages.setError("save");
					}
					
				}
			},
			cancel : {
				showButton:false
			},
			lines : {
				trClass : function(value){
					if(angular.isDefined($scope.run.treatments.ngsrg) && value.number == $scope.run.treatments.ngsrg["default"].controlLane.value){
		    			return "info";
		    		}		    		
				}
			},
			columns : [
			    {  	"property":"number",
			    	"render":function(value, line){
			    		return "<strong>"+value.number+"</strong>";
			    	},
			    	"header": Messages("runs.lane.code"),
			    	"type":"text",
			    	"order":false
				},				
				{	"property":"valuation.valid",
					"header": Messages("runs.lane.valuation.valid"),
					"filter":"codes:'valuation'",
					"type":"text",
					"edit":true,
			    	"order":false,
			    	"choiceInList":true,
			    	"listStyle":'bt-select',
					"possibleValues":'lists.getValuations()'			    	
				},
				{	"property":"valuation.resolutionCodes",
					"header": Messages("runs.lane.valuation.resolutions"),
					"render":"<div bt-select ng-model='value.data.valuation.resolutionCodes' bt-options='valid.code as valid.name group by valid.category.name for valid in lists.getResolutions()' ng-edit='false'></div>",
					"type":"text",
			    	"edit":true,
					"order":false,
			    	"choiceInList":true,
			    	"listStyle":'bt-select-multiple',
			    	"possibleValues":'lists.getResolutions()',
					"groupBy":'category.name'
				}
			]				
	};
	
	
	var readSetsDTConfig = {
			name:'readSetsDT',
			order :{by:'laneNumber',mode:'local'},
			search:{active:false},
			pagination:{
				active:true,
				mode:'local'
			},
			select:{active:false},
			showTotalNumberRecords:false,
			cancel : {active:false},		
			columns : [
				{  	"property":"laneNumber",
					"header": Messages("readsets.laneNumber"),
					"type":"text",
					"order":false,
					"position":1
				}, 
				{  	"property":"code",
					"header": Messages("readsets.code"),
					"render": function(value){
						return '<a href="" ng-click="showReadSet(\''+value.code+'\')">'+value.code+'</a>';
					},
					"type":"text",
					"order":false,
					"position":2
				},
				{	"property":"state.code",
					"filter":"codes:'state'",
					"header": Messages("readsets.stateCode"),
					"type":"text",
					"position":3
				},
				{  	
					"property":"sampleOnContainer.percentage",
					"header": Messages("readsets.sampleOnContainer.percentPerLane"),
					"type":"number",
					"format":2,
					"order":false,
					"position":4
				},	
				{	"property":"productionValuation.valid",
					"header": Messages("readsets.productionValuation.valid"),
					"filter":"codes:'valuation'",
					"type":"text",
					"position":10
				},			
				{	"property":"bioinformaticValuation.valid",
					"header": Messages("readsets.bioinformaticValuation.valid"),
					"filter":"codes:'valuation'",
					"type":"text",
					"position":11
				}
			    
			]
	};
	
	var readSetsPrimaryDTConfig = {
			name:'readSetsPrimaryDT',
			order :{by:'laneNumber',mode:'local'},
			search:{active:false},
			pagination:{active:false},
			exportCSV: {active: true},
			select:{active:false},
			showTotalNumberRecords:false,
			cancel : {active:false},		
			columns : [
				{  	"property":"laneNumber",
					"header": Messages("readsets.laneNumber"),
					"type":"text",
					"order":true,
					"position":1
				}, 
				{  	"property":"primaryIndex",
					"header": Messages("readsets.primaryTag"),
					"type":"text",
					"order":true,
					"position":2
				},
				{	"property":"primaryQ30",
					"header": Messages("readsets.treatments.ngsrg_primary.Q30"),
					"type":"number",
					"order":true,
					"tdClass": "valuationService.valuationCriteriaClass(value.data, run.valuation.criteriaCode, col.property)",
					"position":3
				},
				{  	
					"property":"primaryQualityScore",
					"header": Messages("readsets.treatments.ngsrg_primary.qualityScore"),
					"type":"number",
					"order":true,
					"tdClass": "valuationService.valuationCriteriaClass(value.data, run.valuation.criteriaCode, col.property)",
					"position":4
				},	
				{	"property":"primaryNbCluster",
					"header": Messages("readsets.treatments.ngsrg_primary.nbCluster"),
					"type":"number",
					"order":true,
					"position":5
				},	
				{	"property":"lossDmplxImaryIIndary",
					"header": Messages("readsets.treatments.ngsrg_primary.seqLossPctbetweenImaryAnd2ndaryDmplxing"),
					"type":"number",
					"order":true,
					"tdClass": "valuationService.valuationCriteriaClass(value.data, run.valuation.criteriaCode, col.property)",
					"position":6
				},
				{	"property":"sumPercentPerIndex",
					"header": Messages("readsets.sampleOnContainer.properties.sumPercentPerLane"),
					"type":"number",
					"format":2,
					"order":true,
					"position":7
				}
			    
			]
	};
	
	var saveRun = function(){
		var queries = [];
		queries.push($http.put(jsRoutes.controllers.runs.api.Runs.update($scope.run.code).url+"?fields=keep", {
			keep: $scope.run.keep
		}));
		queries.push($http.put(jsRoutes.controllers.runs.api.Runs.valuation($scope.run.code).url, $scope.run.valuation));
		
		if ($scope.run.typeCode == 'RSAPHYR' || $scope.run.typeCode == 'RIRYS') {
			for (var i = 0; i < $scope.run.lanes.length; i++) {
				queries.push($http.put(jsRoutes.controllers.runs.api.LaneTreatments.update($scope.run.code, $scope.run.lanes[i].number, "mapCreationJobId").url, $scope.run.lanes[i].treatments.mapCreationJobId));
			} 
		} 

		$q.all(queries).then(function(results){
			var error = false;
			for(var i = 0; i  < results.length; i++){
				var result = results[i];
				if(result.status !== 200){
					error = true;
				}
			}
			if(error){
				$scope.messages.setError("save");	
			}else{
				$scope.$broadcast('save');
				$scope.messages.setSuccess("save");
				$scope.mainService.stopEditMode();
				updateData();
			}
		});
	}
	
	$scope.getTabClass = function(value){
		 if(value === mainService.get('runActiveTab')){
			 return 'active';
		 }
	 };
	 
	 $scope.setActiveTab = function(value){
		 mainService.put('runActiveTab', value);
	 };
	 
	 $scope.getTabClassRS = function(value){
		 if(value === mainService.get('readSetActiveTab')){
			 return 'active';
		 }
	 };
	 
	 $scope.setActiveTabRS = function(value){
		 mainService.put('readSetActiveTab', value);
	 };
	
	/* buttons section */
	$scope.save = function(){
		if($scope.isLanesExist()){
			$scope.lanesDT.callbackEndDisplayResult
			$scope.lanesDT.save();
		}else{
			saveRun();
		}
	};
	
	$scope.cancel = function(){
		$scope.messages.clear();
		if($scope.isLanesExist()){
			$scope.lanesDT.cancel();
		}
		$scope.mainService.stopEditMode();
		updateData(true);				
	};
	
	$scope.activeEditMode = function(){
		$scope.messages.clear();
		$scope.mainService.startEditMode();
		if($scope.isLanesExist()){
			$scope.lanesDT.setEdit();
		}
	}

	
	
	/* readset section */
	
	$scope.form = {
	};	
	
	$scope.search = function(){
		//get lane numbers selected
		var laneNum = [];
		if($scope.form.laneNumbers) laneNum = $scope.form.laneNumbers;		
		//query by laneNumbers
		// includes permet de recuperer les colonnes indiquees ici sans ramener celles indiquer dans defaultKeys
		$http.get(jsRoutes.controllers.readsets.api.ReadSets.list().url,{params:{runCode:$scope.run.code,laneNumbers:laneNum,includes:[
			"laneNumber",
			"code",
			"state.code",
			"sampleOnContainer.percentage",
			"treatments.primaryDemultiplexing", 
			"treatments.ngsrg.default.validSeqPercent",
			"treatments.ngsrg.default.nbBases.value",
            "treatments.ngsrg.default.Q30.value",
            "treatments.ngsrg.default.qualityScore.value",
			"treatments.ngsrg.default.nbCluster.value",
			"productionValuation.valid",
			"bioinformaticValuation.valid"
		]}}).success(function(data) {
			// console.log("search data:", data);
			$scope.readSetsDT.setData(data, data.length);
			//console.log("config", $scope.readSetsDT) // pour avoir les noms des colonnes affichees dans config.configDefault.columns pour config.name=readSetsDT
			//console.log("search end:");
		});
	};
	

	$scope.showReadSets = function(){
		var laneNumbers={value:''};
		angular.forEach($scope.form.laneNumbers, function(value, key){
			this.value +='&laneNumbers='+value;
		}, laneNumbers);
		
		
		$window.open(jsRoutes.controllers.readsets.tpl.ReadSets.home('search').url+'?runCode='+$scope.run.code+laneNumbers.value, 'readsets');
	};
	
	$scope.valuateReadSets = function(){
		var laneNumbers={value:''};
		angular.forEach($scope.form.laneNumbers, function(value, key){
			this.value +='&laneNumbers='+value;
		}, laneNumbers);
		
		
		$window.open(jsRoutes.controllers.readsets.tpl.ReadSets.home('valuation').url+'?runCode='+$scope.run.code+laneNumbers.value, 'readsets');
	};
	
	$scope.showReadSet = function(readSetCode){
		$window.open(jsRoutes.controllers.readsets.tpl.ReadSets.get(readSetCode).url, 'readsets');
	};
	
	/* main section  */
	var updateData = function(isCancel){
		$http.get(jsRoutes.controllers.runs.api.Runs.get($routeParams.code).url).success(function(data) {
			$scope.run = data;	
			if($scope.isLanesExist()){
				$scope.lanesDT.setData($scope.run.lanes, $scope.run.lanes.length);
			}
			
			/*
			if(isCancel && !isValuationMode()){
				$scope.lanesDT.cancel();
				$scope.mainService.stopEditMode();
			}else{
				$scope.lanesDT.setEdit();
				$http.get(jsRoutes.controllers.readsets.api.ReadSets.list().url,{params:{runCode:$scope.run.code, includes:["code","state","bioinformaticValuation", "productionValuation","laneNumber","treatments.ngsrg", "sampleOnContainer"]}}).success(function(data) {
					$scope.readSetsDT.setData(data, data.length);				
				});
			}
			*/			
		});
	};
	
	var isValuationMode = function(){
		return ($scope.mainService.isHomePage('valuation') || $routeParams.page === 'valuation');
	};
	
	$scope.isLanesExist = function(){
		return($scope.run.lanes != null &&  $scope.run.lanes.length > 0)
	};
	
	$scope.highLight = function(prop){
			if (lists.getValuationCriterias() && $scope.run && $scope.run.valuation) {
				return "bg-" + $scope.valuationService.valuationCriteriaClass($scope.run, $scope.run.valuation.criteriaCode, prop);
			}
			else {
				return undefined;
			}
	};
	
	$scope.isIPS = function() {
		return $scope.run.state.code == "IP-S";
	};
	
    $scope.deliberatelyTrustHTMLComment = function() {
    	if ($scope.run && $scope.run.valuation && $scope.run.valuation.comment && $scope.run.valuation.comment != null) {
    		return $sce.trustAsHtml($scope.run.valuation.comment.trim().replace(/\n/g, "<br>"));
    	}	
    	else {
    		return "";
    	}
    };
    
   $scope.setImage = function(imageData, imageName, treatmentContext, treatmentCode, imageFullSizeWidth, imageFullSizeHeight) {
		$scope.modalImage = imageData;
	
		$scope.modalTitle = '';
		if (treatmentContext !== '') {
			$scope.modalTitle = treatmentContext + ' : ';
		}
		$scope.modalTitle = $scope.modalTitle + Messages('runs.treatments.' + treatmentCode + '.' + imageName);
	
		var margin = Messages("runs.treatments.images.margin");		
		var zoom = Math.min((document.body.clientWidth - margin) / imageFullSizeWidth, 1);

		$scope.modalWidth = imageFullSizeWidth * zoom;
		$scope.modalHeight = imageFullSizeHeight * zoom; //in order to conserve image ratio
		$scope.modalLeft = (document.body.clientWidth - $scope.modalWidth)/2;
	};
	
	 $scope.goToSeq=function(){
		$scope.value = AppURL("sq");
		
		//Get typeCode
		$scope.typeCodeExp = undefined;
		if($scope.run.categoryCode === "nanopore"){
			$scope.typeCodeExp=["nanopore-depot"];
		}else if($scope.run.categoryCode === "opgen"){
			$scope.typeCodeExp=["opgen-depot"];
		}else if($scope.run.categoryCode === "illumina"){
			$scope.typeCodeExp=["prepa-flowcell","prepa-fc-ordered"];
		}else if($scope.run.categoryCode === "bionano"){
			$scope.typeCodeExp=["irys-chip-preparation"];
		}
		
		if($scope.typeCodeExp !== undefined){
			$http.get(jsRoutes.controllers.experiments.api.Experiments.list().url,{params:{typeCodes:$scope.typeCodeExp,containerSupportCode:$scope.run.containerSupportCode}}).success(function(data) {
				var experiment = data;
				if(experiment.length==1){
					$window.open($scope.value+"/experiments/"+experiment[0].code, 'seq');
				}else{
					$window.open($scope.value+"/supports/"+$scope.run.containerSupportCode, 'seq');
				}
			});
		}else{
			$window.open($scope.value+"/supports/"+$scope.run.containerSupportCode, 'seq');
		}
	};

	var init = function(){
		$scope.messages = messages();
		$scope.lists = lists;
		$scope.treatments = treatments;
		$scope.mainService = mainService;
		$scope.mainService.stopEditMode();
		$scope.valuationService = valuationService();
		$scope.run = {};
		$scope.dataReadSetPrimary = [];
		$scope.convertValueServices = convertValueServices();
		
		$http.get(jsRoutes.controllers.runs.api.Runs.get($routeParams.code).url).success(function(data) {
			
			$scope.run = data;
			
			if(tabService.getTabs().length == 0){
				if(isValuationMode()){ //valuation mode
					tabService.addTabs({label:Messages('runs.page.tab.validate'),href:jsRoutes.controllers.runs.tpl.Runs.home("valuation").url,remove:true});
					tabService.addTabs({label:$scope.run.code,href:jsRoutes.controllers.runs.tpl.Runs.valuation($scope.run.code).url,remove:true})
				}else{ //detail mode
					tabService.addTabs({label:Messages('runs.page.tab.search'),href:jsRoutes.controllers.runs.tpl.Runs.home("search").url,remove:true});
					tabService.addTabs({label:$scope.run.code,href:jsRoutes.controllers.runs.tpl.Runs.get($scope.run.code).url,remove:true})									
				}
				tabService.activeTab(tabService.getTabs(1));
			}
			
			$scope.lists.refresh.resolutions({typeCode:$scope.run.typeCode, objectTypeCode:"Run"});
			
			$scope.lists.clear("valuationCriterias");
			$scope.lists.refresh.valuationCriterias({typeCode:$scope.run.typeCode, objectTypeCode:"Run", orderBy:'name'});
			if ($scope.isLanesExist()) {
				$scope.lanesDT = datatable(lanesDTConfig);
				$scope.lanesDT.setData($scope.run.lanes, $scope.run.lanes.length);
				if(isValuationMode()){
					$scope.mainService.startEditMode();	
					$scope.lanesDT.setEdit();
				}

				var treatments = {};

				for (var index in $scope.run.lanes) {
					for (var treatment in $scope.run.lanes[index].treatments) {
						if (!treatments[$scope.run.lanes[index].treatments[treatment].code]) {
							treatments[$scope.run.lanes[index].treatments[treatment].code] = $scope.run.lanes[index].treatments[treatment];
						} 
					}
				}
				
				$scope.treatments.init(treatments, jsRoutes.controllers.runs.tpl.Runs.laneTreatments, 'runs');				
				
				$scope.laneOptions = $filter('orderBy')($scope.run.lanes, 'number');				
				
			}else{
				if(angular.isDefined($scope.run.treatments)){
					$scope.treatments.init($scope.run.treatments, jsRoutes.controllers.runs.tpl.Runs.treatments, 'runs');				
				}
			}
			
			
			$http.get(jsRoutes.controllers.readsets.api.ReadSets.list().url,{params:{runCode:$scope.run.code, includes:["code","state","bioinformaticValuation", "productionValuation","laneNumber","treatments.ngsrg", "sampleOnContainer","treatments.global"]}}).success(function(data) {
				//Config depends on technology
				if($scope.run.categoryCode === "nanopore"){
					readSetsDTConfig.columns.push(
					{	"property":"treatments.global.default.usefulSequencesPercent.value",
			    		"header": Messages("readsets.treatments.global.usefulSequencesPercent"),
			    		"type":"number",
			    		"format":2,
			    		"order":false,
			    		"position":5
			    		
					},
					{	"property":"treatments.global.default.usefulSequences.value",
			    		"header": Messages("readsets.treatments.global.usefulSequences"),
			    		"type":"number",
			    		"order":false,
			    		"position":6
			    		
					},
					{	"property":"treatments.global.default.usefulBases.value",
			    		"header": Messages("readsets.treatments.global.usefulBases"),
			    		"type":"number",
			    		"order":false,
			    		"position":7
			    		
					});
				}else{
				
					readSetsDTConfig.columns.push(
					{	"property":"treatments.ngsrg.default.validSeqPercent.value",
			    		"header": Messages("readsets.treatments.ngsrg_illumina.validSeqPercent"),
			    		"type":"number",
			    		"format":2,
			    		"order":false,
			    		"tdClass": "valuationService.valuationCriteriaClass({readsets:value.data}, run.valuation.criteriaCode, 'readsets.' + col.property)",
			    		"position":5
			    		
					},
					{  	"property":"treatments.ngsrg.default.nbCluster.value",
			    		"header": Messages("readsets.treatments.ngsrg_illumina.nbCluster"),
			    		"type":"number",
			    		"order":false,
			    		"tdClass": "valuationService.valuationCriteriaClass({readsets:value.data}, run.valuation.criteriaCode, 'readsets.' + col.property)",
			    		"position":6
					},
					{  	"property":"treatments.ngsrg.default.nbBases.value",
			    		"header": Messages("readsets.treatments.ngsrg_illumina.nbBases"),
			    		"type":"number",
			    		"order":false,
			    		"position":7
					},
					{  	"property":"treatments.ngsrg.default.Q30.value",
			    		"header": Messages("readsets.treatments.ngsrg_illumina.Q30"),
			    		"type":"number",
			    		"format":2,
			    		"order":false,
			    		"tdClass": "valuationService.valuationCriteriaClass({readsets:value.data}, run.valuation.criteriaCode, 'readsets.' + col.property)",
			    		"position":8
					},
					{  	"property":"treatments.ngsrg.default.qualityScore.value",
			    		"header": Messages("readsets.treatments.ngsrg_illumina.qualityScore"),
			    		"type":"number",
			    		"format":2,
			    		"order":false,
			    		"tdClass": "valuationService.valuationCriteriaClass({readsets:value.data}, run.valuation.criteriaCode, 'readsets.' + col.property)",
			    		"position":9
					});
				
				}
				
				$scope.readSetsDT = datatable(readSetsDTConfig);
				$scope.readSetsDT.setData(data, data.length);	
			});
			
			$http.get(jsRoutes.controllers.commons.api.StatesHierarchy.list().url,  {params: {objectTypeCode:"Run"}}).success(function(data) { 
				for (var i=0; i<data.length; i++) {
					if (data[i].code == "FE-S") {
						data[i].specificColor = true;
						break;
					}
				}
				$scope.statesHierarchy = data;	
			});	
			
			//get readSet with primaryDemultiplexing
			$http.get(jsRoutes.controllers.readsets.api.ReadSets.list().url,{params:{runCode:$scope.run.code, includes:["code","laneNumber","treatments.primaryDemultiplexing", "sampleOnContainer"],existingFields:["treatments.primaryDemultiplexing"]}}).success(function(data) {
				var mapReadSetPrimary = new Map();
				for (var i=0; i<data.length; i++) {
					//Get tag value
					var tag = data[i].sampleOnContainer.properties.tag.value;
					var laneNumber = data[i].laneNumber;
					var keyTag = tag+''+laneNumber;
					 if(mapReadSetPrimary.get(keyTag) != undefined){
						 mapReadSetPrimary.get(keyTag).sumPercent +=data[i].sampleOnContainer.percentage;
					 }else{
						 mapReadSetPrimary.set(keyTag, {		laneNumber : data[i].laneNumber,	
							 						primaryIndex : tag,
													primaryQ30 : data[i].treatments.primaryDemultiplexing.default.Q30.value,
													primaryQualityScore : data[i].treatments.primaryDemultiplexing.default.qualityScore.value,
													primaryNbCluster : data[i].treatments.primaryDemultiplexing.default.nbCluster.value,
													lossDmplxImaryIIndary : data[i].treatments.primaryDemultiplexing.default.seqLossPctbetweenImaryAnd2ndaryDmplxing.value,
													sumPercentPerIndex : data[i].sampleOnContainer.percentage});
					 }
				}
				var dataReadSetPrimary=Array.from(mapReadSetPrimary.values());
				$scope.readSetsPrimaryDT = datatable(readSetsPrimaryDTConfig);
				$scope.readSetsPrimaryDT.setData(dataReadSetPrimary, dataReadSetPrimary.length);	
			});
			
			if(undefined == mainService.get('runActiveTab')){
				 mainService.put('runActiveTab', 'general');
			}
			if(undefined == mainService.get('readSetActiveTab')){
				 mainService.put('readSetActiveTab', 'RSgeneral');
			}
		});
		
	};
	
	init();
}]).controller('RunMinknowMetrichorCtrl', [ '$scope', '$http', function($scope, $http) {	
	
	var init = function(){
		$scope.propDefinitions = {};
		var tabDefinitions = $scope.treatments.getTreatment().propDefinitions;
		for(var i=0;i<tabDefinitions.length;i++){
			var result = tabDefinitions[i];
			$scope.propDefinitions[result.code]=result;
		}
		
	}
	$scope.$on('save', function(){
		console.log("save RunMinknowMetrichorCtrl");
		$http.put(jsRoutes.controllers.runs.api.RunTreatments.update($scope.run.code, $scope.run.treatments.minknowMetrichor.code).url, $scope.run.treatments.minknowMetrichor)
			.success(function(data){
				$scope.run.treatments.minknowMetrichor = data;				
			})
			.error(function(){
				$scope.messages.setError("save");				
			});
	});
	
	$scope.isEditMode = function(){
		if($scope.mainService.isEditMode()){
			if(['F-V', 'IW-V', 'IP-V'].indexOf($scope.run.state.code) >=0){
				return false;
			}else{
				return true;
			}
		}else
			return $scope.mainService.isEditMode();
    	
    };

	init();
	
}]).controller('RunMinknowBasecallingCtrl', [ '$scope', '$http', function($scope, $http) {	
	
	var init = function(){
		$scope.propDefinitions = {};
		var tabDefinitions = $scope.treatments.getTreatment().propDefinitions;
		for(var i=0;i<tabDefinitions.length;i++){
			var result = tabDefinitions[i];
			$scope.propDefinitions[result.code]=result;
		}
		
	}
	$scope.$on('save', function(){
		console.log("save RunMinknowBasecallingCtrl");
		$http.put(jsRoutes.controllers.runs.api.RunTreatments.update($scope.run.code, $scope.run.treatments.minknowBasecalling.code).url, $scope.run.treatments.minknowBasecalling)
			.success(function(data){
				$scope.run.treatments.minknowBasecalling = data;				
			})
			.error(function(){
				$scope.messages.setError("save");				
			});
	});
	
	$scope.isEditMode = function(){
		if($scope.mainService.isEditMode()){
			if(['F-V', 'IW-V', 'IP-V'].indexOf($scope.run.state.code) >=0){
				return false;
			}else{
				return true;
			}
		}else
			return $scope.mainService.isEditMode();
    	
    };

	init();
	
}]).controller('RunNGSRGIlluminaCtrl', [ '$scope', 'datatable', function($scope, datatable) {
	
	$scope.getNbCycles = function(){
    	if($scope.run.treatments && $scope.run.treatments.ngsrg){
    		var ngsrg = $scope.run.treatments.ngsrg["default"];
    		if(ngsrg.nbCycleRead1){
    			return ngsrg.nbCycleRead1.value+', '+ngsrg.nbCycleReadIndex1.value+', '+ngsrg.nbCycleReadIndex2.value+', '+ngsrg.nbCycleRead2.value;
    		}else{
    			return ngsrg.nbCycle.value;
    		}
    		
    	}
    	return '';
    };
    
    $scope.highLightCtrlLane = function(){
		if ($scope.run && $scope.run.treatments && $scope.run.treatments.ngsrg &&  $scope.run.treatments.ngsrg["default"].controlLane.value) 
			return "bg-info";
		else 
			return undefined;
	};

}]).controller('LanesNGSRGIlluminaCtrl', [ '$scope', 'datatable', function($scope, datatable) {
	
	var lanesNGSRGConfig = {
			name:'lanesNGSRG',
			order :{by:'number',mode:'local'},
			search:{active:false},
			pagination:{active:false},
			select:{active:false},
			showTotalNumberRecords:false,
			cancel : {active:false},
			lines : {
				trClass : function(value){
					if(value.number == $scope.run.treatments.ngsrg["default"].controlLane.value && $scope.run.valuation.criteriaCode == undefined){
		    			return "info";
		    		}		    		
				}
			},
			columns : [
			    {  	"property":"number",
			    	"render":function(value, line){
			    		return "<strong>"+value.number+"</strong>";
			    	},
			    	"header": Messages("runs.lane.code"),
			    	"type":"text",
			    	"order":false,
			    	"tdClass": function(value){
			    		if(value.number == $scope.run.treatments.ngsrg["default"].controlLane.value) {
			    			return "info";
			    		}
			    	}
				},
				{  	"property":function(value){
						if(angular.isDefined(value.treatments.ngsrg["default"].nbUsefulCycleRead2)){
							return value.treatments.ngsrg["default"].nbUsefulCycleRead1.value+', '
								+value.treatments.ngsrg["default"].nbUsefulCycleReadIndex1.value+', '
								+value.treatments.ngsrg["default"].nbUsefulCycleReadIndex2.value +', '
								+value.treatments.ngsrg["default"].nbUsefulCycleRead2.value;
						}else if(angular.isDefined(value.treatments.ngsrg["default"].nbCycleRead2)){
							return value.treatments.ngsrg["default"].nbCycleRead1.value +', '+value.treatments.ngsrg["default"].nbCycleRead2.value;
						}else{
							return value.treatments.ngsrg["default"].nbCycleRead1.value
						}
					},
			    	"header": Messages("runs.lane.ngsrg_illumina.nbCycles"),
			    	"type":"text",
			    	"order":false
				},
				{  	"property":"treatments.ngsrg.default.nbCluster.value",
			    	"header": Messages("runs.lane.ngsrg_illumina.nbCluster"),
			    	"type":"number",
			    	"order":false,
			    	"tdClass": "valuationService.valuationCriteriaClass({lanes:value.data}, run.valuation.criteriaCode, 'lanes.' + col.property)"
				},
				{  	"property":"treatments.ngsrg.default.percentClusterIlluminaFilter.value",
			    	"header": Messages("runs.lane.ngsrg_illumina.percentClusterIlluminaFilter"),
			    	"type":"number",
			    	"order":false,
			    	"tdClass" : "valuationService.valuationCriteriaClass({lanes:value.data}, run.valuation.criteriaCode, 'lanes.' + col.property)"
				},
				{  	"property":"treatments.ngsrg.default.nbClusterIlluminaFilter.value",
			    	"header": Messages("runs.lane.ngsrg_illumina.nbClusterIlluminaFilter"),
			    	"type":"number",
			    	"order":false,
			    	"tdClass" : "valuationService.valuationCriteriaClass({lanes:value.data}, run.valuation.criteriaCode, 'lanes.' + col.property)"
				},
				{  	"property":"treatments.ngsrg.default.percentClusterInternalAndIlluminaFilter.value",
			    	"header": Messages("runs.lane.ngsrg_illumina.percentClusterInternalAndIlluminaFilter"),
			    	"type":"number",
			    	"order":false,
			    	"tdClass" : "valuationService.valuationCriteriaClass({lanes:value.data}, run.valuation.criteriaCode, 'lanes.' + col.property)"
				},
				{  	"property":"treatments.ngsrg.default.nbClusterInternalAndIlluminaFilter.value",
			    	"header": Messages("runs.lane.ngsrg_illumina.nbClusterInternalAndIlluminaFilter"),
			    	"type":"number",
			    	"order":false
				},
				{  	"property":"treatments.ngsrg.default.nbBaseInternalAndIlluminaFilter.value",
			    	"header": Messages("runs.lane.ngsrg_illumina.nbBaseInternalAndIlluminaFilter"),
			    	"type":"number",
			    	"order":false
				},				
				{  	"property":"treatments.ngsrg.default.seqLossPercent.value",
			    	"header": Messages("runs.lane.ngsrg_illumina.seqLossPercent"),
			    	"type":"number",
			    	"order":false,
			    	"tdClass": "valuationService.valuationCriteriaClass({lanes:value.data}, run.valuation.criteriaCode, 'lanes.' + col.property)"
				},
				{  	"property":"treatments.ngsrg.default.occupiedPatternedWellPercentage.value",
			    	"header": Messages("runs.lane.ngsrg_illumina.occupiedPatternedWellPercentage"),
			    	"type":"number",
			    	"order":false,
			    	"tdClass": "valuationService.valuationCriteriaClass({lanes:value.data}, run.valuation.criteriaCode, 'lanes.' + col.property)"
				},
				{  	"property":"treatments.ngsrg.default.nbMismatch.value",
			    	"header": Messages("runs.lane.ngsrg_illumina.nbMismatch"),
			    	"type":"number",
			    	"order":false,
			    	"tdClass": "valuationService.valuationCriteriaClass({lanes:value.data}, run.valuation.criteriaCode, 'lanes.' + col.property)"
				}
				/*,
				{  	property:"treatments.ngsrg.default.phasing.value",
			    	header: Messages("runs.lane.ngsrg.phasing"),
			    	type :"String",
			    	order:false
				},
				{  	property:"treatments.ngsrg.default.prephasing.value",
			    	header: Messages("runs.lane.ngsrg.prephasing"),
			    	type :"String",
			    	order:false
				},*/
								
			]				
	};
	
	
	var init = function(){
		$scope.$watch('run', function() {
			if(angular.isDefined($scope.run) && angular.isDefined($scope.run.lanes) && ($scope.run.lanes != null)){
				$scope.lanesNGSRG = datatable(lanesNGSRGConfig);
				$scope.lanesNGSRG.setData($scope.run.lanes, $scope.run.lanes.length);
			}
		}); 
		
	};
	
	init();
	
}]).controller('LanesSAVCtrl', [ '$scope', '$filter', '$http', 'datatable', function($scope, $filter, $http, datatable) {
	var lanesSAVR1Config = {
			name:'lanesSAVR1',
			order :{by:'number',mode:'local'},
			search:{active:false},
			pagination:{active:false},
			select:{active:false},
			showTotalNumberRecords:false,
			cancel : {active:false},
			extraHeaders:{
				number:1,
				dynamic:true,
			},
			lines : {
				trClass : function(value){
					if(angular.isDefined($scope.run.treatments.ngsrg) && value.number == $scope.run.treatments.ngsrg["default"].controlLane.value  && $scope.run.valuation.criteriaCode == undefined){
		    			return "info";
		    		}		    		
				}
			},
			columns : [
					    {  	"property":"number",
					    	"render":function(value, line){
					    		return "<strong>"+value.number+"</strong>";
					    	},
					    	"header": Messages("runs.lane.code"),
					    	"type":"text",
					    	"order":false,
					    	"extraHeaders":{"0":Messages("runs.lane.sav.read1")},
					    	"tdClass": function(value){
					    		if(angular.isDefined($scope.run.treatments.ngsrg) && (value.number == $scope.run.treatments.ngsrg["default"].controlLane.value)) {
					    			return "info";
					    		}
					    	}
						},
						{  	"property":function(value){
							return $filter('number')(value.treatments.sav.read1.clusterDensity.value,2) +' +/- '+$filter('number')(value.treatments.sav.read1.clusterDensityStd.value,2);						
							},
					    	"header": Messages("runs.lane.sav.clusterDensity"),
					    	"type":"text",
					    	"order":false,
					    	"extraHeaders":{"0":Messages("runs.lane.sav.read1")}
						},
						{  	"property":function(value){
							    if (value.treatments.sav.read1.densityPF) {
							        return $filter('number')(value.treatments.sav.read1.densityPF.value,2) +' +/- '+$filter('number')(value.treatments.sav.read1.densityPFStd.value,2);						
						        }
							},
					    	"header": Messages("runs.lane.sav.densityPF"),
					    	"type":"text",
					    	"order":false,
					    	"extraHeaders":{"0":Messages("runs.lane.sav.read1")}
						},
						{  	"property":function(value){
								return $filter('number')(value.treatments.sav.read1.clusterPFPerc.value,2) +' +/- '+$filter('number')(value.treatments.sav.read1.clusterPFPercStd.value,2);						
							},
					    	"header": Messages("runs.lane.sav.clusterPF"),
					    	"type":"text",
					    	"order":false,
					    	"extraHeaders":{"0":Messages("runs.lane.sav.read1")},
							"tdClass": "valuationService.valuationCriteriaClass({lanes:value.data}, run.valuation.criteriaCode, 'lanes.treatments.sav.read1.clusterPFPerc.value')"
						},
						{  	"property":"treatments.sav.read1.phasing.value",
					    	"header": Messages("runs.lane.sav.phasing"),
					    	"type":"number",
					    	"format":3,
					    	"order":false,
					    	"extraHeaders":{"0":Messages("runs.lane.sav.read1")},
					    	"tdClass": "valuationService.valuationCriteriaClass({lanes:value.data}, run.valuation.criteriaCode, 'lanes.' + col.property)"
						},
						{  	"property":"treatments.sav.read1.prephasing.value",
					    	"header":Messages("runs.lane.sav.prephasing"),
					    	"type":"number",
					    	"format":3,
					    	"order":false,
					    	"extraHeaders":{"0":Messages("runs.lane.sav.read1")},
					    	"tdClass": "valuationService.valuationCriteriaClass({lanes:value.data}, run.valuation.criteriaCode, 'lanes.' + col.property)"
						},
						{  	"property":"treatments.sav.read1.greaterQ30Perc.value",
					    	"header": Messages("runs.lane.sav.greaterQ30Perc"),
					    	"type":"number",
					    	"order":false,
					    	"extraHeaders":{"0":Messages("runs.lane.sav.read1")},
					    	"tdClass": "valuationService.valuationCriteriaClass({lanes:value.data}, run.valuation.criteriaCode, 'lanes.' + col.property)"
						},
						{  	"property":"treatments.sav.read1.cyclesErrRated.value",
					    	"header": Messages("runs.lane.sav.cyclesErrRated"),
					    	"type":"number",
					    	"order":false,
					    	"extraHeaders":{"0":Messages("runs.lane.sav.read1")}
						},
						{  	"property":function(value){
								return $filter('number')(value.treatments.sav.read1.alignedPerc.value,2) +' +/- '+$filter('number')(value.treatments.sav.read1.alignedPercStd.value,2);						
							},
					    	"header": Messages("runs.lane.sav.alignedPerc"),
					    	"type":"text",
					    	"order":false,
					    	"extraHeaders":{"0":Messages("runs.lane.sav.read1")},
					    	"tdClass": "valuationService.valuationCriteriaClass({lanes:value.data}, run.valuation.criteriaCode, 'lanes.treatments.sav.read1.alignedPerc.value')"
						},
						{  	"property":function(value){
								return $filter('number')(value.treatments.sav.read1.errorRatePerc.value,2) +' +/- '+$filter('number')(value.treatments.sav.read1.errorRatePercStd.value,2);						
							},
					    	"header": Messages("runs.lane.sav.errorRatePerc"),
					    	"type" :"text",
					    	"order":false,
					    	"extraHeaders":{"0":Messages("runs.lane.sav.read1")},
					    	"tdClass": "valuationService.valuationCriteriaClass({lanes:value.data}, run.valuation.criteriaCode, 'lanes.treatments.sav.read1.errorRatePerc.value')"
						},											
						{  	"property":function(value){
							return $filter('number')(value.treatments.sav.read1.intensityCycle1.value,2) +' +/- '+$filter('number')(value.treatments.sav.read1.intensityCycle1Std.value,2);						
							},
					    	"header": Messages("runs.lane.sav.intensityCycle1"),
					    	"type":"text",
					    	"order":false,
					    	"extraHeaders":{"0":Messages("runs.lane.sav.read1")}
						},											
						{  	"property":"treatments.sav.read1.alert",
							"render": function(value){
								return getAlertButton(value,'read1');
							},
					    	"header": Messages("runs.lane.sav.alerts"),
					    	"type":"text",
					    	"order":false,
					    	"extraHeaders":{"0":Messages("runs.lane.sav.read1")}
						}												
			]				
	};
	
	var lanesSAVR2Config = {
			name:'lanesSAVR2',
			order :{by:'number',mode:'local'},
			search:{active:false},
			pagination:{active:false},
			select:{active:false},
			showTotalNumberRecords:false,
			cancel : {active:false},
			extraHeaders:{
				number:1,
				dynamic:true,
			},
			lines : {
				trClass : function(value){
					if(angular.isDefined($scope.run.treatments.ngsrg) && value.number == $scope.run.treatments.ngsrg["default"].controlLane.value  && $scope.run.valuation.criteriaCode == undefined){
			    		return "info";
		    		}		    		
				}
			},
			columns : [
			    {  	"property":"number",
			    	"render":function(value, line){
			    		return "<strong>"+value.number+"</strong>";
			    	},
			    	"header": Messages("runs.lane.code"),
			    	"type":"text",
			    	"order":false,
			    	"extraHeaders":{"0":Messages("runs.lane.sav.read2")},
			    	"tdClass": function(value){
			    		if(angular.isDefined($scope.run.treatments.ngsrg) && (value.number == $scope.run.treatments.ngsrg["default"].controlLane.value)) {
			    			return "info";
			    		}
			    	}
				},
				{  	"property":function(value){
					return $filter('number')(value.treatments.sav.read2.clusterDensity.value,2) +' +/- '+$filter('number')(value.treatments.sav.read2.clusterDensityStd.value,2);						
					},
			    	"header": Messages("runs.lane.sav.clusterDensity"),
			    	"type":"text",
			    	"order":false,
			    	"extraHeaders":{"0":Messages("runs.lane.sav.read2")}
				},
				{  	"property":function(value){
					   if (value.treatments.sav.read2.densityPF) {
					      return $filter('number')(value.treatments.sav.read2.densityPF.value,2) +' +/- '+$filter('number')(value.treatments.sav.read2.densityPFStd.value,2);						
						}
				    },
			    	"header": Messages("runs.lane.sav.densityPF"),
			    	"type":"text",
			    	"order":false,
			    	"extraHeaders":{"0":Messages("runs.lane.sav.read2")}
				},
				{  	
					"property":function(value){
						return $filter('number')(value.treatments.sav.read2.clusterPFPerc.value,2) +' +/- '+$filter('number')(value.treatments.sav.read2.clusterPFPercStd.value,2);						
					},
					"header": Messages("runs.lane.sav.clusterPF"),
			    	"type":"text",
			    	"order":false,
			    	"extraHeaders":{"0":Messages("runs.lane.sav.read2")},
					"tdClass": "valuationService.valuationCriteriaClass({lanes:value.data}, run.valuation.criteriaCode, 'lanes.treatments.sav.read2.clusterPFPerc.value')"
				},
				{  	"property":"treatments.sav.read2.phasing.value",
			    	"header": Messages("runs.lane.sav.phasing"),
			    	"type":"number",
			    	"format":3,
			    	"order":false,
			    	"extraHeaders":{"0":Messages("runs.lane.sav.read2")},
			    	"tdClass": "valuationService.valuationCriteriaClass({lanes:value.data}, run.valuation.criteriaCode, 'lanes.' + col.property)"
				},
				{  	"property":"treatments.sav.read2.prephasing.value",
			    	"header": Messages("runs.lane.sav.prephasing"),
			    	"type":"number",
			    	"format":3,
			    	"order":false,
			    	"extraHeaders":{"0":Messages("runs.lane.sav.read2")},
			    	"tdClass": "valuationService.valuationCriteriaClass({lanes:value.data}, run.valuation.criteriaCode, 'lanes.' + col.property)"
				},
				{  	"property":"treatments.sav.read2.greaterQ30Perc.value",
			    	"header": Messages("runs.lane.sav.greaterQ30Perc"),
			    	"type":"number",
			    	"order":false,
			    	"extraHeaders":{"0":Messages("runs.lane.sav.read2")},
			    	"tdClass": "valuationService.valuationCriteriaClass({lanes:value.data}, run.valuation.criteriaCode, 'lanes.' + col.property)"
				},
				{  	"property":"treatments.sav.read2.cyclesErrRated.value",
			    	"header": Messages("runs.lane.sav.cyclesErrRated"),
			    	"type":"number",
			    	"order":false,
			    	"extraHeaders":{"0":Messages("runs.lane.sav.read2")}
				},
				{  	"property":function(value){
						return $filter('number')(value.treatments.sav.read2.alignedPerc.value,2) +' +/- '+$filter('number')(value.treatments.sav.read2.alignedPercStd.value,2);						
					},
			    	"header": Messages("runs.lane.sav.alignedPerc"),
			    	"type":"text",
			    	"order":false,
			    	"extraHeaders":{"0":Messages("runs.lane.sav.read2")},
			    	"tdClass" : "valuationService.valuationCriteriaClass({lanes:value.data}, run.valuation.criteriaCode, 'lanes.treatments.sav.read2.alignedPerc.value')"
				},
				{  	"property":function(value){
						return $filter('number')(value.treatments.sav.read2.errorRatePerc.value,2) +' +/- '+$filter('number')(value.treatments.sav.read2.errorRatePercStd.value,2);						
					},
			    	"header": Messages("runs.lane.sav.errorRatePerc"),
			    	"type":"text",
			    	"order":false,
			    	"extraHeaders":{"0":Messages("runs.lane.sav.read2")},
			    	"tdClass" : "valuationService.valuationCriteriaClass({lanes:value.data}, run.valuation.criteriaCode, 'lanes.treatments.sav.read2.errorRatePerc.value')"
				},											
				{  	"property":function(value){
						if(value.treatments.sav.read2.intensityCycle1 !== undefined){
							return $filter('number')(value.treatments.sav.read2.intensityCycle1.value,2) +' +/- '+$filter('number')(value.treatments.sav.read2.intensityCycle1Std.value,2);
						}else{
							return null;
						}
					},
			    	"header": Messages("runs.lane.sav.intensityCycle1"),
			    	"type":"text",
			    	"order":false,
			    	"extraHeaders":{"0":Messages("runs.lane.sav.read2")}
				},										
				{  	"property":"treatments.sav.read2.alert",
					"render": function(value){
						return getAlertButton(value,'read2');
					},
			    	"header": Messages("runs.lane.sav.alerts"),
			    	"type":"text",
			    	"order":false,
			    	"extraHeaders":{"0":Messages("runs.lane.sav.read2")}
				}													
			]				
	};
	
	
	var getAlertButton = function(lane, readPos){
		var button = "";
		var alert = $scope.alerts[$scope.run.code+'.'+lane.number+'.'+readPos];
		
		if(alert){
			var button = '<button class="btn btn-xs btn-danger" type="button" popover="'+getAlertBody(alert)+'" popover-title="'+getAlertTitle()+'" popover-placement="right"><i class="fa fa-warning"></i></button>'; 			
		}
		return button;
	}
	
	var getAlertTitle = function(){
		return Messages("runs.lane.sav.alerts");
	};
	
	var getAlertBody = function(alert){
		var text = "";
		for(var propertyName in alert.propertiesAlert) {
			var list =alert.propertiesAlert[propertyName];
			text = propertyName+" : \n";
			for(var i = 0; i < list.length; i++ ){
				text = text +"\t"+Messages(Messages("runs.lane.sav."+list[i]))+", ";
			}		
		}
		return text;
	};
	
	var init = function(){
		$scope.$watch('run', function() {
			if (angular.isDefined($scope.run) && angular.isDefined($scope.run.lanes)  && ($scope.run.lanes != null)) {
								
				$http.get(jsRoutes.controllers.alerts.api.Alerts.list().url, {params:{regexCode:$scope.run.code+'*'}})
					.success(function(data, status, headers, config) {
					$scope.alerts = {};
					for(var i =	0; i < data.length ; i++){
						$scope.alerts[data[i].code] = data[i]; 
					}
					
					$scope.lanesSAVR1 = datatable(lanesSAVR1Config);
					$scope.lanesSAVR1.setData($scope.run.lanes, $scope.run.lanes.length);
					
					$scope.lanesSAVR2 = datatable(lanesSAVR2Config);
					$scope.lanesSAVR2.setData($scope.run.lanes, $scope.run.lanes.length);
					
				});
				
				
			};
		});
	
	};
	
	init();
}]).controller('TopIndexCtrl', [ '$scope', 'datatable', function($scope, datatable) {
	
	$scope.existIndexName = function(data){
    	for(var index in data.treatments.topIndex.default.varIndex.value){
    		if(data.treatments.topIndex.default.varIndex.value[index].expectedIndexName!=null){
    			return true;
    		}
    	}
    	return false;
    };
    
}]);





