 "use strict";

angular.module('home').controller('DetailsCtrl', ['$scope', '$http', '$q', '$routeParams', '$sce', '$filter', 'mainService', 'tabService', 'datatable', 'messages', 'lists', 'treatments', '$window', 'valuationService', 
function($scope, $http, $q, $routeParams, $sce, $filter, mainService, tabService, datatable, messages, lists, treatments, $window, valuationService) {
	const CONSTS = treatments.CONSTS;
	var view_mode = CONSTS.UI_COMPONENTS.ACCORDION;
	/**
	 * Buttons to change the view of treatments
	 */
	var buttonList = [
	    { 
	      name: $sce.trustAsHtml('<i class="fa fa-ellipsis-v" style="font-size: 12px;padding-left:3px;padding-right:3px"></i>'), 
	      action: function() {
	        view_mode = CONSTS.UI_COMPONENTS.ACCORDION;
			init();
	      }
	    },
	    { 
	      name: $sce.trustAsHtml('<i class="fa fa-ellipsis-h" style="font-size: 12px"></i>'), 
	      action: function() {
	        view_mode = CONSTS.UI_COMPONENTS.PILL;
			init();
	      }
	    }
	];
	$scope.getTabClass = function(value){
		 if(value === mainService.get('analysesActiveTab')){
			 return 'active';
		 }
	 };
	 
	 $scope.setActiveTab = function(value){
		 mainService.put('analysesActiveTab', value);
	 };
	 
	$scope.setImage = function(imageData, imageName, treatmentContext, treatmentCode, imageFullSizeWidth, imageFullSizeHeight) {
		$scope.modalImage = imageData;
	
		$scope.modalTitle = '';
		if (treatmentContext !== '') {
			$scope.modalTitle = treatmentContext + ' : ';
		}
		$scope.modalTitle = $scope.modalTitle + Messages('analyses.treatments.' + treatmentCode + '.' + imageName);
	
		var margin = Messages("analyses.treatments.images.margin");	
		
		//var zoom = Math.min((document.body.clientWidth - margin) / imageFullSizeWidth, 1);
		var zoom = Math.min((document.body.clientWidth - margin) / imageFullSizeWidth, 0.5);
		console.log("margin=", margin);	
		console.log("document.body.clientWidth", document.body.clientWidth);
		console.log("imageFullSizeWidth=", imageFullSizeWidth);	
		console.log("zoom", zoom);
		
		$scope.modalWidth = imageFullSizeWidth * zoom;
		$scope.modalHeight = imageFullSizeHeight * zoom; //in order to conserve image ratio
		$scope.modalLeft = (document.body.clientWidth - $scope.modalWidth)/2;
		console.log("$scope.modalWidth=", $scope.modalWidth);	
		console.log("$scope.modalHeight=", $scope.modalHeight);	
		console.log("$scope.modalLeft=", $scope.modalLeft);	
		
		//$scope.modalTop = (window.innerHeight - $scope.modalHeight)/2;	
		//$scope.modalTop = $scope.modalTop - 50; //height of header and footer
	};
	
	$scope.save = function() {
			var queries = [];
			queries.push($http.put(jsRoutes.controllers.analyses.api.Analyses.properties($scope.analysis.code).url,
					{properties : $scope.analysis.properties}));
			queries.push($http.put(jsRoutes.controllers.analyses.api.Analyses.valuation($scope.analysis.code).url, 
					$scope.analysis.valuation));
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
					$scope.messages.setSuccess("save");
					mainService.stopEditMode();
					updateData();
				}
			}).catch(function(error){
			    $scope.messages.setError("save");
			});				
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
		
		var updateData = function(){
			$http.get(jsRoutes.controllers.analyses.api.Analyses.get($routeParams.code).url).success(function(data) {
				$scope.analysis = data;	
				
			});
		}
		
		var isValuationMode = function(){
			return (mainService.isHomePage('valuation') || ($routeParams.page && $routeParams.page.indexOf('valuation') == 0));
		};
		
		$scope.deliberatelyTrustHTMLComment = function() {
			if ($scope.analysis && $scope.analysis.valuation.comment && $scope.analysis.valuation.comment != null) {
				return $sce.trustAsHtml($scope.analysis.valuation.comment.trim().replace(/\n/g, "<br>"));
			}
			else {
				return "";
			}
	    };
	 
	    $scope.highLight = function(prop){
			if (lists.getValuationCriterias() && $scope.analysis && $scope.analysis.valuation) {
				return "bg-" + $scope.valuationService.valuationCriteriaClass($scope.analysis, $scope.analysis.valuation.criteriaCode, prop);
			}
			else {
				return undefined;
			}
	    };
	    
	    $scope.showReadSet = function(readSetCode){
		console.log('showReadSet '+ readSetCode);
			$window.open(jsRoutes.controllers.readsets.tpl.ReadSets.get(readSetCode).url, '_blank');
		};
		
		var indexedTrt = [];
		
		$scope.treatmentsFilter = function(){
			indexedTrt = [];
			return $scope.analysis.treatments[treatments.getTreatment().code].read1.assignationBilan.value;
		}
		
		$scope.filterTreatments = function(trt){
			 var trtIsNew = indexedTrt.indexOf(trt.controlName) == -1;
		        if (trtIsNew) {
		        	indexedTrt.push(trt.controlName);
		        }
		        return trtIsNew;
		}
		
		$scope.sorterFunc = function(trt){
		    return -parseInt(trt.abundance);
		};
		
		$scope.getList = function(str){
			return str.split(";"); 
		}
		
	    var init = function(){
		 	$scope.messages = messages();
			$scope.lists = lists;
			$scope.treatments = treatments;
			$scope.valuationService = valuationService();
			mainService.stopEditMode();
			if(isValuationMode()){
				mainService.startEditMode();			
			}
			
			$http.get(jsRoutes.controllers.analyses.api.Analyses.get($routeParams.code).url).success(function(data) {
				$scope.analysis = data;	
					
				if(tabService.getTabs().length == 0){
					if(isValuationMode()){ //valuation mode
						tabService.addTabs({label:Messages('analyses.page.tab.validate'),href:jsRoutes.controllers.analyses.tpl.Analyses.home("valuation").url,remove:true});
						tabService.addTabs({label:$scope.analysis.code,href:jsRoutes.controllers.analyses.tpl.Analyses.valuation( $scope.analysis.code).url,remove:true})
					}else{ //detail mode
						tabService.addTabs({label:Messages('analyses.page.tab.search'),href:jsRoutes.controllers.analyses.tpl.Analyses.home("search").url,remove:true});
						tabService.addTabs({label:$scope.analysis.code,href:jsRoutes.controllers.analyses.tpl.Analyses.get($scope.analysis.code).url,remove:true})									
					}
					tabService.activeTab($scope.getTabs(1));
				}
				
				$scope.lists.refresh.resolutions({typeCode:$scope.analysis.typeCode});
				$scope.lists.clear("valuationCriterias");
				$scope.lists.refresh.valuationCriterias({typeCode:$scope.analysis.typeCode, objectTypeCode:"Analysis", orderBy:'name'});
				$scope.lists.refresh.states({objectTypeCode:"Analysis"});
				
				if(angular.isDefined($scope.analysis.treatments)){				
					$scope.treatments.init($scope.analysis.treatments, jsRoutes.controllers.analyses.tpl.Analyses.treatments, "analyses");				
				}

				$http.get(jsRoutes.controllers.commons.api.Values.list().url, { params: { propertyDefinitionCode: "state" } }).success(function(data) { 
				    $scope.stateProperties = CONSTS.WORKFLOWS.GENOME_DENOVO_ASSEMBLY.map(function(code) {return { code: code };});
				});

				$http.get(jsRoutes.controllers.commons.api.StatesHierarchy.list().url,  {params: {objectTypeCode:"Analysis"}}).success(function(data) {
					$scope.statesHierarchy = data;	
				});
				
				if($scope.analysis.typeCode == CONSTS.TREATMENT_TYPES.GENOME_DENOVO_ASSEMBLY && Object.keys($scope.analysis.treatments).length > 0){
					$scope.treatments.setViewMode(view_mode);
					if(!$scope.buttonList){
						$scope.buttonList = buttonList;
					}
				}
				
				$scope.devono_assembly = false;
				if($scope.analysis.typeCode == CONSTS.TREATMENT_TYPES.GENOME_DENOVO_ASSEMBLY){
					$scope.devono_assembly = true;
				}
						
				/**
				 * NGL-4539 : Récupération des échantillons
				 */
				var samplesCode = $scope.analysis.sampleCodes.map(function(value) { return 'codes=' + value; }).join('&');
				var sampleAttributs = ["code", "taxonCode", "ncbiScientificName", "properties.collabScientificName", "properties.WoRMSCode"];
				var includes = sampleAttributs.map(function(attr) { return 'includes=' + attr; }).join('&');
				var apiUrl = jsRoutes.controllers.samples.api.Samples.list().url + '?' + samplesCode + '&' + includes;
				$http.get(apiUrl).then(function(response) { 
					$scope.samples = response.data; 
				}, function(error) { 
					console.error("Erreur lors de la récupération des échantillons :", error); 
				});
				
			});
			if(undefined == mainService.get('analysesActiveTab')){
				 mainService.put('analysesActiveTab', 'general');
			 }
	    };
	    init();
}]).controller('AssemblyDecontaminationBACtrl', [ '$scope', 'datatable', function($scope, datatable) {

	$scope.openContextPlot = function(pairs){
		var contextPlot = pairs.contextPlot;
		var base64content = contextPlot.value;
		var decodedFileContent = window.atob(base64content);
		if(isGzip()) {
		        decompressFileContent();
		} else {
		        openInNewWindow(decodedFileContent);
		}

		//---

		function isGzip() {
		        if(!contextPlot.extension) return false;
		        var extension = contextPlot.extension.toLowerCase();
		        return extension === "gzip" || extension === "gz";
		}

		function decompressFileContent() {
		        var decompressedChuncks = [];

		        var compressedChars = decodedFileContent.split('').map(asChar);
		        var compressedBytes = Uint8Array.from(compressedChars);
		        var compressedBlob = new Blob([compressedBytes]);
		        var decompressionStream = new DecompressionStream("gzip");
		        var decompressedStream = compressedBlob.stream().pipeThrough(decompressionStream);
		        var decompressedReader = decompressedStream.getReader();
		        decompressedReader.read().then(readChunck);

		        //---

		        function readChunck(chunck) {
		                if(chunck.done) return decodeAllChuncks();
		                decompressedChuncks = decompressedChuncks.concat(chunck.value);
		                decompressedReader.read().then(readChunck);
		        }

		        function decodeAllChuncks() {
		                var chuncksBlob = new Blob(decompressedChuncks);
		                chuncksBlob.arrayBuffer().then(decodeHtml);
		        }

		        function decodeHtml(buffer) {
		                var decompressedBytes = new Uint8Array(buffer);
		                var html = new TextDecoder().decode(decompressedBytes);
		                openInNewWindow(html);
		        }
		}

		function asChar(letter) {
		        return letter.charCodeAt(0);
		}

		function openInNewWindow(html) {
		        var win = window.open("", '_blank');
		        win.document.write(html);
		        win.document.close();
		}
		var base64html = pairs.contextPlot.value;
		var html = window.atob(base64html);
		var win = window.open("", '_blank');
		win.document.write(html);
		win.document.close();
	}
	
}]);

 