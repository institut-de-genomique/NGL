 "use strict";

 angular.module('home').controller('DetailsCtrl', ['$scope', '$http', '$q', '$routeParams', '$sce', '$filter', 'mainService', 'tabService', 'datatable', 'messages', 'lists', 'treatments', '$window', 'valuationService', 
                                                   function($scope, $http, $q, $routeParams, $sce, $filter, mainService, tabService, datatable, messages, lists, treatments, $window, valuationService) {
		
	 
	 $scope.save = function(){
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
			$window.open(jsRoutes.controllers.readsets.tpl.ReadSets.get(readSetCode).url, 'readsets');
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
				
				$http.get(jsRoutes.controllers.commons.api.StatesHierarchy.list().url,  {params: {objectTypeCode:"Analysis"}}).success(function(data) {
					$scope.statesHierarchy = data;	
				});	
			});
	    };
	 
	    init();
	
}]);

 