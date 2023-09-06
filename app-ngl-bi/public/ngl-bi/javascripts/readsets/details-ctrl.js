 "use strict";

 angular.module('home').controller('DetailsCtrl', ['$scope', '$http', '$parse', '$q', '$routeParams', '$sce', '$location', 'mainService', 'tabService', 'datatable', 'messages', 'lists', 'treatments', '$window', 'valuationService', 'convertValueServices', 
     function($scope, $http, $parse, $q, $routeParams, $sce, $location, mainService, tabService, datatable, messages, lists, treatments, $window, valuationService, convertValueServices) {

	 
	 $scope.getTabClass = function(value){
		 if(value === mainService.get('readSetActiveTab')){
			 return 'active';
		 }
	 };
	 
	 $scope.setActiveTab = function(value){
		 mainService.put('readSetActiveTab', value);
	 };
	 
	 
	 $scope.goToRun=function(){
		$window.open(jsRoutes.controllers.runs.tpl.Runs.get($scope.readset.runCode).url, 'runs');
	}
	
	$scope.save = function(){
		
		$scope.messages.clear();
		$http.put(jsRoutes.controllers.readsets.api.ReadSets.properties($scope.readset.code).url,{properties : $scope.readset.properties})
		.success(function(data, status, headers, config) {
			if(data!=null){
				$http.put(jsRoutes.controllers.readsets.api.ReadSets.valuation($scope.readset.code).url,{productionValuation:$scope.readset.productionValuation,bioinformaticValuation:$scope.readset.bioinformaticValuation})
				.success(function(data, status, headers, config) {
					if(data!=null){
						$scope.messages.setSuccess("save");
						mainService.stopEditMode();
						updateData();
					}
				})
				.error(function(data, status, headers, config) {
					$scope.messages.setError("save");
					$scope.messages.setDetails(data);
				});
			}
		})
		.error(function(data, status, headers, config) {
			$scope.messages.setError("save");
			$scope.messages.setDetails(data);
		});
		
		//var queries = [];
		//queries.push($http.put(jsRoutes.controllers.readsets.api.ReadSets.properties($scope.readset.code).url,
		//		{properties : $scope.readset.properties}));
		//queries.push($http.put(jsRoutes.controllers.readsets.api.ReadSets.valuation($scope.readset.code).url, 
		//		{productionValuation:$scope.readset.productionValuation,bioinformaticValuation:$scope.readset.bioinformaticValuation}));
		
		//$q.all(queries).then(function(results){
		//	var error = false;
		//	for(var i = 0; i  < results.length; i++){
		//		var result = results[i];
		//		if(result.status !== 200){
		//			error = true;
		//		}
		//	}
		//	if(error){
		//		$scope.messages.setError("save");	
		//	}else{
		//		$scope.messages.setSuccess("save");
		//		mainService.stopEditMode();
		//		updateData();
		//	}
		//});						
	};
	
	$scope.cancel = function(){
		$scope.messages.clear();
		mainService.stopEditMode();
		updateData();				
	};
	
	$scope.activeEditMode = function(){
		$scope.messages.clear();
		mainService.startEditMode();			
	};
	
	$scope.activePrintMode = function(code){
		$location.url(jsRoutes.controllers.readsets.tpl.ReadSets.other(code, "print-view").url);
	};
	
	$scope.setImage = function(imageData, imageName, treatmentContext, treatmentCode, imageFullSizeWidth, imageFullSizeHeight) {
		$scope.modalImage = imageData;
	
		$scope.modalTitle = '';
		if (treatmentContext !== '') {
			$scope.modalTitle = treatmentContext + ' : ';
		}
		$scope.modalTitle = $scope.modalTitle + Messages('readsets.treatments.' + treatmentCode + '.' + imageName);
	
		var margin = Messages("readsets.treatments.images.margin");		
		var zoom = Math.min((document.body.clientWidth - margin) / imageFullSizeWidth, 1);

		$scope.modalWidth = imageFullSizeWidth * zoom;
		$scope.modalHeight = imageFullSizeHeight * zoom; //in order to conserve image ratio
		$scope.modalLeft = (document.body.clientWidth - $scope.modalWidth)/2;
	
		//$scope.modalTop = (window.innerHeight - $scope.modalHeight)/2;	
		//$scope.modalTop = $scope.modalTop - 50; //height of header and footer
	}
	
	var updateData = function(){
		$http.get(jsRoutes.controllers.readsets.api.ReadSets.get($routeParams.code).url).success(function(data) {
			$scope.readset = data;				
		});
	}
	
	var isValuationMode = function(){
		return (mainService.isHomePage('valuation') || ($routeParams.page && $routeParams.page.indexOf('valuation') == 0));
	}
	
	
    $scope.deliberatelyTrustHTMLComment = function() {
		if ($scope.readset && $scope.readset.productionValuation.comment && $scope.readset.productionValuation.comment != null) {
			return $sce.trustAsHtml($scope.readset.productionValuation.comment.trim().replace(/\n/g, "<br>"));
		}
		return "";
    };

    $scope.highLight = function(expression){
		if (lists.getValuationCriterias() && $scope.readset && $scope.readset.productionValuation) {
			return "bg-" + $scope.valuationService.valuationCriteriaClass($scope.readset, $scope.readset.productionValuation.criteriaCode, expression);
		}
		return undefined;
    };
    
	$scope.showPropertyData = function(read, treatmentCode, property) {
		if (angular.isDefined($scope.readset) && angular.isDefined($scope.readset.treatments) && angular.isDefined($scope.readset.treatments[treatmentCode])) {
			var currentTreatment = $scope.readset.treatments[treatmentCode];
			return (angular.isDefined($parse(read + "." + property)(currentTreatment)) 
					&& ($parse(read + "." + property)(currentTreatment) !== null));
		}
		return false;
	}
	
	$scope.isDataExistsForPhylogeneticTree = function(trtCode) {
		var b = true;
		if (angular.isDefined($scope.readset) && angular.isDefined($scope.readset.treatments) && (trtCode != undefined)) {
			var treatments = $scope.readset.treatments;
			if (angular.isDefined(treatments[trtCode].read1) && (!angular.isDefined(treatments[trtCode].read1.phylogeneticTree) ||
					( angular.isDefined(treatments[trtCode].read1.phylogeneticTree.value) && (treatments[trtCode].read1.phylogeneticTree.value == null) ) )) {
				b = false;
			}
			
			if (angular.isDefined(treatments[trtCode].pairs) && (!angular.isDefined(treatments[trtCode].pairs.phylogeneticTree) ||
					( angular.isDefined(treatments[trtCode].pairs.phylogeneticTree.value) && (treatments[trtCode].pairs.phylogeneticTree.value == null) ) )) {
				b = false;
			}
		}
		return b;
	}
	
	$scope.isRefCollabSub=function(){
		if (angular.isDefined($scope.readset) && angular.isDefined($scope.readset.properties) && angular.isDefined($scope.readset.properties.refCollabSub) && angular.isDefined($scope.readset.properties.refCollabSub.value)){
			return true;
		}
		return false;
	}
	
	$scope.isSubmissionState=function(){
		if ( angular.isDefined($scope.readset) && angular.isDefined($scope.readset.submissionState) && angular.isDefined($scope.readset.submissionState.code) ){
			return true;
		}
		return false;
	}
	
	$scope.getKrona = function(trtCode) {
		if (angular.isDefined($scope.readset) && angular.isDefined($scope.readset.treatments)  && (trtCode != undefined)) {
			
			if($scope.readset.treatments["taxonomy"].pairs != undefined)
				return "data:text/html;base64,"+$scope.readset.treatments["taxonomy"].pairs.krona.value;
			else
				return "data:text/html;base64,"+$scope.readset.treatments["taxonomy"].read1.krona.value;
		}
	}
	
	$scope.getNbCycles = function(){
		
		if($scope.readset && $scope.readset.treatments && $scope.readset.treatments.ngsrg && ($scope.readset.treatments.ngsrg.typeCode == "ngsrg-illumina" || $scope.readset.treatments.ngsrg.typeCode == "ngsrg-mgi")){
    		var ngsrg = $scope.readset.treatments.ngsrg["default"];
    		if(ngsrg.nbUsefulCycleRead2){
    			return ngsrg.nbUsefulCycleRead1.value+', '+ngsrg.nbUsefulCycleReadIndex1.value+', '+ngsrg.nbUsefulCycleReadIndex2.value+', '+ngsrg.nbUsefulCycleRead2.value;
    		}else if($scope.runNGSRG){
    			return $scope.runNGSRG["default"].nbCycle.value
    		}    		
    	}
    	return '';				
	};
	
	$scope.getLoadingReport = function(){
		if($scope.readset.sampleOnContainer && $scope.readset.sampleOnContainer.properties.loadingReport && $scope.readset.sampleOnContainer.properties.loadingReport.value.length>0){
			var loadingReport = $scope.readset.sampleOnContainer.properties.loadingReport.value.length+' '+Messages("readset.sampleOnContainer.loadingReport.loading")+' : ';
			for(var i=0; i<$scope.readset.sampleOnContainer.properties.loadingReport.value.length; i++){
				loadingReport += Messages("readset.sampleOnContainer.loadingReport.time")+' '+$scope.readset.sampleOnContainer.properties.loadingReport.value[i].time+' : '+$scope.readset.sampleOnContainer.properties.loadingReport.value[i].volume+'µL ; '
			}
			return loadingReport.substring(0,loadingReport.length-2);
		}
		return '';
	}
	
	$scope.getQCFlowCell = function(){
		if($scope.readset.sampleOnContainer && $scope.readset.sampleOnContainer.properties.qcFlowcell){
			for(var i=0; i<$scope.readset.sampleOnContainer.properties.qcFlowcell.value.length; i++){
				if($scope.readset.sampleOnContainer.properties.qcFlowcell.value[i].group == 'total'){
					return Messages("readset.sampleOnContainer.qcFlowcell.preLoading")+' : '+$scope.readset.sampleOnContainer.properties.qcFlowcell.value[i].preLoadingNbActivePores +' '+Messages("readset.sampleOnContainer.qcFlowcell.pores")+' ; '+
							Messages("readset.sampleOnContainer.qcFlowcell.postLoading")+' : '+$scope.readset.sampleOnContainer.properties.qcFlowcell.value[i].postLoadingNbActivePores+' '+Messages("readset.sampleOnContainer.qcFlowcell.pores")
					
				}
			}
		}
		return '';
	}
	
	$scope.isDataExistsForRead = function(read) {
		var b = true;
		if (angular.isDefined($scope.readset) && angular.isDefined($scope.readset.treatments)) {
			var treatment = $scope.readset.treatments["sortingRibo"];
			if ( (!angular.isDefined($parse(read)(treatment))) || (!angular.isDefined($parse(read + ".taxonBilan")(treatment))) ||
					( angular.isDefined($parse(read + ".taxonBilan")(treatment)) && ($parse(read + ".taxonBilan")(treatment) === null) ))  {
				b = false;
			}
		}
		return b;
	}
	
	$scope.sorterFunc = function(trt){
	    return -parseInt(trt.abundance);
	};
	
	$scope.setFilterTaxonBilan = function(value){
		 mainService.put('filterTaxonBilan', value);
	}
	
	$scope.setFilterTaxonBilanDefault = function(value){
		 mainService.put('filterTaxonBilanDefault', value);
	}
	
	$scope.setFilterTaxonBilanCommon = function(key,value){
		 mainService.put(key, value);
	}
	
	$scope.setFilterTaxonBilanPlastid = function(value){
		 mainService.put('filterTaxonBilanPlastid', value);
	}
	
	$scope.setFiltertaxonBilanContamination = function(value){
		 mainService.put('filtertaxonBilanContamination', value);
	}
	$scope.setFilterKmerPercentBilanDefault = function(value){
		 mainService.put('filterKmerPercentDefault', value);
	}
	
	var init = function(){
		$scope.messages = messages();
		$scope.lists = lists;
		$scope.treatments = treatments;
		$scope.valuationService = valuationService();
		$scope.filterTaxonBilan=5;
		$scope.filterTaxonBilanDefault=0.2;
		$scope.filterTaxonBilanMitochondrion=0.2;
		$scope.filterTaxonBilanPlastid=0.2;
		$scope.filtertaxonBilanContamination=0.2;
		$scope.filterKmerPercentDefault=1;
		$scope.arrayExpectedSeq=[];
		$scope.convertValueServices = convertValueServices();
		
		mainService.stopEditMode();
		if(isValuationMode()){
			mainService.startEditMode();			
		}
		
		$http.get(jsRoutes.controllers.readsets.api.ReadSets.get($routeParams.code).url).success(function(data) {
			$scope.readset = data;
				
			if(tabService.getTabs().length == 0){
				if(isValuationMode()){ //valuation mode
					tabService.addTabs({label:Messages('readsets.page.tab.validate'),href:jsRoutes.controllers.readsets.tpl.ReadSets.home("valuation").url,remove:true});
					tabService.addTabs({label:$scope.readset.code,href:jsRoutes.controllers.readsets.tpl.ReadSets.valuation( $scope.readset.code).url,remove:true})
				}else{ //detail mode
					tabService.addTabs({label:Messages('readsets.page.tab.search'),href:jsRoutes.controllers.readsets.tpl.ReadSets.home("search").url,remove:true});
					tabService.addTabs({label:$scope.readset.code,href:jsRoutes.controllers.readsets.tpl.ReadSets.get($scope.readset.code).url,remove:true})									
				}
				tabService.activeTab($scope.getTabs(1));
			}
			
			$scope.lists.refresh.resolutions({typeCode:$scope.readset.typeCode});
			$scope.lists.refresh.valuationCriterias({typeCode:$scope.readset.typeCode, objectTypeCode:"ReadSet", orderBy:'name'});
			$scope.lists.refresh.states({objectTypeCode:"ReadSet"});
			
			if(angular.isDefined($scope.readset.sampleOnContainer) && $scope.readset.sampleOnContainer != null && 
				angular.isDefined($scope.readset.sampleOnContainer.properties) && $scope.readset.sampleOnContainer.properties != null && 
				angular.isDefined($scope.readset.sampleOnContainer.properties.expectedSequences) && $scope.readset.sampleOnContainerproperties.expectedSequences != null) {
				var expectedSequences = $scope.readset.sampleOnContainer.properties.expectedSequences.value.replace(/["]/g,'');
				var lastChar = expectedSequences.charAt(expectedSequences.length-1);
				if(lastChar==","){
					expectedSequences=expectedSequences.substr(0,expectedSequences.length-1);
				}
				$scope.arrayExpectedSeq=expectedSequences.split(',');
			}
			
			if(angular.isDefined($scope.readset.treatments)){				
				$scope.treatments.init($scope.readset.treatments, jsRoutes.controllers.readsets.tpl.ReadSets.treatments, 'readsets', {global:true,primaryDemultiplexing:true});				
			}
			
			if($scope.readset.laneNumber){
				$http.get(jsRoutes.controllers.runs.api.Lanes.get($scope.readset.runCode, $scope.readset.laneNumber).url).success(function(data) {
					$scope.lane = data;	
				});	
			}
			
			// NGL-2970 - Vue Bionano. La requête part alors qu'il n'y a pas de traitements ngsrg dans bionano.
			if (data.typeCode != 'rsbionano') {
				$http.get(jsRoutes.controllers.runs.api.RunTreatments.get($scope.readset.runCode, "ngsrg").url).success(function(data) {
					$scope.runNGSRG = data;	
				});	
			}
			
			$http.get(jsRoutes.controllers.commons.api.StatesHierarchy.list().url,  {params: {objectTypeCode:"ReadSet"}}).success(function(data) {
				$scope.statesHierarchy = data;	
			});	
			
			$http.get(jsRoutes.controllers.samples.api.Samples.get($scope.readset.sampleCode).url).success(function(data){
				$scope.sample=data;
			});
					
			if(undefined == mainService.get('readSetActiveTab')){
				 mainService.put('readSetActiveTab', 'general');
			 }
			
			if(undefined != mainService.get('filterTaxonBilan')){
				$scope.filterTaxonBilan=mainService.get('filterTaxonBilan');
			 }
			
			if(undefined != mainService.get('filterTaxonBilanDefault')){
				$scope.filterTaxonBilanDefault=mainService.get('filterTaxonBilanDefault');
			 }
			
			if(undefined != mainService.get('taxonBilanMitochondrion')){
				$scope.filterTaxonBilanMitochondrion=mainService.get('taxonBilanMitochondrion');
			 }
			if(undefined != mainService.get('taxonBilanPlastid')){
				$scope.filterTaxonBilanPlastid=mainService.get('taxonBilanPlastid');
			 }
			if(undefined != mainService.get('taxonBilanContamination')){
				$scope.filtertaxonBilanContamination=mainService.get('taxonBilanContamination');
			 }
			
			if(undefined != mainService.get('filterKmerPercentDefault')){
				$scope.filterKmerPercentDefault=mainService.get('filterKmerPercentDefault');
			 }
		});
		
		$scope.ncbiUrl = Messages("readsets.treatments.taxonomy.beginNcbiUrl");

	};
	
	init();
	
}]);
 
 
 
 

 angular.module('home').controller('DetailsPrintCtrl', ['$scope', '$http', '$window', '$sce', '$routeParams',  'mainService', 'tabService', 'treatments', 
    function($scope,  $http, $window, $sce, $routeParams,  mainService, tabService, treatments ) {
	 
		$scope.print = function() {
			$window.print();
		}

	 	$scope.back = function() {
	 		$window.history.back();
	 	}
	 	
	 
		//function to call just one time the sub-function getCascadedArray
		$scope.getArray = function(property, treatmentCode, read, type) {
			if (angular.isDefined($scope.readset) && angular.isDefined($scope.readset.treatments[treatmentCode])) {
				var numberOfColumnsPerPage = 4;
				var numberOfElementsByColumn = 50;	 
				if (treatmentCode === "readQualityRaw")
					if (read === "read1")
						if (type === "suspectedKmers") {
							if (angular.isDefined($scope.readQualityRawRead1Kmers) && $scope.readQualityRawRead1Kmers.length == 0) {
								$scope.readQualityRawRead1Kmers = getCascadedArray(property, numberOfColumnsPerPage, numberOfElementsByColumn);;
							}
							return $scope.readQualityRawRead1Kmers;
						}
						else {
							if (angular.isDefined($scope.readQualityRawRead1Primers) && $scope.readQualityRawRead1Primers.length == 0) {
								$scope.readQualityRawRead1Primers = getCascadedArray(property, numberOfColumnsPerPage, numberOfElementsByColumn);;
							}
							return $scope.readQualityRawRead1Primers;
						}
					else
						if (type === "suspectedKmers") {
							if (angular.isDefined($scope.readQualityRawRead2Kmers) && $scope.readQualityRawRead2Kmers.length == 0) {
								$scope.readQualityRawRead2Kmers = getCascadedArray(property, numberOfColumnsPerPage, numberOfElementsByColumn);;
							}
							return $scope.readQualityRawRead2Kmers;
						}
						else {
							if (angular.isDefined($scope.readQualityRawRead2Primers) && $scope.readQualityRawRead2Primers.length == 0) {
								$scope.readQualityRawRead2Primers = getCascadedArray(property, numberOfColumnsPerPage, numberOfElementsByColumn);;
							}
							return $scope.readQualityRawRead2Primers;
						}
				else
					if (read === "read1")
						if (type === "suspectedKmers") {
							if (angular.isDefined($scope.readQualityCleanRead1Kmers) && $scope.readQualityCleanRead1Kmers.length == 0) {
								$scope.readQualityCleanRead1Kmers = getCascadedArray(property, numberOfColumnsPerPage, numberOfElementsByColumn);;
							}
							return $scope.readQualityCleanRead1Kmers;
						}
						else {
							if (angular.isDefined($scope.readQualityCleanRead1Primers) && $scope.readQualityCleanRead1Primers.length == 0) {
								$scope.readQualityCleanRead1Primers = getCascadedArray(property, numberOfColumnsPerPage, numberOfElementsByColumn);;
							}
							return $scope.readQualityCleanRead1Primers;
						}
					else
						if (type === "suspectedKmers") {
							if (angular.isDefined($scope.readQualityCleanRead2Kmers) && $scope.readQualityCleanRead2Kmers.length == 0) {
								$scope.readQualityCleanRead2Kmers = getCascadedArray(property, numberOfColumnsPerPage, numberOfElementsByColumn);;
							}
							return $scope.readQualityCleanRead2Kmers;
						}
							
						else {
							if (angular.isDefined($scope.readQualityCleanRead2Primers) && $scope.readQualityCleanRead2Primers.length == 0) {
								$scope.readQualityCleanRead2Primers = getCascadedArray(property, numberOfColumnsPerPage, numberOfElementsByColumn);;
							}
							return $scope.readQualityCleanRead2Primers;
						}
			}
			return;
		}
	    
		//function to make the "pagination" of the suspectedKmers & suspectedPrimers
		//output: an array of pages. each of them contains arrays of columns. each of them contains the data ! 
		var getCascadedArray = function(propertyArray, numberOfColumnsPerPage, numberOfElementsByColumn) {
			if (angular.isDefined(propertyArray)) {
				var tmpArray = propertyArray.slice(0);
				tmpArray.sort(function(a, b){return b.nbOccurences-a.nbOccurences});
				
				for (var i=0, len=tmpArray.length; i<len; i++) {
					tmpArray[i] = {"id":i, "data":tmpArray[i]};
				}
				
				var totalNumberOfColumns = Math.ceil(tmpArray.length / numberOfElementsByColumn); 	
				var pageArray = new Array(Math.ceil(totalNumberOfColumns/numberOfColumnsPerPage));
				var exit = false;
					
				for (var p=0, len=pageArray.length; p<len; p++) {			
					for (var c=0; c<numberOfColumnsPerPage; c++) {
						for (var d=0; d<numberOfElementsByColumn; d++) {
							if (d===0) {var dataArray = new Array();}
							
							var idx = p*numberOfColumnsPerPage*numberOfElementsByColumn + c*numberOfElementsByColumn + d;
							if (idx < tmpArray.length) {
								dataArray.push({"id":d, "line":tmpArray[idx]});
							}
							else {
								exit = true;
								break;
							}
						}
						if (c===0) {var colArray = new Array();}
						colArray.push({"id":c, "columns":dataArray});
						
						if (exit) {break;}
					}
					pageArray[p] = {"id":p, "pages":colArray};
				}	
				return pageArray;
			}
		}

		
	 
		var initArraysOfSuspectedContaminations = function(){
			$scope.readQualityRawRead1Kmers = [];
			$scope.readQualityRawRead1Primers = [];
			$scope.readQualityRawRead2Kmers = [];
			$scope.readQualityRawRead2Primers = [];
			$scope.readQualityCleanRead1Kmers = [];
			$scope.readQualityCleanRead1Primers = [];
			$scope.readQualityCleanRead2Kmers = [];
			$scope.readQualityCleanRead2Primers = [];
		}
	 
		var init = function(){
			initArraysOfSuspectedContaminations(); 
		};
		
		init();
 }]);

 angular.module('home').controller('SizeEstimatingCleanCtrl', [ '$scope', function($scope) {

	$scope.getTabP1SummaryFile = function(readset, treatmentCode) {
		var base64txt = readset.treatments[treatmentCode].pairs.tabP1summaryFile.value;
		return "data:text/txt;base64," + base64txt;
	};

	$scope.getTabP2SummaryFile = function(readset, treatmentCode) {
		var base64txt = readset.treatments[treatmentCode].pairs.tabP2summaryFile.value;
		return "data:text/txt;base64," + base64txt;
	};
	
}])
 
 
 angular.module('home').controller('MappingCtrl', ['$scope', '$parse', function($scope, $parse) {
	 
		$scope.isDataExistsForRead = function(read) {
			var b = true;
			if (angular.isDefined($scope.readset) && angular.isDefined($scope.readset.treatments)) {
				var treatment = $scope.readset.treatments["mapping"];
				if ( (!angular.isDefined($parse(read)(treatment))) || (!angular.isDefined($parse(read + ".errorPosition.value")(treatment))) ||
						( angular.isDefined($parse(read + ".errorPosition.value")(treatment)) && ($parse(read + ".errorPosition.value")(treatment) === null) ))  {
					b = false;
				}
			}
			return b;
		}
		
		var isDEFR1 = $scope.isDataExistsForRead("read1");
		var isDEFR2 = $scope.isDataExistsForRead("read2");

	 	$scope.titleForRead = function() {
	 		var title = "";
	 		if (isDEFR1) {
	 			title += Messages("readsets.treatments.mapping.tabs.read1");
		 		if (isDEFR2) {
		 			title += " / " + Messages("readsets.treatments.mapping.tabs.read2");
		 		}
	 		}
	 		else {
		 		if (isDEFR2) {
		 			title += Messages("readsets.treatments.mapping.tabs.read2");
		 		}
	 		}
	 		return title;
	 	}
}]);

