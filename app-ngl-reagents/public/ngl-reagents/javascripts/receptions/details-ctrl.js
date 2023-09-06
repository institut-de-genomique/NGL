angular.module('home').controller('DetailsCtrl', ['$scope', '$http', '$q', '$routeParams', '$filter','$window', '$sce','mainService', 'tabService', 'reagentReceptionsSearchService', 'lists', 'messages', 
                                          function($scope,$http,$q,$routeParams,$filter,$window,$sce,mainService,tabService,reagentReceptionsSearchService,lists,messages){
	
	
	
	/* buttons section */
	$scope.save = function(){
		startSaveInProgress();
		updateReagentReception()
		.success(updateSuccess)
		.error(updateError);
		
		//---

		function startSaveInProgress(){
			saveInProgress = true;	
			$scope.messages.clear();
		}

		function endSaveInProgress(){
			saveInProgress = false;
		}

		function updateReagentReception(){
			var updateUrl = getReagentReceptionUpdateUrl();
			var reagentReception = $scope.reagentReception;
			return $http.put(updateUrl, reagentReception);
		}

		function getReagentReceptionUpdateUrl(){
			var reagentReceptionCode = $scope.reagentReception.code;
			return jsRoutes.controllers.reagents.api.Receptions.update(reagentReceptionCode).url;
		}

		function updateSuccess(data, status, headers, config) {
			registerUpdatedReagentReception();
			setSuccessMessage();
			mainService.stopEditMode();
			endSaveInProgress();	
			
			//---

			function registerUpdatedReagentReception(){
				$scope.reagentReception = data;
			}

			function setSuccessMessage(){
				$scope.messages.setSuccess("save");	
			}
		}

		function updateError(data, status, headers, config) {
			setErrorMessage();				
			endSaveInProgress();
			
			//---

			function setErrorMessage(){
				$scope.messages.setError("save");
				$scope.messages.setDetails(data);
			}
		}
	};
	
	$scope.cancel = function(){
		$scope.messages.clear();
		mainService.stopEditMode();
		reloadReagentReception();	

		//---
		
		function reloadReagentReception(){
			loadReagentReception();
		}
	};
	
	$scope.activeEditMode = function(){
		$scope.messages.clear();
		mainService.startEditMode();		
	};

	$scope.isSaveInProgress = function(){
		return saveInProgress;
	};

	$scope.convertToBr = function(comments){
		if(isCommentsEmpty()) return;
		return getCommentsAsHtml();

		//---

		function isCommentsEmpty(){
			return !(comments && Array.isArray(comments));
		}

		function getCommentsAsHtml(){
			var commentsBr = comments.map(getCommentText).join("<br>");
			var html = $sce.trustAsHtml(commentsBr);
			return html;
		}

		function getCommentText(commentObj){
			return commentObj.comment;
		}
	};

	// A suppprimer après création des importTypes reagents en description
	$scope.importTypeLabel = function(importTypeCode) {
		switch(importTypeCode) {
			case "illumina-depot-reagents-reception": return "Réception réactifs dépôt Illumina";
			case "nanopore-reagents-reception": return "Réception réactifs Nanopore";
			default: return importTypeCode;
		}
	};

	// A suppprimer après création des types reagents en description
	$scope.typeLabel = function(typeCode) {
		switch(typeCode) {
			case "illumina-depot-reagent": return "Réactif Dépôt Illumina";
			case "nanopore-reagent": return "Réactif Nanopore";
			default: return typeCode;
		}
	};

	$scope.getStorageConditions = reagentReceptionsSearchService.getStorageConditions;

	var loadReagentReception = function(){
		var promise = getReagentReceptionPromise();
		return promise.then(setReagentReceptionInScope);

		//---

		function setReagentReceptionInScope(response){
			$scope.reagentReception = response.data;
			return response;
		}

		function getReagentReceptionPromise(){
			var url = getReagentReceptionUrl();
			return $http.get(url);
		}

		function getReagentReceptionUrl(){
			var reagentReceptionCode = $routeParams.code;
			return jsRoutes.controllers.reagents.api.Receptions.get(reagentReceptionCode).url;
		}
	};

	var saveInProgress = false;
	var init = function(){
		setMiscellaneousInScope();
		initEditMode();
		loadReagentReception().then(initTabs);

		//---

		function setMiscellaneousInScope(){
			$scope.messages = messages();
			$scope.lists = lists;
			$scope.mainService = mainService;
		}

		function initEditMode(){
			mainService.stopEditMode();
		}

		function initTabs(){
			if(isExistingTabs()) return;
			addTabs();
			setDefaultTab();
			
			//---
			
			function isExistingTabs(){
				return tabService.getTabs().length > 0;
			}

			function addTabs(){
				getTabsDefinitions().forEach(addTab);

				//---

				function addTab(tab){
					tabService.addTabs(tab);
				}
			}

			function getTabsDefinitions(){
				return [
					getSearchTabDefinition(),
					getReagentReceptionTabDefinition()
				];

				//---

				function getSearchTabDefinition(){
					return {
						label:Messages('reagent.receptions.page.tab.search'),
						href:jsRoutes.controllers.reagents.tpl.Receptions.home("search").url,
						remove:true
					};
				}

				function getReagentReceptionTabDefinition(){
					return {
						label:$scope.reagentReception.code,
						href:jsRoutes.controllers.reagents.tpl.Receptions.get($scope.reagentReception.code).url,
						remove:true
					};
				}

			}

			function setDefaultTab(){
				var defaultTab = $scope.getTabs(1);
				tabService.activeTab(defaultTab);
			}
		}
	};
	init();
	
}]);